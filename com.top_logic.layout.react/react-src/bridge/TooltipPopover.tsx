import React from 'react';
import {
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
}

export interface TooltipPopoverProps {
  anchor: Element;
  data: TooltipData;
  onClose: () => void;
  onEnter: () => void;
  onLeave: () => void;
}

export function TooltipPopover(props: TooltipPopoverProps) {
  const { anchor, data, onClose, onEnter, onLeave } = props;
  const arrowRef = React.useRef<SVGSVGElement>(null);

  // Set the anchor synchronously during render so the first layout pass already has a reference.
  // Using useEffect would defer this by one commit, causing a visible 0,0 flash before the first
  // positioning update arrives.
  const { refs, floatingStyles, context, isPositioned } = useFloating({
    open: true,
    onOpenChange: (open) => { if (!open) onClose(); },
    placement: 'top',
    elements: { reference: anchor },
    middleware: [offset(10), flip(), shift({ padding: 8 }), arrow({ element: arrowRef })],
    whileElementsMounted: autoUpdate,
  });

  const dismiss = useDismiss(context, { outsidePress: true, escapeKey: true });
  const role = useRole(context, { role: 'tooltip' });
  const { getFloatingProps } = useInteractions([dismiss, role]);

  return (
    <FloatingPortal>
      <div
        ref={refs.setFloating}
        style={{ ...floatingStyles, visibility: isPositioned ? 'visible' : 'hidden' }}
        className="tl-tooltip-popover"
        onPointerEnter={onEnter}
        onPointerLeave={onLeave}
        {...getFloatingProps()}
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
