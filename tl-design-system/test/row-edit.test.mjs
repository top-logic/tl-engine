// Felder in der bearbeiteten Tabellenzeile (#30383): die Zeile behält ihre Höhe, das Feld ist eine Stufe niedriger und
// sitzt in ganzen Pixeln, der Text steht wie beim Lesen, auf der gewählten Zeile trägt border-emphasis den Rahmen.
// Gemessen im Browser an table.spec.html, Panel „Bearbeitete Zeilen“, beide Dichten (Plan 2026-11-17 PR 1, Schritt 5).
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

// Alles in Pixeln relativ zur Zelle bzw. Zeile; Token-Maße über ein Probe-Element, nicht über Rechnen im Test.
const MEASURE = (density) => `(() => {
  document.documentElement.dataset.tlDensity = '${density}';
  const px = (v) => { const e = document.createElement('i'); e.style.cssText = 'position:absolute;width:var(' + v + ')'; document.body.append(e);
    const w = e.getBoundingClientRect().width; e.remove(); return w; };
  const color = (v) => { const e = document.createElement('i'); e.style.color = 'var(' + v + ')'; document.body.append(e);
    const c = getComputedStyle(e).color; e.remove(); return c; };
  const panel = document.querySelector('#ed-panel');
  const out = { row: px('--tl-size-row'), field: px('--tl-size-control') - px('--tl-space-2xs'),
    top: px('--tl-space-2xs') - px('--tl-border-width-sm'), emphasis: color('--tl-border-emphasis'), control: color('--tl-border-control'),
    interactive: color('--tl-border-interactive'), rows: {} };
  const read = {};
  for (const c of panel.querySelectorAll('#ed-read .tl-table__cell')) {
    const cr = c.getBoundingClientRect(), t = c.firstElementChild.getBoundingClientRect();
    read[c.dataset.test.split('-').pop()] = { start: t.left - cr.left, end: cr.right - t.right };
  }
  for (const row of panel.querySelectorAll('.tl-table__row')) {
    const rr = row.getBoundingClientRect();
    const o = { height: rr.height, fields: {} };
    for (const c of row.querySelectorAll('.tl-table__cell')) {
      const f = c.querySelector('.tl-field');
      if (!f) continue;
      const key = c.dataset.test.split('-').pop(), cr = c.getBoundingClientRect(), fr = f.getBoundingClientRect(), cs = getComputedStyle(f);
      // Der Textanfang der Auswahl ist ihr Wert, der der Eingabe ihre linke Kante plus Innenabstand.
      const value = f.querySelector('.tl-select__value')?.getBoundingClientRect();
      o.fields[key] = { height: fr.height, top: fr.top - rr.top, framed: cs.boxShadow !== 'none',
        shadow: cs.boxShadow, background: cs.backgroundColor,
        start: (value ? value.left : fr.left + parseFloat(cs.paddingLeft)) - cr.left,
        end: cr.right - (fr.right - parseFloat(cs.paddingRight)), read: read[key] };
    }
    out.rows[row.id] = o;
  }
  return out;
})()`;

