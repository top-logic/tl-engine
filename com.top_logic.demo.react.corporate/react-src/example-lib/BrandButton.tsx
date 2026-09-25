import React from 'react';
import type { ButtonHTMLAttributes, ReactNode } from 'react';
import { MissingProvider, brandAttributes, useBrand } from './brand';

/** Props of {@link BrandButton}. */
export interface BrandButtonProps extends Omit<ButtonHTMLAttributes<HTMLButtonElement>, 'onClick'> {
  /** Visual weight: a filled accent button or an outlined one. */
  variant?: 'primary' | 'secondary' | 'danger';
  /** Whether the button stands for a state that is currently switched on. */
  pressed?: boolean;
  /** Called when the button is clicked. */
  onClick?: () => void;
  children?: ReactNode;
}

/**
 * A button of the brand. Further attributes (`id`, `title`, `data-*`, `aria-*`) are passed to the
 * `<button>` element.
 */
export function BrandButton({ variant = 'secondary', pressed, onClick, className, children, ...rest }: BrandButtonProps) {
  const brand = useBrand();
  if (brand === null) {
    return <MissingProvider component="BrandButton" />;
  }
  return (
    <button
      type="button"
      {...rest}
      {...brandAttributes(brand, 'brand-button', [`brand-button--${variant}`, className].filter(Boolean).join(' '))}
      aria-pressed={pressed ? true : undefined}
      onClick={onClick}
    >
      {children}
    </button>
  );
}
