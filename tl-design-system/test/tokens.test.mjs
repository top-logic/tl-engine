// Jede Regel aus docs/skalen.md als Test. Gerechnet auf den aufgelösten Werten.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { ratio, rgba, ueberlagert } from '../tools/lib/contrast.mjs';
import { farbtonAbstand, FARBTON_ABSTAND_MIN, hexZuOklch } from '../tools/lib/color.mjs';
import { resolve } from '../tools/resolve.mjs';
import { diagrammReihen, pruefeReihen } from '../tools/lib/diagramm.mjs';

const lum = (hex) => hexZuOklch(hex).L;

const P = JSON.parse(readFileSync(new URL('../src/primitives.json', import.meta.url)));
const T = JSON.parse(readFileSync(new URL('../src/tokens.json', import.meta.url)));
const R = resolve(P, T);
const MODES = ['light', 'dark'];
const HEX = /^#[0-9A-F]{6}$/;

test('jeder Farbwert ist ein großgeschriebenes 6-stelliges Hex', () => {
  for (const [n, v] of Object.entries(P.color)) assert.match(v, HEX, `${n} = ${v}`);
});

test('Text und Linien erfüllen ihre Anforderung gegen die richtige Fläche', () => {
  const forderung = [
    ['text-primary', 'surface-base', 16.0], ['text-secondary', 'surface-base', 7.0],
    ['text-helper', 'surface-base', 5.2], ['text-placeholder', 'surface-field', 4.6],
    ['text-link', 'surface-base', 4.5], ['text-link-hover', 'surface-base', 4.5],
    ['icon-primary', 'surface-base', 3.0], ['icon-secondary', 'surface-base', 3.0],
    ['border-emphasis', 'surface-base', 4.6], ['border-control', 'surface-base', 3.0],
    ['border-interactive', 'surface-base', 3.0], ['focus-ring', 'surface-base', 3.0],
    ['text-link', 'surface-brand-subtle', 4.5], ['text-primary', 'surface-brand-subtle', 4.5],
  ];
  for (const m of MODES) for (const [fg, bg, soll] of forderung) {
    const ist = ratio(R.color[m][fg], R.color[m][bg]);
    assert.ok(ist >= soll, `${m}: ${fg} auf ${bg} = ${ist.toFixed(2)}, gefordert ${soll}`);
  }
});

test('Text steht auf jeder Fläche, auf der er liegen kann', () => {
  for (const m of MODES) for (const bg of ['surface-layer', 'surface-layer-nested', 'surface-overlay',
    'surface-interactive-hover', 'surface-interactive-strong', 'surface-interactive-active']) {
    const ist = ratio(R.color[m]['text-primary'], R.color[m][bg]);
    assert.ok(ist >= 4.5, `${m}: text-primary auf ${bg} = ${ist.toFixed(2)}`);
  }
});

// Nicht in kontrastPaare: surface-brand ist dort Grund für text-on-brand, und die Paarliste verbietet
// eine Rolle, die zugleich Grund und Vordergrund ist (derive führt in einem Durchgang nach).
test('die Marke hebt sich von jedem Grund ab, auf dem sie als Punkt oder Kästchen liegt', () => {
  for (const m of MODES) for (const bg of ['surface-base', 'surface-layer', 'surface-layer-nested', 'surface-overlay', 'surface-field']) {
    const ist = ratio(R.color[m]['surface-brand'], R.color[m][bg]);
    assert.ok(ist >= 3.0, `${m}: surface-brand auf ${bg} = ${ist.toFixed(2)}, gefordert 3,0`);
  }
});

test('border-separator trennt nur und schuldet keinen Kontrast', () => {
  for (const m of MODES) {
    const ist = ratio(R.color[m]['border-separator'], R.color[m]['surface-base']);
    assert.ok(ist < 3.0, `${m}: ein Separator mit ${ist.toFixed(2)} wäre ein Control-Rahmen`);
  }
});

