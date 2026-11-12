import { afterEach, describe, expect, it } from 'vitest';

import { editableInRow, focusTargetInRow } from './tableRowFocus';

/** A table body with one row of the given cells, each the markup of one column. */
function body(cells: Record<string, string>): HTMLElement {
  const result = document.createElement('div');
  result.innerHTML = Object.entries(cells)
    .map(([col, html]) => `<div data-row="r1" data-col="${col}">${html}</div>`)
    .join('');
  document.body.appendChild(result);
  return result;
}

describe('the focus target of a row', () => {
  afterEach(() => {
    document.body.innerHTML = '';
  });

  it('is the first editable element when the row is entered from above', () => {
    const b = body({ name: '<input id="name">', active: '<input type="checkbox" id="active">' });
    expect(focusTargetInRow(b, 'r1')?.id).toBe('name');
  });

  it('is the last focusable element, an action button included, when the row is entered from below', () => {
    const b = body({
      name: '<input id="name">',
      upload: '<input type="file" style="display: none"><button id="upload">Upload</button>',
      actions: '<button id="delete">Delete</button>',
    });
    expect(focusTargetInRow(b, 'r1', { last: true })?.id).toBe('delete');
  });

  it('skips elements that cannot take the focus', () => {
    const b = body({
      name: '<input id="name">',
      upload: '<input type="file" id="file" style="display: none">',
      hiddenCell: '<span hidden><input id="hidden"></span>',
    });
    expect(focusTargetInRow(b, 'r1', { last: true })?.id).toBe('name');
  });

  it('finds no editable element in a row displaying its values only', () => {
    const b = body({
      active: '<input type="checkbox" aria-readonly="true">',
      upload: '<input type="file" style="display: none">',
      actions: '<button>Open</button>',
    });
    expect(editableInRow(b, 'r1')).toBeNull();
  });

  it('keeps the focus in the addressed column', () => {
    const b = body({ name: '<input id="name">', active: '<input type="checkbox" aria-readonly="true">' });
    expect(focusTargetInRow(b, 'r1', { col: 'active' })).toBeNull();
    expect(focusTargetInRow(b, 'r1', { col: 'name' })?.id).toBe('name');
  });
});
