// Die Tabelle — 3b §3–§6: Zeile size-row, Kopf size-bar, Lage und Cursor als Attribute.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readComponent, readGenerated, declarationsOf, rulesOf, missingInContract } from './lib/component.mjs';

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
  // Eine Auswahl-Form (UI-Durchgang 1, Prognose 26.10.): Markenton und Kante; Zeigen auf gewählt eine Stufe weiter.
  assert.match(declarationsOf(css, ':where(.tl-table__row[aria-selected="true"])') ?? '', /background:\s*var\(--tl-surface-selected\)/);
  assert.match(declarationsOf(css, ':where(.tl-table__row[aria-selected="true"]:hover)') ?? '', /background:\s*var\(--tl-surface-selected-hover\)/);
  // Die Hover-auf-gewählt-Regel steht nach dem Zeigen und nach der Auswahl (gleiche Spezifität: die Reihenfolge entscheidet).
  assert.ok(css.indexOf(':where(.tl-table__row[aria-selected="true"]:hover)') > css.indexOf(':where(.tl-table__row[aria-selected="true"]) {'));
  assert.ok(css.indexOf(':where(.tl-table__row[aria-selected="true"]) {') > css.indexOf(':where(.tl-table__row:hover)'));
  assert.match(declarationsOf(css, '.tl-table__row[aria-selected="true"] > .tl-table__cell:first-child') ?? '',
    /box-shadow:\s*inset var\(--tl-border-width-accent\) 0 0 0 var\(--tl-border-selected\)/);
  assert.match(declarationsOf(css, ':where(.tl-table__row)') ?? '', /background:\s*var\(--tl-surface-layer\)/);
  assert.match(declarationsOf(css, '.tl-table__header-row') ?? '', /min-height:\s*var\(--tl-size-bar\)/);
  assert.match(declarationsOf(css, '.tl-table__header') ?? '', /border-bottom:\s*var\(--tl-border-width-sm\) solid var\(--tl-border-emphasis\)/);
  const haftend = declarationsOf(css, '.tl-table__cell[data-tl-state~="frozen"]') ?? '';
  assert.match(haftend, /position:\s*sticky/);
  assert.match(haftend, /z-index:\s*var\(--tl-layer-raised\)/);
  assert.match(haftend, /background:\s*inherit/);
  // Fixierte und angeheftete Zellen erben die Fläche der gewählten Zeile: keine eigene Auswahlregel, die Kante der
  // ersten Zelle bleibt auch an einer haftenden sichtbar (box-shadow liegt auf der Zelle, über ihrer Fläche).
  assert.doesNotMatch(haftend, /surface-selected/);
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
  assert.match(declarationsOf(css, '.tl-table__header-row') ?? '', /background:\s*var\(--tl-surface-layer-muted\)/);
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


// --- UI-Durchgang 1, Task 4: Tabelle mit Tiefe (Spec §1) ---------------------------------------------------------

test('Tiefe: die Tabelle steht auf einer abgesetzten Fläche mit Rundung, Rand und leichtem Schatten, die füllende ohne Schatten', () => {
  const d = declarationsOf(css, '.tl-table:not(.tl-fill)') ?? '';
  assert.match(d, /background:\s*var\(--tl-surface-layer\)/);
  assert.match(d, /border-radius:\s*var\(--tl-radius-md\)/);
  assert.match(d, /border:\s*var\(--tl-border-width-sm\) solid var\(--tl-border-separator\)/);
  assert.match(d, /box-shadow:\s*var\(--tl-shadow-1\)/);
  // Die füllende Tabelle schwebt nicht: kein Schatten (ihre Tiefe kommt vom abgesetzten Kopf).
  assert.doesNotMatch(declarationsOf(css, '.tl-table.tl-fill') ?? '', /shadow/);
  assert.doesNotMatch(declarationsOf(css, '.tl-table') ?? '', /shadow/);
});

test('Tiefe: leiser Kopf — surface-layer-muted, Titel text-secondary in Großbuchstaben, auch unter der Spaltenwahl', () => {
  assert.match(declarationsOf(css, '.tl-table__header-row') ?? '', /background:\s*var\(--tl-surface-layer-muted\)/);
  const label = declarationsOf(css, '.tl-table__label') ?? '';
  assert.match(label, /(^|;)\s*color:\s*var\(--tl-text-secondary\)/);
  assert.match(label, /text-transform:\s*uppercase/);
  // Das Zahnrad deckt die Überschriften unter sich: es trägt die Fläche des Kopfes, ebenso der Verlauf davor.
  assert.match(declarationsOf(css, '.tl-table__columns') ?? '', /background:\s*var\(--tl-surface-layer-muted\)/);
  const verlauf = declarationsOf(css, '.tl-table__frame:has(.tl-table__header[data-tl-state~="covered"]) > .tl-table__columns::before') ?? '';
  assert.match(verlauf, /var\(--tl-surface-layer-muted\)/);
  assert.doesNotMatch(verlauf, /var\(--tl-surface-layer\)/);
});

