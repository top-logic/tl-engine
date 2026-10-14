// Installs Material UI on the page: the theme of the application, the providers above every React
// root, the styling properties of the TopLogic components and the adapters of the selected parts
// and components.

import { React, lockMode, registerRootWrapper, replace, replacePart } from 'tl-react-bridge';
import type { TLCellProps, PartName, PartComponent } from 'tl-react-bridge';
import type { ThemeOptions } from '@mui/material/styles';
import { createMuiRoot, createPageThemes, pageTheme } from './MuiRoot';
import { installThemeProperties, singleScheme } from './themeProperties';
import MuiButtonAdapter from './adapters/MuiButtonAdapter';
import MuiMenuTriggerAdapter from './adapters/MuiMenuTriggerAdapter';
import MuiDisclosureToggleAdapter from './adapters/MuiDisclosureToggleAdapter';
import MuiCheckboxAdapter from './adapters/MuiCheckboxAdapter';
import MuiTextInputAdapter from './adapters/MuiTextInputAdapter';
import MuiFieldValueAdapter from './adapters/MuiFieldValueAdapter';
import MuiChipAdapter from './adapters/MuiChipAdapter';
import MuiBadgeAdapter from './adapters/MuiBadgeAdapter';
import MuiDatePickerAdapter from './adapters/MuiDatePickerAdapter';
import MuiSelectAdapter from './adapters/MuiSelectAdapter';
import MuiDropdownSelectAdapter from './adapters/MuiDropdownSelectAdapter';
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

/**
 * The adapter around MUI components for each part (see PartProps of 'tl-react-bridge') this module
 * draws. A part is drawn wherever a control draws it: the part `Button` draws the button controls
 * (TLButton, TLToggleButton), the part `MenuTrigger` the trigger of a toolbar menu and of a menu
 * region, the part `DisclosureToggle` the toggles expanding a form group, a node of a tree, a row of
 * a tree table and the sidebar, the part `Checkbox` the boolean fields (TLCheckbox), the selection
 * boxes of a table and the boxes of the column selection, the part `TextInput` the text, password
 * and number fields (TLTextInput, TLPasswordInput, TLNumberInput), the search of a table and of its
 * column selection and the filters of a dropdown and of the icon and color popups, the part
 * `FieldValue` the value of every text, password, number, select, date and slider field that is
 * not editable and the chosen values without a color role of a choice field that is not editable,
 * the part `Chip` the toggles of an option chip field (TLOptionChips), the chosen values of a
 * dropdown, the named filters of a table, the files of a file chip field, every value with a color
 * role and every text drawn as a pill, the part `Badge` the counts of the sidebar and of the bottom
 * bar.
 */
const PART_ADAPTERS = {
  Button: MuiButtonAdapter,
  MenuTrigger: MuiMenuTriggerAdapter,
  DisclosureToggle: MuiDisclosureToggleAdapter,
  Checkbox: MuiCheckboxAdapter,
  TextInput: MuiTextInputAdapter,
  FieldValue: MuiFieldValueAdapter,
  Chip: MuiChipAdapter,
  Badge: MuiBadgeAdapter,
} as const satisfies { [K in PartName]?: PartComponent<K> };

/** The adapter around MUI components for each TopLogic component this module replaces. */
const CONTROL_ADAPTERS = {
  TLDatePicker: MuiDatePickerAdapter,
  TLSelect: MuiSelectAdapter,
  TLDropdownSelect: MuiDropdownSelectAdapter,
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

/** The name of a part this module draws with Material UI. */
type PartAdapterName = keyof typeof PART_ADAPTERS;

/** The name of a TopLogic component this module replaces by its adapter. */
type ControlAdapterName = keyof typeof CONTROL_ADAPTERS;

/**
 * The name of a part (e.g. `Button`) or of a TopLogic component (e.g. `TLWindow`) this module
 * renders with Material UI.
 */
export type ComponentName = PartAdapterName | ControlAdapterName;

/** The names of all parts and TopLogic components this module renders with Material UI. */
export const COMPONENT_NAMES: readonly ComponentName[] = Object.freeze(
  [...Object.keys(PART_ADAPTERS), ...Object.keys(CONTROL_ADAPTERS)] as ComponentName[]);

/** Whether the given name is one of a part this module draws. */
function isPartAdapterName(name: ComponentName): name is PartAdapterName {
  return Object.prototype.hasOwnProperty.call(PART_ADAPTERS, name);
}

/** Whether the given name is one of a TopLogic component this module replaces. */
function isControlAdapterName(name: ComponentName): name is ControlAdapterName {
  return Object.prototype.hasOwnProperty.call(CONTROL_ADAPTERS, name);
}

/** Replaces the given part by its adapter. */
function replacePartByAdapter<K extends PartAdapterName>(name: K): void {
  const adapters: { [P in PartAdapterName]: PartComponent<P> } = PART_ADAPTERS;
  replacePart(name, adapters[name]);
}

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
   * The parts and TopLogic components rendered with Material UI: {@link ALL_COMPONENTS}, or a
   * selection of {@link COMPONENT_NAMES}. A part (e.g. `Button`) is drawn with Material UI wherever a
   * control draws it, a component (e.g. `TLWindow`) is replaced as a whole. All other parts and
   * components keep their TopLogic rendering, styled after the theme. Defaults to
   * {@link ALL_COMPONENTS}.
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
 * parts and TopLogic components by their adapters.</p>
 *
 * <p>A theme with a single color scheme holds the page in the mode of that scheme (`lockMode` of
 * 'tl-react-bridge'): the MUI components render in that scheme whatever UI theme the user has
 * selected, and so do the TopLogic components, with every styling property of the design system
 * and of the UI themes. A theme with a light and a dark scheme follows the mode of the selected UI
 * theme.</p>
 *
 * <p>The application calls it once, when its module loads, before the controls of the page are
 * mounted. The installation cannot be changed afterwards: a second call fails.</p>
 *
 * @throws Error if Material UI is already installed, or the selection names a part or component
 *         this module does not render.
 */
export function installMui(options: MuiOptions): void {
  if (installed) {
    throw new Error('Material UI is already installed on this page.');
  }
  const selection = options.replace ?? ALL_COMPONENTS;
  const names = selection === ALL_COMPONENTS ? COMPONENT_NAMES : selection;
  const unknown = names.filter(name => !isPartAdapterName(name) && !isControlAdapterName(name));
  if (unknown.length > 0) {
    throw new Error(`No Material UI adapter for: ${unknown.join(', ')}. Known components: ${COMPONENT_NAMES.join(', ')}.`);
  }
  installed = true;

  const themes = createPageThemes(options.theme);
  const theme = pageTheme(themes);
  installThemeProperties(theme);
  const scheme = singleScheme(theme);
  if (scheme !== null) {
    lockMode(scheme);
  }
  registerRootWrapper(createMuiRoot(themes));
  for (const name of names) {
    if (isPartAdapterName(name)) {
      replacePartByAdapter(name);
    } else {
      replace(name, CONTROL_ADAPTERS[name]);
    }
  }
}
