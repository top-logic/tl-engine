import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { generate, componentSources } from '../tools/generate.mjs';
import { rulesOf, aufgeloest } from './lib/component.mjs';

const P = JSON.parse(readFileSync(new URL('../src/primitives.json', import.meta.url)));
const T = JSON.parse(readFileSync(new URL('../src/tokens.json', import.meta.url)));
const out = generate(P, T);
const prim = out['primitives.css'], tok = out['tokens.css'], base = out['base.css'];

test('primitives.css trägt jede Primitive als --tl-Variable mit ihrem Wert', () => {
  for (const g of Object.values(P)) if (typeof g === 'object')
    for (const [n, v] of Object.entries(g)) assert.ok(prim.includes(`--tl-${n}: ${v};`), n);
});

test('tokens.css enthält keinen Wert, nur var()-Referenzen', () => {
  const decls = tok.match(/--tl-[a-z0-9-]+:\s*[^;]+;/g);
  for (const d of decls) {
    if (/^--tl-(text|icon)-on-/.test(d)) continue; // erzeugte Beschriftungen tragen Hex
    assert.match(d, /:\s*var\(--tl-[a-z0-9-]+\);$/, d);
  }
});

test('jede Farbrolle steht in beiden Modi', () => {
  const light = tok.slice(0, tok.indexOf('[data-tl-mode="dark"]'));
  const dark = tok.slice(tok.indexOf('[data-tl-mode="dark"]'));
  for (const n of Object.keys(T.color.light)) {
    assert.ok(light.includes(`--tl-${n}:`), `${n} fehlt hell`);
    assert.ok(dark.includes(`--tl-${n}:`), `${n} fehlt dunkel`);
  }
});

test('die erzeugten Beschriftungen stehen als Hex in beiden Modi', () => {
  for (const mode of ['light', 'dark']) {
    const block = mode === 'light' ? tok.slice(0, tok.indexOf('[data-tl-mode="dark"]')) : tok.slice(tok.indexOf('[data-tl-mode="dark"]'));
    for (const n of Object.keys(T.derived[mode]))
      assert.match(block, new RegExp(`--tl-${n}: #[0-9A-F]{6};`), `${n} ${mode}`);
  }
});

test('hell ist der Standard, dunkel und kompakt sind Attribute', () => {
  assert.ok(tok.includes(':root,\n[data-tl-mode="light"]'));
  assert.ok(tok.includes('[data-tl-mode="dark"] {'));
  assert.ok(tok.includes('[data-tl-density="compact"] {'));
  assert.ok(!tok.includes('data-modus'), 'alte Attribute');
});

test('color-scheme folgt dem Modus', () => {
  assert.ok(tok.includes('color-scheme: light;'));
  assert.ok(tok.includes('color-scheme: dark;'));
});

test('base.css: jede Typo-Klasse setzt fünf Eigenschaften aus Variablen', () => {
  for (const [n, k] of Object.entries(T.type)) {
    if (n.startsWith('_')) continue;
    const i = base.indexOf(`.tl-type-${n} {`);
    assert.ok(i >= 0, `.tl-type-${n} fehlt`);
    const rule = base.slice(i, base.indexOf('}', i));
    assert.ok(rule.includes(`font-family: var(--tl-font-family-${k.family})`), n);
    assert.ok(rule.includes(`font-size: var(--tl-font-size-${k.size})`), n);
    assert.ok(rule.includes(`font-weight: ${k.weight}`), n);
    assert.ok(rule.includes(`line-height: ${k.lineHeight}`), n);
    assert.ok(rule.includes(`letter-spacing: ${k.tracking}`), n);
  }
});

// Regressionstest U66 (#30322): das gerechnete Ergebnis jeder Typo- und Druckklasse bleibt gleich, egal ob die Werte
// in tokens.json, in Rollen oder in Primitiven stehen. Gelesen wird dist/base.css, jedes var() bis zum Literal verfolgt.
const gerechnet = (selektor, eigenschaften) => {
  const regel = rulesOf(readFileSync(new URL('../dist/base.css', import.meta.url), 'utf8'))
    .find(([sel]) => sel.split(',').map((x) => x.trim()).includes(selektor));
  assert.ok(regel, `${selektor} fehlt in dist/base.css`);
  return Object.fromEntries(eigenschaften.map((e) => {
    const m = new RegExp(`(?:^|;|\\s)${e}:\\s*([^;]+)`).exec(regel[1]);
    assert.ok(m, `${selektor} setzt ${e} nicht`);
    return [e, aufgeloest(m[1])];
  }));
};

test('U66: Typo-Klassen rechnen weiter auf Stärke, Zeilenhöhe und Laufweite von vorher', () => {
  const soll = {
    'display': ['600', '1.25', '-0.01em'], 'heading-lg': ['600', '1.3333', '-0.005em'],
    'heading-md': ['600', '1.4', '0'], 'heading-sm': ['600', '1.5', '0'],
    'body': ['400', '1.4286', '0'], 'body-strong': ['600', '1.4286', '0'],
    'label': ['400', '1.3333', '0.01em'], 'label-strong': ['600', '1.3333', '0.01em'],
    'overline': ['400', '1.3333', '0.04em'], 'code': ['400', '1.4286', '0'],
  };
  for (const [n, [weight, lineHeight, tracking]] of Object.entries(soll))
    assert.deepEqual(gerechnet(`.tl-type-${n}`, ['font-weight', 'line-height', 'letter-spacing']),
      { 'font-weight': weight, 'line-height': lineHeight, 'letter-spacing': tracking }, n);
});

