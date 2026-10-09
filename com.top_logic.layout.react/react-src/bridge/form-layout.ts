import { createContext, useContext } from 'react';

/**
 * What a form layout (`TLFormLayout`) tells the form fields inside it.
 *
 * <p>Provided through {@link FormLayoutContext} by the form layout, read by the frame of each
 * field (`TLFormField`, or a replacement of it) with {@link useFormLayout}.</p>
 */
export interface FormLayout {
  /**
   * Whether the form shows values only. The frame of a field then omits what belongs to editing:
   * the mark for a required field, the error and warning messages and the help.
   */
  readonly readOnly: boolean;

  /**
   * Where a field that states no label position of its own puts its label: beside the input or
   * above it, as the form layout resolved its own position (`auto` by the width of its columns).
   */
  readonly resolvedLabelPosition: 'side' | 'top';

  /** Whether the content is rendered inside a form layout. */
  readonly insideForm: boolean;
}

/** Outside a form layout: an editable form with the labels beside the inputs. */
const NO_FORM_LAYOUT: FormLayout = Object.freeze({
  readOnly: false,
  resolvedLabelPosition: 'side',
  insideForm: false,
});

/** The form layout around the rendered control. */
export const FormLayoutContext = createContext<FormLayout>(NO_FORM_LAYOUT);

/** The form layout around the rendered control, see {@link FormLayout}. */
export function useFormLayout(): FormLayout {
  return useContext(FormLayoutContext);
}
