# FAQ: A TopLogic React application in a customer's Material UI theme

A customer brings an existing [Material UI](https://mui.com/) theme — a `createTheme(...)` call of
its own design system — and expects the application built with the view layer (`.view.xml`) to
look like a MUI application in that theme. On top of that, the customer often wants a few
components of its own: a button in the corporate shape, a key-figure tile, a specialized widget.

This article walks through both:

1. [Part 1](#part-1-the-customer-theme-module): a small module of the application that holds the
   customer's theme and installs Material UI with it;
2. [Part 2](#part-2-customer-specific-components): restyled, replaced and additional components,
   all rendered with the same Material UI and the same theme.

The building blocks are the Material UI module `com.top_logic.layout.react.mui` (artifact
`tl-layout-react-mui`, import specifier `tl-react-mui`) and the bridge `tl-react-bridge`. The
mechanism behind them — root wrappers, `replace`, the state contract `state.proto` — is described in
[customer-component-library.md](customer-component-library.md); this article is the recipe for the
Material UI case. The runnable reference is `com.top_logic.demo.react.mui` (artifact
`tl-demo-react-mui`), a module in exactly the shape of a customer project; the excerpts below are
taken from it.

## How it fits together

```
acme-theme bundle            customerTheme.ts, fonts, entry: installMui({ theme, replace })
  (the application's)        customer components (optional)
        │  imports Material UI from 'tl-react-mui', React from 'tl-react-bridge'
        ▼
tl-react-mui bundle          all of @mui/material and @mui/x-date-pickers (re-exported),
  (tl-layout-react-mui)      the adapters, installMui, the root wrapper, the derived properties
        │  imports React from 'tl-react-bridge'
        ▼
tl-react-bridge bundle       React, the component registry (register / replace), the control hooks
  (tl-layout-react)
```

- **The customer's theme is the single source of the look.** `installMui` creates the MUI theme
  from the customer's `ThemeOptions` unchanged; the MUI components render with it.
- **The TopLogic components that are not replaced follow the theme.** Tables, trees, panels, the
  sidebar and toolbars read CSS custom properties that `installMui` derives from the MUI theme and
  writes into the page ([Following the MUI theme](customer-component-library.md#following-the-mui-theme)).
- **Which components are MUI is a free choice**: all 25 replaceable components, or a selection.
- **There is exactly one Material UI on the page**, the one in `tl-react-mui`. The theme and every
  customer component import Material UI from there, so they share the theme context, the emotion
  style cache and the date localization of the root wrapper with the adapters.

## Part 1: the customer theme module

The module needs no Java code. In the following, it is called `acme.theme` (artifact `acme-theme`,
bundle `acme-theme.js`); replace the names by your own.

```
acme.theme/
├── pom.xml                         dependency on tl-layout-react-mui, frontend-maven-plugin
├── package.json                    fonts; no Material UI, no React
├── tsconfig.json                   paths for the types of tl-react-bridge and tl-react-mui
├── vite.config.ts                  library build, Material UI and the bridge external
├── react-src/
│   ├── customerTheme.ts            the customer's ThemeOptions and its fonts
│   └── acme-theme-entry.ts         installMui({ theme, replace })
└── src/main/
    ├── java/META-INF/web-fragment.xml
    └── webapp/WEB-INF/conf/
        ├── metaConf.txt            acme-theme.conf.config.xml
        └── acme-theme.conf.config.xml      ClientResources: bundle + font stylesheet
```

### 1. `pom.xml`

A module like any React module ([new-react-module.md](new-react-module.md), step 5), with a
dependency on `tl-layout-react-mui` (`com.top_logic.demo.react.mui/pom.xml`, abbreviated):

```xml
<artifactId>tl-demo-react-mui</artifactId>

<dependencies>
  <dependency>
    <groupId>com.top-logic</groupId>
    <artifactId>tl-layout-react-mui</artifactId>
  </dependency>
</dependencies>

<build>
  <plugins>
    <plugin>
      <groupId>com.github.eirslett</groupId>
      <artifactId>frontend-maven-plugin</artifactId>
      <executions>
        <execution>
          <id>install-node</id>
          <goals><goal>install-node-and-npm</goal></goals>
        </execution>
        <execution>
          <id>npm-install</id>
          <goals><goal>npm</goal></goals>
          <configuration><arguments>install</arguments></configuration>
        </execution>
        <execution>
          <id>npm-build</id>
          <phase>generate-resources</phase>
          <goals><goal>npm</goal></goals>
          <configuration><arguments>run build</arguments></configuration>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

The version and the Node version of the `frontend-maven-plugin` come from the plugin management of
the TopLogic parent POMs (`tl-parent-all`); the version of `tl-layout-react-mui` from its
dependency management (`${tl.version}`). Inside the engine reactor, the demo uses the parent
`tl-parent-core-internal`, is listed in `tl-parent-engine/pom.xml` and has a managed version in the
root `pom.xml`; an application project uses its usual parent and module list. Everything else a
module needs is in [new-module-checklist.md](new-module-checklist.md).

### 2. `package.json`

Only what the theme brings along — here its fonts. Material UI, emotion and React are **not**
dependencies: they come from `tl-react-mui` and `tl-react-bridge` at run time
(`com.top_logic.demo.react.mui/package.json`):

```json
{
  "name": "tl-demo-react-mui",
  "version": "7.11.0",
  "private": true,
  "scripts": {
    "build": "vite build"
  },
  "dependencies": {
    "@fontsource/roboto-condensed": "^5.3.0",
    "@fontsource/work-sans": "^5.3.0"
  },
  "devDependencies": {
    "typescript": "^5.7.0",
    "vite": "^6.0.0"
  }
}
```

A module with components of its own (Part 2) adds `@types/react` and `@vitejs/plugin-react`, see
[Building JSX](#building-jsx).

### 3. `tsconfig.json`

The usual compiler options of a React module ([new-react-module.md](new-react-module.md), step 2)
with one `paths` entry per bundle the module imports:

```json
"paths": {
  "tl-react-bridge": ["../com.top_logic.layout.react/react-src/bridge-entry.ts"],
  "tl-react-mui": ["../com.top_logic.layout.react.mui/react-src/mui-entry.ts"]
}
```

The paths serve the type checker (`tsc --noEmit`) and the IDE. The vite build does not check
types and does not need them: both bundles are external. The TopLogic jars contain no TypeScript
declarations, so a project outside the engine checkout points the paths to a checkout of the
engine sources of its TopLogic version.

### 4. `vite.config.ts`

A library build with `tl-react-bridge` and `tl-react-mui` external, plus the plugin
`sharedMaterialUi()`, which keeps a second Material UI out of the bundle
(`com.top_logic.demo.react.mui/vite.config.ts`):

```ts
/** The import specifier of Material UI on the page: the bundle of tl-layout-react-mui. */
const TL_REACT_MUI = 'tl-react-mui';

/** The entry points of Material UI whose exports tl-react-mui re-exports. */
const MUI_ENTRY_POINTS = ['@mui/material', '@mui/material/styles', '@mui/x-date-pickers'];

/** The packages of Material UI and its style engine, which must not be bundled a second time. */
const MUI_PACKAGES = /^@(mui|emotion)\//;

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
```

Why the plugin: a second copy of Material UI in the bundle would bring a theme context and an
emotion style cache of its own. Components from that copy would not see the theme of the root
wrapper, and their styles would land in another cache. The plugin therefore

- resolves the three entry points `tl-react-mui` re-exports (`@mui/material`,
  `@mui/material/styles`, `@mui/x-date-pickers`) to `tl-react-mui` — so even a third-party library
  that imports `@mui/material` shares the page's Material UI;
- fails the build for every other `@mui/…` or `@emotion/…` import, e.g. `@mui/material/Button`.

The module's own code imports Material UI from `tl-react-mui` directly.

### 5. The theme

Port the customer's theme to a `ThemeOptions` object — what the customer passes to `createTheme`.
Functions are allowed (e.g. a `typography` computed from the palette), and `createTheme` is not
called by the module: `installMui` does that. Colors and types are imported from `tl-react-mui`
(`com.top_logic.demo.react.mui/react-src/customerTheme.ts`, abbreviated):

```ts
import { colors } from 'tl-react-mui';
import type { ThemeOptions } from 'tl-react-mui';
import '@fontsource/work-sans/latin-300.css';
import '@fontsource/work-sans/latin-400.css';
import '@fontsource/work-sans/latin-700.css';
import '@fontsource/roboto-condensed/latin-700.css';

const FONT_FAMILY = "'Work Sans', sans-serif";
const FONT_FAMILY_SECONDARY = "'Roboto Condensed', sans-serif";

const customerTheme: ThemeOptions = {
  colorSchemes: {
    light: {
      palette: {
        primary: { light: '#69696a', main: '#28282a', dark: '#1e1e1f' },
        secondary: { light: '#fff5f8', main: '#ff3366', dark: '#e62958' },
        error: { main: colors.red[500], dark: colors.red[700] },
        // ...
      },
    },
    dark: {
      palette: {
        primary: { light: '#f5f5f5', main: '#e6e6e6', dark: '#bdbdbe', contrastText: '#28282a' },
        secondary: { light: '#ff6690', main: '#ff3366', dark: '#e62958' },
        background: { default: '#1e1e1f', paper: '#28282a' },
        // ...
      },
    },
  },
  typography: {
    fontFamily: FONT_FAMILY,
    fontSize: 14,
    h6: { fontWeight: 700, fontFamily: FONT_FAMILY_SECONDARY, color: 'var(--mui-palette-text-primary)', fontSize: 18 },
    body2: { fontSize: 14, lineHeight: 1.5, letterSpacing: '0.00938em' },
    // ...
  },
  components: {
    MuiButton: {
      defaultProps: { disableElevation: true },
      styleOverrides: { root: { borderRadius: 0, fontWeight: 700, fontFamily: FONT_FAMILY_SECONDARY } },
    },
    MuiAppBar: { defaultProps: { elevation: 0 } },
  },
};

export default customerTheme;
```

When porting:

- **Drop keys outside the MUI theme.** A customer theme often carries keys of its own (the demo's
  template had `xLight`, `background.placeholder`, `typography.fontHeader`); fold what they mean
  into real theme keys (here: the heading variants) or leave them out.
- **Keep the customer's component overrides** under `components` — they apply to every component
  the adapters render (see [Restyling within the theme](#a-restyling-within-the-theme)).
- **`cssVariables`.** `installMui` creates the theme with CSS variables itself
  (`createPageThemes` in `react-src/MuiRoot.tsx`), once per page language (German, English) with
  the MUI texts and date-picker texts of that language. Options of a `cssVariables` object (e.g.
  `cssVarPrefix`) are kept; its `colorSchemeSelector` is replaced by the mode of the design system
  (see the next point).
- **Light and dark.** A theme may define `colorSchemes: { light: …, dark: … }`. Both component
  families follow the mode of the UI theme in effect (`data-tl-mode` on `<html>`, written by the
  `UIThemeService`): the MUI components through the selector `[data-tl-mode="%s"]` of the theme's
  CSS variables, the TopLogic components through the styling properties derived per scheme. The
  mode changes with the user's choice of a UI theme and, for the UI theme following the operating
  system, with the system's preference, without a page reload.
  A theme with one scheme (a plain `palette`) holds the whole page in the mode of that scheme:
  `installMui` calls `lockMode(<scheme>)` of `tl-react-bridge`, and the page's theme script keeps
  `data-tl-mode` at that mode whatever UI theme is selected. A selected UI theme of the other mode
  is represented meanwhile by the UI theme of the held mode (the one answering the operating
  system's preference for it), so every styling property — the derived ones, the roles of the
  design system the theme does not set (e.g. the category colors of an avatar) and the tokens of
  the UI themes — is in the theme's scheme, and nothing on the page turns half dark. The user's
  selection is kept: the user menu still shows it, and it takes effect again on a page without
  that lock.
  In a theme with both schemes, a color computed from the palette in the options
  (`typography: palette => ({ h6: { color: palette.text.primary } })`) is computed once, from the
  light palette, and stays so in the dark mode. Refer to the CSS variable instead
  (`color: 'var(--mui-palette-text-primary)'`, or `theme.vars.palette.text.primary` in a
  `styleOverrides` function), which switches with the scheme.

### 6. Fonts

Bundle the customer's fonts with the module instead of loading them from a font CDN: install the
[Fontsource](https://fontsource.org/) packages (`@fontsource/<family>`) and import the CSS of the
weights and subsets the theme uses, as `customerTheme.ts` above does. Library mode writes the
imported CSS — the font files inlined into it — into one stylesheet next to the bundle
(`cssFileName`), which the module announces as a client resource (step 8). Check the license of
each font; the demo's fonts are under the SIL Open Font License.

### 7. The entry: `installMui`

The entry calls `installMui` once, when the bundle loads
(`com.top_logic.demo.react.mui/react-src/demo-mui-entry.ts`):

```ts
import { installMui } from 'tl-react-mui';
import customerTheme from './customerTheme';

installMui({ theme: customerTheme, replace: 'all' });
```

`installMui(options: MuiOptions)` (`com.top_logic.layout.react.mui/react-src/install.ts`):

1. creates the MUI themes from `options.theme`,
2. writes the styling properties derived from the theme into the page
   (`<style id="tl-mui-theme-properties">` at the end of `<head>`),
3. registers the root wrapper (emotion cache, `ThemeProvider`, the date pickers'
   `LocalizationProvider`) above every React root of the bridge,
4. replaces the selected TopLogic components by their MUI adapters (`replace` of the bridge).

The bundle is loaded before the page's controls are mounted, so all of this is in place for the
first render.

### 8. Client resources and web fragment

`WEB-INF/conf/metaConf.txt` lists the module's configuration file, which announces the bundle and
its font stylesheet (`com.top_logic.demo.react.mui/src/main/webapp/WEB-INF/conf/tl-demo-react-mui.conf.config.xml`):

```xml
<config service-class="com.top_logic.layout.react.resource.ClientResources">
	<instance class="com.top_logic.layout.react.resource.ClientResources">
		<resources>
			<module-script name="tl-demo-react-mui"
				requires="tl-react-mui"
				resource="/script/tl-demo-react-mui.js"
			/>
			<stylesheet name="tl-demo-react-mui-css"
				resource="/script/tl-demo-react-mui.css"
			/>
		</resources>
	</instance>
</config>
```

`requires="tl-react-mui"` orders the bundle after the Material UI bundle (which itself requires
`tl-react-bridge`); `tl-react-mui` is registered with `specifier="tl-react-mui"`, so the import
map of the page resolves the bare import `'tl-react-mui'` to it. The theme bundle needs no
`specifier` of its own, since nothing imports it.

A `web-fragment.xml` in `src/main/java/META-INF/` makes the webapp resources of the module part of
the application. The `.gitignore` ignores `/node/`, `/node_modules/` and the two build products
(`src/main/webapp/script/<bundle>.js` and `.css`).

### 9. Wiring it into the application

The application module depends on the theme module; that is all. The demo makes it switchable with
the Maven profile `mui` of `com.top_logic.demo.react/pom.xml`:

```xml
<profile>
  <id>mui</id>
  <dependencies>
    <dependency>
      <groupId>com.top-logic</groupId>
      <artifactId>tl-demo-react-mui</artifactId>
    </dependency>
  </dependencies>
</profile>
```

A customer application usually declares the dependency unconditionally. Without the theme module,
`tl-layout-react-mui` alone changes nothing on the page: loading its bundle does not install
anything.

### Choosing the MUI components

`replace` selects the TopLogic components rendered with Material UI:

- `'all'` (`ALL_COMPONENTS`, the default) — every one of `COMPONENT_NAMES`, the 25 components of
  the state contract listed in
  [The replaceable components](customer-component-library.md#the-replaceable-components);
- a list of names (typed as the union `ComponentName`), e.g.
  `replace: ['TLButton', 'TLCheckbox', 'TLTextInput']` — the other components keep their TopLogic
  rendering and follow the theme through the derived properties.

`installMui` throws for a name without adapter (the message lists the known names) and on a second
call: the installation is made once per page and cannot be changed afterwards. `COMPONENT_NAMES`
is exported, so a selection can also be written as "all but":
`COMPONENT_NAMES.filter(name => name !== 'TLDatePicker')`.

### What of the theme reaches the TopLogic components

The derived properties cover the palette (brand, text, surfaces, actions, status colors, divider),
the typography (font families, the sizes and line heights of `body2`, `caption`, `subtitle1/2`,
`h3`–`h6`), `shape.borderRadius`, four elevations of `shadows` and the heights of the small MUI
controls. The complete mapping is the table in
[Following the MUI theme](customer-component-library.md#following-the-mui-theme); the
computation is `com.top_logic.layout.react.mui/react-src/themeProperties.ts`.

#### Limits

- **Some values do not reach the TopLogic components**: the table's row height (36px, computed with
  by its scrolling), header and bar heights, the tree's row height and font size are literal in the
  TopLogic stylesheets; the spacing scale (`theme.spacing`) is not mapped. Component overrides
  under `components` affect only components MUI renders.
- **No contrast correction.** The theme's values are used as they are; a palette with poor
  contrast stays so on the TopLogic surfaces as well.
## Part 2: customer-specific components

Three levels, from cheapest to most work. Prefer the first that does the job.

### A. Restyling within the theme

The first choice is the theme itself: `components.MuiX.styleOverrides`, `defaultProps` and
`variants`. They apply to every TopLogic component rendered by that MUI component — a
`MuiButton` override restyles the buttons of toolbars, dialogs and forms alike — and to every
customer component built from it:

```ts
import type { ThemeOptions } from 'tl-react-mui';

const acmeTheme: ThemeOptions = {
  palette: { primary: { main: '#00508c' } },
  shape: { borderRadius: 2 },
  components: {
    MuiButton: {
      defaultProps: {
        disableElevation: true,
      },
      styleOverrides: {
        root: ({ theme }) => ({
          fontWeight: theme.typography.fontWeightBold,
          variants: [
            {
              props: { variant: 'contained', color: 'primary' },
              style: { boxShadow: `inset 0 -2px 0 ${theme.palette.primary.dark}` },
            },
          ],
        }),
      },
    },
    MuiOutlinedInput: {
      styleOverrides: {
        notchedOutline: { borderWidth: 2 },
      },
    },
  },
};
```

Two things to know about the adapters:

- **An adapter sets many props itself**, mapped from the control state — the button adapter sets
  `variant`, `color` and `size`, for instance. A `defaultProps` entry for such a prop has no effect;
  restyle through `styleOverrides` (with `variants` matching the props the adapter sets) instead.
  `defaultProps` work for props the adapter leaves open (`disableElevation`, `elevation` of the app
  bar). The mapping of each adapter is documented on the adapter
  (`com.top_logic.layout.react.mui/react-src/adapters/Mui*Adapter.tsx`).
- **A custom variant name** (`variant: 'dashed'`) is never selected by an adapter, since the
  adapters map the server state to MUI's own variants. A different look for one particular
  control is a `css-class` in the view ([react-view-layer.md](react-view-layer.md#styling-a-single-element-css-class));
  the adapters put it on their root element (`rootClassName`).

### B. Replacing a TopLogic component by a customer adapter

If the customer has a component of its own for a TopLogic component — say a corporate button built
with `styled(Button)` — the module renders that TopLogic component with it. The customer adapter
takes the place of the MUI adapter for that name.

#### Building JSX

A module with components of its own compiles JSX. It adds two development dependencies and the
React plugin of vite in the classic runtime, which turns JSX into `React.createElement` calls on
the `React` the component imports from `tl-react-bridge`:

```json
"devDependencies": {
  "@types/react": "^19.0.0",
  "@vitejs/plugin-react": "^4.3.0",
  "typescript": "^5.7.0",
  "vite": "^6.0.0"
}
```

```ts
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [sharedMaterialUi(), react({ jsxRuntime: 'classic' })],
  // build: as in step 4
});
```

The bundle then imports from nothing but `tl-react-bridge` and `tl-react-mui`. The React shims of
[new-react-module.md](new-react-module.md) are needed only if the module bundles a third-party
library that imports `react` itself.

#### The adapter

An adapter is a component of the bridge (`TLCellProps`) that reads the control state with
`useTLState<Partial<XStateJson>>()` and sends the commands of the state contract with
`useTLCommand()`:

```tsx
import { React, useTLState, useTLCommand, useButtonDefaults, rootClassName, ThemeIcon } from 'tl-react-bridge';
import type { TLCellProps, ButtonStateJson } from 'tl-react-bridge';
import { Button, styled } from 'tl-react-mui';

/** The command a button sends when it is clicked (ButtonState in state.proto). */
const CMD_CLICK = 'click';

/** The customer's button: a MUI button with the pill shape of the corporate design. */
const PillButton = styled(Button)(({ theme }) => ({
  borderRadius: 999,
  paddingInline: theme.spacing(2),
}));

/** Renders the state of a TopLogic button (`TLButton`) with the customer's button. */
const AcmeButton: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<ButtonStateJson>>();
  const sendCommand = useTLCommand();
  const defaults = useButtonDefaults();
  if (state.hidden === true) {
    return null;
  }
  const appearance = (state.appearance === 'default' ? undefined : state.appearance) ?? defaults.appearance;
  return (
    <PillButton
      id={controlId}
      disabled={state.disabled === true}
      variant={appearance === 'primary' ? 'contained' : 'outlined'}
      color={state.tone === 'danger' ? 'error' : 'primary'}
      startIcon={state.image ? <ThemeIcon encoded={state.image} className="tl-icon-sm" /> : undefined}
      className={rootClassName(state, state.cssClasses)}
      onClick={() => sendCommand(CMD_CLICK)}
    >
      {state.label}
    </PillButton>
  );
};

export default AcmeButton;
```

`styled`, `Button` and every other MUI export come from `tl-react-mui`, so the styled button is
part of the page's Material UI: it sees the theme of the root wrapper, and the theme's `MuiButton`
overrides apply to it underneath the styles of `styled`.

The sketch maps only part of the contract. A replacement renders the component *everywhere* —
also as a menu entry, as an icon-only toolbar button, as a link — so a production adapter covers
the whole message `ButtonState` of `state.proto`: `displayMode` and the container's `iconOnly`,
the `menu-item` appearance with `menuItemProps`, `tooltip`, `keyGesture` (`useKeyboardBinding`),
`navigateUrl`, `active`, `size`. `MuiButtonAdapter.tsx` is the complete mapping to start from.
The state contract and the helpers of the bridge (`useTLFieldValue` for fields, `useFormLayout`,
`useButtonDefaults`, `ThemeIcon`, `rootClassName`) are described in
[customer-component-library.md](customer-component-library.md#level-2-adapter-components-for-leaf-widgets).

#### Registering it

The entry leaves the name out of the selection of `installMui` and replaces it itself:

```ts
import { replace } from 'tl-react-bridge';
import { installMui, COMPONENT_NAMES } from 'tl-react-mui';
import customerTheme from './customerTheme';
import AcmeButton from './AcmeButton';

installMui({ theme: customerTheme, replace: COMPONENT_NAMES.filter(name => name !== 'TLButton') });
replace('TLButton', AcmeButton);
```

The rule of the bridge (`registry.ts`): a `replace` wins over the component TopLogic `register`s
under the name, whatever the load order; of two `replace` calls for the same name, the later one
wins and the bridge logs the console warning "Component replaced twice". Calling `replace` after
`installMui({ replace: 'all' })` therefore works too, but leaving the name out of the selection
states the intent and keeps the console clean.

### C. A specialized component of its own

A widget TopLogic has no counterpart for (a key-figure tile, a stepper, a domain-specific
visualization) becomes an additional view element: a `UIElement` with its `Config` and tag, a
`ReactControl` that publishes the state, and a client component. The server side is exactly as in
[new-ui-element.md](new-ui-element.md) (steps 1, 2, 4–6); the control names the client component
in its constructor:

```java
super(context, null, "AcmeKpiTile");
```

The client component lives in the theme module (or a module of its own built the same way: the
`sharedMaterialUi()` plugin, the JSX build, `requires="tl-react-mui"`) and is written with Material
UI from `tl-react-mui`, so it renders in the customer's theme:

```tsx
import { React, useTLState, useTLCommand, rootClassName } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { Card, CardActionArea, CardContent, Typography } from 'tl-react-mui';

/** State key of the caption (published by the server with putState). */
const LABEL = 'label';

/** State key of the figure. */
const VALUE = 'value';

/** The command the tile sends when it is clicked. */
const CMD_OPEN = 'open';

/** A key figure as a MUI card, in the theme of the page. */
const AcmeKpiTile: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  return (
    <Card id={controlId} className={rootClassName(state, 'acmeKpiTile')}>
      <CardActionArea onClick={() => sendCommand(CMD_OPEN)}>
        <CardContent>
          <Typography variant="caption" color="textSecondary">{state[LABEL] as string}</Typography>
          <Typography variant="h4">{state[VALUE] as string}</Typography>
        </CardContent>
      </CardActionArea>
    </Card>
  );
};

export default AcmeKpiTile;
```

The entry registers it under the name the control mounts:

```ts
import { register } from 'tl-react-bridge';
import AcmeKpiTile from './AcmeKpiTile';

register('AcmeKpiTile', AcmeKpiTile);
```

The state keys and command names are the contract with the `ReactControl` (`putState(LABEL, …)`,
`@ReactCommandHandler(CMD_OPEN)`); keep them as constants on both sides. Make the element generic
and parameterized by configuration, not tailored to one view
([customer-component-library.md](customer-component-library.md#level-3-new-uielements-only-for-widgets-without-a-toplogic-counterpart)),
and check [react-view-layer.md](react-view-layer.md) first: a composition of existing elements
needs no component at all.

## Checklist and pitfalls

- **React only from `tl-react-bridge`.** `import { React } from 'tl-react-bridge'`, never from
  `react`; a second React fails at run time with "useState is null".
- **Material UI only from `tl-react-mui`.** Never add `@mui/*` or `@emotion/*` to the module's
  `package.json` and never import a deep MUI path (`@mui/material/Button`); `sharedMaterialUi()`
  fails the build for it. Emotion is not re-exported: write styles with MUI's `styled`, `sx` or
  `css`.
- **One `installMui` per page**, in the entry of the application's theme module, with a name list
  that only contains `COMPONENT_NAMES`.
- **After adding the module** to the reactor (module list, dependency management), run
  `.claude/scripts/rebuild-stale.sh` in the engine checkout: it reinstalls the changed parent POMs
  and builds the module without a jar.
- **A running application serves the bundles of its dependency modules from the jars it opened at
  start-up**, and the scripts from the exploded overlay of the application module
  (`target/<app>-app/script/`). After a change of the client code: build the module, build the
  application module, restart the application. Verify with
  `curl <app>/script/<bundle>.js | grep <a string of the change>`.
- **The bundles are build products** (ignored by git): a fresh checkout or a branch switch needs a
  build of the React modules before the application serves current client code.
- **Reference**: `tl-demo-react` with the profile `mui` renders everything with Material UI in the
  demo's theme:

  ```bash
  mvn -B install -pl com.top_logic.layout.react.mui,com.top_logic.demo.react.mui,com.top_logic.demo.react -P mui
  MAVEN_ARGS=-Pmui   # start the app with the same profile, see demo-apps.md
  ```

  Compare the customer application against it when something looks off; the adapters' wire tests
  run with `npm test` in `com.top_logic.layout.react.mui`.
