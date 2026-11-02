// Welle 5b (Spec 2026-10-19 §4, §5): Anzeige aus dem Paket — tl-media, surface-document, tl-prose, Overlay mit
// fill und action. Jede Regel hier steht in der Spec; die Verträge prüft contracts-5b.test.mjs.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { readGenerated, declarationsOf, rulesOf } from './lib/component.mjs';

const T = JSON.parse(readFileSync(new URL('../src/tokens.json', import.meta.url)));
const BASE = readFileSync(new URL('../dist/base.css', import.meta.url), 'utf8');
const decl = (dir, sel, file = `${dir}.css`) => declarationsOf(readGenerated(dir, file), sel) ?? '';
const ohneKommentar = (css) => css.replace(/\/\*[\s\S]*?\*\//g, '');
/** Alle Deklarationen der Regeln, deren Selektorliste sel nennt — ein Element kann in mehreren Regeln stehen. */
const alle = (dir, sel) => rulesOf(readGenerated(dir, `${dir}.css`))
  .filter(([s]) => s.split(',').map((x) => x.trim()).includes(sel)).map(([, d]) => d).join('');

test('surface-document: weiß in beiden Modi', () => {
  assert.equal(T.color.light['surface-document'], '{gray-0}');
  assert.equal(T.color.dark['surface-document'], '{gray-0}');
});

test('media: Rahmen füllt, Ecke radius-sm, Einpassung als Variante', () => {
  assert.match(decl('media', '.tl-media__frame'), /var\(--tl-radius-sm\)/);
  assert.match(decl('media', '.tl-media--cover .tl-media__frame'), /object-fit:\s*cover/);
  assert.match(decl('media', '.tl-media--contain .tl-media__frame'), /object-fit:\s*contain/);
  assert.match(decl('media', '.tl-media--document .tl-media__frame'), /var\(--tl-surface-document\)/);
  assert.match(decl('media', '.tl-media__frame[data-tl-state~="mirrored"]'), /transform:\s*scaleX\(-1\)/);
  assert.doesNotMatch(ohneKommentar(readGenerated('media', 'media.css')), /\d+px|#[0-9a-f]{3,6}\b/i);
});

test('media: Bild so breit wie das Bild, Dokument füllt', () => {
  assert.match(alle('media', '.tl-media--cover'), /inline-size:\s*fit-content/);
  assert.match(alle('media', '.tl-media--contain'), /inline-size:\s*fit-content/);
  assert.doesNotMatch(alle('media', '.tl-media--document'), /fit-content/);
});

test('media: Vorschau schneidet ab, Seite außerhalb des Flusses, ohne Zeiger', () => {
  assert.match(decl('media', '.tl-media--preview'), /overflow:\s*hidden/);
  const frame = decl('media', '.tl-media--preview > .tl-media__frame');
  assert.match(frame, /position:\s*absolute/);
  assert.match(frame, /inset-block-start:\s*0/);
  assert.match(frame, /inset-inline-start:\s*0/);
  assert.match(frame, /transform-origin:\s*0 0/);
  assert.match(frame, /pointer-events:\s*none/);
});

test('prose: lange Wörter brechen, Sprungziele halten Abstand', () => {
  const root = alle('prose', '.tl-prose');
  assert.match(root, /overflow-wrap:\s*break-word/);
  assert.match(root, /scroll-margin-block-start:\s*var\(--tl-space-lg\)/);
  assert.match(alle('prose', '.tl-prose [id]'), /scroll-margin-block-start:\s*var\(--tl-space-lg\)/);
});

/** Die Selektorliste der Regel, die mit `.tl-type-<n> {` endet. */
function typoRegel(n) {
  const ohne = ohneKommentar(BASE);
  const m = [...ohne.matchAll(/([^{}]+)\{[^}]*\}/g)].find((r) => r[1].split(',').map((s) => s.trim()).includes(`.tl-type-${n}`));
  assert.ok(m, `.tl-type-${n} fehlt in base.css`);
  return m[1].split(',').map((s) => s.trim());
}

test('prose: Schrift nur aus den Typo-Klassen', () => {
  const soll = {
    'heading-lg': ['h1'], 'heading-md': ['h2'], 'heading-sm': ['h3', 'h4', 'h5', 'h6'],
    body: ['p', 'li'], 'body-strong': ['th'], code: ['code', 'pre'],
  };
  for (const [n, els] of Object.entries(soll)) {
    const sel = typoRegel(n);
    for (const el of els) assert.ok(sel.includes(`.tl-prose ${el}`), `.tl-prose ${el} fehlt an .tl-type-${n}`);
  }
  // Der Tabellenkopf ist fett: th steht an body-strong, nicht an body.
  assert.ok(!typoRegel('body').includes('.tl-prose th'), '.tl-prose th steht noch an .tl-type-body');
  const prose = ohneKommentar(readGenerated('prose', 'prose.css'));
  assert.doesNotMatch(prose, /font-size|font-weight|line-height/);
  assert.match(alle('prose', '.tl-prose a'), /var\(--tl-text-link\)/);
  assert.match(alle('prose', '.tl-prose a:hover'), /var\(--tl-text-link-hover\)/);
  assert.match(alle('prose', '.tl-prose td'), /var\(--tl-border-separator\)/);
  assert.match(alle('prose', '.tl-prose blockquote'), /var\(--tl-border-separator\)/);
});

test('overlay: fill, base, action', () => {
  const fill = decl('overlay', '.tl-overlay--fill');
  assert.match(fill, /display:\s*(flex|block)/);
  assert.match(fill, /inline-size:\s*100%/);
  assert.match(fill, /block-size:\s*100%/);
  const action = decl('overlay', '.tl-overlay__action');
  for (const t of ['surface-overlay', 'radius-md', 'shadow-popover', 'space-2xs'])
    assert.match(action, new RegExp(`var\\(--tl-${t}\\)`), t);
  // Sichtprüfung 19.10.: der Knopf schwebt mit Abstand zur Ecke, nicht bündig am Rand.
  assert.match(action, /margin:\s*var\(--tl-space-xs\)/);
  // Die Basis gibt ihrem Kind die volle Höhe: sonst bliebe ein iframe darin 150 px hoch.
  assert.match(decl('overlay', '.tl-overlay--fill > .tl-overlay__base'), /flex-direction:\s*column/);
  assert.match(decl('overlay', '.tl-overlay--fill > .tl-overlay__base > *'), /flex:\s*1 1 auto/);
});

test('button: gedrückt in danger ist gefüllt rot', () => {
  const css = ohneKommentar(readGenerated('button', 'button.css'));
  const rollen = {
    '.tl-button--danger[aria-pressed="true"]': '',
    '.tl-button--danger[aria-pressed="true"]:hover': '-hover',
    '.tl-button--danger[aria-pressed="true"]:active': '-active',
  };
  for (const [sel, stufe] of Object.entries(rollen)) {
    const d = decl('button', sel);
    assert.match(d, new RegExp(`background:\\s*var\\(--tl-status-error-surface${stufe}\\)`), sel);
    assert.match(d, new RegExp(`border-color:\\s*var\\(--tl-status-error-surface${stufe}\\)`), sel);
    assert.match(d, new RegExp(`color:\\s*var\\(--tl-text-on-status-error${stufe}\\)`), sel);
  }
  // Nach dem gedrückten Zustand (sonst gewänne die Marke), vor Deaktiviert (sonst schlüge Rot den disabled-Zustand).
  const nach = css.indexOf('.tl-button[aria-pressed="true"]:hover');
  const regel = css.indexOf('.tl-button--danger[aria-pressed="true"]');
  const vor = css.indexOf('.tl-button:disabled');
  assert.ok(nach >= 0 && regel > nach && regel < vor, 'Reihenfolge in button.css');
});

test('file: Name mit Ellipse, leer in text-secondary, nicht kursiv', () => {
  const name = decl('file', '.tl-file__name');
  assert.match(name, /text-overflow:\s*ellipsis/);
  assert.match(name, /overflow:\s*hidden/);
  assert.match(name, /white-space:\s*nowrap/);
  assert.match(name, /max-inline-size:\s*var\(--tl-size-13\)/);
  assert.match(decl('file', '.tl-file[data-tl-state~="empty"]'), /var\(--tl-text-secondary\)/);
  assert.doesNotMatch(ohneKommentar(readGenerated('file', 'file.css')), /font-style/);
});

test('upload: Ziehen an Drop-Zone und Chip-Gruppe gleich', () => {
  const regel = rulesOf(readGenerated('form', 'upload.css')).find(([s]) => {
    const l = s.split(',').map((x) => x.trim());
    return l.includes('.tl-file-upload[data-tl-state~="dragover"]') && l.includes('.tl-chip-group[data-tl-state~="dragover"]');
  });
  assert.ok(regel, 'eine Regel mit beiden Selektoren fehlt');
  assert.match(regel[1], /dashed var\(--tl-border-interactive\)/);
});

test('upload: Fehlerzeile bricht in der Chip-Gruppe unter die Chips', () => {
  // §6.5: die Zeile ist das letzte Kind der Gruppe; sie steht nur dann darunter, wenn die Gruppe umbricht.
  assert.match(decl('choice', '.tl-chip-group'), /flex-wrap:\s*wrap/);
  assert.match(decl('form', '.tl-chip-group > .tl-file-upload__status', 'upload.css'), /flex-basis:\s*100%/);
});

// §7: eine Schrittliste für Wizard und JobStatus (V22). Die Ausrichtung ändert die Anordnung, nicht die Farben.
const regeln = (dir) => rulesOf(readGenerated(dir, `${dir}.css`)).filter(([s]) => !/^(from|to|\d+%)$/.test(s));

test('steps: ein Aussehen in beiden Ausrichtungen', () => {
  const d = decl('steps', '.tl-steps__disc');
  assert.match(d, /width:\s*var\(--tl-size-icon-lg\)/);
  assert.match(d, /height:\s*var\(--tl-size-icon-lg\)/);
  assert.match(d, /border:\s*var\(--tl-border-width-sm\) solid var\(--tl-border-control\)/);
  assert.match(d, /background:\s*var\(--tl-surface-layer-nested\)/);
  assert.match(d, /color:\s*var\(--tl-text-secondary\)/);
  assert.match(decl('steps', '.tl-steps__item[data-tl-state~="done"] > .tl-steps__disc'), /background:\s*var\(--tl-surface-brand-subtle\)/);
  const c = decl('steps', '.tl-steps__item[aria-current="step"] > .tl-steps__disc');
  assert.match(c, /background:\s*var\(--tl-surface-brand\)/);
  assert.match(c, /color:\s*var\(--tl-text-on-brand\)/);
  assert.match(decl('steps', '.tl-steps__name'), /color:\s*var\(--tl-text-secondary\)/);
  assert.match(decl('steps', '.tl-steps__item[aria-current="step"] > .tl-steps__name'), /color:\s*var\(--tl-text-primary\)/);
  const w = decl('steps', '.tl-steps');
  assert.match(w, /flex-wrap:\s*wrap/);
  assert.match(w, /gap:\s*var\(--tl-space-xs\) var\(--tl-space-xl\)/);
  const v = decl('steps', '.tl-steps--vertical');
  assert.match(v, /flex-direction:\s*column/);
  assert.match(v, /gap:\s*var\(--tl-space-2xs\)/);
  assert.match(decl('steps', '.tl-steps--vertical .tl-steps__disc'), /width:\s*var\(--tl-size-icon-md\)/);
  for (const [s, body] of regeln('steps').filter(([s]) => s.includes('tl-steps--vertical')))
    assert.doesNotMatch(body, /(^|;)\s*(color|background|border)[a-z-]*\s*:/, s);
});

test('steps: aktuelle Scheibe pulsiert nur senkrecht', () => {
  const css = ohneKommentar(readGenerated('steps', 'steps.css'));
  assert.match(css, /@keyframes tl-steps-pulse\b/);
  const a = decl('steps', '.tl-steps--vertical .tl-steps__item[aria-current="step"] > .tl-steps__disc');
  assert.match(a, /animation:\s*tl-steps-pulse var\(--tl-duration-loop\) var\(--tl-easing-standard\) infinite alternate/);
  for (const [s, body] of regeln('steps').filter(([, b]) => /animation/.test(b)))
    assert.ok(s.split(',').every((x) => x.includes('.tl-steps--vertical')), `waagrecht animiert: ${s}`);
});

test('wizard: liest tl-steps', () => {
  const css = ohneKommentar(readGenerated('wizard', 'wizard.css'));
  assert.doesNotMatch(css, /tl-wizard__(steps|step-item|disc|step-name)\b/);
});

test('job-status: Live-Zeile, Fehler als alert', () => {
  const md = readFileSync(new URL('../src/components/job-status/job-status.contract.md', import.meta.url), 'utf8');
  assert.match(md, /`tl-job-status__live`[^\n]*`role="status"`/);
  assert.match(md, /`tl-job-status__error`[^\n]*`role="alert"`/);
  for (const n of ['tl-steps--vertical', 'tl-nums', 'tl-visually-hidden']) assert.ok(md.includes('`' + n), n);
  assert.match(decl('job-status', '.tl-job-status__error'), /color:\s*var\(--tl-status-error-text\)/);
  assert.match(decl('job-status', '.tl-job-status__elapsed'), /color:\s*var\(--tl-text-secondary\)/);
  assert.match(decl('job-status', '.tl-job-status__message'), /color:\s*var\(--tl-text-secondary\)/);
  const r = decl('job-status', '.tl-job-status');
  assert.match(r, /flex-direction:\s*column/);
  assert.match(r, /gap:\s*var\(--tl-space-xs\)/);
  assert.match(decl('job-status', '.tl-job-status__header'), /gap:\s*var\(--tl-space-sm\)/);
  assert.match(decl('job-status', '.tl-job-status__cancel'), /margin-inline-start:\s*auto/);
});
