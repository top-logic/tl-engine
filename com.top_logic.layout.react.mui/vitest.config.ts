import { defineConfig } from 'vitest/config';
import path from 'path';

/**
 * Test configuration of the MUI adapter module.
 *
 * The build (vite.config.ts) keeps tl-react-bridge external and redirects the React
 * imports to shims that read React from the bridge. A test has no page that loads the bridge, so it
 * resolves tl-react-bridge to its source (as tsconfig.json does) and lets 'react' and 'react-dom'
 * resolve directly: all of them -- the bridge, Material UI, emotion, the adapters, the testing library --
 * then share one React instance, the one installed in this module.
 */
const reactDir = path.resolve(__dirname, 'node_modules/react');
const reactDomDir = path.resolve(__dirname, 'node_modules/react-dom');

/**
 * The React binding of external stores that the rich text editor (Tiptap) reads its state with. It
 * is CommonJS, so Node loads it and resolves its own 'react' next to it, past the aliases: the copy
 * in the editor module would load the React of that module. The copy installed in this module loads
 * the React of this module.
 */
const syncExternalStoreDir = path.resolve(__dirname, 'node_modules/use-sync-external-store');

export default defineConfig({
  resolve: {
    alias: {
      'tl-react-bridge': path.resolve(__dirname, '../com.top_logic.layout.react/react-src/bridge-entry.ts'),
      'react-dom': reactDomDir,
      'react': reactDir,
      'use-sync-external-store': syncExternalStoreDir,
    },
    dedupe: ['react', 'react-dom'],
  },
  test: {
    environment: 'jsdom',
    include: ['react-src/**/*.test.{ts,tsx}'],
    setupFiles: ['react-src/test-setup.ts'],
    server: {
      deps: {
        // Dependencies of the bridge that import React are processed by Vite, so the aliases above
        // reach them too. The same holds for the libraries of the editor modules a test mounts:
        // the rich text editor (Tiptap) of com.top_logic.layout.react.wysiwyg, and the chart
        // (react-chartjs-2 and Chart.js) of com.top_logic.layout.react.chartjs.
        inline: [
          /@floating-ui/,
          /@testing-library/,
          /@tiptap/,
          /react-chartjs-2/,
          /chart\.js/,
        ],
      },
    },
  },
});
