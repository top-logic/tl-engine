# Access parent determined by a configurable function (TL-Script)

Date: 2026-10-05
Ticket: none yet (work is committed as `Ticket #0` until a ticket number is assigned)
Builds on: #29637 (access parent, coverage view), released with `TL_8.0.0-alpha9`

## Goal

The access parent of a type - the object whose access definition decides access to the objects of
the type - can be determined by an arbitrary TL-Script function, in addition to the fixed relations
available today (container, composition navigated backwards, to-one reference navigated forwards).

Cases this must cover:

- Navigation over several steps, e.g. "the project the milestone of the ticket belongs to".
- Navigation the fixed relations do not offer: backwards over a non-composition reference, over a
  multi-valued or a derived attribute.

If the script navigates a multi-valued reference, the script author is responsible for it yielding
at most one object.

## Decisions

| Topic | Decision |
|---|---|
| Configuration shape | One polymorphic property `access-parent` replaces `access-parent` (enum) and `access-reference`. |
| XML form | Wrapper item with a `@DefaultContainer` holding one tag-named implementation. |
| Script yields one object | That object is the access parent. |
| Script yields `null` / empty collection | No access parent, i.e. no access. Not logged. |
| Script yields several objects / a non-object / throws | No access parent, i.e. no access. Logged as error. |
| Script evaluation | In a system context, without access checks. |
| Coverage analysis | No declared or inferred result type; a script type is "delegated" like any other delegating type. |
| Type-level check with unknown parent types | The type counts as accessible (pre-filter only; per-object check still applies). |
| Migration from alpha9 attribute form | Breaking; no compatibility layer; documented `== Migration ==` section, keyword `RequiresMigration`. |

## Section 1: Core API (`com.top_logic`, package `com.top_logic.model.security`)

### `AccessParentFunction`

Replaces the record `AccessParent` as the runtime value of the access parent relation:

```java
public interface AccessParentFunction {

	/** The object deciding access to the given one, null for none (= no access). */
	TLObject resolve(TLObject object);

	/** The types an access parent of an object of the given type may have, null if unknown. */
	Set<TLClass> getParentTypes(TLClass type);

	/** Short text describing the relation in the coverage display. */
	ResKey getLabel();
}
```

### Configuration

- `TLClassAccessRights.getAccessParent()` (`access-parent`) becomes an optional configuration item
  `AccessParentConfig`. Not set means "automatic" (inherit from generalizations, else the default for
  composition parts).
- `AccessParentConfig` has exactly one property: a `@DefaultContainer` of type
  `PolymorphicConfiguration<? extends AccessParentDefinition>`.
- `AccessParentDefinition` is the configured side: `AccessParentFunction resolve(InstantiationContext,
  TLClass type)` validates the setting against the type it is configured for and answers the runtime
  function (`null` for `self` and for an unusable setting, which is reported to the context). The
  split is needed because the fixed relations resolve their reference against the type of the
  enclosing `<class>` entry, which the configured instance does not know on its own.
- The runtime functions of the fixed relations are `ContainerRelation` (composition or `null` for
  any, plus whether it is configured or the default) and `TargetRelation` (the to-one reference).
- The grants stay the `@DefaultContainer` of `TLClassAccessRights` (inherited from
  `AccessRightsConfig`), which is why the access parent needs the wrapper element.
- The enum `AccessParentKind` and the property `access-reference` are removed.

XML:

```xml
<class name="tl.demo.projectManagement:Milestone">
	<access-parent>
		<container reference="tl.demo.projectManagement:ProjectScope#milestones"/>
	</access-parent>
</class>

<class name="demo.tickets:Reminder">
	<access-parent>
		<target reference="demo.tickets:Reminder#ticket"/>
	</access-parent>
</class>

<class name="demo:Part">
	<access-parent>
		<self/>
	</access-parent>
</class>
```

### Implementations in the core (each with `@TagName`)

- `container` with optional `reference`: the container holding the object. Without a reference
  whichever composition holds the object; with a reference only that composition (navigated
  backwards). The default for composition parts is an internal instance of this implementation
  with its own label ("Container (default)").
