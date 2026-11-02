import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { hexZuOklch, oklchZuHex, beschriftungAbleiten, farbtonAbstand, aufKontrast, grauRampe, GRAU_CHROMA, kategorienVerteilen, kategorieRollen, markenRollen } from '../tools/lib/color.mjs';
import { ratio } from '../tools/lib/contrast.mjs';
import { derive } from '../tools/derive.mjs';

const GRUENDE = { light: ['#FAFAFA', '#FFFFFF'], dark: ['#16181B', '#1B1D21'] };

const P = JSON.parse(readFileSync(new URL('../src/primitives.json', import.meta.url)));
const T = JSON.parse(readFileSync(new URL('../src/tokens.json', import.meta.url)));
const GRAU = Object.fromEntries(Object.entries(P.color).filter(([n]) => n.startsWith('gray-')));
const grad = (H) => ((H * 180) / Math.PI + 360) % 360;

test('Hex → OKLCH → Hex ist verlustfrei bis auf Rundung', () => {
  for (const hex of ['#0B5ED7', '#FAFAFA', '#16181B', '#D7352D'])
    assert.equal(oklchZuHex(hexZuOklch(hex)).hex.toUpperCase(), hex);
});

test('die Beschriftung nimmt den Kandidaten mit dem höheren Kontrast', () => {
  const k = { dunklerKandidat: '#1B1D21', hellerKandidat: '#FFFFFF' };
  assert.equal(beschriftungAbleiten('#0B5ED7', k).hex.toUpperCase(), '#FFFFFF');
  assert.equal(beschriftungAbleiten('#5394FF', k).hex.toUpperCase(), '#1B1D21');
});

test('der Farbtonabstand ist symmetrisch und misst über den kurzen Bogen', () => {
  const a = farbtonAbstand('#DFEBFF', '#CEF3FB'), b = farbtonAbstand('#CEF3FB', '#DFEBFF');
  assert.ok(Math.abs(a - b) < 1e-9);
  assert.ok(a > 30 && a <= 180);
});

test('aufKontrast senkt L einer hellen Marke, bis Text auf Weiß 4,5 erreicht, und hält den Farbton', () => {
  const hell = '#F39C6B';
  const r = aufKontrast(hell, ['#FFFFFF', '#FAFAFA'], 4.5, 'dunkler');
  assert.ok(r.angepasst);
  assert.ok(ratio(r.hex, '#FFFFFF') >= 4.5 && ratio(r.hex, '#FAFAFA') >= 4.5);
  assert.ok(Math.abs(hexZuOklch(r.hex).H - hexZuOklch(hell).H) < 0.05);
});

test('aufKontrast lässt einen Wert, der schon trägt, unverändert', () => {
  const r = aufKontrast('#0B5ED7', ['#FFFFFF'], 4.5, 'dunkler');
  assert.equal(r.hex, '#0B5ED7');
  assert.equal(r.angepasst, false);
});

test('aufKontrast hebt L im dunklen Modus', () => {
  const r = aufKontrast('#3A2A1A', ['#16181B'], 3.0, 'heller');
  assert.ok(r.angepasst && ratio(r.hex, '#16181B') >= 3.0);
});

test('grauRampe hält jede Helligkeit der TopLogic-Rampe auf 0,01 genau', () => {
  for (const temperatur of ['warm', 'neutral', 'cool']) {
    const r = grauRampe(temperatur, GRAU);
    assert.deepEqual(Object.keys(r), Object.keys(GRAU));
    for (const n of Object.keys(GRAU))
      assert.ok(Math.abs(hexZuOklch(r[n]).L - hexZuOklch(GRAU[n]).L) < 0.01, `${temperatur} ${n}`);
  }
});

test('neutral trägt keine Chroma, warm liegt bei 70°, cool bei 250°', () => {
  assert.ok(hexZuOklch(grauRampe('neutral', GRAU)['gray-45']).C < 0.002);
  const warm = hexZuOklch(grauRampe('warm', GRAU)['gray-45']);
  const cool = hexZuOklch(grauRampe('cool', GRAU)['gray-45']);
  assert.ok(Math.abs(grad(warm.H) - 70) < 3 && Math.abs(warm.C - GRAU_CHROMA) < 0.002);
  assert.ok(Math.abs(grad(cool.H) - 250) < 3);
});

