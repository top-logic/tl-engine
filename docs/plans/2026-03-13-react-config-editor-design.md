# React Configuration Editor - Design Document

**Date**: 2026-03-13
**Ticket**: #29108
**Approach**: Option A — ConfigFieldModel with FieldModel binding
**Module**: `com.top_logic.layout.configedit` (artifact: `tl-layout-configedit`)

## 1. Overview

A new module that generates a React control tree for editing arbitrary `ConfigurationItem` hierarchies. The editor maps each `PropertyDescriptor` to a `ConfigFieldModel` (implementing `FieldModel`), which is then wrapped in an existing `ReactFormFieldControl` subclass and `ReactFormFieldChromeControl`. This eliminates the intermediate `FormField`/`FormContext` layer of the old `EditorFactory`.

### Key Principles

1. **ConfigurationItem is the source of truth** — no intermediate data model
2. **FieldModel is the field abstraction** — reuse existing React form field controls
3. **Server-driven dispatch** — PropertyDescriptor metadata determines which React component to use
4. **Recursive composition** — nested items and lists create nested ConfigEditorControls
5. **Direct editing** — no overlay pattern; ConfigurationItem is modified in-place

---

## 2. Module Setup

### Dependencies

```
com.top_logic.layout.configedit
  ├── com.top_logic              (ConfigurationItem, PropertyDescriptor, Labels, I18N)
  ├── com.top_logic.layout.react (ReactControl, ReactFormFieldControl, ReactFormFieldChromeControl)
  └── com.top_logic.basic        (utilities)
```

Note: Does NOT depend on `com.top_logic.layout.view`. The config editor produces `ReactControl` trees that can be used standalone or embedded in a view. View-system integration (a `UIElement` wrapper) can be added later as a separate step without changing the core module.

### Parent POM

`tl-parent-core-internal` (version `7.11.0-SNAPSHOT`)

### Package Structure

```
com.top_logic.layout.configedit/
  src/main/java/com/top_logic/layout/configedit/
    ConfigEditorControl.java         -- Root control: builds form from ConfigurationItem
    ConfigFieldModel.java            -- FieldModel binding to PropertyDescriptor
    ConfigSelectFieldModel.java      -- SelectFieldModel for enum/options properties
    ConfigFieldDispatch.java         -- Maps PropertyDescriptor → ReactControl
    ConfigItemControl.java           -- Nested ITEM property (mono/polymorphic)
    ConfigListControl.java           -- LIST/ARRAY/MAP property editor
    I18NConstants.java               -- Error/label I18N keys
  src/main/java/META-INF/
    web-fragment.xml
  src/test/java/test/com/top_logic/layout/configedit/
    TestConfigFieldModel.java
    TestConfigSelectFieldModel.java
    TestConfigFieldDispatch.java
    TestConfigEditorControl.java
    TestConfigItemControl.java
    TestConfigListControl.java
```

No TypeScript changes in Phase 1 — all existing React components (`TLTextInput`, `TLCheckbox`, `TLSelect`, `TLNumberInput`, `TLFormField`) work unchanged. New TS components for list management are deferred to Phase 3.

---

## 3. Class Design

### 3.1 ConfigFieldModel

Extends `AbstractFieldModel`, implements `ConfigurationListener`.

Binds a single PLAIN/REF/COMPLEX/DERIVED property on a `ConfigurationItem` to the `FieldModel` contract. Reads via `config.value(property)`, writes via `config.update(property, value)`. Listens to `ConfigurationListener` for external changes (e.g., constraint-driven resets or programmatic updates).

