/**
 * Diagrammfarben: Paarliste, Prüfer, zulässige Helligkeitsstufen und die Suche.
 *
 * Teil 1 sagt, was gilt und wie gemessen wird. Die Suche (`diagrammReihen`, Teil 2) legt die
 * Reihenfolge der Kategorien fest und wählt je Ton und Modus eine Stufe aus `zulaessigeStufen`
 * so, dass der Abstand der Reihen nach `pruefeReihen` über beide Modi möglichst groß wird.
 */

import { deltaE, rotGruen, simuliere } from './cvd.mjs';
import { ratio } from './contrast.mjs';
import { hexZuOklch, oklchZuHex, hexZuLinearRgb, linearRgbZuOklab, UNBUNT_C } from './color.mjs';

export const DIAGRAMM = {
  /** Kontrast gegen `surface-base` und `surface-layer` (nicht-textliche Grafik, WCAG 1.4.11). */
  KONTRAST: 3.0,
  /** ΔE (OKLab × 100) für Rot-Grün-Schwäche, min aus protan und deutan. */
  ROT_GRUEN: 8,
  /** ΔE (OKLab × 100) bei normalem Sehen. */
  NORMAL: 15,
  CHROMA_MIN: 0.10,
  CHROMA_MAX: 0.15,
  /**
   * Helligkeitsbereich je Modus (Prototyp C); der Kontrast engt ihn weiter ein. Hell beginnt das
   * Band bei 0,46, die Untergrenze gilt für alle Töne außer denen in `UNTERGRENZE_HELL`.
   */
  BAND: { light: [0.46, 0.77], dark: [0.48, 0.77] },
  /**
   * Höhere Untergrenzen im hellen Modus je Farbtonbereich (Spec §3, Nachtrag 23.10.): Rot und
   * warme Töne wurden bei tiefer Helligkeit zu Weinrot und Braun. Gradzahlen normalisiert auf
   * 0 ≤ Ton < 360; `von` und `bis` schließen ein, `von > bis` heißt über 0° hinweg. Es gilt die
   * erste passende Zeile (die Warm-Zeile steht vorn, damit 30° warm ist und Rot unter 30° endet).
   * Töne in keiner Zeile beginnen bei `BAND.light[0]`. Obergrenze und dunkler Modus bleiben.
   */
  UNTERGRENZE_HELL: [
    { von: 30, bis: 110, L: 0.54 }, // warm: 30° ≤ Ton ≤ 110°, Orange bis Gelb
    { von: 345, bis: 30, L: 0.50 }, // Rot: Ton ≥ 345° oder < 30° (30° fängt die Zeile davor)
  ],
};

/** Raster der Helligkeit (Plan-Review: 0,01 ist zu teuer, 0,03 verliert „acht geliefert“). */
/** Hundertstel je Rasterschritt: das Raster wird in ganzen Hundertsteln gezählt, nicht addiert. */
const L_RASTER_HUNDERTSTEL = 2;
/** Toleranz gegen die 8-Bit-Rundung von `oklchZuHex`, die C um bis zu 0,0015 bewegt. */
const C_TOLERANZ = 2e-3;

/**
 * Paare der Reihen (0-basiert): alle Nachbarn (i, i+1), dazu (0, j) für j ≥ 2, damit sich der
 * Ring schließt. Bei n Reihen sind das (n − 1) + (n − 2) Paare.
 */
export function diagrammPaare(n) {
  const paare = [];
  for (let i = 0; i + 1 < n; i++) paare.push([i, i + 1]);
  for (let j = 2; j < n; j++) paare.push([0, j]);
  return paare;
}

/**
 * Prüft die Reihen eines Modus gegen die Paarliste.
 *
 * `abstand` = min über alle Paare von min(ΔE_rotgrün / 8, ΔE_normal / 15); ab 1 sind beide
 * Schwellen gehalten. `schwaechstes` nennt Paar (1-basiert) und Art, die dieses Minimum bestimmen;
 * `wert` ist das rohe ΔE, `soll` seine Schwelle. Bei Gleichstand gilt das erste Paar der Liste,
 * und bei gleichem Quotienten zählt rot-grün.
 */
