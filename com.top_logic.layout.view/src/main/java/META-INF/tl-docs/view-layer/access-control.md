---
description: Read before restricting who sees a part of a .view.xml or who runs a command - <access-control> on <nav-item>, sidebar <group>, <tab>, accordion <section> and <tile> (operation, inputs, object, type), the hidden / empty / "no access" decision, the Administration operation on the security root, the SecuredObject a unit publishes to its content, the <access> of a command with <operation name object type> and <create type in reference> (CommandAccess, OperationAccess, CreateAccess, hidden / disabled), and the edit-operation of a <form>.
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
  declared `type` (logged): the unit shows a "no access" placeholder naming the refused operation
  instead of its content.
- **Content** - the operation is allowed on every checked object.

The decision follows the inputs: it is made again when an input channel takes a new value, and when an
object an input holds is changed. Content that is no longer allowed is retired after the channel
notification has unwound (`ContentControls.retire`); a content allowed again is built anew. A
`<group>` with an `<access-control>` applies its check to the content of every `<nav-item>` in it, in
addition to the check of the item itself.

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
		<create type="demo.tickets:Comment" in="$ticket" reference="demo.tickets:Ticket#comments"/>
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
| `type` | Mandatory. The qualified name of the type of the created object. |
| `in` | The context(s) the object is created in. Without `in`, the context is the object of the enclosing unit with an `<access-control>`; without such a unit, or for a unit checked on the security root, the object is created without context, decided against the security root. |
| `reference` | The qualified name of the composition reference of the context the object is added to, e.g. `demo.tickets:Ticket#comments`. It needs a context. |

The command is hidden when the user can never create the type (no role is granted `Create` on it,
or, with `reference`, no role may write the reference), and disabled with the refusal when the
creation is refused in a context. The actions creating objects (`<create-transient>`,
`<persist-transient>`) bring the same check of their own, see
[creating and deleting objects](doc:view-layer/commands#creating-and-deleting-objects-create-transient-persist-transient-delete-object).

### Checks when the command is built

The operation of an `<operation>` must apply to its `type`, or, without `object`, to the type of the
enclosing unit or to the security root. An `object` needs a `type` and vice versa. The `type` of a
`<create>` must be a class, its `reference` a composition reference whose type is compatible with
the `type`. A violation is a configuration error.

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