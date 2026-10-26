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

/** The attribute below the document root that switches the tokens: a view or a table names its own density. */
const DENSITY_SWITCH = ['data-tl-density'];

/**
 * A value read from the tokens as state: read when the control is mounted, and again whenever the document root
 * switches density, theme or mode. The state keeps its previous value when the new one is equal, so a switch that does
 * not touch what the control reads renders nothing; after unmount the observer is gone.
 *
 * A density is also named below the document root - a table carries its own - so the element the value is read at
 * and every element above it, up to the document root, are watched for `data-tl-density` as well.
 *
 * @param read Reads the value from the page now. The latest one given is the one called, so an inline closure may
 *        change between renders.
 * @param initial The value of the first render (a function is called once): before the first reading, where the
 *        element to read at does not exist yet, or the first reading itself.
 * @param same Whether two values read are the same; identity by default. The latest one given is the one called.
 * @param deps What `read` depends on: the reading is repeated, and the observer set up anew, when one of them changes.
 * @param target The element the value is read at; absent for a value read at the document root. The element is looked
 *        up when the observer is set up, so a target that is mounted later is watched from the next setup on.
 */
export function useTokenSwitch<T>(
  read: () => T,
  initial: T | (() => T),
  same: (a: T, b: T) => boolean = Object.is,
  deps: React.DependencyList = [],
  target?: React.RefObject<Element | null>,
): T {
  const [value, setValue] = React.useState<T>(initial);
  // The observer outlives a render: it calls what the latest render gave, not the first one's closure.
  const readRef = React.useRef(read);
  const sameRef = React.useRef(same);
  React.useLayoutEffect(() => {
    readRef.current = read;
    sameRef.current = same;
  });
  React.useLayoutEffect(() => {
    const update = () => {
      const next = readRef.current();
      setValue((previous) => (sameRef.current(previous, next) ? previous : next));
    };
    update();
    const observer = new MutationObserver(update);
    observer.observe(document.documentElement, { attributes: true, attributeFilter: TOKEN_SWITCHES });
    for (let at = target?.current ?? null; at && at !== document.documentElement; at = at.parentElement) {
      observer.observe(at, { attributes: true, attributeFilter: DENSITY_SWITCH });
    }
    return () => observer.disconnect();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [target, ...deps]);
  return value;
}

/**
 * A length token as state: measured at the element once it is mounted, and again whenever the
 * document root switches density or theme, or the element or one of its ancestors switches its density.
 *
 * @param ref The element the token is read at.
 * @param name The custom property, e.g. `--tl-size-row`.
 * @param fallback The value before the first measurement and without a design system.
 */
export function useTokenPx(ref: React.RefObject<Element | null>, name: string, fallback: number): number {
  return useTokenSwitch(() => tokenPx(ref.current, name, fallback), fallback, Object.is, [ref, name, fallback], ref);
}
