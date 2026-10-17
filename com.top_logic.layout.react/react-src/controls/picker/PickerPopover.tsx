import { React, useCloseOnOutsidePress, useFocusTrap, useStandaloneKeyboardScope, createPortal, usePopover, useMergeRefs, anchoredOverlayProps } from 'tl-react-bridge';

const { useRef, useEffect } = React;

export interface PickerPopoverProps {
  /** The element the popup hangs under and gets the focus back. */
  anchorRef: React.RefObject<HTMLElement | null>;

  /** The accessible name of the dialog. */
  label: string;

  className?: string;

  /** Where the focus starts: the first focusable content, or the element a function picks. */
  initialFocus: 'first' | ((root: HTMLElement) => HTMLElement | null);

  /** Closes the popup; called on Escape and on a press outside. */
  onClose: () => void;
  children: React.ReactNode;
}

/**
 * The shell of the color and the icon picker: a `tl-popover` portaled to the body, placed under its anchor by
 * usePopover, with a focus trap that gives the focus back to the anchor, closed on Escape and on a press outside.
 * Mounted only while open, and the anchor must be mounted before the picker opens: the position and the return of
 * the focus read it on mount. Its Escape scope is registered on mount, so inside a window it is the newer scope and
 * closes the picker, not the window.
 */
export const PickerPopover: React.FC<PickerPopoverProps> = ({ anchorRef, label, className, initialFocus, onClose, children }) => {
  const ref = useRef<HTMLDivElement | null>(null);
  const { setFloating, style } = usePopover({ open: true, anchor: anchorRef.current, placement: 'bottom-start' });
  const setRefs = useMergeRefs<HTMLDivElement>([ref, setFloating]);
  useCloseOnOutsidePress(true, [ref], onClose);
  useStandaloneKeyboardScope(true, { ESCAPE: () => { onClose(); return true; } });
  useFocusTrap(true, ref, initialFocus);

  // The trap returns the focus to the element that had it; if that is gone, the anchor takes it.
  useEffect(() => () => {
    const anchor = anchorRef.current;
    queueMicrotask(() => {
      if (anchor?.isConnected && (document.activeElement === document.body || document.activeElement === null)) {
        anchor.focus();
      }
    });
  }, [anchorRef]);

  return createPortal(
    <div
      ref={setRefs}
      className={'tl-popover tl-picker' + (className ? ' ' + className : '')}
      role="dialog"
      aria-label={label}
      style={style}
      {...anchoredOverlayProps}
    >
      {children}
    </div>,
    document.body,
  );
};
