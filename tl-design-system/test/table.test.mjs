// Die Tabelle — 3b §3–§6: Zeile size-row, Kopf size-bar, Lage und Cursor als Attribute.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readComponent, readGenerated, declarationsOf, rulesOf } from './lib/component.mjs';

const css = readGenerated('table', 'table.css');

test('tl-table: Zeile size-row, Kopf size-bar, Zustände als Attribute, keine Literale des Altbestands', () => {
  assert.match(declarationsOf(css, '.tl-table__row') ?? '', /min-height:\s*var\(--tl-size-row\)/);
  // Positioniert: Bezug für den Ring (::after) und die Ziehen-Marke (::before), auch im Specimen.
  assert.match(declarationsOf(css, '.tl-table__row') ?? '', /position:\s*relative/);
  // Jedes Element, das display setzt, setzt auch [hidden] (3a-Regel; hidden.test prüft nur Blöcke).
  for (const el of ['filter-bar', 'save', 'frame', 'grid', 'header-row', 'header-cell', 'sort', 'row', 'cell', 'tree', 'placeholder'])
    assert.match(declarationsOf(css, `.tl-table__${el}[hidden]`) ?? '', /display:\s*none/, el);
  assert.match(declarationsOf(css, '.tl-table__row') ?? '', /border-bottom:\s*var\(--tl-border-width-sm\) solid var\(--tl-border-separator\)/);
  assert.match(declarationsOf(css, ':where(.tl-table__row:hover)') ?? '', /surface-interactive-hover/);
  assert.match(declarationsOf(css, ':where(.tl-table__row[aria-selected="true"])') ?? '', /surface-interactive-selected/);
  assert.match(declarationsOf(css, ':where(.tl-table__row)') ?? '', /background:\s*var\(--tl-surface-layer\)/);
  assert.match(declarationsOf(css, '.tl-table__header-row') ?? '', /min-height:\s*var\(--tl-size-bar\)/);
  assert.match(declarationsOf(css, '.tl-table__header') ?? '', /border-bottom:\s*var\(--tl-border-width-sm\) solid var\(--tl-border-emphasis\)/);
  const haftend = declarationsOf(css, '.tl-table__cell[data-tl-state~="frozen"]') ?? '';
  assert.match(haftend, /position:\s*sticky/);
  assert.match(haftend, /z-index:\s*var\(--tl-layer-raised\)/);
  assert.match(haftend, /background:\s*inherit/);
  assert.match(declarationsOf(css, '.tl-table__cell[data-tl-state~="frozen-last"]') ?? '', /border-emphasis/);
  assert.match(declarationsOf(css, '.tl-table__cell[data-tl-state~="pinned-first"]') ?? '', /border-emphasis/);
  assert.match(declarationsOf(css, '.tl-table__freeze-preview') ?? '', /z-index:\s*var\(--tl-layer-sticky\)/);
  assert.match(declarationsOf(css, '.tl-table__spacer') ?? '', /width:\s*var\(--tl-size-control-sm\)/);
  assert.match(declarationsOf(css, '.tl-table__cell .tl-field[aria-invalid="true"]') ?? '',
    /box-shadow:\s*inset 0 0 0 var\(--tl-border-width-md\) var\(--tl-status-error-border\)/);
  assert.match(declarationsOf(css, '.tl-table__cell .tl-field[data-tl-state~="warning"]') ?? '', /status-warning-border/);
  assert.doesNotMatch(css, /shadow-raised|clip-path|accent-color|outline\s*:|font-weight|--(selected|cursor|group|frozen|pinned|active)\b/);
  // Ein aktiver Filter ist farbig, nicht nur ein dunkleres Grau (#29999: icon-primary neben icon-secondary war nicht zu sehen).
  assert.match(declarationsOf(css, '.tl-table__filter') ?? '', /icon-secondary/);
  assert.match(declarationsOf(css, '.tl-table__header-cell[data-tl-state~="filtered"] .tl-table__filter') ?? '', /text-link/);
});

