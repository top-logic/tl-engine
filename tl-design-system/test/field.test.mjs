import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync, readdirSync } from 'node:fs';
import { resolve } from '../tools/resolve.mjs';
import { readComponent, colorVarsOf, declarationsOf, specimenProblems, missingInContract, varsOf } from './lib/component.mjs';

const P = JSON.parse(readFileSync(new URL('../src/primitives.json', import.meta.url)));
const T = JSON.parse(readFileSync(new URL('../src/tokens.json', import.meta.url)));
const css = readComponent('field', 'field.css');

test('field.css liest genau die Farbrollen des Feldes', () => {
  assert.deepEqual(colorVarsOf(css), [
    'border-control', 'border-disabled', 'border-emphasis', 'icon-secondary',
    'status-error-border', 'status-warning-border', 'surface-disabled', 'surface-field',
    'surface-interactive-active', 'surface-interactive-strong', 'text-disabled', 'text-link', 'text-link-hover',
    'text-placeholder', 'text-primary',
  ]);
});

test('der Rahmen folgt der Dichte und hat keine Schriftregel', () => {
  const d = declarationsOf(css, '.tl-field');
  assert.ok(d, '.tl-field fehlt');
  for (const t of ['--tl-size-control', '--tl-space-control-x', '--tl-radius-sm', '--tl-border-width-sm'])
    assert.ok(d.includes(t), `.tl-field liest ${t} nicht`);
  assert.doesNotMatch(css, /font(-size|-weight)?\s*:|line-height\s*:/);
});

