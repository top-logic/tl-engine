// Rollenfarben einmal erzeugt, nicht zweimal geschrieben — docs/skalen.md „Gleiches wird zusammengelegt“.
// Pill, Avatar und Alert lesen je Rolle dieselben zwei Tokens. Der Generator schreibt diese Regeln aus
// src/tokens.json an die Stelle eines Marker-Kommentars; die Quelle nennt nur, welche Rollen ein Block hat.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { generate, componentSources, expandRoles } from '../tools/generate.mjs';
import { primitives } from '../tools/resolve.mjs';
import { componentCssFiles, rulesOf } from './lib/component.mjs';

const P = JSON.parse(readFileSync(new URL('../src/primitives.json', import.meta.url)));
const T = JSON.parse(readFileSync(new URL('../src/tokens.json', import.meta.url)));
const OUT = generate(P, T, componentSources(new URL('../src/components/', import.meta.url)))['components.css'];
const KATEGORIEN = Object.keys(T.color.light).filter((n) => /^category-\d+-subtle$/.test(n)).map((n) => n.replace(/-subtle$/, ''));
const STATUS = ['error', 'warning', 'success', 'info'];

const regel = (sel) => rulesOf(OUT).filter(([s]) => s === sel).map(([, d]) => d.trim().replace(/\s+/g, ' '));

test('tokens.json trägt acht Kategorien, je mit subtle und border', () => {
  assert.equal(KATEGORIEN.length, 8);
  for (const k of KATEGORIEN) assert.ok(T.color.light[`${k}-border`] && T.color.dark[`${k}-border`], k);
});

// Die Tönung ist ein Attribut (3b §11, W7): data-tl-role an Pill, Avatar, Alert, Chip und Segment.
const rolle = (ziel, r) => regel(`${ziel}[data-tl-role="${r}"]`);

for (const ziel of ['.tl-pill', '.tl-avatar', '.tl-chip', '.tl-segmented__segment']) {
  test(`components.css: ${ziel} hat je Kategorie genau eine Regel aus Fläche und Linie`, () => {
    for (const k of KATEGORIEN)
      assert.deepEqual(rolle(ziel, k), [`background: var(--tl-${k}-subtle); border-color: var(--tl-${k}-border);`], k);
  });
}

for (const ziel of ['.tl-pill', '.tl-alert', '.tl-chip', '.tl-segmented__segment']) {
  test(`components.css: ${ziel} hat je Bedeutung genau eine Regel aus Fläche und Linie`, () => {
    for (const s of STATUS)
      assert.deepEqual(rolle(ziel, s), [`background: var(--tl-status-${s}-subtle); border-color: var(--tl-status-${s}-border);`], s);
  });
}

for (const ziel of ['.tl-pill', '.tl-chip', '.tl-segmented__segment']) {
  test(`components.css: ${ziel} kennt die Marke als Rolle`, () => {
    assert.deepEqual(rolle(ziel, 'brand'), ['background: var(--tl-surface-brand-subtle); border-color: var(--tl-border-interactive);']);
  });
}

test('keine Rolle ist mehr ein Modifier: weder im Generat noch in den Quellen', () => {
  assert.doesNotMatch(OUT, /\.tl-(pill|avatar|alert|chip)--(brand|error|warning|success|info|category-\d)\b/);
  for (const [name, css] of componentCssFiles()) assert.doesNotMatch(css, /\.tl-pill--brand\b/, name);
});

// Plan-Review Punkt 18: die Rollenregel (0,2,0) schlüge sonst das deaktivierte Segment, das nur die
// Schriftfarbe setzt. Der Chip braucht das nicht: .tl-chip:disabled setzt Rahmen und Fläche und steht später.
test('ein deaktiviertes Segment zeigt keine Rolle (wie der Chip); gewählt bleibt es erkennbar', () => {
  assert.deepEqual(regel('.tl-segmented__segment[data-tl-role]:disabled:not([aria-checked="true"]):not([aria-pressed="true"])'),
    ['border-color: transparent; background: transparent;']);
});

