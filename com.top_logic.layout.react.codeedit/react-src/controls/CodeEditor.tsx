// Reusable CodeMirror 6 editor surface.
//
// IMPORTANT: React is imported from 'tl-react-bridge' (not 'react') so this bundle shares the
// single React instance. This component owns the CodeMirror runtime for the whole React UI; the
// generic TLCodeEditor and downstream editors (e.g. the TL-Script editor) build on it via the
// primitives re-exported from this bundle's entry point.
import { React } from 'tl-react-bridge';
import {
  EditorView, keymap, lineNumbers, highlightActiveLine, highlightActiveLineGutter, hoverTooltip,
  ViewPlugin,
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

/** The content of a hover tooltip, anchored at a document range. */
export interface CodeEditorHover {
  /** Start of the document range the tooltip describes. */
  from: number;
  /** End of the document range the tooltip describes; defaults to {@link from}. */
  to?: number;
  /** The tooltip content. */
  dom: HTMLElement;
}

/**
 * Supplies the hover tooltip for a document position.
 *
 * @param view The editor view.
 * @param pos The document position under the mouse pointer.
 * @param side Whether the pointer is before (-1) or after (1) the position.
 * @returns The tooltip content, or null when the position has nothing to show.
 */
export type CodeEditorHoverSource =
  (view: EditorView, pos: number, side: -1 | 1) => Promise<CodeEditorHover | null>;

/** Delay in milliseconds the pointer rests on a position before the hover tooltip is requested. */
const HOVER_TIME_MS = 500;

/** Class of CodeMirror's documentation panel shown next to the completion option list. */
const COMPLETION_INFO_CLASS = 'cm-completionInfo';

/**
 * Keeps the focus in the editor on a mousedown inside the completion info panel.
 *
 * <p>
 * CodeMirror closes an open completion when the editor loses the focus. The option list prevents
 * the default action of its own mousedown, the info panel does not; without this guard a click
 * into the panel (or a drag of its scrollbar) blurs the editor and thereby closes the completion
 * the panel belongs to. The listener sits on the editor root in the capture phase, because the
 * tooltips are children of the root but not of the content element that
 * {@code EditorView.domEventHandlers} listens on.
 * </p>
 */
const keepFocusOnCompletionInfo = ViewPlugin.fromClass(class {
  private readonly _dom: HTMLElement;

  constructor(view: EditorView) {
    this._dom = view.dom;
    this._dom.addEventListener('mousedown', this.onMouseDown, true);
  }

  private readonly onMouseDown = (event: MouseEvent) => {
    const target = event.target as Element | null;
    if (target?.closest?.('.' + COMPLETION_INFO_CLASS)) {
      event.preventDefault();
    }
  };

  destroy() {
    this._dom.removeEventListener('mousedown', this.onMouseDown, true);
  }
});

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
  /** Source of hover tooltips (e.g. documentation of the symbol under the mouse pointer). */
  hoverSource?: CodeEditorHoverSource;
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
    controlId, value, readOnly, languageSupport, extraExtensions, completionSource, hoverSource,
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
      extensions.push(autocompletion({ override: [completionSource] }), keepFocusOnCompletionInfo);
    }
    if (hoverSource) {
      extensions.push(hoverTooltip(async (view, pos, side) => {
        const hover = await hoverSource(view, pos, side);
        if (!hover) return null;
        return { pos: hover.from, end: hover.to, above: false, create: () => ({ dom: hover.dom }) };
      }, { hoverTime: HOVER_TIME_MS, hideOnChange: true }));
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
    if (value === current) return;

    // Only the differing middle part is replaced: CodeMirror maps the selection through the change,
    // so a cursor before or after the changed range keeps its position in the text.
    const maxPrefix = Math.min(current.length, value.length);
    let prefixLen = 0;
    while (prefixLen < maxPrefix && current.charCodeAt(prefixLen) === value.charCodeAt(prefixLen)) {
      prefixLen++;
    }
    const maxSuffix = maxPrefix - prefixLen;
    let suffixLen = 0;
    while (suffixLen < maxSuffix
        && current.charCodeAt(current.length - 1 - suffixLen) === value.charCodeAt(value.length - 1 - suffixLen)) {
      suffixLen++;
    }
    view.dispatch({
      changes: {
        from: prefixLen,
        to: current.length - suffixLen,
        insert: value.slice(prefixLen, value.length - suffixLen),
      },
    });
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
