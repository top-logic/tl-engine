import React from 'react';
import {
  type Placement,
  type ReferenceType,
  useFloating,
  autoUpdate,
  offset,
  flip,
  shift,
  arrow,
  useDismiss,
  useRole,
  useInteractions,
  FloatingPortal,
  FloatingArrow,
} from '@floating-ui/react';

export interface TooltipData {
  /** When set, rendered as HTML. Must be sanitized by the emitter. */
  html?: string;
  /** When set, rendered as plain text. */
  text?: string;
  /** Optional caption above body. */
  caption?: string;
  /**
   * When true, the popover stays open while the pointer hovers over it — enabling
   * text selection, copy, and link navigation. Use for help pages with code snippets.
   * Default: passive (pointer-events disabled on the popover).
   */
  interactive?: boolean;
}

export interface TooltipPopoverProps {
  anchor: Element;
  data: TooltipData;
  /** Element used as the portal target for the floating popover (e.g. the per-window tooltip host). */
  portalRoot: HTMLElement;
  onClose: () => void;
  onEnter: () => void;
  onLeave: () => void;
}

/**
 * The list of sibling entries the given anchor is an entry of - a menu, or a single-column list of
 * options - or null for an anchor standing on its own.
 *
 * A list of options laid out as a grid (an icon picker) does not count: there the neighbours of an
 * entry stand beside it, and its tooltip stays at the entry.
 */
function entryList(anchor: Element): Element | null {
  const list = anchor.closest('[role="menu"], [role="listbox"]');
  if (!list || list === anchor) return null;
  if (list.getAttribute('role') === 'menu') return list;
  const entry = anchor.getBoundingClientRect();
  const all = list.getBoundingClientRect();
  return entry.width >= all.width * 0.6 ? list : null;
}

/**
 * Where a tooltip is placed, and what against.
 *
 * A tooltip of an entry of a list opens beside the list - right, or left where there is no room -
 * at the height of the entry, so that it never covers the entries above and below, which the user
 * is about to choose between. Every other tooltip opens above its anchor.
 */
function anchoring(anchor: Element): { reference: ReferenceType; placement: Placement; fallback: Placement[] } {
  const list = entryList(anchor);
  if (!list) {
    return { reference: anchor, placement: 'top', fallback: ['bottom'] };
  }
  return {
    reference: {
      // The horizontal extent of the list and the vertical one of the entry.
      getBoundingClientRect: () => {
        const outer = list.getBoundingClientRect();
        const entry = anchor.getBoundingClientRect();
        return new DOMRect(outer.left, entry.top, outer.width, entry.height);
      },
      contextElement: anchor,
    },
    placement: 'right',
    fallback: ['left'],
  };
}

export function TooltipPopover(props: TooltipPopoverProps) {
  const { anchor, data, portalRoot, onClose, onEnter, onLeave } = props;
  const arrowRef = React.useRef<SVGSVGElement>(null);
  const { reference, placement, fallback } = React.useMemo(() => anchoring(anchor), [anchor]);

  // Set the anchor synchronously during render so the first layout pass already has a reference.
  // Using useEffect would defer this by one commit, causing a visible 0,0 flash before the first
  // positioning update arrives.
  const { refs, floatingStyles, context } = useFloating({
    open: true,
    onOpenChange: (open) => { if (!open) onClose(); },
    placement,
    elements: { reference: anchor },
    middleware: [
      offset(10),
      flip({ fallbackPlacements: fallback }),
      shift({ padding: 8 }),
      arrow({ element: arrowRef }),
    ],
    whileElementsMounted: autoUpdate,
  });

  // A tooltip of a list entry is positioned against the list's extent rather than the entry's;
  // a layout effect sets that before the first paint, so the tooltip never shows at the entry first.
  React.useLayoutEffect(() => {
    if (reference !== anchor) {
      refs.setPositionReference(reference);
    }
  }, [refs, reference, anchor]);

  const dismiss = useDismiss(context, { outsidePress: true, escapeKey: true });
  const role = useRole(context, { role: 'tooltip' });
  const { getFloatingProps } = useInteractions([dismiss, role]);

  return (
    <FloatingPortal root={portalRoot}>
      <div
        ref={refs.setFloating}
        style={data.interactive ? floatingStyles : { ...floatingStyles, pointerEvents: 'none' }}
        className={'tl-tooltip-popover' + (data.interactive ? ' tl-tooltip-popover--interactive' : '')}
        {...getFloatingProps()}
        onPointerEnter={onEnter}
        onPointerLeave={onLeave}
      >
        {data.caption ? <div className="tl-tooltip-caption">{data.caption}</div> : null}
        {data.html != null ? (
          <div
            className="tl-tooltip-body"
            dangerouslySetInnerHTML={{ __html: data.html }}
          />
        ) : (
          <div className="tl-tooltip-body">{data.text ?? ''}</div>
        )}
        <FloatingArrow
          ref={arrowRef}
          context={context}
          className="tl-tooltip-arrow"
          width={12}
          height={6}
        />
      </div>
    </FloatingPortal>
  );
}
