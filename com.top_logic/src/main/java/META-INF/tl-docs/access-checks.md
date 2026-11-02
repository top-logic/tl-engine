---
description: Read before checking access rights in Java or TL-Script code - the ModelAccessRights API (isAllowed, isAllowedCreate, isAllowedInitial, getAllowedRoles, getAccessibleTypes), the two conditions of creating an object, the objects exempt from checks (transient, not yet committed, deleted), the TL-Script functions canRead, canWrite, canDelete, canCreate, canReadAttribute, canWriteAttribute, canExecute and filterSecurity, and secured script evaluation in own code (QueryExecutor.disableSecurity, GenericMethodWithSecurity, WithSecurityCheck, UpdateSecurityVisitor).
order: 23
---

# Checking access in Java and TL-Script

How code asks the model based security whether a user may perform an operation, and how code
executing TL-Script decides whether the script runs on behalf of the user. What the grants and
roles are and where they are configured is described in [the access configuration
article](doc:access-configuration).

## The API: `ModelAccessRights`

`ModelAccessRights.getInstance()` is the single entry point (implemented by the
`SecurityConfigurationService`). Every access path - the UI, TL-Script, the AI tools, REST
endpoints, batch jobs - asks the same definition.

| Method | Answers |
|---|---|
| `isAllowed(person, object, operation)` | may the person perform the operation (built-in or declared by the application) on the object |
| `isAllowed(person, object, attribute, operation)` | may the person perform the operation (typically `Read` or `Write`) on the value of the attribute of the object |
| `isReadAllowed(...)`, `isDiscoverAllowed(...)` | short forms for `Read` and `Discover`; the variants without a person use `TLContext.currentUser()` |
| `isAllowedCreate(person, parent, composition)` and `isAllowedCreate(person, parent, composition, type)` | may the person create an object (of the target type of the composition, or of the given specialization of it) in the composition of the parent, see [Creating objects](#creating-objects) |
| `isAllowedCreate(person, type, context)` | may the person create an object of the type in the context; `null` context: on the security root |
| `isAllowedInitial(person, draft, attribute, operation)` | may the person fill in the attribute of an object that is still to be created (a transient draft, an object created in the current transaction) |
| `getAllowedRoles(type, operation)` | the roles granted the operation on the type, including those holding it through an implying operation; meaningless for a type `isWithoutSecurity` |
| `getAllowedRoles(attribute, operation)`, `hasGrant(attribute, operation)` | the attribute-level grant; without one the result is empty and the decision on the object applies |
| `getAccessibleTypes(person, operation)` | the types the person holds the operation on, on the security root; a person bypassing security gets all types, a restricted person none. The objects still need their own check. |

An answer of `getAllowedRoles` combined with `AccessManager.getRoles(person, object)` explains a
decision ("the type grants `Write` to `Editor` and `Manager`, the user holds only `Viewer` on this
object").

### Objects that are not checked

- An object that is no `BoundObject` is not access controlled; the checks answer `true`.
- A **deleted** object is accessible for nobody, not even for a person bypassing security, and
  nobody may create an object in a deleted context.
- An object that is **not yet committed** - created in the current transaction, or transient - is
  not checked: roles (in particular the ones of role rules) are computed at commit time, so an
  object without roles yet would be denied spuriously. Its creation has been checked against a
  committed context instead, which is what makes `new(T)..set(attr, v)` work in one transaction.
  From the next transaction on, the committed object is checked normally.
- A transient object that never becomes persistent (a working copy, the input of a dialog) is not
  part of the persistent model; creating it is not checked either. Turning it into a persistent
  object (a persistent `copy()` of it) is checked like any creation. Its attributes are restricted
  only by attribute-level grants, see `isAllowedInitial`.

## Creating objects

Creating an object in a composition of a parent requires both:

1. **`Create` on the created type, in the parent context**: the person holds a role on the parent
   that is granted `Create` on the type of the new object.
2. **`Write` on the composition attribute of the parent**: the attribute-level check of
   `isAllowed(person, parent, composition, Write)` - an attribute-level grant on the composition
   decides, else the `Write` right on the parent.

Creating a top-level object (no parent) requires condition 1 only, checked on the security root:
the user must hold a role on the security root that is granted `Create` on the type. Roles held on
the root are per type in effect, since each type grants `Create` to its own roles. Finer control
is gained by creating through a composition, where condition 2 adds the right on the container.

Special cases (`isAllowedCreate(person, type, context)`):

- A type `without-security` may be created by everybody.
- A context that is itself not yet committed is not checked; its own creation was.
- A type with an access parent has no `Create` grants of its own: creating it in a context is
  `Write` on the context; creating it without a context is not restricted, since the object is
  inaccessible until it is put into a container, which is a write of the container.

Where the creation checks are applied: the TL-Script function `new(type, context)` checks
condition 1 for a persistent object (not for `transient=true`), a persistent `copy()` checks
`Create` on the copied type, and instantiating a process (`CreateProcessInstance` of the BPE)
checks `Create` on the security root. The view layer checks both conditions for its create
commands, see the section "Creating objects" of the article `view-layer/access-control` of the
module `com.top_logic.layout.view`.

## TL-Script functions

The functions return the outcome of a check as a boolean for the current user. They never fail and
never filter, so a script can branch on a missing right instead of running into the error of a
denied `set` or `delete`:

| Function | Check |
|---|---|
| `canRead($o)`, `canWrite($o)`, `canDelete($o)` | `Read`, `Write`, `Delete` on the object |
| `canReadAttribute($o, $attr)`, `canWriteAttribute($o, $attr)` | `Read`, `Write` on the attribute of the object |
| `canCreate($parent, $composition, $type)` | both creation conditions; `$type` is optional (default: the target type of the composition), `$parent = null` checks a top-level creation |
| `canExecute($o, "Approve")` | an operation given by its command group id; an unknown id is an error |
| `filterSecurity($value)` | removes from the value every object the user may not discover: a single such object becomes `null`, collection elements are dropped, primitive values stay |

A `null` object answers `false`. The functions are never evaluated at compile time, since their
result depends on the user. A further `canXxx` function for another operation needs no code: a
`<method>` registration of `CanAccess$Builder` (object level) or `CanAccessAttribute$Builder`
(attribute level) with another `command-group`, like the registrations of `canRead` in
`modelSearchConf.config.xml` of `com.top_logic.model.search`.

`filterSecurity` is a safety net for values assembled from unsecured sources within a script: the
object sources of a secured script already deliver only discoverable objects, and the final result
of a secured execution is filtered anyway (see below). It must not be applied to an intermediate
result the script still computes with.

The functions only make sense in a secured script; in an unsecured one (a derived attribute, a
constraint, see rule R12 of [the Discover specification](doc:access-discover-spec#r12---secured-and-unsecured-script-kinds))
they still report the rights of the current user. The function `canCall` for privileged
functions is described in [privileged TL-Script functions](doc:access-privileged-scripts).

## Secured evaluation in own code

A script executed through a `QueryExecutor` runs **secured** by default: reading filters every
object the user may not discover, writing (`set`, `add`, `remove`, `delete`, `new`) checks the
rights and fails with a `TopLogicException`, and the result of `execute(...)` is filtered for
`Discover`. A `SecurityFilterReport` set on the `EvalContext` of the execution records what the
result filter removed (opt-in; without a report nothing is recorded).

- Code that executes a script on behalf of the **system** (an internal computation, a derived
  value, a maintenance job) calls `disableSecurity()` on the executor after compiling it;
  otherwise objects the current user may not read are missing, and a script executed without a
  logged-in user sees nothing. `UpdateSecurityVisitor.disableSecurity(expr)` does the same on a
  compiled `SearchExpression` tree.
- Which engine extension points run unsecured and which secured is listed in rule R12 of [the
  Discover specification](doc:access-discover-spec#r12---secured-and-unsecured-script-kinds). A
  definer's-rights computation (a derived attribute) sees everything; reading its value is
  protected by the right on the derived attribute, so its read grant must match what the value
  discloses (a `salaryBand` derived from `salary` needs a grant at least as restricted as
  `salary`).

A custom TL-Script function (`GenericMethod`) that reads or modifies the model must take part in
this:

- Extend `GenericMethodWithSecurity` (or `SearchExpressionWithSecurity` for a plain
  `SearchExpression`), which implement `WithSecurityCheck`. The `usesSecurity` flag is a
  constructor argument: forward it from the constructor and reproduce it in `copy(...)`, else a
  rewritten expression loses it.
- In `eval`, apply security only when `usesSecurity()` answers `true`: filter what is read with
  `SearchExpression.filterSecurity(value)`, check writes with `ModelAccessRights` and throw a
  `TopLogicException` on a denied operation. A plain `GenericMethod` touching the model bypasses
  the model security.
- A function whose result depends on the user or the session overrides
  `canEvaluateAtCompileTime(...)` to return `false`.

Running code with other rights than those of the current user - a system context, a job keeping
the person but bypassing the checks, a job for another person - is listed in step 1 of [the access
check](doc:access-configuration#the-access-check-in-one-sentence).
