// Ein Block, der display setzt, schlägt die Regel [hidden] { display: none } des Browsers: seine
// Klasse ist spezifischer. Darum setzt jeder solche Block [hidden] selbst — sonst zeigt sich ein
// verborgenes Control (docs/skalen.md, Formulare).
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readdirSync, readFileSync } from 'node:fs';
import { declarationsOf } from './lib/component.mjs';

const DIR = new URL('../src/components/', import.meta.url);

function* cssFiles() {
  for (const d of readdirSync(DIR, { withFileTypes: true })) {
    if (!d.isDirectory()) continue;
    for (const f of readdirSync(new URL(`${d.name}/`, DIR))) {
      if (f.endsWith('.css')) yield [`${d.name}/${f}`, readFileSync(new URL(`${d.name}/${f}`, DIR), 'utf8')];
    }
  }
}

/** Die Blockklassen einer Datei: tl-Klassen ohne Element (__) und ohne Modifier (--). */
function blocksOf(css) {
  const ohne = css.replace(/\/\*[\s\S]*?\*\//g, '');
  return [...new Set([...ohne.matchAll(/\.(tl-[a-z0-9]+(?:-[a-z0-9]+)*)(?![a-z0-9_-])/g)].map((m) => m[1]))];
}

test('jeder Block, der display setzt, setzt auch [hidden] { display: none }', () => {
  const offen = [];
  for (const [name, css] of cssFiles()) {
    for (const block of blocksOf(css)) {
      const base = declarationsOf(css, `.${block}`);
      if (base === null || !/(^|;)\s*display\s*:/.test(base)) continue;
      const hidden = declarationsOf(css, `.${block}[hidden]`);
      if (hidden === null || !/(^|;)\s*display\s*:\s*none\b/.test(hidden)) offen.push(`${name}: .${block}`);
    }
  }
  assert.deepEqual(offen, []);
});
