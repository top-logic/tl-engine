import { createServer } from 'node:http';
import { existsSync } from 'node:fs';
import { readFile } from 'node:fs/promises';
import { extname, join, normalize, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const PORT = 8137;
const ROOT = new URL('..', import.meta.url).pathname;
// Das Modul liegt im Engine-Repo: zwei Ebenen höher ist die Repo-Wurzel (TL_WORKTREE übersteuert sie).
const WORKTREE = resolve(process.env.TL_WORKTREE ?? fileURLToPath(new URL('../../', import.meta.url)));
// /fonts/ liegt in zwei Modulen: Inter zuerst, dann Bootstrap Icons (style/bootstrap-icons.css, fonts/).
const FONTS = ['ext.com.inter.fonts', 'ext.com.bootstrap.icons'].map((m) => join(WORKTREE, m, 'src/main/webapp'));
// Ein falscher Pfad ließe die Schriften still ausbleiben (404, Ersatzschrift) und mit ihnen die Messungen der DOM-Tests.
for (const dir of FONTS) if (!existsSync(dir)) throw new Error(`Schriftenmodul fehlt: ${dir} — Repo-Wurzel falsch? (TL_WORKTREE)`);
const TYPES = { '.html': 'text/html; charset=utf-8', '.css': 'text/css; charset=utf-8', '.mjs': 'text/javascript',
  '.json': 'application/json', '.ttf': 'font/ttf', '.woff': 'font/woff', '.woff2': 'font/woff2' };

/** Der Specimen-Server, noch nicht gestartet — der DOM-Test (focus-clip.test.mjs) startet ihn auf Port 0. */
export const specimenServer = () => createServer(async (req, res) => {
  const p = normalize(decodeURIComponent(new URL(req.url, 'http://x').pathname));
  const files = p.startsWith('/fonts/') ? FONTS.map((f) => join(f, p.slice(7))) : [join(ROOT, p === '/' ? 'specimen/index.html' : p)];
  for (const file of files) {
    try { const data = await readFile(file); res.writeHead(200, { 'content-type': TYPES[extname(file)] ?? 'application/octet-stream' }); return res.end(data); }
    catch { /* nächstes Modul */ }
  }
  res.writeHead(404).end(`Nicht gefunden: ${p}`);
});

if (process.argv[1] === fileURLToPath(import.meta.url)) {
  specimenServer().listen(PORT, () => console.log(`Specimen: http://localhost:${PORT}/`));
}
