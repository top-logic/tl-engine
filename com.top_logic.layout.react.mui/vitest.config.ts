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

export default defineConfig({
  resolve: {
    alias: {
      'tl-react-bridge': path.resolve(__dirname, '../com.top_logic.layout.react/react-src/bridge-entry.ts'),
      'react-dom': reactDomDir,
      'react': reactDir,
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
        // reach them too.
        inline: [/@floating-ui/, /@testing-library/],
      },
    },
  },
});