test('acht Kategorien um die TopLogic-Marke halten 20° zu allem, was blockiert ist, und zueinander', () => {
  const blockiert = [260, 28, 85, 162, 213];
  const { toene, abstand } = kategorienVerteilen(blockiert);
  assert.equal(toene.length, 8);
  assert.equal(abstand, 20);
  const d = (a, b) => Math.abs(((a - b) % 360 + 540) % 360 - 180);
  for (const t of toene) for (const b of blockiert) assert.ok(d(t, b) >= 20, `${t} ↔ ${b}`);
  for (let i = 0; i < 8; i++) for (let j = i + 1; j < 8; j++) assert.ok(d(toene[i], toene[j]) >= 20);
  for (const t of toene) assert.ok(t >= 0 && t < 360, `${t} außerhalb [0, 360)`);
});

test('eine Marke auf einer Bedeutung blockiert nur einen Ton, die Verteilung bleibt bei 20°', () => {
  const { abstand } = kategorienVerteilen([30, 28, 85, 162, 213]);
  assert.equal(abstand, 20);
});

test('die Verteilung ist deterministisch', () => {
  assert.deepEqual(kategorienVerteilen([120, 28, 85, 162, 213]), kategorienVerteilen([120, 28, 85, 162, 213]));
});

test('kategorienVerteilen mit anzahl 0 liefert keine Töne und behält den Mindestabstand', () => {
  assert.deepEqual(kategorienVerteilen([260, 28, 85, 162, 213], 0), { toene: [], abstand: 20 });
});

test('gebrochene blockierte Töne runden den Mindestabstand nicht weg', () => {
  const blockiert = [0.5, 60.5, 120.5, 180.5, 240.5, 300.5];
  const { toene, abstand } = kategorienVerteilen(blockiert);
  assert.equal(abstand, 20);
  const d = (a, b) => Math.abs(((a - b) % 360 + 540) % 360 - 180);
  for (const t of toene) for (const b of blockiert) assert.ok(d(t, b) >= 20, `${t} ↔ ${b}`);
  for (let i = 0; i < toene.length; i++) for (let j = i + 1; j < toene.length; j++) assert.ok(d(toene[i], toene[j]) >= 20);
});

test('ein leerer Kreis (kein blockierter Ton) ist selbst eine Lücke', () => {
  const { toene, abstand } = kategorienVerteilen([]);
  assert.equal(toene.length, 8);
  assert.equal(abstand, 20);
  const d = (a, b) => Math.abs(((a - b) % 360 + 540) % 360 - 180);
  for (const t of toene) assert.ok(t >= 0 && t < 360, `${t} außerhalb [0, 360)`);
  for (let i = 0; i < 8; i++) for (let j = i + 1; j < 8; j++) assert.ok(d(toene[i], toene[j]) >= 20);
});

test('kategorieRollen liefert subtle mit Text 4,5 und border mit 3,0 auf beiden Gründen, je Modus', () => {
  const gruende = { light: ['#FAFAFA', '#FFFFFF'], dark: ['#16181B', '#1B1D21'] };
  const r = kategorieRollen(300, gruende);
  assert.ok(ratio('#1B1D21', r.light.subtle) >= 4.5 && ratio('#F0F3F9', r.dark.subtle) >= 4.5);
  for (const g of gruende.light) assert.ok(ratio(r.light.border, g) >= 3.0);
  for (const g of gruende.dark) assert.ok(ratio(r.dark.border, g) >= 3.0);
});

test('markenRollen: die Saat ist surface-brand hell, hover und active dunkler, im Dunkeln heller', () => {
  const r = markenRollen('#0B5ED7', GRUENDE);
  assert.equal(r.light['surface-brand'], '#0B5ED7');
  const L = (h) => hexZuOklch(h).L;
  assert.ok(L(r.light['surface-brand-hover']) < L(r.light['surface-brand']));
  assert.ok(L(r.light['surface-brand-active']) < L(r.light['surface-brand-hover']));
  assert.ok(L(r.dark['surface-brand']) > L(r.light['surface-brand']));
  assert.ok(L(r.dark['surface-brand-hover']) > L(r.dark['surface-brand']));
  assert.ok(L(r.dark['surface-brand-active']) < L(r.dark['surface-brand']));
  assert.equal(r.hinweise.length, 0);
});

test('markenRollen: eine helle Marke bekommt Textrollen, die auf dem Grund tragen, mit Hinweis', () => {
  const r = markenRollen('#F39C6B', GRUENDE);
  assert.equal(r.light['surface-brand'], '#F39C6B');
  for (const g of GRUENDE.light) assert.ok(ratio(r.light['text-link'], g) >= 4.5);
  for (const g of GRUENDE.light) assert.ok(ratio(r.light['focus-ring'], g) >= 3.0);
  assert.ok(r.hinweise.some((h) => h.includes('text-link in der Helligkeit')));
});

