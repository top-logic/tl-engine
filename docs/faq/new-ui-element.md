# FAQ: New `UIElement` — with a client component of its own

How to add an element that a `.view.xml` can use, and what else has to be in place when the element
brings a React component that does not exist yet.

The names below are placeholders: the module is `my.app.module`, its Java package
`com.example.app.view`, the element `MyElement` (tag `my-element`), its control `MyControl` and the
client component `MyWidget`. Replace them by your own.

Before writing a new client component, check [react-view-layer.md](react-view-layer.md): the
`.view.xml` layer is a *composition* layer. A new component is justified for a genuinely new generic
widget, not for assembling panels, forms and buttons that already exist. An element that only
composes existing controls needs steps 1, 2, 4 and 5 — the whole client part falls away.

## What you are building

```
my-view.view.xml             <my-element label="…" start="5"/>       declaration
        │  parsed once, shared by all sessions
        ▼
MyElement                    implements UIElement                    stateless factory
        │  createControl(ViewContext) — once per session
        ▼
MyControl                    extends ReactControl                    the state the user sees
        │  mounts "MyWidget"      state ──SSE patch──▶   ◀──POST command──
        ▼
MyWidget.tsx                 registered in the control bundle         the rendering
```

The element is shared and must stay stateless; the control belongs to one session and holds the
state; the component only renders what the control publishes and sends back gestures.

## Files at a glance