test('jede Statusbedeutung erfüllt text 4,5 und border 3,0 auf dem Grund — ein getönter Streifen trägt gewöhnlichen Text', () => {
  for (const m of MODES) for (const s of ['error', 'warning', 'success', 'info']) {
    const bg = R.color[m]['surface-base'];
    const t = ratio(R.color[m][`status-${s}-text`], bg);
    const b = ratio(R.color[m][`status-${s}-border`], bg);
    assert.ok(t >= 4.5, `${m}: status-${s}-text = ${t.toFixed(2)}`);
    assert.ok(b >= 3.0, `${m}: status-${s}-border = ${b.toFixed(2)}`);

    // Status-Text steht nicht nur auf dem Grund, sondern auf jeder Fläche, auf der ein Panel
    // liegen kann — surface-layer ist die zweite Fläche, auf der der Button (und jede andere
    // Komponente) diesen Text trägt.
    const layer = ratio(R.color[m][`status-${s}-text`], R.color[m]['surface-layer']);
    assert.ok(layer >= 4.5, `${m}: status-${s}-text auf surface-layer = ${layer.toFixed(2)}`);

    // Dieselbe Anforderung wie beim Text gilt für die Linie: eine Umriss-Erscheinung liegt
    // ebenso auf surface-layer wie auf dem Grund.
    const borderLayer = ratio(R.color[m][`status-${s}-border`], R.color[m]['surface-layer']);
    assert.ok(borderLayer >= 3.0, `${m}: status-${s}-border auf surface-layer = ${borderLayer.toFixed(2)}`);

    // status-X-text ist für den Grund (surface-base) gerechnet, nicht für die eigene
    // subtle-Fläche. Ein getönter Hinweisstreifen (background: subtle) nimmt darum
    // gewöhnlichen Text (text-primary), nicht status-X-text — das Paar
    // status-X-text × status-X-subtle ist von der Skala nicht versprochen und wird hier
    // nur dokumentierend berechnet, nicht erzwungen.
    const textAufSubtle = ratio(R.color[m][`status-${s}-text`], R.color[m][`status-${s}-subtle`]);
    void textAufSubtle; // darf unter 4,5 liegen — kein Textpaar

    const sub = ratio(R.color[m]['text-primary'], R.color[m][`status-${s}-subtle`]);
    assert.ok(sub >= 4.5, `${m}: text-primary auf status-${s}-subtle = ${sub.toFixed(2)}`);
  }
});

test('auf jeder gefüllten Fläche trägt die erzeugte Beschriftung', () => {
  for (const m of MODES) for (const [name, { surface }] of Object.entries(T.derived[m])) {
    const soll = name.startsWith('icon-') ? 3.0 : 4.5;
    const ist = ratio(R.derived[m][name], R.color[m][surface]);
    assert.ok(ist >= soll, `${m}: ${name} auf ${surface} = ${ist.toFixed(2)}`);
  }
});

test('der Fokusring erreicht 3,0 gegen jede Fläche, auf der er liegen kann', () => {
  for (const m of MODES) for (const bg of ['surface-base', 'surface-layer', 'surface-layer-nested',
    'surface-overlay', 'surface-field', 'surface-brand-subtle']) {
    const ist = ratio(R.color[m]['focus-ring'], R.color[m][bg]);
    assert.ok(ist >= 3.0, `${m}: focus-ring auf ${bg} = ${ist.toFixed(2)}`);
  }
});

test('getönte Flächen verschiedener Bedeutung trennen sich im Farbton', () => {
  for (const m of MODES) {
    const subtle = ['surface-brand-subtle', 'status-error-subtle', 'status-warning-subtle',
      'status-success-subtle', 'status-info-subtle'].map((n) => [n, R.color[m][n]]);
    for (let i = 0; i < subtle.length; i++) for (let j = i + 1; j < subtle.length; j++) {
      const d = farbtonAbstand(subtle[i][1], subtle[j][1]);
      assert.ok(d >= FARBTON_ABSTAND_MIN, `${m}: ${subtle[i][0]} ↔ ${subtle[j][0]} = ${d.toFixed(1)}°`);
    }
  }
});

