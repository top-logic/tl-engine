---
description: Read before designing or implementing the Discover right of the model based security - the specification of an object-level right weaker than Read that makes an object visible (in references, enumerations, option lists, with its label) without exposing its attributes, the filtering of every object source in TL-Script and the UI, the checks on inputs, and the parent-path example Root -> A -> B -> C.
order: 21
---

# Specification: the `Discover` right

**Status:** implemented by ticket #29782. It describes the behaviour of the model based security
for TL-Script and the user interface. The configuration syntax and the access check are described
in [the access configuration article](doc:access-configuration). No application used the model
based security before, so the change of the earlier semantics needed no migration.

## Motivation

A user must often see the **path** to an object without being allowed to read the objects on that
path. Example: a structure `Root -> A -> B -> C` with the multi-valued references `Root#as`,
`A#bs` and `B#cs`. The user may read some `C` objects. To show them in a tree, the UI must show the
parent `B` and `A` of each such `C` - but not their data, and not the **siblings** of the path.

Before this change that was impossible:

- Reading `A#bs` requires the class-level `Read` right on `A` (an attribute grant only narrowed the
  class grant). A role that makes `A#bs` readable therefore made *all* attributes of `A` readable,
  unless every other attribute was locked by its own grant (a deny list - a new attribute was
  readable by default).
- A reference delivered its targets unfiltered ("a referenced object shows its label"). `Root#as`
  delivered *all* `A` objects, so the labels of all siblings were visible on every level.
- An enumeration delivered all instances as well. `all(`pm:A`)` delivered every `A` in the system,
  not only those on the user's path, and `all(`pm:A`).size()` disclosed their number.
- Every string coercion in TL-Script (`toString($a)`, `"" + $a`, sorting, formatting) showed the
  label of whatever object the script held.

## Terms

| Term | Meaning |
|---|---|
| Object right | An operation granted on the type of an object: `Discover`, `Read`, `Write`, `Create`, `Delete`, ... |
| Attribute right | An operation granted on a single attribute (`<part>` grant): `Read`, `Write`. |
| Object source | Every place an object enters a computation: reference navigation, backward references (`referers`), `all`, database queries (`kbQuery`), resolving by ID (`objectResolve`), script functions, the inputs of a script, option lists, table and tree models in the UI. |
| Secured evaluation | A computation on behalf of a user (UI, script console, options, commands). |
| Unsecured evaluation | A computation on behalf of the system (derived attributes, constraints, labels, access rules, BPE execution), see R12. |

## Rules

### R1 - `Discover` makes an object visible

A user holding `Discover` on an object

- sees the object wherever an object source delivers it (references, enumerations, option lists,
  trees, tables),
- sees the object's **label**,
- can select the object and set it as the value of a reference (see R7).

`Discover` alone does **not** make any attribute readable.

### R2 - every right implies `Discover`

A user holds `Discover` on an object if they hold a role on the object that is granted

- `Discover`, or
- any other object right (`Read`, `Write`, `Delete`, ...), or
- any attribute right on one of the object's attributes.

A right on a **composition** (creating parts in it, `Create`) implies `Discover` on the
**container**.

### R3 - attribute rights replace the class right for their attribute

An attribute is readable (writable) if the user holds a role on the object that is granted `Read`
(`Write`)

- on the attribute itself, if the attribute has its own grants for the operation,
- on the object's type, otherwise.

Attribute grants therefore **replace** the class grant for their attribute instead of narrowing it.
A role may read `A#bs` without being able to read any other attribute of `A`, and
`Person#password` stays restricted to its own roles. By R2, a role holding an attribute right holds
`Discover` on the object.

### R4 - every object source filters for `Discover`

In a secured evaluation, every object source delivers only the objects the user holds `Discover`
on: a single-valued source delivers `null`, a multi-valued source drops the element. This applies
to TL-Script and to the UI alike:

- reference navigation and `referers`,
- `all(T)`, `all($t)` and their compiled database form (`kbQuery`),
- resolving objects by ID (`objectResolve`, `objectResolveKey`),
- script functions delivering objects (engine and application functions),
- the inputs of a script (model, channels, command arguments, values passed from Java, REST, AI
  agents),
- option lists, table and tree models of the UI.