export function pruefeReihen(farben) {
  let besser = null;
  for (const [i, j] of diagrammPaare(farben.length)) {
    const rg = rotGruen(farben[i], farben[j]);
    const nm = deltaE(farben[i], farben[j]);
    const art = rg / DIAGRAMM.ROT_GRUEN <= nm / DIAGRAMM.NORMAL ? 'rot-gruen' : 'normal';
    const wert = art === 'rot-gruen' ? rg : nm;
    const soll = art === 'rot-gruen' ? DIAGRAMM.ROT_GRUEN : DIAGRAMM.NORMAL;
    const abstand = wert / soll;
    if (besser === null || abstand < besser.abstand) {
      besser = { abstand, schwaechstes: { art, reihen: [i + 1, j + 1], wert, soll } };
    }
  }
  return besser;
}

/**
 * Die Helligkeitsstufen, die ein Ton in einem Modus tragen darf.
 *
 * Raster 0,02 ab der Untergrenze des Bands bis höchstens zur Obergrenze (aus ganzen Hundertsteln
 * gebaut, damit keine Gleitkommadrift entsteht; die Obergrenze 0,77 liegt im dunklen Modus
 * zwischen zwei Stufen, dort ist 0,76 die letzte). `tonGrad` ist der Farbton in Grad. Im hellen
 * Modus hängt die Untergrenze vom Ton ab (`untergrenze`, Spec §3 Nachtrag 23.10.): Rot ab 0,50,
 * warme Töne ab 0,54, alle anderen ab 0,46; das Raster beginnt dort.
 * Behalten wird eine Stufe nur, wenn ihr Hexwert gegen jeden Grund Kontrast ≥ 3,0 hält und die
 * tatsächliche Chroma des Hexwerts (der Farbraum kann sie unter `chroma` drücken) mindestens
 * 0,10 beträgt, abzüglich der Rundungstoleranz.
 */
export function zulaessigeStufen(tonGrad, chroma, modus, gruende) {
  return rasterStufen(tonGrad, chroma, modus).filter(({ hex }) =>
    hexZuOklch(hex).C >= DIAGRAMM.CHROMA_MIN - C_TOLERANZ
    && gruende.every((g) => ratio(hex, g) >= DIAGRAMM.KONTRAST));
}

/**
 * Untergrenze der Helligkeit für einen Ton in einem Modus. Dunkel: die des Bands. Hell: die
 * erste Zeile von `UNTERGRENZE_HELL`, in der der normalisierte Ton liegt, sonst die des Bands.
 */
function untergrenze(tonGrad, modus) {
  if (modus !== 'light') return DIAGRAMM.BAND[modus][0];
  const ton = ((tonGrad % 360) + 360) % 360;
  for (const { von, bis, L } of DIAGRAMM.UNTERGRENZE_HELL) {
    if (von <= bis ? ton >= von && ton <= bis : ton >= von || ton <= bis) return L;
  }
  return DIAGRAMM.BAND.light[0];
}

/** Alle Stufen des Bands auf dem Raster, aufsteigend, noch ohne Kontrast- und Chromafilter. */
function rasterStufen(tonGrad, chroma, modus) {
  const unten = untergrenze(tonGrad, modus), oben = DIAGRAMM.BAND[modus][1];
  const H = (tonGrad * Math.PI) / 180;
  const vonH = Math.round(unten * 100);
  const bisH = Math.round(oben * 100);
  const stufen = [];
  for (let h = vonH; h <= bisH; h += L_RASTER_HUNDERTSTEL) {
    const L = h / 100;
    stufen.push({ L, hex: oklchZuHex({ L, C: chroma, H }).hex });
  }
  return stufen;
}

// ---------------------------------------------------------------------------------------------
// Teil 2: die Suche. Reihenfolge der Kategorien und Stufe jeder Reihe in beiden Modi.
//
// Gesucht wird das Maximum von `abstand` = min über beide Modi und alle Paare der Paarliste von
// min(ΔE_rotgrün / 8, ΔE_normal / 15). Ein Kandidat je Ton ist ein Stufenpaar (hell, dunkel):
// die Reihenfolge gilt für beide Modi, und die Vorauswahl des Helligkeitssprungs bindet beide
// Stufen eines Tons aneinander, also werden beide Modi zugleich optimiert.
//
// Verfahren (bewährt im Plan-Review): Reihe 1 steht vorab fest. Die Paarliste ist dann eine Kette
// (Nachbarn) plus ein Stern von Reihe 1; bei fester Stufe von Reihe 1 bleibt eine Kette, und das
// Maximum des Minimums über eine Kette rechnet eine dynamische Programmierung exakt. Die
// Reihenfolgen werden als Baum durchlaufen (gemeinsame Anfänge rechnen einmal), Werte unter dem
// bisher Besten fallen sofort weg, und ein Teilbaum endet, sobald nichts mehr darüber liegt.
// ---------------------------------------------------------------------------------------------