- `target` with mandatory `reference`: the object a to-one reference of the type points to
  (navigated forwards).
- `self`: no delegation; switches off the default for composition parts and an access parent
  inherited from a generalization. Internally it results in "no access parent"
  (`getAccessParent(type)` answers `null`).

The checks that `SecurityConfigurationService.resolveAccessParent` performs today (the composition
holds objects of the type, the reference is a to-one reference of the type, the reference exists)
move into the implementations and report through the `InstantiationContext`. The type the check
applies to is taken from the enclosing `<class name>`.

The editor support moves with them: `AccessReferenceOptions` (choice of fitting references, labelled
by `TLPartInOwnerResourceProvider`) and `AccessReferenceRequired` attach to the `reference`
property of the respective implementation. `AccessParentStandsAlone` stays: an entry with a
delegating access parent (anything but `self`) must have neither grants nor marks.

### Type-level check

`getAccessibleTypes` / `isAccessibleType` follow the access parent to its possible types
(`getAccessParentTypes`, now based on `AccessParentFunction.getParentTypes`). When the parent types
are unknown (`null`, the script case), the type counts as accessible. The type-level check is a
pre-filter only; the per-object check runs through the function as always. Consequence: a command
without an object may be shown although the user may not see a single object of the type.

### Unchanged

`ModelAccessRights.getAccessParent(TLClass)` (return type now `AccessParentFunction`), inheritance by
specializations (ended by `self`), the default for composition parts, the `AccessDecisionCache`
(per interaction and revision, cycle detection), create and delete mapped to write on the parent.

## Section 2: Script implementation (`com.top_logic.model.search`)

