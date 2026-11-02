import { test } from 'node:test';
import assert from 'node:assert/strict';
import { familie, stufe, primitiveAbleiten } from '../tools/lib/naming.mjs';

test('Familien treffen die Töne, die das System tatsächlich führt', () => {
  assert.equal(familie('#0B5ED7'), 'blue');   // Marke
  assert.equal(familie('#007F92'), 'teal');   // Info neu
  assert.equal(familie('#D7352D'), 'red');    // Fehler
  assert.equal(familie('#936E00'), 'amber');  // Warnung — Gold, nicht Grün
  assert.equal(familie('#008459'), 'green');  // Erfolg
  assert.equal(familie('#FAFAFA'), 'gray');   // Grundfläche
});

test('eine höhere Stufe ist dunkler', () => {
  assert.ok(stufe('#16181B') > stufe('#FFFFFF'));
  assert.equal(stufe('#FFFFFF'), 0);
});

test('kein Name wird zweimal vergeben', () => {
  const werte = ['#22262A', '#242729', '#34383C', '#343A3F', '#F0F2F6', '#F0F3F9', '#F0F4F9'];
  const namen = Object.values(primitiveAbleiten(werte));
  assert.equal(new Set(namen).size, namen.length, `doppelt: ${namen}`);
});

test('die Zuordnung hängt nicht an der Eingabereihenfolge', () => {
  const werte = ['#0B5ED7', '#004DBA', '#5394FF', '#DFEBFF'];
  assert.deepEqual(primitiveAbleiten(werte), primitiveAbleiten([...werte].reverse()));
});