test('Schatten trägt nur, was schwebt — drei Stufen je Modus, keine raised', () => {
  for (const m of MODES) {
    const names = Object.keys(R.effect[m]).filter((n) => n.startsWith('shadow-')).sort();
    assert.deepEqual(names, ['shadow-dialog', 'shadow-drag', 'shadow-popover']);
  }
});

// docs/skalen.md „Höfe sind durchscheinend“: eine Deckkraft hat keinen Kontrast für sich; gemessen wird die Schicht
// über jedem Grund, auf dem ein Hof liegt, gegen diesen Grund.
test('die Zustandsschichten sind durchscheinend und heben sich auf jedem Grund ab, Drücken kräftiger als Zeigen', () => {
  const gruende = ['surface-base', 'surface-layer', 'surface-layer-nested', 'surface-overlay', 'surface-field'];
  for (const m of MODES) {
    for (const s of ['surface-state-hover', 'surface-state-active']) {
      const { a } = rgba(R.effect[m][s]);
      assert.ok(a > 0 && a < 1, `${m}: ${s} ist nicht durchscheinend (${a})`);
      // Durchscheinend heißt: eine Linie darunter bleibt eine Linie. Mehr als ein Viertel Deckung wäre eine Fläche.
      assert.ok(a <= 0.25, `${m}: ${s} deckt ${a}`);
    }
    for (const g of gruende) {
      const grund = R.color[m][g];
      const zeigen = ueberlagert(R.effect[m]['surface-state-hover'], grund);
      const druecken = ueberlagert(R.effect[m]['surface-state-active'], grund);
      assert.ok(ratio(zeigen, grund) >= 1.1, `${m}: surface-state-hover auf ${g} = ${ratio(zeigen, grund).toFixed(2)}`);
      assert.ok(ratio(druecken, grund) >= 1.2, `${m}: surface-state-active auf ${g} = ${ratio(druecken, grund).toFixed(2)}`);
      assert.ok(ratio(druecken, zeigen) >= 1.1, `${m}: Drücken gegen Zeigen auf ${g} = ${ratio(druecken, zeigen).toFixed(2)}`);
    }
  }
});

// Nachtrag 4c (S3, 17.10.): um eine große Karte trägt der Saum von 6 % nicht; die Fläche hat eine eigene, kräftigere Rolle.
test('der Hof um Flächen (surface-state-hover-strong): durchscheinend, auf jedem Grund ≥ 1,3, stärker als Drücken', () => {
  const gruende = ['surface-base', 'surface-layer', 'surface-layer-nested', 'surface-overlay', 'surface-field'];
  for (const m of MODES) {
    const { a } = rgba(R.effect[m]['surface-state-hover-strong']);
    assert.ok(a > 0 && a <= 0.25, `${m}: surface-state-hover-strong deckt ${a}`);
    for (const g of gruende) {
      const grund = R.color[m][g];
      const stark = ratio(ueberlagert(R.effect[m]['surface-state-hover-strong'], grund), grund);
      const druecken = ratio(ueberlagert(R.effect[m]['surface-state-active'], grund), grund);
      assert.ok(stark >= 1.3, `${m}: hover-strong auf ${g} = ${stark.toFixed(2)}`);
      assert.ok(stark > druecken, `${m}: hover-strong ${stark.toFixed(2)} nicht stärker als active ${druecken.toFixed(2)} auf ${g}`);
    }
  }
});

test('jede rem-Größe ist ein glattes Sechzehntel', () => {
  for (const [n, v] of Object.entries(P.measure)) {
    const m = /^([\d.]+)rem$/.exec(v);
    if (m) assert.ok(Number.isInteger(parseFloat(m[1]) * 16), `${n} = ${v} ist kein ganzes Pixel`);
  }
});

test('Abstände, Zeilen- und Control-Höhen liegen auf dem 4-px-Raster', () => {
  for (const [n, v] of Object.entries(P.measure)) {
    if (!/^(size|height)-/.test(n)) continue;
    const px = parseFloat(v) * 16;
    assert.equal(px % 4, 0, `${n} = ${px}px`);
  }
});

test('keine Schriftgröße unterschreitet 12 px', () => {
  for (const [n, v] of Object.entries(P.measure))
    if (n.startsWith('text-')) assert.ok(parseFloat(v) * 16 >= 12, `${n} = ${v}`);
});

