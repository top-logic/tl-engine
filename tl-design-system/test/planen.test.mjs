// Welle 5c (Spec 2026-10-19 Planen und Ziehen §4, §8): gefüllte Kategorien für Termine und die beiden
// Kandidaten der Beschriftung als Rollen. Was derive daraus rechnet, prüft derive.test.mjs.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { readComponent, readGenerated, declarationsOf, rulesOf } from './lib/component.mjs';

const T = JSON.parse(readFileSync(new URL('../src/tokens.json', import.meta.url)));
const P = JSON.parse(readFileSync(new URL('../src/primitives.json', import.meta.url)));
const TOKENS_CSS = readFileSync(new URL('../dist/tokens.css', import.meta.url), 'utf8');
const MODES = ['light', 'dark'];
const KATEGORIEN = [1, 2, 3, 4, 5, 6, 7, 8];

/** Der Block eines Modus in dist/tokens.css: von seinem Selektor bis zur schließenden Klammer. */
const blockVon = (selektor) => {
  const start = TOKENS_CSS.indexOf(selektor);
  assert.ok(start >= 0, `${selektor} fehlt in dist/tokens.css`);
  return TOKENS_CSS.slice(start, TOKENS_CSS.indexOf('\n}', start));
};

test('kategorie: gefüllte Fläche je Kategorie in beiden Modi', () => {
  // Die dunklen -border-Stufen, nach der Rechnung der Diagrammfarben nummeriert (Töne 238°, 186°, 2°, 285°, 56°,
  // 311°, 123°, 337°): weiße Schrift hält dort 5,3–5,4:1 (Spec 5c §4).
  const soll = ['{blue-48}', '{red-46}', '{blue-46}', '{teal-48}', '{amber-47}', '{magenta-46}', '{green-48}', '{magenta-47}'];
  for (const m of MODES) for (const k of KATEGORIEN)
    assert.equal(T.color[m][`category-${k}-surface`], soll[k - 1], `${m}: category-${k}-surface`);
});

test('kategorie: Schrift gerechnet', () => {
  for (const m of MODES) for (const k of KATEGORIEN)
    assert.deepEqual(T.derived[m][`text-on-category-${k}`], { surface: `category-${k}-surface` }, `${m}: text-on-category-${k}`);
  // Helle Schrift in beiden Modi, wie heute (Spec 5c §4).
  for (const sel of ['[data-tl-mode="light"] {', '[data-tl-mode="dark"] {'])
    assert.match(blockVon(sel), new RegExp(`--tl-text-on-category-1: ${P.color['gray-0']};`), sel);
});

test('kandidaten: als Rollen', () => {
  for (const m of MODES) {
    assert.equal(T.color[m]['text-contrast-light'], T.derived.candidates.light, `${m}: text-contrast-light`);
    assert.equal(T.color[m]['text-contrast-dark'], T.derived.candidates.dark, `${m}: text-contrast-dark`);
  }
  assert.equal(T.derived.candidates.light, '{gray-0}');
  assert.equal(T.derived.candidates.dark, '{gray-77}');
});

// --- tl-calendar (Spec 5c §3) -------------------------------------------------------------------------
// Der Block liest nur Tokens; die Kategorie-Regeln schreibt der Generator (Marker in calendar.css).

const KALENDER = (() => { try { return readGenerated('calendar', 'calendar.css'); } catch { return ''; } })();
const kalenderQuelle = () => readComponent('calendar', 'calendar.css');
/** Alle Deklarationen aller Regeln, deren Selektorliste `sel` enthält. */
const kalenderRegel = (sel) => rulesOf(KALENDER)
  .filter(([s]) => s.split(',').map((x) => x.trim()).includes(sel)).map(([, d]) => d).join(';');

