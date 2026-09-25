import React from 'react';
import type { InputHTMLAttributes, ReactNode } from 'react';
import { MissingProvider, brandAttributes, useBrand } from './brand';

/** Props of {@link BrandCheckbox}. */
export interface BrandCheckboxProps
  extends Omit<InputHTMLAttributes<HTMLInputElement>, 'type' | 'checked' | 'onChange' | 'readOnly'> {
  /** Whether the box is ticked. */
  checked: boolean;
  /** Called with the new state when the user toggles the box. */
  onChange?: (checked: boolean) => void;
  /** Shows the state without letting the user change it. */
  readOnly?: boolean;
  /** Marks the box as having an invalid value. */
  invalid?: boolean;
  /** Optional text next to the box. */
  label?: ReactNode;
}

/**
 * A checkbox of the brand: a native checkbox drawn in the brand's style, optionally with a label.
 * Further attributes (`id`, `title`, `data-*`, `aria-*`) are passed to the `<input>` element.
 */
export function BrandCheckbox({ checked, onChange, readOnly, invalid, disabled, label, className, ...rest }: BrandCheckboxProps) {
  const brand = useBrand();
  if (brand === null) {
    return <MissingProvider component="BrandCheckbox" />;
  }
  const root = brandAttributes(brand, 'brand-checkbox',
    [invalid ? 'brand-checkbox--invalid' : '', readOnly ? 'brand-checkbox--readonly' : '', className].filter(Boolean).join(' '));
  const box = (
    <input
      {...rest}
      type="checkbox"
      className="brand-checkbox__box"
      checked={checked}
      disabled={disabled || readOnly}
      aria-invalid={invalid || undefined}
      onChange={e => onChange?.(e.target.checked)}
    />
  );
  return (
    <label {...root}>
      {box}
      {label != null && <span className="brand-checkbox__label">{label}</span>}
    </label>
  );
}
