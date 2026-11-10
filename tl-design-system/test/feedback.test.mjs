import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { resolve } from '../tools/resolve.mjs';
import { readComponent, readGenerated, colorVarsOf, declarationsOf, specimenProblems, missingInContract, varsOf, contrastProblems } from './lib/component.mjs';

const R = resolve(JSON.parse(readFileSync(new URL('../src/primitives.json', import.meta.url))),
  JSON.parse(readFileSync(new URL('../src/tokens.json', import.meta.url))));
const STATUS = ['error', 'warning', 'success', 'info'];

test('Alert: je Bedeutung subtle-Fläche, border-Linie, Text text-primary, Icon status-text', () => {
  const css = readGenerated('alert', 'alert.css'); // mit den erzeugten Rollenregeln (@tl-roles)
  for (const s of STATUS) {
    const d = declarationsOf(css, `.tl-alert[data-tl-role="${s}"]`) ?? '';
    assert.ok(d.includes(`--tl-status-${s}-subtle`) && d.includes(`--tl-status-${s}-border`), s);
    assert.ok((declarationsOf(css, `.tl-alert[data-tl-role="${s}"] .tl-alert__icon`) ?? '').includes(`--tl-status-${s}-text`), `${s} Icon`);
  }
  for (const mode of ['light', 'dark'])
    assert.deepEqual(contrastProblems(R, mode, STATUS.map((s) => ({ fg: `status-${s}-text`, bg: `status-${s}-subtle`, soll: 3.0 }))), []);
});

test('Badge: Marke mit abgeleiteter Beschriftung, Ziffern tabellarisch über tl-nums, Pille', () => {
  const css = readComponent('badge', 'badge.css');
  assert.deepEqual(colorVarsOf(css), ['surface-brand', 'text-on-brand']);
  assert.match(declarationsOf(css, '.tl-badge') ?? '', /--tl-radius-full/);
});

test('Divider: Trennlinie ohne Kontrastschuld, horizontal und vertikal', () => {
  const css = readComponent('divider', 'divider.css');
  assert.deepEqual(colorVarsOf(css), ['border-separator']);
  assert.ok(declarationsOf(css, '.tl-divider--vertical'));
});

test('Skeleton: vertiefte Fläche, Puls über duration-loop, keine eigene Reduced-Motion-Regel', () => {
  const css = readComponent('skeleton', 'skeleton.css');
  assert.deepEqual(colorVarsOf(css), ['surface-interactive-strong']);
  assert.match(css, /--tl-duration-loop/);
  assert.doesNotMatch(css, /prefers-reduced-motion/);
});

for (const name of ['alert', 'badge', 'divider', 'skeleton']) {
  test(`${name}: Specimen und Vertrag`, () => {
    assert.deepEqual(specimenProblems(readComponent(name, `${name}.spec.html`)), []);
    if (name === 'badge') assert.ok(readComponent(name, 'badge.spec.html').includes('role="img"'), 'Badge mit aria-label braucht role="img"');
    const css = readComponent(name, `${name}.css`);
    const md = readComponent(name, `${name}.contract.md`);
    assert.deepEqual(missingInContract(md, [...varsOf(css), `tl-${name}`]), []);
    // Der Alert hat seit master #29652 ein Control (TLAlert), der Skeleton seinen ersten Aufrufer (die Tabelle); Badge und Divider noch nicht.
    if (name === 'alert') assert.match(md, /TLAlert/, 'alert: Vertrag nennt sein Control nicht');
    else if (name !== 'skeleton') assert.match(md, /kein Control/i, `${name}: Vertrag sagt nicht, dass es noch kein Control gibt`);
  });
}

// Entscheidung 07.10.: der TLAlert-Block des alten Stylesheets (#29652) geht mit 3b ins Paket.
test('Alert: Körper, Text, Aktionen und Schließen-Knopf stehen im Paket, eine Regel je Element', () => {
  const css = readGenerated('alert', 'alert.css');
  const body = declarationsOf(css, '.tl-alert__body') ?? '';
  for (const d of [/display:\s*flex/, /flex-direction:\s*column/, /gap:\s*var\(--tl-space-2xs\)/, /min-width:\s*0/]) assert.match(body, d);
  assert.equal((css.replace(/\/\*[\s\S]*?\*\//g, '').match(/\.tl-alert__body\s*\{/g) ?? []).length, 1, 'tl-alert__body genau einmal');
  assert.match(declarationsOf(css, '.tl-alert__message') ?? '', /white-space:\s*pre-line/);
  assert.match(declarationsOf(css, '.tl-alert__actions') ?? '', /padding-block-start:\s*var\(--tl-space-xs\)/);
  assert.match(declarationsOf(css, '.tl-alert__close') ?? '', /align-self:\s*start/);
  for (const e of ['body', 'actions']) assert.match(declarationsOf(css, `.tl-alert__${e}[hidden]`) ?? '', /display:\s*none/, e);
});

// Ein Theme darf das Icon durch das unsichtbare Bild ersetzen; dann rendert TLAlert keines. Die Spalten
// folgen darum dem Inhalt — Icon davor, Schließen-Knopf danach —, statt ein Icon vorauszusetzen.
test('Alert: die Spalten folgen dem Inhalt, mit und ohne Icon, mit und ohne Schließen-Knopf', () => {
  const css = readGenerated('alert', 'alert.css');
  const cols = (sel) => (declarationsOf(css, sel) ?? '').match(/grid-template-columns:\s*([^;]+);/)?.[1].trim();
  assert.equal(cols('.tl-alert'), 'minmax(0, 1fr)', 'nur der Körper');
  assert.equal(cols('.tl-alert:has(> .tl-alert__icon)'), 'auto minmax(0, 1fr)', 'Icon und Körper');
  assert.equal(cols('.tl-alert:has(> .tl-alert__close)'), 'minmax(0, 1fr) auto', 'Körper und Knopf');
  assert.equal(cols('.tl-alert:has(> .tl-alert__icon):has(> .tl-alert__close)'), 'auto minmax(0, 1fr) auto', 'Icon, Körper, Knopf');
});