- Class `ScriptAccessParent` in `com.top_logic.model.search.rules`, next to `PathByExpression`.
- Config interface with `@TagName("script")` and mandatory property `expr` of type `Expr`, in line
  with `<script-step expr="..."/>`:

  ```xml
  <access-parent>
  	<script expr="t -> $t.get(`demo:Ticket#milestone`).container()"/>
  </access-parent>
  ```

  The script is a function of one argument, the object whose access parent is determined.
- Compiled once in the constructor (`QueryExecutor.compile`); compile errors are reported at startup
  through the `InstantiationContext`.
- The compiled script runs with security disabled (`QueryExecutor.disableSecurity()`, as
  `DeleteConstraintByExpression` does), so that navigation in the script neither recurses into the
  access check nor sees only what the user may see. `ScriptAccessParent` is both the definition
  (its `resolve(context, type)` answers itself) and the function. Result handling of
  `resolve(object)`:
  - a `TLObject`: the access parent;
  - a collection holding exactly one `TLObject`: that object;
  - `null` or an empty collection: no access parent, not logged;
  - anything else (several objects, a non-object): no access parent, `Logger.error` with type,
    object and result;
  - an exception thrown by the script: no access parent, logged as error; the access check is not
    aborted.

  The `AccessDecisionCache` limits evaluation and logging to once per object and interaction.
- `getParentTypes(type)` answers `null` (unknown).
- `getLabel()` answers the script text.
- No registration needed: the implementation is found through the type hierarchy of the polymorphic
  property and offered by the configuration editor wherever `com.top_logic.model.search` is on the
  class path.
- No dependency analysis (unlike `PathByExpression`): access decisions are not cached across
  revisions, so the script may use derived attributes and arbitrary navigation.

## Section 3: Coverage and dialog

### Analysis (`com.top_logic.element`, `...boundsec.manager.coverage`)

- `TypeCoverage.accessParent` becomes an `AccessParentFunction`. Status and findings are unchanged:
  a type with any access parent is `DELEGATED`, shadowed role rules are reported.
- `SecurityCoverageAnalysis` takes the value from `getAccessParent(type)` unchanged.

### Table and detail (`SecurityCoverageTable`, `com.top_logic.layout.view`)

- The column "Access parent" and the "delegates to ..." paragraph of the findings use
  `AccessParentFunction.getLabel()`. The case analysis over record fields (`isContainer()`,
  `explicit`) is removed.
- Labels per implementation: "Container (default)", "Container", "Container via <composition>",
  "<reference>" for `target`, the script text for `script` (truncated in the cell, full text in the
  tooltip).
- The I18N constants `COVERAGE_ACCESS_PARENT_CONTAINER` / `_DEFAULT` move from `layout.view` to the
  `I18NConstants` of `com.top_logic.model.security`; the script label lives in `model.search`.

### Dialog "Access rights..." (`coverage-access-rights.view.xml`)

The form shows the group "Access parent" instead of "Kind" and "Access reference": choosing an
implementation (Container, Reference target, Own decision, Script) shows its fields only - for the
script the TL-Script editor of an `Expr` property. The React configuration editor already supports
a single-valued polymorphic property (`ConfigChildren` treats an `ITEM` as a list of at most one
element and offers the implementations). The view file itself does not change.

### Editing the definition (`SecurityDefinitionEditor`, `SecurityDefinitionAction`)

- `setAccessParent(TLClass, AccessParentKind, TLModelPartRef)` becomes
  `setAccessParent(TLClass, PolymorphicConfiguration<? extends AccessParentFunction>)`, `null` for
  automatic.
- Dropping the delegation (when marking a type internal or without access control) tests "a
  delegating access parent is set" (anything but `self`) instead of `AccessParentKind.delegates()`.
- The autoconf files written by the dialog use the new form.

## Section 4: Migration, documentation, tests

### Migration (breaking against `TL_8.0.0-alpha9`)

The attribute form is removed without a compatibility layer; `access-parent="..."` or
`access-reference="..."` on `<class>` is a configuration error at startup with a clear message.

| old | new |
|---|---|
| `access-parent="container"` | `<access-parent><container/></access-parent>` |
| `access-parent="container" access-reference="X"` | `<access-parent><container reference="X"/></access-parent>` |
| `access-parent="target" access-reference="X"` | `<access-parent><target reference="X"/></access-parent>` |
| `access-parent="self"` | `<access-parent><self/></access-parent>` |
| `access-parent="auto"` | remove the attribute |

- Adapted in this repository: `com.top_logic.demo.react/.../demoReactConf.config.xml` (2 entries),
  `com.top_logic.element/src/test/webapp/WEB-INF/conf/element.test.config.xml` (7 entries).
- The ticket description gets a `== Migration ==` section with this table and a hint at
  `WEB-INF/autoconf/...SecurityConfigurationService.config.xml` files written by the dialog, plus
  the keyword `RequiresMigration`. No automatic migration script.

### Documentation

- `docs/faq/access-configuration.md`: the access parent section and its syntax table in the new
  form; a new section on the script (signature, uniqueness as the author's duty, behaviour for empty,
  several and failing results, evaluation without access checks, type-level check with unknown
  parent types).
- JavaDoc of the config interfaces provides the editor labels and tooltips; `messages_en` is
  generated by the build, `messages_de` is reviewed by hand.

### Tests

- Adapt: `TestAccessParentCheck`, `TestSecurityCoverageAnalysis`, `TestSecurityCoverageCheck`,
  `TestSecurityDefinitionEditor` (`com.top_logic.element`); `TestConfigControlService` /
  `TestConfigEditorControl` (`com.top_logic.layout.configedit`) where they use `AccessParentKind`.
- New, core configuration: each implementation parses in its valid form; error cases: a composition
  not holding the type, a multi-valued reference for `target`, `target` without reference, grants
  together with a delegating access parent, the old attribute form.
- New, `TestScriptAccessParent` (`com.top_logic.model.search`, small test model): single object;
  collection of one; empty (no access, no log); several objects, non-object, exception (no access,
  error logged); chain over several steps; the script reads an object the user may not read
  (system context); type-level check counts the type as accessible.
- Manual verification in `com.top_logic.demo.react`: set a script for a demo type in "Access
  rights...", apply, check status and column in the coverage table and the access of a non-admin
  user to the objects.
