// Welle 5d (Spec 2026-10-20): Fundament für Tooltip und Ziehen — zwei Ebenen über dem Toast und die inversen
// Rollen, auf denen der Tooltip steht.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { resolve } from '../tools/resolve.mjs';
import { ratio } from '../tools/lib/contrast.mjs';
import { readComponent, readGenerated, declarationsOf, rulesOf } from './lib/component.mjs';
import { lint } from '../tools/lint-css.mjs';

const P = JSON.parse(readFileSync(new URL('../src/primitives.json', import.meta.url)));
const T = JSON.parse(readFileSync(new URL('../src/tokens.json', import.meta.url)));
const dist = (f) => readFileSync(new URL(`../dist/${f}`, import.meta.url), 'utf8');

/** Wert einer Custom Property aus dist/, var(--x) bis zum Literal verfolgt. */
function ebene(name) {
  const css = dist('primitives.css') + dist('tokens.css');
  let wert = `var(--tl-${name})`;
  for (let i = 0; i < 5; i++) {
    const ref = /^var\(--([a-z0-9-]+)\)$/.exec(wert);
    if (!ref) break;
    const m = new RegExp(`--${ref[1]}:\\s*([^;]+);`).exec(css);
    assert.ok(m, `--${ref[1]} fehlt in dist/`);
    wert = m[1].trim();
  }
  return Number(wert);
}

test('Ebenen: tooltip über toast, drag über tooltip', () => {
  const [toast, tooltip, drag] = ['layer-toast', 'layer-tooltip', 'layer-drag'].map(ebene);
  assert.deepEqual([toast, tooltip, drag], [1200, 1300, 1400]);
});

test('inverse Rollen halten 4,5:1 in beiden Modi', () => {
  const R = resolve(P, T);
  for (const m of ['light', 'dark']) {
    const fg = R.color[m]['text-inverse'], bg = R.color[m]['surface-inverse'];
    assert.ok(fg && bg, `${m}: text-inverse oder surface-inverse fehlt`);
    const ist = ratio(fg, bg);
    assert.ok(ist >= 4.5, `${m}: text-inverse auf surface-inverse = ${ist.toFixed(2)}`);
  }
});

// --- Task 2: Füllen, Anker, Ziehen, Picker, „Aktion beim Zeigen“ (Spec 5d §3, §4, §6; Plan-Review K1, K2, K6, K15) ---

