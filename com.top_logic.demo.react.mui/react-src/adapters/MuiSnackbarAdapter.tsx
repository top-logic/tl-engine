import { React, useTLState, useTLCommand, useI18N, rootClassName } from 'tl-react-bridge';
import type { TLCellProps, SnackbarStateJson } from 'tl-react-bridge';
import Snackbar from '@mui/material/Snackbar';
import type { SnackbarCloseReason } from '@mui/material/Snackbar';
import Alert from '@mui/material/Alert';
import type { SxProps, Theme } from '@mui/material/styles';

const { useCallback } = React;

/** Command dismissing the message shown. */
const CMD_DISMISS = 'dismiss';

/** Argument of {@link CMD_DISMISS}: the generation of the message dismissed. */
const ARG_GENERATION = 'generation';

/** The display time of a message whose state names none, in milliseconds. */
const DEFAULT_DURATION_MS = 5000;

/** The time a hovered message stays once the pointer has left it, in milliseconds. */
const FADEOUT_AFTER_HOVER_MS = 250;

/** The reason MUI gives for a timed-out message. */
const REASON_TIMEOUT: SnackbarCloseReason = 'timeout';

/** The message: as wide as its text, at most as wide as TLSnackbar's. */
const ALERT_SX: SxProps<Theme> = { width: '100%', maxWidth: 'min(40rem, calc(100vw - 32px))' };

const I18N_KEYS = {
  'js.alert.dismiss': 'Dismiss',
};

/**
 * Renders the state of a TopLogic snackbar (module name `TLSnackbar`) with the MUI `Snackbar`
 * holding a filled `Alert`, at the bottom center of the page.
 *
 * <p>Mapping from the control state to the MUI props:</p>
 * <ul>
 * <li>visible → `open`; the message slides out when it is no longer visible;</li>
 * <li>content (HTML) → the text of the `Alert`, else message (plain text);</li>
 * <li>variant → `severity` of the `Alert` (`info`, `success`, `warning`, `error`);</li>
 * <li>duration → `autoHideDuration`; zero keeps the message until it is dismissed. Hovering the
 *     message pauses the timer, and the message leaves shortly after the pointer has left it, as
 *     with TLSnackbar;</li>
 * <li>the timeout, and the close button of the `Alert` → the command `dismiss` with the argument
 *     `generation` naming the message dismissed;</li>
 * <li>generation → the key of the `Snackbar`: the server shows the next message of a series under
 *     a generation of its own, which enters as a message of its own. Several messages are not
 *     stacked: the server queues them and shows one after the other;</li>
 * <li>hidden → nothing is rendered; the configured CSS class → className of the `Snackbar`.</li>
 * </ul>
 *
 * <p>The close button is not part of TLSnackbar; it lets the user dismiss a message that stays.
 * A click beside the message and Escape leave it standing, as with TLSnackbar.</p>
 */
const MuiSnackbarAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<SnackbarStateJson>>();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);

  const generation = state.generation ?? 0;
  const duration = state.duration ?? DEFAULT_DURATION_MS;

  const dismiss = useCallback(() => {
    sendCommand(CMD_DISMISS, { [ARG_GENERATION]: generation });
  }, [sendCommand, generation]);

  const handleClose = useCallback((_event: unknown, reason: SnackbarCloseReason) => {
    if (reason === REASON_TIMEOUT) {
      dismiss();
    }
  }, [dismiss]);

  if (state.hidden === true) {
    return null;
  }

  const content = state.content;
  return (
    <Snackbar
      key={generation}
      id={controlId}
      className={rootClassName(state)}
      open={state.visible === true}
      autoHideDuration={duration === 0 ? null : duration}
      resumeHideDuration={FADEOUT_AFTER_HOVER_MS}
      onClose={handleClose}
      anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
    >
      <Alert
        severity={state.variant ?? 'info'}
        variant="filled"
        onClose={dismiss}
        closeText={i18n['js.alert.dismiss']}
        role="status"
        sx={ALERT_SX}
      >
        {content
          ? <span dangerouslySetInnerHTML={{ __html: content }} />
          : state.message ?? ''}
      </Alert>
    </Snackbar>
  );
};

export default MuiSnackbarAdapter;
