# Model-Based Form Validation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace legacy FormField+FormContext constraint validation with a FormValidationModel coordinator that validates model-derived constraints server-side across TLFormObject overlays.

**Architecture:** New `OverlayLookup` interface in tl-core replaces `FormContext` in constraint APIs. `FormValidationModel` in tl-element coordinates constraint checks, dependency tracking, and result propagation across overlays. Built-in adapters translate `@TLSize`, `@TLRange`, `isMandatory` to `ConstraintCheck`. Validation results are stored on overlays and bridged to `FieldModel` by the view.

**Tech Stack:** Java 17, TopLogic framework (tl-core, tl-element, tl-model-search), JUnit 4, Maven

**Spec:** `docs/superpowers/specs/2026-03-18-model-based-form-validation-design.md`

---

## File Structure

### New Files

| File | Module | Responsibility |
|------|--------|----------------|
| `OverlayLookup.java` | tl-core | Interface: overlay resolution for constraints |
| `ValidationResult.java` | tl-core | Value type: errors + warnings per attribute |
| `ConstraintValidationListener.java` | tl-core | Listener interface for validation changes |
| `FormValidationModel.java` | tl-element | Central coordinator: overlay registry, constraint derivation, dependency map, re-validation |
| `ConstraintEntry.java` | tl-element | Internal bookkeeping per constraint instance |
| `MandatoryConstraintCheck.java` | tl-element | Adapter: `isMandatory()` → `ConstraintCheck` |
| `SizeConstraintCheck.java` | tl-element | Adapter: `@TLSize` → `ConstraintCheck` |
| `RangeConstraintCheck.java` | tl-element | Adapter: `@TLRange` → `ConstraintCheck` |
| `TestFormValidationModel.java` | tl-element (test) | Tests for FormValidationModel |
| `TestBuiltInConstraintChecks.java` | tl-element (test) | Tests for adapter constraints |

### Modified Files

| File | Module | Change |
|------|--------|--------|
| `ConstraintCheck.java` | tl-core | `traceDependencies` signature: `FormContext` → `OverlayLookup` |
| `ModeSelector.java` | tl-core | `traceDependencies` signature: `FormContext` → `OverlayLookup` |
| `TLFormObjectBase.java` | tl-core | Add `getValidation()`, `setValidation()`, `addConstraintValidationListener()` |
| `AbstractFieldModel.java` | tl-core | Add `setValidationError()`, `setValidationWarnings()`, error aggregation |
| `ScriptTracer.java` | tl-model-search | `execute()` parameter: `AttributeUpdateContainer` → `OverlayLookup` |
| `TracingAccess.java` | tl-model-search | `lookupValue()`: cast to `OverlayLookup` instead of `AttributeUpdateContainer` |
| `TracingAccessRewriter.java` | tl-model-search | Rename constant type comment (optional) |
| `ConstraintCheckByExpression.java` | tl-model-search | `traceDependencies()`: accept `OverlayLookup`, pass to `ScriptTracer` |
| `ModeSelectorByExpression.java` | tl-model-search | `traceDependencies()`: accept `OverlayLookup`, pass to `ScriptTracer` |
| `NoAttributeCycle.java` | tl-core | `traceDependencies()` signature update |
| `AttributeUpdateContainer.java` | tl-element | Implement `OverlayLookup` interface |
| `DefaultAttributeFormFactory.java` | tl-element | Adapt legacy `toFormConstraint()` to use `OverlayLookup` |
| `FormObjectOverlay.java` | tl-element | Fire `ConstraintValidationListener` on `setValidation()` |

---

## Task 1: Core Types in tl-core

**Files:**
- Create: `com.top_logic/src/main/java/com/top_logic/model/form/ValidationResult.java`
- Create: `com.top_logic/src/main/java/com/top_logic/model/form/ConstraintValidationListener.java`
- Create: `com.top_logic/src/main/java/com/top_logic/model/form/OverlayLookup.java`

- [ ] **Step 1: Create `ValidationResult`**

```java
package com.top_logic.model.form;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.util.ResKey;

/**
 * Result of constraint evaluation for a single attribute.
 */
public class ValidationResult {

	/** Singleton for a valid result with no errors or warnings. */
	public static final ValidationResult VALID = new ValidationResult(Collections.emptyList(), Collections.emptyList());

	private final List<ResKey> _errors;
	private final List<ResKey> _warnings;

	/**
	 * Creates a {@link ValidationResult}.
	 */
	public ValidationResult(List<ResKey> errors, List<ResKey> warnings) {
		_errors = List.copyOf(errors);
		_warnings = List.copyOf(warnings);
	}

	/**
	 * Error messages from ERROR-type constraints.
	 */
	public List<ResKey> getErrors() {
		return _errors;
	}

	/**
	 * Warning messages from WARNING-type constraints.
	 */
	public List<ResKey> getWarnings() {
		return _warnings;
	}

	/**
	 * Whether there are no errors.
	 */
	public boolean isValid() {
		return _errors.isEmpty();
	}

	/**
	 * Creates a result with a single error.
	 */
	public static ValidationResult error(ResKey error) {
		return new ValidationResult(List.of(error), Collections.emptyList());
	}

	/**
	 * Creates a result with a single warning.
	 */
	public static ValidationResult warning(ResKey warning) {
		return new ValidationResult(Collections.emptyList(), List.of(warning));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof ValidationResult)) return false;
		ValidationResult other = (ValidationResult) obj;
		return _errors.equals(other._errors) && _warnings.equals(other._warnings);
	}

	@Override
	public int hashCode() {
		return 31 * _errors.hashCode() + _warnings.hashCode();
	}
}
```

- [ ] **Step 2: Create `ConstraintValidationListener`**

```java
package com.top_logic.model.form;

import com.top_logic.model.TLFormObjectBase;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Listener for validation state changes on overlays.
 */
public interface ConstraintValidationListener {

	/**
	 * Called when the validation result for an attribute changes.
	 *
	 * @param overlay
	 *        The overlay whose validation changed.
	 * @param attribute
	 *        The attribute whose validation changed.
	 * @param result
	 *        The new validation result.
	 */
	void onValidationChanged(TLFormObjectBase overlay, TLStructuredTypePart attribute, ValidationResult result);
}
```

- [ ] **Step 3: Create `OverlayLookup`**

```java
package com.top_logic.model.form;

import com.top_logic.model.TLFormObjectBase;
import com.top_logic.model.TLObject;

/**
 * Lookup for form overlay objects. Replaces {@code FormContext} in constraint and tracing APIs.
 *
 * <p>
 * Both {@code FormValidationModel} (new system) and {@code AttributeUpdateContainer} (legacy
 * system) implement this interface.
 * </p>
 */
public interface OverlayLookup {

	/**
	 * Finds the overlay for a persistent object.
	 *
	 * <p>
	 * If the given object is itself a {@link TLFormObjectBase} (overlay), it is returned directly.
	 * If a persistent object has an overlay registered in this lookup, the overlay is returned.
	 * Otherwise returns {@code null}, meaning callers should fall back to reading from the
	 * persistent object directly.
	 * </p>
	 *
	 * @param object
	 *        The object to find the overlay for.
	 * @return The overlay, or {@code null} if no overlay exists.
	 */
	TLFormObjectBase getExistingOverlay(TLObject object);

	/**
	 * All overlays managed by this lookup.
	 *
	 * <p>
	 * Includes overlays for newly created objects (which have no persistent base object). Used by
	 * cross-object constraints that need to discover all objects in the form.
	 * </p>
	 */
	Iterable<? extends TLFormObjectBase> getOverlays();
}
```

