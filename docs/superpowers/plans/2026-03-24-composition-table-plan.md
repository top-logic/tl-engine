# Composition Table Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enable editing composition references in the React UI via an editable table with optional detail dialog.

**Architecture:** New `CompositionTableElement` UIElement creates a `CompositionTableControl` that assembles existing `ReactTableControl`, `ReactCellControlProvider`, and `DialogManager` components. Two-layer overlay model: composition reference value in main overlay contains row overlays, each wrapping a composed object. `FormParticipant` interface abstracts form lifecycle so `FormControl` handles all participant types uniformly.

**Tech Stack:** Java 17, TopLogic model/form/react framework, TL-Script for expression constraints, XML configuration

**Spec:** `docs/superpowers/specs/2026-03-24-composition-table-design.md`

---

## File Structure

### New Files

| File | Responsibility |
|------|---------------|
| `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/FormParticipant.java` | Interface: validate, apply, cancel, revealAll, isDirty |
| `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/CompositionFieldModel.java` | FieldModel for composition reference as a whole (list value, reference-level constraints, dirty = list changes + row overlay dirty) |
| `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/CompositionRowModel.java` | Per-row model: holds row overlay or transient object + AttributeFieldModel instances per column |
| `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/CompositionTableControl.java` | Orchestrator: creates ReactTableControl, manages overlays, add/delete commands, detail dialog, implements FormParticipant |
| `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/CompositionTableElement.java` | UIElement: parses `<composition-table>` config, creates CompositionTableControl |
| `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/command/DiscardFormStateAction.java` | ViewAction: discards form overlay without applying |
| `com.top_logic.demo/src/main/webapp/WEB-INF/views/demo/edit-constraint-test-item.view.xml` | Detail dialog layout for ConstraintTestItem |

### Modified Files

| File | Change |
|------|--------|
| `com.top_logic.layout.view/.../form/FormControl.java` | Refactor to use FormParticipant list; delegate save/cancel/validate/dirty/reveal to participants |
| `com.top_logic.layout.view/.../form/AttributeFieldControl.java` | Implement FormParticipant (apply=no-op, delegate validate/cancel/revealAll/isDirty to model) |
| `com.top_logic.layout.view/.../element/FormElement.java` | Add optional `save-action`/`cancel-action` config; use action chains when configured |
| `com.top_logic.layout.view/.../form/FormCommandModel.java` | Support configurable action chains for save/cancel |
| `com.top_logic.demo/.../model/test.constraints.model.xml` | Add ConstraintTestItem type + composition reference + cross-level constraint |
| `com.top_logic.demo/.../views/demo/constraint-test.view.xml` | Add `<composition-table>` to form |
| `com.top_logic.demo/.../views/demo/create-constraint-test.view.xml` | Add `<composition-table>` to create dialog |
| `com.top_logic.demo/.../conf/resources/model.test.constraints.messages_en.properties` | I18N for new attributes |
| `com.top_logic.demo/.../conf/resources/model.test.constraints.messages_de.properties` | I18N for new attributes |

---

## Task 1: FormParticipant Interface + FormControl Refactoring

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/FormParticipant.java`
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/FormControl.java`
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/AttributeFieldControl.java`

This is the foundational refactoring. After this task, the existing form functionality works exactly as before, but through the FormParticipant abstraction.

- [ ] **Step 1: Create FormParticipant interface**

```java
// com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/FormParticipant.java
package com.top_logic.layout.view.form;

import com.top_logic.knowledge.service.Transaction;

/**
 * Participant in a form's editing lifecycle.
 *
 * <p>
 * {@link FormControl} delegates save/cancel/validate/dirty/reveal operations to all registered
 * participants uniformly, without knowing what kind of content they manage (primitive fields,
 * composition tables, sub-forms, etc.).
 * </p>
 */
public interface FormParticipant {

    /**
     * Validates this participant's content.
     *
     * @return {@code true} if all content is valid, {@code false} if there are errors.
     */
    boolean validate();

    /**
     * Applies changes within the given transaction.
     *
     * @param tx The active KB transaction.
     */
    void apply(Transaction tx);

