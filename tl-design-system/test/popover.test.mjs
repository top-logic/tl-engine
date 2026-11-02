import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { readGenerated, declarationsOf, missingInContract, componentCssFiles, colorVarsOf, rulesOf } from './lib/component.mjs';

test('tl-popover: Overlay-Fläche, Schatten, Ebene popover, Maße aus den Tokens', () => {
  const d = declarationsOf(readGenerated('popover', 'popover.css'), '.tl-popover');
  for (const t of ['surface-overlay', 'border-separator', 'radius-md', 'shadow-popover', 'layer-popover',
    'size-popover-min', 'size-popover-max']) assert.match(d, new RegExp(`var\\(--tl-${t}\\)`));
});

test('tl-popover--content (#29659): bündig, Breite aus size-popover-content-max (size-14), höchstens Viewport', () => {
  const d = declarationsOf(readGenerated('popover', 'popover.css'), '.tl-popover--content');
  assert.match(d, /padding:\s*0/);
  assert.match(d, /max-width:\s*min\(var\(--tl-size-popover-content-max\),\s*calc\(var\(--tl-size-viewport-width\)/);
  const md = readFileSync(new URL('../src/components/popover/popover.contract.md', import.meta.url), 'utf8');
  assert.deepEqual(missingInContract(md, ['tl-popover--content', '--tl-size-popover-content-max']), []);
});

test('tl-menu__item: Zeilenhöhe size-row, Zustand nur als Attribut, In-Kraft über die Typo-Klasse', () => {
  const css = readGenerated('menu', 'menu.css');
  assert.match(declarationsOf(css, '.tl-menu__item'), /min-height:\s*var\(--tl-size-row\)/);
  assert.doesNotMatch(css, /--(focused|active|disabled)\b/);
  assert.doesNotMatch(css, /font-weight/);
  assert.doesNotMatch(declarationsOf(css, '.tl-menu__item'), /(^|;)\s*font\s*:/);
  const md = readFileSync(new URL('../src/components/menu/menu.contract.md', import.meta.url), 'utf8');
  assert.deepEqual(missingInContract(md, ['aria-current', 'tl-type-body-strong', 'tl-menu-region', 'data-tl-trigger']), []);
});

test('nur tl-popover setzt die Popup-Fläche: kein anderer Block liest shadow-popover', () => {
  const hits = [];
  for (const [name, css] of componentCssFiles())
    // Ausnahme 19.10. (Welle 5b): der Knopf über einer Fläche (tl-overlay__action) schwebt wie ein Popover, ohne Portal.
    // Ausnahme 20.10. (Welle 5d, Plan-Review K4): der Tooltip schwebt am Anker wie ein Popover, auf seiner inversen
    // Fläche; drop-shadow() nimmt die zweischichtige Liste nicht, darum box-shadow aus derselben Rolle.
    // Ausnahme 29.10. (#29658): die schwebenden Aktionen (tl-fab) liegen über dem Körper eines Rahmens wie der Knopf über
    // einer Fläche, ohne Portal aus dem Rahmen heraus.
    if (!name.startsWith('popover/') && !['overlay/overlay.css', 'tooltip/tooltip.css', 'floating-action/floating-action.css'].includes(name)
      && /--tl-shadow-popover/.test(css)) hits.push(name);
  assert.deepEqual(hits, []);
});

test('tl-menu__item--danger: genau die drei Paare von tl-button--ghost.tl-button--danger', () => {
  const menu = readGenerated('menu', 'menu.css');
  const button = readGenerated('button', 'button.css');
  const paar = (css, sel) => colorVarsOf(declarationsOf(css, sel) ?? '');
  assert.deepEqual(paar(menu, '.tl-menu__item--danger'), ['status-error-text']);
  assert.deepEqual(paar(menu, '.tl-menu__item--danger'), paar(button, '.tl-button--danger.tl-button--ghost'));
  assert.deepEqual(paar(menu, '.tl-menu__item--danger:hover'), ['status-error-surface', 'text-on-status-error']);
  assert.deepEqual(paar(menu, '.tl-menu__item--danger:hover'), paar(button, '.tl-button--danger.tl-button--ghost:hover'));
  assert.deepEqual(paar(menu, '.tl-menu__item--danger:active'), ['status-error-surface-active', 'text-on-status-error-active']);
  assert.deepEqual(paar(menu, '.tl-menu__item--danger:active'), paar(button, '.tl-button--danger.tl-button--ghost:active'));
  const md = readFileSync(new URL('../src/components/menu/menu.contract.md', import.meta.url), 'utf8');
  assert.deepEqual(missingInContract(md, ['tl-menu__item--danger', 'tone']), []);
});

test('tl-menu__item--danger: deaktiviert steht nach dem Ton, der Ton nach dem Eintrag', () => {
  // Gleiche Spezifität (0,2,0): ohne diese Reihenfolge färbte Zeigen einen deaktivierten Eintrag rot,
  // oder der Grundeintrag überschriebe das Rot.
  const order = rulesOf(readGenerated('menu', 'menu.css')).map(([sel]) => sel);
  const ton = ['.tl-menu__item--danger', '.tl-menu__item--danger:hover', '.tl-menu__item--danger:active'].map((s) => order.indexOf(s));
  assert.ok(ton.every((i) => i >= 0), 'eine Regel des Tons fehlt');
  assert.ok(order.indexOf('.tl-menu__item:active') < Math.min(...ton), 'der Ton steht vor dem Grundeintrag');
  assert.ok(order.indexOf('.tl-menu__item:disabled') > Math.max(...ton), 'deaktiviert steht vor dem Ton');
});
