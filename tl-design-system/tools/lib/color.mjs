/**
 * Rechnet Rampen, Kategorien und Beschriftungen in OKLCH.
 *
 * Gerechnet wird in OKLCH, nicht in HSL. HSL-Rampen sind perzeptuell ungleichmäßig: derselbe
 * Schritt in L (HSL) macht Gelb kaum dunkler und Blau stark dunkler, weil HSL keine
 * Wahrnehmungsgleichheit kennt. OKLCH ist so konstruiert, dass ein Schritt in L über alle Farbtöne
 * ungefähr gleich stark wahrgenommen wird — die Rampen von Rot, Grün und Blau werden damit
 * vergleichbar. Die Umrechnung sRGB ↔ lineares RGB ↔ OKLab ↔ OKLCH ist geschlossene Mathematik
 * (Björn Ottossons Matrizen für OKLab) und zieht keine Abhängigkeit nach sich.
 *
 * Daneben leitet dieses Modul auch die Beschriftungsfarbe (text-on-color, icon-on-color) ab —
 * dafür wird contrast.mjs importiert, nicht neu erfunden.
 */

import { ratio } from './contrast.mjs';

// ---------------------------------------------------------------------------------------------
// sRGB ↔ linear ↔ OKLab ↔ OKLCH — reine Zahlenumrechnung, keine Bibliothek.
// ---------------------------------------------------------------------------------------------

function srgbKanalZuLinear(kanal) {
  const wert = kanal / 255;
  return wert <= 0.04045 ? wert / 12.92 : ((wert + 0.055) / 1.055) ** 2.4;
}

function linearKanalZuSrgb(wert) {
  const begrenzt = Math.max(0, Math.min(1, wert));
  const kodiert = begrenzt <= 0.0031308 ? begrenzt * 12.92 : 1.055 * begrenzt ** (1 / 2.4) - 0.055;
  return Math.round(Math.max(0, Math.min(1, kodiert)) * 255);
}

