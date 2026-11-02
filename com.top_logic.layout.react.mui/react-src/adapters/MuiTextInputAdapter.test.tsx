// The part TextInput drawn with Material UI: a small MUI TextField without a label of its own.

import React from 'react';
import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup, render, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { TOOLTIP_ATTR } from 'tl-react-bridge';
import type { TextInputPartProps } from 'tl-react-bridge';
import { createMuiRoot, createPageThemes } from '../MuiRoot';
import MuiTextInputAdapter from './MuiTextInputAdapter';

/** The root wrapper the adapter is drawn below: the one of the MUI default theme. */
const MuiRoot = createMuiRoot(createPageThemes({}));

/** The accessible name of the box. */
const NAME = 'Name';

function draw(props: Partial<TextInputPartProps>) {
  const element = (next: Partial<TextInputPartProps>) => (
    <MuiRoot><MuiTextInputAdapter value="" aria-label={NAME} {...props} {...next} /></MuiRoot>
  );
  const result = render(element({}));
  return {
    redraw: (next: Partial<TextInputPartProps>) => result.rerender(element(next)),
  };
}

/** The input of the box. */
function input(): HTMLInputElement {
  return screen.getByLabelText(NAME) as HTMLInputElement;
}

afterEach(() => {
  cleanup();
});

