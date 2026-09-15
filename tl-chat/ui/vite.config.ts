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
    cssCodeSplit: false,
    lib: {
      entry: {
        'tl-chat-ui': 'src/main.tsx',
        'tl-chat-seams': 'src/index.ts',
      },
      formats: ['es'],
      fileName: (_format, entryName) => `${entryName}.js`,
    },
    outDir: '../src/main/webapp/script',
    rollupOptions: {
      external: ['tl-react-bridge'],
      output: {
        chunkFileNames: 'tl-chat-shared.js',
        assetFileNames: (asset) => {
          const names = [...(asset.names ?? []), asset.name ?? '']
          return names.some((name) => name.endsWith('.css')) ? 'tl-chat-ui.css' : 'assets/[name][extname]'
        },
      },
    },
  },
})
