import { afterEach, describe, expect, it } from 'vitest';

import { isOperableTarget, operableSelector } from './interactive';

/** The selector of the editable elements of a table cell, as the table builds it. */
const EDITABLE = operableSelector('input, textarea, select, [contenteditable="true"]');

/** Puts the given markup into the document and returns its first element. */
function mount(html: string): HTMLElement {
  const host = document.createElement('div');
  host.innerHTML = html;
  document.body.appendChild(host);
  return host.firstElementChild as HTMLElement;
}

describe('operableSelector', () => {
  afterEach(() => {
    document.body.innerHTML = '';
  });

  it('matches a checkbox the user can tick', () => {
    const box = mount('<input type="checkbox">');
    expect(box.matches(EDITABLE)).toBe(true);
    expect(isOperableTarget(box)).toBe(true);
  });

  it('leaves out a checkbox displaying a value, marked aria-readonly', () => {
    const box = mount('<input type="checkbox" aria-readonly="true">');
    expect(box.matches(EDITABLE)).toBe(false);
    expect(isOperableTarget(box)).toBe(false);
  });

  it('leaves out disabled, read-only and aria-disabled inputs', () => {
    for (const html of ['<input disabled>', '<input readonly>', '<textarea readonly></textarea>',
      '<select disabled></select>', '<input aria-disabled="true">']) {
      const element = mount(html);
      expect(element.matches(EDITABLE), html).toBe(false);
      expect(isOperableTarget(element), html).toBe(false);
    }
  });

  it('finds the first operable element of a row past a read-only one', () => {
    const row = mount('<div><input type="checkbox" aria-readonly="true"><input type="text" id="name"></div>');
    expect(row.querySelector(EDITABLE)?.id).toBe('name');
  });
});
