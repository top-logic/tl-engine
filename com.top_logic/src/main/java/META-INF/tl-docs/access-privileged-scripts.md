---
description: Read before configuring or changing privileged TL-Script functions - a privileged-script (PrivilegedScript) in the configured TL-Script functions (ConfiguredTLScriptFunctions) authorized as a whole by a command group on a security object computed from its arguments, its unsecured implementation, the Discover filter on its result, the on-denied fallback and the canCall function.
order: 22
---

# Privileged TL-Script functions

**Status:** implemented by ticket #29858. It extends the model based security
described in [the access configuration article](doc:access-configuration) and
[the Discover specification](doc:access-discover-spec).

## Motivation

In a secured evaluation every step of a script is checked against the rights of the current user:
reading an attribute requires its `Read` right, writing it its `Write` right, creating a part
`Create` on the composition, deleting `Delete`, and every object source delivers only discoverable
objects (R4, R5 of the Discover specification).

A business operation often consists of many such steps. Example: "approve a milestone" sets the
approval flag of the milestone, writes an audit entry and updates a counter on the project. Checked step by step, the
user must hold every low-level right involved - the `Write` right on the counter of the project,
the `Create` right on the audit log, and so on. The business rule is a different one: **a user
holding the operation `Approve` on the project of the milestone may approve the milestone**, whatever the
operation touches.

A privileged function authorizes such an operation **as a whole**: one check of one operation on
one object before the function runs, no checks inside it.

## Terms

| Term | Meaning |
|---|---|
| Configured function | A TL-Script function defined in the application configuration of `ConfiguredTLScriptFunctions` by a name, parameters and an implementation expression. |
| Privileged function | A configured function of the type `privileged-script` (`PrivilegedScript`). |
| Security object | The object(s) the access check of a privileged function is made on, computed from the arguments of the call: a single object or a collection. |
| Command group | The operation (`BoundCommandGroup`) the user must hold on the security object, e.g. `Approve`. |
| Secured / unsecured evaluation | See R12 of [the Discover specification](doc:access-discover-spec). |

## Rules

### P1 - a privileged function is a configured function of its own type

The configured functions (`ConfiguredTLScriptFunctions`, property `scripts`) accept a second entry
type besides `configured-script`:

```xml
<privileged-script name="approveMilestone"
	command-group="Approve"
	security-object="$milestone.container()"
>
	<parameters>
		<parameter name="milestone"/>
	</parameters>
	<implementation>...</implementation>
	<on-denied>null</on-denied>
</privileged-script>
```

`PrivilegedScript` extends `ConfiguredScript`; its configuration extends `ConfiguredScript.Config`
and adds:

| Property | Type | Meaning |
|---|---|---|
| `security-object` | expression over the parameters, mandatory | Computes the security object, a single object or a collection (P3). Like `implementation`, the expression accesses the parameters as variables (`$milestone`), it is no function. |
| `command-group` | command group reference (`CommandGroupReference`, resolved like the `operation` of a grant), mandatory | The operation checked on the security object (P3). An unknown command group is a configuration error at startup. |
| `on-denied` | expression over the parameters, optional | The result of a denied call (P4). |

In the configuration editor of the application, the user chooses between the plain and the
privileged script type. A `configured-script` keeps its behaviour; no migration is needed.

### P2 - an unsecured call is not checked

A privileged function called from an unsecured evaluation (a derived attribute, a constraint, BPE
execution, another privileged function, ...) runs unsecured without any check, like a plain
configured function (R9, R12).

### P3 - a secured call checks once and runs unsecured

A privileged function called from a secured evaluation

1. evaluates `security-object` **unsecured** with the arguments of the call, in an evaluation
   context of its own (not the one of the caller). The arguments of a secured call contain only
   discoverable objects (R4), so the unsecured evaluation starts from objects the user may see; it
   may navigate to objects the user may not see (e.g. the project of a watched ticket). An
   exception raised by the expression propagates; it does not deny silently.
2. checks the command group on the security object, with the rule for checked objects shared with
   views and commands (`CheckedObjects.checkStored`, next to `ModelAccessRights`):
   - a single object: `ModelAccessRights.isAllowed(currentUser, securityObject, commandGroup)`;
   - a collection: the user must hold the command group on **every** element, each element
     checked like a single object;
   - `null` or an empty collection: there is no object, the call is denied.

   An edit overlay of a form (a `TLFormObjectBase` that is no create overlay, e.g. the edited
   object an edit form passes as argument) is checked as the object it edits. A value that is no
   `TLObject`, a create overlay, a transient object, or an object created in the current
   transaction denies the whole call, also as one element of a collection: the access check
   allows everything on such an object, so the caller could otherwise authorize the call with an
   object of its own making (`new(..., transient: true)`).
