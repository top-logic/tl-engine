import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync, readdirSync } from 'node:fs';
import { lint, knownVariables } from '../tools/lint-css.mjs';

const dist = new URL('../dist/', import.meta.url);
const known = knownVariables(readFileSync(new URL('primitives.css', dist), 'utf8') + readFileSync(new URL('tokens.css', dist), 'utf8'));

test('ein Literal in einer Klasse ist ein Verstoß', () => {
  const v = lint('.tl-x { color: #fff; padding: 12px; transition: all 0.2s; }', { known, file: 'x.css' });
  assert.equal(v.length, 3, v.join('\n'));
});

test('erlaubte Literale: 0, 1, auto, none, 100%, currentColor, inherit', () => {
  assert.deepEqual(lint('.tl-x { margin: 0; flex: 1; width: auto; border: none; max-width: 100%; color: currentColor; font: inherit; }', { known, file: 'x.css' }), []);
});

test('eine unbekannte Variable ist ein Verstoß', () => {
  assert.equal(lint('.tl-x { color: var(--tl-nope); }', { known, file: 'x.css' }).length, 1);
});

test('Klassen ohne tl-Präfix oder gegen BEM sind Verstöße', () => {
  assert.equal(lint('.button { }', { known, file: 'x.css' }).length, 1);
  assert.equal(lint('.tlButton { }', { known, file: 'x.css' }).length, 1);
  assert.equal(lint('.tl-button__icon--big__x { }', { known, file: 'x.css' }).length, 1);
  assert.deepEqual(lint('.tl-button__icon--large { }', { known, file: 'x.css' }), []);
});

test(':focus ohne -visible und outline: none sind Verstöße', () => {
  assert.equal(lint('.tl-x:focus { outline: none; }', { known, file: 'x.css' }).length, 2);
});

test('font-size gehört in eine tl-type-Klasse', () => {
  assert.equal(lint('.tl-x { font-size: var(--tl-font-size-body); }', { known, file: 'x.css' }).length, 1);
  assert.deepEqual(lint('.tl-type-body { font-size: var(--tl-font-size-body); }', { known, file: 'base.css' }), []);
});

test('z-index, box-shadow und Dauern nur aus Tokens', () => {
  assert.equal(lint('.tl-x { z-index: 10; }', { known, file: 'x.css' }).length, 1);
  assert.ok(lint('.tl-x { box-shadow: 0 1px 2px var(--tl-surface-base); }', { known, file: 'x.css' }).length >= 1);
  assert.deepEqual(lint('.tl-x { box-shadow: inset 0 0 0 var(--tl-border-width-md) var(--tl-border-control); }', { known, file: 'x.css' }), []);
  assert.deepEqual(lint('.tl-x { z-index: var(--tl-layer-popover); box-shadow: var(--tl-shadow-popover); transition: color var(--tl-duration-fast) var(--tl-easing-standard); }', { known, file: 'x.css' }), []);
});

test('der Hof: nur Ausdehnung aus Tokens, nur in der durchscheinenden Zeigen- oder Drücken-Schicht', () => {
  const ok = (v) => lint(`.tl-x { box-shadow: ${v}; }`, { known, file: 'x.css' });
  assert.deepEqual(ok('0 0 0 calc((var(--tl-size-control) - var(--tl-size-choice)) / 2) var(--tl-surface-state-hover)'), []);
  assert.deepEqual(ok('0 0 0 var(--tl-space-xs) var(--tl-surface-state-active)'), []);
  assert.ok(ok('0 0 0 8px var(--tl-surface-state-hover)').length >= 1, 'Maß-Literal');
  assert.ok(ok('0 0 0 var(--tl-space-xs) var(--tl-focus-ring)').length >= 1, 'kein Fokusersatz');
  assert.ok(ok('0 0 2px var(--tl-space-xs) var(--tl-surface-state-hover)').length >= 1, 'keine Unschärfe');
  assert.ok(ok('0 1px 0 var(--tl-space-xs) var(--tl-surface-state-hover)').length >= 1, 'kein Versatz');
  // Der Hof ist durchscheinend: eine deckende Fläche als Hof verdeckte, was darunter liegt.
  assert.ok(ok('0 0 0 var(--tl-space-xs) var(--tl-surface-interactive-hover)').length >= 1, 'deckender Hof');
  // Die Deckkraft steht im Token, nie in der Komponente.
  assert.ok(ok('0 0 0 var(--tl-space-xs) color-mix(in srgb, var(--tl-text-primary) 8%, transparent)').length >= 1, 'color-mix');
  assert.ok(ok('0 0 0 var(--tl-space-xs) rgba(0, 0, 0, 0.08)').length >= 1, 'rgba-Literal');
});

