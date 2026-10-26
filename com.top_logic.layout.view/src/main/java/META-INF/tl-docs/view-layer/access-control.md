---
description: Read before restricting who sees a part of a .view.xml - <access-control> on <nav-item>, sidebar <group>, <tab>, accordion <section> and <tile> (operation, inputs, object, type), the hidden / empty / "no access" decision, the Administration operation on the security root, and the SecuredObject a unit publishes to its content.
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