3. allowed: evaluates `implementation` **unsecured** with the arguments and filters its result for
   `Discover` (R4): a single object the user does not hold `Discover` on becomes `null`, such an
   element of a collection is dropped. Computed values (numbers, strings, ...) are not filtered;
   like a derived attribute (R12), the modeler decides what the function discloses.
4. denied: see P4.

A function acting on several objects checks all of them, e.g. the projects of all given
milestones:

```xml
<privileged-script name="approveMilestones"
	command-group="Approve"
	security-object="$milestones.map(m -> $m.container())"
>
	<parameters>
		<parameter name="milestones"/>
	</parameters>
	<implementation>...</implementation>
</privileged-script>
```

The result is filtered at the call, not only at the end of the calling script: the calling script
continues secured and must not hold objects it may not discover.

Every privileged function called inside the unsecured implementation runs unsecured (P2); the
outer check authorizes the whole operation.

### P4 - a denied call

| Configuration | Result of a denied call |
|---|---|
| `on-denied` set | The result of `on-denied`, evaluated **secured** with the arguments of the call. |
| no `on-denied` | A `TopLogicException` naming the function and the reason: no security object, a security object that is no stored object, or the command group not held on a security object (naming it). |

`on-denied` expresses the behaviour fitting the operation: `null` or `list()` for a reading
function (like reading an attribute without the right), `throw(...)` with a message of its own for
a writing function (like writing an attribute without the right). Without it the call fails: a
writing function whose denial was not configured must not silently do nothing.

`on-denied` is evaluated secured because it runs exactly for a user who failed the check: evaluated
unsecured, it would be a second unchecked computation, reachable without the command group. A
denied call therefore behaves like a call of a plain configured function. Where a denied user
should perform a weaker operation instead (e.g. request an approval instead of approving),
`on-denied` calls a second privileged function with a command group of its own, for example
`on-denied="requestApproval($milestone)"`; since `on-denied` is secured, that call is checked.

### P5 - `canCall` reports whether a function may be called

The TL-Script function `canCall(call)` reports whether the current user may make the given call of
a configured function, without making it. Its argument is the call itself; its typical use is the
executability of a command that calls a privileged function:

```
canCall(approveMilestone($milestone))
```

- The argument must be a call of a configured function; anything else is an error when the script
  is compiled. An unknown function name is the usual compile error of a call.
- The arguments of the call are evaluated and bound as in the call itself (named arguments and
  default values included), but the function is not called.
- For a privileged function: the security object is computed unsecured (P3, step 1) and checked
  as in the call (P3, step 2): `true` iff the user holds the command group on the security object,
  on every element of a collection. `false` for `null`, an empty collection, and a security
  object or element that is no stored object (a create overlay, a transient object, an object
  created in the current transaction). An edit overlay of a form is checked as the object it
  edits. `on-denied` is not evaluated.
- For a plain configured function: `true`, it has no function level permission.
- Like `canExecute`, the check is always made for the current user, also in an unsecured
  evaluation.

`canCall` keeps the permission in one place: the executability of a command does not repeat the
security object expression of the function.

## Example

The project model of the React demo (`com.top_logic.demo.react`, module
`tl.demo.projectManagement`): a project (`ProjectScope`) contains its milestones in the composition
`ProjectScope#milestones`; a milestone has the flag `Milestone#approved`. A member of a project
(`demo.react.ProjectMember`) may approve a milestone of the project, but must not write
`Milestone#approved` in general (e.g. reset it in a form):

```xml
<class name="tl.demo.projectManagement:ProjectScope">
	<operations>
		<operation name="Approve"/>
	</operations>
	<grant operation="Approve" roles="demo.react.ProjectMember"/>
</class>
```

```xml
<privileged-script name="approveMilestone"
	command-group="Approve"
	security-object="$milestone.container()"
>
	<parameters>
		<parameter name="milestone"/>
	</parameters>
	<implementation>{
		$milestone.set(`tl.demo.projectManagement:Milestone#approved`, true);
		$milestone;
	}</implementation>
</privileged-script>
```

The command group needs a label in an application resource bundle, otherwise startup logs a
missing resource:

```
commandGroup.Approve = Approve
commandGroup.Approve.tooltip = Approving an object, e.g. a milestone of a project.
```

The command "Approve" in `views/projects/milestone-list.view.xml` calls the function and is enabled
by `canCall`:

```xml
<disabled-if expr="if(canCall(approveMilestone($milestone)), null, #('You may not approve this milestone.'@en))"/>
...
<execute-script function="approveMilestone($milestone)"/>
```

| Call | Result |
|---|---|
| `approveMilestone($m)` by a member of the project of `$m` | The milestone is approved, although the member holds no `Write` right on `Milestone#approved`. |
| `approveMilestone($m)` by a ticket watcher (`demo.react.ProjectPathViewer` on the project) | `TopLogicException` (no `on-denied`). |
| `canCall(approveMilestone($m))` | `true` for the member, `false` for the watcher. |

