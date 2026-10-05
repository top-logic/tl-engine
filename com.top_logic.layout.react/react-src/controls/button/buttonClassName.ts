import type { ButtonAppearance } from 'tl-react-bridge';
import { menuItemClassName } from '../menu/Menu';

/**
 * The class list of a button: block, appearance, tone, size, shape, typography, passed-through
 * classes. `icon` is a button showing only an icon: a square of the control height. A `menu-item`
 * is an entry of a menu (tl-menu__item, as MenuItem draws it): it keeps its tone - a destructive
 * entry is tl-menu__item--danger - but has no size or shape of its own; `current` marks the entry
 * in force, which reads strong.
 */
export function buttonClassName(opts: {
  appearance: ButtonAppearance; danger?: boolean; small?: boolean; icon?: boolean; current?: boolean; extra?: string;
}): string {
  if (opts.appearance === 'menu-item') {
    return menuItemClassName(opts.current, opts.extra, opts.danger);
  }
  return ['tl-button', `tl-button--${opts.appearance}`, 'tl-type-body',
    opts.danger && opts.appearance !== 'link' ? 'tl-button--danger' : '',
    opts.small ? 'tl-button--sm' : '',
    opts.icon ? 'tl-button--icon' : '',
    opts.extra ?? ''].filter(Boolean).join(' ');
}
