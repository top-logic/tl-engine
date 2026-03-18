# Design: Model-Based Form Validation for React Controls

**Ticket**: #29108
**Date**: 2026-03-18
**Status**: Draft

## Problem

The legacy form system validates user input through `FormField.addConstraint(Constraint)`, where constraints are derived from model annotations (`@TLSize`, `@TLRange`, `@TLConstraints`, `isMandatory`) by `DefaultAttributeFormFactory`. This system is tightly coupled to the `FormField` class hierarchy and `FormContext`.

The new React-based form system uses `FieldModel` and `TLFormObject` overlays instead of `FormField` and `FormContext`. A new validation mechanism is needed that:

- Runs entirely server-side
- Supports cross-attribute and cross-object constraints
- Uses dynamic dependency tracking (dependencies change based on current values)
- Reuses the existing `ConstraintCheck` interface and `TracingAccess` infrastructure
- Is transparent to users — model annotations continue to work as before

## Architecture

### Core Components

**FormValidationModel** — Central validation coordinator per form. Manages all overlay objects and their constraints. Created by the view component that renders the form. (Named `FormValidationModel` to avoid collision with the existing `FormModel` interface in `com.top_logic.layout.view.form`.)

**ConstraintEntry** — Internal bookkeeping per constraint instance:
- Reference to the `ConstraintCheck` implementation
- Owning `(TLObject, TLStructuredTypePart)` pair (the attribute the constraint belongs to)
- Constraint type (ERROR or WARNING)
- Current set of dependencies (recomputed on every check)

**Built-in ConstraintCheck adapters** — Convert model annotations to `ConstraintCheck` internally:
- `MandatoryConstraintCheck` — checks non-empty value, derived from `isMandatory()`
- `SizeConstraintCheck` — checks string length, derived from `@TLSize`
- `RangeConstraintCheck` — checks numeric range, derived from `@TLRange`

These are created automatically when an overlay is registered. Users only interact with model annotations as before.

### Data Flow on Value Change

```
1. User changes value in React control
2. Control calls FieldModel.setValue(newValue)
3. FieldModel writes to overlay: overlay.setValue(attribute, newValue)
4. Overlay fires ValueChanged event
5. FormValidationModel receives event, consults dependency map:
   "Which ConstraintEntries have (overlay, attribute) as a dependency?"
6. For each affected ConstraintEntry:
   a. Execute ConstraintCheck.check(overlay, attribute)
      — the overlay IS a TLObject, so check() reads edited values
        directly via overlay.tValue()
      — TracingAccess records all (TLObject, TLStructuredTypePart) pairs
        read during evaluation
   b. Extract new dependencies from the trace
   c. Update dependency map (remove old dependencies, add new ones)
   d. Store validation result on the overlay that owns the constraint
7. Overlay fires ValidationChanged event
8. View component (which wired the listeners during setup) propagates
   the validation result to the affected FieldModel
9. FieldModel updates hasError/hasWarnings, notifies its listeners
10. Control displays error/warning state
```

**Important**: `ConstraintCheck.check(object, attribute)` receives the overlay as its `object` parameter. Since overlays implement `TLObject`, the check reads edited form values transparently via `object.tValue(attribute)`. No special value redirection is needed.

### Initial Validation

When an overlay is registered via `formModel.addOverlay(overlay)`:

1. FormValidationModel iterates all `TLStructuredTypePart`s of the overlay's type
2. For each attribute: checks `isMandatory()`, `@TLSize`, `@TLRange`, `@TLConstraints`
3. Creates corresponding `ConstraintEntry` instances
4. Registers as ValueChanged listener on the overlay
5. Runs all constraints once to build the initial dependency map and set initial errors

### Cascading Re-Validation

When a constraint check updates the dependency map, new dependencies may link to attributes that were not previously tracked. The FormValidationModel uses a fixed-point loop:

1. Collect all `(TLObject, TLStructuredTypePart)` pairs whose validation results changed in this round
2. Look up which ConstraintEntries have any of these pairs as dependencies
3. If there are un-evaluated entries, run another round
4. Repeat until no new constraints are triggered (stable state)

**Termination**: Each round can only trigger constraints that were not already evaluated in the current change propagation. Since the total number of ConstraintEntries is finite and each is evaluated at most once per propagation, the loop terminates. Cyclic dependencies are safe — they simply mean both constraints are re-evaluated, and the loop stabilizes after one round.

### Save/Commit Gate

When the user triggers a save action, the view component checks `formModel.isValid()` before committing. This method returns `true` only if no overlay has ERROR-type validation results. WARNING-type results do not block saving.

