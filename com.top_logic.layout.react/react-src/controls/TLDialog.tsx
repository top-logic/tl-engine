import {
  React, useTLState, useTLCommand, TLChild, KeyboardScopeProvider, EscapeToClose, FillBarrier, rootClassName,
} from 'tl-react-bridge';
import type { TLCellProps, DialogStateJson } from 'tl-react-bridge';

const { useCallback } = React;

/**
 * The scrim of a dialog (`tl-dialog` of the design system package) with the dialog's content, in
 * practice a {@code TLWindow} rendering the surface.
 *
 * State:
 * - open: boolean
 * - closeOnBackdrop: boolean  (default: true)
 * - closable: boolean  (default: true)
 * - child: ChildDescriptor
 *
 * Escape is bound in this dialog's scope; a window inside binds it in its own, inner scope first, so
 * this binding only fires for plain content.
 */
const TLDialog: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<DialogStateJson>>();
  const sendCommand = useTLCommand();

  const open = state.open === true;
  const closeOnBackdrop = state.closeOnBackdrop !== false;
  // A dialog held open by ongoing work: neither Escape nor a click on the scrim dismisses it.
  const closable = state.closable !== false;
  const child = state.child;

  const handleClose = useCallback(() => {
    sendCommand('close');
  }, [sendCommand]);

  const handleScrimClick = useCallback((e: React.MouseEvent) => {
    if (closable && closeOnBackdrop && e.target === e.currentTarget) {
      handleClose();
    }
  }, [closable, closeOnBackdrop, handleClose]);

  if (!open) return null;

  return (
    <KeyboardScopeProvider>
      {closable && <EscapeToClose onClose={handleClose} />}
      <div id={controlId} className={rootClassName(state, 'tl-dialog')} onClick={handleScrimClick} tabIndex={-1}>
        <FillBarrier>
          <TLChild control={child} />
        </FillBarrier>
      </div>
    </KeyboardScopeProvider>
  );
};

export default TLDialog;
