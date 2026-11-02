// Die Komponente ist fertig, wenn jede Erscheinung × Tönung × Modus die Anforderung erfüllt —
// gegen beide Flächen, auf denen ein Button liegen kann.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { ratio, AA_TEXT, AA_NON_TEXT } from '../tools/lib/contrast.mjs';
import { resolve } from '../tools/resolve.mjs';
import { readGenerated, componentCssFiles, declarationsOf } from './lib/component.mjs';
import { declarations } from '../tools/lib/css.mjs';

const P = JSON.parse(readFileSync(new URL('../src/primitives.json', import.meta.url)));
const T = JSON.parse(readFileSync(new URL('../src/tokens.json', import.meta.url)));
const R = resolve(P, T); // R.color[mode] / R.derived[mode]: Rolle → Hex

const GRUND = ['surface-base', 'surface-layer'];

/** Was jede Kombination liest — dieselben Namen wie in button.css. */
const BUTTON = {
  primary:            { fill: 'surface-brand', text: 'text-on-brand' },
  'primary-hover':    { fill: 'surface-brand-hover', text: 'text-on-brand-hover' },
  'primary-active':   { fill: 'surface-brand-active', text: 'text-on-brand-active' },
  secondary:          { text: 'text-primary', line: 'border-control' },
  'secondary-hover':  { fill: 'surface-interactive-hover', text: 'text-primary', line: 'border-control' },
  'secondary-active': { fill: 'surface-interactive-active', text: 'text-primary', line: 'border-control' },
  ghost:              { text: 'text-primary' },
  'ghost-hover':      { fill: 'surface-interactive-hover', text: 'text-primary' },
  'ghost-active':     { fill: 'surface-interactive-active', text: 'text-primary' },
  link:               { text: 'text-link' },
  'link-hover':       { text: 'text-link-hover' },
  pressed:            { fill: 'surface-brand-subtle', text: 'text-primary', line: 'border-interactive' },
  'pressed-hover':    { fill: 'surface-brand-subtle-hover', text: 'text-primary', line: 'border-interactive' },
  'danger-primary':   { fill: 'status-error-surface', text: 'text-on-status-error' },
  'danger-primary-hover':  { fill: 'status-error-surface-hover', text: 'text-on-status-error-hover' },
  'danger-primary-active': { fill: 'status-error-surface-active', text: 'text-on-status-error-active' },
  'danger-secondary': { text: 'status-error-text', line: 'status-error-border' },
  'danger-secondary-hover': { fill: 'status-error-surface', text: 'text-on-status-error' },
  'danger-secondary-active': { fill: 'status-error-surface-active', text: 'text-on-status-error-active' },
  'danger-ghost':     { text: 'status-error-text' },
  'danger-ghost-hover': { fill: 'status-error-surface', text: 'text-on-status-error' },
  'danger-ghost-active': { fill: 'status-error-surface-active', text: 'text-on-status-error-active' },
  // Disabled ist vom Kontrast-Gate ausgenommen (WCAG 1.4.3 nimmt inaktive Bedienelemente aus;
  // skalen.md „Disabled: vier Rollen ohne Gate“) — nur Token-Existenz wird geprüft, keine Ratio.
  disabled:           { fill: 'surface-disabled', text: 'text-disabled', line: 'border-disabled', exempt: true },
  'ghost-disabled':   { text: 'text-disabled', exempt: true },
};

for (const mode of ['light', 'dark']) {
  const C = { ...R.color[mode], ...R.derived[mode] };
  test(`Button ${mode}: Text 4,5 und Linie 3,0 in jeder Erscheinung × Tönung, auf base und layer`, () => {
    for (const [name, k] of Object.entries(BUTTON)) {
      for (const g of GRUND) {
        // Exempt (disabled, ghost-disabled): nur prüfen, dass die Token existieren, keine Ratio-Assertion.
        if (k.exempt) {
          if (k.fill) assert.ok(C[k.fill], `${name}: Token ${k.fill} fehlt`);
          assert.ok(C[k.text], `${name}: Token ${k.text} fehlt`);
          if (k.line) assert.ok(C[k.line], `${name}: Token ${k.line} fehlt`);
          continue;
        }
        const flaeche = k.fill ? C[k.fill] : C[g];
        assert.ok(C[k.text], `${name}: Token ${k.text} fehlt`);
        const t = ratio(C[k.text], flaeche);
        assert.ok(t >= AA_TEXT, `${mode} ${name} auf ${g}: Text ${k.text} ${t.toFixed(2)} < 4,5`);
        if (k.line) {
          const l = ratio(C[k.line], C[g]);
          assert.ok(l >= AA_NON_TEXT, `${mode} ${name} auf ${g}: Linie ${k.line} ${l.toFixed(2)} < 3,0`);
        }
        if (k.fill && !k.line) {
          // Eine gefüllte Fläche ohne Linie muss sich vom Grund abheben, sonst ist der Button unsichtbar.
          const f = ratio(C[k.fill], C[g]);
          assert.ok(f >= AA_NON_TEXT || /hover|active|pressed/.test(name), `${mode} ${name} auf ${g}: Fläche ${k.fill} ${f.toFixed(2)} < 3,0`);
        }
      }
    }
  });
}

