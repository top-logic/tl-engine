// Der Baum — 3b §7: Cursor und Auswahl wie in der Tabelle, kein Balken, kein eigener Spinner.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readGenerated, readComponent, declarationsOf, colorVarsOf, specimenProblems, missingInContract } from './lib/component.mjs';

test('tl-tree: Knoten size-row, positioniert für den Cursor-Ring, Auswahl als Fläche ohne Balken', () => {
  const css = readGenerated('tree', 'tree.css');
  const knoten = declarationsOf(css, '.tl-tree__node') ?? '';
  assert.match(knoten, /min-height:\s*var\(--tl-size-row\)/);
  assert.match(knoten, /position:\s*relative/);
  assert.match(declarationsOf(css, ':where(.tl-tree__node[aria-selected="true"])') ?? '', /background:\s*var\(--tl-surface-interactive-selected\)/);
  assert.match(declarationsOf(css, '.tl-tree') ?? '', /isolation:\s*isolate/);
  assert.match(declarationsOf(css, '.tl-tree__spacer') ?? '', /width:\s*var\(--tl-size-control-sm\)/);
  assert.doesNotMatch(css, /border-left|outline|--(selected|focused|cursor)\b|@keyframes/);
  assert.deepEqual(colorVarsOf(css), ['surface-interactive-hover', 'surface-interactive-selected', 'text-primary']);
});

test('tl-tree: Knoten bricht nicht um und endet nicht mit Ellipse, der Baum rollt waagerecht', () => {
  const css = readGenerated('tree', 'tree.css');
  const baum = declarationsOf(css, '.tl-tree') ?? '';
  assert.match(baum, /(^|[;\s])overflow:\s*auto/);
  assert.doesNotMatch(baum, /overflow-y/);
  assert.match(baum, /display:\s*grid/);
  assert.match(baum, /grid-template-columns:\s*minmax\(max-content,\s*100%\)/);
  assert.match(baum, /align-content:\s*start/);
  assert.doesNotMatch(declarationsOf(css, '.tl-tree__node') ?? '', /min-width/);
  const inhalt = declarationsOf(css, '.tl-tree__content') ?? '';
  assert.match(inhalt, /white-space:\s*nowrap/);
  assert.doesNotMatch(inhalt, /text-overflow|overflow|min-width/);
  assert.match(declarationsOf(css, '.tl-tree__content > .tl-text') ?? '', /white-space:\s*nowrap/);
});

test('tl-tree: Vertrag nennt Cursor ohne Knoten (aria-activedescendant nur mit Knoten) und die Ladezeile als treeitem', () => {
  const md = readComponent('tree', 'tree.contract.md');
  assert.deepEqual(missingInContract(md, ['tl-tree', 'tl-tree__node', 'tl-tree__spacer', 'tl-tree__content', 'aria-activedescendant', 'aria-busy', 'js.tree.loading', 'js.tree.label']), []);
  assert.match(md, /nur, solange ein Knoten mit\s+dieser Id/);
  assert.match(md, /`depth` \+ 2/);
  assert.match(md, /ohne `aria-controls`/);
});

test('tl-tree: Specimen — Ladezeile ist ein treeitem mit Namen, Ids tragen das Suffix', () => {
  const html = readComponent('tree', 'tree.spec.html');
  assert.deepEqual(specimenProblems(html), []);
  assert.match(html, /<li class="tl-status[^"]*" role="treeitem" aria-level="\d+" aria-label="Lädt">/);
  assert.match(html, /aria-busy="true"/);
  assert.doesNotMatch(html, /aria-valuetext|aria-controls/);
});

test('tl-tree: Ladezeile trägt den Namen an der li, der Fortschritt darin ist aria-hidden; Verträge stimmen überein', () => {
  const html = readComponent('tree', 'tree.spec.html');
  const zeile = html.match(/<li class="tl-status[^>]*>([\s\S]*?)<\/li>/);
  assert.ok(zeile, 'Ladezeile fehlt');
  assert.match(zeile[0], /^<li[^>]*aria-label="Lädt"/);
  const progress = zeile[1].match(/<div class="tl-progress"[^>]*>/)[0];
  assert.match(progress, /aria-hidden="true"/);
  assert.doesNotMatch(progress, /aria-label|aria-labelledby|aria-valuetext/);
  const tree = readComponent('tree', 'tree.contract.md');
  assert.match(tree, /`depth` \+ 2/);
  assert.match(tree, /`tl-progress` mit `aria-hidden="true"`/);
  const status = readComponent('status', 'status.contract.md');
  assert.match(status, /Element, das der Screenreader ansagt/);
});
