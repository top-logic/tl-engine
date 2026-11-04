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

test('tl-filter-list__form: das Formular eines Filters rollt mit Rand für die Fokusringe, die Hauptaktion steht am Ende des Fußes', () => {
  // Wie das Reiterfeld des Pickers: der Rand hält den Fokusring der Felder an der Kante im overflow des Rollbereichs.
  assert.match(declarationsOf(filterList, '.tl-filter-list__form') ?? '', /padding:\s*var\(--tl-space-2xs\)/);
  // „Anwenden“ (Filter-UI einer Anwendung) steht am Ende, „Filter entfernen“ vorn — wie im Mockup u27.
  assert.match(declarationsOf(filterList, '.tl-filter-list__footer > .tl-button--primary') ?? '', /margin-inline-start:\s*auto/);
  const md = readComponent('filter-list', 'filter-list.contract.md');
  assert.deepEqual(missingInContract(md, ['tl-filter-list__form', 'tl-button--primary', 'tl-status']), []);
  // Jedes Filter-Popover trägt die Wurzel tl-filter-list: die Tabelle kennt die Art des Filters nicht.
  assert.match(md, /jede[sm]? Filter-Popover|jedes Popover am Trichter/i);
  const html = readComponent('filter-list', 'filter-list.spec.html');
  const formulare = [...html.matchAll(/<div class="tl-popover tl-picker tl-filter-list[^"]*"[\s\S]*?<div class="tl-filter-list__footer">[\s\S]*?<\/div>/g)]
    .map((m) => m[0]).filter((p) => p.includes('tl-filter-list__form'));
  assert.ok(formulare.length >= 2, 'Specimen zeigt Textfilter und Filter-UI einer Anwendung');
  for (const f of formulare) {
    assert.match(f, /class="tl-picker__scroll tl-filter-list__form"/);
    assert.match(f, /<input class="tl-field/, 'ein Feld, dessen Fokusring gemessen wird (focus-clip)');
  }
  assert.ok(formulare.some((f) => /tl-filter-list__footer">[\s\S]*tl-button tl-button--primary[^"]*"><span class="tl-button__label">Anwenden/.test(f)), 'Anwenden im Fuß');
  // Die Werteliste ohne Treffer: tl-status an Stelle des Menüs.
  assert.match(html, /class="tl-status tl-type-body" role="status">Kein Wert passt/);
});

// Die Aktionen des Fußes sind Knöpfe wie jeder andere (Part `Button`, button.contract.md): Body-Schrift, Beschriftung in
// tl-button__label — kein Sonderweg in Label-Schrift (Entscheidung 04.11., Befund 2 des Final Review).
test('tl-filter-list__footer: die Aktionen sind Knöpfe des Parts Button — tl-button in tl-type-body, Beschriftung in tl-button__label', () => {
  const md = readComponent('filter-list', 'filter-list.contract.md');
  const fussZeile = md.split('\n').find((zeile) => zeile.startsWith('| `tl-filter-list__footer`')) ?? '';
  assert.match(fussZeile, /Part `Button`/);
  assert.match(fussZeile, /tl-type-body/);
  assert.doesNotMatch(fussZeile, /tl-type-label/);
  const html = readComponent('filter-list', 'filter-list.spec.html');
  const fuesse = [...html.matchAll(/<div class="tl-filter-list__footer">([\s\S]*?)<\/div>/g)].map((m) => m[1]);
  assert.ok(fuesse.length >= 4, 'Specimen zeigt die Füße');
  for (const fuss of fuesse) {
    const knoepfe = [...fuss.matchAll(/<button[^>]*>[\s\S]*?<\/button>/g)].map((m) => m[0]);
    assert.ok(knoepfe.length > 0, fuss);
    for (const k of knoepfe) {
      assert.match(k, /class="tl-button tl-button--(ghost|primary) tl-type-body"/, k);
      assert.match(k, /<span class="tl-button__label">[^<]+<\/span><\/button>$/, k);
    }
  }
});

test('table.contract.md: Filter-Popover als State und Aktion', () => {
  const md = readComponent('table', 'table.contract.md');
  assert.deepEqual(missingInContract(md, ['filterPopover', 'closeFilter', 'openFilter']), []);
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

// Die Zeile der Spaltenwahl klickt (K3): Zeiger, Hover, und die letzte sichtbare Spalte klickt nicht — kein Zeiger, keine Hover-Fläche.
// Die Liste hat drei Abschnitte in einem Rollbereich (E12): Fixiert, Beweglich, Ausgeblendet; die Zeilen darunter sind Zeilen derselben Liste.
test('Spaltenwahl: Zeile mit Zeiger, die letzte sichtbare ohne Zeiger und Hover; Abschnitte Fixiert, Beweglich, Ausgeblendet', () => {
  assert.match(declarationsOf(reorder, '.tl-reorder-list__item') ?? '', /cursor:\s*pointer/);
  assert.match(declarationsOf(reorder, '.tl-reorder-list__item[data-tl-state~="last"]') ?? '', /cursor:\s*default/);
  assert.match(declarationsOf(reorder, ':where(.tl-reorder-list__item[data-tl-state~="last"]:hover)') ?? '', /background:\s*none/);
  const md = readComponent('reorder-list', 'reorder-list.contract.md');
  assert.deepEqual(missingInContract(md, ['tl-reorder-list__section', 'last']), []);
  for (const w of ['„Beweglich“', '„Ausgeblendet“', 'Ziehen geht über jede Grenze']) assert.ok(md.includes(w), `Vertrag nennt ${w}`);
  const html = readComponent('reorder-list', 'reorder-list.spec.html');
  for (const t of ['>Fixiert<', '>Beweglich<', '>Ausgeblendet<', 'data-tl-state="last"', 'bi-plus', 'tl-reorder-list__section'])
    assert.ok(html.includes(t), `Specimen zeigt ${t}`);
  // Eine ausgeblendete Zeile hat einen Griff und ist ziehbar (Z3); leere Abschnitte zeigen den Platzhalter (Z4).
  const aus = html.match(/<li class="tl-reorder-list__item" data-tl-state="off[^"]*">[\s\S]*?<\/li>/)?.[0] ?? '';
  assert.ok(aus.includes('tl-reorder-list__handle'), 'die ausgeblendete Zeile hat einen Griff');
  assert.ok(html.includes('tl-reorder-list__empty') && html.includes('drop-target'), 'Specimen zeigt Platzhalter und Zielabschnitt');
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

// Die erste Seite von „Ansicht“ ist ein Menü (K4, #30102): benannt nach ihrem Titel, Titel und Kopf „Dichte“ sind keine Einträge.
test('Ansicht: die erste Seite ist role="menu" mit aria-labelledby auf ihren Titel; Titel und Kopf „Dichte“ tragen role="presentation"', () => {
  const md = readComponent('table', 'table.contract.md');
  const ansicht = md.match(/## Ansicht \(Popover des Zahnrads\)[\s\S]*?\n## /)?.[0] ?? '';
  assert.deepEqual(missingInContract(ansicht, ['role="menu"', 'aria-labelledby', 'role="presentation"']), []);
  const html = readComponent('table', 'table.spec.html');
  const seiten = [...html.matchAll(/<div class="tl-popover tl-picker tl-menu tl-table-view-options[^"]*" role="dialog" aria-label="Ansicht">\s*(<div[^>]*>)\s*(<div[^>]*>)/g)];
  assert.ok(seiten.length > 0, 'Specimen zeigt die Seite „Ansicht“');
  for (const [, seite, titel] of seiten) {
    assert.match(seite, /role="menu"/, seite);
    const id = seite.match(/aria-labelledby="([^"]+)"/)?.[1];
    assert.ok(id, `aria-labelledby an ${seite}`);
    assert.match(titel, new RegExp(`id="${id}"`), `der Titel benennt die Seite: ${titel}`);
    assert.match(titel, /role="presentation"/, titel);
  }
  assert.match(html, /<div[^>]*role="presentation"[^>]*class="tl-menu__header tl-type-label">Dichte<\/div>|<div[^>]*class="tl-menu__header tl-type-label"[^>]*role="presentation"[^>]*>Dichte<\/div>/, 'Kopf „Dichte“');
});

// Die Spaltenwahl ist die zweite Seite von „Ansicht“ und wirkt sofort (E7, #30102): der Eintrag sagt „n von m“, der Server hält die
// Spaltenwahl als Kind, zwei Kommandos öffnen und schließen die Seite; den Dialog „Spalten auswählen und ordnen …“ gibt es nicht mehr.
test('Ansicht: der Eintrag „Spalten · n von m ›“ öffnet die Seite; Vertrag nennt Zustand und Kommandos', () => {
  const md = readComponent('table', 'table.contract.md');
  assert.deepEqual(missingInContract(md, ['columnSelection', 'columnsShown', 'columnsTotal', 'openColumns', 'closeColumns']), []);
  assert.doesNotMatch(md, /openColumnSelect/);
  const html = readComponent('table', 'table.spec.html');
  assert.match(html, /Spalten · \d+ von \d+/, 'das Specimen zeigt den Eintrag mit n von m');
  assert.doesNotMatch(html, /Spalten auswählen und ordnen/, 'der Dialog der Spaltenwahl ist weg');
  const reorder = readComponent('reorder-list', 'reorder-list.contract.md');
  assert.match(reorder, /Klick auf die Zeile/, 'die Zeile der Spaltenwahl klickt');
});

// --- Anker des offenen Popovers -------------------------------------------------------------------------------------------

test('tl-reveal-on-hover: der Anker eines offenen Popovers (aria-expanded="true") bleibt sichtbar', () => {
  assert.match(declarationsOf(layout, '.tl-reveal-on-hover[aria-expanded="true"]') ?? '', /opacity:\s*1/);
  const md = readComponent('layout', 'layout.contract.md');
  assert.deepEqual(missingInContract(md, ['aria-expanded']), []);
});