```java
public class ConfigFieldModel extends AbstractFieldModel implements ConfigurationListener {

    private ConfigurationItem _config;
    private final PropertyDescriptor _property;

    /**
     * Creates a field model bound to the given property.
     */
    public ConfigFieldModel(ConfigurationItem config, PropertyDescriptor property) {
        super(config.value(property)); // initial value = current config value
        _config = config;
        _property = property;
        setMandatory(property.isMandatory());
        _config.addConfigurationListener(_property, this);
    }

    @Override
    public Object getValue() {
        return _config.value(_property);
    }

    @Override
    public void setValue(Object value) {
        // Delegates to config.update(), which fires ConfigurationListener,
        // which calls onChange() below, which fires FieldModel listeners.
        Object oldValue = _config.value(_property);
        if (!Objects.equals(oldValue, value)) {
            _config.update(_property, value);
        }
    }

    @Override
    public void onChange(ConfigurationChange change) {
        // ConfigurationItem notifies us that this property changed.
        // Forward to FieldModel listeners.
        if (change.getKind() == ConfigurationChange.Kind.SET) {
            fireValueChanged(change.getOldValue(), change.getNewValue());
        }
    }

    /** The bound property descriptor. */
    public PropertyDescriptor getProperty() {
        return _property;
    }

    /** Rebind to a different ConfigurationItem (same descriptor). */
    public void rebind(ConfigurationItem newConfig) {
        _config.removeConfigurationListener(_property, this);
        Object oldValue = getValue();
        _config = newConfig;
        _config.addConfigurationListener(_property, this);
        Object newValue = getValue();
        if (!Objects.equals(oldValue, newValue)) {
            fireValueChanged(oldValue, newValue);
        }
    }

    /** Detach from config listeners. */
    public void detach() {
        _config.removeConfigurationListener(_property, this);
    }
}
```

**Design Notes**:
- `getValue()` reads from config directly (not from `_value` field in `AbstractFieldModel`) to ensure consistency. The `super(initialValue)` call sets `_defaultValue` for dirty tracking.
- `setValue()` does NOT call `super.setValue()` — it delegates to `config.update()`, which triggers `onChange()`, which fires listeners. This avoids double-notification.
- `isDirty()` compares current `getValue()` against `_defaultValue` from construction time.

### 3.2 ConfigSelectFieldModel

Extends `ConfigFieldModel`, implements `SelectFieldModel`.

For properties with enum types or `@Options` annotation, provides a list of selectable options.

```java
public class ConfigSelectFieldModel extends ConfigFieldModel implements SelectFieldModel {

    private List<?> _options;
    private final boolean _multiple;
    private final List<SelectOptionsListener> _optionsListeners = new ArrayList<>();

    public ConfigSelectFieldModel(ConfigurationItem config, PropertyDescriptor property,
                                   List<?> options, boolean multiple) {
        super(config, property);
        _options = options;
        _multiple = multiple;
    }

    @Override
    public List<?> getOptions() { return _options; }

    @Override
    public boolean isMultiple() { return _multiple; }

    @Override
    public void setOptions(List<?> options) {
        _options = options;
        for (SelectOptionsListener l : _optionsListeners) {
            l.onOptionsChanged(this, _options);
        }
    }

    @Override
    public void addOptionsListener(SelectOptionsListener listener) {
        _optionsListeners.add(listener);
    }

    @Override
    public void removeOptionsListener(SelectOptionsListener listener) {
        _optionsListeners.remove(listener);
    }
}
```

### 3.3 ConfigFieldDispatch

Static utility that creates the appropriate `ReactControl` for a given `PropertyDescriptor`.

```java
public class ConfigFieldDispatch {

    /**
     * Creates a ReactControl for the given PLAIN/REF property.
     *
     * @return An input control (not wrapped in chrome).
     */
    public static ReactControl createPlainControl(ReactContext context,
            ConfigFieldModel model) {
        PropertyDescriptor property = model.getProperty();
        Class<?> type = property.getType();

        // Enum → Select
        if (type.isEnum()) {
            return createEnumSelect(context, property, model);
        }

        // @Options annotation → Select
        // (deferred to Phase 4)

        // Primitive types
        if (type == String.class)   return new ReactTextInputControl(context, model);
        if (type == boolean.class || type == Boolean.class)
                                    return new ReactCheckboxControl(context, model);
        if (type == int.class || type == Integer.class
            || type == long.class || type == Long.class)
                                    return new ReactNumberInputControl(context, model);
        if (type == double.class || type == Double.class
            || type == float.class || type == Float.class)
                                    return new ReactNumberInputControl(context, model);

        // Fallback: text input with Format-based toString/parse
        return new ReactTextInputControl(context, model);
    }

    private static ReactControl createEnumSelect(ReactContext context,
            PropertyDescriptor property, ConfigFieldModel model) {
        Object[] constants = property.getType().getEnumConstants();
        List<Object> options = Arrays.asList(constants);
        ConfigSelectFieldModel selectModel = new ConfigSelectFieldModel(
            /* re-wrap with options */ model.getConfig(), property, options, false);
        LabelProvider labelProvider = enumLabelProvider(property);
        return new ReactSelectFormFieldControl(context, selectModel, labelProvider);
    }

    private static LabelProvider enumLabelProvider(PropertyDescriptor property) {
        // Use Labels.propertyLabel infrastructure for enum constant labels
        return value -> {
            if (value == null) return "";
            ResKey key = property.labelKey("@" + ((Enum<?>) value).name());
            String label = Resources.getInstance().getString(key, null);
            return label != null ? label : ((Enum<?>) value).name();
        };
    }
}
```

