import {
  React, useTLState, useTLCommand, TLChild, FillBarrier, createPortal, rootClassName, usePart, useButtonDefaults,
  usePopover, useMergeRefs, useCloseOnOutsidePress, pressClosedSurface, useStandaloneKeyboardScope, useFocusTrap,
  anchoredOverlayProps, ANCHORED_OVERLAY_ATTR,
} from 'tl-react-bridge';
import type { TLCellProps, PopoverStateJson } from 'tl-react-bridge';

const { useCallback, useEffect, useRef, useState } = React;

/** The command asking the server to open the popover. */
const CMD_OPEN = 'open';

/** The command asking the server to close the popover. */
const CMD_CLOSE = 'close';

/**
 * Whether the keyboard brought the focus to the element: a focus the browser would draw a focus
 * ring for. A browser that cannot tell counts every focus as such.
 */
function focusVisible(element: Element): boolean {
  try {
    return element.matches(':focus-visible');
  } catch {
    return true;
  }
}

/**
 * A non-modal surface of arbitrary content at the trigger that opens it (state: `PopoverStateJson`).
 *
 * <p>The trigger is the part `MenuTrigger`, ghost unless its container says otherwise, announcing
 * a dialog (`aria-haspopup`) and whether it is open (`aria-expanded`). It shows the trigger content
 * if there is one - presentational content, the trigger itself being the button - else the icon
 * alone named by the label, or the label. The surface is a `tl-popover` portaled to the document
 * body, placed at the trigger by `usePopover`, a dialog named by the label or else by the
 * trigger. It carries the anchored-overlay mark,
 * so that a modal surface around the trigger accepts the focus in it, and so that a surface the
 * popover was opened from stays open on a press in it.</p>
 *
 * <p>The open state is the server's: the component asks for a change with the commands
 * {@link CMD_OPEN} and {@link CMD_CLOSE}. Escape and a press outside of trigger and surface ask to
 * close. In mode `click`, a click on the trigger toggles. In mode `hover`, the pointer resting on
 * the trigger asks to open, and the pointer leaving trigger and surface asks to close unless the
 * focus is in the surface; the keyboard focus arriving at the trigger asks to open, and the focus
 * leaving trigger and surface asks to close; a click on the trigger opens.</p>
 *
 * <p>Opening by a click or the keyboard moves the focus into the surface, closing gives it back to
 * the trigger unless the user has put it elsewhere meanwhile. Opening by the pointer resting on the
 * trigger leaves the focus where it is.</p>
 */
