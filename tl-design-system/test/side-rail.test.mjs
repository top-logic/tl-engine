// Die Side Rail (`<side-rail>`, TLSideRail): Vertrag mit seinen Pflichtbegriffen, Specimen nach dem Harness, Zustände als
// Attribute, die Schublade ist die modale tl-drawer, die Leiste selbst ist nicht positioniert (Anker der Schublade).
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { readGenerated, declarationsOf, specimenProblems, missingInContract } from './lib/component.mjs';

const datei = (f) => readFileSync(new URL(`../src/components/side-rail/${f}`, import.meta.url), 'utf8');
const css = readGenerated('side-rail', 'side-rail.css');
const decl = (sel) => declarationsOf(css, sel) ?? '';

test('side-rail.contract.md nennt seine Pflichtbegriffe', () => {
  assert.deepEqual(missingInContract(datei('side-rail.contract.md'), [
    'tl-side-rail', 'tl-side-rail--start', 'tl-side-rail--end', 'tl-side-rail__strip', 'tl-side-rail__column',
    'tl-side-rail__handle', 'tl-side-rail__sections', 'tl-side-rail__header', 'data-tl-state~="open"',
    'data-tl-state~="drawer"', 'data-tl-state~="resizable"', 'aria-pressed="true"', 'aria-expanded', 'role="separator"',
    'showSection', 'toggleSection', 'setDrawer', 'setWidth', 'drawerBelow', 'tl-drawer', '--tl-size-bar',
  ]), []);
});

test('side-rail.spec.html besteht den Specimen-Harness und zeigt offen, zu und die Schublade', () => {
  const html = datei('side-rail.spec.html');
  assert.deepEqual(specimenProblems(html), []);
  assert.match(html, /class="tl-side-rail tl-side-rail--start[^"]*" data-tl-state="open resizable"/);
  assert.match(html, /class="tl-side-rail tl-side-rail--end" data-tl-state="resizable"/);
  assert.match(html, /<div class="tl-drawer__scrim"[^>]*><\/div>\s*<aside class="tl-drawer[^"]*tl-drawer--container" data-tl-state="open" role="dialog" aria-modal="true"/);
  assert.match(html, /class="tl-icon-badge"/);
});

test('tl-side-rail: nicht positioniert, damit die Schublade am Bereich um die Leiste verankert ist', () => {
  assert.doesNotMatch(decl('.tl-side-rail'), /position:/);
  assert.match(decl('.tl-side-rail--end'), /flex-direction:\s*row-reverse/);
  assert.match(decl('.tl-side-rail__column[hidden]'), /display:\s*none/);
  assert.match(decl('.tl-side-rail__body[hidden]'), /display:\s*none/);
});
