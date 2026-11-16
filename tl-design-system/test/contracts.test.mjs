// Gate: Der Vertrag einer Komponente nennt jedes Token, das ihr CSS liest. Gerechnet auf dem CSS, wie es in
// dist/components.css landet (Rollenregeln expandiert) — ein Token, das nirgends steht, kennt kein Kunde als Schnittstelle.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync, existsSync } from 'node:fs';
import { componentCssFiles, namedInContract } from './lib/component.mjs';
import { expandRoles } from '../tools/generate.mjs';

const TOKENS = JSON.parse(readFileSync(new URL('../src/tokens.json', import.meta.url), 'utf8'));
const stripComments = (css) => css.replace(/\/\*[\s\S]*?\*\//g, '');
const tokensOf = (css) => new Set([...stripComments(css).matchAll(/var\(\s*(--tl-[a-z0-9-]+)/g)].map((m) => m[1]));

/** Muster für ein per Rolle erzeugtes Token: `category-<n>-subtle`, `status-<s>-border`, `text-on-category-<n>`.
    Ein Token ohne Muster (die Rolle `brand`: `surface-brand-subtle`, `border-interactive`) steht wörtlich. */
function rolePattern(token) {
  let m;
  if ((m = /^--tl-category-\d+-([a-z-]+)$/.exec(token))) return `category-<n>-${m[1]}`;
  if ((m = /^--tl-text-on-category-\d+$/.exec(token))) return 'text-on-category-<n>';
  if ((m = /^--tl-status-(?:error|warning|success|info)-([a-z-]+)$/.exec(token))) return `status-<s>-${m[1]}`;
  return null;
}

const named = (md, token, generated) => {
  if (namedInContract(md, token)) return true;
  const p = generated && rolePattern(token);
  return !!p && (namedInContract(md, p) || namedInContract(md, '--tl-' + p));
};

test('der Vertrag jeder Komponentendatei nennt jedes Token, das ihr CSS liest', () => {
  const problems = [];
  for (const [name, css] of componentCssFiles()) {
    const [dir, file] = name.split('/');
    const base = file.replace(/\.css$/, '');
    const contract = new URL(`../src/components/${dir}/${base}.contract.md`, import.meta.url);
    if (!existsSync(contract)) { problems.push(`${name}: kein Vertrag ${base}.contract.md`); continue; }
    const md = readFileSync(contract, 'utf8');
    const own = tokensOf(css);
    const missing = [...tokensOf(expandRoles(css, TOKENS))].filter((t) => !named(md, t, !own.has(t))).sort();
    if (missing.length) problems.push(`${name}: ${missing.map((t) => t.slice(5)).join(', ')}`);
  }
  assert.ok(problems.length === 0, `Der Vertrag nennt nicht jedes gelesene Token:\n${problems.join('\n')}`);
});
