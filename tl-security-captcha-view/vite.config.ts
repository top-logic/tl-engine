import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react({ jsxRuntime: 'classic' })],
  define: {
    'process.env.NODE_ENV': JSON.stringify('production'),
  },
  build: {
    lib: {
      entry: 'react-src/captcha-entry.ts',
      fileName: () => 'tl-security-captcha-view.js',
      formats: ['es'],
    },
    outDir: 'src/main/webapp/script',
    emptyOutDir: false,
    rollupOptions: {
      // One React instance for the page. The ALTCHA widget is a web component without React,
      // the script /script/altcha.js of tl-security-captcha, shared with the classic UI.
      external: ['tl-react-bridge', 'altcha'],
    },
  },
});