### 3.4 ConfigEditorControl

Root control that builds a form from a `ConfigurationItem`. Iterates the configuration descriptor's properties, creates `ConfigFieldModel` + control for each visible property, wraps each in chrome, and presents them as children in a vertical stack.

```java
public class ConfigEditorControl extends ReactControl {

    private final ConfigurationItem _config;
    private final List<ReactControl> _chromeControls = new ArrayList<>();

    public ConfigEditorControl(ReactContext context, ConfigurationItem config) {
        this(context, config, Collections.emptySet());
    }

    public ConfigEditorControl(ReactContext context, ConfigurationItem config,
                                Set<PropertyDescriptor> hiddenProperties) {
        super(context, config, "TLFormLayout");
        _config = config;
        buildForm(context, hiddenProperties);
    }

    private void buildForm(ReactContext context,
                           Set<PropertyDescriptor> hiddenProperties) {
        ConfigurationDescriptor descriptor = _config.descriptor();

        for (PropertyDescriptor property : descriptor.getProperties()) {
            if (hiddenProperties.contains(property)) continue;
            if (property.kind() == PropertyKind.DERIVED) continue; // skip derived for now

            ReactControl chrome = createPropertyControl(context, property);
            if (chrome != null) {
                _chromeControls.add(chrome);
            }
        }
        putState("children", _chromeControls);
    }

    private ReactControl createPropertyControl(ReactContext context,
                                                PropertyDescriptor property) {
        switch (property.kind()) {
            case PLAIN:
            case REF: {
                ConfigFieldModel model = createFieldModel(property);
                ReactControl input = ConfigFieldDispatch.createPlainControl(context, model);
                return wrapInChrome(context, property, model, input);
            }
            case ITEM: {
                return new ConfigItemControl(context, _config, property);
            }
            case LIST:
            case ARRAY:
            case MAP: {
                return new ConfigListControl(context, _config, property);
            }
            default:
                return null; // DERIVED, COMPLEX — skip for now
        }
    }

    private ConfigFieldModel createFieldModel(PropertyDescriptor property) {
        return new ConfigFieldModel(_config, property);
    }

    private ReactControl wrapInChrome(ReactContext context,
            PropertyDescriptor property, ConfigFieldModel model, ReactControl input) {
        String label = Labels.propertyLabel(property, false);
        if (label == null) {
            label = enhancePropertyName(property.getPropertyName());
        }
        return new ReactFormFieldChromeControl(context, label,
            model.isMandatory(), false, null, helpText(property),
            labelPosition(property), false, true, input);
    }

    private String helpText(PropertyDescriptor property) {
        // Resolve tooltip from property's label key with "@tooltip" suffix
        return Labels.propertyLabel(property, "@tooltip", true);
    }

    private String labelPosition(PropertyDescriptor property) {
        Class<?> type = property.getType();
        if (type == boolean.class || type == Boolean.class) {
            return "after"; // checkbox label goes after
        }
        return null; // default: label on the side
    }

    private static String enhancePropertyName(String name) {
        // "myPropertyName" → "My property name"
        // Capitalize first letter, split on camelCase boundaries
        // (Same logic as EditorFactory for fallback labels)
        // ...
    }

    @Override
    protected void cleanupChildren() {
        for (ReactControl child : _chromeControls) {
            child.cleanupTree();
        }
    }
}
```

**React module**: Uses `"TLFormLayout"` — the existing form layout component that renders children as label+field rows. Each child is a `ReactFormFieldChromeControl` which the form layout knows how to arrange.

### 3.5 ConfigItemControl

Handles `PropertyKind.ITEM` — a single nested `ConfigurationItem`.

**Monomorphic items** (concrete type known at compile time):
- Creates a nested `ConfigEditorControl` for the item value
- If item is null and property is mandatory, creates empty instance first

