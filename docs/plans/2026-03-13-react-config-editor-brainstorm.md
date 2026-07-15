# React Configuration Editor - Brainstorming

**Date**: 2026-03-13
**Ticket**: #29108
**Module**: `com.top_logic.layout.configedit` (proposed, artifact: `tl-layout-configedit`)

## 1. Problem Statement

The existing `EditorFactory` in `com.top_logic.layout.form.values.edit` generates UI for editing arbitrary `ConfigurationItem` hierarchies. While powerful, it is overly complex because it:

1. **Creates an intermediate FormContext/FormField tree** - Every configuration property becomes a `FormField` inside `FormGroup`s, even though the underlying data model is already fully typed via `PropertyDescriptor`. The FormField layer duplicates type information, adds conversion overhead, and introduces its own lifecycle.

2. **Tightly couples to the legacy layout system** - The generated `FormMember` tree must be rendered through `ControlProvider`s and the traditional `DisplayContext`/`TagWriter` pipeline. This prevents use in the new React-based view system.

3. **Has complex binding machinery** - Two-way binding goes through `ValueModel` → `ModifiableValue` → `FormField` → UI, with separate UI-conversion and storage-conversion layers. Every value change traverses multiple adapter layers.

4. **Recreates nested forms from scratch** - When an item property changes (e.g., polymorphic type switch), the entire nested `FormGroup` is torn down and rebuilt. There's no incremental update path.

**Goal**: Build a new React-based configuration editor that directly maps `ConfigurationItem` properties to a React control tree, using `FieldModel` bindings for read/write — eliminating the intermediate `FormField`/`FormContext` layer entirely.

---

## 2. Systems Under Consideration

### 2.1 Old World: EditorFactory

**Entry point**: `EditorFactory.initEditorGroup(FormContainer, ConfigurationItem)`

**Property dispatch** (switch on `PropertyKind`):
| Kind | Editor | FormMember |
|------|--------|------------|
| PLAIN/REF | `ValueEditor` | `FormField` (StringField, BooleanField, IntField, etc.) |
| ITEM | `ItemEditor` | `FormGroup` (nested, with polymorphic type selector) |
| LIST/ARRAY/MAP | `ListEditor` | `FormGroup` (entries + add/remove buttons) |
| COMPLEX | `ComplexEditor` | `FormGroup` (for ListBinding/MapBinding) |
| DERIVED | `ValueDisplay` | `FormField` (read-only) |

**Key annotations** that drive behavior:
- `@Options` → option providers for selects
- `@ItemDisplay` → MONOMORPHIC / POLYMORPHIC / VALUE display mode
- `@PropertyEditor` → custom editor override
- `@ControlProvider` → custom rendering
- `@TitleProperty` → display label for list entries
- `@DynamicMode` → conditional visibility/editability
- `@DisplayMinimized` → initial collapse state

**Weaknesses**:
- FormField creation for every property (even nested configs)
- Conversion layers (UI ↔ storage)
- Synchronous DOM rendering via TagWriter
- No incremental updates for nested config changes
- Complex constraint scheduling (deferred to post-creation pass)

### 2.2 New World: React Control Infrastructure

**Server-side**: `ReactControl` → puts state map → serializes as `data-react-state` JSON
**Client-side**: `tl-react-bridge` auto-mounts, `useTLState()` / `useTLFieldValue()` / `useTLCommand()`
**Transport**: SSE for server→client patches, POST for client→server commands

**Form field controls**:
- `ReactFormFieldControl` (Java) wraps `FieldModel`, listens for changes, sends patches
- `ReactFormFieldChromeControl` (Java) wraps input control with label/error/mandatory chrome
- TypeScript components: `TLTextInput`, `TLCheckbox`, `TLSelect`, `TLNumberInput`, `TLDatePicker`, etc.

**Composition**: `ReactStackControl` holds children as state, `TLChild` renders each, `TLFormLayout` arranges label+field pairs.

