import {
  React, useTLState, useTLCommand, TLChild, KeyboardScopeProvider, useKeyboardBinding, FillBarrier,
  rootClassName,
} from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback, useRef } = React;

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
 * - closable: boolean  (default: true)
 * - child: ChildDescriptor
 */
const TLDialog: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const open = state.open === true;
  const closeOnBackdrop = state.closeOnBackdrop !== false;
  // A dialog held open by ongoing work: neither Escape nor a backdrop click dismisses it.
  const closable = state.closable !== false;
  const child = state.child;

  const backdropRef = useRef<HTMLDivElement>(null);

  const handleClose = useCallback(() => {
    sendCommand('close');
  }, [sendCommand]);

  const handleBackdropClick = useCallback((e: React.MouseEvent) => {
    if (closable && closeOnBackdrop && e.target === e.currentTarget) {
      handleClose();
    }
  }, [closable, closeOnBackdrop, handleClose]);

  if (!open) return null;

  return (
    <KeyboardScopeProvider>
      {closable && <EscapeToClose onClose={handleClose} />}
      <div
        id={controlId}
        className={rootClassName(state, 'tlDialog__backdrop')}
        onClick={handleBackdropClick}
        ref={backdropRef}
        tabIndex={-1}
      >
        <FillBarrier>
          <TLChild control={child} />
        </FillBarrier>
      </div>
    </KeyboardScopeProvider>
  );
};

export default TLDialog;
