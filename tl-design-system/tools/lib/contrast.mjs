/** Die Schwelle, die WCAG 2.1 AA für normalen Text verlangt. */
export const AA_TEXT = 4.5;

/** Die Schwelle für große Schrift, Icons und die Begrenzung eines Controls. */
export const AA_NON_TEXT = 3;

const KANAL_GEWICHT = { r: 0.2126, g: 0.7152, b: 0.0722 };
const LINEAR_GRENZE = 0.03928;
const LINEAR_STEIGUNG = 12.92;
const GAMMA_VERSATZ = 0.055;
const GAMMA_EXPONENT = 2.4;

/** Hebt beide Luminanzen von der Null weg, damit das Verhältnis endlich bleibt. */
const STREULICHT = 0.05;

/**
 * Die Farbe, die ein CSS-Hexwert benennt.
 *
 * Nimmt die Schreibweisen `#rgb`, `#rrggbb` und `#rrggbbaa`; der Alphakanal wird für die
 * Kontrastrechnung verworfen, weil WCAG über der tatsächlich gezeichneten Farbe rechnet.
 */
export function color(cssValue) {
  const ziffern = cssValue.trim().replace(/^#/, '');
  if (ziffern.length === 3) {
    const [r, g, b] = [...ziffern].map((z) => parseInt(z + z, 16));
    return { r, g, b };
  }
  if (ziffern.length === 6 || ziffern.length === 8) {
    return {
      r: parseInt(ziffern.slice(0, 2), 16),
      g: parseInt(ziffern.slice(2, 4), 16),
      b: parseInt(ziffern.slice(4, 6), 16),
    };
  }
  throw new Error(`Kein Hexwert: '${cssValue}'`);
}

function linear(kanal) {
  const wert = kanal / 255;
  return wert <= LINEAR_GRENZE
    ? wert / LINEAR_STEIGUNG
    : ((wert + GAMMA_VERSATZ) / (1 + GAMMA_VERSATZ)) ** GAMMA_EXPONENT;
}

function luminanz(hex) {
  const { r, g, b } = color(hex);
  return KANAL_GEWICHT.r * linear(r) + KANAL_GEWICHT.g * linear(g) + KANAL_GEWICHT.b * linear(b);
}

/**
 * Das Kontrastverhältnis der beiden Farben, zwischen 1 und 21.
 *
 * Welche der beiden der Vordergrund ist, ändert das Ergebnis nicht.
 */
export function ratio(foreground, background) {
  const eine = luminanz(foreground);
  const andere = luminanz(background);
  const heller = Math.max(eine, andere);
  const dunkler = Math.min(eine, andere);
  return (heller + STREULICHT) / (dunkler + STREULICHT);
}

const RGBA = /^rgba\(\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*,\s*([\d.]+)\s*\)$/;

/** Die Kanäle und die Deckkraft eines Werts `rgba(r, g, b, a)`, wie die Primitive ihn schreiben; ein Hexwert deckt. */
export function rgba(cssValue) {
  if (/^#/.test(String(cssValue).trim())) return { ...color(cssValue), a: 1 };
  const m = RGBA.exec(String(cssValue).trim());
  if (!m) throw new Error(`Kein rgba-Wert: '${cssValue}'`);
  return { r: Number(m[1]), g: Number(m[2]), b: Number(m[3]), a: Number(m[4]) };
}

/**
 * Die Farbe, die eine durchscheinende Schicht über einem deckenden Grund ergibt, als Hex.
 *
 * Eine Deckkraft hat keinen Kontrast für sich; gemessen wird, was gezeichnet wird: die Schicht über
 * dem Grund, gemischt im sRGB-Raum wie der Browser (docs/skalen.md „Höfe sind durchscheinend“).
 */
export function ueberlagert(schicht, grundHex) {
  const s = rgba(schicht);
  const g = color(grundHex);
  const kanal = (k) => Math.round(s[k] * s.a + g[k] * (1 - s.a)).toString(16).padStart(2, '0');
  return `#${kanal('r')}${kanal('g')}${kanal('b')}`.toUpperCase();
}
