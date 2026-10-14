import type React from 'react';
import type { ComponentType } from 'react';
import type { ButtonAppearance } from './button-defaults';
import { menuItemProps } from './button-defaults';
import type { ButtonStateJson } from '../state/control-state';
import { TOOLTIP_ATTR, TOOLTIP_WHEN_ATTR, WHEN_TRUNCATED } from './tooltip-host';

// The parts (atoms) of the React UI: the presentational elements the controls draw themselves with
// - a button, the trigger of a menu, the toggle disclosing content. A part has a typed props
// contract and no server state: the control maps its state to the props of the part, sends its
// commands and handles keyboard bindings and navigation, and the part only draws. The controls
// bundle registers a default for every part (registerPart); a customer component library replaces a
// part (replacePart) and so changes how the part looks wherever a control draws it - as a control
// of its own, in a toolbar or in a table.
//
// A part is added by a props interface, an entry in PartProps, a default registered by the controls
// bundle and, where a library draws it, an adapter of that library.

/**
 * Attributes a part passes on to the element it draws as they are: the `aria-*` attributes naming
 * or describing the element where the props of the part do not, and `data-*` attributes another
 * component finds the element by.
 */
export interface PartAttributes {
  [attribute: `aria-${string}`]: string | number | boolean | undefined;
  [attribute: `data-${string}`]: string | number | boolean | undefined;
}

/**
 * The ref of a part: receives the element the part draws. A callback or a ref object, as React
 * takes them, stated without the types of React, so that an adapter module type-checking against
 * its own React types passes it to the components of its library.
 */
export type PartRef<T> = ((instance: T | null) => void) | { current: T | null } | null;

/**
 * What a button part shows of its {@link ButtonPartProps.image} and its
 * {@link ButtonPartProps.label}: the display mode a button control resolves from its state and its
 * container.
 */
export type ButtonDisplay = ButtonStateJson.DisplayMode;

/**
 * The props of the part {@link PartProps.Button}: a button, or an entry of a menu.
 *
 * <p>All values are resolved: the control has already applied the defaults of its container
 * ({@link ButtonAppearance} `menu-item` inside a menu, `icon-only` in a compact toolbar), so the
 * part draws exactly what it is given.</p>
 *
 * <p>A button with {@link checked} is a toggle: it always has a pressed state. Outside a menu it is
 * marked pressed while checked, inside a menu it is a checkbox entry. A button with {@link active}
 * is the alternative in force (e.g. the active theme): outside a menu it is marked pressed, inside
 * a menu it is the current entry.</p>
 *
 * <p>A button with {@link href} is a link: pressing it navigates to the address, as a link does,
 * so the button is drawn as an anchor with the look of a button.</p>
 *
 * <p>{@link buttonPartAttributes} gives the attributes every implementation sets on its element:
 * the accessible name, the pressed, current or checked state, the role and roving tabindex of a
 * menu entry, the tooltip. {@link buttonLinkAttributes} gives the attributes of the anchor of a
 * link.</p>
 */
export interface ButtonPartProps extends PartAttributes {
  /** The ID of the drawn element. */
  id?: string;

  /**
   * Receives the drawn element, e.g. as anchor of a popover: the button, or the anchor of a link
   * ({@link href}).
   */
  ref?: PartRef<HTMLElement>;

  /**
   * The address the button navigates to. A button with an address is a link, drawn as an anchor
   * with the look of the button. A disabled link has no address and is marked `aria-disabled`.
   */
  href?: string;

  /**
   * The browsing context a link ({@link href}) opens its address in, e.g. `_blank` for a window
   * of its own; absent: the current one. A link into a window of its own gets no access to the
   * page that opened it (`rel="noopener noreferrer"`).
   */
  target?: string;

  /**
   * The label. It is the accessible name of the button, also when the button does not show it,
   * and its tooltip wherever the button does not read it out.
   */
  label?: string;

  /** The icon, a theme image in its encoded form (see `ThemeIcon`); absent: none. */
  image?: string;

  /**
   * What the button shows. A button without {@link image} shows its label in every mode. Inside a
   * menu, an entry with an icon shows icon and label.
   */
  display: ButtonDisplay;

  /** How the button is drawn; `menu-item` draws an entry of the menu around it. */
  appearance: ButtonAppearance;

  /** Whether the button triggers a destructive action. A `link` shows no tone. */
  danger?: boolean;

  /** Whether the button is drawn small; applies to a button showing its icon alone. */
  small?: boolean;

  /** Whether the button stands for the alternative currently in force. */
  active?: boolean;

