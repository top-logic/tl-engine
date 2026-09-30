// A stand-in for a customer's React component library.
//
// Everything in this folder knows nothing about TopLogic: it imports React from 'react' (redirected
// to the shared React instance by the aliases in vite.config.ts, like any third-party library) and
// offers plain controlled components with ordinary props. Like many real libraries, its components
// need a provider above them and render an error marker when it is missing.

import React, { createContext, useContext } from 'react';
import type { ReactNode } from 'react';

/** Corner style of the brand's controls. */
export type BrandCorners = 'pill' | 'square';

/** The brand settings a {@link BrandProvider} makes available to the components below it. */
export interface BrandSettings {
  /** Name of the brand, written to the `data-brand` attribute of each component. */
  name: string;
  /** Accent colour (any CSS colour) of primary buttons and checked boxes. */
  accent: string;
  /** Corner style of buttons and boxes. */
  corners: BrandCorners;
}

const BrandContext = createContext<BrandSettings | null>(null);

/** Props of {@link BrandProvider}. */
export interface BrandProviderProps extends BrandSettings {
  children?: ReactNode;
}

/** Makes the brand settings available to all brand components rendered below it. */
export function BrandProvider({ children, ...settings }: BrandProviderProps) {
  return <BrandContext.Provider value={settings}>{children}</BrandContext.Provider>;
}

/** Class of the marker a brand component renders in place of itself without a provider. */
export const BRAND_ERROR_CLASS = 'brand-error';

/**
 * The brand settings of the enclosing {@link BrandProvider}, or `null` if there is none.
 */
export function useBrand(): BrandSettings | null {
  return useContext(BrandContext);
}

/**
 * The marker a brand component renders when no {@link BrandProvider} is above it.
 */
export function MissingProvider({ component }: { component: string }) {
  console.error(`[brand] <${component}> rendered outside of a <BrandProvider>.`);
  return (
    <span className={BRAND_ERROR_CLASS} role="alert">
      {component}: missing BrandProvider
    </span>
  );
}

/** The attributes shared by the root element of all brand components. */
export function brandAttributes(brand: BrandSettings, className: string, extra?: string) {
  return {
    'data-brand': brand.name,
    className: [className, `${className}--${brand.corners}`, extra].filter(Boolean).join(' '),
    style: { '--brand-accent': brand.accent } as React.CSSProperties,
  };
}
