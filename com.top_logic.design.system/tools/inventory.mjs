// Die Ratsche über dem Altbestand: zählte je Block des alten tlReactControls.css die Zeilen und
// über die Blöcke die Literale (Farben, Schriftgrößen, Zeitangaben, z-index, outline: none). Seit Welle 5d
// ist die Datei gelöscht; die CLI meldet „gelöscht“, die Funktionen bleiben für den Namensraum-Schutz.
//
// Das frühere Abbildungswerkzeug (ein hand-geschriebener Regex-Parser, der Werte
// gruppiert nach px auszählte) ist nach dem Schnitt vom 17.09. exportiert und
// abgeschnitten — siehe docs/migration/altlast.md. Was die Ratsche hier braucht,
// bleibt: measure() auf Basis von declarations() aus ./lib/css.mjs, plus eine CLI,
// die das Ergebnis als JSON ausgibt.
//
// Aufruf: node inventory.mjs [pfad-zum-css]

import { declarations } from './lib/css.mjs';
import { knownVariables } from './lint-css.mjs';
import { existsSync, readdirSync, readFileSync, statSync } from 'node:fs';
import { join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

/** Der Namensraum des Design Systems: jede Custom Property, die mit `--tl` beginnt. */
const TL_NAME = /--tl[A-Za-z0-9-]*/g;

/**
 * Custom Properties im Namensraum `--tl`, die im Altbestand vorkommen — definiert oder
 * gelesen, ungefiltert. Lesen ist erlaubt (siehe `foreignOf`); erst gefiltert gegen die
 * bekannten Paket-Tokens ist das Ergebnis `foreignTl` aus test/ratchet.json.
 */
export function foreignTlNames(css) {
  return uniq(css.replace(/\/\*[\s\S]*?\*\//g, '').match(TL_NAME) ?? []);
}

/** Dasselbe für die TSX-Quellen: jeder String-Literal mit `--tl` zählt. */
export function foreignTlInSources(dir) {
  const names = [];
  for (const file of walk(dir, /\.(tsx?|css)$/)) names.push(...(readFileSync(file, 'utf8').match(TL_NAME) ?? []));
  return uniq(names);
}

/**
 * Lesen ist erlaubt — dafür sind Tokens da; fremd ist nur, was der Altbestand selbst im
 * Namensraum definiert oder was kein Paket-Token ist. `known` kommt aus `knownVariables()`
 * (tools/lint-css.mjs) über die erzeugten Dateien des Pakets.
 */
export function foreignOf(names, known) {
  return uniq(names.filter((n) => !known.has(n)));
}

function* walk(dir, re) {
  for (const name of readdirSync(dir)) {
    const p = join(dir, name);
    // tl-design-system im Stylesheet-Ordner ist das Erzeugnis des Pakets (beim Build erzeugt): es gehört dem Paket.
    if (name === 'node_modules' || name === 'tl-design-system') continue;
    if (statSync(p).isDirectory()) yield* walk(p, re);
    else if (re.test(name)) yield p;
  }
}

const uniq = (xs) => [...new Set(xs)].sort();

/**
 * Die Blöcke des alten Stylesheets: jede Überschrift der Form `/* --- Name ------ *\/` eröffnet
 * einen, der bis zur nächsten reicht. Was vor der ersten steht, ist der Block `Kopf`. Die Ratsche
 * misst je Block, nicht die Datei: Fremdzuwachs in einem Block, den keine Welle angefasst hat,
 * bleibt dem Block zugeordnet und dem Absender zurechenbar (docs/skalen.md, „Die Ratsche misst
 * Blöcke“).
 */
export function blocks(css) {
  const re = /^\/\* --- (.*?) -{2,} \*\/[ \t]*$/gm;
  const found = [];
  let m;
  while ((m = re.exec(css)) !== null) found.push({ name: m[1].trim(), start: m.index });
  const result = [];
  const seen = new Map();
  const push = (name, text) => {
    const n = (seen.get(name) ?? 0) + 1;
    seen.set(name, n);
    result.push({ name: n > 1 ? `${name} (${n})` : name, text, lines: text.replace(/\n$/, '').split('\n').length });
  };
  const head = css.slice(0, found[0]?.start ?? css.length);
  if (head.trim()) push('Kopf', head);
  found.forEach((b, i) => push(b.name, css.slice(b.start, found[i + 1]?.start ?? css.length)));
  return result;
}

/**
 * Die Ratsche über den Blöcken, die im Budget stehen: Zeilen je Block, Literale über alle
 * gelisteten Blöcke, dazu die Blöcke, die das Budget nennt und die Datei nicht mehr hat
 * (abgeschnitten — aus dem Budget streichen), und die Blöcke der Datei, die das Budget nicht
 * kennt (neu von außen — die Welle, die sie holt, nimmt sie auf).
 */
export function measureBlocks(css, names) {
  const all = blocks(css);
  const byName = new Map(all.map((b) => [b.name, b]));
  const listed = names.filter((n) => byName.has(n)).map((n) => byName.get(n));
  const joined = listed.map((b) => b.text).join('\n');
  const lines = Object.fromEntries(listed.map((b) => [b.name, b.lines]));
  return {
    blocks: lines,
    lines: listed.reduce((sum, b) => sum + b.lines, 0),
    literals: measure(joined).literals,
    missing: names.filter((n) => !byName.has(n)),
    unlisted: all.map((b) => b.name).filter((n) => !names.includes(n)),
  };
}

/** Was die Ratsche zählt — Zeilen und Literale eines CSS-Texts. Nur sinkend erlaubt. */
export function measure(css) {
  const lines = css.split('\n').length;
  const decls = declarations(css);
  const lit = (prop, re) => decls.filter((d) => d.property === prop && re.test(d.value.replace(/var\([^)]*\)/g, ''))).length;
  return {
    lines,
    literals: {
      color: (css.replace(/\/\*[\s\S]*?\*\//g, '').match(/#[0-9a-fA-F]{3,8}\b|\brgba?\(/g) ?? []).length,
      fontSize: lit('font-size', /\d/),
      duration: decls.filter((d) => /^(transition|animation)/.test(d.property) && /\d+m?s/.test(d.value)).length,
      zIndex: lit('z-index', /\d/),
      outlineNone: decls.filter((d) => d.property === 'outline' && /none/.test(d.value)).length,
    },
  };
}

// ---------------------------------------------------------------------------
// Aufruf von der Kommandozeile: node inventory.mjs [pfad]
// ---------------------------------------------------------------------------
if (import.meta.url === `file://${process.argv[1]}`) {

  const modul = join(
    resolve(process.env.TL_WORKTREE ?? fileURLToPath(new URL('../../', import.meta.url))),
    'com.top_logic.layout.react',
  );
  const pfad = process.argv[2] ?? join(modul, 'src/main/webapp/style/tlReactControls.css');
  const dist = new URL('../dist/', import.meta.url);
  const known = knownVariables(
    readFileSync(new URL('primitives.css', dist), 'utf8')
    + readFileSync(new URL('tokens.css', dist), 'utf8')
    + readFileSync(new URL('base.css', dist), 'utf8'),
  );
  const gelesen = [...foreignTlInSources(join(modul, 'react-src')), ...foreignTlInSources(join(modul, 'src/main/webapp/style'))];
  if (!existsSync(pfad)) {
    // Sollzustand seit Welle 5d: das alte Stylesheet ist gelöscht, es gibt nichts zu zählen.
    console.log(JSON.stringify({ file: 'gelöscht', foreignTl: foreignOf(gelesen, known) }, null, 2));
  } else {
    const css = readFileSync(pfad, 'utf8');
    const budget = JSON.parse(readFileSync(new URL('../test/ratchet.json', import.meta.url), 'utf8'));
    const ist = measureBlocks(css, Object.keys(budget.blocks));
    console.log(JSON.stringify({ ...ist, file: measure(css).lines, foreignTl: foreignOf([...foreignTlNames(css), ...gelesen], known) }, null, 2));
  }
}
