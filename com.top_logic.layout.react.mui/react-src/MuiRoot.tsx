import { React } from 'tl-react-bridge';
import createCache from '@emotion/cache';
import type { EmotionCache } from '@emotion/cache';
import { CacheProvider } from '@emotion/react';
import { ThemeProvider, createTheme, useColorScheme } from '@mui/material/styles';
import type { Theme, ThemeOptions, ThemeProviderProps } from '@mui/material/styles';
import { deDE as coreDeDE, enUS as coreEnUS } from '@mui/material/locale';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { deDE as pickersDeDE, enUS as pickersEnUS } from '@mui/x-date-pickers/locales';
import 'dayjs/locale/de';
import 'dayjs/locale/en';
import { CSS_LAYER, MODE_ATTRIBUTE } from './themeProperties';
import type { SchemeName } from './themeProperties';

/** The language of the MUI texts and the date format while the page names none this bundle has. */
const DEFAULT_LOCALE = 'en';

/** The key emotion prefixes the class names and style elements of Material UI with. */
const CACHE_KEY = 'mui';

/** Styles that are a layer statement only (`@layer a, b;`), which are not put into a layer. */
const LAYER_STATEMENT = /^@layer\s+[^{]*$/;

/**
 * Creates the style cache of Material UI: it appends its style elements to the end of `<head>` and
 * puts every rule into the CSS cascade layer {@link CSS_LAYER} (`@layer mui { … }`).
 *
 * <p>The page names the order of the layers in its `<head>` before any of its styles
 * (ClientResources): the layer `tl` of the TopLogic stylesheets comes before `mui`, so that a MUI
 * rule wins against a TopLogic rule on the same element, whatever their specificity and their
 * position in the page. The stylesheets of the application are unlayered and win against both.</p>
 *
 * <p>The cache wraps the rules as the `StyledEngineProvider` of MUI does with `enableCssLayer`.
 * That provider itself is not used: it writes the global styles (the CSS variables of the theme)
 * to the start of `<head>`, before the layer order of the page, and the first mention of a layer
 * fixes its position in the order, so `mui` would come before `tl`.</p>
 */
function createLayeredCache(): EmotionCache {
  const cache = createCache({ key: CACHE_KEY, container: document.head, prepend: false });
  const insert = cache.insert;
  cache.insert = (selector, serialized, sheet, shouldCache) => {
    const layered = LAYER_STATEMENT.test(serialized.styles)
      ? serialized
      : { ...serialized, styles: `@layer ${CSS_LAYER}{${serialized.styles}}` };
    return insert(selector, layered, sheet, shouldCache);
  };
  return cache;
}

/** The style cache of Material UI, shared by all React roots of the page. */
const muiCache = createLayeredCache();

/**
 * The selector of a color scheme of the MUI theme: the mode of the design system the scheme is in
 * effect for (`data-tl-mode` of `<html>`, written by the UI theme in effect).
 */
const COLOR_SCHEME_SELECTOR = `[${MODE_ATTRIBUTE}="%s"]`;

/** The mode of the design system while `<html>` names none. */
const DEFAULT_MODE: SchemeName = 'light';

/**
 * The themes of Material UI per page language: the theme of the application, published as CSS
 * custom properties (`--mui-…`, or after the `cssVarPrefix` of the options), with the texts of the
 * core components (e.g. of an autocomplete) and of the date pickers (the names of a picker's sections, its buttons) in that language. Each key is
 * also a dayjs locale this bundle loads.
 */
export type PageThemes = Readonly<Record<string, Theme>>;

/**
 * The options of the application's theme with CSS custom properties whose color schemes follow the
 * mode of the design system: the light scheme of a theme with a light and a dark one is in effect
 * in the light mode, the dark scheme in the dark mode. The `colorSchemeSelector` of the given
 * options is replaced, their other `cssVariables` options (e.g. `cssVarPrefix`) are kept.
 */
function withModeSelector(options: ThemeOptions): ThemeOptions {
  const cssVariables = typeof options.cssVariables === 'object' ? options.cssVariables : {};
  return { ...options, cssVariables: { ...cssVariables, colorSchemeSelector: COLOR_SCHEME_SELECTOR } };
}

/**
 * Creates the {@link PageThemes} from the options of the application's theme, as `createTheme`
 * takes them (they may contain functions, e.g. a typography computed from the palette).
 *
 * <p>A theme with a light and a dark color scheme renders the MUI components in the scheme of the
 * mode of the design system (see {@link withModeSelector}), as the TopLogic components are. A
 * theme with a single scheme renders them in that scheme in every mode.</p>
 */
export function createPageThemes(options: ThemeOptions): PageThemes {
  const themeOptions = withModeSelector(options);
  return {
    de: createTheme(themeOptions, pickersDeDE, coreDeDE),
    en: createTheme(themeOptions, pickersEnUS, coreEnUS),
  };
}

/** The mode of the design system: the `data-tl-mode` of `<html>`. */
function pageMode(): SchemeName {
  return document.documentElement.getAttribute(MODE_ATTRIBUTE) === 'dark' ? 'dark' : DEFAULT_MODE;
}

/** The functions called when the mode of the design system changes. */
const modeListeners = new Set<() => void>();

/** The observer of the mode attribute of `<html>`, while there are {@link modeListeners}. */
let modeObserver: MutationObserver | null = null;

/**
 * Calls the given function whenever the mode of the design system changes: the UI theme in effect
 * changes it without a page reload when the user selects another theme, or when the operating
 * system changes its appearance while the page follows it. All subscribers share one observer.
 *
 * @returns The function ending the subscription.
 */
function subscribeMode(listener: () => void): () => void {
  modeListeners.add(listener);
  if (modeObserver === null) {
    modeObserver = new MutationObserver(() => modeListeners.forEach(notify => notify()));
    modeObserver.observe(document.documentElement, { attributes: true, attributeFilter: [MODE_ATTRIBUTE] });
  }
  return () => {
    modeListeners.delete(listener);
    if (modeListeners.size === 0) {
      modeObserver?.disconnect();
      modeObserver = null;
    }
  };
}

/**
 * Keeps the mode of the MUI color scheme context (`useColorScheme`) at the mode of the design
 * system. Rendered below the `ThemeProvider` of a theme with a light and a dark scheme.
 */
function ModeSync(): null {
  const mode = React.useSyncExternalStore(subscribeMode, pageMode);
  const { setMode } = useColorScheme();
  React.useEffect(() => {
    setMode(mode);
  }, [mode, setMode]);
  return null;
}

/** A theme with CSS custom properties: its color schemes. */
type ThemeWithSchemes = Theme & { colorSchemes?: Partial<Record<SchemeName, unknown>> };

/** Whether the given theme has a light and a dark color scheme. */
function hasBothSchemes(theme: Theme): boolean {
  const schemes = (theme as ThemeWithSchemes).colorSchemes;
  return schemes?.light !== undefined && schemes?.dark !== undefined;
}

/**
 * The `ThemeProvider` of Material UI with the props it takes for a theme with CSS custom properties
 * (the themes of {@link createPageThemes}): its types offer them only to an application that
 * declares all its themes to have CSS custom properties.
 */
const VarsThemeProvider = ThemeProvider as React.ComponentType<
  ThemeProviderProps<Theme> & { colorSchemeNode?: Element | null }
>;

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
 * <p>The cache puts every rule of Material UI into the CSS cascade layer {@link CSS_LAYER} (see
 * {@link createLayeredCache}).</p>
 *
 * <p>The wrapper renders providers only, no element of its own: it sits above the content of every
 * React root, and an element there would break the fill layout of the TopLogic components (see
 * `useFill` of 'tl-react-bridge'). For the same reason it uses no `ScopedCssBaseline`, which renders
 * an element, and no global `CssBaseline`, whose reset collides with the TopLogic stylesheets: the
 * MUI components render without the MUI baseline.</p>
 */
export function createMuiRoot(themes: PageThemes): React.ComponentType<{ children?: React.ReactNode }> {
  return function MuiRoot({ children }: { children?: React.ReactNode }) {
    const theme = pageTheme(themes);
    const twoSchemes = hasBothSchemes(theme);
    return (
      <CacheProvider value={muiCache}>
        <VarsThemeProvider theme={theme} colorSchemeNode={null} storageManager={null} defaultMode={pageMode()}>
          {twoSchemes && <ModeSync />}
          <LocalizationProvider dateAdapter={AdapterDayjs} adapterLocale={pageLocale(themes)}>
            {children}
          </LocalizationProvider>
        </VarsThemeProvider>
      </CacheProvider>
    );
  };
}
