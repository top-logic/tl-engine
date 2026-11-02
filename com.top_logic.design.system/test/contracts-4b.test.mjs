// Die Verträge der Welle 4b nennen ihre Pflichtbegriffe; die neuen Specimen bestehen den Harness und zeigen die
// Zustände, die die Spec verlangt.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { missingInContract, specimenProblems } from './lib/component.mjs';
const datei = (d, f) => readFileSync(new URL(`../src/components/${d}/${f}`, import.meta.url), 'utf8');
const SOLL = {
  'app-shell/app-shell.contract.md': ['tl-app-shell', 'tl-app-shell__content', 'data-tl-display', '<main>', 'reportDisplayClass', 'useDisplayClass'],
  'app-bar/app-bar.contract.md': ['tl-app-bar', 'tl-app-bar--elevated', 'tl-app-bar__title', 'tl-app-bar__actions', 'data-tl-overflow', '--tl-size-shell-bar'],
  'sidebar/sidebar.contract.md': ['tl-sidebar', 'tl-sidebar__nav', 'tl-menu__item', 'aria-current="page"', 'data-tl-state~="collapsed"',
    'data-tl-state~="contains-current"', 'aria-expanded', 'aria-controls', 'tl-drawer', 'role="dialog"', '--tl-size-sidebar',
    'selectItem', 'executeCommand', 'toggleGroup', 'toggleCollapse', 'toggleDrawer'],
  'bottom-bar/bottom-bar.contract.md': ['tl-bottom-bar', 'aria-current="page"', 'tl-icon-badge', '--tl-size-shell-bar', 'selectItem'],
  'badge/badge.contract.md': ['tl-icon-badge', 'tl-icon-badge__count'],
  'menu/menu.contract.md': ['tl-sidebar'],
};
for (const [file, names] of Object.entries(SOLL)) test(`${file} nennt ${names.length} Pflichtbegriffe`, () => {
  const [d, f] = file.split('/');
  assert.deepEqual(missingInContract(datei(d, f), names), []);
});
for (const name of ['app-shell', 'app-bar', 'sidebar', 'bottom-bar'])
  test(`${name}.spec.html besteht den Specimen-Harness`, () => assert.deepEqual(specimenProblems(datei(name, `${name}.spec.html`)), []));

test('sidebar.spec.html zeigt die aktuelle Seite, eine zugeklappte Gruppe mit ihr, die eingeklappte Rail und den modalen Drawer', () => {
  const html = datei('sidebar', 'sidebar.spec.html');
  assert.match(html, /class="tl-menu__item[^"]*tl-sidebar__item[^"]*"[^>]*aria-current="page"/);
  assert.doesNotMatch(html, /aria-current="(true|step)"[^>]*tl-sidebar__item|tl-sidebar__item[^>]*aria-current="true"/);
  assert.match(html, /aria-expanded="false"[^>]*data-tl-state="contains-current"|data-tl-state="contains-current"[^>]*aria-expanded="false"/);
  assert.match(html, /class="tl-sidebar" data-tl-state="collapsed"/);
  assert.match(html, /<div class="tl-dialog">\s*<aside class="tl-drawer tl-drawer--start tl-drawer--narrow[^"]*" data-tl-state="open" role="dialog" aria-modal="true"/);
  assert.match(html, /<aside class="tl-drawer[^>]*>\s*<nav [^>]*class="tl-sidebar__nav"/);
  assert.match(html, /class="tl-icon-badge"/);
});

test('Specimen der Spec §7: <main> in der Shell, aria-expanded am Hamburger, aria-controls auf die Nav', () => {
  assert.match(datei('app-shell', 'app-shell.spec.html'), /<main class="tl-app-shell__content"/);
  assert.match(datei('app-bar', 'app-bar.spec.html'), /<div class="tl-app-bar__leading"><button [^>]*aria-expanded="false"/);
  assert.match(datei('sidebar', 'sidebar.spec.html'), /aria-controls="sb1-nav"/);
});

test('bottom-bar.spec.html: genau eine aktuelle Seite, ein Zähler an der Ecke', () => {
  const html = datei('bottom-bar', 'bottom-bar.spec.html');
  assert.equal((html.match(/aria-current="page"/g) ?? []).length, 1);
  assert.match(html, /class="tl-icon-badge__count"/);
});
