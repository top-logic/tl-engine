// Die Verträge der Welle 5d nennen ihre Pflichtbegriffe.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { missingInContract, specimenProblems } from './lib/component.mjs';
const datei = (d, f) => readFileSync(new URL(`../src/components/${d}/${f}`, import.meta.url), 'utf8');
const SOLL = {
  'layout/layout.contract.md': ['tl-fill', 'tl-reveal-on-hover'],
  'drag/drag.contract.md': ['tl-drag-shield', 'tl-drag-image', 'data-tl-armed', 'tl-pick-highlight', '--tl-layer-drag',
    'data-tl-state~="picking"'],
  // Task 3 (Spec 5d §5, §6, §7; K3, K4).
  'tooltip/tooltip.contract.md': ['tl-tooltip', 'tl-tooltip__caption', 'tl-tooltip__body', 'tl-tooltip__arrow',
    'data-tl-state~="interactive"', 'role="tooltip"', 'tl-type-label-strong', 'tl-type-label', '--tl-surface-inverse',
    '--tl-text-inverse', '--tl-layer-tooltip', '--tl-shadow-popover', '--tl-size-13', 'TooltipPopover'],
  'rich-text/rich-text.contract.md': ['tl-rich-text', 'tl-field', 'tl-prose', 'tl-toolbar--wrap', 'tl-toolbar__group',
    'tl-divider--vertical', 'menuitemradio', 'aria-pressed', 'aria-invalid', '--tl-focus-width', '--tl-focus-ring',
    '--tl-border-interactive', '--tl-surface-layer-nested', '--tl-size-10', 'TLWysiwygEditor'],
  'compact-field/compact-field.contract.md': ['tl-compact-field', 'tl-compact-field__preview', 'tl-compact-field__opener',
    'tl-reveal-on-hover', 'tl-field', 'data-tl-state~="edit"', 'tl-select__controls', 'ReactCompactFieldControl'],
  // Task 10 (Spec 5d §8; Plan-Review K12, Ruling R3).
  'chart/chart.contract.md': ['tl-chart', 'tl-chart__tooltip', 'tl-chart__zoom-reset', 'tl-popover', 'tl-status',
    'role="status"', 'role="alert"', 'role="img"', '--tl-category-1-mark', '--tl-category-8-mark',
    '--tl-border-separator', '--tl-text-secondary', '--tl-layer-sticky', '--tl-layer-raised', 'data-tl-mode',
    'data-tl-theme', 'TLChart'],
  'menu/menu.contract.md': ['menuitemradio', 'aria-checked', 'ButtonDefaults', 'radio'],
  'toolbar/toolbar.contract.md': ['tl-toolbar--wrap'],
  'select/select.contract.md': ['tl-compact-field__opener'],
};
const SPECIMEN = ['tooltip', 'rich-text', 'compact-field', 'chart'];

for (const [file, names] of Object.entries(SOLL)) test(`${file} nennt ${names.length} Pflichtbegriffe`, () => {
  const [d, f] = file.split('/');
  let md = '';
  try { md = datei(d, f); } catch { /* fehlt: alle Begriffe fehlen */ }
  assert.deepEqual(missingInContract(md, names), []);
});

for (const name of SPECIMEN) test(`${name}.spec.html besteht den Specimen-Harness`, () => {
  let html = '';
  try { html = datei(name, `${name}.spec.html`); } catch { /* fehlt */ }
  assert.ok(html, `${name}.spec.html fehlt`);
  assert.deepEqual(specimenProblems(html), []);
});

test('popover.contract.md: der Fokus kehrt zum Auslöser oder zum Rückgabeziel des Aufrufers zurück', () => {
  assert.match(datei('popover', 'popover.contract.md'), /Rückgabeziel/);
});

test('specimen/index.html verlinkt Tooltip, Rich Text, Compact field und Chart', () => {
  const html = readFileSync(new URL('../specimen/index.html', import.meta.url), 'utf8');
  for (const name of SPECIMEN) assert.ok(html.includes(`/src/components/${name}/${name}.spec.html`), name);
});
