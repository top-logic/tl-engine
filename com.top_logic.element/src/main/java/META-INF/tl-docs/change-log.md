---
description: Read before showing, undoing or reverting the changes of an object and the objects belonging to it - the change subtree of a root object (SubtreeFilter), the <change-subtree> annotation (TLChangeSubtree) on reference ends, the change log table (ChangeLogListModelBuilder, ChangeLogOptions), the commands UndoCommandHandler, RedoCommandHandler and RevertCommandHandler, ChangeSetReverter and the TL-Script functions changeLog, changeSetRestrict and revertChanges, and the access rights on the change log (ChangeSetRestriction, ChangeSet#incomplete).
---

# Change log of an object and its subtree

The model change log turns the technical revisions of the knowledge base into change sets of the
model (`tl.changelog:ChangeSet`, built by `ChangeLogBuilder`): each change set holds the
creations, deletions and updates (`tl.changelog:Change`) of one commit. A change log restricted to
one object shows the change sets that touched the object **or an object belonging to it** - its
*change subtree*. The same restriction is used to undo, redo and roll back the changes of an
object.

## The change subtree

The change subtree of a root object `R` consists of `R` and every object reachable from `R` over
**qualifying reference ends**: an end `E` declared on the type of `y` (the *root side*) with the
target type of `z` (the *child side*) qualifies the edge `y -> z` when `z` is a value of `E` on `y`.
Membership is evaluated in the revision of the change, so an object belongs to the subtree of the
root it was connected to when it was changed.

Which ends qualify:

| Reference end | Qualifies |
|---|---|
| the composite end of a composition (the reference holding the parts) | yes, unless an end of the pair is annotated `<change-subtree include="false"/>` |
| a reference that is no composition | only when it is annotated `<change-subtree/>` (`include` defaults to `true`) |
| any end annotated `<change-subtree include="false"/>`, or whose opposite end is | no |

Each end decides for its own direction: the owner of the annotated end is the root side. Annotating
the forward reference `Project#documents` puts the documents into the subtree of the project;
annotating the backwards reference `Document#project` instead puts the project into the subtree of
each of its documents. Both ends may be annotated, the relation then qualifies in both directions;
a cycle of qualifying references is no problem.

The annotation (`TLChangeSubtree`, tag `change-subtree`) sits on a reference or composition in the
model and can also be edited in the in-app model editor:

```xml
<class name="Milestone">
	<attributes>
		<!-- Opt in: the tickets of a milestone belong to its change subtree. -->
		<reference name="tickets"
			inverse-reference="milestone"
			kind="backwards"
			multiple="true"
			type="Ticket"
		>
			<annotations>
				<change-subtree/>
			</annotations>
		</reference>
	</attributes>
</class>
<class name="Ticket">
	<attributes>
		<!-- Opt out: the notes are parts of a ticket, but their changes are not shown in the
		     change log of the ticket. -->
		<reference name="notes"
			composite="true"
			kind="forwards"
			multiple="true"
			type="Note"
		>
			<annotations>
				<change-subtree include="false"/>
			</annotations>
		</reference>
	</attributes>
</class>
```

(The example model is `tl.demo.projectManagement` of `com.top_logic.demo`.)

### Which changes belong to the subtree

`SubtreeFilter` decides per change, on the historic object of the change:

- A **creation** or an **update of a plain attribute** belongs to the subtree when the object is in
  the subtree in the revision of the change.
- A **deletion** is checked on the object as it was before the deletion.
- An **update of a qualifying reference** (or of its opposite end) is checked in the revision of
  the change *and* in the revision before. Moving an object from one root to another therefore
  shows up in the change log of both roots: as moving out in the old one, as moving in in the new
  one. This holds for every storage of the reference: when the values are stored in the rows of
  the referenced objects (e.g. `inline-set-storage`), the move is reported as an update of the
  reference at the old and at the new container and as an update of the opposite end (if
  modelled) at the moved object.
- An update that only modifies references opted out by `include="false"` is not shown, even on
  the root itself: adding a note to `Ticket#notes` in the example is no change of the ticket.
  An update modifying such a reference together with another attribute stays visible.

A change set of the change log of a root holds only the changes belonging to its subtree: undoing
or reverting it reverts these changes, the other changes of the same commit stay.

`new SubtreeFilter(root, false)` accepts the changes of the root object only. The filter starts
the analysis at the creation of the root; it memoizes its results for its own lifetime and is not
thread-safe, so a filter is created per change log request.

## Showing a change log

- **Table**: `ChangeLogListModelBuilder` as model builder of a table. With a `TLObject` as model it
  shows the change log of the object's subtree, without a model the change log of the whole
  application (`root-required="true"` keeps the table empty instead). `max-entries` (default
  `30`) limits the number of rows. Every row is restricted to what the current user may see, see
  [Access rights](#access-rights).
- **TL-Script**: `changeLog(obj, maxEntries, includeSubtree, startRev, stopRev, author,
  includeTechnical, excludedModules)`, all arguments optional, returns the change sets newest
  first. `includeSubtree` defaults to `true`; `obj = null` returns the change log of the whole
  application. See the TL-Script documentation of `changeLog` in the application's help. With
  security checks, only administrators may call it, see [Access rights](#access-rights).

The options shared by every configuration inspecting the change log are `ChangeLogOptions`:

| Option | Meaning |
|---|---|
| `all-users` | the changes of every user, not only those of the current user |
| `include-technical-changes` | also the change sets of purely technical changes that do not show in the model |
| `max-time` | how far to look into the past (default `7d`, `0` is unlimited) |
| `excluded-modules` | modules whose objects' changes are not reported |

Configure a table and the undo/redo commands operating on it with the same options, so that the
commands consider exactly the changes the table shows.

## Access rights

A change set holds the old and new values of every changed attribute, regardless of whether the
user may read the object or the attribute. The change log is therefore not shown unrestricted to
a user who is no administrator:

- `changeLog()` in an evaluation with security checks (a script of the user interface, an API
  endpoint, an agent tool, the console) is reserved to administrators, with or without an object
  argument; other users get an error. Without security checks (a privileged function, model
  logic) it is not restricted and does not filter.
- `changeSetRestrict(changeSet, user)` (`ChangeSetRestriction` in Java) copies a change set,
  keeping the creations, deletions and modifications the user may read, and in the values only
  the objects the user may discover. A hidden author is removed. A copy that lost a change, a
  modification or a value object is marked `tl.changelog:ChangeSet#incomplete`.
- An incomplete change set is not reverted: `ChangeSetReverter.revert` and `revertAll`, the
  TL-Script function `revertChanges` and the revert commands of the user interfaces refuse it with
  an error message, nothing is reverted.

`ChangeLogListModelBuilder` restricts every row for the current user. A view of the React UI reads
the change log in a privileged function and restricts each commit, like the function
`changeLogOfCurrentUser` the engine ships for the view "My latest changes"
(`tl-layout-view.conf.config.xml`):

```xml
<privileged-script name="changeLogOfCurrentUser"
	command-group="Read"
	security-object="currentUser()"
>
	<parameters>
		<parameter name="maxEntries"/>
	</parameters>
	<implementation>changeLog(null, $maxEntries, true, null, null, currentUser())
		.map(cs -> changeSetRestrict($cs, currentUser()))</implementation>
</privileged-script>
```

The tables show the flag `incomplete` as a column, so that a user recognizes a commit with hidden
parts.

The change sets are transient objects, which the access check does not check: what a user sees of
them is decided by the restriction above. The module `tl.changelog` is marked `internal` and grants
nothing (`ElementConf.config.xml`).

## Undo, redo and revert

The commands interpret their target model as the subtree root (`null`: the whole application):

- `UndoCommandHandler` reverts the newest change set in the subtree that is neither an undo nor a
  redo and has not been undone yet (`ChangeSetReverter.findUndoCandidate`).
- `RedoCommandHandler` re-applies the newest undo that has not been redone yet
  (`ChangeSetReverter.findRedoCandidate`). A redo is only possible while the newest change sets of
  the subtree are undos and redos only; after a regular change the command fails with the error
  that the redo chain is broken.
- `RevertCommandHandler` reverts the change set it is invoked on (typically the selected row of a
  change log table); for a change set that has already been reverted it reverts the revert.

Options of undo and redo besides the `ChangeLogOptions`:

| Option | Default | Meaning |
|---|---|---|
| `window-size` | `50` | how many of the newest change log entries are searched for the change set to undo or redo; `0` is unlimited |
| `include-subtree` | `true` | whether the whole change subtree of the target model is considered, or the object itself only |
| `check-conflicts` | `true` | ask for confirmation when the revert cannot be applied cleanly, e.g. because a later change touched the same values; without it the conflicts are shown as warnings and the revert is committed |

`RevertCommandHandler` has `check-conflicts` as well. Example
(`com.top_logic.demo/.../technical/components/undoRedo/changeLogTable.layout.xml`):

```xml
<modelBuilder class="com.top_logic.element.changelog.ChangeLogListModelBuilder"
	all-users="true"
	root-required="true"
/>
...
<button id="undoRedo_undo"
	all-users="true"
	class="com.top_logic.element.changelog.UndoCommandHandler"
	group="Write"
	include-subtree="true"
/>
<button id="undoRedo_redo"
	all-users="true"
	class="com.top_logic.element.changelog.RedoCommandHandler"
	group="Write"
	include-subtree="true"
/>
<button id="undoRedo_revert"
	class="com.top_logic.element.changelog.RevertCommandHandler"
	group="Write"
	target="selection(self())"
/>
```

### Reverting in code and TL-Script

`ChangeSetReverter` is the UI independent API:

- `revertAll(changeSets)` reverts the given change sets newest first **within the caller's
  transaction** and returns the conflicts reported; `revertAll(kb, changeSets)` opens and commits
  a transaction of its own.
- `revertSubtreeTo(root, targetRevision, message)` rolls the change subtree of `root` back to the
  state of `targetRevision`: it reverts every change set after that revision touching the subtree,
  in one transaction, without a window limit.
- `undoLast(root, windowSize, includeSubtree)` and `redoLast(...)` find the candidate like the
  commands and apply it in a transaction of its own, considering the changes of all users.

In TL-Script, `revertChanges(changes)` reverts the given change sets (e.g. a result of
`changeLog`) within the transaction of the calling script and returns the list of conflicts; an
empty list means a clean revert.
