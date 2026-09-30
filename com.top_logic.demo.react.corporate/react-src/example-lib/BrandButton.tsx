import React from 'react';
import type { ButtonHTMLAttributes, ReactNode } from 'react';
import { MissingProvider, brandAttributes, useBrand } from './brand';

/** Props of {@link BrandButton}. */
export interface BrandButtonProps extends Omit<ButtonHTMLAttributes<HTMLButtonElement>, 'onClick'> {
  /** Visual weight: a filled accent button or an outlined one. */
  variant?: 'primary' | 'secondary' | 'danger';
  /** Size of the button. */
  size?: 'small' | 'medium';
  /** Whether the button stands for a state that is currently switched on. */
  pressed?: boolean;
  /**
   * An icon shown before the label. A button with an icon and no label is drawn as a square icon
   * button; it then needs an `aria-label` to be named for assistive technology.
   */
  icon?: ReactNode;
  /** Called when the button is clicked. */
  onClick?: () => void;
  /** The label of the button. */
  children?: ReactNode;
}

/**
 * A button of the brand. Further attributes (`id`, `title`, `data-*`, `aria-*`) are passed to the
 * `<button>` element.
 */
export function BrandButton({ variant = 'secondary', size = 'medium', pressed, icon, onClick, className, children, ...rest }: BrandButtonProps) {
  const brand = useBrand();
  if (brand === null) {
    return <MissingProvider component="BrandButton" />;
  }
  const hasLabel = children != null && children !== false && children !== '';
  const iconOnly = icon != null && !hasLabel;
  const classes = [
    `brand-button--${variant}`,
    size === 'small' ? 'brand-button--small' : '',
    iconOnly ? 'brand-button--icon-only' : '',
    className,
  ].filter(Boolean).join(' ');
  return (
    <button
      type="button"
      {...rest}
      {...brandAttributes(brand, 'brand-button', classes)}
      aria-pressed={pressed ? true : undefined}
      onClick={onClick}
    >
      {icon != null && <span className="brand-button__icon" aria-hidden="true">{icon}</span>}
      {hasLabel && <span className="brand-button__label">{children}</span>}
    </button>
  );
}
