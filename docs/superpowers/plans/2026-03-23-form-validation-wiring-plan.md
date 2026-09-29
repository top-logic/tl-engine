# Form Validation Wiring & Demo Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Wire FormValidationModel into the view layer so that model-derived constraints are checked server-side on every value change, errors/warnings display in the UI, and invalid forms block saving. Provide a demo model `test.constraints` exercising all constraint types.

**Architecture:** `FormValidationModel` accepts plain `TLObject` overlays and stores validation results internally (not on overlays). `FormControl` creates and manages the `FormValidationModel` lifecycle. `AttributeFieldControl` bridges value changes to the validation model and queries validation results to update the chrome. A new `test.constraints` model in the demo app provides types with mandatory, size, range, and expression-based constraints for end-to-end testing. No changes to `TLObjectOverlay` or `TLFormObjectBase` needed.

**Tech Stack:** Java 17, TopLogic framework, Maven, ISO-8859-1

**Spec:** `docs/superpowers/specs/2026-03-18-model-based-form-validation-design.md`
**Previous plan:** `docs/superpowers/plans/2026-03-19-model-based-form-validation-plan.md`

---

## File Structure

### New Files

| File | Module | Responsibility |
|------|--------|----------------|
| `test.constraints.model.xml` | com.top_logic.demo | Test model with all constraint types |
| `constraintTest.layout.xml` | com.top_logic.demo | Layout tab for the constraint test form |

### Modified Files

| File | Module | Change |
|------|--------|--------|
| `FormControl.java` | layout.view | Create/manage `FormValidationModel`, block save on invalid |
| `AttributeFieldControl.java` | layout.view | Call `onValueChanged()` on value change, query validation results for chrome |
| `pom.xml` (layout.view) | layout.view | Add `tl-element` dependency |

Note: `FormValidationModel` was already refactored to accept plain `TLObject` overlays (not `TLFormObject`). `TLObjectOverlay` does NOT need to implement `TLFormObjectBase`.

---

## Task 1: Add tl-element Dependency to layout.view

**Files:**
- Modify: `com.top_logic.layout.view/pom.xml`

- [ ] **Step 1: Read pom.xml**

Read `com.top_logic.layout.view/pom.xml`.

- [ ] **Step 2: Add tl-element dependency**

Add after the existing dependencies:

```xml
<dependency>
    <groupId>com.top-logic</groupId>
    <artifactId>tl-element</artifactId>
</dependency>
```

No version needed — managed by parent POM.

- [ ] **Step 3: Build to verify**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.view`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```
Ticket #29108: Add tl-element dependency to layout.view for validation integration.
```

---

## Task 2: Wire FormValidationModel into FormControl

FormControl manages the form lifecycle (enter/exit edit, save). It creates the overlay. Now it must also create and manage the FormValidationModel.

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/FormControl.java`

- [ ] **Step 1: Read FormControl**

Read `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/FormControl.java`.

- [ ] **Step 2: Add FormValidationModel field and accessor**

Add field after `_overlay`:
```java
private FormValidationModel _validationModel;
```

Add accessor:
```java
/**
 * The validation model for the current form, or {@code null} if not in edit mode.
 */
public FormValidationModel getValidationModel() {
    return _validationModel;
}
```

Add import: `import com.top_logic.element.meta.form.validation.FormValidationModel;`

- [ ] **Step 3: Create and wire in enterEditMode()**

After `_overlay = new TLObjectOverlay(_currentObject);` (line 186), add:
```java
_validationModel = new FormValidationModel();
_validationModel.addOverlay(_overlay, _currentObject);
```

- [ ] **Step 4: Add state key for form validity**

Add constant:
```java
/** State key for form validity (blocks save when false). */
private static final String VALID = "valid";
```

After creating the validation model in enterEditMode(), add a listener:
```java
_validationModel.addConstraintValidationListener((overlay, attribute, result) -> {
    putState(VALID, Boolean.valueOf(_validationModel.isValid()));
});
putState(VALID, Boolean.valueOf(_validationModel.isValid()));
```

- [ ] **Step 5: Clean up in exitEditMode()**

In `exitEditMode()`, the cleanup order matters. `fireFormStateChanged()` triggers `AttributeFieldControl.onFormStateChanged()` which calls `clearModel()` — that needs the overlay reference to remove validation listeners. So validation cleanup must happen AFTER the state change fires.

Add AFTER `fireFormStateChanged()` (at the very end of `exitEditMode()`):
```java
_validationModel = null;
putState(VALID, Boolean.TRUE);
```

Note: `AttributeFieldControl.clearModel()` uses its own `_validationOverlay` reference for listener cleanup, so it doesn't depend on `FormControl._overlay` still being non-null. But `_validationModel` should still be available during `onFormStateChanged()` so fields can read final state.

- [ ] **Step 6: Block save when invalid**

In `executeSave()`, add validation check before the save:

```java
if (_validationModel != null && !_validationModel.isValid()) {
    return; // Block save when validation errors exist.
}
```

