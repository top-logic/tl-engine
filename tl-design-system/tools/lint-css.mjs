// Das Klassen-Gate. Jede Regel aus docs/skalen.md, die ein Stylesheet brechen kann.
import { declarations } from './lib/css.mjs';

const ALLOWED = new Set(['0', '1', 'auto', 'none', '100%', '50%', '-50%', 'currentColor', 'inherit', 'initial', 'transparent',
  'solid', 'hidden', 'visible', 'block', 'flex', 'grid', 'inline-flex', 'contents', 'relative', 'absolute',
  'fixed', 'sticky', 'center', 'start', 'end', 'stretch', 'baseline', 'nowrap', 'wrap', 'ellipsis', 'pointer',
  'default', 'border-box', 'both', 'row', 'column', 'space-between', 'left', 'right', 'inset',
  'uppercase', 'underline', 'middle', 'top', 'bottom', 'normal', 'clip', 'light', 'dark', 'reduce', 'antialiased',
  'tabular-nums', 'slashed-zero']);
// !important ist NICHT mehr pauschal erlaubt (Punkt 7) — nur noch im Reduced-Motion-Block,
// dort per Sonderfall unten geprüft, nicht über ALLOWED.

// Punkt 1: die 147 CSS-Farbnamen aus dem CSS Color Level 4 Standard plus rebeccapurple.
// `transparent`/`currentColor` sind Keywords, keine Farbnamen, und bleiben separat in ALLOWED.
const NAMED_COLORS = new Set([
  'aliceblue', 'antiquewhite', 'aqua', 'aquamarine', 'azure', 'beige', 'bisque', 'black',
  'blanchedalmond', 'blue', 'blueviolet', 'brown', 'burlywood', 'cadetblue', 'chartreuse',
  'chocolate', 'coral', 'cornflowerblue', 'cornsilk', 'crimson', 'cyan', 'darkblue', 'darkcyan',
  'darkgoldenrod', 'darkgray', 'darkgreen', 'darkgrey', 'darkkhaki', 'darkmagenta',
  'darkolivegreen', 'darkorange', 'darkorchid', 'darkred', 'darksalmon', 'darkseagreen',
  'darkslateblue', 'darkslategray', 'darkslategrey', 'darkturquoise', 'darkviolet', 'deeppink',
  'deepskyblue', 'dimgray', 'dimgrey', 'dodgerblue', 'firebrick', 'floralwhite', 'forestgreen',
  'fuchsia', 'gainsboro', 'ghostwhite', 'gold', 'goldenrod', 'gray', 'grey', 'green',
  'greenyellow', 'honeydew', 'hotpink', 'indianred', 'indigo', 'ivory', 'khaki', 'lavender',
  'lavenderblush', 'lawngreen', 'lemonchiffon', 'lightblue', 'lightcoral', 'lightcyan',
  'lightgoldenrodyellow', 'lightgray', 'lightgreen', 'lightgrey', 'lightpink', 'lightsalmon',
  'lightseagreen', 'lightskyblue', 'lightslategray', 'lightslategrey', 'lightsteelblue',
  'lightyellow', 'lime', 'limegreen', 'linen', 'magenta', 'maroon', 'mediumaquamarine',
  'mediumblue', 'mediumorchid', 'mediumpurple', 'mediumseagreen', 'mediumslateblue',
  'mediumspringgreen', 'mediumturquoise', 'mediumvioletred', 'midnightblue', 'mintcream',
  'mistyrose', 'moccasin', 'navajowhite', 'navy', 'oldlace', 'olive', 'olivedrab', 'orange',
  'orangered', 'orchid', 'palegoldenrod', 'palegreen', 'paleturquoise', 'palevioletred',
  'papayawhip', 'peachpuff', 'peru', 'pink', 'plum', 'powderblue', 'purple', 'rebeccapurple',
  'red', 'rosybrown', 'royalblue', 'saddlebrown', 'salmon', 'sandybrown', 'seagreen', 'seashell',
  'sienna', 'silver', 'skyblue', 'slateblue', 'slategray', 'slategrey', 'snow', 'springgreen',
  'steelblue', 'tan', 'teal', 'thistle', 'tomato', 'turquoise', 'violet', 'wheat', 'white',
  'whitesmoke', 'yellow', 'yellowgreen',
]);

