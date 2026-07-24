import { React, useTLState, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { FormLayoutContext } from './FormLayoutContext';

const { useMemo, useRef, useState, useEffect } = React;

/** Column width threshold (px) below which labels switch from side to top. */
const LABEL_SIDE_MIN_WIDTH = 320;

/**
 * React modules of full-bleed container controls that manage their own layout and spacing. A form
 * whose sole content is one of these renders flush (see below): an editable table, for instance,
 * renders as a {@code TLPanel} wrapping a {@code TLTableView}.
 */
const FULL_BLEED_MODULES = ['TLPanel', 'TLTableView'];

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
  const noModelMessage = state.noModelMessage as string | null;

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

  // Clamp the track minimum to the container width via min(minColWidth, 100%): a bare
  // minmax(minColWidth, 1fr) gives the track a hard minColWidth floor, so in a container
  // narrower than minColWidth (e.g. a single-column form in a slim dialog) the column cannot
  // shrink and the form overflows horizontally. min(..., 100%) caps the floor at the available
  // width, so the column always fits while still wrapping multi-column layouts at minColWidth.
  const style: React.CSSProperties = {
    gridTemplateColumns: `repeat(auto-fit, minmax(min(${minColWidth}, 100%), 1fr))`,
  };

  // A form whose sole content is a full-bleed container (a "grid" form wrapping just a table/panel)
  // renders flush: the form's own page inset would otherwise draw a frame of empty space around a
  // control that already manages its own layout. Detected from the single child's React module.
  const isFullBleedOnly = children.length === 1
    && FULL_BLEED_MODULES.includes((children[0] as { module?: string } | undefined)?.module ?? '');

  const className = [
    'tlFormLayout',
    readOnly ? 'tlFormLayout--readonly' : '',
    isFullBleedOnly ? 'tlFormLayout--flush' : '',
  ].filter(Boolean).join(' ');

  if (noModelMessage) {
    return (
      <div id={controlId} className="tlFormLayout tlFormLayout--empty" ref={containerRef}>
        <p className="tlFormLayout__noModel">{noModelMessage}</p>
      </div>
    );
  }

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