test('die durchscheinenden Schichten surface-state-* gibt es nur als Hof', () => {
  for (const [p, v] of [['background', 'var(--tl-surface-state-hover)'], ['background-color', 'var(--tl-surface-state-active)'],
    ['border-color', 'var(--tl-surface-state-hover)'], ['color', 'var(--tl-surface-state-hover)'],
    ['box-shadow', 'inset 0 0 0 var(--tl-border-width-md) var(--tl-surface-state-hover)']])
    assert.ok(lint(`.tl-x { ${p}: ${v}; }`, { known, file: 'x.css' }).length >= 1, `${p}: ${v}`);
});

test('benannte Farben sind ein Verstoß — transparent und currentColor bleiben erlaubt', () => {
  assert.equal(lint('.tl-x { color: white; }', { known, file: 'x.css' }).length, 1);
  assert.equal(lint('.tl-x { border: 1px solid red; }', { known, file: 'x.css' }).length >= 1, true);
  assert.equal(lint('.tl-x { background: black; }', { known, file: 'x.css' }).length, 1);
  assert.deepEqual(lint('.tl-x { background: transparent; color: currentColor; }', { known, file: 'x.css' }), []);
});

test('ein Fallback im var() ist ein Verstoß', () => {
  assert.equal(lint('.tl-x { color: var(--tl-text-primary, #fff); }', { known, file: 'x.css' }).length, 1);
  assert.equal(lint('.tl-x { width: var(--tl-space-md, 12px); }', { known, file: 'x.css' }).length, 1);
  assert.deepEqual(lint('.tl-x { color: var(--tl-text-primary); }', { known, file: 'x.css' }), []);
});

test('eine fremde Variable ohne tl-Präfix ist ein Verstoß', () => {
  assert.equal(lint('.tl-x { color: var(--foo); }', { known, file: 'x.css' }).length, 1);
});

test('eine eigene Variable in einer Klasse ist ein Verstoß — Tokens gehören nach src/tokens.json', () => {
  assert.equal(lint('.tl-x { --foo: 12px; }', { known, file: 'x.css' }).length, 1);
  assert.equal(lint('.tl-x { --tl-button-gap: 4px; }', { known, file: 'x.css' }).length, 1);
  assert.deepEqual(lint(':root { --tl-button-gap: 4px; }', { known, file: 'tokens.css' }), []);
  assert.deepEqual(lint('[data-tl-mode="dark"] { --tl-button-gap: 4px; }', { known, file: 'tokens.css' }), []);
});

test('der Fokusring darf nicht per Einzeleigenschaft abgeschaltet werden', () => {
  assert.equal(lint('.tl-x:focus-visible { outline-width: 0; }', { known, file: 'x.css' }).length, 1);
  assert.equal(lint('.tl-x:focus-visible { outline-style: none; }', { known, file: 'x.css' }).length, 1);
  assert.equal(lint('.tl-x:focus-visible { outline-color: transparent; }', { known, file: 'x.css' }).length, 1);
  assert.equal(lint('.tl-x:focus-visible { outline: 0 0; }', { known, file: 'x.css' }).length, 1);
  assert.equal(lint('.tl-x:focus-visible { outline: 0px; }', { known, file: 'x.css' }).length >= 1, true);
  assert.equal(lint('.tl-x:focus-visible { outline: 0 none; }', { known, file: 'x.css' }).length >= 1, true);
  assert.equal(lint('.tl-x:focus-visible { outline: solid 0 currentColor; }', { known, file: 'x.css' }).length >= 1, true);
  assert.deepEqual(lint('.tl-x:focus-visible { outline: var(--tl-focus-width) solid var(--tl-focus-ring); }', { known, file: 'x.css' }), []);
  assert.deepEqual(lint('.tl-x:focus-visible { outline: var(--tl-focus-width) solid var(--tl-focus-ring); }', { known, file: 'x.css' }), []);
});