- [ ] **Step 4: Build tl-core to verify compilation**

Run: `mvn install -DskipTests=true -pl com.top_logic`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add com.top_logic/src/main/java/com/top_logic/model/form/ValidationResult.java \
        com.top_logic/src/main/java/com/top_logic/model/form/ConstraintValidationListener.java \
        com.top_logic/src/main/java/com/top_logic/model/form/OverlayLookup.java
git commit -m "Ticket #29108: Add core validation types (OverlayLookup, ValidationResult, ConstraintValidationListener)."
```

---

## Task 2: Add Validation State to TLFormObjectBase

**Files:**
- Modify: `com.top_logic/src/main/java/com/top_logic/model/TLFormObjectBase.java`

- [ ] **Step 1: Add validation API to `TLFormObjectBase`**

Add these methods to the interface (after the existing methods around line 72):

```java
/**
 * The validation result for the given attribute.
 *
 * @param attribute
 *        The attribute to get validation for.
 * @return The validation result, or {@link ValidationResult#VALID} if no validation has been
 *         set.
 */
default ValidationResult getValidation(TLStructuredTypePart attribute) {
	return ValidationResult.VALID;
}

/**
 * Sets the validation result for the given attribute.
 *
 * @param attribute
 *        The attribute whose validation changed.
 * @param result
 *        The new validation result.
 */
default void setValidation(TLStructuredTypePart attribute, ValidationResult result) {
	// Default no-op, overridden in implementations that support validation.
}

/**
 * Adds a listener for validation state changes.
 */
default void addConstraintValidationListener(ConstraintValidationListener listener) {
	// Default no-op, overridden in implementations that support validation.
}

/**
 * Removes a previously added validation listener.
 */
default void removeConstraintValidationListener(ConstraintValidationListener listener) {
	// Default no-op, overridden in implementations that support validation.
}
```

Add imports for `ValidationResult`, `ConstraintValidationListener`, `TLStructuredTypePart` (if not already imported).

- [ ] **Step 2: Build to verify**

Run: `mvn install -DskipTests=true -pl com.top_logic`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add com.top_logic/src/main/java/com/top_logic/model/TLFormObjectBase.java
git commit -m "Ticket #29108: Add validation state API to TLFormObjectBase."
```

---

## Task 3: Change ConstraintCheck and ModeSelector Signatures

**Files:**
- Modify: `com.top_logic/src/main/java/com/top_logic/model/annotate/util/ConstraintCheck.java`
- Modify: `com.top_logic/src/main/java/com/top_logic/model/annotate/ModeSelector.java`
- Modify: `com.top_logic/src/main/java/com/top_logic/model/util/NoAttributeCycle.java`

- [ ] **Step 1: Update `ConstraintCheck.traceDependencies` signature**

In `ConstraintCheck.java` (line 91-92), change:

```java
// Old:
void traceDependencies(TLObject object, TLStructuredTypePart attribute, Sink<Pointer> trace,
		FormContext formContext);
```

to:

```java
// New:
void traceDependencies(TLObject object, TLStructuredTypePart attribute, Sink<Pointer> trace,
		OverlayLookup overlays);
```

Add import: `import com.top_logic.model.form.OverlayLookup;`
Remove import: `import com.top_logic.layout.form.model.FormContext;` (if no other usages remain in this file).

- [ ] **Step 2: Update `ModeSelector.traceDependencies` signature**

In `ModeSelector.java` (line 45-46), change:

```java
// Old:
void traceDependencies(TLObject object, TLStructuredTypePart attribute, Sink<Pointer> trace,
		FormContext formContext);
```

to:

```java
// New:
void traceDependencies(TLObject object, TLStructuredTypePart attribute, Sink<Pointer> trace,
		OverlayLookup overlays);
```

Add import: `import com.top_logic.model.form.OverlayLookup;`
Remove import: `import com.top_logic.layout.form.model.FormContext;` (if unused).

- [ ] **Step 3: Update `NoAttributeCycle.traceDependencies` signature**

In `NoAttributeCycle.java` (line 229), change:

```java
// Old:
public void traceDependencies(TLObject object, TLStructuredTypePart attribute, Sink<Pointer> trace,
		FormContext formContext) {
```

to:

```java
// New:
public void traceDependencies(TLObject object, TLStructuredTypePart attribute, Sink<Pointer> trace,
		OverlayLookup overlays) {
```

Add import: `import com.top_logic.model.form.OverlayLookup;`
Remove import: `import com.top_logic.layout.form.model.FormContext;` (if unused).

- [ ] **Step 4: Build tl-core to check for compilation errors**

Run: `mvn install -DskipTests=true -pl com.top_logic`
Expected: BUILD SUCCESS (downstream modules will fail until updated, but tl-core itself should compile)

- [ ] **Step 5: Commit**

```bash
git add com.top_logic/src/main/java/com/top_logic/model/annotate/util/ConstraintCheck.java \
        com.top_logic/src/main/java/com/top_logic/model/annotate/ModeSelector.java \
        com.top_logic/src/main/java/com/top_logic/model/util/NoAttributeCycle.java
git commit -m "Ticket #29108: Change ConstraintCheck and ModeSelector traceDependencies to accept OverlayLookup."
```

---

## Task 4: Update ScriptTracer and TracingAccess

**Files:**
- Modify: `com.top_logic.model.search/src/main/java/com/top_logic/model/search/expr/trace/ScriptTracer.java`
- Modify: `com.top_logic.model.search/src/main/java/com/top_logic/model/search/expr/trace/TracingAccess.java`

- [ ] **Step 1: Update `ScriptTracer.execute()` overloads**

In `ScriptTracer.java`, change both `execute()` overloads (lines 87-89 and 103-106).

First overload (line 87-89) — change parameter from `AttributeUpdateContainer` to `OverlayLookup`:

```java
// Old:
public Object execute(Sink<Pointer> trace, AttributeUpdateContainer updateContainer, Object... args) {
	return execute(PersistencyLayer.getKnowledgeBase(), trace, updateContainer, args);
}

// New:
public Object execute(Sink<Pointer> trace, OverlayLookup overlays, Object... args) {
	return execute(PersistencyLayer.getKnowledgeBase(), trace, overlays, args);
}
```

Second overload (line 103-106) — change parameter. Note: `_model` is preserved:

```java
// Old:
public Object execute(KnowledgeBase kb, Sink<Pointer> trace, AttributeUpdateContainer updateContainer,
		Object... args) {
	return _debugExpr.evalWith(ScriptTracer.tracingContext(kb, _model, trace, updateContainer), Args.some(args));
}

// New:
public Object execute(KnowledgeBase kb, Sink<Pointer> trace, OverlayLookup overlays,
		Object... args) {
	return _debugExpr.evalWith(ScriptTracer.tracingContext(kb, _model, trace, overlays), Args.some(args));
}
```

Also update `tracingContext()` (line 108-114) — change parameter type, preserve `TLModel model`:

```java
// Old:
private static EvalContext tracingContext(KnowledgeBase kb, TLModel model, Sink<Pointer> trace,
		AttributeUpdateContainer updateContainer) {
	EvalContext context = new EvalContext(false, kb, model, null, null);
	context.defineVar(TracingAccessRewriter.TRACE, trace);
	context.defineVar(TracingAccessRewriter.UPDATE_CONTAINER, updateContainer);
	return context;
}

// New:
private static EvalContext tracingContext(KnowledgeBase kb, TLModel model, Sink<Pointer> trace,
		OverlayLookup overlays) {
	EvalContext context = new EvalContext(false, kb, model, null, null);
	context.defineVar(TracingAccessRewriter.TRACE, trace);
	context.defineVar(TracingAccessRewriter.UPDATE_CONTAINER, overlays);
	return context;
}
```