test('U66: Druckklassen rechnen weiter auf Größe, Stärke und Zeilenhöhe von vorher', () => {
  const soll = {
    title: ['18pt', '600', '1.25'], heading: ['12pt', '600', '1.35'],
    body: ['10pt', '400', '1.4'], caption: ['8pt', '400', '1.35'],
  };
  for (const [n, [size, weight, lineHeight]] of Object.entries(soll))
    assert.deepEqual(gerechnet(`.tl-print-${n}`, ['font-size', 'font-weight', 'line-height']),
      { 'font-size': size, 'font-weight': weight, 'line-height': lineHeight }, n);
});

test('base.css: Zahlenklasse, Fokusring, Reduced Motion, Druckklassen', () => {
  assert.ok(base.includes('.tl-nums {'));
  assert.ok(base.includes('font-variant-numeric: tabular-nums slashed-zero'));
  // Drei Selektoren, eine Regel: der Ring am fokussierten Element, am Cursor-Element eines Behälters
  // mit aria-activedescendant (3b §3) und an der Dashboard-Kachel, deren verborgener Knopf den Fokus hat (5c §6).
  // Die Deklaration steht genau einmal (focus.test.mjs). Dazu die eine Ausnahme: der Behälter mit Cursor zeichnet
  // kein Browser-Outline.
  assert.equal((base.match(/:focus-visible/g) ?? []).length, 4, 'ein Fokusring, drei Selektoren, eine Ausnahme');
  assert.ok(base.includes('outline: var(--tl-focus-width) solid var(--tl-focus-ring)'));
  assert.ok(base.includes('outline-offset: var(--tl-focus-offset)'));
  assert.ok(!/:focus\b(?!-visible)/.test(base), 'kein :focus ohne -visible');
  assert.ok(base.includes('@media (prefers-reduced-motion: reduce)'));
  assert.ok(base.includes('transition-duration: 0.01ms !important'));
  assert.ok(base.includes('@media print'));
  for (const n of Object.keys(T.print)) assert.ok(base.includes(`.tl-print-${n} {`), n);
});

test('base.css: der Body liest Grund, Text und Schrift aus Tokens', () => {
  assert.ok(base.includes('background: var(--tl-surface-base)'));
  assert.ok(base.includes('color: var(--tl-text-primary)'));
  assert.ok(base.includes('font-family: var(--tl-font-family-sans)'));
});

test('außerhalb von primitives.css steht keine Farbe und keine Dauer als Literal', () => {
  const text = tok.replace(/--tl-(text|icon)-on-[a-z0-9-]+: #[0-9A-F]{6};/g, '') + base;
  assert.equal((text.match(/#[0-9a-fA-F]{3,8}\b|\brgba?\(|\bhsla?\(/g) ?? []).length, 0, 'Farbliteral');
  assert.equal((text.replace(/0\.01ms/g, '').match(/\b\d+m?s\b/g) ?? []).length, 0, 'Dauer-Literal');
});

test('base.css führt vier Icon-Klassen mit Größe aus size-icon-*, line-height 1, quadratisch', () => {
  const base = generate(P, T)['base.css'];
  for (const s of ['glyph', 'sm', 'md', 'lg']) {
    const re = new RegExp(`\\.tl-icon-${s}\\s*\\{[^}]*font-size:\\s*var\\(--tl-size-icon-${s}\\)[^}]*\\}`);
    assert.ok(re.test(base), `.tl-icon-${s} liest nicht --tl-size-icon-${s}`);
  }
  assert.ok(/\.tl-icon-glyph, \.tl-icon-sm, \.tl-icon-md, \.tl-icon-lg\s*\{[^}]*line-height:\s*1;[^}]*width:\s*1em;[^}]*height:\s*1em;[^}]*display:\s*inline-block;/.test(base),
    'gemeinsame Regel fehlt (line-height 1, 1em-Quadrat, inline-block)');
});

test('components.css hängt jede Komponente unter src/components/**/ in Pfadreihenfolge zusammen', () => {
  const comps = componentSources(new URL('../src/components/', import.meta.url));
  assert.ok(comps.some((c) => c.name === 'button/button.css'), 'button.css fehlt');
  const out = generate(P, T, comps)['components.css'];
  assert.ok(out.startsWith('/* Generated by tl-design-system'), 'Herkunftszeile fehlt');
  assert.ok(out.includes('/* Components, one section per file under src/components/. */'), 'Kopfzeile fehlt');
  assert.ok(out.includes('/* --- button/button.css --- */'), 'Abschnittsmarke fehlt');
  assert.ok(out.includes('.tl-button {'), 'Inhalt fehlt');
  const names = comps.map((c) => c.name);
  assert.deepEqual(names, [...names].sort(), 'nicht nach Pfad sortiert');
});
