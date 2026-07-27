import { React, useTLState, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { FormLayoutContext } from './FormLayoutContext';

const { useMemo, useRef, useState, useEffect } = React;

/** Column width threshold (px) below which labels switch from side to top. */
const LABEL_SIDE_MIN_WIDTH = 320;

/** React module of the table control. */
const TABLE_MODULE = 'TLTableView';

/** React module of the panel control (an editable table renders as a bare panel wrapping a table). */
const PANEL_MODULE = 'TLPanel';

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

  // A form whose sole content is a full-bleed, chrome-less region renders flush: the form's own
  // page inset would otherwise frame a control that already manages its own layout. That region is
  // a table rendered directly, or a panel that declares itself `bare` (the TLPanel state flag - see
  // its doc - set by a frameless table wrapper like RowSetTableControl). A panel with chrome
  // (title, toolbar, card border) is not bare and keeps the surrounding inset.
  const soleChild = children.length === 1
    ? (children[0] as { module?: string; state?: { bare?: boolean } } | undefined)
    : undefined;
  const isFullBleedOnly = !!soleChild
    && (soleChild.module === TABLE_MODULE
      || (soleChild.module === PANEL_MODULE && soleChild.state?.bare === true));

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
