// Welle 4a (Spec 2026-10-13): Overlays aus dem Paket. Jede Regel hier steht in der Spec; die Verträge prüft
// contracts-4a.test.mjs.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { readGenerated, declarationsOf, rulesOf } from './lib/component.mjs';

const P = JSON.parse(readFileSync(new URL('../src/primitives.json', import.meta.url)));
const T = JSON.parse(readFileSync(new URL('../src/tokens.json', import.meta.url)));
const decl = (dir, sel) => declarationsOf(readGenerated(dir, `${dir}.css`), sel) ?? '';

test('Maße der Overlays: Drawer-Breiten und Toast-Breite als Tokens über Primitive', () => {
  assert.deepEqual(['size-12', 'size-13', 'size-14', 'size-15'].map((n) => P.measure[n]), ['16rem', '24rem', '36rem', '40rem']);
  assert.equal(T.measure['size-drawer-narrow'], '{size-12}');
  assert.equal(T.measure['size-drawer-medium'], '{size-13}');
  assert.equal(T.measure['size-drawer-wide'], '{size-14}');
  assert.equal(T.measure['size-toast-max'], '{size-15}');
});

test('tl-overlay: Ebenen decken die Basis, lassen den Zeiger durch, zehn Anker, keine Ebene und keine Fläche', () => {
  assert.match(decl('overlay', '.tl-overlay'), /position:\s*relative/);
  assert.match(decl('overlay', '.tl-overlay__layer'), /inset:\s*0/);
  assert.match(decl('overlay', '.tl-overlay__layer'), /pointer-events:\s*none/);
  assert.match(decl('overlay', '.tl-overlay__layer > *'), /pointer-events:\s*auto/);
  for (const a of ['fill', 'top-left', 'top', 'top-right', 'left', 'center', 'right', 'bottom-left', 'bottom', 'bottom-right'])
    assert.notEqual(declarationsOf(readGenerated('overlay', 'overlay.css'), `.tl-overlay__layer--${a}`), null, a);
  // Ausnahme 19.10. (5b): tl-overlay__action trägt die Fläche für den Knopf über dem Dokument.
  const stapel = rulesOf(readGenerated('overlay', 'overlay.css'))
    .filter(([sel]) => sel === '.tl-overlay' || sel.startsWith('.tl-overlay__layer'));
  assert.ok(stapel.length > 10);
  for (const [sel, d] of stapel) assert.doesNotMatch(d, /z-index|background/, sel);
});

test('tl-titlebar: Leistenhöhe, Linie unten, Titel mit Ellipse, Aktionen am Ende', () => {
  assert.match(decl('titlebar', '.tl-titlebar'), /min-height:\s*var\(--tl-size-bar\)/);
  assert.match(decl('titlebar', '.tl-titlebar'), /border-bottom:\s*var\(--tl-border-width-sm\) solid var\(--tl-border-separator\)/);
  assert.match(decl('titlebar', '.tl-titlebar__title'), /flex:\s*1 1 0;/);
  assert.match(decl('titlebar', '.tl-titlebar__title'), /margin:\s*0/);
  assert.match(decl('titlebar', '.tl-titlebar__actions'), /display:\s*flex/);
  assert.match(decl('titlebar', '.tl-titlebar > .tl-button'), /flex:\s*none/);
});