## Consequences

- **The implementation is trusted.** Inside a privileged function nothing is checked. A privileged
  function must do only what the command group justifies, and must not take objects or attribute
  names from its arguments that let a caller steer it to arbitrary data (e.g. a generic
  `setAnything($obj, $attr, $value)` checked on an unrelated object).
- **The security object decides.** A security object expression that does not lead to the object
  the operation affects (e.g. a constant global object) grants the operation for all objects. The
  documentation of the configuration points this out.
- **The command group is an operation of its own.** It must be declared for the type it is
  granted on (`<operation name="..."/>`, see
  [Operations](doc:access-configuration#operations-which-operations-apply-to-a-type)) and needs
  labels (`commandGroup.<Name>`, `commandGroup.<Name>.tooltip`) in an application resource bundle,
  see the example.
- **Computed values are disclosed.** Only objects in the result are filtered (P3).
- **Only objects, collections and maps are filtered.** A function value (lambda) returned by the
  implementation is not filtered and runs unsecured when the caller calls it. Objects inside any
  other value (e.g. a `ReflectiveDataObject` assembled by the implementation) are not filtered
  either. A privileged function must not return such values holding objects the caller may not
  discover.
- **The tracing evaluation sees the overlays of a form.** Called from a traced computation (e.g. in
  a form), every attribute read on the thread is answered from the form's overlays
  (`TracingScope`): the implementation and the `security-object` expression see the not yet stored
  values of the form. The check itself is made on the persistent object (P3, step 2), but a
  `security-object` expression navigating along a reference (e.g. `$m.get(...)`) follows the value
  the user entered. A `security-object` expression should therefore start from the argument itself
  or navigate along references the user cannot change in the form (e.g. `$m.container()`).
- **The security object must be of the intended type.** With `security-object="$arg"`, every
  persistent object the user holds the command group on passes the check - of any type carrying a
  grant of it, whatever the implementation expects. The `type` of a parameter is documentation
  only, it is not checked at call time. The modeler must ensure the argument is of the intended
  type, e.g. by checking it in the expression:
  ``security-object="if($arg.instanceOf(`my.module:MyType`), $arg, null)"``.
- **Discover still applies to the arguments.** A user can only pass objects they may discover (R4,
  R5); a privileged function does not open access to undiscoverable arguments.

## Scope

Not part of this specification:

- privileged Java functions (`TLScriptFunctions`, `MethodBuilder`),
- a generic `privileged(...)` construct inside TL-Script, which would place the permission in the
  calling script instead of the configuration,
- reporting command groups used only by privileged functions in the security coverage check
  (`SecurityCoverageCheck`).

## Testing

JUnit tests in `com.top_logic.model.search` (`test.com.top_logic.model.search.configured.TestPrivilegedScript`
and `TestPrivilegedScriptConfig`, next to `TestDiscoverRight`, `TestTLScriptSecurity`), on the test
model type `Proposal`. The rule for checked objects itself is tested in
`test.com.top_logic.model.security.TestCheckedObjects` (`com.top_logic`) and, on persistent
objects, in `test.com.top_logic.model.search.expr.TestOperationDeclarations`:

- allowed call reads / writes attributes the user holds no right on,
- denied call without `on-denied` throws, with `on-denied` returns its (secured) result,
- security object `null` denies, a transient or uncommitted security object denies,
- a collection as security object: allowed iff the command group is held on every element; one
  element without it, an empty collection, `null`, or a transient element denies the call (with
  the message naming the case), `canCall` agrees in each case; an edit overlay in the collection
  is checked as the edited object,
- an edit overlay is checked as the object it edits, a create overlay denies,
- undiscoverable objects in the result are filtered at the call (observed by the calling
  script), computed values are not,
- the tracing route (`ScriptTracer`) checks and filters like the plain route,
- an unsecured call does not check,
- a nested privileged function is not checked again,
- `canCall`: privileged allowed / denied, plain script `true`, an argument that is no call of a
  configured function a compile error,
- a missing `security-object` or `command-group`, or an unknown command group, is a configuration
  error.

The demo `tl-demo-react` shows the function: the command "Approve" of the milestone list, tried as
project member and as ticket watcher.

## Documentation

- this article,
- a help page for `canCall` (en / de) under
  `com.top_logic.model.search/src/main/webapp/doc/{en,de}/DeveloperGuide/TLScript/`,
- R12 of [the Discover specification](doc:access-discover-spec): a privileged function called
  secured does not take over the setting of its caller, it checks and runs unsecured (P3).
