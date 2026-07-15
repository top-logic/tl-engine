# React Configuration Editor - Implementation Plan

**Date**: 2026-03-13
**Ticket**: #29108
**Design**: `docs/plans/2026-03-13-react-config-editor-design.md`
**Module**: `com.top_logic.layout.configedit` (artifact: `tl-layout-configedit`)

## Phase 1: Module Setup + PLAIN Properties (MVP)

### Step 1.1: Create Module

Create the Maven module `com.top_logic.layout.configedit`:

- **`pom.xml`**: Parent `tl-parent-core-internal`, artifact `tl-layout-configedit`
  - Dependencies: `com.top_logic` (tl-core), `com.top_logic.layout.react` (tl-layout-react), `junit` (test)
- **`src/main/java/META-INF/web-fragment.xml`**: Standard fragment descriptor
- Register in `tl-parent-engine/pom.xml` module list

### Step 1.2: ConfigFieldModel

Create `com.top_logic.layout.configedit.ConfigFieldModel`:

- Extends `AbstractFieldModel`
- Implements `ConfigurationListener`
- Constructor: `(ConfigurationItem config, PropertyDescriptor property)`
  - Sets initial value from `config.value(property)`
  - Sets mandatory from `property.isMandatory()`
  - Registers as `ConfigurationListener` on the config item for this property
- `getValue()`: returns `_config.value(_property)` (read-through)
- `setValue(Object)`: calls `_config.update(_property, value)` (write-through)
- `onChange(ConfigurationChange)`: on `Kind.SET`, calls `fireValueChanged(old, new)`
- `rebind(ConfigurationItem)`: switches to different config item, fires if value changed
- `detach()`: removes config listener

**Test**: `TestConfigFieldModel`
- Create a simple `ConfigurationItem` with String, int, boolean properties
- Verify `getValue()` returns initial config value
- Verify `setValue()` updates config and fires listener
- Verify external `config.update()` fires listener via `onChange()`
- Verify `isMandatory()` reflects `@Mandatory` annotation
- Verify `isDirty()` after value change

### Step 1.3: ConfigSelectFieldModel

Create `com.top_logic.layout.configedit.ConfigSelectFieldModel`:

- Extends `ConfigFieldModel`
- Implements `SelectFieldModel`
- Constructor: `(ConfigurationItem config, PropertyDescriptor property, List<?> options, boolean multiple)`
- `getOptions()`, `setOptions()`, `isMultiple()`
- `addOptionsListener()`, `removeOptionsListener()`

**Test**: `TestConfigSelectFieldModel`
- Create config with enum property
- Verify options match enum constants
- Verify `setValue()` with enum value works
- Verify options listener fires on `setOptions()`

### Step 1.4: ConfigFieldDispatch

Create `com.top_logic.layout.configedit.ConfigFieldDispatch`:

- Static method: `createPlainControl(ReactContext, ConfigFieldModel) → ReactControl`
- Type mapping:
  - `String` → `ReactTextInputControl`
  - `boolean`/`Boolean` → `ReactCheckboxControl`
  - `int`/`Integer`/`long`/`Long` → `ReactNumberInputControl`
  - `double`/`Double`/`float`/`Float` → `ReactNumberInputControl`
  - `Enum` → `ReactSelectFormFieldControl` (with enum constants as options)
  - Default fallback → `ReactTextInputControl`

**Test**: `TestConfigFieldDispatch`
- Verify each type maps to expected control class
- Verify enum creates select with correct options

### Step 1.5: ConfigEditorControl

Create `com.top_logic.layout.configedit.ConfigEditorControl`:

- Extends `ReactControl`
- Constructor: `(ReactContext context, ConfigurationItem config)`
- Iterates `config.descriptor().getProperties()`
- For each PLAIN/REF property (skip ITEM/LIST/DERIVED for now):
  1. Create `ConfigFieldModel`
  2. Call `ConfigFieldDispatch.createPlainControl()` for input control
  3. Wrap in `ReactFormFieldChromeControl` with label from `Labels.propertyLabel()`
  4. Add to children list
