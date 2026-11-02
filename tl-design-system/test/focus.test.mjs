import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { declarations } from '../tools/lib/css.mjs';
import { componentCssFiles } from './lib/component.mjs';

const OFFSET = /^(calc\(-1 \* var\(--tl-focus-offset\)\)|var\(--tl-focus-offset\))$/;

test('kein Komponentenblock setzt den Fokusring; outline nur als Drop-Ziel in border-interactive', () => {
  const bad = [];
  for (const [name, css] of componentCssFiles()) {
    for (const d of declarations(css)) {
      if (/^outline(-color|-width)?$/.test(d.property) && !/--tl-border-interactive/.test(d.value)) bad.push(`${name}:${d.line} ${d.property}: ${d.value}`);
      if (/--tl-focus-ring/.test(d.value)) bad.push(`${name}:${d.line} liest focus-ring`);
      if (d.property === 'outline-offset' && !OFFSET.test(d.value.trim())) bad.push(`${name}:${d.line} ${d.value}`);
    }
  }
  assert.deepEqual(bad, []);
});

test('base.css trägt den Ring genau einmal', () => {
  const base = readFileSync(new URL('../dist/base.css', import.meta.url), 'utf8');
  assert.equal((base.match(/outline:\s*var\(--tl-focus-width\) solid var\(--tl-focus-ring\)/g) ?? []).length, 1);
});

test('der Cursor-Ring: ein Behälter mit aria-activedescendant gibt den Ring an das Cursor-Element ab', () => {
  const base = readFileSync(new URL('../dist/base.css', import.meta.url), 'utf8');
  const ring = /([^{}]+)\{\s*outline:\s*var\(--tl-focus-width\) solid var\(--tl-focus-ring\);/.exec(base);
  assert.ok(ring, 'Ring-Regel fehlt');
  const selektoren = ring[1].split(/,\n/).map((s) => s.trim());
  // Ein Feld mit aria-activedescendant (Suchfeld, Select ohne Suchfeld: role=combobox) ist kein Behälter:
  // sein Cursor steht im Popup, es behält den Ring.
  assert.ok(selektoren.some((s) => s.includes(':where(:not(:not(input, textarea, [role="combobox"])[aria-activedescendant])):focus-visible')),
    'der Behälter mit aria-activedescendant trägt den Ring nicht selbst, ein Feld schon');
  assert.ok(selektoren.some((s) => s === ':where(:not(input, textarea, [role="combobox"])[aria-activedescendant]:focus-visible [data-tl-state~="cursor"])::after'),
    'das Cursor-Element trägt den Ring');
  const flaeche = /:where\(:not\(input, textarea, \[role="combobox"\]\)\[aria-activedescendant\] \[data-tl-state~="cursor"\]\)::after\s*\{([^}]*)\}/.exec(base);
  assert.ok(flaeche, 'Fläche des Cursor-Rings fehlt');
  for (const d of [/content:\s*""/, /position:\s*absolute/, /inset:\s*0/, /z-index:\s*var\(--tl-layer-sticky\)/,
    /outline-offset:\s*calc\(-1 \* var\(--tl-focus-offset\)\)/, /pointer-events:\s*none/])
    assert.match(flaeche[1], d);
});

test('der Behälter mit Cursor zeichnet kein Browser-Outline — die einzige Ausnahme vom Ring', () => {
  const base = readFileSync(new URL('../dist/base.css', import.meta.url), 'utf8');
  const sel = ':where(:not(input, textarea, [role="combobox"])[aria-activedescendant]):focus-visible';
  const m = [...base.matchAll(/([^{}]+)\{([^}]*outline-style:\s*none[^}]*)\}/g)];
  assert.equal(m.length, 1, 'genau eine Regel mit outline-style: none');
  assert.equal(m[0][1].replace(/\/\*[\s\S]*?\*\//g, '').trim(), sel);
  assert.ok(/\[data-tl-state~="cursor"\]\)::after\s*\{[^}]*outline-offset/.test(base), 'Cursor-Ring-Fläche daneben');
  assert.equal((base.match(/outline:\s*var\(--tl-focus-width\) solid var\(--tl-focus-ring\)/g) ?? []).length, 1);
});
