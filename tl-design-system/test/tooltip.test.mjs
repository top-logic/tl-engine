// Tooltip: reiner Text behält seine Zeilenumbrüche, HTML bricht nach seinem Markup um (#30214).
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readGenerated, declarationsOf } from './lib/component.mjs';

test('tl-tooltip__body--text behält die Zeilenumbrüche, tl-tooltip__body (HTML) nicht', () => {
  const css = readGenerated('tooltip', 'tooltip.css');
  assert.match(declarationsOf(css, '.tl-tooltip__body--text') ?? '', /white-space:\s*pre-line/);
  assert.match(declarationsOf(css, '.tl-tooltip__body') ?? '', /white-space:\s*normal/);
  // Gleiche Spezifität: die Variante gewinnt nur, weil sie hinter dem Grundelement steht.
  assert.ok(css.search(/^\.tl-tooltip__body\s*\{/m) < css.search(/^\.tl-tooltip__body--text\s*\{/m),
    '.tl-tooltip__body vor .tl-tooltip__body--text');
});