Same in `executeApply()`.

- [ ] **Step 7: Build**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.view`
Expected: BUILD SUCCESS

- [ ] **Step 8: Commit**

```
Ticket #29108: Wire FormValidationModel into FormControl lifecycle.
```

---

## Task 3: Bridge Value Changes and Validation Results in AttributeFieldControl

AttributeFieldControl must:
1. Call `FormValidationModel.onValueChanged()` when a value changes
2. Bridge validation results from the overlay to the FieldModel

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/AttributeFieldControl.java`

- [ ] **Step 1: Read AttributeFieldControl**

Read `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/AttributeFieldControl.java`.

- [ ] **Step 2: Add onValueChanged call to the FieldModelListener**

In `addModelListener()`, inside `onValueChanged()` (after `_formControl.updateDirtyState()`), add:

```java
// Trigger constraint validation.
FormValidationModel validationModel = _formControl.getValidationModel();
if (validationModel != null && _model != null) {
    validationModel.onValueChanged(_formControl.getOverlay(), _model.getPart());
}
```

Add import: `import com.top_logic.element.meta.form.validation.FormValidationModel;`

- [ ] **Step 3: Register as ConstraintValidationListener on overlay**

When entering edit mode, register a listener on the `FormValidationModel` (not the overlay) to bridge validation results to the FieldModel.

In `addModelListener()`, after creating the `_modelListener` and `_model.addListener(_modelListener)`, add:

```java
// Bridge FormValidationModel validation to FieldModel.
FormValidationModel validationModel = _formControl.getValidationModel();
if (validationModel != null && _model != null) {
    TLStructuredTypePart part = _model.getPart();
    TLObject overlay = _formModel.getCurrentObject();

    // Read initial validation state from FormValidationModel.
    ValidationResult initial = validationModel.getValidation(overlay, part);
    applyValidationResult(initial);

    // Listen for future changes.
    _validationListener = (o, attr, result) -> {
        if (o == overlay && attr == part) {
            applyValidationResult(result);
        }
    };
    validationModel.addConstraintValidationListener(_validationListener);
}
```

Add a helper method:
```java
private void applyValidationResult(ValidationResult result) {
    if (result.isValid()) {
        _model.setModelValidationError(null);
    } else {
        _model.setModelValidationError(result.getErrors().get(0));
    }
    _model.setModelValidationWarnings(result.getWarnings());
}
```

Add field:
```java
private ConstraintValidationListener _validationListener;
```

Add imports:
```java
import com.top_logic.element.meta.form.validation.FormValidationModel;
import com.top_logic.model.form.ConstraintValidationListener;
import com.top_logic.model.form.ValidationResult;
```

- [ ] **Step 4: Clean up listener in clearModel()**

In `clearModel()`, before clearing `_model`, also remove the validation listener from the FormValidationModel:

```java
if (_validationListener != null) {
    FormValidationModel validationModel = _formControl.getValidationModel();
    if (validationModel != null) {
        validationModel.removeConstraintValidationListener(_validationListener);
    }
    _validationListener = null;
}
```

Note: We need a `removeConstraintValidationListener()` method on `FormValidationModel`. If it doesn't exist yet, add it (simple `_listeners.remove(listener)`).

- [ ] **Step 5: Build**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.view`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```
Ticket #29108: Bridge value changes and validation results in AttributeFieldControl.
```

---

## Task 4: Test Model and Layout for Demo App

Create a model `test.constraints` with a type that exercises all constraint types, and a layout to view/edit it.

**Files:**
- Create: `com.top_logic.demo/src/main/webapp/WEB-INF/model/test.constraints.model.xml`
- Modify: `com.top_logic.demo/src/main/webapp/WEB-INF/layouts/com.top_logic.demo/technical/reactDemo/index.layout.xml` (add tab)
- Create: `com.top_logic.demo/src/main/webapp/WEB-INF/layouts/com.top_logic.demo/technical/reactDemo/constraintTest.layout.xml`

- [ ] **Step 1: Create test.constraints.model.xml**

```xml
<?xml version="1.0" encoding="utf-8" ?>

