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
      'react/jsx-runtime': path.resolve(__dirname, 'react-src/react-jsx-runtime-shim.ts'),
      'react-dom': path.resolve(__dirname, 'react-src/react-dom-shim.ts'),
      'react': path.resolve(__dirname, 'react-src/react-shim.ts'),
    },
  },
  build: {
    lib: {
      entry: 'react-src/wysiwyg-entry.ts',
      fileName: () => 'tl-react-wysiwyg.js',
      formats: ['es'],
    },
    outDir: 'src/main/webapp/script',
    emptyOutDir: false,
    rollupOptions: {
      external: ['tl-react-bridge'],
    },
  },
});
