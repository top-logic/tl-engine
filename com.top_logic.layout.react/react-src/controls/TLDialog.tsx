import { React, useTLState, useTLCommand, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback, useEffect, useRef } = React;

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

  // Escape key.
  useEffect(() => {
    if (!open) return;
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        handleClose();
      }
    };
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [open, handleClose]);

  // Focus trap: focus the backdrop when the dialog opens.
  useEffect(() => {
    if (open && backdropRef.current) {
      backdropRef.current.focus();
    }
  }, [open]);

  if (!open) return null;

  return (
    <div
      id={controlId}
      className="tlDialog__backdrop"
      onClick={handleBackdropClick}
      ref={backdropRef}
      tabIndex={-1}
    >
      <TLChild control={child} />
    </div>
  );
};

export default TLDialog;
