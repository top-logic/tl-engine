import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { lezer } from '@lezer/generator/rollup';
import path from 'path';

// The CodeMirror 6 + Lezer runtime is owned by the tl-code-editor bundle. The grammar and language
// definition here are aliased onto that single runtime instance (see react-src/shims) so highlight
// tags, node props and language objects keep one identity across bundles — the same single-instance
// discipline tl-react-bridge enforces for React.
export default defineConfig({
  plugins: [react({ jsxRuntime: 'classic' }), lezer()],
  define: {
    'process.env.NODE_ENV': JSON.stringify('production'),
  },
  resolve: {
    alias: {
      '@codemirror/language': path.resolve(__dirname, 'react-src/shims/cm-language-shim.ts'),
      '@lezer/highlight': path.resolve(__dirname, 'react-src/shims/lezer-highlight-shim.ts'),
      '@lezer/lr': path.resolve(__dirname, 'react-src/shims/lezer-lr-shim.ts'),
    },
  },
  build: {
    lib: {
      entry: 'react-src/entry.ts',
      fileName: () => 'tl-script-editor.js',
      formats: ['es'],
    },
    outDir: 'src/main/webapp/script',
    emptyOutDir: false,
    rollupOptions: {
      external: ['tl-react-bridge', 'tl-code-editor'],
    },
  },
});
