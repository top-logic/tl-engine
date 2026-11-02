// Ein Icon-Knopf (und ab P3 der Toggle) ist quadratisch: Breite und Höhe lesen dasselbe Maß.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readGenerated, declarationsOf } from './lib/component.mjs';

export const SQUARE = [
  ['button', 'button.css', '.tl-button--icon', 'size-control'],
  ['button', 'button.css', '.tl-button--icon.tl-button--sm', 'size-control-sm'],
  ['toggle', 'toggle.css', '.tl-toggle', 'size-control-sm'],
];

for (const [dir, file, sel, mass] of SQUARE) {
  test(`${sel} ist quadratisch aus ${mass}`, () => {
    const d = declarationsOf(readGenerated(dir, file), sel);
    assert.ok(d, `${sel} fehlt`);
    assert.match(d, new RegExp(`width:\\s*var\\(--tl-${mass}\\)`));
    assert.match(d, new RegExp(`height:\\s*var\\(--tl-${mass}\\)`));
    assert.match(d, /padding:\s*0\b/);
  });
}
