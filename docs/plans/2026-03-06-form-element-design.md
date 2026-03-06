# FormElement Design for the View System

**Ticket**: #29108
**Date**: 2026-03-06
**Module**: `com.top_logic.layout.view` (artifact: `tl-layout-view`)

## Overview

The `FormElement` brings data-binding and editing capabilities to the new view system. It provides a declarative way to display and edit TLObject attributes using `<form>` and `<field>` elements in view XML files. The form manages its own editing lifecycle (view/edit mode, locking, transactions) and uses a lean overlay pattern for transient editing state.

## Design Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Field source | Explicit `<field>` elements in XML | Full control over which fields appear; type/annotations provide defaults |
| Edit buffer | Overlay pattern (Map of changed values) | Avoids composition-copy scope issues; proven pattern from legacy FormObjectOverlay |
| Overlay impl | New lean `TLObjectOverlay` in view module | No dependency on `com.top_logic.element`; much simpler than FormObjectOverlay |
| Form structure | Form-as-container (Approach A) | Fields mix freely with layout elements (stack, grid); composable |
| Edit mode | Form-internal with channel exposure | Form owns lock/transaction/overlay; exposes state via channels |
| Validation | Deferred to later phase | Existing constraint system depends on legacy FormField; use pure model attributes for now |
| Dependencies | View module only (tl-core APIs) | No dependency on `com.top_logic.element`; lean field-creation from model types |
| Field chrome | Reuse `ReactFormFieldChromeControl` | Existing `TLFormField` React component provides label/required/dirty/error wrapper |
| Field labels | `MetaLabelProvider.INSTANCE.getLabel(part)` | Internationalized display label from the attribute definition |

## 1. TLObjectOverlay

A lean overlay implementing `TLObject` that wraps a persistent object and intercepts writes into a local `Map<TLStructuredTypePart, Object>`. Unchanged attributes delegate to the base object.

```java
public class TLObjectOverlay extends TransientObject {
    private final TLObject _base;           // the persistent object (null for creation)
    private final Map<TLStructuredTypePart, Object> _changes = new LinkedHashMap<>();

    public TLStructuredType tType() { return _base.tType(); }

    public Object tValue(TLStructuredTypePart part) {
        if (_changes.containsKey(part)) {
            return _changes.get(part);
        }
        return _base.tValue(part);
    }

    public void tUpdate(TLStructuredTypePart part, Object value) {
        _changes.put(part, value);
    }

    // Form-specific:
    public boolean isDirty() { return !_changes.isEmpty(); }

    public boolean isChanged(TLStructuredTypePart part) {
        return _changes.containsKey(part);
    }

    public void applyTo(TLObject target) {
        for (var entry : _changes.entrySet()) {
            target.tUpdate(entry.getKey(), entry.getValue());
        }
    }

    public void reset() { _changes.clear(); }
}
```

Key points:
- Extends `TransientObject` (from tl-core) -- no dependency on `com.top_logic.element`
- `containsKey` check distinguishes "not changed" from "changed to null"
- `applyTo()` transfers accumulated changes to the persistent object
- `reset()` discards all changes (cancel/post-apply)
- For object creation, `_base` would be `null` and all attributes come from `_changes`

## 2. FormElement

The `<form>` element is a container that manages the editing lifecycle. It receives the edited object via an input channel and provides the editing context to nested `<field>` elements.

### Config

```java
@TagName("form")
public interface Config extends ContainerElement.Config {
    // Input: the object being displayed/edited
    @Name("input")
    @Mandatory
    @Format(ChannelRefFormat.class)
    ChannelRef getInput();

    // Optional output channel: exposes edit mode state
    @Name("editMode")
    @Format(ChannelRefFormat.class)
    ChannelRef getEditMode();

    // Optional output channel: exposes dirty state
    @Name("dirty")
    @Format(ChannelRefFormat.class)
    ChannelRef getDirty();
}
```

### createControl() flow

1. Resolve input channel to get the `TLObject` to display
2. Create a `FormControl` (the session-scoped React control)
3. Store `FormControl` in `ViewContext` for child access: `context.setFormControl(formControl)`
4. Create child controls (they access the form via `ViewContext.getFormControl()`)
5. Wire input channel listener: when input object changes, exit edit mode and reset

### ViewContext extension

```java
public class ViewContext {
    // New: form context slot
    private FormControl _formControl;

    public void setFormControl(FormControl control) { _formControl = control; }
    public FormControl getFormControl() { return _formControl; }
}
```

Child contexts created via `childContext()` inherit the form control reference, so `<field>` elements at any nesting depth can access the enclosing form.

## 3. FormControl (Session-Scoped State)

The `FormControl` manages the full editing lifecycle per session.

### State

```java
public class FormControl extends ReactControl {
    private final ViewChannel _inputChannel;
    private final ViewChannel _editModeChannel;  // nullable
    private final ViewChannel _dirtyChannel;      // nullable

    private TLObject _currentObject;        // the persistent object from input channel
    private TLObjectOverlay _overlay;       // non-null only in edit mode
    private boolean _editMode;

    private final List<FieldControl> _fields = new ArrayList<>();

    void registerField(FieldControl field) { _fields.add(field); }
}
```