/** Die acht Kategorien sind die acht Reihen. */
const REIHEN = 8;
const MODI = ['light', 'dark'];
/**
 * Größter normierter Helligkeitssprung einer Reihe zwischen den Modi (Spec §3, ein Ziel, kein
 * Gate). Normiert je Ton und Modus auf seine zulässigen Stufen: 0 die dunkelste, 1 die hellste.
 */
const SPRUNG_MAX = 0.5;
/** Markiert einen Kandidaten, der das bisher Beste nicht mehr übertreffen kann. */
const TOT = -Infinity;

/** Farbton einer Farbe in Grad, 0 ≤ h < 360. */
const gradVon = (hex) => (((hexZuOklch(hex).H * 180) / Math.PI) % 360 + 360) % 360;
/** Abstand zweier Farbtöne auf dem Kreis, 0…180. */
const gradAbstand = (a, b) => {
  const d = (((a - b) % 360) + 360) % 360;
  return d > 180 ? 360 - d : d;
};

/**
 * Chroma der Reihen aus der Marke (Spec §6): `clamp(C der Marke, 0,10, 0,15)`. Eine unbunte
 * Marke (C < `UNBUNT_C`) hat keinen Ton, an dem Reihe 1 sich ausrichten könnte; sie gibt 0,10.
 */
function markenChroma(marke) {
  const { C } = hexZuOklch(marke);
  if (C < UNBUNT_C) return { chroma: DIAGRAMM.CHROMA_MIN, unbunt: true };
  return { chroma: Math.min(DIAGRAMM.CHROMA_MAX, Math.max(DIAGRAMM.CHROMA_MIN, C)), unbunt: false };
}

/**
 * Lage jeder Stufe im zulässigen Bereich ihres Tons, als Bruch [Zähler, Nenner] in ganzen
 * Hundertsteln: (L − L_min) / (L_max − L_min), 0 bei nur einer Stufe. Ganzzahlig, damit der
 * Vergleich mit der Sprunggrenze exakt bleibt (0,5 liegt genau auf dem Raster).
 */
function lageJeStufe(stufen) {
  const h = stufen.map(({ L }) => Math.round(L * 100));
  const spanne = h.at(-1) - h[0];
  return h.map((x) => (spanne === 0 ? [0, 1] : [x - h[0], spanne]));
}

/**
 * Sprung zwischen zwei Lagen [p, q] und [r, s]: |p/q − r/s|, als eine Division ganzer Zahlen
 * gerechnet. Sie rundet korrekt, also liegt ein Sprung von genau 0,5 nie darüber.
 */
const sprung = ([p, q], [r, s]) => Math.abs(p * s - r * q) / (q * s);

/**
 * Die Stufen eines Tons in einem Modus, für Suche und Sprungmaß gleich.
 *
 * Trägt der Ton keine zulässige Stufe (nur bei ungewöhnlichen Gründen möglich), bricht nichts ab:
 * Ersatz ist die eine Stufe des Bands (gleiche Chroma, Raster 0,02), die gegen den schwächeren
 * Grund den höchsten Kontrast hat; bei Gleichstand die dunklere. `ersatz` meldet das. Eine
 * einzelne Stufe hat die Lage 0 (`lageJeStufe`).
 */
function stufenMitErsatz(ton, chroma, modus, gruende) {
  const stufen = zulaessigeStufen(ton, chroma, modus, gruende);
  if (stufen.length > 0) return { stufen, ersatz: false };
  let beste = null, besterKontrast = -Infinity;
  for (const stufe of rasterStufen(ton, chroma, modus)) {
    const kontrast = Math.min(...gruende.map((g) => ratio(stufe.hex, g)));
    if (kontrast > besterKontrast) { beste = stufe; besterKontrast = kontrast; }
  }
  return { stufen: [beste], ersatz: true };
}

