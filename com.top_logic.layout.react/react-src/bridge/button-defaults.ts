import React, { createContext, useContext, useMemo } from 'react';

/**
 * The look of a button: its own appearance, or `menu-item` for an entry of a menu.
 */
export type ButtonAppearance = 'primary' | 'secondary' | 'ghost' | 'link' | 'menu-item';

/**
 * What a container tells the buttons inside it (`TLButton`, `TLToggleButton`, or a replacement of
 * them). The server state of a button wins over these defaults; the defaults win over the built-in
 * default (`secondary`).
 *
 * - `appearance`: a toolbar or an app bar says `ghost`, the button bar of a window says
 *   `secondary`. A menu says `menu-item`, and that one wins over the server: inside a menu every
 *   button is an entry, carrying the {@link menuItemProps} the menu's keyboard navigation finds its
 *   entries by.
 * - `iconOnly`: a compact toolbar says `true`; every button that shows its icon drops its label
 *   then and becomes a square icon button, its label moving to the tooltip.
 */
export interface ButtonDefaultsValue {
  appearance?: ButtonAppearance;
  iconOnly?: boolean;
}

/** Outside every container: no defaults. */
const NO_DEFAULTS: ButtonDefaultsValue = Object.freeze({});

const ButtonDefaultsContext = createContext<ButtonDefaultsValue>(NO_DEFAULTS);

/**
 * Gives the buttons inside the given defaults, merged over the ones of the enclosing containers.
 */
export function ButtonDefaults({ children, ...value }: React.PropsWithChildren<ButtonDefaultsValue>) {
  const parent = useContext(ButtonDefaultsContext);
  const merged = useMemo(() => ({ ...parent, ...value }), [parent, value.appearance, value.iconOnly]);
  return React.createElement(ButtonDefaultsContext.Provider, { value: merged }, children);
}

/** The defaults the containers around the rendered button give it, see {@link ButtonDefaultsValue}. */
export function useButtonDefaults(): ButtonDefaultsValue {
  return useContext(ButtonDefaultsContext);
}

/**
 * What an entry of a menu carries besides its look: the role and the roving tabindex, by which the
 * menu finds its entries for its keyboard navigation (arrows, Home, End, Escape). An entry is a
 * `menuitem`; a toggle, which always has a pressed state (`checked` true or false), is a
 * `menuitemcheckbox`. Outside a menu, nothing.
 */
export function menuItemProps(defaults: ButtonDefaultsValue, checked?: boolean): { role?: string; tabIndex?: number } {
  if (defaults.appearance !== 'menu-item') return {};
  return { role: checked === undefined ? 'menuitem' : 'menuitemcheckbox', tabIndex: -1 };
}
