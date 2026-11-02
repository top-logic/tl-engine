import { test } from 'node:test';
import assert from 'node:assert/strict';
import { varsOf, colorVarsOf, declarationsOf, specimenProblems, missingInContract } from './lib/component.mjs';

const CSS = `/* Kopf — steht vor dem ersten Selektor, wie in jeder Komponentendatei. */
.tl-a, .tl-a--x { color: var(--tl-text-primary); border: var(--tl-border-width-sm) solid var(--tl-border-control); }
.tl-a:hover { background: var(--tl-surface-interactive-hover); }`;

test('varsOf und colorVarsOf lesen Tokens, colorVarsOf ohne Maß-Rollen', () => {
  assert.deepEqual([...varsOf(CSS)].sort(), ['--tl-border-control', '--tl-border-width-sm', '--tl-surface-interactive-hover', '--tl-text-primary']);
  assert.deepEqual(colorVarsOf(CSS), ['border-control', 'surface-interactive-hover', 'text-primary']);
});

test('declarationsOf findet eine Regel über ein Glied der Selektorliste, auch hinter einem Kommentar', () => {
  assert.match(declarationsOf(CSS, '.tl-a'), /text-primary/);
  assert.match(declarationsOf(CSS, '.tl-a--x'), /text-primary/);
  assert.equal(declarationsOf(CSS, '.tl-b'), null);
});

test('specimenProblems meldet fehlende Schalter und Inline-Styles', () => {
  const ok = '<link href="/dist/components.css"><input id="mode"><input id="density"><style>.x{}</style>';
  assert.deepEqual(specimenProblems(ok), []);
  assert.equal(specimenProblems('<div style="color:red"></div>').length, 4);
});

test('missingInContract nennt, was nicht in Backticks steht', () => {
  assert.deepEqual(missingInContract('liest `--tl-text-primary`', ['--tl-text-primary', 'aria-invalid']), ['aria-invalid']);
});
