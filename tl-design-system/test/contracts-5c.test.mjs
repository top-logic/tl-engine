// Die Verträge der Welle 5c nennen ihre Pflichtbegriffe; die neuen Specimen bestehen den Harness.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { missingInContract, specimenProblems } from './lib/component.mjs';
const datei = (d, f) => readFileSync(new URL(`../src/components/${d}/${f}`, import.meta.url), 'utf8');
const SOLL = {
  'calendar/calendar.contract.md': ['tl-calendar', 'tl-calendar__event', 'tl-toolbar', 'tl-segmented', 'role="listbox"',
    'role="option"', 'aria-selected', 'aria-current="date"', 'data-tl-role', 'useRovingFocus', 'textOn', 'categoryOf',
    'useStandaloneKeyboardScope'],
  'choice/choice.contract.md': ['TLCalendar'],
  'drag/drag.contract.md': ['TLCalendar', 'tl-drop-hint', 'TLKanbanBoard', 'TLDashboard'],
  'board/board.contract.md': ['tl-board', 'tl-board__column', 'tl-board__header', 'tl-board__label', 'tl-board__cards',
    'tl-board__card', 'tl-board__live', 'tl-item', 'tl-badge', 'tl-drop-hint', 'data-tl-state', 'role="listbox"',
    'role="option"', 'aria-selected', 'useRovingFocus', 'TLKanbanBoard'],
  'dashboard/dashboard.contract.md': ['tl-dashboard', 'tl-dashboard__grid', 'tl-dashboard__tile', 'tl-dashboard__action',
    'tl-dashboard__cover', 'tl-card', 'tl-overlay', 'tl-overlay__layer--fill', 'tl-overlay__layer--top-right',
    'tl-overlay__action', 'tl-visually-hidden', 'tl-inset', 'data-tl-state~="edit"', 'data-tl-axis="horizontal"',
    'useListReorder', 'TLDashboard'],
  'value-list/value-list.contract.md': ['tl-value-list', 'tl-value-list--block', 'tl-value-list__item',
    'tl-value-list__row', 'tl-select__separator', 'role="group"', 'useListReorder', 'TLValueList'],
  'table/table.contract.md': ['tl-table__cell--command', 'tl-value-list', 'tl-drop-hint'],
  'layout/layout.contract.md': ['TLKanbanBoard'],
  'overlay/overlay.contract.md': ['TLDashboard'],
  'card/card.contract.md': ['TLDashboard'],
  'badge/badge.contract.md': ['TLKanbanBoard'],
  'select/select.contract.md': ['TLValueList'],
};
const SPECIMEN = ['calendar', 'board', 'dashboard', 'value-list'];

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
