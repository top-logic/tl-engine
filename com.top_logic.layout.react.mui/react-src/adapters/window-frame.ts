// The geometry of a window: where it stands, how large it is, and the gestures that change both
// (moving it by its title bar, resizing it by its edges, maximizing it).
//
// The behaviour is the one of TLWindow, for a window drawn with other components: a window is
// centered by the backdrop around it until the user moves or resizes it, then stands at a position
// of its own; the server keeps no position, only the size the user gave the window last (command
// `resize`, forgotten again with `resetSize`).

import { React, startPointerDrag } from 'tl-react-bridge';
import type { WindowStateJson } from 'tl-react-bridge';

const { useCallback, useEffect, useRef, useState } = React;

/** Command reporting the size the user gave the window. */
export const CMD_RESIZE = 'resize';

/** Argument of {@link CMD_RESIZE}: the width in pixels. */
export const ARG_WIDTH = 'width';

/** Argument of {@link CMD_RESIZE}: the height in pixels. */
export const ARG_HEIGHT = 'height';

/** Command making the server forget the size the user gave the window. */
export const CMD_RESET_SIZE = 'resetSize';

/** The width of a window whose state names none. */
const DEFAULT_WIDTH = '32rem';

/** The smallest width a window can be resized to. */
const MIN_WIDTH = 200;

/** The smallest height a window can be resized to. */
const MIN_HEIGHT = 100;

/**
 * The space a window keeps free towards each edge of the browser window, when it is resized and
 * when a remembered size is checked against the browser window.
 */
const VIEWPORT_MARGIN = 24;

/** An edge or corner of a window a resize gesture starts at. */
export type ResizeDir = 'n' | 'ne' | 'e' | 'se' | 's' | 'sw' | 'w' | 'nw';

/** The edges and corners a resizable window can be resized at. */
export const RESIZE_HANDLES: readonly ResizeDir[] = ['n', 'ne', 'e', 'se', 's', 'sw', 'w', 'nw'];

/** The cursor a resize handle shows, kept for the whole gesture by the drag shield. */
export const RESIZE_CURSORS: Record<ResizeDir, string> = {
  n: 'ns-resize', s: 'ns-resize',
  e: 'ew-resize', w: 'ew-resize',
  ne: 'nesw-resize', sw: 'nesw-resize',
  nw: 'nwse-resize', se: 'nwse-resize',
};

/** Thickness of an edge handle, in pixels; it overlaps the edge by half of it. */
const EDGE = 6;

/** Size of a corner handle, in pixels. */
const CORNER = 12;

/** Distance of an edge handle from the corners, leaving them to the corner handles. */
const EDGE_INSET = 8;

/** Where each resize handle stands on the window and how large it is. */
export const RESIZE_HANDLE_STYLES: Record<ResizeDir, React.CSSProperties> = {
  n: { top: -EDGE / 2, left: EDGE_INSET, right: EDGE_INSET, height: EDGE },
  s: { bottom: -EDGE / 2, left: EDGE_INSET, right: EDGE_INSET, height: EDGE },
  e: { top: EDGE_INSET, bottom: EDGE_INSET, right: -EDGE / 2, width: EDGE },
  w: { top: EDGE_INSET, bottom: EDGE_INSET, left: -EDGE / 2, width: EDGE },
  ne: { top: -EDGE / 2, right: -EDGE / 2, width: CORNER, height: CORNER },
  nw: { top: -EDGE / 2, left: -EDGE / 2, width: CORNER, height: CORNER },
  se: { bottom: -EDGE / 2, right: -EDGE / 2, width: CORNER, height: CORNER },
  sw: { bottom: -EDGE / 2, left: -EDGE / 2, width: CORNER, height: CORNER },
};

/** The largest width a window may take in a browser window of the given width. */
function maxWindowWidth(viewportWidth: number): number {
  return Math.max(MIN_WIDTH, viewportWidth - 2 * VIEWPORT_MARGIN);
}

/** The largest height a window may take in a browser window of the given height. */
function maxWindowHeight(viewportHeight: number): number {
  return Math.max(MIN_HEIGHT, viewportHeight - 2 * VIEWPORT_MARGIN);
}

/** The size of the browser window, updated when the browser window is resized. */
function useViewportSize(): { width: number; height: number } {
  const [size, setSize] = useState(() => ({ width: window.innerWidth, height: window.innerHeight }));
  useEffect(() => {
    const update = () => setSize({ width: window.innerWidth, height: window.innerHeight });
    window.addEventListener('resize', update);
    return () => window.removeEventListener('resize', update);
  }, []);
  return size;
}

