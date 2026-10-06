// Renders the TopLogic React UI with Material UI.
//
// All of it runs when this bundle loads, before the controls of the page are mounted:
//
// - The styling properties of the TopLogic components are derived from the MUI theme of the page
//   (themeProperties.ts), so the components TopLogic keeps follow the customer's theme too.
// - The root wrapper (MuiRoot) puts the providers Material UI needs above every React root of the
//   bridge.
// - The replacements render the state of TopLogic controls with adapters around MUI components.
//   All other controls keep their TopLogic components.

import { registerRootWrapper, replace } from 'tl-react-bridge';
import MuiRoot, { pageTheme } from './MuiRoot';
import { installThemeProperties } from './themeProperties';
import MuiButtonAdapter from './adapters/MuiButtonAdapter';
import MuiToggleButtonAdapter from './adapters/MuiToggleButtonAdapter';
import MuiCheckboxAdapter from './adapters/MuiCheckboxAdapter';
import MuiTextInputAdapter from './adapters/MuiTextInputAdapter';
import MuiPasswordInputAdapter from './adapters/MuiPasswordInputAdapter';
import MuiNumberInputAdapter from './adapters/MuiNumberInputAdapter';
import MuiDatePickerAdapter from './adapters/MuiDatePickerAdapter';
import MuiSelectAdapter from './adapters/MuiSelectAdapter';
import MuiDropdownSelectAdapter from './adapters/MuiDropdownSelectAdapter';
import MuiOptionChipsAdapter from './adapters/MuiOptionChipsAdapter';
import MuiSegmentedChoiceAdapter from './adapters/MuiSegmentedChoiceAdapter';
import MuiChoiceGroupAdapter from './adapters/MuiChoiceGroupAdapter';
import MuiTabBarAdapter from './adapters/MuiTabBarAdapter';
import MuiWindowAdapter from './adapters/MuiWindowAdapter';
import MuiDialogAdapter from './adapters/MuiDialogAdapter';
import MuiMenuAdapter from './adapters/MuiMenuAdapter';
import MuiSnackbarAdapter from './adapters/MuiSnackbarAdapter';
import MuiAlertAdapter from './adapters/MuiAlertAdapter';
import MuiFormFieldAdapter from './adapters/MuiFormFieldAdapter';
import MuiTextAdapter from './adapters/MuiTextAdapter';
import MuiCardAdapter from './adapters/MuiCardAdapter';
import MuiAppBarAdapter from './adapters/MuiAppBarAdapter';
import MuiBreadcrumbAdapter from './adapters/MuiBreadcrumbAdapter';
import MuiProgressAdapter from './adapters/MuiProgressAdapter';
import MuiSliderAdapter from './adapters/MuiSliderAdapter';

installThemeProperties(pageTheme());
registerRootWrapper(MuiRoot);
replace('TLButton', MuiButtonAdapter);
replace('TLToggleButton', MuiToggleButtonAdapter);
replace('TLCheckbox', MuiCheckboxAdapter);
replace('TLTextInput', MuiTextInputAdapter);
replace('TLPasswordInput', MuiPasswordInputAdapter);
replace('TLNumberInput', MuiNumberInputAdapter);
replace('TLDatePicker', MuiDatePickerAdapter);
replace('TLSelect', MuiSelectAdapter);
replace('TLDropdownSelect', MuiDropdownSelectAdapter);
replace('TLOptionChips', MuiOptionChipsAdapter);
replace('TLSegmentedChoice', MuiSegmentedChoiceAdapter);
replace('TLChoiceGroup', MuiChoiceGroupAdapter);
replace('TLTabBar', MuiTabBarAdapter);
replace('TLWindow', MuiWindowAdapter);
replace('TLDialog', MuiDialogAdapter);
replace('TLMenu', MuiMenuAdapter);
replace('TLSnackbar', MuiSnackbarAdapter);
replace('TLAlert', MuiAlertAdapter);
replace('TLFormField', MuiFormFieldAdapter);
replace('TLText', MuiTextAdapter);
replace('TLCard', MuiCardAdapter);
replace('TLAppBar', MuiAppBarAdapter);
replace('TLBreadcrumb', MuiBreadcrumbAdapter);
replace('TLProgress', MuiProgressAdapter);
replace('TLSlider', MuiSliderAdapter);
