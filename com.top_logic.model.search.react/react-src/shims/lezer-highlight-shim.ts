// Redirects '@lezer/highlight' imports to the shared tl-code-editor bundle, so the grammar's
// highlight tags are the SAME Tag instances the base editor's highlight style maps — otherwise
// highlighting silently fails across bundle boundaries. Aliased in via vite.config.ts.
export { styleTags, tags } from 'tl-code-editor';
