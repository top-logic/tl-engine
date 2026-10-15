// The part Checkbox drawn with Material UI: the MUI Checkbox, the MUI Switch, a FormControlLabel
// around a box with a label.

import React from 'react';
import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup, render } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import type { CheckboxPartProps } from 'tl-react-bridge';
import { createMuiRoot, createPageThemes } from '../MuiRoot';
import MuiCheckboxAdapter from './MuiCheckboxAdapter';

/** The root wrapper the adapter is drawn below: the one of the MUI default theme. */
const MuiRoot = createMuiRoot(createPageThemes({}));

/** The accessible name of the box. */
const NAME = 'Auswählen';

function draw(props: Partial<CheckboxPartProps>) {
  const element = (next: Partial<CheckboxPartProps>) => (
    <MuiRoot><MuiCheckboxAdapter checked={false} aria-label={NAME} {...props} {...next} /></MuiRoot>
  );
  const result = render(element({}));
  return {
    redraw: (next: Partial<CheckboxPartProps>) => result.rerender(element(next)),
  };
}

/** The input of the box. */
function box(): HTMLInputElement {
  return screen.getByRole('checkbox') as HTMLInputElement;
}

afterEach(() => {
  cleanup();
});

describe('the part Checkbox as MUI Checkbox', () => {
  it('puts ID, name, tab index, ref and passed-through attributes on the input, the class on the root', () => {
    const ref = React.createRef<HTMLInputElement>();
    draw({ id: 'b1', ref, name: 'n', tabIndex: -1, className: 'x', checked: true, 'aria-describedby': 'hint' });

    const input = box();
    expect(input.id).toBe('b1');
    expect(ref.current).toBe(input);
    expect(input.name).toBe('n');
    expect(input.getAttribute('tabindex')).toBe('-1');
    expect(input.getAttribute('aria-label')).toBe(NAME);
    expect(input.getAttribute('aria-describedby')).toBe('hint');
    expect(input.checked).toBe(true);
    const root = input.closest('.MuiCheckbox-root')!;
    expect(root.classList).toContain('x');
    expect(input.classList).not.toContain('tl-checkbox');
  });

  it('calls the handler with the state asked for', async () => {
    const onChange = vi.fn();
    draw({ onChange });

    await userEvent.click(box());

    expect(onChange.mock.calls).toEqual([[true]]);
    // Controlled: the box shows the state it is given.
    expect(box().checked).toBe(false);
  });

  it('draws an indeterminate box and marks it mixed', async () => {
    const onChange = vi.fn();
    const { redraw } = draw({ indeterminate: true, onChange });

    expect(box().getAttribute('aria-checked')).toBe('mixed');
    expect(box().indeterminate).toBe(true);
    expect(box().closest('.MuiCheckbox-root')!.classList).toContain('MuiCheckbox-indeterminate');

    await userEvent.click(box());
    expect(onChange.mock.calls).toEqual([[true]]);
    // A press clears the native state; the box keeps the one it is given.
    expect(box().indeterminate).toBe(true);

    redraw({ indeterminate: false });
    expect(box().hasAttribute('aria-checked')).toBe(false);
    expect(box().indeterminate).toBe(false);
  });

  it('calls nothing while read-only and keeps its state', async () => {
    const onChange = vi.fn();
    draw({ checked: true, readOnly: true, onChange });

    expect(box().getAttribute('aria-readonly')).toBe('true');
    await userEvent.click(box());

    expect(box().checked).toBe(true);
    expect(onChange).not.toHaveBeenCalled();
  });

  it('calls nothing while disabled', async () => {
    const onChange = vi.fn();
    draw({ disabled: true, onChange });

    expect(box().disabled).toBe(true);
    expect(box().hasAttribute('aria-readonly')).toBe(false);
    await userEvent.setup({ pointerEventsCheck: 0 }).click(box());

    expect(onChange).not.toHaveBeenCalled();
  });

  it('colors an invalid required box, and a questionable one', () => {
    const { redraw } = draw({ validation: 'error', required: true });

    expect(box().getAttribute('aria-invalid')).toBe('true');
    expect(box().getAttribute('aria-required')).toBe('true');
    expect(box().closest('.MuiCheckbox-root')!.classList).toContain('MuiCheckbox-colorError');

    redraw({ validation: 'warning', required: false });
    expect(box().hasAttribute('aria-invalid')).toBe(false);
    expect(box().hasAttribute('aria-required')).toBe(false);
    expect(box().closest('.MuiCheckbox-root')!.classList).toContain('MuiCheckbox-colorWarning');
  });

  it('draws a switch, which is never indeterminate', async () => {
    const onChange = vi.fn();
    draw({ display: 'switch', checked: true, indeterminate: true, id: 's1', onChange });

    const toggle = screen.getByRole('switch');
    expect(toggle.id).toBe('s1');
    expect(toggle.closest('.MuiSwitch-root')).not.toBeNull();
    expect(toggle.hasAttribute('aria-checked')).toBe(false);
    expect(screen.queryByRole('checkbox')).toBeNull();

    await userEvent.click(toggle);
    expect(onChange.mock.calls).toEqual([[false]]);
  });

  it('draws a box with a label in a form control label, which presses the box', async () => {
    const onChange = vi.fn();
    draw({ label: 'Name', className: 'x', 'aria-label': undefined, onChange });

    const input = screen.getByRole('checkbox', { name: 'Name' });
    const label = input.closest('label')!;
    expect(label.classList).toContain('MuiFormControlLabel-root');
    expect(label.classList).toContain('x');
    expect(input.closest('.MuiCheckbox-root')!.classList).not.toContain('x');

    await userEvent.click(screen.getByText('Name'));
    expect(onChange.mock.calls).toEqual([[true]]);
  });

  it('draws a small box that fits the selection column of a table', () => {
    const { redraw } = draw({ small: true });

    const root = box().closest('.MuiCheckbox-root')!;
    expect(root.classList).toContain('MuiCheckbox-sizeSmall');
    expect(getComputedStyle(root).padding).toBe('9px');
    expect(root.querySelector('svg')!.classList).toContain('MuiSvgIcon-fontSizeSmall');

    redraw({ small: false });
    expect(box().closest('.MuiCheckbox-root')!.classList).not.toContain('MuiCheckbox-sizeSmall');
  });
  it('lets the touch area of a box aligned at the start hang over the start edge', () => {
    const { redraw } = draw({ alignStart: true });
    expect(getComputedStyle(box().closest('.MuiCheckbox-root')!).marginLeft).toBe('-12px');

    redraw({ alignStart: false });
    expect(getComputedStyle(box().closest('.MuiCheckbox-root')!).marginLeft).not.toBe('-12px');
  });

  it('leaves the alignment of a box with a label to its form control label', () => {
    draw({ alignStart: true, label: 'Name' });

    expect(getComputedStyle(box().closest('.MuiCheckbox-root')!).marginLeft).not.toBe('-12px');
  });

  it('aligns a switch at the start as well', () => {
    draw({ alignStart: true, display: 'switch' });

    expect(getComputedStyle(screen.getByRole('switch').closest('.MuiSwitch-root')!).marginLeft).toMatch(/^-/);
  });
});
