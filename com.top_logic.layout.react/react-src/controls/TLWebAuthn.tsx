import { BACKGROUND_COMMAND, React, useTLState, useTLCommand, usePart, rootClassName, useButtonDefaults } from 'tl-react-bridge';
import type { TLCellProps, ButtonAppearance, ButtonStateJson } from 'tl-react-bridge';
import { failureOf, startCeremony, unavailability, KIND_CREATE, KIND_GET } from './webauthn/webauthn-json';
import type { WebAuthnKind } from './webauthn/webauthn-json';
import { watchExpiry } from './webauthn/options-expiry';

/** The command carrying the credential of a completed ceremony (`ReactWebAuthnControl.CMD_CREDENTIAL`). */
const CMD_CREDENTIAL = 'credential';

/** The argument of {@link CMD_CREDENTIAL}: the credential as JSON text. */
const ARG_CREDENTIAL = 'credential';

/** The command reporting a failed ceremony (`ReactWebAuthnControl.CMD_FAILURE`). */
const CMD_FAILURE = 'failure';

/** The argument of {@link CMD_FAILURE}: the name of the error. */
const ARG_ERROR_NAME = 'errorName';

/** The argument of {@link CMD_FAILURE}: the technical message of the error. */
const ARG_MESSAGE = 'message';

/**
 * The command reporting once, when the component is mounted, that the page cannot run a ceremony
 * (`ReactWebAuthnControl.CMD_UNAVAILABLE`), with the arguments of {@link CMD_FAILURE}.
 */
const CMD_UNAVAILABLE = 'unavailable';

/**
 * The command reporting once, when the component is mounted, the origin of the page
 * (`ReactWebAuthnControl.CMD_ORIGIN`).
 */
const CMD_ORIGIN = 'origin';

/** The argument of {@link CMD_ORIGIN}: `window.location.origin` (`WebAuthnOriginArguments.ORIGIN`). */
const ARG_ORIGIN = 'origin';

/** The command asking for fresh options shortly before the current ones expire (`ReactWebAuthnControl.CMD_REFRESH`). */
const CMD_REFRESH = 'refresh';

/** The error name of an aborted ceremony; an abort comes from this component and is not reported. */
const ABORT_ERROR = 'AbortError';

/** The state of `ReactWebAuthnControl`. */
interface WebAuthnState extends Record<string, unknown> {
  kind?: WebAuthnKind;
  options?: string | null;
  label?: string;
  image?: string;
  appearance?: ButtonAppearance;
  autoStart?: boolean;
  disabled?: boolean;
  tooltip?: string | null;
  hidden?: boolean;
}

/**
 * A button running a WebAuthn ceremony (`navigator.credentials.create` for the kind `create`,
 * `navigator.credentials.get` for `get`) with the options the server supplies.
 *
 * <p>The browser is called directly in the click handler, without waiting for the server first, since
 * a browser requiring a user gesture accepts the ceremony only while the gesture lasts. The credential
 * is sent with the command `credential` (the JSON form of `PublicKeyCredential.toJSON()`), a failure
 * or cancellation with the command `failure` (the error name and message of the browser).</p>
 *
 * <p>The component is the part `Button` alone, resolved like `TLButton`: the appearance of the
 * server, else the one the container suggests (`useButtonDefaults`), else `secondary`; a compact
 * toolbar shows it by its icon, a menu as an entry. It shows no error: the owner on the server tells
 * the user about a failure through the messages of the window, and a reason why the button cannot
 * be used arrives as its `tooltip`.</p>
 *
 * <p>When the component is mounted, it reports the origin of the page once with the command
 * `origin`, so the server can check it against the origins its relying party accepts before it
 * offers a ceremony.</p>
 *
 * <p>The signals of the server to the WebAuthn Signal API (unknown credential, all accepted
 * credentials) do not pass this component: they are calls of the bridge function
 * `TLReact.webAuthnSignal` (`webauthn-signals.ts`), so they reach the browser whatever the state of
 * the button.</p>
 *
 * <p>A page that cannot run a ceremony at all ({@link unavailability}: no secure context, or no
 * WebAuthn API) shows the button disabled right away and reports the cause once, when the component
 * is mounted, with the command `unavailable`, so the server can explain it in the tooltip.</p>
 *
 * <p>While a ceremony runs, while the server has no options, and while the server disables it, the
 * button is disabled; a running ceremony marks it busy. With `autoStart`, the ceremony starts once
 * when the component is mounted. Shortly before options with a `publicKey.timeout` expire, the
 * component asks with the command `refresh` for fresh ones, unless a ceremony runs. Unmounted
 * during a ceremony, the component aborts it.</p>
 *
 * <p>A hidden button (`hidden`) renders nothing, so that its place in the container collapses, also
 * where it is rendered as a child of another control rather than at a mount point of its own. While
 * hidden, it reports nothing, starts no ceremony and asks for no fresh options; the origin of the
 * page and a page that cannot run a ceremony are reported once it is shown.</p>
 */
