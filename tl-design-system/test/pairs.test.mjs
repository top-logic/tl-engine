import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { kontrastPaare, werte } from '../tools/lib/pairs.mjs';
import { resolve } from '../tools/resolve.mjs';
import { ratio } from '../tools/lib/contrast.mjs';

const P = JSON.parse(readFileSync(new URL('../src/primitives.json', import.meta.url)));
const T = JSON.parse(readFileSync(new URL('../src/tokens.json', import.meta.url)));
const R = resolve(P, T);

test('die Paarliste nennt jede Rolle, die tokens.test prüft, und nur vorhandene Rollen', () => {
  const paare = kontrastPaare(T);
  const namen = new Set(paare.flatMap((p) => [p.fg, p.bg]));
  for (const n of ['text-primary', 'text-link', 'focus-ring', 'status-error-text', 'category-1-border', 'text-on-brand'])
    assert.ok(namen.has(n), n);
  for (const n of namen) {
    // Eine Zusammensetzung <Schicht>/<Grund>: eine durchscheinende Effekt-Rolle über einer deckenden Fläche.
    const [schicht, grund] = n.includes('/') ? n.split('/') : [null, n];
    if (schicht) assert.ok(schicht in T.effect.light && schicht.startsWith('surface-state-'), `${schicht} ist keine Schicht`);
    assert.ok(grund in T.color.light || grund in T.derived.light, `${n} unbekannt`);
  }
});

test('jedes Paar der Liste trägt in beiden Modi im Paket', () => {
  for (const mode of ['light', 'dark']) for (const { fg, bg, soll } of kontrastPaare(T)) {
    const ist = ratio(werte(R, mode, fg), werte(R, mode, bg));
    assert.ok(ist >= soll, `${mode}: ${fg} auf ${bg} = ${ist.toFixed(2)}, gefordert ${soll}`);
  }
});

test('kein Grund der Paarliste ist anderswo ein Vordergrund', () => {
  // Darauf steht der Nachführ-Durchgang von derive: eine Rolle, die dort in der Helligkeit bewegt
  // wird, taucht in keinem anderen Paar als Grund auf und kann kein geprüftes Paar wieder
  // aufreißen. Ein einziger Durchgang genügt deshalb — fällt die Invariante, fällt dieser Test.
  const paare = kontrastPaare(T);
  const vordergruende = new Set(paare.map((p) => p.fg));
  for (const { bg } of paare) assert.ok(!vordergruende.has(bg), `${bg} ist Grund und Vordergrund zugleich`);
});

test('kein Paar steht zweimal in der Liste', () => {
  // Zwei Schleifen dürfen dasselbe Paar nennen; es zählt einmal, mit der höheren Schwelle. Sonst
  // meldet derive einen Befund doppelt und zählt ihn im Kopf des Themes zweimal.
  const gesehen = new Set();
  for (const { fg, bg } of kontrastPaare(T)) {
    const schluessel = `${fg}|${bg}`;
    assert.ok(!gesehen.has(schluessel), `${fg} auf ${bg} steht doppelt`);
    gesehen.add(schluessel);
  }
});

test('3b: die Paare der Daten stehen in der Liste', () => {
  const namen = new Set(kontrastPaare(T).map((p) => `${p.fg}|${p.bg}`));
  for (const k of ['icon-secondary|surface-interactive-strong', 'icon-secondary|surface-layer',
    'icon-secondary|surface-selected', 'icon-secondary|surface-selected-hover',
    'status-error-border|surface-selected', 'status-error-border|surface-selected-hover', 'status-error-border|surface-interactive-hover',
    'status-warning-border|surface-selected', 'status-warning-border|surface-selected-hover', 'status-warning-border|surface-interactive-hover',
    'focus-ring|surface-selected', 'focus-ring|surface-selected-hover',
    'focus-ring|surface-interactive-hover', 'border-emphasis|surface-layer',
    'text-placeholder|surface-base', 'text-placeholder|surface-layer', 'text-placeholder|surface-layer-nested',
    'icon-primary|surface-state-hover/surface-interactive-hover', 'icon-primary|surface-state-active/surface-selected', 'icon-primary|surface-state-hover/surface-selected-hover'])
    assert.ok(namen.has(k), k);
});

test('UI-Durchgang 1: die Paare der Auswahl und der gedämpften Fläche stehen in der Liste', () => {
  const namen = new Set(kontrastPaare(T).map((p) => `${p.fg}|${p.bg}`));
  for (const k of ['border-interactive|surface-selected', 'border-selected|surface-selected', 'border-selected|surface-selected-hover',
    'icon-primary|surface-selected', 'icon-primary|surface-selected-hover', 'text-primary|surface-selected', 'text-primary|surface-selected-hover',
    'text-primary|surface-layer-muted', 'text-secondary|surface-layer-muted',
    'icon-primary|surface-layer-muted', 'icon-secondary|surface-layer-muted',
    'border-interactive|surface-selected-hover', 'text-secondary|surface-selected', 'text-secondary|surface-selected-hover',
    'text-link|surface-selected', 'text-link|surface-selected-hover', 'text-helper|surface-selected', 'text-helper|surface-selected-hover'])
    assert.ok(namen.has(k), k);
});