### 2.3 View System

**ViewElement** tree (stateless, shared) → `createControl(ViewContext)` → per-session ReactControl tree
**Channels**: `ViewChannel` (reactive named values), `DerivedViewChannel` (computed)
**Forms**: `FormElement` wraps `TLObject` with overlay pattern, `FieldElement` creates `AttributeFieldModel`

---

## 3. Architecture Options

### Option A: ConfigFieldModel — Direct Property Binding

**Core idea**: Create a `ConfigFieldModel` that implements `FieldModel` and binds directly to a `PropertyDescriptor` on a `ConfigurationItem`. Reuse the existing `ReactFormFieldControl` subclasses and `ReactFormFieldChromeControl` for rendering.

```
ConfigurationItem
  └─ PropertyDescriptor (typed metadata)
       └─ ConfigFieldModel (implements FieldModel)
            └─ ReactFormFieldControl (existing: TLTextInput, TLCheckbox, etc.)
                 └─ ReactFormFieldChromeControl (label + error + mandatory chrome)
```

**ConfigFieldModel responsibilities**:
- `getValue()` → `config.value(property)` (reads property value)
- `setValue(value)` → `config.update(property, value)` (writes property value)
- `isEditable()` → based on `@DynamicMode` evaluation or form mode
- `isMandatory()` → `property.isMandatory()`
- `hasError()` → delegates to constraint evaluation
- Listens to `ConfigurationListener` on the config item for external changes

**Nested items**: A `ConfigItemControl` creates a sub-form recursively, with its own `ConfigurationItem` and set of `ConfigFieldModel`s.

**Lists**: A `ConfigListControl` manages an ordered list of sub-forms, with add/remove/reorder commands.

**Pros**:
- Maximally reuses existing React form field infrastructure
- FieldModel is a proven, well-tested abstraction
- Minimal new TypeScript code needed
- Chrome, validation display, editability all come for free

**Cons**:
- Still has one abstraction layer (FieldModel) between config and control
- FieldModel was designed for TLObject attributes, may need extension for config-specific features (e.g., format annotations, option providers from config descriptors)
- Type mapping needs new dispatch (PropertyDescriptor types → ReactFieldControlProvider)

### Option B: Direct React State Mapping

**Core idea**: Skip FieldModel entirely. Create a single `ConfigEditorControl` that serializes the entire configuration tree as a nested JSON state object. TypeScript-side components read/write state directly.

```
ConfigurationItem
  └─ ConfigEditorControl (ReactControl)
       └─ state: { "name": "foo", "port": 8080, "nested": { ... }, "items": [...] }
            └─ React components read/write paths in state
```

**Server-side**: One `ConfigEditorControl` owns the entire config. React commands carry property paths (e.g., `"nested.timeout"` or `"items[2].name"`).

**Client-side**: TypeScript components are pure React — `TLConfigForm`, `TLConfigField`, `TLConfigList`, `TLConfigItemGroup`. They receive the full state tree and render recursively.

**Pros**:
- Simplest server-side: one control, one state object
- Client-side can be highly interactive (expand/collapse, drag-reorder) without server roundtrips
- No FieldModel abstraction needed
- Natural for JSON-like config structures

**Cons**:
- Large state serialization for complex configs (every change patches the whole tree)
- All type dispatch logic must be duplicated in TypeScript
- Option providers, constraint evaluation, and annotation interpretation all move to client — major effort
- Loses server-side validation guarantees
- Format/parse logic must be reimplemented in TS

### Option C: Hybrid — Server-Driven Descriptor + Client-Side Rendering

**Core idea**: Server generates a *schema descriptor* from the `ConfigurationItem`'s `ConfigurationDescriptor`, describing the form structure (property names, types, options, constraints). The client renders the form from the descriptor plus the current values. Edits are sent back as property-path updates.

