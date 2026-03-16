# Hide Unsupported Form Fields Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Hide form fields whose attribute doesn't exist on the currently selected object's type, and show them again when a compatible object is selected.

**Architecture:** Make `AttributeFieldControl.resolvePart()` tolerant (return null instead of throwing), then use the existing `visible` state on `ReactFormFieldChromeControl` to dynamically hide/show the chrome. All changes in one file.

**Tech Stack:** Java 17, TopLogic view framework (`com.top_logic.layout.view`)

**Spec:** `docs/superpowers/specs/2026-03-16-hide-unsupported-form-fields-design.md`

---

### Task 1: Make `resolvePart()` tolerant

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/AttributeFieldControl.java:199-207`

- [ ] **Step 1: Change `resolvePart()` to return null instead of throwing**

Replace the `IllegalArgumentException` with a `null` return:

```java
private TLStructuredTypePart resolvePart(TLObject obj) {
    TLStructuredType type = obj.tType();
    return type.getPart(_attributeName);
}
```

- [ ] **Step 2: Compile to verify no errors**

Run: `mvn compile -DskipTests=true -pl com.top_logic.layout.view`
Expected: BUILD SUCCESS (callers will be updated in subsequent tasks)

Note: At this point the code has a bug — `createChromeControl()` and `onFormStateChanged()` don't handle null from `resolvePart()` yet. This is fixed in Tasks 2 and 3.

---

### Task 2: Handle missing attribute in `createChromeControl()`

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/AttributeFieldControl.java:86-114`

- [ ] **Step 1: Add null-check after `resolvePart()` in `createChromeControl()`**

After the existing `current == null` early return (line 88-96), add handling for when the attribute doesn't exist on the current object's type. The chrome should be created invisible with a placeholder control:

```java
public ReactFormFieldChromeControl createChromeControl() {
    TLObject current = _formModel.getCurrentObject();
    if (current == null) {
        _innerControl = new ReactTextInputControl(
            _context, new AbstractFieldModel(null) {
                // Placeholder model with default state.
            });
        _chrome = new ReactFormFieldChromeControl(_context, _attributeName,
            false, false, null, null, null, false, true, _innerControl);
        return _chrome;
    }

    TLStructuredTypePart part = resolvePart(current);
    if (part == null) {
        // Attribute not supported by this object's type — hide the field.
        _innerControl = new ReactTextInputControl(
            _context, new AbstractFieldModel(null) {
                // Placeholder model with default state.
            });
        _chrome = new ReactFormFieldChromeControl(_context, _attributeName,
            false, false, null, null, null, false, false, _innerControl);
        return _chrome;
    }

    _model = new AttributeFieldModel(current, part);
    // ... rest unchanged
```

Note the difference: `current == null` creates chrome with `visible=true` (the form shows "no object selected" anyway), while `part == null` creates chrome with `visible=false`.

- [ ] **Step 2: Compile to verify**

Run: `mvn compile -DskipTests=true -pl com.top_logic.layout.view`
Expected: BUILD SUCCESS

---

### Task 3: Handle missing attribute in `onFormStateChanged()`

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/AttributeFieldControl.java:117-150`

- [ ] **Step 1: Restructure `onFormStateChanged()` with attribute existence check**

The key constraint: `resolvePart()` must be called **before** `_model.setObject()`, because `AttributeFieldModel.setObject()` internally calls its own `resolvePart()` which throws on missing attributes.

```java
@Override
public void onFormStateChanged(FormModel source) {
    if (_chrome == null) {
        return;
    }

    TLObject current = source.getCurrentObject();

    if (current == null) {
        // Object gone - hide field.
        _chrome.setVisible(false);
        clearModel();
        return;
    }

    TLStructuredTypePart part = resolvePart(current);

    if (part == null) {
        // Attribute not supported by this object's type - hide field.
        _chrome.setVisible(false);
        clearModel();
        return;
    }

    // Attribute exists - ensure field is visible.
    _chrome.setVisible(true);

    if (_model == null) {
        // First compatible object arrived or re-appearing after hide.
        _model = new AttributeFieldModel(current, part);
        _model.setEditable(source.isEditMode() && !_forceReadonly);

        addModelListener();

        _innerControl = FieldControlService.getInstance().createFieldControl(_context, part, _model);

        _chrome.setLabel(resolveLabel());
        _chrome.setRequired(part.isMandatory());
        _chrome.setField(_innerControl);
        _chrome.setDirty(false);
        return;
    }

    // Rebind existing model to the current object.
    _model.setObject(current);
    _model.setEditable(source.isEditMode() && !_forceReadonly);
    _chrome.setDirty(_model.isDirty());
}
```

- [ ] **Step 2: Add the `clearModel()` helper method**

Add a private method to clean up the model and listener when hiding:

```java
private void clearModel() {
    if (_model != null && _modelListener != null) {
        _model.removeListener(_modelListener);
        _modelListener = null;
    }
    _model = null;
}
```

- [ ] **Step 3: Compile to verify**

Run: `mvn compile -DskipTests=true -pl com.top_logic.layout.view`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/AttributeFieldControl.java
git commit -m "Ticket #29108: Hide form fields for unsupported attributes."
```

---

### Task 4: Build dependent modules and manual test

**Files:**
- No code changes — build and test only.

- [ ] **Step 1: Build the library module**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.view`
Expected: BUILD SUCCESS

- [ ] **Step 2: Start the demo app**

Run: `mvn jetty:run -pl com.top_logic.demo` (or use tl-app skill)
Navigate to the input-controls-demo view.

- [ ] **Step 3: Manual test — all transitions**

1. Select a DemoType object that has all attributes (name, color, string, boolean, date, float, icon) → **all fields visible**
2. Select an object whose type lacks some attributes → **those fields disappear completely (no space)**
3. Select back an object with the attributes → **fields reappear**
4. Select null (deselect) → **"no object selected" message shown**
