// Ziehen und Ablegen: ein Rechner, eine Optik — 3b §9 (W11). Die Optik steht einmal, in drag.css, und
// liest nur Zustände; kein Block zeichnet ein Drop-Ziel oder die gezogene Quelle selbst.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readComponent, readGenerated, declarationsOf, componentCssFiles } from './lib/component.mjs';

const css = readGenerated('drag', 'drag.css');

// Alle Deklarationen aller Regeln, die den Selektor nennen (die Fläche teilen sich vier Zustände;
// declarationsOf gibt nur die erste Regel).
const alle = (sel) => [...css.replace(/\/\*[\s\S]*?\*\//g, '').matchAll(/([^{}]+)\{([^}]*)\}/g)]
  .filter((m) => m[1].split(',').map((s) => s.trim()).includes(sel)).map((m) => m[2]).join(';');

// Plan-Review M4: ein inset-Schatten am Ziel läge unter den Hintergründen seiner Kinder (Filterleiste,
// Kopf und Zeilen an der Tabellenwurzel; haftende Zellen an einer Zeile). Marke und Rahmen liegen darum
// auf einer Fläche ::before über dem Ziel; ::after ist der Cursor-Ring (base.css).
test('drag.css: Einfügemarke, Ablegen darauf, Ablehnung auf einer Fläche über dem Ziel, gezogene Quelle blass', () => {
  for (const z of ['drop-before', 'drop-after', 'drop-onto', 'drop-refused']) {
    const flaeche = alle(`[data-tl-state~="${z}"]::before`);
    for (const d of [/content:\s*""/, /position:\s*absolute/, /inset:\s*0/, /z-index:\s*var\(--tl-layer-sticky\)/, /pointer-events:\s*none/])
      assert.match(flaeche, d, z);
    // Die Marke folgt der Form des Ziels (runde Pille, Option mit radius-sm); eckige Ziele bleiben eckig.
    assert.match(flaeche, /border-radius:\s*inherit/, z);
    assert.match(declarationsOf(css, `:where([data-tl-state~="${z}"])`) ?? '', /position:\s*relative/, z);
    assert.doesNotMatch(alle(`[data-tl-state~="${z}"]`), /box-shadow/, `${z}: der Rahmen liegt auf ::before, nicht am Ziel`);
  }
  assert.match(alle('[data-tl-state~="drop-before"]::before'),
    /box-shadow:\s*inset 0 var\(--tl-border-width-accent\) 0 0 var\(--tl-border-interactive\)/);
  assert.match(alle('[data-tl-state~="drop-after"]::before'),
    /box-shadow:\s*inset 0 calc\(-1 \* var\(--tl-border-width-accent\)\) 0 0 var\(--tl-border-interactive\)/);
  assert.match(alle('[data-tl-axis="horizontal"] > [data-tl-state~="drop-before"]::before'),
    /box-shadow:\s*inset var\(--tl-border-width-accent\) 0 0 0 var\(--tl-border-interactive\)/);
  assert.match(alle('[data-tl-axis="horizontal"] > [data-tl-state~="drop-after"]::before'),
    /box-shadow:\s*inset calc\(-1 \* var\(--tl-border-width-accent\)\) 0 0 0 var\(--tl-border-interactive\)/);
  assert.match(alle('[data-tl-state~="drop-onto"]'), /background:\s*var\(--tl-surface-interactive-strong\)/);
  assert.match(alle('[data-tl-state~="drop-onto"]::before'),
    /box-shadow:\s*inset 0 0 0 var\(--tl-border-width-md\) var\(--tl-border-interactive\)/);
  assert.match(alle('[data-tl-state~="drop-refused"]::before'),
    /box-shadow:\s*inset 0 0 0 var\(--tl-border-width-md\) var\(--tl-status-error-border\)/);
  assert.match(alle('[data-tl-state~="dragging"]'), /opacity:\s*0\.4/);
});

test('kein anderer Block zeichnet die Ziehen-Zustände', () => {
  const offen = [];
  for (const [name, src] of componentCssFiles()) {
    if (name === 'drag/drag.css') continue;
    const ohne = src.replace(/\/\*[\s\S]*?\*\//g, '');
    if (/data-tl-state~="(drop-[a-z]+|dragging)"/.test(ohne)) offen.push(name);
  }
  assert.deepEqual(offen, []);
});

test('tl-reorder-list: Liste ohne Aufzählung, Einträge size-row, Griff innen fokussiert, höchstens size-popover-max', () => {
  const liste = readGenerated('reorder-list', 'reorder-list.css');
  const d = declarationsOf(liste, '.tl-reorder-list') ?? '';
  assert.match(d, /list-style:\s*none/);
  assert.match(d, /max-height:\s*var\(--tl-size-popover-max\)/);
  assert.match(declarationsOf(liste, '.tl-reorder-list__item') ?? '', /min-height:\s*var\(--tl-size-row\)/);
  assert.match(declarationsOf(liste, '.tl-reorder-list__handle:focus-visible') ?? '', /outline-offset:\s*calc\(-1 \* var\(--tl-focus-offset\)\)/);
  assert.doesNotMatch(liste, /--(dragover|drag-over|group-by|fixed|active)\b/);
});

// Sichtprüfung 12.10.: mit Suche davor füllen Suche und Liste ihren Behälter, nur die Liste scrollt. Die
// feste Höhe size-popover-max (Fix E5) lief im kleineren Fensterkörper über — zwei Scrollbereiche, die
// Suche scrollte weg. Die Höhe hält jetzt der Behälter (das Fenster der Spaltenwahl, 70 %).
test('tl-reorder-list: mit Suche davor füllt sie den Behälter, keine feste Höhe, Suche mit Innenabstand', () => {
  const liste = readGenerated('reorder-list', 'reorder-list.css');
  const d = declarationsOf(liste, '.tl-reorder-list__search ~ .tl-reorder-list') ?? '';
  assert.match(d, /flex:\s*1 1 auto/);
  assert.match(d, /min-block-size:\s*0/);
  assert.doesNotMatch(d, /block-size:\s*var\(--tl-size-popover-max\)/, 'keine feste Höhe mehr');
  const behaelter = declarationsOf(liste, ':where(:has(> .tl-reorder-list__search))') ?? '';
  assert.match(behaelter, /flex-direction:\s*column/);
  assert.match(behaelter, /block-size:\s*100%/);
  assert.match(behaelter, /padding:\s*var\(--tl-space-md\) var\(--tl-space-md\) 0/);
});

// Ablegen darauf färbt das Ziel surface-interactive-strong ([data-tl-state~="drop-onto"], eine
// Attribut-Spezifität). Eine Fläche des Ziels selbst — Grund, gezeigt, ausgewählt — stünde mit Klasse plus
// Pseudo-Klasse oder Attribut darüber und verdeckte das Ziel unter dem Zeiger (drag.contract.md „Bild“).
// Jede Regel eines Blocks, dessen Ziele drag.css bedient, die dem Ziel selbst eine Fläche gibt, steht darum
// ganz in :where.
test('jede Fläche eines Ablage-Ziels steht in :where, Ablegen darauf übermalt sie', () => {
  const ziele = [
    ['table', 'table.css', 'tl-table__row'],
    ['tree', 'tree.css', 'tl-tree__node'],
    ['reorder-list', 'reorder-list.css', 'tl-reorder-list__item'],
  ];
  const offen = [];
  for (const [komp, datei, ziel] of ziele) {
    const ohne = readGenerated(komp, datei).replace(/\/\*[\s\S]*?\*\//g, '');
    const subjekt = new RegExp(`\\.${ziel}(?![\\w-])[^\\s>+~]*$`);
    let gesehen = 0;
    for (const m of ohne.matchAll(/([^{}]+)\{([^}]*)\}/g)) {
      if (!/(^|;)\s*background(-color)?\s*:/.test(m[2])) continue;
      for (const sel of m[1].split(',').map((x) => x.trim())) {
        // Das Ziel selbst ist das Subjekt: kein Kombinator hinter seiner Klasse.
        if (!subjekt.test(sel.replace(/^:where\((.*)\)$/, '$1'))) continue;
        gesehen++;
        if (!/^:where\(.*\)$/.test(sel)) offen.push(`${datei}: ${sel}`);
      }
    }
    // Der Test greift nur, wenn er die Flächen des Ziels auch findet.
    assert.ok(gesehen > 0, `${datei}: keine Fläche an .${ziel} gefunden`);
  }
  assert.deepEqual(offen, []);
});

// Spaltenwahl als Ebenen-Panel (#29825, Spec 2026-10-12 §3): Griff und Knöpfe erscheinen beim Zeigen oder Fokus,
// über opacity — ein Knopf bleibt mit Tab erreichbar; gesetzte Zustände halten ihr Icon sichtbar. Seit 5d (K6) liest
// die Liste dafür die eine Regel tl-reveal-on-hover (layout.css); reorder-list.css hat keine eigene Fassung mehr.
test('tl-reorder-list: Griff und Knöpfe erscheinen über tl-reveal-on-hover, nie über visibility; gesetzte Zustände bleiben sichtbar', () => {
  const liste = readGenerated('reorder-list', 'reorder-list.css');
  const ohne = liste.replace(/\/\*[\s\S]*?\*\//g, '');
  // Keine zweite Fassung: kein Verbergen, kein Einblenden beim Zeigen, kein eigener Touch-Fall.
  assert.doesNotMatch(declarationsOf(liste, '.tl-reorder-list__handle') ?? '', /opacity|transition/);
  assert.equal(declarationsOf(liste, '.tl-reorder-list__actions > .tl-button'), null);
  assert.doesNotMatch(ohne, /opacity:\s*0(;|\s|$)/, 'reorder-list.css verbirgt nichts mehr selbst');
  assert.doesNotMatch(ohne, /:hover\s*>|@media \(hover: none\)/, 'reorder-list.css blendet nichts mehr selbst ein');
  assert.doesNotMatch(ohne, /:focus-within/, 'Zeigen bei Fokus nur über :focus-visible');
  assert.doesNotMatch(ohne, /visibility:\s*hidden/);
  for (const sel of [
    '.tl-reorder-list__item[data-tl-state~="off"] .tl-reorder-list__eye',
    '.tl-reorder-list__item[data-tl-state~="locked"] .tl-reorder-list__lock',
  ]) assert.match(declarationsOf(liste, sel) ?? '', /opacity:\s*1/, sel);

  // Die Regel, die die Liste liest: dieselbe Mechanik wie vorher, einmal (layout.css).
  const reveal = readGenerated('layout', 'layout.css');
  assert.match(declarationsOf(reveal, '.tl-reveal-on-hover') ?? '', /opacity:\s*0/);
  // Die Übergangsliste ersetzt die von .tl-button: opacity kommt dazu, die weichen Hover-Übergänge bleiben.
  assert.match(declarationsOf(reveal, '.tl-reveal-on-hover') ?? '', /transition:[^;]*opacity[^;]*background-color/);
  // Die Zeile der Liste zeigt Griff und Knöpfe wie bisher, auch wenn der Zeiger nicht auf der Knopfgruppe steht.
  for (const sel of [
    '.tl-reorder-list__item:hover .tl-reveal-on-hover',
    '.tl-reorder-list__item:has(:focus-visible) .tl-reveal-on-hover',
  ]) assert.match(declarationsOf(reveal, sel) ?? '', /opacity:\s*1/, sel);
  assert.match(reveal, /@media \(hover: none\)/, 'ohne Zeigen (Touch) bleibt alles sichtbar');

  // Das Specimen der Liste trägt die Klasse an Griff, Schloss und Auge.
  const spec = readComponent('reorder-list', 'reorder-list.spec.html');
  for (const teil of ['handle', 'lock', 'eye']) {
    const knoepfe = [...spec.matchAll(new RegExp(`class="[^"]*tl-reorder-list__${teil}[^"]*"`, 'g'))].map((m) => m[0]);
    assert.ok(knoepfe.length > 0, teil);
    for (const k of knoepfe) assert.match(k, /\btl-reveal-on-hover\b/, `${teil}: ${k}`);
  }
});

test('tl-reorder-list: ausgeblendet gedämpft (text-secondary), Block mit Linie, Überschrift, Knöpfe rechts; kein Kästchen mehr', () => {
  const liste = readGenerated('reorder-list', 'reorder-list.css');
  // Der ausgeblendete Eintrag bleibt bedienbar: text-disabled (WCAG-Ausnahme nur für Inaktives) wäre zu blass.
  const aus = declarationsOf(liste, '.tl-reorder-list__item[data-tl-state~="off"] > .tl-reorder-list__label') ?? '';
  assert.match(aus, /color:\s*var\(--tl-text-secondary\)/);
  assert.doesNotMatch(aus, /text-disabled/);
  assert.match(declarationsOf(liste, '.tl-reorder-list__item[data-tl-state~="section-end"]') ?? '',
    /border-bottom:\s*var\(--tl-border-width-sm\) solid var\(--tl-border-separator\)/);
  assert.match(declarationsOf(liste, '.tl-reorder-list__section') ?? '', /color:\s*var\(--tl-text-secondary\)/);
  assert.match(declarationsOf(liste, '.tl-reorder-list__actions') ?? '', /gap:\s*var\(--tl-space-2xs\)/);
  assert.match(declarationsOf(liste, '.tl-reorder-list__actions') ?? '', /margin-inline-start:\s*auto/);
  assert.match(declarationsOf(liste, '.tl-reorder-list__actions > .tl-button:focus-visible') ?? '',
    /outline-offset:\s*calc\(-1 \* var\(--tl-focus-offset\)\)/, 'der Ring der Knöpfe liegt innen wie am Griff');
  assert.match(declarationsOf(liste, '.tl-reorder-list__label') ?? '', /flex:\s*1 1 auto/);
  assert.equal(declarationsOf(liste, '.tl-reorder-list__item > .tl-choice'), null, 'das Kästchen fällt');
});