/** OKLab einer Farbe bei normalem Sehen, protan und deutan — derselbe Weg wie `deltaE`. */
const labDreifach = (hex) => [
  linearRgbZuOklab(hexZuLinearRgb(hex)),
  linearRgbZuOklab(simuliere(hex, 'protan')),
  linearRgbZuOklab(simuliere(hex, 'deutan')),
];
const dE = (x, y) => 100 * Math.hypot(x.L - y.L, x.a - y.a, x.b - y.b);
/** Abstand eines Paars wie in `pruefeReihen`: min(ΔE_rotgrün / 8, ΔE_normal / 15), bitgleich. */
const paarWert = (a, b) => Math.min(
  Math.min(dE(a[1], b[1]), dE(a[2], b[2])) / DIAGRAMM.ROT_GRUEN,
  dE(a[0], b[0]) / DIAGRAMM.NORMAL,
);

/** Ton auf 0 ≤ Ton < 360 bringen: −122° und 238° sind derselbe Ton und rechnen gleich. */
const mod360 = (g) => ((g % 360) + 360) % 360;

/**
 * Eingabe prüfen und in eine Form bringen, die weder von der Reihenfolge noch von der Schreibweise
 * der Töne abhängt (Spec §5, gleiche Eingabe gleiche Ausgabe): alle Töne, freie wie gelieferte,
 * stehen danach in 0 ≤ Ton < 360. Bei gleichem Abstand entscheidet die Reihenfolge der
 * aufsteigenden Töne, also darf sie nicht an der Darstellung hängen (−122° sortiert vor 186°).
 */
function normiereEingabe({ toene, fest = {}, marke, gruende }) {
  const geliefert = Object.entries(fest).map(([nr, ton]) => [Number(nr), ton]).sort((a, b) => a[0] - b[0]);
  for (const [nr] of geliefert) {
    if (!Number.isInteger(nr) || nr < 1 || nr > REIHEN) throw new Error(`Gelieferte Kategorie ${nr}: die Nummer muss 1–${REIHEN} sein`);
  }
  for (const ton of [...toene, ...geliefert.map(([, t]) => t)]) {
    if (!Number.isFinite(ton)) throw new Error(`Ton ${ton} ist keine Gradzahl`);
  }
  if (toene.length + geliefert.length !== REIHEN) {
    throw new Error(`Diagrammreihen brauchen genau ${REIHEN} Töne, erhalten ${toene.length} freie und ${geliefert.length} gelieferte`);
  }
  const frei = toene.map(mod360).sort((a, b) => a - b);
  return { frei, fest: geliefert.map(([nr, ton]) => [nr, mod360(ton)]), marke: marke.toUpperCase(), gruende: { light: [...gruende.light], dark: [...gruende.dark] } };
}

/**
 * Reihe 1 (Spec §4): der freie Ton, der der Marke auf dem Kreis am nächsten liegt; bei gleichem
 * Abstand der kleinere. Bei einer unbunten Marke der kleinste freie Ton. `frei` ist aufsteigend.
 */
function ersteReihe(frei, marke, unbunt) {
  if (unbunt) return 0;
  const ziel = gradVon(marke);
  let wahl = 0;
  for (let i = 1; i < frei.length; i++) if (gradAbstand(frei[i], ziel) < gradAbstand(frei[wahl], ziel)) wahl = i;
  return wahl;
}

/**
 * Die Suche über Reihenfolge und Stufen, für eine Sprunggrenze.
 *
 * `belegt[j]` ist der Index des Tons auf Reihe j+1 oder −1 (frei); `rest` die freien Töne als
 * Indizes, aufsteigend. Ein Kandidat c eines Tons ist das Stufenpaar (l, d) mit c = l·nD + d.
 *
 * Gleichstand: es gewinnt die erste Reihenfolge (lexikographisch über die aufsteigenden freien
 * Töne), in ihr die erste Stufe von Reihe 1 und rückwärts je Reihe der erste passende Kandidat
 * (hell aufsteigend, dann dunkel aufsteigend). Ersetzt wird nur bei echt größerem Wert.
 */