<model xmlns="http://www.top-logic.com/ns/dynamic-types/6.0">
	<module name="test.constraints">
		<annotations>
			<display-group value="demo"/>
		</annotations>

		<class name="ConstraintTestType">
			<generalizations>
				<generalization type="tl.model:TLObject"/>
			</generalizations>
			<annotations>
				<table name="GenericObject"/>
				<instance-presentation>
					<icon value="css:bi bi-check-circle"/>
				</instance-presentation>
			</annotations>
			<attributes>
				<!-- Mandatory string: must not be empty -->
				<property name="mandatoryString"
					mandatory="true"
					type="tl.core:String"
				/>

				<!-- String with size constraint: 3-50 characters -->
				<property name="sizedString"
					type="tl.core:String"
				>
					<annotations>
						<size-constraint
							lower-bound="3"
							upper-bound="50"
						/>
					</annotations>
				</property>

				<!-- Mandatory string with size constraint -->
				<property name="mandatorySizedString"
					mandatory="true"
					type="tl.core:String"
				>
					<annotations>
						<size-constraint
							lower-bound="5"
							upper-bound="100"
						/>
					</annotations>
				</property>

				<!-- Integer with range constraint: 0-100 -->
				<property name="rangedInt"
					type="tl.core:Integer"
				>
					<annotations>
						<value-range
							min="0.0"
							max="100.0"
						/>
					</annotations>
				</property>

				<!-- Double with range constraint: -10.0 to 10.0 -->
				<property name="rangedDouble"
					type="tl.core:Double"
				>
					<annotations>
						<value-range
							min="-10.0"
							max="10.0"
						/>
					</annotations>
				</property>

				<!-- Expression-based constraint: must start with uppercase letter -->
				<property name="uppercaseStart"
					type="tl.core:String"
				>
					<annotations>
						<constraints>
							<constraint-by-expression>
								<check><![CDATA[value -> obj -> {
	if ($value != null and $value.length() > 0,
		if ($value.subString(0, 1) == $value.subString(0, 1).toUpperCase(),
			null,
			#("Must start with an uppercase letter."@en, "Muss mit einem Grossbuchstaben beginnen."@de)),
		null)
}]]></check>
							</constraint-by-expression>
						</constraints>
					</annotations>
				</property>

				<!-- Warning constraint (non-blocking) -->
				<property name="warningField"
					type="tl.core:String"
				>
					<annotations>
						<constraints>
							<constraint-by-expression type="warning">
								<check><![CDATA[value -> obj -> {
	if ($value != null and $value.length() > 20,
		#("Consider keeping text under 20 characters."@en, "Text sollte unter 20 Zeichen bleiben."@de),
		null)
}]]></check>
							</constraint-by-expression>
						</constraints>
					</annotations>
				</property>

				<!-- Optional plain string (no constraints, for comparison) -->
				<property name="freeText"
					type="tl.core:String"
				/>
			</attributes>
		</class>
	</module>
</model>
```

- [ ] **Step 2: Read the existing reactDemo index.layout.xml**

Read `com.top_logic.demo/src/main/webapp/WEB-INF/layouts/com.top_logic.demo/technical/reactDemo/index.layout.xml` to understand how tabs are structured.

- [ ] **Step 3: Create constraintTest.layout.xml**

This layout should use the `form` and `field` elements from the view framework to create a form editing a `test.constraints:ConstraintTestType` object. Read how other layouts in the demo app use `FormElement` and `FieldElement` patterns.

Note: The exact layout structure depends on how the view framework's declarative layout works. The implementor should look at existing examples in the `reactDemo/` directory and follow the same pattern. The layout needs:
- A selection mechanism to pick a `ConstraintTestType` object (or auto-create one)
- A form with fields for each attribute
- Edit/save/cancel buttons (provided by `FormElement` with `withEditMode="true"`)

- [ ] **Step 4: Add the tab to the reactDemo index**

Add a tab entry in the index layout referencing `constraintTest.layout.xml`.

- [ ] **Step 5: Build demo app (incremental, NO clean)**

Run: `mvn install -DskipTests=true -pl com.top_logic.demo`
Expected: BUILD SUCCESS

Note: NEVER use `mvn clean` on com.top_logic.demo — it causes PluginContainerException.

- [ ] **Step 6: Commit**

```
Ticket #29108: Add test.constraints model and layout for validation demo.
```

---

## Task 5: Full Build and Manual Testing

- [ ] **Step 1: Build all affected modules**

```bash
mvn install -DskipTests=true -pl com.top_logic.element,com.top_logic.layout.view,com.top_logic.demo
```

- [ ] **Step 2: Run automated tests**

```bash
mvn test -DskipTests=false -pl com.top_logic.element -Dtest=TestFormValidationModel,TestBuiltInConstraintChecks
```

- [ ] **Step 3: Start the demo app**

Use @tl-app skill to start the demo app. Login with root/root1234.

- [ ] **Step 4: Navigate to the constraint test tab**

Navigate to Technical > React Demo > Constraint Test (or wherever the tab was added).

- [ ] **Step 5: Test each constraint type**

Manual test checklist:
- Create or select a ConstraintTestType object
- Enter edit mode
- **Mandatory**: Clear `mandatoryString` → expect error message
- **Size**: Enter "ab" in `sizedString` → expect "too short" error; enter 51+ chars → expect "too long"
- **Range**: Enter -1 in `rangedInt` → expect below-minimum error; enter 101 → expect above-maximum
- **Expression**: Enter "lowercase" in `uppercaseStart` → expect error; enter "Uppercase" → no error
- **Warning**: Enter 21+ chars in `warningField` → expect warning (yellow, not blocking save)
- **Save blocked**: With errors present, try save → should not save
- **Save allowed**: Fix all errors, save → should succeed

- [ ] **Step 6: Commit any fixes**

```
Ticket #29108: Fix issues found during manual validation testing.
```