test('ein bearbeitbares Feld in der Zelle zeigt sich durch eine Linie unten, auf gezeigter und ausgewählter Zeile in border-interactive', () => {
  // Sichtprüfung 23.10.: randlos und durchsichtig sah die Zelle im Bearbeiten-Modus aus wie im Lesen. Gemessen gegen die
  // Zeile: border-control hält auf surface-layer 3,42 hell / 3,69 dunkel, auf der ausgewählten Zeile nur 2,78 / 2,44;
  // border-interactive dort 4,75 / 3,63, gezeigt 5,21 / 3,87.
  // Das Feld ist so hoch wie die Zeile: die Zelle streckt ihre Hülle, sonst sitzt die Linie mitten in der Zeile.
  assert.match(declarationsOf(css, '.tl-table__cell:has(.tl-field)') ?? '', /align-items:\s*stretch/);
  const feld = '.tl-table__cell .tl-field:not(:disabled, [readonly])';
  assert.match(declarationsOf(css, feld) ?? '',
    /box-shadow:\s*inset 0 calc\(-1 \* var\(--tl-border-width-sm\)\) 0 var\(--tl-border-control\)/);
  assert.match(declarationsOf(css, `:where(.tl-table__row:hover, .tl-table__row[aria-selected="true"]) ${feld}`) ?? '',
    /box-shadow:\s*inset 0 calc\(-1 \* var\(--tl-border-width-sm\)\) 0 var\(--tl-border-interactive\)/);
  // Fehler und Warnung stehen danach und gewinnen: ihr Rahmen ist die ganze Zelle.
  assert.ok(css.indexOf(`:where(.tl-table__row:hover, .tl-table__row[aria-selected="true"]) ${feld}`)
    < css.indexOf('.tl-table__cell .tl-field[data-tl-state~="warning"]'));
});

test('der Körper isoliert seinen Stapel, Spaltenwahl und Fixiergrenze sind so hoch wie die Kopfzeile', () => {
  // Fix-Welle 3b (7): die Wurzel isoliert ebenso — ihre Ablage-Marke (layer-sticky) bleibt in der Tabelle.
  assert.match(declarationsOf(css, '.tl-table') ?? '', /isolation:\s*isolate/);
  assert.match(declarationsOf(css, '.tl-table__body') ?? '', /isolation:\s*isolate/);
  // Der Rahmen isoliert die Ebenen von Kopf, Griff und Vorschau: eine haftende Kopfzelle (layer-raised)
  // liegt nie über einem Popover oder einem Nachbarn der Seite (E7, Orchestrator-Entscheid 6).
  assert.match(declarationsOf(css, '.tl-table__frame') ?? '', /isolation:\s*isolate/);
  // Der Kopf steht außerhalb des Rollbehälters: der Körper schneidet Ring (::after) und Marke (::before) seiner
  // Zeilen ab, die unter den Kopf rollen. Darum braucht der Kopf keine eigene Ebene (table.contract.md „Stapel“).
  assert.match(declarationsOf(css, '.tl-table__body') ?? '', /overflow:\s*auto/);
  assert.doesNotMatch(declarationsOf(css, '.tl-table__header') ?? '', /z-index|position/);
  assert.match(declarationsOf(css, '.tl-table__columns') ?? '', /height:\s*var\(--tl-size-bar\)/);
  assert.match(declarationsOf(css, '.tl-table__freeze') ?? '', /height:\s*var\(--tl-size-bar\)/);
  assert.match(declarationsOf(readGenerated('drag', 'drag.css'), '.tl-popover.tl-drop-hint') ?? '', /status-error-border/);
});