test('die font-Kurzform ist außerhalb einer Typo-Klasse ein Verstoß — inherit/initial/unset/normal bleiben erlaubt', () => {
  assert.equal(lint('.tl-x { font: 12px sans-serif; }', { known, file: 'x.css' }).length >= 1, true);
  assert.deepEqual(lint('.tl-x { font: inherit; }', { known, file: 'x.css' }), []);
  // In der Typo-Klasse feuert nur die font-Kurzform-Regel nicht mehr — ein Literal bleibt
  // ein Literal (Maß-Literal-Regel) und muss weiter aus Tokens kommen.
  assert.deepEqual(
    lint('.tl-type-body { font: 12px sans-serif; }', { known, file: 'base.css' })
      .filter((v) => v.includes('font-Kurzform')),
    [],
  );
});

test('line-height gehört in eine Typo-Klasse — body ist die einzige Ausnahme', () => {
  assert.equal(lint('.tl-x { line-height: 1.4; }', { known, file: 'x.css' }).length, 1);
  assert.deepEqual(lint('.tl-type-body { line-height: 1.4; }', { known, file: 'base.css' }), []);
  assert.deepEqual(lint('body { line-height: 1.4; }', { known, file: 'base.css' }), []);
});

test('!important ist nur im Reduced-Motion-Block erlaubt', () => {
  assert.equal(lint('.tl-x { color: red !important; }', { known, file: 'x.css' }).length >= 1, true);
  assert.deepEqual(lint('*, *::before, *::after { animation-duration: 0.01ms !important; }', { known, file: 'base.css' }), []);
  assert.equal(lint('.tl-x { animation-duration: 0.01ms !important; }', { known, file: 'base.css' }).length, 1);
});

test('Kurven-Literale in transition/animation sind ein Verstoß — der Token bleibt erlaubt', () => {
  assert.equal(lint('.tl-x { transition: color 0.2s ease-in-out; }', { known, file: 'x.css' }).length >= 1, true);
  assert.equal(lint('.tl-x { transition: color 0.2s linear; }', { known, file: 'x.css' }).length >= 1, true);
  assert.equal(lint('.tl-x { transition: color 0.2s cubic-bezier(0.2, 0, 1, 1); }', { known, file: 'x.css' }).length >= 1, true);
  assert.deepEqual(lint('.tl-x { transition: color var(--tl-duration-fast) var(--tl-easing-linear); }', { known, file: 'x.css' }), []);
});

test('ein Attributselektor auf Klassen ist ein Verstoß', () => {
  assert.equal(lint('[class*="tl-button"] { color: red; }', { known, file: 'x.css' }).length >= 1, true);
  assert.equal(lint('[class^="tl-"] { color: red; }', { known, file: 'x.css' }).length >= 1, true);
  assert.equal(lint('[class~="tl-x"] { color: red; }', { known, file: 'x.css' }).length >= 1, true);
});

test('einheitenlose Literale bei opacity/flex bleiben erlaubt (Punkt 10, nicht verschärft)', () => {
  assert.deepEqual(lint('.tl-x { opacity: 0.5; flex: 2; }', { known, file: 'x.css' }), []);
});

test('font-size ist in .tl-icon-* in base.css erlaubt, außerhalb nicht', () => {
  assert.deepEqual(lint('.tl-icon-sm { font-size: var(--tl-size-icon-sm); }', { known: new Set(['--tl-size-icon-sm']), file: 'base.css' }), []);
  assert.equal(lint('.tl-button__icon { font-size: var(--tl-size-icon-sm); }', { known: new Set(['--tl-size-icon-sm']), file: 'button/button.css' }).length, 1);
});

test('die erzeugten Dateien des Pakets bestehen das Gate', () => {
  for (const f of readdirSync(dist).filter((f) => f.endsWith('.css') && f !== 'primitives.css')) {
    const v = lint(readFileSync(new URL(f, dist), 'utf8'), { known, file: f });
    assert.deepEqual(v, [], `${f}:\n${v.join('\n')}`);
  }
});

test('ein Zustandsname als Modifier bricht — Zustände sind Attribute', () => {
  for (const z of ['active', 'disabled', 'loading', 'hover', 'focus', 'pressed', 'selected', 'open', 'checked', 'busy'])
    assert.equal(lint(`.tl-button--${z} { color: var(--tl-text-primary); }`, { known: new Set(['--tl-text-primary']), file: 'button/button.css' })
      .filter((m) => /Zustand als Modifier/.test(m)).length, 1, z);
});

