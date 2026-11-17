// Die Liste (`<data-list>`, TLDataList): Eintrag aus Avatar, Zeilen und Metadaten; gewählt wie Baum und Sidebar
// (Markenton und Kante), Nebentext lesbar auf jeder Fläche, die ein Eintrag annimmt.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { readGenerated, readComponent, declarationsOf, declarationsOfAll, colorVarsOf, specimenProblems, missingInContract, contrastProblems } from './lib/component.mjs';
import { kontrastPaare } from '../tools/lib/pairs.mjs';
import { resolve } from '../tools/resolve.mjs';

const P = JSON.parse(readFileSync(new URL('../src/primitives.json', import.meta.url)));
const T = JSON.parse(readFileSync(new URL('../src/tokens.json', import.meta.url)));
const R = resolve(P, T);

const css = readGenerated('list', 'list.css');
const decl = (sel) => declarationsOf(css, sel) ?? '';
const SELECTED = '.tl-list__item:has(> .tl-list__main[aria-current="true"])';

test('tl-list: Eintrag gezeigt, gewählt (Markenton und Kante), gewählt und gezeigt — in dieser Reihenfolge', () => {
  assert.match(decl('.tl-list__item:hover'), /background:\s*var\(--tl-surface-interactive-hover\)/);
  const gewaehlt = decl(SELECTED);
  assert.match(gewaehlt, /background:\s*var\(--tl-surface-selected\)/);
  assert.match(gewaehlt, /box-shadow:\s*inset var\(--tl-border-width-accent\) 0 0 0 var\(--tl-border-selected\)/);
  assert.match(decl(`${SELECTED}:hover`), /background:\s*var\(--tl-surface-selected-hover\)/);
  assert.ok(css.indexOf(`${SELECTED}:hover`) > css.indexOf(`${SELECTED} {`));
  assert.ok(css.indexOf(`${SELECTED} {`) > css.indexOf('.tl-list__item:hover'));
});

test('tl-list: Zustände sind Attribute, die Liste setzt weder Fläche noch Schrift', () => {
  const ohne = css.replace(/\/\*[\s\S]*?\*\//g, '');
  assert.doesNotMatch(ohne, /\.tl-list[a-z_-]*--/);
  assert.doesNotMatch(ohne, /font-size|font-weight|border-left|@keyframes/);
  assert.match(decl('.tl-list[data-tl-state~="collapsed"]'), /width:\s*calc\(var\(--tl-size-control\) \+ 2 \* var\(--tl-space-2xs\)\)/);
  assert.doesNotMatch(decl('.tl-list'), /background|border(-[a-z-]+)?:/);
  assert.deepEqual(colorVarsOf(css), ['border-selected', 'focus-offset', 'surface-interactive-hover', 'surface-selected', 'surface-selected-hover',
    'text-primary', 'text-secondary']);
});

test('tl-list: Zeilen auf einer Zeile mit Ellipse, Nebentext und Metadaten in text-secondary', () => {
  for (const sel of ['.tl-list__primary', '.tl-list__secondary', '.tl-list__title']) {
    assert.match(declarationsOfAll(css, sel), /white-space:\s*nowrap/, sel);
    assert.match(declarationsOfAll(css, sel), /text-overflow:\s*ellipsis/, sel);
  }
  assert.match(decl('.tl-list__text'), /min-width:\s*0/);
  assert.match(declarationsOfAll(css, '.tl-list__secondary'), /color:\s*var\(--tl-text-secondary\)/);
  assert.match(declarationsOfAll(css, '.tl-list__trailing'), /color:\s*var\(--tl-text-secondary\)/);
  assert.match(decl('.tl-list__subheader'), /color:\s*var\(--tl-text-secondary\)/);
});

test('tl-list: der Körper rollt, die Ringe darin liegen innen', () => {
  assert.match(decl('.tl-list__body'), /overflow-y:\s*auto/);
  assert.match(decl('.tl-list__main:focus-visible'), /outline-offset:\s*calc\(-1 \* var\(--tl-focus-offset\)\)/);
  assert.match(decl('.tl-list__item > .tl-button:focus-visible'), /outline-offset:\s*calc\(-1 \* var\(--tl-focus-offset\)\)/);
});

test('tl-list: Kontrast — Text und Nebentext auf gezeigt, gewählt, gewählt und gezeigt; Kante auf der Auswahl', () => {
  const paare = kontrastPaare(T);
  const namen = new Set(paare.map((p) => `${p.fg}|${p.bg}`));
  const noetig = [];
  for (const bg of ['surface-base', 'surface-layer', 'surface-interactive-hover', 'surface-selected', 'surface-selected-hover'])
    for (const fg of ['text-primary', 'text-secondary']) noetig.push(`${fg}|${bg}`);
  noetig.push('border-selected|surface-selected', 'border-selected|surface-selected-hover', 'focus-ring|surface-selected',
    'focus-ring|surface-interactive-hover');
  assert.deepEqual(noetig.filter((k) => !namen.has(k)), []);
  const eigene = paare.filter((p) => noetig.includes(`${p.fg}|${p.bg}`));
  for (const mode of ['light', 'dark']) assert.deepEqual(contrastProblems(R, mode, eigene), []);
});

test('tl-list: Vertrag nennt jede Klasse, die Zustände als Attribute und die Parts', () => {
  const md = readComponent('list', 'list.contract.md');
  assert.deepEqual(missingInContract(md, ['tl-list', 'tl-list__header', 'tl-list__title', 'tl-list__commands', 'tl-list__search',
    'tl-list__body', 'tl-list__group', 'tl-list__subheader', 'tl-list__items', 'tl-list__item', 'tl-list__main',
    'tl-list__leading', 'tl-list__text', 'tl-list__primary', 'tl-list__secondary', 'tl-list__trailing', 'tl-list__message',
    'aria-current="true"', 'data-tl-state~="collapsed"', 'tl-reveal-on-hover', 'tl-avatar', 'tl-badge', 'select', 'rowMenu']), []);
});

test('tl-list: Specimen — gewählter Eintrag, Avatar und Badge als Parts, Auslöser mit Namen', () => {
  const html = readComponent('list', 'list.spec.html');
  assert.deepEqual(specimenProblems(html), []);
  assert.match(html, /<button type="button" class="tl-list__main" aria-current="true"/);
  assert.match(html, /<span class="tl-list__leading"><span class="tl-avatar /);
  assert.match(html, /<span class="tl-list__trailing"><span class="tl-badge /);
  assert.match(html, /tl-reveal-on-hover" aria-haspopup="menu" aria-label="Aktionen: /);
  assert.match(html, /<div class="tl-list" data-tl-state="collapsed"/);
});
