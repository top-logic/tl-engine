// Die Verträge der Welle 4c nennen ihre Pflichtbegriffe; die neuen Specimen bestehen den Harness und zeigen die
// Zustände, die die Spec verlangt. Tasks 3 und 4 erweitern SOLL und SPECIMEN.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { missingInContract, specimenProblems } from './lib/component.mjs';
const datei = (d, f) => readFileSync(new URL(`../src/components/${d}/${f}`, import.meta.url), 'utf8');
const SOLL = {
  'panel/panel.contract.md': ['tl-panel', 'tl-titlebar', 'tl-titlebar__buttons', 'tl-panel__body', 'tl-button-bar',
    'data-tl-state~="minimized"', 'data-tl-state~="maximized"', 'aria-expanded', 'aria-controls', 'tl-panel--hover-actions',
    'tl-panel--full-line', '--tl-layer-raised', 'toggleMinimize', 'toggleMaximize', 'popOut', 'Escape'],
  'card/card.contract.md': ['tl-card', 'tl-card--nested', 'tl-card--overlay', 'tl-card__body', '--tl-surface-layer',
    '--tl-surface-layer-nested', '--tl-surface-overlay', 'elevated', 'useSurface'],
  'accordion/accordion.contract.md': ['tl-accordion', 'tl-card', 'tl-accordion__trigger', 'aria-expanded',
    'aria-controls', 'role="region"', 'toggleSection'],
  'button-bar/button-bar.contract.md': ['tl-button-bar'],
  'titlebar/titlebar.contract.md': ['tl-titlebar__content', 'tl-titlebar__buttons'],
  'dialog/dialog.contract.md': ['tl-button-bar'],
  'layout/layout.contract.md': ['tl-stack', 'tl-grid', 'tl-inset', 'tl-inset--panel', 'tl-inset--card', 'tl-bounded',
    'tl-item', 'tl-deck', '--item-index', '--tl-space-sm', '--tl-space-md', '--tl-space-lg', '--tl-surface-brand',
    '--tl-text-on-brand', '--tl-size-icon-md', '--tl-space-xs', '--tl-border-width-md', 'useSurface',
    'role="listbox"', 'role="option"', 'aria-selected', 'tl-item__check', 'selectItem', '--tl-surface-state-hover-strong', '--tl-border-interactive'],
  'columns/columns.contract.md': ['tl-columns__column', 'tl-item__check', 'aria-selected'],
  'split/split.contract.md': ['tl-split', 'tl-split__pane', 'tl-split__handle', 'role="separator"', 'aria-valuenow',
    'aria-orientation', 'aria-controls', 'data-tl-state~="collapsed"', 'updateSizes', 'resetSizes', 'Home', 'End'],
  'slide/slide.contract.md': ['tl-slide-in-forward', 'tl-slide-out-backward', '--tl-space-md'],
  'tile-stack/tile-stack.contract.md': ['tl-tile-stack', 'tl-tile-stack__frame', 'data-tl-state~="covered"',
    'data-tl-state~="entering"', 'data-tl-state~="exiting"', 'data-tl-state~="backward"', 'useKeyedTransition'],
  'wizard/wizard.contract.md': ['tl-wizard', 'tl-steps', 'tl-steps__disc', 'aria-current="step"',
    'data-tl-state~="done"', 'aria-hidden', 'tl-button--link', '--tl-size-wizard-progress', 'gotoStep', 'advanceStep'],
  'adaptive-detail/adaptive-detail.contract.md': ['tl-adaptive-detail', 'tl-adaptive-detail__bar', 'tl-breadcrumb',
    'aria-current="page"', 'tl-drawer--container', 'navigate'],
  'breadcrumb/breadcrumb.contract.md': ['tl-adaptive-detail'],
  'alert/alert.contract.md': ['ViewLoadError', 'TLHtml'],
  'table/table.contract.md': ['tabindex="0"', 'aria-valuenow', 'columnResize'],
};
const SPECIMEN = ['panel', 'card', 'accordion', 'layout', 'split', 'wizard', 'adaptive-detail'];

for (const [file, names] of Object.entries(SOLL)) test(`${file} nennt ${names.length} Pflichtbegriffe`, () => {
  const [d, f] = file.split('/');
  assert.deepEqual(missingInContract(datei(d, f), names), []);
});
for (const name of SPECIMEN)
  test(`${name}.spec.html besteht den Specimen-Harness`, () => assert.deepEqual(specimenProblems(datei(name, `${name}.spec.html`)), []));

