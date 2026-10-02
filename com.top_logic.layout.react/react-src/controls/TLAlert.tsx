import { React, useTLState, useTLCommand, useI18N, TLChild, ThemeIcon, rootClassName, tooltipProps } from 'tl-react-bridge';
import type { TLCellProps, AlertStateJson, ChildControlJson } from 'tl-react-bridge';

const { useCallback } = React;

const I18N_KEYS = {
  'js.alert.dismiss': 'Dismiss',
};

/** The command dismissing the alert, see AlertStateJson. */
const DISMISS_COMMAND = 'dismiss';

/** The argument of {@link DISMISS_COMMAND} naming the content dismissed. */
const GENERATION_ARG = 'generation';

/**
 * A highlighted message standing in the content of a page: an icon, an optional heading, the
 * message, buttons offering what to do about it, and a dismiss button.
 *
 * The markup takes the alert component of the design system (`tl-alert`, colored by the
 * `tl-alert--<variant>` modifier). An error or a warning is announced as an alert, an information
 * or a success politely as a status, so that a page showing a hint does not interrupt the reading.
 */
const TLAlert: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<AlertStateJson>>();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);

  const variant = state.variant ?? 'info';
  const generation = state.generation ?? 0;
  const actions: ChildControlJson[] = state.actions ?? [];

  const handleDismiss = useCallback(() => {
    sendCommand(DISMISS_COMMAND, { [GENERATION_ARG]: generation });
  }, [sendCommand, generation]);

  if (state.hidden === true) return null;

  const urgent = variant === 'error' || variant === 'warning';
  const closable = state.closable === true;

  return (
    <div id={controlId}
      className={rootClassName(state, 'tl-alert', `tl-alert--${variant}`, closable && 'tl-alert--closable')}
      role={urgent ? 'alert' : 'status'}>
      {state.icon
        ? <ThemeIcon encoded={state.icon} className="tl-alert__icon tl-icon-md" />
        : <span className="tl-alert__icon" />}
      <div className="tl-alert__body">
        {state.title && <div className="tl-alert__title tl-type-body-strong">{state.title}</div>}
        {state.message && <div className="tl-alert__message tl-type-body">{state.message}</div>}
        {actions.length > 0 && (
          <div className="tl-alert__actions">
            {actions.map(action => <TLChild key={action.controlId} control={action} />)}
          </div>
        )}
      </div>
      {closable && (
        <button type="button"
          className="tl-button tl-button--ghost tl-button--sm tl-alert__close"
          onClick={handleDismiss}
          aria-label={i18n['js.alert.dismiss']}
          {...tooltipProps(i18n['js.alert.dismiss'])}>
          <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true">
            <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
            <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
          </svg>
        </button>
      )}
    </div>
  );
};

export default TLAlert;
