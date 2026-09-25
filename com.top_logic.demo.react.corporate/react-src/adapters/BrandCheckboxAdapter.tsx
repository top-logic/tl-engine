import { React, useTLState, useTLFieldValue, rootClassName } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { BrandCheckbox } from '../example-lib';
import { STATE_DISABLED, STATE_EDITABLE, STATE_HAS_ERROR, STATE_HIDDEN } from './state-keys';

/**
 * Renders the state of a TopLogic boolean field (module name `TLCheckbox`) with the library's
 * {@link BrandCheckbox}.
 *
 * <p>The field value is read and written through {@link useTLFieldValue}: the library's change
 * callback sends the new value to the server, which is what makes it part of the form's edit and
 * save cycle. A field that is not editable (a form in view mode) renders read-only, a disabled one
 * disabled, and a field with an error is marked invalid. The field's label is rendered by the form
 * field around it, so the library's own label stays unused.</p>
 *
 * <p>Deliberately not reproduced: the third "no value" state of a tri-state field (it shows as
 * unticked, and a click sets a value) and the switch presentation.</p>
 */
const BrandCheckboxAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const [value, setValue] = useTLFieldValue();

  if (state[STATE_HIDDEN] === true) {
    return null;
  }

  return (
    <BrandCheckbox
      id={controlId}
      checked={value === true}
      onChange={setValue}
      readOnly={state[STATE_EDITABLE] === false}
      disabled={state[STATE_DISABLED] === true}
      invalid={state[STATE_HAS_ERROR] === true}
      className={rootClassName(state)}
    />
  );
};

export default BrandCheckboxAdapter;
