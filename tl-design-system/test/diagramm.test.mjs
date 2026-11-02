import { test } from 'node:test';
import assert from 'node:assert/strict';
import { DIAGRAMM, diagrammPaare, pruefeReihen, zulaessigeStufen, diagrammReihen, groessterSprung } from '../tools/lib/diagramm.mjs';
import { ratio } from '../tools/lib/contrast.mjs';
import { hexZuOklch, oklchZuHex } from '../tools/lib/color.mjs';

test('Paarliste: Nachbarn und der Ring', () => {
  assert.deepEqual(diagrammPaare(2), [[0, 1]]);
  assert.deepEqual(diagrammPaare(4), [[0, 1], [1, 2], [2, 3], [0, 2], [0, 3]]);
  assert.equal(diagrammPaare(8).length, 7 + 6);
});

test('der Prüfer nennt das schwächste Paar der heutigen 5d-Palette', () => {
  const heute = ['#AA4969', '#A45502', '#5B7400', '#00786F', '#0071A3', '#6760B5', '#8756A6', '#9D4D8A'];
  const r = pruefeReihen(heute);
  assert.ok(r.abstand < 0.5);
  assert.equal(r.schwaechstes.art, 'rot-gruen');
  assert.deepEqual(r.schwaechstes.reihen, [2, 3]);
  assert.equal(r.schwaechstes.soll, DIAGRAMM.ROT_GRUEN);
  assert.ok(Math.abs(r.abstand - r.schwaechstes.wert / r.schwaechstes.soll) < 1e-12);
});

test('zulässige Stufen halten 3:1 auf beiden Gründen und liegen im Band', () => {
  for (const [modus, gruende] of [['light', ['#FAFAFA', '#FFFFFF']], ['dark', ['#16181B', '#1D2023']]]) {
    const s = zulaessigeStufen(238, 0.15, modus, gruende);
    assert.ok(s.length >= 5, modus);
    for (const { L, hex } of s) {
      assert.ok(L >= DIAGRAMM.BAND[modus][0] && L <= DIAGRAMM.BAND[modus][1]);
      for (const g of gruende) assert.ok(ratio(hex, g) >= 3.0, `${modus} ${hex} auf ${g}`);
    }
  }
});

test('zulässige Stufen: Raster 0,02 ab der Untergrenze, aufsteigend, ohne Drift', () => {
  for (const [modus, gruende] of [['light', ['#FAFAFA', '#FFFFFF']], ['dark', ['#16181B', '#1D2023']]]) {
    const [unten, oben] = DIAGRAMM.BAND[modus];
    const s = zulaessigeStufen(238, 0.15, modus, gruende);
    for (let i = 0; i < s.length; i++) {
      // L ist exakt die Hundertstel-Zahl durch 100, keine aufsummierte Drift.
      assert.ok(Math.abs(Math.round(s[i].L * 100) - s[i].L * 100) < 1e-9, `L ${s[i].L}`);
      assert.equal(s[i].L, Math.round(s[i].L * 100) / 100);
      assert.equal((Math.round(s[i].L * 100) - Math.round(unten * 100)) % 2, 0, `${modus} ${s[i].L}`);
      if (i > 0) assert.equal(Math.round((s[i].L - s[i - 1].L) * 100), 2);
    }
    assert.ok(s.at(-1).L <= oben);
  }
  // Dunkel: 0,77 liegt zwischen zwei Stufen, die letzte ist 0,76; hell reicht das Band bis 0,77.
  assert.equal(zulaessigeStufen(238, 0.15, 'dark', ['#16181B', '#1D2023']).at(-1).L, 0.76);
});

test('zulässige Stufen: der Kontrast schneidet die dunkle Seite ab', () => {
  // 0,48 auf #1D2023 bringt nur 2,55:1, 0,52 trägt 3:1.
  assert.equal(zulaessigeStufen(238, 0.15, 'dark', ['#16181B', '#1D2023'])[0].L, 0.52);
  // Hell: das Band beginnt bei 0,46 (Nachtrag 23.10.); die erste Stufe des Blautons trägt C 0,10.
  const hell = zulaessigeStufen(238, 0.15, 'light', ['#FAFAFA', '#FFFFFF']);
  assert.equal(hell[0].L, 0.46);
  assert.ok(hexZuOklch(hell[0].hex).C >= DIAGRAMM.CHROMA_MIN - 2e-3);
});