test('Linien stehen in px, Maße in rem', () => {
  for (const [n, v] of Object.entries(P.measure)) {
    if (n.startsWith('line-')) assert.match(v, /px$/, n);
    if (/^(size|text|height)-/.test(n)) assert.match(v, /rem$/, n);
  }
});

test('die kleine Control-Höhe trägt die Trefferfläche aus WCAG 2.5.8', () => {
  for (const d of ['normal', 'compact'])
    assert.ok(parseFloat(R.density[d]['size-control-sm']) * 16 >= 24, d);
});

test('eine Leiste ist Control-Höhe plus space-2xs oben und unten, in beiden Dichten', () => {
  for (const d of ['normal', 'compact']) {
    const bar = parseFloat(R.density[d]['size-bar']);
    const control = parseFloat(R.density[d]['size-control']);
    assert.equal(bar, control + 2 * parseFloat(R.measure['space-2xs']), d);
  }
});

test('Zeilenhöhen der Typo-Klassen liegen auf dem 4-px-Raster', () => {
  for (const [n, k] of Object.entries(T.type)) {
    if (n.startsWith('_')) continue;
    const px = parseFloat(R.measure[`font-size-${k.size}`]) * 16 * k.lineHeight;
    assert.ok(Math.abs(px - Math.round(px)) < 0.01 && Math.round(px) % 4 === 0, `${n}: ${px}px`);
  }
});

test('die Ebenen steigen streng', () => {
  const v = ['layer-base', 'layer-raised', 'layer-sticky', 'layer-overlay', 'layer-popover', 'layer-toast',
    'layer-tooltip', 'layer-drag']
    .map((n) => Number(R.layer[n]));
  for (let i = 1; i < v.length; i++) assert.ok(v[i] > v[i - 1], v.join(' < '));
});

test('die Fehlerfläche hat eine Rampe wie die Marke: hover und active je Modus, Beschriftung abgeleitet', () => {
  for (const m of ['light', 'dark']) {
    for (const k of ['status-error-surface-hover', 'status-error-surface-active'])
      assert.ok(T.color[m][k], `${m}: ${k} fehlt`);
    for (const k of ['text-on-status-error-hover', 'text-on-status-error-active'])
      assert.ok(T.derived[m][k], `${m}: ${k} fehlt`);
  }
  const R = resolve(P, T);
  // Rampenrichtung wie surface-brand: hell wird beim Zeigen dunkler, dunkel wird heller —
  // aber 'active' geht in beiden Modi weiter in dieselbe Richtung als 'hover', nicht zurück.
  assert.ok(lum(R.color.light['status-error-surface-hover']) < lum(R.color.light['status-error-surface']), 'hell: hover muss dunkler sein');
  assert.ok(lum(R.color.dark['status-error-surface-hover']) > lum(R.color.dark['status-error-surface']), 'dunkel: hover muss heller sein');

  // Rampenreihenfolge wie bei surface-brand (dunkel blue-32 -> hover blue-27 -> active blue-43):
  // hell faellt streng (base > hover > active), dunkel steigt zwischen hover und base, faellt
  // dann unter die Flaeche zurueck (hover > base > active).
  assert.ok(
    lum(R.color.light['status-error-surface']) > lum(R.color.light['status-error-surface-hover'])
      && lum(R.color.light['status-error-surface-hover']) > lum(R.color.light['status-error-surface-active']),
    'hell: base > hover > active');
  assert.ok(
    lum(R.color.dark['status-error-surface-hover']) > lum(R.color.dark['status-error-surface'])
      && lum(R.color.dark['status-error-surface']) > lum(R.color.dark['status-error-surface-active']),
    'dunkel: hover > base > active');
});