    /**
     * Discards all changes made through this participant.
     */
    void cancel();

    /**
     * Makes all hidden validation errors visible.
     */
    void revealAll();

    /**
     * Whether this participant has unsaved changes.
     */
    boolean isDirty();
}
```

- [ ] **Step 2: Make AttributeFieldControl implement FormParticipant**

Add `implements FormParticipant` to `AttributeFieldControl`. Implement:

```java
@Override
public boolean validate() {
    if (_model == null) return true;
    _model.validate();
    return !_model.hasError();
}

@Override
public void apply(Transaction tx) {
    // No-op: the main overlay's applyTo() handles primitive attribute changes.
}

@Override
public void cancel() {
    // No-op: FormControl discards the overlay, model rebinds on form state change.
}

@Override
public void revealAll() {
    if (_model != null) {
        _model.setRevealed(true);
    }
}

@Override
public boolean isDirty() {
    return _model != null && _model.isDirty();
}
```

- [ ] **Step 3: Refactor FormControl to use FormParticipant list**

Add a `List<FormParticipant> _participants` field. Add `registerParticipant(FormParticipant)` and `unregisterParticipant(FormParticipant)` methods.

Refactor `executeSave()`:
- Before applying overlay, call `participant.validate()` on all participants. If any returns false, call `revealAllValidation()` and return.
- **CRITICAL ordering:** Within the transaction, call `participant.apply(tx)` on all participants FIRST (they prepare the main overlay, e.g. composition tables persist new objects and update the reference list in the overlay), THEN call `_overlay.applyTo(_currentObject)` to transfer all changes to the KB. Participants update the overlay; the overlay applies last.

Refactor `executeCancel()`:
- Call `participant.cancel()` on all participants before `exitEditMode()`.

Refactor `updateDirtyState()`:
- Dirty = overlay.isDirty() OR any participant.isDirty().

Refactor `revealAllValidation()`:
- Delegate entirely to `participant.revealAll()` on all participants.
- Note: The `_fieldModels` list becomes redundant once all reveal/dirty operations go through `FormParticipant`. Keep `_fieldModels` for now to avoid breaking changes, but all new reveal/dirty paths go through participants. A follow-up cleanup can remove `_fieldModels`.

Refactor `exitEditMode()`:
- Clear `_participants` list alongside `_fieldModels`.

- [ ] **Step 4: Register AttributeFieldControl as FormParticipant**

In `AttributeFieldControl.createChromeControl()` and `onFormStateChanged()`, where `_formControl.registerFieldModel(_model)` is called, also call `_formControl.registerParticipant(this)`. In `clearModel()`, call `_formControl.unregisterParticipant(this)`.

- [ ] **Step 5: Build and verify existing constraint demo still works**

```bash
mvn install -DskipTests=true -pl com.top_logic.layout.view
mvn install -DskipTests=true -pl com.top_logic.demo
```

Start the demo app, navigate to Constraint Test, verify edit/save/cancel still works as before.

- [ ] **Step 6: Commit**

```
Ticket #29108: Introduce FormParticipant abstraction in FormControl.
```

---

## Task 2: Configurable Save/Cancel Actions on FormElement

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/FormElement.java`
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/FormCommandModel.java`
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/command/DiscardFormStateAction.java`

- [ ] **Step 1: Add save-action/cancel-action config to FormElement.Config**

Add two new configuration properties:

```java
/** Configuration name for {@link #getSaveActions()}. */
String SAVE_ACTION = "save-action";

/** Configuration name for {@link #getCancelActions()}. */
String CANCEL_ACTION = "cancel-action";

/**
 * Optional action chain to execute on save instead of the default behavior.
 *
 * <p>When configured, the save button executes this ViewAction chain
 * (via GenericViewCommand) instead of calling FormControl.executeSave().</p>
 */
@Name(SAVE_ACTION)
List<PolymorphicConfiguration<ViewAction>> getSaveActions();

/**
 * Optional action chain to execute on cancel instead of the default behavior.
 *
 * <p>When configured, the cancel button executes this ViewAction chain
 * instead of calling FormControl.executeCancel().</p>
 */
@Name(CANCEL_ACTION)
List<PolymorphicConfiguration<ViewAction>> getCancelActions();
```