test('tl-dialog: Scrim deckt den Viewport auf der Overlay-Ebene, ohne Blur, mit Rand space-lg', () => {
  const d = decl('dialog', '.tl-dialog');
  assert.match(d, /position:\s*fixed/);
  assert.match(d, /inset:\s*0/);
  assert.match(d, /z-index:\s*var\(--tl-layer-overlay\)/);
  assert.match(d, /background:\s*var\(--tl-surface-scrim\)/);
  assert.doesNotMatch(d, /padding/);
  assert.doesNotMatch(readGenerated('dialog', 'dialog.css').replace(/\/\*[\s\S]*?\*\//g, ''), /backdrop-filter|blur/i);
});

test('tl-dialog: Ein- und Ausblenden über data-tl-state mit Tokens; die Kopie lässt den Zeiger durch', () => {
  assert.match(decl('dialog', '.tl-dialog[data-tl-state~="entering"]'), /animation:\s*tl-dialog-in var\(--tl-duration-base\) var\(--tl-easing-entrance\)/);
  const aus = decl('dialog', '.tl-dialog[data-tl-state~="exiting"]');
  assert.match(aus, /animation:\s*tl-dialog-out var\(--tl-duration-base\) var\(--tl-easing-exit\) forwards/);
  assert.match(aus, /pointer-events:\s*none/);
});

test('tl-dialog__surface: Overlay-Fläche mit Rahmen, Schatten dialog, Ecke md; maximiert ohne Rand und Ecke', () => {
  const s = decl('dialog', '.tl-dialog__surface');
  assert.match(s, /background:\s*var\(--tl-surface-overlay\)/);
  assert.match(s, /border:\s*var\(--tl-border-width-sm\) solid var\(--tl-border-separator\)/);
  assert.match(s, /box-shadow:\s*var\(--tl-shadow-dialog\)/);
  assert.match(s, /border-radius:\s*var\(--tl-radius-md\)/);
  assert.match(s, /max-width:\s*calc\(var\(--tl-size-viewport-width\) - 2 \* var\(--tl-space-lg\)\)/);
  assert.match(s, /max-height:\s*calc\(var\(--tl-size-viewport-height\) - 2 \* var\(--tl-space-lg\)\)/);
  const m = decl('dialog', '.tl-dialog__surface[data-tl-state~="maximized"]');
  assert.match(m, /inset:\s*0/);
  assert.match(m, /max-width:\s*none/);
  assert.match(m, /max-height:\s*none/);
  assert.match(m, /border-radius:\s*0/);
});

test('tl-dialog: Kopf zum Ziehen, Fuß rechtsbündig und leer verborgen, acht Griffe', () => {
  assert.match(decl('dialog', '.tl-dialog__surface > .tl-titlebar'), /cursor:\s*grab/);
  assert.match(decl('dialog', '.tl-dialog__surface[data-tl-state~="maximized"] > .tl-titlebar'), /cursor:\s*default/);
  assert.match(decl('button-bar', '.tl-button-bar'), /justify-content:\s*end/);
  for (const e of ['n', 'ne', 'e', 'se', 's', 'sw', 'w', 'nw'])
    assert.match(decl('dialog', `.tl-dialog__resize[data-tl-edge="${e}"]`), /cursor:\s*[a-z]+-resize/, e);
});

test('tl-drawer: Overlay-Ebene am Viewport, raised im Behälter, schiebt mit Tokens, offen ohne Versatz', () => {
  const d = decl('drawer', '.tl-drawer');
  assert.match(d, /position:\s*fixed/);
  assert.match(d, /z-index:\s*var\(--tl-layer-overlay\)/);
  assert.match(d, /background:\s*var\(--tl-surface-overlay\)/);
  assert.doesNotMatch(d, /box-shadow/);
  assert.match(d, /transition:\s*transform var\(--tl-duration-base\) var\(--tl-easing-exit\)/);
  const open = decl('drawer', '.tl-drawer[data-tl-state~="open"]');
  assert.match(open, /transform:\s*none/);
  assert.match(open, /box-shadow:\s*var\(--tl-shadow-dialog\)/);
  assert.match(decl('drawer', '.tl-drawer--container'), /z-index:\s*var\(--tl-layer-raised\)/);
  assert.match(decl('drawer', '.tl-drawer--narrow'), /width:\s*var\(--tl-size-drawer-narrow\)/);
  assert.match(decl('drawer', '.tl-drawer--bottom.tl-drawer--wide'), /height:\s*var\(--tl-size-drawer-wide\)/);
});

test('tl-drawer: geschlossen aus dem Rand geschoben, im RTL gespiegelt, Spiegelung vor der Offen-Regel', () => {
  assert.match(decl('drawer', '.tl-drawer--bottom'), /transform:\s*translateY\(100%\)/);
  assert.match(decl('drawer', '.tl-drawer--start:dir(rtl)'), /transform:\s*translateX\(100%\)/);
  assert.match(decl('drawer', '.tl-drawer--end:dir(rtl)'), /transform:\s*translateX\(-100%\)/);
  const css = readGenerated('drawer', 'drawer.css');
  assert.match(css, /\.tl-drawer--start \{[^}]*transform:\s*translateX\(-100%\)/);
  assert.match(css, /\.tl-drawer--end \{[^}]*transform:\s*translateX\(100%\)/);
  const open = css.indexOf('.tl-drawer[data-tl-state~="open"] {');
  assert.ok(open > css.indexOf('.tl-drawer--start:dir(rtl) {'));
  assert.ok(open > css.indexOf('.tl-drawer--end:dir(rtl) {'));
});

test('tl-snackbar: schwebt unten mittig auf der Toast-Ebene, Schatten drag, steigt und blendet mit Tokens', () => {
  const s = decl('snackbar', '.tl-snackbar');
  assert.match(s, /position:\s*fixed/);
  assert.match(s, /z-index:\s*var\(--tl-layer-toast\)/);
  assert.match(s, /bottom:\s*var\(--tl-space-md\)/);
  assert.match(s, /margin-inline:\s*auto/);
  assert.match(s, /max-width:\s*min\(var\(--tl-size-toast-max\)/);
  assert.match(s, /box-shadow:\s*var\(--tl-shadow-drag\)/);
  assert.match(s, /animation:\s*tl-snackbar-in var\(--tl-duration-base\) var\(--tl-easing-entrance\)/);
  assert.match(decl('snackbar', '.tl-snackbar[data-tl-state~="exiting"]'), /animation:\s*tl-snackbar-out var\(--tl-duration-base\) var\(--tl-easing-exit\) forwards/);
  assert.match(decl('snackbar', '.tl-snackbar .tl-alert__message'), /white-space:\s*normal/);
});

test('tl-notice-bar: Balken ohne Ecke, Linie nur unten und am Anfang, klickbar zeigt den Zeiger', () => {
  const n = decl('notice-bar', '.tl-notice-bar');
  assert.match(n, /border-radius:\s*0/);
  assert.match(n, /border-block-start-width:\s*0/);
  assert.match(n, /border-inline-end-width:\s*0/);
  assert.match(decl('notice-bar', '.tl-notice-bar[role="button"]'), /cursor:\s*pointer/);
  assert.match(decl('notice-bar', '.tl-notice-bar__countdown'), /white-space:\s*nowrap/);
});

test('keine Farbe, kein Literal: Snackbar und NoticeBar lesen ihre Farben nur aus tl-alert', () => {
  for (const d of ['snackbar', 'notice-bar']) {
    const css = readGenerated(d, `${d}.css`).replace(/\/\*[\s\S]*?\*\//g, '');
    assert.doesNotMatch(css, /--tl-(status|surface|text)-/, d);
  }
});
