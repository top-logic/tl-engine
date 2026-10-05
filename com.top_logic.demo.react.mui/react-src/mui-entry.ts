// Renders the TopLogic React UI with Material UI.
//
// Both registrations run when this bundle loads, before the controls of the page are mounted:
//
// - The root wrapper (MuiRoot) puts the providers Material UI needs above every React root of the
//   bridge.
// - The replacements render the state of TopLogic controls with adapters around MUI components.
//   All other controls keep their TopLogic components.

import { registerRootWrapper, replace } from 'tl-react-bridge';
import MuiRoot from './MuiRoot';
import MuiButtonAdapter from './adapters/MuiButtonAdapter';
import MuiToggleButtonAdapter from './adapters/MuiToggleButtonAdapter';
import MuiCheckboxAdapter from './adapters/MuiCheckboxAdapter';
import MuiTextInputAdapter from './adapters/MuiTextInputAdapter';
import MuiPasswordInputAdapter from './adapters/MuiPasswordInputAdapter';
import MuiNumberInputAdapter from './adapters/MuiNumberInputAdapter';
import MuiDatePickerAdapter from './adapters/MuiDatePickerAdapter';
import MuiSelectAdapter from './adapters/MuiSelectAdapter';

registerRootWrapper(MuiRoot);
replace('TLButton', MuiButtonAdapter);
replace('TLToggleButton', MuiToggleButtonAdapter);
replace('TLCheckbox', MuiCheckboxAdapter);
replace('TLTextInput', MuiTextInputAdapter);
replace('TLPasswordInput', MuiPasswordInputAdapter);
replace('TLNumberInput', MuiNumberInputAdapter);
replace('TLDatePicker', MuiDatePickerAdapter);
replace('TLSelect', MuiSelectAdapter);