- [ ] **Step 2: Create DiscardFormStateAction**

```java
// com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/command/DiscardFormStateAction.java
package com.top_logic.layout.view.command;

// ... imports ...

/**
 * {@link ViewAction} that discards the form overlay without applying changes.
 */
public class DiscardFormStateAction implements ViewAction {

    @TagName("discard-form-state")
    public interface Config extends PolymorphicConfiguration<DiscardFormStateAction> {
        @Override
        @ClassDefault(DiscardFormStateAction.class)
        Class<? extends DiscardFormStateAction> getImplementationClass();
    }

    @CalledByReflection
    public DiscardFormStateAction(InstantiationContext context, Config config) {
        // No configuration.
    }

    @Override
    public Object execute(ReactContext context, Object input) {
        if (!(context instanceof ViewContext)) return input;
        FormModel formModel = ((ViewContext) context).getFormModel();
        if (!(formModel instanceof FormControl)) return input;
        ((FormControl) formModel).executeCancel();
        return input;
    }
}
```

- [ ] **Step 3: Wire action chains into FormElement.contributeEditCommands()**

When `_config.getSaveActions()` is non-empty, the save `FormCommandModel` should execute the action chain instead of `form.executeSave()`. Similarly for cancel.

Modify `FormCommandModel` to accept an optional `Consumer<ReactContext>` override, or modify `FormElement.contributeEditCommands()` to create a `GenericViewCommand` wrapper when action chains are configured.

The simplest approach: in `contributeEditCommands()`, if `_config.getSaveActions()` is non-empty, instantiate the ViewActions and create a save command that chains them instead of calling `form.executeSave()`. Same for cancel.

- [ ] **Step 4: Build and verify**

```bash
mvn install -DskipTests=true -pl com.top_logic.layout.view
```

Verify: existing forms without save-action/cancel-action config still work (default behavior).

- [ ] **Step 5: Commit**

```
Ticket #29108: Add configurable save/cancel action chains to FormElement.
```

---

## Task 3: CompositionFieldModel

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/CompositionFieldModel.java`

- [ ] **Step 1: Implement CompositionFieldModel**

```java
package com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.model.TLObject;

/**
 * {@link com.top_logic.layout.form.model.FieldModel} for a composition reference as a whole.
 *
 * <p>
 * Value is the current list of composed objects (row overlays + transient new objects)
 * stored in the main overlay. Reference-level constraints (min/max count) apply here.
 * </p>
 *
 * <p>
 * Dirty tracking checks both the list membership (added/removed objects) and whether
 * any row overlay is dirty (attribute changes within composed objects).
 * </p>
 */
public class CompositionFieldModel extends AbstractFieldModel {

    // Note: AbstractFieldModel tracks _defaultValue and _value internally.
    // We use _value (via getValue/setValue) as the current list and _defaultValue as the
    // original list for dirty tracking. We override isDirty() to also check row overlays.
    // No separate _currentList/_originalList fields to avoid state duplication.

    private final List<TLObjectOverlay> _rowOverlays = new ArrayList<>();

    public CompositionFieldModel(List<TLObject> initialList) {
        super(new ArrayList<>(initialList)); // defaultValue = initial snapshot
    }

    @Override
    public Object getValue() {
        return getCachedValue(); // Returns the internally stored list
    }

    @Override
    public void setValue(Object value) {
        Object oldValue = getCachedValue();
        setValueInternal(value);
        fireValueChanged(oldValue, value);
    }

    /**
     * Registers a row overlay for dirty tracking.
     */
    public void addRowOverlay(TLObjectOverlay overlay) {
        _rowOverlays.add(overlay);
    }

    /**
     * Removes a row overlay from dirty tracking.
     */
    public void removeRowOverlay(TLObjectOverlay overlay) {
        _rowOverlays.remove(overlay);
    }

