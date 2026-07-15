# LIST Property Editor for ConfigEditorControl

## Goal

Add full LIST property editing support to `ConfigEditorControl` so that non-`@TreeProperty` list properties (channels, bindings, executability rules, etc.) can be viewed and edited inline in the configuration form.

## Current State

`ConfigEditorControl` supports PLAIN, REF, and ITEM property kinds. LIST properties were recently added as read-only collapsible groups, but without Add/Remove/Move operations or dynamic labels. This design replaces that basic implementation with a full-featured list editor.

## Design

### Rendering

Each LIST property is rendered as an **outer collapsible `ReactFormGroupControl`** with the property's I18N label. Inside:

- Each list element is a **nested collapsible `ReactFormGroupControl`** containing:
  - A **dynamic label** derived from the element's title property (see Label Resolution below)
  - **Icon buttons** in the group header: Move Up, Move Down, Remove
  - A nested `ConfigEditorControl` for the element's properties
- An **Add button** at the bottom of the list
- For polymorphic lists (`List<PolymorphicConfiguration<...>>`), new elements are created with the default type. The user can change the type via the `implementation-class` field in the element editor (consistent with existing `PolymorphicItemControl` behavior).

### Operations

All operations modify the live `ConfigurationItem` list and trigger a re-render of the list container:

- **Add**: Creates a new `ConfigurationItem` via `TypedConfiguration.newConfigItem()` using the element type from `PropertyDescriptor.getElementType()`. For polymorphic lists, uses the default descriptor. Appends to the list.
- **Remove**: Removes the element from the list.
- **Move Up**: Swaps element with its predecessor in the list.
- **Move Down**: Swaps element with its successor in the list.

After each operation, the list control rebuilds its children to reflect the new list state.

### Label Resolution

Group labels for list elements are **dynamic** — they update when the user edits the title property value.

Resolution order:
1. `@TitleProperty` annotation on the LIST property or element type — specifies which property to use as the title
2. `PropertyDescriptor.getKeyProperty()` — the key property of the list element descriptor
3. Fallback: property named `name`, then `id`
4. Last resort: TagName annotation or simple interface name + 1-based index

A `ConfigurationListener` is registered on the title property of each element. When the value changes, the `ReactFormGroupControl` label is updated via `putState()`.

### New Control: `ConfigListEditorControl`

A new `ConfigListEditorControl extends ReactFormLayoutControl` encapsulates the list editing logic. `ConfigEditorControl` creates a `ConfigListEditorControl` for each LIST property (instead of handling it inline in its constructor).

Responsibilities of `ConfigListEditorControl`:
- Holds a reference to the parent `ConfigurationItem`, the `PropertyDescriptor`, and the `ReactContext`
- Builds child controls for each list element (nested `ConfigEditorControl` wrapped in `ReactFormGroupControl` with action buttons)
- Provides `addElement()`, `removeElement(int)`, `moveUp(int)`, `moveDown(int)` methods
- After each mutation, calls a `rebuild()` method that clears children and recreates them from the current list state
- Registers `ConfigurationListener` on title properties to update group labels dynamically

### Integration with ConfigEditorControl

In `ConfigEditorControl`'s constructor, the LIST handling becomes:

```
if (property.kind() == PropertyKind.LIST) {
    String label = resolveLabel(property);
    ConfigListEditorControl listEditor = new ConfigListEditorControl(context, config, property);
    ReactFormGroupControl group = new ReactFormGroupControl(
        context, label, true, false, "default", false,
        List.of(), List.of(listEditor));
    addChild(group);
    continue;
}
```

### Dirty Tracking

List mutations (add, remove, move) are performed directly on the `ConfigurationItem`'s list property. The existing `ConfigurationListener` mechanism in `ConfigEditorElement.installDirtyTracking()` already listens on all properties, so dirty tracking works automatically.

### Out of Scope

- Table-based rendering mode (future work, selectable via annotation)
- Drag-and-drop reordering (up/down buttons are sufficient)
- Inline validation of list constraints (min/max size)

## Files

- Create: `com.top_logic.layout.configedit/src/main/java/com/top_logic/layout/configedit/ConfigListEditorControl.java`
- Modify: `com.top_logic.layout.configedit/src/main/java/com/top_logic/layout/configedit/ConfigEditorControl.java` (replace basic LIST handling with `ConfigListEditorControl`)
