// Liest die Kundendatei der Stufe A und prüft sie gegen das Paket. Ein Fehler nennt den Pfad in
// der JSON; nichts wird geraten.
const HEX = /^#[0-9a-fA-F]{6}$/;
const THEME = /^[a-z][a-z0-9-]*$/;
const FAMILIEN = ['color', 'font', 'measure', 'motion', 'effect'];
const BEDEUTUNGEN = ['red', 'amber', 'green', 'teal'];
const TEMPERATUREN = ['warm', 'neutral', 'cool'];
const KATEGORIEN = Array.from({ length: 8 }, (_, i) => `category-${i + 1}`);

export class ThemeEingabeFehler extends Error {
  constructor(pfad, meldung) { super(`${pfad}: ${meldung}`); this.pfad = pfad; }
}

const hex = (pfad, v) => {
  if (typeof v !== 'string' || !HEX.test(v)) throw new ThemeEingabeFehler(pfad, `kein Hexwert #rrggbb: ${JSON.stringify(v)}`);
  return v.toUpperCase();
};
const objekt = (pfad, v) => {
  if (!v || typeof v !== 'object' || Array.isArray(v)) throw new ThemeEingabeFehler(pfad, 'ein Objekt erwartet');
  return v;
};

export function leseThemeEingabe(json, P, T) {
  const wurzel = objekt('', json);
  if (typeof wurzel.theme !== 'string' || !THEME.test(wurzel.theme) || wurzel.theme === 'tl')
    throw new ThemeEingabeFehler('theme', 'Pflicht, [a-z][a-z0-9-]*, nie "tl"');
  for (const k of Object.keys(wurzel)) if (k !== 'theme' && !FAMILIEN.includes(k))
    throw new ThemeEingabeFehler(k, `unbekannte Familie, erlaubt: ${FAMILIEN.join(', ')}`);

  const geliefert = {};
  const saat = {};
  const markenFamilie = /^\{([a-z]+)-\d+\}$/.exec(T.color.light['surface-brand'])[1];

  const stufen = (pfad, familie, v) => {
    for (const [stufe, wert] of Object.entries(objekt(pfad, v))) {
      const name = `${familie}-${stufe}`;
      if (!Object.hasOwn(P.color, name)) throw new ThemeEingabeFehler(`${pfad}.${stufe}`, `Stufe ${name} gibt es nicht`);
      geliefert[name] = hex(`${pfad}.${stufe}`, wert);
    }
  };

  if (wurzel.color !== undefined) {
    const c = objekt('color', wurzel.color);
    for (const k of Object.keys(c)) if (!['brand', 'gray', 'category', ...BEDEUTUNGEN].includes(k))
      throw new ThemeEingabeFehler(`color.${k}`, 'unbekannt; erlaubt: brand, gray, category, red, amber, green, teal');
    if (typeof c.brand === 'string') saat.brand = hex('color.brand', c.brand);
    else if (c.brand !== undefined) {
      stufen('color.brand', markenFamilie, c.brand);
      if (!(`${markenFamilie}-49` in geliefert)) throw new ThemeEingabeFehler('color.brand.49', 'die Stufe 49 ist die Saat und fehlt');
      saat.brand = geliefert[`${markenFamilie}-49`];
    }
    if (typeof c.gray === 'string') {
      if (!TEMPERATUREN.includes(c.gray)) throw new ThemeEingabeFehler('color.gray', `warm, neutral oder cool, nicht ${JSON.stringify(c.gray)}`);
      saat.gray = c.gray;
    } else if (c.gray !== undefined) stufen('color.gray', 'gray', c.gray);
    if (c.category !== undefined) {
      saat.category = {};
      for (const [k, v] of Object.entries(objekt('color.category', c.category))) {
        if (!KATEGORIEN.includes(k)) throw new ThemeEingabeFehler(`color.category.${k}`, 'category-1 bis category-8');
        saat.category[k] = hex(`color.category.${k}`, v);
      }
    }
    for (const b of BEDEUTUNGEN) if (c[b] !== undefined) stufen(`color.${b}`, b, c[b]);
  }
  for (const f of FAMILIEN.slice(1)) if (wurzel[f] !== undefined) {
    for (const [stufe, wert] of Object.entries(objekt(f, wurzel[f]))) {
      if (!Object.hasOwn(P[f], stufe)) throw new ThemeEingabeFehler(`${f}.${stufe}`, `Stufe gibt es nicht in ${f}`);
      if (typeof wert !== 'string' && typeof wert !== 'number') throw new ThemeEingabeFehler(`${f}.${stufe}`, 'ein Wert erwartet');
      geliefert[stufe] = String(wert);
    }
  }
  return { theme: wurzel.theme, geliefert, saat };
}