    @Override
    public boolean isDirty() {
        // Check list membership changes (uses AbstractFieldModel's default comparison).
        if (super.isDirty()) {
            return true;
        }
        // Check row overlay changes.
        for (TLObjectOverlay overlay : _rowOverlays) {
            if (overlay.isDirty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * The current transient list (typed accessor).
     */
    @SuppressWarnings("unchecked")
    public List<TLObject> getCurrentList() {
        return (List<TLObject>) getCachedValue();
    }
}
```

- [ ] **Step 2: Build**

```bash
mvn install -DskipTests=true -pl com.top_logic.layout.view
```

- [ ] **Step 3: Commit**

```
Ticket #29108: Add CompositionFieldModel for composition reference values.
```

---

## Task 4: CompositionRowModel

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/CompositionRowModel.java`

- [ ] **Step 1: Implement CompositionRowModel**

```java
package com.top_logic.layout.view.form;

import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Per-row model for a composition table.
 *
 * <p>
 * Holds a reference to the row's overlay (for existing persistent objects) or transient object
 * (for new entries), plus the {@link AttributeFieldModel} instances for each editable column.
 * </p>
 */
public class CompositionRowModel {

    private final TLObject _rowObject;
    private final TLObjectOverlay _rowOverlay;
    private final boolean _isNew;
    private final Map<String, AttributeFieldModel> _columnModels = new LinkedHashMap<>();

    private CompositionRowModel(TLObjectOverlay rowOverlay, TLObject rowObject, boolean isNew) {
        _rowOverlay = rowOverlay;
        _rowObject = rowObject;
        _isNew = isNew;
    }

    /** Factory for existing persistent objects (wrapped in overlay). */
    public static CompositionRowModel forExisting(TLObjectOverlay rowOverlay) {
        return new CompositionRowModel(rowOverlay, rowOverlay, false);
    }

    /** Factory for new transient objects. */
    public static CompositionRowModel forNew(TLObject transientObject) {
        return new CompositionRowModel(null, transientObject, true);
    }

    /** The object to read/write attribute values from (overlay or transient). */
    public TLObject getRowObject() {
        return _rowObject;
    }

    /** The overlay, or {@code null} for new transient objects. */
    public TLObjectOverlay getRowOverlay() {
        return _rowOverlay;
    }

    /** Whether this is a newly created (transient) row. */
    public boolean isNew() {
        return _isNew;
    }

    /** Registers an AttributeFieldModel for a column. */
    public void putColumnModel(String columnName, AttributeFieldModel model) {
        _columnModels.put(columnName, model);
    }

    /** Gets the AttributeFieldModel for a column. */
    public AttributeFieldModel getColumnModel(String columnName) {
        return _columnModels.get(columnName);
    }

    /** All column field models. */
    public Map<String, AttributeFieldModel> getColumnModels() {
        return _columnModels;
    }
}
```

- [ ] **Step 2: Build**

```bash
mvn install -DskipTests=true -pl com.top_logic.layout.view
```

- [ ] **Step 3: Commit**

```
Ticket #29108: Add CompositionRowModel for per-row state management.
```

---

## Task 5: CompositionTableControl

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/CompositionTableControl.java`

This is the central orchestrator. It creates the ReactTableControl, manages overlays, implements FormParticipant, and handles add/delete.

- [ ] **Step 1: Implement CompositionTableControl — core structure**

The control must:
1. On construction, receive: ReactContext, FormControl, attribute name, column configs, optional detail dialog config.
2. Implement `FormModelListener` to react to form state changes (enter/exit edit mode, object switch).
3. Implement `FormParticipant` for lifecycle integration.
4. Create a `ReactTableControl` with a custom `ReactCellControlProvider`.
5. Manage `CompositionFieldModel` and `CompositionRowModel` instances.

Key methods:
- `onFormStateChanged()`: When form enters edit mode, read the composition reference value, create row overlays for each existing object, store the overlay list in the main overlay, create the CompositionFieldModel.
- `createRowModels()`: For each object in the composition, create a CompositionRowModel with overlay (existing) or direct reference (new transient).
- Cell provider: In edit mode, create `AttributeFieldModel` per cell and get the appropriate `ReactFormFieldControl` from `FieldControlService`. In view mode, create text cell controls.
- `addRow()`: Create a new transient object, add to current list, update table.
- `deleteRow(rowObject)`: Remove from current list, update table.

The `apply()` method must follow the hard-constraint ordering:
1. Apply row overlays to existing objects.
2. Persist new transient objects (via `obj.copy(transient: false)` pattern or equivalent).
3. Update composition reference on base object.
4. Delete orphaned objects (present in original list but absent from current list).

```java
package com.top_logic.layout.view.form;

// imports...

public class CompositionTableControl implements FormModelListener, FormParticipant {

    private final ReactContext _context;
    private final FormControl _formControl;
    private final String _attributeName;
    private final List<ColumnConfig> _columnConfigs;
    private final DetailDialogConfig _detailDialogConfig; // nullable

    private ReactTableControl _tableControl;
    private CompositionFieldModel _fieldModel;
    private final List<CompositionRowModel> _rowModels = new ArrayList<>();

    private TLStructuredTypePart _compositionPart;

    // ... constructor, FormModelListener, FormParticipant implementation ...
}
```

- [ ] **Step 2: Implement FormParticipant methods**

```java
@Override
public boolean validate() {
    if (_fieldModel == null) return true;
    _fieldModel.validate();
    if (_fieldModel.hasError()) return false;
    // Validate all row field models.
    for (CompositionRowModel row : _rowModels) {
        for (AttributeFieldModel colModel : row.getColumnModels().values()) {
            colModel.validate();
            if (colModel.hasError()) return false;
        }
    }
    return true;
}

@Override
public void apply(Transaction tx) {
    if (_fieldModel == null) return;
    List<TLObject> currentList = _fieldModel.getCurrentList();
    List<TLObject> originalList = _fieldModel.getOriginalList();

    // 1. Apply row overlays (attribute changes on existing objects).
    for (CompositionRowModel row : _rowModels) {
        TLObjectOverlay overlay = row.getRowOverlay();
        if (overlay != null && overlay.isDirty()) {
            TLObject base = overlay.getBase();
            overlay.applyTo(base);
        }
    }

    // 2. Persist new transient objects.
    List<TLObject> persistedList = new ArrayList<>();
    for (TLObject obj : currentList) {
        if (obj.tTransient()) {
            // Copy transient to persistent.
            TLObject persisted = obj.copy(/* transient: false */);
            persistedList.add(persisted);
        } else if (obj instanceof TLObjectOverlay) {
            persistedList.add(((TLObjectOverlay) obj).getBase());
        } else {
            persistedList.add(obj);
        }
    }

    // 3. Update composition reference IN THE MAIN OVERLAY (not the base object).
    // FormControl.executeSave() calls participant.apply() BEFORE overlay.applyTo(),
    // so we prepare the overlay with the correct persisted list.
    _formControl.getOverlay().tUpdate(_compositionPart, persistedList);

    // 4. Delete orphaned objects.
    Set<TLObject> currentBases = new HashSet<>();
    for (TLObject obj : currentList) {
        if (obj instanceof TLObjectOverlay) {
            currentBases.add(((TLObjectOverlay) obj).getBase());
        } else if (!obj.tTransient()) {
            currentBases.add(obj);
        }
    }
    for (TLObject original : originalList) {
        if (!currentBases.contains(original)) {
            original.tDelete();
        }
    }
}

@Override
public void cancel() {
    _rowModels.clear();
    _fieldModel = null;
}

@Override
public void revealAll() {
    if (_fieldModel != null) {
        _fieldModel.setRevealed(true);
    }
    for (CompositionRowModel row : _rowModels) {
        for (AttributeFieldModel colModel : row.getColumnModels().values()) {
            colModel.setRevealed(true);
        }
    }
}

@Override
public boolean isDirty() {
    return _fieldModel != null && _fieldModel.isDirty();
}
```

- [ ] **Step 3: Implement onFormStateChanged() — overlay setup**

When the form enters edit mode (`source.isEditMode()` becomes true):
1. Resolve `_compositionPart` from the current object's type.
2. Read the persistent composition value as a list.
3. Create a `TLObjectOverlay` per existing composed object.
4. Create `CompositionRowModel` per row.
5. Build the overlay list (containing overlays, not originals) and store in main overlay.
6. Create `CompositionFieldModel` with the overlay list.
7. Register as participant on FormControl.

When the form exits edit mode: clean up all row models and field model.

- [ ] **Step 4: Implement ReactCellControlProvider for editable cells**

Create an inner class or separate class that implements `ReactCellControlProvider`:
- Receives the column config list and edit mode state.
- For each cell: if edit mode and column is not readonly, create an `AttributeFieldModel` on the row's overlay/transient object, then use `FieldControlService.getInstance().createFieldControl()` to get the appropriate React control.
- If view mode or readonly column: create a `ReactTextCellControl` with the formatted value.
- Store the `AttributeFieldModel` in the `CompositionRowModel` for validation/reveal access.
- **Critical:** Add a `FieldModelListener` to each cell's `AttributeFieldModel` that calls `_formControl.updateDirtyState()` on value change (same pattern as `AttributeFieldControl`'s listener at line 257). Without this, row-level edits do not propagate dirty state to the main form.

- [ ] **Step 5: Implement add/delete row commands**

`addRow()`:
1. Determine the type to create (from the composition reference's target type).
2. Create a transient object via TL-Script or `TransientObjectFactory`.
3. Create a `CompositionRowModel` for it.
4. Add to `_fieldModel.getCurrentList()`.
5. Store the updated list in the main overlay.
6. Update `_formControl.updateDirtyState()`.
7. Refresh the table.

`deleteRow(TLObject rowObject)`:
1. Remove from `_fieldModel.getCurrentList()`.
2. Remove the `CompositionRowModel`.
3. If it had an overlay, remove from `_fieldModel`.
4. Store the updated list in the main overlay.
5. Update `_formControl.updateDirtyState()`.
6. Refresh the table.

- [ ] **Step 6: Build**

```bash
mvn install -DskipTests=true -pl com.top_logic.layout.view
```

- [ ] **Step 7: Commit**

```
Ticket #29108: Implement CompositionTableControl with overlay management.
```

---

## Task 6: CompositionTableElement

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/CompositionTableElement.java`

- [ ] **Step 1: Implement CompositionTableElement**

```java
package com.top_logic.layout.view.element;

// imports...

/**
 * Declarative {@link UIElement} that renders a composition reference as an editable table.
 *
 * <p>Must be nested inside a {@link FormElement}.</p>
 */
public class CompositionTableElement implements UIElement {

    @TagName("composition-table")
    public interface Config extends UIElement.Config {

        @Override
        @ClassDefault(CompositionTableElement.class)
        Class<? extends UIElement> getImplementationClass();

        /** The composition reference attribute name. */
        @Name("attribute")
        @Mandatory
        String getAttribute();

        /** Column definitions. */
        @Name("columns")
        ColumnsConfig getColumns();

        /** Optional detail dialog configuration. */
        @Name("detail-dialog")
        DetailDialogConfig getDetailDialog();
    }

    public interface ColumnsConfig extends ConfigurationItem {
        @DefaultContainer
        List<ColumnConfig> getColumns();
    }

    public interface ColumnConfig extends ConfigurationItem {
        @TagName("column")
        // ...

        @Name("attribute")
        @Mandatory
        String getAttribute();

        @Name("readonly")
        boolean getReadonly();
    }

    public interface DetailDialogConfig extends ConfigurationItem {
        @Name("layout")
        @Mandatory
        String getLayout();

        @Name("input-channel")
        @Mandatory
        String getInputChannel();

        @Name("width")
        int getWidth();

        @Name("height")
        int getHeight();
    }

    private final Config _config;

    @CalledByReflection
    public CompositionTableElement(InstantiationContext context, Config config) {
        _config = config;
    }

    @Override
    public IReactControl createControl(ViewContext context) {
        FormModel formModel = context.getFormModel();
        if (formModel == null) {
            throw new IllegalStateException(
                "CompositionTableElement must be nested inside a FormElement.");
        }
        FormControl formControl = (FormControl) formModel;

        return new CompositionTableControl(context, formControl, _config);
    }
}
```

- [ ] **Step 2: Build**

```bash
mvn install -DskipTests=true -pl com.top_logic.layout.view
```

- [ ] **Step 3: Commit**

```
Ticket #29108: Add CompositionTableElement UIElement.
```

---

## Task 7: Demo Model Extension

**Files:**
- Modify: `com.top_logic.demo/src/main/webapp/WEB-INF/model/test.constraints.model.xml`
- Modify: `com.top_logic.demo/src/main/webapp/WEB-INF/conf/resources/model.test.constraints.messages_en.properties`
- Modify: `com.top_logic.demo/src/main/webapp/WEB-INF/conf/resources/model.test.constraints.messages_de.properties`

- [ ] **Step 1: Add ConstraintTestItem type and composition reference**

Add to `test.constraints.model.xml`:

```xml
<class name="ConstraintTestItem">
    <generalizations>
        <generalization type="tl.model:TLObject"/>
    </generalizations>
    <annotations>
        <table name="GenericObject"/>
    </annotations>
    <attributes>
        <property name="name" mandatory="true" type="tl.core:String"/>
        <property name="quantity" type="tl.core:Integer">
            <annotations>
                <value-range min="1.0" max="1000.0"/>
            </annotations>
        </property>
        <property name="unitPrice" type="tl.core:Double">
            <annotations>
                <value-range min="0.01" max="99999.0"/>
            </annotations>
        </property>
    </attributes>
</class>
```

Add composition reference to `ConstraintTestType`:

```xml
<reference name="items"
    composite="true"
    multiple="true"
    type="ConstraintTestItem"
>
    <annotations>
        <constraints>
            <constraint-by-expression>
                <check><![CDATA[value -> obj -> {
    if ($value == null or $value.size() == 0,
        #("At least one item is required."@en, "Mindestens ein Eintrag ist erforderlich."@de),
        null)
}]]></check>
            </constraint-by-expression>
        </constraints>
    </annotations>
</reference>
```

Add cross-level expression constraint on `ConstraintTestType` (on a new or existing attribute, or as a type-level constraint). Best approach: add a constraint on `rangedInt` that cross-references `items`:

```xml
<!-- Add a second constraint on rangedInt that validates against items total -->
<constraint-by-expression>
    <check><![CDATA[value -> obj -> {
        total = $obj.get(`test.constraints:ConstraintTestType#items`)
            .map(item -> {
                q = $item.get(`test.constraints:ConstraintTestItem#quantity`);
                p = $item.get(`test.constraints:ConstraintTestItem#unitPrice`);
                if ($q != null and $p != null, $q * $p, 0)
            })
            .sum();
        if ($value != null and $total > $value,
            #("Item total ({0}) exceeds the maximum ({1})."@en, "Summe der Eintraege ({0}) uebersteigt das Maximum ({1})."@de).fill($total, $value),
            null)
    }]]></check>
