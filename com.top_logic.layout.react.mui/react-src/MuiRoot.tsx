import { React } from 'tl-react-bridge';
import createCache from '@emotion/cache';
import { CacheProvider } from '@emotion/react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import type { Theme, ThemeOptions } from '@mui/material/styles';
import { deDE as coreDeDE, enUS as coreEnUS } from '@mui/material/locale';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { deDE as pickersDeDE, enUS as pickersEnUS } from '@mui/x-date-pickers/locales';
import 'dayjs/locale/de';
import 'dayjs/locale/en';

/** The key emotion prefixes the class names and style elements of Material UI with. */
const CACHE_KEY = 'mui';

/** The language of the MUI texts and the date format while the page names none this bundle has. */
const DEFAULT_LOCALE = 'en';

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
 * The themes of Material UI per page language: the theme of the application, published as CSS
 * custom properties (`--mui-…`), with the texts of the core components (e.g. of an autocomplete) and
 * of the date pickers (the names of a picker's sections, its buttons) in that language. Each key is
 * also a dayjs locale this bundle loads.
 */
export type PageThemes = Readonly<Record<string, Theme>>;

/**
 * Creates the {@link PageThemes} from the options of the application's theme, as `createTheme`
 * takes them (they may contain functions, e.g. a typography computed from the palette).
 */
export function createPageThemes(options: ThemeOptions): PageThemes {
  return {
    de: createTheme({ ...options, cssVariables: true }, pickersDeDE, coreDeDE),
    en: createTheme({ ...options, cssVariables: true }, pickersEnUS, coreEnUS),
  };
}

/**
 * The language the page is rendered in (the `lang` of `<html>`), as far as the themes have texts
 * for it: one of the keys of the given {@link PageThemes}, else {@link DEFAULT_LOCALE}.
 */
function pageLocale(themes: PageThemes): string {
  const lang = document.documentElement.lang;
  return lang in themes ? lang : DEFAULT_LOCALE;
}

/**
 * The MUI theme of the page: the one of the given {@link PageThemes} for the page language.
 */
export function pageTheme(themes: PageThemes): Theme {
  return themes[pageLocale(themes)];
}

/**
 * Creates the root wrapper of every React root of the bridge: the emotion cache, the MUI theme of
 * the page language and the localization of the date pickers (the dayjs locale of that language).
 *
 * <p>The wrapper renders providers only, no element of its own: it sits above the content of every
 * React root, and an element there would break the fill layout of the TopLogic components (see
 * `useFill` of 'tl-react-bridge'). For the same reason it uses no `ScopedCssBaseline`, which renders
 * an element, and no global `CssBaseline`, whose reset collides with the TopLogic stylesheets: the
 * MUI components render without the MUI baseline.</p>
 */
export function createMuiRoot(themes: PageThemes): React.ComponentType<{ children?: React.ReactNode }> {
  return function MuiRoot({ children }: { children?: React.ReactNode }) {
    return (
      <CacheProvider value={muiCache}>
        <ThemeProvider theme={pageTheme(themes)}>
          <LocalizationProvider dateAdapter={AdapterDayjs} adapterLocale={pageLocale(themes)}>
            {children}
          </LocalizationProvider>
        </ThemeProvider>
      </CacheProvider>
    );
  };
}