const COLOR = /#[0-9a-fA-F]{3,8}\b|\b(rgba?|hsla?|oklch|color-mix)\(/;
// Nach `%` liegt keine Wortgrenze — darum eine eigene Alternative, die auf Leerraum, `;`, `)` oder das
// Wertende schaut. Sonst ginge `width: 37%` unbemerkt durch.
const LENGTH = /(^|\s)-?\d*\.?\d+(px|rem|em|vh|vw|dvh|dvw|svh|svw|lvh|lvw|ch|pt)\b|(^|\s)-?\d*\.?\d+%(?=\s|;|$|\))/;
// Einheitenlose Literale (Punkt 10): opacity, flex, order und grid-* nehmen bewusst
// dimensionslose Zahlen entgegen (0.5, 2, -1 …) — das ist keine Maß-Angabe und wird darum
// nicht verschärft. LENGTH greift ohnehin nur bei einer Einheit, hier nur dokumentiert.
const TIME = /\b\d*\.?\d+m?s\b/;
const BEM = /^tl-[a-z0-9]+(-[a-z0-9]+)*(__[a-z0-9]+(-[a-z0-9]+)*)?(--[a-z0-9]+(-[a-z0-9]+)*)?$/;
const TYPE_FILES = /^(base|type)\.css$/;
const TYPE_SELECTOR = /\.tl-(type|print|icon)-/;