function durchsuche(belegt, rest, { stufen, paare, paarMax }, grenze) {
  const U = stufen.length;
  const nL = stufen.map((s) => s.light.length);
  const nD = stufen.map((s) => s.dark.length);
  const kand = stufen.map((s, u) => {
    const lage = { light: lageJeStufe(s.light), dark: lageJeStufe(s.dark) };
    const maske = new Uint8Array(nL[u] * nD[u]);
    for (let l = 0; l < nL[u]; l++) {
      for (let d = 0; d < nD[u]; d++) maske[l * nD[u] + d] = sprung(lage.light[l], lage.dark[d]) <= grenze ? 1 : 0;
    }
    return maske;
  });
  const WL = paare.light, WD = paare.dark;
  // Obere Schranke eines Tonpaars: kein Kandidatenpaar kommt über den größten Wert beider Modi.
  const schranke = (u, v) => Math.min(paarMax.light[u * U + v], paarMax.dark[u * U + v]);

  const u0 = belegt[0];
  const nD0 = nD[u0];
  const anfaenge = [];
  for (let c = 0; c < nL[u0] * nD0; c++) if (kand[u0][c]) anfaenge.push(c);
  const K = anfaenge.length;
  const N = Math.max(...nL.map((l, u) => l * nD[u]));
  const f = Array.from({ length: REIHEN }, () => new Float64Array(K * N));
  const lebend = Array.from({ length: REIHEN }, () => new Int32Array(K));
  const anzahl = new Int32Array(REIHEN);
  for (let k = 0; k < K; k++) lebend[0][k] = k;
  anzahl[0] = K;
  const g = new Float64Array(Math.max(...nL) * Math.max(...nD));
  const zeileLebt = new Uint8Array(Math.max(...nL));
  const ton = new Int32Array(REIHEN);
  ton[0] = u0;

  let B = TOT;
  let beste = null;

  // Reihe 2: nur das Paar (1, 2).
  const anfang = (k, b) => {
    const c0 = anfaenge[k], l0 = (c0 / nD0) | 0, d0 = c0 % nD0;
    const wl = WL[u0 * U + b], wd = WD[u0 * U + b], m = kand[b], nLb = nL[b], nDb = nD[b];
    const aus = f[1], o = k * N;
    let lebt = false;
    for (let l = 0; l < nLb; l++) {
      const sl = wl[l0 * nLb + l];
      for (let d = 0; d < nDb; d++) {
        let v = TOT;
        if (m[l * nDb + d]) {
          const sd = wd[d0 * nDb + d];
          v = sl < sd ? sl : sd;
          if (v > B) lebt = true; else v = TOT;
        }
        aus[o + l * nDb + d] = v;
      }
    }
    return lebt;
  };

  // Ab Reihe 3 (Index j ≥ 2): Kette zur Reihe davor und Stern von Reihe 1. Das Maximum über (l', d') von
  // min(f(l', d'), WL(l', l), WD(d', d)) zerfällt in zwei Schritte, weil min über max verteilt:
  // erst g(l', d) = max_d' min(f(l', d'), WD(d', d)), dann max_l' min(g(l', d), WL(l', l)).
  const kette = (j, k) => {
    const a = ton[j - 1], b = ton[j];
    const c0 = anfaenge[k], l0 = (c0 / nD0) | 0, d0 = c0 % nD0;
    const nLa = nL[a], nDa = nD[a], nLb = nL[b], nDb = nD[b];
    const wl = WL[a * U + b], wd = WD[a * U + b];
    const sternL = WL[u0 * U + b], sternD = WD[u0 * U + b];
    const ein = f[j - 1], aus = f[j], o = k * N, m = kand[b];
    for (let lp = 0; lp < nLa; lp++) {
      zeileLebt[lp] = 0;
      const gz = lp * nDb;
      for (let d = 0; d < nDb; d++) g[gz + d] = TOT;
      for (let dp = 0; dp < nDa; dp++) {
        const v = ein[o + lp * nDa + dp];
        if (!(v > B)) continue;
        zeileLebt[lp] = 1;
        const wz = dp * nDb;
        for (let d = 0; d < nDb; d++) {
          const w = wd[wz + d];
          const x = v < w ? v : w;
          if (x > g[gz + d]) g[gz + d] = x;
        }
      }
    }
    let lebt = false;
    for (let l = 0; l < nLb; l++) {
      const az = o + l * nDb;
      for (let d = 0; d < nDb; d++) aus[az + d] = TOT;
      const sl = sternL[l0 * nLb + l];
      if (!(sl > B)) continue;
      for (let lp = 0; lp < nLa; lp++) {
        if (!zeileLebt[lp]) continue;
        const w = wl[lp * nLb + l];
        if (!(w > B)) continue;
        const gz = lp * nDb;
        for (let d = 0; d < nDb; d++) {
          const x = g[gz + d] < w ? g[gz + d] : w;
          if (x > aus[az + d]) aus[az + d] = x;
        }
      }
      for (let d = 0; d < nDb; d++) {
        let v = aus[az + d];
        if (!m[l * nDb + d]) v = TOT;
        else {
          if (sl < v) v = sl;
          const sd = sternD[d0 * nDb + d];
          if (sd < v) v = sd;
        }
        if (v > B) lebt = true; else v = TOT;
        aus[az + d] = v;
      }
    }
    return lebt;
  };

  // Rechnet Reihe j für alle noch lebenden Stufen von Reihe 1; false, wenn keine übrig bleibt.
  const schritt = (j) => {
    const vor = lebend[j - 1], nach = lebend[j];
    let n = 0;
    for (let i = 0; i < anzahl[j - 1]; i++) {
      const k = vor[i];
      if (j === 1 ? anfang(k, ton[1]) : kette(j, k)) nach[n++] = k;
    }
    anzahl[j] = n;
    return n > 0;
  };

  // Paarwert zweier Kandidaten auf benachbarten Reihen (beide Modi).
  const wert = (a, ca, b, cb) => {
    const x = WL[a * U + b][((ca / nD[a]) | 0) * nL[b] + ((cb / nD[b]) | 0)];
    const y = WD[a * U + b][(ca % nD[a]) * nD[b] + (cb % nD[b])];
    return x < y ? x : y;
  };

  const blatt = () => {
    const letzte = REIHEN - 1, b = ton[letzte], n = nL[b] * nD[b];
    let kBest = -1, cBest = -1;
    for (let i = 0; i < anzahl[letzte]; i++) {
      const k = lebend[letzte][i], o = k * N;
      for (let c = 0; c < n; c++) {
        if (f[letzte][o + c] > B) { B = f[letzte][o + c]; kBest = k; cBest = c; }
      }
    }
    if (kBest < 0) return;
    // Rückwärts: je Reihe der erste Kandidat, der den Wert hält und zum Nachfolger passt.
    const wahl = new Array(REIHEN);
    wahl[letzte] = cBest;
    for (let j = letzte; j >= 2; j--) {
      const a = ton[j - 1], o = kBest * N, n = nL[a] * nD[a];
      let c = 0;
      while (c < n && !(f[j - 1][o + c] >= B && wert(a, c, ton[j], wahl[j]) >= B)) c++;
      // Die DP garantiert einen Vorgänger; fehlt er, ist die Rechnung falsch, nicht die Eingabe.
      if (c === n) throw new Error(`Diagrammreihen: Rückverfolgung findet auf Reihe ${j} keinen Kandidaten`);
      wahl[j - 1] = c;
    }
    wahl[0] = anfaenge[kBest];
    beste = { wert: B, reihe: [...ton], wahl };
  };

  const benutzt = new Uint8Array(rest.length);
  const tiefe = (j) => {
    if (j === REIHEN) { blatt(); return; }
    const setze = (v) => {
      if (schranke(ton[j - 1], v) <= B || (j >= 2 && schranke(u0, v) <= B)) return;
      ton[j] = v;
      if (schritt(j)) tiefe(j + 1);
    };
    if (belegt[j] >= 0) { setze(belegt[j]); return; }
    let zuletzt = -1;
    for (let i = 0; i < rest.length; i++) {
      // Gleiche Töne geben gleiche Teilbäume: je Reihe nur einmal.
      if (benutzt[i] || rest[i] === zuletzt) continue;
      zuletzt = rest[i];
      benutzt[i] = 1;
      setze(rest[i]);
      benutzt[i] = 0;
    }
  };
  tiefe(1);
  return beste;
}

