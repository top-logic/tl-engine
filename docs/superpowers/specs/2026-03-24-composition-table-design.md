# Composition Table for React UI

**Date**: 2026-03-24
**Ticket**: #29108
**Status**: Draft

## Problem

The React UI (`FormElement` / `FieldElement`) currently supports editing only primitive attributes of a TL object. Composition references — where the lifecycle of composed objects is bound to the referencing object — cannot be edited. The legacy UI handles this via `CompositionFieldProvider`, presenting a table with inline editing and optional detail dialogs. The React UI needs equivalent functionality.

## Design

### New UIElement: `<composition-table>`

A new `CompositionTableElement` alongside `FieldElement`, placed inside a `<form>` (`FormElement`). It renders a composition reference as an editable table.

```xml
<composition-table attribute="uses">
    <columns>
        <column attribute="name"/>
        <column attribute="date"/>
        <column attribute="amount" readonly="true"/>
    </columns>
    <detail-dialog layout="path/to/detail-layout.xml"
                   input-channel="editedObject"
                   edit-mode-channel="editMode"
                   width="600" height="400"/>
</composition-table>
```

**Configuration:**
- `attribute` (mandatory): Name of the composition reference on the base object.
- `columns`: Which attributes of the composed type appear as table columns. Each column can be marked `readonly`.
- `detail-dialog` (optional): Reference to a separate layout fragment that defines the detail editing form. `input-channel` names the channel receiving the row overlay. `edit-mode-channel` names the channel receiving the edit mode state (boolean). Width/height configure the dialog window.

Without `detail-dialog`, only inline table editing is available.

### Panel Wrapper and UI Layout

The composition table is wrapped in a `TLPanel` (existing component) that provides:
- **Title**: The attribute's display label (e.g., "Items")
- **Toolbar**: Contains the Add button (only visible in edit mode)

**Table columns** (in addition to the configured data columns):
- **Detail-Open column**: Always visible (view and edit mode). A button per row that opens the detail dialog (if configured).
- **Delete column**: Only visible in edit mode. A button per row that removes the row from the composition.

These action columns are injected by `CompositionTableControl` — not configured in XML.

**Add button**: Appears in the panel toolbar, only in edit mode. Creates a new transient object and adds it as a row.

### Detail Dialog as Separate Layout

The detail dialog is defined as a standalone layout fragment containing a `FormElement` with `FieldElement`s (and potentially nested `composition-table`s). It receives the row object and edit mode state via channels.

**Configuration:**
```xml
<detail-dialog layout="demo/edit-constraint-test-item.view.xml"
               input-channel="editedItem"
               edit-mode-channel="editMode"
               width="500" height="300"/>
```

When a user opens the detail dialog for a row:
1. `CompositionTableControl` pushes the row's overlay onto `input-channel` and the current edit mode (boolean) onto `edit-mode-channel`.
2. In edit mode: The dialog's `FormElement` creates its own overlay on top of the row overlay (recursive overlay pattern). Save/cancel buttons are visible.
3. In view mode: The dialog shows the row's data read-only. Only a close button is visible.
4. Dialog Save applies the dialog overlay onto the row overlay — table cells see the changes immediately.
5. Dialog Cancel discards the dialog overlay — the row overlay remains unchanged.

This is not a special case. The dialog is a normal form whose input happens to be an overlay instead of a persistent object. `TLObjectOverlay extends TransientObject` implements `TLObject`, so overlays stack naturally.

**Edit mode propagation:** `FormElement.editMode` becomes bidirectional — it receives the initial state from the channel AND publishes changes back. The dialog form does NOT use `initial-edit-mode`; it receives the edit mode from the parent form via the channel.

**Locking:** Since the dialog's input is a transient overlay (not a persistent object), the dialog's `FormElement` must not attempt to acquire a lock. Configure the dialog form with `mode-switch="false"` to bypass locking.

**Dialog layout example:**

```xml
<channels>
    <channel name="editedItem"/>
    <channel name="editMode"/>
</channels>
<window title="Edit Item" width="500px" height="300px">
    <form input="editedItem" editMode="editMode" mode-switch="false">
        <save-action>
            <store-form-state/>
            <close-dialog/>
        </save-action>
        <cancel-action>
            <close-dialog/>
        </cancel-action>
        <field attribute="name"/>
        <field attribute="quantity"/>
        <field attribute="unitPrice"/>
    </form>
</window>
```

