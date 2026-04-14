import React from 'react';
import {
  useFloating,
  autoUpdate,
  offset,
  flip,
  shift,
  useDismiss,
  useRole,
  useInteractions,
  FloatingPortal,
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

  const { refs, floatingStyles, context } = useFloating({
    open: true,
    onOpenChange: (open) => { if (!open) onClose(); },
    placement: 'top',
    middleware: [offset(8), flip(), shift({ padding: 8 })],
    whileElementsMounted: autoUpdate,
  });

  React.useEffect(() => {
    refs.setReference(anchor);
  }, [anchor, refs]);

  const dismiss = useDismiss(context, { outsidePress: true, escapeKey: true });
  const role = useRole(context, { role: 'tooltip' });
  const { getFloatingProps } = useInteractions([dismiss, role]);

  return (
    <FloatingPortal>
      <div
        ref={refs.setFloating}
        style={floatingStyles}
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
      </div>
    </FloatingPortal>
  );
}