describe('the part TextInput as MUI TextField', () => {
  it('puts ID, name, ref and passed-through attributes on the input, the class on the root', () => {
    const ref = React.createRef<HTMLInputElement | HTMLTextAreaElement>();
    draw({ id: 't1', ref, name: 'n', value: 'Hallo', placeholder: 'Name eingeben', className: 'x',
      'aria-describedby': 'hint' });

    const box = input();
    expect(box.id).toBe('t1');
    expect(ref.current).toBe(box);
    expect(box.name).toBe('n');
    expect(box.value).toBe('Hallo');
    expect(box.placeholder).toBe('Name eingeben');
    expect(box.type).toBe('text');
    expect(box.getAttribute('aria-describedby')).toBe('hint');
    expect(box.closest('.MuiTextField-root')!.classList).toContain('x');
    expect(box.closest('.MuiInputBase-sizeSmall')).not.toBeNull();
    expect(box.classList).not.toContain('tl-field');
    expect(document.querySelector('label')).toBeNull();
  });

  it('reports each change with the text, and shows the text it is given', () => {
    const onChange = vi.fn();
    const { redraw } = draw({ value: 'a', onChange });

    fireEvent.change(input(), { target: { value: 'ab' } });

    expect(onChange.mock.calls).toEqual([['ab']]);
    // Controlled: the box shows the text it is given.
    expect(input().value).toBe('a');
    redraw({ value: 'ab' });
    expect(input().value).toBe('ab');
  });

  it('passes key, focus and blur to the handlers', () => {
    const onKeyDown = vi.fn();
    const onFocus = vi.fn();
    const onBlur = vi.fn();
    draw({ onKeyDown, onFocus, onBlur });

    fireEvent.focus(input());
    fireEvent.keyDown(input(), { key: 'Enter' });
    fireEvent.blur(input());

    expect(onFocus).toHaveBeenCalledTimes(1);
    expect(onKeyDown.mock.calls.map(([event]) => event.key)).toEqual(['Enter']);
    expect(onBlur).toHaveBeenCalledTimes(1);
  });

  it('draws the icon ahead of the text and the action at the end, inside the frame', () => {
    draw({ icon: 'css:my-icon', action: <button type="button">Leeren</button> });

    const box = input();
    const frame = box.closest('.MuiInputBase-root')!;
    const icon = frame.querySelector('.MuiInputAdornment-positionStart i.my-icon')!;
    expect(icon).not.toBeNull();
    expect(icon.getAttribute('aria-hidden')).toBe('true');
    const action = screen.getByRole('button', { name: 'Leeren' });
    expect(action.closest('.MuiInputAdornment-positionEnd')).not.toBeNull();
    expect(frame.contains(action)).toBe(true);
  });

  it('draws a text area of the given rows, without icon and action, passing its keys', () => {
    const onKeyDown = vi.fn();
    draw({ multiline: true, rows: 5, value: 'a\nb', icon: 'css:my-icon', action: <button type="button">X</button>,
      onKeyDown });

    const area = input();
    expect(area.tagName).toBe('TEXTAREA');
    expect(area.getAttribute('rows')).toBe('5');
    expect(document.querySelector('.my-icon')).toBeNull();
    expect(screen.queryByRole('button')).toBeNull();
    fireEvent.keyDown(area, { key: 'Enter' });
    expect(onKeyDown.mock.calls.map(([event]) => event.key)).toEqual(['Enter']);
  });

  it('puts the combobox role and attributes of a box offering suggestions on the input', () => {
    draw({ role: 'combobox', 'aria-autocomplete': 'list', 'aria-expanded': true, 'aria-controls': 'l1',
      'aria-activedescendant': 'l1-0' });

    const box = screen.getByRole('combobox');
    expect(box).toBe(input());
    expect(box.tagName).toBe('INPUT');
    expect(box.getAttribute('aria-autocomplete')).toBe('list');
    expect(box.getAttribute('aria-expanded')).toBe('true');
    expect(box.getAttribute('aria-controls')).toBe('l1');
    expect(box.getAttribute('aria-activedescendant')).toBe('l1-0');
  });

  it('puts the combobox role on a text area offering suggestions', () => {
    draw({ multiline: true, role: 'combobox', 'aria-expanded': false });

    const area = screen.getByRole('combobox');
    expect(area.tagName).toBe('TEXTAREA');
    expect(area.getAttribute('aria-expanded')).toBe('false');
  });

  it('draws a preformatted text area without wrapping its lines', () => {
    draw({ multiline: true, preformatted: true, value: 'a\nb' });

    expect(input().getAttribute('wrap')).toBe('off');
  });

  it('wraps the lines of a text area that is not preformatted', () => {
    draw({ multiline: true, value: 'a\nb' });

    expect(input().hasAttribute('wrap')).toBe(false);
  });

  it('draws a multi-line box of three rows where it names none', () => {
    draw({ multiline: true });

    expect(input().getAttribute('rows')).toBe('3');
  });

  it('marks an invalid required box, without the MUI required mark', () => {
    draw({ validation: 'error', required: true });

    const box = input();
    expect(box.getAttribute('aria-invalid')).toBe('true');
    expect(box.getAttribute('aria-required')).toBe('true');
    expect(box.required).toBe(false);
    expect(box.closest('.Mui-error')).not.toBeNull();
  });

  it('draws a questionable value in the warning color, not as error', () => {
    draw({ validation: 'warning' });

    const box = input();
    expect(box.hasAttribute('aria-invalid')).toBe(false);
    expect(box.closest('.Mui-error')).toBeNull();
    expect(box.closest('.MuiInputBase-colorWarning')).not.toBeNull();
  });

  it('marks nothing on a valid optional box', () => {
    draw({});

    const box = input();
    expect(box.hasAttribute('aria-invalid')).toBe(false);
    expect(box.hasAttribute('aria-required')).toBe(false);
    expect(box.hasAttribute(TOOLTIP_ATTR)).toBe(false);
  });

  it('puts the tooltip on the input', () => {
    draw({ tooltip: 'Falsch' });

    expect(input().getAttribute(TOOLTIP_ATTR)).toBe('text:Falsch');
  });

  it('accepts no input and reports nothing while disabled', async () => {
    const onChange = vi.fn();
    draw({ value: 'abc', disabled: true, onChange });

    expect(input().disabled).toBe(true);
    await userEvent.setup({ pointerEventsCheck: 0 }).type(input(), 'x');

    expect(onChange).not.toHaveBeenCalled();
  });

  it('accepts no input and reports nothing while read-only', async () => {
    const onChange = vi.fn();
    draw({ value: 'abc', readOnly: true, onChange });

    expect(input().readOnly).toBe(true);
    await userEvent.type(input(), 'x');

    expect(onChange).not.toHaveBeenCalled();
    expect(input().value).toBe('abc');
  });

  it('draws a password box', () => {
    draw({ type: 'password', value: 'geheim' });

    expect(input().type).toBe('password');
    expect(input().value).toBe('geheim');
  });

  it('draws a number box with its bounds and the keyboard asked for', () => {
    draw({ type: 'number', min: 0, max: 255, inputMode: 'numeric', maxLength: 3, autoFocus: true });

    const box = input();
    expect(box.type).toBe('number');
    expect(box.min).toBe('0');
    expect(box.max).toBe('255');
    expect(box.getAttribute('inputmode')).toBe('numeric');
    expect(box.maxLength).toBe(3);
    expect(document.activeElement).toBe(box);
  });
});