test('Fehler liest aria-invalid und data-tl-state, Warnung data-tl-state, Deaktiviert :disabled', () => {
  assert.match(declarationsOf(css, '.tl-field[aria-invalid="true"]') ?? '', /status-error-border/);
  assert.match(declarationsOf(css, '.tl-field[data-tl-state~="error"]') ?? '', /status-error-border/);
  assert.match(declarationsOf(css, '.tl-field[data-tl-state~="warning"]') ?? '', /status-warning-border/);
  assert.match(declarationsOf(css, '.tl-field:disabled') ?? '', /surface-disabled/);
  // Ohne Kommentare vergleichen — der Kopfkommentar nennt die Warnung schon, vor jeder Regel.
  const regeln = css.replace(/\/\*[\s\S]*?\*\//g, '');
  const warnung = regeln.indexOf('data-tl-state~="warning"');
  const fehler = regeln.indexOf('aria-invalid="true"');
  assert.ok(warnung >= 0, 'data-tl-state~="warning" fehlt');
  assert.ok(fehler >= 0, 'aria-invalid="true" fehlt');
  assert.ok(warnung < fehler, 'Warnung muss vor dem Fehler stehen, damit der Fehler gewinnt');
});

test('der Lesewert endet mit Ellipse und lässt seine Kinder schrumpfen', () => {
  assert.match(declarationsOf(css, '.tl-field-value') ?? '', /min-width:\s*0/);
  assert.match(declarationsOf(css, '.tl-field-value__text') ?? '', /text-overflow:\s*ellipsis/);
  assert.match(declarationsOf(css, '.tl-field-value > *') ?? '', /min-width:\s*0/);
});

test('der mehrzeilige Lesewert stellt seine Zeilen untereinander, der Link-Lesewert ist unterstrichen', () => {
  assert.match(declarationsOf(css, '.tl-field-value--multiline') ?? '', /flex-direction:\s*column/);
  assert.match(declarationsOf(css, 'a.tl-field-value') ?? '', /text-decoration:\s*underline/);
});

test('der mehrzeilige Lesewert bricht um: pre-wrap am Block, overflow-wrap am Zeilentext', () => {
  assert.match(declarationsOf(css, '.tl-field-value--multiline') ?? '', /white-space:\s*pre-wrap/);
  assert.match(declarationsOf(css, '.tl-field-value--multiline > .tl-field-value__text') ?? '', /overflow-wrap:\s*anywhere/);
});

test('ein Chip passt ins Feld: size-control-sm plus zwei Ränder ist höchstens size-control, in jeder Dichte', () => {
  const R = resolve(P, T);
  const px = (v) => (v.endsWith('rem') ? parseFloat(v) * 16 : v.endsWith('px') ? parseFloat(v) : NaN);
  const rand = px(R.measure['border-width-sm']);
  assert.ok(Number.isFinite(rand), 'border-width-sm nicht in px auflösbar');
  for (const [dichte, d] of Object.entries(R.density)) {
    const feld = px(d['size-control']);
    const chip = px(d['size-control-sm']);
    assert.ok(Number.isFinite(feld) && Number.isFinite(chip), `${dichte}: Maße nicht auflösbar`);
    assert.ok(chip + 2 * rand <= feld, `${dichte}: Chip ${chip}px + 2 × ${rand}px Rand > Feld ${feld}px`);
  }
});

test('Felder halten size-control: space-control-y liest nur das mehrzeilige Feld', () => {
  const regeln = css.replace(/\/\*[\s\S]*?\*\//g, '');
  const leser = [...regeln.matchAll(/([^{}]+)\{([^}]*)\}/g)]
    .filter((m) => m[2].includes('--tl-space-control-y')).map((m) => m[1].trim());
  assert.deepEqual(leser, ['.tl-field--multiline']);
  assert.doesNotMatch(declarationsOf(css, '.tl-field') ?? '', /padding-block/);
  const select = readComponent('select', 'select.css');
  assert.match(declarationsOf(select, '.tl-select') ?? '',
    /padding-block:\s*calc\(\(var\(--tl-size-control\) - var\(--tl-size-control-sm\)\) \/ 2 - var\(--tl-border-width-sm\)\)/);
  const form = readComponent('form', 'form.css');
  assert.match(declarationsOf(form, '.tl-form-field__input > .tl-field-value') ?? '', /min-height:\s*var\(--tl-size-control\)/);
});

test('tl-visually-hidden steht in base.css, ohne Maß-Literal', () => {
  const base = readFileSync(new URL('../dist/base.css', import.meta.url), 'utf8');
  const d = declarationsOf(base, '.tl-visually-hidden');
  assert.ok(d, '.tl-visually-hidden fehlt in base.css');
  assert.match(d, /clip-path:\s*inset\(50%\)/);
  assert.match(d, /position:\s*absolute/);
  assert.match(d, /width:\s*var\(--tl-size-hidden\)/);
});

test('das Feld-Specimen zeigt jeden Zustand, jede Form, beide Flächen und eine schmale Zelle', () => {
  const html = readComponent('field', 'field.spec.html');
  assert.deepEqual(specimenProblems(html), []);
  for (const s of ['aria-invalid="true"', 'data-tl-state="warning"', 'disabled', 'placeholder=', 'tl-field--multiline',
    'tl-field-group__icon', 'tl-button--icon', 'tl-field-value', 'tl-field-value--multiline', 'tl-pill', 'specimen__cell', 'surface-layer', 'surface-overlay'])
    assert.ok(html.includes(s), `Specimen zeigt ${s} nicht`);
});

test('der Feld-Vertrag nennt jedes Token, jede Klasse und jedes Zustandsattribut', () => {
  const md = readComponent('field', 'field.contract.md');
  const names = [...varsOf(css), 'tl-field', 'tl-field--multiline', 'tl-field-group', 'tl-field-value',
    'tl-field-value--multiline', 'aria-invalid', 'data-tl-state', 'disabled', 'aria-required', 'hasError', 'hasWarnings', 'editable'];
  assert.deepEqual(missingInContract(md, names), []);
});

test('die Aktion in der Gruppe ist ein tl-button--icon: tl-field-group__action gibt es in src/ nicht mehr', () => {
  const src = new URL('../src/', import.meta.url);
  const treffer = readdirSync(src, { recursive: true }).filter((f) => /\.(css|html|md|json)$/.test(f))
    .filter((f) => readFileSync(new URL(f, src), 'utf8').includes('tl-field-group__action'));
  assert.deepEqual(treffer, []);
  assert.match(declarationsOf(css, '.tl-field-group > .tl-button') ?? '', /inset-inline-end:\s*var\(--tl-space-2xs\)/);
  assert.match(declarationsOf(css, '.tl-field-group:has(> .tl-button) > .tl-field') ?? '', /padding-inline-end:[^;]*var\(--tl-size-control-sm\)/);
  const md = readComponent('field', 'field.contract.md');
  assert.deepEqual(missingInContract(md, ['tl-button--icon']), []);
});

test('Knöpfe im Feld (06.10.): Endknopf und Kalenderknopf zeigen selected und drücken active', () => {
  assert.match(declarationsOf(css, '.tl-field-group > .tl-button--ghost:not(:disabled):hover') ?? '', /background:\s*var\(--tl-surface-interactive-strong\)/);
  assert.match(declarationsOf(css, '.tl-field-group > .tl-button--ghost:not(:disabled):active') ?? '', /background:\s*var\(--tl-surface-interactive-active\)/);
  const knopf = declarationsOf(css, '.tl-field::-webkit-calendar-picker-indicator') ?? '';
  for (const r of [/width:\s*var\(--tl-size-control-sm\)/, /height:\s*var\(--tl-size-control-sm\)/,
    /border-radius:\s*var\(--tl-radius-sm\)/, /cursor:\s*pointer/, /padding:\s*calc\(\(var\(--tl-size-control-sm\) - var\(--tl-size-icon-sm\)\) \/ 2\)/])
    assert.match(knopf, r);
  assert.match(declarationsOf(css, '.tl-field:enabled::-webkit-calendar-picker-indicator:hover') ?? '', /background-color:\s*var\(--tl-surface-interactive-strong\)/);
  assert.match(declarationsOf(css, '.tl-field:enabled::-webkit-calendar-picker-indicator:active') ?? '', /background-color:\s*var\(--tl-surface-interactive-active\)/);
  assert.match(declarationsOf(css, '.tl-field:disabled::-webkit-calendar-picker-indicator') ?? '', /cursor:\s*default/);
});

test('der Kalenderknopf steht in eigenen Regeln und lässt das native Icon stehen', () => {
  // Ein unbekanntes Pseudo-Element verwirft die ganze Selektorliste (Firefox); die Kurzform background
  // löschte das Icon, das Chromium als Hintergrundbild malt.
  const regeln = [...css.replace(/\/\*[\s\S]*?\*\//g, '').matchAll(/([^{}]+)\{([^}]*)\}/g)]
    .filter((m) => m[1].includes('::-webkit-'));
  assert.ok(regeln.length >= 4, 'Kalenderknopf fehlt');
  for (const [, sel, block] of regeln) {
    assert.ok(!sel.includes(','), `${sel.trim()} teilt die Regel mit anderen Selektoren`);
    assert.doesNotMatch(block, /(^|[\s;])background\s*:/, `${sel.trim()} setzt die Kurzform background`);
  }
});

// Ein Suchfeld mit eigenem Löschknopf (Filterleiste der Tabelle) zeigte in Chromium und WebKit daneben das
// Löschen des Browsers: zwei Kreuze an einem Feld.
// Nur ein Löschknopf (button) ersetzt das Löschen des Browsers; ein Öffnen-Link (a.tl-button) nicht.
test('ein Suchfeld mit Löschknopf zeigt das Löschen des Browsers nicht, ein Öffnen-Link lässt es stehen', () => {
  assert.match(declarationsOf(css, '.tl-field-group:has(> button.tl-button) > .tl-field::-webkit-search-cancel-button') ?? '',
    /appearance:\s*none/);
  assert.equal(declarationsOf(css, '.tl-field-group:has(> .tl-button) > .tl-field::-webkit-search-cancel-button'), null);
});

test('der Lesewert erbt die Schrift vom Ort: keine Typo-Klasse am tl-field-value, der Ort trägt sie (09.10.)', () => {
  for (const name of ['field', 'select']) {
    const html = readFileSync(new URL(`../src/components/${name}/${name}.spec.html`, import.meta.url), 'utf8');
    assert.doesNotMatch(html, /class="tl-field-value[^"]*\btl-type-/, `${name}.spec.html: Lesewert mit eigener Typo-Klasse`);
  }
  const field = readFileSync(new URL('../src/components/field/field.contract.md', import.meta.url), 'utf8');
  assert.match(field, /setzt keine Typo-Klasse/);
  const form = readFileSync(new URL('../src/components/form/form.contract.md', import.meta.url), 'utf8');
  assert.match(form, /`tl-form-field__input` \| der Platz der Eingabe; trägt `tl-type-body`/);
  const table = readFileSync(new URL('../src/components/table/table.contract.md', import.meta.url), 'utf8');
  assert.match(table, /jede ihrer Zellen\nträgt `tl-type-body-strong`/);
});
