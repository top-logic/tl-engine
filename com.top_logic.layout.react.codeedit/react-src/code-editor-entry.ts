// Entry point for the tl-code-editor bundle.
//
// IMPORTANT: All control components MUST import React from 'tl-react-bridge' (not 'react').
//
// This bundle owns the single CodeMirror 6 + Lezer runtime instance for the React UI. Besides
// registering the generic TLCodeEditor control, it re-exports the base CodeEditor component and
// the CodeMirror/Lezer primitives that downstream editors build on. Downstream bundles (e.g. the
// TL-Script editor) import those from the 'tl-code-editor' specifier instead of from their own
// copies, so highlight tags, node props and language objects keep a single identity across
// bundles — the same discipline tl-react-bridge enforces for React.
import { register } from 'tl-react-bridge';

import TLCodeEditor from './controls/TLCodeEditor';

register('TLCodeEditor', TLCodeEditor);

// Reusable surface for specialized editors.
export { default as CodeEditor } from './controls/CodeEditor';
export type { CodeEditorProps, CodeEditorDiagnostic } from './controls/CodeEditor';

// Shared runtime primitives (single instance owned by this bundle).
export { LRLanguage, LanguageSupport } from '@codemirror/language';
export { styleTags, tags } from '@lezer/highlight';
export { LRParser } from '@lezer/lr';
