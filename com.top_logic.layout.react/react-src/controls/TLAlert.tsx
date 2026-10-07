import { React, useTLState, useTLCommand, useI18N, TLChild, ThemeIcon, rootClassName, tooltipProps } from 'tl-react-bridge';
import type { TLCellProps, AlertStateJson, ChildControlJson } from 'tl-react-bridge';
import { buttonClassName } from './button/ButtonDefaults';

const { useCallback } = React;

const I18N_KEYS = {
  'js.alert.dismiss': 'Dismiss',
};

/** The command dismissing the alert, see AlertStateJson. */
const DISMISS_COMMAND = 'dismiss';

/** The argument of {@link DISMISS_COMMAND} naming the content dismissed. */
const GENERATION_ARG = 'generation';

/** The glyph of the dismiss button, the same as removing a value of a select. */
const DISMISS_ICON = 'css:fa-solid fa-xmark';

/**
 * A highlighted message standing in the content of a page: an icon, an optional heading, the
 * message, buttons offering what to do about it, and a dismiss button.
 *
 * The markup takes the alert component of the design system (`tl-alert`, colored by its variant as
 * `data-tl-role`; the dismiss button gets a column of its own by the design system, and so does the
 * icon, which a theme may replace by the invisible image). An error is announced as an alert, a
 * warning, an information or a success politely as a status, so that a page showing a hint does not
 * interrupt the reading.
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

  const urgent = variant === 'error';
  const closable = state.closable === true;

  return (
    <div id={controlId}
      className={rootClassName(state, 'tl-alert')}
      data-tl-role={variant}
      role={urgent ? 'alert' : 'status'}>
      <ThemeIcon encoded={state.icon} className="tl-alert__icon tl-icon-md" />
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
          className={buttonClassName({ appearance: 'ghost', small: true, icon: true, extra: 'tl-alert__close' })}
          onClick={handleDismiss}
          aria-label={i18n['js.alert.dismiss']}
          {...tooltipProps(i18n['js.alert.dismiss'])}>
          <ThemeIcon encoded={DISMISS_ICON} className="tl-button__icon tl-icon-sm" />
        </button>
      )}
    </div>
  );
};

export default TLAlert;
