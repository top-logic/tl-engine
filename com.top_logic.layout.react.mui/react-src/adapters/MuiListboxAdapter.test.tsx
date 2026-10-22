// The part Listbox drawn with Material UI: a list of options on a Paper floating at its anchor.

import React from 'react';
import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup, render, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import type { ListboxPartProps } from 'tl-react-bridge';
import { createMuiRoot, createPageThemes } from '../MuiRoot';
import MuiListboxAdapter from './MuiListboxAdapter';

/** The root wrapper the adapter is drawn below: the one of the MUI default theme. */
const MuiRoot = createMuiRoot(createPageThemes({}));

/** The entries of the list. */
const ENTRIES = [{ id: 'l1-0', label: 'Küche' }, { id: 'l1-1', label: 'Serverraum' }];

/** The element the list stands at, holding the focus. */
let anchor: HTMLInputElement;

function draw(props: Partial<ListboxPartProps>) {
  anchor = document.createElement('input');
  document.body.appendChild(anchor);
  anchor.focus();
  render(
    <MuiRoot>
      <MuiListboxAdapter id="l1" anchor={anchor} entries={ENTRIES} onPick={() => {}} aria-label="Vorschläge"
        {...props} />
    </MuiRoot>,
  );
}

afterEach(() => {
  cleanup();
  anchor?.remove();
});

describe('the part Listbox as MUI List', () => {
  it('draws a listbox of options with the IDs of the entries, the active one selected', () => {
    draw({ activeIndex: 1, className: 'x', 'aria-describedby': 'hint' });

    const list = screen.getByRole('listbox', { name: 'Vorschläge' });
    expect(list.id).toBe('l1');
    expect(list.classList).toContain('MuiList-root');
    expect(list.classList).toContain('x');
    expect(list.getAttribute('aria-describedby')).toBe('hint');
    expect(list.closest('.MuiPaper-root')).not.toBeNull();
    const options = screen.getAllByRole('option');
    expect(options.map(option => option.id)).toEqual(['l1-0', 'l1-1']);
    expect(options.map(option => option.textContent)).toEqual(['Küche', 'Serverraum']);
    expect(options.map(option => option.getAttribute('aria-selected'))).toEqual(['false', 'true']);
    expect(options[1].classList).toContain('Mui-selected');
    // The entries are not in the tab order: the element controlling the list keeps the focus.
    expect(options.map(option => option.getAttribute('tabindex'))).toEqual(['-1', '-1']);
  });

  it('marks no entry selected without an active one', () => {
    draw({});

    expect(screen.getAllByRole('option').map(option => option.getAttribute('aria-selected')))
      .toEqual(['false', 'false']);
  });

  it('picks an entry by click, leaving the focus in the anchor', async () => {
    const onPick = vi.fn();
    const onActivate = vi.fn();
    draw({ onPick, onActivate });

    await userEvent.click(screen.getByRole('option', { name: 'Serverraum' }));

    expect(onPick.mock.calls).toEqual([[1]]);
    expect(onActivate).toHaveBeenCalledWith(1);
    expect(document.activeElement).toBe(anchor);
  });

  it('is dismissed by a press outside of it and its anchor, not by one into it', () => {
    const onDismiss = vi.fn();
    draw({ onDismiss });

    fireEvent.pointerDown(screen.getByRole('option', { name: 'Küche' }));
    fireEvent.pointerDown(anchor);
    expect(onDismiss).not.toHaveBeenCalled();

    fireEvent.pointerDown(document.body);
    expect(onDismiss).toHaveBeenCalledTimes(1);
  });
});
