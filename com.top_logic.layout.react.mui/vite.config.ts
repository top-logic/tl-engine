import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react({ jsxRuntime: 'classic' })],
  define: {
    'process.env.NODE_ENV': JSON.stringify('production'),
  },
  resolve: {
    alias: [
      // Material UI and emotion import React from 'react' like any third-party library. These shims
      // redirect those imports to tl-react-bridge, so the libraries render with the shared React
      // instance instead of a bundled copy of their own. The patterns match the bare specifiers
      // exactly; any other React entry point would resolve to a bundled copy, which the build
      // check against React's internals in the bundle reveals.
      { find: /^react\/jsx-runtime$/, replacement: path.resolve(__dirname, 'react-src/react-jsx-runtime-shim.ts') },
      { find: /^react-dom$/, replacement: path.resolve(__dirname, 'react-src/react-dom-shim.ts') },
      { find: /^react$/, replacement: path.resolve(__dirname, 'react-src/react-shim.ts') },
    ],
  },
  build: {
    lib: {
      entry: 'react-src/mui-entry.ts',
      fileName: () => 'tl-react-mui.js',
      formats: ['es'],
    },
    outDir: 'src/main/webapp/script',
    emptyOutDir: false,
    rollupOptions: {
      external: ['tl-react-bridge'],
    },
  },
});
