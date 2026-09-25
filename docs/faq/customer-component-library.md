# FAQ: Rendering the React UI with a customer's component library

A customer often brings a React component library of its own that defines the corporate design,
stylesheets included. Such a library knows nothing about TopLogic: it keeps no server state and binds
no data. This article describes how to attach it so that the applications built with the view layer
(`.view.xml`) appear in the customer's design — and which extension points of `tl-react-bridge` serve
that purpose.

A runnable reference is the module `com.top_logic.demo.react.corporate` (see
[The example module](#the-example-module)).

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

## Level 2: adapter components for leaf widgets

For every TopLogic component the library has a counterpart for, an adapter module contains a small
adapter: it maps the control state to the props of the library component and the library's
callbacks to TopLogic commands.

### The module

The adapter module is a React module as described in [new-react-module.md](new-react-module.md)
(`package.json`, `tsconfig.json`, `vite.config.ts`, `pom.xml` with the `frontend-maven-plugin`). Its
bundle and the library's stylesheet are announced as client resources in a configuration file
listed in `WEB-INF/conf/metaConf.txt`:

```xml
<config service-class="com.top_logic.layout.react.resource.ClientResources">
	<instance class="com.top_logic.layout.react.resource.ClientResources">
		<resources>
			<module-script name="tl-demo-react-corporate"
				requires="tl-react-bridge"
				resource="/script/tl-demo-react-corporate.js"
			/>
			<stylesheet name="tl-demo-react-corporate-css"
				resource="/style/tl-demo-react-corporate.css"
			/>
		</resources>
	</instance>
</config>
```

The module needs no Java code; a `web-fragment.xml` in `src/main/java/META-INF/` makes its webapp
resources part of the application.

### The entry file: root wrappers and replacements

The bundle's entry file registers everything when it loads
(`com.top_logic.demo.react.corporate/react-src/corporate-entry.ts`):

```ts
import { React, registerRootWrapper, replace } from 'tl-react-bridge';
import { BrandProvider } from './example-lib';
import BrandButtonAdapter from './adapters/BrandButtonAdapter';
import BrandCheckboxAdapter from './adapters/BrandCheckboxAdapter';

function AcmeBrand({ children }: { children?: React.ReactNode }) {
  return React.createElement(BrandProvider, { name: 'acme', accent: '#0e7c66', corners: 'pill', children });
}

registerRootWrapper(AcmeBrand);
replace('TLButton', BrandButtonAdapter);
replace('TLCheckbox', BrandCheckboxAdapter);
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
- **`<ThemeIcon encoded={…}/>`** (`bridge/ThemeIcon.tsx`) renders a theme image from the encoded
  form a control sends in its state (`ButtonState.image`, the icons of menu entries and tabs, …): an
  icon font class, an image file, or nothing for the invisible image. An adapter passes the element
  to the library wherever the library takes an icon.

### An adapter

An adapter reads the typed state with `useTLState<XState>()` — the state types are exported by
`tl-react-bridge` — and sends commands with `useTLCommand()` (shortened from
`react-src/adapters/BrandButtonAdapter.tsx`):

```tsx
import { React, useTLState, useTLCommand, useKeyboardBinding, rootClassName, ThemeIcon } from 'tl-react-bridge';
import type { TLCellProps, ButtonState } from 'tl-react-bridge';
import { BrandButton } from '../example-lib';

const CMD_CLICK = 'click';

const BrandButtonAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<ButtonState>();
  const sendCommand = useTLCommand();
  const disabled = state.disabled === true;
  const click = () => sendCommand(CMD_CLICK);   // the full adapter also honours navigateUrl

  useKeyboardBinding(state.keyGesture, () => {
    if (disabled || state.hidden) return false;  // declined: falls through to an outer binding
    click();
    return true;
  });

  if (state.hidden === true) return null;
  const mode = state.displayMode ?? 'label-only';
  const showIcon = !!state.image && mode !== 'label-only';
  const iconOnly = showIcon && mode === 'icon-only';
  return (
    <BrandButton id={controlId}
      variant={state.tone === 'danger' ? 'danger' : state.appearance === 'primary' ? 'primary' : 'secondary'}
      disabled={disabled} pressed={state.active === true}
      icon={showIcon ? <ThemeIcon encoded={state.image!} /> : undefined}
      aria-label={showIcon ? state.label : undefined}   // the full adapter also sets the tooltip
      className={rootClassName(state, state.cssClasses)}
      onClick={click}>
      {iconOnly ? undefined : state.label}
    </BrandButton>
  );
};
```

Theme images reach an adapter in their encoded form (`state.image`); `ThemeIcon` from
`tl-react-bridge` turns them into an element the library shows as its icon. An icon-only button is
named by its label: the full adapter sets it as `aria-label` and declares it as tooltip through
`TOOLTIP_ATTR` — always for an icon-only button, and with `TOOLTIP_WHEN_ATTR` = `WHEN_TRUNCATED`
(shown only while the label is clipped) otherwise, as `TLButton` does.

A field adapter reads and writes its value through `useTLFieldValue()`, which sends `valueChanged`
and so makes the library component part of the form's edit and save cycle
(`react-src/adapters/BrandCheckboxAdapter.tsx`):

```tsx
const state = useTLState<CheckboxState>();
const [value, setValue] = useTLFieldValue();
return <BrandCheckbox id={controlId} checked={value === true} onChange={setValue}
  readOnly={state.editable === false} invalid={state.hasError === true}
  className={rootClassName(state)} />;
```

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
| `TLDropdownSelect`, `TLOptionChips`, `TLSegmentedChoice` | `DropdownSelectState` |
| `TLTabBar` | `TabBarState` |
| `TLWindow` | `WindowState` |
| `TLDialog` | `DialogState` |
| `TLMenu` | `MenuState` |
| `TLSnackbar` | `SnackbarState` |

The shared parts are `ControlState` (`hidden`, `cssClass`), `FieldState` (value, `editable`,
`mandatory`, error and warning flags, label, placeholder, …), `TypingFieldState` (debounce, send on
blur) and `ChildControl` (a control embedded in the state of another one, rendered with
`<TLChild control={…}/>`). The TypeScript types are generated into
`com.top_logic.layout.react/react-src/state/control-state.ts`; enumerations appear there as string
unions (`ButtonState.Appearance` is `'primary' | 'ghost' | 'link'`).

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
  `tl-react-bridge` (more specific paths first), and `tl-react-bridge` is `external`. See
  `com.top_logic.demo.react.corporate/vite.config.ts` and `react-src/react-*-shim.ts`, or the same
  setup in `com.top_logic.layout.react.wysiwyg` and `com.top_logic.layout.react.chartjs`. The
  library's React peer version must match the bridge's (`react` in
  `com.top_logic.layout.react/package.json`). A shim re-exports the hooks the library uses — extend
  the list in `react-shim.ts` when the library imports one it does not name.
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
- **Fields have no `disabled`.** The server never sends `disabled` for a field; a field that cannot
  be edited (a form in view mode) has `editable: false`. Map that to the library's read-only or
  disabled prop. `disabled` exists for buttons (`ButtonState`) and menu entries only.
- **A partial adapter is legitimate.** An adapter maps what the library can express and documents
  what it drops (the example button ignores the appearance defaults of its container; the example
  checkbox has no tri-state and no switch presentation). The server state stays complete regardless.

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
3. In the component, read the state with `useTLState<XState>()`.
4. Extend `test.com.top_logic.layout.react.state.TestControlStateSchema`: drive the control
   through its setters and check that every key of its state (`stateAsJSON()`, or a subclass
   recording its `putState` calls) is declared in the message (`assertDeclared`; for lists and
   embedded controls `assertEachDeclared` and `assertChild`).
5. An enumeration sent to the client is declared in the message with `@Name("…")` on each constant,
   spelled as the external name of the Java enum (`ExternallyNamed`); the test compares both with
   `assertSameNames`.

The Java state classes are used for their property constants only: `ReactControl` keeps its state
as a map and sends patches of it.

## The example module

`com.top_logic.demo.react.corporate` (artifact `tl-demo-react-corporate`) attaches a stand-in library
to the UI:

| Path | Content |
|---|---|
| `react-src/example-lib/` | the stand-in library: `BrandProvider`, `BrandButton`, `BrandCheckbox`; imports React from `'react'` and knows nothing about TopLogic; renders an error marker without its provider |
| `react-src/adapters/` | `BrandButtonAdapter`, `BrandCheckboxAdapter` |
| `react-src/corporate-entry.ts` | root wrapper and replacements |
| `react-src/react-shim.ts`, `react-dom-shim.ts`, `react-jsx-runtime-shim.ts` | the shims the aliases of `vite.config.ts` point to |
| `src/main/webapp/WEB-INF/conf/tl-demo-react-corporate.conf.config.xml` | `ClientResources` registration |
| `src/main/webapp/style/tl-demo-react-corporate.css` | the library's stylesheet |

`tl-demo-react` includes it with the Maven profile `corporate-example` (off by default):

```bash
mvn -B install -pl com.top_logic.demo.react.corporate,com.top_logic.demo.react -P corporate-example
MAVEN_ARGS=-Pcorporate-example   # start the app with the same profile, see demo-apps.md
```

Every button and checkbox of the demo — in forms, toolbars and dialogs — then renders as a brand
component with the pill-shaped accent style. See [demo-apps.md](demo-apps.md) for URL and login.