A filter on `all(...)` is compiled into the database query for an access on the query root only if,
for the owner type of the accessed attribute and its concrete specializations, every role holding
`Discover` also holds `Read`, no access parent is configured, no singleton of these types has grants
of its own and the attribute has no read grant of its own. An access to a reference attribute on the root is never compiled; otherwise the filter is
interpreted.

Model elements (types, attributes, modules) are not filtered. A form field showing the value of a
reference attribute masks instead of filtering, see R11.

The security filter report (`SecurityFilterReport`, the diagnostic for hidden rows) records only the
objects the executor removes from the final result, not the objects withheld by sources or inputs.

Composition parts are rows, not reference elements: in table cells and in their read-only dialog
the undiscoverable parts are filtered, not masked, and on save the hidden parts are kept.

As a consequence, every object a secured computation holds is discoverable, and every string coercion
(label) is allowed by R1. `filterSecurity` on the final result of a script becomes a safety net,
not the line of defence.

### R5 - inputs are checked for the operation

Filtering the output guarantees `Discover` only. The operations on an object are checked on their
input:

| Operation | Checked on the input |
|---|---|
| Reading an attribute (`$o.get(attr)`, UI field) | Attribute right `Read` (R3). Denied: the empty value; a table cell shows a placeholder (R11a). |
| Writing an attribute (`update`, `set`, UI field) | Attribute right `Write` (R3). |
| Creating a part | `Create` on the composition of the container. |
| Deleting an object | `Delete` on the object. |
| Setting a reference | `Discover` on every new target (R7). |

### R6 - results are user dependent

The result of a secured computation depends on the user: counts (`size()`), emptiness checks,
comparisons and aggregates see only discoverable objects. This is intended: an option list with a
business restriction (e.g. all projects of one organisation) shows each user only the projects they
hold `Discover` on.

Consequences:

- A cache of a secured result must be per user, or must not be shared.
- Checks that must see every object (constraints, uniqueness checks) run unsecured.

### R7 - setting references

A user may set a reference to any object they hold `Discover` on. Option lists deliver only such
objects (R4); a target reaching the operation in another way (e.g. by ID) is checked on the input
(R5).

### R8 - one mechanism for TL-Script and UI

TL-Script and the UI use the same mechanism. The `Discover` filter and the attribute checks live in
one layer below both (`ModelAccessRights` and a secured value access), not in individual TL-Script
expressions or UI components. UI components do not access values through the unchecked Java model
API (`tValue`) on behalf of a user.

The layer offers two views of a reference value: the **filtered** view (R4), used by TL-Script and
the navigation structures of the UI, and the **masked** view (R11), used by form fields.

### R9 - unsecured evaluation sees everything

An unsecured evaluation is neither filtered nor checked. It sees every object and every attribute.

### R10 - writing a multi-valued reference: replace or change

Writing a reference distinguishes two kinds of operation:

| Operation | Meaning |
|---|---|
| `$o.set(ref, value)` | **Replaces** the whole value. Afterwards the reference contains exactly the given elements - elements the user does not hold `Discover` on are removed as well. |
| `$o.add(ref, value)` | **Adds** the given element. All other elements, discoverable or not, stay. |
| `$o.remove(ref, value)` | **Removes** the given element. All other elements, discoverable or not, stay. |

A user who sets a value expects the reference to contain exactly what was set; `set(ref, [])`
empties the reference. To change a reference without touching what the user cannot see, a script
uses `add` and `remove`.

Consequences:

- The idiom `$o.set(ref, $o.get(ref).concat($x))` removes all elements the user does not hold
  `Discover` on, since the value read is filtered (R4). Secured scripts use `$o.add(ref, $x)`
  instead; the documentation of `set` points this out.
- `add` and `remove` are applied to the value at commit time and are therefore robust against
  concurrent changes; `set` is "last write wins".
- Replacing requires the `Write` right on the attribute (R5) - with it, the user may dispose of the
  whole value, including the elements they cannot discover.
- Validation (mandatory, cardinality, constraints) checks the stored value, not the filtered one.

### R11 - form fields mask undiscoverable elements

The UI distinguishes two uses of a reference value:

| Use | Behaviour |
|---|---|
| **Navigation structures** - trees, tables, option lists, enumerations | Filtered (R4): an undiscoverable object does not appear. |
| **Form fields** - the value of a reference attribute of an object, shown or edited | Masked: the field holds the **unfiltered** value and shows every undiscoverable element as a placeholder (e.g. `*****`). |