**Polymorphic items** (`PolymorphicConfiguration<T>`):
- Renders a type selector (select dropdown) showing available implementations
- Below the selector, renders a `ConfigEditorControl` for the current implementation's config
- When user changes type: creates new config instance, replaces nested editor

```java
public class ConfigItemControl extends ReactControl {

    private final ConfigurationItem _parentConfig;
    private final PropertyDescriptor _property;
    private ReactControl _nestedEditor;  // current nested form (or null)
    private ReactControl _typeSelector;  // polymorphic type selector (or null)

    public ConfigItemControl(ReactContext context, ConfigurationItem parentConfig,
                              PropertyDescriptor property) {
        super(context, parentConfig, "TLConfigItemGroup");
        _parentConfig = parentConfig;
        _property = property;

        String label = Labels.propertyLabel(property, false);
        putState("label", label != null ? label : property.getPropertyName());
        putState("collapsed", isMinimized(property));

        if (isPolymorphic(property)) {
            buildPolymorphicUI(context);
        } else {
            buildMonomorphicUI(context);
        }

        // Listen for property value changes (e.g., programmatic replacement)
        _parentConfig.addConfigurationListener(_property, change -> {
            if (change.getKind() == ConfigurationChange.Kind.SET) {
                rebuildNestedEditor(context, (ConfigurationItem) change.getNewValue());
            }
        });
    }

    private boolean isPolymorphic(PropertyDescriptor property) {
        return PolymorphicConfiguration.class.isAssignableFrom(
            property.getType());
    }

    private void buildMonomorphicUI(ReactContext context) {
        Object currentValue = _parentConfig.value(_property);
        if (currentValue == null && _property.isMandatory()) {
            // Auto-create default instance
            currentValue = createDefaultInstance(_property);
            _parentConfig.update(_property, currentValue);
        }
        if (currentValue != null) {
            ConfigurationItem nestedConfig =
                _property.getConfigurationAccess().getConfig(currentValue);
            _nestedEditor = new ConfigEditorControl(context, nestedConfig);
            putState("content", _nestedEditor);
        }
    }

    private void buildPolymorphicUI(ReactContext context) {
        // Build type options from property's element descriptors
        List<Map<String, String>> typeOptions = buildTypeOptions(_property);
        putState("typeOptions", typeOptions);

        Object currentValue = _parentConfig.value(_property);
        if (currentValue != null) {
            ConfigurationItem nestedConfig =
                _property.getConfigurationAccess().getConfig(currentValue);
            String currentType = nestedConfig.getConfigurationInterface().getName();
            putState("selectedType", currentType);
            _nestedEditor = new ConfigEditorControl(context, nestedConfig);
            putState("content", _nestedEditor);
        }
    }

    @ReactCommand("typeChanged")
    void handleTypeChanged(ReactContext context, Map<String, Object> args) {
        String newTypeName = (String) args.get("type");
        // Create new config instance of selected type
        ConfigurationItem newConfig = createInstance(newTypeName, _property);
        _parentConfig.update(_property, newConfig);
        // onChange listener triggers rebuildNestedEditor
    }

    private void rebuildNestedEditor(ReactContext context,
                                      ConfigurationItem newValue) {
        if (_nestedEditor != null) {
            _nestedEditor.cleanupTree();
            unregisterChildControl(_nestedEditor);
        }
        if (newValue != null) {
            ConfigurationItem nestedConfig =
                _property.getConfigurationAccess().getConfig(newValue);
            _nestedEditor = new ConfigEditorControl(context, nestedConfig);
            registerChildControl(_nestedEditor);
            putState("content", _nestedEditor);
            if (isPolymorphic(_property)) {
                putState("selectedType",
                    nestedConfig.getConfigurationInterface().getName());
            }
        } else {
            _nestedEditor = null;
            putState("content", null);
        }
    }
}
```

**React component** (`TLConfigItemGroup`): A collapsible group with optional header and type selector. New TS component, renders:
- Header row: label + collapse toggle (+ type selector if polymorphic)
- Content area: `<TLChild control={state.content} />` for the nested editor

### 3.6 ConfigListControl

Handles `PropertyKind.LIST`, `ARRAY`, `MAP` — a collection of configuration items.

