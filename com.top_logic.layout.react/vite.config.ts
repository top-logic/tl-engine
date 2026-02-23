import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  build: {
    lib: {
      entry: 'react-src/index.ts',
      name: 'TLReact',
      fileName: () => 'tl-react-bridge.js',
      formats: ['iife'],
    },
    outDir: 'src/main/webapp/script',
    emptyOutDir: false,
    rollupOptions: {
      output: {
        // Ensure the IIFE assigns to 'window' explicitly, not 'this' (which is
        // undefined in strict mode).
        footer: 'window.TLReact = TLReact;',
      },
    },
  },
});
