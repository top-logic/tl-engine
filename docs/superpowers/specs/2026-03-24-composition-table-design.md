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
    <detail-dialog layout="path/to/detail-layout.xml" width="600" height="400"/>
</composition-table>
```

**Configuration:**
- `attribute` (mandatory): Name of the composition reference on the base object.
- `columns`: Which attributes of the composed type appear as table columns. Each column can be marked `readonly`.
- `detail-dialog` (optional): Reference to a separate layout fragment that defines the detail editing form. Width/height configure the dialog window.

Without `detail-dialog`, only inline table editing is available.

### Detail Dialog as Separate Layout

The detail dialog is defined as a standalone layout fragment containing a `FormElement` with `FieldElement`s (and potentially nested `composition-table`s). It receives the row object via an input channel.

When a user opens the detail dialog for a row:
1. `CompositionTableControl` pushes the row's overlay onto the dialog's input channel.
2. The dialog's `FormElement` creates its own overlay on top of the row overlay (recursive overlay pattern).
3. Dialog Save applies the dialog overlay onto the row overlay — table cells see the changes immediately.
4. Dialog Cancel discards the dialog overlay — the row overlay remains unchanged.

This is not a special case. The dialog is a normal form whose input happens to be an overlay instead of a persistent object. `TLObjectOverlay extends TransientObject` implements `TLObject`, so overlays stack naturally.

### Two-Layer Overlay Model

**Layer 1 — Composition reference in the main overlay:**
- On entering edit mode, the current value of the composition reference is copied into the base object's `TLObjectOverlay` as a modifiable list.
- Any access to the reference (e.g., from constraints) returns this transient list.
- Add/delete operations modify this list directly.
- The main overlay becomes dirty.

**Layer 2 — Row overlays for existing composed objects:**
- Each already-persistent composed object gets its own `TLObjectOverlay`.
- `AttributeFieldModel` instances in table cells and detail dialog work on these row overlays.
- New (transient) objects need no overlay — fields work directly on the transient object.

**Deletion semantics:** An object is deleted when it is no longer present in the overlay's reference list. On save, objects that existed in the persistent state but are absent from the overlay value are deleted. This follows naturally from composition semantics.

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

### Validation

**Two levels:**

1. **Field-level:** Constraints on individual attributes of composed objects. Each `AttributeFieldModel` validates as usual (mandatory, format, model constraints). Errors shown in table cells and detail dialog fields.

2. **Reference-level:** Constraints on the composition reference itself (e.g., minimum/maximum count, no duplicates). These run against the reference value in the main overlay. Errors shown at the composition table control level (e.g., below the table).

Reveal semantics follow the main form: validation errors are hidden until submit attempt or user interaction. `revealAll()` on the main form propagates through `FormParticipant` into all rows and fields.

### Dirty Tracking

- Row overlay changes (attribute edits) and reference list changes (add/delete) both propagate via `FormParticipant.isDirty()` to the main form's dirty channel.
- The main form's dirty indicator reflects changes across all participants uniformly.

### Save Flow (single KB transaction)

1. All `FormParticipant`s validate — abort on any error.
2. All `FormParticipant`s apply:
   - Row overlays apply attribute changes to existing objects.
   - Transient new objects are persisted.
   - Composition reference on base object is updated.
   - Objects no longer in the reference list are deleted.
3. Transaction commit.

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

## Scope

**In scope:**
- `CompositionTableElement` with column configuration
- Inline editing in table cells
- Optional detail dialog via referenced layout
- Two-layer overlay model with recursive overlay stacking for detail dialog
- `FormParticipant` abstraction for uniform form lifecycle
- Add/delete commands with type selection for polymorphic compositions
- Field-level and reference-level validation
- Dirty tracking integrated with main form

**Out of scope (future work):**
- Inline sub-form rendering (non-table composition display)
- Row reordering / drag-and-drop for ordered compositions
- Copy row functionality
- Nested composition tables (supported architecturally via recursive overlays, but not explicitly tested/targeted)
