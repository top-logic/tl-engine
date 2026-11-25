// Tabellenmenüs (#30102): Filter und Spaltenwahl sind Popover, die sofort wirken; jedes Tabellenmenü hat dasselbe Verhalten.
// Das Paket liefert dafür die Zeile als Klickfläche (tl-menu__item--check), die Werteliste (tl-filter-list), den Rollbereich
// im Popover (tl-picker__scroll), das Popover des Zahnrads und die Wurzel der Spaltenwahl. Reihenfolge der Tests wie die
// der Regeln: erst die Zeile, dann was darauf aufbaut.
import { test } from 'node:test';
import { readFileSync } from 'node:fs';
import assert from 'node:assert/strict';
import { readComponent, readGenerated, declarationsOf, rulesOf, missingInContract, specimenProblems } from './lib/component.mjs';

const menu = readGenerated('menu', 'menu.css');
const choice = readGenerated('choice', 'choice.css');
const filterList = readGenerated('filter-list', 'filter-list.css');
const picker = readGenerated('picker', 'picker.css');
const table = readGenerated('table', 'table.css');
const reorder = readGenerated('reorder-list', 'reorder-list.css');
const layout = readGenerated('layout', 'layout.css');

/** Alle Regeln, deren Selektorliste `selector` enthält. */
const rulesWith = (css, selector) => rulesOf(css)
  .filter(([sel]) => sel.split(/,(?![^(]*\))/).map((s) => s.trim()).includes(selector));

// --- Die Zeile als Klickfläche ---------------------------------------------------------------------------------------

test('tl-menu__item--check: die ganze Zeile klickt — Fläche und Hover vom Eintrag, kein tl-choice darin, die Marke zeigt nicht selbst', () => {
  const item = declarationsOf(menu, '.tl-menu__item') ?? '';
  assert.match(item, /display:\s*flex/);
  assert.match(item, /width:\s*100%/);
  assert.match(declarationsOf(menu, '.tl-menu__item:hover') ?? '', /background:\s*var\(--tl-surface-interactive-hover\)/);
  // Eine Hover-Fläche: der Modifier malt keine eigene, sonst gäbe es zwei Zeigen-Bilder in einer Zeile.
  assert.doesNotMatch(declarationsOf(menu, '.tl-menu__item--check') ?? '', /background/);
  assert.equal(declarationsOf(menu, '.tl-menu__item--check:hover'), null);
  // Die gezeichnete Marke nimmt keinen Zeiger an: Rand und Hof der Marke (choice.css) gehören dem Label tl-choice, und
  // das gibt es in der Zeile nicht. Der Zeiger trifft die Zeile, es bleibt bei ihrer Fläche.
  assert.match(declarationsOf(menu, '.tl-menu__item--check .tl-checkbox') ?? '', /pointer-events:\s*none/);
  const md = readComponent('menu', 'menu.contract.md');
  assert.deepEqual(missingInContract(md, ['tl-menu__item--check', 'tl-checkbox--switch', 'aria-hidden', 'menuitemcheckbox']), []);
  const html = readComponent('menu', 'menu.spec.html');
  assert.match(html, /tl-menu__item--check/);
  assert.doesNotMatch(html.match(/<button[^>]*tl-menu__item--check[\s\S]*?<\/button>/g)?.join('') ?? '', /tl-choice|<input/, 'kein tl-choice, keine Eingabe in der Zeile');
});