```
ConfigurationItem
  └─ ConfigEditorControl (ReactControl)
       └─ state: {
            schema: { properties: [ {name: "port", type: "int", mandatory: true}, ... ] },
            values: { "port": 8080, ... },
            errors: { "port": null, ... }
          }
            └─ React TLConfigForm renders from schema + values
```

**Server-side**:
- `ConfigSchemaBuilder` walks `ConfigurationDescriptor` → produces schema JSON
- `ConfigEditorControl` holds schema + values + errors in state
- On value change command: update config item, re-validate, patch errors

**Client-side**:
- `TLConfigForm` iterates schema properties, renders appropriate input per type
- Nested items: schema contains sub-schemas
- Lists: schema describes element type, client manages add/remove

**Pros**:
- Schema generated once, cached (config descriptors are static)
- Client renders efficiently from static schema
- Server still owns validation and constraint logic
- Options can be included in schema (or fetched lazily)

**Cons**:
- Schema format is a new protocol to design and maintain
- Two serialization formats (schema + values) instead of one
- Must handle polymorphic types (schema changes when type changes)
- More complex than Option A for simple cases

---

## 4. Recommended Approach: Option A (ConfigFieldModel)

Option A is the strongest fit because:

1. **Reuses proven infrastructure** — The `FieldModel` → `ReactFormFieldControl` → React component pipeline is already built and working for TLObject forms. We extend it to configuration, not replace it.

2. **Keeps type dispatch on the server** — `PropertyDescriptor` provides complete type metadata. Server-side dispatch selects the right React component, just like `FieldControlService` does for model attributes.

3. **Incremental path** — Can start with PLAIN properties (strings, ints, booleans, enums), then add ITEM (nested configs), then LIST/MAP. Each step is independently testable.

4. **Annotation compatibility** — Configuration annotations (`@Options`, `@ItemDisplay`, `@DynamicMode`, etc.) are evaluated server-side in Java where the annotation processing infrastructure exists.

5. **Minimal new TypeScript** — The existing `TLTextInput`, `TLCheckbox`, `TLSelect`, `TLNumberInput`, etc. all work unchanged. We may need new components only for config-specific patterns (polymorphic type selectors, list management).

---

## 5. Detailed Design Sketch (Option A)

### 5.1 Module Structure

```
com.top_logic.layout.configedit/
  pom.xml (parent: tl-parent-core-internal, depends on tl-layout-view, tl-core)
  src/main/java/com/top_logic/layout/configedit/
    ConfigEditorElement.java          -- UIElement: <config-editor> tag
    ConfigEditorControl.java          -- Root ReactControl for config form
    ConfigFieldModel.java             -- FieldModel binding to PropertyDescriptor
    ConfigItemControl.java            -- Nested ITEM property sub-form
    ConfigListControl.java            -- LIST/ARRAY/MAP property with entries
    ConfigFieldDispatch.java          -- Maps PropertyDescriptor → ReactControl
    ConfigOptionProvider.java         -- Resolves @Options for config properties
    ConfigConstraintEvaluator.java    -- Evaluates config constraints
    I18NConstants.java                -- Error messages, labels
  src/main/java/META-INF/
    web-fragment.xml
  src/main/webapp/WEB-INF/conf/
    tl-layout-configedit.conf.config.xml
  src/test/java/test/com/top_logic/layout/configedit/
    TestConfigFieldModel.java
    TestConfigFieldDispatch.java
    TestConfigEditorControl.java
```

### 5.2 Core Classes

#### ConfigFieldModel

