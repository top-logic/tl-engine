// Ein Chrome ohne Fenster, gesteuert über das DevTools-Protokoll (CDP) — ohne npm-Abhängigkeit: Node bringt
// WebSocket mit. Gebraucht von Tests, die das Specimen messen statt sein CSS zu lesen (focus-clip.test.mjs).
// Der Browser ist Teil des Gates: fehlt er, scheitert der Test, statt still zu bestehen. TL_CHROME nennt ihn,
// wo er nicht an einer der üblichen Stellen liegt.
import { spawn } from 'node:child_process';
import { existsSync, mkdtempSync, readFileSync, rmSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { setTimeout as sleep } from 'node:timers/promises';

const CANDIDATES = [
  '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',
  '/Applications/Chromium.app/Contents/MacOS/Chromium',
  '/usr/bin/google-chrome', '/usr/bin/google-chrome-stable', '/usr/bin/chromium', '/usr/bin/chromium-browser',
];

export function findChrome() {
  const found = [process.env.TL_CHROME, ...CANDIDATES].find((p) => p && existsSync(p));
  if (!found) throw new Error(`Kein Chrome gefunden — TL_CHROME setzen (gesucht: ${CANDIDATES.join(', ')})`);
  return found;
}

/** Startet Chrome und öffnet eine Seite. Liefert { goto(url), evaluate(ausdruck), close() }. */
export async function launch() {
  const profile = mkdtempSync(join(tmpdir(), 'tl-chrome-'));
  const proc = spawn(findChrome(), ['--headless=new', '--remote-debugging-port=0', `--user-data-dir=${profile}`,
    '--no-first-run', '--no-default-browser-check', 'about:blank'],
    // Eigene Prozessgruppe: close() wartet auf sie, nicht nur auf den Hauptprozess.
    { stdio: 'ignore', detached: process.platform !== 'win32' });
  // Chrome schreibt Port und Pfad des Browser-Sockets in DevToolsActivePort, sobald er lauscht.
  const portFile = join(profile, 'DevToolsActivePort');
  let endpoint;
  for (let i = 0; i < 200 && !endpoint; i++) {
    if (existsSync(portFile)) {
      const [port, path] = readFileSync(portFile, 'utf8').trim().split('\n');
      if (path) endpoint = `ws://127.0.0.1:${port}${path}`;
    }
    if (!endpoint) await sleep(50);
  }
  if (!endpoint) { proc.kill(); throw new Error('Chrome meldet keinen DevTools-Port'); }

  const ws = new WebSocket(endpoint);
  await new Promise((resolve, reject) => { ws.addEventListener('open', resolve); ws.addEventListener('error', reject); });
  let next = 0;
  const pending = new Map();
  const listeners = new Set();
  ws.addEventListener('message', (e) => {
    const m = JSON.parse(e.data);
    if (m.id && pending.has(m.id)) {
      const { resolve, reject } = pending.get(m.id);
      pending.delete(m.id);
      m.error ? reject(new Error(`${m.error.message} (${m.error.code})`)) : resolve(m.result);
    } else for (const l of listeners) l(m);
  });
  const send = (method, params = {}, sessionId) => new Promise((resolve, reject) => {
    const id = ++next;
    pending.set(id, { resolve, reject });
    ws.send(JSON.stringify({ id, method, params, sessionId }));
  });

  const { targetId } = await send('Target.createTarget', { url: 'about:blank' });
  const { sessionId } = await send('Target.attachToTarget', { targetId, flatten: true });
  await send('Page.enable', {}, sessionId);

  return {
    async goto(url) {
      const loaded = new Promise((resolve) => {
        const l = (m) => { if (m.sessionId === sessionId && m.method === 'Page.loadEventFired') { listeners.delete(l); resolve(); } };
        listeners.add(l);
      });
      await send('Page.navigate', { url }, sessionId);
      await loaded;
      // Schriften laden nach dem load-Ereignis fertig; vorher messen hieße, mit Ersatzschrift zu messen.
      await send('Runtime.evaluate', { expression: 'document.fonts.ready.then(() => true)', awaitPromise: true }, sessionId);
    },
    async evaluate(expression) {
      const { result, exceptionDetails } = await send('Runtime.evaluate', { expression, returnByValue: true, awaitPromise: true }, sessionId);
      if (exceptionDetails) throw new Error(exceptionDetails.exception?.description ?? exceptionDetails.text);
      return result.value;
    },
    async close() {
      ws.close();
      proc.kill();
      await new Promise((resolve) => (proc.exitCode !== null ? resolve() : proc.once('exit', resolve)));
      // Chromes Hilfsprozesse überleben den Hauptprozess um einige hundert Millisekunden und schreiben dabei
      // noch ins Profil; ein Löschen, das darauf trifft, scheitert mit ENOTEMPTY. Sie bilden mit ihm eine
      // Prozessgruppe: warten, bis sie leer ist, zuletzt abbrechen; das Löschen wiederholt, was noch auftritt.
      if (process.platform !== 'win32') {
        const alive = () => { try { process.kill(-proc.pid, 0); return true; } catch { return false; } };
        for (let i = 0; i < 100 && alive(); i++) await sleep(50);
        if (alive()) { try { process.kill(-proc.pid, 'SIGKILL'); } catch { /* schon weg */ } }
      }
      rmSync(profile, { recursive: true, force: true, maxRetries: 10, retryDelay: 100 });
    },
  };
}