## Integration with Existing Code

### TracingAccess — Generalized for OverlayLookup

`TracingAccess.lookupValue()` currently casts `UPDATE_CONTAINER` from the `EvalContext` to `AttributeUpdateContainer`. This must be generalized:

- `TracingAccess.lookupValue()` reads `UPDATE_CONTAINER` as `OverlayLookup` (instead of `AttributeUpdateContainer`)
- It calls `overlayLookup.getExistingOverlay(self)` to find the overlay, then reads the value from it

`ScriptTracer.execute()` currently takes `AttributeUpdateContainer` as parameter and stores it as `UPDATE_CONTAINER` in the `EvalContext`. This must be changed to accept `OverlayLookup`.

The full call chain that must be updated:

1. `ConstraintCheckByExpression.traceDependencies()` — currently casts `FormContext` to `AttributeFormContext` to obtain `AttributeUpdateContainer`, then passes it to `ScriptTracer.execute()`. Must be changed to accept `OverlayLookup` and pass it directly.
2. `ScriptTracer.execute()` — parameter type changes from `AttributeUpdateContainer` to `OverlayLookup`
3. `TracingAccess.lookupValue()` — casts `UPDATE_CONTAINER` to `OverlayLookup` instead of `AttributeUpdateContainer`

`AttributeUpdateContainer` already has `getExistingOverlay(TLObject)` and can implement `OverlayLookup`, so the legacy path continues to work.

### ConstraintCheck Interface — Signature Change

```java
// Old:
void traceDependencies(TLObject object, TLStructuredTypePart attribute,
                       Sink<Pointer> trace, FormContext formContext);

// New:
void traceDependencies(TLObject object, TLStructuredTypePart attribute,
                       Sink<Pointer> trace, OverlayLookup overlays);
```

**Affected implementors** (all must update their signature):
- `ConstraintCheckByExpression` — the most common implementation; uses the parameter to pass to `ScriptTracer`
- `NoAttributeCycle` — implements `ConstraintCheck`; does not use the `FormContext`/`OverlayLookup` parameter directly (cross-object navigation is handled transparently by `TracingAccess`), but must update signature
- Any custom `ConstraintCheck` implementations in application code

**Migration**: This is a breaking change to a public interface. Since `ConstraintCheck` is an SPI (implemented by application code), all custom implementations must update their `traceDependencies` signature. The old `FormContext` parameter is replaced by `OverlayLookup`. Implementations that ignored `FormContext` only need a signature update. Implementations that used `FormContext` (e.g., to look up form fields) must be reworked to use `OverlayLookup` instead.

### ModeSelector — Same Signature Change

`ModeSelector.traceDependencies()` has the same `FormContext` parameter and must change to `OverlayLookup` for consistency. This is **in scope** for this change because `ModeSelector` is used alongside `ConstraintCheck` in the same form infrastructure. Affected implementors:
- `ModeSelectorByExpression` — analogous to `ConstraintCheckByExpression`

### Relationship to FieldModel's Existing Constraint System

The existing `FieldModel` interface already has `addConstraint(FieldConstraint)` and `validate()`. In the new system:

- **FormValidationModel-level validation replaces FieldModel-level constraints for model-derived checks.** The built-in adapters (`MandatoryConstraintCheck`, `SizeConstraintCheck`, `RangeConstraintCheck`) and `@TLConstraints` all run at the FormValidationModel level.
- **FieldModel.addConstraint() remains available** for control-specific validation that is not model-derived (e.g., input format validation that depends on the control type). These run locally in the FieldModel and are orthogonal to FormValidationModel validation.
- **Error aggregation**: A FieldModel's `hasError()` is true if either its local constraints or the FormValidationModel-propagated validation has errors. The FieldModel combines both sources.
- **New FieldModel setters needed**: `setValidationError(ResKey)` and `setValidationWarnings(List<ResKey>)` must be added to `FieldModel` (or its implementation). These are called by the view's bridging listener to propagate FormValidationModel results.

## Lifecycle

### Setup

1. View component creates `FormValidationModel`
2. For each edited object: view creates overlay and registers it via `formModel.addOverlay(overlay)`
3. FormValidationModel derives constraints from model type, registers listeners, runs initial validation
4. View creates FieldModels pointing to their overlays
5. **View wires validation bridging**: For each FieldModel, the view registers a `ConstraintValidationListener` on the overlay that propagates results to the FieldModel (via `fieldModel.setValidationError()` / `fieldModel.setValidationWarnings()`). The FieldModel itself does not know about overlays — the view component owns this wiring.
6. FieldModels read initial validation state

