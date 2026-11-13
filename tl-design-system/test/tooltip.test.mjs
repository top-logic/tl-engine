// Tooltip: reiner Text behält seine Zeilenumbrüche, HTML bricht nach seinem Markup um (#30214); der volle Text eines
// abgeschnittenen Elements steht abgesetzt über der Erklärung (#30292).
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readGenerated, declarationsOf } from './lib/component.mjs';
import { readFileSync } from 'node:fs';
import { resolve } from '../tools/resolve.mjs';

const P = JSON.parse(readFileSync(new URL('../src/primitives.json', import.meta.url)));
const T = JSON.parse(readFileSync(new URL('../src/tokens.json', import.meta.url)));

test('tl-tooltip__body--text behält die Zeilenumbrüche, tl-tooltip__body (HTML) nicht', () => {
  const css = readGenerated('tooltip', 'tooltip.css');
  assert.match(declarationsOf(css, '.tl-tooltip__body--text') ?? '', /white-space:\s*pre-line/);
  assert.match(declarationsOf(css, '.tl-tooltip__body') ?? '', /white-space:\s*normal/);
  // Gleiche Spezifität: die Variante gewinnt nur, weil sie hinter dem Grundelement steht.
  assert.ok(css.search(/^\.tl-tooltip__body\s*\{/m) < css.search(/^\.tl-tooltip__body--text\s*\{/m),
    '.tl-tooltip__body vor .tl-tooltip__body--text');
});

test('tl-tooltip__full-text ist unten durch eine Linie in border-inverse abgesetzt', () => {
  const css = readGenerated('tooltip', 'tooltip.css');
  assert.match(declarationsOf(css, '.tl-tooltip__full-text') ?? '',
    /border-bottom:\s*var\(--tl-border-width-sm\) solid var\(--tl-border-inverse\)/);
});

test('border-inverse ist die Trennlinie des anderen Modus: auf surface-inverse wie border-separator auf seinem Grund', () => {
  const R = resolve(P, T);
  assert.equal(R.color.light['border-inverse'], R.color.dark['border-separator']);
  assert.equal(R.color.dark['border-inverse'], R.color.light['border-separator']);
});
