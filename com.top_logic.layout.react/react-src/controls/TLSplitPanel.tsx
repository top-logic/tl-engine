import {
  React, useTLState, useTLCommand, useI18N, TLChild, useFill, FillBarrier, startPointerDrag, rootClassName, stateAttr,
  separatorMove, separatorStep, movedSize,
} from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback, useRef } = React;

/** Command sending the sizes of the open panes after a splitter was moved, each in the unit of its pane. */
const UPDATE_SIZES_COMMAND = 'updateSizes';

/** Argument of {@link UPDATE_SIZES_COMMAND}: map of the control IDs of the open panes to their sizes. */
const SIZES_ARG = 'sizes';

/** Command giving the panes their configured sizes back. */
const RESET_SIZES_COMMAND = 'resetSizes';

/** The unit of a pane whose size is given in pixels; any other pane is sized in percent. */
const UNIT_PX = 'px';

const I18N_KEYS = {
  'js.splitPanel.handle': 'Change the size of the areas',
};

interface ChildDescriptor {
  control: unknown;
  size: number;
  unit: string;
  minSize: number;
  scrolling: string;
  collapsed: boolean;
}

/**
 * The share of the area before a handle, in percent of the two areas beside it, as the weights of the server state
 * give it; `null` where the two are given in different units and no share can be read from them.
 */
function share(before: ChildDescriptor, after: ChildDescriptor): number | null {
  if (before.unit !== after.unit || before.size + after.size <= 0) {
    return null;
  }
  return Math.round((100 * before.size) / (before.size + after.size));
}

/**
 * The `flex` value a pane is rendered with.
 *
 * A pane sized in pixels keeps its size while there is room for it and shrinks in a container too small for it (down
 * to its minimum size, set as minimum width or height). The panes sized in percent share the rest, their sizes acting
 * as weights among each other.
 *
 * @param allCollapsed Whether all panes of the split panel are collapsed.
 * @param effectiveHorizontal Whether the panes are laid out side by side.
 */
function paneFlex(child: ChildDescriptor, allCollapsed: boolean, effectiveHorizontal: boolean): string {
  if (child.collapsed) {
    // Horizontal split, all collapsed -> flipped to rows: share the height equally.
    return allCollapsed && !effectiveHorizontal ? '1 0 0%' : '0 0 auto';
  }
  return child.unit === UNIT_PX ? `0 1 ${child.size}px` : `${child.size} 1 0%`;
}

/**
 * The panes with the sizes measured in pixels, each converted to the unit of its pane: a pane in pixels gets its
 * measured size, a pane in percent its share of the summed sizes of the open panes in percent, scaled to a sum of
 * 100. A collapsed pane keeps its descriptor.
 *
 * @param pixels The size of each pane in pixels, in the order of the panes.
 */
function resized(children: ChildDescriptor[], pixels: number[]): ChildDescriptor[] {
  let percentPixels = 0;
  children.forEach((child, i) => {
    if (!child.collapsed && child.unit !== UNIT_PX) {
      percentPixels += pixels[i] ?? 0;
    }
  });
  return children.map((child, i) => {
    if (child.collapsed) {
      return child;
    }
    if (child.unit === UNIT_PX) {
      return { ...child, size: Math.round(pixels[i] ?? child.size) };
    }
    if (percentPixels <= 0) {
      return child;
    }
    return { ...child, size: Math.round((10000 * (pixels[i] ?? 0)) / percentPixels) / 100 };
  });
}

/**
 * A split panel that arranges children along a single axis with resizable handles between them (tl-split).
 *
 * State:
 * - orientation: "horizontal" | "vertical"
 * - resizable: boolean
 * - children: ChildDescriptor[]
 *
 * A handle is a separator (role="separator"): dragged with the pointer, moved with the arrow keys by one step, with
 * Home and End to the smallest and the largest size of the pane before it, reset to the configured sizes by a double
 * click. The sizes of the open panes reach the server, each in the unit of its pane, when a drag ends and when a key
 * is released.
 *
 * Always fills its container: a pane sized in pixels keeps its size while there is room for it, the panes sized in
 * percent share the rest, and the handle between them has to span the available extent. Each pane is a bounded region
 * of the fill contract: a column flex box that scrolls what does not fit into it and ends the fill chain (a
 * FillBarrier), so that whatever fills - a table, a switch showing a tab bar, a stack around a table - takes the height
 * of the pane, while content of its own size keeps its size.
 */