const TLPopover: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<PopoverStateJson>>();
  const sendCommand = useTLCommand();
  const MenuTrigger = usePart('MenuTrigger');
  const defaults = useButtonDefaults();

  const open = state.open === true;
  const hoverMode = state.mode === 'hover';
  const placement = state.placement ?? 'bottom-start';
  const surfaceId = controlId + '-surface';
  const label = state.label ?? undefined;
  const icon = state.icon ?? undefined;
  const triggerContent = state.trigger ?? undefined;

  const triggerRef = useRef<HTMLButtonElement | null>(null);
  const surfaceRef = useRef<HTMLDivElement | null>(null);
  const [anchor, setAnchor] = useState<HTMLButtonElement | null>(null);
  const setTriggerRefs = useMergeRefs([triggerRef, setAnchor]);

  // Whether the popover was asked to open by the pointer resting on the trigger: such an opening
  // leaves the focus where it is.
  const pointerOpenRef = useRef(false);
  // Set from closing until the focus has settled: the focus given back to the trigger on closing
  // must not reopen a popover in mode hover.
  const settlingRef = useRef(false);
  const wasOpenRef = useRef(open);
  if (wasOpenRef.current && !open) {
    pointerOpenRef.current = false;
    settlingRef.current = true;
  }
  wasOpenRef.current = open;
  useEffect(() => {
    if (open) {
      return;
    }
    const timer = setTimeout(() => { settlingRef.current = false; }, 0);
    return () => clearTimeout(timer);
  }, [open]);

  const requestOpen = useCallback((byPointer: boolean) => {
    pointerOpenRef.current = byPointer;
    sendCommand(CMD_OPEN);
  }, [sendCommand]);

  const requestClose = useCallback(() => {
    sendCommand(CMD_CLOSE);
  }, [sendCommand]);

  /** Whether the element lies in the trigger, the surface, or a sub-surface opened from them. */
  const belongsHere = useCallback((node: Node | null): boolean => {
    if (node == null) {
      return false;
    }
    if (triggerRef.current?.contains(node) || surfaceRef.current?.contains(node)) {
      return true;
    }
    const element = node instanceof Element ? node : node.parentElement;
    return element?.closest(`[${ANCHORED_OVERLAY_ATTR}]`) != null;
  }, []);

  const openRef = useRef(open);
  openRef.current = open;

  const handleHover = useCallback((next: boolean) => {
    if (next) {
      // A popover open already keeps the way it was opened, and with it where the focus is.
      if (!openRef.current) {
        requestOpen(true);
      }
      return;
    }
    // The pointer has wandered off, but the keyboard is working in the surface.
    if (surfaceRef.current?.contains(document.activeElement)) {
      return;
    }
    requestClose();
  }, [requestOpen, requestClose]);

  const { setFloating, style } = usePopover({
    open,
    anchor,
    placement,
    hover: hoverMode ? { onOpenChange: handleHover } : undefined,
  });
  const setSurfaceRefs = useMergeRefs([surfaceRef, setFloating]);

  useStandaloneKeyboardScope(open, { ESCAPE: requestClose });
  useCloseOnOutsidePress(open, [triggerRef, surfaceRef], requestClose);
  useFocusTrap(open && !pointerOpenRef.current, surfaceRef, 'first',
    { trap: false, preventScroll: true, restore: 'from-inside' });

  const handleFocusOut = useCallback((event: FocusEvent | React.FocusEvent) => {
    const next = event.relatedTarget as Node | null;
    // A focus dropped to nowhere (a press beside every field) is left to the pointer.
    if (next != null && !belongsHere(next)) {
      requestClose();
    }
  }, [belongsHere, requestClose]);

  // The part draws the trigger and takes no focus handlers: listen at the element it draws.
  useEffect(() => {
    if (!hoverMode || anchor == null) {
      return;
    }
    const onFocusIn = (event: FocusEvent) => {
      if (open || settlingRef.current || !focusVisible(event.target as Element)) {
        return;
      }
      requestOpen(false);
    };
    const onFocusOut = (event: FocusEvent) => {
      if (open) {
        handleFocusOut(event);
      }
    };
    anchor.addEventListener('focusin', onFocusIn);
    anchor.addEventListener('focusout', onFocusOut);
    return () => {
      anchor.removeEventListener('focusin', onFocusIn);
      anchor.removeEventListener('focusout', onFocusOut);
    };
  }, [hoverMode, anchor, open, requestOpen, handleFocusOut]);

  const handleClick = useCallback((e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    // The press of this click has closed a surface: the gesture is spent.
    if (pressClosedSurface()) {
      return;
    }
    if (!open) {
      requestOpen(false);
    } else if (!hoverMode) {
      requestClose();
    }
  }, [open, hoverMode, requestOpen, requestClose]);

  const hiddenStyle = state.hidden === true ? { display: 'none' } : undefined;

  return (
    <>
      <MenuTrigger
        id={controlId}
        ref={setTriggerRefs}
        data-tl-trigger={hoverMode ? 'hover' : 'click'}
        appearance={defaults.appearance ?? 'ghost'}
        expanded={open}
        haspopup="dialog"
        label={label}
        icon={icon}
        // Trigger content is presentational and names the button unless a label does. The name is
        // left out otherwise, so that it does not override the one the part gives an icon alone.
        {...(triggerContent && label !== undefined ? { 'aria-label': label } : {})}
        aria-controls={open ? surfaceId : undefined}
        className={rootClassName(state, 'tl-popover-trigger')}
        style={hiddenStyle}
        onClick={handleClick}
      >
        {triggerContent ? <TLChild control={triggerContent} /> : undefined}
      </MenuTrigger>
      {open && createPortal(
        <div
          ref={setSurfaceRefs}
          id={surfaceId}
          className="tl-popover tl-popover--content"
          role="dialog"
          aria-label={label}
          aria-labelledby={label === undefined ? controlId : undefined}
          style={style}
          onBlur={hoverMode ? handleFocusOut : undefined}
          {...anchoredOverlayProps}
        >
          <FillBarrier>
            {state.content ? <TLChild control={state.content} /> : null}
          </FillBarrier>
        </div>,
        document.body
      )}
    </>
  );
};

export default TLPopover;
