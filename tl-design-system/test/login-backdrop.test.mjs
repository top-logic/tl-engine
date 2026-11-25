// login-backdrop: Hintergrund der Anmeldeseite — eine Zeichenfläche hinter dem Inhalt (Halbton-Raster).
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { declarationsOf, missingInContract, specimenProblems, readComponent } from './lib/component.mjs';

const css = readComponent('login-backdrop', 'login-backdrop.css');
const decl = (sel) => (declarationsOf(css, sel) ?? '').replace(/\s+/g, ' ');
const has = (sel, d) => assert.ok(decl(sel).includes(d), `${sel} { ${d} } fehlt in: ${decl(sel)}`);

test('Wurzel: Fläche mit Rand, Inhalt mittig', () => {
  // Reicht die Höhe nicht, scrollt die ganze Seite (Wurzel), kein Kasten darin; seitlich nie.
  for (const d of ['position: relative;', 'overflow: hidden auto;', 'display: grid;', 'place-items: center;',
    'grid-template-rows: 1fr auto;',
    'background: var(--tl-surface-base);', 'padding: var(--tl-space-lg);']) has('.tl-login-backdrop', d);
});

test('schmales Fenster: Inhalt und Karte werden nie breiter als die Wurzel', () => {
  // Die Karte trägt eine feste Breite (width="420px"); auf dem Handy (375 px) ragte sie rechts hinaus, und die
  // Wurzel schnitt sie ab. Die Spalte darf unter ihren Inhalt schrumpfen, Inhalt und seine Kinder folgen ihr.
  has('.tl-login-backdrop', 'grid-template-columns: minmax(0, 1fr);');
  has('.tl-login-backdrop__content', 'max-width: 100%;');
  has('.tl-login-backdrop__content > *', 'max-width: 100%;');
});

test('Wurzel verborgen: display none (hidden.test)', () => {
  has('.tl-login-backdrop[hidden]', 'display: none;');
});

test('Zeichenfläche füllt die Wurzel und fängt keine Zeiger', () => {
  // Canvas ist ein ersetztes Element: inset dehnt es nicht, width/height 100% schon.
  for (const d of ['position: absolute;', 'inset: 0;', 'width: 100%;', 'height: 100%;', 'pointer-events: none;']) has('.tl-login-backdrop__canvas', d);
});

test('Inhalt: Spalte, kein eigener Scrollbereich', () => {
  for (const d of ['position: relative;', 'display: flex;', 'flex-direction: column;', 'align-items: center;',
    'gap: var(--tl-space-md);']) has('.tl-login-backdrop__content', d);
  assert.ok(!/overflow|max-height/.test(decl('.tl-login-backdrop__content')), 'Inhalt scrollt nicht selbst');
  // Der Rand sitzt an der Wurzel; der Innenabstand des Inhalts ist nur Platz für den Fokusring (focus-clip).
  has('.tl-login-backdrop__content', 'padding: calc(var(--tl-focus-width) + var(--tl-focus-offset));');
});

test('Logo steht auf eigener Unterlage', () => {
  for (const d of ['background: var(--tl-surface-layer);', 'border-radius: var(--tl-radius-md);',
    'padding: var(--tl-space-sm) var(--tl-space-md);',
    'border: var(--tl-border-width-sm) solid var(--tl-border-separator);']) has('.tl-login-backdrop__brand', d);
  for (const d of ['max-height: var(--tl-size-login-logo);', 'max-width: 100%;']) has('.tl-login-backdrop__logo', d);
});

test('Name der Anwendung und Fußzeile', () => {
  for (const d of ['margin: 0;', 'color: var(--tl-text-primary);']) has('.tl-login-backdrop__title', d);
  // Positioniert, sonst malt die Zeichenfläche über sie.
  for (const d of ['position: relative;', 'margin: 0;', 'padding-top: var(--tl-space-md);',
    'display: flex;', 'align-items: center;', 'gap: var(--tl-space-xs);',
    'color: var(--tl-text-secondary);']) has('.tl-login-backdrop__footer', d);
  for (const d of ['height: var(--tl-size-login-wordmark);', 'width: auto;']) has('.tl-login-backdrop__wordmark', d);
});

test('kein Modus-Selektor im CSS (ein img, Quelle folgt dem Modus)', () => {
  assert.ok(!/data-tl-mode|data-tl-logo-mode/.test(css.replace(/\/\*[\s\S]*?\*\//g, '')));
});

test('login-backdrop.contract.md nennt Klassen, Tokens und Client', () => {
  let md = '';
  try { md = readComponent('login-backdrop', 'login-backdrop.contract.md'); } catch { /* fehlt */ }
  assert.deepEqual(missingInContract(md, [
    'tl-login-backdrop', 'tl-login-backdrop__canvas', 'tl-login-backdrop__content', 'tl-login-backdrop__brand',
    'tl-login-backdrop__logo', 'tl-login-backdrop__title', 'tl-login-backdrop__footer', 'tl-login-backdrop__wordmark', '--tl-surface-base', '--tl-surface-layer', '--tl-border-separator',
    '--tl-border-width-sm', '--tl-radius-md', '--tl-space-sm', '--tl-space-md', '--tl-space-lg',
    '--tl-size-login-logo', '--tl-focus-width', '--tl-focus-offset', '--tl-text-primary', '--tl-text-secondary', '--tl-space-xs', '--tl-size-login-wordmark', '--tl-surface-halftone', '--tl-surface-halftone-strong', '--tl-size-halftone-step',
    '--tl-size-halftone-glow', '--tl-duration-ripple', 'TLLoginBackdrop',
    'child', 'logo', 'logoDark', 'logoAlt', 'appTitle', 'js.loginBackdrop.poweredBy']), []);
});

test('login-backdrop.spec.html besteht den Specimen-Harness', () => {
  let html = '';
  try { html = readComponent('login-backdrop', 'login-backdrop.spec.html'); } catch { /* fehlt */ }
  assert.ok(html, 'login-backdrop.spec.html fehlt');
  assert.deepEqual(specimenProblems(html), []);
});

test('specimen/index.html verlinkt login-backdrop', () => {
  const html = readFileSync(new URL('../specimen/index.html', import.meta.url), 'utf8');
  assert.ok(html.includes('/src/components/login-backdrop/login-backdrop.spec.html'));
});

test("lint-css kennt 'tl-login-backdrop' ohne Modifier", () => {
  const src = readFileSync(new URL('../tools/lint-css.mjs', import.meta.url), 'utf8');
  assert.match(src, /'tl-login-backdrop': new Set\(\[\]\)/);
});