**Order matters**: Overlays are registered at FormValidationModel first (constraints + dependency map must be established), then FieldModels are created and wired.

### Dynamic Objects

When the user creates a new child object in the form:
- View creates a new overlay (without persistent base object), registers it at FormValidationModel
- FormValidationModel derives constraints, validates initially, updates dependency map
- Existing constraints of other overlays that now discover the new object as a dependency are automatically linked on their next re-check

### Teardown

When an overlay is removed (e.g., child object deleted): `formModel.removeOverlay(overlay)`
- FormValidationModel removes all ConstraintEntries owned by this overlay
- Cleans up the dependency map (removes all entries pointing to this overlay)
- Triggers re-validation of affected constraints on other overlays

## API

### FormValidationModel

```java
/**
 * Central validation coordinator for a form.
 * Manages all overlay objects and their constraints.
 */
public class FormValidationModel implements OverlayLookup {

    /** Register overlay, derive constraints, validate initially. */
    void addOverlay(TLFormObject overlay);

    /** Remove overlay, clean up dependency map. */
    void removeOverlay(TLFormObject overlay);

    /** All registered overlays. */
    Collection<TLFormObject> getOverlays();

    /** True if no overlay has ERROR-type validation results. */
    boolean isValid();

    /** Listener for validation changes across all overlays. */
    void addConstraintValidationListener(ConstraintValidationListener listener);

    // OverlayLookup implementation
    @Override
    TLFormObject getExistingOverlay(TLObject object);
}
```

Note: `FormValidationModel` does not have `getError()`/`getWarnings()` accessors — validation results live exclusively on the overlays. `FormValidationModel` writes results there; consumers read them from there.

### OverlayLookup

```java
/**
 * Replaces FormContext for constraint and tracing purposes.
 * Both FormValidationModel and AttributeUpdateContainer can implement this.
 *
 * Uses TLFormObjectBase (in tl-core) instead of TLFormObject (in tl-element)
 * to avoid a dependency from tl-core to tl-element.
 */
public interface OverlayLookup {

    /** Find the overlay for a persistent object, or null.
     *  If the object is itself an overlay, returns it directly. */
    TLFormObjectBase getExistingOverlay(TLObject object);

    /** All overlays managed by this lookup (needed for cross-object
     *  constraints that must discover newly created objects). */
    Iterable<? extends TLFormObjectBase> getOverlays();
}
```

### ConstraintValidationListener

```java
/**
 * Listener for validation state changes.
 * Uses TLFormObjectBase to stay in tl-core.
 */
public interface ConstraintValidationListener {

    void onValidationChanged(TLFormObjectBase overlay,
                             TLStructuredTypePart attribute,
                             ValidationResult result);
}
```

### ValidationResult

```java
/**
 * Result of constraint evaluation for a single attribute.
 * Uses ResKey for internationalized error messages, consistent
 * with ConstraintCheck.check() return type.
 */
public class ValidationResult {

    /** Error messages (from ERROR-type constraints). */
    List<ResKey> getErrors();

    /** Warning messages (from WARNING-type constraints). */
    List<ResKey> getWarnings();

    /** True if getErrors() is empty. */
    boolean isValid();
}
```

### Changes to TLFormObject

```java
// Additional API for validation state:
ValidationResult getValidation(TLStructuredTypePart attribute);
void setValidation(TLStructuredTypePart attribute, ValidationResult result);
void addConstraintValidationListener(ConstraintValidationListener listener);
```

### Changes to ConstraintCheck

```java
// traceDependencies signature change:
// Old:
void traceDependencies(TLObject object, TLStructuredTypePart attribute,
                       Sink<Pointer> trace, FormContext formContext);
// New:
void traceDependencies(TLObject object, TLStructuredTypePart attribute,
                       Sink<Pointer> trace, OverlayLookup overlays);
```

### Changes to ScriptTracer

```java
// execute() parameter change:
// Old:
void execute(Sink<Pointer> trace, AttributeUpdateContainer container,
             Object value, TLObject self);
// New:
void execute(Sink<Pointer> trace, OverlayLookup overlays,
             Object value, TLObject self);
```

## Module Placement

