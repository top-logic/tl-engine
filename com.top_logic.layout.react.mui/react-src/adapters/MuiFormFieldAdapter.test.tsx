// The wire contract of TLFormField, checked against the MUI adapter.

import React from 'react';
import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { register, useFieldLabelProps, fieldInputId, TOOLTIP_ATTR } from 'tl-react-bridge';
import type { TLCellProps, FormFieldStateJson, ChildControlJson, FormLayout } from 'tl-react-bridge';
import MuiFormFieldAdapter from './MuiFormFieldAdapter';
import { CONTROL_ID, mountAdapter, patchState } from './wire-test-support';

/** The component name of the stand-in for the input control of the field. */
const TEST_INPUT = 'MuiFormFieldTestInput';

/** The control ID of the input control. */
const INPUT_CONTROL_ID = 'i1';

/** The ID of the focusable element of the input control. */
const INPUT_ID = INPUT_CONTROL_ID + '-input';

/** The ID of the label text of the field. */
const LABEL_ID = CONTROL_ID + '-label';

/** The label of the help button: the key, as the test stub translates each key to itself. */
const LABEL_HELP = 'js.formField.help';

/** An icon font image in its encoded form. */
const IMAGE_ERROR = 'css:fas fa-circle-exclamation';

/** A form being edited, with the labels above the inputs. */
const EDIT_FORM_TOP: FormLayout = { readOnly: false, resolvedLabelPosition: 'top' };

/** A form showing values only. */
const READ_ONLY_FORM: FormLayout = { readOnly: true, resolvedLabelPosition: 'side' };

/** The stand-in for the input control: a text input associated with the label of the field. */
function TestInput({ controlId }: TLCellProps) {
  const inputId = fieldInputId(controlId);
  const labelProps = useFieldLabelProps(controlId, inputId);
  return (
    <span id={controlId}>
      <input id={inputId} {...labelProps} />
    </span>
  );
}

register(TEST_INPUT, TestInput);

function input(): ChildControlJson {
  return { controlId: INPUT_CONTROL_ID, module: TEST_INPUT, state: {} } as ChildControlJson;
}

function mountField(state: Partial<FormFieldStateJson>, formLayout?: FormLayout) {
  return mountAdapter(MuiFormFieldAdapter, { label: 'Name', field: input(), ...state }, { formLayout });
}

