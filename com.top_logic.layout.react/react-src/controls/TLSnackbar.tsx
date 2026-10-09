import { React, useTLState, useTLCommand, useI18N, rootClassName, tooltipProps } from 'tl-react-bridge';
import type { TLCellProps, SnackbarStateJson } from 'tl-react-bridge';
import { buttonClassName } from './button/buttonClassName';

const { useCallback, useEffect, useRef, useState } = React;

const I18N_KEYS = {
  'js.snackbar.dismiss': 'Dismiss',
};

/** The command dismissing the shown message, see SnackbarStateJson. */
const DISMISS_COMMAND = 'dismiss';

/** The argument of {@link DISMISS_COMMAND} naming the message dismissed. */
const GENERATION_ARG = 'generation';

/** Duration in ms of the fade-out animation (tlSnackbarFadeOut) before the dismiss is sent. */
const FADEOUT_MS = 200;

/** Grace period in ms before the fade-out resumes once the mouse leaves the message. */
const FADEOUT_AFTER_HOVER_MS = 250;

/**
 * Transient notification message at bottom of screen.
 *
 * State:
 * - message: string
 * - content: string (HTML)
 * - variant: "info" | "success" | "warning" | "error"
 * - duration: number  (ms, 0 = sticky)
 * - visible: boolean
 * - generation: number
 *
 * Hovering the message pins it, so a long text stays readable for as long as the user needs;
 * leaving resumes the fade-out after a short grace period. A close button at the end of the message
 * dismisses it right away; the next queued message, if any, is shown afterwards.
 */
const TLSnackbar: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<SnackbarStateJson>>();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);

  const message = state.message ?? '';
  const content = state.content ?? '';
  const variant = state.variant ?? 'info';
  const duration = state.duration ?? 5000;
  const visible = state.visible === true;
  const generation = state.generation ?? 0;

  const [exiting, setExiting] = useState(false);
  const [hovered, setHovered] = useState(false);

  // Whether this message was hovered at least once: after the mouse leaves, it disappears after a
  // short grace period rather than starting its full display duration over again.
  const wasHovered = useRef(false);

  // The generation whose dismiss is already under way, so that a click on the close button during
  // the fade-out or a concurrently expiring timer does not dismiss the same message twice.
  const dismissedGeneration = useRef<number | null>(null);

  useEffect(() => {
    wasHovered.current = false;
  }, [generation]);

  const handleDismiss = useCallback(() => {
    if (dismissedGeneration.current === generation) return;
    dismissedGeneration.current = generation;
    setExiting(true);
    setTimeout(() => {
      sendCommand(DISMISS_COMMAND, { [GENERATION_ARG]: generation });
      setExiting(false);
    }, FADEOUT_MS);
  }, [sendCommand, generation]);

  // Auto-dismiss timer, suspended while the message is hovered.
  useEffect(() => {
    if (!visible || duration === 0 || hovered) return;
    const timer = setTimeout(handleDismiss, wasHovered.current ? FADEOUT_AFTER_HOVER_MS : duration);
    return () => clearTimeout(timer);
  }, [visible, duration, hovered, handleDismiss]);

  if (!visible && !exiting) return null;

  return (
    <div id={controlId} className={rootClassName(state, `tlSnackbar tlSnackbar--${variant}${exiting ? ' tlSnackbar--exiting' : ''}`)}
      role="status" aria-live="polite"
      onMouseEnter={() => { wasHovered.current = true; setHovered(true); }}
      onMouseLeave={() => setHovered(false)}>
      {content
        ? <span className="tlSnackbar__message" dangerouslySetInnerHTML={{ __html: content }} />
        : <span className="tlSnackbar__message">{message}</span>
      }
      <button type="button"
        className={buttonClassName({ appearance: 'ghost', small: true, icon: true, extra: 'tlSnackbar__close' })}
        onClick={handleDismiss}
        aria-label={i18n['js.snackbar.dismiss']}
        {...tooltipProps(i18n['js.snackbar.dismiss'])}>
        <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true">
          <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
          <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
        </svg>
      </button>
    </div>
  );
};

export default TLSnackbar;