// Acht Kategorien ohne Bedeutung — für Werte, die sich nur unterscheiden sollen (Projektfarben,
// Diagrammreihen). Eine Kategorie hat vier Rollen: subtle (die getönte Fläche, Text darauf ist
// gewöhnlicher Text), border (die Linie), surface (die gefüllte Fläche der Termine, Beschriftung
// gerechnet, Spec 5c §4) und mark (die Diagrammmarke, gerechnet nach der Reihenfolge der Töne). skalen.md „Pill: Rolle statt Farbe“.
const KATEGORIEN = [1, 2, 3, 4, 5, 6, 7, 8];
const BEDEUTUNGS_TINTS = ['surface-brand-subtle', 'status-error-subtle', 'status-warning-subtle',
  'status-success-subtle', 'status-info-subtle'];
/** 13 getönte Flächen auf 360° lassen keine 30° zu; Kategorien halten 20° zueinander und zu jeder Bedeutung. */
const KATEGORIE_ABSTAND_MIN = 20;

test('jede Kategorie trägt gewöhnlichen Text auf ihrer Fläche und zieht eine Linie mit 3,0 auf Grund und Layer', () => {
  for (const m of MODES) for (const k of KATEGORIEN) {
    const C = R.color[m];
    const t = ratio(C['text-primary'], C[`category-${k}-subtle`]);
    assert.ok(t >= 4.5, `${m}: text-primary auf category-${k}-subtle = ${t.toFixed(2)}`);
    for (const g of ['surface-base', 'surface-layer']) {
      const b = ratio(C[`category-${k}-border`], C[g]);
      assert.ok(b >= 3.0, `${m}: category-${k}-border auf ${g} = ${b.toFixed(2)}`);
    }
  }
});

test('Kategorien trennen sich im Farbton voneinander und von jeder Bedeutung', () => {
  for (const m of MODES) {
    const tints = KATEGORIEN.map((k) => [`category-${k}-subtle`, R.color[m][`category-${k}-subtle`]]);
    const bedeutung = BEDEUTUNGS_TINTS.map((n) => [n, R.color[m][n]]);
    for (let i = 0; i < tints.length; i++) {
      for (let j = i + 1; j < tints.length; j++) {
        const d = farbtonAbstand(tints[i][1], tints[j][1]);
        assert.ok(d >= KATEGORIE_ABSTAND_MIN, `${m}: ${tints[i][0]} ↔ ${tints[j][0]} = ${d.toFixed(1)}°`);
      }
      for (const [n, hex] of bedeutung) {
        const d = farbtonAbstand(tints[i][1], hex);
        assert.ok(d >= KATEGORIE_ABSTAND_MIN, `${m}: ${tints[i][0]} ↔ ${n} = ${d.toFixed(1)}°`);
      }
    }
  }
});

test('eine Kategorie hat genau vier Rollen: subtle, border, surface und mark', () => {
  for (const m of MODES) {
    const rollen = Object.keys(T.color[m]).filter((n) => n.startsWith('category-'));
    assert.equal(rollen.length, 32, rollen.join(', '));
    for (const n of rollen) assert.match(n, /^category-[1-8]-(subtle|border|surface|mark)$/, n);
  }
});

/** Die Eingabe der Rechnung, wie sie für TopLogic gilt: Töne der hellen Linien, Marke, Gründe je Modus. */
// Die Töne stehen so da, wie `hexZuOklch` sie liefert (auch negativ): die Rechnung normalisiert selbst.
const diagrammEingabe = () => ({
  toene: KATEGORIEN.map((k) => Math.round(hexZuOklch(R.color.light[`category-${k}-border`]).H * 180 / Math.PI)),
  marke: R.color.light['surface-brand'],
  gruende: Object.fromEntries(MODES.map((m) => [m, [R.color[m]['surface-base'], R.color[m]['surface-layer']]])),
});

test('Diagrammreihen: TopLogic ist das Ergebnis der Rechnung', () => {
  const eingabe = diagrammEingabe();
  const r = diagrammReihen(eingabe);
  // Die Nummerierung ist schon die gerechnete: Kategorie k trägt den Ton, den die Rechnung als k-ten setzt.
  assert.deepEqual(r.toene, eingabe.toene.map((t) => (t + 360) % 360));
  assert.deepEqual(r.ohneStufe, []);
  for (const m of MODES) for (const k of KATEGORIEN)
    assert.equal(R.color[m][`category-${k}-mark`], r[m][k - 1], `${m}: category-${k}-mark`);
});

