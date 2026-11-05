import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react({ jsxRuntime: 'classic' })],
  define: {
    'process.env.NODE_ENV': JSON.stringify('production'),
  },
  build: {
    lib: {
      entry: 'react-src/bpe-view-entry.ts',
      fileName: () => 'tl-bpe-view.js',
      formats: ['es'],
    },
    outDir: 'src/main/webapp/script',
    emptyOutDir: false,
    rollupOptions: {
      external: ['tl-react-bridge'],
    },
  },
});