/** Hexwert (#rgb, #rrggbb) zu {r,g,b}, jeweils 0–255. */
function hexZuRgb(hex) {
  const ziffern = hex.trim().replace(/^#/, '');
  const voll = ziffern.length === 3 ? [...ziffern].map((z) => z + z).join('') : ziffern;
  return {
    r: parseInt(voll.slice(0, 2), 16),
    g: parseInt(voll.slice(2, 4), 16),
    b: parseInt(voll.slice(4, 6), 16),
  };
}

function rgbZuHex({ r, g, b }) {
  const zweistellig = (kanal) => kanal.toString(16).padStart(2, '0').toUpperCase();
  return `#${zweistellig(r)}${zweistellig(g)}${zweistellig(b)}`;
}

/** Lineares sRGB [r, g, b] (je 0–1) zu OKLab {L,a,b} — Ottossons Matrizen (linear sRGB → LMS → OKLab).
 *  Ohne Rundung auf 8 Bit, damit auch gerechnete Zwischenwerte (Farbsehschwäche) exakt bleiben. */
export function linearRgbZuOklab([lr, lg, lb]) {
  const l = 0.4122214708 * lr + 0.5363325363 * lg + 0.0514459929 * lb;
  const m = 0.2119034982 * lr + 0.6806995451 * lg + 0.1073969566 * lb;
  const s = 0.0883024619 * lr + 0.2817188376 * lg + 0.6299787005 * lb;

  const l_ = Math.cbrt(l);
  const m_ = Math.cbrt(m);
  const s_ = Math.cbrt(s);

  return {
    L: 0.2104542553 * l_ + 0.7936177850 * m_ - 0.0040720468 * s_,
    a: 1.9779984951 * l_ - 2.4285922050 * m_ + 0.4505937099 * s_,
    b: 0.0259040371 * l_ + 0.7827717662 * m_ - 0.8086757660 * s_,
  };
}

/** Hexwert zu linearem sRGB [r, g, b], je 0–1. */
export function hexZuLinearRgb(hex) {
  const { r, g, b } = hexZuRgb(hex);
  return [srgbKanalZuLinear(r), srgbKanalZuLinear(g), srgbKanalZuLinear(b)];
}

/** OKLab {L,a,b} → lineares sRGB {lr,lg,lb}, UNBEGRENZT — kann außerhalb von 0…1 liegen. Reine
 *  Zahlenumrechnung, damit die Gamut-Suche unten dieselbe Rechnung beliebig oft wiederholen kann,
 *  ohne jedes Mal auch schon zu runden oder zu klemmen. */
function oklabZuLinearRgb({ L, a, b }) {
  const l_ = L + 0.3963377774 * a + 0.2158037573 * b;
  const m_ = L - 0.1055613458 * a - 0.0638541728 * b;
  const s_ = L - 0.0894841775 * a - 1.2914855480 * b;

  const l = l_ ** 3;
  const m = m_ ** 3;
  const s = s_ ** 3;

  return {
    lr: 4.0767416621 * l - 3.3077115913 * m + 0.2309699292 * s,
    lg: -1.2684380046 * l + 2.6097574011 * m - 0.3413193965 * s,
    lb: -0.0041960863 * l - 0.7034186147 * m + 1.7076147010 * s,
  };
}

const KANAL_IM_GAMUT = (kanal) => kanal >= 0 && kanal <= 1;
const ALLE_KANAELE_IM_GAMUT = ({ lr, lg, lb }) => KANAL_IM_GAMUT(lr) && KANAL_IM_GAMUT(lg) && KANAL_IM_GAMUT(lb);

/** OKLab {L,a,b} als Zylinder {L,C,H} — H in Radiant, damit die Trigonometrie ohne Umrechnung passt. */
function oklabZuOklch({ L, a, b }) {
  return { L, C: Math.sqrt(a * a + b * b), H: Math.atan2(b, a) };
}

function oklchZuOklab({ L, C, H }) {
  return { L, a: C * Math.cos(H), b: C * Math.sin(H) };
}

/** Hexwert zu OKLCH. */
export function hexZuOklch(hex) {
  return oklabZuOklch(linearRgbZuOklab(hexZuLinearRgb(hex)));
}

/**
 * Anzahl der Intervallhalbierungen bei der Chroma-Suche (siehe oklchZuHex()).
 *
 * Jeder Schritt halbiert das Unsicherheitsintervall der Chroma. 20 Schritte drücken es auf das
 * 2^-20-fache seiner Ausgangsbreite zusammen — bei einer Ausgangs-Chroma von realistisch bis zu
 * 0,4 sind das rund 4·10⁻⁷, weit unterhalb dessen, was ein 8-Bit-Farbkanal (Auflösung 1/255 ≈
 * 0,0039) überhaupt unterscheiden kann. Eine analytische Lösung des Gamut-Rands in OKLCH gibt es
 * nicht geschlossen (die Hin- und Rückmatrizen sind über die Kubikwurzel verschachtelt); die
 * Suche ist dafür unabhängig vom Farbton immer gleich schnell und gleich genau.
 */
export const CHROMA_SUCH_SCHRITTE = 20;

/**
 * OKLCH zu Hexwert — mit Gamut-Anpassung statt stillem Verlust.
 *
 * Liegt {L,C,H} außerhalb dessen, was sRGB darstellen kann, wird NICHT geklemmt (das würde L und
 * H verzerren, denn nach der Rundung ist der Kanal nicht mehr auf der ursprünglichen L/H-Linie).
 * Statt dessen wird C bei gehaltenem L und H per Intervallhalbierung so weit verringert, bis alle
 * drei linearen Kanäle in 0…1 liegen — das genau ist die Bedeutung von "Helligkeit und Farbton
 * bleiben unangetastet, Chroma ist die Stellschraube". Das Ergebnis ist blasser als angefragt,
 * aber auf derselben L/H-Linie: der schwächere, ehrliche Ersatz für einen Ton, den es an dieser
 * Helligkeit in dieser Sättigung in sRGB nicht gibt.
 *
 * Rückgabe: { hex, reduziert, geklemmt }.
 *   - reduziert: die Chroma wurde gesenkt, um im Gamut zu bleiben (normal, informativ).
 *   - geklemmt: auch bei C=0 (vollständig entsättigt, ein Grauton auf derselben Helligkeit) bleibt
 *     der Kanal außerhalb von 0…1 — das kann nur passieren, wenn L selbst außerhalb von 0…1 liegt.
 *     Dann hilft keine Chroma-Reduktion mehr; das ist ein Fehlerfall, kein normaler Bestandteil
 *     der Ableitung, und wird als solcher weiterhin gemeldet.
 */
export function oklchZuHex({ L, C, H }) {
  const angefragt = oklabZuLinearRgb(oklchZuOklab({ L, C, H }));
  if (ALLE_KANAELE_IM_GAMUT(angefragt)) {
    return { hex: rgbZuHex(rundeLinearRgb(angefragt)), reduziert: false, geklemmt: false };
  }

  // Fehlerfall zuerst ausschließen: wenn schon der vollständig entsättigte Punkt (C=0) auf
  // dieser Helligkeit außerhalb des Gamuts liegt, ist L selbst nicht darstellbar — keine
  // Chroma-Reduktion kann das beheben.
  const achromatisch = oklabZuLinearRgb(oklchZuOklab({ L, C: 0, H }));
  if (!ALLE_KANAELE_IM_GAMUT(achromatisch)) {
    return { hex: rgbZuHex(rundeLinearRgb(angefragt)), reduziert: false, geklemmt: true };
  }

  // Intervallhalbierung: unten ist immer im Gamut (Invariante, durch den Test oben gesichert),
  // oben immer außerhalb (die Anfrage selbst, laut erstem Test). Nach CHROMA_SUCH_SCHRITTE
  // Schritten ist "unten" die größte im Gamut liegende Chroma, bis auf die oben berechnete
  // Rundungstoleranz.
  let unten = 0;
  let oben = C;
  for (let schritt = 0; schritt < CHROMA_SUCH_SCHRITTE; schritt++) {
    const mitte = (unten + oben) / 2;
    const probe = oklabZuLinearRgb(oklchZuOklab({ L, C: mitte, H }));
    if (ALLE_KANAELE_IM_GAMUT(probe)) unten = mitte; else oben = mitte;
  }

  const gefunden = oklabZuLinearRgb(oklchZuOklab({ L, C: unten, H }));
  return { hex: rgbZuHex(rundeLinearRgb(gefunden)), reduziert: true, geklemmt: false };
}

/** Rundet lineares RGB auf sRGB-Kanäle 0–255. Wird nur auf Werte angewendet, die bereits (durch
 *  oklchZuHex() sichergestellt) im Gamut liegen — bis auf den gemeldeten Fehlerfall, wo trotzdem
 *  gerundet werden muss, um überhaupt einen Hexwert anzuzeigen. */
function rundeLinearRgb({ lr, lg, lb }) {
  return { r: linearKanalZuSrgb(lr), g: linearKanalZuSrgb(lg), b: linearKanalZuSrgb(lb) };
}

// ---------------------------------------------------------------------------------------------
// Die Schrittweiten der Ableitung — an einer Stelle, benannt, mit Begründung.
//
// Alle Schritte bewegen ausschließlich L (Helligkeit in OKLCH); C (Sättigung) und H (Farbton)
// bleiben über die ganze Rampe erhalten. Das hält die Rampe an EINEM Farbton entlang einer
// Helligkeitslinie, statt nebenbei den Ton zu verschieben — genau das Problem, das HSL hat.
//
// Ursprünglich gegen die heute geltenden Gate-Zahlen für #2968C8 kalibriert (hell 1 Verstoß,
// dunkel 7 Verstöße). Seit oklchZuHex() bei Bedarf die Chroma senkt statt zu klemmen, weichen
// diese Zahlen für #2968C8 leicht ab (siehe task-7-brand-report.md, Korrekturrunde 2) — die
// Schrittweiten selbst sind unverändert, nur der Weg zum Hexwert an den Stufen, die den Gamut
// verlassen, ist ein anderer. Die Werte bleiben trotzdem nur ein Vorschlag, keine Festlegung.
// ---------------------------------------------------------------------------------------------

/** Hell: hover ist um diesen Betrag dunkler als der Grundton. */
export const HELL_HOVER_SCHRITT = 0.06;

/** Hell: active ist um diesen Betrag dunkler als der Grundton (nicht nur als hover). */
export const HELL_ACTIVE_SCHRITT = 0.14;

/**
 * Dunkel: der Grundton selbst wird um diesen Betrag aufgehellt, weil die Richtung sich umkehrt —
 * ein Button, der im hellen Modus durch Verdunkeln "gedrückt" wirkt, braucht im dunklen Modus vor
 * dem fast schwarzen Hintergrund die gegenteilige Bewegung, um überhaupt als Fläche lesbar zu sein.
 */
export const DUNKEL_GRUNDTON_SCHRITT = 0.16;

/** Dunkel: hover und link-hover hellen vom Grundton aus jeweils um diesen Betrag weiter auf
 *  (link-hover zwei solcher Schritte). Ein einziger Schrittwert für die aufhellende Seite der
 *  dunklen Rampe, statt mehrerer verschiedener. */
export const DUNKEL_SCHRITT = 0.06;

/**
 * Dunkel: 'active' tritt vom Grundton aus zurück — dunkler, nicht heller wie hover. Das ist die
 * Umkehr gegenüber der ersten Fassung dieser Ableitung: dort hellte active im dunklen Modus
 * weiter auf als hover, was dem gängigen "gedrückt wirkt dunkler"-Verständnis widerspricht und
 * zusätzlich Farbkraft kostete (Chroma musste bei der sehr viel höheren Helligkeit stark
 * zurückgenommen werden). Ein Schritt zurück in Richtung Grundton braucht dagegen kaum
 * Chroma-Reduktion, weil er sich von einem Punkt entfernt, an dem noch reichlich Gamut-Raum ist.
 * Der Betrag liegt über dem Hover-Schritt (0,06) — der Unterschied zwischen "gedrückt" und
 * "Maus drüber" soll deutlich bleiben — aber unter dem hellen Active-Schritt (0,14), weil der
 * dunkle Grundton bereits angehoben ist und dieselbe Distanz ihn zu nah an Schwarz drücken würde.
 */
export const DUNKEL_ACTIVE_SCHRITT = 0.10;

/**
 * Wählt die Beschriftungsfarbe für EINE Fläche — für jede gefüllte Fläche einzeln aufzurufen
 * (button-primary, button-danger, layer-accent, …), nicht einmal global. Eine Fläche, die sich
 * nicht mit der Marke ändert (button-danger, layer-accent), bekommt trotzdem eine ABGELEITETE
 * Beschriftung, keinen fest hingeschriebenen Wert — falls sich diese Fläche selbst einmal
 * ändert (anderes Theme, andere Statusfarbe), stimmt die Beschriftung dann automatisch weiter.
 *
 * Zwei feste Kandidaten — ein naher-schwarzer und ein weißer — treten gegeneinander an; es
 * gewinnt der mit dem höheren Kontrastverhältnis gegen die Fläche. Das ist bewusst KEIN fester
 * Wert je Modus: welcher Kandidat gewinnt, hängt von der Fläche ab (eine sehr helle Marke
 * verlangt auch im hellen Modus dunkle Beschriftung auf button-primary), und ein fest
 * hingeschriebener Wert wäre beim nächsten Markenton, oder in einem anderen Theme, wieder falsch.
 */
export function beschriftungAbleiten(flaecheHex, { dunklerKandidat, hellerKandidat }) {
  const kontrastDunkel = ratio(dunklerKandidat, flaecheHex);
  const kontrastHell = ratio(hellerKandidat, flaecheHex);
  return kontrastDunkel >= kontrastHell
    ? { hex: dunklerKandidat.toUpperCase(), verhaeltnis: kontrastDunkel, kandidat: 'dunkel' }
    : { hex: hellerKandidat.toUpperCase(), verhaeltnis: kontrastHell, kandidat: 'hell' };
}

/**
 * Abstand zweier Farben im Farbton, in Grad (0 bis 180).
 *
 * Für getönte Flächen ist das WCAG-Kontrastverhältnis das falsche Maß: Es
 * misst Helligkeit. Zwei Flächen gleicher Helligkeit erreichen immer rund
 * 1,0 — auch wenn die eine blau und die andere orange ist. Ob zwei Tönungen
 * als verschieden gelesen werden, entscheidet der Farbton.
 */
export function farbtonAbstand(hexA, hexB) {
  const a = hexZuOklch(hexA), b = hexZuOklch(hexB);
  const grad = ((a.H - b.H) * 180) / Math.PI;
  return Math.abs(((grad % 360) + 540) % 360 - 180);
}

/** Ab diesem Farbtonabstand lesen sich zwei getönte Flächen als verschieden. */
export const FARBTON_ABSTAND_MIN = 30;

/** Unter dieser Chroma ist eine Farbe unbunt: ihr Farbton ist Rechenrauschen, kein Ton (markenRollen, derive). */
export const UNBUNT_C = 0.02;

/** Schrittweite in L, mit der aufKontrast() einen Wert nachführt. */
export const KONTRAST_SCHRITT = 0.005;

/**
 * Führt einen Wert in L nach, bis er gegen jeden Grund das geforderte Verhältnis erreicht.
 * Farbton und Chroma bleiben; das ist dieselbe Regel wie bei oklchZuHex(): L und H sind die
 * Aussage, C die Stellschraube — hier ist L die Stellschraube, weil Kontrast Helligkeit ist.
 * Ein Wert, der schon trägt, kommt unverändert zurück (angepasst: false).
 */
export function aufKontrast(hex, gruende, soll, richtung) {
  const { L, C, H } = hexZuOklch(hex);
  const traegt = (h) => gruende.every((g) => ratio(h, g) >= soll);
  if (traegt(hex)) return { hex: hex.toUpperCase(), angepasst: false, L };
  const schritt = richtung === 'dunkler' ? -KONTRAST_SCHRITT : KONTRAST_SCHRITT;
  for (let l = L + schritt; l > 0 && l < 1; l += schritt) {
    const { hex: probe } = oklchZuHex({ L: l, C, H });
    if (traegt(probe)) return { hex: probe, angepasst: true, L: l };
  }
  throw new Error(`${hex} erreicht ${soll} gegen ${gruende.join(', ')} in keiner Helligkeit`);
}

/** Chroma eines temperierten Graus — so viel, wie unser heutiges kühles Grau trägt. */
export const GRAU_CHROMA = 0.008;
/** Farbton je Temperatur in Grad: warm ein gelbliches, cool ein bläuliches Grau. */
export const GRAU_TON = { warm: 70, cool: 250 };

/**
 * Eine Graurampe auf den Helligkeiten der TopLogic-Rampe. Nur der Farbton wechselt, nie L —
 * jede Helligkeit der TopLogic-Rampe bleibt; was ein Paar dennoch verfehlt (OKLCH-L ist nicht
 * WCAG-Leuchtdichte), führt derive nach.
 */
export function grauRampe(temperatur, stufen) {
  if (!(temperatur === 'neutral' || Object.hasOwn(GRAU_TON, temperatur))) throw new Error(`Grau-Temperatur unbekannt: ${temperatur}`);
  const C = temperatur === 'neutral' ? 0 : GRAU_CHROMA;
  const H = temperatur === 'neutral' ? 0 : (GRAU_TON[temperatur] * Math.PI) / 180;
  return Object.fromEntries(Object.entries(stufen).map(([n, hex]) => [n, oklchZuHex({ L: hexZuOklch(hex).L, C, H }).hex]));
}

const rad = (g) => (g * Math.PI) / 180;

/**
 * Verteilt `anzahl` Farbtöne auf dem Kreis so, dass jeder mindestens `mindest` Grad zu jedem
 * blockierten Ton und zu jedem anderen hält. Verfahren: die Lücken zwischen den blockierten
 * Tönen bekommen reihum den nächsten Punkt, immer die Lücke, in der der Abstand danach am
 * größten bleibt. Reicht der Kreis nicht, sinkt der Mindestabstand in Schritten von 2°.
 */
export function kategorienVerteilen(blockiert, anzahl = 8, mindest = 20) {
  const feste = [...new Set(blockiert.map((g) => ((g % 360) + 360) % 360))].sort((a, b) => a - b);
  for (let abstand = mindest; abstand > 0; abstand -= 2) {
    const luecken = feste.length === 0 ? [{ von: 0, breite: 360, punkte: 0 }] : feste.map((von, i) => {
      const bis = i + 1 < feste.length ? feste[i + 1] : feste[0] + 360;
      return { von, breite: bis - von, punkte: 0 };
    });
    for (let n = 0; n < anzahl; n++) {
      let beste = null;
      for (const l of luecken) {
        const spanne = l.breite / (l.punkte + 2);
        if (spanne >= abstand && (!beste || spanne > beste.breite / (beste.punkte + 2))) beste = l;
      }
      if (!beste) break;
      beste.punkte++;
    }
    const gesetzt = luecken.reduce((s, l) => s + l.punkte, 0);
    if (gesetzt < anzahl) continue;
    const toene = luecken.flatMap((l) => Array.from({ length: l.punkte },
      (_, k) => (l.von + (l.breite * (k + 1)) / (l.punkte + 1)) % 360));
    return { toene, abstand };
  }
  throw new Error(`${anzahl} Kategorien finden keinen Platz`);
}

export const KATEGORIE_SUBTLE = { light: { L: 0.955, C: 0.03 }, dark: { L: 0.27, C: 0.05 } };
export const KATEGORIE_BORDER_CHROMA = 0.13;
/**
 * Die gefüllte Kategorie (Termine, Spec 5c §4): Helligkeit und Chroma der TopLogic-Stufe, in beiden
 * Modi dieselbe — die Schrift darauf ist in beiden Modi hell. Gelesen aus den Stufen hinter
 * category-N-surface (L 0,52–0,54, C 0,13); auf jedem Ton hält Weiß dort mindestens 4,7:1.
 */
export const KATEGORIE_SURFACE = { L: 0.54, C: 0.13 };

/** Eine Kategorie aus einem Farbton: getönte Fläche und Linie je Modus, Linie der hellste
 *  (hell) bzw. dunkelste (dunkel) Wert mit 3,0 auf beiden Gründen. Dazu die gefüllte Fläche als
 *  Rohstufe — ob der helle Kandidat des Themes auf ihr hält, prüft derive, denn nur derive kennt ihn
 *  und darf eine verfehlte Stufe zum Befund machen statt abzubrechen. */
export function kategorieRollen(tonGrad, gruende) {
  const H = rad(tonGrad);
  const subtle = (m) => oklchZuHex({ ...KATEGORIE_SUBTLE[m], H }).hex;
  const border = (m) => aufKontrast(oklchZuHex({ L: m === 'light' ? 0.75 : 0.45, C: KATEGORIE_BORDER_CHROMA, H }).hex,
    gruende[m], 3.0, m === 'light' ? 'dunkler' : 'heller').hex;
  const surface = oklchZuHex({ ...KATEGORIE_SURFACE, H }).hex;
  return {
    light: { subtle: subtle('light'), border: border('light'), surface },
    dark: { subtle: subtle('dark'), border: border('dark'), surface },
  };
}

/** Helligkeit und Chroma der getönten Markenflächen, aus der TopLogic-Palette gelesen. */
export const MARKE_SUBTLE = {
  light: { subtle: { L: 0.937, C: 0.03 }, hover: { L: 0.905, C: 0.046 } },
  dark: { subtle: { L: 0.255, C: 0.044 }, hover: { L: 0.305, C: 0.056 } },
};

/** Helligkeit und Chroma der Auswahlflächen. Hell liegt die gewählte Fläche auf der getönten Marke, die gezeigte
 *  (blue-8) knapp darunter. Dunkel läge blue-74 mit 1,04 kaum über surface-layer; gewählt ist blue-72 (1,12), gezeigt
 *  blue-69. Die gezeigte Stufe muss Hilfetext und Link noch mit 4,5 tragen (Paarliste), darum sind die Stufen eng. */
export const MARKE_AUSWAHL = {
  light: { selected: MARKE_SUBTLE.light.subtle, hover: { L: 0.921, C: 0.0353 } },
  dark: { selected: { L: 0.28, C: 0.05 }, hover: MARKE_SUBTLE.dark.hover },
};

/**
 * Die Markenrollen beider Modi aus einer Saat. Die Schritte sind die dokumentierten
 * (HELL_HOVER_SCHRITT …); die Link-Regel bleibt: hell folgt der Link der Fläche, dunkel liegt er
 * eine Stufe über ihr — jetzt ausgehend vom NACHGEFÜHRTEN text-link, nicht von der ungeprüften
 * Fläche, damit Reihenfolge und Kontrast von link-hover nie an einer eigenen, unabhängigen
 * Rechnung vorbeirutschen. Text- und Linienrollen werden gegen die Gründe nachgeführt, damit ein
 * gerechneter Wert nie ein Paar verfehlt — die Fläche selbst bleibt die Marke des Kunden.
 *
 * Drei Randfälle bleiben nicht stumm, sondern werden zu Hinweisen: eine Saat, die den Gamut
 * verlässt (bei() meldet geklemmt), eine Saat, deren Rampe im gewählten Modus auf einen einzigen
 * Wert kollabiert (zu hell oder zu dunkel für drei unterscheidbare Stufen), und eine unbunte
 * Saat, deren getönte Flächen sonst am zufälligen Restfarbton der Fließkomma-Rechnung hingen.
 */
export function markenRollen(saatHex, gruende) {
  const { L, C, H } = hexZuOklch(saatHex);
  const hinweise = [];
  const unbunt = C < UNBUNT_C;
  if (unbunt) hinweise.push('die Marke ist unbunt; getönte Flächen bleiben grau');
  const bei = (mode, rolle, l, c = C) => {
    const r = oklchZuHex({ L: l, C: c, H });
    if (r.geklemmt) hinweise.push(`${mode}: ${rolle} liegt außerhalb des Gamuts und wurde geklemmt`);
    return r.hex;
  };
  const fuehre = (mode, rolle, hex, soll, zusatz = []) => {
    const r = aufKontrast(hex, [...gruende[mode], ...zusatz], soll, mode === 'light' ? 'dunkler' : 'heller');
    if (r.angepasst) {
      const zahl = soll.toFixed(1).replace('.', ',');
      hinweise.push(`${mode}: ${rolle} in der Helligkeit nachgeführt, damit ${zahl} auf dem Grund hält (${hex} → ${r.hex})`);
    }
    return r.hex;
  };
  const pruefeKollaps = (mode, rollen) => {
    if (new Set(rollen).size < rollen.length) {
      hinweise.push(`${mode}: surface-brand, surface-brand-hover und surface-brand-active fallen auf denselben Wert zusammen (Saat zu hell oder zu dunkel)`);
    }
  };
  const subtleC = (mode, teil) => (unbunt ? 0 : MARKE_SUBTLE[mode][teil].C);
  const auswahlC = (mode, teil) => (unbunt ? 0 : MARKE_AUSWAHL[mode][teil].C);

  const dunkelBasis = L + DUNKEL_GRUNDTON_SCHRITT;
  const light = {
    'surface-brand': saatHex.toUpperCase(),
    'surface-brand-hover': bei('light', 'surface-brand-hover', L - HELL_HOVER_SCHRITT),
    'surface-brand-active': bei('light', 'surface-brand-active', L - HELL_ACTIVE_SCHRITT),
    'surface-brand-subtle': bei('light', 'surface-brand-subtle', MARKE_SUBTLE.light.subtle.L, subtleC('light', 'subtle')),
    'surface-brand-subtle-hover': bei('light', 'surface-brand-subtle-hover', MARKE_SUBTLE.light.hover.L, subtleC('light', 'hover')),
    'surface-selected': bei('light', 'surface-selected', MARKE_AUSWAHL.light.selected.L, auswahlC('light', 'selected')),
    'surface-selected-hover': bei('light', 'surface-selected-hover', MARKE_AUSWAHL.light.hover.L, auswahlC('light', 'hover')),
  };
  pruefeKollaps('light', [light['surface-brand'], light['surface-brand-hover'], light['surface-brand-active']]);
  // Die getönte Markenfläche ist selbst ein Grund: die Paarliste verlangt text-link auf
  // surface-brand-subtle. Deshalb wird sie erst gerechnet und dann mitgeführt — sonst reicht
  // eine Marke, deren Tönung dicht an ihrem Link liegt, um ein gerechnetes Paar zu verfehlen.
  light['text-link'] = fuehre('light', 'text-link', light['surface-brand'], 4.5, [light['surface-brand-subtle']]);
  light['text-link-hover'] = fuehre('light', 'text-link-hover',
    bei('light', 'text-link-hover', hexZuOklch(light['text-link']).L - HELL_HOVER_SCHRITT), 4.5);
  light['border-interactive'] = fuehre('light', 'border-interactive', light['surface-brand'], 3.0);
  light['border-selected'] = light['border-interactive'];
  light['focus-ring'] = fuehre('light', 'focus-ring', light['surface-brand'], 3.0);

  const dark = {
    'surface-brand': bei('dark', 'surface-brand', dunkelBasis),
    'surface-brand-hover': bei('dark', 'surface-brand-hover', dunkelBasis + DUNKEL_SCHRITT),
    'surface-brand-active': bei('dark', 'surface-brand-active', dunkelBasis - DUNKEL_ACTIVE_SCHRITT),
    'surface-brand-subtle': bei('dark', 'surface-brand-subtle', MARKE_SUBTLE.dark.subtle.L, subtleC('dark', 'subtle')),
    'surface-brand-subtle-hover': bei('dark', 'surface-brand-subtle-hover', MARKE_SUBTLE.dark.hover.L, subtleC('dark', 'hover')),
    'surface-selected': bei('dark', 'surface-selected', MARKE_AUSWAHL.dark.selected.L, auswahlC('dark', 'selected')),
    'surface-selected-hover': bei('dark', 'surface-selected-hover', MARKE_AUSWAHL.dark.hover.L, auswahlC('dark', 'hover')),
  };
  pruefeKollaps('dark', [dark['surface-brand'], dark['surface-brand-hover'], dark['surface-brand-active']]);
  dark['text-link'] = fuehre('dark', 'text-link', dark['surface-brand-hover'], 4.5, [dark['surface-brand-subtle']]);
  dark['text-link-hover'] = fuehre('dark', 'text-link-hover',
    bei('dark', 'text-link-hover', hexZuOklch(dark['text-link']).L + DUNKEL_SCHRITT), 4.5);
  dark['border-interactive'] = fuehre('dark', 'border-interactive', dark['surface-brand'], 3.0);
  dark['border-selected'] = dark['border-interactive'];
  dark['focus-ring'] = fuehre('dark', 'focus-ring', dark['surface-brand'], 3.0);

  return { light, dark, hinweise };
}