The `Read` right on an attribute therefore releases its **value** - the number and, for an ordered
reference, the order of the elements. `Discover` protects the **identity** of each element.

Rules for a masked element:

- The client receives no identity of it: no object ID in the client state, no link, no tooltip, no
  type icon, no label.
- Its position does not disclose its label. An ordered reference shows it at its stored position;
  an unordered reference, which the UI sorts by label, shows masked elements at a fixed position
  (at the end).
- It stays masked in every derived output of the field (export, copy to the clipboard).
- A user holding `Write` on the attribute may **remove** it and, in an ordered reference, **move**
  it (consistent with R10: the user disposes of the whole value).

Consequences:

- Saving a form field replaces the value (`set`, R10). Since the field holds the unfiltered value,
  the masked elements the user did not remove are written back unchanged - no difference
  computation, no position problem.
- A single-valued reference to an undiscoverable target shows the placeholder instead of an empty
  field: the mandatory check does not fail, and saving the unchanged field keeps the value.
- A field shows the stored value, so field and validation see the same value.
- TL-Script and form fields differ: `$root.get(`pm:Root#as`).size()` counts the discoverable
  elements only, the field of `Root#as` shows all of them, the undiscoverable ones masked.

### R11a - table cells mask unreadable attributes

A table cell of an attribute the user may not read on its row shows the placeholder (`*****`),
whatever its value - also when no value is set, and for every type of attribute (text, number,
date, boolean, reference, composition). The cell is distinguishable from an empty value: an
unreadable boolean is no unchecked checkbox, an unreadable reference no empty cell.

- The value is not read; the cell holds a marker instead of the empty value of the attribute.
- The cell is not editable, even for a user holding `Write` on the attribute.
- Sorting, filtering and export see only the placeholder: an unreadable cell sorts after every
  value, matches no filter criterion on a value, is found by the table search only through the
  placeholder text, and is exported as the placeholder.
- An embedded column whose path passes a reference the user may not read shows the placeholder as
  well.
- A column whose attribute the user may read on no row is not offered at all.
- Exception: a column built by a column provider of the application (`ColumnProvider`) shows and
  searches the cell by the placeholder, but its own sort and filter see the cell as empty - the
  provider is given the readable values only.
- Form fields are not affected: a field of an attribute the user may not read is hidden.

### R12 - secured and unsecured script kinds

Whether a script is evaluated secured is decided by its kind, not by the script itself:

| Unsecured (on behalf of the system) | Secured (on behalf of the user) |
|---|---|
| derived attributes (`AttributeByExpression`, `AttributeValueLocatorByExpression`) | option lists (`OptionsByExpression`, `TreeOptionsByExpression`, `DefaultSelectionProviderByExpression`, `CreateTypeOptionsByExpression`) |
| constraints, including uniqueness checks (`ConstraintCheckByExpression`, `DeleteConstraintByExpression`) | default values (`DefaultByExpression`) |
| **labels** (`LabelProviderByExpression`, the label script of `LabelByExpression`) | executability (`ExecutabilityByExpression`) |
| role rules and access parents (`PathByExpression`, `ScriptAccessParent`, `SecurityObjectByExpression`) | process initialisation (`ProcessInitializerByExpression`) |
| lock strategies (`LockStrategyByExpression`) | scripts in layouts and views, commands, the script console |
| BPE execution, scheduled tasks (`ScriptTask`), migration scripts, message consumers | tooltips and titles (`TooltipProviderByExpression`, `TitleByExpression`, the tooltip script of `LabelByExpression`) |

A called function - built-in or configured (`ConfiguredTLScriptFunctions`) - has no setting of its
own: it takes over the setting of the calling script.

Rules for the individual kinds:

- **Labels are computed unsecured.** By R1 the label belongs to `Discover`; a secured label script
  reading `A#name` would deliver an empty label for a user holding only `Discover` on `A`.
  *Before this change, label scripts were evaluated secured.*
- **Tooltips and titles are computed secured.** Only the label belongs to `Discover` (R1). A tooltip
  or title script typically shows attribute values (a budget, a description) and would disclose them
  to a user holding only `Discover`.
- **Default values are computed secured.** An unsecured default could set a reference to an object
  the user may not discover and thereby bypass R7. The default is user dependent (R6).
