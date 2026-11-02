// Die Verträge der Welle 3b nennen ihre Pflichtbegriffe; die neuen Specimen bestehen den Harness.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { missingInContract, specimenProblems } from './lib/component.mjs';
const datei = (d, f) => readFileSync(new URL(`../src/components/${d}/${f}`, import.meta.url), 'utf8');
const SOLL = {
  'status/status.contract.md': ['tl-status', 'role="alert"', 'tl-progress', 'aria-busy', '--tl-size-row', '--tl-text-secondary', '--tl-status-error-text'],
  'drag/drag.contract.md': ['drop-before', 'drop-after', 'drop-onto', 'drop-refused', 'dragging', 'data-tl-axis', 'dropPositionAt', 'useListReorder'],
  'reorder-list/reorder-list.contract.md': ['tl-reorder-list', 'tl-reorder-list__item', 'tl-reorder-list__handle', 'tl-reorder-list__search', 'aria-describedby', 'role="status"', 'js.reorder.moved',
    'tl-reorder-list__actions', 'tl-reorder-list__lock', 'tl-reorder-list__eye', 'tl-reorder-list__section', 'data-tl-state="off"', 'locked', 'section-end', 'columnLock', 'js.table.columnLastVisible'],
  'text/text.contract.md': ['tl-text', 'data-tl-tone', 'ellipsis', 'tl-text--truncate', 'tl-type-body-strong', 'placeholder', 'on-brand', 'tl-pill', 'data-tl-role'],
  'columns/columns.contract.md': ['tl-columns', 'tl-columns--gap-compact', 'tl-columns--gap-default', 'tl-columns--gap-loose', 'tl-columns__column'],
  'table/table.contract.md': ['tl-table', 'tl-table__grid', 'role="grid"', 'role="treegrid"', 'aria-rowcount', 'aria-rowindex', 'aria-activedescendant',
    'aria-selected', 'aria-sort', 'data-tl-state', 'cursor', 'frozen', 'pinned', 'filtered', 'tl-checkbox', 'indeterminate',
    'js.table.selectAll', 'js.table.selectRow', 'js.table.empty', 'js.table.filterActive', 'js.table.columnWidth', 'js.table.freezeSplitter', 'tl-toggle', 'tl-skeleton', 'tl-status', 'tl-chip__toggle', 'tl-field-group',
    '--tl-size-row', '--tl-size-bar'],
  'tree/tree.contract.md': ['tl-tree', 'role="tree"', 'role="treeitem"', 'aria-level', 'aria-expanded', 'aria-selected', 'aria-busy',
    'aria-activedescendant', 'js.tree.label', 'js.tree.loading', 'tl-toggle', 'tl-status', 'cursor'],
  'pill/pill.contract.md': ['data-tl-role'],
  'avatar/avatar.contract.md': ['data-tl-role'],
  'alert/alert.contract.md': ['data-tl-role'],
  'select/select.contract.md': ['tl-status', 'data-tl-axis', 'useListReorder', 'filterOptions'],
  'field/field.contract.md': ['onGoto', 'href', 'data-tl-role', 'ThemeIcon'],
  'skeleton/skeleton.contract.md': ['tl-table__placeholder'],
};
for (const [file, names] of Object.entries(SOLL)) test(`${file} nennt ${names.length} Pflichtbegriffe`, () => {
  const [d, f] = file.split('/');
  assert.deepEqual(missingInContract(datei(d, f), names), []);
});
for (const name of ['status', 'reorder-list', 'text', 'columns', 'table', 'tree'])
  test(`${name}.spec.html besteht den Specimen-Harness`, () => assert.deepEqual(specimenProblems(datei(name, `${name}.spec.html`)), []));