function root(): HTMLElement {
  return document.getElementById(CONTROL_ID)!;
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe('TLFormField as MUI FormControl', () => {
  it('keeps the structure of the form grid around an MUI label and the input', () => {
    mountField({ labelPosition: 'top', fullLine: true, cssClass: 'my-field' });

    const field = root();
    expect(field.classList).toContain('MuiFormControl-root');
    expect(field.classList).toContain('tl-form-field');
    expect(field.classList).toContain('tl-form-field--top');
    expect(field.classList).toContain('tl-form-field--full');
    expect(field.classList).toContain('my-field');
    expect(getComputedStyle(field).display).toBe('grid');
    const labelPart = field.querySelector(':scope > .tl-form-field__label')!;
    expect(labelPart.querySelector('label.MuiFormLabel-root')!.textContent).toBe('Name');
    expect(field.querySelector(':scope > .tl-form-field__input > #' + INPUT_CONTROL_ID)).not.toBeNull();
  });

  it('associates the label with the focusable element of the input', () => {
    mountField({});

    const label = document.getElementById(LABEL_ID)!;
    expect(label.getAttribute('for')).toBe(INPUT_ID);
    const box = screen.getByRole('textbox');
    expect(box.getAttribute('aria-labelledby')).toBe(LABEL_ID);
    expect(screen.getByLabelText('Name')).toBe(box);
  });

  it('marks a required field with the MUI asterisk', () => {
    mountField({ required: true });

    expect(root().querySelector('.MuiFormLabel-asterisk')).not.toBeNull();
  });

  it('shows the error with its icon as an MUI helper text describing the input', () => {
    mountField({ error: 'Pflichtfeld', errorIcon: IMAGE_ERROR, warnings: ['Kurz'] });

    const message = screen.getByRole('alert');
    expect(message.classList).toContain('MuiFormHelperText-root');
    expect(message.classList).toContain('Mui-error');
    expect(message.textContent).toBe('Pflichtfeld');
    expect(message.querySelector('i.fa-circle-exclamation')).not.toBeNull();
    expect(message.parentElement!.classList).toContain('tl-form-field__message');
    expect(screen.getByRole('textbox').getAttribute('aria-describedby')).toBe(message.id);
    // An error displaces the warnings.
    expect(screen.queryByText('Kurz')).toBeNull();
  });

  it('shows each warning while there is no error', () => {
    mountField({ warnings: ['Kurz', 'Alt'] });

    const describedBy = screen.getByRole('textbox').getAttribute('aria-describedby')!.split(' ');
    expect(describedBy.map(id => document.getElementById(id)!.textContent)).toEqual(['Kurz', 'Alt']);
  });

  it('toggles the help text with its button', async () => {
    mountField({ helpText: 'Der volle Name.' });

    const help = document.getElementById(CONTROL_ID + '-help')!;
    expect(help.parentElement!.hidden).toBe(true);
    const button = screen.getByRole('button', { name: LABEL_HELP });
    expect(button.getAttribute('aria-expanded')).toBe('false');

    await userEvent.click(button);

    expect(help.parentElement!.hidden).toBe(false);
    expect(button.getAttribute('aria-expanded')).toBe('true');
    expect(screen.getByRole('textbox').getAttribute('aria-describedby')).toBe(help.id);
  });

  it('offers the description as tooltip of the label, a rich tooltip first', () => {
    mountField({ tooltipText: 'Wie man heißt.' });
    expect(document.getElementById(LABEL_ID)!.getAttribute(TOOLTIP_ATTR)).toBe('text:Wie man heißt.');

    patchState({ hasTooltip: true });
    expect(document.getElementById(LABEL_ID)!.getAttribute(TOOLTIP_ATTR)).toBe('key:tooltip');
  });

  it('marks a changed value', () => {
    mountField({ dirty: true });

    expect(root().getAttribute('data-tl-state')).toBe('dirty');
    expect(root().querySelector('.tl-form-field__dirty')).not.toBeNull();
  });

  it('keeps a hidden label off the screen, still naming the input', () => {
    mountField({ labelPosition: 'hidden' });

    expect(root().classList).toContain('tl-form-field--hidden');
    expect(root().querySelector('.tl-form-field__label')).toBeNull();
    expect(document.getElementById(LABEL_ID)!.classList).toContain('tl-visually-hidden');
    expect(screen.getByLabelText('Name')).toBe(screen.getByRole('textbox'));
  });

  it('keeps the input mounted while the field is not visible', () => {
    mountField({ visible: false });

    expect(root().hidden).toBe(true);
    expect(document.getElementById(INPUT_ID)).not.toBeNull();
  });

  it('shows no required mark, messages or help in a read-only form', () => {
    mountField({ required: true, error: 'Pflichtfeld', warnings: ['Kurz'], helpText: 'Hilfe' }, READ_ONLY_FORM);

    expect(root().querySelector('.MuiFormLabel-asterisk')).toBeNull();
    expect(screen.queryByRole('alert')).toBeNull();
    expect(screen.queryByText('Kurz')).toBeNull();
    expect(screen.queryByRole('button')).toBeNull();
    expect(screen.getByRole('textbox').hasAttribute('aria-describedby')).toBe(false);
  });

  it('shows required mark, messages and help in a form being edited', () => {
    mountField({ required: true, error: 'Pflichtfeld', helpText: 'Hilfe' }, EDIT_FORM_TOP);

    expect(root().querySelector('.MuiFormLabel-asterisk')).not.toBeNull();
    expect(screen.getByRole('alert').textContent).toBe('Pflichtfeld');
    expect(screen.getByRole('button', { name: LABEL_HELP })).not.toBeNull();
  });

  it('puts its label where the form layout puts the labels of its fields', () => {
    mountField({}, EDIT_FORM_TOP);
    expect(root().classList).toContain('tl-form-field--top');
    cleanup();

    mountField({});
    expect(root().classList).toContain('tl-form-field--side');
  });

  it('puts its label where its own label position says, whatever the form layout says', () => {
    mountField({ labelPosition: 'after' }, EDIT_FORM_TOP);

    expect(root().classList).toContain('tl-form-field--after');
    expect(root().classList).not.toContain('tl-form-field--top');
  });

  it('names no input without a label', () => {
    mountField({ label: '' });

    expect(screen.getByRole('textbox').hasAttribute('aria-labelledby')).toBe(false);
  });
});