```java
/**
 * FieldModel binding a single PropertyDescriptor on a ConfigurationItem.
 *
 * Reads via config.value(property), writes via config.update(property, value).
 * Listens to ConfigurationListener for external changes.
 */
public class ConfigFieldModel extends AbstractFieldModel implements ConfigurationListener {
    private ConfigurationItem _config;
    private final PropertyDescriptor _property;
    private boolean _editable = true;

    public ConfigFieldModel(ConfigurationItem config, PropertyDescriptor property) {
        _config = config;
        _property = property;
        _config.addConfigurationListener(_property, this);
    }

    @Override
    public Object getValue() {
        return _config.value(_property);
    }

    @Override
    public void setValue(Object newValue) {
        _config.update(_property, newValue);
        // ConfigurationListener callback will fire, updating listeners
    }

    @Override
    public boolean isMandatory() {
        return _property.isMandatory();
    }

    @Override
    public void onChange(ConfigurationChange change) {
        // External change to this property
        fireValueChanged(change.getOldValue(), change.getNewValue());
    }

    public PropertyDescriptor getProperty() {
        return _property;
    }

    public void rebind(ConfigurationItem newConfig) {
        _config.removeConfigurationListener(_property, this);
        _config = newConfig;
        _config.addConfigurationListener(_property, this);
        fireValueChanged(/* old */ null, getValue());
    }
}
```

#### ConfigFieldDispatch

```java
/**
 * Maps PropertyDescriptor metadata to the appropriate ReactControl.
 *
 * Analogous to FieldControlService but for configuration properties
 * instead of TLStructuredTypePart attributes.
 */
public class ConfigFieldDispatch {

    public ReactControl createControl(ReactContext context, ConfigFieldModel model) {
        PropertyDescriptor property = model.getProperty();

        // Check for @PropertyEditor annotation first (custom override)
        // ...

        switch (property.kind()) {
            case PLAIN:
            case REF:
                return createPlainControl(context, property, model);
            case ITEM:
                return createItemControl(context, property, model);
            case LIST:
            case ARRAY:
            case MAP:
                return createListControl(context, property, model);
            case DERIVED:
                return createDerivedDisplay(context, property, model);
            default:
                return createFallbackControl(context, model);
        }
    }

    private ReactControl createPlainControl(ReactContext ctx, PropertyDescriptor prop,
                                             ConfigFieldModel model) {
        Class<?> type = prop.getType();

        if (type == String.class)          return new ReactTextInputControl(ctx, model);
        if (type == boolean.class)         return new ReactCheckboxControl(ctx, model);
        if (type == int.class)             return new ReactNumberInputControl(ctx, model);
        if (type == long.class)            return new ReactNumberInputControl(ctx, model);
        if (type == double.class)          return new ReactNumberInputControl(ctx, model);
        if (type == float.class)           return new ReactNumberInputControl(ctx, model);
        if (Enum.class.isAssignableFrom(type)) return createEnumSelect(ctx, prop, model);
        if (hasOptionsAnnotation(prop))    return createOptionsSelect(ctx, prop, model);

        // Fallback: use Format annotation to display as text
        return new ReactTextInputControl(ctx, model);
    }
}
```

#### ConfigEditorControl

```java
/**
 * Root ReactControl for editing a ConfigurationItem.
 *
 * Creates a ReactStackControl containing chrome-wrapped field controls
 * for each visible property. Handles nested items and lists recursively.
 */
public class ConfigEditorControl extends ReactControl {
    private final ConfigurationItem _config;
    private final ConfigurationDescriptor _descriptor;
    private final List<ReactControl> _fieldControls;

    public ConfigEditorControl(ReactContext context, ConfigurationItem config) {
        super(context, config, "TLConfigEditor");
        _config = config;
        _descriptor = config.descriptor();
        _fieldControls = new ArrayList<>();

        buildForm(context);
    }

    private void buildForm(ReactContext context) {
        for (PropertyDescriptor property : _descriptor.getProperties()) {
            if (isHidden(property)) continue;

            ReactControl fieldControl = createFieldControl(context, property);
            _fieldControls.add(fieldControl);
        }
        putState("children", _fieldControls);
    }

    private ReactControl createFieldControl(ReactContext context, PropertyDescriptor property) {
        ConfigFieldModel model = new ConfigFieldModel(_config, property);
        ReactControl inputControl = _dispatch.createControl(context, model);

        // Wrap in chrome (label + mandatory + error)
        String label = resolveLabel(property);
        return new ReactFormFieldChromeControl(context, label,
            model.isMandatory(), /* dirty */ false, inputControl);
    }
}
```