test('keine Komponentendatei schreibt eine Rollenregel aus, die der Generator erzeugt', () => {
  const offen = [];
  for (const [name, css] of componentCssFiles()) {
    const ohne = css.replace(/\/\*[\s\S]*?\*\//g, '');
    ohne.split('\n').forEach((l, i) => {
      if (/--tl-category-\d+-(subtle|border)\b/.test(l)) offen.push(`${name}:${i + 1} Kategorie`);
      if (/^\s*background\s*:\s*var\(--tl-status-[a-z]+-subtle\)/.test(l)) offen.push(`${name}:${i + 1} Status`);
    });
  }
  assert.deepEqual(offen, []);
});

test('der Marker wird ersetzt, ein unbekannter Marker bricht den Build', () => {
  const css = expandRoles('.tl-x { }\n/* @tl-roles: status .tl-x */\n/* @tl-roles: brand .tl-x__y */\n', T);
  assert.ok(!css.includes('@tl-roles'), 'Marker steht noch im Generat');
  assert.ok(css.includes('.tl-x[data-tl-role="info"] { background: var(--tl-status-info-subtle); border-color: var(--tl-status-info-border); }'));
  assert.ok(css.includes('.tl-x__y[data-tl-role="brand"] { background: var(--tl-surface-brand-subtle); border-color: var(--tl-border-interactive); }'));
  assert.ok(expandRoles('/* @tl-roles: status :where(.tl-x__y) */', T)
    .includes(':where(.tl-x__y[data-tl-role="info"]) { background: var(--tl-status-info-subtle);'), ':where-Form');
  assert.ok(expandRoles('/* @tl-roles: status-mark .tl-x--y */', T).includes('.tl-x--y[data-tl-role="info"] {'), 'Modifier als Ziel');
  assert.throws(() => expandRoles('/* @tl-roles: status :where(.tl-x[data-y]) */', T), /weder Block-, Element- noch Modifierklasse/);
  assert.throws(() => expandRoles('/* @tl-roles: farbe .tl-x */', T), /farbe/);
  assert.throws(() => expandRoles('/* @tl-roles: status .x */', T), /weder Block-, Element- noch Modifierklasse/);
  assert.throws(() => expandRoles('/* @tl-roles: status .tl-x--y--z */', T), /weder Block-, Element- noch Modifierklasse/);
  assert.ok(!OUT.includes('@tl-roles'), 'Marker im dist-Generat');
});

// Welle 5c (Spec §3, §4): gefüllte Termine lesen je Kategorie Fläche und gerechnete Schrift; der Punkt eines
// Termins im Monat nur die Fläche. Dieselbe Mechanik wie -subtle/-border, zwei weitere Arten.
test('der Marker category-surface schreibt Fläche und Schrift, category-fill nur die Fläche', () => {
  const css = expandRoles('/* @tl-roles: category-surface .tl-x__y */\n/* @tl-roles: category-fill .tl-x__z */', T);
  for (const k of KATEGORIEN) {
    assert.ok(css.includes(`.tl-x__y[data-tl-role="${k}"] { background: var(--tl-${k}-surface); color: var(--tl-text-on-${k}); }`), k);
    assert.ok(css.includes(`.tl-x__z[data-tl-role="${k}"] { background: var(--tl-${k}-surface); }`), k);
  }
  assert.equal(css.split('\n').length, 2 * KATEGORIEN.length);
});

test('components.css: der gefüllte Termin und sein Punkt lesen je Kategorie die gefüllte Fläche', () => {
  for (const k of KATEGORIEN) {
    assert.deepEqual(rolle('.tl-calendar__event', k), [`background: var(--tl-${k}-surface); color: var(--tl-text-on-${k});`], k);
    assert.deepEqual(rolle('.tl-calendar__chip-dot', k), [`background: var(--tl-${k}-surface);`], k);
  }
});

// Badge (#29653): der Zähler füllt sich je Bedeutung mit Fläche und gerechneter Schrift; der Punkt hat keine Schrift und
// liest den Text der Bedeutung, der 3:1 zu jedem Grund hat (pairs.mjs).
test('components.css: das Badge liest je Bedeutung die gefüllte Fläche, sein Punkt den Text der Bedeutung', () => {
  for (const s of STATUS) {
    assert.deepEqual(rolle('.tl-badge', s), [`background: var(--tl-status-${s}-surface); color: var(--tl-text-on-status-${s});`], s);
    assert.deepEqual(rolle('.tl-badge--dot', s), [`background: var(--tl-status-${s}-text);`], s);
  }
  assert.deepEqual(rolle('.tl-badge', 'brand'), [], 'die Marke ist die Grundfläche, keine eigene Regel');
  assert.match(regel('.tl-badge--dot').join(';'), /background: var\(--tl-border-interactive\)/, 'Punkt der Marke: ihre Linie');
  const reihe = rulesOf(OUT).map(([sel]) => sel);
  assert.ok(reihe.indexOf('.tl-badge--dot[data-tl-role="info"]') > reihe.indexOf('.tl-badge[data-tl-role="info"]'),
    'die Punktregel steht nach der Zählerregel und gewinnt bei gleicher Spezifität');
});

// Der Excel-Export der Tabelle füllt Zellen in der Fläche ihrer Rolle; er liest die Werte aus role-fills.properties,
// das der Build aus tokens.json schreibt. Je getönte Rolle eine Zeile, der Wert der Primitive hinter ihrer Fläche
// im hellen Modus.
test('role-fills.properties nennt je getönte Rolle die Fläche des hellen Modus als Wert', () => {
  const prim = primitives(P);
  const wert = (token) => prim[/^\{([a-z0-9-]+)\}$/.exec(T.color.light[token])[1]];
  const erwartet = [['brand', wert('surface-brand-subtle')],
    ...KATEGORIEN.map((k) => [k, wert(`${k}-subtle`)]),
    ...STATUS.map((s) => [s, wert(`status-${s}-subtle`)])];
  const gelesen = generate(P, T)['role-fills.properties'].split('\n')
    .filter((z) => z && !z.startsWith('#')).map((z) => z.split('='));
  assert.deepEqual(new Map(gelesen), new Map(erwartet));
  for (const [, farbe] of gelesen) assert.match(farbe, /^#[0-9A-F]{6}$/i);
  const datei = (rel) => readFileSync(new URL(rel, import.meta.url), 'utf8');
  assert.equal(datei('../src/main/webapp/style/tl-design-system/role-fills.properties'), datei('../dist/role-fills.properties'),
    'role-fills.properties: Webapp-Ordner weicht von dist/ ab — node tools/generate.mjs');
});
