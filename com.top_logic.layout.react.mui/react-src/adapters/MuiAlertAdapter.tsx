import { React, useTLState, useTLCommand, useI18N, TLChild, ThemeIcon, rootClassName } from 'tl-react-bridge';
import type { TLCellProps, AlertStateJson, ChildControlJson } from 'tl-react-bridge';
import Alert from '@mui/material/Alert';
import AlertTitle from '@mui/material/AlertTitle';

const { useCallback } = React;

/** Command dismissing the alert. */
const CMD_DISMISS = 'dismiss';

/** Argument of {@link CMD_DISMISS}: the generation of the content dismissed. */
const ARG_GENERATION = 'generation';

/** Size class of the design system for the icon of a message. */
const ICON_CLASS = 'tl-icon-md';

/** The buttons below the message, in a row that wraps. */
const ACTIONS_STYLE: React.CSSProperties = { display: 'flex', flexWrap: 'wrap', gap: 8, marginTop: 8 };

const I18N_KEYS = {
  'js.alert.dismiss': 'Dismiss',
};

/**
 * Renders the state of a TopLogic alert (module name `TLAlert`) with the MUI `Alert` and its
 * `AlertTitle`.
 *
 * <p>Mapping from the control state to the MUI props:</p>
 * <ul>
 * <li>variant → `severity` (absent: `info`);</li>
 * <li>icon → `icon`, rendered by the bridge's {@link ThemeIcon}; without one, the icon MUI shows
 *     for the severity;</li>
 * <li>title → `AlertTitle`; message → the text of the alert;</li>
 * <li>actions → the buttons below the message, each rendered through {@link TLChild};</li>
 * <li>closable → `onClose`: the close button sends the command `dismiss` with the argument
 *     `generation` naming the content dismissed; its label is TLAlert's;</li>
 * <li>an error or a warning is announced as an alert, an information or a success politely as a
 *     status (`role`), as with TLAlert;</li>
 * <li>hidden → nothing is rendered; the configured CSS class → className of the `Alert`.</li>
 * </ul>
 */
const MuiAlertAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<AlertStateJson>>();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);

  const generation = state.generation ?? 0;

  const dismiss = useCallback(() => {
    sendCommand(CMD_DISMISS, { [ARG_GENERATION]: generation });
  }, [sendCommand, generation]);

  if (state.hidden === true) {
    return null;
  }

  const severity = state.variant ?? 'info';
  const urgent = severity === 'error' || severity === 'warning';
  const actions: ChildControlJson[] = state.actions ?? [];

  return (
    <Alert
      id={controlId}
      className={rootClassName(state)}
      severity={severity}
      role={urgent ? 'alert' : 'status'}
      icon={state.icon ? <ThemeIcon encoded={state.icon} className={ICON_CLASS} /> : undefined}
      onClose={state.closable === true ? dismiss : undefined}
      closeText={i18n['js.alert.dismiss']}
    >
      {state.title && <AlertTitle>{state.title}</AlertTitle>}
      {state.message}
      {actions.length > 0 && (
        <div style={ACTIONS_STYLE}>
          {actions.map(action => <TLChild key={action.controlId} control={action} />)}
        </div>
      )}
    </Alert>
  );
};

export default MuiAlertAdapter;