test('am Block tl-button sind nur die entschiedenen Modifier erlaubt', () => {
  const known = new Set(['--tl-text-primary']);
  for (const ok of ['primary', 'secondary', 'ghost', 'link', 'danger', 'sm'])
    assert.deepEqual(lint(`.tl-button--${ok} { color: var(--tl-text-primary); }`, { known, file: 'button/button.css' }), [], ok);
  assert.equal(lint('.tl-button--large { color: var(--tl-text-primary); }', { known, file: 'button/button.css' })
    .filter((m) => /kein Modifier von tl-button/.test(m)).length, 1);
});

test('ein Zustand als Modifier des Feldes bricht das Gate — als Zustandswort oder als fremder Modifier', () => {
  const known = new Set(['--tl-text-primary']);
  for (const m of ['immutable', 'readonly', 'dragover'])
    assert.ok(lint(`.tl-field--${m} { color: var(--tl-text-primary); }`, { known, file: 'x.css' })
      .some((v) => v.includes('Zustand als Modifier')), m);
  for (const m of ['error', 'warning'])
    assert.ok(lint(`.tl-field--${m} { color: var(--tl-text-primary); }`, { known, file: 'x.css' })
      .some((v) => v.includes('kein Modifier von tl-field')), m);
  // Seit 3b ist die Rolle ein Attribut (data-tl-role): auch die Bedeutung an der Pill ist kein Modifier mehr.
  assert.ok(lint('.tl-pill--error { color: var(--tl-text-primary); }', { known, file: 'x.css' })
    .some((v) => v.includes('kein Modifier von tl-pill')), 'Rolle als Modifier der Pill');
  assert.deepEqual(lint('.tl-pill[data-tl-role="error"] { color: var(--tl-text-primary); }', { known, file: 'x.css' }), []);
});

test('ein Zustand als Element-Modifier ist ein Verstoß — auch Fehler und Warnung', () => {
  for (const c of ['tl-x__y--selected', 'tl-x__y--error', 'tl-form-field__message--warning']) {
    const v = lint(`.${c} { color: var(--tl-text-primary); }`, { known, file: 'x.css' });
    assert.equal(v.length, 1, v.join('\n'));
    assert.match(v[0], /Zustand als Element-Modifier/);
  }
  assert.deepEqual(lint('.tl-alert[data-tl-role="error"] { color: var(--tl-text-primary); }', { known, file: 'x.css' }), []);
  assert.deepEqual(lint('.tl-button__icon--large { }', { known, file: 'x.css' }), []);
});

test('cursor: not-allowed ist ein Verstoß — Deaktiviert zeigt den gewöhnlichen Zeiger (skalen.md „Disabled“)', () => {
  assert.equal(lint('.tl-x:disabled { cursor: not-allowed; }', { known, file: 'x.css' }).length, 1);
  assert.deepEqual(lint('.tl-x:disabled { cursor: default; }', { known, file: 'x.css' }), []);
});

test('ein Prozent-Literal ist ein Maß-Literal — 50 % zum Zentrieren und 100 % bleiben erlaubt', () => {
  assert.equal(lint('.tl-x{width:37%}', { known, file: 'x.css' }).length, 1);
  assert.equal(lint('.tl-x { inset: 0 12.5% 0 0; }', { known, file: 'x.css' }).length, 1);
  assert.deepEqual(lint('.tl-x { top: 50%; translate: 0 -50%; max-width: 100%; }', { known, file: 'x.css' }), []);
});

test('dynamische Viewport-Einheiten (dvh, dvw, svh, lvw …) sind Maß-Literale', () => {
  assert.equal(lint('.tl-x { max-height: 100dvh; }', { known, file: 'x.css' }).length, 1);
  assert.equal(lint('.tl-x { width: 50svw; }', { known, file: 'x.css' }).length, 1);
});

test('ein Pseudo-Element des Browsers fällt unter dasselbe Gate wie seine Klasse', () => {
  // Der Kalenderknopf (field.css) liegt hinter ::-webkit-calendar-picker-indicator: Literale dort brechen.
  const sel = '.tl-field:enabled::-webkit-calendar-picker-indicator:hover';
  assert.deepEqual(lint(`${sel} { background-color: var(--tl-surface-interactive-selected); }`, { known, file: 'x.css' }), []);
  assert.equal(lint(`${sel} { background-color: #eee; }`, { known, file: 'x.css' }).length, 1, 'Farbliteral');
  assert.equal(lint('.tl-field::-webkit-calendar-picker-indicator { width: 24px; }', { known, file: 'x.css' }).length, 1, 'Maß-Literal');
  assert.equal(lint('.tlField::-webkit-calendar-picker-indicator { width: auto; }', { known, file: 'x.css' }).length, 1, 'BEM');
});

