// Redirects '@lezer/lr' imports to the shared tl-code-editor bundle, so the generated grammar
// parser is built on the SAME @lezer runtime (and thus @lezer/common node types) that the base
// editor's language and highlighter consume. Aliased in via vite.config.ts.
export { LRParser } from 'tl-code-editor';
