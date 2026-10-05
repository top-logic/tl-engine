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

registerRootWrapper(MuiRoot);
replace('TLButton', MuiButtonAdapter);
replace('TLToggleButton', MuiToggleButtonAdapter);
replace('TLCheckbox', MuiCheckboxAdapter);
