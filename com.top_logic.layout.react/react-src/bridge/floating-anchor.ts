/**
 * The layer an element floating over a frame - a floating action button - is rendered into.
 *
 * <p>A frame is a box whose content scrolls in a body of its own: a panel, a window. An element
 * floating over the corner of such a body must not scroll with it, so it is not placed inside the
 * body; and it must not float over the browser window either, where the frames of a split view
 * would put theirs on top of each other. The frame therefore offers two rails: boxes of no height
 * directly before and after its body, positioned, so that an element portaled into a rail is placed
 * against the top or the bottom edge of the body and over the full width of the frame, and stays
 * there while the body scrolls. The frame keeps its own box and the body its position: an
 * absolutely positioned element inside the body (a maximized panel) still resolves against the body.</p>
 *
 * <p>The rails exist only while something floats in the frame: a frame renders them once a
 * descendant asks for them with {@link useFloatingAnchor}, so that a frame without a floating element
 * keeps its markup.</p>
 *
 * <p>A frame (the component rendering it):</p>
 * <pre>
 * const floating = useFloatingFrame();
 * &lt;FloatingRail host={floating} edge="top" /&gt;
 * &lt;div className="…body"&gt;&lt;FloatingFrame host={floating}&gt;{content}&lt;/FloatingFrame&gt;&lt;/div&gt;
 * &lt;FloatingRail host={floating} edge="bottom" /&gt;
 * </pre>
 */
import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';

/** The edge of the body of a frame a floating element is placed against. */
export type FloatingEdge = 'top' | 'bottom';

/** The class of a rail, the design system places it (`floating-action.css`). */
export const FLOATING_RAIL_CLASS = 'tl-floating-rail';

/** What a frame tells the floating elements inside it. */
interface FloatingFrameValue {
  /** The rail before the body, while it is rendered. */
  top: HTMLElement | null;

  /** The rail after the body, while it is rendered. */
  bottom: HTMLElement | null;

  /** Asks the frame for its rails; the returned function withdraws the request. */
  request: () => () => void;
}

const FloatingFrameContext = createContext<FloatingFrameValue | null>(null);

/**
 * The rails of a frame, see {@link useFloatingFrame}.
 */
export interface FloatingFrameHost {
  /** Whether an element floats in the frame: the rails are rendered while it does. */
  readonly active: boolean;

  /** Receives the rail before the body. */
  readonly topRef: (element: HTMLElement | null) => void;

  /** Receives the rail after the body. */
  readonly bottomRef: (element: HTMLElement | null) => void;

  /** What the frame gives the floating elements inside it, see {@link FloatingFrame}. */
  readonly value: FloatingFrameValue;
}

/**
 * Makes the calling component a frame: an element floating inside its content is placed against
 * its body. Render the rails with {@link FloatingRail} around the body and the content inside
 * {@link FloatingFrame}.
 */
export function useFloatingFrame(): FloatingFrameHost {
  const [requests, setRequests] = useState(0);
  const [top, topRef] = useState<HTMLElement | null>(null);
  const [bottom, bottomRef] = useState<HTMLElement | null>(null);

  const request = useCallback(() => {
    setRequests(count => count + 1);
    let withdrawn = false;
    return () => {
      if (withdrawn) return;
      withdrawn = true;
      setRequests(count => count - 1);
    };
  }, []);

  const value = useMemo<FloatingFrameValue>(() => ({ top, bottom, request }), [top, bottom, request]);
  return { active: requests > 0, topRef, bottomRef, value };
}

/**
 * Gives the content of a frame the rails of the frame, see {@link useFloatingFrame}.
 */
export function FloatingFrame({ host, children }: React.PropsWithChildren<{ host: FloatingFrameHost }>) {
  return React.createElement(FloatingFrameContext.Provider, { value: host.value }, children);
}

/**
 * The rail of a frame at the given edge of its body, rendered while an element floats in the frame:
 * directly before the body for the top edge, directly after it for the bottom edge.
 */
export function FloatingRail({ host, edge }: { host: FloatingFrameHost; edge: FloatingEdge }) {
  if (!host.active) {
    return null;
  }
  return React.createElement('div', {
    ref: edge === 'top' ? host.topRef : host.bottomRef,
    className: FLOATING_RAIL_CLASS,
    'data-tl-edge': edge,
  });
}

/**
 * Where an element floating at an edge is rendered, see {@link useFloatingAnchor}.
 */
export interface FloatingAnchor {
  /** The element to portal the floating element into. */
  readonly container: HTMLElement;

  /**
   * Whether no frame encloses the floating element: the container is the document body, and the
   * element floats over the corner of the browser window.
   */
  readonly viewport: boolean;
}

/**
 * Where an element floating at the given edge of the body of the enclosing frame is rendered: the
 * rail of the frame at that edge, or the document body outside of every frame.
 *
 * <p>The frame renders its rails on the first request of this hook, so the anchor is
 * {@code null} in the render before: the element is rendered once the rail exists.</p>
 */
export function useFloatingAnchor(edge: FloatingEdge): FloatingAnchor | null {
  const frame = useContext(FloatingFrameContext);
  const request = frame?.request;

  useEffect(() => request?.(), [request]);

  return useMemo(() => {
    if (frame == null) {
      return { container: document.body, viewport: true };
    }
    const rail = edge === 'top' ? frame.top : frame.bottom;
    return rail == null ? null : { container: rail, viewport: false };
  }, [frame, edge]);
}