// Entscheidung 07.10. (Hendrik): der Auf-/Zuklapper steht auf jedem Zeilengrund (gezeigt, ausgewählt) und zeigt
// sein Zeigen und Drücken darum als durchscheinende eigene Fläche — die eine Ausnahme der Schicht-Regel, nur
// dort und nur als background. Der Hof bleibt, wie er ist.
test('surface-state-* als Fläche gibt es genau am tl-toggle, beim Zeigen und Drücken', () => {
  const known = new Set(['--tl-surface-state-hover', '--tl-surface-state-active']);
  for (const [sel, v] of [['.tl-toggle:hover', 'hover'], ['.tl-toggle:active', 'active']])
    assert.deepEqual(lint(`${sel} { background: var(--tl-surface-state-${v}); }`, { known, file: 'x.css' }), [], sel);
  for (const css of ['.tl-toggle { background: var(--tl-surface-state-hover); }',
    '.tl-toggle:hover { color: var(--tl-surface-state-hover); }',
    '.tl-toggle:hover { border-color: var(--tl-surface-state-hover); }',
    '.tl-toggle:hover, .tl-x:hover { background: var(--tl-surface-state-hover); }',
    '.tl-button:hover { background: var(--tl-surface-state-hover); }',
    '.tl-toggle:hover { box-shadow: inset 0 0 0 var(--tl-border-width-md) var(--tl-surface-state-hover); }'])
    assert.ok(lint(css, { known, file: 'x.css' }).length >= 1, css);
});

test('outline-style: none ist nur am Behälter mit Cursor in base.css erlaubt', () => {
  const sel = ':where(:not(input, textarea, [role="combobox"])[aria-activedescendant]):focus-visible';
  assert.deepEqual(lint(`${sel} { outline-style: none; }`, { known, file: 'base.css' }), []);
  assert.equal(lint(`${sel} { outline-style: none; }`, { known, file: 'tl-table.css' }).length, 1);
  assert.equal(lint('button:focus-visible { outline-style: none; }', { known, file: 'base.css' }).length, 1);
  assert.equal(lint(`${sel} { outline: none; }`, { known, file: 'base.css' }).length, 1);
});

test('3b: Cursor, Lage, Filter und Ziehen sind Zustände, keine Modifier — an Block und Element', () => {
  const known = new Set(['--tl-text-primary']);
  for (const z of ['cursor', 'frozen', 'frozen-last', 'pinned', 'pinned-first', 'filtered', 'expanded', 'drop-before', 'drop-after', 'drop-onto', 'drop-refused']) {
    assert.equal(lint(`.tl-table--${z} { color: var(--tl-text-primary); }`, { known, file: 'x.css' })
      .filter((m) => /Zustand als Modifier/.test(m)).length, 1, `Block ${z}`);
    assert.equal(lint(`.tl-table__row--${z} { color: var(--tl-text-primary); }`, { known, file: 'x.css' })
      .filter((m) => /Zustand als Element-Modifier/.test(m)).length, 1, `Element ${z}`);
  }
  assert.deepEqual(lint('.tl-table__row[data-tl-state~="cursor"] { color: var(--tl-text-primary); }', { known, file: 'x.css' }), []);
});

test('Zustände der Overlays als Modifier brechen das Gate (Welle 4a)', () => {
  for (const sel of ['.tl-dialog--entering', '.tl-dialog__surface--maximized', '.tl-notice-bar--clickable', '.tl-snackbar--visible', '.tl-x__y--exiting'])
    assert.ok(lint(`${sel} { display: none; }`, { known, file: 'x.css' }).some((v) => /Zustand als/.test(v)), sel);
});

test('Zustände der Behälter als Modifier brechen das Gate (Welle 4c) — an Block und Element', () => {
  for (const sel of ['.tl-panel--minimized', '.tl-panel--maximized', '.tl-split__pane--collapsed', '.tl-tile-stack__frame--covered',
    '.tl-tile-stack--forward', '.tl-wizard--backward', '.tl-wizard__step-item--done', '.tl-wizard__step-item--current',
    '.tl-wizard__step-item--upcoming', '.tl-wizard__step--entering'])
    assert.ok(lint(`${sel} { display: none; }`, { known, file: 'x.css' }).some((v) => /Zustand als/.test(v)), sel);
});
