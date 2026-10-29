---
description: Read before restricting who sees a part of a .view.xml or who runs a command - <access-control> on <nav-item>, sidebar <group>, <tab>, accordion <section> and <tile> (operation, inputs, object, type, denied), the hidden / empty / "no access" decision, hiding a denied unit (UnitHiding) or showing a placeholder, the Administration operation on the security root, the SecuredObject a unit publishes to its content, the <access> of a command with <operation name object type> and <create type in reference> (CommandAccess, OperationAccess, CreateAccess, hidden / disabled), the checks actions bring (<link-access> / <remove-access> of an <object-list>), and the edit-operation of a <form>.
order: 75
---

# Access control of UI units

A navigation item, a sidebar group, a tab of a `<tab-bar>`, a section of an `<accordion>` and a tile
of a `<dashboard>` take an `<access-control>`. It states **which operation** the user needs **on which
object** to see the content of the unit. It has no grants of its own: the decision is made by the model
access rights, i.e. the grants of the `<security-config>` of the `SecurityConfigurationService` (see
[access configuration](doc:access-configuration)), with `ModelAccessRights.isAllowed(person, object,
operation)` per checked object.

```xml
<!-- Checked on the security root. -->
<nav-item id="administration">
	<access-control operation="Administration"/>
	<view-ref view="admin/admin.view.xml"/>
</nav-item>

<!-- Checked on the project selected in the channel "project". -->
<tab id="planning">
	<access-control operation="ShowPlanning" inputs="project" object="$project"
		type="tl.demo.projectManagement:ProjectScope"/>
	<view-ref view="planning.view.xml"/>
</tab>
```

