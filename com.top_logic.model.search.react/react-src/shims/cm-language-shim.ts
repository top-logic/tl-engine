// Redirects '@codemirror/language' imports to the shared tl-code-editor bundle, so the TL-Script
// language is defined against the SAME @codemirror/language runtime instance that the base
// CodeEditor renders with. Aliased in via vite.config.ts (see resolve.alias).
export { LRLanguage, LanguageSupport } from 'tl-code-editor';
