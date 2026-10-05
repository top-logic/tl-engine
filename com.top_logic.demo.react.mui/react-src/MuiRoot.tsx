import { React } from 'tl-react-bridge';
import createCache from '@emotion/cache';
import { CacheProvider } from '@emotion/react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import 'dayjs/locale/de';
import 'dayjs/locale/en';

/** The key emotion prefixes the class names and style elements of Material UI with. */
const CACHE_KEY = 'mui';

/** The locale of the date pickers while the page names no language of its own. */
const DEFAULT_LOCALE = 'en';

/** The dayjs locales this bundle loads; any other page language falls back to {@link DEFAULT_LOCALE}. */
const LOCALES = new Set(['de', 'en']);

/**
 * The style cache of Material UI, shared by all React roots of the page.
 *
 * <p>The TopLogic stylesheets are `<link>` elements the server writes into the page `<head>`
 * (ClientResources), and this bundle is an ES module script, which runs once the page is parsed. A
 * cache that appends to the end of the `<head>` (`prepend: false`) therefore puts every MUI style
 * after the TopLogic stylesheets: on its own elements Material UI wins a tie in specificity.</p>
 */
const muiCache = createCache({ key: CACHE_KEY, container: document.head, prepend: false });

/**
 * The theme of Material UI: its default theme, published as CSS custom properties (`--mui-…`).
 */
const muiTheme = createTheme({ cssVariables: true });

/** The dayjs locale for the language the page is rendered in (the `lang` of `<html>`). */
function pageLocale(): string {
  const lang = document.documentElement.lang;
  return LOCALES.has(lang) ? lang : DEFAULT_LOCALE;
}

/**
 * Root wrapper of every React root of the bridge: the emotion cache, the MUI theme and the
 * localization of the date pickers.
 *
 * <p>The wrapper renders providers only, no element of its own: it sits above the content of every
 * React root, and an element there would break the fill layout of the TopLogic components (see
 * `useFill` of 'tl-react-bridge'). For the same reason it uses no `ScopedCssBaseline`, which renders
 * an element, and no global `CssBaseline`, whose reset collides with the TopLogic stylesheets: the
 * MUI components render without the MUI baseline.</p>
 */
export default function MuiRoot({ children }: { children?: React.ReactNode }) {
  return (
    <CacheProvider value={muiCache}>
      <ThemeProvider theme={muiTheme}>
        <LocalizationProvider dateAdapter={AdapterDayjs} adapterLocale={pageLocale()}>
          {children}
        </LocalizationProvider>
      </ThemeProvider>
    </CacheProvider>
  );
}