// Die gezeichnete Marke ohne Eingabe liest dieselben Regeln wie die Eingabe: jede Regel, die :checked / :indeterminate
// zeichnet, trägt auch die Zeilenfassung in ihrer Selektorliste — gleiche Liste, gleicher Block, gleiches Bild.
const ZEILE = '.tl-menu__item--check';
const BEDIENBAR = ':where(:not([aria-readonly="true"]))';
test('Auswahl: Schalter und Kästchen ohne Eingabe in tl-menu__item--check zeichnen dasselbe wie mit :checked', () => {
  const PAARE = [
    ['.tl-checkbox:checked', `${ZEILE}[aria-checked="true"] .tl-checkbox`],
    ['.tl-checkbox:indeterminate', `${ZEILE}[aria-checked="mixed"] .tl-checkbox`],
    ['.tl-checkbox:checked::after', `${ZEILE}[aria-checked="true"] .tl-checkbox::after`],
    ['.tl-checkbox:indeterminate::after', `${ZEILE}[aria-checked="mixed"] .tl-checkbox::after`],
    // Beim Schalter gilt dasselbe für beide Zustände: aus und an.
    // Den Schalter mit Eingabe zeichnet nur der bedienbare (:where(:not([aria-readonly="true"])), choice.css).
    [`.tl-checkbox--switch${BEDIENBAR}::after`, `${ZEILE} .tl-checkbox--switch::after`],
    [`.tl-checkbox--switch${BEDIENBAR}:checked::after`, `${ZEILE}[aria-checked="true"] .tl-checkbox--switch::after`],
  ];
  for (const [eingabe, gezeichnet] of PAARE) {
    const regeln = rulesWith(choice, eingabe);
    assert.ok(regeln.length > 0, `Regel fehlt: ${eingabe}`);
    for (const [sel] of regeln)
      assert.ok(sel.split(/,(?![^(]*\))/).map((s) => s.trim()).includes(gezeichnet), `${eingabe} steht ohne ${gezeichnet}: ${sel}`);
  }
  // Der Schalter ist ein Kästchen mit anderer Form: seine Regeln stehen nach denen des Hakens, damit bei gleicher
  // Spezifität die Schalterfassung gewinnt (der Haken ist am Schalter keiner).
  const i = (t) => choice.indexOf(t);
  assert.ok(i(`${ZEILE}[aria-checked="true"] .tl-checkbox--switch::after`) > i(`${ZEILE}[aria-checked="true"] .tl-checkbox::after`));
});

test('Segmente: das gewählte Segment liest aria-checked, gleich welche role es trägt (menuitemradio im Menü)', () => {
  const ausgewaehlt = rulesWith(choice, '.tl-segmented__segment[aria-checked="true"]');
  assert.equal(ausgewaehlt.length, 1);
  assert.match(ausgewaehlt[0][1], /background:\s*var\(--tl-surface-brand-subtle\)/);
  assert.doesNotMatch(choice, /\[role="radio"\]|\[role=radio\]/, 'keine Regel hängt an role="radio"');
});

// --- Werteliste ---------------------------------------------------------------------------------------------------------

