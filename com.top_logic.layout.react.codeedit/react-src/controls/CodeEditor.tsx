// Reusable CodeMirror 6 editor surface.
//
// IMPORTANT: React is imported from 'tl-react-bridge' (not 'react') so this bundle shares the
// single React instance. This component owns the CodeMirror runtime for the whole React UI; the
// generic TLCodeEditor and downstream editors (e.g. the TL-Script editor) build on it via the
// primitives re-exported from this bundle's entry point.
import { React } from 'tl-react-bridge';
import {
  EditorView, keymap, lineNumbers, highlightActiveLine, highlightActiveLineGutter,
} from '@codemirror/view';
import { EditorState, Compartment } from '@codemirror/state';
import type { Extension } from '@codemirror/state';
import { defaultKeymap, history, historyKeymap, indentWithTab } from '@codemirror/commands';
import { autocompletion, closeBrackets } from '@codemirror/autocomplete';
import type { CompletionSource } from '@codemirror/autocomplete';
import { setDiagnostics } from '@codemirror/lint';
import type { Diagnostic } from '@codemirror/lint';
import {
  syntaxHighlighting, defaultHighlightStyle, bracketMatching, LanguageSupport,
} from '@codemirror/language';

const { useRef, useEffect } = React;

/** A diagnostic expressed in 1-based line/column coordinates (as produced server-side). */
export interface CodeEditorDiagnostic {
  /** 1-based line of the start position. */
  line: number;
  /** 1-based column of the start position. */
  col: number;
  /** 1-based line of the end position; defaults to a single-character range when absent. */
  endLine?: number;
  /** 1-based column of the end position. */
  endCol?: number;
  /** Marker severity. */
  severity: 'error' | 'warning' | 'info';
  /** Human-readable message. */
  message: string;
}

/** Props of the reusable {@link CodeEditor}. */
export interface CodeEditorProps {
  /** DOM id for the editor container (the mount-point of the host control). */
  controlId: string;
  /** The current document text (controlled: external changes replace the document). */
  value: string;
  /** Whether editing is disabled. */
  readOnly: boolean;
  /** CodeMirror language support providing syntax parsing/highlighting. */
  languageSupport?: LanguageSupport;
  /** Additional CodeMirror extensions (e.g. a client-side linter). */
  extraExtensions?: Extension[];
  /** Completion source for server- or client-backed autocompletion. */
  completionSource?: CompletionSource;
  /** Diagnostics to render as markers, in 1-based line/column coordinates. */
  diagnostics?: CodeEditorDiagnostic[];
  /** Called (debounced) with the full text whenever the document changes. */
  onChange?: (text: string) => void;
  /** Debounce delay for {@link onChange} in milliseconds (default 300). */
  debounceMs?: number;
  /** CSS class of the editor container. */
  className?: string;
}

/** Maps a 1-based line/column diagnostic onto an absolute CodeMirror document range. */
function toEditorDiagnostic(view: EditorView, d: CodeEditorDiagnostic): Diagnostic {
  const doc = view.state.doc;
  const lineNum = Math.max(1, Math.min(d.line, doc.lines));
  const line = doc.line(lineNum);
  const from = line.from + Math.max(0, Math.min(d.col - 1, line.length));
  const to = d.endCol != null && d.endLine != null
    ? doc.line(Math.max(1, Math.min(d.endLine, doc.lines))).from + Math.max(0, Math.min(d.endCol - 1, line.length))
    : Math.min(from + 1, line.to);
  return { from, to, severity: d.severity, message: d.message };
}

/**
 * Renders a CodeMirror 6 editor with line numbers, bracket matching, history and an editable
 * compartment. Value is controlled; document changes are reported through {@link CodeEditorProps#onChange}.
 */
const CodeEditor: React.FC<CodeEditorProps> = (props) => {
  const {
    controlId, value, readOnly, languageSupport, extraExtensions, completionSource,
    diagnostics, debounceMs = 300, className,
  } = props;

  const editorRef = useRef<HTMLDivElement>(null);
  const viewRef = useRef<EditorView | null>(null);
  const editableComp = useRef(new Compartment());
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  // Latest onChange, read through a ref so the mount-once editor always calls the current callback.
  const onChangeRef = useRef(props.onChange);
  onChangeRef.current = props.onChange;

  // --- Create editor on mount ---
  useEffect(() => {
    if (!editorRef.current) return;

    const extensions: Extension[] = [
      lineNumbers(),
      highlightActiveLine(),
      highlightActiveLineGutter(),
      bracketMatching(),
      closeBrackets(),
      history(),
      keymap.of([...defaultKeymap, ...historyKeymap, indentWithTab]),
      syntaxHighlighting(defaultHighlightStyle),
      editableComp.current.of(EditorView.editable.of(!readOnly)),
      EditorView.updateListener.of((update) => {
        if (update.docChanged) {
          if (timerRef.current) clearTimeout(timerRef.current);
          timerRef.current = setTimeout(() => {
            onChangeRef.current?.(update.state.doc.toString());
          }, debounceMs);
        }
      }),
    ];
    if (languageSupport) {
      extensions.push(languageSupport);
    }
    if (completionSource) {
      extensions.push(autocompletion({ override: [completionSource] }));
    }
    if (extraExtensions) {
      extensions.push(...extraExtensions);
    }

    const view = new EditorView({
      state: EditorState.create({ doc: value, extensions }),
      parent: editorRef.current,
    });
    viewRef.current = view;

    return () => {
      if (timerRef.current) clearTimeout(timerRef.current);
      view.destroy();
      viewRef.current = null;
    };
    // Mount once; runtime updates flow through the dedicated effects below.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // --- Sync external value changes into the document ---
  useEffect(() => {
    const view = viewRef.current;
    if (!view) return;
    const current = view.state.doc.toString();
    if (value !== current) {
      view.dispatch({ changes: { from: 0, to: current.length, insert: value } });
    }
  }, [value]);

  // --- Reflect readOnly changes via the editable compartment ---
  useEffect(() => {
    const view = viewRef.current;
    if (!view) return;
    view.dispatch({ effects: editableComp.current.reconfigure(EditorView.editable.of(!readOnly)) });
  }, [readOnly]);

  // --- Render diagnostics ---
  useEffect(() => {
    const view = viewRef.current;
    if (!view) return;
    const cmDiags = (diagnostics ?? [])
      .map((d) => toEditorDiagnostic(view, d))
      .filter((d) => d.from <= view.state.doc.length && d.to <= view.state.doc.length);
    view.dispatch(setDiagnostics(view.state, cmDiags));
  }, [diagnostics]);

  // The root always carries `tlCodeEditorSurface` (structural styling shared by every editor built
  // on this base, e.g. the TL-Script editor) plus the consumer's cosmetic class.
  return (
    <div
      ref={editorRef}
      id={controlId}
      className={['tlCodeEditorSurface', className ?? 'tlCodeEditor'].join(' ')}
    />
  );
};

export default CodeEditor;