#### ConfigItemControl

```java
/**
 * Handles ITEM properties (nested ConfigurationItem).
 *
 * For monomorphic items: renders sub-form inline.
 * For polymorphic items: renders type selector + dynamic sub-form.
 */
public class ConfigItemControl extends ReactControl {
    // Observes the property value; when it changes (e.g., polymorphic type switch),
    // rebuilds the nested form and patches React state.

    // For polymorphic: state includes "options" (available implementation types)
    // and "selectedType" (current type). Type change → new ConfigurationItem instance
    // → new nested ConfigEditorControl.
}
```

#### ConfigListControl

```java
/**
 * Handles LIST/ARRAY/MAP properties.
 *
 * Renders ordered list of ConfigEditorControl sub-forms, each wrapped in a
 * collapsible panel with remove button. Provides add button (monomorphic or
 * polymorphic type selector).
 */
public class ConfigListControl extends ReactControl {
    // State: { entries: [child1, child2, ...], canAdd: true, addOptions: [...] }
    // Commands: "addEntry", "removeEntry", "moveEntry"
    // Each entry is a child ConfigEditorControl
}
```

### 5.3 React Components (TypeScript)

Most existing components work unchanged. New components needed:

| Component | Purpose |
|-----------|---------|
| `TLConfigEditor` | Root form: iterates children, renders in `TLFormLayout` |
| `TLConfigItemGroup` | Nested item: collapsible group, polymorphic type selector |
| `TLConfigList` | List editor: entry list + add/remove/reorder |
| `TLConfigListEntry` | Single list entry: collapsible, removable, draggable |
| `TLPolymorphicSelect` | Type selector dropdown for polymorphic properties |

### 5.4 Property Type Mapping

| Java Type | PropertyKind | ReactControl | React Component |
|-----------|-------------|--------------|-----------------|
| `String` | PLAIN | `ReactTextInputControl` | `TLTextInput` |
| `boolean` | PLAIN | `ReactCheckboxControl` | `TLCheckbox` |
| `int`, `long` | PLAIN | `ReactNumberInputControl` | `TLNumberInput` |
| `double`, `float` | PLAIN | `ReactNumberInputControl` | `TLNumberInput` |
| `Date` | PLAIN | `ReactDatePickerControl` | `TLDatePicker` |
| `Enum<E>` | PLAIN | `ReactSelectControl` | `TLSelect` |
| `Class<?>` | PLAIN | `ReactSelectControl` (with options) | `TLSelect` |
| `ConfigurationItem` | ITEM | `ConfigItemControl` | `TLConfigItemGroup` |
| `PolymorphicConfiguration` | ITEM | `ConfigItemControl` (polymorphic) | `TLConfigItemGroup` |
| `List<ConfigurationItem>` | LIST | `ConfigListControl` | `TLConfigList` |
| `Map<K, ConfigurationItem>` | MAP | `ConfigListControl` (keyed) | `TLConfigList` |
| `ResKey` | PLAIN | Custom `ReactResKeyControl` | New `TLResKeyInput` |
| Formatted types | PLAIN | `ReactTextInputControl` with Format | `TLTextInput` |

### 5.5 Annotation Support

