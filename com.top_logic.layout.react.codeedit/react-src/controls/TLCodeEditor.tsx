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

/** Resolves a server language id (see {@code CodeEditorLanguage}) to its CodeMirror support. */
function bindingFor(language: string): LanguageBinding {
  switch (language) {
    case 'xml':
      return { support: xml(), extras: [] };
    case 'json':
      return { support: json(), extras: [linter(jsonParseLinter())] };
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
