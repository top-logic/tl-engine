import { test } from 'node:test';
import assert from 'node:assert/strict';
import { deltaE, rotGruen } from '../tools/lib/cvd.mjs';

test('ΔE stimmt mit dem Prüfer überein', () => {
  const nah = (ist, soll) => assert.ok(Math.abs(ist - soll) < 0.02, `${ist} statt ${soll}`);
  nah(deltaE('#A45502', '#5B7400'), 14.35);
  nah(deltaE('#A45502', '#5B7400', 'protan'), 4.67);
  nah(deltaE('#A45502', '#5B7400', 'deutan'), 2.36);
  nah(rotGruen('#6760B5', '#8756A6'), 2.53);
  nah(deltaE('#FFFFFF', '#000000'), 100);
});

test('ΔE ist symmetrisch und null für gleiche Farben', () => {
  assert.equal(deltaE('#0B5ED7', '#0B5ED7', 'deutan'), 0);
  assert.equal(deltaE('#0B5ED7', '#E8541E'), deltaE('#E8541E', '#0B5ED7'));
});
