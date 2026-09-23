// TL-Script editor: a specialization of the shared CodeEditor (from the tl-code-editor bundle).
//
// IMPORTANT: React is imported from 'tl-react-bridge' (not 'react') to share the single React
// instance. The CodeMirror runtime is shared from 'tl-code-editor'; this control adds only the
// TL-Script-specific parts — the language grammar and the server-backed completion, hover
// documentation and diagnostics.
import { React, useTLState, useTLCommand, rootClassName } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { CodeEditor } from 'tl-code-editor';
import type { CodeEditorDiagnostic, CodeEditorHover } from 'tl-code-editor';
import type { CompletionContext, CompletionResult } from '@codemirror/autocomplete';
import type { EditorView } from '@codemirror/view';
import { tlscript } from '../lang/tlscript-lang';

const { useRef, useEffect, useCallback, useMemo } = React;

/** Timeout for server responses to completion and hover requests (ms). */
const RESPONSE_TIMEOUT_MS = 3000;

/** Command reporting the edited text to the server. */
const CMD_VALUE_CHANGED = 'valueChanged';

/** Command requesting diagnostics for a text. */
const CMD_VALIDATE = 'validate';

/** Command requesting completions at the cursor. */
const CMD_COMPLETE = 'complete';

/** Command requesting the documentation of a token. */
const CMD_HOVER = 'hover';

/** Argument of {@link CMD_VALUE_CHANGED}: the full text. */
const ARG_VALUE = 'value';

/** Argument of {@link CMD_VALIDATE}: the text to validate. */
const ARG_TEXT = 'text';

/** Argument of {@link CMD_COMPLETE}: the line text from its start up to the cursor. */
const ARG_LINE = 'line';

/** Argument of {@link CMD_COMPLETE}: the word being typed. */
const ARG_PREFIX = 'prefix';

/** Argument of {@link CMD_COMPLETE}: the full text up to the cursor. */
const ARG_TEXT_TO_CURSOR = 'textToCursor';

/** Argument of {@link CMD_HOVER}: the token under the mouse pointer. */
const ARG_TOKEN = 'token';

/** Argument of requests answered through a state response, echoed back in the response. */
const ARG_REQUEST_ID = 'requestId';

/** State key of the text. */
const STATE_VALUE = 'value';

/** State key of the read-only flag. */
const STATE_READ_ONLY = 'readOnly';

/** State key of the diagnostics of the text. */
const STATE_DIAGNOSTICS = 'diagnostics';

/** State key of the answer to {@link CMD_COMPLETE}. */
const STATE_COMPLETION_RESPONSE = 'completionResponse';

/** State key of the answer to {@link CMD_HOVER}. */
const STATE_HOVER_RESPONSE = 'hoverResponse';

/** Class of the hover tooltip content. */
const HOVER_CLASS = 'tlScriptHover';