test('calendar: Termin gefüllt in der Kategorie', () => {
  for (const k of KATEGORIEN) {
    const d = kalenderRegel(`.tl-calendar__event[data-tl-role="category-${k}"]`);
    assert.match(d, new RegExp(`background:\\s*var\\(--tl-category-${k}-surface\\)`), `Fläche category-${k}`);
    assert.match(d, new RegExp(`color:\\s*var\\(--tl-text-on-category-${k}\\)`), `Schrift category-${k}`);
    const punkt = kalenderRegel(`.tl-calendar__chip-dot[data-tl-role="category-${k}"]`);
    assert.match(punkt, new RegExp(`background:\\s*var\\(--tl-category-${k}-surface\\)`), `Punkt category-${k}`);
  }
  const ohne = declarationsOf(KALENDER, '.tl-calendar__event') ?? '';
  assert.match(ohne, /background:\s*var\(--tl-surface-brand\)/);
  assert.match(ohne, /color:\s*var\(--tl-text-on-brand\)/);
  assert.match(declarationsOf(KALENDER, '.tl-calendar__chip-dot') ?? '', /background:\s*var\(--tl-surface-brand\)/);
  // Die 16 + 8 Regeln stehen nicht von Hand in der Quelle, sondern als Marker.
  assert.doesNotMatch(kalenderQuelle().replace(/\/\*[\s\S]*?\*\//g, ''), /--tl-(category-\d+-surface|text-on-category-\d+)/);
});

test('calendar: ausgewählt ist ein Ring', () => {
  const d = kalenderRegel('.tl-calendar__event[aria-selected="true"]::after');
  for (const r of [/content:\s*""/, /position:\s*absolute/, /inset:\s*0/,
    /border:\s*var\(--tl-border-width-md\) solid var\(--tl-border-selected\)/, /border-radius:\s*inherit/,
    /pointer-events:\s*none/]) assert.match(d, r);
  // Auf der Füllung trägt der Ring die Schriftfarbe (hält gegen jede Fläche), am Chip bleibt border-selected.
  assert.match(d, /border-color:\s*currentColor/);
  assert.doesNotMatch(kalenderRegel('.tl-calendar__chip[aria-selected="true"]::after'), /currentColor/);
  assert.doesNotMatch(kalenderQuelle().replace(/\/\*[\s\S]*?\*\//g, ''), /(^|[\s;{])outline\s*:/);
});

test('calendar: das Anlegen-Feld ist nicht blass', () => {
  assert.match(kalenderRegel('.tl-calendar__event[data-tl-state~="preview"]'), /opacity:\s*0\.4/);
  assert.match(kalenderRegel('.tl-calendar__event[data-tl-state~="preview"]:has(.tl-calendar__create)'), /opacity:\s*1\b/);
});

test('calendar: heute überall derselbe Kreis', () => {
  for (const sel of ['.tl-calendar__day-head[aria-current="date"] .tl-calendar__day-number',
    '.tl-calendar__day[aria-current="date"] .tl-calendar__day-number',
    '.tl-calendar__mini-day[aria-current="date"]']) {
    const d = kalenderRegel(sel);
    assert.match(d, /background:\s*var\(--tl-surface-brand\)/, sel);
    assert.match(d, /color:\s*var\(--tl-text-on-brand\)/, sel);
    assert.match(d, /border-radius:\s*var\(--tl-radius-full\)/, sel);
  }
});

test('calendar: Flächen', () => {
  assert.match(kalenderRegel('.tl-calendar__column'), /background:\s*var\(--tl-surface-base\)/);
  assert.match(kalenderRegel('.tl-calendar__work'), /background:\s*var\(--tl-surface-layer\)/);
  const jetzt = kalenderRegel('.tl-calendar__now');
  assert.match(jetzt, /var\(--tl-status-error-border\)/);
  assert.match(jetzt, /var\(--tl-border-width-md\)/);
  assert.match(kalenderRegel('.tl-calendar__hour-line'), /var\(--tl-border-separator\)/);
  const mit = (zustand) => rulesOf(KALENDER).filter(([s]) => s.includes(`[data-tl-state~="${zustand}"]`)).map(([, d]) => d).join(';');
  assert.match(mit('nonworking'), /background:\s*var\(--tl-surface-base\)/);
  assert.match(mit('other'), /color:\s*var\(--tl-text-secondary\)/);
});

test('calendar: keine Literale', () => {
  const ohne = kalenderQuelle().replace(/\/\*[\s\S]*?\*\//g, '');
  assert.doesNotMatch(ohne, /#[0-9a-f]{3,6}\b/i);
  assert.doesNotMatch(ohne, /rgba\(/);
  assert.doesNotMatch(ohne, /\d+px/);
  assert.doesNotMatch(ohne, /z-index:\s*\d/);
  assert.doesNotMatch(ohne, /font-size/);
  assert.doesNotMatch(ohne, /data-tl-state~="(drop-|dragging)/);
});

// --- tl-board (Spec 5c §5) ----------------------------------------------------------------------------
// Spalten und Karten aus Tokens; Einfügelinie, Ablegen und Ablehnung zeichnet allein drag.css.

const leseOder = (dir, file) => { try { return readGenerated(dir, file); } catch { return ''; } };
const ohneKommentar = (css) => css.replace(/\/\*[\s\S]*?\*\//g, '');
const vertrag = (dir) => { try { return readComponent(dir, `${dir}.contract.md`); } catch { return ''; } };
/** Alle Deklarationen aller Regeln, deren Selektorliste `sel` enthält. */
const regel = (css, sel) => rulesOf(css)
  .filter(([s]) => s.split(',').map((x) => x.trim()).includes(sel)).map(([, d]) => d).join(';');

const BOARD = leseOder('board', 'board.css');

test('board: Spalten und Karten aus Tokens', () => {
  const spalte = regel(BOARD, '.tl-board__column');
  assert.match(spalte, /var\(--tl-size-12\)/, 'Breite size-12');
  assert.match(spalte, /max-width:\s*var\(--tl-size-13\)/, 'Höchstbreite size-13');
  assert.match(spalte, /background:\s*var\(--tl-surface-base\)/);
  assert.match(spalte, /border-radius:\s*var\(--tl-radius-md\)/);
  assert.match(regel(BOARD, '.tl-board__label'), /text-overflow:\s*ellipsis/);
  const karte = regel(BOARD, '.tl-board__card');
  assert.match(karte, /border:\s*var\(--tl-border-width-sm\) solid var\(--tl-border-separator\)/);
  assert.doesNotMatch(karte, /box-shadow/, 'eine Karte schwebt nicht (tl-item, Q24)');
  assert.match(karte, /padding:[^;]*calc\(var\(--tl-space-xs\) \+ var\(--tl-size-icon-md\) \+ var\(--tl-space-xs\)\)/,
    'die Karte hält die Ecke des Auswahlhakens frei');
  assert.match(karte, /flex-shrink:\s*0/,
    'die Karten-Liste rollt, statt ihre Karten unter den Text zu stauchen (tl-item setzt min-height: 0; Sichtprüfung 23.10.)');
  const ohne = ohneKommentar(BOARD);
  assert.ok(ohne.trim(), 'board.css fehlt');
  assert.doesNotMatch(ohne, /999px/);
  assert.doesNotMatch(ohne, /z-index/);
  assert.doesNotMatch(ohne, /#[0-9a-f]{3,8}\b/i);
});

test('board: Ziehen nur über drag.css', () => {
  assert.doesNotMatch(ohneKommentar(BOARD), /drop-before|drop-after|drop-onto|drop-refused|dragging/);
  const md = vertrag('board');
  for (const n of ['data-tl-state', 'tl-drop-hint', 'tl-item', 'tl-badge'])
    assert.ok(md.includes('`' + n), `board.contract.md nennt ${n}`);
});

// --- tl-dashboard (Spec 5c §6) ------------------------------------------------------------------------

const DASHBOARD = leseOder('dashboard', 'dashboard.css');
const BASE = readFileSync(new URL('../dist/base.css', import.meta.url), 'utf8');

test('dashboard: Kachel ist eine Karte', () => {
  const md = vertrag('dashboard');
  for (const n of ['tl-card', 'tl-overlay__action', 'tl-overlay__layer--top-right', 'tl-visually-hidden', 'data-tl-axis="horizontal"'])
    assert.ok(md.includes('`' + n), `dashboard.contract.md nennt ${n}`);
  assert.ok(ohneKommentar(DASHBOARD).trim(), 'dashboard.css fehlt');
  assert.doesNotMatch(regel(DASHBOARD, '.tl-dashboard__tile'), /box-shadow/);
  assert.doesNotMatch(ohneKommentar(DASHBOARD), /box-shadow/, 'kein Schatten, auch nicht beim Zeigen');
  assert.match(regel(DASHBOARD, '.tl-dashboard__cover'), /cursor:\s*move/);
  // Zeigen auf eine Kachel mit Aktion — nicht im Bearbeiten, nicht deaktiviert — wie heute border-interactive.
  const zeigen = rulesOf(DASHBOARD).filter(([s]) => /\.tl-dashboard__tile:has\(> \.tl-dashboard__action:not\(:disabled, \[aria-disabled="true"\]\)\):hover/.test(s)
    && /\.tl-dashboard:not\(\[data-tl-state~="edit"\]\)/.test(s));
  assert.equal(zeigen.length, 1, 'eine Regel für die gezeigte Kachel mit Aktion');
  assert.match(zeigen[0][1], /border-color:\s*var\(--tl-border-interactive\)/);
  assert.doesNotMatch(DASHBOARD, /--tl-focus-ring/);
  // Der Fokusring der Kachel kommt aus base.css: die eine Ring-Regel trägt die Kachel als Alternative.
  const ring = /([^{}]+)\{\s*outline:\s*var\(--tl-focus-width\) solid var\(--tl-focus-ring\);/.exec(BASE);
  assert.ok(ring, 'Ring-Regel fehlt');
  assert.ok(ring[1].split(/,\n/).map((s) => s.trim()).includes('.tl-dashboard__tile:has(> .tl-dashboard__action:focus-visible)'),
    'die Kachel trägt den Ring, wenn ihr verborgener Knopf den Fokus hat');
  // Der verborgene Knopf steht mit Abstand space-sm zur Kachelkante: sein Ring liegt in der Kachel (focus-clip).
  const knopf = regel(DASHBOARD, '.tl-dashboard__action');
  assert.match(knopf, /position:\s*absolute/);
  assert.match(knopf, /inset-block-start:\s*var\(--tl-space-sm\)/);
  assert.match(knopf, /inset-inline-start:\s*var\(--tl-space-sm\)/);
});

// --- tl-value-list (Spec 5c §7) und die Zellregeln -----------------------------------------------------

const WERTELISTE = leseOder('value-list', 'value-list.css');
const TABELLE = readGenerated('table', 'table.css');

test('value-list: Block und Zelle', () => {
  assert.match(regel(WERTELISTE, '.tl-value-list--block'), /flex-direction:\s*column/);
  const zelle = regel(TABELLE, '.tl-table__cell > .tl-value-list:where(span)');
  for (const r of [/text-overflow:\s*ellipsis/, /overflow:\s*hidden/, /white-space:\s*nowrap/, /display:\s*block/])
    assert.match(zelle, r, 'Lesewert in der Zelle: ganze Breite, Ellipse');
  const eintraege = rulesOf(TABELLE).filter(([s]) => s.includes('.tl-table__cell .tl-value-list--block')).map(([, d]) => d).join(';');
  assert.match(eintraege, /display:\s*inline/, 'die Einträge der Variante --block stehen in der Zelle in einer Zeile');
  assert.match(regel(TABELLE, '.tl-table__cell > .tl-stack--row'), /inline-size:\s*100%/);
  const befehl = regel(TABELLE, '.tl-table__cell--command');
  assert.match(befehl, /padding:\s*0/);
  assert.match(befehl, /justify-content:\s*center/);
  assert.match(regel(TABELLE, '.tl-table__header-cell.tl-table__cell--command'), /padding:\s*0/);
  assert.doesNotMatch(ohneKommentar(TABELLE), /tlValueList|tlRowCommandCell|tlStack/);
  // Der Griff ist ein Part Button: kein eigener Block.
  assert.doesNotMatch(ohneKommentar(WERTELISTE), /__handle/);
});

test('drop-hint: im Block drag', () => {
  const drag = readGenerated('drag', 'drag.css');
  assert.match(regel(drag, '.tl-popover.tl-drop-hint'), /border-color:\s*var\(--tl-status-error-border\)/);
  assert.doesNotMatch(TABELLE, /tl-table__hint/);
});
