import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// This bundle is the single owner of the CodeMirror 6 + Lezer runtime for the React UI.
// It registers the generic TLCodeEditor control and re-exports (from code-editor-entry.ts)
// the base CodeEditor component plus the few CodeMirror/Lezer primitives that downstream
// editors (e.g. the TL-Script editor) build on, so those share THIS bundle's single runtime
// instance — the same single-instance discipline tl-react-bridge enforces for React.
export default defineConfig({
  plugins: [react({ jsxRuntime: 'classic' })],
  define: {
    'process.env.NODE_ENV': JSON.stringify('production'),
  },
  build: {
    lib: {
      entry: 'react-src/code-editor-entry.ts',
      fileName: () => 'tl-code-editor.js',
      formats: ['es'],
    },
    outDir: 'src/main/webapp/script',
    emptyOutDir: false,
    rollupOptions: {
      external: ['tl-react-bridge'],
    },
  },
});