// E8 (08.10.): fixierte und angeheftete Kopfzellen sind deckend — sie erben die Fläche der Kopfzeile, über die
// scrollende Überschriften laufen. Jede Kopfzelle isoliert ihren Stapel: die Einfügemarke eines Spaltenzugs
// (::before, layer-sticky) einer Überschrift, die unter die fixierten rollt, malt nicht über deren Zellen.
test('Kopf: haftende Zellen deckend, Einfügemarke einer Überschrift bleibt in ihrer Zelle', () => {
  assert.match(declarationsOf(css, '.tl-table__header-row') ?? '', /background:\s*var\(--tl-surface-layer\)/);
  for (const lage of ['frozen', 'pinned']) {
    const d = declarationsOf(css, `.tl-table__header-cell[data-tl-state~="${lage}"]`) ?? '';
    assert.match(d, /position:\s*sticky/, lage);
    assert.match(d, /z-index:\s*var\(--tl-layer-raised\)/, lage);
    assert.match(d, /background:\s*inherit/, lage);
  }
  assert.match(declarationsOf(css, '.tl-table__header-cell') ?? '', /isolation:\s*isolate/);
});

// Fix-Runde 1 (07.10.): Füllen ist für Lesewerte ohne eigene Form. Eine Kapsel (Pill — seit P6 die Wurzel eines
// TLText als Pill —, Badge, Chip) direkt in der Zelle würde sonst zum Block über die ganze Zellbreite.
test('der Lesewert füllt die Zelle: nur span ohne Klasse und tl-text, keine Kapsel', () => {
  assert.equal(declarationsOf(css, '.tl-table__cell > span'), null, 'keine Füll-Regel für jeden span');
  for (const sel of ['.tl-table__cell > :where(span):not([class])', '.tl-table__cell > .tl-text']) {
    const d = declarationsOf(css, sel) ?? '';
    for (const re of [/display:\s*block/, /width:\s*100%/, /text-overflow:\s*ellipsis/, /white-space:\s*nowrap/]) assert.match(d, re, sel);
  }
  for (const [sels, body] of rulesOf(css))
    if (/\.tl-(pill|badge|chip)\b/.test(sels)) assert.doesNotMatch(body, /display\s*:|(^|[^-])width\s*:\s*100%/, sels);
});

test('Kapseln in der Zelle behalten ihre Form und bleiben in der Zelle (max-width), die Pill kürzt mit Ellipse', () => {
  const kapsel = declarationsOf(css, '.tl-table__cell > .tl-pill') ?? '';
  assert.match(kapsel, /max-width:\s*100%/);
  for (const k of ['.tl-table__cell > .tl-badge', '.tl-table__cell > .tl-chip']) assert.equal(declarationsOf(css, k), kapsel, k);
  assert.match(declarationsOf(css, '.tl-table__cell > .tl-pill > .tl-pill__label') ?? '', /min-width:\s*0/);
});


test('die Leerzeile klebt am linken Rand des Körpers, auch waagerecht gescrollt (Sichtprüfung 12.10.)', () => {
  // Befund: Filter nimmt die letzte Zeile, der Körper steht bei scrollLeft 500 — „Keine Einträge" lag links
  // außerhalb des Bildes. Die Zeile ist der einzige Block im Körper, der nicht mit den Spalten wandern soll.
  const d = declarationsOf(css, '.tl-table__body > [role="row"]:has(> .tl-status)') ?? '';
  assert.match(d, /position:\s*sticky/);
  assert.match(d, /inset-inline-start:\s*0/);
  const md = readComponent('table', 'table.contract.md');
  assert.match(md, /klebt am linken Rand des Körpers/);
});

test('der Kopf scrollt waagerecht ohne eigenen Balken (Hendrik 12.10.)', () => {
  // Vorher overflow: hidden — Mausrad und Trackpad über dem Kopf taten nichts; seit der Körper dem Kopf folgt,
  // darf der Kopf selbst scrollen.
  const d = declarationsOf(css, '.tl-table__header') ?? '';
  assert.match(d, /overflow-x:\s*auto/);
  assert.match(d, /overflow-y:\s*hidden/);
  assert.match(d, /scrollbar-width:\s*none/);
});