test('im hellen Modus beginnt Rot bei 0,50, warm bei 0,54, sonst bei 0,46 (Spec §3, Nachtrag 23.10.)', () => {
  const G = ['#FAFAFA', '#FFFFFF'];
  const erste = (ton, modus = 'light') => zulaessigeStufen(ton, 0.15, modus, G)[0].L;
  assert.ok(erste(2) >= 0.50, `Rot ${erste(2)}`);
  assert.ok(erste(56) >= 0.54, `warm ${erste(56)}`);
  assert.ok(erste(238) >= 0.46 && erste(238) < 0.50, `sonst ${erste(238)}`);
  // Grenzfälle: 345° und 0° sind Rot, 29° noch Rot, 30° und 110° warm, 111° wieder „sonst“.
  for (const ton of [345, 350, 0, 29]) assert.ok(erste(ton) >= 0.50, `Rot ${ton}°: ${erste(ton)}`);
  for (const ton of [30, 110]) assert.ok(erste(ton) >= 0.54, `warm ${ton}°: ${erste(ton)}`);
  for (const ton of [344, 111]) assert.ok(erste(ton) < 0.50, `sonst ${ton}°: ${erste(ton)}`);
  // Töne außerhalb 0–360 werden normalisiert: −10° = 350° (Rot), 370° = 10° (Rot), 420° = 60° (warm).
  assert.ok(erste(-10) >= 0.50 && erste(370) >= 0.50 && erste(420) >= 0.54);
  // Das Raster beginnt an der Untergrenze des Tons und bleibt 0,02, der Hexwert der ersten Stufe liegt auf ihr.
  const rot = zulaessigeStufen(2, 0.15, 'light', G);
  assert.equal(Math.round(rot[0].L * 100) % 2, 0);
  for (let i = 1; i < rot.length; i++) assert.equal(Math.round((rot[i].L - rot[i - 1].L) * 100), 2);
  // Die Obergrenze bleibt für alle Töne 0,77, das Band hell beginnt bei 0,46.
  assert.deepEqual(DIAGRAMM.BAND.light, [0.46, 0.77]);
  for (const ton of [2, 56, 238]) assert.ok(zulaessigeStufen(ton, 0.15, 'light', G).at(-1).L <= 0.77);
});

test('der dunkle Modus bleibt: keine Untergrenze je Ton, nur der Kontrast schneidet', () => {
  const G = ['#16181B', '#1D2023'];
  // Werte wie vor dem Nachtrag (erste und letzte Stufe, Anzahl).
  for (const [ton, erste, letzte, anzahl] of [[2, 0.56, 0.76, 11], [56, 0.54, 0.76, 12], [238, 0.52, 0.76, 13]]) {
    const s = zulaessigeStufen(ton, 0.15, 'dark', G);
    assert.deepEqual([s[0].L, s.at(-1).L, s.length], [erste, letzte, anzahl], `${ton}°`);
  }
  assert.deepEqual(DIAGRAMM.BAND.dark, [0.48, 0.77]);
});

test('zulässige Stufen: keine Stufe unter Chroma 0,10 abzüglich Toleranz', () => {
  // Gelbgrün (H 125) trägt auf der dunklen Seite des Hellbands nicht C 0,10: die Stufen fallen weg.
  const alle = zulaessigeStufen(125, 0.15, 'light', ['#FAFAFA', '#FFFFFF']);
  for (const { hex } of alle) assert.ok(hexZuOklch(hex).C >= DIAGRAMM.CHROMA_MIN - 2e-3, hex);
  // Mit Chroma-Wunsch 0,05 ist jede Stufe zu blass: nichts bleibt übrig.
  assert.deepEqual(zulaessigeStufen(238, 0.05, 'light', ['#FAFAFA', '#FFFFFF']), []);
});

// --- Die Suche: Reihenfolge und Helligkeit je Modus ---

const TL = { toene: [2, 56, 123, 186, 238, 285, 311, 337], marke: '#0B5ED7',
  gruende: { light: ['#FAFAFA', '#FFFFFF'], dark: ['#16181B', '#1D2023'] } };
