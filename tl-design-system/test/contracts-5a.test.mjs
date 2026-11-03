// Die Verträge der Welle 5a nennen ihre Pflichtbegriffe; die neuen Specimen bestehen den Harness.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { missingInContract, specimenProblems } from './lib/component.mjs';
const datei = (d, f) => readFileSync(new URL(`../src/components/${d}/${f}`, import.meta.url), 'utf8');
const SOLL = {
  'swatch/swatch.contract.md': ['tl-swatch', 'tl-swatch--color', 'tl-swatch--wide', 'data-tl-state~="empty"',
    'aria-invalid', 'aria-required', '--tl-border-control'],
  'picker/picker.contract.md': ['tl-picker', 'tl-popover', 'role="dialog"', 'aria-label', 'tl-tabs', 'tl-button-bar',
    'useTabList', 'PickerPopover', 'Escape'],
  'color-picker/color-picker.contract.md': ['role="listbox"', 'role="option"', 'role="slider"', 'aria-valuetext',
    'aria-orientation', 'useRovingFocus', 'valueChanged', 'paletteChanged', 'tl-swatch'],
  'icon-picker/icon-picker.contract.md': ['role="listbox"', 'role="option"', 'aria-selected', 'tl-status', 'loadIcons',
    'valueChanged', 'useRovingFocus'],
  'slider/slider.contract.md': ['tl-slider', 'tl-slider__fill', 'aria-valuetext', 'aria-invalid', 'aria-required',
    'data-tl-state~="warning"', 'valueChanged'],
  'tabs/tabs.contract.md': ['useTabList'],
  'popover/popover.contract.md': ['tl-picker'],
  'button-bar/button-bar.contract.md': ['tl-picker'],
};
const SPECIMEN = ['swatch', 'picker', 'color-picker', 'icon-picker', 'slider'];

for (const name of ['color-picker', 'icon-picker'])
  test(`${name}.spec.html: genau eine Option der Palette bzw. des Rasters hat tabindex="0"`, () => {
    const html = datei(name, `${name}.spec.html`);
    const raster = html.match(/role="listbox"[\s\S]*?<\/section>/)[0];
    const optionen = [...raster.matchAll(/<[^>]*role="option"[^>]*>/g)].map((m) => m[0]);
    assert.ok(optionen.length > 1);
    assert.equal(optionen.filter((o) => /tabindex="0"/.test(o)).length, 1);
    assert.ok(optionen.filter((o) => !/tabindex="0"/.test(o)).every((o) => /tabindex="-1"/.test(o)));
  });

for (const [file, names] of Object.entries(SOLL)) test(`${file} nennt ${names.length} Pflichtbegriffe`, () => {
  const [d, f] = file.split('/');
  assert.deepEqual(missingInContract(datei(d, f), names), []);
});
for (const name of SPECIMEN)
  test(`${name}.spec.html besteht den Specimen-Harness`, () => assert.deepEqual(specimenProblems(datei(name, `${name}.spec.html`)), []));
