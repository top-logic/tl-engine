import { React, useTLState, useTLCommand } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { EditorView, keymap, lineNumbers, highlightActiveLine, highlightActiveLineGutter } from '@codemirror/view';
import { EditorState, Compartment } from '@codemirror/state';
import { defaultKeymap, history, historyKeymap } from '@codemirror/commands';
import { autocompletion, closeBrackets } from '@codemirror/autocomplete';
import type { CompletionContext, CompletionResult } from '@codemirror/autocomplete';
import { setDiagnostics } from '@codemirror/lint';
import type { Diagnostic } from '@codemirror/lint';
import { syntaxHighlighting, defaultHighlightStyle, bracketMatching } from '@codemirror/language';
import { tlscript } from '../lang/tlscript-lang';

const { useRef, useEffect, useCallback } = React;

/** Debounce delay for value sync and validation (ms). */
const DEBOUNCE_MS = 300;

/** Timeout for server completion responses (ms). */
const COMPLETION_TIMEOUT_MS = 3000;

interface ServerDiagnostic {
  line: number;
  col: number;
  endLine?: number;
  endCol?: number;
  severity: 'error' | 'warning' | 'info';
  message: string;
}

interface CompletionItem {
  name: string;
  value: string;
  replacement?: string;
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
 * Communicates with {@code TLScriptEditorReactControl} on the server via
 * {@code useTLCommand()} for value changes, validation, completions, and hover.
 */
const TLScriptEditor: React.FC<TLCellProps> = ({ controlId, state }) => {
  const currentState = useTLState();
  const sendCommand = useTLCommand();
  const editorRef = useRef<HTMLDivElement>(null);
  const viewRef = useRef<EditorView | null>(null);
  const editableComp = useRef(new Compartment());
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const pendingCompletion = useRef<PendingCompletion | null>(null);

  const initialValue = (state.value as string) ?? '';
  const readOnly = state.readOnly === true;

  // --- Completion support (promise-over-SSE) ---

  const completionSource = useCallback(
    (context: CompletionContext): Promise<CompletionResult | null> => {
      const pos = context.pos;
      const line = context.state.doc.lineAt(pos);

      // Broad match for trigger detection (includes dots, backticks, etc.)
      const triggerMatch = context.matchBefore(/[\w$`.:]+/);
      if (!triggerMatch && !context.explicit) return Promise.resolve(null);

      // Narrow match for the word being typed — determines replacement range
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

  // --- Create editor on mount ---

  useEffect(() => {
    if (!editorRef.current) return;

    const view = new EditorView({
      state: EditorState.create({
        doc: initialValue,
        extensions: [
          tlscript(),
          syntaxHighlighting(defaultHighlightStyle),
          lineNumbers(),
          highlightActiveLine(),
          highlightActiveLineGutter(),
          bracketMatching(),
          closeBrackets(),
          history(),
          keymap.of([...defaultKeymap, ...historyKeymap]),
          editableComp.current.of(EditorView.editable.of(!readOnly)),
          autocompletion({ override: [completionSource] }),
          EditorView.updateListener.of((update) => {
            if (update.docChanged) {
              if (timerRef.current) clearTimeout(timerRef.current);
              timerRef.current = setTimeout(() => {
                const text = update.state.doc.toString();
                sendCommand('valueChanged', { value: text });
                sendCommand('validate', { text });
              }, DEBOUNCE_MS);
            }
          }),
        ],
      }),
      parent: editorRef.current,
    });

    viewRef.current = view;

    return () => {
      if (timerRef.current) clearTimeout(timerRef.current);
      view.destroy();
      viewRef.current = null;
    };
  }, []); // Mount once

  // --- Update readOnly dynamically via Compartment ---

  useEffect(() => {
    const view = viewRef.current;
    if (!view) return;
    view.dispatch({
      effects: editableComp.current.reconfigure(EditorView.editable.of(!readOnly)),
    });
  }, [readOnly]);

  // --- Diagnostics display (from server SSE state patches) ---

  const diagnostics = (currentState.diagnostics as ServerDiagnostic[]) ?? [];

  useEffect(() => {
    const view = viewRef.current;
    if (!view) return;

    const doc = view.state.doc;
    const cmDiags: Diagnostic[] = diagnostics
      .map((d) => {
        const lineNum = Math.max(1, Math.min(d.line, doc.lines));
        const line = doc.line(lineNum);
        const from = line.from + Math.max(0, Math.min(d.col - 1, line.length));
        const to = d.endCol != null && d.endLine != null
          ? doc.line(Math.max(1, Math.min(d.endLine, doc.lines))).from +
            Math.max(0, Math.min(d.endCol - 1, line.length))
          : Math.min(from + 1, line.to);
        return { from, to, severity: d.severity, message: d.message };
      })
      .filter((d) => d.from <= doc.length && d.to <= doc.length);

    view.dispatch(setDiagnostics(view.state, cmDiags));
  }, [diagnostics]);

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
      options: items.map((c) => ({
        label: c.name,
        apply: c.replacement ?? c.name,
        detail: c.value !== c.name ? c.value : undefined,
        info: c.docHTML ? () => {
          const div = document.createElement('div');
          div.innerHTML = c.docHTML!;
          return div;
        } : undefined,
        boost: c.score ?? 0,
      })),
    });
  }, [completionResponse]);

  const handleContainerClick = useCallback((e: React.MouseEvent) => {
    const view = viewRef.current;
    if (view && !view.dom.contains(e.target as Node)) {
      view.focus();
    }
  }, []);

  return <div ref={editorRef} id={controlId} className="tlScriptEditor" onClick={handleContainerClick} />;
};

export default TLScriptEditor;