test('markenRollen: subtle liegt auf den Helligkeiten der TopLogic-Tönung', () => {
  const r = markenRollen('#0B5ED7', GRUENDE);
  assert.ok(Math.abs(hexZuOklch(r.light['surface-brand-subtle']).L - 0.937) < 0.01);
  assert.ok(Math.abs(hexZuOklch(r.dark['surface-brand-subtle']).L - 0.255) < 0.01);
});

test('markenRollen: alle zwölf Rollen sind in beiden Modi vorhanden', () => {
  const r = markenRollen('#0B5ED7', GRUENDE);
  const rollen = ['surface-brand', 'surface-brand-hover', 'surface-brand-active',
    'surface-brand-subtle', 'surface-brand-subtle-hover', 'text-link', 'text-link-hover',
    'border-interactive', 'focus-ring', 'surface-selected', 'surface-selected-hover', 'border-selected'];
  for (const mode of ['light', 'dark']) for (const rolle of rollen) assert.ok(r[mode][rolle], `${mode}.${rolle}`);
});

test('markenRollen: im Dunkeln tragen text-link und focus-ring gegen beide Gründe', () => {
  for (const saat of ['#F39C6B', '#1A1A40']) {
    const r = markenRollen(saat, GRUENDE);
    for (const g of GRUENDE.dark) {
      assert.ok(ratio(r.dark['text-link'], g) >= 4.5, `${saat}: text-link gegen ${g}`);
      assert.ok(ratio(r.dark['focus-ring'], g) >= 3.0, `${saat}: focus-ring gegen ${g}`);
    }
  }
});

test('markenRollen: text-link-hover bleibt in der richtigen Reihenfolge zu text-link', () => {
  const L = (h) => hexZuOklch(h).L;
  const hell = markenRollen('#F39C6B', GRUENDE);
  assert.ok(L(hell.light['text-link-hover']) < L(hell.light['text-link']));
  const dunkel = markenRollen('#1A1A40', GRUENDE);
  assert.ok(L(dunkel.dark['text-link-hover']) > L(dunkel.dark['text-link']));
});

test('markenRollen: subtle behält die eigene Chroma und den Farbton der Saat', () => {
  const r = markenRollen('#0B5ED7', GRUENDE);
  const saat = hexZuOklch('#0B5ED7');
  const subtle = hexZuOklch(r.light['surface-brand-subtle']);
  assert.ok(Math.abs(subtle.C - 0.03) < 0.005);
  const dH = Math.abs(((subtle.H - saat.H + Math.PI) % (2 * Math.PI) + 2 * Math.PI) % (2 * Math.PI) - Math.PI);
  assert.ok(dH < 0.05, `Farbtonabstand ${dH}`);
});

test('markenRollen: eine unbunte Saat bleibt bei subtle grau, mit Hinweis', () => {
  const r = markenRollen('#808080', GRUENDE);
  assert.ok(hexZuOklch(r.light['surface-brand-subtle']).C < 0.002);
  assert.ok(hexZuOklch(r.dark['surface-brand-subtle']).C < 0.002);
  assert.ok(r.hinweise.some((h) => h.includes('unbunt')));
});

test('markenRollen: eine zu helle Saat kollabiert im Dunkeln, mit Hinweis', () => {
  const r = markenRollen('#F5F5F5', GRUENDE);
  assert.ok(r.hinweise.some((h) => h.includes('zusammen')));
});

test('derive: surface-selected und surface-layer-muted heben sich über mehrere Saaten von surface-layer ab (>= 1,1), beide Modi', () => {
  for (const brand of ['#E8541E', '#2E8B57', '#808080', '#0B5ED7']) for (const gray of [undefined, 'warm']) {
    const { ergebnis } = derive(P, T, { theme: 'saat', color: { brand, ...(gray ? { gray } : {}) } });
    for (const m of ['light', 'dark']) for (const rolle of ['surface-selected', 'surface-layer-muted']) {
      const ist = ratio(ergebnis.color[m][rolle], ergebnis.color[m]['surface-layer']);
      assert.ok(ist >= 1.1, `${brand}/${gray ?? 'ohne Grau'} ${m}: ${rolle} auf surface-layer = ${ist.toFixed(3)}`);
    }
  }
});