| File | Role |
|------|------|
| **Server — always** | |
| `src/main/java/com/example/app/view/MyElement.java` | `UIElement` implementation plus its `Config` (the `@TagName` is the element's name in XML) |
| `src/main/java/com/example/app/view/MyControl.java` | `ReactControl`: names the client component, publishes state, answers commands |
| `src/main/webapp/WEB-INF/views/my-view.view.xml` | uses the new tag |
| `src/main/webapp/WEB-INF/views/app.view.xml` (or another view) | makes the view reachable, e.g. as a `nav-item` |
| `src/main/java/META-INF/messages_de.properties` | German labels of the new configuration properties (English is generated) |
| **Client — only for a new component** | |
| `react-src/controls/MyWidget.tsx` | the component |
| `react-src/my-app-entry.ts` | `register('MyWidget', MyWidget)` |
| `package.json`, `tsconfig.json`, `vite.config.ts` | build of the module's control bundle |
| `pom.xml` | `frontend-maven-plugin` runs that build |
| `src/main/webapp/WEB-INF/conf/myAppConf.config.xml` | announces the bundle (and its stylesheet) as a client resource |
| `src/main/webapp/WEB-INF/conf/metaConf.txt` | lists that config file — only for a **new** module |
| `src/main/webapp/script/tl-my-app-controls.js` | the built bundle; **is committed** |
| `src/main/webapp/style/myWidget.css` | styles of the component (optional) |
| `.gitignore` | ignores `/node/` and `/node_modules/` |

## 1. The element

```java
public class MyElement implements UIElement {

	@TagName("my-element")
	public interface Config extends UIElement.Config {

		/** The heading shown above the value. */
		@Name("label")
		@StringDefault("My widget")
		String getLabel();

		/** The value the widget starts each session with. */
		@Name("start")
		@IntDefault(0)
		int getStart();

		@Override
		@ClassDefault(MyElement.class)
		Class<? extends UIElement> getImplementationClass();
	}

	private final Config _config;

	@CalledByReflection
	public MyElement(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		return new MyControl(context, _config.getLabel(), _config.getStart());
	}
}
```

- **The `@TagName` is the whole registration.** `UIElement.Config` extends `PolymorphicConfiguration`,
  and the tag is found wherever the class lies on the class path — there is no list of element types
  to extend. What the tag *needs* is the module's type index, see [step 6](#6-build-and-verify).
- `@ClassDefault` is mandatory. Without it the framework tries to instantiate the configuration
  interface itself.
- The constructor signature `(InstantiationContext, Config)` is what the configuration framework
  calls; mark it `@CalledByReflection`.
- Configuration properties are ordinary typed configuration (`@Name` + a default annotation). Their
  documentation becomes label and tooltip in the view editor, so write it for a reader.
- Keep the element stateless: it is parsed once and shared by every session. Anything the user
  changes belongs in the control.
- An element that should not claim a global tag can be placed by `class=` instead — see
  [react-view-layer.md](react-view-layer.md).

## 2. The control

```java
public class MyControl extends ReactControl {

	private static final String VALUE = "value";

	private int _value;

	public MyControl(ReactContext context, String label, int start) {
		super(context, null, "MyWidget");   // ← the component to mount
		_value = start;
		putState("label", label);
		putState(VALUE, Integer.valueOf(_value));
	}

	@ReactCommandHandler("increment")
	void handleIncrement() {
		_value++;
		putState(VALUE, Integer.valueOf(_value));   // reaches the client as a state patch
	}
}
```

- The third constructor argument is the **name of the client component** — the one contract between
  the two halves. Pass the name a `register(…)` call in the bundle uses (step 3), or the name of a
  component the framework already ships (then skip step 3 entirely).
- `putState(key, value)` before the first render fills the initial state, afterwards it sends an SSE
  patch: no re-render, the component simply reads the new value.
- A command handler is found by `@ReactCommandHandler` alone. Any visibility works; the method may
  take the client's arguments as a `Map<String, Object>` or as a typed `ReactCommand` subtype.
- The second argument is the control's model (`null` when there is none).

## 3. The client component

### 3.1 The component

```tsx
import { React, useTLState, useTLCommand } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const MyWidget: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const value = (state.value as number) ?? 0;

  return (
    <div id={controlId} className="myWidget">
      <span className="myWidget__value">{value}</span>
      <button type="button" onClick={() => sendCommand('increment')}>+</button>
    </div>
  );
};

export default MyWidget;
```

**Import `React` from `tl-react-bridge`, never from `react`.** A direct `react` import bundles a
second React copy, and the hooks fail at runtime with “useState is null”. Render the state the
server publishes; do not keep a second copy of it in the component.

Put the element's id on the outermost node (`id={controlId}`), the way the other controls do.

### 3.2 The bundle entry

```typescript
// react-src/my-app-entry.ts
import { register } from 'tl-react-bridge';

import MyWidget from './controls/MyWidget';

register('MyWidget', MyWidget);
```

Name the entry after the bundle, as the existing modules do (`chartjs-entry.ts`,
`code-editor-entry.ts`, `wysiwyg-entry.ts`); the plain name `controls-entry.ts` belongs to the
framework module `com.top_logic.layout.react` itself.

### 3.3 `package.json`, `tsconfig.json`, `vite.config.ts`

```json
{
  "name": "tl-my-app",
  "version": "7.11.0",
  "private": true,
  "scripts": { "build": "vite build" },
  "devDependencies": {
    "@types/react": "^19.0.0",
    "@vitejs/plugin-react": "^4.3.0",
    "typescript": "^5.7.0",
    "vite": "^6.0.0"
  }
}
```

Do **not** depend on `react` / `react-dom` — they come from `tl-react-bridge`. Third-party React
libraries go under `dependencies` and then need the shim aliases described in
[new-react-module.md](new-react-module.md).

`tsconfig.json` is the same in every module; the `paths` entry is what makes `tl-react-bridge`
resolvable for the type checker:

```json
"paths": { "tl-react-bridge": ["../com.top_logic.layout.react/react-src/bridge-entry.ts"] }
```

```typescript
// vite.config.ts
export default defineConfig({
  plugins: [react({ jsxRuntime: 'classic' })],
  define: { 'process.env.NODE_ENV': JSON.stringify('production') },
  build: {
    lib: {
      entry: 'react-src/my-app-entry.ts',
      fileName: () => 'tl-my-app-controls.js',
      formats: ['es'],
    },
    outDir: 'src/main/webapp/script',
    emptyOutDir: false,                          // the folder holds other scripts, too
    rollupOptions: { external: ['tl-react-bridge'] },   // one React instance for the page
  },
});
```

### 3.4 `pom.xml`

```xml
<build>
  <plugins>
    <plugin>
      <groupId>com.github.eirslett</groupId>
      <artifactId>frontend-maven-plugin</artifactId>
      <version>1.15.4</version>
      <executions>
        <execution>
          <id>install-node</id>
          <goals><goal>install-node-and-npm</goal></goals>
          <configuration><nodeVersion>v20.10.0</nodeVersion></configuration>
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

Never run `npx vite build` by hand; the build runs it, and only the build knows the module layout.

### 3.5 Announce the bundle as a client resource

The page loads only the bundles the application announces. Add them to the module's application
configuration (`WEB-INF/conf/<module>.conf.config.xml`; in an application module the existing
`…Conf.config.xml` will do):

```xml
<config service-class="com.top_logic.layout.react.resource.ClientResources">
  <instance class="com.top_logic.layout.react.resource.ClientResources">
    <resources>
      <module-script name="tl-my-app-controls"
        requires="tl-react-bridge"
        resource="/script/tl-my-app-controls.js"
        specifier="tl-my-app-controls"
      />
      <stylesheet name="tl-my-app-controls-css"
        resource="/style/myWidget.css"
      />
    </resources>
  </instance>
</config>
```

`requires="tl-react-bridge"` is what puts the bundle *after* the bridge that owns the React
instance. Without this whole section the browser reports “Component not registered”.

A **new** module also needs its config file listed in its own `WEB-INF/conf/metaConf.txt`, plus the
rest of the [new module checklist](new-module-checklist.md).

### 3.6 Git

Ignore `/node/` and `/node_modules/`, and **commit the built bundle**
(`src/main/webapp/script/tl-my-app-controls.js`) — every React module in this repository does, so an
application can be run without a JS toolchain.

## 4. The view and its entry point

```xml
<?xml version="1.0" encoding="utf-8" ?>
<view>
	<panel>
		<title>
			<en>My view</en>
			<de>Meine Ansicht</de>
		</title>
		<my-element
			label="My widget"
			start="5"
		/>
	</panel>
</view>
```

Reachable, e.g. through the application's navigation:

```xml
<nav-item id="my-view"
	icon="css:bi bi-plus-slash-minus"
>
	<view-ref view="my-view.view.xml"/>
	<label>
		<en>My view</en>
		<de>Meine Ansicht</de>
	</label>
</nav-item>
```

Changed `.view.xml` files take effect after a logout / login — no restart needed.

## 5. Resources

The English `messages_en.properties` are generated from the configuration property names and their
documentation; the German ones are hand-maintained. A new configuration property therefore needs an
entry in `src/main/java/META-INF/messages_de.properties` — otherwise the build fails with

```
Resource check: Missing resource keys in '…/messages_de.properties':
  com.example.app.view.MyElement.Config.label,
  com.example.app.view.MyElement.Config.label.tooltip, …
```

Keep the file sorted the way the generated English one is. Details in [i18n.md](i18n.md).

## 6. Build and verify

```bash
mvn clean install -pl my.app.module        # always from the project root
```

Two traps worth knowing:

- **`clean` matters for a new element.** The tag is resolved through the module's type index
  `META-INF/com.top_logic.basic.reflect.TypeIndex.json`, written by the `TypeIndexer` annotation
  processor while the sources are compiled. A build that decides “Nothing to compile” does not write
  it, and a stale target directory can lack it entirely — the application then reports

  ```
  Configuration descriptor 'com.top_logic.layout.view.element.PanelElement$Config'
  has no property 'my-element'
  ```

  even though the class is right there. Verify with:

  ```bash
  unzip -p my.app.module/target/*.jar META-INF/com.top_logic.basic.reflect.TypeIndex.json | grep MyElement
  ```

- **Client changes need the application module rebuilt.** The running application serves the scripts
  from its own exploded overlay (`target/<app>-app/script/`), so a bundle rebuilt in a library module
  stays invisible until the application module is built again. Symptom: server-side changes take
  effect, client-side ones silently do not.

  ```bash
  grep -rl "<a string from your component>" my.app.module/target
  ```

Then start the application (see [demo-apps.md](demo-apps.md)) and exercise the element in the
browser.

## Troubleshooting

| Symptom | Cause | Fix |
|---------|-------|-----|
| `… has no property 'my-element'` when loading the view | Type index missing or stale | `mvn clean install -pl my.app.module` (step 6) |
| Element renders as an empty box, console says “Component not registered” | Bundle not announced, or the name in `register(…)` differs from the one passed to `super(…)` | Step 3.5, then compare the two names |
| “useState is null” / “useRef is null” | Second React instance: a component or a third-party library imports from `react` | Import from `tl-react-bridge`; for libraries add the shim aliases ([new-react-module.md](new-react-module.md)) |
| Component shows the initial state but never updates | State written into fields instead of `putState`, or the client keeps its own copy | Publish every change with `putState`; render from `useTLState()` |
| Command does nothing | Name mismatch between `sendCommand('x')` and `@ReactCommandHandler("x")` | Align the names |
| CSS not applied | Stylesheet not announced | Add the `<stylesheet>` resource (step 3.5) |
| Build fails: “Cannot find module 'tl-react-bridge'” | `paths` missing in `tsconfig.json` | Step 3.3 |

## The module afterwards

```
my.app.module/
├── package.json · tsconfig.json · vite.config.ts             bundle build
├── pom.xml                                                   frontend-maven-plugin
├── react-src/
│   ├── my-app-entry.ts                                       register('MyWidget', …)
│   └── controls/MyWidget.tsx                                 the component
└── src/main/
    ├── java/com/example/app/view/
    │   ├── MyElement.java                                    UIElement + Config (@TagName)
    │   └── MyControl.java                                    ReactControl → "MyWidget"
    ├── java/META-INF/messages_{de,en}.properties              labels of the configuration
    └── webapp/
        ├── WEB-INF/views/my-view.view.xml                     uses <my-element …/>
        ├── WEB-INF/views/app.view.xml                         navigation entry
        ├── WEB-INF/conf/myAppConf.config.xml                  ClientResources: bundle + stylesheet
        ├── script/tl-my-app-controls.js                       built bundle (committed)
        └── style/myWidget.css                                 styles of the component
```

## See also

- [react-view-layer.md](react-view-layer.md) — when a new component is justified at all, and how to
  compose existing ones
- [new-react-module.md](new-react-module.md) — a module whose purpose *is* React controls, including
  third-party libraries and their React shims
- [new-module-checklist.md](new-module-checklist.md) — everything a new module needs beyond this
- [i18n.md](i18n.md) — the resource workflow
