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
      // Redirect 'react' and 'react-dom' imports (from react-chartjs-2 etc.)
      // to shims that re-export from tl-react-bridge, ensuring a single
      // shared React instance. Without this, third-party libraries bundle
      // their own React copy causing "useState is null" errors.
      // Order matters: more specific paths must come first.
      'react/jsx-runtime': path.resolve(__dirname, 'react-src/react-jsx-runtime-shim.ts'),
      'react-dom': path.resolve(__dirname, 'react-src/react-dom-shim.ts'),
      'react': path.resolve(__dirname, 'react-src/react-shim.ts'),
    },
  },
  build: {
    lib: {
      entry: 'react-src/chartjs-entry.ts',
      fileName: () => 'tl-react-chartjs.js',
      formats: ['es'],
    },
    outDir: 'src/main/webapp/script',
    emptyOutDir: false,
    rollupOptions: {
      external: ['tl-react-bridge'],
    },
  },
});