const TLSplitPanel: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);
  const fillClass = useFill(true);

  const orientation = state.orientation as string;
  const resizable = state.resizable === true;
  const children = (state.children as ChildDescriptor[]) ?? [];
  const isHorizontal = orientation === 'horizontal';
  const allCollapsed = children.length > 0 && children.every(c => c.collapsed);
  const effectiveHorizontal = allCollapsed ? !isHorizontal : isHorizontal;

  const containerRef = useRef<HTMLDivElement>(null);

  /** The sizes moved by keys and not yet sent, in pixels per pane. */
  const keySizes = useRef<number[] | null>(null);

  const paneId = (index: number) => `${controlId}-pane-${index}`;

  const panes = useCallback((): HTMLElement[] => {
    const container = containerRef.current;
    return container ? [...container.querySelectorAll<HTMLElement>(':scope > .tl-split__pane')] : [];
  }, []);

  const measure = useCallback((): number[] =>
    panes().map(el => (isHorizontal ? el.offsetWidth : el.offsetHeight)), [panes, isHorizontal]);

  /**
   * Sends the sizes the panes have after a move, given in pixels, and gives the panes the sizing they are rendered
   * with for these sizes: the move wrote pixel sizes onto the elements, past the React rendering, which keeps them
   * wherever the sizes the server answers with render as before.
   */
  const commitSizes = useCallback((pixels: number[]) => {
    const sized = resized(children, pixels);
    const map: Record<string, number> = {};
    sized.forEach(child => {
      const ctrl = child.control as { controlId: string };
      if (!child.collapsed && ctrl?.controlId) {
        map[ctrl.controlId] = child.size;
      }
    });
    sendCommand(UPDATE_SIZES_COMMAND, { [SIZES_ARG]: map });
    const els = panes();
    sized.forEach((child, i) => {
      if (els[i]) els[i].style.flex = paneFlex(child, allCollapsed, effectiveHorizontal);
    });
  }, [children, sendCommand, panes, allCollapsed, effectiveHorizontal]);

  const computeChildStyle = (child: ChildDescriptor): React.CSSProperties => {
    const style: React.CSSProperties = {
      display: 'flex',
      flexDirection: 'column',
      overflow: child.scrolling || 'auto',
      flex: paneFlex(child, allCollapsed, effectiveHorizontal),
    };
    if (child.minSize > 0 && !child.collapsed) {
      style.minWidth = isHorizontal ? child.minSize : undefined;
      style.minHeight = !isHorizontal ? child.minSize : undefined;
    }
    return style;
  };

  const handlePointerDown = useCallback((e: React.PointerEvent, index: number) => {
    e.preventDefault();
    const els = panes();
    const sizes = measure();
    const before = els[index];
    const after = els[index + 1];
    // The sizing the panes are rendered with, to fall back on when the gesture is cancelled: the drag writes pixel
    // sizes onto the elements, past the React rendering.
    const flexBefore = before?.style.flex ?? '';
    const flexAfter = after?.style.flex ?? '';
    const startPos = isHorizontal ? e.clientX : e.clientY;
    const total = sizes[index] + sizes[index + 1];
    // The size of the pane before the handle at the press: each move places the handle at the pointer's distance from
    // the press position, measured from this size.
    const startBefore = sizes[index];
    const minBefore = children[index].minSize || 0;
    const minAfter = children[index + 1].minSize || 0;

    startPointerDrag(e, {
      cursor: isHorizontal ? 'col-resize' : 'row-resize',
      onMove: (moveEvent) => {
        const delta = (isHorizontal ? moveEvent.clientX : moveEvent.clientY) - startPos;
        const size = Math.min(total - minAfter, Math.max(minBefore, startBefore + delta));
        sizes[index] = size;
        sizes[index + 1] = total - size;
        if (before) before.style.flex = `0 0 ${size}px`;
        if (after) after.style.flex = `0 0 ${total - size}px`;
      },
      // A press that stayed where it was leaves the panes alone.
      onEnd: (_event, dragged) => {
        if (dragged) {
          commitSizes(sizes);
        }
      },
      onCancel: () => {
        if (before) before.style.flex = flexBefore;
        if (after) after.style.flex = flexAfter;
      },
    });
  }, [panes, measure, isHorizontal, children, commitSizes]);

  const handleKeyDown = useCallback((e: React.KeyboardEvent<HTMLElement>, index: number) => {
    const handle = e.currentTarget;
    const move = separatorMove(e.key, isHorizontal ? 'vertical' : 'horizontal', handle);
    if (!move) {
      return;
    }
    e.preventDefault();
    e.stopPropagation();
    const sizes = keySizes.current ?? measure();
    const total = sizes[index] + sizes[index + 1];
    const minBefore = children[index].minSize || 0;
    const maxBefore = Math.max(minBefore, total - (children[index + 1].minSize || 0));
    const size = movedSize(sizes[index], move, minBefore, maxBefore, separatorStep(handle));
    sizes[index] = size;
    sizes[index + 1] = total - size;
    keySizes.current = sizes;
    const els = panes();
    if (els[index]) els[index].style.flex = `0 0 ${size}px`;
    if (els[index + 1]) els[index + 1].style.flex = `0 0 ${total - size}px`;
    if (total > 0) {
      handle.setAttribute('aria-valuenow', String(Math.round((100 * size) / total)));
    }
  }, [isHorizontal, measure, panes, children]);

  /** Sends the sizes moved by keys; a lost key-up (focus left the handle) must not leave them behind. */
  const flushKeySizes = useCallback(() => {
    const sizes = keySizes.current;
    keySizes.current = null;
    if (sizes) {
      commitSizes(sizes);
    }
  }, [commitSizes]);

  const handleReset = useCallback(() => {
    sendCommand(RESET_SIZES_COMMAND);
  }, [sendCommand]);

  const elements: React.ReactElement[] = [];
  children.forEach((child, i) => {
    elements.push(
      <div
        key={`child-${i}`}
        id={paneId(i)}
        className="tl-split__pane"
        data-tl-state={stateAttr(child.collapsed && effectiveHorizontal && 'collapsed')}
        style={computeChildStyle(child)}
      >
        <FillBarrier>
          <TLChild control={child.control} />
        </FillBarrier>
      </div>
    );

    // A handle between two panes that are both open.
    if (resizable && i < children.length - 1 && !child.collapsed && !children[i + 1].collapsed) {
      elements.push(
        <div
          key={`handle-${i}`}
          className="tl-split__handle"
          role="separator"
          tabIndex={0}
          aria-orientation={isHorizontal ? 'vertical' : 'horizontal'}
          aria-controls={paneId(i)}
          aria-valuenow={share(child, children[i + 1]) ?? 50}
          aria-valuemin={0}
          aria-valuemax={100}
          aria-label={i18n['js.splitPanel.handle']}
          onPointerDown={e => handlePointerDown(e, i)}
          onKeyDown={e => handleKeyDown(e, i)}
          onKeyUp={flushKeySizes}
          onBlur={flushKeySizes}
          onDoubleClick={handleReset}
        />
      );
    }
  });

  return (
    <div
      ref={containerRef}
      id={controlId}
      className={rootClassName(state, 'tl-split', `tl-split--${orientation}`, fillClass)}
      data-tl-state={stateAttr(allCollapsed && 'all-collapsed')}
    >
      {elements}
    </div>
  );
};

export default TLSplitPanel;
