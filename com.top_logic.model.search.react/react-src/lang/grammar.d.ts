// Type of the modules that the @lezer/generator rollup plugin (see vite.config.ts) builds from
// '*.grammar' files: the generated parser, instantiated on the shared @lezer/lr runtime.
declare module '*.grammar' {
  import type { LRParser } from 'tl-code-editor';

  export const parser: LRParser;
}
