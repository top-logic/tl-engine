import type React from 'react';
import type { ComponentType } from 'react';
import type { ButtonAppearance } from './button-defaults';
import { menuItemProps } from './button-defaults';
import type { ButtonStateJson } from '../state/control-state';
import { TOOLTIP_ATTR, TOOLTIP_WHEN_ATTR, WHEN_TRUNCATED } from './tooltip-host';

// The parts (atoms) of the React UI: the presentational elements the controls draw themselves with
// - a button, the trigger of a menu. A part has a typed props contract and no server state: the
// control maps its state to the props of the part, sends its commands and handles keyboard bindings
// and navigation, and the part only draws. The controls bundle registers a default for every part
// (registerPart); a customer component library replaces a part (replacePart) and so changes how the
// part looks wherever a control draws it - as a control of its own, in a toolbar or in a table.
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
 * The parts of the React UI by name, each with its props. The name is what {@link registerPart},
 * {@link replacePart} and {@link usePart} take; the props are the contract every implementation of
 * the part fulfils.
 */
export interface PartProps {
  /** A button, or an entry of a menu: {@link ButtonPartProps}. */
  Button: ButtonPartProps;

  /** The button that opens a menu: {@link MenuTriggerPartProps}. */
  MenuTrigger: MenuTriggerPartProps;
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
