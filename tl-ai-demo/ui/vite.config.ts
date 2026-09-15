import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react({ jsxRuntime: 'classic' })],
  define: {
    'process.env.NODE_ENV': JSON.stringify('production'),
  },
  build: {
    sourcemap: false,
    emptyOutDir: false,
    lib: {
      entry: 'src/main.tsx',
      formats: ['es'],
      fileName: () => 'tl-ai-demo-chat.js',
    },
    outDir: '../src/main/webapp/script',
    rollupOptions: {
      external: ['tl-react-bridge', 'tl-chat-seams'],
    },
  },
})
