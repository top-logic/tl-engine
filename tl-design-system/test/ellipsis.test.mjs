// Ellipse vollständig oder gar nicht — docs/skalen.md „Gleiches wird zusammengelegt“. text-overflow wirkt
// nur, wenn der Block selbst abschneidet (overflow: hidden) und nicht umbricht (white-space: nowrap).
// Geerbtes nowrap zählt nicht: wer den Elternblock ändert, macht die Ellipse sonst stumm tot.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { componentCssFiles, rulesOf } from './lib/component.mjs';

test('jeder Block mit text-overflow: ellipsis setzt overflow: hidden und white-space: nowrap selbst', () => {
  const offen = [];
  for (const [name, css] of componentCssFiles()) {
    for (const [sel, d] of rulesOf(css)) {
      if (!/text-overflow\s*:\s*ellipsis/.test(d)) continue;
      const fehlt = [];
      if (!/(^|;)\s*overflow\s*:\s*hidden\b/.test(d)) fehlt.push('overflow: hidden');
      if (!/(^|;)\s*white-space\s*:\s*nowrap\b/.test(d)) fehlt.push('white-space: nowrap');
      if (fehlt.length) offen.push(`${name}: ${sel} — fehlt ${fehlt.join(', ')}`);
    }
  }
  assert.deepEqual(offen, []);
});

test('das Formularlabel bricht um statt abzuschneiden', () => {
  const [[, css]] = [...componentCssFiles()].filter(([n]) => n === 'form/form.css');
  const d = rulesOf(css).find(([s]) => s === '.tl-form-field__label-text')?.[1] ?? '';
  assert.match(d, /min-width\s*:\s*0/);
  assert.doesNotMatch(d, /text-overflow|overflow\s*:|white-space\s*:\s*nowrap/);
});
