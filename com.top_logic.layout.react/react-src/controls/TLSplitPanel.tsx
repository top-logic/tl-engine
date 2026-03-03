import { React, useTLState, useTLCommand, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback, useRef } = React;

interface ChildDescriptor {
  control: unknown;
  size: number;
  unit: string;
  minSize: number;
  scrolling: string;
  collapsed: boolean;
}

/**
 * A split panel that arranges children along a single axis with optional drag-resizable splitters.
 *
 * State:
 * - orientation: "horizontal" | "vertical"
 * - resizable: boolean
 * - children: ChildDescriptor[]
 */
const TLSplitPanel: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const orientation = state.orientation as string;
  const resizable = state.resizable === true;
  const children = (state.children as ChildDescriptor[]) ?? [];
  const isHorizontal = orientation === 'horizontal';
  const allCollapsed = children.length > 0 && children.every(c => c.collapsed);
  const someCollapsed = !allCollapsed && children.some(c => c.collapsed);
  const effectiveHorizontal = allCollapsed ? !isHorizontal : isHorizontal;

  const containerRef = useRef<HTMLDivElement>(null);
  const dragState = useRef<{
    splitterIndex: number;
    startPos: number;
    startSizeBefore: number;
    startSizeAfter: number;
    childBefore: ChildDescriptor;
    childAfter: ChildDescriptor;
  } | null>(null);

  // Local sizes for drag preview (pixel sizes during drag).
  const localSizes = useRef<number[] | null>(null);

  const computeChildStyle = useCallback((child: ChildDescriptor, localSize?: number): React.CSSProperties => {
    const style: React.CSSProperties = {
      overflow: child.scrolling || 'auto',
    };

    if (child.collapsed) {
      if (allCollapsed && !effectiveHorizontal) {
        // Horizontal split, all collapsed → flipped to vertical rows: share height equally.
        style.flex = '1 0 0%';
      } else {
        style.flex = '0 0 auto';
      }
    } else if (localSize !== undefined) {
      // During drag, use pixel sizes.
      style.flex = `0 0 ${localSize}px`;
    } else if (child.unit === '%' || someCollapsed) {
      // Use size as proportional flex-grow so non-collapsed children fill space freed by collapsed siblings.
      style.flex = `${child.size} 0 0%`;
    } else {
      style.flex = `0 0 ${child.size}px`;
    }

    if (child.minSize > 0 && !child.collapsed) {
      style.minWidth = isHorizontal ? child.minSize : undefined;
      style.minHeight = !isHorizontal ? child.minSize : undefined;
    }

    return style;
  }, [isHorizontal, allCollapsed, someCollapsed, effectiveHorizontal]);

  const handleMouseDown = useCallback((e: React.MouseEvent, splitterIndex: number) => {
    e.preventDefault();
    const container = containerRef.current;
    if (!container) return;

    const childBefore = children[splitterIndex];
    const childAfter = children[splitterIndex + 1];

    // Compute current pixel sizes from the DOM.
    const childElements = container.querySelectorAll(':scope > .tlSplitPanel__child');
    const sizes: number[] = [];
    childElements.forEach(el => {
      sizes.push(isHorizontal ? (el as HTMLElement).offsetWidth : (el as HTMLElement).offsetHeight);
    });
    localSizes.current = sizes;

    dragState.current = {
      splitterIndex,
      startPos: isHorizontal ? e.clientX : e.clientY,
      startSizeBefore: sizes[splitterIndex],
      startSizeAfter: sizes[splitterIndex + 1],
      childBefore,
      childAfter,
    };

    const handleMouseMove = (moveEvent: MouseEvent) => {
      const ds = dragState.current;
      if (!ds || !localSizes.current) return;

      const currentPos = isHorizontal ? moveEvent.clientX : moveEvent.clientY;
      const delta = currentPos - ds.startPos;

      const minBefore = ds.childBefore.minSize || 0;
      const minAfter = ds.childAfter.minSize || 0;

      let newBefore = ds.startSizeBefore + delta;
      let newAfter = ds.startSizeAfter - delta;

      // Enforce minimums.
      if (newBefore < minBefore) {
        newAfter += (newBefore - minBefore);
        newBefore = minBefore;
      }
      if (newAfter < minAfter) {
        newBefore += (newAfter - minAfter);
        newAfter = minAfter;
      }

      localSizes.current[ds.splitterIndex] = newBefore;
      localSizes.current[ds.splitterIndex + 1] = newAfter;

      // Force re-render by updating the container's child styles directly for performance.
      const childElements = container.querySelectorAll(':scope > .tlSplitPanel__child');
      const elBefore = childElements[ds.splitterIndex] as HTMLElement;
      const elAfter = childElements[ds.splitterIndex + 1] as HTMLElement;
      if (elBefore) elBefore.style.flex = `0 0 ${newBefore}px`;
      if (elAfter) elAfter.style.flex = `0 0 ${newAfter}px`;
    };

    const handleMouseUp = () => {
      document.removeEventListener('mousemove', handleMouseMove);
      document.removeEventListener('mouseup', handleMouseUp);
      document.body.style.cursor = '';
      document.body.style.userSelect = '';

      // Send updated sizes to the server.
      if (localSizes.current) {
        const sizes: Record<string, number> = {};
        children.forEach((child, i) => {
          const ctrl = child.control as { controlId: string };
          if (ctrl?.controlId && localSizes.current) {
            sizes[ctrl.controlId] = localSizes.current[i];
          }
        });
        sendCommand('updateSizes', { sizes });
      }
      localSizes.current = null;
      dragState.current = null;
    };

    document.addEventListener('mousemove', handleMouseMove);
    document.addEventListener('mouseup', handleMouseUp);
    document.body.style.cursor = isHorizontal ? 'col-resize' : 'row-resize';
    document.body.style.userSelect = 'none';
  }, [children, isHorizontal, sendCommand]);

  const elements: React.ReactElement[] = [];
  children.forEach((child, i) => {
    // Child element.
    elements.push(
      <div
        key={`child-${i}`}
        className={`tlSplitPanel__child${child.collapsed && effectiveHorizontal ? ' tlSplitPanel__child--collapsedHorizontal' : ''}`}
        style={computeChildStyle(child)}
      >
        <TLChild control={child.control} />
      </div>
    );

    // Splitter between children (not after collapsed or before collapsed, not after last).
    if (resizable && i < children.length - 1) {
      const nextChild = children[i + 1];
      const showSplitter = !child.collapsed && !nextChild.collapsed;
      if (showSplitter) {
        elements.push(
          <div
            key={`splitter-${i}`}
            className={`tlSplitPanel__splitter tlSplitPanel__splitter--${orientation}`}
            onMouseDown={e => handleMouseDown(e, i)}
          />
        );
      }
    }
  });

  return (
    <div
      ref={containerRef}
      id={controlId}
      className={`tlSplitPanel tlSplitPanel--${orientation}${allCollapsed ? ' tlSplitPanel--allCollapsed' : ''}`}
      style={{
        display: 'flex',
        flexDirection: effectiveHorizontal ? 'row' : 'column',
        width: '100%',
        height: '100%',
      }}
    >
      {elements}
    </div>
  );
};

export default TLSplitPanel;
