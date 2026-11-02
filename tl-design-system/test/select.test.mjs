import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync, readdirSync } from 'node:fs';
import { resolve } from '../tools/resolve.mjs';
import { readComponent, readGenerated, colorVarsOf, declarationsOf, specimenProblems, missingInContract, varsOf } from './lib/component.mjs';

const css = readComponent('select', 'select.css');
const P = JSON.parse(readFileSync(new URL('../src/primitives.json', import.meta.url)));
const T = JSON.parse(readFileSync(new URL('../src/tokens.json', import.meta.url)));

test('select.css liest genau diese Farbrollen', () => {
  assert.deepEqual(colorVarsOf(css), [
    'icon-secondary', 'surface-interactive-active', 'surface-interactive-hover',
    'surface-interactive-strong', 'text-placeholder',
  ]);
});

test('das Popup schwebt als tl-popover: der Vertrag nennt beide Klassen, kein z-index-Literal', () => {
  const md = readComponent('select', 'select.contract.md');
  assert.deepEqual(missingInContract(md, ['tl-popover tl-select__popup']), []);
  assert.doesNotMatch(css, /z-index/);
});

test('das Select-Popup trägt die Fläche nicht selbst: tl-popover liefert sie', () => {
  const d = declarationsOf(readGenerated('select', 'select.css'), '.tl-select__popup');
  assert.doesNotMatch(d, /shadow-popover|surface-overlay|layer-popover|border-radius|position/);
  assert.match(d, /max-height:\s*calc\(8 \* var\(--tl-size-row\)/);
});

test('Hervorhebung ist data-tl-state, das Ziehen zeichnet drag.css, gewählt ist aria-selected', () => {
  const hervor = declarationsOf(css, '.tl-select__option[data-tl-state~="highlighted"]');
  assert.ok(hervor, 'Hervorhebung fehlt');
  // Die Tastaturposition ist Fokus: kräftige neutrale Fläche und der Ring innen in der Form des Fokus; Markenton und
  // Kante heißen nur noch „gewählt“, Zeigen bleibt surface-interactive-hover.
  assert.match(hervor, /background:\s*var\(--tl-surface-interactive-strong\)/);
  // Der Ring ist der eine von base.css: der ::after der Option steht in dessen Selektorliste, die Komponente zeichnet keinen.
  assert.doesNotMatch(hervor, /box-shadow|focus|surface-selected|border|outline/);
  assert.match(declarationsOf(css, '.tl-select__option') ?? '', /position:\s*relative/);
  const base = readFileSync(new URL('../dist/base.css', import.meta.url), 'utf8');
  const ring = /([^{}]+)\{\s*outline:\s*var\(--tl-focus-width\) solid var\(--tl-focus-ring\);/.exec(base);
  assert.ok(ring[1].includes('.tl-select__option[data-tl-state~="highlighted"]::after'), 'die Option steht in der Ring-Liste');
  const flaeche = [...base.matchAll(/\.tl-select__option\[data-tl-state~="highlighted"\]::after\s*\{([^}]*)\}/g)]
    .map((m) => m[1]).find((d) => /content:/.test(d)) ?? '';
  assert.ok(base.indexOf(flaeche) > base.indexOf(ring[0]), 'die Fläche steht nach dem Ring: ihr Versatz nach innen gewinnt');
  assert.match(flaeche,
    /position:\s*absolute[\s\S]*inset:\s*0[\s\S]*outline-offset:\s*calc\(-1 \* var\(--tl-focus-offset\)\)[\s\S]*pointer-events:\s*none/);
  assert.match(declarationsOf(css, '.tl-select__option:hover') ?? '', /background:\s*var\(--tl-surface-interactive-hover\)/);
  assert.doesNotMatch(css.replace(/\/\*[\s\S]*?\*\//g, ''), /data-tl-state~="(dragging|drop-)/, 'die Ziehen-Optik steht in drag.css');
  // Der Griff ist ein Kreis wie das Entfernen (eine Regel .tl-select__chip > .tl-button); am Anfang der Pille
  // trägt sie dieselbe Lücke als Innenabstand wie am Ende (Entscheidung 07.10.: ein Modell).
  assert.match(declarationsOf(css, '.tl-select__chip:has(> .tl-select__drag-handle)') ?? '', /padding-inline-start:\s*calc\(var\(--tl-space-2xs\) \/ 2\)/);
  assert.doesNotMatch(css, /--highlighted|--dragging|--open|--drop/);
});

test('eine Option ist eine Zeile hoch, ein Treffer ist fett über die Typo-Klasse', () => {
  assert.match(declarationsOf(css, '.tl-select__option') ?? '', /min-height:\s*var\(--tl-size-row\)/);
  // Die Rolle surface-interactive-strong ist ein Name, kein Gewicht: Token-Namen fallen vor der Prüfung heraus.
  const ohneKommentare = css.replace(/\/\*[\s\S]*?\*\//g, '').replace(/--tl-[\w-]+/g, '');
  assert.doesNotMatch(ohneKommentare, /\bstrong\b|font-weight/);
});

test('Wurzel und Suchfeld schalten den Fokusring nicht ab', () => {
  assert.doesNotMatch(css, /outline\s*:\s*(none|0)/);
});

test('das Select-Specimen zeigt leer, einzeln mit Pill, mehrfach, offen, laden, Fehler, keine Treffer, Lesewert', () => {
  const html = readComponent('select', 'select.spec.html');
  assert.deepEqual(specimenProblems(html), []);
  for (const s of ['tl-select__placeholder', 'tl-select__value', 'tl-pill', 'tl-select__chip', 'aria-expanded="true"',
    'tl-popover tl-select__popup', 'tl-button--icon', 'data-tl-state="highlighted"', 'aria-busy="true"', 'tl-status', 'aria-invalid="true"',
    'tl-field-value', 'aria-activedescendant'])
    assert.ok(html.includes(s), `Specimen zeigt ${s} nicht`);
  // Die Liste führt nur ungewählte Optionen: keine trägt aria-selected="true", jede aria-selected="false".
  assert.doesNotMatch(html, /aria-selected="true"/);
  const optionen = [...html.matchAll(/<[^>]*role="option"[^>]*>/g)].map((m) => m[0]);
  assert.ok(optionen.length > 0, 'Specimen zeigt keine Option');
  for (const o of optionen) assert.match(o, /aria-selected="false"/, o);
});

test('der Select-Vertrag nennt Tokens, Klassen, Attribute und die Tastatur', () => {
  const md = readComponent('select', 'select.contract.md');
  const names = [...varsOf(css), 'tl-select', 'tl-field', 'role="combobox"', 'aria-expanded', 'aria-activedescendant',
    'aria-selected', 'role="listbox"', 'role="option"', 'data-tl-state', 'aria-invalid', 'colorRole',
    'ArrowDown', 'ArrowUp', 'Enter', 'Escape', 'Backspace', 'valueChanged', 'loadOptions', 'goto',
    'tl-select__search-group'];
  assert.deepEqual(missingInContract(md, names), []);
});

test('der Abstand unter dem Suchfeld sitzt an seiner Gruppe, nicht am Feld', () => {
  assert.match(declarationsOf(css, '.tl-select__search-group') ?? '', /margin-bottom/);
  assert.equal(declarationsOf(css, '.tl-select__search'), null);
});

test('Entfernen und Löschen sind tl-button--icon: die eigenen Knopfklassen gibt es in src/ nicht mehr', () => {
  const src = new URL('../src/', import.meta.url);
  const treffer = readdirSync(src, { recursive: true }).filter((f) => /\.(css|html|md|json)$/.test(f))
    .filter((f) => /tl-select__chip-remove|tl-select__clear/.test(readFileSync(new URL(f, src), 'utf8')));
  assert.deepEqual(treffer, []);
  const md = readComponent('select', 'select.contract.md');
  assert.deepEqual(missingInContract(md, ['tl-button--icon']), []);
});

test('ein Feld mit entfernbaren Chips bleibt size-control hoch, in jeder Dichte', () => {
  const R = resolve(P, T);
  const px = (v) => (v.endsWith('rem') ? parseFloat(v) * 16 : v.endsWith('px') ? parseFloat(v) : NaN);
  const rand = px(R.measure['border-width-sm']);
  for (const [dichte, d] of Object.entries(R.density)) {
    const feld = px(d['size-control']);
    const pille = px(d['size-control-sm']);
    const innen = (feld - pille) / 2 - rand; // padding-block von .tl-select
    assert.ok(innen >= 0, `${dichte}: Innenabstand ${innen}px < 0`);
    assert.ok(pille + 2 * innen + 2 * rand <= feld, `${dichte}: ${pille} + 2 × ${innen} + 2 × ${rand} > ${feld}`);
  }
  // Die Pille wächst nicht: der Knopf (tl-button--icon --sm) ist size-control-sm hoch und ragt um die
  // Randbreite über ihren Inhalt.
  const button = readComponent('button', 'button.css');
  assert.match(declarationsOf(button, '.tl-button--icon.tl-button--sm') ?? '', /height:\s*var\(--tl-size-control-sm\)/);
  // Der Entfernen-Knopf im Chip ist kleiner als die Pille und liegt in ihr (Test unten).
  // Die Lesewert-Liste im Formular steht so hoch wie ein Feld.
  const form = readComponent('form', 'form.css');
  const liste = declarationsOf(form, '.tl-form-field__input > .tl-select__values') ?? '';
  assert.match(liste, /min-height:\s*var\(--tl-size-control\)/);
  assert.match(liste, /align-items:\s*center/);
});

test('Knöpfe im Feld (06.10.): Löschen und Pfeil zeigen selected und drücken active, am Ende space-2xs', () => {
  assert.match(declarationsOf(css, '.tl-select') ?? '', /padding-inline-end:\s*var\(--tl-space-2xs\)/);
  assert.match(declarationsOf(css, '.tl-select__controls > .tl-button--ghost:not(:disabled):hover') ?? '', /background:\s*var\(--tl-surface-interactive-strong\)/);
  assert.match(declarationsOf(css, '.tl-select__controls > .tl-button--ghost:not(:disabled):active') ?? '', /background:\s*var\(--tl-surface-interactive-active\)/);
  const pfeil = declarationsOf(css, '.tl-select__arrow') ?? '';
  for (const r of [/width:\s*var\(--tl-size-control-sm\)/, /height:\s*var\(--tl-size-control-sm\)/,
    /border-radius:\s*var\(--tl-radius-sm\)/, /justify-content:\s*center/, /color:\s*var\(--tl-icon-secondary\)/])
    assert.match(pfeil, r);
  assert.match(declarationsOf(css, '.tl-select:not([aria-disabled="true"]) .tl-select__arrow:hover') ?? '', /background:\s*var\(--tl-surface-interactive-strong\)/);
  assert.match(declarationsOf(css, '.tl-select:not([aria-disabled="true"]) .tl-select__arrow:active') ?? '', /background:\s*var\(--tl-surface-interactive-active\)/);
  // Der Pfeil ist ein span um das Icon, wie TLDropdownSelect ihn rendert.
  const html = readComponent('select', 'select.spec.html');
  assert.match(html, /<span class="tl-select__arrow" aria-hidden="true"><i class="tl-icon-sm bi bi-chevron-(down|up)"><\/i><\/span>/);
  assert.doesNotMatch(html, /<i [^>]*tl-select__arrow/);
});

test('Knöpfe im Feld (06.10.): der Entfernen-Knopf im Chip zeigt und drückt active', () => {
  for (const zustand of ['hover', 'active'])
    assert.match(declarationsOf(css, `.tl-select__chip > .tl-button--ghost:not(:disabled):${zustand}`) ?? '',
      /background:\s*var\(--tl-surface-interactive-active\)/, zustand);
});

test('der Entfernen-Knopf liegt ganz im Pillenrand: Kreis ohne negativen Rand, konzentrisch zum runden Ende', () => {
  const knopf = declarationsOf(css, '.tl-select__chip > .tl-button') ?? '';
  assert.doesNotMatch(knopf, /margin/, 'kein Rand, der den Knopf über den Pillenrand zieht');
  assert.doesNotMatch(css.replace(/\/\*[\s\S]*?\*\//g, ''), /calc\(-1 \* var\(--tl-border-width-sm\)\)/);
  const mass = 'calc(var(--tl-size-control-sm) - 2 * var(--tl-border-width-sm) - var(--tl-space-2xs))';
  for (const p of ['width', 'height']) assert.ok(knopf.includes(`${p}: ${mass}`), `${p} fehlt`);
  assert.match(knopf, /border-radius:\s*var\(--tl-radius-round\)/);
  // Lücke oben und unten = (Innenhöhe − Durchmesser) / 2 = space-2xs / 2; am Ende derselbe Innenabstand.
  assert.match(declarationsOf(css, '.tl-select__chip:has(> .tl-button)') ?? '', /padding-inline-end:\s*calc\(var\(--tl-space-2xs\) \/ 2\)/);
  const R = resolve(P, T);
  const px = (v) => (v.endsWith('rem') ? parseFloat(v) * 16 : parseFloat(v));
  const rand = px(R.measure['border-width-sm']);
  const luecke = px(R.measure['space-2xs']) / 2;
  for (const [dichte, d] of Object.entries(R.density)) {
    const pille = px(d['size-control-sm']);
    const kreis = pille - 2 * rand - 2 * luecke;
    assert.ok(luecke > 0 && kreis >= px(R.measure['size-icon-sm']), `${dichte}: Kreis ${kreis}px kleiner als das Icon`);
  }
});

test('das Komma zwischen zwei Lesewerten folgt dem Wert davor: der Trenner nimmt den Spalt der Reihe zurück (12.10.)', () => {
  // master #29693 rendert "Alpha, Beta" als tl-select__separator in der Flex-Reihe; ohne die Regel stünde der
  // Spalt vor dem Komma ("Alpha , Beta"). Die Regel ging beim master-Merge vom 09.10. verloren.
  const md = readComponent('select', 'select.contract.md');
  assert.deepEqual(missingInContract(md, ['tl-select__separator']), []);
  assert.match(declarationsOf(css, '.tl-select__values > .tl-select__separator') ?? '',
    /margin-inline-start:\s*calc\(-1 \* var\(--tl-space-2xs\)\)/);
  assert.match(declarationsOf(css, '.tl-select__values') ?? '', /gap:\s*var\(--tl-space-2xs\)/, 'der Spalt, den der Trenner zurücknimmt');
});
