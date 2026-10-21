import { React, useTLState, TLChild, rootClassName, useFillHost, FillProvider, FormLayoutContext } from 'tl-react-bridge';
import type { TLCellProps, FormLayout } from 'tl-react-bridge';

const { useContext, useMemo, useRef, useState, useEffect } = React;

/** Column width threshold (px) below which labels switch from side to top. */
const LABEL_SIDE_MIN_WIDTH = 320;

/**
 * Top-level responsive form grid.
 *
 * A plain layout: the grid reaches up to the border of its container. A form that needs distance
 * from that border is wrapped in a TLInset.
 *
 * State:
 * - maxColumns: number
 * - labelPosition: "side" | "top" | "auto"
 * - readOnly: boolean
 * - inline: boolean
 * - children: ChildDescriptor[]
 *
 * Takes part in the fill contract as a container: a form hosting a filling child - a split panel, a
 * panel that fills - fills its own container in turn, so that the child's height resolves against
 * the height the form is offered instead of against its content. A form around content of its own
 * size stays as high as that content.
 *
 * A form inside another form - the body of a group, an entry of an edited list - is a section of the
 * outer one and takes its whole row. In a single column of the outer grid it would lay out its own
 * columns in that column alone, and leave the rest of the row empty for everything nested in it.
 * Like any form, it lays out as many columns as fit into the width it gets - it is indented by the
 * frames around it, so it may get fewer columns than the outer one. Unlike the outermost form, it
 * keeps the columns it has no field for: a single field is not stretched over the whole row.
 *
 * An inline form inside another form has no grid of its own: its children are items of the outer
 * form's grid, among the outer form's fields, and take the outer form's label position. It is
 * read-only if it says so itself or if the outer form is.
 */
const TLFormLayout: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const [fillClass, fillHost] = useFillHost();
  const outerLayout = useContext(FormLayoutContext);
  const insideForm = outerLayout.insideForm;

  const maxColumns = (state.maxColumns as number) ?? 3;
  const labelPosition = (state.labelPosition as string) ?? 'auto';
  const readOnly = state.readOnly === true;
  const inline = insideForm && state.inline === true;
  const hidden = state.hidden === true;
  const children = (state.children as unknown[]) ?? [];
  const noModelMessage = state.noModelMessage as string | null;

  const containerRef = useRef<HTMLDivElement>(null);
  const [resolvedPosition, setResolvedPosition] = useState<'side' | 'top'>(
    labelPosition === 'top' ? 'top' : 'side'
  );

  // Observe container width to resolve "auto" label position.
  useEffect(() => {
    if (inline) {
      // The label position is the one of the outer form.
      return;
    }
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
  }, [labelPosition, maxColumns, inline]);

  const ctxValue = useMemo<FormLayout>(() => (inline
    ? {
      readOnly: readOnly || outerLayout.readOnly,
      resolvedLabelPosition: outerLayout.resolvedLabelPosition,
      insideForm: true,
    }
    : {
      readOnly,
      resolvedLabelPosition: resolvedPosition,
      insideForm: true,
    }), [inline, readOnly, resolvedPosition, outerLayout.readOnly, outerLayout.resolvedLabelPosition]);

  // Compute min column width for auto-fit.
  // This ensures columns don't go below a reasonable width before wrapping.
  const minColWidth = `${Math.max(16, Math.floor(64 / maxColumns))}rem`;

  // Clamp the track minimum to the container width via min(minColWidth, 100%): a bare
  // minmax(minColWidth, 1fr) gives the track a hard minColWidth floor, so in a container
  // narrower than minColWidth (e.g. a single-column form in a slim dialog) the column cannot
  // shrink and the form overflows horizontally. min(..., 100%) caps the floor at the available
  // width, so the column always fits while still wrapping multi-column layouts at minColWidth.
  const tracks = `minmax(min(${minColWidth}, 100%), 1fr)`;
  const style: React.CSSProperties = insideForm
    ? { gridTemplateColumns: `repeat(auto-fill, ${tracks})`, gridColumn: '1 / -1' }
    : { gridTemplateColumns: `repeat(auto-fit, ${tracks})` };

  const className = [
    'tl-form-layout',
    fillClass ? 'tl-form-layout--fill' : '',
    fillClass,
  ].filter(Boolean).join(' ');

  if (inline) {
    // Without a box of its own, the children are laid out by the grid of the outer form.
    return (
      <FormLayoutContext.Provider value={ctxValue}>
        <div id={controlId} className={rootClassName(state, 'tl-form-layout--inline')}
          style={{ display: hidden ? 'none' : 'contents' }}>
          {children.map((child, i) => (
            <TLChild key={i} control={child} />
          ))}
        </div>
      </FormLayoutContext.Provider>
    );
  }

  if (noModelMessage) {
    return (
      <div id={controlId} className={rootClassName(state, 'tl-form-layout')} ref={containerRef}>
        <div className="tl-form-layout__empty tl-type-body">{noModelMessage}</div>
      </div>
    );
  }

  return (
    <FormLayoutContext.Provider value={ctxValue}>
      <FillProvider host={fillHost}>
        <div id={controlId} className={rootClassName(state, className)} style={style} ref={containerRef}>
          {children.map((child, i) => (
            <TLChild key={i} control={child} />
          ))}
        </div>
      </FillProvider>
    </FormLayoutContext.Provider>
  );
};

export default TLFormLayout;