```java
public class ConfigListControl extends ReactControl {

    private final ConfigurationItem _parentConfig;
    private final PropertyDescriptor _property;
    private final List<ReactControl> _entryControls = new ArrayList<>();

    public ConfigListControl(ReactContext context, ConfigurationItem parentConfig,
                              PropertyDescriptor property) {
        super(context, parentConfig, "TLConfigList");
        _parentConfig = parentConfig;
        _property = property;

        String label = Labels.propertyLabel(property, false);
        putState("label", label != null ? label : property.getPropertyName());

        // Build initial entries
        List<?> items = (List<?>) parentConfig.value(property);
        if (items != null) {
            for (Object item : items) {
                ConfigurationItem itemConfig =
                    property.getConfigurationAccess().getConfig(item);
                ReactControl entryEditor = createEntryEditor(context, itemConfig);
                _entryControls.add(entryEditor);
            }
        }
        putState("entries", _entryControls);

        // Available types for "add" button
        if (isPolymorphic(property)) {
            putState("addOptions", buildTypeOptions(property));
        }

        // Listen for ADD/REMOVE changes
        parentConfig.addConfigurationListener(property, change -> {
            switch (change.getKind()) {
                case ADD:
                    handleEntryAdded(context, change);
                    break;
                case REMOVE:
                    handleEntryRemoved(context, change);
                    break;
            }
        });
    }

    @ReactCommand("addEntry")
    void handleAddEntry(ReactContext context, Map<String, Object> args) {
        String typeName = (String) args.get("type"); // null for monomorphic
        ConfigurationItem newEntry = createInstance(typeName, _property);
        // Add to config list — triggers ConfigurationListener
        List<Object> list = (List<Object>) _parentConfig.value(_property);
        list.add(newEntry);
    }

    @ReactCommand("removeEntry")
    void handleRemoveEntry(ReactContext context, Map<String, Object> args) {
        int index = ((Number) args.get("index")).intValue();
        List<Object> list = (List<Object>) _parentConfig.value(_property);
        list.remove(index);
    }

    @ReactCommand("moveEntry")
    void handleMoveEntry(ReactContext context, Map<String, Object> args) {
        int from = ((Number) args.get("from")).intValue();
        int to = ((Number) args.get("to")).intValue();
        List<Object> list = (List<Object>) _parentConfig.value(_property);
        Object item = list.remove(from);
        list.add(to, item);
        // Rebuild entries state
        rebuildEntries(context);
    }

    private ReactControl createEntryEditor(ReactContext context,
                                            ConfigurationItem itemConfig) {
        String title = resolveEntryTitle(itemConfig);
        // Wrap nested editor in a panel-like chrome with title and remove button
        ConfigEditorControl inner = new ConfigEditorControl(context, itemConfig);
        // Return composite: collapsible entry with title + inner form
        // (details depend on TLConfigListEntry React component)
        return inner; // simplified for now
    }
}
```

**React component** (`TLConfigList`): New TS component, renders:
- Header: label + entry count
- Entry list: each entry as a collapsible card with title, remove button, nested form
- Footer: "Add" button (with type dropdown if polymorphic)

---

## 4. Property Label Resolution

Labels are resolved using the existing `Labels` utility from `com.top_logic.layout.form.values.edit`:

```java
// Primary: use I18N resource from PropertyDescriptor.labelKey()
String label = Labels.propertyLabel(property, false);

// Fallback: convert property name "myPropertyName" → "My property name"
if (label == null) {
    label = enhancePropertyName(property.getPropertyName());
}

// Tooltip: use "@tooltip" key suffix
String tooltip = Labels.propertyLabel(property, "@tooltip", true);
```

This reuses the same I18N infrastructure as EditorFactory — generated `messages.properties` files from `@Label` annotations and JavaDoc comments on config interfaces.

---

## 5. Value Parsing

`ReactFormFieldControl.parseClientValue()` converts JSON values from the client to Java types for `FieldModel.setValue()`. The existing subclass implementations handle:

| Control | parseClientValue |
|---------|-----------------|
| `ReactTextInputControl` | Returns String as-is |
| `ReactCheckboxControl` | Returns Boolean |
| `ReactNumberInputControl` | Returns Number (Integer, Long, Double) |
| `ReactSelectFormFieldControl` | Returns selected option object |

