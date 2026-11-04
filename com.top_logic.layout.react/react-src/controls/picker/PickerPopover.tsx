import { React, usePopupSurface, createPortal, anchoredOverlayProps } from 'tl-react-bridge';

export interface PickerPopoverProps {
  /** The element the popup hangs under and gets the focus back. */
  anchorRef: React.RefObject<HTMLElement | null>;

  /** The accessible name of the dialog. */
  label: string;

  className?: string;

  /** Where the focus starts: the first focusable content, or the element a function picks. */
  initialFocus: 'first' | ((root: HTMLElement) => HTMLElement | null);

  /**
   * Whether the focus is confined to the popup, `true` if not given. A popup that is not modal (`false`) lets Tab and
   * a press move the focus out; Escape and a press outside close it all the same.
   */
  trapFocus?: boolean;

  /** Closes the popup; called on Escape and on a press outside. */
  onClose: () => void;
  children: React.ReactNode;
}

/**
 * A popup of content with fields - the color and the icon picker, the view of a table: a `tl-popover` portaled to
 * the body, named as a dialog, in the shell of every floating surface (usePopupSurface): placed under its anchor and
 * held to the room there, closed on Escape and on a press outside, the focus in the popup while open - confined to it
 * unless `trapFocus` is `false` - and back at the anchor afterwards. Mounted only while open, and the anchor must be
 * mounted before the picker opens: the position and the return of the focus read it on mount. Its Escape scope is
 * registered on mount, so inside a window it is the newer scope and closes the picker, not the window.
 */
export const PickerPopover: React.FC<PickerPopoverProps> = ({ anchorRef, label, className, initialFocus, trapFocus, onClose, children }) => {
  const { setRefs, style } = usePopupSurface({
    open: true,
    anchor: anchorRef.current,
    placement: 'bottom-start',
    onClose,
    initialFocus,
    trapFocus,
  });

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