/** A point in the viewport, in pixels. */
interface Point {
  x: number;
  y: number;
}

/** The geometry of a window and the handlers of the gestures changing it. */
export interface WindowFrame {
  /** The element of the window, measured when a gesture starts. */
  windowRef: React.RefObject<HTMLDivElement | null>;

  /** The position and size of the window, as inline style of its element. */
  style: React.CSSProperties;

  /** Whether the window covers the browser window. */
  maximized: boolean;

  /** Starts moving the window; for the `pointerdown` of the title bar. */
  onTitlePointerDown: (event: React.PointerEvent) => void;

  /** Maximizes the window, or gives it back the bounds it had before. */
  toggleMaximize: () => void;

  /** Starts resizing the window at the given edge or corner; for the `pointerdown` of a handle. */
  onResizePointerDown: (dir: ResizeDir, event: React.PointerEvent) => void;

  /** Gives the window its configured size back; for the double click on a handle. */
  resetSize: () => void;
}

/**
 * The geometry of the window with the given state.
 *
 * @param state The state of the window: its configured and its remembered size.
 * @param sendCommand Sends the commands {@link CMD_RESIZE} and {@link CMD_RESET_SIZE}.
 */
export function useWindowFrame(
  state: Partial<WindowStateJson>,
  sendCommand: (command: string, args?: Record<string, unknown>) => unknown,
): WindowFrame {
  const serverWidth = state.width ?? DEFAULT_WIDTH;
  const serverHeight = state.height ?? null;
  const customWidth = state.customWidth ?? null;
  const customHeight = state.customHeight ?? null;
  const viewport = useViewportSize();
  const customFits = customWidth != null && customHeight != null
    && customWidth <= maxWindowWidth(viewport.width)
    && customHeight <= maxWindowHeight(viewport.height);

  // The size during and after a resize gesture; null: the size of the state.
  const [localWidth, setLocalWidth] = useState<number | null>(null);
  const [localHeight, setLocalHeight] = useState<number | null>(null);
  const localWidthRef = useRef<number | null>(null);
  const localHeightRef = useRef<number | null>(null);

  // The position once the window was moved or resized; null: centered by the backdrop.
  const [position, setPosition] = useState<Point | null>(null);
  const positionRef = useRef<Point | null>(null);

  const [maximized, setMaximized] = useState(false);
  const regularBoundsRef = useRef<{ x: number; y: number; w: number | null; h: number | null } | null>(null);

  const windowRef = useRef<HTMLDivElement | null>(null);

  const place = (pos: Point | null) => {
    positionRef.current = pos;
    setPosition(pos);
  };

  const size = (w: number | null, h: number | null) => {
    localWidthRef.current = w;
    localHeightRef.current = h;
    setLocalWidth(w);
    setLocalHeight(h);
  };

  const onResizePointerDown = useCallback((dir: ResizeDir, e: React.PointerEvent) => {
    e.preventDefault();
    const el = windowRef.current;
    if (!el) return;
    const rect = el.getBoundingClientRect();

    // A centered window grows to both sides, so the handle stays under the pointer when the size
    // changes by twice the distance moved. It stands at a position of its own from now on.
    const symmetric = !positionRef.current;
    const startPos = positionRef.current ?? { x: rect.left, y: rect.top };
    if (symmetric) {
      place(startPos);
    }
    const startW = rect.width;
    const startH = rect.height;

    startPointerDrag(e, {
      cursor: RESIZE_CURSORS[dir],

      onMove: (ev) => {
        const dx = ev.clientX - e.clientX;
        const dy = ev.clientY - e.clientY;
        const factor = symmetric ? 2 : 1;
        let w = startW;
        let h = startH;
        if (dir.includes('e')) w = startW + factor * dx;
        if (dir.includes('w')) w = startW - factor * dx;
        if (dir.includes('s')) h = startH + factor * dy;
        if (dir.includes('n')) h = startH - factor * dy;

        // The window never grows beyond the browser window, so the size reported and remembered
        // for it fits again when the window is opened next time.
        const newW = Math.min(maxWindowWidth(window.innerWidth), Math.max(MIN_WIDTH, w));
        const newH = Math.min(maxWindowHeight(window.innerHeight), Math.max(MIN_HEIGHT, h));

        let posXDelta = 0;
        let posYDelta = 0;
        if (symmetric) {
          // The center stays where it is.
          posXDelta = (startW - newW) / 2;
          posYDelta = (startH - newH) / 2;
        } else {
          // The opposite edge stays where it is, also where the size hit its minimum or maximum.
          if (dir.includes('w')) posXDelta = startW - newW;
          if (dir.includes('n')) posYDelta = startH - newH;
        }

        size(newW, newH);
        place({ x: startPos.x + posXDelta, y: startPos.y + posYDelta });
      },

      onEnd: (_event, dragged) => {
        // A press that stayed where it was leaves the window as it is and reports nothing.
        const lw = localWidthRef.current;
        const lh = localHeightRef.current;
        if (dragged && (lw != null || lh != null)) {
          sendCommand(CMD_RESIZE, {
            ...(lw != null ? { [ARG_WIDTH]: Math.round(lw) } : {}),
            ...(lh != null ? { [ARG_HEIGHT]: Math.round(lh) } : {}),
          });
        }
      },

      onCancel: () => {
        // Nothing is reported, so the window returns to the size and place it had before.
        size(startW, startH);
        place(symmetric ? null : { ...startPos });
      },
    });
  }, [sendCommand]);

  const resetSize = useCallback(() => {
    size(null, null);
    place(null);
    sendCommand(CMD_RESET_SIZE);
  }, [sendCommand]);

  const onTitlePointerDown = useCallback((e: React.PointerEvent) => {
    // Only the main button moves the window, and a press on a button of the title bar is that
    // button's.
    if (e.button !== 0 || (e.target as HTMLElement).closest('button')) return;
    e.preventDefault();

    const el = windowRef.current;
    if (!el) return;
    const rect = el.getBoundingClientRect();

    const wasCentered = positionRef.current === null;
    const startPos = positionRef.current ?? { x: rect.left, y: rect.top };
    const offsetX = e.clientX - startPos.x;
    const offsetY = e.clientY - startPos.y;

    // The server keeps no position, so a finished move reports nothing.
    startPointerDrag(e, {
      cursor: 'grabbing',

      onMove: (ev) => {
        // The window stays inside the browser window.
        const maxX = window.innerWidth - el.offsetWidth;
        const maxY = window.innerHeight - el.offsetHeight;
        place({
          x: Math.max(0, Math.min(ev.clientX - offsetX, maxX)),
          y: Math.max(0, Math.min(ev.clientY - offsetY, maxY)),
        });
      },

      onCancel: () => {
        place(wasCentered ? null : { ...startPos });
      },
    });
  }, []);

  const toggleMaximize = useCallback(() => {
    if (maximized) {
      const rb = regularBoundsRef.current;
      if (rb) {
        place(rb.x !== -1 ? { x: rb.x, y: rb.y } : null);
        size(rb.w, rb.h);
      }
      setMaximized(false);
    } else {
      // A centered window is restored centered and with the size it had, not pinned to the place
      // it was rendered at.
      regularBoundsRef.current = {
        x: positionRef.current?.x ?? -1,
        y: positionRef.current?.y ?? -1,
        w: localWidth,
        h: localHeight,
      };
      setMaximized(true);
      place({ x: 0, y: 0 });
      size(null, null);
    }
  }, [maximized, localWidth, localHeight]);

  const style: React.CSSProperties = maximized
    ? {
        position: 'absolute', top: 0, left: 0,
        width: '100vw', maxWidth: '100vw', height: '100vh', maxHeight: '100vh', borderRadius: 0,
      }
    : {
        width: localWidth != null ? localWidth + 'px' : customFits ? customWidth + 'px' : serverWidth,
        ...(localHeight != null
          ? { height: localHeight + 'px' }
          : serverHeight != null
            ? { height: serverHeight }
            : {}),
        // The remembered height is a minimum only, so content that has grown since still fits.
        ...(customFits && localHeight == null ? { minHeight: customHeight + 'px' } : {}),
        // The height limit of the window's own style holds until the user gives it a height: a
        // window that is moved keeps the height it had, also where its content is taller. A height
        // given by resizing fits into the browser window already.
        ...(localHeight != null ? { maxHeight: '100vh' } : {}),
        ...(position ? { position: 'absolute', left: position.x + 'px', top: position.y + 'px' } : {}),
      };

  return { windowRef, style, maximized, onTitlePointerDown, toggleMaximize, onResizePointerDown, resetSize };
}