For config properties with `@Format` annotations, we may need a `ConfigFormattedFieldControl` that uses the property's `ConfigurationValueProvider` for parse/format. This is deferred to Phase 4.

---

## 6. TypeScript Components (New)

### Phase 1: No new TS components
All PLAIN properties use existing `TLTextInput`, `TLCheckbox`, `TLSelect`, `TLNumberInput`. Chrome uses existing `TLFormField`. Layout uses existing `TLFormLayout`.

### Phase 2: TLConfigItemGroup
```typescript
// Collapsible group for nested ITEM properties
const TLConfigItemGroup: FC<TLCellProps> = ({ state }) => {
  const [collapsed, setCollapsed] = useState(state.collapsed);
  const sendCommand = useTLCommand();

  return (
    <div className="tlConfigItemGroup">
      <div className="tlConfigItemGroup__header" onClick={() => setCollapsed(!collapsed)}>
        <span className="tlConfigItemGroup__label">{state.label}</span>
        {state.typeOptions && (
          <select value={state.selectedType}
                  onChange={e => sendCommand('typeChanged', { type: e.target.value })}>
            {state.typeOptions.map(opt => (
              <option key={opt.value} value={opt.value}>{opt.label}</option>
            ))}
          </select>
        )}
      </div>
      {!collapsed && state.content && <TLChild control={state.content} />}
    </div>
  );
};
```

### Phase 3: TLConfigList
```typescript
// List editor for LIST/ARRAY/MAP properties
const TLConfigList: FC<TLCellProps> = ({ state }) => {
  const sendCommand = useTLCommand();
  const entries = (state.entries as any[]) ?? [];

  return (
    <div className="tlConfigList">
      <div className="tlConfigList__header">
        <span>{state.label}</span>
        <span className="tlConfigList__count">({entries.length})</span>
      </div>
      <div className="tlConfigList__entries">
        {entries.map((entry, i) => (
          <div key={i} className="tlConfigList__entry">
            <TLChild control={entry} />
            <button onClick={() => sendCommand('removeEntry', { index: i })}>Remove</button>
          </div>
        ))}
      </div>
      <button onClick={() => sendCommand('addEntry', {})}>Add</button>
    </div>
  );
};
```

---

## 7. What Is NOT Needed

The following EditorFactory mechanisms are deliberately excluded:

| Mechanism | Why Not Needed |
|-----------|---------------|
| `FormField` / `FormGroup` / `FormContainer` | Replaced by `ConfigFieldModel` + `ReactControl` tree |
| `ValueModel` / `ModifiableValue` | Replaced by direct `config.value()` / `config.update()` |
| UI-conversion / storage-conversion | Not needed — ConfigFieldModel values are already in the right type |
| `FormGroupBuilder` / `ElementFactory` | List entry management is done by `ConfigListControl` directly |
| `PropertyEditModel` (constraint bridge) | Constraints evaluated directly on `ConfigFieldModel` |
| HTML template fragments | React components handle all rendering |
| `InitializerProvider` / `Initializer` | New config instances are created with TypedConfiguration defaults |

---

## 8. Data Flow Summary

### Read Path (initial render)
```
ConfigurationItem.value(property)
  → ConfigFieldModel (getValue())
    → ReactFormFieldControl (initFieldState → putState("value", ...))
      → data-react-state JSON → SSE initial state
        → React component renders input with value
```

### Write Path (user edits field)
```
React input onChange
  → useTLFieldValue() optimistic update + POST command
    → ReactFormFieldControl.handleValueChanged() → parseClientValue()
      → ConfigFieldModel.setValue()
        → ConfigurationItem.update(property, value)
          → ConfigurationListener.onChange()
            → ConfigFieldModel.fireValueChanged()
              → ReactFormFieldControl listener → putState("value", newValue)
                → SSE PatchEvent → React re-renders
```

### Nested Item Type Change
```
React type selector onChange
  → POST 'typeChanged' command
    → ConfigItemControl.handleTypeChanged()
      → Create new ConfigurationItem of selected type
      → parentConfig.update(property, newInstance)
        → ConfigurationListener fires
          → ConfigItemControl.rebuildNestedEditor()
            → Cleanup old ConfigEditorControl
            → Create new ConfigEditorControl for new type
            → putState("content", newEditor)
              → SSE PatchEvent → React replaces nested form
```
