import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { missingInContract } from './lib/component.mjs';
const md = (d, f) => readFileSync(new URL(`../src/components/${d}/${f}`, import.meta.url), 'utf8');
const SOLL = {
  'popover/popover.contract.md': ['tl-popover', 'data-tl-anchored-overlay', 'layer-popover'],
  'menu/menu.contract.md': ['tl-menu', 'tl-menu__item', 'role="menu"', 'aria-current', 'Home', 'End', 'Escape', 'tl-menu-region', 'aria-expanded'],
  'toolbar/toolbar.contract.md': ['tl-toolbar', 'data-tl-overflow', 'data-tl-compact', 'tl-divider--vertical', 'iconOnly'],
  'tabs/tabs.contract.md': ['tl-tabs', 'role="tablist"', 'aria-selected', 'aria-controls', 'ArrowRight', 'Home',
    'tl-tabs__strip', 'tl-tabs__scroll', 'data-tl-edge', 'preventScroll', 'useScrollStrip', 'js.tabs.scrollStart', 'js.tabs.scrollEnd', 'disabled'],
  'breadcrumb/breadcrumb.contract.md': ['tl-breadcrumb', 'aria-current="page"', 'tl-button--link', 'js.breadcrumb.label'],
  'toggle/toggle.contract.md': ['tl-toggle', 'aria-expanded', 'aria-controls', 'aria-label'],
};
for (const [file, names] of Object.entries(SOLL)) test(`${file} nennt ${names.length} Pflichtbegriffe`, () => {
  const [d, f] = file.split('/');
  assert.deepEqual(missingInContract(md(d, f), names), []);
});
