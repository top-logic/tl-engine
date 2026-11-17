// login-form: die Alternativen eines Anmeldeformulars — Gruppe unter dem Hauptknopf, „oder“ als leiser Text.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { declarationsOf, missingInContract, specimenProblems, readComponent } from './lib/component.mjs';

const css = readComponent('login-form', 'login-form.css');
const decl = (sel) => (declarationsOf(css, sel) ?? '').replace(/\s+/g, ' ');
const has = (sel, d) => assert.ok(decl(sel).includes(d), `${sel} { ${d} } fehlt in: ${decl(sel)}`);

test('Gruppe ohne Knopf verschwindet', () => {
  has('.tl-login-alternatives:not(:has(button))', 'display: none;');
});

test('Gruppe verborgen: display none (hidden.test)', () => {
  has('.tl-login-alternatives[hidden]', 'display: none;');
});

test('„oder“ steht zentriert, die Farbe kommt aus tone', () => {
  has('.tl-login-alternatives__or', 'text-align: center;');
  assert.ok(!/color:/.test(decl('.tl-login-alternatives__or')), 'keine Farbe an __or');
});

test('die Anmeldekarte hat Platz: großer Rand, Titel ohne Linie, weiter Feldabstand', () => {
  has('.tl-login-card > .tl-titlebar', 'padding: var(--tl-space-lg) var(--tl-space-xl) var(--tl-space-sm);');
  has('.tl-login-card > .tl-titlebar', 'border-bottom: 0;');
  has('.tl-login-card .tl-inset--card', 'padding: var(--tl-space-sm) var(--tl-space-xl) var(--tl-space-xl);');
  has('.tl-login-card .tl-form-layout', 'row-gap: var(--tl-space-lg);');
});

test('login-form.contract.md nennt Klassen, Randfälle', () => {
  let md = '';
  try { md = readComponent('login-form', 'login-form.contract.md'); } catch { /* fehlt */ }
  assert.deepEqual(missingInContract(md, ['tl-login-alternatives', 'tl-login-alternatives__or', 'tl-login-card', '--tl-space-xl', '--tl-space-lg', '--tl-space-sm']), []);
  assert.ok(/WebAuthn/.test(md), 'Randfall ohne WebAuthn fehlt');
  assert.ok(/Schließknopf|SSO-Fehlermeldung/.test(md), 'Randfall SSO-Fehlermeldung fehlt');
});

test('login-form.spec.html besteht den Specimen-Harness', () => {
  let html = '';
  try { html = readComponent('login-form', 'login-form.spec.html'); } catch { /* fehlt */ }
  assert.ok(html, 'login-form.spec.html fehlt');
  assert.deepEqual(specimenProblems(html), []);
});

test('specimen/index.html verlinkt login-form', () => {
  const html = readFileSync(new URL('../specimen/index.html', import.meta.url), 'utf8');
  assert.ok(html.includes('/src/components/login-form/login-form.spec.html'));
});

test("lint-css kennt 'tl-login-alternatives' ohne Modifier", () => {
  const src = readFileSync(new URL('../tools/lint-css.mjs', import.meta.url), 'utf8');
  assert.match(src, /'tl-login-alternatives': new Set\(\[\]\)/);
});