</constraint-by-expression>
```

- [ ] **Step 2: Add I18N messages**

Add labels and tooltips for the new type and attributes to both `messages_en.properties` and `messages_de.properties`.

- [ ] **Step 3: Build**

```bash
mvn install -DskipTests=true -pl com.top_logic.demo
```

- [ ] **Step 4: Commit**

```
Ticket #29108: Add ConstraintTestItem composition to demo model.
```

---

## Task 8: Demo View Extension

**Files:**
- Modify: `com.top_logic.demo/src/main/webapp/WEB-INF/views/demo/constraint-test.view.xml`
- Modify: `com.top_logic.demo/src/main/webapp/WEB-INF/views/demo/create-constraint-test.view.xml`
- Create: `com.top_logic.demo/src/main/webapp/WEB-INF/views/demo/edit-constraint-test-item.view.xml`

- [ ] **Step 1: Add composition-table to constraint-test.view.xml**

After the existing `<field>` elements in the form:

```xml
<composition-table attribute="items">
    <columns>
        <column attribute="name"/>
        <column attribute="quantity"/>
        <column attribute="unitPrice"/>
    </columns>
    <detail-dialog layout="demo/edit-constraint-test-item.view.xml"
                   input-channel="editedItem"
                   width="500" height="300"/>
</composition-table>
```

- [ ] **Step 2: Create detail dialog layout**

```xml
<?xml version="1.0" encoding="utf-8" ?>
<view>
    <channels>
        <channel name="editedItem"/>
    </channels>
    <window title="Edit Item" width="500px" height="300px">
        <form input="editedItem" initial-edit-mode="true" mode-switch="false">
            <save-action>
                <store-form-state/>
                <close-dialog/>
            </save-action>
            <field attribute="name"/>
            <field attribute="quantity"/>
            <field attribute="unitPrice"/>
            <commands>
                <generic-command label="OK" placement="TOOLBAR">
                    <store-form-state/>
                    <close-dialog/>
                </generic-command>
            </commands>
        </form>
        <actions>
            <button>
                <action class="com.top_logic.layout.view.command.CancelDialogCommand" label="Cancel"/>
            </button>
        </actions>
    </window>
