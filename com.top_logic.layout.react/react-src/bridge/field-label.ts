import { createContext, useContext, useLayoutEffect } from 'react';

/**
 * Association between a form field's label and the control that is the field's input.
 *
 * <p>A form field (`TLFormField`) renders its label text and its input control side by side, not
 * one inside the other: a `label` wrapping the input would forward a click on any non-interactive
 * part of a composite input (a rich-text editor, say) to the input's first labelable descendant,
 * e.g. a toolbar button. The label and the input refer to each other by id instead:</p>
 *
 * <ul>
 * <li>The field provides a {@link FieldLabel} through {@link FieldLabelContext} for the control in
 * its input slot, keyed by that control's `controlId`.</li>
 * <li>The input control calls {@link useFieldLabelProps} with its own `controlId` and the DOM id of
 * its focusable element - the `input`, `select`, `textarea`, the button opening a picker, the
 * `contenteditable` element, or the element with role `combobox`, `radiogroup` or `group` holding
 * the options. A control whose root element is the focusable one passes its `controlId`; a control
 * with a focusable element inside its root gives that element the id {@link fieldInputId} builds.
 * The control keeps `id={controlId}` on its root element in any case.</li>
 * <li>The hook reports that id to the field, whose `label` refers to it (`htmlFor`), and returns
 * the `aria-labelledby` naming the element by the label text - and the `aria-describedby` naming
 * the field's error message and shown help text, if any -, to be spread onto the focusable
 * element.</li>
 * <li>Outside a field, and for every control nested deeper inside the input (e.g. the inputs of a
 * table row inside a field), the hook reports nothing and returns no props, so no control takes a
 * label that is not its own.</li>
 * </ul>
 *
 * <p>The returned props are plain strings, so that a control can hand them to a DOM element it does
 * not render itself (e.g. through an editor library's attribute option).</p>
 */
export interface FieldLabel {
  /** The `controlId` of the control that is the field's input. */
  readonly controlId: string;

  /** The id of the element holding the field's label text. */
  readonly labelId: string;

  /**
   * The ids of the elements describing the input - the error message, the help text while it is
   * shown -, separated by spaces, or `undefined` when nothing describes it.
   */
  readonly describedBy?: string;

  /**
   * Receives the DOM id of the input control's focusable element, or `null` when the control no
   * longer states one.
   */
  readonly setInputId: (inputId: string | null) => void;
}

/** The props an input control puts on its focusable element, see {@link useFieldLabelProps}. */
export interface FieldLabelProps {
  'aria-labelledby'?: string;
  'aria-describedby'?: string;
}

/** Appended to a control's `controlId` to form the id of the focusable element inside it. */
const INPUT_ID_SUFFIX = '-input';

/** Appended to a form field's `controlId` to form the id of its label text. */
const LABEL_ID_SUFFIX = '-label';

const NO_PROPS: FieldLabelProps = Object.freeze({});

/** The label association of the form field around the rendered control, if any. */
export const FieldLabelContext = createContext<FieldLabel | null>(null);

/**
 * The id of the focusable element inside the control with the given `controlId`, for a control
 * whose root element is not the focusable one.
 */
export function fieldInputId(controlId: string): string {
  return controlId + INPUT_ID_SUFFIX;
}

/**
 * Creates the label association of the form field with the given `controlId` for the input
 * control with the given `controlId`, reporting the id of the input's focusable element to
 * `setInputId`. `describedBy` names the elements describing the input, see
 * {@link FieldLabel.describedBy}.
 */
export function fieldLabel(
  fieldControlId: string,
  inputControlId: string,
  setInputId: (inputId: string | null) => void,
  describedBy?: string
): FieldLabel {
  return {
    controlId: inputControlId,
    labelId: fieldControlId + LABEL_ID_SUFFIX,
    describedBy,
    setInputId,
  };
}

/**
 * Associates the focusable element with the DOM id `inputId` of the control with the given
 * `controlId` with the label of the form field around it, if the control is that field's input.
 *
 * @returns The props to spread onto the focusable element: `aria-labelledby` naming the label
 *          text and, while something describes the input, `aria-describedby`, if the control is
 *          the input of a form field; no props otherwise.
 */