test('panel.spec.html: Minimieren mit aria-expanded, aria-controls nur offen; minimiert, maximiert, als Karte', () => {
  const html = datei('panel', 'panel.spec.html');
  assert.match(html, /aria-expanded="true" aria-controls="p1-body"/);
  assert.match(html, /class="tl-panel" data-tl-state="minimized"/);
  assert.doesNotMatch(html, /aria-expanded="false" aria-controls=/);
  assert.match(html, /data-tl-state="maximized"/);
  assert.match(html, /class="tl-panel tl-card tl-panel--hover-actions"/);
  assert.match(html, /<div class="tl-button-bar">/);
  assert.match(html, /id="shrink"/);
});

test('card.spec.html: Karte auf der Seite, in einer Fläche (nested), im Dialog (overlay)', () => {
  const html = datei('card', 'card.spec.html');
  assert.match(html, /class="tl-card"/);
  assert.match(html, /class="tl-card tl-card--nested"/);
  assert.match(html, /tl-dialog__surface[\s\S]*class="tl-card tl-card--overlay"/);
  // nested nur in einer Fläche (Panel als Karte); ein schlichtes Panel hebt die Stufe nicht.
  assert.match(html, /class="tl-panel tl-card">\s*<div class="tl-panel__body">\s*<div class="tl-card tl-card--nested"/);
  assert.match(html, /class="tl-panel">\s*<div class="tl-panel__body">\s*<div class="tl-card">/);
});

test('accordion.spec.html: Zustand nur in aria-expanded, kein --expanded', () => {
  const html = datei('accordion', 'accordion.spec.html');
  assert.doesNotMatch(html, /--expanded/);
  assert.match(html, /aria-expanded="true"/);
});

test('layout.spec.html: eine Liste auswählbarer Karten, eine ausgewählt, ohne Zustandsklasse', () => {
  const html = datei('layout', 'layout.spec.html');
  assert.match(html, /role="listbox"/);
  assert.equal((html.match(/class="tl-item" role="option"/g) ?? []).length >= 3, true);
  assert.match(html, /class="tl-item" role="option" aria-selected="true" tabindex="0"/);
  assert.match(html, /<span class="tl-item__check" aria-hidden="true"><\/span>/);
  assert.equal((html.match(/tl-item__check/g) ?? []).length, 1, 'nur die ausgewählte Option trägt die Marke');
  assert.doesNotMatch(html, /--selectable|--selected/);
});

test('split.spec.html: Griff fokussierbar mit Wert; eine eingeklappte Fläche im waagrechten Split', () => {
  const html = datei('split', 'split.spec.html');
  assert.match(html, /class="tl-split__handle" role="separator" tabindex="0" aria-orientation="vertical"[^>]*aria-valuenow="30"/);
  assert.match(html, /class="tl-split__pane" data-tl-state="collapsed"/);
});

test('wizard.spec.html: erledigt, aktuell, kommend; die Liste hat einen Namen, Zähler und Scheiben sind still', () => {
  const html = datei('wizard', 'wizard.spec.html');
  assert.match(html, /<ol class="tl-steps" aria-label="[^"]+"/);
  assert.match(html, /class="tl-steps__item" data-tl-state="done"/);
  assert.match(html, /class="tl-steps__item" aria-current="step"/);
  assert.match(html, /class="tl-wizard__counter tl-type-label tl-nums" aria-hidden="true"/);
  assert.match(html, /class="tl-steps__disc tl-type-label tl-nums" aria-hidden="true"/);
  assert.doesNotMatch(html, /tl-wizard__(steps|step-item|disc|step-name)\b/);
});

test('adaptive-detail.spec.html: die letzte Krume ist die aktuelle Seite, der Drawer liegt im Behälter', () => {
  const html = datei('adaptive-detail', 'adaptive-detail.spec.html');
  assert.match(html, /<div class="tl-adaptive-detail__bar">\s*<nav class="tl-breadcrumb" aria-label="[^"]+"/);
  assert.match(html, /tl-breadcrumb__current[^"]*" aria-current="page"|aria-current="page"[^>]*tl-breadcrumb__current/);
  assert.match(html, /class="tl-drawer [^"]*tl-drawer--container[^"]*" data-tl-state="open"/);
});
