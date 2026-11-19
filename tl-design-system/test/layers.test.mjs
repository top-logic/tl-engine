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
  ...leaves(T.density), ...leaves(T.layer), ...leaves(T.derived.candidates)
    .map(([p, v]) => [`derived.candidates.${p}`, v])];
const schluessel = (o) => Object.keys(o).filter((k) => !k.startsWith('_')).sort();
const NAME = /^[a-z][a-z0-9-]*$/;

test('jede oberste Gruppe von tokens.json hat eine Regel, und die Untergruppen sind geschlossen', () => {
  // semantisch() deckt color, effect, measure, density, layer und derived.candidates ab (Literal- und
  // Referenzprüfung oben); die übrigen Gruppen werden hier geprüft.
  const regeln = {
    color: () => assert.deepEqual(schluessel(T.color), ['dark', 'light'], 'color: genau light, dark'),
    effect: () => assert.deepEqual(schluessel(T.effect), ['dark', 'light'], 'effect: genau light, dark'),
    measure: () => {},
    density: () => assert.deepEqual(schluessel(T.density), ['compact', 'normal'], 'density: genau normal, compact'),
    layer: () => {},
    derived: () => {
      assert.deepEqual(schluessel(T.derived), ['candidates', 'dark', 'light'], 'derived: genau light, dark, candidates');
      for (const modus of ['light', 'dark'])
        for (const [n, v] of Object.entries(T.derived[modus]).filter(([k]) => !k.startsWith('_'))) {
          assert.deepEqual(Object.keys(v), ['surface'], `derived.${modus}.${n}: genau der Schlüssel surface`);
          assert.ok(Object.hasOwn(T.color[modus], v.surface), `derived.${modus}.${n}.surface = ${v.surface} ist kein Schlüssel von color.${modus}`);
        }
    },
    type: () => feldRegeln('type', ['family', 'size', 'weight', 'lineHeight', 'tracking']),
    print: () => feldRegeln('print', ['size', 'weight', 'lineHeight']),
    prose: () => {
      for (const [n, v] of Object.entries(T.prose).filter(([k]) => !k.startsWith('_'))) {
        assert.ok(Array.isArray(v), `prose.${n}: Array von Elementnamen erwartet`);
        for (const e of v) assert.match(String(e), /^[a-z][a-z0-9]*$/, `prose.${n}: ${e} ist kein Elementname`);
      }
    },
  };
  const ROLLE = { family: 'font-family', size: 'font-size', weight: 'font-weight',
    lineHeight: 'font-line-height', tracking: 'font-letter-spacing' };
  function feldRegeln(gruppe, felder) {
    for (const [n, def] of Object.entries(T[gruppe]).filter(([k]) => !k.startsWith('_'))) {
      assert.deepEqual(Object.keys(def).sort(), [...felder].sort(), `${gruppe}.${n}: Felder genau ${felder.join(', ')}`);
      for (const f of felder) {
        const v = def[f];
        assert.ok(typeof v === 'string' && NAME.test(v), `${gruppe}.${n}.${f} = ${v} — Rollenname ohne Präfix erwartet, kein Wert`);
        assert.ok(Object.hasOwn(T.measure, `${ROLLE[f]}-${v}`), `${gruppe}.${n}.${f}: Rolle ${ROLLE[f]}-${v} fehlt in measure`);
      }
    }
  }
  for (const g of schluessel(T)) {
    assert.ok(Object.hasOwn(regeln, g), `Gruppe ${g} ohne Literalprüfung`);
    regeln[g]();
  }
});

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