`<store-form-state/>` validates, reveals errors, and applies the dialog overlay onto the row overlay. `<close-dialog/>` then closes the dialog window. If validation fails, `<store-form-state/>` throws a `TopLogicException` which aborts the chain — the dialog stays open with errors visible. Save/cancel actions are only visible in edit mode; in view mode only a close button is shown.

### Two-Layer Overlay Model

**Layer 1 — Composition reference in the main overlay:**
- On entering edit mode, `CompositionTableControl` creates a row overlay (`TLObjectOverlay`) for each existing composed object, plus keeps transient objects for new entries.
- The composition reference value in the main overlay is set to the list of **row overlays and transient objects** (not the original persistent objects). This ensures navigational consistency: `overlay.tValue(compositionRef)` returns overlays, and `rowOverlay.tValue(attribute)` returns the transient value.
- Constraints navigating from the base overlay through the composition reference automatically see the current transient state at every level.
- Add/delete operations modify this list directly (adding transient objects, removing overlays).
- The main overlay becomes dirty.

**Layer 2 — Row overlays for existing composed objects:**
- Each already-persistent composed object gets its own `TLObjectOverlay`, which is stored in the Layer 1 list.
- `AttributeFieldModel` instances in table cells and detail dialog work on these row overlays.
- New (transient) objects are stored directly in the Layer 1 list (no overlay needed since there is no persistent state to buffer against).

**Deletion semantics:** An object is deleted when it is no longer present in the overlay's reference list. On save, objects that existed in the persistent state but are absent from the overlay value are deleted. This follows naturally from composition semantics.

**Important:** `TLObjectOverlay.applyTo()` only copies attribute values — it does not delete orphaned composed objects. `CompositionTableControl.apply()` must explicitly compute the set difference (objects in the persistent reference but absent from the overlay list) and delete them within the same transaction.

### No New React Components

The composition table is assembled entirely from existing building blocks:

- **`ReactTableControl`** + **`ReactCellControlProvider`** — Table rendering with virtual scrolling. The cell provider creates `ReactFormFieldControl` instances for editable columns (working on row overlays) and text cell controls for readonly/view mode.
- **`DialogManager`** + **`ReactWindowControl`** — Detail dialog window.
- **`ReactFormFieldControl` subclasses** — Editable fields in both table cells and detail dialog.
- **`ReactCompositeControl`** — Container for dialog content.

Table cells and detail dialog fields on the same row work on the same row overlay — synchronization is automatic.

### Generic Form Abstraction: `FormParticipant`

`FormControl` currently knows only `AttributeFieldControl` instances. To support composition tables (and future sub-forms) without adding special cases, a uniform interface is introduced:

```java
interface FormParticipant {
    boolean validate();
    void apply(Transaction tx);
    void cancel();
    void revealAll();
    boolean isDirty();
}
```

`FormControl` maintains a list of `FormParticipant`s and iterates over them uniformly:

- **Save:** `participants.forEach(validate)` — abort on error — `participants.forEach(apply)` — commit
- **Cancel:** `participants.forEach(cancel)`
- **Reveal:** `participants.forEach(revealAll)`
- **Dirty:** `participants.any(isDirty)`

`AttributeFieldControl` and `CompositionTableControl` both implement `FormParticipant`. Future participants (inline sub-forms, etc.) plug in identically without changing `FormControl`.

**Migration note:** `FormControl` must be refactored to delegate save/cancel/validate to `FormParticipant` instances instead of directly managing overlays and field models. `AttributeFieldControl`'s `apply()` is a no-op — the main overlay's `applyTo()` already handles primitive attribute changes. Its `validate()`, `cancel()`, `revealAll()`, and `isDirty()` delegate to the existing `AttributeFieldModel` logic.

### New Java Classes

**`com.top_logic.layout.view.element.CompositionTableElement`:**
UIElement that parses the `<composition-table>` configuration and creates a `CompositionTableControl`. Must be placed inside a `FormElement` scope.

**`com.top_logic.layout.view.form.CompositionTableControl`:**
Orchestrates the composition table. Responsibilities:
- Creates and configures a `ReactTableControl` with appropriate cell providers.
- Manages row overlays for existing objects, transient objects for new ones.
- Maintains the composition reference value in the main overlay.
- Provides add/delete commands (with type selection dialog when multiple subtypes exist).
- Opens detail dialog via `DialogManager` when configured.
- Implements `FormParticipant`.
- Propagates dirty state to the main form.

**`com.top_logic.layout.view.form.CompositionFieldModel`:**
`FieldModel` implementation for the composition reference as a whole. Its value is the current list of composed objects in the overlay. Reference-level constraints (min/max count, etc.) apply here. Registers with `FormControl` for reveal-all propagation.

