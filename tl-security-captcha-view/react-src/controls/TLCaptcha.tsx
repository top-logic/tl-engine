import { BACKGROUND_COMMAND, React, useTLState, useTLCommand, rootClassName } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import 'altcha';

/** The command carrying the solution of the challenge (`CaptchaControl.CMD_SOLVED`). */
const CMD_SOLVED = 'solved';

/** The argument of {@link CMD_SOLVED}: the payload of the widget, `null` when the solution is gone. */
const ARG_PAYLOAD = 'payload';

/** The command telling that the challenge has expired (`CaptchaControl.CMD_EXPIRED`). */
const CMD_EXPIRED = 'expired';

/** The tag of the ALTCHA web component. */
const WIDGET_TAG = 'altcha-widget';

/** The widget state of a solved challenge. */
const STATE_VERIFIED = 'verified';

/** The state of `CaptchaControl`. */
interface CaptchaState extends Record<string, unknown> {
  challenge?: string | null;
  language?: string;
  strings?: Record<string, string>;
  auto?: 'off' | 'onfocus' | 'onload';
  configuration?: string;
}

/** The value of `auto` that starts solving as soon as the widget is displayed. */
const AUTO_ON_LOAD = 'onload';

/** The part of the ALTCHA web component this component uses. */
interface AltchaWidget extends HTMLElement {
  configure?: (config: Record<string, unknown>) => void;
  reset?: () => void;
  verify?: () => Promise<unknown>;
}

/** The part of the global registry of ALTCHA (`globalThis.$altcha`) this component uses. */
interface AltchaGlobal {
  i18n: {
    get: (language: string) => Record<string, string> | undefined;
    set: (language: string, strings: Record<string, string>) => void;
  };
}

/**
 * The ALTCHA proof-of-work captcha (web component `altcha-widget` of the npm package `altcha`).
 *
 * <p>The challenge comes from the server as JSON text in the state (`challenge`), so the widget
 * fetches nothing. When the widget has solved it, the component sends the payload with the command
 * `solved`; when the widget leaves the solved state again (reset, error), it sends `solved` without
 * payload. When the challenge expires, it sends `expired`, and the server answers with a new
 * challenge. A new challenge from the server resets the widget; with `auto` set to `onload`, the
 * widget then starts solving it right away, as it does when it is displayed first.</p>
 *
 * <p>The texts of the widget come from the server (`strings`, in the language `language`) and are
 * registered with ALTCHA for that language, on top of the English texts the widget ships. The
 * configuration of the widget comes from the server as well (`configuration`,
 * `CaptchaWidget.configuration()`): it keeps the widget from reaching anything outside the
 * application and hides the ALTCHA branding.</p>
 *
 * <p>The web component is not part of this bundle: the import `altcha` resolves through the import
 * map to the script `/script/altcha.js` of `tl-security-captcha`, which the classic captcha field
 * loads as well.</p>
 */
const TLCaptcha: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<CaptchaState>();
  const sendCommand = useTLCommand();
  const widgetRef = React.useRef<AltchaWidget | null>(null);
  const sentRef = React.useRef(false);

  const challenge = state.challenge ?? null;
  const language = state.language || 'en';
  const strings = state.strings;
  const auto = state.auto ?? 'onfocus';

  // Registered before the widget is created, so that its first rendering shows them.
  React.useMemo(() => {
    const altcha = (globalThis as unknown as { $altcha?: AltchaGlobal }).$altcha;
    if (altcha && strings) {
      altcha.i18n.set(language, { ...(altcha.i18n.get('en') ?? {}), ...strings });
    }
  }, [language, strings]);

  React.useEffect(() => {
    const widget = widgetRef.current;
    if (!widget) {
      return;
    }
    const onStateChange = (event: Event) => {
      const detail = (event as CustomEvent<{ state?: string; payload?: string | null }>).detail ?? {};
      if (detail.state === STATE_VERIFIED && detail.payload) {
        sentRef.current = true;
        sendCommand(CMD_SOLVED, { [ARG_PAYLOAD]: detail.payload }, BACKGROUND_COMMAND);
      } else if (sentRef.current) {
        sentRef.current = false;
        sendCommand(CMD_SOLVED, { [ARG_PAYLOAD]: null }, BACKGROUND_COMMAND);
      }
    };
    const onExpired = () => {
      sentRef.current = false;
      sendCommand(CMD_EXPIRED, {}, BACKGROUND_COMMAND);
    };
    widget.addEventListener('statechange', onStateChange);
    widget.addEventListener('expired', onExpired);
    return () => {
      widget.removeEventListener('statechange', onStateChange);
      widget.removeEventListener('expired', onExpired);
    };
  }, [sendCommand]);

  // A new challenge of the server starts over. The widget starts solving by itself only when it is
  // mounted, so a reset with `onload` starts the solving here.
  const firstChallengeRef = React.useRef(true);
  React.useEffect(() => {
    if (firstChallengeRef.current) {
      firstChallengeRef.current = false;
      return;
    }
    const widget = widgetRef.current;
    sentRef.current = false;
    widget?.configure?.({ challenge });
    widget?.reset?.();
    if (challenge && auto === AUTO_ON_LOAD) {
      widget?.verify?.();
    }
  }, [challenge]);

  return (
    <div id={controlId} className={rootClassName(state, 'tlCaptcha')}>
      {React.createElement(WIDGET_TAG, {
        ref: widgetRef,
        challenge: challenge ?? undefined,
        language,
        auto,
        configuration: state.configuration,
      })}
    </div>
  );
};

export default TLCaptcha;
