// Kein Vorfahr schneidet einen Fokusring ab (docs/skalen.md „Fokus“, „Gemessen statt gelesen“). Statisch ist das
// nicht prüfbar — ob ein Behälter schneidet, hängt an Lage, Innenabstand und Dichte, nicht an seinem
// overflow allein. Darum misst der Test im Browser: jede Specimen-Seite, beide Dichten, jedes fokussierbare
// Element einmal mit Tastaturfokus. Der Ring reicht outline-offset + outline-width über das Element hinaus
// (nach innen: gar nicht); liegt er hinter der Kante eines Vorfahren, der schneidet (overflow nicht
// visible; clip zählt seinen overflow-clip-margin), ist das ein Befund.
//
// Nicht gemessen wird, wohin Tastaturfokus nie kommt: tabindex="-1" unter einem Behälter mit
// aria-activedescendant (der Cursor ist virtuell, der Ring liegt am Cursor-Element, base.css). Nicht als
// Schneider zählt das Gerüst der Specimen-Seite — ein Vorfahr ohne tl-Klasse gehört nicht zum Paket.
import { test, before, after } from 'node:test';
import assert from 'node:assert/strict';
import { readdirSync } from 'node:fs';
import { specimenServer } from '../tools/serve.mjs';
import { launch } from './lib/browser.mjs';

const COMPONENTS = new URL('../src/components/', import.meta.url);
const PAGES = ['/specimen/index.html', ...readdirSync(COMPONENTS, { withFileTypes: true })
  .filter((d) => d.isDirectory())
  .flatMap((d) => readdirSync(new URL(`${d.name}/`, COMPONENTS)).filter((f) => f.endsWith('.spec.html'))
    .map((f) => `/src/components/${d.name}/${f}`))];

const MEASURE = (density) => `(() => {
  document.documentElement.dataset.tlDensity = ${JSON.stringify(density)};
  // Eingänge (@starting-style) sind Übergänge: gemessen wird der Endstand, nicht der Weg dorthin.
  for (const a of document.getAnimations()) if (a instanceof CSSTransition) a.finish();
  const FOCUSABLE = 'a[href], button, input, select, textarea, summary, [tabindex], [contenteditable]';
  const name = (e) => e.tagName.toLowerCase() + [...e.classList].filter((c) => c.startsWith('tl-')).map((c) => '.' + c).join('');
  const findings = [];
  let rings = 0;
  for (const f of document.querySelectorAll(FOCUSABLE)) {
    if (!f.getBoundingClientRect().width) continue;
    if (f.getAttribute('tabindex') === '-1' && f.parentElement?.closest('[aria-activedescendant]')) continue;
    f.focus({ focusVisible: true });
    if (document.activeElement !== f) continue;
    if (!f.matches(':focus-visible')) { findings.push(name(f) + ': Tastaturfokus ohne :focus-visible'); f.blur(); continue; }
    const s = getComputedStyle(f);
    const reach = s.outlineStyle === 'none' ? 0 : Math.max(0, parseFloat(s.outlineOffset) + parseFloat(s.outlineWidth));
    const k = f.getBoundingClientRect();
    f.blur();
    rings++;
    if (!reach) continue;
    for (let el = f.parentElement; el && el !== document.body; el = el.parentElement) {
      if (![...el.classList].some((c) => c.startsWith('tl-'))) continue;
      const c = getComputedStyle(el);
      const axes = [c.overflowX, c.overflowY];
      if (axes.every((a) => a === 'visible')) continue;
      const margin = axes.every((a) => a === 'clip') ? parseFloat(c.overflowClipMargin) || 0 : 0;
      const b = el.getBoundingClientRect();
      const left = b.left + el.clientLeft - margin, top = b.top + el.clientTop - margin;
      const right = b.left + el.clientLeft + el.clientWidth + margin, bottom = b.top + el.clientTop + el.clientHeight + margin;
      const cut = { links: left - (k.left - reach), rechts: k.right + reach - right, oben: top - (k.top - reach), unten: k.bottom + reach - bottom };
      const sides = Object.entries(cut).filter(([, v]) => v >= 0.5).map(([s, v]) => s + ' ' + Math.round(v) + 'px');
      if (sides.length) findings.push(name(el) + ' (' + axes.join('/') + ') schneidet den Ring von ' + name(f) + ': ' + sides.join(', '));
    }
  }
  return { rings, findings: [...new Set(findings)] };
})()`;

let server, browser, origin;
before(async () => {
  server = specimenServer();
  await new Promise((resolve) => server.listen(0, '127.0.0.1', resolve));
  origin = `http://127.0.0.1:${server.address().port}`;
  browser = await launch();
});
after(async () => {
  // Der Server schließt auch, wenn der Browser beim Beenden scheitert; offen hielte er den Prozess am Leben.
  try {
    await browser?.close();
  } finally {
    await new Promise((resolve) => (server ? server.close(resolve) : resolve()));
  }
});

test('kein Vorfahr schneidet einen Fokusring ab — jede Specimen-Seite, beide Dichten', async () => {
  const findings = [];
  let rings = 0;
  for (const page of PAGES) {
    await browser.goto(origin + page);
    for (const density of ['normal', 'compact']) {
      const r = await browser.evaluate(MEASURE(density));
      rings += r.rings;
      findings.push(...r.findings.map((f) => `${page} [${density}] ${f}`));
    }
  }
  // Misst der Test keinen einzigen Ring, prüft er nichts — etwa wenn focus() die Tastaturheuristik verfehlt.
  assert.ok(rings > 100, `nur ${rings} Ringe gemessen`);
  assert.deepEqual(findings, []);
});