// Alle Deklarationen, die ein Selektor in der Datei bekommt (declarationsOf liefert nur den ersten Block).
const alleDeklarationen = (css, selector) => rulesOf(css)
  .filter(([sels]) => sels.split(/,(?![^(]*\))/).map((x) => x.trim()).includes(selector)).map(([, d]) => d).join(';');
const ohneKommentar = (t) => t.replace(/\/\*[\s\S]*?\*\//g, '');

test('Tiefe: der Trichter ist ein tl-reveal-on-hover (layout.css), nur eine gefilterte Spalte zeigt ihn immer; Sortierpfeil und Gruppenmarke bleiben', () => {
  // Keine eigene Reveal-Regel in table.css: opacity, :focus-within und @media (hover: none) kommen aus layout.css.
  assert.doesNotMatch(alleDeklarationen(css, '.tl-table__filter'), /opacity|transition/);
  assert.doesNotMatch(ohneKommentar(css), /focus-within|hover: none|\.tl-table__header-cell:hover/);
  const gefiltert = alleDeklarationen(css, '.tl-table__header-cell[data-tl-state~="filtered"] .tl-table__filter');
  assert.match(gefiltert, /opacity:\s*1/);
  assert.match(gefiltert, /color:\s*var\(--tl-text-link\)/); // Markenblau seit #29999
  // Die Regel schlägt .tl-reveal-on-hover (0,1,0) und sein @media (hover: none) durch Spezifität (0,3,0).
  const layout = readGenerated('layout', 'layout.css');
  assert.match(alleDeklarationen(layout, '.tl-reveal-on-hover'), /opacity:\s*0/);
  assert.match(alleDeklarationen(layout, ':hover > .tl-reveal-on-hover'), /opacity:\s*1/);
  // Im Specimen trägt jeder Trichter die Klasse und steht direkt in der Kopfzelle (:hover > .tl-reveal-on-hover).
  const html = readComponent('table', 'table.spec.html');
  const trichter = [...html.matchAll(/<button[^>]*class="[^"]*\btl-table__filter\b[^"]*"/g)].map((m) => m[0]);
  assert.ok(trichter.length >= 4);
  for (const t of trichter) assert.match(t, /\btl-reveal-on-hover\b/);
  assert.match(html, /<\/button>\s*<button[^>]*tl-table__filter/, 'der Trichter steht neben dem Sortierknopf in der Kopfzelle');
  // Was in Kraft ist, bleibt zu sehen: Sortierpfeil (im Sortierknopf) und Gruppenmarke haben keine Verbergen-Regel.
  for (const [sels, body] of rulesOf(css))
    if (/\.tl-table__mark/.test(sels)) assert.doesNotMatch(body, /opacity\s*:\s*0|visibility/, sels);
});

test('Tiefe: Spaltenlinien sind ab Werk aus und kommen mit data-tl-column-lines, ohne die Kante fixierter Spalten zu überstimmen', () => {
  assert.doesNotMatch(declarationsOf(css, '.tl-table__cell') ?? '', /border-right/);
  assert.doesNotMatch(declarationsOf(css, '.tl-table__header-cell') ?? '', /border-right/);
  const linie = /border-right:\s*var\(--tl-border-width-sm\) solid var\(--tl-border-separator\)/;
  for (const el of ['cell', 'header-cell'])
    assert.match(declarationsOf(css, `:where(.tl-table[data-tl-column-lines]) .tl-table__${el}`) ?? '', linie, el);
  // Die Linien stehen in :where (Spezifität (0,1,0)): frozen-last (0,2,0) behält border-emphasis, die letzte Zelle bleibt ohne.
  assert.match(declarationsOf(css, '.tl-table__cell:last-child') ?? '', /border-right:\s*0/);
  assert.match(declarationsOf(css, '.tl-table__cell[data-tl-state~="frozen-last"]') ?? '', /border-emphasis/);
});

test('Tiefe: Zebra als Attribut über das Zustandswort even — nie nth-child, und Auswahl und Zeigen gewinnen', () => {
  const zebra = ':where(.tl-table[data-tl-striped] .tl-table__row[data-tl-state~="even"])';
  assert.match(declarationsOf(css, zebra) ?? '', /background:\s*var\(--tl-surface-layer-muted\)/);
  assert.doesNotMatch(ohneKommentar(css), /nth-child|nth-of-type|:nth-/);
  // Gleiche Spezifität (0,0,0) wie Zeigen und Auswahl: die Reihenfolge entscheidet. Zebra nach der Grundfläche, vor beiden.
  const i = (t) => css.indexOf(t);
  assert.ok(i(':where(.tl-table__row) {') < i(zebra));
  assert.ok(i(zebra) < i(':where(.tl-table__row:hover)'));
  assert.ok(i(zebra) < i(':where(.tl-table__row[aria-selected="true"]) {'));
  assert.ok(i(zebra) < i(':where(.tl-table__row[aria-selected="true"]:hover)'));
  // Alle drei in :where: eine Klasse stark (Ablegen, drag.css) übermalt sie weiterhin ohne Spezifitätskampf.
  assert.doesNotMatch(css, /(^|\n)\.tl-table\[data-tl-striped\]/);
});

test('Tiefe: Zahlen rechtsbündig mit tabular-nums, Kopf mit ausgerichtet', () => {
  for (const el of ['cell', 'header-cell']) {
    const d = declarationsOf(css, `.tl-table__${el}[data-tl-align="end"]`) ?? '';
    assert.match(d, /text-align:\s*end/, el);
    assert.match(d, /justify-content:\s*flex-end/, el);
    assert.match(d, /font-variant-numeric:\s*tabular-nums/, el);
  }
});

test('Tiefe: die Gruppenzeile ist eine eigene Zeilenform, ihre Anzahl eine Kapsel', () => {
  assert.match(declarationsOf(css, ':where(.tl-table__row[data-tl-state~="group"])') ?? '', /background:\s*var\(--tl-surface-layer-muted\)/);
  const i = (t) => css.indexOf(t);
  assert.ok(i(':where(.tl-table__row[data-tl-state~="group"])') < i(':where(.tl-table__row:hover)'), 'Zeigen und Auswahl gewinnen');
  const k = declarationsOf(css, '.tl-table__count') ?? '';
  assert.match(k, /border-radius:\s*var\(--tl-radius-full\)/);
  assert.match(k, /background:\s*var\(--tl-surface-interactive-strong\)/);
  // text-secondary hielte auf der Kapsel dunkel nur 4,27: text-primary, Paar steht in pairs.mjs.
  assert.match(k, /(^|;)\s*color:\s*var\(--tl-text-primary\)/);
  assert.doesNotMatch(k, /text-secondary/);
});

test('Tiefe: „+ n weitere“ ist ein Element der Filterleiste, das nur sein Layout braucht (Chip liefert die Form)', () => {
  assert.match(declarationsOf(css, '.tl-table__more') ?? '', /flex:\s*none/);
});

test('Tiefe: Vertrag und Specimen nennen die neuen Attribute und Klassen', () => {
  const md = readComponent('table', 'table.contract.md');
  assert.deepEqual(missingInContract(md, ['data-tl-striped', 'data-tl-column-lines', 'data-tl-align="end"', 'data-tl-density',
    'data-tl-state~="group"', 'data-tl-state~="even"', 'tl-table__more', 'tl-type-overline', '--tl-surface-layer-muted', 'tl-reveal-on-hover']), []);
  const html = readComponent('table', 'table.spec.html');
  for (const t of ['data-tl-striped', 'data-tl-column-lines', 'data-tl-align="end"', 'tl-table__more', 'tl-type-overline',
    'data-tl-state="filtered"', 'aria-sort="ascending"'])
    assert.ok(html.includes(t), `Specimen zeigt ${t}`);
  for (const wort of ['group', 'even']) assert.match(html, new RegExp(`data-tl-state="[^"]*\\b${wort}\\b`), `Specimen zeigt das Zustandswort ${wort}`);
});

test('der Knopf im Streifen der Spaltenwahl trägt den Ring innen: tl-table schneidet, rechts bündig wäre er weg (Hendrik 27.10.)', () => {
  assert.match(declarationsOf(css, '.tl-table__columns > .tl-button:focus-visible') ?? '', /outline-offset:\s*calc\(-1 \* var\(--tl-focus-offset\)\)/);
});