// Punkt 2: Fallback im var() — wird VOR dem Entfernen von var(...) aus dem Wert geprüft.
const VAR_FALLBACK = /var\(\s*--[a-z0-9-]+\s*,/i;
// Punkt 3: Variable ohne tl-Präfix.
const FOREIGN_VAR = /var\(\s*(--(?!tl-)[a-z0-9-]+)/gi;
// Punkt 3: eigene Variablen dürfen nur in tokens.css stehen (:root oder [data-tl-…]).
const TOKEN_SELECTOR = /^(:root\b|\[data-tl-)/;
// Punkt 4: Fokusring nur als Mechanismus, nie per Einzeleigenschaft abgeschaltet.
// Jede outline-Kurzform, die den Ring abschaltet: `none`, `hidden` oder eine Breite 0 an beliebiger Stelle.
const OUTLINE_OFF = /(^|\s)(none|hidden|0|0px)(\s|$)/;
// Punkt 7: !important nur im Reduced-Motion-Block, über genau diesen Selektor/diese Eigenschaften.
const REDUCED_MOTION_SELECTOR = '*, *::before, *::after';
const REDUCED_MOTION_PROPS = /^(animation-duration|animation-iteration-count|transition-duration|scroll-behavior)$/;
// Punkt 8: Kurven nur aus --tl-easing-*, nie als Literal in transition*/animation*.
const EASING_WORDS = /\b(ease-in-out|ease-in|ease-out|ease|linear)\b/;
// Der Hof (docs/skalen.md „Auswahlmarken zeigen einen Hof“, „Höfe sind durchscheinend“): ein Schatten ohne
// Versatz und Unschärfe, nur Ausdehnung aus Tokens, in der durchscheinenden Zeigen- oder Drücken-Schicht. Kein
// dritter Schatten, kein Fokusersatz. Die Deckkraft steht im Token, nie in der Komponente.
const HOF = /^0 0 0 (calc\((?:[^;]*)\)|var\(--tl-[a-z0-9-]+\)) var\(--tl-surface-state-(hover-strong|hover|active)\)$/;
// Die Zustandsschichten sind durchscheinend und darum nur als Hof erlaubt; eine Fläche, ein Rand oder Text aus
// ihnen hätte keinen messbaren Kontrast. Zeigen auf einer Fläche bleibt deckend (surface-interactive-*).
const SCHICHT = /var\(--tl-surface-state-[a-z0-9-]+\)/;
// Die eine Ausnahme (07.10., Hendrik): der Auf-/Zuklapper steht auf jedem Zeilengrund (gezeigt, ausgewählt);
// eine deckende Zeigen-Stufe verschwände dort. Sein Zeigen und Drücken ist darum die Schicht als eigene Fläche
// — nur an diesen zwei Selektoren, nur als background, nur genau das Token.
const SCHICHT_FLAECHE = { '.tl-toggle:hover': 'var(--tl-surface-state-hover)', '.tl-toggle:active': 'var(--tl-surface-state-active)' };
// Punkt 9: Attributselektoren, die auf Klassen zielen, umgehen das BEM/Prefix-Gate.
const CLASS_ATTR_SELECTOR = /\[class[*^~]=/;
// Punkt 10: Zustände sind Attribute (:disabled, [aria-pressed], [aria-busy], :focus-visible), nie Klassen.
const STATE_MODIFIER = /--(active|disabled|loading|hover|focus|focused|pressed|selected|open|closed|entering|exiting|maximized|clickable|visible|checked|busy|invalid|dirty|immutable|readonly|dragover|dragging|highlighted|collapsed|expanded|cursor|filtered|frozen|frozen-last|pinned|pinned-first|drop-before|drop-after|drop-onto|drop-refused|minimized|covered|current|done|upcoming|forward|backward)$/;
// Punkt 10a: Am Element ist jeder Zustand ein Attribut — auch Fehler und Warnung, die ein Block (tl-alert,
// tl-pill) als Bedeutung trägt. tl-x__y--error ist ein Zustand, der sich als Modifier tarnt.
const ELEMENT_STATE_MODIFIER = /^tl-[a-z0-9-]+__[a-z0-9-]+--(active|disabled|loading|hover|focus|focused|pressed|selected|open|closed|entering|exiting|maximized|clickable|visible|checked|busy|invalid|dirty|error|warning|immutable|readonly|dragover|dragging|highlighted|collapsed|expanded|cursor|filtered|frozen|frozen-last|pinned|pinned-first|drop-before|drop-after|drop-onto|drop-refused|minimized|covered|current|done|upcoming|forward|backward)$/;
// Punkt 11: Die Modifier eines Blocks sind entschieden (docs/skalen.md), nicht gewachsen.
const MODIFIERS = {
  'tl-button': new Set(['primary', 'secondary', 'ghost', 'link', 'danger', 'sm', 'icon']),
  'tl-text': new Set(['truncate']),
  'tl-columns': new Set(['gap-compact', 'gap-default', 'gap-loose']),
  'tl-pill': new Set([]), // Die Rolle ist ein Attribut (data-tl-role), kein Modifier.
  'tl-avatar': new Set(['sm', 'lg', 'xl']), // Die Kategorie ist ein Attribut (data-tl-role).
  'tl-progress': new Set([]), // Unbestimmt ist ein Zustand (aria-busy), keine Klasse.
  'tl-field': new Set(['multiline', 'preformatted']), // Fehler, Warnung, Schreibschutz sind Attribute bzw. ein anderes Element.
  'tl-field-group': new Set([]),
  'tl-field-value': new Set(['multiline', 'preformatted']),
  'tl-checkbox': new Set(['switch']),
  'tl-radio': new Set([]),
  'tl-choice': new Set([]),
  'tl-choice-group': new Set(['vertical']), // Untereinander (TLChoiceGroup, #29654).
  'tl-segmented': new Set([]),
  'tl-chip-group': new Set([]),
  'tl-chip': new Set([]),
  'tl-select': new Set([]),
  'tl-reorder-list': new Set([]),
  'tl-form-field': new Set(['side', 'top', 'after', 'hidden', 'full']),
  'tl-form-group': new Set(['separator', 'outlined', 'full']),
  'tl-form-layout': new Set(['fill']),
  'tl-field-list': new Set([]),
  'tl-upload': new Set([]),
  'tl-file-upload': new Set([]),
  'tl-alert': new Set([]), // Die Bedeutung ist Pflicht und ein Attribut (data-tl-role).
  'tl-badge': new Set([]),
  'tl-divider': new Set(['vertical']),
  'tl-toggle': new Set(['end']), // Öffnet zum Zeilenende (Seitenleiste, #29787): eine Form, kein Zustand.
  'tl-spinner': new Set(['sm']),
  'tl-popover': new Set(['content']), // Inhalts-Popover (#29659): bündig, begrenzte Breite — eine Form, kein Zustand.
  'tl-menu': new Set([]),
  'tl-menu-region': new Set([]),
  'tl-toolbar': new Set(['wrap']), // Bricht um statt zu kollabieren (Wysiwyg, 5d): eine Form, kein Zustand.
  'tl-tabs': new Set([]),
  'tl-breadcrumb': new Set([]),
  'tl-skeleton': new Set([]),
  'tl-status': new Set([]),
  'tl-table': new Set([]),
  'tl-tree': new Set([]),
  'tl-overlay': new Set(['fill']), // Die Anker sind Element-Modifier (tl-overlay__layer--<anker>), keine des Blocks.
  'tl-titlebar': new Set([]),
  'tl-dialog': new Set([]), // Ein- und Ausblenden, maximiert: data-tl-state.
  'tl-drawer': new Set(['start', 'end', 'bottom', 'narrow', 'medium', 'wide', 'container']), // offen: data-tl-state.
  'tl-snackbar': new Set([]), // Die Bedeutung trägt tl-alert (data-tl-role).
  'tl-notice-bar': new Set([]), // klickbar: role="button", kein Modifier.
  'tl-app-shell': new Set([]), // Kompakt ist ein Attribut (data-tl-display) der Wurzel.
  'tl-app-bar': new Set(['elevated']), // Eine Variante, kein Zustand.
  'tl-sidebar': new Set([]), // Eingeklappt: data-tl-state~="collapsed"; aktuell: aria-current.
  'tl-bottom-bar': new Set([]), // Aktuell: aria-current.
  'tl-icon-badge': new Set([]),
  'tl-button-bar': new Set([]), // Leer verborgen über :has, kein Modifier.
  'tl-panel': new Set(['hover-actions', 'full-line']), // Minimiert, maximiert: data-tl-state.
  'tl-card': new Set(['nested', 'overlay']), // Die Stufe folgt der Verschachtelung (Weg B), keine Erscheinung.
  'tl-accordion': new Set([]), // Offen: aria-expanded am Auslöser.
  'tl-stack': new Set(['row', 'column', 'gap-compact', 'gap-default', 'gap-loose', 'align-start', 'align-center',
    'align-end', 'align-stretch', 'justify-start', 'justify-center', 'justify-end', 'justify-space-between',
    'justify-space-around', 'justify-space-evenly', 'wrap', 'grow-first']),
  'tl-grid': new Set(['gap-compact', 'gap-default', 'gap-loose']),
  'tl-inset': new Set(['panel', 'card']), // Ohne Modifier: die Seite.
  'tl-bounded': new Set([]),
  'tl-item': new Set([]),
  'tl-deck': new Set([]),
  'tl-split': new Set(['horizontal', 'vertical']), // Alle eingeklappt: data-tl-state.
  'tl-tile-stack': new Set([]), // Richtung und Übergang: data-tl-state.
  'tl-swatch': new Set(['color', 'wide']), // Leer, Fehler, Warnung: Attribute.
  'tl-picker': new Set([]),
  'tl-color-picker': new Set([]),
  'tl-icon-picker': new Set([]),
  'tl-slider': new Set([]), // Fehler und Warnung: aria-invalid, data-tl-state.
  'tl-wizard': new Set([]), // Richtung: data-tl-state; Schritt erledigt: data-tl-state, aktuell: aria-current.
  'tl-adaptive-detail': new Set([]),
  'tl-media': new Set(['cover', 'contain', 'document', 'preview']), // Einpassung, Papier, Vorschau; gespiegelt: data-tl-state.
  'tl-prose': new Set([]),
  'tl-file': new Set([]), // Leer: data-tl-state.
  'tl-steps': new Set(['vertical']), // Erledigt: data-tl-state; aktuell: aria-current.
  'tl-job-status': new Set([]),
  'tl-calendar': new Set([]), // Heute, arbeitsfrei, anderer Monat, Termintag, Vorschau, gewählt: Attribute; Kategorie: data-tl-role.
  'tl-board': new Set([]), // Gewählt: aria-selected am tl-item; Ziehen und Ablegen: data-tl-state (drag.css).
  'tl-dashboard': new Set([]), // Bearbeiten: data-tl-state~="edit"; Aktion deaktiviert: disabled/aria-disabled am Knopf.
  'tl-value-list': new Set(['block']), // Untereinander statt in einer Zeile: eine Form, kein Zustand.
  'tl-drop-hint': new Set([]),
  'tl-tooltip': new Set([]), // Interaktiv: data-tl-state~="interactive".
  'tl-rich-text': new Set([]), // Fehler: aria-invalid am Rahmen (tl-field).
  'tl-compact-field': new Set([]), // Bearbeitbar: data-tl-state~="edit".
  'tl-chart': new Set([]),
};

// Die einzige Ausnahme vom Ring: der Behälter mit virtuellem Cursor, nur in base.css (Spec 3b §3).
const CURSOR_CONTAINER = ':where(:not(input, textarea, [role="combobox"])[aria-activedescendant]):focus-visible';

export function knownVariables(css) {
  return new Set([...css.matchAll(/--tl-[a-z0-9-]+(?=:)/g)].map((m) => m[0]));
}

export function lint(css, { known, file }) {
  const out = [];
  const say = (d, msg) => out.push(`${file}:${d.line} ${d.selector} { ${d.property} } — ${msg}`);
  for (const d of declarations(css)) {
    const sel = d.selector;
    const isTypeClass = TYPE_FILES.test(file) && TYPE_SELECTOR.test(sel);

    // Punkt 2: Fallback im var() — vor jeder weiteren Wert-Verarbeitung, sonst ist der
    // Fallback schon aus dem Wert verschwunden.
    if (VAR_FALLBACK.test(d.value)) say(d, 'Fallback im var() — fehlt ein Token, wird es angelegt, nicht umgangen');

    if (d.property.startsWith('--')) {
      // Punkt 3: eine eigene Variable außerhalb von tokens.css (:root/[data-tl-…]) ist ein
      // Verstoß — Komponenten bekommen ihre Werte aus src/tokens.json, nicht aus einer
      // lokalen Deklaration in der Klasse. Das gilt auch für --tl-*-Namen.
      if (!TOKEN_SELECTOR.test(sel)) say(d, 'eigene Variable in einer Klasse — Tokens gehören nach src/tokens.json');
      continue; // Token-Deklarationen (tokens.css) werden nicht auf Literale geprüft
    }

    const sel2 = sel;
    for (const cls of sel2.matchAll(/\.([A-Za-z0-9_-]+)/g))
      if (!BEM.test(cls[1])) out.push(`${file}:${d.line} .${cls[1]} — kein tl-Präfix oder kein BEM (block__element--modifier)`);
    for (const cls of sel2.matchAll(/\.([A-Za-z0-9_-]+)/g)) {
      if (ELEMENT_STATE_MODIFIER.test(cls[1])) out.push(`${file}:${d.line} .${cls[1]} — Zustand als Element-Modifier; Zustände sind Attribute`);
      else if (STATE_MODIFIER.test(cls[1])) out.push(`${file}:${d.line} .${cls[1]} — Zustand als Modifier; Zustände sind Attribute`);
      const m = /^(tl-[a-z0-9-]+?)--([a-z0-9-]+)$/.exec(cls[1]);
      if (m && MODIFIERS[m[1]] && !MODIFIERS[m[1]].has(m[2]))
        out.push(`${file}:${d.line} .${cls[1]} — kein Modifier von ${m[1]}; erlaubt: ${[...MODIFIERS[m[1]]].join(', ')}`);
    }
    if (CLASS_ATTR_SELECTOR.test(sel)) say(d, 'Attributselektor auf Klassen — umgeht das BEM/Prefix-Gate');
    if (/:focus\b(?!-visible|-within)/.test(sel)) say(d, ':focus ohne -visible');
    // Ein Zeiger für Deaktiviert (docs/skalen.md „Disabled“): not-allowed ist kein Literal, das ALLOWED
    // fangen könnte — ein unbekanntes Wort meldet das Gate nicht —, darum eigens.
    if (d.property === 'cursor' && d.value.trim() === 'not-allowed') say(d, 'cursor: not-allowed — Deaktiviert zeigt cursor: default');

    if (d.property === 'outline' && OUTLINE_OFF.test(d.value.trim())) say(d, 'outline: none/0 — der Ring ist Pflicht');
    if (d.property === 'outline-width' && /^0(px)?$/.test(d.value.trim())) say(d, 'outline-width: 0 — der Ring ist Pflicht');
    if (d.property === 'outline-style' && d.value.trim() === 'none' && !(file === 'base.css' && sel.trim() === CURSOR_CONTAINER)) say(d, 'outline-style: none — der Ring ist Pflicht');
    if (d.property === 'outline-color' && d.value.trim() === 'transparent') say(d, 'outline-color: transparent — der Ring ist Pflicht');

    if (d.property === 'font-size' && !isTypeClass) say(d, 'font-size außerhalb einer tl-type-Klasse');
    if (d.property === 'font-weight' && !isTypeClass) say(d, 'font-weight außerhalb einer tl-type-Klasse');
    // Punkt 5: die font-Kurzform setzt Größe und Gewicht mit — Keyword-only-Werte (inherit,
    // initial, unset, normal) setzen nichts Konkretes und bleiben darum erlaubt.
    if (d.property === 'font' && !isTypeClass && !/^(inherit|initial|unset|normal)$/.test(d.value.trim()))
      say(d, 'font-Kurzform setzt Größe und Gewicht außerhalb einer Typo-Klasse');
    // Punkt 6: line-height wie font-size — body bekommt die System-Zeilenhöhe, sonst nur Typo-Klassen.
    if (d.property === 'line-height' && !isTypeClass && sel !== 'body')
      say(d, 'line-height außerhalb einer Typo-Klasse');

    if (d.property === 'z-index' && !/^var\(--tl-layer-/.test(d.value)) say(d, 'z-index nur aus --tl-layer-*');
    if (d.property === 'box-shadow' && !/^var\(--tl-shadow-/.test(d.value) && !/^inset\b/.test(d.value) && !HOF.test(d.value.trim()))
      say(d, 'box-shadow nur --tl-shadow-*, inset-Linie oder Hof (0 0 0 <Maß> surface-state-hover/-hover-strong/-active)');
    if (SCHICHT.test(d.value) && !(d.property === 'box-shadow' && HOF.test(d.value.trim()))
      && !(d.property === 'background' && SCHICHT_FLAECHE[sel] === d.value.trim()))
      say(d, 'surface-state-* ist durchscheinend und nur als Hof erlaubt (box-shadow 0 0 0 <Maß> <Schicht>), als Fläche nur am tl-toggle');

    // Punkt 8: Kurve nur aus --tl-easing-* — Literale in transition*/animation* geprüft
    // NACH Entfernen von var(...), sonst matcht "linear" aus --tl-easing-linear.
    if (/^(transition|animation)/.test(d.property)) {
      const ohneVar = d.value.replace(/var\([^)]*\)/g, ' ');
      if (/cubic-bezier\(/.test(ohneVar) || EASING_WORDS.test(ohneVar)) say(d, 'Kurve nur aus --tl-easing-*');
    }

    // Punkt 3: var(--foo) ohne tl-Präfix.
    for (const v of d.value.matchAll(FOREIGN_VAR)) say(d, `Variable ohne tl-Präfix: ${v[1]}`);
    for (const v of d.value.matchAll(/var\((--tl-[a-z0-9-]+)/g))
      if (!known.has(v[1])) say(d, `unbekannte Variable ${v[1]}`);

    // Punkt 7: !important nur im Reduced-Motion-Block (genau dieser Selektor/diese Eigenschaften).
    if (/!important/.test(d.value)) {
      const erlaubt = sel === REDUCED_MOTION_SELECTOR && REDUCED_MOTION_PROPS.test(d.property);
      if (!erlaubt) say(d, '!important nur im Reduced-Motion-Block');
    }

    let rest = d.value.replace(/var\([^)]*\)/g, '').replace(/calc\(|\)/g, ' ').replace(/,/g, ' ').replace(/!important/g, '');
    rest = rest.split(/\s+/).filter((word) => word && !ALLOWED.has(word)).join(' ');
    if (COLOR.test(rest)) say(d, `Farbliteral: ${d.value}`);
    // Punkt 1: benannte Farben (weiß, rot, …) sind ein Farbliteral wie ein Hex-Wert.
    for (const word of rest.split(/\s+/)) if (NAMED_COLORS.has(word.toLowerCase())) say(d, `Farbliteral (benannte Farbe): ${d.value}`);
    if (TIME.test(rest) && !/0\.01ms/.test(rest)) say(d, `Dauer-Literal: ${d.value}`);
    if (file !== 'base.css' || !/\.tl-print-|\.tl-icon-|line-height|letter-spacing/.test(sel + d.property))
      if (LENGTH.test(rest)) say(d, `Maß-Literal: ${d.value}`);
  }
  return out;
}

if (typeof process !== 'undefined' && import.meta.url === `file://${process.argv[1]}`) {
  const { readFileSync, readdirSync, existsSync } = await import('node:fs');
  const dist = new URL('../dist/', import.meta.url);
  const comps = new URL('../src/components/', import.meta.url);
  const known = knownVariables(readFileSync(new URL('primitives.css', dist), 'utf8') + readFileSync(new URL('tokens.css', dist), 'utf8'));
  const files = [...readdirSync(dist).filter((f) => f.endsWith('.css') && f !== 'primitives.css' && f !== 'components.css').map((f) => [f, readFileSync(new URL(f, dist), 'utf8')])];
  if (existsSync(comps)) {
    // Mit den erzeugten Rollenregeln (@tl-roles), sonst prüft das Gate sie nirgends.
    const { componentSources, expandRoles } = await import('./generate.mjs');
    const T = JSON.parse(readFileSync(new URL('../src/tokens.json', import.meta.url), 'utf8'));
    for (const c of componentSources(comps)) files.push([c.name, expandRoles(c.css, T)]);
  }
  let n = 0;
  for (const [f, css] of files) for (const v of lint(css, { known, file: f })) { console.log(v); n++; }
  console.log(n ? `${n} Verstöße` : 'Klassen-Gate grün');
  process.exit(n ? 1 : 0);
}