**`com.top_logic.layout.view.form.CompositionRowModel`:**
Lightweight per-row model holding a reference to the row overlay (or transient object) and the `AttributeFieldModel` instances for the row's columns.

**`com.top_logic.layout.view.form.FormParticipant`:**
Interface for uniform form lifecycle participation (validate, apply, cancel, revealAll, isDirty).

**`com.top_logic.layout.view.command.DiscardFormStateAction`:**
New `ViewAction` (tag `<discard-form-state/>`) that discards the form overlay without applying changes. Used in cancel-action chains for detail dialogs.

### Configurable Save/Cancel Actions on FormElement

`FormElement` currently hard-codes save/cancel behavior in `FormCommandModel` (calling `FormControl.executeSave()` / `executeCancel()` directly). To support the detail dialog use case — and any future customization — `FormElement` gets optional `save-action` and `cancel-action` configuration as `ViewAction` chains.

```xml
<form input="..." save-action="..." cancel-action="...">
```

When configured, the save/cancel buttons execute the configured `ViewAction` chain (via `GenericViewCommand`) instead of the default behavior.

**Default behavior** (when not configured): The existing hard-coded `FormControl.executeSave()` / `executeCancel()` logic remains unchanged. No migration needed for existing forms.

**Available actions for composition:**
- `<store-form-state/>` — validates, reveals errors, applies overlay to base object (already exists)
- `<close-dialog/>` — closes the topmost dialog (already exists)
- `<with-transaction>...</with-transaction>` — wraps inner actions in a KB transaction (already exists)

**New action needed:**
- `<discard-form-state/>` — discards the form overlay without applying (equivalent of cancel). This is a new `ViewAction` that calls the cancel logic on `FormControl`.

This keeps `FormElement` simple: it either uses the default behavior or delegates entirely to a configured action chain. No hybrid.

### Validation

**Two levels:**

1. **Field-level:** Constraints on individual attributes of composed objects. Each `AttributeFieldModel` validates as usual (mandatory, format, model constraints). Errors shown in table cells and detail dialog fields. `CompositionTableControl` manages its own per-row validation (encapsulated, not registered with the main `FormValidationModel`).

2. **Reference-level:** Constraints on the composition reference itself (e.g., minimum/maximum count, no duplicates). These run against the reference value in the main overlay. Errors shown at the composition table control level (e.g., below the table).

Reveal semantics follow the main form: validation errors are hidden until submit attempt or user interaction. `revealAll()` on the main form propagates through `FormParticipant` into all rows and fields.

### Dirty Tracking

- Row overlay changes (attribute edits) and reference list changes (add/delete) both propagate via `FormParticipant.isDirty()` to the main form's dirty channel.
- The main form's dirty indicator reflects changes across all participants uniformly.
- `CompositionFieldModel.isDirty()` must check both the reference list (added/removed objects) and whether any row overlay is dirty (attribute changes within composed objects). Simple list equality is not sufficient.

### Save Flow (single KB transaction)

1. All `FormParticipant`s validate — abort on any error.
2. All `FormParticipant`s apply, within a single KB transaction:
   - Row overlays apply attribute changes to existing objects.
   - Transient new objects are persisted (must happen before reference update, since KB expects persistent objects in reference values).
   - Composition reference on base object is updated.
   - Objects no longer in the reference list are explicitly deleted (must happen after reference update, to avoid dangling reference errors).
3. Transaction commit.

**The ordering within apply is a hard constraint**, not a convention. New objects must exist before they can be referenced; orphaned objects must be unreferenced before they can be deleted.

### Cancel Flow

1. All `FormParticipant`s cancel:
   - Row overlays are discarded.
   - Transient objects are cleaned up.
   - Composition reference in main overlay is reset.
2. Table reverts to persistent state.

### Transaction Semantics

All changes are transient until save. The composition table is fully integrated into the surrounding form's transaction:
- One Save persists everything (base object attributes + composition changes).
- One Cancel discards everything.
- No intermediate persistence of composed objects.

## Demo

Extend the existing constraint demo (`test.constraints` module in `com.top_logic.demo`) with composition editing to validate the implementation end-to-end.

### New Model Type: `ConstraintTestItem`

Added to `test.constraints.model.xml` as a composed child of `ConstraintTestType`:

