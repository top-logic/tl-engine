// Text und Spalten-Layout — 3b §11, §12.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { readGenerated, declarationsOf } from './lib/component.mjs';

test('tl-text: Ton als data-tl-tone, ohne Ton erbt der Text; Ellipse ist --truncate; leer nimmt keinen Platz; Zeilenumbrüche bleiben', () => {
  const css = readGenerated('text', 'text.css');
  const TOENE = { secondary: 'text-secondary', helper: 'text-helper', placeholder: 'text-placeholder',
    success: 'status-success-text', warning: 'status-warning-text', error: 'status-error-text', 'on-brand': 'text-on-brand' };
  for (const [ton, rolle] of Object.entries(TOENE))
    assert.match(declarationsOf(css, `.tl-text[data-tl-tone="${ton}"]`) ?? '', new RegExp(`color:\\s*var\\(--tl-${rolle}\\)`), ton);
  assert.doesNotMatch(declarationsOf(css, '.tl-text') ?? '', /color/, 'ohne Ton keine Farbregel: der Text erbt');
  // #29886: der Text behält die Zeilenumbrüche seines Werts; --truncate bleibt einzeilig und steht dahinter,
  // damit es bei gleicher Spezifität gewinnt.
  assert.match(declarationsOf(css, '.tl-text') ?? '', /white-space:\s*pre-line/);
  assert.match(declarationsOf(css, '.tl-text--truncate') ?? '', /white-space:\s*nowrap/);
  assert.ok(css.search(/^\.tl-text\s*\{/m) < css.search(/^\.tl-text--truncate\s*\{/m), '.tl-text vor .tl-text--truncate');
  assert.doesNotMatch(css, /accent|font-style|font-weight/);
  assert.match(declarationsOf(css, '.tl-text--truncate') ?? '', /text-overflow:\s*ellipsis/);
  // master #29752: ein Text ohne Wert nimmt keinen Platz, einer mit Rolle (Live-Bereich) bleibt.
  assert.match(declarationsOf(css, '.tl-text:empty:not([role])') ?? '', /display:\s*none/);
});

// Fix-Welle 3b (1): Stark ist die -strong-Klasse als css-class neben der Varianten-Klasse, die der Text
// selbst setzt (tl-type-body). Beide haben dieselbe Spezifität; -strong gewinnt nur, weil es in base.css
// hinter seiner Grundklasse steht. Die Gruppenzeile der Tabelle verlässt sich darauf.
test('tl-type-*-strong steht in base.css hinter seiner Grundklasse und gewinnt neben ihr', () => {
  const base = readFileSync(new URL('../dist/base.css', import.meta.url), 'utf8');
  for (const grund of ['body', 'label']) {
    const vorn = base.search(new RegExp(`^\\.tl-type-${grund}\\s*\\{`, 'm'));
    const stark = base.search(new RegExp(`^\\.tl-type-${grund}-strong\\s*\\{`, 'm'));
    assert.ok(vorn >= 0 && stark >= 0, `tl-type-${grund} und -strong in base.css`);
    assert.ok(stark > vorn, `tl-type-${grund}-strong muss hinter tl-type-${grund} stehen`);
  }
});

test('tl-columns: drei Abstände aus der Skala, Spalten schrumpfen', () => {
  const css = readGenerated('columns', 'columns.css');
  assert.match(declarationsOf(css, '.tl-columns--gap-compact') ?? '', /gap:\s*var\(--tl-space-sm\)/);
  assert.match(declarationsOf(css, '.tl-columns--gap-default') ?? '', /gap:\s*var\(--tl-space-md\)/);
  assert.match(declarationsOf(css, '.tl-columns--gap-loose') ?? '', /gap:\s*var\(--tl-space-lg\)/);
  assert.match(declarationsOf(css, '.tl-columns__column') ?? '', /min-width:\s*0/);
});