const ohneKommentar = (css) => css.replace(/\/\*[\s\S]*?\*\//g, '');
const layout = () => readGenerated('layout', 'layout.css');
const drag = () => readGenerated('drag', 'drag.css');
const decl = (css, sel) => declarationsOf(css, sel) ?? '';

test('Füllen: tl-fill im Paket, die Hülle eines füllenden Kindes füllt mit; kein tlFill mehr', () => {
  const d = decl(layout(), '.tl-fill');
  assert.match(d, /flex:\s*1(;|$)/);
  assert.match(d, /min-height:\s*0/);
  assert.match(decl(layout(), '.tl-item:has(> .tl-fill)'), /flex:\s*1(;|$)/);
  for (const [k, f] of [['layout', 'layout.css'], ['form', 'form.css']])
    assert.doesNotMatch(readComponent(k, f), /tlFill/, `${f} nennt tlFill noch`);
});

// K1: Viewport und Mount bleiben Seitenrahmen der Engine (tlReactBase.css), nicht im Paket.
test('base.css: kein Seitenrahmen der Engine (Viewport, Mount)', () => {
  const base = ohneKommentar(dist('base.css'));
  assert.doesNotMatch(base, /\.is-control|\[data-react-module\]/);
  assert.doesNotMatch(base, /html,\s*body\s*\{/);
  assert.doesNotMatch(decl(base, 'body'), /overflow/);
});

// K2: das Aufblitzen ist ein Hof (focus-width in surface-state-hover-strong), kein Ring aus focus-ring.
test('Anker: Aufblitzen als Zustand, Hof aus focus-width, Dauer duration-loop, Ecke radius-sm', () => {
  const base = dist('base.css');
  const d = decl(base, '[data-tl-anchor][data-tl-state~="flash"]');
  assert.match(d, /animation:\s*tl-anchor-flash var\(--tl-duration-loop\) var\(--tl-easing-[a-z]+\)/);
  assert.match(d, /border-radius:\s*var\(--tl-radius-sm\)/);
  const kf = /@keyframes tl-anchor-flash\s*\{([\s\S]*?)\n\}/.exec(ohneKommentar(base));
  assert.ok(kf, '@keyframes tl-anchor-flash fehlt in base.css');
  assert.match(kf[1], /from\s*\{\s*box-shadow:\s*0 0 0 var\(--tl-focus-width\) var\(--tl-surface-state-hover-strong\);?\s*\}/);
  assert.doesNotMatch(kf[1], /--tl-focus-ring/);
});

test('drag.css: Schild und Zieh-Bild auf layer-drag', () => {
  const s = decl(drag(), '.tl-drag-shield');
  for (const r of [/position:\s*fixed/, /inset:\s*0/, /z-index:\s*var\(--tl-layer-drag\)/, /user-select:\s*none/]) assert.match(s, r);
  const b = decl(drag(), '.tl-drag-image');
  for (const r of [/z-index:\s*var\(--tl-layer-drag\)/, /opacity:\s*0\.9/, /box-shadow:\s*var\(--tl-shadow-drag\)/]) assert.match(b, r);
});

// K15: „bereit“ ist ein eigenes Attribut (data-tl-armed); data-tl-state gehört dem Control.
test('drag.css: bereit nach langem Drücken ist data-tl-armed — Schatten, leicht vergrößert, Übergang duration-fast', () => {
  const a = decl(drag(), '[data-tl-armed]');
  assert.match(a, /box-shadow:\s*var\(--tl-shadow-drag\)/);
  assert.match(a, /transform:\s*scale\(1\.02\)/);
  assert.match(a, /transition:[^;]*var\(--tl-duration-fast\)/);
  assert.doesNotMatch(ohneKommentar(drag()), /drag-armed|data-touch-drag/);
});

test('drag.css: ohne Maus kein Markieren und kein Kontextmenü auf ziehbaren Elementen', () => {
  const m = /@media \(pointer: coarse\)\s*\{\s*\[draggable="true"\]\s*\{([^}]*)\}/.exec(ohneKommentar(drag()));
  assert.ok(m, '@media (pointer: coarse) { [draggable="true"] { … } } fehlt');
  assert.match(m[1], /-webkit-touch-callout:\s*none/);
  assert.match(m[1], /user-select:\s*none/);
});

test('drag.css: der Ablehnungshinweis liegt über dem Zieh-Bild', () => {
  assert.match(decl(drag(), '.tl-popover.tl-drop-hint'), /z-index:\s*var\(--tl-layer-drag\)/);
});

// Spec 5d §4: der Rand deckend in border-interactive, nur die Fläche durchscheinend (auf ::before).
test('drag.css: Picker-Markierung auf layer-drag aus dem Theme, Fadenkreuz als Zustand am body', () => {
  const p = decl(drag(), '.tl-pick-highlight');
  for (const r of [/position:\s*fixed/, /pointer-events:\s*none/, /z-index:\s*var\(--tl-layer-drag\)/,
    /border:\s*var\(--tl-border-width-md\) solid var\(--tl-border-interactive\)/]) assert.match(p, r);
  assert.doesNotMatch(p, /opacity|background/, 'der Rand bleibt deckend, die Fläche liegt auf ::before');
  const f = decl(drag(), '.tl-pick-highlight::before');
  for (const r of [/content:\s*""/, /position:\s*absolute/, /inset:\s*0/, /background:\s*var\(--tl-surface-brand-subtle\)/,
    /opacity:\s*0?\.\d+(;|$)/, /pointer-events:\s*none/]) assert.match(f, r);
  assert.match(decl(drag(), 'body[data-tl-state~="picking"]'), /cursor:\s*crosshair/);
  assert.match(decl(drag(), 'body[data-tl-state~="picking"] *'), /cursor:\s*crosshair/);
});

test('die geänderten Dateien tragen kein z-index-Literal, kein #hex, kein rgba(', () => {
  const dateien = { 'base.css': dist('base.css'), 'layout.css': layout(), 'drag.css': drag(),
    'reorder-list.css': readGenerated('reorder-list', 'reorder-list.css'), 'form.css': readGenerated('form', 'form.css') };
  for (const [f, css] of Object.entries(dateien)) {
    const o = ohneKommentar(css);
    assert.doesNotMatch(o, /z-index:\s*\d/, f);
    assert.doesNotMatch(o, /#[0-9a-fA-F]{3,8}\b/, f);
    assert.doesNotMatch(o, /rgba\(/, f);
  }
});

// K6: „Aktion beim Zeigen“ einmal. Übernimmt, was reorder-list.css für Griff und Knöpfe hatte.
test('tl-reveal-on-hover: verborgen über opacity, sichtbar beim Zeigen, bei Tastaturfokus und ohne Hover', () => {
  const l = layout();
  const d = decl(l, '.tl-reveal-on-hover');
  assert.match(d, /opacity:\s*0(;|$)/);
  assert.match(d, /transition:[^;]*opacity var\(--tl-duration-fast\)[^;]*background-color var\(--tl-duration-fast\)/);
  for (const sel of [
    ':hover > .tl-reveal-on-hover',
    ':has(:focus-visible) > .tl-reveal-on-hover',
    '.tl-table__row:hover .tl-reveal-on-hover',
    '.tl-reveal-on-hover:focus-visible',
  ]) assert.match(decl(l, sel), /opacity:\s*1/, sel);
  const m = /@media \(hover: none\)\s*\{\s*\.tl-reveal-on-hover\s*\{([^}]*)\}/.exec(ohneKommentar(l));
  assert.ok(m, '@media (hover: none) { .tl-reveal-on-hover { … } } fehlt');
  assert.match(m[1], /opacity:\s*1/);
  assert.doesNotMatch(ohneKommentar(l), /visibility:\s*hidden|:focus-within/);
});

// --- Task 3: tl-tooltip, tl-rich-text, tl-compact-field, Leiste mit Umbruch (Spec 5d §5, §6, §7; Plan-Review K3, K4, K5) ---

const lies = (k, f) => { try { return readGenerated(k, f); } catch { return ''; } };
const tooltip = () => lies('tooltip', 'tooltip.css');
const richText = () => lies('rich-text', 'rich-text.css');
const compactField = () => lies('compact-field', 'compact-field.css');

test('tooltip: Fläche surface-inverse, Schrift text-inverse, layer-tooltip, Breite size-13, Ecke radius-md', () => {
  const d = decl(tooltip(), '.tl-tooltip');
  for (const r of [/background:\s*var\(--tl-surface-inverse\)/, /(^|;)\s*color:\s*var\(--tl-text-inverse\)/,
    /z-index:\s*var\(--tl-layer-tooltip\)/, /max-width:\s*var\(--tl-size-13\)/, /border-radius:\s*var\(--tl-radius-md\)/,
    /animation:\s*tl-tooltip-in var\(--tl-duration-fast\) var\(--tl-easing-[a-z]+\)/]) assert.match(d, r);
  assert.match(decl(tooltip(), '.tl-tooltip[hidden]'), /display:\s*none/);
});

// K4: drop-shadow() nimmt die zweischichtige Liste nicht; der Schatten steht am Tooltip, der Pfeil hat keinen.
test('tooltip: box-shadow aus shadow-popover am Tooltip, Pfeil gleich gefüllt und ohne Schatten', () => {
  assert.match(decl(tooltip(), '.tl-tooltip'), /box-shadow:\s*var\(--tl-shadow-popover\)/);
  const a = decl(tooltip(), '.tl-tooltip__arrow');
  assert.match(a, /fill:\s*var\(--tl-surface-inverse\)/);
  assert.doesNotMatch(a, /shadow|filter/);
  assert.doesNotMatch(ohneKommentar(tooltip()), /drop-shadow|filter:/);
});

test('tooltip: Abstände space-2xs/space-sm, interaktiv als Zustand mit Zeiger und Textauswahl', () => {
  for (const sel of ['.tl-tooltip__caption', '.tl-tooltip__body'])
    assert.match(decl(tooltip(), sel), /padding:\s*var\(--tl-space-2xs\) var\(--tl-space-sm\)/, sel);
  assert.match(decl(tooltip(), '.tl-tooltip'), /pointer-events:\s*none/);
  const i = decl(tooltip(), '.tl-tooltip[data-tl-state~="interactive"]');
  for (const r of [/pointer-events:\s*auto/, /cursor:\s*text/, /user-select:\s*text/]) assert.match(i, r);
  assert.match(decl(tooltip(), '.tl-tooltip__body *'), /color:\s*inherit/);
});

test('tooltip: keine Schriftgröße, kein Gewicht, keine Zeilenhöhe im CSS — die Typo-Klassen tragen sie', () => {
  const o = ohneKommentar(tooltip());
  assert.ok(o.includes('.tl-tooltip'), 'tooltip.css fehlt');
  assert.doesNotMatch(o, /font-size|font-weight|line-height|(^|[;{\s])font:/);
});

test('rich-text: Innenabstand space-sm/space-md und Mindesthöhe size-10 an der Fläche, Kopfzelle surface-layer-nested', () => {
  const css = richText();
  const f = decl(css, '.tl-rich-text .tl-prose');
  assert.match(f, /padding:\s*var\(--tl-space-sm\) var\(--tl-space-md\)/);
  assert.match(f, /min-height:\s*var\(--tl-size-10\)/);
  assert.match(decl(css, '.tl-rich-text .tl-prose th'), /background:\s*var\(--tl-surface-layer-nested\)/);
  assert.match(decl(css, '.tl-rich-text'), /padding:\s*0/);
  assert.match(decl(css, '.tl-rich-text[hidden]'), /display:\s*none/);
});

// K3: keine Klassen der Bibliothek im Paket; Bildauswahl und Spaltengriff zeichnet das Engine-Sheet des Editors.
test('rich-text: nur .tl-rich-text, seine Elemente und .tl-prose, kein outline: none, Ring der scrollenden Fläche und der Quelle innen', () => {
  const o = ohneKommentar(richText());
  assert.ok(o.includes('.tl-rich-text'), 'rich-text.css fehlt');
  assert.doesNotMatch(o, /ProseMirror|column-resize-handle|selectednode/);
  assert.doesNotMatch(o, /outline:\s*(none|0)/);
  for (const [sel] of rulesOf(o)) assert.match(sel, /^\.tl-rich-text(__[a-z-]+)?\b/, sel);
  assert.match(decl(o, '.tl-rich-text .tl-prose:focus-visible'), /outline-offset:\s*calc\(-1 \* var\(--tl-focus-offset\)\)/);
  // master #29860: die Quelle steht an Stelle der Schreibfläche, ihr Ring liegt ebenso innen; behaltenes Markup
  // steht als Quelltext im Text, im Fließtext ohne Abstand oben und unten.
  assert.match(decl(o, '.tl-rich-text__source:focus-visible'), /outline-offset:\s*calc\(-1 \* var\(--tl-focus-offset\)\)/);
  assert.match(decl(o, '.tl-rich-text__source'), /min-height:\s*var\(--tl-size-10\)/);
  assert.match(decl(o, '.tl-rich-text__markup'), /border:\s*var\(--tl-border-width-sm\) dashed var\(--tl-border-separator\)/);
  assert.match(decl(o, '.tl-rich-text__markup--inline'), /display:\s*inline/);
});

test('compact-field: Öffner teilt die Regel mit dem Knopf im Select (ein Selektor), bearbeitbar ist ein Zustand', () => {
  const sel = readGenerated('select', 'select.css');
  for (const z of ['hover', 'active']) {
    const regel = rulesOf(sel).find(([s]) => s.split(',').map((x) => x.trim())
      .includes(`.tl-select__controls > .tl-button--ghost:not(:disabled):${z}`));
    assert.ok(regel, `Regel der Select-Knöpfe (:${z}) fehlt`);
    assert.ok(regel[0].split(',').map((x) => x.trim())
      .includes(`.tl-compact-field[data-tl-state~="edit"] > .tl-compact-field__opener.tl-button--ghost:not(:disabled):${z}`),
      `der Öffner liest die Regel der Select-Knöpfe nicht (:${z})`);
  }
  const css = compactField();
  const e = decl(css, '.tl-compact-field[data-tl-state~="edit"]');
  for (const r of [/display:\s*flex/, /gap:\s*var\(--tl-space-xs\)/, /padding-inline-end:\s*var\(--tl-space-2xs\)/]) assert.match(e, r);
  assert.match(decl(css, '.tl-compact-field[hidden]'), /display:\s*none/);
  assert.match(decl(css, '.tl-compact-field > .tl-compact-field__opener'), /color:\s*var\(--tl-icon-secondary\)/);
});

// K6: „Aktion beim Zeigen“ ist eine Regel (layout.css); der Öffner trägt tl-reveal-on-hover im Markup.
test('compact-field: liest tl-reveal-on-hover und blendet nichts selbst ein', () => {
  const o = ohneKommentar(compactField());
  assert.ok(o.includes('.tl-compact-field'), 'compact-field.css fehlt');
  assert.doesNotMatch(o, /opacity|visibility|:hover|--edit|--view/);
  const md = (() => { try { return readComponent('compact-field', 'compact-field.contract.md'); } catch { return ''; } })();
  assert.match(md, /`tl-reveal-on-hover`/);
});

test('toolbar--wrap: bricht um, sonst nichts', () => {
  const d = decl(readGenerated('toolbar', 'toolbar.css'), '.tl-toolbar--wrap');
  assert.equal(d.trim().replace(/\s+/g, ' '), 'flex-wrap: wrap;');
});

// Fix-Runde Task 9: eine Gruppe, die eine eingebettete Toolbar hält (die Befehle des Servers in der Leiste des
// Wysiwyg-Editors), schrumpft unter ihren Inhalt — sonst kollabiert die eingebettete Toolbar nie in ihren Überlauf.
test('toolbar: eine Gruppe mit eingebetteter Toolbar darf schrumpfen', () => {
  const d = decl(readGenerated('toolbar', 'toolbar.css'), '.tl-toolbar__group:has(> .tl-toolbar)');
  assert.match(d, /flex-shrink:\s*1(;|$)/);
  assert.match(d, /min-width:\s*0(;|$)/);
  assert.match(readComponent('toolbar', 'toolbar.contract.md'), /`tl-toolbar__group:has\(> \.tl-toolbar\)`/);
});

// K5: die Modifier der neuen Blöcke sind entschieden — tl-toolbar kennt wrap, die neuen Blöcke keinen.
test('lint-css: tl-toolbar--wrap erlaubt; tl-tooltip, tl-rich-text, tl-compact-field, tl-chart ohne Modifier', () => {
  const known = new Set();
  assert.deepEqual(lint('.tl-toolbar--wrap { flex-wrap: wrap; }', { known, file: 'x.css' }), []);
  assert.equal(lint('.tl-toolbar--dense { flex-wrap: wrap; }', { known, file: 'x.css' }).length, 1);
  for (const b of ['tl-tooltip', 'tl-rich-text', 'tl-compact-field', 'tl-chart'])
    assert.equal(lint(`.${b}--x { flex-wrap: wrap; }`, { known, file: 'x.css' }).length, 1, b);
});

test('compact-field: der Ring des Öffners liegt innen (die Tabellenzelle schneidet)', () => {
  assert.match(decl(compactField(), '.tl-compact-field > .tl-compact-field__opener:focus-visible'),
    /outline-offset:\s*calc\(-1 \* var\(--tl-focus-offset\)\)/);
});

// --- Task 10: tl-chart (Spec 5d §8; Plan-Review K12, Ruling R3) ---

const chart = () => lies('chart', 'chart.css');

test('chart: die Wurzel liegt relativ, füllt ihren Behälter und hat eine Mindesthöhe aus den Maßen', () => {
  const d = decl(chart(), '.tl-chart');
  for (const r of [/position:\s*relative/, /width:\s*100%/, /height:\s*100%/, /min-height:\s*var\(--tl-size-12\)/])
    assert.match(d, r);
});

test('chart: leer und Fehler stehen als tl-status mittig in der Fläche; die Wurzel setzt hidden zurück', () => {
  const d = decl(chart(), '.tl-chart:has(> .tl-status)');
  for (const r of [/display:\s*flex/, /align-items:\s*center/, /justify-content:\s*center/]) assert.match(d, r);
  assert.match(decl(chart(), '.tl-chart[hidden]'), /display:\s*none/);
});

// Ruling R3: der Tooltip liest die Fläche von tl-popover (Klasse am Element), das Chart-CSS schreibt keinen Wert davon
// ab — nur Lage und Ebene. Der Selektor trägt beide Klassen: components.css ordnet chart vor popover, ein einzelner
// Klassenselektor verlöre gegen `.tl-popover { position: fixed }`.
test('chart: der Tooltip liegt absolut am Datenpunkt, über der Fläche, fängt keinen Zeiger und kopiert keine Popover-Werte', () => {
  const d = decl(chart(), '.tl-chart__tooltip.tl-popover');
  for (const r of [/position:\s*absolute/, /z-index:\s*var\(--tl-layer-sticky\)/, /pointer-events:\s*none/,
    /min-width:\s*0/]) assert.match(d, r);
  assert.doesNotMatch(d, /background|box-shadow|border|padding|color:/);
  assert.doesNotMatch(ohneKommentar(chart()), /--tl-shadow-popover|--tl-surface-overlay/);
});

test('chart: der Zurücksetzen-Knopf schwebt oben rechts auf einer eigenen Fläche, Ebene raised', () => {
  const d = decl(chart(), '.tl-chart__zoom-reset');
  for (const r of [/position:\s*absolute/, /top:\s*var\(--tl-space-xs\)/, /inset-inline-end:\s*var\(--tl-space-xs\)/,
    /z-index:\s*var\(--tl-layer-raised\)/, /background:\s*var\(--tl-surface-layer\)/,
    /border-radius:\s*var\(--tl-radius-md\)/]) assert.match(d, r);
});

test('chart: nur Selektoren des Blocks, keine Schrift, kein Literal', () => {
  const o = ohneKommentar(chart());
  assert.ok(o.includes('.tl-chart'), 'chart.css fehlt');
  for (const [sel] of rulesOf(o)) assert.match(sel, /^\.tl-chart(__[a-z-]+)?\b/, sel);
  assert.doesNotMatch(o, /font-size|font-weight|line-height|(^|[;{\s])font:/);
});