test('Diagrammreihen halten die Schwellen (Kontrast, Abstand, keine Bedeutungsfarbe)', () => {
  for (const m of MODES) {
    const C = R.color[m];
    const marken = KATEGORIEN.map((k) => C[`category-${k}-mark`]);
    const { abstand, schwaechstes } = pruefeReihen(marken);
    assert.ok(abstand >= 1, `${m}: abstand = ${abstand.toFixed(3)}, schwächstes ${JSON.stringify(schwaechstes)}`);
    for (const k of KATEGORIEN) for (const g of ['surface-base', 'surface-layer']) {
      const b = ratio(C[`category-${k}-mark`], C[g]);
      assert.ok(b >= 3.0, `${m}: category-${k}-mark auf ${g} = ${b.toFixed(2)}`);
    }
    for (const [rolle, hex] of Object.entries(C).filter(([n]) => n.startsWith('status-'))) for (const k of KATEGORIEN)
      assert.notEqual(C[`category-${k}-mark`], hex, `${m}: category-${k}-mark gleicht ${rolle}`);
  }
});

test('Zeigen auf allen Gründen (01.10.): dunkel liegen hover, strong, active über dem Overlay', () => {
  const C = R.color.dark;
  for (const rolle of ['surface-interactive-hover', 'surface-interactive-strong', 'surface-interactive-active']) {
    assert.ok(ratio(C[rolle], C['surface-overlay']) >= 1.1, `${rolle} auf surface-overlay < 1,1`);
  }
  // Zeilencursor und Zelleditor-Linie liegen auf der gewählten Zeile, gewählt und gezeigt.
  for (const bg of ['surface-selected', 'surface-selected-hover'])
    assert.ok(ratio(C['border-interactive'], C[bg]) >= 3.0, `border-interactive auf ${bg}`);
  assert.equal(T.measure['size-popover-min'], '{size-11}');
  assert.equal(T.measure['size-popover-max'], '{view-1}');
  assert.equal(T.measure['size-popover-content-max'], '{size-14}');
  assert.equal(P.measure['view-2'], '100dvw');
  assert.equal(P.measure['view-3'], '100dvh');
  assert.equal(T.measure['size-viewport-width'], '{view-2}');
  assert.equal(T.measure['size-viewport-height'], '{view-3}');
});

test('Knöpfe im Feld (06.10.): Zeigen und Drücken heben sich von surface-field ab, Drücken vom Zeigen', () => {
  for (const m of MODES) {
    const C = R.color[m];
    const zeigen = ratio(C['surface-interactive-strong'], C['surface-field']);
    const druecken = ratio(C['surface-interactive-active'], C['surface-interactive-strong']);
    assert.ok(zeigen >= 1.1, `${m}: surface-interactive-strong auf surface-field = ${zeigen.toFixed(2)}, gefordert 1,1`);
    assert.ok(druecken >= 1.1, `${m}: surface-interactive-active auf -strong = ${druecken.toFixed(2)}, gefordert 1,1`);
  }
  // Der Befund: die Zeigen-Stufe des Ghost-Knopfs verschwindet hell auf dem Feld.
  assert.ok(ratio(R.color.light['surface-interactive-hover'], R.color.light['surface-field']) < 1.1);
});

test('Knöpfe im Feld (06.10.): der Entfernen-Knopf im Chip hebt sich auf jeder der 14 Pillenflächen ab', () => {
  const pillen = ['surface-interactive-strong', 'surface-brand-subtle',
    ...['error', 'warning', 'success', 'info'].map((s) => `status-${s}-subtle`),
    ...[1, 2, 3, 4, 5, 6, 7, 8].map((k) => `category-${k}-subtle`)];
  assert.equal(pillen.length, 14);
  for (const m of MODES) for (const bg of pillen) {
    const ist = ratio(R.color[m]['surface-interactive-active'], R.color[m][bg]);
    assert.ok(ist >= 1.1, `${m}: surface-interactive-active auf ${bg} = ${ist.toFixed(2)}, gefordert 1,1`);
  }
});

