import { test } from 'node:test';
import assert from 'node:assert/strict';
import { ratio, color, AA_TEXT, AA_NON_TEXT } from '../tools/lib/contrast.mjs';

const nah = (ist, soll) => assert.ok(Math.abs(ist - soll) < 0.005,
  `${ist} weicht von ${soll} ab`);

test('Schwarz auf Weiß ist der größte Kontrast, den WCAG kennt', () => {
  nah(ratio('#FFFFFF', '#000000'), 21.0);
});

test('Eine Farbe auf sich selbst ist der kleinste', () => {
  nah(ratio('#FFFFFF', '#FFFFFF'), 1.0);
});

test('Die Reihenfolge der Farben ändert das Ergebnis nicht', () => {
  nah(ratio('#FFFFFF', '#2968C8'), 5.3730);
  nah(ratio('#2968C8', '#FFFFFF'), 5.3730);
});

test('Das Grau, das AA für normalen Text auf Weiß gerade zulässt', () => {
  nah(ratio('#767676', '#FFFFFF'), 4.5422);
});

test('Die Luminanz rechnet auf linearisierten Kanälen', () => {
  // Ein lineares (r+g+b)/3 käme hier auf einen anderen Wert.
  nah(ratio('#808080', '#FFFFFF'), 3.9494);
});

test('Ein Hexwert wird in seine Kanäle gelesen, kurz wie lang', () => {
  assert.deepEqual(color('#2968c8'), { r: 0x29, g: 0x68, b: 0xc8 });
  assert.deepEqual(color('#abc'), { r: 0xaa, g: 0xbb, b: 0xcc });
});

test('Die Schwellen sind die von WCAG 2.1 AA', () => {
  assert.equal(AA_TEXT, 4.5);
  assert.equal(AA_NON_TEXT, 3);
});
