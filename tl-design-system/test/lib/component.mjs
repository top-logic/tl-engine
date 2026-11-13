// Gemeinsame Helfer der Komponententests: lesen, was eine Komponente liest, zeigt und verspricht.
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { ratio } from '../../tools/lib/contrast.mjs';
import { componentSources, expandRoles } from '../../tools/generate.mjs';

const TOKENS = JSON.parse(readFileSync(new URL('../../src/tokens.json', import.meta.url), 'utf8'));

export const readComponent = (dir, file) =>
  readFileSync(new URL(`../../src/components/${dir}/${file}`, import.meta.url), 'utf8');

export const varsOf = (css) => new Set([...css.matchAll(/var\((--tl-[a-z0-9-]+)\)/g)].map((m) => m[1]));

export const colorVarsOf = (css) => [...varsOf(css)]
  .map((v) => v.replace(/^--tl-/, ''))
  .filter((v) => /^(surface|text|icon|border|status|focus|category)-/.test(v) && !/^border-width-/.test(v))
  .sort();

export function declarationsOf(css, selector) {
  // Kommentare zuerst weg — sonst hängt der Kopfkommentar der Datei vor dem ersten Selektor.
  const ohne = css.replace(/\/\*[\s\S]*?\*\//g, '');
  for (const m of ohne.matchAll(/([^{}]+)\{([^}]*)\}/g)) {
    // Nur Kommata außerhalb von Klammern trennen Selektoren: `:not(:disabled, [readonly])` ist einer.
    const selectors = m[1].split(/,(?![^(]*\))/).map((s) => s.trim());
    if (selectors.includes(selector)) return m[2];
  }
  return null;
}

export function specimenProblems(html) {
  const p = [];
  if (!html.includes('id="mode"')) p.push('Schalter id="mode" fehlt');
  if (!html.includes('id="density"')) p.push('Schalter id="density" fehlt');
  if (!html.includes('/dist/components.css')) p.push('lädt /dist/components.css nicht');
  const ohne = html.replace(/<style>[\s\S]*?<\/style>/g, '').replace(/<script>[\s\S]*?<\/script>/g, '');
  if (/style="/.test(ohne)) p.push('Inline-Style im Specimen');
  return p;
}

const escapeRegExp = (s) => s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');

/** Ein Begriff gilt als genannt, wenn er in einem Codespan als ganzes Wort steht: davor Spananfang oder ein Zeichen
    außerhalb [A-Za-z0-9_-], danach Spanende oder ein solches Zeichen. `tl-picker` in `tl-picker__actions` zählt nicht,
    `tl-menu` in `tl-popover tl-menu` zählt. Spans dürfen über Zeilen gehen. */
export function namedInContract(md, term) {
  const re = new RegExp('(?<![A-Za-z0-9_-])' + escapeRegExp(term) + '(?![A-Za-z0-9_-])');
  for (const m of md.matchAll(/`([^`]*)`/g)) if (re.test(m[1])) return true;
  return false;
}

export const missingInContract = (md, names) => names.filter((n) => !namedInContract(md, n));

export function contrastProblems(R, mode, pairs) {
  const C = { ...R.color[mode], ...R.derived[mode] };
  const out = [];
  for (const { fg, bg, soll } of pairs) {
    if (!C[fg] || !C[bg]) { out.push(`${mode}: ${!C[fg] ? fg : bg} fehlt`); continue; }
    const r = ratio(C[fg], C[bg]);
    if (r < soll) out.push(`${mode}: ${fg} auf ${bg} ${r.toFixed(2)} < ${soll}`);
  }
  return out;
}

/** Alle CSS-Dateien unter src/components/, rekursiv: [Name relativ mit '/', Inhalt]. */
export function* componentCssFiles() {
  const root = new URL('../../src/components/', import.meta.url);
  for (const c of componentSources(root)) yield [c.name, c.css];
}

/** Die Regeln einer Datei ohne Kommentare: [Selektorliste, Deklarationsblock]. Verschachtelte Blöcke
    (@media) liefern ihre inneren Regeln. */
export function rulesOf(css) {
  const ohne = css.replace(/\/\*[\s\S]*?\*\//g, '');
  return [...ohne.matchAll(/([^{};]+)\{([^{}]*)\}/g)].map((m) => [m[1].trim(), m[2]]);
}

/** Die Deklarationen aller Regeln, deren Selektorliste `selector` enthält (auch in @media), mit `;` verbunden.
    Es muss mindestens eine Regel geben: wer „darin steht X nicht“ prüft, prüft zuerst, dass die Regel da ist. */
export function declarationsOfAll(css, selector) {
  // Nur Kommata außerhalb von Klammern trennen Selektoren, wie in declarationsOf.
  const gefunden = rulesOf(css).filter(([s]) => s.split(/,(?![^(]*\))/).map((x) => x.trim()).includes(selector));
  assert.ok(gefunden.length > 0, `Regel ${selector} fehlt`);
  return gefunden.map(([, d]) => d).join(';');
}

/** Eine Komponentendatei, wie sie in dist/components.css landet — mit erzeugten Rollenregeln. */
export const readGenerated = (dir, file) => expandRoles(readComponent(dir, file), TOKENS);