// Statusfamilien: dieselbe Stufe als Text und Fläche (oder Rand und Fläche) macht den Zustand
// unsichtbar, und ein Kundentheme könnte die beiden nie trennen (docs/skalen.md, Status-Rollen).
const STATUS_FAMILIEN = ['error', 'warning', 'success', 'info'];
const STATUS_ROLLEN = ['text', 'border', 'surface', 'surface-hover', 'surface-active', 'subtle'];

// Liefert je Doppelbelegung eine Meldung; leer, wenn kein Primitiv zwei Rollen einer Familie trägt.
function statusDoppelbelegungen(color) {
  const funde = [];
  for (const mode of MODES) {
    for (const s of STATUS_FAMILIEN) {
      const gesehen = new Map();
      for (const rolle of STATUS_ROLLEN) {
        const name = `status-${s}-${rolle}`;
        const ref = color[mode]?.[name];
        if (ref === undefined) continue;
        if (gesehen.has(ref))
          funde.push(`${mode}/${s}: ${ref} trägt ${gesehen.get(ref)} und ${name}`);
        else gesehen.set(ref, name);
      }
    }
  }
  return funde;
}

test('kein Primitiv trägt in einem Modus zwei Rollen einer Statusfamilie', () => {
  assert.deepEqual(statusDoppelbelegungen(T.color), []);
});

test('das Status-Gate meldet den verworfenen Tausch (dunkel Rand und Fläche beide red-35)', () => {
  const kaputt = structuredClone(T.color);
  kaputt.dark['status-error-border'] = '{red-35}';
  kaputt.dark['status-error-surface'] = '{red-35}';
  assert.deepEqual(statusDoppelbelegungen(kaputt),
    ['dark/error: {red-35} trägt status-error-border und status-error-surface']);
});

test('Hilfetext, Nebentext und Link stehen auch auf der ausgewählten Zeile, gewählt und gezeigt (09.10., Hendrik)', () => {
  // Seit UI-Durchgang 1 in beiden Modi: die dunkle Fläche `surface-selected` liegt auf blue-72, damit die gezeigte
  // Stufe (blue-69) den Hilfetext noch trägt.
  for (const m of MODES) for (const bg of ['surface-selected', 'surface-selected-hover']) for (const fg of ['text-helper', 'text-secondary', 'text-link']) {
    const ist = ratio(R.color[m][fg], R.color[m][bg]);
    assert.ok(ist >= 4.5, `${m}: ${fg} auf ${bg} = ${ist.toFixed(2)}`);
  }
  // Gewählt und gezeigt unterscheiden sich sichtbar.
  for (const m of MODES)
    assert.ok(ratio(R.color[m]['surface-selected'], R.color[m]['surface-selected-hover']) >= 1.05, `${m}: selected gegen selected-hover`);
  assert.ok(ratio(R.color.light['text-secondary'], R.color.light['surface-base'])
    > ratio(R.color.light['text-helper'], R.color.light['surface-base']), 'Hilfetext nicht heller als Nebentext');
});

test('Auswahl hat eigene Rollen, der alte Name ist weg', () => {
  for (const mode of ['light', 'dark']) {
    for (const r of ['surface-selected', 'surface-selected-hover', 'border-selected', 'surface-interactive-strong'])
      assert.ok(T.color[mode][r], `${mode}: ${r}`);
    assert.equal(T.color[mode]['surface-interactive-selected'], undefined);
    for (const r of ['surface-selected', 'surface-layer-muted'])
      assert.ok(ratio(R.color[mode][r], R.color[mode]['surface-layer']) >= 1.1, `${mode}: ${r} gegen surface-layer`);
  }
});

test('Typo-Klasse overline: Größe, Gewicht und Zeilenhöhe wie label, Sperrung 0.04em', () => {
  const o = T.type.overline;
  assert.ok(o, 'overline fehlt');
  for (const k of ['family', 'size', 'weight', 'lineHeight']) assert.equal(o[k], T.type.label[k], k);
  assert.equal(o.tracking, '0.04em');
  assert.ok(readFileSync(new URL('../dist/base.css', import.meta.url), 'utf8').includes('.tl-type-overline {'));
});
