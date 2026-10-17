import React, { createContext, useContext } from 'react';

/**
 * Where a component is drawn, and how far an inset placed there keeps its content from the border.
 *
 * <p>A surface of the page - a panel, a card, an accordion - and a surface floating above it - a window, a drawer -
 * tell the components inside. A card takes its surface from the level: on the page the layer, inside a surface the
 * nested step, on a floating surface that surface's own (the nested step would lie below it in the dark mode and
 * sink in). An inset takes its padding from the inset kind. Outside any surface both are the page's.</p>
 */
export type SurfaceLevel = 'base' | 'layer' | 'overlay';

/** How far an inset keeps its content from the border: as on the page, in a panel, in a card. */
export type InsetKind = 'page' | 'panel' | 'card';

/** The surface a component is drawn on. */
export interface Surface {
  level: SurfaceLevel;
  inset: InsetKind;
}

const PAGE: Surface = { level: 'base', inset: 'page' };

const SurfaceContext = createContext<Surface>(PAGE);

/** Gives the components inside the surface they are drawn on. The value should keep its identity across renders. */
export function SurfaceProvider({ value, children }: React.PropsWithChildren<{ value: Surface }>) {
  return React.createElement(SurfaceContext.Provider, { value }, children);
}

/** The surface around the rendered component; the page outside any. */
export function useSurface(): Surface {
  return useContext(SurfaceContext);
}

/**
 * What a panel tells the components inside. Only what paints a surface raises the level: a panel drawn as a card
 * does, a plain panel paints none and passes on the level it lies on. Its insets are those of a card or a panel.
 */
export function panelSurface(outer: Surface, card: boolean): Surface {
  return { level: card ? 'layer' : outer.level, inset: card ? 'card' : 'panel' };
}

/** The class giving a card the surface of its level: none on the page. */
export function cardSurfaceClass(level: SurfaceLevel): string | undefined {
  if (level === 'layer') {
    return 'tl-card--nested';
  }
  if (level === 'overlay') {
    return 'tl-card--overlay';
  }
  return undefined;
}

/** The class giving an inset the padding of its surface: none on the page. */
export function insetClass(inset: InsetKind): string | undefined {
  return inset === 'page' ? undefined : `tl-inset--${inset}`;
}