  /** The pressed state of a toggle; absent for a button that is no toggle. */
  checked?: boolean;

  /** Whether the button cannot be pressed. */
  disabled?: boolean;

  /**
   * An explicit tooltip, shown whatever the button shows. Absent: the label is the tooltip wherever
   * the button does not read it out.
   */
  tooltip?: string;

  /** Classes added to the drawn element. */
  className?: string;

  /** Called when the button is pressed. */
  onClick?: (event: React.MouseEvent<HTMLElement>) => void;

  /** Called when a mouse button goes down on the button, e.g. to keep the focus where it is. */
  onMouseDown?: (event: React.MouseEvent<HTMLElement>) => void;
}

/**
 * The props of the part {@link PartProps.MenuTrigger}: the button that opens a menu.
 *
 * <p>Its content is, in this order of precedence: the given {@link children}; the {@link icon}
 * alone, with the label as accessible name and tooltip; or the {@link label} followed by a chevron
 * pointing at the menu. Every implementation marks the trigger as opening a menu
 * (`aria-haspopup`) and whether that menu is open ({@link expanded}, `aria-expanded`).</p>
 */
export interface MenuTriggerPartProps extends PartAttributes {
  /** The ID of the drawn element. */
  id?: string;

  /** Receives the drawn element, the anchor of the menu and the inside of its outside press. */
  ref?: PartRef<HTMLButtonElement>;

  /** How the trigger is drawn, like the buttons beside it. */
  appearance: ButtonAppearance;

  /** Whether the menu of the trigger is open. */
  expanded: boolean;

  /** The label; the accessible name of an icon-only trigger. */
  label?: string;

  /** The icon of an icon-only trigger, a theme image in its encoded form (see `ThemeIcon`). */
  icon?: string;

  /** Free content of the trigger, drawn instead of label and icon. */
  children?: React.ReactNode;

  /** Classes added to the drawn element. */
  className?: string;

  /** Inline style of the drawn element. */
  style?: React.CSSProperties;

  /** Called when the trigger is pressed. */
  onClick?: (event: React.MouseEvent<HTMLElement>) => void;

  /** Called when a mouse button goes down on the trigger, e.g. to keep the focus where it is. */
  onMouseDown?: (event: React.MouseEvent<HTMLElement>) => void;
}

/**
 * The direction the content of a {@link DisclosureTogglePartProps disclosure toggle} extends in
 * from the toggle when it is shown:
 *
 * <ul>
 * <li>`below`: the content opens below the toggle - a section, a node of a tree, a row of a tree
 *     table. The chevron points to the end of the line while the content is hidden, and down
 *     while it is shown.</li>
 * <li>`end`: the content opens toward the end of the line - a sidebar extending into the page.
 *     The chevron points to the end while the content is hidden, and back to the start while it is
 *     shown.</li>
 * </ul>
 */
export type DisclosureDirection = 'below' | 'end';

/**
 * The props of the part {@link PartProps.DisclosureToggle}: the button that shows or hides content
 * belonging to it - the body of a section, the children of a node in a tree, a sidebar.
 *
 * <p>The toggle shows a chevron pointing where the content opens ({@link opens}) and is named by its
 * {@link label}, which is also its tooltip. Every implementation marks whether the content is shown
 * ({@link expanded}, `aria-expanded`); the element holding the content is named by `aria-controls`
 * where the control has one. While the content is being loaded ({@link loading}), the toggle shows a
 * busy indicator in place of the chevron.</p>
 */
export interface DisclosureTogglePartProps extends PartAttributes {
  /** The ID of the drawn element. */
  id?: string;

  /** Receives the drawn element. */
  ref?: PartRef<HTMLButtonElement>;

  /** Whether the content of the toggle is shown. */
  expanded: boolean;

  /**
   * The accessible name and tooltip of the toggle: what pressing it does, e.g. "Expand" or
   * "Collapse".
   */
  label: string;

  /** Where the content opens from the toggle; absent: `below`. */
  opens?: DisclosureDirection;

  /** Whether the content is being loaded; the toggle then shows a busy indicator. */
  loading?: boolean;

  /** Whether the toggle cannot be pressed. */
  disabled?: boolean;

  /**
   * The tab index of the toggle; `-1` keeps it out of the tab order, e.g. in a tree, which handles
   * the keyboard for its nodes itself. Absent: the toggle is in the tab order.
   */
  tabIndex?: number;

  /** Classes added to the drawn element. */
  className?: string;

  /** Called when the toggle is pressed. */
  onClick?: (event: React.MouseEvent<HTMLElement>) => void;
}

