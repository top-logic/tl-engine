// Startet die Tests des Pakets: ohne Argument alle ohne Browser (das ist `npm test`, im Maven-Build), mit --dom
// genau die, die einen Browser brauchen (`npm run test:dom`). Ein Test braucht einen Browser, wenn er
// test/lib/browser.mjs einbindet — so landet ein neuer DOM-Test von selbst im richtigen Lauf. Eine Dateiliste
// statt Muster und Globs, damit es auf Node 22 und 24 gleich geht (der Glob von `node --test` fehlt in 20,
// --test-skip-pattern filtert nur nach Testnamen).
import { spawnSync } from 'node:child_process';
import { readdirSync, readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';

const dom = process.argv.includes('--dom');
const dir = new URL('../test/', import.meta.url);
const files = readdirSync(dir).filter((f) => f.endsWith('.test.mjs')).sort()
  .filter((f) => /lib\/browser\.mjs/.test(readFileSync(new URL(f, dir), 'utf8')) === dom)
  .map((f) => fileURLToPath(new URL(f, dir)));
if (files.length === 0) throw new Error('Keine Testdateien gefunden');
const result = spawnSync(process.execPath, ['--test', ...files], { stdio: 'inherit' });
process.exit(result.status ?? 1);