for (const density of ['normal', 'compact']) {
  test(`Bearbeitete Zeile [${density}]: Zeilenhöhe, Feldhöhe und Lage in ganzen Pixeln`, async () => {
    await browser.goto(origin + '/src/components/table/table.spec.html');
    const m = await browser.evaluate(MEASURE(density));
    assert.equal(Object.keys(m.rows).length, 5, 'Zeilen im Panel');
    const falsch = [];
    for (const [id, row] of Object.entries(m.rows)) {
      if (Math.abs(row.height - m.row) > 0.5) falsch.push(`${id}: Zeile ${row.height} px statt ${m.row}`);
      for (const [key, f] of Object.entries(row.fields)) {
        if (Math.abs(f.height - m.field) > 0.5) falsch.push(`${id}/${key}: Feld ${f.height} px statt ${m.field}`);
        if (Math.abs(f.top - m.top) > 0.01) falsch.push(`${id}/${key}: Feld ${f.top} px unter der Oberkante statt ${m.top}`);
      }
    }
    assert.deepEqual(falsch, []);
  });

  test(`Bearbeitete Zeile [${density}]: der Text steht wie beim Lesen, auch im readonly-Feld`, async () => {
    await browser.goto(origin + '/src/components/table/table.spec.html');
    const m = await browser.evaluate(MEASURE(density));
    const falsch = [];
    for (const [id, row] of Object.entries(m.rows))
      for (const [key, f] of Object.entries(row.fields)) {
        // Zahlenspalte: die rechte Kante der Ziffern; Datum und Auswahl tragen am Ende den Knopf und sind nur am Anfang gleich.
        const zahl = key === 'menge';
        const soll = zahl ? f.read.end : f.read.start, ist = zahl ? f.end : f.start;
        if (Math.abs(soll - ist) > 0.5) falsch.push(`${id}/${key}: Text ${zahl ? 'endet' : 'beginnt'} bei ${ist} px statt ${soll}`);
      }
    assert.deepEqual(falsch, []);
  });

  test(`Bearbeitete Zeile [${density}]: Rahmen in border-emphasis auf der gewählten Zeile, in border-control sonst, readonly ohne`, async () => {
    await browser.goto(origin + '/src/components/table/table.spec.html');
    const m = await browser.evaluate(MEASURE(density));
    const rahmen = (f) => f.shadow.slice(0, f.shadow.indexOf(')') + 1);
    for (const [key, f] of Object.entries(m.rows['ed-sel'].fields)) {
      assert.equal(rahmen(f), m.emphasis, `ed-sel/${key}`);
      assert.notEqual(rahmen(f), m.interactive, `ed-sel/${key} ist nicht Auswahl-Blau`);
    }
    // Eine nicht gewählte Zeile: border-control; der Fehler (leerer Name) gewinnt mit seinem Statusrahmen.
    for (const key of ['datum', 'status', 'menge']) assert.equal(rahmen(m.rows['ed-err'].fields[key]), m.control, `ed-err/${key}`);
    assert.notEqual(rahmen(m.rows['ed-err'].fields.name), m.control, 'ed-err/name trägt den Fehlerrahmen');
    for (const [key, f] of Object.entries(m.rows['ed-ro'].fields)) {
      assert.equal(f.framed, false, `ed-ro/${key} ohne Rahmen`);
      assert.equal(f.background, 'rgba(0, 0, 0, 0)', `ed-ro/${key} ohne Fläche`);
    }
  });

  test(`Bearbeitete Zeile [${density}]: Knöpfe im Feld lassen dessen Rahmen oben und unten frei`, async () => {
    await browser.goto(origin + '/src/components/table/table.spec.html');
    const m = await browser.evaluate(`(() => {
      document.documentElement.dataset.tlDensity = '${density}';
      const e = document.createElement('i'); e.style.cssText = 'position:absolute;width:var(--tl-border-width-sm)'; document.body.append(e);
      const bw = e.getBoundingClientRect().width; e.remove();
      const knoepfe = document.querySelectorAll('#ed-panel .tl-field-group > .tl-button, #ed-panel .tl-select__controls > .tl-button, #ed-panel .tl-compact-field > .tl-button');
      return { bw, list: [...knoepfe].map((k) => {
        const f = k.closest('.tl-field-group')?.querySelector('.tl-field') ?? k.closest('.tl-field');
        const kr = k.getBoundingClientRect(), fr = f.getBoundingClientRect();
        return { name: k.closest('.tl-table__cell').dataset.test + ' ' + [...k.classList].filter((c) => c.includes('__')).join(''),
          oben: kr.top - fr.top, unten: fr.bottom - kr.bottom };
      }) };
    })()`);
    assert.ok(m.list.some((k) => k.name.startsWith('ed-direct-status')), 'der Öffner des kompakten Felds wird gemessen');
    const falsch = m.list.filter((k) => k.oben < m.bw - 0.01 || k.unten < m.bw - 0.01)
      .map((k) => `${k.name}: oben ${k.oben} px, unten ${k.unten} px, Rahmen ${m.bw} px`);
    assert.deepEqual(falsch, []);
  });
}
