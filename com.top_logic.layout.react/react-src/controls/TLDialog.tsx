import { React, useTLState, useTLCommand, TLChild, KeyboardScopeProvider, useKeyboardBinding } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback, useEffect, useRef } = React;

/**
 * Registers Escape -> close in the enclosing dialog scope. When the dialog's child is a
 * {@code TLWindow} (its own, inner scope), that inner scope handles Escape first; this binding
 * only fires for plain-content dialogs.
 */
const EscapeToClose: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  useKeyboardBinding('ESCAPE', () => {
    onClose();
    return true;
  });
  return null;
};

/**
 * Pure overlay: backdrop + child.
 *
 * State:
 * - open: boolean
 * - closeOnBackdrop: boolean  (default: true)
 * - child: ChildDescriptor
 */
const TLDialog: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const open = state.open === true;
  const closeOnBackdrop = state.closeOnBackdrop !== false;
  const child = state.child;

  const backdropRef = useRef<HTMLDivElement>(null);

  const handleClose = useCallback(() => {
    sendCommand('close');
  }, [sendCommand]);

  const handleBackdropClick = useCallback((e: React.MouseEvent) => {
    if (closeOnBackdrop && e.target === e.currentTarget) {
      handleClose();
    }
  }, [closeOnBackdrop, handleClose]);

  // Focus trap: focus the backdrop when the dialog opens.
  useEffect(() => {
    if (open && backdropRef.current) {
      backdropRef.current.focus();
    }
  }, [open]);

  if (!open) return null;

  return (
    <KeyboardScopeProvider>
      <EscapeToClose onClose={handleClose} />
      <div
        id={controlId}
        className="tlDialog__backdrop"
        onClick={handleBackdropClick}
        ref={backdropRef}
        tabIndex={-1}
      >
        <TLChild control={child} />
      </div>
    </KeyboardScopeProvider>
  );
};

export default TLDialog;