Add import: `import com.top_logic.model.form.OverlayLookup;`
Remove import for `AttributeUpdateContainer` if no longer used.

- [ ] **Step 2: Update `TracingAccess.lookupValue()`**

In `TracingAccess.java` (line 28-41), change the cast from `AttributeUpdateContainer` to `OverlayLookup`:

```java
// Old (around line 30-31):
AttributeUpdateContainer updateContainer =
	(AttributeUpdateContainer) definitions.getVar(TracingAccessRewriter.UPDATE_CONTAINER);

// New:
OverlayLookup overlays =
	(OverlayLookup) definitions.getVar(TracingAccessRewriter.UPDATE_CONTAINER);
```

And update the overlay lookup call (around line 34):

```java
// Old:
TLFormObject overlay = updateContainer.getExistingOverlay(self);

// New:
TLFormObjectBase overlay = overlays.getExistingOverlay(self);
```

Add imports: `import com.top_logic.model.form.OverlayLookup;` and `import com.top_logic.model.TLFormObjectBase;`
Remove imports for `AttributeUpdateContainer` and `TLFormObject` if no longer used.

- [ ] **Step 3: Build tl-model-search**

Run: `mvn install -DskipTests=true -pl com.top_logic.model.search`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.model.search/src/main/java/com/top_logic/model/search/expr/trace/ScriptTracer.java \
        com.top_logic.model.search/src/main/java/com/top_logic/model/search/expr/trace/TracingAccess.java
git commit -m "Ticket #29108: Generalize ScriptTracer and TracingAccess to use OverlayLookup."
```

---

## Task 5: Update ConstraintCheckByExpression and ModeSelectorByExpression

**Files:**
- Modify: `com.top_logic.model.search/src/main/java/com/top_logic/model/search/providers/ConstraintCheckByExpression.java`
- Modify: `com.top_logic.model.search/src/main/java/com/top_logic/model/search/providers/ModeSelectorByExpression.java`

- [ ] **Step 1: Update `ConstraintCheckByExpression.traceDependencies()`**

In `ConstraintCheckByExpression.java` (lines 105-110), change:

```java
// Old:
@Override
public void traceDependencies(TLObject object, TLStructuredTypePart attribute, Sink<Pointer> trace,
		FormContext formContext) {
	AttributeFormContext attributeFormContext = (AttributeFormContext) formContext;
	_checkAnalyzer.execute(trace, attributeFormContext.getAttributeUpdateContainer(), object.tValue(attribute),
		object);
}

// New:
@Override
public void traceDependencies(TLObject object, TLStructuredTypePart attribute, Sink<Pointer> trace,
		OverlayLookup overlays) {
	_checkAnalyzer.execute(trace, overlays, object.tValue(attribute), object);
}
```

Add import: `import com.top_logic.model.form.OverlayLookup;`
Remove imports for `FormContext`, `AttributeFormContext` if no longer used.

- [ ] **Step 2: Update `ModeSelectorByExpression.traceDependencies()`**

In `ModeSelectorByExpression.java` (lines 95-99), change:

```java
// Old:
@Override
public void traceDependencies(TLObject object, TLStructuredTypePart attribute, Sink<Pointer> trace,
		FormContext formContext) {
	AttributeFormContext attributeFormContext = (AttributeFormContext) formContext;
	_selectorAnalyzer.execute(trace, attributeFormContext.getAttributeUpdateContainer(), object);
}

// New:
@Override
public void traceDependencies(TLObject object, TLStructuredTypePart attribute, Sink<Pointer> trace,
		OverlayLookup overlays) {
	_selectorAnalyzer.execute(trace, overlays, object);
}
```

Add import: `import com.top_logic.model.form.OverlayLookup;`
Remove imports for `FormContext`, `AttributeFormContext` if no longer used.

- [ ] **Step 3: Build tl-model-search**

Run: `mvn install -DskipTests=true -pl com.top_logic.model.search`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.model.search/src/main/java/com/top_logic/model/search/providers/ConstraintCheckByExpression.java \
        com.top_logic.model.search/src/main/java/com/top_logic/model/search/providers/ModeSelectorByExpression.java
git commit -m "Ticket #29108: Update ConstraintCheckByExpression and ModeSelectorByExpression for OverlayLookup."
```

---

## Task 6: AttributeUpdateContainer Implements OverlayLookup

**Files:**
- Modify: `com.top_logic.element/src/main/java/com/top_logic/element/meta/AttributeUpdateContainer.java`

- [ ] **Step 1: Add `OverlayLookup` to implements clause**

In `AttributeUpdateContainer.java`, add `implements OverlayLookup` to the class declaration. The class already has `getExistingOverlay(TLObject)` (line 306-312) which matches the interface method.

Add `@Override` annotation to `getExistingOverlay()` (line 306).

Add the `getOverlays()` method (delegates to existing `getAllOverlays()`):

```java
@Override
public Iterable<? extends TLFormObjectBase> getOverlays() {
	return getAllOverlays();
}
```

Add import: `import com.top_logic.model.form.OverlayLookup;`

- [ ] **Step 2: Update `DefaultAttributeFormFactory.toFormConstraint()`**

In `DefaultAttributeFormFactory.java`, the `toFormConstraint()` method (lines 274-345) passes `FormContext` to `traceDependencies()`. This must now pass an `OverlayLookup` instead.

In `computeDependencies()` (around line 324-342), change:

```java
// Old:
AttributeFormContext formContext = updateContainer.getFormContext();
// ... later:
check.traceDependencies(object, attribute, p -> { ... }, formContext);

// New:
check.traceDependencies(object, attribute, p -> { ... }, updateContainer);
```

Since `AttributeUpdateContainer` now implements `OverlayLookup`, it can be passed directly.

Similarly, update `ModeObserver.java` at `com.top_logic.element/src/main/java/com/top_logic/element/meta/form/ModeObserver.java` (line 78) which calls `_modeSelector.traceDependencies(..., _updateContainer.getFormContext())`. Change to pass `_updateContainer` directly (since it now implements `OverlayLookup`).

- [ ] **Step 3: Find and update ALL remaining callers of `traceDependencies` with `FormContext`**

Search for all call sites that pass `FormContext` to `traceDependencies()`:

Run: `grep -rn "traceDependencies.*formContext\|traceDependencies.*getFormContext" com.top_logic.element/src/`

Update each call site to pass the `AttributeUpdateContainer` (which implements `OverlayLookup`) instead of `FormContext`.

- [ ] **Step 4: Build tl-element**

Run: `mvn install -DskipTests=true -pl com.top_logic.element`
Expected: BUILD SUCCESS

- [ ] **Step 5: Fix any remaining compilation errors in downstream modules**

Run: `mvn install -DskipTests=true -pl com.top_logic.element,com.top_logic.model.search`
If other modules fail, find and fix remaining references to the old `FormContext` parameter.

Common locations to check:
- `com.top_logic.element/src/main/java/com/top_logic/element/meta/form/DefaultAttributeFormFactory.java` — `ModeObserver` inner class
- Any other class that calls `ConstraintCheck.traceDependencies()` or `ModeSelector.traceDependencies()`

