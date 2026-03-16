# Design: Hide Unsupported Form Fields at Runtime

**Ticket**: #29108
**Date**: 2026-03-16
**Branch**: `CWS/CWS_29108_hide_unsupported_form_fields`

## Problem

When a React form (`.view.xml`) defines `<field attribute="X"/>` and the currently selected object's type does not have attribute `X` (e.g. because a supertype is selected in a tree), the form either:
- Throws an `IllegalArgumentException` in `AttributeFieldControl.resolvePart()`, or
- Renders a default empty input field that the user cannot interact with.

The field (including its chrome — label, required indicator, etc.) should be **completely invisible** when the attribute is not supported by the current object's type, and reappear when an object with that attribute is selected.

## Solution: Visibility Flag in Chrome

The `ReactFormFieldChromeControl` already has a `visible` state property and `setVisible()` method. The React component `TLFormField.tsx` already renders `null` when `visible` is `false`. No changes are needed on the Chrome or React side.

All changes are in **`AttributeFieldControl.java`** (`com.top_logic.layout.view` module).

### Change 1: `resolvePart()` — return null instead of throwing

Current behavior:
```java
private TLStructuredTypePart resolvePart(TLObject obj) {
    TLStructuredType type = obj.tType();
    TLStructuredTypePart part = type.getPart(_attributeName);
    if (part == null) {
        throw new IllegalArgumentException(...);
    }
    return part;
}
```

New behavior: return `null` when the attribute does not exist on the type.

### Change 2: `createChromeControl()` — start invisible when attribute missing

When `resolvePart()` returns `null`, create the chrome with `visible=false` and a placeholder inner control (same as the `current == null` case). No model or listener is created.

### Change 3: `onFormStateChanged()` — dynamic show/hide on object switch

The `resolvePart()` null-check must happen **before** any call to `_model.setObject()`, because `AttributeFieldModel.setObject()` internally calls its own `resolvePart()` which also throws on missing attributes.

The restructured `onFormStateChanged()` logic:

1. Get `current` from `source.getCurrentObject()`.
2. If `current == null` → hide chrome, return.
3. Call `resolvePart(current)` on the **new** object.
4. If part is `null` (attribute missing):
   - `chrome.setVisible(false)`
   - Remove model listener if present, set `_model = null`
   - Return (do NOT call `_model.setObject()`)
5. If part is non-null (attribute exists):
   - `chrome.setVisible(true)`
   - If `_model == null`: create new `AttributeFieldModel`, inner control, wire listener (same as "first object arrived" path)
   - If `_model != null`: call `_model.setObject(current)` to rebind (safe because we verified the attribute exists)
   - Update chrome label, required, dirty state

This handles all transitions:
- Object with attribute → object without attribute (hide)
- Object without attribute → object with attribute (show)
- Null → object with attribute (show)
- Null → object without attribute (stay hidden)

## Files Changed

| File | Change |
|------|--------|
| `com.top_logic.layout.view/.../form/AttributeFieldControl.java` | Tolerant `resolvePart()`, visibility logic in `createChromeControl()` and `onFormStateChanged()` |

## Files NOT Changed

- `ReactFormFieldChromeControl.java` — already has `setVisible()`
- `TLFormField.tsx` — already renders `null` when `!visible`
- `FormControl.java` — no change needed
- `FormElement.java` — no change needed
- `AttributeFieldModel.java` — its `resolvePart()` still throws, but the caller (`AttributeFieldControl`) guards by checking the attribute existence before calling `setObject()`

## Test Scenario

Use `input-controls-demo.view.xml` in `com.top_logic.demo`:
1. Select a DemoType object that has all configured attributes (name, color, string, boolean, date, float, icon) → all fields visible.
2. Select an object of a supertype that lacks some attributes → those fields disappear (no space in layout).
3. Select back an object with the attributes → fields reappear.
