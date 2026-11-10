// Welle 5a (Spec 2026-10-17): Eingaben aus dem Paket. Jede Regel hier steht in der Spec; die Verträge prüft
// contracts-5a.test.mjs.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { readGenerated, declarationsOf, rulesOf } from './lib/component.mjs';

const T = JSON.parse(readFileSync(new URL('../src/tokens.json', import.meta.url)));
const decl = (dir, sel, file = `${dir}.css`) => declarationsOf(readGenerated(dir, file), sel) ?? '';
const ohneKommentar = (css) => css.replace(/\/\*[\s\S]*?\*\//g, '');

test('swatch: Maß size-control, Rahmen border-control, keine px', () => {
  const d = decl('swatch', '.tl-swatch');
  assert.match(d, /inline-size:\s*var\(--tl-size-control\)/);
  assert.match(d, /block-size:\s*var\(--tl-size-control\)/);
  assert.match(d, /var\(--tl-border-control\)/);
  assert.doesNotMatch(ohneKommentar(readGenerated('swatch', 'swatch.css')), /\d+px|#[0-9a-f]{3,6}\b/i);
});

test('swatch: keine Farbe ist ein Schachbrett aus Tokens, nur am Farb-Swatch', () => {
  const d = decl('swatch', '.tl-swatch--color[data-tl-state~="empty"]');
  assert.match(d, /--tl-surface-field/);
  assert.match(d, /--tl-border-separator/);
  assert.match(d, /--tl-space-xs/);
  assert.equal(decl('swatch', '.tl-swatch[data-tl-state~="empty"]'), '');
});

test('swatch: Fehler und Warnung am Rahmen', () => {
  assert.match(decl('swatch', '.tl-swatch[aria-invalid="true"]'), /--tl-status-error-border/);
  assert.match(decl('swatch', '.tl-swatch[data-tl-state~="warning"]'), /--tl-status-warning-border/);
});

test('picker: Hülle liegt auf tl-popover, keine feste Breite', () => {
  const d = decl('picker', '.tl-picker');
  assert.notEqual(d, '');
  assert.doesNotMatch(d, /(^|[;\s])(width|inline-size)\s*:/);
  assert.match(readFileSync(new URL('../src/components/picker/picker.contract.md', import.meta.url), 'utf8'), /tl-popover/);
});

test('tokens: Griff-Rollen in beiden Modi gleich', () => {
  for (const r of ['border-handle-light', 'border-handle-dark']) {
    assert.ok(T.color.light[r]);
    assert.equal(T.color.light[r], T.color.dark[r]);
  }
});

test('swatch: die Warnung steht vor dem Fehler, damit der Fehler sie verdrängt', () => {
  const css = ohneKommentar(readGenerated('swatch', 'swatch.css'));
  assert.ok(css.indexOf('.tl-swatch[data-tl-state~="warning"]') < css.indexOf('.tl-swatch[aria-invalid="true"]'));
});

test('swatch: deaktiviert zeichnet den Rahmen wie tl-field:disabled', () => {
  const d = decl('swatch', 'button.tl-swatch:disabled');
  assert.match(d, /border-color:\s*var\(--tl-border-disabled\)/);
  assert.match(d, /cursor:\s*default/);
  assert.match(d, /background-color:\s*var\(--tl-surface-disabled\)/);
  assert.match(d, /color:\s*var\(--tl-text-disabled\)/);
});

test('picker: picker.css hat keine eigene [hidden]-Regel für Reiter und Knopfleiste', () => {
  const css = ohneKommentar(readGenerated('picker', 'picker.css'));
  assert.doesNotMatch(css, /\.tl-picker__tabs\[hidden\]|\.tl-picker__actions\[hidden\]/);
});

test('picker-spec: Reiter in tl-type-body, Knopfleiste Leeren · Abbrechen · OK', () => {
  const html = readFileSync(new URL('../src/components/picker/picker.spec.html', import.meta.url), 'utf8');
  assert.match(html, /tl-tabs__tab tl-type-body/);
  const bar = html.slice(html.indexOf('tl-picker__actions'));
  const i = [bar.indexOf('Leeren'), bar.indexOf('Abbrechen'), bar.indexOf('>OK<')];
  assert.ok(i[0] > 0 && i[0] < i[1] && i[1] < i[2], 'Reihenfolge');
  assert.match(bar, /tl-button--ghost tl-button--danger[^>]*>Leeren/);
  assert.match(bar, /tl-button--primary[^>]*>OK</);
});

test('color-picker: Mischfeld fest size-12 im Quadrat, Farbtonregler size-control-sm breit und size-12 hoch, Mischer flex: none', () => {
  const field = decl('color-picker', '.tl-color-picker__field');
  assert.match(field, /(?<![-\w])inline-size:\s*var\(--tl-size-12\)/);
  assert.match(field, /(?<![-\w])block-size:\s*var\(--tl-size-12\)/);
  const hue = decl('color-picker', '.tl-color-picker__hue');
  assert.match(hue, /(?<![-\w])inline-size:\s*var\(--tl-size-control-sm\)/);
  assert.match(hue, /(?<![-\w])block-size:\s*var\(--tl-size-12\)/);
  const css = ohneKommentar(readGenerated('color-picker', 'color-picker.css'));
  assert.match(css, /\.tl-color-picker__mixer\s*\{[^}]*(?<![-\w])flex:\s*none/);
  const all = ohneKommentar(readGenerated('color-picker', 'color-picker.css')) + ohneKommentar(readGenerated('picker', 'picker.css'));
  assert.doesNotMatch(all, /size-11/, 'kein Nachgeben mehr');
  assert.doesNotMatch(all, /aspect-ratio/);
});

test('color-picker: Griff ist ein Doppelring aus den Griff-Rollen', () => {
  const d = decl('color-picker', '.tl-color-picker__handle');
  assert.match(d, /border:[^;]*var\(--tl-border-handle-dark\)/);
  assert.match(d, /box-shadow:\s*inset[^;]*var\(--tl-border-handle-light\)/);
  assert.doesNotMatch(d, /outline/);
});

test('color-picker: keine Farbe im CSS', () => {
  const css = ohneKommentar(readGenerated('color-picker', 'color-picker.css'));
  assert.doesNotMatch(css, /#[0-9a-f]{3,6}\b|rgb\(|hsl\(/i);
});

test('color-picker: die gewählte Palettenoption trägt einen Ring in border-interactive', () => {
  const d = decl('color-picker', '.tl-color-picker__palette [aria-selected="true"]');
  assert.match(d, /border:\s*var\(--tl-border-width-md\) solid var\(--tl-border-interactive\)/);
  assert.doesNotMatch(d, /outline/);
});

test('icon-picker: Zelle size-control, gewählt in surface-selected mit Ring border-selected, Popup size-13 mit resize', () => {
  const c = decl('icon-picker', '.tl-icon-picker__cell');
  assert.match(c, /inline-size:\s*var\(--tl-size-control\)/);
  assert.match(c, /block-size:\s*var\(--tl-size-control\)/);
  const gewaehlt = decl('icon-picker', '.tl-icon-picker__cell[aria-selected="true"]');
  assert.match(gewaehlt, /background:\s*var\(--tl-surface-selected\)/);
  assert.match(gewaehlt, /box-shadow:\s*inset 0 0 0 var\(--tl-border-width-md\) var\(--tl-border-selected\)/);
  const p = decl('icon-picker', '.tl-picker.tl-icon-picker');
  assert.match(p, /inline-size:\s*var\(--tl-size-13\)/);
  assert.match(p, /block-size:\s*var\(--tl-size-13\)/);
  assert.match(p, /resize:\s*both/);
  assert.doesNotMatch(readGenerated('icon-picker', 'icon-picker.css'), /scrollbar/);
});

test('slider: Füllung ist ein Element, Spur des range durchsichtig, kein outline:none', () => {
  const css = ohneKommentar(readGenerated('slider', 'slider.css'));
  assert.notEqual(decl('slider', '.tl-slider__fill'), '');
  assert.doesNotMatch(css, /--tlSlider|outline:\s*none/);
  for (const s of ['.tl-slider__input::-webkit-slider-runnable-track', '.tl-slider__input::-moz-range-track'])
    assert.match(decl('slider', s), /background:\s*transparent/);
  for (const s of ['.tl-slider__input::-webkit-slider-thumb', '.tl-slider__input::-moz-range-thumb'])
    assert.match(decl('slider', s), /var\(--tl-size-choice\)/);
});

const W = '.tl-slider[data-tl-state~="warning"] .tl-slider__input:not(:disabled):not([aria-invalid="true"])';
test('slider: Zustand als Attribut, keine --error/--warning-Klasse', () => {
  const css = ohneKommentar(readGenerated('slider', 'slider.css'));
  for (const s of [W + ' ~ .tl-slider__value .tl-slider__text', W + '::-webkit-slider-thumb', W + '::-moz-range-thumb',
    '.tl-slider__input[aria-invalid="true"]:not(:disabled)::-webkit-slider-thumb',
    '.tl-slider__input[aria-invalid="true"]:not(:disabled)::-moz-range-thumb',
    '.tl-slider__input[aria-invalid="true"]:not(:disabled) ~ .tl-slider__value .tl-slider__text'])
    assert.notEqual(decl('slider', s), '', s);
  assert.doesNotMatch(css, /tl-slider--(error|warning)/);
});

test('slider: Vorrang über die Spezifität — Fehler vor Warnung, deaktiviert vor beiden', () => {
  const sel = rulesOf(readGenerated('slider', 'slider.css')).map(([s]) => s).filter((s) => /::-webkit-slider-thumb|__text/.test(s));
  for (const s of sel.filter((x) => /warning/.test(x))) assert.match(s, /:not\(\[aria-invalid="true"\]\)/, s);
  for (const s of sel.filter((x) => /warning|aria-invalid/.test(x))) assert.match(s, /:not\(:disabled\)/, s);
});

test('swatch: der Feldknopf zeigt den Hof -hover, die Option den Hof -hover-strong (skalen 4c S3)', () => {
  assert.match(decl('swatch', 'button.tl-swatch:not(:disabled):hover'), /var\(--tl-space-2xs\) var\(--tl-surface-state-hover\)/);
  assert.match(decl('swatch', '.tl-swatch[role="option"]:hover'), /var\(--tl-space-2xs\) var\(--tl-surface-state-hover-strong\)/);
});
