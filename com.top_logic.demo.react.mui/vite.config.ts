import { defineConfig } from 'vite';
import type { Plugin } from 'vite';

/** The import specifier of Material UI on the page: the bundle of tl-layout-react-mui. */
const TL_REACT_MUI = 'tl-react-mui';

/** The entry points of Material UI whose exports tl-react-mui re-exports. */
const MUI_ENTRY_POINTS = ['@mui/material', '@mui/material/styles', '@mui/x-date-pickers'];

/** The packages of Material UI and its style engine, which must not be bundled a second time. */
const MUI_PACKAGES = /^@(mui|emotion)\//;

/**
 * Resolves the imports of Material UI to tl-react-mui, the Material UI of the page.
 *
 * <p>The code of the module imports Material UI from tl-react-mui. A library that imports one of
 * {@link MUI_ENTRY_POINTS} gets tl-react-mui as well. Every other import of Material UI or emotion
 * fails the build: it would bundle a second copy of Material UI, with a theme and a style cache of
 * its own.</p>
 */
function sharedMaterialUi(): Plugin {
  return {
    name: 'shared-material-ui',
    enforce: 'pre',
    resolveId(source) {
      if (MUI_ENTRY_POINTS.includes(source)) {
        return { id: TL_REACT_MUI, external: true };
      }
      if (MUI_PACKAGES.test(source)) {
        this.error(`'${source}' would bundle a second copy of Material UI; import it from '${TL_REACT_MUI}'.`);
      }
      return null;
    },
  };
}

export default defineConfig({
  plugins: [sharedMaterialUi()],
  build: {
    lib: {
      entry: 'react-src/demo-mui-entry.ts',
      fileName: () => 'tl-demo-react-mui.js',
      // The stylesheet of the bundle: the fonts of the theme, embedded (library mode inlines the
      // assets the stylesheet references).
      cssFileName: 'tl-demo-react-mui',
      formats: ['es'],
    },
    outDir: 'src/main/webapp/script',
    emptyOutDir: false,
    rollupOptions: {
      external: ['tl-react-bridge', TL_REACT_MUI],
    },
  },
});
