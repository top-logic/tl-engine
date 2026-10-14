// The values of fields that are not editable, drawn by the MUI adapter of the part FieldValue: the
// TopLogic text, password, number, select, date and slider fields show their value through it, so
// that one replacement of the part reaches all of them.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup } from '@testing-library/react';
import { replacePart } from 'tl-react-bridge';
import TLTextInput from '../../../com.top_logic.layout.react/react-src/controls/TLTextInput';
import TLPasswordInput from '../../../com.top_logic.layout.react/react-src/controls/TLPasswordInput';
import TLNumberInput from '../../../com.top_logic.layout.react/react-src/controls/TLNumberInput';
import TLSelect from '../../../com.top_logic.layout.react/react-src/controls/TLSelect';
import TLDatePicker from '../../../com.top_logic.layout.react/react-src/controls/TLDatePicker';
import TLSlider from '../../../com.top_logic.layout.react/react-src/controls/TLSlider';
import MuiFieldValueAdapter from './MuiFieldValueAdapter';
import MuiTextInputAdapter from './MuiTextInputAdapter';
import MuiButtonAdapter from './MuiButtonAdapter';
import { CONTROL_ID, mountAdapter, settle } from './wire-test-support';

replacePart('FieldValue', MuiFieldValueAdapter);
// The text fields look up their box and its buttons as well.
replacePart('TextInput', MuiTextInputAdapter);
replacePart('Button', MuiButtonAdapter);

/** The class of the MUI typography of a value. */
const MUI_BODY = 'MuiTypography-body2';

/** The element of the control: the drawn value of a field that is not editable. */
function shown(): HTMLElement {
  return document.getElementById(CONTROL_ID)!;
}

/** Asserts that the value is drawn by MUI and not by the design system of TopLogic. */
function expectMuiValue(element: HTMLElement) {
  expect(element.classList).toContain(MUI_BODY);
  expect(element.classList).not.toContain('tl-field-value');
  expect(element.querySelector('.tl-field-value__text')).toBeNull();
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe('the value of a read-only text field as MUI Typography', () => {
  it('shows the text on one line, with the configured class', () => {
    mountAdapter(TLTextInput, { value: 'Hallo Welt', editable: false, cssClass: 'my-class' });

    expectMuiValue(shown());
    expect(shown().tagName).toBe('SPAN');
    expect(shown().classList).toContain('MuiTypography-noWrap');
    expect(shown().classList).toContain('my-class');
    expect(shown().textContent).toBe('Hallo Welt');
    expect(screen.queryByRole('textbox')).toBeNull();
  });

  it('shows a web address as MUI link opening it in a window of its own', () => {
    mountAdapter(TLTextInput, { value: 'https://example.org', inputType: 'url', editable: false });

    const link = screen.getByRole('link', { name: 'https://example.org' });
    expect(link).toBe(shown());
    expect(link.classList).toContain('MuiLink-root');
    expect(link.getAttribute('href')).toBe('https://example.org');
    expect(link.getAttribute('target')).toBe('_blank');
    expect(link.getAttribute('rel')).toBe('noopener noreferrer');
  });

  it('shows no link for a value the server rejected, nor for plain text', () => {
    mountAdapter(TLTextInput, { value: 'kein link', inputType: 'url', hasError: true, editable: false });
    expect(screen.queryByRole('link')).toBeNull();
    expectMuiValue(shown());
    cleanup();

    mountAdapter(TLTextInput, { value: 'a@b.de', editable: false });
    expect(screen.queryByRole('link')).toBeNull();
  });

  it('keeps the line breaks of a multi-line text, an empty line keeping its height', () => {
    mountAdapter(TLTextInput, { value: 'eins\n\ndrei', multiline: true, editable: false });

    expectMuiValue(shown());
    expect(shown().tagName).toBe('DIV');
    expect(Array.from(shown().children).map(line => line.textContent)).toEqual(['eins', ' ', 'drei']);
  });

  it('shows an input, not the value, while only disabled', async () => {
    mountAdapter(TLTextInput, { value: 'abc', editable: false, disabled: true });
    await settle();

    expect((screen.getByRole('textbox') as HTMLInputElement).disabled).toBe(true);
    expect(document.querySelector('.' + MUI_BODY)).toBeNull();
  });
});

describe('the values of other read-only fields as MUI Typography', () => {
  it('shows a mask in place of a password', () => {
    mountAdapter(TLPasswordInput, { value: 'geheim', editable: false });

    expectMuiValue(shown());
    expect(shown().textContent).toBe('••••••••');
  });

  it('shows the text of a number', () => {
    mountAdapter(TLNumberInput, { value: '1.234,5', editable: false });

    expectMuiValue(shown());
    expect(shown().textContent).toBe('1.234,5');
  });

  it('shows the label of the chosen option of a select, and nothing without one', () => {
    const options = [{ value: 'a', label: 'Alpha' }, { value: 'b', label: 'Beta' }];
    mountAdapter(TLSelect, { value: 'b', options, editable: false });

    expectMuiValue(shown());
    expect(shown().textContent).toBe('Beta');
    expect(screen.queryByRole('combobox')).toBeNull();
    cleanup();

    mountAdapter(TLSelect, { value: null, options, editable: false });
    expectMuiValue(shown());
    expect(shown().textContent).toBe('');
  });

  it('shows the written date', () => {
    mountAdapter(TLDatePicker, { value: '2026-06-01', displayValue: '01.06.2026', editable: false });

    expectMuiValue(shown());
    expect(shown().textContent).toBe('01.06.2026');
  });

  it('shows the label of the value of a slider', () => {
    mountAdapter(TLSlider, { value: 20, valueLabel: '20 %', editable: false });

    expectMuiValue(shown());
    expect(shown().textContent).toBe('20 %');
    expect(screen.queryByRole('slider')).toBeNull();
  });
});