/** A model reference in backticks, e.g. `tl.accounts:Person#name`. */
const MODEL_REFERENCE_PATTERN = /`[^`]*`/g;

/** An identifier or a variable reference. */
const WORD_PATTERN = /[\w$]+/g;

interface CompletionItem {
  name: string;
  value: string;
  replacement?: string;
  /** Offset within the inserted text where the cursor should land (e.g. inside "all(|)"). */
  cursorOffset?: number;
  score?: number;
  docHTML?: string;
}

/** A server answer to a request, identified by the request ID the request carried. */
interface ServerResponse {
  requestId?: string;
}

interface CompletionResponse extends ServerResponse {
  completions?: CompletionItem[];
}

interface HoverResponse extends ServerResponse {
  docHTML?: string;
}

interface PendingRequest<R> {
  requestId: string;
  resolve: (response: R | null) => void;
}

/**
 * A request/response round trip through the server: the request is sent as a command carrying a
 * request ID, the server answers by setting a state value echoing that ID.
 *
 * @param response The current value of the state key the server answers through.
 * @returns A function sending a request and resolving to its answer, or to null when a later
 *     request supersedes it or no answer arrives within {@link RESPONSE_TIMEOUT_MS}.
 */
function useServerRequest<R extends ServerResponse>(
  response: R | undefined
): (send: (requestId: string) => void) => Promise<R | null> {
  const pending = useRef<PendingRequest<R> | null>(null);

  useEffect(() => {
    const current = pending.current;
    if (!current || !response || response.requestId !== current.requestId) {
      return;
    }
    pending.current = null;
    current.resolve(response);
  }, [response]);

  return useCallback((send: (requestId: string) => void) => {
    pending.current?.resolve(null);
    const requestId = String(Date.now()) + Math.random();
    const result = new Promise<R | null>((resolve) => {
      pending.current = { requestId, resolve };
      setTimeout(() => {
        if (pending.current?.requestId === requestId) {
          pending.current = null;
          resolve(null);
        }
      }, RESPONSE_TIMEOUT_MS);
    });
    send(requestId);
    return result;
  }, []);
}

/**
 * The token at a position of a line: a backtick model reference containing the position, else the
 * identifier or variable reference containing it.
 *
 * @param text The line text.
 * @param offset The position within the line.
 * @returns The token with its range within the line, or null when there is none.
 */
function tokenAt(text: string, offset: number): { start: number; end: number; token: string } | null {
  for (const pattern of [MODEL_REFERENCE_PATTERN, WORD_PATTERN]) {
    for (const match of text.matchAll(pattern)) {
      const start = match.index!;
      const end = start + match[0].length;
      if (start <= offset && offset <= end) {
        return { start, end, token: match[0] };
      }
    }
  }
  return null;
}

/** Wraps documentation HTML delivered by the server into an element. */
function docElement(docHTML: string, className?: string): HTMLElement {
  const div = document.createElement('div');
  if (className) div.className = className;
  div.innerHTML = docHTML;
  return div;
}

/**
 * CodeMirror 6-based TL-Script editor control.
 *
 * Renders through the shared {@code CodeEditor} and communicates with
 * {@code TLScriptEditorReactControl} on the server via {@code useTLCommand()} for value changes,
 * validation, completions and hover documentation.
 */
const TLScriptEditor: React.FC<TLCellProps> = ({ controlId, state }) => {
  const currentState = useTLState();
  const sendCommand = useTLCommand();

  const value = (state[STATE_VALUE] as string) ?? '';
  const readOnly = state[STATE_READ_ONLY] === true;
  const diagnostics = (currentState[STATE_DIAGNOSTICS] as CodeEditorDiagnostic[]) ?? [];

  const languageSupport = useMemo(() => tlscript(), []);

  const requestCompletion = useServerRequest(
    currentState[STATE_COMPLETION_RESPONSE] as CompletionResponse | undefined);
  const requestHover = useServerRequest(
    currentState[STATE_HOVER_RESPONSE] as HoverResponse | undefined);

  // --- Completion support ---
  const completionSource = useCallback(
    async (context: CompletionContext): Promise<CompletionResult | null> => {
      const pos = context.pos;
      const line = context.state.doc.lineAt(pos);

      // Broad match for trigger detection (includes dots, backticks, etc.)
      const triggerMatch = context.matchBefore(/[\w$`.:]+/);
      if (!triggerMatch && !context.explicit) return null;

      // Narrow match for the word being typed — determines replacement range.
      // Include an optional leading '$' so a variable completion value ("$name")
      // returned by the server replaces the typed "$fo" cleanly (no doubled '$').
      const wordMatch = context.matchBefore(/\$?[\w]*/);
      const prefix = wordMatch?.text ?? '';
      const from = wordMatch?.from ?? pos;

      // Send text from line start to cursor (not full line) —
      // the server uses backtick parity to detect model-part mode.
      const lineUpToCursor = line.text.substring(0, pos - line.from);

      // Full text up to the cursor — the server determines the in-scope
      // variables (which may span earlier lines) for '$'-completion.
      const textToCursor = context.state.sliceDoc(0, pos);

      const response = await requestCompletion((requestId) => sendCommand(CMD_COMPLETE, {
        [ARG_LINE]: lineUpToCursor,
        [ARG_PREFIX]: prefix,
        [ARG_TEXT_TO_CURSOR]: textToCursor,
        [ARG_REQUEST_ID]: requestId,
      }));
      const items = response?.completions ?? [];
      if (items.length === 0) {
        return null;
      }
      return {
        from,
        options: items.map((c) => {
          const text = c.replacement ?? c.name;
          const cursorOffset = c.cursorOffset;
          // With a cursor offset, insert the text and place the cursor at the offset (e.g. inside
          // the parentheses of "all()") instead of letting CodeMirror drop it after the inserted text.
          const apply =
            cursorOffset == null
              ? text
              : (view: EditorView, _completion: unknown, applyFrom: number, applyTo: number) => {
                  view.dispatch({
                    changes: { from: applyFrom, to: applyTo, insert: text },
                    selection: { anchor: applyFrom + cursorOffset },
                  });
                };
          const docHTML = c.docHTML;
          return {
            label: c.name,
            apply,
            detail: c.value !== c.name ? c.value : undefined,
            info: docHTML ? () => docElement(docHTML) : undefined,
            boost: c.score ?? 0,
          };
        }),
      };
    },
    [sendCommand, requestCompletion]
  );

  // --- Hover documentation ---
  const hoverSource = useCallback(
    async (view: EditorView, pos: number): Promise<CodeEditorHover | null> => {
      const line = view.state.doc.lineAt(pos);
      const found = tokenAt(line.text, pos - line.from);
      if (!found || found.token.startsWith('$')) {
        return null;
      }
      const response = await requestHover((requestId) => sendCommand(CMD_HOVER, {
        [ARG_TOKEN]: found.token,
        [ARG_REQUEST_ID]: requestId,
      }));
      const docHTML = response?.docHTML;
      if (!docHTML) {
        return null;
      }
      return { from: line.from + found.start, to: line.from + found.end, dom: docElement(docHTML, HOVER_CLASS) };
    },
    [sendCommand, requestHover]
  );

  // --- Sync edits + request validation ---
  const handleChange = useCallback(
    (text: string) => {
      sendCommand(CMD_VALUE_CHANGED, { [ARG_VALUE]: text });
      sendCommand(CMD_VALIDATE, { [ARG_TEXT]: text });
    },
    [sendCommand]
  );

  return (
    <CodeEditor
      controlId={controlId}
      value={value}
      readOnly={readOnly}
      languageSupport={languageSupport}
      completionSource={completionSource}
      hoverSource={hoverSource}
      diagnostics={diagnostics}
      onChange={handleChange}
      className={rootClassName(state, 'tlScriptEditor')}
    />
  );
};

export default TLScriptEditor;