/** Rechnet die Reihen für eine geprüfte, normierte Eingabe (siehe `diagrammReihen`). */
function rechneReihen({ frei, fest, marke, gruende }) {
  const { chroma, unbunt } = markenChroma(marke);
  const belegtGrad = new Array(REIHEN).fill(null);
  for (const [nr, t] of fest) belegtGrad[nr - 1] = t;
  let restGrad = frei;
  if (belegtGrad[0] === null) {
    const i = ersteReihe(frei, marke, unbunt);
    belegtGrad[0] = frei[i];
    restGrad = frei.filter((_, k) => k !== i);
  }

  // Je verschiedenem Ton einmal: Stufen, OKLab und die Paarmatrizen beider Modi. Die Matrix des
  // Tonpaars (u, v) im Modus m ist paare[m][u·U + v], Zeile = Stufe von u, Spalte = Stufe von v.
  const grade = [...new Set([...belegtGrad.filter((t) => t !== null), ...restGrad])].sort((a, b) => a - b);
  const U = grade.length;
  const daten = { stufen: [], ersatz: [], paare: {}, paarMax: {} };
  for (const t of grade) {
    const je = Object.fromEntries(MODI.map((m) => [m, stufenMitErsatz(t, chroma, m, gruende[m])]));
    daten.stufen.push({ light: je.light.stufen, dark: je.dark.stufen });
    daten.ersatz.push({ light: je.light.ersatz, dark: je.dark.ersatz });
  }
  for (const m of MODI) {
    const lab = daten.stufen.map((s) => s[m].map(({ hex }) => labDreifach(hex)));
    const paare = new Array(U * U), max = new Float64Array(U * U);
    for (let u = 0; u < U; u++) {
      for (let v = u; v < U; v++) {
        const nu = lab[u].length, nv = lab[v].length;
        const hin = new Float64Array(nu * nv), her = new Float64Array(nv * nu);
        let hoechst = 0;
        for (let i = 0; i < nu; i++) {
          for (let k = 0; k < nv; k++) {
            const w = paarWert(lab[u][i], lab[v][k]);
            hin[i * nv + k] = w;
            her[k * nu + i] = w;
            if (w > hoechst) hoechst = w;
          }
        }
        paare[u * U + v] = hin;
        paare[v * U + u] = her;
        max[u * U + v] = max[v * U + u] = hoechst;
      }
    }
    daten.paare[m] = paare;
    daten.paarMax[m] = max;
  }

  const index = (t) => grade.indexOf(t);
  const belegt = belegtGrad.map((t) => (t === null ? -1 : index(t)));
  const rest = restGrad.map(index);
  // Erst mit der Sprunggrenze. Hält das die Schwellen nicht, ein zweiter Lauf ohne sie; sein
  // Ergebnis zählt nur, wenn es echt besser ist. Bei gleichem Abstand bleibt der kleine Sprung.
  let beste = durchsuche(belegt, rest, daten, SPRUNG_MAX);
  if (beste.wert < 1) {
    const ohneGrenze = durchsuche(belegt, rest, daten, Infinity);
    if (ohneGrenze.wert > beste.wert) beste = ohneGrenze;
  }

  const reihe = beste.reihe.map((u) => grade[u]);
  const farben = {};
  for (const m of MODI) {
    farben[m] = beste.wahl.map((c, j) => {
      const s = daten.stufen[beste.reihe[j]], nDu = s.dark.length;
      return s[m][m === 'light' ? (c / nDu) | 0 : c % nDu].hex;
    });
  }
  const je = { light: pruefeReihen(farben.light), dark: pruefeReihen(farben.dark) };
  const modus = je.dark.abstand < je.light.abstand ? 'dark' : 'light';
  const ohneStufe = [];
  beste.reihe.forEach((u, j) => {
    for (const m of MODI) if (daten.ersatz[u][m]) ohneStufe.push({ nummer: j + 1, modus: m });
  });
  return {
    toene: reihe,
    light: farben.light,
    dark: farben.dark,
    abstand: je[modus].abstand,
    schwaechstes: { modus, ...je[modus].schwaechstes },
    ohneStufe,
  };
}

