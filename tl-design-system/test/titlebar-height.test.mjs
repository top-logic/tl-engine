// Jeder Kopf ist size-bar hoch, mit und ohne Toolbar (Spec 2026-10-16 Welle 4c §3, §9), und eine überlaufende
// Toolbar gibt vor dem Titel nach. Gemessen im Browser an panel.spec.html und dialog.spec.html, beide Dichten.
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
  // Der Server schließt auch, wenn der Browser beim Beenden scheitert; offen hielte er den Prozess am Leben.
  try {
    await browser?.close();
  } finally {
    await new Promise((resolve) => (server ? server.close(resolve) : resolve()));
  }
});

const HOEHEN = (density) => `(() => {
  document.documentElement.dataset.tlDensity = '${density}';
  const bar = parseFloat(getComputedStyle(document.documentElement).getPropertyValue('--tl-size-bar')) * 16;
  const koepfe = [...document.querySelectorAll('.tl-titlebar')]
    .filter((t) => t.getBoundingClientRect().height > 0 && !t.closest('[data-tl-state~="collapsed"]'))
    .map((t) => [t.querySelector('.tl-titlebar__title')?.textContent ?? '(ohne Titel)', t.getBoundingClientRect().height]);
  return { bar, koepfe };
})()`;

for (const page of ['/src/components/panel/panel.spec.html', '/src/components/dialog/dialog.spec.html'])
  for (const density of ['normal', 'compact'])
    test(`${page} [${density}]: jeder Kopf ist size-bar hoch`, async () => {
      await browser.goto(origin + page);
      const { bar, koepfe } = await browser.evaluate(HOEHEN(density));
      assert.ok(koepfe.length > 0, 'kein Kopf gefunden');
      const falsch = koepfe.filter(([, h]) => Math.abs(h - bar) > 0.5).map(([n, h]) => `${n}: ${h} px statt ${bar}`);
      assert.deepEqual(falsch, []);
    });

const NACHGEBEN = (px) => `(() => {
  const stage = document.querySelector('#shrink');
  stage.style.width = '${px}px';
  const title = stage.querySelector('.tl-titlebar__title');
  const actions = stage.querySelector('.tl-titlebar__actions');
  const control = parseFloat(getComputedStyle(document.documentElement).getPropertyValue('--tl-size-control')) * 16;
  const toolbar = actions.querySelector(':scope > .tl-toolbar');
  return { titelGekuerzt: title.scrollWidth > title.clientWidth + 0.5, aktionen: actions.getBoundingClientRect().width, control,
    luecke: actions.getBoundingClientRect().right - toolbar.getBoundingClientRect().right };
})()`;

test('Panelkopf: bei 640 px ist der Titel ganz, bei 260 px hält die Toolbar am Knopf und der Titel kürzt', async () => {
  await browser.goto(origin + '/src/components/panel/panel.spec.html');
  const breit = await browser.evaluate(NACHGEBEN(640));
  assert.equal(breit.titelGekuerzt, false);
  assert.ok(breit.aktionen > breit.control + 1, `Aktionen ${breit.aktionen} px`);
  const schmal = await browser.evaluate(NACHGEBEN(260));
  assert.ok(Math.abs(schmal.aktionen - schmal.control) < 1, `Aktionen ${schmal.aktionen} px statt ${schmal.control}`);
  assert.equal(schmal.titelGekuerzt, true);
});

// #29918: die Aktionen nehmen beim Nachgeben den ganzen Rest; die Toolbar steht darin am Ende, nicht am Titel.
test('Panelkopf: die Toolbar steht am Ende des Kopfes, auch wenn die Aktionen den Rest nehmen', async () => {
  await browser.goto(origin + '/src/components/panel/panel.spec.html');
  const breit = await browser.evaluate(NACHGEBEN(1200));
  assert.ok(Math.abs(breit.luecke) < 1, `Toolbar endet ${breit.luecke} px vor dem Ende der Aktionen`);
});