test('tl-filter-list: Zeile = tl-menu__item--check, Anzahl rechts, leiser Wert in text-helper, Kopfzeile „Alle“ mit Trennlinie, Fuß mit Linie', () => {
  const zahl = declarationsOf(filterList, '.tl-filter-list__count') ?? '';
  assert.match(zahl, /margin-inline-start:\s*auto/);
  assert.match(zahl, /flex:\s*0 0 auto/);
  assert.match(zahl, /color:\s*var\(--tl-text-helper\)/);
  // Das Ziffernbild (tl-nums) setzt das Element, nicht diese Datei: es gibt es einmal (base.css).
  assert.doesNotMatch(zahl, /font-variant-numeric/);
  // Ein Wert ohne Treffer ist leiser, bleibt aber bedienbar — text-helper, nicht text-disabled (WCAG-Ausnahme gilt nur für Inaktives).
  assert.match(declarationsOf(filterList, '.tl-filter-list .tl-menu__item--check[data-tl-state~="empty"]') ?? '', /color:\s*var\(--tl-text-helper\)/);
  assert.doesNotMatch(filterList.replace(/\/\*[\s\S]*?\*\//g, ''), /text-disabled/);
  assert.match(declarationsOf(filterList, '.tl-menu__item.tl-filter-list__all') ?? '', /border-bottom:\s*var\(--tl-border-width-sm\) solid var\(--tl-border-separator\)/);
  const fuss = declarationsOf(filterList, '.tl-filter-list__footer') ?? '';
  assert.match(fuss, /border-top:\s*var\(--tl-border-width-sm\) solid var\(--tl-border-separator\)/);
  assert.match(fuss, /display:\s*flex/);
  assert.match(fuss, /gap:\s*var\(--tl-space-xs\)/);
  // Die Hülle tl-picker setzt eine Lücke; hier gilt die dichte Folge der Zeilen (zwei Klassen gegen die eine von .tl-picker).
  assert.match(declarationsOf(filterList, '.tl-popover.tl-filter-list') ?? '', /gap:\s*var\(--tl-space-2xs\)/);
  for (const el of ['all', 'footer'])
    if (/display:\s*flex/.test(declarationsOf(filterList, `.tl-filter-list__${el}`) ?? ''))
      assert.match(declarationsOf(filterList, `.tl-filter-list__${el}[hidden]`) ?? '', /display:\s*none/, el);
});

test('tl-filter-list: Vertrag und Specimen — Zeilen als menuitemcheckbox mit gezeichnetem Kästchen, Anzahl mit tl-nums', () => {
  const md = readComponent('filter-list', 'filter-list.contract.md');
  assert.deepEqual(missingInContract(md, ['tl-filter-list', 'tl-filter-list__count', 'tl-filter-list__all', 'tl-filter-list__footer',
    'tl-menu__item--check', 'tl-picker__scroll', 'menuitemcheckbox', 'aria-checked', 'mixed', 'tl-nums', 'data-tl-state~="empty"',
    '--tl-text-helper', 'role="dialog"', 'Escape']), []);
  const html = readComponent('filter-list', 'filter-list.spec.html');
  assert.deepEqual(specimenProblems(html), []);
  const zeilen = [...html.matchAll(/<button[^>]*tl-menu__item--check[\s\S]*?<\/button>/g)].map((m) => m[0]);
  assert.ok(zeilen.length >= 5, 'Specimen zeigt Alle und Werte');
  for (const z of zeilen) {
    assert.match(z, /role="menuitemcheckbox"/);
    assert.match(z, /aria-checked="(true|false|mixed)"/);
    assert.match(z, /<span class="tl-checkbox" aria-hidden="true"><\/span>/, 'Kästchen ohne Eingabe');
    assert.doesNotMatch(z, /<input|tl-choice/);
  }
  assert.match(html, /tl-filter-list__count[^"]*tl-nums|tl-nums[^"]*tl-filter-list__count/);
  for (const t of ['data-tl-state="empty"', 'aria-checked="mixed"', 'tl-filter-list__all', 'tl-filter-list__footer', 'tl-picker__scroll', 'tl-reveal-on-hover'])
    assert.ok(html.includes(t), `Specimen zeigt ${t}`);
  const index = readFileSync(new URL('../specimen/index.html', import.meta.url), 'utf8');
  assert.match(index, /filter-list\.spec\.html/);
});

// --- Rollbereich im Popover ---------------------------------------------------------------------------------------------

test('tl-picker__scroll: füllt den Rest der Hülle, schrumpft mit ihr, rollt allein — die Höhe gibt usePopover als maxHeight', () => {
  const d = declarationsOf(picker, '.tl-picker__scroll') ?? '';
  assert.match(d, /flex:\s*1 1 auto/);
  assert.match(d, /min-block-size:\s*0/);
  assert.match(d, /overflow-y:\s*auto/);
  assert.doesNotMatch(d, /var\(/, 'keine Variable und kein Rückfall: die Höhe kommt als maxHeight an der Hülle');
  // Die Hülle bleibt, wie sie ist: .tl-picker ist schon die Flex-Spalte, .tl-popover trägt keine eigene Höhe (popover.test.mjs).
  assert.match(declarationsOf(picker, '.tl-picker') ?? '', /flex-direction:\s*column/);
  const md = readComponent('picker', 'picker.contract.md');
  assert.deepEqual(missingInContract(md, ['tl-picker__scroll', 'maxHeight', 'usePopover']), []);
  assert.match(readComponent('picker', 'picker.spec.html'), /tl-picker__scroll/);
});

// --- Spalten: das Popover des Zahnrads und die Spaltenwahl ---------------------------------------------------------------

const SPALTE = [/display:\s*flex/, /flex-direction:\s*column/, /flex:\s*1 1 auto/, /min-block-size:\s*0/];

test('Spalten: das Popover hat eine Breite aus dem Token', () => {
  const popover = declarationsOf(table, '.tl-popover.tl-table-view-options') ?? '';
  assert.match(popover, /inline-size:\s*min\(var\(--tl-size-popover-view\)/);
});

test('Spalten: eine Seite im Popover — Flex-Spalte mit einem Rollbereich (der Liste); keine zweite Seite, kein Zurück, kein Menü der Ansicht', () => {
  const seite = declarationsOf(table, '.tl-table-view-options__page') ?? '';
  for (const re of SPALTE) assert.match(seite, re, String(re));
  assert.match(seite, /gap:\s*var\(--tl-space-2xs\)/, 'die dichte Folge des Popovers gilt auch in der Seite');
  // Kein zweiter Rollbereich: die Seite scrollt nicht selbst, es rollt die Liste der Spaltenwahl.
  assert.doesNotMatch(seite, /overflow/);
  assert.match(declarationsOf(table, '.tl-table-view-options__title') ?? '', /padding:\s*var\(--tl-space-2xs\) var\(--tl-space-control-x\)/);
  // Dichte, Zebra und Spaltenlinien kommen aus dem Benutzermenü: was die zweite Seite der Ansicht zeichnete, ist weg.
  for (const weg of ['.tl-table-view-options__page[hidden]', '.tl-table-view-options__back', '.tl-table-view-options__back:focus-visible',
    '.tl-table-view-options__menu', '.tl-table-view-options__segments', '.tl-table-view-options__switch'])
    assert.equal(declarationsOf(table, weg), null, `${weg} gibt es nicht mehr`);
  assert.doesNotMatch(table, /tl-table-view-options__(back|menu|segments)/);
});

test('Spaltenwahl: die Wurzel trägt tl-reorder-list__host — Flex-Spalte, füllt die Seite, schrumpft; die Liste behält max-height', () => {
  const host = declarationsOf(reorder, '.tl-reorder-list__host') ?? '';
  for (const re of SPALTE) assert.match(host, re, String(re));
  assert.match(declarationsOf(reorder, '.tl-reorder-list__host[hidden]') ?? '', /display:\s*none/);
  // Im Popover liefern Popover und Titelzeile den Abstand: die Wurzel trägt dort weder Fensterabstand noch Fensterhöhe.
  const imPopover = declarationsOf(table, '.tl-table-view-options__page > .tl-reorder-list__host') ?? '';
  assert.match(imPopover, /padding:\s*0/);
  assert.match(imPopover, /block-size:\s*auto/);
  // Die Liste bleibt gedeckelt (drag.test.mjs); nur mit Suche davor füllt sie den Behälter.
  assert.match(declarationsOf(reorder, '.tl-reorder-list') ?? '', /max-height:\s*var\(--tl-size-popover-max\)/);
  assert.doesNotMatch(reorder, /\.tl-reorder-list__host[^{]*\{[^}]*(overflow|max-height)/, 'der Rollbereich ist die Liste, nicht die Wurzel');
  const md = readComponent('reorder-list', 'reorder-list.contract.md');
  assert.deepEqual(missingInContract(md, ['tl-reorder-list__host']), []);
  assert.match(readComponent('reorder-list', 'reorder-list.spec.html'), /tl-reorder-list__host/);
});

// Die Zeile der Spaltenwahl klickt (K3): Zeiger, Hover, und die letzte sichtbare Spalte klickt nicht — kein Zeiger, keine Hover-Fläche.
// Die Liste hat drei Abschnitte in einem Rollbereich (E12): Fixiert, Beweglich, Ausgeblendet; die Zeilen darunter sind Zeilen derselben Liste.
test('Spaltenwahl: Zeile mit Zeiger, die letzte sichtbare ohne Zeiger und Hover; Abschnitte Fixiert, Beweglich, Ausgeblendet', () => {
  assert.match(declarationsOf(reorder, '.tl-reorder-list__item') ?? '', /cursor:\s*pointer/);
  assert.match(declarationsOf(reorder, '.tl-reorder-list__item[data-tl-state~="last"]') ?? '', /cursor:\s*default/);
  assert.match(declarationsOf(reorder, ':where(.tl-reorder-list__item[data-tl-state~="last"]:hover)') ?? '', /background:\s*none/);
  const md = readComponent('reorder-list', 'reorder-list.contract.md');
  assert.deepEqual(missingInContract(md, ['tl-reorder-list__section', 'last']), []);
  for (const w of ['„Beweglich“', '„Ausgeblendet“', 'Ziehen geht über jede Grenze']) assert.ok(md.includes(w), `Vertrag nennt ${w}`);
  // Die Abschnitte sind Ablagezonen (Z1–Z8): ein Befehl columnMove für Ziehen und Tastatur, columnReorder sendet die Spaltenwahl
  // nicht mehr; die Platzhalter, die Ansagen beim Wechsel des Abschnitts, der Zielzustand und die Revision stehen im Vertrag.
  assert.deepEqual(missingInContract(md, ['columnMove', 'revision',
    'js.table.columnDropLocked', 'js.table.columnDropMovable', 'js.table.columnDropHidden',
    'js.table.columnMovedLocked', 'js.table.columnMovedMovable', 'js.table.columnMovedHidden']), []);
  assert.doesNotMatch(md, /columnReorder/, 'die Spaltenwahl kennt columnReorder nicht mehr');
  for (const w of ['„Hierher ziehen, um zu fixieren“', '„Hierher ziehen, um zu lösen“', '„Hierher ziehen, um auszublenden“',
    'data-tl-state="drop-target"']) assert.ok(md.includes(w), `Vertrag nennt ${w}`);
  const html = readComponent('reorder-list', 'reorder-list.spec.html');
  for (const t of ['>Fixiert<', '>Beweglich<', '>Ausgeblendet<', 'data-tl-state="last"', 'bi-plus', 'bi-dash', 'tl-reorder-list__section'])
    assert.ok(html.includes(t), `Specimen zeigt ${t}`);
  // Eine ausgeblendete Zeile hat einen Griff und ist ziehbar (Z3); leere Abschnitte zeigen den Platzhalter (Z4).
  const aus = html.match(/<li class="tl-reorder-list__item" data-tl-state="off[^"]*">[\s\S]*?<\/li>/)?.[0] ?? '';
  assert.ok(aus.includes('tl-reorder-list__handle'), 'die ausgeblendete Zeile hat einen Griff');
  assert.ok(html.includes('tl-reorder-list__empty') && html.includes('drop-target'), 'Specimen zeigt Platzhalter und Zielabschnitt');
});

test('Spalten: Vertrag und Specimen — eine Seite, Spaltenwahl, Filter-Popover am Trichter; nichts von der Ansicht, die ins Benutzermenü zog', () => {
  const md = readComponent('table', 'table.contract.md');
  assert.deepEqual(missingInContract(md, ['tl-table-view-options__page', 'tl-table-view-options__title', 'tl-reorder-list__host',
    'tl-filter-list', 'aria-expanded', 'data-tl-striped', 'data-tl-column-lines']), []);
  assert.match(md, /Benutzermenü/, 'der Vertrag sagt, wo Dichte, Zebra und Spaltenlinien eingestellt werden');
  assert.doesNotMatch(md, /tl-table-view-options__(back|menu|segments|switch)|tl-menu__item--nav|menuitemradio|data-tl-density="/);
  const html = readComponent('table', 'table.spec.html');
  for (const t of ['tl-table-view-options__page', 'tl-reorder-list__host', 'tl-filter-list', 'aria-expanded="true"'])
    assert.ok(html.includes(t), `Specimen zeigt ${t}`);
  assert.doesNotMatch(html, /tl-table-view-options__(back|menu|segments|switch)|tl-menu__item--nav|menuitemradio/);
});

// Das Popover des Zahnrads ist kein Menü (#30233): kein role="menu", der Knopf „Spalten zurücksetzen“ ist ein schlichter Knopf.
// Ohne Spaltenwahl besteht es aus diesem Knopf allein, ohne Titel und ohne Trennlinie.
test('Spalten: das Popover ist kein Menü; „Spalten zurücksetzen“ ohne Rolle, allein ohne Titel und Trennlinie', () => {
  const html = readComponent('table', 'table.spec.html');
  const panel = html.match(/<p class="tl-type-heading-sm">Spalten: das Popover des Zahnrads<\/p>[\s\S]*?<\/section>/)?.[0] ?? '';
  assert.ok(panel.length > 0, 'Specimen zeigt das Popover des Zahnrads');
  assert.doesNotMatch(panel, /role="menu"|role="menuitem/, 'kein Menü');
  assert.doesNotMatch(panel, /tl-menu__value|\d+ von \d+/, 'kein Wert „n von m“');
  const popovers = [...panel.matchAll(/<div class="tl-popover tl-picker tl-menu tl-table-view-options[^"]*" role="dialog" aria-label="Spalten">[\s\S]*?<\/div>\s*<\/div>\s*(?=<div class="tl-popover|<\/div>\s*<\/section>|$)/g)].map((m) => m[0]);
  assert.equal(popovers.length, 2, 'das Specimen zeigt das Popover mit Spaltenwahl und das mit dem Knopf allein');
  const reset = /<button type="button" class="tl-menu__item tl-type-body"><i class="tl-menu__icon [^"]*" aria-hidden="true"><\/i><span class="tl-menu__label">Spalten zurücksetzen<\/span><\/button>/;
  // Mit Spaltenwahl: Titel, Spaltenwahl, Trennlinie, „Spalten zurücksetzen“ als Letztes.
  const [mitWahl, allein] = popovers;
  assert.match(mitWahl, /tl-table-view-options__title[^>]*>Spalten<\/div>/);
  assert.ok(mitWahl.indexOf('tl-reorder-list__host') < mitWahl.indexOf('<hr class="tl-divider">'), 'die Trennlinie steht nach der Spaltenwahl');
  assert.match(mitWahl, new RegExp('<hr class="tl-divider">\\s*' + reset.source + '\\s*</div>\\s*</div>\\s*$'), 'Trennlinie, dann der Knopf als Letztes');
  // Allein: nur der Knopf.
  assert.doesNotMatch(allein, /tl-table-view-options__title|<hr|tl-reorder-list/, 'allein: ohne Titel, Trennlinie und Spaltenwahl');
  assert.match(allein, reset);
});

// Die Spaltenwahl wirkt sofort (E7, #30102): der Server hält sie als Kind, solange das Popover offen ist; am Fuß „Spalten
// zurücksetzen“ (#30233).
test('Spalten: der Vertrag nennt Zustand und Kommandos der Spaltenwahl und „Spalten zurücksetzen“', () => {
  const md = readComponent('table', 'table.contract.md');
  assert.deepEqual(missingInContract(md, ['columnSelection', 'openColumns', 'closeColumns']), []);
  assert.doesNotMatch(md, /openColumnSelect/);
  assert.doesNotMatch(md, /columnsShown|columnsTotal|n von m/, 'der Vertrag kennt kein „n von m“ mehr');
  assert.match(md, /Spalten zurücksetzen/, 'der Vertrag nennt „Spalten zurücksetzen“');
  const reorder = readComponent('reorder-list', 'reorder-list.contract.md');
  assert.match(reorder, /Klick auf die Zeile/, 'die Zeile der Spaltenwahl klickt');
});

// Der Eintrag mit Weg (Wert und Chevron, eine zweite Seite im Popover) ist mit der Seite „Ansicht“ gegangen (#30233): niemand nutzt ihn.
test('Menü: kein Eintrag mit Weg mehr — weder Regel noch Vertrag noch Specimen', () => {
  assert.doesNotMatch(menu, /tl-menu__item--nav|tl-menu__value|tl-menu__chevron/);
  assert.doesNotMatch(readComponent('menu', 'menu.contract.md'), /tl-menu__item--nav|tl-menu__value|tl-menu__chevron/);
  assert.doesNotMatch(readComponent('menu', 'menu.spec.html'), /tl-menu__item--nav|tl-menu__value|tl-menu__chevron/);
});

// --- Anker des offenen Popovers -------------------------------------------------------------------------------------------

test('tl-reveal-on-hover: der Anker eines offenen Popovers (aria-expanded="true") bleibt sichtbar', () => {
  assert.match(declarationsOf(layout, '.tl-reveal-on-hover[aria-expanded="true"]') ?? '', /opacity:\s*1/);
  const md = readComponent('layout', 'layout.contract.md');
  assert.deepEqual(missingInContract(md, ['aria-expanded']), []);
});