/**
 * The shape of a {@link CheckboxPartProps checkbox}:
 *
 * <ul>
 * <li>`checkbox`: a box that is ticked;</li>
 * <li>`switch`: a toggle sliding between its two states, with the role `switch`. A switch has no
 *     indeterminate state.</li>
 * </ul>
 */
export type CheckboxDisplay = 'checkbox' | 'switch';

/**
 * The state of the value a {@link CheckboxPartProps checkbox} stands for, as the checks of its form
 * field judge it:
 *
 * <ul>
 * <li>`error`: the value is invalid; the checkbox is marked `aria-invalid`.</li>
 * <li>`warning`: the value is questionable without being invalid.</li>
 * </ul>
 */
export type CheckboxValidation = 'error' | 'warning';

/**
 * The props of the part {@link PartProps.Checkbox}: a box that is ticked, or a switch that is
 * flipped - the input of a boolean field, the selection of a row, the choice whether a column is
 * shown.
 *
 * <p>The checkbox is controlled: it shows {@link checked} and {@link indeterminate} as given, and a
 * press asks for the new state through {@link onChange}; it shows the new state only once the
 * control passes it.</p>
 *
 * <p>Every implementation draws an indeterminate box as indeterminate and marks it
 * `aria-checked="mixed"`, marks a read-only box `aria-readonly`, an invalid one `aria-invalid` and a
 * required one `aria-required`, and puts {@link id}, {@link ref}, {@link name}, {@link tabIndex} and
 * the passed-through attributes on the focusable input. A checkbox without {@link label} is named by
 * the control: by an `aria-label`, or by `aria-labelledby` naming the label of its form field.</p>
 */
export interface CheckboxPartProps extends PartAttributes {
  /** The ID of the input. */
  id?: string;

  /** Receives the input. */
  ref?: PartRef<HTMLInputElement>;

  /** Whether the box is ticked, the switch on. */
  checked: boolean;

  /**
   * Whether the box shows that it is neither ticked nor clear: the "no value" of a tri-state field,
   * the selection of some of the rows of a table. It is drawn instead of {@link checked}. Ignored
   * for a switch.
   */
  indeterminate?: boolean;

  /** The shape; absent: `checkbox`. */
  display?: CheckboxDisplay;

  /**
   * The label shown beside the box: a text, or content such as the icon and text of an option. It
   * names the box, and pressing it presses the box. Absent: the box is drawn alone.
   */
  label?: React.ReactNode;

  /** Whether the box is drawn small, e.g. in the narrow selection column of a table. */
  small?: boolean;

  /** Whether the checkbox cannot be pressed; it is drawn inactive. */
  disabled?: boolean;

  /**
   * Whether the checkbox shows its state only: it is drawn as an active one, but a press changes
   * nothing and calls no {@link onChange}.
   */
  readOnly?: boolean;

  /** Whether the value is required. */
  required?: boolean;

  /** The state of the value; absent: valid. */
  validation?: CheckboxValidation;

  /** The name of the input. */
  name?: string;

  /**
   * The tab index of the input; `-1` keeps it out of the tab order, e.g. in the row of a table,
   * which handles the keyboard for its rows itself. Absent: the input is in the tab order.
   */
  tabIndex?: number;

  /** Classes added to the outermost drawn element: the label around the box, else the box. */
  className?: string;

  /**
   * Called when the checkbox is pressed, with the state it asks for: the opposite of
   * {@link checked}, `true` for an indeterminate box. Not called while {@link disabled} or
   * {@link readOnly}.
   */
  onChange?: (checked: boolean) => void;
}

/**
 * The parts of the React UI by name, each with its props. The name is what {@link registerPart},
 * {@link replacePart} and {@link usePart} take; the props are the contract every implementation of
 * the part fulfils.
 */
export interface PartProps {
  /** A button, or an entry of a menu: {@link ButtonPartProps}. */
  Button: ButtonPartProps;

  /** The button that opens a menu: {@link MenuTriggerPartProps}. */
  MenuTrigger: MenuTriggerPartProps;

  /** The button that shows or hides content: {@link DisclosureTogglePartProps}. */
  DisclosureToggle: DisclosureTogglePartProps;

  /** A box that is ticked, or a switch: {@link CheckboxPartProps}. */
  Checkbox: CheckboxPartProps;
}

/** The name of a part, see {@link PartProps}. */
export type PartName = keyof PartProps;

/** A component drawing the part of the given name. */
export type PartComponent<K extends PartName> = ComponentType<PartProps[K]>;

