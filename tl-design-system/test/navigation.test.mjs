// Welle 4b (Spec 2026-10-16): Navigation aus dem Paket. Jede Regel hier steht in der Spec; die Verträge prüft
// contracts-4b.test.mjs, die Reihenfolge des Nachgebens der AppBar app-bar-shrink.test.mjs.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { readGenerated, declarationsOf, componentCssFiles } from './lib/component.mjs';

const T = JSON.parse(readFileSync(new URL('../src/tokens.json', import.meta.url)));
const decl = (dir, sel) => declarationsOf(readGenerated(dir, `${dir}.css`), sel) ?? '';
const ohneKommentar = (css) => css.replace(/\/\*[\s\S]*?\*\//g, '');

test('Maße der Navigation: size-sidebar fest, size-shell-bar folgt der Dichte (48/40 px)', () => {
  assert.equal(T.measure['size-sidebar'], '{size-12}');
  assert.equal(T.density.normal['size-shell-bar'], '{size-8}');
  assert.equal(T.density.compact['size-shell-bar'], '{height-5}');
});

test('keine Komponentendatei kennt einen Breakpoint: kein @media auf Breite oder Höhe', () => {
  const treffer = [];
  for (const [name, css] of componentCssFiles())
    for (const m of ohneKommentar(css).matchAll(/@media[^{]*\b(min-|max-)?(width|height)\b[^{]*\{/g)) treffer.push(`${name}: ${m[0]}`);
  assert.deepEqual(treffer, []);
});

test('tl-icon-badge: der Behälter ist positioniert, der Zähler sitzt an der Ecke — Lage am Element um das Part', () => {
  assert.match(decl('badge', '.tl-icon-badge'), /position:\s*relative/);
  assert.match(decl('badge', '.tl-icon-badge'), /display:\s*inline-flex/);
  const z = decl('badge', '.tl-icon-badge__count');
  assert.match(z, /position:\s*absolute/);
  assert.match(z, /top:\s*calc\(-1 \* var\(--tl-space-2xs\)\)/);
  assert.match(z, /inset-inline-end:\s*calc\(-1 \* var\(--tl-space-xs\)\)/);
  assert.doesNotMatch(ohneKommentar(readGenerated('badge', 'badge.css')), /\.tl-badge--/);
});

test('tl-app-shell: Spalte, füllt; der Inhalt ist begrenzt, Hinweise stehen bündig', () => {
  const s = decl('app-shell', '.tl-app-shell');
  assert.match(s, /display:\s*flex/);
  assert.match(s, /flex-direction:\s*column/);
  assert.match(s, /height:\s*100%/);
  const c = decl('app-shell', '.tl-app-shell__content');
  assert.match(c, /flex:\s*1 1 auto/);
  assert.match(c, /min-height:\s*0/);
  assert.match(decl('app-shell', '.tl-app-shell .tl-app-shell__notices > *'), /gap:\s*0/);
});

test('tl-app-bar: Höhe size-shell-bar, Fläche surface-layer; --elevated ist eine Linie, kein Schatten', () => {
  const b = decl('app-bar', '.tl-app-bar');
  assert.match(b, /height:\s*var\(--tl-size-shell-bar\)/);
  assert.match(b, /background:\s*var\(--tl-surface-layer\)/);
  assert.match(decl('app-bar', '.tl-app-bar--elevated'), /border-bottom:\s*var\(--tl-border-width-sm\) solid var\(--tl-border-separator\)/);
  assert.doesNotMatch(ohneKommentar(readGenerated('app-bar', 'app-bar.css')), /box-shadow|100000/);
});

test('tl-app-bar: eine überlaufende Toolbar nimmt nur den Rest und hält am Überlaufknopf', () => {
  const a = decl('app-bar', '.tl-app-bar__actions:has(> .tl-toolbar[data-tl-overflow])');
  assert.match(a, /flex:\s*1 1 0/);
  assert.match(a, /min-width:\s*var\(--tl-size-control\)/);
  assert.match(decl('app-bar', '.tl-app-bar__title'), /min-width:\s*0/);
  assert.match(decl('app-bar', '.tl-app-bar__children'), /flex:\s*0 1 auto/);
  assert.match(decl('app-bar', '.tl-app-bar__actions:empty'), /display:\s*none/);
});

test('tl-sidebar: Nav size-sidebar auf surface-layer mit Linie; eingeklappt eine Rechnung aus size-row und space-2xs', () => {
  const n = decl('sidebar', '.tl-sidebar__nav');
  assert.match(n, /width:\s*var\(--tl-size-sidebar\)/);
  assert.match(n, /background:\s*var\(--tl-surface-layer\)/);
  assert.match(n, /border-inline-end:\s*var\(--tl-border-width-sm\) solid var\(--tl-border-separator\)/);
  assert.match(n, /transition:\s*width var\(--tl-duration-base\) var\(--tl-easing-standard\)/);
  assert.match(decl('sidebar', '.tl-sidebar[data-tl-state~="collapsed"] .tl-sidebar__nav'),
    /width:\s*calc\(var\(--tl-size-row\) \+ 2 \* var\(--tl-space-2xs\)\)/);
  assert.match(decl('sidebar', '.tl-sidebar[data-tl-state~="collapsed"] .tl-sidebar__item'), /width:\s*var\(--tl-size-row\)/);
});

test('tl-sidebar: die aktuelle Seite und der Kopf einer zugeklappten Gruppe, die sie enthält, tragen die Fläche der Auswahl — kein Balken', () => {
  const css = readGenerated('sidebar', 'sidebar.css');
  const regel = declarationsOf(css, '.tl-sidebar__item[aria-current="page"]') ?? '';
  assert.match(regel, /background:\s*var\(--tl-surface-interactive-selected\)/);
  assert.notEqual(declarationsOf(css, '.tl-sidebar__item[data-tl-state~="contains-current"]'), null);
  assert.doesNotMatch(ohneKommentar(css), /border-left|border-width-accent|font-weight/);
});

test('tl-sidebar: der Chevron folgt tl-toggle — rechts zu, bei aria-expanded="true" 90° nach unten', () => {
  assert.match(decl('sidebar', '.tl-sidebar__chevron'), /transition:\s*rotate var\(--tl-duration-fast\) var\(--tl-easing-standard\)/);
  assert.match(decl('sidebar', '.tl-sidebar__item[aria-expanded="true"] > .tl-sidebar__chevron'), /rotate:\s*90deg/);
  assert.doesNotMatch(ohneKommentar(readGenerated('sidebar', 'sidebar.css')), /180deg/);
});

test('tl-sidebar: im Drawer füllt die Nav die Schublade, Breite und Linie trägt der Drawer', () => {
  const d = decl('sidebar', '.tl-drawer > .tl-sidebar__nav');
  assert.match(d, /width:\s*auto/);
  assert.match(d, /border-inline-end:\s*0/);
});

test('tl-bottom-bar: Höhe size-shell-bar; die aktuelle Seite trägt die Fläche hinter dem Icon', () => {
  assert.match(decl('bottom-bar', '.tl-bottom-bar'), /height:\s*var\(--tl-size-shell-bar\)/);
  assert.match(decl('bottom-bar', '.tl-bottom-bar__item[aria-current="page"] .tl-bottom-bar__icon'),
    /background:\s*var\(--tl-surface-interactive-selected\)/);
  assert.match(decl('bottom-bar', '.tl-bottom-bar__item:focus-visible'), /outline-offset:\s*calc\(-1 \* var\(--tl-focus-offset\)\)/);
});

test('tl-drawer gleitet auch, wenn er offen eingefügt wird (mobile Sidebar): @starting-style je Seite, auch rtl', () => {
  const css = ohneKommentar(readGenerated('drawer', 'drawer.css'));
  const start = css.match(/@starting-style\s*\{([\s\S]*?)\n\}/)?.[1] ?? '';
  assert.match(start, /\.tl-drawer--start\[data-tl-state~="open"\]\s*\{\s*transform:\s*translateX\(-100%\)/);
  assert.match(start, /\.tl-drawer--start\[data-tl-state~="open"\]:dir\(rtl\)\s*\{\s*transform:\s*translateX\(100%\)/);
  assert.match(start, /\.tl-drawer--end\[data-tl-state~="open"\]\s*\{\s*transform:\s*translateX\(100%\)/);
  assert.match(start, /\.tl-drawer--end\[data-tl-state~="open"\]:dir\(rtl\)\s*\{\s*transform:\s*translateX\(-100%\)/);
  assert.match(start, /\.tl-drawer--bottom\[data-tl-state~="open"\]\s*\{\s*transform:\s*translateY\(100%\)/);
});
