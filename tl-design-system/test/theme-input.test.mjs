import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { leseThemeEingabe, ThemeEingabeFehler } from '../tools/lib/theme-input.mjs';

const P = JSON.parse(readFileSync(new URL('../src/primitives.json', import.meta.url)));
const T = JSON.parse(readFileSync(new URL('../src/tokens.json', import.meta.url)));
const fehler = (json, pfad) => assert.throws(() => leseThemeEingabe(json, P, T),
  (e) => e instanceof ThemeEingabeFehler && e.pfad === pfad);

test('Saat: Hausfarbe und Temperatur landen unter saat, nichts unter geliefert', () => {
  const e = leseThemeEingabe({ theme: 'acme', color: { brand: '#e8541e', gray: 'warm' } }, P, T);
  assert.equal(e.theme, 'acme');
  assert.deepEqual(e.saat, { brand: '#E8541E', gray: 'warm' });
  assert.deepEqual(e.geliefert, {});
});

test('Stufen: gelieferte Werte landen unter Primitivnamen, die Saat ist die Stufe 49', () => {
  const e = leseThemeEingabe({ theme: 'acme', color: { brand: { 49: '#E8541E', 55: '#C9440F' }, red: { 45: '#B00020' } },
    font: { 'font-1': 'Acme Sans' }, measure: { 'corner-1': '0' } }, P, T);
  assert.deepEqual(e.geliefert, { 'blue-49': '#E8541E', 'blue-55': '#C9440F', 'red-45': '#B00020', 'font-1': 'Acme Sans', 'corner-1': '0' });
  assert.equal(e.saat.brand, '#E8541E');
});

test('Fehler tragen den Pfad', () => {
  fehler({ color: { brand: '#E8541E' } }, 'theme');
  fehler({ theme: 'tl', color: { brand: '#E8541E' } }, 'theme');
  fehler({ theme: 'Acme', color: { brand: '#E8541E' } }, 'theme');
  fehler({ theme: 'acme', colour: {} }, 'colour');
  fehler({ theme: 'acme', color: { brand: 'orange' } }, 'color.brand');
  fehler({ theme: 'acme', color: { brand: { 55: '#C9440F' } } }, 'color.brand.49');
  fehler({ theme: 'acme', color: { brand: '#E8541E', gray: 'hot' } }, 'color.gray');
  fehler({ theme: 'acme', color: { brand: '#E8541E', red: { 99: '#B00020' } } }, 'color.red.99');
  fehler({ theme: 'acme', color: { brand: '#E8541E', category: { 'category-9': '#B00020' } } }, 'color.category.category-9');
  fehler({ theme: 'acme', measure: { 'corner-9': '0' } }, 'measure.corner-9');
});

test('Prototypkette wird nicht als Stufe akzeptiert', () => {
  fehler({ theme: 'acme', measure: { toString: '0' } }, 'measure.toString');
});

test('Wurzel muss ein Objekt sein', () => {
  fehler(null, '');
  fehler([], '');
  fehler('x', '');
});

test('color darf kein Objekt-Nichtwert sein', () => {
  fehler({ theme: 'acme', color: null }, 'color');
});

test('color.gray als Objekt landet unter geliefert, saat.gray bleibt aus', () => {
  const e = leseThemeEingabe({ theme: 'acme', color: { brand: '#e8541e', gray: { 3: '#eeeeee' } } }, P, T);
  assert.equal(e.geliefert['gray-3'], '#EEEEEE');
  assert.equal(e.saat.gray, undefined);
});
