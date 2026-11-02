// Löst Referenzen auf Werte auf und erzeugt die Beschriftungen auf Flächen.
// Gebraucht zum Rechnen (Kontrast, DTCG) — das erzeugte CSS löst NICHT auf,
// dort bleibt var(--tl-blue-49) stehen, damit ein Kunde die Primitive tauschen kann.
import { beschriftungAbleiten } from './lib/color.mjs';

const REF = /^\{([a-z0-9-]+)\}$/;

export function primitives(P) {
  return Object.assign({}, ...Object.entries(P).filter(([k]) => !k.startsWith('_')).map(([, g]) => g));
}

export function deref(value, prim) {
  const m = REF.exec(String(value));
  if (!m) return value;
  if (!(m[1] in prim)) throw new Error(`unbekannte Primitive: ${m[1]}`);
  return prim[m[1]];
}

function walk(o, prim) {
  return Object.fromEntries(Object.entries(o).filter(([k]) => !k.startsWith('_'))
    .map(([k, v]) => [k, v && typeof v === 'object' ? walk(v, prim) : deref(v, prim)]));
}

/** @returns { color:{light,dark}, derived:{light,dark}, effect, measure, density, layer } mit Werten */
export function resolve(P, T) {
  const prim = primitives(P);
  const color = walk(T.color, prim);
  const kandidaten = {
    dunklerKandidat: deref(T.derived.candidates.dark, prim),
    hellerKandidat: deref(T.derived.candidates.light, prim),
  };
  const derived = {};
  for (const mode of ['light', 'dark']) {
    derived[mode] = {};
    for (const [name, { surface }] of Object.entries(T.derived[mode])) {
      if (!(surface in color[mode])) throw new Error(`${name}: Fläche ${surface} unbekannt`);
      derived[mode][name] = beschriftungAbleiten(color[mode][surface], kandidaten).hex;
    }
  }
  return {
    color, derived,
    effect: walk(T.effect, prim), measure: walk(T.measure, prim),
    density: walk(T.density, prim), layer: walk(T.layer, prim),
  };
}
