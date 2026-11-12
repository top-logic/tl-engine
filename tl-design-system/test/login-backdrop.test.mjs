// login-backdrop: Hintergrund der Anmeldeseite — eine Zeichenfläche hinter dem Inhalt (Halbton-Raster).
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { declarationsOf, missingInContract, specimenProblems, readComponent } from './lib/component.mjs';

const css = readComponent('login-backdrop', 'login-backdrop.css');
const decl = (sel) => (declarationsOf(css, sel) ?? '').replace(/\s+/g, ' ');
const has = (sel, d) => assert.ok(decl(sel).includes(d), `${sel} { ${d} } fehlt in: ${decl(sel)}`);

test('Wurzel: Fläche mit Rand, Inhalt mittig', () => {
  for (const d of ['position: relative;', 'overflow: hidden;', 'display: grid;', 'place-items: center;',
    'background: var(--tl-surface-base);', 'padding: var(--tl-space-lg);']) has('.tl-login-backdrop', d);
});

test('Wurzel verborgen: display none (hidden.test)', () => {
  has('.tl-login-backdrop[hidden]', 'display: none;');
});

test('Zeichenfläche füllt die Wurzel und fängt keine Zeiger', () => {
  // Canvas ist ein ersetztes Element: inset dehnt es nicht, width/height 100% schon.
  for (const d of ['position: absolute;', 'inset: 0;', 'width: 100%;', 'height: 100%;', 'pointer-events: none;']) has('.tl-login-backdrop__canvas', d);
});

test('Inhalt: Spalte, scrollt statt abzuschneiden', () => {
  for (const d of ['position: relative;', 'display: flex;', 'flex-direction: column;', 'align-items: center;',
    'gap: var(--tl-space-md);', 'max-height: 100%;', 'overflow-y: auto;', 'min-height: 0;']) has('.tl-login-backdrop__content', d);
  // Der Rand sitzt an der Wurzel; der Innenabstand des Inhalts ist nur Platz für den Fokusring (focus-clip).
  has('.tl-login-backdrop__content', 'padding: calc(var(--tl-focus-width) + var(--tl-focus-offset));');
});

test('Logo steht auf eigener Unterlage', () => {
  for (const d of ['background: var(--tl-surface-layer);', 'border-radius: var(--tl-radius-md);',
    'padding: var(--tl-space-sm) var(--tl-space-md);',
    'border: var(--tl-border-width-sm) solid var(--tl-border-separator);']) has('.tl-login-backdrop__brand', d);
  for (const d of ['max-height: var(--tl-size-login-logo);', 'max-width: 100%;']) has('.tl-login-backdrop__logo', d);
});

test('kein Modus-Selektor im CSS (ein img, Quelle folgt dem Modus)', () => {
  assert.ok(!/data-tl-mode|data-tl-logo-mode/.test(css.replace(/\/\*[\s\S]*?\*\//g, '')));
});

test('login-backdrop.contract.md nennt Klassen, Tokens und Client', () => {
  let md = '';
  try { md = readComponent('login-backdrop', 'login-backdrop.contract.md'); } catch { /* fehlt */ }
  assert.deepEqual(missingInContract(md, [
    'tl-login-backdrop', 'tl-login-backdrop__canvas', 'tl-login-backdrop__content', 'tl-login-backdrop__brand',
    'tl-login-backdrop__logo', '--tl-surface-base', '--tl-surface-layer', '--tl-border-separator',
    '--tl-border-width-sm', '--tl-radius-md', '--tl-space-sm', '--tl-space-md', '--tl-space-lg',
    '--tl-size-login-logo', '--tl-focus-width', '--tl-focus-offset', '--tl-surface-halftone', '--tl-surface-halftone-strong', '--tl-size-halftone-step',
    '--tl-size-halftone-glow', '--tl-duration-ripple', '--tl-duration-morph', 'TLLoginBackdrop',
    'child', 'logo', 'logoDark', 'logoAlt']), []);
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
