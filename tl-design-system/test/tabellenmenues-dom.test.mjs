// Tabellenmenüs (#30102), gemessen im Browser: die Marke ohne Eingabe sieht aus wie die mit Eingabe, die Zeile füllt die
// Breite, und in einem gedeckelten Popover rollt genau ein Bereich — Kopf und Fuß stehen.
import { test, before, after } from 'node:test';
import assert from 'node:assert/strict';
import { specimenServer } from '../tools/serve.mjs';
import { launch } from './lib/browser.mjs';

let server, browser, origin;
before(async () => {
  server = specimenServer();
  await new Promise((resolve) => server.listen(0, '127.0.0.1', resolve));
  origin = `http://127.0.0.1:${server.address().port}`;
  browser = await launch();
});
after(async () => {
  try {
    await browser?.close();
  } finally {
    await new Promise((resolve) => (server ? server.close(resolve) : resolve()));
  }
});

// Eine Marke mit Eingabe und die gezeichnete in einer Zeile nebeneinander; verglichen werden die Stile, die das Bild machen.
const VERGLEICH = `(() => {
  const host = document.createElement('div');
  host.innerHTML = [
    ['', 'tl-checkbox tl-checkbox--switch', 'checked'], ['', 'tl-checkbox tl-checkbox--switch', ''],
    ['', 'tl-checkbox', 'checked'], ['', 'tl-checkbox', ''], ['', 'tl-checkbox', 'indeterminate'],
  ].map(([, cls, st], i) => '<input type="checkbox" id="in' + i + '" class="' + cls + '"' + (st === 'checked' ? ' checked' : '') + '>'
    + '<button type="button" role="menuitemcheckbox" id="dr' + i + '" class="tl-menu__item tl-menu__item--check" aria-checked="'
    + (st === 'checked' ? 'true' : st === 'indeterminate' ? 'mixed' : 'false') + '"><span class="' + cls + '" aria-hidden="true"></span></button>').join('');
  document.body.append(host);
  document.getElementById('in4').indeterminate = true;
  const PROPS = ['width', 'height', 'background-color', 'border-top-color', 'border-top-width', 'border-top-left-radius', 'box-shadow'];
  const AFTER = ['content', 'width', 'height', 'background-color', 'transform', 'margin-inline-start', 'border-top-width', 'border-right-width',
    'border-bottom-width', 'border-radius'];  // Farbe einer 0 px breiten Linie ist unsichtbar (erbt color des Elternteils)
  const lies = (el, pseudo, props) => { const cs = getComputedStyle(el, pseudo); return Object.fromEntries(props.map((p) => [p, cs.getPropertyValue(p)])); };
  const out = [];
  for (let i = 0; i < 5; i++) {
    const ein = document.getElementById('in' + i), dr = document.getElementById('dr' + i).firstElementChild;
    ein.style.transition = 'none'; dr.style.transition = 'none';
    out.push({ i, marke: [lies(ein, null, PROPS), lies(dr, null, PROPS)], nach: [lies(ein, '::after', AFTER), lies(dr, '::after', AFTER)] });
  }
  return out;
})()`;

test('Marke ohne Eingabe in tl-menu__item--check: Schalter und Kästchen, an, aus und gemischt, wie die Eingabe', async () => {
  await browser.goto(origin + '/src/components/filter-list/filter-list.spec.html');
  const paare = await browser.evaluate(VERGLEICH);
  assert.equal(paare.length, 5);
  for (const { i, marke, nach } of paare) {
    assert.deepEqual(marke[1], marke[0], `Fall ${i}: Marke`);
    assert.deepEqual(nach[1], nach[0], `Fall ${i}: ::after`);
  }
  // Der Schalter an steht rechts, der aus links: die Bewegung des Knopfs ist dabei (Fall 0 gegen 1).
  assert.notEqual(paare[0].nach[1].transform, paare[1].nach[1].transform);
  // Das Kästchen an trägt die Marke (Haken), das leere nicht.
  assert.equal(paare[2].nach[1].content, '""');
  assert.equal(paare[3].nach[1].content, 'none');
});