/** Gemerkte Ergebnisse je normierter Eingabe: `derive` rechnet dieselbe Eingabe oft. */
const GEMERKT = new Map();

/**
 * Reihenfolge der Kategorien und `mark` je Modus, gerechnet (Spec §2–§4, §6, §11).
 *
 * Eingabe: `toene` die freien Töne in Grad (jede Schreibweise, −122° gilt wie 238°; zurück kommen die
 * Töne in 0–360), `fest` gelieferte Kategorien (Nummer 1–8 → Grad, sie
 * behalten ihre Nummer), zusammen genau acht; `marke` der Hexwert der Marke; `gruende` je Modus
 * `surface-base` und `surface-layer`.
 *
 * Rückgabe in der Reihenfolge der Kategorien 1–8: `toene` (Grad), `light` und `dark` (Hex).
 * `abstand` und `schwaechstes` sind die des Prüfers über beide Modi (`schwaechstes.modus` nennt
 * den Modus; bei Gleichstand hell); `abstand ≥ 1` heißt: alle Schwellen erfüllt.
 * `ohneStufe` listet `{ nummer, modus }` (Nummer 1–8 in dieser Reihenfolge) für jede Reihe, deren
 * Ton in dem Modus keine zulässige Stufe trägt; leer, wenn es keine gibt. Für sie gilt die eine
 * Stufe des Bands mit dem höchsten Kontrast gegen den schwächeren Grund (gleiche Chroma, Raster
 * 0,02), auch wenn sie 3:1 oder Chroma 0,10 verfehlt; der Rest der Suche bleibt gleich.
 *
 * Reihe 1 ist der freie Ton nächst der Marke (bei unbunter Marke der kleinste), es sei denn,
 * Kategorie 1 ist geliefert. Chroma: `clamp(C der Marke, 0,10, 0,15)`, unbunt 0,10. Je Ton gelten
 * nur Stufenpaare mit normiertem Helligkeitssprung ≤ 0,5; hält das `abstand ≥ 1` nicht, rechnet
 * die Suche ohne diese Grenze erneut und nimmt deren Ergebnis nur, wenn es echt besser ist.
 *
 * Deterministisch und unabhängig von der Reihenfolge der Eingabetöne; das Ergebnis wird je
 * Eingabe gemerkt, jeder Aufruf bekommt eine eigene Kopie. Wirft, wenn es nicht genau acht Töne
 * sind oder eine Nummer außerhalb 1–8 liegt.
 */
