import { React } from 'tl-react-bridge';

export type ButtonAppearance = 'primary' | 'secondary' | 'ghost' | 'link';

/**
 * What a container tells the buttons inside it. The server state of a button wins over these
 * defaults; the defaults win over the built-in default (`secondary`).
 *
 * - `appearance`: a toolbar says `ghost`, a dialog's button bar says `secondary`.
 */
export interface ButtonDefaultsValue {
  appearance?: ButtonAppearance;
}

const Context = React.createContext<ButtonDefaultsValue>({});

export function ButtonDefaults({ children, ...value }: React.PropsWithChildren<ButtonDefaultsValue>) {
  const parent = React.useContext(Context);
  const merged = React.useMemo(() => ({ ...parent, ...value }), [parent, value.appearance]);
  return <Context.Provider value={merged}>{children}</Context.Provider>;
}

export function useButtonDefaults(): ButtonDefaultsValue {
  return React.useContext(Context);
}

/**
 * The class list of a button: block, appearance, tone, size, shape, typography, passed-through
 * classes. `icon` is a button showing only an icon: a square of the control height.
 */
export function buttonClassName(opts: {
  appearance: ButtonAppearance; danger?: boolean; small?: boolean; icon?: boolean; extra?: string;
}): string {
  return ['tl-button', `tl-button--${opts.appearance}`, 'tl-type-body',
    opts.danger && opts.appearance !== 'link' ? 'tl-button--danger' : '',
    opts.small ? 'tl-button--sm' : '',
    opts.icon ? 'tl-button--icon' : '',
    opts.extra ?? ''].filter(Boolean).join(' ');
}