| Annotation | Handling |
|------------|---------|
| `@Options` | Server resolves options, sends as state to `TLSelect` |
| `@ItemDisplay(VALUE)` | Treat item as atomic select (not nested form) |
| `@ItemDisplay(POLYMORPHIC)` | Show type selector + nested form |
| `@PropertyEditor` | Override dispatch to use specified Editor |
| `@DynamicMode` | Evaluate mode function, set `editable` on `ConfigFieldModel` |
| `@DisplayMinimized` | Set `collapsed: true` in initial state |
| `@TitleProperty` | Use property value as list entry title |
| `@Mandatory` | `ConfigFieldModel.isMandatory()` returns true |
| `@Format` | Parse/format through configured `ConfigurationValueProvider` |
| `@ControlProvider` | (Legacy, not supported — use `@PropertyEditor`) |

### 5.6 View System Integration

The config editor can be used as a `UIElement` in `.view.xml` files:

```xml
<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
  <channels>
    <channel name="configItem"/>
  </channels>

  <config-editor input="configItem"/>
</view>
```

Or programmatically:
```java
ConfigurationItem config = /* obtain config */;
ConfigEditorControl editor = new ConfigEditorControl(context, config);
```

### 5.7 Data Flow

```
ConfigurationItem (source of truth)
  │
  ├── PropertyDescriptor "name" (String)
  │     └── ConfigFieldModel ──→ ReactTextInputControl ──→ TLTextInput
  │           ↑ ConfigurationListener        ↓ @ReactCommand("valueChanged")
  │           └── config.update(prop, val) ←─┘
  │
  ├── PropertyDescriptor "timeout" (int)
  │     └── ConfigFieldModel ──→ ReactNumberInputControl ──→ TLNumberInput
  │
  ├── PropertyDescriptor "handler" (PolymorphicConfiguration<Handler>)
  │     └── ConfigItemControl
  │           ├── Type selector ──→ TLPolymorphicSelect
  │           └── Nested ConfigEditorControl (for selected type's config)
  │                 ├── ConfigFieldModel "param1" ──→ ...
  │                 └── ConfigFieldModel "param2" ──→ ...
  │
  └── PropertyDescriptor "filters" (List<FilterConfig>)
        └── ConfigListControl
              ├── Entry[0]: ConfigEditorControl ──→ ...
              ├── Entry[1]: ConfigEditorControl ──→ ...
              └── Add button ──→ TLPolymorphicSelect (if polymorphic)
```

---

## 6. Key Design Decisions to Make

### 6.1 ConfigFieldModel vs. Extending AttributeFieldModel

**Question**: Should `ConfigFieldModel` be a new class or should we generalize `AttributeFieldModel`?

**Recommendation**: New class. `AttributeFieldModel` is tightly coupled to `TLObject`/`TLStructuredTypePart`. Configuration properties use `PropertyDescriptor` which has a completely different API (`config.value(prop)` vs `object.tValue(part)`). Sharing a base class (`AbstractFieldModel`) is sufficient.

### 6.2 Edit Mode: Direct or Overlay?

**Question**: Should config editing use an overlay pattern (like `TLObjectOverlay`) or edit the `ConfigurationItem` directly?

**Recommendation**: Edit directly. `ConfigurationItem` already supports transient modification — it's an in-memory object, not a persistent KB object. There's no transaction/commit cycle needed. If undo is needed later, we can snapshot the config XML before editing.

For cases where the config is loaded from a file and changes need to be saved explicitly, the caller provides a save callback. The config editor doesn't own persistence.

### 6.3 Constraint Evaluation Strategy

**Question**: When should constraints be evaluated?

**Options**:
- **Eager (on every change)**: Evaluate all constraints after every field change. Simple but potentially expensive.
- **Deferred (on validation request)**: Only evaluate when explicitly asked (e.g., before save). Fast editing but late feedback.
- **Incremental (dependency-tracked)**: Only re-evaluate constraints that depend on changed properties. Best UX, most complex.

**Recommendation**: Start with eager evaluation. Config forms are typically small (10-50 properties). If performance becomes an issue, add dependency tracking later.

### 6.4 Polymorphic Type Switching

**Question**: When the user changes the implementation class of a polymorphic property, what happens to the nested form?

