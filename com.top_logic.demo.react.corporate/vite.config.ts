import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react({ jsxRuntime: 'classic' })],
  define: {
    'process.env.NODE_ENV': JSON.stringify('production'),
  },
  resolve: {
    alias: {
      // The example component library (react-src/example-lib) imports React from 'react' like any
      // third-party library. These shims redirect those imports to tl-react-bridge, so the library
      // renders with the shared React instance instead of a bundled copy of its own.
      // Order matters: more specific paths must come first.
      'react/jsx-runtime': path.resolve(__dirname, 'react-src/react-jsx-runtime-shim.ts'),
      'react-dom': path.resolve(__dirname, 'react-src/react-dom-shim.ts'),
      'react': path.resolve(__dirname, 'react-src/react-shim.ts'),
    },
  },
  build: {
    lib: {
      entry: 'react-src/corporate-entry.ts',
      fileName: () => 'tl-demo-react-corporate.js',
      formats: ['es'],
    },
    outDir: 'src/main/webapp/script',
    emptyOutDir: false,
    rollupOptions: {
      external: ['tl-react-bridge'],
    },
  },
});