export function diagrammReihen(eingabe) {
  const normiert = normiereEingabe(eingabe);
  const schluessel = JSON.stringify(normiert);
  let ergebnis = GEMERKT.get(schluessel);
  if (!ergebnis) {
    ergebnis = rechneReihen(normiert);
    GEMERKT.set(schluessel, ergebnis);
  }
  return structuredClone(ergebnis);
}

/**
 * Größter normierter Helligkeitssprung einer Reihe zwischen hell und dunkel (Spec §3).
 *
 * Die Stufen jedes Tons rechnet die Funktion aus `eingabe` neu (Marke und Gründe wie bei
 * `diagrammReihen`, samt Ersatzstufe); je Modus ist die Lage der gewählten Stufe
 * (L − L_min) / (L_max − L_min) über die Stufen des Tons, 0 bei nur einer, der Sprung der Betrag
 * der Differenz beider Lagen.
 */
export function groessterSprung(ergebnis, { marke, gruende }) {
  const { chroma } = markenChroma(marke);
  let groesster = 0;
  // Auch hier gilt die Schreibweise 0–360 (wie in `diagrammReihen`); der Hexwert ändert sich dadurch nicht.
  ergebnis.toene.map(mod360).forEach((ton, k) => {
    const [hell, dunkel] = MODI.map((m) => {
      const { stufen } = stufenMitErsatz(ton, chroma, m, gruende[m]);
      const i = stufen.findIndex(({ hex }) => hex === ergebnis[m][k]);
      if (i < 0) throw new Error(`Reihe ${k + 1}: ${ergebnis[m][k]} ist keine zulässige Stufe von ${ton}° im Modus ${m}`);
      return lageJeStufe(stufen)[i];
    });
    groesster = Math.max(groesster, sprung(hell, dunkel));
  });
  return groesster;
}
