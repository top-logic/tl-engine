// Ein Zeiger für Deaktiviert: cursor: default — docs/skalen.md „Disabled“. not-allowed verspricht eine
// Erklärung, die nicht kommt, und wird über einem Control zum Verbotsschild; der Button hatte schon default.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { componentCssFiles } from './lib/component.mjs';

test('kein Block in src/components setzt cursor: not-allowed', () => {
  const offen = [];
  for (const [name, css] of componentCssFiles()) {
    const ohne = css.replace(/\/\*[\s\S]*?\*\//g, '');
    ohne.split('\n').forEach((l, i) => { if (/cursor\s*:\s*not-allowed/.test(l)) offen.push(`${name}:${i + 1}`); });
  }
  assert.deepEqual(offen, []);
});
