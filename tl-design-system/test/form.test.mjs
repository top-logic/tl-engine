import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync, readdirSync } from 'node:fs';
import { readComponent, colorVarsOf, declarationsOf, specimenProblems, missingInContract, varsOf } from './lib/component.mjs';

const P = JSON.parse(readFileSync(new URL('../src/primitives.json', import.meta.url)));
const T = JSON.parse(readFileSync(new URL('../src/tokens.json', import.meta.url)));
const form = readComponent('form', 'form.css');
const upload = readComponent('form', 'upload.css');

test('die Label-Spalte und der Geändert-Punkt sind Tokens auf der Skala', () => {
  assert.equal(P.measure['size-10'], '6rem');
  assert.equal(P.measure['size-11'], '10rem');
  assert.equal(T.measure['size-field-label-min'], '{size-10}');
  assert.equal(T.measure['size-field-label-max'], '{size-11}');
  assert.equal(T.measure['size-indicator'], '{size-2}');
});

test('form.css liest genau diese Farbrollen, layer-accent nicht mehr', () => {
  assert.deepEqual(colorVarsOf(form), [
    'border-control', 'border-separator', 'icon-primary', 'icon-secondary', 'status-error-text',
    'status-warning-text', 'surface-brand', 'text-helper', 'text-primary', 'text-secondary',
  ]);
});

test('der Hilfeknopf ist leise: icon-secondary, erst unter dem Zeiger icon-primary', () => {
  assert.match(declarationsOf(form, '.tl-form-field__help') ?? '', /color:\s*var\(--tl-icon-secondary\)/);
  assert.match(declarationsOf(form, '.tl-form-field__help:hover') ?? '', /color:\s*var\(--tl-icon-primary\)/);
});

test('die Seitenlage nimmt die Label-Spalte aus Tokens, kein Literal', () => {
  const d = declarationsOf(form, '.tl-form-field--side') ?? '';
  assert.match(d, /minmax\(var\(--tl-size-field-label-min\),\s*var\(--tl-size-field-label-max\)\)/);
});

test('geändert und eingeklappt sind Attribute', () => {
  assert.ok(declarationsOf(form, '.tl-form-field[data-tl-state~="dirty"] .tl-form-field__dirty'), 'dirty fehlt');
  assert.ok(declarationsOf(form, '.tl-form-group__body[hidden]'), 'eingeklappt fehlt');
  assert.doesNotMatch(form, /--collapsed|--dirty|--error|--warning|--readonly/);
});

test('upload.css: die Drop-Zone zeigt dragover als Attribut, gestrichelt in der Markenlinie', () => {
  const d = declarationsOf(upload, '.tl-file-upload[data-tl-state~="dragover"]') ?? '';
  assert.match(d, /dashed/);
  assert.match(d, /--tl-border-interactive/);
  assert.ok(declarationsOf(upload, '.tl-upload'), 'tl-upload fehlt');
  assert.doesNotMatch(upload, /outline\s*:\s*(none|0)/);
});

test('upload.css: eine verborgene Upload-Hülle bleibt verborgen, obwohl tl-upload display setzt', () => {
  assert.match(declarationsOf(upload, '.tl-upload') ?? '', /display\s*:\s*inline-flex/);
  assert.match(declarationsOf(upload, '.tl-upload[hidden]') ?? '', /display\s*:\s*none/);
});

test('das Formular-Specimen zeigt jede Label-Lage, Pflicht, Hilfe, Fehler, Warnung, geändert, Gruppe, Upload', () => {
  const html = readComponent('form', 'form.spec.html');
  assert.deepEqual(specimenProblems(html), []);
  for (const s of ['tl-form-field--side', 'tl-form-field--top', 'tl-form-field--after', 'tl-visually-hidden',
    'tl-form-field__required', 'aria-required="true"', 'tl-form-field__help-text', 'role="alert"', 'aria-live="polite"',
    'data-tl-state="dirty"', 'tl-form-group--outlined', 'tl-toggle', 'aria-expanded="false"', 'hidden', 'tl-form-layout',
    'tl-field-list', 'tl-upload', 'tl-file-upload', 'data-tl-state="dragover"', 'aria-busy="true"'])
    assert.ok(html.includes(s), `Specimen zeigt ${s} nicht`);
});

test('die Verträge nennen Tokens, Klassen und Attribute', () => {
  const md = readComponent('form', 'form.contract.md');
  assert.deepEqual(missingInContract(md, [...varsOf(form), 'tl-form-field', 'tl-form-group', 'tl-form-layout',
    'tl-field-list', 'data-tl-state', 'aria-required', 'aria-expanded', 'role="alert"', 'aria-live', 'labelPosition']), []);
  const up = readComponent('form', 'upload.contract.md');
  assert.deepEqual(missingInContract(up, [...varsOf(upload), 'tl-upload', 'tl-file-upload', 'tl-button',
    'aria-busy', 'data-tl-state']), []);
});

test('die Warnung steht vor dem Fehler, der Einklappknopf ist ein tl-toggle', () => {
  const ohne = form.replace(/\/\*[\s\S]*?\*\//g, '');
  const warnung = ohne.indexOf('.tl-form-field__message[aria-live="polite"]');
  const fehler = ohne.indexOf('.tl-form-field__message[role="alert"]');
  assert.ok(warnung >= 0 && fehler >= 0, 'Warn- oder Fehlerregel fehlt');
  assert.ok(warnung < fehler, 'bei gleicher Spezifität muss der Fehler die Warnung verdrängen');
  const src = new URL('../src/', import.meta.url);
  const treffer = readdirSync(src, { recursive: true }).filter((f) => /\.(css|html|md|json)$/.test(f))
    .filter((f) => readFileSync(new URL(f, src), 'utf8').includes('tl-form-group__toggle'));
  assert.deepEqual(treffer, []);
  const md = readComponent('form', 'form.contract.md');
  assert.deepEqual(missingInContract(md, ['tl-toggle']), []);
});

test('Gruppen behalten die Spalten des Layouts, --after setzt Meldungen in Spalte 2', () => {
  const body = declarationsOf(form, '.tl-form-group__body') ?? '';
  assert.match(body, /grid-template-columns:\s*subgrid/);
  assert.match(body, /grid-column:\s*1 \/ -1/);
  assert.match(declarationsOf(form, '.tl-form-group') ?? '', /grid-template-columns:\s*subgrid/);
  assert.match(declarationsOf(form, '.tl-form-group__header') ?? '', /grid-column:\s*1 \/ -1/);
  assert.match(declarationsOf(form, '.tl-form-field--after > .tl-form-field__message') ?? '', /grid-column:\s*2/);
  assert.match(declarationsOf(form, '.tl-form-field--after > .tl-form-field__help-text') ?? '', /grid-column:\s*2/);
});

test('die Beschriftung ist ein div; das label trägt nur den Text, kein Knopf darin', () => {
  const html = readComponent('form', 'form.spec.html');
  assert.doesNotMatch(html, /<label[^>]*class="tl-form-field__label[ "]/);
  for (const m of html.matchAll(/<label\b[^>]*>([\s\S]*?)<\/label>/g))
    assert.doesNotMatch(m[1], /<button/, 'Knopf im label');
});