| Type | Module | Rationale |
|------|--------|-----------|
| `OverlayLookup` | `com.top_logic` (tl-core) | Referenced by `ConstraintCheck.traceDependencies()` which lives in tl-core. Uses `TLFormObjectBase` (tl-core) as return type, not `TLFormObject` (tl-element). |
| `ValidationResult` | `com.top_logic` (tl-core) | Value type used by `OverlayLookup` consumers; no heavy dependencies. |
| `ConstraintValidationListener` | `com.top_logic` (tl-core) | Used alongside `ConstraintCheck`. |
| `FormValidationModel` | `com.top_logic.element` (tl-element) | References `TLFormObject` which lives in tl-element. |
| Built-in adapters (`MandatoryConstraintCheck`, etc.) | `com.top_logic.element` (tl-element) | Use `TLFormObject` and model annotations. |

`AttributeUpdateContainer` (in `com.top_logic.element`) already has `getExistingOverlay(TLObject)` and can implement `OverlayLookup`. `OverlayLookup.getOverlays()` returns `Iterable<? extends TLFormObjectBase>` (not `Collection`) to match `AttributeUpdateContainer.getAllOverlays()`. The `FormValidationModel` internally works with `TLFormObject` (the tl-element subtype) but exposes `TLFormObjectBase` through the `OverlayLookup` interface.

## Cross-Object Constraint Navigation

Constraints like `NoAttributeCycle` navigate from the root object to related objects via references (e.g., `object.tValue(referenceAttribute)` returns another `TLObject`). In the new system, these navigated objects are persistent `TLObject`s, not overlays.

**Solution**: The `TracingAccess.lookupValue()` mechanism handles this. When a constraint expression navigates to a related object, `TracingAccess` intercepts the attribute access and checks `OverlayLookup.getExistingOverlay(navigatedObject)`. If an overlay exists (because the related object is also being edited in the form), the overlay value is returned. If no overlay exists, the persistent value is used. This is exactly how the legacy system works via `AttributeUpdateContainer`.

The `check()` method itself does not need an `OverlayLookup` parameter. The overlay resolution happens transparently during expression evaluation through `TracingAccess`, which reads the `OverlayLookup` from the `EvalContext`. For built-in constraint adapters (mandatory, size, range) that do not use expression evaluation, cross-object navigation is not relevant — they only inspect the single attribute they are bound to.

## Constraint Aggregation

When multiple `ConstraintEntry` instances target the same `(overlay, attribute)` pair:

1. **All constraints run**, even if earlier ones fail. This gives the user complete feedback in one round.
2. Results are **merged**: all non-null `ResKey`s from ERROR-type constraints go into `ValidationResult.getErrors()`, all from WARNING-type constraints into `getWarnings()`.
3. **Exception**: `MandatoryConstraintCheck` runs first. If the value is empty/null, further constraints on the same attribute are **skipped** — checking length or range on an empty value produces confusing redundant messages.

## OverlayLookup Contract

`getExistingOverlay(TLObject object)`:
- If `object` is itself a `TLFormObjectBase` (overlay), returns it directly.
- If `object` is a persistent object and an overlay exists for it, returns the overlay.
- If no overlay exists, returns `null`. Callers (e.g., `TracingAccess`) fall back to reading from the persistent object directly.

`getOverlays()`:
- Returns all overlays, including those for newly created objects (which have no persistent base object).
- Used by cross-object constraints that need to discover all objects in the form.

## Threading

All validation runs on the session thread (the server thread handling the current user request). Value changes arrive one at a time per session. No additional synchronization is needed.

## Design Decisions

| Decision | Rationale |
|----------|-----------|
| Validation results on overlay, not FieldModel | Overlays are the shared data layer. Multiple FieldModels could observe the same overlay. The view wires overlay events to FieldModels. |
| FormValidationModel as coordinator, not owner of results | Overlays hold data and validation state. FormValidationModel orchestrates constraint evaluation and dependency tracking. |
| Dynamic dependency tracking | Dependencies change based on current values (conditional expressions). Static tracking would miss these changes. |
| Built-in constraints as ConstraintCheck adapters | Uniform internal API — all validation goes through ConstraintCheck. Users see no difference; model annotations work as before. |
| OverlayLookup replaces FormContext | Minimal interface that both legacy and new systems can implement. Includes `getOverlays()` for cross-object discovery of newly created objects. |
| ResKey for validation messages | Consistent with `ConstraintCheck.check()` return type. I18N resolution happens at the UI boundary (when sending to client). |
| FieldModel retains local constraints | Control-specific validation (format, parsing) remains on FieldModel. Model-derived validation runs at FormValidationModel level. Both sources are aggregated. |
| Server-side only | No client-side validation. All checks run on the server; results are pushed to controls via SSE state updates. |
| View owns the wiring | The view component bridges overlay validation events to FieldModels. FieldModels do not know about overlays; overlays do not know about FieldModels. |