/** Components registered with {@link registerPart}, by part name. */
const _parts = new Map<PartName, ComponentType<never>>();

/** Components registered with {@link replacePart}, by part name. Consulted before {@link _parts}. */
const _partReplacements = new Map<PartName, ComponentType<never>>();

/**
 * Registers the default component drawing the given part.
 *
 * <p>The controls bundle registers one for every part. Registering a part a second time overrides
 * the first registration and logs a warning. A component registered with {@link replacePart} for
 * the same part takes precedence.</p>
 */
export function registerPart<K extends PartName>(name: K, component: PartComponent<K>): void {
  if (_parts.has(name)) {
    console.warn('[TLReact] Part registered twice, the later registration wins:', name);
  }
  _parts.set(name, component as ComponentType<never>);
}

/**
 * Replaces the component drawing the given part.
 *
 * <p>This is the hook for a customer component library: an adapter module draws a part with a
 * component of the library, wherever a control draws that part. The replacement takes precedence
 * over the component registered with {@link registerPart}, independent of the order in which the
 * scripts are loaded. Replacing a part a second time overrides the first replacement and logs a
 * warning.</p>
 *
 * <p>A control looks up its parts each time it renders; an adapter module registers its
 * replacements when its script loads, before the page's controls are mounted.</p>
 */
export function replacePart<K extends PartName>(name: K, component: PartComponent<K>): void {
  if (_partReplacements.has(name)) {
    console.warn('[TLReact] Part replaced twice, the later replacement wins:', name);
  }
  _partReplacements.set(name, component as ComponentType<never>);
}

/**
 * The component drawing the given part: the one registered with {@link replacePart}, else the one
 * registered with {@link registerPart}.
 *
 * @throws Error if no component is registered for the part.
 */
export function usePart<K extends PartName>(name: K): PartComponent<K> {
  const component = _partReplacements.get(name) ?? _parts.get(name);
  if (component === undefined) {
    throw new Error(`[TLReact] No component registered for the part: ${name}`);
  }
  return component as unknown as PartComponent<K>;
}

/** The relation of a link opening its address in a window of its own to the page it is on. */
const REL_NEW_WINDOW = 'noopener noreferrer';

/** The browsing context of a window of its own. */
const TARGET_NEW_WINDOW = '_blank';

/**
 * The attributes of the anchor an implementation of {@link ButtonPartProps} draws for a link
 * ({@link ButtonPartProps.href}): the address - none while disabled, which is then marked
 * `aria-disabled` -, the target and, for a window of its own, the relation that keeps the opened
 * page from reaching the page it was opened from.
 */
export function buttonLinkAttributes(props: ButtonPartProps): {
  href?: string; target?: string; rel?: string; 'aria-disabled'?: boolean;
} {
  const { href, target, disabled } = props;
  return {
    href: disabled ? undefined : href,
    target,
    rel: target === TARGET_NEW_WINDOW ? REL_NEW_WINDOW : undefined,
    'aria-disabled': disabled ? true : undefined,
  };
}

/**
 * The attributes every implementation of {@link ButtonPartProps} sets on the element it draws:
 *
 * <ul>
 * <li>the label as `aria-label` for a button with an icon or one showing its icon alone;</li>
 * <li>outside a menu `aria-pressed` for an active button or a checked toggle;</li>
 * <li>inside a menu the role and roving tabindex of an entry ({@link menuItemProps}),
 *     `aria-current` for the active entry and `aria-checked` for a toggle;</li>
 * <li>the tooltip: the explicit one, else the label - always for a button showing its icon alone,
 *     otherwise only while the label is clipped.</li>
 * </ul>
 */
export function buttonPartAttributes(props: ButtonPartProps): Record<string, string | number | boolean | undefined> {
  const { label, image, display, appearance, active, checked, tooltip } = props;
  const asMenuItem = appearance === 'menu-item';
  const iconOnly = display === 'icon-only';
  const result: Record<string, string | number | boolean | undefined> = {
    ...menuItemProps({ appearance }, checked),
    'aria-pressed': !asMenuItem && (checked ?? active) ? true : undefined,
    'aria-current': asMenuItem && active ? 'true' : undefined,
    'aria-checked': asMenuItem && checked !== undefined ? checked : undefined,
    'aria-label': image || iconOnly ? label : undefined,
  };
  const tooltipText = tooltip ?? label;
  if (tooltipText) {
    result[TOOLTIP_ATTR] = `text:${tooltipText}`;
    if (!tooltip && !iconOnly) {
      result[TOOLTIP_WHEN_ATTR] = WHEN_TRUNCATED;
    }
  }
  return result;
}
