// Die Ratsche ist eine Wache: das alte tlReactControls.css ist mit Welle 5d gelöscht. Was bleibt,
// ist der Beweis, dass es gelöscht bleibt, und der Schutz des Namensraums `--tl`.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync, existsSync } from 'node:fs';
import { join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { measureBlocks, blocks, foreignTlInSources, foreignOf } from '../tools/inventory.mjs';
import { knownVariables } from '../tools/lint-css.mjs';

// Das Modul liegt im Engine-Repo: zwei Ebenen höher ist die Repo-Wurzel (TL_WORKTREE übersteuert sie).
const MODULE = join(resolve(process.env.TL_WORKTREE ?? fileURLToPath(new URL('../../', import.meta.url))), 'com.top_logic.layout.react');
const OLD = join(MODULE, 'src/main/webapp/style/tlReactControls.css');
const SOURCES = join(MODULE, 'react-src');
const SHEETS = join(MODULE, 'src/main/webapp/style');
const budget = JSON.parse(readFileSync(new URL('./ratchet.json', import.meta.url)));

// Das Fehlen der Datei ist der Sollzustand; legt jemand sie wieder an, bricht dieser Test — neue Regeln gehören ins
// Paket. Fehlt das Modul selbst, scheitert der Lauf, statt zu überspringen.
test('das Engine-Modul com.top_logic.layout.react liegt in der Repo-Wurzel (Pfad der Gates)', () => {
  assert.ok(existsSync(MODULE), `${MODULE} fehlt — Repo-Wurzel falsch? (TL_WORKTREE)`);
});

test('tlReactControls.css existiert nicht', () => {
  assert.ok(!existsSync(OLD), 'tlReactControls.css ist gelöscht (Welle 5d); neue Regeln gehören als Komponente ins Paket, nicht in ein altes Stylesheet');
  assert.deepEqual(budget.blocks, {}, 'ratchet.json kennt keinen Block mehr');
});

test('die Blockzerlegung kennt Kopf, Überschriften und doppelte Namen', () => {
  const css = '/* Kopf */\n.a{}\n/* --- Eins ----- */\n.b{}\n.c{}\n/* --- Zwei (mit Klammer). -- */\n.d{}\n/* --- Eins --- */\n.e{}\n';
  assert.deepEqual(blocks(css).map((b) => [b.name, b.lines]),
    [['Kopf', 2], ['Eins', 3], ['Zwei (mit Klammer).', 2], ['Eins (2)', 2]]);
  const m = measureBlocks(css, ['Eins', 'Drei']);
  assert.equal(m.lines, 3);
  assert.deepEqual(m.missing, ['Drei']);
  assert.deepEqual(m.unlisted, ['Kopf', 'Zwei (mit Klammer).', 'Eins (2)']);
});

// Der Namensraum `--tl` gehört dem Design System. Lesen ist erlaubt — dafür sind Tokens da;
// fremd ist nur, was der Altbestand selbst im Namensraum definiert oder was kein Paket-Token
// ist. Das steht als Einfang-Liste im Budget: Neues bricht, Eingefangenes wird gestrichen.
const DIST = new URL('../dist/', import.meta.url);
const KNOWN = knownVariables(
  readFileSync(new URL('primitives.css', DIST), 'utf8')
  + readFileSync(new URL('tokens.css', DIST), 'utf8')
  + readFileSync(new URL('base.css', DIST), 'utf8'),
);
// Gelesen wird, was die Engine selbst schreibt: react-src und die Stylesheets des Moduls.
const foreignTl = () => foreignOf([...foreignTlInSources(SOURCES), ...foreignTlInSources(SHEETS)], KNOWN);

test('der Altbestand greift nicht neu in den Namensraum --tl', () => {
  const neu = foreignTl().filter((n) => !budget.foreignTl.includes(n));
  assert.deepEqual(neu, [], `Neu im Namensraum --tl außerhalb des Pakets: ${neu.join(', ')} — eintragen in foreignTl oder umbenennen`);
});

test('die Einfang-Liste ist nachgezogen, wenn ein Name eingefangen ist', () => {
  const ist = foreignTl();
  const weg = budget.foreignTl.filter((n) => !ist.includes(n));
  assert.deepEqual(weg, [], `Aus foreignTl streichen: ${weg.join(', ')}`);
});

test('foreignOf lässt ein Paket-Token durch, das der Altbestand nur liest, und fängt ein eigenes --tl ein', () => {
  const known = new Set(['--tl-surface-layer']);
  const namen = ['--tl-surface-layer', '--tl-eigenes-ding'];
  assert.deepEqual(foreignOf(namen, known), ['--tl-eigenes-ding']);
});
