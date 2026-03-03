import { React, useTLState, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { FormLayoutContext } from './FormLayoutContext';

const { useMemo, useRef, useState, useEffect } = React;

/** Column width threshold (px) below which labels switch from side to top. */
const LABEL_SIDE_MIN_WIDTH = 320;

/**
 * Top-level responsive form grid.
 *
 * State:
 * - maxColumns: number
 * - labelPosition: "side" | "top" | "auto"
 * - readOnly: boolean
 * - children: ChildDescriptor[]
 */
const TLFormLayout: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();

  const maxColumns = (state.maxColumns as number) ?? 3;
  const labelPosition = (state.labelPosition as string) ?? 'auto';
  const readOnly = state.readOnly === true;
  const children = (state.children as unknown[]) ?? [];

  const containerRef = useRef<HTMLDivElement>(null);
  const [resolvedPosition, setResolvedPosition] = useState<'side' | 'top'>(
    labelPosition === 'top' ? 'top' : 'side'
  );

  // Observe container width to resolve "auto" label position.
  useEffect(() => {
    if (labelPosition !== 'auto') {
      setResolvedPosition(labelPosition as 'side' | 'top');
      return;
    }

    const el = containerRef.current;
    if (!el) return;

    const observer = new ResizeObserver((entries) => {
      for (const entry of entries) {
        const containerWidth = entry.contentRect.width;
        // Estimate column width: container / maxColumns (approximate)
        const estimatedColWidth = containerWidth / maxColumns;
        setResolvedPosition(estimatedColWidth < LABEL_SIDE_MIN_WIDTH ? 'top' : 'side');
      }
    });
    observer.observe(el);
    return () => observer.disconnect();
  }, [labelPosition, maxColumns]);

  const ctxValue = useMemo(() => ({
    readOnly,
    resolvedLabelPosition: resolvedPosition,
  }), [readOnly, resolvedPosition]);

  // Compute min column width for auto-fit.
  // This ensures columns don't go below a reasonable width before wrapping.
  const minColWidth = `${Math.max(16, Math.floor(64 / maxColumns))}rem`;

  const style: React.CSSProperties = {
    gridTemplateColumns: `repeat(auto-fit, minmax(${minColWidth}, 1fr))`,
  };

  const className = [
    'tlFormLayout',
    readOnly ? 'tlFormLayout--readonly' : '',
  ].filter(Boolean).join(' ');

  return (
    <FormLayoutContext.Provider value={ctxValue}>
      <div id={controlId} className={className} style={style} ref={containerRef}>
        {children.map((child, i) => (
          <TLChild key={i} control={child} />
        ))}
      </div>
    </FormLayoutContext.Provider>
  );
};

export default TLFormLayout;