export function useFieldLabelProps(controlId: string, inputId: string): FieldLabelProps {
  const label = useContext(FieldLabelContext);
  const own = label !== null && label.controlId === controlId ? label : null;

  useLayoutEffect(() => {
    if (own === null) {
      return undefined;
    }
    own.setInputId(inputId);
    return () => own.setInputId(null);
  }, [own, inputId]);

  if (own === null) {
    return NO_PROPS;
  }
  return own.describedBy === undefined
    ? { 'aria-labelledby': own.labelId }
    : { 'aria-labelledby': own.labelId, 'aria-describedby': own.describedBy };
}

/**
 * Whether a click on a `label` referring to the given element activates it natively: HTML
 * forwards the click to a labelable element and focuses (or toggles) it. A button is left out: a
 * label click would press it, which for the button opening a picker is not what a click on the
 * field's name means.
 */
function activatedByLabel(element: HTMLElement): boolean {
  if (element instanceof HTMLInputElement) {
    return element.type !== 'hidden';
  }
  return element instanceof HTMLSelectElement
    || element instanceof HTMLTextAreaElement
    || element instanceof HTMLMeterElement
    || element instanceof HTMLOutputElement
    || element instanceof HTMLProgressElement;
}

function focusable(element: HTMLElement): boolean {
  if ((element as HTMLElement & { disabled?: boolean }).disabled === true) {
    return false;
  }
  if (element.tabIndex < 0 && !element.isContentEditable) {
    return false;
  }
  return element.getClientRects().length > 0;
}

/** Types of an `input` element that are buttons rather than a value being entered. */
const BUTTON_INPUT_TYPES = new Set(['button', 'submit', 'reset', 'image', 'file']);

/** Roles of an element taking a value, as opposed to one triggering an action. */
const VALUE_ROLES = new Set([
  'textbox', 'searchbox', 'combobox', 'listbox', 'radio', 'checkbox', 'switch', 'spinbutton', 'slider',
]);

/** Whether the given element takes a value: a text, a choice, a number, not an action button. */
function takesValue(element: HTMLElement): boolean {
  if (element instanceof HTMLInputElement) {
    return !BUTTON_INPUT_TYPES.has(element.type);
  }
  if (element instanceof HTMLSelectElement || element instanceof HTMLTextAreaElement
      || element.isContentEditable) {
    return true;
  }
  const role = element.getAttribute('role');
  return role !== null && VALUE_ROLES.has(role);
}

/** Whether the given element is the chosen option of a group of options. */
function checked(element: HTMLElement): boolean {
  return (element instanceof HTMLInputElement && element.checked)
    || element.getAttribute('aria-checked') === 'true';
}

/**
 * The element a click on a field's label focuses: the input's focusable element itself, if
 * focusable. Otherwise a focusable descendant of it, in this order of preference: the checked
 * option (the one a radio group or segmented choice sends Tab to), the first element taking a value
 * (see {@link takesValue}) - so that e.g. a list of values focuses its first value rather than the
 * button moving it -, or else the first focusable element at all.
 */
function focusTarget(input: HTMLElement): HTMLElement | null {
  if (focusable(input)) {
    return input;
  }
  let firstValue: HTMLElement | null = null;
  let first: HTMLElement | null = null;
  for (const candidate of input.querySelectorAll<HTMLElement>('*')) {
    if (!focusable(candidate)) {
      continue;
    }
    if (checked(candidate)) {
      return candidate;
    }
    if (firstValue === null && takesValue(candidate)) {
      firstValue = candidate;
    }
    if (first === null) {
      first = candidate;
    }
  }
  return firstValue ?? first;
}

/**
 * Click handler of a `label` referring to the focusable element of a field input by its id, as
 * reported through {@link FieldLabel.setInputId}.
 *
 * <p>Complements HTML's label activation for an input HTML does not activate from a label (see
 * {@link activatedByLabel}): a `contenteditable` element, a group of option buttons, an element
 * with role `combobox`, a button. Such an input is focused as described at {@link focusTarget},
 * without pressing a button or choosing an option.</p>
 */
export function focusFieldInput(event: { preventDefault(): void }, inputId: string): void {
  const input = document.getElementById(inputId);
  if (input === null || activatedByLabel(input)) {
    return;
  }
  event.preventDefault();
  focusTarget(input)?.focus();
}
