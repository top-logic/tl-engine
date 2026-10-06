import {
  React, useTLState, useTLCommand, TLChild, KeyboardScopeProvider, useKeyboardBinding, FillBarrier, rootClassName,
} from 'tl-react-bridge';
import type { TLCellProps, DialogStateJson } from 'tl-react-bridge';
import Backdrop from '@mui/material/Backdrop';
import type { SxProps, Theme } from '@mui/material/styles';

const { useCallback } = React;

/** Command dismissing the dialog. */
const CMD_CLOSE = 'close';

/** The gesture dismissing a dialog that can be dismissed. */
const GESTURE_ESCAPE = 'ESCAPE';

/**
 * The backdrop: on the overlay layer of TopLogic (MUI places a backdrop below its modal, at a
 * negative z-index), and without a transition of its own: the dialog manager marks the backdrop
 * of an arriving and a leaving dialog for the transition an application stylesheet defines.
 */
const BACKDROP_SX: SxProps<Theme> = { zIndex: 'var(--tl-layer-overlay)', outline: 'none' };

/**
 * Binds Escape to dismissing the dialog in the dialog's keyboard scope. A window inside the dialog
 * has a scope of its own, which handles Escape first.
 */
const EscapeToClose: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  useKeyboardBinding(GESTURE_ESCAPE, () => {
    onClose();
    return true;
  });
  return null;
};

/**
 * Renders the state of a TopLogic dialog (module name `TLDialog`) with the MUI `Backdrop`: the
 * dimmed layer over the page that centers the content of the dialog, usually a window.
 *
 * <p>The dialog keeps the ownership TopLogic gives it: the dialog manager stacks the open dialogs
 * (a dialog opened later lies above), the keyboard scope of the bridge binds Escape, and the window
 * inside carries the focus trap. MUI's `Modal` is not used, as its focus trap, Escape handling,
 * scroll lock and stacking would compete with these.</p>
 *
 * <p>Mapping from the control state to the MUI props:</p>
 * <ul>
 * <li>open → the backdrop is rendered; a dialog that is not open renders nothing;</li>
 * <li>child → the content of the backdrop, rendered through {@link TLChild} behind a
 *     {@link FillBarrier} (a dialog is laid out apart from the page);</li>
 * <li>closable, closeOnBackdrop → a click on the backdrop beside the content sends `close` when
 *     both allow it; Escape sends `close` when the dialog is closable;</li>
 * <li>hidden → nothing is rendered; the configured CSS class → className of the backdrop, which
 *     carries the control ID (the dialog manager finds it by that ID).</li>
 * </ul>
 */
const MuiDialogAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<DialogStateJson>>();
  const sendCommand = useTLCommand();

  const closeOnBackdrop = state.closeOnBackdrop !== false;
  const closable = state.closable !== false;

  const handleClose = useCallback(() => {
    sendCommand(CMD_CLOSE);
  }, [sendCommand]);

  const handleBackdropClick = useCallback((event: React.MouseEvent) => {
    if (closable && closeOnBackdrop && event.target === event.currentTarget) {
      handleClose();
    }
  }, [closable, closeOnBackdrop, handleClose]);

  if (state.open !== true || state.hidden === true) {
    return null;
  }

  return (
    <KeyboardScopeProvider>
      {closable && <EscapeToClose onClose={handleClose} />}
      <Backdrop
        open
        transitionDuration={0}
        id={controlId}
        className={rootClassName(state)}
        onClick={handleBackdropClick}
        tabIndex={-1}
        sx={BACKDROP_SX}
      >
        <FillBarrier>
          {!!state.child && <TLChild control={state.child} />}
        </FillBarrier>
      </Backdrop>
    </KeyboardScopeProvider>
  );
};

export default MuiDialogAdapter;
