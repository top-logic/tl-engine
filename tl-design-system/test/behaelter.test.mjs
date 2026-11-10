// Welle 4c (Spec 2026-10-16): Behälter aus dem Paket. Jede Regel hier steht in der Spec; die Verträge prüft
// contracts-4c.test.mjs, Kopfhöhe und Nachgeben titlebar-height.test.mjs im Browser.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { readGenerated, declarationsOf } from './lib/component.mjs';

const T = JSON.parse(readFileSync(new URL('../src/tokens.json', import.meta.url)));
const decl = (dir, sel, file = `${dir}.css`) => declarationsOf(readGenerated(dir, file), sel) ?? '';
const ohneKommentar = (css) => css.replace(/\/\*[\s\S]*?\*\//g, '');

test('size-wizard-progress ist eine eigene Rolle: 24 rem', () => {
  assert.equal(T.measure['size-wizard-progress'], '{size-13}');
});

test('tl-titlebar: eine Toolbar im Kopf bringt keine eigene Leistenhöhe mit', () => {
  const t = decl('titlebar', '.tl-titlebar__actions > .tl-toolbar');
  assert.match(t, /min-height:\s*0/);
  assert.match(t, /padding-block:\s*0/);
  // Die Linie unten zählt zur Höhe (border-box): unten space-2xs abzüglich der Linie, sonst misst ein Kopf mit
  // Toolbar-Knopf size-bar + 1 px (Plan-Review, Befund 2).
  assert.match(decl('titlebar', '.tl-titlebar'),
    /padding-bottom:\s*calc\(var\(--tl-space-2xs\) - var\(--tl-border-width-sm\)\)/);
});

test('tl-titlebar: Inhalt neben dem Titel, Kopfknöpfe in einer Hülle, ohne Titel alles am Ende', () => {
  assert.match(decl('titlebar', '.tl-titlebar__content'), /flex:\s*1 1 auto/);
  assert.match(decl('titlebar', '.tl-titlebar__title:has(+ .tl-titlebar__content)'), /flex:\s*0 1 auto/);
  const b = decl('titlebar', '.tl-titlebar__buttons');
  assert.match(b, /flex:\s*none/);
  assert.match(b, /gap:\s*var\(--tl-space-xs\)/);
  // Die Selektorliste in :not(…) enthält ein Komma; declarationsOf trennt daran, darum per Regex über die Datei.
  assert.match(ohneKommentar(readGenerated('titlebar', 'titlebar.css')),
    /\.tl-titlebar > :first-child:not\(\.tl-titlebar__title, \.tl-titlebar__content\)\s*\{[^}]*margin-inline-start:\s*auto/);
});

test('tl-titlebar: eine überlaufende Toolbar gibt vor dem Titel nach, ohne 100000', () => {
  const a = decl('titlebar', '.tl-titlebar__actions:has(> .tl-toolbar[data-tl-overflow])');
  assert.match(a, /flex:\s*1 1 0/);
  assert.match(a, /min-width:\s*var\(--tl-size-control\)/);
  assert.match(decl('titlebar', '.tl-titlebar:has(> .tl-titlebar__actions > .tl-toolbar[data-tl-overflow]) > .tl-titlebar__title'),
    /flex:\s*0 1 auto/);
  assert.doesNotMatch(ohneKommentar(readGenerated('titlebar', 'titlebar.css')), /100000/);
});

test('tl-titlebar: die Aktionen stehen am Ende, wie in der AppBar (#29918)', () => {
  assert.match(decl('titlebar', '.tl-titlebar__actions'), /justify-content:\s*end/);
});

test('tl-button-bar: rechtsbündig, Linie oben, verschwindet ohne Knopf — Fuß des Dialogs und des Panels', () => {
  const b = decl('button-bar', '.tl-button-bar');
  assert.match(b, /justify-content:\s*end/);
  assert.match(b, /gap:\s*var\(--tl-space-xs\)/);
  assert.match(b, /padding:\s*var\(--tl-space-sm\) var\(--tl-space-md\)/);
  assert.match(b, /border-top:\s*var\(--tl-border-width-sm\) solid var\(--tl-border-separator\)/);
  assert.match(decl('button-bar', '.tl-button-bar:not(:has(button))'), /display:\s*none/);
  assert.doesNotMatch(ohneKommentar(readGenerated('dialog', 'dialog.css')), /tl-dialog__footer/);
});

test('tl-panel: Spalte; maximiert über dem Behälter auf layer-raised, Grund surface-base; minimiert ist der Kopf ein Ziel', () => {
  assert.match(decl('panel', '.tl-panel'), /flex-direction:\s*column/);
  const m = decl('panel', '.tl-panel[data-tl-state~="maximized"]');
  assert.match(m, /position:\s*absolute/);
  assert.match(m, /inset:\s*0/);
  assert.match(m, /z-index:\s*var\(--tl-layer-raised\)/);
  assert.match(m, /background:\s*var\(--tl-surface-base\)/);
  assert.match(decl('panel', '.tl-panel[data-tl-state~="minimized"] > .tl-titlebar'), /cursor:\s*pointer/);
  const b = decl('panel', '.tl-panel__body');
  assert.match(b, /overflow:\s*auto/);
  assert.match(b, /flex:\s*1/);
  assert.match(decl('panel', '.tl-panel__body > *'), /flex-shrink:\s*0/);
  assert.match(decl('panel', '.tl-panel--full-line'), /grid-column:\s*1 \/ -1/);
});

test('tl-panel--hover-actions: Aktionen und Knöpfe beim Zeigen und bei Tastaturfokus, auf Touch immer', () => {
  const css = ohneKommentar(readGenerated('panel', 'panel.css'));
  assert.match(css, /\.tl-panel--hover-actions > \.tl-titlebar > :is\(\.tl-titlebar__actions, \.tl-titlebar__buttons\)\s*\{[^}]*opacity:\s*0/);
  assert.match(css, /transition:\s*opacity var\(--tl-duration-base\) var\(--tl-easing-standard\)/);
  assert.match(css, /:has\(:focus-visible\)/);
  assert.match(css, /@media \(hover: none\)/);
  assert.doesNotMatch(css, /focus-within/);
});

test('tl-card: Linie, Radius, beschnitten; die Fläche folgt der Verschachtelung (Weg B), kein Schatten', () => {
  const c = decl('card', '.tl-card');
  assert.match(c, /background:\s*var\(--tl-surface-layer\)/);
  assert.match(c, /border:\s*var\(--tl-border-width-sm\) solid var\(--tl-border-separator\)/);
  assert.match(c, /border-radius:\s*var\(--tl-radius-md\)/);
  assert.match(c, /overflow:\s*clip/);
  assert.match(decl('card', '.tl-card--nested'), /background:\s*var\(--tl-surface-layer-nested\)/);
  assert.match(decl('card', '.tl-card--overlay'), /background:\s*var\(--tl-surface-overlay\)/);
  assert.match(decl('card', '.tl-card__body--pad-default'), /padding:\s*var\(--tl-space-md\)/);
  assert.match(decl('card', '.tl-card__body--pad-compact'), /padding:\s*var\(--tl-space-xs\)/);
  assert.doesNotMatch(ohneKommentar(readGenerated('card', 'card.css')), /box-shadow|--elevated/);
});

test('tl-accordion: die Fläche ist tl-card; nur Linien, Kopf auf size-bar, Körper space-md, Ring innen', () => {
  const css = ohneKommentar(readGenerated('accordion', 'accordion.css'));
  assert.doesNotMatch(css, /\.tl-accordion\s*\{/); // keine eigene Fläche
  assert.doesNotMatch(css, /--expanded|border-top-left-radius/);
  assert.match(decl('accordion', '.tl-accordion__header'), /min-height:\s*var\(--tl-size-bar\)/);
  assert.match(decl('accordion', '.tl-accordion__panel'), /padding:\s*var\(--tl-space-md\)/);
  assert.match(decl('accordion', '.tl-accordion__trigger:focus-visible'), /outline-offset:\s*calc\(-1 \* var\(--tl-focus-offset\)\)/);
  assert.match(decl('accordion', '.tl-accordion__trigger[aria-expanded="true"] > .tl-accordion__chevron'), /rotate:\s*90deg/);
});

test('eine Gap-Skala: Stack, Grid und Spalten lesen space-sm, -md, -lg', () => {
  for (const [stufe, token] of [['compact', 'sm'], ['default', 'md'], ['loose', 'lg']]) {
    for (const block of ['tl-stack', 'tl-grid'])
      assert.match(decl('layout', `.${block}--gap-${stufe}`), new RegExp(`gap:\\s*var\\(--tl-space-${token}\\)`), `${block} ${stufe}`);
    assert.match(decl('columns', `.tl-columns--gap-${stufe}`), new RegExp(`gap:\\s*var\\(--tl-space-${token}\\)`));
  }
});

test('tl-inset: auf der Seite space-lg, im Panel space-md, in der Karte space-xs space-md — keine eigene Variable', () => {
  assert.match(decl('layout', '.tl-inset'), /padding:\s*var\(--tl-space-lg\)/);
  assert.match(decl('layout', '.tl-inset--panel'), /padding:\s*var\(--tl-space-md\)/);
  assert.match(decl('layout', '.tl-inset--card'), /padding:\s*var\(--tl-space-xs\) var\(--tl-space-md\)/);
  assert.doesNotMatch(ohneKommentar(readGenerated('layout', 'layout.css')), /page-inset|--tl-grid-gap|tlFill/);
});

test('tl-bounded, tl-item, tl-deck: Breite mittig begrenzt, Hülle durchlässig, ein Deck ohne eigene Box', () => {
  assert.match(decl('layout', '.tl-bounded'), /margin-inline:\s*auto/);
  assert.match(decl('layout', '.tl-item'), /min-height:\s*0/);
  assert.match(decl('layout', '.tl-deck'), /display:\s*contents/);
  assert.match(decl('layout', '.tl-deck[hidden]'), /display:\s*none/);
});

// #29990: eine begrenzte Region gibt ihrem einen Inhalt ihre Höhe — ein füllender genau, einer eigener Größe gestreckt.
test('tl-region: eine Zelle aus minmax(0, 1fr); Reiterfeld und Dialogkörper ohne Region bleiben Spalten', () => {
  const region = decl('layout', '.tl-region');
  assert.match(region, /display:\s*grid/);
  assert.match(region, /grid-template-rows:\s*minmax\(0, 1fr\)/);
  assert.match(region, /grid-template-columns:\s*minmax\(0, 1fr\)/);
  assert.match(decl('layout', '.tl-region[hidden]'), /display:\s*none/);
  assert.match(decl('tabs', '.tl-tabs__panel:not(.tl-region)'), /flex-direction:\s*column/);
  assert.match(decl('dialog', '.tl-dialog__body:not(.tl-region)'), /flex-direction:\s*column/);
  for (const [dir, sel] of [['tabs', '.tl-tabs__panel'], ['dialog', '.tl-dialog__body'], ['drawer', '.tl-drawer__body'],
    ['app-shell', '.tl-app-shell__content'], ['sidebar', '.tl-sidebar__content']]) {
    assert.doesNotMatch(decl(dir, sel), /display:/, `${sel} überlässt das Layout der Region`);
  }
});

// Q24 (#29755): eine Option schwebt nicht — beim Zeigen ein Hof; ausgewählt ein Ring über dem Eintrag, Zustand aus
// aria-selected; Fokus aus base.css. Für die Hülle tl-item und die Spalte von tl-columns, die useItems als Hülle nimmt.
test('auswählbare Einträge (Q24): Hof beim Zeigen, Markenton und Ring bei aria-selected, kein Schatten, keine Zustandsklasse', () => {
  for (const host of ['.tl-item', '.tl-columns__column']) {
    assert.match(decl('layout', `${host}[role="option"]`), /position:\s*relative/, host);
    assert.match(decl('layout', `${host}[role="option"]`), /cursor:\s*pointer/, host);
    assert.match(decl('layout', `${host}[role="option"]`), /transition:\s*box-shadow var\(--tl-duration-fast\) var\(--tl-easing-standard\)/, host);
    assert.match(decl('layout', `${host}[role="option"]:hover`), /box-shadow:\s*0 0 0 var\(--tl-space-2xs\) var\(--tl-surface-state-hover-strong\)/, host);
    const ring = decl('layout', `${host}[aria-selected="true"]::after`);
    assert.match(ring, /border:\s*var\(--tl-border-width-md\) solid var\(--tl-border-selected\)/, host);
    assert.match(decl('layout', `${host}[aria-selected="true"]`), /background:\s*var\(--tl-surface-selected\)/, host);
    // Eine Karte in der Hülle deckt sie ganz: sie erbt die Fläche der gewählten Hülle.
    assert.match(decl('layout', `${host}[aria-selected="true"] > .tl-card`), /background:\s*inherit/, host);
    assert.match(ring, /inset:\s*0/, host);
    assert.match(ring, /pointer-events:\s*none/, host);
  }
  const css = ohneKommentar(readGenerated('layout', 'layout.css'));
  assert.doesNotMatch(css, /--selectable|--selected|--tl-shadow|outline|surface-brand-subtle/);
});

// Nachtrag 4c (S2): ausgewählt heißt Ring und Haken; die Marke ist dekoratives Element der Hülle, kein Pseudo.
test('tl-item__check: Kreis oben am Ende, Fläche der Marke, Haken in text-on-brand, nur Tokens', () => {
  const marke = decl('layout', '.tl-item__check');
  assert.match(marke, /position:\s*absolute/);
  assert.match(marke, /top:\s*var\(--tl-space-xs\)/);
  assert.match(marke, /inset-inline-end:\s*var\(--tl-space-xs\)/);
  assert.match(marke, /width:\s*var\(--tl-size-icon-md\)/);
  assert.match(marke, /height:\s*var\(--tl-size-icon-md\)/);
  assert.match(marke, /border-radius:\s*var\(--tl-radius-round\)/);
  assert.match(marke, /background:\s*var\(--tl-surface-brand\)/);
  assert.doesNotMatch(marke, /z-index/, 'ohne z-index: sie folgt dem Kind im DOM und liegt so über der Karte, ohne Panels zu überdecken');
  assert.match(marke, /pointer-events:\s*none/);
  const haken = decl('layout', '.tl-item__check::after');
  assert.match(haken, /border:\s*solid var\(--tl-text-on-brand\)/);
  assert.doesNotMatch(marke + haken, /\d+px/);
});

test('der Haken der Option und der des Kästchens sind gleich gebaut: rechter und unterer Rand, rotate(45deg)', () => {
  const kaestchen = declarationsOf(readGenerated('choice', 'choice.css'), '.tl-checkbox:checked::after') ?? '';
  const option = decl('layout', '.tl-item__check::after');
  for (const d of [kaestchen, option]) {
    assert.match(d, /border-width:\s*0 var\(--tl-border-width-md\) var\(--tl-border-width-md\) 0/);
    assert.match(d, /rotate\(45deg\)/);
    assert.match(d, /width:\s*calc\(var\(--tl-size-[a-z-]+\) \* 0\.3\)/);
    assert.match(d, /height:\s*calc\(var\(--tl-size-[a-z-]+\) \* 0\.55\)/);
    assert.match(d, /translateY\(calc\(var\(--tl-size-[a-z-]+\) \* -0\.06\)\)/);
  }
  assert.match(option, /var\(--tl-size-icon-md\)/);
});

test('tl-split: Richtung aus dem Modifier, Griff space-xs mit Linie; unter dem Zeiger, beim Ziehen, mit Fokus kräftig', () => {
  assert.match(decl('split', '.tl-split--horizontal'), /flex-direction:\s*row/);
  assert.match(decl('split', '.tl-split--vertical[data-tl-state~="all-collapsed"]'), /flex-direction:\s*row/);
  assert.match(decl('split', '.tl-split--horizontal > .tl-split__handle'), /width:\s*var\(--tl-space-xs\)/);
  assert.match(decl('split', '.tl-split--horizontal > .tl-split__handle'), /cursor:\s*col-resize/);
  assert.match(decl('split', '.tl-split__handle::after'), /background:\s*var\(--tl-border-separator\)/);
  const aktiv = ohneKommentar(readGenerated('split', 'split.css'));
  assert.match(aktiv, /\.tl-split__handle:focus-visible::after[\s\S]*?background:\s*var\(--tl-border-interactive\)/);
  assert.match(decl('split', '.tl-split__handle:focus-visible'), /outline-offset:\s*calc\(-1 \* var\(--tl-focus-offset\)\)/);
});

test('die kräftige Linie des Griffs steht im Kontrast-Gate auf Grund und Layer (Spec §9)', async () => {
  const { kontrastPaare } = await import('../tools/lib/pairs.mjs');
  const P = kontrastPaare(T).map((p) => `${p.fg}|${p.bg}`);
  for (const bg of ['surface-base', 'surface-layer', 'surface-layer-nested']) assert.ok(P.includes(`border-interactive|${bg}`), bg);
});

test('eingeklappt im waagrechten Split: Titel senkrecht über writing-mode, nicht gedreht', () => {
  const css = ohneKommentar(readGenerated('split', 'split.css'));
  assert.match(css, /writing-mode:\s*vertical-rl/);
  assert.doesNotMatch(css, /rotate/);
});

test('ein Versatz für Wizard und TileStack: tl-slide-* aus space-md, keine px', () => {
  const css = ohneKommentar(readGenerated('slide', 'slide.css'));
  for (const name of ['tl-slide-in-forward', 'tl-slide-in-backward', 'tl-slide-out-forward', 'tl-slide-out-backward'])
    assert.match(css, new RegExp(`@keyframes ${name}\\b`));
  assert.match(css, /translateX\(var\(--tl-space-md\)\)/);
  assert.match(css, /translateX\(calc\(-1 \* var\(--tl-space-md\)\)\)/);
  for (const dir of ['tile-stack', 'wizard'])
    assert.doesNotMatch(ohneKommentar(readGenerated(dir, `${dir}.css`)), /\d+px|@keyframes/, dir);
});

test('Übergänge als Attribute: entering/exiting/backward in data-tl-state, verdeckter Rahmen ebenso', () => {
  const t = ohneKommentar(readGenerated('tile-stack', 'tile-stack.css'));
  assert.match(t, /\.tl-tile-stack__frame\[data-tl-state~="entering"\]\s*\{[^}]*animation:\s*tl-slide-in-forward var\(--tl-duration-slow\) var\(--tl-easing-entrance\)/);
  assert.match(t, /\.tl-tile-stack\[data-tl-state~="backward"\] > \.tl-tile-stack__frame\[data-tl-state~="entering"\]/);
  assert.match(t, /:dir\(rtl\)/);
  assert.match(decl('tile-stack', '.tl-tile-stack__frame[data-tl-state~="covered"]'), /visibility:\s*hidden/);
  const w = ohneKommentar(readGenerated('wizard', 'wizard.css'));
  assert.match(w, /\.tl-wizard__step\[data-tl-state~="exiting"\]\s*\{[^}]*animation:\s*tl-slide-out-forward var\(--tl-duration-slow\) var\(--tl-easing-exit\) forwards/);
});

test('Wizard: drei Stufen der Scheibe aus Rollen (tl-steps), Breite des Balkens als Token', () => {
  const d = decl('steps', '.tl-steps__disc');
  assert.match(d, /width:\s*var\(--tl-size-icon-lg\)/);
  assert.match(d, /border-radius:\s*var\(--tl-radius-full\)/);
  assert.match(d, /background:\s*var\(--tl-surface-layer-nested\)/);
  assert.match(d, /border:\s*var\(--tl-border-width-sm\) solid var\(--tl-border-control\)/);
  assert.match(decl('steps', '.tl-steps__item[data-tl-state~="done"] > .tl-steps__disc'), /background:\s*var\(--tl-surface-brand-subtle\)/);
  const c = decl('steps', '.tl-steps__item[aria-current="step"] > .tl-steps__disc');
  assert.match(c, /background:\s*var\(--tl-surface-brand\)/);
  assert.match(c, /color:\s*var\(--tl-text-on-brand\)/);
  assert.match(decl('wizard', '.tl-wizard__progress'), /max-width:\s*var\(--tl-size-wizard-progress\)/);
});

test('Wizard: die Paare der Scheibe stehen im Kontrast-Gate', async () => {
  const { kontrastPaare } = await import('../tools/lib/pairs.mjs');
  const P = kontrastPaare(T).map((p) => `${p.fg}|${p.bg}`);
  for (const p of ['text-on-brand|surface-brand', 'text-primary|surface-brand-subtle', 'text-secondary|surface-layer-nested',
    'border-control|surface-layer-nested', 'border-control|surface-base', 'border-control|surface-layer'])
    assert.ok(P.includes(p), p);
});

test('AdaptiveDetail: Behälter des Drawers, Leiste der Breadcrumb auf size-bar mit Linie', () => {
  const r = decl('adaptive-detail', '.tl-adaptive-detail');
  assert.match(r, /position:\s*relative/);
  assert.match(r, /overflow:\s*clip/);
  // Der Behälter beschneidet; der Ring der Schublade darin liegt innen (Abweichung 18, focus-clip).
  assert.match(decl('drawer', '.tl-drawer--container:focus-visible'), /outline-offset:\s*calc\(-1 \* var\(--tl-focus-offset\)\)/);
  const b = decl('adaptive-detail', '.tl-adaptive-detail__bar');
  assert.match(b, /min-height:\s*var\(--tl-size-bar\)/);
  assert.match(b, /border-bottom:\s*var\(--tl-border-width-sm\) solid var\(--tl-border-separator\)/);
});