```xml
<class name="ConstraintTestItem">
    <attributes>
        <property name="name" type="tl.core:String" mandatory="true"/>
        <property name="quantity" type="tl.core:Integer">
            <annotations><value-range min="1.0" max="1000.0"/></annotations>
        </property>
        <property name="unitPrice" type="tl.core:Double">
            <annotations><value-range min="0.01" max="99999.0"/></annotations>
        </property>
    </attributes>
</class>
```

### New Composition Reference on `ConstraintTestType`

```xml
<reference name="items"
    composite="true"
    multiple="true"
    type="ConstraintTestItem"/>
```

### Constraints Demonstrating Three Levels

1. **Field-level (on item attributes):** `name` is mandatory, `quantity` has range 1-1000, `unitPrice` has range 0.01-99999. Validates that field-level constraints work in table cells and detail dialog.

2. **Reference-level (on `items` reference):** Minimum 1 item required. Validates that reference-level constraints on the composition work.

3. **Cross-level (on `ConstraintTestType`, navigating through composition):** Expression-based constraint: *"The sum of `quantity * unitPrice` across all items must not exceed `rangedInt`."* This constraint navigates from the base overlay through the composition reference into item overlays — the key scenario that validates overlay navigational consistency.

### UI Changes

**`constraint-test.view.xml`:** Add `<composition-table attribute="items">` below the existing fields:

```xml
<composition-table attribute="items">
    <columns>
        <column attribute="name"/>
        <column attribute="quantity"/>
        <column attribute="unitPrice"/>
    </columns>
    <detail-dialog layout="demo/edit-constraint-test-item.view.xml"
                   input-channel="editedItem"
                   edit-mode-channel="editMode"
                   width="500" height="300"/>
</composition-table>
```

**New `edit-constraint-test-item.view.xml`:** Detail dialog layout for item editing:

```xml
<channels>
    <channel name="editedItem"/>
    <channel name="editMode"/>
</channels>
<window title="Edit Item" width="500px" height="300px">
    <form input="editedItem" editMode="editMode" mode-switch="false">
        <save-action>
            <store-form-state/>
            <close-dialog/>
        </save-action>
        <cancel-action>
            <close-dialog/>
        </cancel-action>
        <field attribute="name"/>
        <field attribute="quantity"/>
        <field attribute="unitPrice"/>
    </form>
</window>
```

**`create-constraint-test.view.xml`:** Also extended with the same `<composition-table>` for the create dialog.

### What the Demo Validates

- Inline editing in composition table cells with field-level constraints
- Detail dialog with overlay stacking (dialog overlay on top of row overlay)
- Reference-level constraint (min count) on the composition
- Cross-level expression constraint navigating base overlay → composition reference → row overlays (proves overlay navigational consistency)
- Dirty tracking propagation from items to main form
- Save/cancel semantics across all layers

## Verification

Use Playwright (browser automation) to verify the implementation against the running demo application. Start the app, navigate to the constraint demo, and interactively test until all scenarios work:

- Create a `ConstraintTestType` with items via the composition table
- Inline-edit item fields, verify field-level constraints fire in cells
- Open detail dialog, edit, save — verify values sync back to table
- Open detail dialog, edit, cancel — verify changes are discarded
- Delete an item, verify reference list updates and dirty state
- Trigger the cross-level constraint (sum exceeds `rangedInt`) — verify error appears
- Save the form — verify all layers persist correctly
- Cancel the form after edits — verify everything reverts

This is not a permanent test suite but a hands-on verification loop during development.

## Scope

**In scope:**
- `CompositionTableElement` with column configuration
- Panel wrapper with attribute label as title and toolbar for Add button
- Inline editing in table cells
- Action columns: detail-open (always visible) and delete (edit mode only)
- Add button in toolbar (edit mode only)
- Optional detail dialog via referenced layout with edit-mode propagation via channel
- Bidirectional `editMode` channel on `FormElement` (receives and publishes edit mode state)
- Two-layer overlay model with recursive overlay stacking for detail dialog
- `FormParticipant` abstraction for uniform form lifecycle
- Configurable save/cancel action chains on `FormElement` (using existing `ViewAction` infrastructure)
- `DiscardFormStateAction` for cancel-action chains
- Add/delete commands with type selection for polymorphic compositions
- Field-level and reference-level validation
- Dirty tracking integrated with main form

**Out of scope (future work):**
- Inline sub-form rendering (non-table composition display)
- Row reordering / drag-and-drop for ordered compositions
- Copy row functionality
- Nested composition tables (supported architecturally via recursive overlays, but not explicitly tested/targeted)