test('TopLogic: alle Schwellen erfüllt, Reihe 1 neben der Marke', () => {
  const r = diagrammReihen(TL);
  assert.ok(r.abstand >= 1, JSON.stringify(r.schwaechstes));
  assert.equal(r.toene[0], 238);
  assert.deepEqual([...r.toene].sort((a, b) => a - b), TL.toene);
  for (const m of ['light', 'dark']) assert.ok(pruefeReihen(r[m]).abstand >= 1, m);
});
test('die Schreibweise der Töne ändert nichts: rohe Winkel (negativ) und 0–360 geben dasselbe Ergebnis', () => {
  // Wie `hexZuOklch` sie liefert (atan2, −180…180): TopLogic hat zwei Reihenfolgen mit gleichem Abstand,
  // die Schreibweise darf nicht zwischen ihnen entscheiden (Spec §5).
  const roh = TL.toene.map((t) => (t > 180 ? t - 360 : t));
  assert.ok(roh.some((t) => t < 0));
  const a = diagrammReihen(TL);
  const b = diagrammReihen({ ...TL, toene: roh });
  assert.deepEqual(b, a);
  assert.deepEqual(a.toene, [238, 2, 285, 186, 56, 311, 123, 337]);
  // Auch Töne ≥ 360, gelieferte Kategorien und das Sprungmaß.
  assert.deepEqual(diagrammReihen({ ...TL, toene: TL.toene.map((t) => t + 360) }), a);
  const fest = { toene: [2, 56, 123, 186, 285, 311, 337], fest: { 3: 238 } };
  assert.deepEqual(diagrammReihen({ ...TL, ...fest, fest: { 3: -122 } }), diagrammReihen({ ...TL, ...fest }));
  assert.equal(groessterSprung({ ...a, toene: roh.map((t, k) => (a.toene[k] > 180 ? a.toene[k] - 360 : a.toene[k])) }, TL), groessterSprung(a, TL));
});
test('Probe-Marke (orange): alle Schwellen erfüllt', () => {
  const r = diagrammReihen({ ...TL, toene: [111, 136, 187, 242, 271, 301, 330, 359], marke: '#E8541E' });
  assert.ok(r.abstand >= 1, JSON.stringify(r.schwaechstes));
});
test('deterministisch, unabhängig von der Reihenfolge der Eingabe', () => {
  assert.deepEqual(diagrammReihen(TL), diagrammReihen({ ...TL, toene: [...TL.toene].reverse() }));
});
test('Chroma zwischen 0,10 und 0,15', () => {
  for (const hex of [...diagrammReihen(TL).light, ...diagrammReihen(TL).dark]) {
    const { C } = hexZuOklch(hex);
    assert.ok(C >= 0.10 - 2e-3 && C <= 0.15 + 2e-3, `${hex} C ${C}`);
  }
});
test('eine unbunte Marke wirft nicht, nimmt Chroma 0,10 und beginnt mit dem kleinsten Ton', () => {
  const r = diagrammReihen({ ...TL, marke: '#6B6B6B' });
  assert.equal(r.toene[0], 2);
  for (const hex of r.light) assert.ok(hexZuOklch(hex).C <= 0.10 + 2e-3);
});
test('der Helligkeitssprung je Reihe bleibt bei TopLogic und der Probe-Marke ≤ 0,5', () => {
  for (const eingabe of [TL, { ...TL, toene: [111, 136, 187, 242, 271, 301, 330, 359], marke: '#E8541E' }])
    assert.ok(groessterSprung(diagrammReihen(eingabe), eingabe) <= 0.5);
});
test('acht gelieferte Kategorien behalten ihre Nummern', () => {
  const fest = Object.fromEntries(TL.toene.map((t, i) => [i + 1, t]));
  assert.deepEqual(diagrammReihen({ ...TL, toene: [], fest }).toene, TL.toene);
});
test('eine gelieferte Kategorie behält ihre Nummer, die anderen ordnen sich um sie', () => {
  const r = diagrammReihen({ ...TL, toene: TL.toene.filter((t) => t !== 123), fest: { 4: 123 } });
  assert.equal(r.toene[3], 123);
});

