// Wer in der AppBar nachgibt (Spec 2026-10-16 Welle 4b §5): erst die überlaufende Toolbar bis zu ihrem Überlaufknopf,
// dann der Titel. Gemessen im Browser an app-bar.spec.html, Abschnitt #shrink, bei zwei Breiten.
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

const MESSE = (px) => `(() => {
  const stage = document.querySelector('#shrink');
  stage.style.width = '${px}px';
  const bar = stage.querySelector('.tl-app-bar');
  const title = bar.querySelector('.tl-app-bar__title');
  const actions = bar.querySelector('.tl-app-bar__actions');
  const control = parseFloat(getComputedStyle(document.documentElement).getPropertyValue('--tl-size-control')) * 16;
  return { titelGekuerzt: title.scrollWidth > title.clientWidth + 0.5, aktionen: actions.getBoundingClientRect().width, control };
})()`;

test('AppBar: bei 640 px ist der Titel ganz und die Toolbar breiter als ihr Knopf; bei 300 px hält die Toolbar am Knopf, der Titel kürzt', async () => {
  await browser.goto(origin + '/src/components/app-bar/app-bar.spec.html');
  const breit = await browser.evaluate(MESSE(640));
  assert.equal(breit.titelGekuerzt, false, 'Titel gekürzt, obwohl die Toolbar noch Platz hergeben kann');
  assert.ok(breit.aktionen > breit.control + 1, `Aktionen ${breit.aktionen} px, Knopf ${breit.control} px`);
  const schmal = await browser.evaluate(MESSE(300));
  assert.ok(Math.abs(schmal.aktionen - schmal.control) < 1, `Aktionen ${schmal.aktionen} px statt ${schmal.control} px`);
  assert.equal(schmal.titelGekuerzt, true, 'bei 300 px muss der Titel kürzen');
});
