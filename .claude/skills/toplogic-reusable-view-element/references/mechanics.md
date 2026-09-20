# Mechanics: a data-bound, themeable view element

Public API signatures and a minimal skeleton for the Java↔React spine of a reusable, data-bound view
element. All of this is public API usable from an application module (e.g. `tl-chat`); none requires
editing the engine. The canonical live example is `DataListElement`/`DataListControl` +
`data-list.tsx` in `tl-chat`.

## Table of contents
- [Key public API](#key-public-api)
- [Element skeleton (Java)](#element-skeleton-java)
- [Control skeleton (Java)](#control-skeleton-java)
- [React skeleton](#react-skeleton)
- [Token + base-CSS scoping](#token--base-css-scoping)
- [Testing](#testing)

## Key public API

- `com.top_logic.model.search.expr.query.QueryExecutor`
  - `static QueryExecutor compile(Expr)` — `Expr` is `com.top_logic.model.search.expr.config.dom.Expr`
  - `Object execute(Object arg)` / `Object execute(Object[] args)` — applies a one-arg-per-row lambda,
    or feeds channel values positionally to a `rows` expression.
- `com.top_logic.layout.react.control.ReactControl` (extend it)
  - `protected void putState(String key, Object value)` — value may be a nested JSON tree
    (`List<Map<String,Object>>` works; proven by `ChatRailControl`). React reads `useTLState()[key]`.
    An embedded `ReactControl` — anywhere in that tree — is serialized as a child descriptor and
    rendered by `<TLChild control={…}/>`; attach, detach and disposal follow the state automatically.
  - `@ReactCommandHandler(value="name", params={@ReactParam(name="x", type="string", description="…")})`
    on a `public void m(Map<String,Object> args)` — React invokes with `useTLCommand()('name', {x})`.
  - `addCleanupAction(Runnable)`, `addAttachListener(Runnable)`, `addDetachListener(Runnable)` (from
    `Control`).
- `com.top_logic.layout.view.element.ContentControls.toControl(List<UIElement>, ViewContext)` — a
  container's children as one control (the sole one, or a `ReactStackControl` of them).
- `com.top_logic.layout.view.model.RowSourceObserver` — generic constructor:
  `new RowSourceObserver<>(List<R> initial, Function<Object[],Collection<?>> rowFn,
  Set<TLStructuredType> observedTypes, List<ViewChannel> inputChannels, Consumer<List<R>> sink)`,
  then `attach(ModelScope)` / `detach()`. The sink is where you re-serialize + `putState`.
- `com.top_logic.layout.view.ViewContext`: `resolveChannel(ChannelRef)`, `getModelScope()`,
  `childContext(String)` + `withChildSlotPath(String)` (one per child of a container).
- `com.top_logic.layout.view.channel.ViewChannel`: `get()`, `set(Object)`,
  `addListener(ChannelListener)` / `removeListener`. `ChannelListener` = `(sender, old, new) -> void`.
- Config formats: `Expr` (renders as TL-Script), `ChannelRef` + `@Format(ChannelRefFormat.class)`,
  `TLModelPartRef` (+ `TLModelPartRef.CommaSeparatedTLModelPartRefs` for lists), `ResKey`.
- Stable row id: `obj.tHandle().getObjectName().toExternalForm()` (see `ChatSessionIds.idOf`).
- Label a value for display: `com.top_logic.layout.provider.MetaLabelProvider.INSTANCE.getLabel(value)`
  (formats dates/objects/text uniformly; resolves `ResKey`s too).
- Read an attribute by name: `obj.tType().getPart(name)` then `obj.tValue(part)`.

## Element skeleton (Java)

```java
public class MyListElement implements UIElement {
  @TagName("my-list")
  public interface Config extends UIElement.Config {
    @Override @ClassDefault(MyListElement.class) Class<? extends UIElement> getImplementationClass();

    @Name("types") @Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class) List<TLModelPartRef> getTypes();
    @Name("rows") @Mandatory @NonNullable Expr getRows();
    @Name("observed-types") @Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class) List<TLModelPartRef> getObservedTypes();
    @Name("selection") @Format(ChannelRefFormat.class) @Nullable ChannelRef getSelection();
    // display config (e.g. <lines> of <line attribute|value>), plus UX flags…
  }
  private final Config _config;
  @CalledByReflection public MyListElement(InstantiationContext c, Config config) { _config = config; }
  @Override public IReactControl createControl(ViewContext c) { return new MyListControl(c, _config); }
}
```

## Control skeleton (Java)

```java
public class MyListControl extends ReactControl {
  private final QueryExecutor _rows;
  private final ViewChannel _selection;              // nullable
  private final boolean[] _applyingFromChannel = {false};
  private Map<String, TLObject> _byId = Map.of();

  public MyListControl(ViewContext context, MyListElement.Config config) {
    super(context, null, "MyList");                  // registered React element name
    _rows = QueryExecutor.compile(config.getRows());

    ChannelRef sel = config.getSelection();
    _selection = sel == null ? null : context.resolveChannel(sel);
    if (_selection != null) {
      ViewChannel.ChannelListener l = (s, o, n) -> applyFromChannel(n);
      _selection.addListener(l);
      addCleanupAction(() -> _selection.removeListener(l));
    }

    List<Object> initial = new ArrayList<>(rows(readChannelValues()));
    serialize(initial);
    if (_selection != null) applyFromChannel(_selection.get());

    RowSourceObserver<Object> obs = new RowSourceObserver<>(
      initial, args -> new ArrayList<>(rows(args)), observedTypes(), List.of(), this::serialize);
    addAttachListener(() -> obs.attach(context.getModelScope()));
    addDetachListener(obs::detach);
  }

  @ReactCommandHandler(value = "select", params = {@ReactParam(name = "id", type = "string", description = "Row id")})
  public void select(Map<String,Object> args) {
    if (_applyingFromChannel[0] || _selection == null) return;
    Object id = args == null ? null : args.get("id");
    _selection.set(id == null ? null : _byId.get(String.valueOf(id)));
  }

  private void applyFromChannel(Object v) {
    _applyingFromChannel[0] = true;
    try { putState("selectedId", /* idOf(first TLObject in v) or "" */ ""); }
    finally { _applyingFromChannel[0] = false; }
  }
  private void serialize(Collection<?> rows) { /* build List<Map>, refill _byId, putState("rows", list) */ }
  private Collection<?> rows(Object[] args) { Object r = _rows.execute(args); return r instanceof Collection<?> c ? c : (r == null ? List.of() : List.of(r)); }
  private Object[] readChannelValues() { return new Object[0]; }
  private Set<TLStructuredType> observedTypes() { /* resolve config.getObservedTypes() */ return Set.of(); }
}
```

The `_applyingFromChannel` guard is essential: without it, `set()` → listener → `putState` → React →
`select` command → `set()` loops.

## React skeleton

```tsx
import { React, useTLCommand, useTLState } from 'tl-react-bridge'
export function MyList({ controlId }: TLCellProps) {
  const state = useTLState()
  const command = useTLCommand()
  const rows = Array.isArray(state.rows) ? (state.rows as any[]) : []
  const selectedId = typeof state.selectedId === 'string' ? state.selectedId : ''
  return (
    <div id={controlId} className="my-list">
      {rows.map((r) => (
        <button key={r.id} className={`my-row${r.id === selectedId ? ' is-active' : ''}`}
                onClick={() => void command('select', { id: r.id })}>
          {r.primary}
        </button>
      ))}
    </div>
  )
}
// register('MyList', MyList) in the bundle entry (main.tsx)
```

Reactivity is push-only: the control decides when to `putState("rows", …)`; React just renders what it
receives. Client-only concerns (search text, local collapse) stay in React `useState`.

## Token + base-CSS scoping

A reusable element renders **outside** the host app's root (e.g. not inside `.tl-cockpit`), so it does
not inherit tokens or base CSS scoped there. Give the element root its own:

```css
/* Button reset + focus ring the element relies on (else <button>s show browser chrome: grey, square). */
:where(.my-list button) { font: inherit; color: inherit; background: none; border: 0; padding: 0; cursor: pointer; }
.my-list :focus-visible { outline: 2px solid var(--focus, var(--button-primary)); outline-offset: 2px; }

/* Component tokens that are otherwise only defined on the host root. Prefer hoisting these to :root
   as the real fix; redeclaring on the element root is the local workaround. */
.my-list { --my-radius: var(--corner-radius, 0.25rem); /* … */ }

/* Rows: flat by default, colour on hover/selected, rounded like siblings. */
.my-row { border-radius: var(--my-radius); background: none; }
.my-row:hover { background: var(--layer-hover); }
.my-row.is-active { background: var(--layer-selected); }
```

Consume only theme primitives (`--layer`, `--layer-hover`, `--layer-selected`, `--text-primary`,
`--border-subtle`, `--button-primary`, `--corner-radius`, `--shadow-menu`) plus aliased fallbacks, so an
app restyles the element through its own theme with no component-CSS change. If the host defines
media-query rules on a shared class your element reuses, override them under `.my-list <class>` (higher
specificity) so the element keeps its own behaviour at all widths.

## Testing

Config instantiation needs the module system (`TypeIndex`), so a plain `TestCase` fails with
"module not started". Boot it:

```java
public static Test suite() {
  return ModuleTestSetup.setupModule(new TestSuite(TestMyListConfig.class));
}
```

Then `TypedConfiguration.newConfigItem(MyListElement.Config.class)` works, and you can assert defaults.
Build with `mvn -o -pl <module> install -DskipTests` (JS typecheck, bundle and vitest all run even
with `-DskipTests`). Always finish with a live check in the real
app, in both themes and at a narrow width.
```