**Approach**:
1. Server receives `typeChanged` command with new implementation class
2. Server creates new `ConfigurationItem` of the selected type
3. Server copies compatible property values from old config to new (where property names/types match)
4. Server rebuilds nested `ConfigEditorControl` for new type
5. Server patches React state with new child controls

### 6.5 List Ordering

**Question**: How should list reordering work?

**Approach**:
- Client-side: Drag-and-drop in `TLConfigList` (using HTML5 drag or a library)
- On drop: send `moveEntry` command with `{fromIndex, toIndex}`
- Server reorders the list, patches state with new order

### 6.6 Module Dependencies

```
com.top_logic.layout.configedit
  ├── com.top_logic.layout.view  (UIElement, ViewContext, FieldModel, ReactFormFieldControl)
  ├── com.top_logic.layout.react (ReactControl, SSE, tl-react-bridge)
  ├── com.top_logic              (ConfigurationItem, PropertyDescriptor, TypedConfiguration)
  └── com.top_logic.basic        (I18N, utilities)
```

The module does NOT depend on the old `EditorFactory` or `FormContext` system.

---

## 7. Implementation Phases

### Phase 1: PLAIN Properties (MVP)

- `ConfigFieldModel` for PLAIN/REF property kinds
- `ConfigFieldDispatch` type mapping (String, boolean, int, double, enum)
- `ConfigEditorControl` root control (flat form, no nesting)
- `ConfigEditorElement` UIElement for view XML integration
- Unit tests for ConfigFieldModel
- Demo: edit a simple config interface with text fields, checkboxes, number inputs

### Phase 2: ITEM Properties (Nested Configs)

- `ConfigItemControl` for single nested ConfigurationItem
- Monomorphic items: inline sub-form
- Polymorphic items: type selector + dynamic sub-form
- `TLConfigItemGroup` React component (collapsible group)
- `TLPolymorphicSelect` React component
- Test: edit config with nested PolymorphicConfiguration

### Phase 3: LIST/MAP Properties

- `ConfigListControl` for ordered lists and keyed maps
- `TLConfigList` and `TLConfigListEntry` React components
- Add/remove entries
- Polymorphic add (type selector before adding)
- `@TitleProperty` for entry labels
- Test: edit config with List<PolymorphicConfiguration<...>>

### Phase 4: Advanced Features

- `@Options` annotation support (dynamic option providers)
- `@DynamicMode` (conditional editability)
- `@DisplayMinimized` (initial collapse)
- Constraint annotation evaluation
- `@Format` custom value formatting
- Drag-and-drop list reordering
- ResKey editor

### Phase 5: Parity with EditorFactory

- COMPLEX properties (ListBinding/MapBinding)
- @UseBuilder / @UseTemplate support
- Full annotation compatibility audit
- Performance optimization for large config trees
- Documentation

---

## 8. Open Questions

1. **Label resolution**: EditorFactory derives labels from property names and `@Label` annotations via generated `messages.properties`. Should we reuse the same label resolution mechanism or introduce a new one?

2. **Error reporting**: How should config parsing errors (e.g., invalid format string) be displayed? Inline per-field (via FieldModel.hasError) or in a summary area?

3. **Read-only mode**: Should the config editor support a read-only display mode? If so, should it use the same controls with `editable=false` or a separate display-only rendering?

4. **Config source**: Where does the `ConfigurationItem` come from? Typical sources:
   - Parsed from XML file (editable, save back to file)
   - Loaded from application config (read-only inspection)
   - Created in-memory for a wizard workflow
   - The editor should be source-agnostic.

5. **Undo/Redo**: Should we support undo/redo for config edits? If so, snapshot-based (serialize/restore XML) or command-based (record and invert each change)?

6. **Integration with existing EditorFactory**: Should there be a migration path where the old EditorFactory can optionally delegate to the new system? Or is this a clean break?