test('abstand und schwaechstes sind die des Prüfers, über beide Modi', () => {
  const r = diagrammReihen(TL);
  const je = { light: pruefeReihen(r.light), dark: pruefeReihen(r.dark) };
  assert.equal(r.abstand, Math.min(je.light.abstand, je.dark.abstand));
  assert.deepEqual(r.schwaechstes, { modus: r.schwaechstes.modus, ...je[r.schwaechstes.modus].schwaechstes });
  assert.equal(je[r.schwaechstes.modus].abstand, r.abstand);
  // Jede Marke ist eine zulässige Stufe ihres Tons: Kontrast und Chroma hält die Suche nie verfehlt.
  r.toene.forEach((ton, k) => {
    for (const m of ['light', 'dark']) {
      const stufen = zulaessigeStufen(ton, 0.15, m, TL.gruende[m]).map((s) => s.hex);
      assert.ok(stufen.includes(r[m][k]), `${m} Reihe ${k + 1}`);
    }
  });
});

test('das gemerkte Ergebnis lässt sich vom Aufrufer nicht verändern', () => {
  const r = diagrammReihen(TL);
  const vorher = structuredClone(r);
  r.light[0] = '#000000';
  r.toene.reverse();
  assert.deepEqual(diagrammReihen(TL), vorher);
});

test('nicht genau acht Töne oder eine Nummer außerhalb 1–8 werfen', () => {
  assert.throws(() => diagrammReihen({ ...TL, toene: TL.toene.slice(1) }), /8/);
  assert.throws(() => diagrammReihen({ ...TL, toene: TL.toene.slice(1), fest: { 9: 56 } }), /1–8/);
});

test('der Lauf ohne Sprunggrenze zählt nur, wenn er den Abstand echt erhöht', () => {
  // Drei gelieferte Kategorien mit demselben Ton: keine Belegung hält die Schwellen. Mit und
  // ohne Grenze erreicht die Suche denselben Abstand 0,6796; ohne Grenze springt eine Reihe um
  // 0,636, mit Grenze höchstens um 0,167. Der kleine Sprung muss bleiben.
  const e = { ...TL, toene: [123, 186, 285, 311, 337], fest: { 1: 238, 2: 238, 3: 238 } };
  const r = diagrammReihen(e);
  assert.ok(r.abstand < 1, String(r.abstand));
  assert.deepEqual(r.toene.slice(0, 3), [238, 238, 238]);
  assert.ok(groessterSprung(r, e) <= 0.5, String(groessterSprung(r, e)));
});

test('ein Ton ohne zulässige Stufe bricht nicht ab: Ersatz mit dem höchsten Kontrast, gemeldet', () => {
  // Gründe so dunkel, dass 200° keine Stufe trägt (das Raster ab 0,46 liegt auf geraden Hundertsteln:
  // mit #E0E0E0 trüge 0,58 noch 3:1, mit #D8D8D8 nicht mehr).
  const gruende = { light: ['#D8D8D8', '#EEEEEE'], dark: TL.gruende.dark };
  const e = { ...TL, toene: [2, 56, 123, 200, 238, 285, 311, 337], gruende };
  assert.deepEqual(zulaessigeStufen(200, 0.15, 'light', gruende.light), []);
  const r = diagrammReihen(e);
  const k = r.toene.indexOf(200);
  assert.deepEqual(r.ohneStufe, [{ nummer: k + 1, modus: 'light' }]);
  // Ersatz: von allen Stufen des Bands (Raster 0,02, gleiche Chroma) die mit dem höchsten
  // Kontrast gegen den schwächeren Grund.
  const schwaecher = (hex) => Math.min(...gruende.light.map((g) => ratio(hex, g)));
  const band = [];
  for (let h = 46; h <= 77; h += 2) band.push(oklchZuHex({ L: h / 100, C: 0.15, H: (200 * Math.PI) / 180 }).hex);
  assert.equal(schwaecher(r.light[k]), Math.max(...band.map(schwaecher)));
  // Eine einzelne Stufe hat die Lage 0; Sprungmaß und Vorauswahl kommen damit zurecht.
  assert.ok(groessterSprung(r, e) <= 0.5);
  assert.deepEqual(diagrammReihen(TL).ohneStufe, []);
});
