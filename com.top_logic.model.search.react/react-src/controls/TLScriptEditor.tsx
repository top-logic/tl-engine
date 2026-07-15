// TL-Script editor: a specialization of the shared CodeEditor (from the tl-code-editor bundle).
//
// IMPORTANT: React is imported from 'tl-react-bridge' (not 'react') to share the single React
// instance. The CodeMirror runtime is shared from 'tl-code-editor'; this control adds only the
// TL-Script-specific parts — the language grammar and the server-backed completion/diagnostics.
import { React, useTLState, useTLCommand } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { CodeEditor } from 'tl-code-editor';
import type { CodeEditorDiagnostic } from 'tl-code-editor';
import type { CompletionContext, CompletionResult } from '@codemirror/autocomplete';
import type { EditorView } from '@codemirror/view';
import { tlscript } from '../lang/tlscript-lang';

const { useRef, useEffect, useCallback, useMemo } = React;

/** Timeout for server completion responses (ms). */
const COMPLETION_TIMEOUT_MS = 3000;

interface CompletionItem {
  name: string;
  value: string;
  replacement?: string;
  /** Offset within the inserted text where the cursor should land (e.g. inside "all(|)"). */
  cursorOffset?: number;
  score?: number;
  docHTML?: string;
}

interface CompletionResponse {
  requestId?: string;
  completions?: CompletionItem[];
}

interface PendingCompletion {
  requestId: string;
  from: number;
  resolve: (result: CompletionResult | null) => void;
}

/**
 * CodeMirror 6-based TL-Script editor control.
 *
 * Renders through the shared {@code CodeEditor} and communicates with
 * {@code TLScriptEditorReactControl} on the server via {@code useTLCommand()} for value changes,
 * validation and completions.
 */
const TLScriptEditor: React.FC<TLCellProps> = ({ controlId, state }) => {
  const currentState = useTLState();
  const sendCommand = useTLCommand();
  const pendingCompletion = useRef<PendingCompletion | null>(null);

  const value = (state.value as string) ?? '';
  const readOnly = state.readOnly === true;
  const diagnostics = (currentState.diagnostics as CodeEditorDiagnostic[]) ?? [];

  const languageSupport = useMemo(() => tlscript(), []);

  // --- Completion support (promise-over-SSE) ---
  const completionSource = useCallback(
    (context: CompletionContext): Promise<CompletionResult | null> => {
      const pos = context.pos;
      const line = context.state.doc.lineAt(pos);

      // Broad match for trigger detection (includes dots, backticks, etc.)
      const triggerMatch = context.matchBefore(/[\w$`.:]+/);
      if (!triggerMatch && !context.explicit) return Promise.resolve(null);

      // Narrow match for the word being typed — determines replacement range.
      const wordMatch = context.matchBefore(/[\w]+/);
      const prefix = wordMatch?.text ?? '';
      const from = wordMatch?.from ?? pos;

      // Send text from line start to cursor (not full line) —
      // the server uses backtick parity to detect model-part mode.
      const lineUpToCursor = line.text.substring(0, pos - line.from);

      const requestId = String(Date.now()) + Math.random();
      sendCommand('complete', { line: lineUpToCursor, prefix, requestId });

      return new Promise((resolve) => {
        pendingCompletion.current = { requestId, resolve, from };
        setTimeout(() => {
          if (pendingCompletion.current?.requestId === requestId) {
            pendingCompletion.current = null;
            resolve(null);
          }
        }, COMPLETION_TIMEOUT_MS);
      });
    },
    [sendCommand]
  );

  // --- Completion response handling (from server SSE state patches) ---
  const completionResponse = currentState.completionResponse as CompletionResponse | undefined;

  useEffect(() => {
    const pending = pendingCompletion.current;
    if (!pending || !completionResponse || completionResponse.requestId !== pending.requestId) {
      return;
    }
    const items = completionResponse.completions ?? [];
    pendingCompletion.current = null;

    if (items.length === 0) {
      pending.resolve(null);
      return;
    }
    pending.resolve({
      from: pending.from,
      options: items.map((c) => {
        const text = c.replacement ?? c.name;
        const cursorOffset = c.cursorOffset;
        // With a cursor offset, insert the text and place the cursor at the offset (e.g. inside the
        // parentheses of "all()") instead of letting CodeMirror drop it after the inserted text.
        const apply =
          cursorOffset == null
            ? text
            : (view: EditorView, _completion: unknown, from: number, to: number) => {
                view.dispatch({
                  changes: { from, to, insert: text },
                  selection: { anchor: from + cursorOffset },
                });
              };
        return {
          label: c.name,
          apply,
          detail: c.value !== c.name ? c.value : undefined,
          info: c.docHTML
            ? () => {
                const div = document.createElement('div');
                div.innerHTML = c.docHTML!;
                return div;
              }
            : undefined,
          boost: c.score ?? 0,
        };
      }),
    });
  }, [completionResponse]);

  // --- Sync edits + request validation ---
  const handleChange = useCallback(
    (text: string) => {
      sendCommand('valueChanged', { value: text });
      sendCommand('validate', { text });
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
      diagnostics={diagnostics}
      onChange={handleChange}
      className="tlScriptEditor"
    />
  );
};

export default TLScriptEditor;
