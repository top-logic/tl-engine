import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  define: {
    'process.env.NODE_ENV': JSON.stringify('production'),
  },
  build: {
    lib: {
      entry: 'react-src/controls-entry.ts',
      name: 'TLReactControls',
      fileName: () => 'tl-react-controls.js',
      formats: ['iife'],
    },
    outDir: 'src/main/webapp/script',
    emptyOutDir: false,
    rollupOptions: {
      external: ['react', 'react-dom', 'react/jsx-runtime', 'tl-react-bridge'],
      output: {
        globals: {
          'react': 'TLReact.React',
          'react-dom': 'TLReact.ReactDOM',
          'react/jsx-runtime': 'TLReact.React',
          'tl-react-bridge': 'TLReact',
        },
      },
    },
  },
});