- `putState("children", chromeControls)` — uses `"TLFormLayout"` React module
- `cleanupChildren()`: cleanup all child controls

**Test**: `TestConfigEditorControl`
- Create config with mix of String, int, boolean, enum properties
- Verify correct number of children created
- Verify each child is a `ReactFormFieldChromeControl`
- Verify labels resolve correctly

### Step 1.6: Build and Verify

- `mvn install -DskipTests=true` on the new module
- `mvn test -DskipTests=false` to run all tests
- Fix any compilation or test issues

---

## Phase 2: ITEM Properties (Nested Configs)

### Step 2.1: ConfigItemControl (Monomorphic)

Create `com.top_logic.layout.configedit.ConfigItemControl`:

- Extends `ReactControl`, module `"TLConfigItemGroup"`
- Handles `PropertyKind.ITEM` properties
- For monomorphic items (non-PolymorphicConfiguration type):
  - Get current value from `config.value(property)`
  - If null and mandatory: create default instance via `TypedConfiguration.newConfigItem()`
  - Create nested `ConfigEditorControl` for the item's config
  - Put as `state.content`
- Register `ConfigurationListener` for property — rebuild nested editor on value change
- `state.label`: property label
- `state.collapsed`: from `@DisplayMinimized` annotation

### Step 2.2: ConfigItemControl (Polymorphic)

Extend `ConfigItemControl` for `PolymorphicConfiguration` properties:

- Detect polymorphic: `PolymorphicConfiguration.class.isAssignableFrom(property.getType())`
- Build type options from `property.getValueDescriptor()` and concrete subtypes
- Put `state.typeOptions` as list of `{value: className, label: displayName}`
- Put `state.selectedType` as current implementation class name
- Handle `@ReactCommand("typeChanged")`:
  1. Create new config instance of selected type
  2. Copy compatible property values from old to new config
  3. Update parent config via `config.update(property, newInstance)`
  4. Listener triggers `rebuildNestedEditor()`

### Step 2.3: TLConfigItemGroup (TypeScript)

New React component in `com.top_logic.layout.react` (or in the new module if it gets its own TS build):

- Renders collapsible group with label header
- If `state.typeOptions`: renders type selector dropdown
- Renders `<TLChild control={state.content} />` for nested form
- Toggle collapse on header click

### Step 2.4: Integrate into ConfigEditorControl

Update `ConfigEditorControl.createPropertyControl()`:
- For `PropertyKind.ITEM`: create `ConfigItemControl` instead of skipping
- Pass parent config + property descriptor

### Step 2.5: Tests

- `TestConfigItemControl`: monomorphic item with nested properties
- `TestConfigItemControl`: polymorphic item with type switching
- Test that changing nested property values propagates correctly

---

## Phase 3: LIST/MAP Properties

### Step 3.1: ConfigListControl

Create `com.top_logic.layout.configedit.ConfigListControl`:

- Extends `ReactControl`, module `"TLConfigList"`
- Constructor: iterate current list, create `ConfigEditorControl` per entry
- State: `entries` (list of child controls), `label`, `addOptions` (if polymorphic)
- Register `ConfigurationListener` for ADD/REMOVE events on the list property
- Commands:
  - `addEntry(type?)`: create new instance, add to config list
  - `removeEntry(index)`: remove from config list, cleanup child control
  - `moveEntry(from, to)`: reorder list (deferred to Phase 4)
- Entry titles: resolve `@TitleProperty` if annotated, else use type name + index

### Step 3.2: TLConfigList (TypeScript)

New React component:

- Header: label + count badge
- Entry list: each entry as collapsible card
  - Title bar (from `@TitleProperty` or type name)
  - Remove button
  - Nested form via `<TLChild />`
- Add button at bottom
  - If polymorphic: dropdown to select type before adding
  - If monomorphic: direct add

### Step 3.3: TLConfigListEntry (TypeScript)

New React component for individual list entries:

- Collapsible card with title
- Remove button in header
- Content: `<TLChild control={state.content} />`

