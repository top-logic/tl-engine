// Generic source-code editor form control, keyed by a server-provided language id.
//
// IMPORTANT: React is imported from 'tl-react-bridge' (not 'react') to share the single React
// instance. Language packages are imported directly because this component lives in the same
// bundle as CodeEditor, i.e. shares this bundle's single CodeMirror runtime.
import { React, useTLFieldValue } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import type { Extension } from '@codemirror/state';
import { LanguageSupport } from '@codemirror/language';
import { linter } from '@codemirror/lint';
import type { Diagnostic } from '@codemirror/lint';
import { xml } from '@codemirror/lang-xml';
import { json, jsonParseLinter } from '@codemirror/lang-json';
import { css } from '@codemirror/lang-css';
import { javascript } from '@codemirror/lang-javascript';
import { html } from '@codemirror/lang-html';
import { markdown } from '@codemirror/lang-markdown';
import CodeEditor from './CodeEditor';

const { useMemo } = React;

/** Language support plus any client-side extras (e.g. a linter) for a given language id. */
interface LanguageBinding {
  support?: LanguageSupport;
  extras: Extension[];
}

/**
 * Linter checking XML well-formedness through the browser's {@code DOMParser}. Reports the first
 * parse error (bare {@code &}, mismatched/unclosed tags, multiple roots, ...) at its reported
 * line/column when the parser exposes one, otherwise at the document start.
 */
function xmlLinter(): Extension {
  return linter((view): Diagnostic[] => {
    const text = view.state.doc.toString();
    if (text.trim() === '') {
      return [];
    }
    const parsed = new DOMParser().parseFromString(text, 'application/xml');
    const error = parsed.querySelector('parsererror');
    if (!error) {
      return [];
    }
    // Chrome nests the "error on line N at column M: <reason>" text in a child element and wraps
    // the parsererror itself in boilerplate; prefer the child, falling back to the full text (e.g.
    // Firefox, whose parsererror text is already the bare message).
    const detail = error.querySelector('div')?.textContent ?? error.textContent ?? 'Invalid XML.';
    const message = detail.replace(/\s+/g, ' ').trim() || 'Invalid XML.';
    const doc = view.state.doc;
    const match = /line (\d+)[^\d]+column (\d+)/i.exec(message);
    if (match) {
      const lineNo = Math.max(1, Math.min(parseInt(match[1], 10), doc.lines));
      const line = doc.line(lineNo);
      const from = line.from + Math.max(0, Math.min(parseInt(match[2], 10) - 1, line.length));
      return [{ from, to: Math.min(from + 1, line.to), severity: 'error', message }];
    }
    return [{ from: 0, to: Math.min(text.length, doc.line(1).to), severity: 'error', message }];
  });
}

/**
 * JSON validation that treats an empty (or whitespace-only) document as valid. The bare
 * {@code jsonParseLinter} reports "Unexpected end of JSON input" for an empty field, which is
 * noise for an optional value.
 */
function jsonLinter(): Extension {
  const parse = jsonParseLinter();
  return linter((view) => (view.state.doc.toString().trim() === '' ? [] : parse(view)));
}

/** Resolves a server language id (see {@code CodeEditorLanguage}) to its CodeMirror support. */
function bindingFor(language: string): LanguageBinding {
  switch (language) {
    case 'xml':
      return { support: xml(), extras: [xmlLinter()] };
    case 'json':
      return { support: json(), extras: [jsonLinter()] };
    case 'css':
      return { support: css(), extras: [] };
    case 'javascript':
      return { support: javascript(), extras: [] };
    case 'html':
      return { support: html(), extras: [] };
    case 'markdown':
      return { support: markdown(), extras: [] };
    default:
      return { support: undefined, extras: [] };
  }
}

/**
 * Source-code editor bound to a string/text form field. The language is fixed by the
 * {@code language} state contributed by the server-side control.
 */
const TLCodeEditor: React.FC<TLCellProps> = ({ controlId, state }) => {
  const [value, setValue] = useTLFieldValue();
  const language = (state.language as string) ?? 'plain';
  const readOnly = state.editable === false;

  const binding = useMemo(() => bindingFor(language), [language]);

  const cls = [
    'tlCodeEditor',
    state.hasError === true ? 'tlCodeEditor--error' : '',
  ].filter(Boolean).join(' ');

  return (
    <CodeEditor
      controlId={controlId}
      value={(value as string) ?? ''}
      readOnly={readOnly}
      languageSupport={binding.support}
      extraExtensions={binding.extras}
      onChange={setValue}
      className={cls}
    />
  );
};

export default TLCodeEditor;
