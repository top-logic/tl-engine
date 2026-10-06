// Installs Material UI on the page: the theme of the application, the providers above every React
// root, the styling properties of the TopLogic components and the adapters of the selected
// components.

import { React, registerRootWrapper, replace } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import type { ThemeOptions } from '@mui/material/styles';
import { createMuiRoot, createPageThemes, pageTheme } from './MuiRoot';
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

/** The adapter around MUI components for each TopLogic component this module replaces. */
const ADAPTERS = {
  TLButton: MuiButtonAdapter,
  TLToggleButton: MuiToggleButtonAdapter,
  TLCheckbox: MuiCheckboxAdapter,
  TLTextInput: MuiTextInputAdapter,
  TLPasswordInput: MuiPasswordInputAdapter,
  TLNumberInput: MuiNumberInputAdapter,
  TLDatePicker: MuiDatePickerAdapter,
  TLSelect: MuiSelectAdapter,
  TLDropdownSelect: MuiDropdownSelectAdapter,
  TLOptionChips: MuiOptionChipsAdapter,
  TLSegmentedChoice: MuiSegmentedChoiceAdapter,
  TLChoiceGroup: MuiChoiceGroupAdapter,
  TLTabBar: MuiTabBarAdapter,
  TLWindow: MuiWindowAdapter,
  TLDialog: MuiDialogAdapter,
  TLMenu: MuiMenuAdapter,
  TLSnackbar: MuiSnackbarAdapter,
  TLAlert: MuiAlertAdapter,
  TLFormField: MuiFormFieldAdapter,
  TLText: MuiTextAdapter,
  TLCard: MuiCardAdapter,
  TLAppBar: MuiAppBarAdapter,
  TLBreadcrumb: MuiBreadcrumbAdapter,
  TLProgress: MuiProgressAdapter,
  TLSlider: MuiSliderAdapter,
} as const satisfies Record<string, React.ComponentType<TLCellProps>>;

/** The name of a TopLogic component this module renders with Material UI. */
export type ComponentName = keyof typeof ADAPTERS;

/** The names of all TopLogic components this module renders with Material UI. */
export const COMPONENT_NAMES: readonly ComponentName[] = Object.freeze(Object.keys(ADAPTERS) as ComponentName[]);

/** The value of {@link MuiOptions.replace} selecting all of {@link COMPONENT_NAMES}. */
export const ALL_COMPONENTS = 'all';

/** Options of {@link installMui}. */
export interface MuiOptions {
  /**
   * The options of the application's MUI theme, as `createTheme` takes them. The MUI components
   * render with this theme, and the TopLogic components follow it through the styling properties
   * derived from it.
   */
  theme: ThemeOptions;

  /**
   * The TopLogic components rendered with Material UI: {@link ALL_COMPONENTS}, or a selection of
   * {@link COMPONENT_NAMES}. All other components keep their TopLogic rendering, styled after the
   * theme. Defaults to {@link ALL_COMPONENTS}.
   */
  replace?: typeof ALL_COMPONENTS | readonly ComponentName[];
}

/** Whether {@link installMui} has run on this page. */
let installed = false;

/**
 * Renders the TopLogic React UI with Material UI.
 *
 * <p>Creates the MUI theme from the given options (one per page language, see
 * {@link createPageThemes}), writes the styling properties derived from it into the page, puts the
 * providers Material UI needs above every React root of the bridge and replaces the selected
 * TopLogic components by their adapters.</p>
 *
 * <p>The application calls it once, when its module loads, before the controls of the page are
 * mounted. The installation cannot be changed afterwards: a second call fails.</p>
 *
 * @throws Error if Material UI is already installed, or the selection names a component this
 *         module does not replace.
 */
export function installMui(options: MuiOptions): void {
  if (installed) {
    throw new Error('Material UI is already installed on this page.');
  }
  const selection = options.replace ?? ALL_COMPONENTS;
  const names = selection === ALL_COMPONENTS ? COMPONENT_NAMES : selection;
  const unknown = names.filter(name => !(name in ADAPTERS));
  if (unknown.length > 0) {
    throw new Error(`No Material UI adapter for: ${unknown.join(', ')}. Known components: ${COMPONENT_NAMES.join(', ')}.`);
  }
  installed = true;

  const themes = createPageThemes(options.theme);
  installThemeProperties(pageTheme(themes));
  registerRootWrapper(createMuiRoot(themes));
  for (const name of names) {
    replace(name, ADAPTERS[name]);
  }
}