const TLWebAuthn: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<WebAuthnState>();
  const sendCommand = useTLCommand();
  const Button = usePart('Button');
  const defaults = useButtonDefaults();

  // Known before any click and fixed for the page.
  const [unavailable] = React.useState(unavailability);

  const [running, setRunning] = React.useState(false);
  const runningRef = React.useRef(false);
  const abortRef = React.useRef<AbortController | null>(null);

  const kind: WebAuthnKind = state.kind === KIND_CREATE ? KIND_CREATE : KIND_GET;
  const options = state.options ?? null;
  const disabled = state.disabled === true || options === null || unavailable !== null;
  const hidden = state.hidden === true;

  const start = React.useCallback(() => {
    if (runningRef.current || options === null || hidden) {
      return;
    }
    const controller = new AbortController();
    abortRef.current = controller;
    runningRef.current = true;
    setRunning(true);

    // Synchronous call of the browser, inside the gesture.
    const ceremony = startCeremony(kind, options, controller.signal);

    ceremony.then(
      credential => sendCommand(CMD_CREDENTIAL, { [ARG_CREDENTIAL]: JSON.stringify(credential) }),
      error => {
        const failure = failureOf(error);
        if (controller.signal.aborted && failure.name === ABORT_ERROR) {
          return;
        }
        return sendCommand(CMD_FAILURE, { [ARG_ERROR_NAME]: failure.name, [ARG_MESSAGE]: failure.message });
      },
    ).finally(() => {
      if (abortRef.current === controller) {
        abortRef.current = null;
        runningRef.current = false;
        setRunning(false);
      }
    });
  }, [kind, options, hidden, sendCommand]);

  // The origin of the page, and a page that cannot run a ceremony: reported once, when first shown.
  const reportedRef = React.useRef(false);
  React.useEffect(() => {
    if (hidden || reportedRef.current) {
      return;
    }
    reportedRef.current = true;
    sendCommand(CMD_ORIGIN, { [ARG_ORIGIN]: window.location.origin }, BACKGROUND_COMMAND);
    if (unavailable !== null) {
      sendCommand(CMD_UNAVAILABLE, { [ARG_ERROR_NAME]: unavailable.name, [ARG_MESSAGE]: unavailable.message }, BACKGROUND_COMMAND);
    }
  }, [hidden, unavailable, sendCommand]);

  // When the current options arrived, as the start of their timeout.
  const receivedRef = React.useRef<{ options: string | null; at: number }>({ options, at: Date.now() });
  if (receivedRef.current.options !== options) {
    receivedRef.current = { options, at: Date.now() };
  }

  // Fresh options shortly before the current ones expire, not while a ceremony runs.
  const refreshable = !running && !disabled && !hidden;
  React.useEffect(() => {
    if (!refreshable) {
      return undefined;
    }
    return watchExpiry(options, receivedRef.current.at, () => sendCommand(CMD_REFRESH, {}, BACKGROUND_COMMAND));
  }, [options, refreshable, sendCommand]);

  // Automatic start: once, when mounted shown.
  const autoStartRef = React.useRef(state.autoStart === true && !disabled && !hidden);
  React.useEffect(() => {
    if (autoStartRef.current) {
      autoStartRef.current = false;
      start();
    }
  }, [start]);

  // Unmounted: abort a running ceremony.
  React.useEffect(() => () => {
    const controller = abortRef.current;
    abortRef.current = null;
    controller?.abort();
  }, []);

  // The appearance of the server, else the container's, as for TLButton; inside a menu every button is an entry.
  const appearance: ButtonAppearance = defaults.appearance === 'menu-item'
    ? 'menu-item'
    : (state.appearance ?? defaults.appearance ?? 'secondary');
  const display: ButtonStateJson.DisplayMode = !state.image
    ? 'label-only'
    : defaults.iconOnly && appearance !== 'menu-item' ? 'icon-only' : 'icon-label';

  // A hidden button renders nothing: its empty place (e.g. an item of a form grid) can then
  // collapse, so it leaves no gap between its visible neighbors. The hooks above stay in effect and
  // do nothing while hidden.
  if (hidden) {
    return null;
  }

  return (
    <Button
      id={controlId}
      label={state.label}
      image={state.image}
      display={display}
      appearance={appearance}
      disabled={disabled || running}
      tooltip={state.tooltip ?? undefined}
      aria-busy={running ? true : undefined}
      className={rootClassName(state)}
      onClick={start}
    />
  );
};

export default TLWebAuthn;
