// Vergibt Namen für die Primitiv-Schicht — reproduzierbar, nicht handverlesen.
//
// Die Primitive sind die Stelle, an der ein Kundenprojekt eingreift: Wer seine
// eigene Hausfarbe einsetzen will, tauscht hier Werte und nicht Klassen. Damit
// das trägt, darf ein Primitivname nichts über seinen Inhalt behaupten außer
// seiner Lage in der Leiter: Familie plus Stufe, wobei eine höhere Stufe
// dunkler ist.
import { hexZuOklch } from './color.mjs';

const GRAD = 180 / Math.PI;

/** Unterhalb dieses Chromas nennen wir eine Farbe neutral. */
export const NEUTRAL_CHROMA = 0.02;

/** Farbfamilie nach dem Farbton in Grad. Die Grenzen sind bewusst grob. */
export const FAMILIEN = [
  { name: 'red',     bis: 45 },
  { name: 'amber',   bis: 110 },
  { name: 'green',   bis: 175 },
  { name: 'teal',    bis: 235 },
  { name: 'blue',    bis: 310 },
  { name: 'magenta', bis: 360 },
];

export function familie(hex) {
  const { C, H } = hexZuOklch(hex);
  if (C < NEUTRAL_CHROMA) return 'gray';
  const grad = (((H * GRAD) % 360) + 360) % 360;
  return FAMILIEN.find((f) => grad < f.bis).name;
}

/** Stufe aus der Helligkeit: 00 ist das hellste Ende, 99 das dunkelste. */
export function stufe(hex) {
  return Math.round((1 - hexZuOklch(hex).L) * 100);
}

/**
 * Ordnet jedem Hexwert genau einen Primitivnamen zu. Gleiche Stufen innerhalb
 * einer Familie rücken auf, damit kein Name doppelt vergeben wird — sonst
 * hinge die Zuordnung an der Reihenfolge, in der die Werte hereinkommen.
 */
export function primitiveAbleiten(hexWerte) {
  const nachFamilie = new Map();
  for (const hex of [...new Set(hexWerte)]) {
    const f = familie(hex);
    if (!nachFamilie.has(f)) nachFamilie.set(f, []);
    nachFamilie.get(f).push(hex);
  }
  const namen = {};
  for (const [f, liste] of [...nachFamilie].sort()) {
    const vergeben = new Set();
    for (const hex of liste.sort((a, b) => hexZuOklch(a).L - hexZuOklch(b).L)) {
      let s = stufe(hex);
      while (vergeben.has(s)) s += 1;
      vergeben.add(s);
      namen[hex] = `${f}-${String(s).padStart(2, '0')}`;
    }
  }
  return namen;
}
