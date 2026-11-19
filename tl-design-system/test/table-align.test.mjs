// Der Titel einer Zahlenspalte endet bündig mit ihren Zahlen (Review !2206): der Trichter ist ohne Zeigen
// unsichtbar, darf darum aber nicht zwischen Titel und Zellenende Platz halten. Auf einem Touch-Gerät, wo es kein
// Zeigen gibt, steht der Trichter immer (#30228); dort endet der Titel um Trichter und Abstand vor den Zahlen.
// Gemessen in table.spec.html.
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

const DENSITIES = ['normal', 'compact'];

const KANTEN = (density) => `(() => {
  document.documentElement.dataset.tlDensity = '${density}';
  for (const a of document.getAnimations()) if (a instanceof CSSTransition) a.finish();
  const head = document.querySelector('.tl-table__header-cell[data-tl-align="end"]');
  const cell = document.querySelector('.tl-table__cell[data-tl-align="end"] > span');
  return {
    titel: head.querySelector('.tl-table__label').getBoundingClientRect().right,
    zahl: cell.getBoundingClientRect().right,
    trichter: head.querySelector('.tl-table__filter').getBoundingClientRect().width,
    abstand: parseFloat(getComputedStyle(head).columnGap),
    soll: parseFloat(getComputedStyle(document.documentElement).getPropertyValue('--tl-size-control-sm')) * 16,
  };
})()`;

// Die bündige Kante gilt mit Maus; ohne sie misst der Test den Touch-Fall.
test('Test-Browser: Desktop mit Maus', async () => {
  await browser.goto(origin + '/src/components/table/table.spec.html');
  const media = await browser.evaluate(`['(hover: hover)', '(pointer: fine)'].map((q) => [q, matchMedia(q).matches])`);
  assert.deepEqual(media, [['(hover: hover)', true], ['(pointer: fine)', true]]);
});

for (const density of DENSITIES)
  test(`Zahlenspalte [${density}]: der Titel endet bündig mit den Zahlen`, async () => {
    await browser.goto(origin + '/src/components/table/table.spec.html');
    const { titel, zahl } = await browser.evaluate(KANTEN(density));
    assert.ok(Math.abs(titel - zahl) < 1, `Titel endet ${(zahl - titel).toFixed(1)} px vor den Zahlen`);
  });

for (const density of DENSITIES)
  test(`Zahlenspalte [${density}, Touch]: der Trichter steht, der Titel endet um ihn vor den Zahlen`, async () => {
    await browser.emulateTouch(true);
    try {
      await browser.goto(origin + '/src/components/table/table.spec.html');
      const { titel, zahl, trichter, abstand, soll } = await browser.evaluate(KANTEN(density));
      assert.ok(Math.abs(trichter - soll) < 0.5, `Trichter ${trichter} px statt ${soll}`);
      assert.ok(Math.abs(zahl - titel - (trichter + abstand)) < 1,
        `Titel endet ${(zahl - titel).toFixed(1)} px vor den Zahlen statt ${trichter + abstand}`);
    } finally {
      await browser.emulateTouch(false);
    }
  });

// Mit der Tastatur im Kopf fährt der Trichter auf: er ist zu sehen und so breit wie ein kleiner Icon-Knopf.
test('Zahlenspalte: Fokus auf dem Sortierknopf deckt den Trichter in voller Breite auf', async () => {
  await browser.goto(origin + '/src/components/table/table.spec.html');
  const { breite, soll } = await browser.evaluate(`(() => {
    const head = document.querySelector('.tl-table__header-cell[data-tl-align="end"]');
    head.querySelector('.tl-table__sort').focus({ focusVisible: true });
    for (const a of document.getAnimations()) if (a instanceof CSSTransition) a.finish();
    const soll = parseFloat(getComputedStyle(document.documentElement).getPropertyValue('--tl-size-control-sm')) * 16;
    return { breite: head.querySelector('.tl-table__filter').getBoundingClientRect().width, soll };
  })()`);
  assert.ok(Math.abs(breite - soll) < 0.5, `Trichter ${breite} px statt ${soll}`);
});