test('disabled liest die drei Disabled-Rollen und sonst nichts', () => {
  const css = readFileSync(new URL('../src/components/button/button.css', import.meta.url), 'utf8');
  const block = /\.tl-button:disabled[^{]*\{([^}]*)\}/.exec(css);
  assert.ok(block, ':disabled-Regel fehlt');
  const vars = [...block[1].matchAll(/var\((--tl-[a-z0-9-]+)\)/g)].map((m) => m[1]).sort();
  assert.deepEqual(vars, ['--tl-border-disabled', '--tl-surface-disabled', '--tl-text-disabled'].sort());
});

test('beschäftigt steht nach disabled und pulsiert: ein hochladender Knopf ist beides', () => {
  const css = readFileSync(new URL('../src/components/button/button.css', import.meta.url), 'utf8')
    .replace(/\/\*[\s\S]*?\*\//g, '');
  const busy = css.indexOf('.tl-button[aria-busy="true"] {');
  const disabled = css.indexOf('.tl-button:disabled,');
  assert.ok(busy >= 0 && disabled >= 0, 'Regel für aria-busy oder :disabled fehlt');
  assert.ok(busy > disabled, 'aria-busy muss nach :disabled stehen, sonst verliert cursor: progress');
  assert.match(css, /\.tl-button\[aria-busy="true"\] \.tl-button__icon\s*\{[^}]*animation:\s*tl-button-busy\b/);
  assert.match(css, /@keyframes tl-button-busy\s*\{/);
});

test('deaktiviert bleibt zeigbar: der Tooltip nennt den Grund (#29675)', () => {
  const css = readFileSync(new URL('../src/components/button/button.css', import.meta.url), 'utf8')
    .replace(/\/\*[\s\S]*?\*\//g, '');
  assert.doesNotMatch(css, /pointer-events:\s*none/, 'ein deaktivierter Knopf muss den Zeiger empfangen');
  // Zeigen und Drücken ändern einen deaktivierten Knopf nicht.
  assert.match(css, /\.tl-button:disabled,\s*\.tl-button:disabled:hover,\s*\.tl-button:disabled:active\s*\{/);
  // Über einem deaktivierten Knopf bleibt der Zeiger default, auch in aria-busy: der Puls ist dort
  // der einzige Hinweis (docs/skalen.md, Busy).
  assert.match(css, /\.tl-button:disabled:active\s*\{[^}]*cursor:\s*default/);
});

test('das Button-Specimen zeigt jede Erscheinung, die Tönung, sm, und jeden Zustand', () => {
  const html = readFileSync(new URL('../src/components/button/button.spec.html', import.meta.url), 'utf8');
  for (const m of ['primary', 'secondary', 'ghost', 'link', 'danger', 'sm'])
    assert.ok(html.includes(`tl-button--${m}`), `Modifier ${m} fehlt`);
  for (const attr of ['disabled', 'aria-pressed="true"', 'aria-busy="true"', 'aria-label='])
    assert.ok(html.includes(attr), `Zustand ${attr} fehlt`);
  assert.ok(html.includes('tl-button__icon') && html.includes('tl-button__label'), 'Elemente fehlen');
  assert.ok(html.includes('id="mode"') && html.includes('id="density"'), 'Schalter für Modus und Dichte fehlen');
  assert.ok(html.includes('/dist/components.css'), 'lädt components.css nicht');
  assert.ok(!/style="/.test(html.replace(/<style>[\s\S]*?<\/style>/, '')), 'Inline-Style im Specimen');
});

test('jedes Farb-Token in button.css kommt in der BUTTON-Map vor', () => {
  const css = readFileSync(new URL('../src/components/button/button.css', import.meta.url), 'utf8');
  const farbTokens = new Set(
    [...css.matchAll(/var\((--tl-(?:surface|text|icon|border|status)-[a-z0-9-]+)\)/g)]
      .map((m) => m[1])
      .filter((t) => !/^--tl-border-width-/.test(t)) // Maß-Rolle, kein Farbwert
  );
  const belegt = new Set();
  for (const k of Object.values(BUTTON)) {
    for (const rolle of [k.fill, k.text, k.line]) if (rolle) belegt.add(rolle);
  }
  for (const t of farbTokens) {
    const rolle = t.replace(/^--tl-/, '');
    assert.ok(belegt.has(rolle), `${t} kommt in keinem BUTTON-Map-Eintrag vor`);
  }
});

test('der Vertrag nennt jedes Token, das button.css liest, und jedes Zustandsattribut', () => {
  const css = readFileSync(new URL('../src/components/button/button.css', import.meta.url), 'utf8');
  const md = readFileSync(new URL('../src/components/button/button.contract.md', import.meta.url), 'utf8');
  const tokens = new Set([...css.matchAll(/var\((--tl-[a-z0-9-]+)\)/g)].map((m) => m[1]));
  for (const t of tokens) assert.ok(md.includes('`' + t + '`'), `Vertrag nennt ${t} nicht`);
  for (const a of ['disabled', 'aria-pressed', 'aria-busy', 'aria-label', 'focus-visible'])
    assert.ok(md.includes(a), `Vertrag nennt ${a} nicht`);
  for (const h of ['## Zustand', '## Aktionen', '## Tokens', '## Attribute', '## Konformität'])
    assert.ok(md.includes(h), `Abschnitt ${h} fehlt`);
});

// --- Pill --------------------------------------------------------------------------------------
// Jede Rolle liest eine getönte Fläche und eine Linie; der Text darauf ist gewöhnlicher Text.
// skalen.md „Pill: Rolle statt Farbe“.
const PILL = {
  neutral:  { fill: 'surface-interactive-selected', line: 'border-control' },
  brand:    { fill: 'surface-brand-subtle', line: 'border-interactive' },
  error:    { fill: 'status-error-subtle', line: 'status-error-border' },
  warning:  { fill: 'status-warning-subtle', line: 'status-warning-border' },
  success:  { fill: 'status-success-subtle', line: 'status-success-border' },
  info:     { fill: 'status-info-subtle', line: 'status-info-border' },
  ...Object.fromEntries([1, 2, 3, 4, 5, 6, 7, 8].map((k) => [`category-${k}`, { fill: `category-${k}-subtle`, line: `category-${k}-border` }])),
};

for (const mode of ['light', 'dark']) {
  const C = { ...R.color[mode], ...R.derived[mode] };
  test(`Pill ${mode}: Text 4,5 auf jeder Fläche, Linie 3,0 gegen base und layer, in jeder Rolle`, () => {
    for (const [name, k] of Object.entries(PILL)) {
      assert.ok(C[k.fill], `${name}: Token ${k.fill} fehlt`);
      assert.ok(C[k.line], `${name}: Token ${k.line} fehlt`);
      const t = ratio(C['text-primary'], C[k.fill]);
      assert.ok(t >= AA_TEXT, `${mode} ${name}: text-primary auf ${k.fill} ${t.toFixed(2)} < 4,5`);
      for (const g of GRUND) {
        const l = ratio(C[k.line], C[g]);
        assert.ok(l >= AA_NON_TEXT, `${mode} ${name} auf ${g}: Linie ${k.line} ${l.toFixed(2)} < 3,0`);
      }
    }
  });
}

test('pill.css liest je Rolle genau die Fläche und die Linie der PILL-Map, und Text nur als text-primary', () => {
  const css = readGenerated('pill', 'pill.css'); // mit den erzeugten Rollenregeln (@tl-roles)
  const farbTokens = new Set([...css.matchAll(/var\((--tl-(?:surface|text|icon|border|status|category)-[a-z0-9-]+)\)/g)]
    .map((m) => m[1]).filter((t) => !/^--tl-border-width-/.test(t)));
  const belegt = new Set(['text-primary']);
  for (const k of Object.values(PILL)) { belegt.add(k.fill); belegt.add(k.line); }
  for (const t of farbTokens) assert.ok(belegt.has(t.replace(/^--tl-/, '')), `${t} kommt in keinem PILL-Map-Eintrag vor`);
  for (const rolle of belegt) assert.ok(farbTokens.has('--tl-' + rolle), `pill.css liest ${rolle} nicht`);
  assert.ok(!/color-mix|#[0-9a-fA-F]{3}/.test(css), 'kein color-mix, kein Literal');
  for (const k of Object.keys(PILL).filter((n) => n !== 'neutral'))
    assert.ok(css.includes(`.tl-pill[data-tl-role="${k}"]`), `Rolle ${k} fehlt`);
});

test('das Pill-Specimen zeigt jede Rolle, Icon und Beschriftung, auf beiden Flächen', () => {
  const html = readFileSync(new URL('../src/components/pill/pill.spec.html', import.meta.url), 'utf8');
  // Name und Rolle stehen als Paar in VARIANTEN (['brand', 'brand', 'Eigenes']): an der alten Datei
  // (['brand', 'tl-pill--brand', …]) greift die Prüfung nicht.
  for (const k of Object.keys(PILL).filter((n) => n !== 'neutral'))
    assert.ok(html.includes(`['${k}', '${k}',`), `Rolle ${k} fehlt`);
  assert.ok(html.includes('data-tl-role="%ROLE%"'), 'Specimen setzt die Rolle nicht als Attribut');
  assert.doesNotMatch(html, /tl-pill--/, 'Specimen zeigt noch einen Rollen-Modifier');
  assert.ok(html.includes('tl-pill__icon') && html.includes('tl-pill__label'), 'Elemente fehlen');
  assert.ok(html.includes('id="on-layer"') && html.includes('id="on-base"'), 'beide Flächen fehlen');
  assert.ok(html.includes('id="mode"') && html.includes('id="density"'), 'Schalter für Modus und Dichte fehlen');
  assert.ok(html.includes('/dist/components.css'), 'lädt components.css nicht');
  assert.ok(!/style="/.test(html.replace(/<style>[\s\S]*?<\/style>/, '')), 'Inline-Style im Specimen');
});

test('der Pill-Vertrag nennt jedes Token, das pill.css liest, und jede Rolle', () => {
  const css = readGenerated('pill', 'pill.css'); // mit den erzeugten Rollenregeln (@tl-roles)
  const md = readFileSync(new URL('../src/components/pill/pill.contract.md', import.meta.url), 'utf8');
  for (const t of new Set([...css.matchAll(/var\((--tl-[a-z0-9-]+)\)/g)].map((m) => m[1])))
    assert.ok(md.includes('`' + t + '`'), `Vertrag nennt ${t} nicht`);
  for (const k of Object.keys(PILL).filter((n) => !n.startsWith('category-')))
    assert.ok(md.includes('`' + k + '`'), `Vertrag nennt Rolle ${k} nicht`);
  for (const h of ['## Zustand', '## Aktionen', '## Tokens', '## Attribute', '## Konformität'])
    assert.ok(md.includes(h), `Abschnitt ${h} fehlt`);
});

// --- Avatar ------------------------------------------------------------------------------------
// Die Farbe ist eine Kategorie aus dem Namen; ohne Namen die neutrale Fläche. Dieselben zwei Tokens je
// Rolle wie bei der Pill, Initialen in text-primary. skalen.md „Avatar: die Farbe ist eine Kategorie“.
const AVATAR = {
  neutral: PILL.neutral,
  ...Object.fromEntries([1, 2, 3, 4, 5, 6, 7, 8].map((k) => [`category-${k}`, PILL[`category-${k}`]])),
};
const AVATAR_SIZES = { sm: 'size-6', md: 'size-7', lg: 'size-8', xl: 'size-9' };
// Externe Namen (XML, Java, Zustand) → Skalenkürzel der Klasse. skalen.md „Zwei Namensräume für die Größen“.
const AVATAR_SIZE_NAMES = { small: 'sm', medium: 'md', large: 'lg', 'x-large': 'xl' };

for (const mode of ['light', 'dark']) {
  const C = { ...R.color[mode], ...R.derived[mode] };
  test(`Avatar ${mode}: Initialen 4,5 auf jeder Fläche, Linie 3,0 gegen base und layer`, () => {
    for (const [name, k] of Object.entries(AVATAR)) {
      const t = ratio(C['text-primary'], C[k.fill]);
      assert.ok(t >= AA_TEXT, `${mode} ${name}: text-primary auf ${k.fill} ${t.toFixed(2)} < 4,5`);
      for (const g of GRUND) {
        const l = ratio(C[k.line], C[g]);
        assert.ok(l >= AA_NON_TEXT, `${mode} ${name} auf ${g}: Linie ${k.line} ${l.toFixed(2)} < 3,0`);
      }
    }
  });
}

test('die vier Avatar-Größen sind Maß-Tokens der Skala, 24/32/48/64 px, und folgen nicht der Dichte', () => {
  for (const [s, prim] of Object.entries(AVATAR_SIZES)) {
    assert.equal(T.measure[`size-avatar-${s}`], `{${prim}}`, `size-avatar-${s} liest nicht ${prim}`);
  }
  assert.deepEqual(Object.values(AVATAR_SIZES).map((p) => parseFloat(P.measure[p]) * 16), [24, 32, 48, 64]);
  for (const d of Object.values(T.density)) assert.ok(!Object.keys(d).some((k) => k.startsWith('size-avatar')), 'Avatar-Größe in der Dichte');
});

test('avatar.css liest je Rolle genau Fläche und Linie der AVATAR-Map, Text nur als text-primary, Größen nur aus size-avatar-*', () => {
  const css = readGenerated('avatar', 'avatar.css'); // mit den erzeugten Rollenregeln (@tl-roles)
  const farbTokens = new Set([...css.matchAll(/var\((--tl-(?:surface|text|icon|border|status|category)-[a-z0-9-]+)\)/g)]
    .map((m) => m[1]).filter((t) => !/^--tl-border-width-/.test(t)));
  const belegt = new Set(['text-primary']);
  for (const k of Object.values(AVATAR)) { belegt.add(k.fill); belegt.add(k.line); }
  for (const t of farbTokens) assert.ok(belegt.has(t.replace(/^--tl-/, '')), `${t} kommt in keinem AVATAR-Map-Eintrag vor`);
  for (const rolle of belegt) assert.ok(farbTokens.has('--tl-' + rolle), `avatar.css liest ${rolle} nicht`);
  assert.ok(!/color-mix|#[0-9a-fA-F]{3}|hsl\(/.test(css), 'kein color-mix, kein Literal, kein hsl');
  for (const k of Object.keys(AVATAR).filter((n) => n !== 'neutral')) assert.ok(css.includes(`.tl-avatar[data-tl-role="${k}"]`), `Modifier --${k} fehlt`);
  for (const s of ['sm', 'lg', 'xl']) assert.ok(css.includes(`.tl-avatar--${s}`), `Größe --${s} fehlt`);
  const masse = [...css.matchAll(/(?:width|height):\s*var\((--tl-[a-z0-9-]+)\)/g)].map((m) => m[1]);
  assert.ok(masse.length >= 8 && masse.every((m) => /^--tl-size-avatar-(sm|md|lg|xl)$/.test(m)), `Maße nur aus size-avatar-*: ${masse}`);
  assert.ok(!/font-size|font-weight|letter-spacing/.test(css), 'Typo kommt von der Klasse, nicht aus avatar.css');
  assert.ok(css.includes('--tl-radius-round'), 'Form ist radius-round');
});

test('das Avatar-Specimen zeigt jede Kategorie, jede Größe, ohne Namen und mit Bild, auf beiden Flächen', () => {
  const html = readFileSync(new URL('../src/components/avatar/avatar.spec.html', import.meta.url), 'utf8');
  for (let k = 1; k <= 8; k++) assert.ok(html.includes(`category-${k}`), `Kategorie ${k} fehlt`);
  for (const s of ['sm', 'lg', 'xl']) assert.ok(html.includes(`tl-avatar--${s}`), `Größe ${s} fehlt`);
  for (const t of ['tl-type-label-strong', 'tl-type-body-strong', 'tl-type-heading-md', 'tl-type-heading-lg']) assert.ok(html.includes(t), `Typo ${t} fehlt`);
  assert.ok(html.includes('tl-avatar__image') && html.includes('aria-hidden="true"') && html.includes('role="img"'), 'Bild, leer oder role fehlen');
  assert.ok(html.includes('id="on-layer"') && html.includes('id="on-base"'), 'beide Flächen fehlen');
  assert.ok(html.includes('id="mode"') && html.includes('id="density"'), 'Schalter für Modus und Dichte fehlen');
  assert.ok(html.includes('/dist/components.css'), 'lädt components.css nicht');
  assert.ok(!/style="/.test(html.replace(/<style>[\s\S]*?<\/style>/, '')), 'Inline-Style im Specimen');
});

test('der Avatar-Vertrag nennt jedes Token, das avatar.css liest, jede Größe und die Attribute', () => {
  const css = readGenerated('avatar', 'avatar.css'); // mit den erzeugten Rollenregeln (@tl-roles)
  const md = readFileSync(new URL('../src/components/avatar/avatar.contract.md', import.meta.url), 'utf8');
  for (const t of new Set([...css.matchAll(/var\((--tl-[a-z0-9-]+)\)/g)].map((m) => m[1])))
    assert.ok(md.includes('`' + t + '`'), `Vertrag nennt ${t} nicht`);
  // Extern Wörter, im CSS die Skalenkürzel; der Vertrag nennt beide und die Abbildung. medium hat keinen Modifier.
  for (const [wort, kuerzel] of Object.entries(AVATAR_SIZE_NAMES)) {
    assert.ok(md.includes('`' + wort + '`'), `Vertrag nennt Größe ${wort} nicht`);
    if (kuerzel !== 'md') assert.ok(md.includes('`' + wort + '` → `tl-avatar--' + kuerzel + '`'), `Vertrag bildet ${wort} nicht auf tl-avatar--${kuerzel} ab`);
  }
  for (const a of ['role="img"', 'aria-label', 'aria-hidden', 'alt']) assert.ok(md.includes(a), `Vertrag nennt ${a} nicht`);
  for (const h of ['## Zustand', '## Aktionen', '## Tokens', '## Attribute', '## Konformität'])
    assert.ok(md.includes(h), `Abschnitt ${h} fehlt`);
});

// --- Progress ----------------------------------------------------------------------------------
// Spur ist die vertiefte Fläche, Füllung die Marke; keine Tönung. Unbestimmt ist ein Zustand.
// skalen.md „Progress: ein Anteil, kein Urteil“.
const PROGRESS = { track: 'surface-interactive-selected', fill: 'surface-brand', label: 'text-secondary' };

for (const mode of ['light', 'dark']) {
  const C = { ...R.color[mode], ...R.derived[mode] };
  test(`Progress ${mode}: Füllung 3,0 gegen die Spur, Spur sichtbar auf base und layer, Beschriftung 4,5`, () => {
    const f = ratio(C[PROGRESS.fill], C[PROGRESS.track]);
    assert.ok(f >= AA_NON_TEXT, `${mode}: Füllung ${PROGRESS.fill} auf Spur ${PROGRESS.track} ${f.toFixed(2)} < 3,0`);
    for (const g of GRUND) {
      assert.ok(C[PROGRESS.track] !== C[g], `${mode}: Spur fällt mit ${g} zusammen`);
      const t = ratio(C[PROGRESS.label], C[g]);
      assert.ok(t >= AA_TEXT, `${mode} auf ${g}: Beschriftung ${t.toFixed(2)} < 4,5`);
    }
  });
}

test('progress.css liest Spur, Füllung und Beschriftung der PROGRESS-Map und sonst keine Farbe; keine Breite, keine eigene Variable', () => {
  const css = readFileSync(new URL('../src/components/progress/progress.css', import.meta.url), 'utf8');
  const farbTokens = new Set([...css.matchAll(/var\((--tl-(?:surface|text|icon|border(?!-width)|status|category)-[a-z0-9-]+)\)/g)].map((m) => m[1]));
  assert.deepEqual([...farbTokens].sort(), Object.values(PROGRESS).map((r) => '--tl-' + r).sort());
  assert.ok(!/--tl-progress-[a-z]+:/.test(css), 'keine komponenteneigene Variable');
  assert.ok(!/^\.tl-progress__fill\s*\{[^}]*\bwidth:/m.test(css), 'die Breite der Füllung ist Daten und kommt vom Control');
  assert.ok(css.includes('[aria-busy="true"]') && !/tl-progress--/.test(css), 'unbestimmt ist ein Attribut, kein Modifier');
  assert.ok(css.includes('var(--tl-duration-loop)') && !/\d+(\.\d+)?m?s\b/.test(css.replace(/var\([^)]*\)/g, '')), 'Schwung nur aus duration-loop');
  assert.ok(!/prefers-reduced-motion|font-size|font-weight/.test(css), 'kein eigener Motion-Schalter, keine Schriftregel');
  assert.ok(css.includes('--tl-size-progress') && css.includes('--tl-radius-full'), 'Höhe und Form aus der Skala');
});

test('duration-loop ist die vierte Dauer, nur für Endlos-Animationen, und size-progress liest size-2', () => {
  assert.equal(T.measure['duration-loop'], '{dur-4}');
  assert.equal(parseFloat(P.motion['dur-4']), 1200);
  assert.equal(T.measure['size-progress'], '{size-2}');
});

test('das Progress-Specimen zeigt Anteile, unbestimmt, vom Ort benannt und eine schmale Zelle, auf beiden Flächen', () => {
  const html = readFileSync(new URL('../src/components/progress/progress.spec.html', import.meta.url), 'utf8');
  for (const s of ['role="progressbar"', 'aria-valuemin="0"', 'aria-valuemax="100"', 'aria-valuenow=', 'aria-busy="true"'])
    assert.ok(html.includes(s), `${s} fehlt`);
  // Fix-Welle 3b (2): jeder beschriftete Balken ist über aria-labelledby nach seiner Beschriftung benannt.
  // 09.10.: und trägt keinen Wertetext — er wäre derselbe Text wie der Name und würde doppelt angesagt.
  assert.doesNotMatch(html, /aria-valuetext/, 'Wertetext neben dem Namen');
  const bars = html.match(/<div class="tl-progress"[^>]*>[\s\S]*?<\/div>/g) ?? [];
  assert.ok(bars.some((b) => b.includes('tl-progress__label')), 'kein beschrifteter Balken');
  for (const bar of bars.filter((b) => b.includes('tl-progress__label')))
    assert.match(bar, /aria-labelledby="[^"]+"/, `beschrifteter Balken ohne Namen: ${bar}`);
  // 09.10. (Hendrik): jeder Balken ist benannt; ist die Beschriftung nur der Wert, gibt der Ort den Namen, und
  // die Beschriftung ist aria-hidden — den Wert sagt aria-valuenow.
  for (const bar of bars) {
    const name = bar.match(/aria-labelledby="([^"]+)"/)?.[1];
    assert.ok(name, `Balken ohne Namen: ${bar.slice(0, 120)}`);
    const label = bar.match(/<span class="tl-progress__label[^>]*>/)?.[0];
    if (label && !label.includes(`id="${name}"`)) {
      assert.match(label, /aria-hidden="true"/, `Wert-Beschriftung nicht aria-hidden: ${label}`);
      assert.ok(html.includes(`id="${name}"`), `Name ${name} steht nicht am Ort`);
    }
  }
  assert.ok(bars.some((b) => /aria-labelledby="([^"]+)"/.test(b) && /tl-progress__label[^>]*aria-hidden="true"/.test(b)),
    'kein Balken mit Namen vom Ort');
  for (const c of ['tl-progress__track', 'tl-progress__fill', 'tl-progress__label', 'tl-nums']) assert.ok(html.includes(c), `${c} fehlt`);
  assert.ok(html.includes(".style.width = Math.round("), 'Breite nicht als Daten gesetzt');
  assert.ok(html.includes('id="on-layer"') && html.includes('id="on-base"'), 'beide Flächen fehlen');
  assert.ok(html.includes('id="mode"') && html.includes('id="density"'), 'Schalter für Modus und Dichte fehlen');
  assert.ok(html.includes('/dist/components.css'), 'lädt components.css nicht');
  assert.ok(!/style="/.test(html.replace(/<style>[\s\S]*?<\/style>/, '')), 'Inline-Style im Specimen');
});

test('der Progress-Vertrag nennt jedes Token, das progress.css liest, die Custom Property und die Attribute', () => {
  const css = readFileSync(new URL('../src/components/progress/progress.css', import.meta.url), 'utf8');
  const md = readFileSync(new URL('../src/components/progress/progress.contract.md', import.meta.url), 'utf8');
  for (const t of new Set([...css.matchAll(/var\((--tl-[a-z0-9-]+)\)/g)].map((m) => m[1])))
    assert.ok(md.includes('`' + t + '`'), `Vertrag nennt ${t} nicht`);
  for (const a of ['role="progressbar"', 'aria-valuemin', 'aria-valuemax', 'aria-valuenow', 'aria-valuetext', 'aria-labelledby', 'aria-hidden', 'aria-busy', 'style="width:'])
    assert.ok(md.includes(a), `Vertrag nennt ${a} nicht`);
  for (const h of ['## Zustand', '## Aktionen', '## Tokens', '## Attribute', '## Konformität'])
    assert.ok(md.includes(h), `Abschnitt ${h} fehlt`);
});

// --- Icon --------------------------------------------------------------------------------------
// Das Icon hat kein eigenes Stylesheet: die vier Stufen stehen in base.css. skalen.md „Icons“.
const ICON_SIZES = { sm: 'size-4', md: 'size-5', lg: 'size-6', glyph: 'size-3' };
// Externe Namen (XML, Java, Zustand) → Skalenkürzel der Klasse. skalen.md „Zwei Namensräume für die Größen“.
const ICON_SIZE_NAMES = { small: 'sm', medium: 'md', large: 'lg', glyph: 'glyph' };

test('die vier Icon-Stufen stehen als Klassen in base.css, font-size nur dort, und lesen size-icon-*', () => {
  const base = readFileSync(new URL('../dist/base.css', import.meta.url), 'utf8');
  for (const [s, prim] of Object.entries(ICON_SIZES)) {
    assert.equal(T.measure[`size-icon-${s}`], `{${prim}}`);
    assert.ok(new RegExp(`\\.tl-icon-${s}\\s*\\{\\s*font-size: var\\(--tl-size-icon-${s}\\);`).test(base), `.tl-icon-${s} fehlt oder liest nicht size-icon-${s}`);
  }
  assert.ok(/\.tl-icon-glyph, \.tl-icon-sm, \.tl-icon-md, \.tl-icon-lg \{[^}]*line-height: 1;[^}]*width: 1em;[^}]*height: 1em;/.test(base), 'line-height 1 und 1-em-Quadrat fehlen');
  const comps = readFileSync(new URL('../dist/components.css', import.meta.url), 'utf8');
  assert.ok(!/font-size/.test(comps), 'font-size in components.css — Icon- und Typo-Größen kommen aus base.css');
});

test('das Icon-Specimen zeigt jede Stufe, das Quadrat in einer Spalte und ein Bild-Icon ohne Inline-Breite', () => {
  const html = readFileSync(new URL('../src/components/icon/icon.spec.html', import.meta.url), 'utf8');
  for (const s of Object.keys(ICON_SIZES)) assert.ok(html.includes(`tl-icon-${s}`), `Stufe ${s} fehlt`);
  assert.ok(html.includes('role="img"') && html.includes('aria-hidden="true"') && html.includes('<img class="tl-icon-sm" alt=""'), 'Attribute oder Bild-Icon fehlen');
  assert.ok(html.includes('id="mode"') && html.includes('id="density"'), 'Schalter fehlen');
  assert.ok(!/style="/.test(html.replace(/<style>[\s\S]*?<\/style>/, '')), 'Inline-Style im Specimen');
});

test('der Icon-Vertrag nennt die vier Klassen, die vier Tokens und die Attribute', () => {
  const md = readFileSync(new URL('../src/components/icon/icon.contract.md', import.meta.url), 'utf8');
  for (const s of Object.keys(ICON_SIZES)) {
    assert.ok(md.includes(`.tl-icon-${s}`), `Vertrag nennt .tl-icon-${s} nicht`);
    assert.ok(md.includes(`--tl-size-icon-${s}`), `Vertrag nennt --tl-size-icon-${s} nicht`);
  }
  for (const [wort, kuerzel] of Object.entries(ICON_SIZE_NAMES))
    assert.ok(md.includes('`' + wort + '` → `.tl-icon-' + kuerzel + '`'), `Vertrag bildet ${wort} nicht auf .tl-icon-${kuerzel} ab`);
  for (const a of ['role="img"', 'aria-label', 'aria-hidden', 'alt=""']) assert.ok(md.includes(a), `Vertrag nennt ${a} nicht`);
  for (const h of ['## Zustand', '## Aktionen', '## Tokens', '## Attribute', '## Konformität']) assert.ok(md.includes(h), `Abschnitt ${h} fehlt`);
});

test('Toggle: offen dreht das Icon, Reduced Motion gilt über base.css', () => {
  const css = readGenerated('toggle', 'toggle.css');
  assert.match(css, /\.tl-toggle\[aria-expanded="true"\] > \.tl-icon-sm\s*\{[^}]*rotate:\s*90deg/);
  assert.match(css, /transition:\s*rotate var\(--tl-duration-fast\) var\(--tl-easing-standard\)/);
});

test('Schrift-Reset nur in :where — jede tl-type-*-Klasse am Element gewinnt', () => {
  // Der font-Shorthand setzt Gewicht, Größe und Zeilenhöhe zurück. In einem Komponentenblock hat er
  // dieselbe Spezifität wie die Typo-Klasse und lädt nach base.css — er schlüge sie.
  const out = [];
  for (const [name, css] of componentCssFiles())
    for (const d of declarations(css))
      if (d.property === 'font' && !d.selector.startsWith(':where(')) out.push(`${name}:${d.line} ${d.selector}`);
  assert.deepEqual(out, []);
});

test('Toolbar, Tabs, Breadcrumb lesen nur Tokens und kennen keine Zustandsklasse', () => {
  for (const [dir, file] of [['toolbar', 'toolbar.css'], ['tabs', 'tabs.css'], ['breadcrumb', 'breadcrumb.css']]) {
    assert.doesNotMatch(readGenerated(dir, file), /--(active|compact|collapsible)\b/, `${file}: Zustand als Klasse`);
  }
  const tabs = readGenerated('tabs', 'tabs.css');
  const aktiv = declarationsOf(tabs, '.tl-tabs__tab[aria-selected="true"]');
  assert.match(aktiv, /border-color:\s*var\(--tl-border-interactive\)/);
  assert.doesNotMatch(aktiv, /font-weight/);
  assert.match(declarationsOf(tabs, '.tl-tabs__tab'), /min-height:\s*var\(--tl-size-bar\)/);
  assert.match(declarationsOf(readGenerated('toolbar', 'toolbar.css'), '.tl-toolbar'), /min-height:\s*var\(--tl-size-bar\)/);
  assert.match(declarationsOf(tabs, '.tl-tabs__panel:not(.tl-region)'), /flex-direction:\s*column/);
  assert.match(declarationsOf(tabs, '.tl-tabs__panel[hidden]'), /display:\s*none/);
});

// Ticket #29983: Reiter breiter als ihr Behälter scrollen in der Reiterreihe, nicht im Vorfahren; die Linie und die
// Scrollknöpfe gehören der Leiste, damit beides beim Scrollen stehen bleibt.
test('Tabs: die Reiterreihe scrollt in sich, die Leiste trägt Linie und Knöpfe', () => {
  const tabs = readGenerated('tabs', 'tabs.css');
  const list = declarationsOf(tabs, '.tl-tabs__list');
  assert.match(list, /overflow-x:\s*auto/);
  assert.match(list, /scrollbar-width:\s*none/);
  assert.match(list, /min-width:\s*0/);
  assert.doesNotMatch(list, /border-bottom/, 'die Linie liefe mit den Reitern weg');
  assert.match(declarationsOf(tabs, '.tl-tabs') ?? '', /min-width:\s*0/);
  const strip = declarationsOf(tabs, '.tl-tabs__strip') ?? '';
  assert.match(strip, /min-width:\s*0/);
  assert.match(strip, /border-bottom:\s*var\(--tl-border-width-sm\) solid var\(--tl-border-separator\)/);
  assert.match(declarationsOf(tabs, '.tl-tabs__scroll') ?? '', /flex-shrink:\s*0/);
  // Am Ende ohne Weiteres unsichtbar, aber mit Platz: Scrollen ändert die Breite der Reiterreihe nie.
  const amEnde = declarationsOf(tabs, '.tl-tabs__scroll:disabled') ?? '';
  assert.match(amEnde, /visibility:\s*hidden/);
  assert.doesNotMatch(amEnde, /display/);
  // Die Reiterreihe schneidet: ein Ring außen wäre abgeschnitten.
  assert.match(declarationsOf(tabs, '.tl-tabs__tab:focus-visible') ?? '', /outline-offset:\s*calc\(-1 \* var\(--tl-focus-offset\)\)/);
});

// Entscheidung 07.10. (Hendrik): Zeigen und Drücken des Auf-/Zuklappers sind der durchscheinende Schleier,
// überall (Formulargruppe, Tabellen- und Baumzeile) — auf einem gezeigten oder ausgewählten Grund bleibt er sichtbar.
test('tl-toggle zeigt und drückt mit dem Schleier, deaktiviert ohne', () => {
  const css = readGenerated('toggle', 'toggle.css');
  assert.match(declarationsOf(css, '.tl-toggle:hover') ?? '', /background:\s*var\(--tl-surface-state-hover\)/);
  assert.match(declarationsOf(css, '.tl-toggle:active') ?? '', /background:\s*var\(--tl-surface-state-active\)/);
  assert.match(declarationsOf(css, '.tl-toggle:disabled') ?? '', /background:\s*transparent/);
  assert.doesNotMatch(css.replace(/\/\*[\s\S]*?\*\//g, ''), /surface-interactive-(hover|active)/, 'keine deckende Stufe');
});
