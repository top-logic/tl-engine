import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { lezer } from '@lezer/generator/rollup';

export default defineConfig({
  plugins: [react({ jsxRuntime: 'classic' }), lezer()],
  define: {
    'process.env.NODE_ENV': JSON.stringify('production'),
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
      external: ['tl-react-bridge'],
    },
  },
});
