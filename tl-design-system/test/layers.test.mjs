// Die Trennung der Schichten ist die Stelle, an der ein Kundenprojekt eingreift.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';

const P = JSON.parse(readFileSync(new URL('../src/primitives.json', import.meta.url)));
const T = JSON.parse(readFileSync(new URL('../src/tokens.json', import.meta.url)));
const REF = /^\{([a-z0-9-]+)\}$/;
const KATEGORIEN = ['surface', 'text', 'icon', 'border', 'focus', 'shadow', 'space', 'radius',
  'size', 'font', 'duration', 'easing', 'layer', 'status', 'category'];

export const primitive = () => Object.assign({}, ...Object.values(P).filter((g) => typeof g === 'object'));

function leaves(o, path = '') {
  const out = [];
  for (const [k, v] of Object.entries(o)) {
    if (k.startsWith('_')) continue;
    if (v && typeof v === 'object') out.push(...leaves(v, `${path}${k}.`));
    else out.push([`${path}${k}`, v]);
  }
  return out;
}
const semantisch = () => [...leaves(T.color), ...leaves(T.effect), ...leaves(T.measure),
  ...leaves(T.density), ...leaves(T.layer)];

test('kein Wert in der semantischen Schicht, nur Referenzen', () => {
  for (const [path, v] of semantisch())
    assert.match(String(v), REF, `${path} = ${v} — Werte gehören in primitives.json`);
});

test('jede Referenz zeigt auf eine vorhandene Primitive', () => {
  const known = new Set(Object.keys(primitive()));
  for (const [path, v] of semantisch())
    assert.ok(known.has(REF.exec(v)[1]), `${path} zeigt auf unbekanntes ${v}`);
});

test('keine Primitive ohne Leser', () => {
  // T.type verweist nicht per {ref} auf Primitive (family: "sans" statt {font-1}) — font-1/
  // font-2 werden bereits über measure.font-family-* gelesen, eine Schleife über T.type hier
  // wäre tote Schleife ohne Wirkung auf das Ergebnis.
  const used = new Set(semantisch().map(([, v]) => REF.exec(v)[1]));
  for (const name of Object.keys(primitive()))
    assert.ok(used.has(name), `${name} wird von keinem Token gelesen`);
});

test('Primitivnamen sind Familie plus Stufe und tragen keine Rolle', () => {
  const rolle = /(primary|secondary|brand|focus|border|surface|status|field|helper|placeholder|control|row|padding|gap|link|hover|active|selected|disabled)/;
  for (const name of Object.keys(primitive())) {
    assert.match(name, /^[a-z]+-\d+$/, `${name}: Form ist familie-stufe`);
    assert.ok(!rolle.test(name), `${name} trägt eine Rolle im Namen`);
  }
});

test('jeder Tokenname beginnt mit einer der geschlossenen Kategorien', () => {
  const names = [...Object.keys(T.color.light), ...Object.keys(T.effect.light),
    ...Object.keys(T.measure), ...Object.keys(T.density.normal), ...Object.keys(T.layer),
    ...Object.keys(T.derived.light)];
  for (const n of names)
    assert.ok(KATEGORIEN.some((k) => n === k || n.startsWith(`${k}-`)),
      `${n}: keine bekannte Kategorie (${KATEGORIEN.join(', ')})`);
});

test('beide Modi belegen dieselben Rollen, nichts wird geerbt', () => {
  assert.deepEqual(Object.keys(T.color.dark).sort(), Object.keys(T.color.light).sort());
  assert.deepEqual(Object.keys(T.effect.dark).sort(), Object.keys(T.effect.light).sort());
  assert.deepEqual(Object.keys(T.derived.dark).sort(), Object.keys(T.derived.light).sort());
});

test('kompakt überschreibt dieselben Maße wie normal, und nie eine Schriftgröße', () => {
  assert.deepEqual(Object.keys(T.density.compact).sort(), Object.keys(T.density.normal).sort());
  for (const n of Object.keys(T.density.normal)) assert.ok(!n.startsWith('font-'), n);
});

test('Beschriftungen auf Flächen stehen nur unter derived, nie als Referenz', () => {
  for (const n of Object.keys(T.color.light))
    assert.ok(!/^(text|icon)-on-/.test(n), `${n} gehört unter derived`);
  for (const [n, v] of Object.entries(T.derived.light))
    assert.ok(Object.keys(T.color.light).includes(v.surface), `${n}: Fläche ${v.surface} unbekannt`);
});

test('ein Kunde tauscht eine Primitive und alle Rollen ziehen mit', async () => {
  const { resolve } = await import('../tools/resolve.mjs');
  const kunde = structuredClone({ P, T });
  const marke = REF.exec(T.color.light['surface-brand'])[1];
  kunde.P.color[marke] = '#7A1FA2';
  const r = resolve(kunde.P, kunde.T);
  for (const rolle of ['surface-brand', 'text-link', 'focus-ring', 'border-interactive'])
    assert.equal(r.color.light[rolle], '#7A1FA2', `${rolle} ist nicht gefolgt`);
});
