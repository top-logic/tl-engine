// Die Wellen 2, 3a und 3b sind fertig, wenn keine ihrer alten Klassen (Formular, Menü, Toolbar, Tabs,
// Breadcrumb, Tabelle, Baum, Spaltenwahl, Spalten, Text) mehr in der Engine steht — weder im TSX noch im Stylesheet (das alte, tlReactControls.css, ist seit Welle 5d gelöscht). Das Paket liegt im selben Repo; fehlt das Modul `com.top_logic.layout.react`, scheitert der Lauf, statt still zu bestehen.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { existsSync, readdirSync, readFileSync, statSync } from 'node:fs';
import { join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

// Das Modul liegt im Engine-Repo: zwei Ebenen höher ist die Repo-Wurzel (TL_WORKTREE übersteuert sie).
const WT = resolve(process.env.TL_WORKTREE ?? fileURLToPath(new URL('../../', import.meta.url)));
const M = join(WT, 'com.top_logic.layout.react');
// Kein schließendes \b: vor `__` und `--` liegt keine Wortgrenze, tlFormField__error und tlTableView__row
// müssen treffen. tlColumns trifft tlColumnSelect nicht (das S ist groß).
const ALT = /\b(tlReactTextInput|tlReactNumberInput|tlReactDatePicker|tlReactSelect|tlReactCheckbox|tlBooleanChoice|tlSegmentedChoice|tlOptionChips|tlDropdownSelect|tlOptionValue|tlOptionImage|tlOptionIcon|tlResourceCell|tlFormField|tlFormGroup|tlFormLayout|tlFieldList|tlFileUpload|tlVisuallyHidden|tlMenu|tlMenuRegion|tlUserMenu|tlToolbar|tlReactTabBar|tlBreadcrumb|tlTableView|tlTreeView|tlColumnSelect|tlColumns|tlText)/;

test('das Engine-Modul com.top_logic.layout.react liegt in der Repo-Wurzel (Pfad der Gates)', () => {
  assert.ok(existsSync(M), `${M} fehlt — Repo-Wurzel falsch? (TL_WORKTREE)`);
});

function* files(dir) {
  for (const f of readdirSync(dir)) {
    const p = join(dir, f);
    if (statSync(p).isDirectory()) yield* files(p);
    else if (/\.(tsx?|css)$/.test(f)) yield p;
  }
}

// Jedes Modul mit react-src: die Satelliten (Demo, Kundenmodule im Worktree) nutzen die Controls
// ebenso und dürfen keine alte Klasse behalten.
function* reactSources() {
  for (const d of readdirSync(WT)) {
    if (!d.startsWith('com.top_logic.')) continue;
    const src = join(WT, d, 'react-src');
    if (existsSync(src) && statSync(src).isDirectory()) yield* files(src);
  }
}

test('keine alte Klasse der Wellen 2, 3a und 3b in react-src eines Moduls', () => {
  const hits = [];
  for (const p of reactSources()) {
    readFileSync(p, 'utf8').split('\n').forEach((line, i) => { if (ALT.test(line)) hits.push(`${p.slice(WT.length + 1)}:${i + 1}`); });
  }
  assert.deepEqual(hits, []);
});

// Gruppieren blieb erst nicht im Dialog (#29825) und kam zurück (#29967): eine ausgeblendete Spalte hat keinen Kopf, an
// dem das Kontextmenü hängt. Der Schalter ist ein Button-Part — gedrückt über `checked`, nie ein handgeschriebenes
// aria-pressed — und erscheint beim Zeigen, außer auf der gruppierten Zeile.
test('die Spaltenwahl ist ein Ebenen-Panel: kein Kästchen, Gruppieren als Button-Part beim Zeigen (#29825, revidiert #29967)', () => {
  const tsx = readFileSync(join(M, 'react-src/controls/TLColumnSelect.tsx'), 'utf8');
  assert.doesNotMatch(tsx, /aria-pressed|tl-checkbox|tl-choice/);
  assert.match(tsx, /image=\{ICON_GROUP\}/);
  assert.match(tsx, /checked=\{grouped\}/);
  assert.match(tsx, /'tl-reveal-on-hover'/);
});

// Welle 4a (#29836): keine alte Overlay-Klasse mehr — im TSX jedes Moduls, im alten Stylesheet, in den Java-Quellen,
// den Entwickler-Artikeln (tl-docs) und im Stylesheet der Demo. Modulnamen (TLWindow) treffen nicht: großes TL.
const ALT_4A = /\btl(Dialog|DialogManager|Window|Drawer|Snackbar|NoticeBar|Overlay)(__|--|\b)|\btlPhotoCapture__overlay(Backdrop)?\b/;

function* tree(dir, pattern) {
  if (!existsSync(dir)) return;
  for (const f of readdirSync(dir)) {
    const p = join(dir, f);
    if (statSync(p).isDirectory()) yield* tree(p, pattern);
    else if (pattern.test(f)) yield p;
  }
}

test('keine alte Klasse der Welle 4a in react-src, Stylesheets, Java, tl-docs und Views', () => {
  const quellen = [
    ...reactSources(),
    join(WT, 'com.top_logic.demo.react/src/main/webapp/style/tl-demo-react.css'),
    ...tree(join(M, 'src/main/java'), /\.java$/),
    ...tree(join(WT, 'com.top_logic.layout.view/src/main/java/META-INF/tl-docs'), /\.md$/),
    ...tree(join(WT, 'com.top_logic.demo.react/src/main/webapp'), /\.view\.xml$/),
    ...tree(join(WT, 'com.top_logic.layout.view/src/main/webapp'), /\.view\.xml$/),
  ];
  const hits = [];
  for (const p of quellen) readFileSync(p, 'utf8').split('\n').forEach((line, i) => { if (ALT_4A.test(line)) hits.push(`${p.slice(WT.length + 1)}:${i + 1}`); });
  assert.deepEqual(hits, []);
});

// Welle 4b (#29866): keine alte Klasse der Navigation mehr — im TSX jedes Moduls (MUI eingeschlossen), in Java, tl-docs und Views. Modulnamen (TLSidebar) treffen nicht: großes TL.
const ALT_4B = /\btl(Sidebar|AppBar|BottomBar|AppShell|DrawerToggle)(__|--|\b)/;

test('keine alte Klasse der Welle 4b in react-src, Stylesheets, Java, tl-docs und Views', () => {
  const quellen = [
    ...reactSources(),
    join(WT, 'com.top_logic.demo.react/src/main/webapp/style/tl-demo-react.css'),
    ...tree(join(M, 'src/main/java'), /\.java$/),
    ...tree(join(WT, 'com.top_logic.layout.view/src/main/java'), /\.(java|md)$/),
    ...tree(join(WT, 'com.top_logic.demo.react/src/main/webapp'), /\.view\.xml$/),
    ...tree(join(WT, 'com.top_logic.layout.view/src/main/webapp'), /\.view\.xml$/),
  ];
  const hits = [];
  for (const p of quellen) readFileSync(p, 'utf8').split('\n').forEach((line, i) => { if (ALT_4B.test(line)) hits.push(`${p.slice(WT.length + 1)}:${i + 1}`); });
  assert.deepEqual(hits, []);
});

test('Welle 4b im TSX: main in der Shell, aria-current an den Seiten, aria-expanded am Hamburger, kein englischer Name, 768 nur am Breakpoint der Brücke', () => {
  const tsx = (n) => readFileSync(join(M, `react-src/controls/${n}.tsx`), 'utf8');
  assert.match(tsx('TLAppShell'), /<main className="tl-app-shell__content tl-region"/);
  assert.match(tsx('TLSidebar'), /aria-current=\{active \? 'page' : undefined\}/);
  assert.match(tsx('TLBottomBar'), /aria-current=\{isActive \? 'page' : undefined\}/);
  assert.match(tsx('TLDrawerToggle'), /aria-expanded=\{open\}/);
  for (const n of ['TLAppShell', 'TLAppBar', 'TLSidebar', 'TLBottomBar', 'TLDrawerToggle'])
    assert.doesNotMatch(tsx(n), /aria-label="[A-Za-z]/, `${n}: Name als Literal`);
  const mit768 = [...reactSources()].filter((p) => /\b768\b/.test(readFileSync(p, 'utf8'))).map((p) => p.slice(WT.length + 1));
  assert.deepEqual(mit768, ['com.top_logic.layout.react/react-src/bridge/display-class.ts']);
});

// Welle 4c (#29870): keine alte Klasse der Behälter mehr — im TSX jedes Moduls (MUI eingeschlossen), in Java, tl-docs, Views und der Demo; kein --page-inset, kein --tl-item-index, kein Dialog-Fuß der 4a.
const ALT_4C = /\btl(Panel|Card|Stack|Grid|Inset|Bounded|Item|DeckPane|SplitPanel|TileStack|Wizard|AdaptiveDetail|ViewError)(__|--|\b)|\btlHtml__error\b|--page-inset\b|--tl-item-index\b|--tlGrid-gap\b|\btl-dialog__footer\b/;

test('keine alte Klasse der Welle 4c in react-src, Stylesheets, Java, tl-docs und Views', () => {
  const quellen = [
    ...reactSources(),
    join(WT, 'com.top_logic.demo.react/src/main/webapp/style/tl-demo-react.css'),
    ...tree(join(M, 'src/main/java'), /\.java$/),
    ...tree(join(WT, 'com.top_logic.layout.view/src/main/java'), /\.(java|md)$/),
    ...tree(join(WT, 'com.top_logic.demo.react/src/main/webapp'), /\.view\.xml$/),
    ...tree(join(WT, 'com.top_logic.layout.view/src/main/webapp'), /\.view\.xml$/),
  ];
  const hits = [];
  for (const p of quellen) readFileSync(p, 'utf8').split('\n').forEach((line, i) => { if (ALT_4C.test(line)) hits.push(`${p.slice(WT.length + 1)}:${i + 1}`); });
  assert.deepEqual(hits, []);
});

test('Welle 4c im TSX: Griffe mit role=separator und Tastatur, Minimieren mit aria-expanded, Fensterfuß direkt an der Fläche, kein englischer Name', () => {
  const tsx = (n) => readFileSync(join(M, `react-src/controls/${n}.tsx`), 'utf8');
  assert.match(tsx('TLSplitPanel'), /role="separator"[\s\S]*tabIndex=\{0\}[\s\S]*onKeyDown=/);
  assert.match(tsx('TLSplitPanel'), /onDoubleClick=\{handleReset\}/);
  assert.match(tsx('TLTableView'), /onKeyDown=\{\(e\) => handleResizeKeyDown\(/);
  assert.match(tsx('TLPanel'), /aria-expanded=\{!isMinimized\}/);
  assert.match(tsx('TLWindow'), /querySelector<HTMLElement>\(':scope > \.tl-button-bar'\)/);
  for (const n of ['TLPanel', 'TLCard', 'TLSplitPanel', 'TLWizard', 'TLAdaptiveDetail', 'TLAccordion'])
    assert.doesNotMatch(tsx(n), /aria-label="[A-Za-z]/, `${n}: Name als Literal`);
});

test('der Abstand des Grids im TSX ist der des Stylesheets', async () => {
  const { readGenerated, declarationsOf } = await import('./lib/component.mjs');
  const tsx = readFileSync(join(M, 'react-src/controls/TLGrid.tsx'), 'utf8');
  const map = Object.fromEntries([...tsx.matchAll(/(compact|default|loose): 'var\((--tl-space-[a-z0-9]+)\)'/g)].map((m) => [m[1], m[2]]));
  const css = readGenerated('layout', 'layout.css');
  for (const stufe of ['compact', 'default', 'loose']) {
    const gap = /gap:\s*var\((--tl-space-[a-z0-9]+)\)/.exec(declarationsOf(css, `.tl-grid--gap-${stufe}`) ?? '')?.[1];
    assert.equal(map[stufe], gap, stufe);
  }
});

// Welle 5a (#29875): keine alte Klasse der Eingaben mehr — Farbfeld, Icon-Feld, Slider samt --tlSlider-*.
const ALT_5A = /\btl(ColorInput|IconSelect|Slider)(__|--|\b)|--tlSlider-/;

test('keine alte Klasse der Welle 5a in react-src, Stylesheets, Java, tl-docs und Views', () => {
  const quellen = [
    ...reactSources(),
    join(WT, 'com.top_logic.demo.react/src/main/webapp/style/tl-demo-react.css'),
    ...tree(join(M, 'src/main/java'), /\.java$/),
    ...tree(join(WT, 'com.top_logic.layout.view/src/main/java'), /\.(java|md)$/),
    ...tree(join(WT, 'com.top_logic.demo.react/src/main/webapp'), /\.view\.xml$/),
    ...tree(join(WT, 'com.top_logic.layout.view/src/main/webapp'), /\.view\.xml$/),
  ];
  const hits = [];
  for (const p of quellen) readFileSync(p, 'utf8').split('\n').forEach((line, i) => { if (ALT_5A.test(line)) hits.push(`${p.slice(WT.length + 1)}:${i + 1}`); });
  assert.deepEqual(hits, []);
});

test('Welle 5a im TSX: Picker als Dialog mit usePopover, Reiter mit useTabList, kein englischer Name', () => {
  const tsx = (n) => {
    const p = [...reactSources()].find((q) => q.endsWith(`/${n}.tsx`));
    assert.ok(p, `${n}.tsx nicht in den React-Quellen gefunden`);
    return readFileSync(p, 'utf8');
  };
  const pp = tsx('PickerPopover');
  assert.match(pp, /role="dialog"/);
  assert.match(pp, /usePopover/);
  assert.match(tsx('TLTabBar'), /useTabList/);
  for (const n of ['TLColorInput', 'ColorPopup', 'TLIconSelect', 'IconSelectPopup', 'TLSlider'])
    assert.doesNotMatch(tsx(n), /aria-label="[A-Za-z]/, `${n}: Name als Literal`);
});

// Welle 5b (Medien): \btl ist klein geschrieben und trifft die Control-Namen TLImage, TLHtml … in
// register('TLImage', …) nicht; TLCounter ist als Control ganz gestrichen und darum groß geschrieben gelistet.
const ALT_5B = /\btl(AudioRecorder|AudioPlayer|PhotoCapture|Download|FileChips|Image|PdfViewer|Html|JobStatus|Counter|BinaryField)(__|--|\b)|\bTLCounter\b|\btl-wizard__(steps|step-item|disc|step-name)\b/;

test('keine alte Klasse der Welle 5b in react-src, Stylesheets, Java, tl-docs und Views', () => {
  const quellen = [
    ...reactSources(),
    join(WT, 'com.top_logic.demo.react/src/main/webapp/style/tl-demo-react.css'),
    ...tree(join(M, 'src/main/java'), /\.java$/),
    ...tree(join(WT, 'com.top_logic.layout.view/src/main/java'), /\.(java|md)$/),
    ...tree(join(WT, 'com.top_logic.demo.react/src/main/webapp'), /\.view\.xml$/),
    ...tree(join(WT, 'com.top_logic.layout.view/src/main/webapp'), /\.view\.xml$/),
  ];
  const hits = [];
  for (const p of quellen) readFileSync(p, 'utf8').split('\n').forEach((line, i) => { if (ALT_5B.test(line)) hits.push(`${p.slice(WT.length + 1)}:${i + 1}`); });
  assert.deepEqual(hits, []);
});

test('Welle 5b im TSX: Medienknöpfe ohne Bootstrap-Glyphe und SVG, Kamera als Dialog im Portal, kein englischer Name', () => {
  const tsx = (n) => {
    const p = [...reactSources()].find((q) => q.endsWith(`/${n}.tsx`));
    assert.ok(p, `${n}.tsx nicht in den React-Quellen gefunden`);
    return readFileSync(p, 'utf8');
  };
  for (const n of ['TLAudioRecorder', 'TLAudioPlayer', 'TLPhotoCapture', 'TLHtml']) {
    const s = tsx(n);
    assert.doesNotMatch(s, /css:bi /, `${n}: Bootstrap-Glyphe`);
    assert.doesNotMatch(s, /<svg/, `${n}: eigenes SVG`);
  }
  const pc = tsx('TLPhotoCapture');
  assert.match(pc, /tl-dialog__surface/);
  assert.match(pc, /createPortal/);
  for (const n of ['TLAudioRecorder', 'TLAudioPlayer', 'TLPhotoCapture', 'TLDownload', 'TLFileChips', 'TLFileUpload', 'TLBinaryField', 'TLImage', 'TLPdfViewer', 'TLHtml', 'TLJobStatus'])
    assert.doesNotMatch(tsx(n), /aria-label="[A-Za-z]/, `${n}: Name als Literal`);
});

// Welle 5c (Planen und Ziehen): \btl ist klein geschrieben und trifft register('TLCalendar', …) und die
// Java-Klassen CalendarViewControl, DashboardElement nicht. --cal-ev-bg war die Terminfarbe am Element,
// tl-table__hint der Ablagehinweis der Tabelle (heute tl-drop-hint).
const ALT_5C = /\btl(Cal[A-Z]\w*|Calendar|KanbanBoard|Dashboard|ValueList|RowCommandCell)(__|--|\b)|--cal-ev-bg|\btl-table__hint\b/;

test('keine alte Klasse der Welle 5c in react-src, Stylesheets, Java, tl-docs und Views', () => {
  const quellen = [
    ...reactSources(),
    join(WT, 'com.top_logic.demo.react/src/main/webapp/style/tl-demo-react.css'),
    ...tree(join(M, 'src/main/java'), /\.java$/),
    ...tree(join(WT, 'com.top_logic.layout.view/src/main/java'), /\.(java|md)$/),
    ...tree(join(WT, 'com.top_logic.demo.react/src/main/webapp'), /\.view\.xml$/),
    ...tree(join(WT, 'com.top_logic.layout.view/src/main/webapp'), /\.view\.xml$/),
  ];
  const hits = [];
  for (const p of quellen) readFileSync(p, 'utf8').split('\n').forEach((line, i) => { if (ALT_5C.test(line)) hits.push(`${p.slice(WT.length + 1)}:${i + 1}`); });
  assert.deepEqual(hits, []);
});

test('Welle 5c im TSX: Interaktiv-Prüfung und Ablagehinweis zusammengelegt, keine feste Farbe im Kalender, kein englischer Name', () => {
  const tsx = (n) => {
    const p = [...reactSources()].find((q) => q.endsWith(`/${n}.tsx`));
    assert.ok(p, `${n}.tsx nicht in den React-Quellen gefunden`);
    return readFileSync(p, 'utf8');
  };
  assert.ok(!existsSync(join(M, 'react-src/controls/interactive.ts')), 'controls/interactive.ts lebt in bridge/interactive.ts');
  assert.ok(!existsSync(join(M, 'react-src/controls/drop-hint.ts')), 'controls/drop-hint.ts lebt in controls/DropHint.tsx');
  assert.doesNotMatch(tsx('TLCalendar'), /#[0-9a-f]{6}/, 'TLCalendar: feste Farbe');
  for (const n of ['TLKanbanBoard', 'TLDashboard', 'TLCalendar'])
    assert.doesNotMatch(tsx(n), /aria-label="[A-Za-z]/, `${n}: Name als Literal`);
});

// Welle 5d, Task 11 (Editoren, Flow, Demo, MUI-Text): die fremden Stylesheets und der Text-Adapter lesen Rollen des
// Pakets, kein altes Theme-Token mehr. Ausgenommen sind das Glass-Theme der Demo (--shadow-glow, --surface-blur: Token,
// die ein Theme einschaltet), und `--item-index` (die Position je Eintrag, kein Theme-Token) — beides nur in der Demo.
// Die Wurzeln der Editoren behalten ihre Klassen als Geltungsbereich der CodeMirror-Regeln und bekommen den Feldrahmen; nur der Zustand `--error` fällt.
const ALT_THEME_TOKEN = /var\(--(?!tl-)[a-z0-9-]*/g;
// Ausnahmen, jede mit ihrem Grund: das Hintergrundbild der Seite (kein Rollen-Token), die Tokens, die das Glass-Theme der
// Demo einschaltet, und die Position, die ein Wiederholer der Demo je Eintrag setzt (kein Theme-Token, kein Glass).
const BACKGROUND_IMAGE = 'var(--background-image';
const GLASS_TOKENS = ['var(--shadow-glow', 'var(--surface-blur'];
const ITEM_INDEX = 'var(--item-index';
const DEMO_CSS = 'com.top_logic.demo.react/src/main/webapp/style/tl-demo-react.css';

test('Welle 5d: kein altes Theme-Token in den Editor-, Flow-, Demo- und Basis-Stylesheets und im MUI-Text', () => {
  const dateien = [
    'com.top_logic.layout.react.codeedit/src/main/webapp/style/tlCodeEditor.css',
    'com.top_logic.model.search.react/src/main/webapp/style/tlScriptEditor.css',
    'com.top_logic.react.flow.server/src/main/webapp/style/tl-react-flow.css',
    DEMO_CSS,
    'com.top_logic.layout.react/src/main/webapp/style/tlReactBase.css',
    'com.top_logic.layout.react.mui/react-src/adapters/MuiTextAdapter.tsx',
  ];
  const hits = [];
  for (const d of dateien) {
    readFileSync(join(WT, d), 'utf8').split('\n').forEach((line, i) => {
      const erlaubt = [BACKGROUND_IMAGE, ...(d === DEMO_CSS ? [...GLASS_TOKENS, ITEM_INDEX] : [])];
      if ((line.match(ALT_THEME_TOKEN) ?? []).some((v) => !erlaubt.includes(v))) hits.push(`${d}:${i + 1}`);
    });
  }
  assert.deepEqual(hits, []);
});

test('Welle 5d: die Editoren sind Felder (tl-field), der Fehler ist aria-invalid, kein --error-Modifier', () => {
  const code = readFileSync(join(WT, 'com.top_logic.layout.react.codeedit/react-src/controls/TLCodeEditor.tsx'), 'utf8');
  const script = readFileSync(join(WT, 'com.top_logic.model.search.react/react-src/controls/TLScriptEditor.tsx'), 'utf8');
  assert.match(code, /tl-field tl-type-code/);
  assert.match(script, /tl-field tl-type-code/);
  assert.doesNotMatch(code + script, /tl(CodeEditor|ScriptEditor)--/);
  for (const css of [
    'com.top_logic.layout.react.codeedit/src/main/webapp/style/tlCodeEditor.css',
    'com.top_logic.model.search.react/src/main/webapp/style/tlScriptEditor.css',
  ]) assert.doesNotMatch(readFileSync(join(WT, css), 'utf8'), /--error/, css);
});

// Welle 5d, Task 12 (Fundament): keine alte Klasse, kein altes Attribut und keine alte Custom Property des Fundaments mehr —
// Füllen (tlFill), Ziehen mit dem Finger, Aufblitzen und Wahl-Modus, Tooltip, Kompaktfeld, Wysiwyg und Diagramm,
// die Fehlerklassen der Editoren und die Custom Properties --tl-tt-*/--tl-wysiwyg-*. Die Wurzeln `tlCodeEditor`,
// `tlCodeEditorSurface` und `tlScriptEditor` bleiben als Geltungsbereich der CodeMirror-Regeln, nur ihr `--`-Zustand fällt.
// `dataset.tlAnchor` ist das neue Attribut data-tl-anchor (MuiFloatingActionAdapter.test.tsx), nicht die alte Klasse tlAnchor.
const ALT_5D = /\btlFill\b|data-touch-drag|\btlDragShield\b|\btlTouchDragImage\b|\btlPick--active\b|(?<!dataset\.)\btlAnchor\b|\btl-tooltip-(popover|caption|body|arrow)\b|\btlCompactField|\btl(WysiwygEditor|WysiwygToolbar|ReactChart)(__|--|\b)|\btl(CodeEditor|ScriptEditor)--|--tl-(tt-|wysiwyg-)/;

// Je Alternative ein Treffer und ein Fehltreffer: ohne diese Tabelle kann ein Tippfehler im Regex das Gate still leeren.
const ALT_5D_TABELLE = [
  ['tlFill', '<div className="tlFill">', 'tl-fill tlFillColor'],
  ['data-touch-drag', "el.setAttribute('data-touch-drag', '')", 'data-tl-armed'],
  ['tlDragShield', '.tlDragShield { }', 'tl-drag-shield tlDragShieldX'],
  ['tlTouchDragImage', 'tlTouchDragImage', 'tl-touch-drag-image'],
  ['tlPick--active', 'body.tlPick--active', 'tlPick tlPick-active'],
  ['tlAnchor', '<div className={rootClassName(state, \'tlAnchor\')} data-tl-anchor={anchor}>', 'data-tl-anchor tlAnchors root.dataset.tlAnchor'],
  ['tlAnchor--flash', 'tlAnchor--flash', 'tl-anchor-flash'],
  ['tl-tooltip-popover', 'tl-tooltip-popover', 'tl-tooltip tl-tooltip__popover'],
  ['tl-tooltip-caption', 'tl-tooltip-caption', 'tl-tooltip-captions'],
  ['tl-tooltip-body', 'tl-tooltip-body', 'tl-tooltip-bodyguard'],
  ['tl-tooltip-arrow', 'tl-tooltip-arrow', 'tl-tooltip__arrow'],
  ['tlCompactField', 'tlCompactField__input', 'tl-compact-field'],
  ['tlWysiwygEditor', 'tlWysiwygEditor__content', 'TLWysiwygEditor tlWysiwygEditorLegacy'],
  ['tlWysiwygToolbar', 'tlWysiwygToolbar--sticky', 'tl-toolbar'],
  ['tlReactChart', 'tlReactChart__tooltip', 'tl-chart__tooltip tlReactChartX'],
  ['tlCodeEditor--', 'tlCodeEditor--error', 'tlCodeEditor tlCodeEditorSurface'],
  ['tlScriptEditor--', 'tlScriptEditor--error', 'tlScriptEditor'],
  ['--tl-tt-', 'padding: var(--tl-tt-padding)', 'var(--tl-text-primary) var(--tl-tone)'],
  ['--tl-wysiwyg-', 'var(--tl-wysiwyg-bg)', 'var(--tl-wysiwygx)'],
];

test('ALT_5D trifft je Alternative einen Fall und verfehlt den Gegenfall', () => {
  for (const [name, treffer, fehltreffer] of ALT_5D_TABELLE) {
    assert.match(treffer, ALT_5D, `${name}: kein Treffer`);
    assert.doesNotMatch(fehltreffer, ALT_5D, `${name}: Fehltreffer`);
  }
});

// Engine-Module, die Controls zeichnen oder deren Stylesheets das Fundament lesen.
function* reactModules() {
  for (const d of readdirSync(WT)) if (/^com\.top_logic\.(layout\.react|model\.search\.react|react\.flow|demo\.react|layout\.view$)/.test(d)) yield join(WT, d);
}

// Die Vitest-Dateien prüfen die Abwesenheit alter Namen und müssen sie dafür nennen: erlaubt sind dort nur
// Zeilen, die verneinen (not, toBeNull, toBe(false)) oder die alten Namen sammeln (old.push).
const VERNEINUNG = /\.not\.|toBeNull\(\)|toBe\(false\)|\bold\.push\(/;

test('Welle 5d: keine alte Klasse, kein altes Attribut und keine alte Custom Property in react-src, Stylesheets, Java, tl-docs, Views und im Wysiwyg-Bundle', () => {
  // Nicht gelesen wird docs/ der Engine (Pläne und Entwürfe vergangener Wochen nennen die alten Namen als Geschichte).
  const quellen = [...reactSources()];
  for (const m of reactModules()) {
    quellen.push(
      ...tree(join(m, 'src/main/webapp'), /\.(css|view\.xml)$/),
      ...tree(join(m, 'src/main/java'), /\.(java|md)$/),
    );
  }
  quellen.push(join(WT, 'com.top_logic.layout.react.wysiwyg/src/main/webapp/script/tl-react-wysiwyg.css'));
  const hits = [];
  for (const p of new Set(quellen)) {
    const vitest = /\.test\.tsx?$/.test(p);
    readFileSync(p, 'utf8').split('\n').forEach((line, i) => {
      if (ALT_5D.test(line) && !(vitest && VERNEINUNG.test(line))) hits.push(`${p.slice(WT.length + 1)}:${i + 1}`);
    });
  }
  assert.deepEqual(hits, []);
});