### Edit lifecycle state machine

```
         +------------------------------+
         |         VIEW MODE            |
         |  _overlay = null             |
         |  _editMode = false           |
         |  fields read from _current   |
         +-------------+----------------+
                       | edit command
                       | (acquire lock, create overlay)
                       v
         +------------------------------+
         |         EDIT MODE            |<--+
         |  _overlay = new Overlay(obj) |   | apply command
         |  _editMode = true            |   | (validate, commit,
         |  fields read/write overlay   |---+  reset overlay)
         +-------+-------------+--------+
                 |             |
      save cmd   |             | cancel command
      (validate, |             | (overlay.reset(),
       commit,   |             |  release lock)
       release)  |             |
                 v             v
         +------------------------------+
         |         VIEW MODE            |
         +------------------------------+
```

### Internal commands

- **edit**: Create `TLObjectOverlay` wrapping the input object, acquire lock, set editMode=true
- **apply**: Validate, open KB transaction, `overlay.applyTo(baseObject)`, commit, reset overlay, keep editMode=true
- **save**: Same as apply, then release lock, set editMode=false
- **cancel**: Discard overlay (`reset()`), release lock, set editMode=false

### Input channel change handling

When the input channel value changes (user selects a different object):
1. If in edit mode: release lock, discard overlay, exit edit mode
2. Set `_currentObject` to the new value
3. Notify all registered `FieldControl`s to re-render with the new object

### Channel synchronization

- When `_editMode` changes: write to `editModeChannel` (if bound)
- When overlay dirty state changes: write to `dirtyChannel` (if bound)
- These are output-only: the form owns the state, channels just expose it

### Command executability

- **edit**: executable when not in edit mode and input is non-null
- **apply/save**: executable when in edit mode and dirty
- **cancel**: executable when in edit mode

## 4. FieldElement and FieldControl

### FieldElement Config

```java
@TagName("field")
public interface Config extends UIElement.Config {
    @Name("attribute")
    @Mandatory
    String getAttribute();

    @Name("label")
    ResKey getLabel();

    @Name("readonly")
    boolean getReadonly();
}
```

### FieldControl (Per-Field Session State)

```java
public class FieldControl extends ViewControl {
    private final FormControl _form;
    private final String _attributeName;
    private final ResKey _labelOverride;
    private final boolean _forceReadonly;

    private TLStructuredTypePart _resolvedPart;
    private ReactFormFieldChromeControl _chrome;   // wrapper (label, error, required)
    private ReactControl _innerControl;            // actual input widget
}
```

### Control tree per field

```
FieldControl (ViewControl, manages lifecycle)
  +-- ReactFormFieldChromeControl ("TLFormField")
       +-- label: "Customer Name"
       +-- required: true
       +-- dirty: false
       +-- error: null
       +-- field: ReactTextFieldControl ("TLTextField")
```

### createControl() flow

1. Get `FormControl` from `ViewContext.getFormControl()`
2. Register with the form: `formControl.registerField(this)`
3. Resolve attribute against `formControl.getCurrentObject().tType()` to get `TLStructuredTypePart`
4. Determine label: `_labelOverride != null ? _labelOverride : MetaLabelProvider.INSTANCE.getLabel(_resolvedPart)`
5. Create inner input control via `FieldControlFactory.createFieldControl(part, value, editable)`
6. Wrap in `ReactFormFieldChromeControl(label, part.isMandatory(), false, null, null, null, false, true, innerControl)`
7. Return the chrome control

### State updates at runtime

- Edit mode changes: rebuild inner control as editable/readonly
- Value changes: update inner control value
- Dirty state: `_chrome.setDirty(overlay.isChanged(part))`
- Validation errors (future): `_chrome.setError(message)`

### Value change handling (client to server)

1. React widget sends update command with typed value
2. Inner `ReactControl` handles its own serialization/deserialization
3. `FieldControl` receives the typed value and writes to overlay: `_form.getOverlay().tUpdate(_resolvedPart, typedValue)`
4. Form updates dirty channel

Field controls present typed values (Boolean, Date, Long, etc.) to the application layer. Client/server serialization is an internal concern of each React control.

## 5. FieldControlFactory

Maps `TLStructuredTypePart` to the appropriate React input control based on the attribute's TL type.

```java
public class FieldControlFactory {

    public static ReactControl createFieldControl(
            TLStructuredTypePart part, Object value, boolean editable) {

        TLType type = part.getType();

        if (type == lookupType("tl.core:Boolean")) {
            return new ReactCheckboxControl((Boolean) value, editable);
        }
        if (type == lookupType("tl.core:String")) {
            return new ReactTextFieldControl((String) value, editable);
        }
        // ... Date, Integer, Long, Float, Double, references ...

        // Fallback: read-only text display
        return new ReactTextFieldControl(
            MetaLabelProvider.INSTANCE.getLabel(value), false);
    }
}
```

Type mapping:

| TL Type | React Control | Notes |
|---------|--------------|-------|
| `tl.core:String` | `ReactTextFieldControl` | Single-line text input |
| `tl.core:Text` | `ReactTextAreaControl` | Multi-line text area |
| `tl.core:Boolean` | `ReactCheckboxControl` | Checkbox |
| `tl.core:Integer`, `Long` | `ReactNumberFieldControl` | Number input |
| `tl.core:Float`, `Double` | `ReactNumberFieldControl` | Number input with decimals |
| `tl.core:Date` | `ReactDateFieldControl` | Date picker |
| `tl.core:DateTime` | `ReactDateTimeFieldControl` | Date+time picker |
| Reference (single) | `ReactSelectControl` | Dropdown/popup select |
| Reference (multiple) | `ReactMultiSelectControl` | Multi-select |
| Unknown/fallback | `ReactTextFieldControl` | Display as text via MetaLabelProvider |

This is intentionally simple -- a plain type switch. Can be extended later with annotation-driven overrides.

## 6. Save/Apply Flow and KB Integration

### Save command

```java
void executeSave(ViewDisplayContext context) {
    TLObject base = _currentObject;
    TLObjectOverlay overlay = _overlay;

    KnowledgeBase kb = base.tHandle().getKnowledgeBase();
    Transaction tx = kb.beginTransaction();
    try {
        overlay.applyTo(base);
        tx.commit();
    } finally {
        tx.rollback();  // no-op if already committed
    }

    releaseLock(base);
    _overlay = null;
    _editMode = false;
    updateChannels();
    notifyFields();
}
```

### Apply command

Same transaction logic, but keeps edit mode and resets overlay:

```java
void executeApply(ViewDisplayContext context) {
    // ... same transaction logic ...

    _overlay.reset();  // changes committed, overlay now reads through to updated base
    // Stay in edit mode, keep lock
    updateDirtyChannel();
    notifyFields();
}
```

### Cancel command

```java
void executeCancel(ViewDisplayContext context) {
    releaseLock(_currentObject);
    _overlay = null;
    _editMode = false;
    updateChannels();
    notifyFields();
}
```

### Lock management

- `edit` command: acquire lock via `LockService` on the base object
- `save`/`cancel`: release lock
- Input channel changes while in edit mode: release lock, exit edit mode

### Object creation (future extension)

For creating new objects, the `<form>` could receive a `TLClass` (the type to create) instead of a `TLObject` on the input channel. The overlay would have `_base = null` and all values come from `_changes`. On save, a new persistent object is created via `KnowledgeBase.createObject()` and the overlay values are applied.

## 7. Complete Example

```xml
<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
  <channels>
    <channel name="selectedCustomer"/>
    <channel name="isEditing"/>
    <channel name="isDirty"/>
  </channels>

  <split-panel>
    <pane size="300" unit="pixel">
      <table selection="selectedCustomer">
        <inputs><input channel="selectedCustomer"/></inputs>
        <rows>all(`my.module:Customer`)</rows>
        <types>my.module:Customer</types>
      </table>
    </pane>

    <pane>
      <panel title="Customer Details">
        <form input="selectedCustomer" editMode="isEditing" dirty="isDirty">
          <stack direction="vertical" gap="8">
            <field attribute="name"/>
            <field attribute="email"/>
            <grid minColumnWidth="200">
              <field attribute="street"/>
              <field attribute="city"/>
              <field attribute="zipCode"/>
              <field attribute="country"/>
            </grid>
            <field attribute="notes" readonly="true"/>
          </stack>
        </form>
      </panel>
    </pane>
  </split-panel>
</view>
```

### Runtime flow

1. View loads, channels created, table populates
2. User clicks a row: `selectedCustomer` channel receives the `TLObject`
3. `FormControl` receives the object, fields resolve attributes and render in view mode
4. User clicks "Edit" (internal form command): form acquires lock, creates `TLObjectOverlay`, fields become editable
5. User edits "email": `ReactTextFieldControl` sends typed value, `FieldControl` writes to overlay, dirty channel becomes true
6. User clicks "Save": form validates, opens KB transaction, `overlay.applyTo(customer)`, commits, releases lock, exits edit mode
7. User clicks "Cancel" instead: overlay discarded, lock released, fields revert to persistent values

Command visibility is driven by the form's edit mode channel:
- "Edit" button: visible when `isEditing = false` and `selectedCustomer != null`
- "Save"/"Cancel" buttons: visible when `isEditing = true`

## Summary of New Classes

| Class | Package | Purpose |
|-------|---------|---------|
| `TLObjectOverlay` | `c.t.l.layout.view.form` | Lean overlay over persistent TLObject |
| `FormElement` | `c.t.l.layout.view.element` | `<form>` UIElement with Config |
| `FormControl` | `c.t.l.layout.view.form` | Session-scoped form lifecycle manager |
| `FieldElement` | `c.t.l.layout.view.element` | `<field>` UIElement with Config |
| `FieldControl` | `c.t.l.layout.view.form` | Session-scoped field state manager |
| `FieldControlFactory` | `c.t.l.layout.view.form` | Type-to-ReactControl mapping |
