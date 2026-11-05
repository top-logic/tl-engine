/**
 * Keeping an uncontrolled editor and the value of its field on the server in step.
 *
 * <p>An editor that holds its text itself - a rich-text editor, a code editor - is not re-rendered
 * from the state on every keystroke as a controlled input is. It reports its text after a pause
 * (debounced), and it has to decide on its own what to do with a value the server sends while the
 * user is editing. {@link useUncontrolledFieldSync} makes that decision the same way for every such
 * editor.</p>
 */

import { useEffect, useMemo, useRef } from 'react';
import { registerPendingFlush, unregisterPendingFlush } from './command-channel';
import { VALUE_DEBOUNCE_MS } from './tl-react-bridge';

/**
 * Options for {@link useUncontrolledFieldSync}.
 */
export interface UncontrolledFieldSyncOptions {
  /**
   * The value of the field as the server holds it. It must follow each text given to
   * {@link UncontrolledFieldSyncOptions.send}, as the value of {@code useTLFieldValue} follows its
   * {@code setValue}: the server takes a reported text without sending it back, so a value that
   * stayed at what the server sent last would keep a later value equal to it from being noticed.
   */
  value: string;

  /** Reports a text the user entered to the server, e.g. the {@code setValue} of {@code useTLFieldValue}. */
  send: (text: string) => unknown;

  /**
   * Shows a value of the server in the editor in place of its content, keeping the caret where
   * possible. Must not report the value back as an edit.
   */
  apply: (value: string) => void;

  /** The pause after an edit before its text is reported, in milliseconds; default {@link VALUE_DEBOUNCE_MS}. */
  debounceMs?: number;
}

/**
 * What an uncontrolled editor calls to report its text, see {@link useUncontrolledFieldSync}.
 */
export interface UncontrolledFieldSync {
  /**
   * Tells that the user changed the text: it is reported after the debounce pause, before an action
   * command, or when {@link UncontrolledFieldSync.flush} is called, whichever comes first.
   *
   * @param read Reads the text of the editor; called when the text is reported, not on every edit.
   */
  edited(read: () => string): void;

  /** Reports a text still waiting for the debounce pause at once (e.g. when the editor is left). */
  flush(): Promise<void>;

  /** The text the server holds as far as the client knows: the last one reported or received. */
  current(): string;
}

/**
 * Synchronizes an uncontrolled editor with the value of its field on the server.
 *
 * <p>A value the server sends is decided on as follows, whether the editor has the focus or not:</p>
 * <ul>
 * <li>A value equal to the text reported last is the echo of the editor's own input and is ignored,
 * so that what was typed since is not lost.</li>
 * <li>Any other value is shown at once ({@link UncontrolledFieldSyncOptions.apply}). If the user typed
 * something not reported yet, the server value wins: the waiting report is dropped.</li>
 * </ul>
 *
 * <p>A text waiting for the debounce pause is registered with the command channel, so that an
 * action command (a button, a keyboard shortcut) reports it first and acts on the text as typed.
 * Unmounting the editor reports it as well.</p>
 */
export function useUncontrolledFieldSync(options: UncontrolledFieldSyncOptions): UncontrolledFieldSync {
  const { value } = options;

  const sendRef = useRef(options.send);
  sendRef.current = options.send;
  const applyRef = useRef(options.apply);
  applyRef.current = options.apply;
  const delayRef = useRef(options.debounceMs ?? VALUE_DEBOUNCE_MS);
  delayRef.current = options.debounceMs ?? VALUE_DEBOUNCE_MS;

  /** The last text reported or received. */
  const knownRef = useRef(value);

  /** Reads the text not reported yet, `null` while there is none. */
  const pendingRef = useRef<(() => string) | null>(null);

  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  // Built once: everything it reads is held in the references above. The flush function keeps its
  // identity, as the command channel requires of a registered flush.
  const sync = useMemo(() => {
    const drop = (): void => {
      if (timerRef.current !== null) {
        clearTimeout(timerRef.current);
        timerRef.current = null;
      }
      pendingRef.current = null;
      unregisterPendingFlush(flush);
    };

    function flush(): Promise<void> {
      const read = pendingRef.current;
      drop();
      if (read === null) {
        return Promise.resolve();
      }
      const text = read();
      if (text === knownRef.current) {
        return Promise.resolve();
      }
      knownRef.current = text;
      // Sent within this call, so that a command enqueued right after follows the text.
      return Promise.resolve(sendRef.current(text)).then(() => undefined);
    }

    const edited = (read: () => string): void => {
      pendingRef.current = read;
      registerPendingFlush(flush);
      if (timerRef.current !== null) {
        clearTimeout(timerRef.current);
      }
      timerRef.current = setTimeout(() => {
        timerRef.current = null;
        void flush();
      }, delayRef.current);
    };

    const current = (): string => knownRef.current;

    return { drop, flush, edited, current };
  }, []);

  useEffect(() => {
    if (value === knownRef.current) {
      return;
    }
    sync.drop();
    knownRef.current = value;
    applyRef.current(value);
  }, [value, sync]);

  useEffect(() => () => { void sync.flush(); }, [sync]);

  return useMemo(() => ({ edited: sync.edited, flush: sync.flush, current: sync.current }), [sync]);
}