| Attribute | Meaning |
|---|---|
| `operation` | Mandatory. A command group: a built-in operation (`Read`, `Write`, …) or one declared with `<operation name="…"/>` for the type (see [operations](doc:access-configuration#operations-which-operations-apply-to-a-type)). |
| `inputs` | The input channels of the unit, written like the inputs of a command: `inputs="selection as projects, mode"`. The `object` expression sees them as variables. |
| `object` | TL-Script expression over the inputs yielding the checked object(s): a single object, or a collection, where the operation must be allowed on every element. Without `object`, the operation is checked on the security root (the singleton `SecurityStructure#ROOT`). |
| `type` | The qualified name of the static type of the checked object(s), mandatory with `object`. |
| `denied` | How the unit is displayed while the operation is refused on a checked object: `hide` (default) or `placeholder`, see below. |

## Decision

The unit is displayed as the check decides for the current user (`UnitAccess`, mapped like
`ModelAccessPolicy` maps the decision of a command):

- **Hidden** - the user can never hold the operation: no role is granted it (or an operation implying
  it) on the `type`, its specializations or, for a type with an access parent, the parent types; or the
  user is restricted. Without `object`, a refusal on the security root hides the unit as well. A hidden
  unit is not built at all: the sidebar shows no entry, the tab bar no tab, the dashboard no tile, and
  a hidden group withholds every item in it.
- **Empty** - the expression yields `null` or an empty collection (e.g. nothing is selected): the unit
  is shown without content.
- **No access** - the operation is refused on a checked object, or a checked value is no object of the
  declared `type` (logged). With `denied="hide"` (the default), the unit is not displayed, see
  [hiding a denied unit](#hiding-a-denied-unit). With `denied="placeholder"`, the unit stays and shows
  a "no access" placeholder naming the refused operation instead of its content.
- **Content** - the operation is allowed on every checked object.

The decision follows the inputs: it is made again when an input channel takes a new value, and when an
object an input holds is changed. Content that is no longer allowed is retired after the channel
notification has unwound (`ContentControls.retire`); a content allowed again is built anew. A
`<group>` with an `<access-control>` applies its check to the content of every `<nav-item>` in it, in
addition to the check of the item itself.

## Hiding a denied unit

A unit with `denied="hide"` is left out of its container while the check fails on its object, and
drawn again as soon as the object is allowed again - e.g. when the selection moves to another
project. The container neither removes nor rebuilds the unit, it only does not draw it:

- a tile is left out of the dashboard grid, which closes the gap; it is not offered to be added
  again, unlike a tile the user hid;
- a tab is left out of the tab strip; when the selected tab is hidden, the nearest visible tab is
  selected (the next one, the previous one at the end of the strip). A tab shown again is not
  selected again: the selection the user made in the meantime stays;
- an accordion section is left out, header included, keeping its expansion;
- a sidebar entry (a `<nav-item>` or a `<group>` with all its items) is left out. An open navigation
  item stays open: the user navigated there, so its content shows the "no access" placeholder
  rather than a blank page.

The state is one mechanism, `UnitHiding` (`AccessChecks.hiding(…)`): the container builds the
entry as hidden or not and binds the state to its control, which follows the inputs of the unit for
as long as the control lives. The entries carry a `hidden` flag the client skips: `TabBarState.Tab`
and `AccordionState.Section` in `state.proto`, the items of the sidebar and the tiles of the
dashboard in their state maps (`ReactTabBarControl.setTabHidden`, `ReactAccordionControl.setSectionHidden`,
`ReactSidebarControl.setItemWithheld`, `ReactDashboardControl.setTileWithheld`).

A unit checked on the security root is decided once when it is built, and a unit whose expression
yields no object is displayed empty with either setting.

## Checks when the view is loaded

When the view is loaded, the operation must apply to the `type` (or, without `object`, to the security
root): an operation that is not declared there is a configuration error, as is an `object` without
`type`, a `type` without `object`, an unknown type and an unknown command group.

## The administration

The engine declares the operation `Administration` on the security root and guards the administration
entry of `app.view.xml` with `<access-control operation="Administration"/>`. Without a grant, only the
technical super user (`root`) holds it. An application grants it to its administrator roles:

```xml
<security-config>
	<singleton name="SecurityStructure#ROOT">
		<grant operation="Administration" roles="my.app.Administrator"/>
	</singleton>
</security-config>
```

The rights of a singleton with grants of its own replace the rights of its type for that singleton, so
an application that also grants other operations on the security root through its type (`<class
name="SecurityStructure:Root">`) repeats those grants in the `<singleton>` entry.

## The object of a unit in its content

A unit with an `<access-control>` publishes what it checks to its content as a `SecuredObject` in the
`ViewContext` (`SecuredObject.of(context)`): the static `type` (or the security root), the input
channels and the current checked value (`getValue()`, computed anew from the current input values),
and the operations applying to it (`getOperations()`). It is independent of the operation of the unit,
so that a command inside the unit can check an operation of its own on the same object. The innermost
unit wins.


## Commands: the access they declare

A command (`<generic-command>`, `<upload-command>`, the `<action>` of a `<button>`, …) declares in
`<access>` what it does in the model (`ViewCommand.Config#getAccess()`, combined by `CommandAccess`):
the operations it performs on objects and the objects it creates. Like a unit, it has no grants of
its own.

```xml
<generic-command inputs="ticket, selection as milestones">
	<access>
		<!-- Approve on the projects of all selected milestones. -->
		<operation name="Approve" object="$milestones.map(m -> $m.container())"
			type="tl.demo.projectManagement:ProjectScope"/>
		<!-- A comment created in the ticket and added to its comments. -->
		<create in="$ticket" reference="demo.tickets:Ticket#comments"/>
	</access>
	...
</generic-command>
```

All checks must pass. When one of them can never pass for the current user, the command is
**hidden**; otherwise the first failing check **disables** it, giving its refusal as tooltip. The
access comes before the `<executability>` rules: it decides the state of the button and, again,
whether a command sent by the client runs. It is evaluated anew when an input of the command or an
input of the enclosing unit changes.

The expressions (`object`, `in`) see the arguments of the command as variables (its inputs and the
value its trigger supplies, as for the scripts of its actions) and are evaluated without the user's
rights. They yield a single object, or a collection, where every element must pass; `null` or an
empty collection is no object, which disables the command.

### Operations on objects: `<operation>`

`<operation name="…" object="…" type="…"/>` (`OperationAccess`):

| Attribute | Meaning |
|---|---|
| `name` | Mandatory. A command group, as the `operation` of a unit. |
| `object` | The object(s) the operation is checked on. Without `object`, the operation is checked on the object of the innermost enclosing unit with an `<access-control>` (its `SecuredObject`), and without such a unit, or for a unit checked on the security root, on the security root. |
| `type` | The static type of the checked object(s), mandatory with `object`. An object that is no instance of it is refused. |

The command is hidden when the user can never hold the operation on the type (as for a unit), or,
on the security root, when it is refused there. On a checked object, a refusal disables it, and an
allowed operation is finally decided by the global `CommandApprovalService`, asked without
component and command ID (e.g. the anonymous account is neither edited nor deleted).

### Creating objects: `<create>`

`<create type="…" in="…" reference="…"/>` (`CreateAccess`):

Creating an object is no operation on an object: the user needs the `Create` grant of the created
`type` on the roles held on the context the object is created in (for a type with an access parent:
`Write` on the context), and, when it is added to a composition `reference` of the context, `Write`
on that reference.

| Attribute | Meaning |
|---|---|
| `type` | The qualified name of the type of the created object. Mandatory without `reference`; with `reference`, it defaults to the type of the reference and must be compatible with it (e.g. a specialization). |
| `in` | The context(s) the object is created in. Without `in`, the context is the object of the enclosing unit with an `<access-control>`; without such a unit, or for a unit checked on the security root, the object is created without context, decided against the security root. |
| `reference` | The qualified name of the composition reference of the context the object is added to, e.g. `demo.tickets:Ticket#comments`. It needs a context. |

The command is hidden when the user can never create the type (no role is granted `Create` on it,
or, with `reference`, no role may write the reference), and disabled with the refusal when the
creation is refused in a context.

**An abstract type** stands for the choice among its concrete specializations, e.g. a command
asking the user which kind of object to create. The check passes in a context where *at least one*
concrete specialization may be created (`ModelAccessRights.isAllowedCreate` per concrete type, with
`reference` together with `Write` on the reference); in a collection of contexts, in every one. It
hides the command only when no role can ever create any of them, and disables it otherwise. The
concrete specializations are computed once, when the command is built. The chooser should offer only
the kinds the user may create there, with the TL-Script function `canCreate(parent, reference,
type)`:

```xml
<value-input inputs="project" type="tl.model:TLClass" value="kind"
	options="[`my.app:Specification`, `my.app:Prototype`, `my.app:Report`]
		.filter(t -> $project.canCreate(`my.app:Project#deliverables`, $t))"/>
<generic-command inputs="project, kind">
	<access>
		<create in="$project" reference="my.app:Project#deliverables"/>
	</access>
	<execute-script function="new($kind, context: $project, transient: true)">…</execute-script>
</generic-command>
```

The demo `tl-demo-react` shows this in the Deliverables tile of a project cockpit
(`projects/deliverables.view.xml`).

The actions creating objects (`<create-transient>`,
`<persist-transient>`) bring the same check of their own, see
[creating and deleting objects](doc:view-layer/commands#creating-and-deleting-objects-create-transient-persist-transient-delete-object).

### Checks the actions bring

Some actions bring a check of their own, so the command running them needs no `<access>` for it;
the checks decide on the main argument of the command and combine with the command's `<access>`
like its own checks:

- `<create-transient>` and `<persist-transient>`: the creation in their container, the check of a
  `<create>`. An empty container channel means a creation without container: without `reference`
  it is checked against the security root (refused → hidden), with `reference` there is nothing to
  add the object to and the command is disabled;
- `<delete-object>`: `Delete` on every processed object, plus the delete veto;
- `<link-element>` and `<remove-element>`: the `<link-access>` and `<remove-access>` the enclosing
  `<object-list>` declares, with the same `<operation>` and `<create>` checks over the inputs of the
  list and the element (see [the object list](doc:view-layer/layout#repeating-a-template-over-a-computed-list-object-list)). Without a declaration
  nothing is checked.

### Checks when the command is built

The operation of an `<operation>` must apply to its `type`, or, without `object`, to the type of the
enclosing unit or to the security root. An `object` needs a `type` and vice versa. The `type` of a
`<create>` must be a class, its `reference` a composition reference whose type is compatible with
the `type`. A `<create>` without `reference` needs a `type`, and an abstract created type needs at
least one concrete specialization. A violation is a configuration error.

## Editing a form

Entering the edit mode of a `<form>` requires the `edit-operation` on the displayed object, `Write` by
default. It is checked like the operation of a command, with the type of the displayed object: the
Edit button is hidden for a user who can never hold the operation on that type, and disabled with the
refusal as reason for a user refused on the object. The draft of a create dialog is always editable.
An operation that does not apply to the type of the displayed object is reported and refuses editing.
The rules of `<edit-executability>` decide in addition.

```xml
<form input="milestone" edit-operation="Approve">
	...
</form>
```
## Administration of grants

Units, commands, forms and privileged scripts only name the operation they check; who holds it is
decided by the grants of the type of the checked object. The administration edits these grants per
type under *Administration → Access control → Grants* (`admin/access-control/grants.view.xml`):

- The left side is the type tree of the security coverage (`SecurityCoverageTable`); "Apply
  configuration" makes the stored changes effective.
- The right side shows the grants of the selected type as a matrix (`TypeGrants` of
  `com.top_logic.element`, presented by the TL-Script functions `securityGrants…` of
  `SecurityGrantFunctions`): a row per role a user can hold on objects of the type - delivered by a
  role rule or a role parent rule, or assigned to such an object directly
  (`SecurityCoverageAnalysis.getHoldableRoles`) - and a row per such role on each attribute; a
  column per operation applying to the type, the built-in ones first. An attribute row offers
  `Read` and `Write` only. Roles that cannot be held on the type and operations that do not apply
  to it are not offered, since a grant of them has no effect.
- The tooltip of a column lists the places checking the operation on the type or one of its
  generalizations; for an operation the application declares, also the places not declaring the
  type they check (privileged scripts, forms, commands outside a unit of their view file).
- A click on a cell stores the grant, or its revocation, at once: a single `<grant>` or `<revoke>`
  of the role in the entry of the type (or of the attribute) in the autoconf file of the
  `SecurityConfigurationService`, applying to the type alone, not to its specializations. A cell
  whose stored state is not in effect yet is marked (`tl-status-warning`) until "Apply
  configuration". A grant held through an implying operation (`implied-by`, and every operation
  implying `Discover`) is shown checked and cannot be changed in that column. An attribute without
  a grant of the operation is not restricted; granting it to a role restricts the attribute to the
  roles granted, and revoking it from the last of them lifts the restriction again.
- A role that is granted an operation on the type (in effect or by a stored rule) but cannot be
  held there gets a row as well, marked ineffective (`tl-status-warning`, with a tooltip on the
  role), so that the grant the coverage check reports as dead can be revoked here.
- A grant held through an implying operation shows a read-only check box.
- A type delegating to an access parent, and a type exempt from access control, has no grants:
  the panel names the access parent by the labels of its type and reference
  (`AccessParentFunction.getLabel()`), or says that the type is exempt, instead of showing a
  matrix.

The matrix is a plain `<table edit-in-place="true">` with a `<dynamic-columns>` per operation (see
[tables](doc:view-layer/tables#column-declarations)) inside a `<switch rebuild="true">`, which
builds the table anew for every selected type, since the columns of a table are computed when it is
built.

The places checking an operation come from the `OperationUseIndex`, the provider this module
contributes to the `operation-uses` of the `SecurityCoverageCheck`. It scans every `*.view.xml` of
the application - `<access-control>` (checked on its `type`, or on the security root without
`object`), `<access>` with `<operation>` (on its `type`, or on what the innermost enclosing unit of
the same file checks) and `<create>` (`Create` on the created type), `<form edit-operation>` (type
not declared) - and the privileged scripts of the configured TL-Script functions (type of the
security object not declared). A view is scanned again when the `ViewLoader` reads it anew. The
coverage check reports a place checking an operation that does not apply to the checked type, or
that is unknown, see
[the coverage check](doc:access-configuration#checking-that-the-definition-is-complete-the-coverage-check).
