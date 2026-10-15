---
description: Read before designing or implementing privileged TL-Script functions - a privileged-script (PrivilegedScript) in the configured TL-Script functions (ConfiguredTLScriptFunctions) authorized as a whole by a command group on a security object computed from its arguments, its unsecured implementation, the Discover filter on its result, the on-denied fallback and the canCall function.
order: 22
---

# Specification: privileged TL-Script functions

**Status:** specified by ticket #29858, not yet implemented. It extends the model based security
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
| Security object | The object the access check of a privileged function is made on, computed from the arguments of the call. |
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
| `security-object` | expression over the parameters, mandatory | Computes the security object (P3). Like `implementation`, the expression accesses the parameters as variables (`$milestone`), it is no function. |
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

1. evaluates `security-object` **unsecured** with the arguments of the call. The arguments of a
   secured call contain only discoverable objects (R4), so the unsecured evaluation starts from
   objects the user may see; it may navigate to objects the user may not see (e.g. the project of a
   watched ticket).
2. checks `ModelAccessRights.isAllowed(currentUser, securityObject, commandGroup)`. A security object
   `null` (or an expression result that is no `TLObject`) denies the call.
3. allowed: evaluates `implementation` **unsecured** with the arguments and filters its result for
   `Discover` (R4): a single object the user does not hold `Discover` on becomes `null`, such an
   element of a collection is dropped. Computed values (numbers, strings, ...) are not filtered;
   like a derived attribute (R12), the modeler decides what the function discloses.
4. denied: see P4.

The result is filtered at the call, not only at the end of the calling script: the calling script
continues secured and must not hold objects it may not discover.

Every privileged function called inside the unsecured implementation runs unsecured (P2); the
outer check authorizes the whole operation.

### P4 - a denied call

| Configuration | Result of a denied call |
|---|---|
| `on-denied` set | The result of `on-denied`, evaluated **secured** with the arguments of the call. |
| no `on-denied` | A `TopLogicException` naming the function ("no permission to call ..."). |

`on-denied` expresses the behaviour fitting the operation: `null` or `list()` for a reading
function (like reading an attribute without the right), `throw(...)` with a message of its own for
a writing function (like writing an attribute without the right). Without it the call fails: a
writing function whose denial was not configured must not silently do nothing.

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
- For a privileged function: the security object is computed unsecured (P3, step 1) and the result
  of `isAllowed(currentUser, securityObject, commandGroup)` is returned; `false` for a security
  object `null`. `on-denied` is not evaluated.
- For a plain configured function: `true`, it has no function level permission.
- Like `canExecute`, the check is always made for the current user, also in an unsecured
  evaluation.

`canCall` keeps the permission in one place: the executability of a command does not repeat the
security object expression of the function.

## Example

The project model of the React demo (`com.top_logic.demo.react`, prefix `pm:` for
`tl.demo.projectManagement`): a project (`ProjectScope`) contains its milestones in the composition
`ProjectScope#milestones`; a milestone has the flag `Milestone#approved`. A member of a project
(`demo.react.ProjectMember`) may approve a milestone of the project, but must not write
`Milestone#approved` in general (e.g. reset it in a form):

```xml
<class name="pm:ProjectScope">
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
		$milestone.set(`pm:Milestone#approved`, true);
		$milestone
	}</implementation>
</privileged-script>
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
- **Computed values are disclosed.** Only objects in the result are filtered (P3).
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

JUnit tests in `com.top_logic.model.search` (next to `TestDiscoverRight`, `TestTLScriptSecurity`):

- allowed call reads / writes attributes the user holds no right on,
- denied call without `on-denied` throws, with `on-denied` returns its (secured) result,
- security object `null` denies,
- undiscoverable objects in the result are filtered, computed values are not,
- an unsecured call does not check,
- a nested privileged function is not checked again,
- `canCall`: privileged allowed / denied, plain script `true`, an argument that is no call of a
  configured function a compile error,
- a missing `security-object` or `command-group`, or an unknown command group, is a configuration
  error.

Manual verification in `tl-demo-react`: a command "Approve milestone" calling `approveMilestone` with
`canCall` as executability, tried as project member and as ticket watcher.

## Documentation

- this article,
- a help page for `canCall` (en / de) under
  `com.top_logic.model.search/src/main/webapp/doc/{en,de}/DeveloperGuide/TLScript/`,
- R12 of [the Discover specification](doc:access-discover-spec): a privileged function called
  secured does not take over the setting of its caller, it checks and runs unsecured (P3).
