// Tabellenmenüs (#30102): Filter und Spaltenwahl sind Popover, die sofort wirken; jedes Tabellenmenü hat dasselbe Verhalten.
// Das Paket liefert dafür die Zeile als Klickfläche (tl-menu__item--check), die Werteliste (tl-filter-list), den Rollbereich
// im Popover (tl-picker__scroll), die zweite Seite der Ansicht und die Wurzel der Spaltenwahl. Reihenfolge der Tests wie die
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
test('Auswahl: Schalter und Kästchen ohne Eingabe in tl-menu__item--check zeichnen dasselbe wie mit :checked', () => {
  const PAARE = [
    ['.tl-checkbox:checked', `${ZEILE}[aria-checked="true"] .tl-checkbox`],
    ['.tl-checkbox:indeterminate', `${ZEILE}[aria-checked="mixed"] .tl-checkbox`],
    ['.tl-checkbox:checked::after', `${ZEILE}[aria-checked="true"] .tl-checkbox::after`],
    ['.tl-checkbox:indeterminate::after', `${ZEILE}[aria-checked="mixed"] .tl-checkbox::after`],
    // Beim Schalter gilt dasselbe für beide Zustände: aus und an.
    ['.tl-checkbox--switch::after', `${ZEILE} .tl-checkbox--switch::after`],
    ['.tl-checkbox--switch:checked::after', `${ZEILE}[aria-checked="true"] .tl-checkbox--switch::after`],
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

test('Segmente der Dichte: das gewählte Segment liest aria-checked, gleich welche role es trägt (menuitemradio im Menü)', () => {
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
  // Die Hülle tl-picker (später in der Datei, gleiche Spezifität) setzt eine Lücke; hier gilt die dichte Folge der Zeilen.
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

// --- Ansicht: zweite Seite und Spaltenwahl -------------------------------------------------------------------------------

const SPALTE = [/display:\s*flex/, /flex-direction:\s*column/, /flex:\s*1 1 auto/, /min-block-size:\s*0/];

test('Ansicht: zwei Seiten im Popover — jede eine Flex-Spalte mit einem Rollbereich (der Liste), Kopf mit Zurück-Knopf', () => {
  const seite = declarationsOf(table, '.tl-table-view-options__page') ?? '';
  for (const re of SPALTE) assert.match(seite, re, String(re));
  assert.match(seite, /gap:\s*var\(--tl-space-2xs\)/, 'die dichte Folge des Popovers gilt auch in der Seite');
  assert.match(declarationsOf(table, '.tl-table-view-options__page[hidden]') ?? '', /display:\s*none/);
  // Kein zweiter Rollbereich: die Seite scrollt nicht selbst, es rollt die Liste der Spaltenwahl.
  assert.doesNotMatch(seite, /overflow/);
  // Der Kopf mit Zurück: Knopf und Titel in einer Zeile, der Ring des Knopfs innen (das Popover schneidet).
  assert.match(declarationsOf(table, '.tl-table-view-options__title') ?? '', /display:\s*flex/);
  assert.match(declarationsOf(table, '.tl-table-view-options__title') ?? '', /align-items:\s*center/);
  assert.match(declarationsOf(table, '.tl-table-view-options__back') ?? '', /flex:\s*0 0 auto/);
  assert.match(declarationsOf(table, '.tl-table-view-options__back:focus-visible') ?? '', /outline-offset:\s*calc\(-1 \* var\(--tl-focus-offset\)\)/);
  // Der Schalter ist eine Zeile des Menüs (tl-menu__item--check), kein tl-choice mehr: die Einrückungsregel entfällt.
  assert.equal(declarationsOf(table, '.tl-table-view-options__switch'), null);
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

test('Ansicht: Vertrag und Specimen — Seiten, Zurück, Schalter als Zeile ohne Eingabe, Dichte als menuitemradio, Filter-Popover am Trichter', () => {
  const md = readComponent('table', 'table.contract.md');
  assert.deepEqual(missingInContract(md, ['tl-table-view-options__page', 'tl-table-view-options__back', 'tl-reorder-list__host',
    'tl-menu__item--check', 'menuitemcheckbox', 'menuitemradio', 'tl-filter-list', 'aria-expanded', 'tl-checkbox--switch']), []);
  assert.doesNotMatch(md, /tl-table-view-options__switch/);
  const html = readComponent('table', 'table.spec.html');
  for (const t of ['tl-table-view-options__page', 'tl-table-view-options__back', 'tl-reorder-list__host', 'tl-menu__item--check',
    'role="menuitemradio"', 'tl-checkbox tl-checkbox--switch', 'tl-filter-list', 'aria-expanded="true"'])
    assert.ok(html.includes(t), `Specimen zeigt ${t}`);
  assert.doesNotMatch(html, /tl-table-view-options__switch/);
  assert.doesNotMatch(html.match(/tl-table-view-options[\s\S]*?<\/section>/)?.[0] ?? '', /type="checkbox" role="switch"/, 'der Schalter der Ansicht ist gezeichnet');
});

// --- Anker des offenen Popovers -------------------------------------------------------------------------------------------

test('tl-reveal-on-hover: der Anker eines offenen Popovers (aria-expanded="true") bleibt sichtbar', () => {
  assert.match(declarationsOf(layout, '.tl-reveal-on-hover[aria-expanded="true"]') ?? '', /opacity:\s*1/);
  const md = readComponent('layout', 'layout.contract.md');
  assert.deepEqual(missingInContract(md, ['aria-expanded']), []);
});