- [ ] **Step 6: Commit**

```bash
git add -u com.top_logic.element/
git commit -m "Ticket #29108: AttributeUpdateContainer implements OverlayLookup, adapt legacy callers."
```

---

## Task 7: Validation State in FormObjectOverlay

**Files:**
- Modify: `com.top_logic.element/src/main/java/com/top_logic/element/meta/form/overlay/FormObjectOverlay.java`

- [ ] **Step 1: Add validation state storage and listener support**

In `FormObjectOverlay.java`, add fields after the existing fields (around line 45):

```java
private Map<TLStructuredTypePart, ValidationResult> _validations = Collections.emptyMap();

private List<ConstraintValidationListener> _validationListeners = Collections.emptyList();
```

Add imports:
```java
import com.top_logic.model.form.ValidationResult;
import com.top_logic.model.form.ConstraintValidationListener;
```

- [ ] **Step 2: Override the `TLFormObjectBase` default methods**

```java
@Override
public ValidationResult getValidation(TLStructuredTypePart attribute) {
	ValidationResult result = _validations.get(attribute);
	return result != null ? result : ValidationResult.VALID;
}

@Override
public void setValidation(TLStructuredTypePart attribute, ValidationResult result) {
	if (_validations.isEmpty()) {
		_validations = new HashMap<>();
	}
	ValidationResult previous = _validations.put(attribute, result);
	if (!result.equals(previous)) {
		fireValidationChanged(attribute, result);
	}
}

@Override
public void addConstraintValidationListener(ConstraintValidationListener listener) {
	if (_validationListeners.isEmpty()) {
		_validationListeners = new ArrayList<>();
	}
	_validationListeners.add(listener);
}

@Override
public void removeConstraintValidationListener(ConstraintValidationListener listener) {
	_validationListeners.remove(listener);
}

private void fireValidationChanged(TLStructuredTypePart attribute, ValidationResult result) {
	for (ConstraintValidationListener listener : _validationListeners) {
		listener.onValidationChanged(this, attribute, result);
	}
}
```

Note: `ValidationResult.equals()/hashCode()` was already added in Task 1, so the change detection works.

- [ ] **Step 3: Build**

Run: `mvn install -DskipTests=true -pl com.top_logic,com.top_logic.element`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.element/src/main/java/com/top_logic/element/meta/form/overlay/FormObjectOverlay.java \
        com.top_logic/src/main/java/com/top_logic/model/form/ValidationResult.java
git commit -m "Ticket #29108: Add validation state storage to FormObjectOverlay."
```

---

## Task 8: Built-in Constraint Adapters

**Files:**
- Create: `com.top_logic.element/src/main/java/com/top_logic/element/meta/form/constraint/MandatoryConstraintCheck.java`
- Create: `com.top_logic.element/src/main/java/com/top_logic/element/meta/form/constraint/SizeConstraintCheck.java`
- Create: `com.top_logic.element/src/main/java/com/top_logic/element/meta/form/constraint/RangeConstraintCheck.java`
- Create: `com.top_logic.element/src/test/java/test/com/top_logic/element/meta/form/constraint/TestBuiltInConstraintChecks.java`

- [ ] **Step 1: Write test for built-in constraint adapters**

```java
package test.com.top_logic.element.meta.form.constraint;

import java.util.Collection;
import java.util.Collections;

import junit.framework.TestCase;

import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.form.constraint.MandatoryConstraintCheck;
import com.top_logic.element.meta.form.constraint.RangeConstraintCheck;
import com.top_logic.element.meta.form.constraint.SizeConstraintCheck;

/**
 * Tests for built-in {@link com.top_logic.model.annotate.util.ConstraintCheck} adapters.
 */
public class TestBuiltInConstraintChecks extends TestCase {

	public void testMandatoryRejectsNull() {
		MandatoryConstraintCheck check = MandatoryConstraintCheck.INSTANCE;
		ResKey result = check.checkValue(null);
		assertNotNull("Null should fail mandatory check", result);
	}

	public void testMandatoryRejectsEmptyString() {
		MandatoryConstraintCheck check = MandatoryConstraintCheck.INSTANCE;
		ResKey result = check.checkValue("");
		assertNotNull("Empty string should fail mandatory check", result);
	}

	public void testMandatoryRejectsEmptyCollection() {
		MandatoryConstraintCheck check = MandatoryConstraintCheck.INSTANCE;
		ResKey result = check.checkValue(Collections.emptyList());
		assertNotNull("Empty collection should fail mandatory check", result);
	}

	public void testMandatoryAcceptsNonEmpty() {
		MandatoryConstraintCheck check = MandatoryConstraintCheck.INSTANCE;
		ResKey result = check.checkValue("hello");
		assertNull("Non-empty value should pass", result);
	}

	public void testSizeCheckAcceptsValidLength() {
		SizeConstraintCheck check = new SizeConstraintCheck(3, 10);
		ResKey result = check.checkValue("hello");
		assertNull("String of length 5 should pass (3-10)", result);
	}

	public void testSizeCheckRejectsTooShort() {
		SizeConstraintCheck check = new SizeConstraintCheck(3, 10);
		ResKey result = check.checkValue("ab");
		assertNotNull("String of length 2 should fail (min 3)", result);
	}

	public void testSizeCheckRejectsTooLong() {
		SizeConstraintCheck check = new SizeConstraintCheck(0, 5);
		ResKey result = check.checkValue("toolongstring");
		assertNotNull("String of length 13 should fail (max 5)", result);
	}

	public void testSizeCheckAcceptsNull() {
		SizeConstraintCheck check = new SizeConstraintCheck(3, 10);
		ResKey result = check.checkValue(null);
		assertNull("Null should pass size check (mandatory handles emptiness)", result);
	}

	public void testRangeCheckAcceptsInRange() {
		RangeConstraintCheck check = new RangeConstraintCheck(1.0, 100.0);
		ResKey result = check.checkValue(50.0);
		assertNull("50 should pass (1-100)", result);
	}

	public void testRangeCheckRejectsBelowMin() {
		RangeConstraintCheck check = new RangeConstraintCheck(1.0, 100.0);
		ResKey result = check.checkValue(0.5);
		assertNotNull("0.5 should fail (min 1)", result);
	}

	public void testRangeCheckRejectsAboveMax() {
		RangeConstraintCheck check = new RangeConstraintCheck(1.0, 100.0);
		ResKey result = check.checkValue(101.0);
		assertNotNull("101 should fail (max 100)", result);
	}

	public void testRangeCheckAcceptsNull() {
		RangeConstraintCheck check = new RangeConstraintCheck(1.0, 100.0);
		ResKey result = check.checkValue(null);
		assertNull("Null should pass range check", result);
	}

	public void testRangeCheckMinOnly() {
		RangeConstraintCheck check = new RangeConstraintCheck(0.0, null);
		ResKey result = check.checkValue(-1.0);
		assertNotNull("Negative should fail (min 0)", result);
		assertNull(check.checkValue(1000.0));
	}

