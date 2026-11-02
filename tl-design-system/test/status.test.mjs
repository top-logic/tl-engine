// Laden und Leer — 3b §10 (W10): eine Zeile, herausgelöst aus tl-select__status.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readGenerated, declarationsOf } from './lib/component.mjs';

test('tl-status: eine Zeile size-row in text-secondary, als Alarm status-error-text', () => {
  const css = readGenerated('status', 'status.css');
  const d = declarationsOf(css, '.tl-status') ?? '';
  assert.match(d, /min-height:\s*var\(--tl-size-row\)/);
  assert.match(d, /gap:\s*var\(--tl-space-xs\)/);
  assert.match(d, /padding-inline:\s*var\(--tl-space-control-x\)/);
  assert.match(d, /color:\s*var\(--tl-text-secondary\)/);
  assert.match(declarationsOf(css, '.tl-status[role="alert"]') ?? '', /color:\s*var\(--tl-status-error-text\)/);
  assert.equal(declarationsOf(readGenerated('select', 'select.css'), '.tl-select__status'), null, 'tl-select__status ist aufgegangen');
});
