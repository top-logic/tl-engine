import { React } from 'tl-react-bridge';

export type ButtonAppearance = 'primary' | 'secondary' | 'ghost' | 'link' | 'menu-item';

/**
 * What a container tells the buttons inside it. The server state of a button wins over these
 * defaults; the defaults win over the built-in default (`secondary`).
 *
 * - `appearance`: a toolbar says `ghost`, a dialog's button bar says `secondary`. A menu says
 *   `menu-item`, and that one wins over the server: inside a menu every button is an entry.
 * - `iconOnly`: a compact toolbar says `true`; every button that shows its icon drops its label
 *   then and becomes a square icon button, its label moving to the tooltip.
 */
export interface ButtonDefaultsValue {
  appearance?: ButtonAppearance;
  iconOnly?: boolean;
}

const Context = React.createContext<ButtonDefaultsValue>({});

export function ButtonDefaults({ children, ...value }: React.PropsWithChildren<ButtonDefaultsValue>) {
  const parent = React.useContext(Context);
  const merged = React.useMemo(() => ({ ...parent, ...value }), [parent, value.appearance, value.iconOnly]);
  return <Context.Provider value={merged}>{children}</Context.Provider>;
}

export function useButtonDefaults(): ButtonDefaultsValue {
  return React.useContext(Context);
}

/**
 * The class list of a button: block, appearance, tone, size, shape, typography, passed-through
 * classes. `icon` is a button showing only an icon: a square of the control height. A `menu-item`
 * is an entry of a menu (tl-menu__item), which has no tone, size or shape of its own.
 */
export function buttonClassName(opts: {
  appearance: ButtonAppearance; danger?: boolean; small?: boolean; icon?: boolean; extra?: string;
}): string {
  if (opts.appearance === 'menu-item') {
    return ['tl-menu__item', 'tl-type-body', opts.extra ?? ''].filter(Boolean).join(' ');
  }
  return ['tl-button', `tl-button--${opts.appearance}`, 'tl-type-body',
    opts.danger && opts.appearance !== 'link' ? 'tl-button--danger' : '',
    opts.small ? 'tl-button--sm' : '',
    opts.icon ? 'tl-button--icon' : '',
    opts.extra ?? ''].filter(Boolean).join(' ');
}

/**
 * What an entry of a menu carries besides its classes: the role and the roving tabindex. A
 * button with a pressed state (`pressed` true or false) is a checkbox entry; outside a menu,
 * nothing.
 */
export function menuItemProps(defaults: ButtonDefaultsValue, pressed?: boolean): { role?: string; tabIndex?: number } {
  if (defaults.appearance !== 'menu-item') return {};
  return { role: pressed === undefined ? 'menuitem' : 'menuitemcheckbox', tabIndex: -1 };
}