const ZEILE = `(() => {
  const z = document.querySelector('.tl-filter-list .tl-menu__item--check');
  const cs = getComputedStyle(z);
  return { display: cs.display, breite: z.getBoundingClientRect().width, eltern: z.parentElement.clientWidth,
    marke: getComputedStyle(z.querySelector('.tl-checkbox')).pointerEvents };
})()`;

test('Zeile mit Häkchen: Flex, volle Breite der Liste, Marke nimmt keinen Zeiger an', async () => {
  await browser.goto(origin + '/src/components/filter-list/filter-list.spec.html');
  const z = await browser.evaluate(ZEILE);
  assert.equal(z.display, 'flex');
  assert.ok(Math.abs(z.breite - z.eltern) < 1, `Zeile ${z.breite} px in ${z.eltern} px`);
  assert.equal(z.marke, 'none');
});

// Im gedeckelten Popover rollt allein der Rollbereich: das Popover selbst nicht, Suche und Fuß bleiben im Bild.
const ROLLEN = `(() => {
  const pop = document.querySelector('.tl-filter-list.specimen__capped');
  const scroll = pop.querySelector('.tl-picker__scroll'), fuss = pop.querySelector('.tl-filter-list__footer');
  const p = pop.getBoundingClientRect(), f = fuss.getBoundingClientRect();
  return { popRollt: pop.scrollHeight > pop.clientHeight + 1, scrollRollt: scroll.scrollHeight > scroll.clientHeight + 1,
    fussImPopover: f.bottom <= p.bottom + 0.5 && f.top >= p.top, hoehe: p.height, cap: parseFloat(getComputedStyle(pop).maxHeight) };
})()`;

test('Werteliste, gedeckelt: der Rollbereich rollt, das Popover nicht, der Fuß bleibt im Popover', async () => {
  await browser.goto(origin + '/src/components/filter-list/filter-list.spec.html');
  const r = await browser.evaluate(ROLLEN);
  assert.equal(r.scrollRollt, true);
  assert.equal(r.popRollt, false);
  assert.equal(r.fussImPopover, true);
  assert.ok(r.hoehe <= r.cap + 0.5, `${r.hoehe} px über Deckel ${r.cap}`);
});

const SPALTEN = `(() => {
  const pop = document.querySelector('.tl-table-view-options.specimen__capped');
  const seite = pop.querySelector('.tl-table-view-options__page'), liste = pop.querySelector('.tl-reorder-list');
  const host = pop.querySelector('.tl-reorder-list__host'), suche = pop.querySelector('.tl-reorder-list__search');
  const titel = pop.querySelector('.tl-table-view-options__title'), p = pop.getBoundingClientRect();
  return { popRollt: pop.scrollHeight > pop.clientHeight + 1, seiteRollt: seite.scrollHeight > seite.clientHeight + 1,
    hostRollt: host.scrollHeight > host.clientHeight + 1, listeRollt: liste.scrollHeight > liste.clientHeight + 1,
    sucheImBild: suche.getBoundingClientRect().top >= p.top && suche.getBoundingClientRect().bottom <= p.bottom,
    titelZeile: getComputedStyle(titel).display, hoehe: p.height, cap: parseFloat(getComputedStyle(pop).maxHeight) };
})()`;

test('Ansicht, Seite „Spalten“ gedeckelt: genau ein Rollbereich — die Liste; Titel mit Zurück und Suche bleiben stehen', async () => {
  await browser.goto(origin + '/src/components/table/table.spec.html');
  const r = await browser.evaluate(SPALTEN);
  assert.equal(r.listeRollt, true);
  assert.deepEqual([r.popRollt, r.seiteRollt, r.hostRollt], [false, false, false]);
  assert.equal(r.sucheImBild, true);
  assert.equal(r.titelZeile, 'flex');
  assert.ok(r.hoehe <= r.cap + 0.5, `${r.hoehe} px über Deckel ${r.cap}`);
});

test('Trichter mit offenem Popover (aria-expanded) bleibt sichtbar, auch ohne Zeigen', async () => {
  await browser.goto(origin + '/src/components/filter-list/filter-list.spec.html');
  const o = await browser.evaluate(`getComputedStyle(document.querySelector('.tl-reveal-on-hover[aria-expanded="true"]')).opacity`);
  assert.equal(o, '1');
});
