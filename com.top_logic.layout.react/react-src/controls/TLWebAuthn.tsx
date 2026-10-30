import { React, useTLState, useTLCommand, usePart, rootClassName } from 'tl-react-bridge';
import type { TLCellProps, ButtonAppearance } from 'tl-react-bridge';
import { failureOf, startCeremony, KIND_CREATE, KIND_GET } from './webauthn/webauthn-json';
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
  error?: string | null;
}

/**
 * A button running a WebAuthn ceremony (`navigator.credentials.create` for the kind `create`,
 * `navigator.credentials.get` for `get`) with the options the server supplies.
 *
 * <p>The browser is called directly in the click handler, without waiting for the server first, since
 * a browser requiring a user gesture accepts the ceremony only while the gesture lasts. The credential
 * is sent with the command `credential` (the JSON form of `PublicKeyCredential.toJSON()`), a failure
 * or cancellation with the command `failure` (the error name and message of the browser; a browser
 * without WebAuthn reports `NotSupportedError`).</p>
 *
 * <p>While a ceremony runs, while the server has no options, and while the server disables it, the
 * button is disabled; a running ceremony marks it busy. With `autoStart`, the ceremony starts once
 * when the component is mounted. Shortly before options with a `publicKey.timeout` expire, the
 * component asks with the command `refresh` for fresh ones, unless a ceremony runs. The error message of the server is announced in the status line
 * beside the button. Unmounted during a ceremony, the component aborts it.</p>
 */
const TLWebAuthn: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<WebAuthnState>();
  const sendCommand = useTLCommand();
  const Button = usePart('Button');

  const [running, setRunning] = React.useState(false);
  const runningRef = React.useRef(false);
  const abortRef = React.useRef<AbortController | null>(null);

  const kind: WebAuthnKind = state.kind === KIND_CREATE ? KIND_CREATE : KIND_GET;
  const options = state.options ?? null;
  const disabled = state.disabled === true || options === null;

  const start = React.useCallback(() => {
    if (runningRef.current || options === null) {
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
  }, [kind, options, sendCommand]);

  // When the current options arrived, as the start of their timeout.
  const receivedRef = React.useRef<{ options: string | null; at: number }>({ options, at: Date.now() });
  if (receivedRef.current.options !== options) {
    receivedRef.current = { options, at: Date.now() };
  }

  // Fresh options shortly before the current ones expire, not while a ceremony runs.
  const refreshable = !running && !disabled;
  React.useEffect(() => {
    if (!refreshable) {
      return undefined;
    }
    return watchExpiry(options, receivedRef.current.at, () => sendCommand(CMD_REFRESH));
  }, [options, refreshable, sendCommand]);

  // Automatic start: once, when mounted.
  const autoStartRef = React.useRef(state.autoStart === true && !disabled);
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

  return (
    <div id={controlId} className={rootClassName(state, 'tl-upload')}>
      <Button
        label={state.label}
        image={state.image}
        display={state.image ? 'icon-label' : 'label-only'}
        appearance={state.appearance ?? 'secondary'}
        disabled={disabled || running}
        aria-busy={running ? true : undefined}
        onClick={start}
      />
      {state.error && !running && (
        <span className="tl-file-upload__status tl-type-label" role="alert">{state.error}</span>
      )}
    </div>
  );
};

export default TLWebAuthn;