### Step 3.4: Integrate into ConfigEditorControl

Update `ConfigEditorControl.createPropertyControl()`:
- For `PropertyKind.LIST`/`ARRAY`/`MAP`: create `ConfigListControl`

### Step 3.5: Tests

- `TestConfigListControl`: add, remove entries
- `TestConfigListControl`: polymorphic list with type selection
- `TestConfigListControl`: map property with key constraints

---

## Phase 4: Advanced Features

### Step 4.1: @Options Annotation

- In `ConfigFieldDispatch`: detect `@Options` annotation on property
- Evaluate `OptionsFunction` to get option list
- Create `ConfigSelectFieldModel` with resolved options
- Handle `@OptionLabels` for display labels
- Handle option mapping via `OptionMapping`

### Step 4.2: @DynamicMode

- In `ConfigEditorControl`: after building form, evaluate `@DynamicMode` functions
- Set `ConfigFieldModel.setEditable(false)` for IMMUTABLE/LOCALLY_IMMUTABLE modes
- Register listener on referenced properties to re-evaluate mode on changes

### Step 4.3: @Format Support

- Create `ConfigFormattedFieldControl` extending `ReactTextInputControl`
- Override `parseClientValue()` to use `ConfigurationValueProvider.getValue(String)`
- Override `handleModelValueChanged()` to use `ConfigurationValueProvider.getSpecification(Object)`
- Wire in `ConfigFieldDispatch` for properties with explicit format

### Step 4.4: @DisplayMinimized

- Already partially supported in `ConfigItemControl` (reads annotation, sets `state.collapsed`)
- Also apply to `ConfigListControl` entries

### Step 4.5: Constraint Evaluation

- Inspect `@ConstraintAnnotation` meta-annotations on property annotations
- Create constraint instances and add to `ConfigFieldModel` via `addConstraint()`
- Evaluate constraints eagerly on value change (via `validate()`)
- Errors propagate through `FieldModel.hasError()` → chrome error display

### Step 4.6: @TitleProperty for List Entries

- Detect `@TitleProperty` annotation on list element type
- Resolve title property value from each entry's config
- Listen for title property changes to update entry header
- Send title as state to `TLConfigListEntry`

### Step 4.7: Drag-and-Drop Reordering

- Add `moveEntry` command to `ConfigListControl` (from/to indices)
- TypeScript: implement drag-and-drop in `TLConfigList` using HTML5 Drag API
- On drop: send `moveEntry` command
- Server reorders list, patches state

---

## Phase 5: View System Integration (Optional)

### Step 5.1: ConfigEditorElement

Create `UIElement` for use in `.view.xml`:

```xml
<config-editor input="configChannel" />
```

- Reads `ConfigurationItem` from input channel
- Creates `ConfigEditorControl` in `createControl()`
- Optionally publishes dirty state to output channel

This step requires a dependency on `com.top_logic.layout.view` — can be in a separate integration module or added to the existing `com.top_logic.layout.view` module.

---

## Implementation Order Summary

| Step | Deliverable | Dependencies |
|------|-------------|-------------|
| 1.1 | Module skeleton | None |
| 1.2 | ConfigFieldModel + tests | 1.1 |
| 1.3 | ConfigSelectFieldModel + tests | 1.2 |
| 1.4 | ConfigFieldDispatch + tests | 1.2, 1.3 |
| 1.5 | ConfigEditorControl + tests | 1.4 |
| 1.6 | Build verification | 1.5 |
| 2.1 | ConfigItemControl (mono) | 1.5 |
| 2.2 | ConfigItemControl (poly) | 2.1 |
| 2.3 | TLConfigItemGroup (TS) | 2.2 |
| 2.4 | Integration + tests | 2.3 |
| 3.1 | ConfigListControl | 2.4 |
| 3.2 | TLConfigList (TS) | 3.1 |
| 3.3 | TLConfigListEntry (TS) | 3.2 |
| 3.4 | Integration + tests | 3.3 |
| 4.x | Advanced features | 3.4 |
| 5.1 | View integration | 4.x |