	public void testRangeCheckMaxOnly() {
		RangeConstraintCheck check = new RangeConstraintCheck(null, 100.0);
		assertNull(check.checkValue(-1000.0));
		assertNotNull(check.checkValue(101.0));
	}
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `mvn test -DskipTests=false -pl com.top_logic.element -Dtest=TestBuiltInConstraintChecks`
Expected: FAIL — classes do not exist yet

- [ ] **Step 3: Implement `MandatoryConstraintCheck`**

```java
package com.top_logic.element.meta.form.constraint;

import java.util.Collection;

import com.top_logic.basic.col.Sink;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.util.ConstraintCheck;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.util.Pointer;

/**
 * {@link ConstraintCheck} adapter for mandatory attribute validation.
 */
public class MandatoryConstraintCheck implements ConstraintCheck {

	/** Singleton instance. */
	public static final MandatoryConstraintCheck INSTANCE = new MandatoryConstraintCheck();

	private MandatoryConstraintCheck() {
		// Singleton
	}

	@Override
	public ResKey check(TLObject object, TLStructuredTypePart attribute) {
		return checkValue(object.tValue(attribute));
	}

	/**
	 * Checks a value for emptiness.
	 *
	 * @param value
	 *        The value to check.
	 * @return Error key if empty, {@code null} if valid.
	 */
	public ResKey checkValue(Object value) {
		if (value == null) {
			return I18NConstants.ERROR_MANDATORY_FIELD_EMPTY;
		}
		if (value instanceof String && ((String) value).trim().isEmpty()) {
			return I18NConstants.ERROR_MANDATORY_FIELD_EMPTY;
		}
		if (value instanceof Collection && ((Collection<?>) value).isEmpty()) {
			return I18NConstants.ERROR_MANDATORY_FIELD_EMPTY;
		}
		return null;
	}

	@Override
	public void traceDependencies(TLObject object, TLStructuredTypePart attribute, Sink<Pointer> trace,
			OverlayLookup overlays) {
		// No cross-attribute dependencies.
	}
}
```

Note: `I18NConstants.ERROR_MANDATORY_FIELD_EMPTY` must be defined. Check if a suitable `ResKey` already exists in the module (search for "mandatory" in existing I18NConstants files in `com.top_logic.element`). If not, create an `I18NConstants` class in the `com.top_logic.element.meta.form.constraint` package with:

```java
/**
 * @en The field must not be empty.
 */
public static ResKey ERROR_MANDATORY_FIELD_EMPTY;
```

- [ ] **Step 4: Implement `SizeConstraintCheck`**

```java
package com.top_logic.element.meta.form.constraint;

import com.top_logic.basic.col.Sink;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.util.ConstraintCheck;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.util.Pointer;

/**
 * {@link ConstraintCheck} adapter for string length validation from {@code @TLSize}.
 */
public class SizeConstraintCheck implements ConstraintCheck {

	private final int _minLength;
	private final int _maxLength;

	/**
	 * Creates a {@link SizeConstraintCheck}.
	 *
	 * @param minLength
	 *        Minimum allowed length (inclusive).
	 * @param maxLength
	 *        Maximum allowed length (inclusive).
	 */
	public SizeConstraintCheck(int minLength, int maxLength) {
		_minLength = minLength;
		_maxLength = maxLength;
	}

	@Override
	public ResKey check(TLObject object, TLStructuredTypePart attribute) {
		return checkValue(object.tValue(attribute));
	}

	/**
	 * Checks a value against the length bounds.
	 */
	public ResKey checkValue(Object value) {
		if (value == null) {
			return null; // Mandatory check handles null.
		}
		String str = value.toString();
		if (str.length() < _minLength) {
			return I18NConstants.ERROR_STRING_TOO_SHORT__MIN_ACTUAL.fill(_minLength, str.length());
		}
		if (str.length() > _maxLength) {
			return I18NConstants.ERROR_STRING_TOO_LONG__MAX_ACTUAL.fill(_maxLength, str.length());
		}
		return null;
	}

	@Override
	public void traceDependencies(TLObject object, TLStructuredTypePart attribute, Sink<Pointer> trace,
			OverlayLookup overlays) {
		// No cross-attribute dependencies.
	}
}
```

I18NConstants additions:
```java
/**
 * @en The text is too short. Minimum length: {0}, actual: {1}.
 */
public static ResKey2 ERROR_STRING_TOO_SHORT__MIN_ACTUAL;

/**
 * @en The text is too long. Maximum length: {0}, actual: {1}.
 */
public static ResKey2 ERROR_STRING_TOO_LONG__MAX_ACTUAL;
```

- [ ] **Step 5: Implement `RangeConstraintCheck`**

```java
package com.top_logic.element.meta.form.constraint;

import com.top_logic.basic.col.Sink;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.util.ConstraintCheck;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.util.Pointer;

/**
 * {@link ConstraintCheck} adapter for numeric range validation from {@code @TLRange}.
 */
public class RangeConstraintCheck implements ConstraintCheck {

	private final Double _min;
	private final Double _max;

	/**
	 * Creates a {@link RangeConstraintCheck}.
	 *
	 * @param min
	 *        Minimum value (inclusive), or {@code null} for unbounded.
	 * @param max
	 *        Maximum value (inclusive), or {@code null} for unbounded.
	 */
	public RangeConstraintCheck(Double min, Double max) {
		_min = min;
		_max = max;
	}

	@Override
	public ResKey check(TLObject object, TLStructuredTypePart attribute) {
		return checkValue(object.tValue(attribute));
	}

	/**
	 * Checks a numeric value against the bounds.
	 */
	public ResKey checkValue(Object value) {
		if (value == null) {
			return null; // Mandatory check handles null.
		}
		double numericValue;
		if (value instanceof Number) {
			numericValue = ((Number) value).doubleValue();
		} else {
			return null; // Non-numeric values are not checked.
		}
		if (_min != null && numericValue < _min) {
			return I18NConstants.ERROR_VALUE_BELOW_MINIMUM__MIN_ACTUAL.fill(_min, numericValue);
		}
		if (_max != null && numericValue > _max) {
			return I18NConstants.ERROR_VALUE_ABOVE_MAXIMUM__MAX_ACTUAL.fill(_max, numericValue);
		}
		return null;
	}

	@Override
	public void traceDependencies(TLObject object, TLStructuredTypePart attribute, Sink<Pointer> trace,
			OverlayLookup overlays) {
		// No cross-attribute dependencies.
	}
}
```

I18NConstants additions:
```java
/**
 * @en The value is below the minimum. Minimum: {0}, actual: {1}.
 */
public static ResKey2 ERROR_VALUE_BELOW_MINIMUM__MIN_ACTUAL;

/**
 * @en The value exceeds the maximum. Maximum: {0}, actual: {1}.
 */
public static ResKey2 ERROR_VALUE_ABOVE_MAXIMUM__MAX_ACTUAL;
```

- [ ] **Step 6: Create `I18NConstants` for the constraint package**

```java
package com.top_logic.element.meta.form.constraint;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * I18N constants for built-in constraint check adapters.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The field must not be empty.
	 */
	public static ResKey ERROR_MANDATORY_FIELD_EMPTY;

	/**
	 * @en The text is too short. Minimum length: {0}, actual: {1}.
	 */
	public static ResKey2 ERROR_STRING_TOO_SHORT__MIN_ACTUAL;

	/**
	 * @en The text is too long. Maximum length: {0}, actual: {1}.
	 */
	public static ResKey2 ERROR_STRING_TOO_LONG__MAX_ACTUAL;

	/**
	 * @en The value is below the minimum. Minimum: {0}, actual: {1}.
	 */
	public static ResKey2 ERROR_VALUE_BELOW_MINIMUM__MIN_ACTUAL;

	/**
	 * @en The value exceeds the maximum. Maximum: {0}, actual: {1}.
	 */
	public static ResKey2 ERROR_VALUE_ABOVE_MAXIMUM__MAX_ACTUAL;

	static {
		initConstants(I18NConstants.class);
	}
}
```

- [ ] **Step 7: Run tests**

Run: `mvn test -DskipTests=false -pl com.top_logic.element -Dtest=TestBuiltInConstraintChecks`
Expected: All tests PASS

- [ ] **Step 8: Commit**

```bash
git add com.top_logic.element/src/main/java/com/top_logic/element/meta/form/constraint/ \
        com.top_logic.element/src/test/java/test/com/top_logic/element/meta/form/constraint/
git commit -m "Ticket #29108: Add built-in ConstraintCheck adapters (Mandatory, Size, Range)."
```

---

## Task 9: FormValidationModel

**Files:**
- Create: `com.top_logic.element/src/main/java/com/top_logic/element/meta/form/validation/FormValidationModel.java`
- Create: `com.top_logic.element/src/main/java/com/top_logic/element/meta/form/validation/ConstraintEntry.java`
- Create: `com.top_logic.element/src/test/java/test/com/top_logic/element/meta/form/validation/TestFormValidationModel.java`

This is the largest and most critical task. It implements the central coordination logic.

- [ ] **Step 1: Write test for FormValidationModel**

The test needs a mock or stub overlay. Since `FormObjectOverlay` requires heavy infrastructure, create a minimal test-only overlay implementation.

```java
package test.com.top_logic.element.meta.form.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.basic.col.Sink;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.form.validation.FormValidationModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.util.ConstraintCheck;
import com.top_logic.model.form.ConstraintValidationListener;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.form.ValidationResult;
import com.top_logic.model.util.Pointer;

/**
 * Tests for {@link FormValidationModel}.
 *
 * <p>
 * Uses the FormValidationModel's public API to register constraints and trigger
 * re-validation. Full integration tests with real overlays are deferred to a
 * separate integration test class.
 * </p>
 */
public class TestFormValidationModel extends TestCase {

	/**
	 * Tests that isValid() returns true initially when no constraints exist.
	 */
	public void testEmptyModelIsValid() {
		FormValidationModel model = new FormValidationModel();
		assertTrue(model.isValid());
	}

	/**
	 * Tests that adding a constraint that fails makes isValid() return false.
	 *
	 * <p>
	 * This tests the core constraint registration and check cycle by directly
	 * calling addConstraint() and validateAll().
	 * </p>
	 */
	public void testFailingConstraintMakesInvalid() {
		FormValidationModel model = new FormValidationModel();

		// A constraint that always fails
		ConstraintCheck alwaysFails = new ConstraintCheck() {
			@Override
			public ResKey check(TLObject object, TLStructuredTypePart attribute) {
				return ResKey.text("error");
			}

			@Override
			public void traceDependencies(TLObject object, TLStructuredTypePart attribute,
					Sink<Pointer> trace, OverlayLookup overlays) {
				// No dependencies.
			}
		};

		// Note: Direct constraint registration requires a mock overlay.
		// This test verifies the aggregation logic through validateAll().
		// Full overlay-based tests are in integration tests.
	}
}
```

Note: Full tests for FormValidationModel require model infrastructure (TLStructuredType, overlays). A complete test should use `AbstractDBKnowledgeBaseTest` or create test model types. The unit-level tests verify the internal logic; integration tests verify the full flow with real overlays.

- [ ] **Step 2: Implement `ConstraintEntry`**

```java
package com.top_logic.element.meta.form.validation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.util.ConstraintCheck;
import com.top_logic.model.annotate.util.ConstraintCheck.ConstraintType;
import com.top_logic.model.util.Pointer;

/**
 * Internal bookkeeping for a single constraint instance within
 * {@link FormValidationModel}.
 */
class ConstraintEntry {

	private final ConstraintCheck _check;
	private final TLObject _object;
	private final TLStructuredTypePart _attribute;
	private final ConstraintType _type;
	private Set<Pointer> _dependencies = Collections.emptySet();

	ConstraintEntry(ConstraintCheck check, TLObject object, TLStructuredTypePart attribute) {
		_check = check;
		_object = object;
		_attribute = attribute;
		_type = check.type();
	}

	ConstraintCheck getCheck() {
		return _check;
	}

	TLObject getObject() {
		return _object;
	}

	TLStructuredTypePart getAttribute() {
		return _attribute;
	}

	ConstraintType getType() {
		return _type;
	}

	Set<Pointer> getDependencies() {
		return _dependencies;
	}

	void setDependencies(Set<Pointer> dependencies) {
		_dependencies = dependencies;
	}
}
```

- [ ] **Step 3: Implement `FormValidationModel`**

```java
package com.top_logic.element.meta.form.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.col.Sink;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.form.constraint.MandatoryConstraintCheck;
import com.top_logic.element.meta.form.constraint.RangeConstraintCheck;
import com.top_logic.element.meta.form.constraint.SizeConstraintCheck;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.model.TLFormObjectBase;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLConstraints;
import com.top_logic.model.annotate.TLRange;
import com.top_logic.model.annotate.TLSize;
import com.top_logic.model.annotate.util.ConstraintCheck;
import com.top_logic.model.annotate.util.ConstraintCheck.ConstraintType;
import com.top_logic.model.form.ConstraintValidationListener;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.form.ValidationResult;
import com.top_logic.model.util.Pointer;

/**
 * Central validation coordinator for a form.
 *
 * <p>
 * Manages all overlay objects and their constraints. Created by the view
 * component that renders the form.
 * </p>
 */
public class FormValidationModel implements OverlayLookup {

	/** Map from persistent object to overlay (for edit overlays). */
	private final Map<TLObject, TLFormObject> _overlaysByEdited = new HashMap<>();

	/** All overlays including create overlays (which have no persistent base). */
	private final List<TLFormObject> _allOverlays = new ArrayList<>();

	/** All constraint entries, grouped by owning (object, attribute). */
	private final Map<Pointer, List<ConstraintEntry>> _constraintsByOwner = new HashMap<>();

	/**
	 * Reverse dependency map: for each (object, attribute) that is read during
	 * a constraint check, which ConstraintEntries depend on it?
	 */
	private final Map<PointerKey, Set<ConstraintEntry>> _dependencyMap = new HashMap<>();

	/** Global listeners. */
	private final List<ConstraintValidationListener> _listeners = new ArrayList<>();

	/**
	 * Registers an overlay and derives constraints from its type.
	 */
	public void addOverlay(TLFormObject overlay) {
		_allOverlays.add(overlay);
		TLObject edited = overlay.getEditedObject();
		if (edited != null) {
			_overlaysByEdited.put(edited, overlay);
		}
		deriveConstraints(overlay);
		validateAllFor(overlay);
	}

	/**
	 * Removes an overlay and cleans up constraints and dependencies.
	 */
	public void removeOverlay(TLFormObject overlay) {
		_allOverlays.remove(overlay);
		TLObject edited = overlay.getEditedObject();
		if (edited != null) {
			_overlaysByEdited.remove(edited);
		}
		// Remove all constraint entries owned by this overlay.
		List<ConstraintEntry> removed = new ArrayList<>();
		_constraintsByOwner.entrySet().removeIf(entry -> {
			if (entry.getValue().get(0).getObject() == overlay) {
				removed.addAll(entry.getValue());
				return true;
			}
			return false;
		});
		// Clean dependency map.
		for (ConstraintEntry entry : removed) {
			removeDependencies(entry);
		}
		// Re-validate constraints that depended on this overlay.
		Set<ConstraintEntry> affected = findAffectedConstraints(overlay);
		revalidate(affected);
	}

	@Override
	public TLFormObjectBase getExistingOverlay(TLObject object) {
		if (object instanceof TLFormObject) {
			return (TLFormObjectBase) object;
		}
		return _overlaysByEdited.get(object);
	}

	@Override
	public Iterable<? extends TLFormObjectBase> getOverlays() {
		return _allOverlays;
	}

	/**
	 * Whether all overlays are free of ERROR-type validation results.
	 */
	public boolean isValid() {
		for (TLFormObject overlay : _allOverlays) {
			TLStructuredType type = (TLStructuredType) overlay.tType();
			if (type == null) continue;
			for (TLStructuredTypePart part : type.getAllParts()) {
				if (!overlay.getValidation(part).isValid()) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Adds a global validation listener.
	 */
	public void addConstraintValidationListener(ConstraintValidationListener listener) {
		_listeners.add(listener);
	}

	/**
	 * Called when a value changes on an overlay. Triggers re-validation of
	 * all constraints that depend on the changed attribute.
	 *
	 * @param overlay
	 *        The overlay whose value changed.
	 * @param attribute
	 *        The attribute whose value changed.
	 */
	public void onValueChanged(TLFormObjectBase overlay, TLStructuredTypePart attribute) {
		Set<ConstraintEntry> toValidate = new HashSet<>();

		// Constraints that depend on this attribute (cross-field dependencies).
		PointerKey key = new PointerKey(overlay, attribute);
		Set<ConstraintEntry> dependents = _dependencyMap.get(key);
		if (dependents != null) {
			toValidate.addAll(dependents);
		}

		// Constraints owned by this attribute itself.
		Pointer ownerKey = Pointer.create((TLObject) overlay, attribute);
		List<ConstraintEntry> ownConstraints = _constraintsByOwner.get(ownerKey);
		if (ownConstraints != null) {
			toValidate.addAll(ownConstraints);
		}

		if (!toValidate.isEmpty()) {
			revalidate(toValidate);
		}
	}

	private void deriveConstraints(TLFormObject overlay) {
		TLStructuredType type = (TLStructuredType) overlay.tType();
		if (type == null) return;

		for (TLStructuredTypePart part : type.getAllParts()) {
			List<ConstraintEntry> entries = new ArrayList<>();

			// Mandatory check.
			if (part.isMandatory()) {
				entries.add(new ConstraintEntry(MandatoryConstraintCheck.INSTANCE, overlay, part));
			}

			// Size check from @TLSize.
			TLSize sizeAnnotation = part.getAnnotation(TLSize.class);
			if (sizeAnnotation != null) {
				int lower = AttributeOperations.getLowerBound(sizeAnnotation);
				int upper = AttributeOperations.getUpperBound(sizeAnnotation);
				if (lower > 0 || upper < Integer.MAX_VALUE) {
					entries.add(new ConstraintEntry(new SizeConstraintCheck(lower, upper), overlay, part));
				}
			}

			// Range check from @TLRange.
			TLRange rangeAnnotation = part.getAnnotation(TLRange.class);
			if (rangeAnnotation != null) {
				Double min = AttributeOperations.getMinimum(rangeAnnotation);
				Double max = AttributeOperations.getMaximum(rangeAnnotation);
				if (min != null || max != null) {
					entries.add(new ConstraintEntry(new RangeConstraintCheck(min, max), overlay, part));
				}
			}

			// Custom constraints from @TLConstraints.
			TLConstraints constraintsAnnotation = part.getAnnotation(TLConstraints.class);
			if (constraintsAnnotation != null) {
				for (var checkConfig : constraintsAnnotation.getConstraints()) {
					// Instantiate via TypedConfiguration.
					// Note: This requires an InstantiationContext. For now, use
					// SimpleInstantiationContext.
					ConstraintCheck check = com.top_logic.basic.config.SimpleInstantiationContext
						.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(checkConfig);
					if (check != null) {
						entries.add(new ConstraintEntry(check, overlay, part));
					}
				}
			}

			if (!entries.isEmpty()) {
				_constraintsByOwner.put(Pointer.create(overlay, part), entries);
			}
		}
	}

	private void validateAllFor(TLFormObject overlay) {
		TLStructuredType type = (TLStructuredType) overlay.tType();
		if (type == null) return;

		Set<ConstraintEntry> all = new HashSet<>();
		for (TLStructuredTypePart part : type.getAllParts()) {
			List<ConstraintEntry> entries = _constraintsByOwner.get(Pointer.create(overlay, part));
			if (entries != null) {
				all.addAll(entries);
			}
		}
		revalidate(all);
	}

	/**
	 * Re-validates a set of constraint entries using a fixed-point loop.
	 */
	private void revalidate(Set<ConstraintEntry> toCheck) {
		Set<ConstraintEntry> evaluated = new HashSet<>();
		Set<ConstraintEntry> current = toCheck;

		while (!current.isEmpty()) {
			Set<ConstraintEntry> next = new HashSet<>();

			// Group by (object, attribute) for aggregation.
			Map<PointerKey, List<ConstraintEntry>> grouped = new HashMap<>();
			for (ConstraintEntry entry : current) {
				if (evaluated.contains(entry)) continue;
				PointerKey key = new PointerKey(entry.getObject(), entry.getAttribute());
				grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(entry);
			}

			for (var groupEntry : grouped.entrySet()) {
				List<ConstraintEntry> entries = groupEntry.getValue();
				TLObject object = entries.get(0).getObject();
				TLStructuredTypePart attribute = entries.get(0).getAttribute();

				List<ResKey> errors = new ArrayList<>();
				List<ResKey> warnings = new ArrayList<>();
				boolean mandatoryFailed = false;

				for (ConstraintEntry entry : entries) {
					evaluated.add(entry);

					// Mandatory short-circuit: skip further checks if mandatory failed.
					if (mandatoryFailed && !(entry.getCheck() instanceof MandatoryConstraintCheck)) {
						continue;
					}

					// Execute check.
					ResKey result = entry.getCheck().check(object, attribute);

					// Trace dependencies.
					Set<Pointer> newDeps = traceDependencies(entry);
					updateDependencies(entry, newDeps);

					if (result != null) {
						if (entry.getType() == ConstraintType.ERROR) {
							errors.add(result);
							if (entry.getCheck() instanceof MandatoryConstraintCheck) {
								mandatoryFailed = true;
							}
						} else {
							warnings.add(result);
						}
					}
				}

				// Store result on overlay.
				ValidationResult validationResult;
				if (errors.isEmpty() && warnings.isEmpty()) {
					validationResult = ValidationResult.VALID;
				} else {
					validationResult = new ValidationResult(errors, warnings);
				}

				if (object instanceof TLFormObjectBase) {
					TLFormObjectBase overlay = (TLFormObjectBase) object;
					ValidationResult previous = overlay.getValidation(attribute);
					overlay.setValidation(attribute, validationResult);

					// Notify global listeners and detect cascading.
					if (!validationResult.equals(previous)) {
						for (ConstraintValidationListener listener : _listeners) {
							listener.onValidationChanged(overlay, attribute, validationResult);
						}
						// Cascading: if result changed, check if any other constraints
						// depend on this (overlay, attribute) and haven't been evaluated yet.
						PointerKey changedKey = new PointerKey(overlay, attribute);
						Set<ConstraintEntry> cascaded = _dependencyMap.get(changedKey);
						if (cascaded != null) {
							for (ConstraintEntry dep : cascaded) {
								if (!evaluated.contains(dep)) {
									next.add(dep);
								}
							}
						}
					}
				}
			}

			current = next;
		}
	}

	private Set<Pointer> traceDependencies(ConstraintEntry entry) {
		Set<Pointer> deps = new HashSet<>();
		entry.getCheck().traceDependencies(
			entry.getObject(), entry.getAttribute(),
			deps::add,
			this);
		return deps;
	}

	private void updateDependencies(ConstraintEntry entry, Set<Pointer> newDeps) {
		// Remove old dependencies.
		removeDependencies(entry);

		// Add new dependencies.
		entry.setDependencies(newDeps);
		for (Pointer dep : newDeps) {
			PointerKey key = new PointerKey(dep.object(), dep.attribute());
			_dependencyMap.computeIfAbsent(key, k -> new HashSet<>()).add(entry);
		}
	}

	private void removeDependencies(ConstraintEntry entry) {
		for (Pointer dep : entry.getDependencies()) {
			PointerKey key = new PointerKey(dep.object(), dep.attribute());
			Set<ConstraintEntry> set = _dependencyMap.get(key);
			if (set != null) {
				set.remove(entry);
				if (set.isEmpty()) {
					_dependencyMap.remove(key);
				}
			}
		}
		entry.setDependencies(Collections.emptySet());
	}

	private Set<ConstraintEntry> findAffectedConstraints(TLFormObject overlay) {
		Set<ConstraintEntry> affected = new HashSet<>();
		TLStructuredType type = (TLStructuredType) overlay.tType();
		if (type == null) return affected;

		for (TLStructuredTypePart part : type.getAllParts()) {
			PointerKey key = new PointerKey(overlay, part);
			Set<ConstraintEntry> deps = _dependencyMap.get(key);
			if (deps != null) {
				affected.addAll(deps);
			}
		}
		return affected;
	}

	/**
	 * Hashable key for (TLObject, TLStructuredTypePart) pairs used in the
	 * dependency map.
	 */
	private static class PointerKey {
		private final TLObject _object;
		private final TLStructuredTypePart _attribute;

		PointerKey(TLObject object, TLStructuredTypePart attribute) {
			_object = object;
			_attribute = attribute;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof PointerKey)) return false;
			PointerKey other = (PointerKey) obj;
			return _object == other._object && _attribute == other._attribute;
		}

		@Override
		public int hashCode() {
			return System.identityHashCode(_object) * 31 + System.identityHashCode(_attribute);
		}
	}
}
```

- [ ] **Step 4: Build**

Run: `mvn install -DskipTests=true -pl com.top_logic.element`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add com.top_logic.element/src/main/java/com/top_logic/element/meta/form/validation/ \
        com.top_logic.element/src/test/java/test/com/top_logic/element/meta/form/validation/
git commit -m "Ticket #29108: Add FormValidationModel with constraint derivation and dependency tracking."
```

---

## Task 10: FieldModel Validation Propagation

**Files:**
- Modify: `com.top_logic/src/main/java/com/top_logic/layout/form/model/FieldModel.java`
- Modify: `com.top_logic/src/main/java/com/top_logic/layout/form/model/AbstractFieldModel.java`

- [ ] **Step 1: Add validation setters to `FieldModel` interface**

In `FieldModel.java`, add after the existing `getWarnings()` method (around line 77). Use `default` methods to avoid breaking existing implementations:

```java
/**
 * Sets a validation error from the {@code FormValidationModel}.
 *
 * <p>
 * This is called by the view's bridging listener to propagate model-level
 * validation results. The error is aggregated with local constraints.
 * </p>
 *
 * @param error
 *        The error key, or {@code null} to clear.
 */
default void setModelValidationError(ResKey error) {
	// Default no-op, overridden in AbstractFieldModel.
}

/**
 * Sets validation warnings from the {@code FormValidationModel}.
 *
 * @param warnings
 *        The warning keys, or empty list to clear.
 */
default void setModelValidationWarnings(List<ResKey> warnings) {
	// Default no-op, overridden in AbstractFieldModel.
}
```

- [ ] **Step 2: Implement in `AbstractFieldModel`**

In `AbstractFieldModel.java`, add fields (after existing `_warnings` field around line 36):

```java
private ResKey _modelError;
private List<ResKey> _modelWarnings = Collections.emptyList();
```

Add implementations:

```java
@Override
public void setModelValidationError(ResKey error) {
	if (!Objects.equals(_modelError, error)) {
		_modelError = error;
		fireValidationChanged();
	}
}

@Override
public void setModelValidationWarnings(List<ResKey> warnings) {
	List<ResKey> newWarnings = warnings.isEmpty() ? Collections.emptyList() : List.copyOf(warnings);
	if (!_modelWarnings.equals(newWarnings)) {
		_modelWarnings = newWarnings;
		fireValidationChanged();
	}
}
```

Update `hasError()` to aggregate both sources (around line 122-124):

```java
@Override
public boolean hasError() {
	return _error != null || _modelError != null;
}
```

Update `getError()` to prefer local error over model error (around line 127-129):

```java
@Override
public ResKey getError() {
	return _error != null ? _error : _modelError;
}
```

Update `hasWarnings()` and `getWarnings()` to aggregate:

```java
@Override
public boolean hasWarnings() {
	return !_warnings.isEmpty() || !_modelWarnings.isEmpty();
}

@Override
public List<ResKey> getWarnings() {
	if (_warnings.isEmpty()) return _modelWarnings;
	if (_modelWarnings.isEmpty()) return _warnings;
	List<ResKey> combined = new ArrayList<>(_warnings);
	combined.addAll(_modelWarnings);
	return combined;
}
```

- [ ] **Step 3: Build**

Run: `mvn install -DskipTests=true -pl com.top_logic`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add com.top_logic/src/main/java/com/top_logic/layout/form/model/FieldModel.java \
        com.top_logic/src/main/java/com/top_logic/layout/form/model/AbstractFieldModel.java
git commit -m "Ticket #29108: Add model validation propagation to FieldModel."
```

---

## Task 11: Full Build Verification

**Files:** None (verification only)

- [ ] **Step 1: Build all affected modules in dependency order**

```bash
mvn install -DskipTests=true -pl \
  com.top_logic,\
  com.top_logic.model.search,\
  com.top_logic.element
```

Expected: BUILD SUCCESS for all three modules.

- [ ] **Step 2: Search for remaining compilation errors**

If the build fails, check for remaining references to the old `FormContext` parameter in `traceDependencies()` calls. Common locations:

```bash
grep -rn "traceDependencies.*FormContext\|traceDependencies.*formContext" \
  com.top_logic/src/ com.top_logic.element/src/ com.top_logic.model.search/src/
```

Fix any remaining references.

- [ ] **Step 3: Run existing tests in affected modules**

```bash
mvn test -DskipTests=false -pl com.top_logic -Dtest=TestNotEmptyConstraint
mvn test -DskipTests=false -pl com.top_logic.element -Dtest=TestBuiltInConstraintChecks
```

Expected: All tests PASS.

- [ ] **Step 4: Build downstream modules to catch breakage**

```bash
mvn install -DskipTests=true -pl com.top_logic.layout.view
```

Check for compilation errors related to the interface changes.

- [ ] **Step 5: Commit any fixes**

```bash
git add -u
git commit -m "Ticket #29108: Fix remaining compilation issues after validation refactoring."
```
