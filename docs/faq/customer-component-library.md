# FAQ: Rendering the React UI with a customer's component library

A customer often brings a React component library of its own that defines the corporate design,
stylesheets included. Such a library knows nothing about TopLogic: it keeps no server state and binds
no data. This article describes how to attach it so that the applications built with the view layer
(`.view.xml`) appear in the customer's design — and which extension points of `tl-react-bridge` serve
that purpose.

A runnable reference is the Material UI module `com.top_logic.layout.react.mui`, which renders
every replaceable component with Material UI, and the demo project `com.top_logic.demo.react.mui`,
which installs it with a theme of its own the way an application does (see
[The Material UI module](#the-material-ui-module)). The excerpts below are taken from them.

## Principle

The customer library is attached as a **rendering layer below the component contract**, not as a
second set of building blocks beside it. Views, `UIElement`s, `ReactControl`s, data binding and state
synchronization stay as they are. What changes is the React component that renders the state of a
control, per component name, plus the theme tokens. The corporate design becomes a matter of
deployment: the same views appear in the customer's design.

The seam between server and client consists of exactly three things:

- the **component name** — the third constructor argument of `ReactControl`, e.g. `"TLButton"`;
- the **state keys** the control publishes with `putState` and the component reads with
  `useTLState()`;
- the **commands** the component sends back with `useTLCommand()` (e.g. `click`, `valueChanged`).

The adaptation has three levels. Level 1 always applies, level 2 covers the leaf widgets the library
has a counterpart for, level 3 is the exception.

## Level 1: a theme from the customer's design tokens

Map the customer's tokens (brand colours, fonts, radii, shadows) onto a theme of the
`UIThemeService` that extends the default one and overrides only what differs:

```xml
<config service-class="com.top_logic.layout.react.theme.UIThemeService">
	<instance class="com.top_logic.layout.react.theme.UIThemeService"
		default-theme="acme"
	>
		<themes>
			<theme name="acme"
				extends="default"
			>
				<label>
					<en>ACME</en>
					<de>ACME</de>
				</label>
				<length name="corner-radius" value="0"/>
				...
			</theme>
		</themes>
	</instance>
</config>
```

The shipped themes are in `com.top_logic.layout.react/src/main/webapp/WEB-INF/conf/tl-react-theme.config.xml`;
the tokens, their tiers and the audit test are described in
[react-theme-tokens.md](react-theme-tokens.md). Where possible, let the customer's CSS variables and
the TopLogic tokens refer to the same source.

This level alone covers a large part of the appearance, in particular of the complex components that
are not replaced (tables, trees, flow diagrams, layout panels).

When the customer's design is itself a theme of the component library — a Material UI theme, for
instance — that theme is the source, and the TopLogic properties can be derived from it at run time
instead of being copied into a theme of the `UIThemeService`; the Material UI module does so (see
[Following the MUI theme](#following-the-mui-theme)).

## Level 2: adapter components for leaf widgets

For every TopLogic component the library has a counterpart for, an adapter module contains a small
adapter: it maps the control state to the props of the library component and the library's
callbacks to TopLogic commands.

### The module

The adapter module is a React module as described in [new-react-module.md](new-react-module.md)
(`package.json`, `tsconfig.json`, `vite.config.ts`, `pom.xml` with the `frontend-maven-plugin`). Its
bundle is announced as a client resource in a configuration file listed in
`WEB-INF/conf/metaConf.txt` (`com.top_logic.layout.react.mui/src/main/webapp/WEB-INF/conf/tl-layout-react-mui.conf.config.xml`):

```xml
<config service-class="com.top_logic.layout.react.resource.ClientResources">
	<instance class="com.top_logic.layout.react.resource.ClientResources">
		<resources>
			<module-script name="tl-react-mui"
				requires="tl-react-bridge"
				resource="/script/tl-react-mui.js"
				specifier="tl-react-mui"
			/>
		</resources>
	</instance>
</config>
```

The `specifier` puts the bundle into the import map of the page, so that the module of the
application imports it by name (`import { installMui } from 'tl-react-mui'`); the page loads it once,
under the URL of the script tag. The file is written by the vite build (`build.lib` with `fileName`,
`outDir` `src/main/webapp/script`). A bundle that imports CSS gets a stylesheet as well
(`cssFileName`), announced as a `<stylesheet>` — for Material UI, whose styles are CSS-in-JS, only the
application's fonts, which its own module brings.

The module needs no Java code; a `web-fragment.xml` in `src/main/java/META-INF/` makes its webapp
resources part of the application.

### Installation: root wrappers and replacements

The adapter module registers its root wrapper and its replacements in one call, which the
application makes with its theme before the controls of the page are mounted
(`com.top_logic.layout.react.mui/react-src/install.ts`, shortened):

```ts
import { registerRootWrapper, replace } from 'tl-react-bridge';

const ADAPTERS = {
  TLButton: MuiButtonAdapter,
  TLCheckbox: MuiCheckboxAdapter,
  // ... one adapter per component of the state contract
} as const satisfies Record<string, React.ComponentType<TLCellProps>>;

export function installMui(options: MuiOptions): void {
  // ... checks: installed once, known component names
  const themes = createPageThemes(options.theme);
  installThemeProperties(pageTheme(themes));
  registerRootWrapper(createMuiRoot(themes));
  for (const name of names) {         // the selection of options.replace
    replace(name, ADAPTERS[name]);
  }
}
```

The root wrapper puts the library's providers above every React root and renders no element of its
own (`react-src/MuiRoot.tsx`, shortened):

```tsx
const muiCache = createCache({ key: 'mui', container: document.head, prepend: false });

export function createMuiRoot(themes: PageThemes) {
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
```

- **`registerRootWrapper(wrapper, { order? })`** (`bridge/root-wrapper.ts`) puts a component — the
  library's theme provider, intl provider, CSS-in-JS cache — above the content of every React root
  the bridge renders: each control mounted with `mount`, and the tooltip host. Nested controls render
  through `TLChild` inside their parent's tree and portals inherit the context of the tree they are
  created in, so both are covered as well. A wrapper must render its `children`.
- **`replace(name, component)`** (`bridge/registry.ts`) renders the component name with the given
  component instead of the TopLogic one. The replacement receives the same props (`TLCellProps`) and
  runs in the same control context. The compositions of TopLogic obtain their leaf components through
  the registry (`TLChild`), so a replaced button also appears inside toolbars, dialogs and forms.
- **`useFormLayout()`** (`bridge/form-layout.ts`) returns what the form layout (`TLFormLayout`)
  around a control tells its fields through `FormLayoutContext`: whether the form is read-only and
  the label position it resolved for fields that state none. A replacement of `TLFormField` reads
  it to omit the required mark, the messages and the help in a read-only form and to place the
  label as `TLFormField` does. With `FieldLabelContext` (`bridge/field-label.ts`) such a frame
  names its input by the label, as `TLFormField` does.
- **`useButtonDefaults()`** and **`menuItemProps(defaults, checked?)`** (`bridge/button-defaults.ts`)
  tell a replacement of `TLButton` or `TLToggleButton` what the container around it suggests,
  which `<ButtonDefaults appearance=… iconOnly=…>` provides: the appearance (`ghost` in a toolbar
  and an app bar, `secondary` in the button bar of a window), the icon-only presentation of a
  compact toolbar, and `menu-item` inside a menu. A menu entry carries the role and the roving
  tabindex `menuItemProps` returns, by which the menu's keyboard navigation (arrows, Escape) finds
  it. A replacement of a container (`TLAppBar`, `TLWindow`) provides the same defaults to its
  buttons with `ButtonDefaults`.
- **`<ThemeIcon encoded={…}/>`** (`bridge/ThemeIcon.tsx`) renders a theme image from the encoded
  form a control sends in its state (`ButtonState.image`, the icons of menu entries and tabs, …): an
  icon font class, an image file, or nothing for the invisible image. An adapter passes the element
  to the library wherever the library takes an icon.

### An adapter

An adapter reads the typed state with `useTLState<Partial<XStateJson>>()` — the state types are exported by
`tl-react-bridge` — and sends commands with `useTLCommand()` (shortened from
`react-src/adapters/MuiButtonAdapter.tsx`, which in addition renders a menu entry, an icon-only
button and a link):

```tsx
import { React, useTLState, useTLCommand, useKeyboardBinding, rootClassName, ThemeIcon,
  useButtonDefaults } from 'tl-react-bridge';
import type { TLCellProps, ButtonStateJson, ButtonAppearance } from 'tl-react-bridge';
import Button from '@mui/material/Button';

const CMD_CLICK = 'click';

const VARIANTS = { secondary: 'outlined', primary: 'contained', ghost: 'text', link: 'text' } as const;

const MuiButtonAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<ButtonStateJson>>();
  const sendCommand = useTLCommand();
  const defaults = useButtonDefaults();
  const disabled = state.disabled === true;
  const hidden = state.hidden === true;
  // The default appearance of the server is the one the container suggests.
  const serverAppearance = state.appearance === 'default' ? undefined : state.appearance;
  const appearance: ButtonAppearance = serverAppearance ?? defaults.appearance ?? 'secondary';
  const handleClick = () => sendCommand(CMD_CLICK);   // the full adapter also honours navigateUrl

  // A hidden or disabled button declines the gesture, so it falls through to an outer binding.
  useKeyboardBinding(state.keyGesture, () => {
    if (disabled || hidden) return false;
    handleClick();
    return true;
  });

  if (hidden) return null;
  const showIcon = !!state.image && (state.displayMode ?? 'label-only') !== 'label-only';
  return (
    <Button id={controlId} disabled={disabled} onClick={handleClick}
      variant={VARIANTS[appearance]}
      color={state.tone === 'danger' ? 'error' : 'primary'}
      startIcon={showIcon ? <ThemeIcon encoded={state.image!} className="tl-icon-sm" /> : undefined}
      aria-label={showIcon ? state.label : undefined}   // the full adapter also sets the tooltip
      aria-pressed={state.active === true ? true : undefined}
      className={rootClassName(state, state.cssClasses)}>
      {state.label}
    </Button>
  );
};
```

Theme images reach an adapter in their encoded form (`state.image`); `ThemeIcon` from
`tl-react-bridge` turns them into an element the library shows as its icon. An icon-only button is
named by its label: the full adapter sets it as `aria-label` and declares it as tooltip through
`TOOLTIP_ATTR` — always for an icon-only button, and with `TOOLTIP_WHEN_ATTR` = `WHEN_TRUNCATED`
(shown only while the label is clipped) otherwise, as `TLButton` does.

A field adapter reads and writes its value through `useTLFieldValue()`, which sends `valueChanged`
and so makes the library component part of the form's edit and save cycle (shortened from
`react-src/adapters/MuiCheckboxAdapter.tsx`, which in addition handles the tri-state and the switch
presentation):

```tsx
const state = useTLState<Partial<CheckboxStateJson>>();
const labelProps = useFieldLabelProps(controlId, controlId);
const [value, setValue] = useTLFieldValue();
const editable = state.editable !== false;
const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
  if (editable) setValue(event.target.checked);
};
if (state.hidden === true) return null;
return <Checkbox id={controlId} checked={value === true} onChange={handleChange}
  readOnly={!editable} disabled={state.disabled === true}
  color={state.hasError === true ? 'error' : fieldColor(state)}
  className={rootClassName(state)}
  slotProps={{ input: { ...labelProps, ...fieldAriaProps(state) } }} />;
```

MUI puts the `id` of a checkbox on its `input`; the label of the surrounding form field refers to it
through `useFieldLabelProps`. `fieldColor` and `fieldAriaProps` are shared helpers of the module
(`react-src/adapters/field.tsx`) mapping the error, warning and mandatory flags of a field.

`rootClassName(state, …classes)` appends the CSS class configured for the control (`cssClass`) to
the component's own classes — use it on the root element of every adapter.

### The replaceable components

The state contract is `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/state/state.proto`,
**the source of truth**: its file header lists the replaceable components, and each message documents
every property, its type, its default (an absent property has its documented default) and the
commands the component sends. The list in the header:

| Component | State message |
|---|---|
| `TLButton` | `ButtonState` |
| `TLToggleButton` | `ToggleButtonState` |
| `TLCheckbox` | `CheckboxState` |
| `TLTextInput` | `TextInputState` |
| `TLPasswordInput` | `PasswordInputState` |
| `TLNumberInput` | `NumberInputState` |
| `TLDatePicker` | `DatePickerState` |
| `TLSelect` | `SelectState` |
| `TLDropdownSelect`, `TLOptionChips`, `TLSegmentedChoice`, `TLChoiceGroup` | `DropdownSelectState` |
| `TLTabBar` | `TabBarState` |
| `TLWindow` | `WindowState` |
| `TLDialog` | `DialogState` |
| `TLMenu` | `MenuState` |
| `TLSnackbar` | `SnackbarState` |
| `TLAlert` | `AlertState` |
| `TLFormField` | `FormFieldState` |
| `TLText` | `TextState` |
| `TLCard` | `CardState` |
| `TLAppBar` | `AppBarState` |
| `TLBreadcrumb` | `BreadcrumbState` |
| `TLProgress` | `ProgressState` |
| `TLSlider` | `SliderState` |

The shared parts are `ControlState` (`hidden`, `cssClass`), `FieldState` (value, `editable`,
`disabled`, `mandatory`, error and warning flags, label, placeholder, …), `TypingFieldState` (debounce, send on
blur) and `ChildControl` (a control embedded in the state of another one, rendered with
`<TLChild control={…}/>`). The TypeScript types are generated into
`com.top_logic.layout.react/react-src/state/control-state.ts`, named after the message with the
suffix `Json` (`ButtonState` → `ButtonStateJson`); enumerations appear there as string unions
(`ButtonStateJson.Appearance` is `'primary' | 'ghost' | 'link'`). The generated types declare every
property as present, but a control sends only the properties that differ from their default — read
the state as `Partial<XStateJson>`.

## Level 3: new UIElements only for widgets without a TopLogic counterpart

If the customer has a widget TopLogic has no counterpart for (a stepper, a KPI tile), build a
`UIElement` + `ReactControl` + component as described in [new-ui-element.md](new-ui-element.md). Make
it generic and parameterized by configuration, not tailored to one view.

## What to avoid

- **No parallel element set** (`<acme-button>`, `<acme-field>`, …). It duplicates the binding and
  synchronization logic, makes views unportable and forces every fix in TopLogic to be repeated. The
  view describes *what* is shown; the adapter module decides *how* it looks.
- **The complex compositions stay TopLogic's.** `TableViewControl`, the fill/layout contract, the
  window manager, drag and drop and the keyboard scopes are styled through tokens and `css-class`,
  not rebuilt on a foreign grid or layout system. That would be a project of its own.

## Requirements on the customer library

- **One React instance.** The library is bundled against the React of `tl-react-bridge`:
  `vite.config.ts` aliases `react`, `react-dom` and `react/jsx-runtime` to shims that re-export from
  `tl-react-bridge` (matching the bare specifiers exactly), and `tl-react-bridge` is `external`. See
  `com.top_logic.layout.react.mui/vite.config.ts` and `react-src/react-*-shim.ts`, or the same
  setup in `com.top_logic.layout.react.wysiwyg` and `com.top_logic.layout.react.chartjs`. The
  library's React peer version must match the bridge's (`react` in
  `com.top_logic.layout.react/package.json`). The `react/jsx-runtime` shim re-exports the automatic
  JSX runtime the bridge exports (`export { jsx, jsxs, Fragment } from 'tl-react-bridge'`), which
  the prebuilt code of a library calls; `React.createElement` is no substitute, since it reads the
  key argument of `jsx(type, props, key)` as children. The `react` and `react-dom` shims name the
  complete public API of React and react-dom, since a library reads it from a namespace import,
  partly under computed names (see `com.top_logic.layout.react.mui/react-src/react-shim.ts` and
  [new-react-module.md](new-react-module.md)).
- **Controlled components only.** The state belongs to the server. A component that keeps state of
  its own (an uncontrolled input, a self-managed open or selected flag) is used in its controlled mode
  (`value` + `onChange`, `open` + `onOpenChange`), otherwise it drifts away from the server.
- **Global CSS.** Resets and generic class names of the library can collide with the TopLogic
  stylesheets. Load the library's stylesheet through `ClientResources` and check it for side effects
  early.
- **Portals, focus, outside press.** Dialogs and popovers that portal into `document.body` must
  cooperate with the focus trap (`useFocusTrap`; a portaled popup anchored inside a modal surface
  carries `anchoredOverlayProps`), with closing on an outside press (`useCloseOnOutsidePress`) and
  with the window manager. Most integration defects are to be expected here.

## Behaviour details and limits

- **Wrapper order.** The wrapper with the lowest `order` is the outermost one; wrappers of equal
  order nest in registration order, the first registered outermost (default order
  `DEFAULT_ROOT_WRAPPER_ORDER` = 0).
- **Late wrapper registration.** A wrapper registered after controls are mounted takes effect
  immediately: the mounted roots re-render with the extended chain. Since that changes the element
  tree above the control, the subtree is remounted and loses its local React state (the server state
  is kept). Register wrappers when the script loads.
- **Replacement precedence.** A `replace` wins over every `register` of the same name, independent
  of the order in which the scripts load. A second `register` or a second `replace` of the same name
  logs a console warning; the later one wins.
- **Lookup timing.** A top-level control looks up its component when it is mounted, a nested control
  each time it is rendered. Register replacements when the script loads, before the page's controls
  are mounted.
- **Parts not rendered through the registry keep the TopLogic look.** A composition that draws a
  part itself, rather than through `TLChild`, is not affected by a replacement: the help and close
  buttons of a window's title bar, for instance, are plain `<button>`s inside `TLWindow`, not
  `TLButton`s. To restyle them, replace `TLWindow` (or style them through tokens).
- **Field values are untyped.** `value` is declared in `FieldState` as `json` and is `unknown` in
  TypeScript for every field; msgbuf cannot narrow an inherited field. Its shape is documented per
  message (a string for `TextInputState`, `true`/`false`/`null` for `CheckboxState`, a list of
  options for `DropdownSelectState`, …) — cast accordingly.
- **A field that cannot be edited is read-only or disabled.** A field that cannot be edited has
  `editable: false`. Usually it displays its value only (a form in view mode) — map that to the
  library's read-only prop. If it additionally has `disabled: true`, it is shown as an inactive
  input (an attribute whose dynamic visibility computes "disabled" in edit mode) — map that to the
  library's disabled prop. A field is never both editable and disabled.
- **A partial adapter is legitimate.** An adapter maps what the library can express and documents
  what it drops (the MUI date picker has no warning color; the MUI segmented choice does not
  reproduce the radio keyboard pattern of `TLSegmentedChoice`, since the MUI button group has no
  such mode). The server state stays complete regardless.

## Extending the contract (engine developers)

To make another component replaceable:

1. Add its state message to `state.proto` (extending `ControlState` or `FieldState`), document
   every property, and add the component to the list in the file header. The file carries
   `option TypeScript = "../../../react-src/state/control-state.ts"`; the msgbuf generator of the
   module's build (`msgbuf-generator-maven-plugin`, version `msgbuf.version` of the root `pom.xml`)
   writes the Java message classes and the TypeScript types. Export the new type from
   `react-src/bridge-entry.ts`.
2. In the Java control, publish every key through the generated constants
   (`putState(ButtonState.LABEL__PROP, label)`) instead of string literals.
3. In the component, read the state with `useTLState<Partial<XStateJson>>()`.
4. Extend `test.com.top_logic.layout.react.state.TestControlStateSchema`: drive the control
   through its setters and check that every key of its state (`stateAsJSON()`, or a subclass
   recording its `putState` calls) is declared in the message (`assertDeclared`; for lists and
   embedded controls `assertEachDeclared` and `assertChild`).
5. An enumeration sent to the client is declared in the message with `@Name("…")` on each constant,
   spelled as the external name of the Java enum (`ExternallyNamed`); the test compares both with
   `assertSameNames`.

The Java state classes are used for their property constants only: `ReactControl` keeps its state
as a map and sends patches of it.

## The Material UI module

`com.top_logic.layout.react.mui` (artifact `tl-layout-react-mui`) renders every replaceable component
with [Material UI](https://mui.com/) — a complete adapter module for a real component library. It
uses only MIT-licensed packages: `@mui/material`, `@mui/x-date-pickers`, `@emotion/*` and `dayjs`.
An application that wants the Material UI look depends on it and installs it with its own theme
(see [An application on Material UI](#an-application-on-material-ui)).

| Path | Content |
|---|---|
| `react-src/mui-entry.ts` | the entry of the bundle `tl-react-mui`: `installMui` and the re-exports of Material UI |
| `react-src/install.ts` | `installMui`: theme, derived properties, root wrapper, the selected replacements |
| `react-src/themeProperties.ts` | derives the styling properties of the TopLogic components from the MUI theme and writes them into the page |
| `react-src/MuiRoot.tsx` | the root wrapper: emotion cache, MUI theme and date localization |
| `react-src/adapters/` | one adapter per component of the state contract, each with a wire test (`*.test.tsx`); the shared parts are `field.tsx` (field state, typing fields), `choice.tsx` (selection fields) and `window-frame.ts` (moving and resizing a window) |
| `react-src/react-*-shim.ts` | the shims the aliases of `vite.config.ts` point to; `react-shim.ts` and `react-dom-shim.ts` name the complete public API, as prebuilt library code needs |
| `src/main/webapp/WEB-INF/conf/tl-layout-react-mui.conf.config.xml` | `ClientResources` registration of the bundle under the import specifier `tl-react-mui` |

Loading the bundle changes nothing on the page; `installMui` does. How it attaches Material UI:

- **Root wrapper.** `MuiRoot` nests an emotion `CacheProvider`, a `ThemeProvider` and the
  `LocalizationProvider` of the date pickers. It renders no element of its own, since it sits above
  every React root and an element there would break the fill layout. For the same reason there is
  no `ScopedCssBaseline`, and no global `CssBaseline`, whose reset would collide with the TopLogic
  stylesheets. The emotion cache appends its styles to the end of `<head>`, after the TopLogic
  stylesheets, so MUI wins on its own elements. The theme is the application's theme passed to
  `installMui`, unchanged (with CSS variables), in the language of the page (`<html lang>`, German or
  English).
- **Overlays.** Windows and dialogs keep the TopLogic window manager: MUI supplies the look
  (`Paper`, `DialogTitle`, `DialogContent`, `DialogActions`), while positioning, moving and
  resizing, the focus trap, Escape and the stacking stay with TopLogic. MUI's `Modal` is not used,
  since every one of its mechanisms already has an owner. Menus are positioned with `usePopover`
  and close through `useCloseOnOutsidePress`, like `TLMenu`. Popups of selects and date pickers
  carry `anchoredOverlayProps`, so the focus trap of a surrounding window accepts them.
- **Container context.** Form fields read `useFormLayout()`, buttons read `useButtonDefaults()`
  and apply `menuItemProps` inside a menu, so they behave like the TopLogic components in forms,
  toolbars, menus, app bars and windows.

The wire tests and the tests of `installMui` run with vitest (`npm test` in the module directory).

### An application on Material UI

An application brings its MUI theme in a React module of its own that depends on
`tl-layout-react-mui`. `com.top_logic.demo.react.mui` (artifact `tl-demo-react-mui`) is such a
module, in exactly the shape of a customer project:

| Path | Content |
|---|---|
| `react-src/customerTheme.ts` | the application's MUI theme (`createTheme` options; here the theme of MUI's "Onepirate" template) and its fonts (Fontsource packages) |
| `react-src/demo-mui-entry.ts` | `installMui({ theme: customerTheme, replace: 'all' })` |
| `vite.config.ts` | library build; `tl-react-bridge` and `tl-react-mui` external, Material UI resolved to `tl-react-mui` |
| `tsconfig.json` | `paths` for the types of `tl-react-bridge` and `tl-react-mui` |
| `src/main/webapp/WEB-INF/conf/tl-demo-react-mui.conf.config.xml` | the bundle (`requires="tl-react-mui"`) and its stylesheet (the fonts) |

The module needs neither React shims nor Material UI packages of its own: its bundle holds the theme
and the fonts only (the JavaScript is under 2 kB).

The entry calls `installMui` once, when the bundle loads:

```ts
import { installMui } from 'tl-react-mui';
import customerTheme from './customerTheme';

installMui({ theme: customerTheme, replace: 'all' });
```

`installMui(options: MuiOptions): void` takes

- `theme: ThemeOptions` — the options of the application's theme, as `createTheme` takes them,
  functions included (e.g. a `typography` computed from the palette);
- `replace?: 'all' | readonly ComponentName[]` — the TopLogic components rendered with Material UI,
  `'all'` (the default, `ALL_COMPONENTS`) or a selection of `COMPONENT_NAMES` (the 25 names of the
  table [The replaceable components](#the-replaceable-components), typed as the union
  `ComponentName`), e.g. `replace: ['TLButton', 'TLCheckbox']`. The components not selected keep
  their TopLogic rendering and follow the theme through the derived properties.

A second call fails, as does a name without adapter: the installation is made once, before the
controls of the page are mounted, and cannot be changed afterwards.

**One Material UI for the page.** The bundle `tl-react-mui` re-exports Material UI:
`export * from '@mui/material'` (which contains `@mui/material/styles` and the `colors` namespace)
and `export * from '@mui/x-date-pickers'`. The theme and the MUI-based controls of the application
import Material UI from there, not from npm packages of their own:

```ts
import { colors } from 'tl-react-mui';
import type { ThemeOptions } from 'tl-react-mui';
```

So they share one instance of Material UI with the adapters — and with it the theme, the emotion
style cache and the date localization of the root wrapper. A control of the application written
with MUI components (`import { Button, Stack, styled } from 'tl-react-mui'`) renders inside the root
wrapper like the adapters do and is styled by the same theme. Emotion is not re-exported: styles
are written with MUI's `styled`, `sx` or `css`.

The vite configuration of the application's module keeps `tl-react-mui` external and resolves the
imports of a library that uses Material UI to it — the entry points `@mui/material`,
`@mui/material/styles` and `@mui/x-date-pickers`, whose exports `tl-react-mui` has. Every other
import of `@mui/…` or `@emotion/…` (e.g. `@mui/material/Button`) fails the build, since it would
bundle a second copy of Material UI with a theme and a style cache of its own
(`com.top_logic.demo.react.mui/vite.config.ts`, shortened):

```ts
const TL_REACT_MUI = 'tl-react-mui';
const MUI_ENTRY_POINTS = ['@mui/material', '@mui/material/styles', '@mui/x-date-pickers'];

function sharedMaterialUi(): Plugin {
  return {
    name: 'shared-material-ui',
    enforce: 'pre',
    resolveId(source) {
      if (MUI_ENTRY_POINTS.includes(source)) return { id: TL_REACT_MUI, external: true };
      if (/^@(mui|emotion)\//.test(source)) this.error(`'${source}' would bundle a second copy of Material UI`);
      return null;
    },
  };
}

export default defineConfig({
  plugins: [sharedMaterialUi()],
  build: {
    lib: { entry: 'react-src/demo-mui-entry.ts', fileName: () => 'tl-demo-react-mui.js',
      cssFileName: 'tl-demo-react-mui', formats: ['es'] },
    outDir: 'src/main/webapp/script',
    emptyOutDir: false,
    rollupOptions: { external: ['tl-react-bridge', TL_REACT_MUI] },
  },
});
```

A module with React code of its own (an MUI-based control) also needs the React shims of
[new-react-module.md](new-react-module.md).

The re-export makes the bundle contain all of Material UI, not only the components the adapters use:
about 1.23 MB (gzip about 306 kB) instead of about 0.98 MB (gzip about 251 kB) with the adapters alone. The difference is what
lets an application use any MUI component without a second copy.

`tl-demo-react` includes the demo module with the Maven profile `mui` (off by default):

```bash
mvn -B install -pl com.top_logic.layout.react.mui,com.top_logic.demo.react.mui,com.top_logic.demo.react -P mui
MAVEN_ARGS=-Pmui   # start the app with the same profile, see demo-apps.md
```

### Following the MUI theme

The application's MUI theme is the single source of the look. The MUI components render with it
unchanged; the TopLogic components that are not replaced — tables, trees, panels, the
sidebar, toolbars, layouts — follow it through their styling properties:

- `themeProperties(theme)` computes, for each color scheme of the theme, values for the roles of
  the design system (`--tl-…`, `tokens.css`) and for the tokens of the UI themes (`--text-primary`,
  …, `tl-react-theme.config.xml`), the way MUI computes the corresponding values of its own
  components. Only properties that exist are set; aliases (`<ref>` tokens) follow the token they
  name.
- `installThemeProperties(theme)` writes them as one `<style id="tl-mui-theme-properties">` to the
  end of `<head>` when `installMui` runs, after the TopLogic stylesheets and the theme styles of
  the page, with the same specificity, so it wins. No build step and no theme of the
  `UIThemeService` is involved.
- A theme with a light and a dark scheme sets each for its mode of the design system
  (`[data-tl-mode]`); a theme with one scheme sets it on `:root`, in effect in every mode: the
  page stays in that scheme, as the MUI components do.

The values of the theme are used as they are — there is no contrast correction. What is mapped:

| MUI value | Properties |
|---|---|
| `primary.main` | brand surface, interactive border, focus ring, link (`--tl-surface-brand`, `--tl-border-interactive`, `--tl-focus-ring`, `--tl-text-link`, `--button-primary`, `--button-secondary`, `--interactive`, `--focus`, `--link-primary`, …) |
| `primary.dark` | brand hover/active, link hover (MUI's hover of a contained button) |
| `primary.contrastText` | text and icons on the brand (`--tl-text-on-brand…`, `--text-on-color`, `--icon-on-color`) |
| `primary.main` at `action.selectedOpacity` (+ `hoverOpacity`) on the paper | brand-subtle surfaces, selected table rows and sidebar items (`--layer-selected`, `--layer-selected-hover`) |
| `error`/`warning`/`success`/`info` | text ← `main`, border ← `light`, surface ← `main` (dark scheme: `dark`), subtle ← `light` lightened by 0.9 (dark scheme: darkened) as the background of a standard alert, text on it ← `contrastText`; `--support-*`, `--text-error`, `--button-danger(-hover)` |
| `text.primary`/`secondary`/`disabled` | text roles, helper text; placeholder ← `text.primary` at 0.42 (dark: 0.5) as in an MUI input; border emphasis ← `text.primary` |
| `background.default` | page surface (`--tl-surface-base`, `--background`); `--layer` ← `action.hover` on it |
| `background.paper` | layer, nested layer, overlay, field surfaces (`--layer-01`, `--layer-02`, `--field`, `--color-surface`, `--focus-inset`) |
| `background.default` emphasized by 0.8 | inverse surface (`--background-inverse`, as a snackbar), its text ← contrast text |
| `divider` | separator (`--tl-border-separator`, `--border-subtle`) |
| `common.black`/`white` at 0.23 | control border (`--tl-border-control`, `--border-strong`), as an outlined input |
| `action.hover`/`selected`/`focus` on the paper | interactive hover/selected/active surfaces (`--layer-hover`, `--background-selected`, `--layer-active`), opaque so that a frozen table cell hides what scrolls beneath it |
| `action.active`/`disabled`/`disabledBackground` | icons, disabled icons and borders, disabled surfaces |
| `typography.fontFamily`; `h6.fontFamily` | `--tl-font-family-sans`, `--font-family`; `--font-family-display` |
| `body2`, `caption`, `subtitle2`, `subtitle1`, `h6`, `h5`, `h4`, `h3` | body, label, panel title (`heading-compact-02`), headings and display sizes and line heights |
| `shape.borderRadius` | `--tl-radius-sm`, `--tl-radius-md`, `--corner-radius`, `--border-radius-02` |
| `shadows[1]`, `[8]`, `[16]`, `[24]` | raised, popover/menu, drag, dialog shadow |
| heights of the MUI controls | `--tl-size-control` ← a small outlined input: `body1` size × 1.4375 + 2 × 8.5px; `--tl-size-control-sm` ← a small button: `pxToRem(13)` × `button.lineHeight` + 2 × 4px; `--tl-size-row` ← a small table cell: `body2` size × line height + 2 × 6px + 1px border |
| `palette.mode` | `color-scheme` |

Some values of the TopLogic stylesheets are literal and reach no property, so they stay as they
are: the row height of the table (the state `rowHeight` of the server, 36px, which its scrolling
computes with) and its header and bar heights (`2.5rem`, `2rem`), the row height and font size of
the tree (`32px`, `14px`). The spacing scale (`--spacing-0…`, `--tl-space-…`) is not mapped: it
stays the TopLogic one. An application passes its own theme to `installMui` and picks the
replaced components with its `replace` option; everything not replaced follows the theme through
the derived properties.