</view>
```

- [ ] **Step 3: Add composition-table to create-constraint-test.view.xml**

Add the same `<composition-table>` block after the existing fields.

- [ ] **Step 4: Build**

```bash
mvn install -DskipTests=true -pl com.top_logic.demo
```

- [ ] **Step 5: Commit**

```
Ticket #29108: Add composition table to constraint demo views.
```

---

## Task 9: Integration Testing with Playwright

**Files:** None (browser-based verification)

Start the demo app and use Playwright to verify all scenarios interactively.

- [ ] **Step 1: Start demo app**

Use the `tl-app` skill to start the demo application.

- [ ] **Step 2: Verify basic table rendering**

Navigate to Constraint Test. Select an existing object (or create one). Verify the composition table appears below the existing fields, initially empty.

- [ ] **Step 3: Verify add row**

Enter edit mode. Click "Add" on the composition table. Verify a new row appears with editable cells.

- [ ] **Step 4: Verify inline editing + field-level validation**

Edit the `name` field (leave empty, verify mandatory error). Edit `quantity` (enter 0, verify range error). Edit `unitPrice` (enter -1, verify range error). Fix the values, verify errors clear.

- [ ] **Step 5: Verify detail dialog**

Click to open the detail dialog for a row. Verify fields show the same values as the table cells. Edit a value in the dialog, click OK. Verify the table cell updates.

- [ ] **Step 6: Verify detail dialog cancel**

Open detail dialog, change a value, click Cancel. Verify the table cell retains the old value.

- [ ] **Step 7: Verify dirty state**

After adding/editing items, verify the form's dirty indicator is active. After save, verify it clears.

- [ ] **Step 8: Verify reference-level constraint**

Delete all items. Try to save. Verify the "At least one item required" error appears.

- [ ] **Step 9: Verify cross-level constraint**

Set `rangedInt` to 10. Add an item with quantity=5, unitPrice=3 (total=15 > 10). Verify the cross-level constraint error appears on rangedInt.

- [ ] **Step 10: Verify save persists everything**

Add items, edit attributes, save. Reload the page. Verify all changes persisted.

- [ ] **Step 11: Verify cancel discards everything**

Enter edit mode, add items, edit attributes. Cancel. Verify everything reverts to the pre-edit state.

- [ ] **Step 12: Fix any issues found, re-test, commit fixes**

Iterate until all scenarios pass. Each fix gets its own commit.

---

## Known Limitations

1. **StoreFormStateAction does not use FormParticipant protocol.** It directly applies the overlay without calling `participant.validate()` / `participant.apply()`. This means detail dialog forms with nested composition tables would bypass participant lifecycle. For the current scope (detail dialogs have only FieldElements), this is not a problem. A future enhancement should update `StoreFormStateAction` to delegate through participants.

2. **Polymorphic type selection for "Add" is not implemented.** When the composition reference's target type has multiple subtypes, the add command should show a type selection dialog. The demo model has no subtypes, so this is deferred.

3. **`_fieldModels` list in FormControl is redundant** once all operations go through `FormParticipant`. A follow-up cleanup can remove it and have `revealAllValidation()` delegate entirely through participants.
