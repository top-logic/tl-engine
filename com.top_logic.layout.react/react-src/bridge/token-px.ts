/**
 * Lengths of the design system read where they apply.
 *
 * A control that lays out by numbers - a virtual list placing its rows, a tree indenting its nodes, a
 * toolbar measuring what fits - takes those numbers from the tokens rather than from constants or the
 * server: the density (`data-tl-density`) and a customer theme change the tokens, and the control
 * follows them. A custom property's computed value has its `var()` references substituted, so the
 * token reads as the length it resolves to (`2.25rem`, `36px`).
 */
import React from 'react';

/**
 * The pixel value of a length token at the given element.
 *
 * @param element Where the token is read; the document root for `null`.
 * @param name The custom property, e.g. `--tl-size-row`.
 * @param fallback What is returned while the token is not defined (no design system loaded).
 */
export function tokenPx(element: Element | null, name: string, fallback: number): number {
  const at = element ?? document.documentElement;
  const value = getComputedStyle(at).getPropertyValue(name).trim();
  const n = parseFloat(value);
  if (isNaN(n)) {
    return fallback;
  }
  if (value.endsWith('rem')) {
    return n * (parseFloat(getComputedStyle(document.documentElement).fontSize) || 16);
  }
  if (value.endsWith('em')) {
    return n * (parseFloat(getComputedStyle(at).fontSize) || 16);
  }
  return n;
}

/**
 * The duration a token holds, in milliseconds - `150ms` or `0.15s` - read at the document root.
 *
 * @param name The custom property, e.g. `--tl-duration-base`.
 * @param fallback The value without a design system.
 */
export function tokenMs(name: string, fallback: number): number {
  const value = getComputedStyle(document.documentElement).getPropertyValue(name).trim();
  const n = parseFloat(value);
  if (isNaN(n)) {
    return fallback;
  }
  return value.endsWith('ms') ? n : value.endsWith('s') ? n * 1000 : n;
}

/** The attributes of the document root that switch the tokens. */
const TOKEN_SWITCHES = ['data-tl-density', 'data-tl-theme', 'data-tl-mode'];

/**
 * A length token as state: measured at the element once it is mounted, and again whenever the
 * document root switches density or theme.
 *
 * @param ref The element the token is read at.
 * @param name The custom property, e.g. `--tl-size-row`.
 * @param fallback The value before the first measurement and without a design system.
 */
export function useTokenPx(ref: React.RefObject<Element | null>, name: string, fallback: number): number {
  const [px, setPx] = React.useState(fallback);
  React.useLayoutEffect(() => {
    const measure = () => {
      const next = tokenPx(ref.current, name, fallback);
      setPx((previous) => (previous === next ? previous : next));
    };
    measure();
    const observer = new MutationObserver(measure);
    observer.observe(document.documentElement, { attributes: true, attributeFilter: TOKEN_SWITCHES });
    return () => observer.disconnect();
  }, [ref, name, fallback]);
  return px;
}