- **Constraint messages name only discoverable objects.** A constraint is evaluated unsecured (a
  uniqueness check must see every object), but its message is shown to the user: an object the user
  does not hold `Discover` on is not named ("name already in use", not "name already used by
  *Project X*"). This holds for the veto message of a delete constraint as well.
- **Derived attributes are computed unsecured.** Their value may contain data derived from objects
  the user may not read (e.g. a sum of budgets). Objects in the value are filtered (R4), computed
  values are not. The modeler decides with the derived attribute what it discloses; reading it is
  protected by the attribute right (R3).

## Example: showing the parent path

### Model

```
Root  --as-->  A  --bs-->  B  --cs-->  C
```

Instances: `Root` with `A1`, `A2`; `A1` with `B1`, `B2`; `B1` with `C1`, `C2`; the user is a
`Reader` of `C1` only.

### Configuration

Grants (`<security-config>`): `Reader` reads `C`; the path role `PathViewer` reads only the
structure attributes, which by R2 implies `Discover` on `Root`, `A` and `B`:

```xml
<class name="pm:C">
	<grant operation="Read" roles="Reader"/>
</class>
<part name="pm:Root#as">
	<grant operation="Read" roles="PathViewer"/>
</part>
<part name="pm:A#bs">
	<grant operation="Read" roles="PathViewer"/>
</part>
<part name="pm:B#cs">
	<grant operation="Read" roles="PathViewer"/>
</part>
```

Role rules (`AccessManager`): an object on the path gets `PathViewer` if the user is a `Reader` of
a `C` below it. The existing inheritance rules with path steps express this, the role mechanism
needs no change:

```xml
<inheritance-rule id="PathViewerOnB" meta-element="pm:B" role="PathViewer" source-role="Reader">
	<path>
		<step attribute="pm:B#cs"/>
	</path>
</inheritance-rule>
<inheritance-rule id="PathViewerOnA" meta-element="pm:A" role="PathViewer" source-role="Reader">
	<path>
		<step attribute="pm:A#bs"/>
		<step attribute="pm:B#cs"/>
	</path>
</inheritance-rule>
<inheritance-rule id="PathViewerOnRoot" meta-element="pm:Root" role="PathViewer" source-role="Reader">
	<path>
		<step attribute="pm:Root#as"/>
		<step attribute="pm:A#bs"/>
		<step attribute="pm:B#cs"/>
	</path>
</inheritance-rule>
```

`PathViewer` must not be the `source-role` of a rule inheriting roles **downwards** (container to
part): the siblings below a path object would then get the role and become visible.

### Result

| Expression / UI | Result for the user | Why |
|---|---|---|
| Tree below `Root` | `A1` | `A2` has no `PathViewer` and no other right: no `Discover` (R4). |
| Tree below `A1` | `B1` | `B2` is filtered (R4). |
| Tree below `B1` | `C1` | `C2` is filtered (R4). |
| `$root.get(`pm:Root#as`)` | `[A1]` | R4 |
| `toString($a1)` | `"A1"` | label visible with `Discover` (R1) |
| `$a1.get(`pm:A#name`)` | `null` | no attribute right on `A#name` (R3, R5) |
| `$a1.get(`pm:A#bs`)` | `[B1]` | attribute right, then filtered (R3, R4) |
| `all(`pm:A`)` | `[A1]` | R4 |
| `all(`pm:A`).size()` | `1` | R6 |
| `$c1.get(`pm:C#name`)` | the name | `Reader` reads `C` |

### Trying it in `tl-demo-react`

The React demo (`com.top_logic.demo.react`) configures this example on its project model as the
ticket-watcher scenario (see `demoReactConf.config.xml`): a person listed in the watchers of a
ticket (`Ticket#watchers`) is a `demo.react.TicketWatcher` of the ticket and reads it; on the project
containing the ticket, it is a `demo.react.ProjectPathViewer`, which reads only the names of the
project and its milestones and the structure references `ProjectScope#milestones` and
`Milestone#tickets`. Unlike in the example above, a milestone has its
project as access parent, so the watcher discovers every milestone of the project, but only the
watched tickets below them.

1. Log in as `root` and create an account in the administration.
2. Open the ticket `T1` (e.g. in the detail drawer demo), edit it and add the person of the new
   account to its watchers.
3. Log in as the new account and open the tree demo: it shows the project of `T1` with its
   milestones, and `T1` below its milestone - not the other tickets and not the other projects.
   The project and its milestones show their names, every other attribute (description, budget,
   members, due date) stays empty, and the project cannot be edited.

The group `users` does not matter here: its members read every account, every group and every
contributor, but get no role on the project model. To additionally see accounts masked in a
reference field (`*****`, e.g. the members of a project the account may read), remove the account
from the group `users`.

## Example: option list with a business restriction

```
all(`pm:Project`).filter(p -> $p.get(`pm:Project#organization`) == $org)
```

Each user gets the projects of the organisation they hold `Discover` on, and may select any of them
as the value of a reference (R7).

## Consequences

- **Scope: the React view layer.** The user interface part of this specification (R4, R8, R11, R11a) is
  implemented for the React view layer. The classic layout UI does not check attribute rights; its
  script based providers deliver the discoverable (not only the readable) objects, so a table there
  shows all column values of a row the user may only discover. An application using model based
  security with `Discover`-only roles must use the React view layer for such types.

- **The guarantee depends on every object source filtering.** Every source - including script
  functions of applications - must use the common layer (R8). The input checks (R5) stay as a second
  line of defence for the attribute values, but a source that does not filter discloses labels and
  existence.
- **Writing a filtered reference back with `set` removes the invisible elements** (R10).
  `$o.set(ref, $o.get(ref).concat($x))` is a trap in secured scripts; `add` / `remove` must be used.
  Form fields are not affected, they hold the unfiltered value (R11).
- **The `Read` right on a reference attribute discloses the number of its undiscoverable elements**
  in a form field (R11), though not their identity.
- **Business logic is user dependent** (R6). Checks that must see everything run unsecured.
- **Denials are silent.** A missing object is an empty value or a shorter
  list, not an error; a wrong configuration shows up as missing data. Before this change denials were
  silent as well, but left placeholders: with 20 `A` objects of which the user may read 5,
  `all(`pm:A`).map(a -> $a.get(`pm:A#name`))` delivered 5 names and 15 `null` values (the
  final `filterSecurity` kept `null`), and `all(`pm:A`).filter(a -> $a.get(`pm:A#name`) == null)`
  delivered the 15 unreadable objects, whose number `size()` disclosed. With R4 both deliver results
  for the 5 discoverable objects only.
- **Labels are disclosed by `Discover`.** Everything a label is computed from (the name, a label
  script) is visible to every user holding `Discover`. Labels must not contain confidential data;
  making the label attributes readable by an attribute grant keeps `toString($a)` and `$a.get(...)`
  consistent.
- **Performance.** Every navigation step decides `Discover` for every delivered object. The
  decision memo of the interaction helps; a reference cannot be filtered in the database.
- **Role recomputation.** Path roles are inherited upwards; a change on a leaf recomputes the roles
  of all its ancestors on commit.
- **Configuration.** Every type needs a deliberate `Discover` definition; the coverage check
  (`SecurityCoverageCheck`) and the access rights dialog must know the new operation.

## Open questions

1. **Cost of the upward role inheritance** in deep or wide structures.

## Relation to the earlier behaviour

The right is implemented by ticket #29782; this section lists what changed against the behaviour
before it.

- Attribute grants used to **narrow** the class grant; with R3 they **replace** it for their
  attribute (see [the access configuration article](doc:access-configuration)).
- References used to deliver their targets unfiltered; with R4 they are filtered for `Discover`.
- Form fields used to show all elements of a reference with their labels; with R11 the
  undiscoverable ones are masked.
- Table cells of an attribute the user may not read used to show the empty value of the attribute;
  with R11a they show the placeholder.
- Label scripts used to be evaluated secured; with R12 they are evaluated unsecured.
- Constraint messages could name any object; with R12 they mask undiscoverable ones.
- Ticket #29453 restricted `all()`, `all($t)` and `kbQuery` in a secured evaluation to the readable
  instances. This is now R4 with `Discover` as criterion, and the filter lives in the common layer
  (R8).
- Object sources outside the TL-Script functions that do not use the common layer (for example
  `loadFromSession`, `changeLog`, `inRevision`) are only covered by the input and result filters;
  moving them onto the common layer is follow-up work.
