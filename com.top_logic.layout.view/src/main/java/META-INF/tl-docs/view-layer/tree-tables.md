---
description: Read before configuring a <tree-table> in a .view.xml - a table whose rows form a tree (root and children functions, columns, selection, expansion and initial-expansion-depth, row-role and row-class, showing a node in the tree column with a display), editing its rows in a <form> (row-edit="ALL|SELECTED", rows joining the edit when displayed, the tree showing the edited state, adding a child of the selected row with <add-child create-type="..." insert="..."> and parent-as / child-as, deleting rows with on-remove="DELETE") - or drag and drop on it (<drag>, <drop target="ordered"> with the arguments objects / parent / before and as / parent-as / before-as), and before touching TreeTableElement, AbstractTableElement, TreeFunctions, RowTree, TreeRowArrangement, ChildRowCreation, RowCreation, RowHierarchy or TreeDropPlace.
order: 65
---

# Tree tables

A `<tree-table>` is a table whose rows form a tree: each object is a row with the columns of a [table](doc:view-layer/tables), its children are the rows below it, indented and opened by the toggle in front of the first column. The tree is computed exactly as a `<tree>` computes it, the table around it is configured exactly as a `<table>` is.

```xml
<tree-table
    root="all(`demo.tickets:Project`).firstElement()"
    children="$node.get(`demo.tickets:Item#children`)"
    parents="$node.get(`demo.tickets:Item#parent`)"
    observed-types="demo.tickets:Item"
    selection="selectedItem" selection-mode="multi"
    types="demo.tickets:Item" personalization-key="project-items"
>
    <columns>
        <column attribute="name"/>
        <column attribute="dueDate"/>
    </columns>
</tree-table>
```

- **The tree**: `root`, `children`, `parents`, `node-as` and `canExpandAll` are the properties of a `<tree>` (`TreeStructureConfig`), compiled and evaluated by the same `TreeFunctions`. Its scripts see the inputs, and `children` and `parents` the object of the node as `$node` (renamed by `node-as`), while the scripts of the columns see the same object as the row, `$row` (renamed by `row-as`; see [which names a script sees](doc:view-layer/channels#which-names-a-script-sees)). The object `root` computes is not displayed; the objects `children` returns for it are the top-level rows, and the rows are the objects `children` returns - so a cell, the selection channel, a drag and a drop all see business objects, never nodes. A row's children are computed when it is opened (and to tell whether it has any). The top-level rows are open when the tree table is displayed: `initial-expansion-depth` (default `1`) says how many levels are, `0` shows the top-level rows closed, `2` opens their children as well. It applies when the tree is built and when an input names another root, which starts the tree afresh; on every other update a row the user opened or closed keeps its state, and only rows of objects the tree had no row for are opened down to the depth.
- **Following the model** is that of a `<tree>`: the tree is held in a `DefaultTreeUINodeModel` kept in sync by the same `ObservableTreeModel` (through its `Display` seam), so a deleted object loses its row, an object that appeared in a child list gets one, the rows the user opened stay open, and an input naming another root builds the tree anew. An object of an `observed-types` type that is created or changed while the tree table has no row for it is placed through `parents`: the row of what holds it now has its children computed again when that row is open, and a closed row computes them when it is opened - so `observed-types` requires `parents` (see [a tree follows the model it displays](doc:view-layer/tables#selecting-rows-and-nodes)). The rows are keyed by their business object (`TreeStructure.key`), so expansion and selection survive a rebuild.
- **The table**: `types` (or `types-expr`, see [types known only at runtime](doc:view-layer/tables#column-declarations)), `<columns>` (whose scripts see the row as `$row`, or by the name `row-as` gives it), `fixed-columns`, `selection`, `selection-mode`, `<on-activate>`, `activation-button`, `filter-bar`, `<presets>`, `active-preset`, `search-term`, `displayed-rows` (see [the displayed rows](doc:view-layer/tables#the-displayed-rows-displayed-rows)), `row-role` and `row-class` (with the `cell-role` and `cell-class` of its columns, see [marking rows and cells](doc:view-layer/tables#row-and-cell-roles-marking-rows-and-cells-by-their-data)) `<drag>` and `<export>` (the tree written as an outline, see [the export](doc:view-layer/tables#exporting-a-table-to-excel-export)) are those of a `<table>` (`AbstractTableElement.Config`), built by the same code (`AbstractTableElement.createTable`). Sorting orders siblings; a filter keeps the rows that match and their ancestors, all opened. A tree table has no `rows` and no `group-by`; its `row-edit` is described under [editing rows](#editing-rows).
- **Showing a node in the tree column**: the toggle stands in front of the first column, which shows what its declaration says. A node shown by its icon, a label of its own and a color role is a column over the row object with a [display](doc:view-layer/tables#displaying-a-cell-or-a-node-display-and-nodecontent) computing these parts - no column class of the application's own:

  ```xml
  <columns>
  	<computed-column name="device" value="$row">
  		<display class="com.top_logic.layout.view.element.ResourceDisplayByExpression"
  			label="$value.get(`my:Device#name`) + ' (' + $value.get(`my:Device#address`) + ')'"
  			role="$value.get(`my:Device#active`) ? null : 'warning'"
  		/>
  	</computed-column>
  	…
  </columns>
  ```

  The display leaves the click to the row (`link` is off by default), and the search of the tree table finds the row by the computed label.
- **Selection** works as in a `<tree>` (`TreeSelectionBinding`, shown on the rows through `TreeSelectionDisplay.of(TableViewControl, …)`): the channel holds the selected object, a set of them, or nothing, and an object written to it is revealed - the rows above it are opened (`TableViewControl#expandRows`), it is selected and its row scrolled into view; its row is found through `parents` where the tree table declares it, and by searching otherwise. When the selected object loses its row - deleted, or taken out of the child list it hung in, also while the tree table was off screen - the nearest remaining row above it becomes the selection and is written to the channel; where no row above it is left, the channel is cleared (see [selecting rows and nodes](doc:view-layer/tables#selecting-rows-and-nodes)).
- The control is the `TableViewControl` in tree mode, fed by a `TreeRowSource`; the source is the table's `RowHierarchy` (`TableView.hierarchy()`): which row holds a row, and the children of a row in display order, whether it is open or not.

## Editing rows

A `<tree-table row-edit="ALL">` inside a `<form>` edits the values of its rows as a row-editing `<table>` does (see [tables](doc:view-layer/tables)): while the form is in edit mode, the cells show the inputs of their columns, the changes are buffered in the form and stored by its *Save*, discarded by its *Cancel*.

```xml
<form input="editAnchor">
    <tree-table row-edit="SELECTED" selection="selected"
        types="demo.tickets:Project,demo.tickets:Item"
        root="all(`demo.tickets:Project`).firstElement()"
        children="$node.get(`demo.tickets:Item#children`)"
        parents="$node.get(`demo.tickets:Item#parent`)"
    >
        <columns>
            <column attribute="name"/>
            <column attribute="dueDate"/>
        </columns>
    </tree-table>
</form>
```

- **Rows join the edit when they are displayed.** The edit session of the table reads no rows when the form enters edit mode. A row joins it when the table computes it - the top-level rows and those below open rows at once, the children of a row when it is opened in edit mode, also in a tree declared `canExpandAll="false"`: the row object of a node is then the buffer the session edits the node's object through (`RowSetEditSession#adoptRows`). A row that joins is no change: the form does not become dirty by opening rows, and saving stores the changes of the rows, nothing else - no row is written into the object holding it.
- **One attribute, resolved per row.** An attribute column (`<column attribute="…">`) is resolved against the `types` of the table, and each cell edits the attribute of its own row's type (`AttributeCellEditing`): a row whose type lacks the attribute shows an empty cell without input. A table over rows of several types therefore lists them all in `types`.
- **The tree shows the edited state.** In edit mode the tree is not observed: a change of the model or of an input made elsewhere does not change its rows, so the rows being edited stay where they are. Instead the tree follows the edits of the form: the `children` function is evaluated over the buffers of the form, so a row added to the buffered composition of its parent appears under it, and a removed row disappears (see [the tree in edit mode](#the-tree-in-edit-mode)). After *Save* or *Cancel* the tree follows the model again and takes up what changed meanwhile.
- **The place survives the switch.** Each switch between view and edit mode builds the table anew over the same tree: the rows are keyed by their stored objects, the rows that were open stay open, and the selection channel is applied to the new table, so the selected row stays selected.
- **Selection** is that of a tree table, in both modes. The channel holds what a row-editing `<table>` puts there: in edit mode the buffer of the selected row, in view mode the stored object; a buffer or a stored object written to it is revealed and selected either way (`TreeSelectionBinding.ChannelObjects`). With `row-edit="SELECTED"` exactly the selected rows are editable.
- **Adding and removing rows** is configured by `<add-child>` and `on-remove="DELETE"`, see [below](#adding-and-removing-rows); without them, the tree table edits the values of its rows only.
- **Configuration errors** as for a `<table>`: `row-edit` outside a `<form>` fails when the table is built, `row-edit` together with `edit-in-place`, a `<drag>` or a `<drop>` is reported (`AbstractTableElement#checkRowEdit`).
- **Underneath**, the table is the `RowSetTableControl` of a row-editing `<table>` (built by `AbstractTableElement#createRowEditControl`), which arranges its rows through a `RowArrangement`: a flat table through `ListRowArrangement`, a tree table through `TreeRowArrangement` over the `RowTree` the read-only tree table is built on as well. The arrangement creates the `TreeRowSource` of each table it builds with the expanded rows of the one before (`TreeRowSource#expandedKeys()`), maps a node to the buffer of its object while the session runs, and suspends the observation of the tree in edit mode (`ObservableTreeModel` catches up when it is attached again). The binding is a `QueryRowSetBinding` reading no rows and offering no create types, with the remove mode of `on-remove`; rows are created by the `ChildRowCreation` of `<add-child>` (`RowSetTableControl#setRowCreation`).
- Demo in `com.top_logic.demo.react`: *Tree grid* (`demo/tree-grid-demo.view.xml`) edits the project scopes, their milestones, deliverables and tickets, once with `row-edit="ALL"` - adding milestones to a scope and deleting rows - and once with `row-edit="SELECTED"` - adding deliverables of a chosen type to a scope.

### Adding and removing rows

```xml
<form input="editAnchor">
    <tree-table row-edit="ALL" on-remove="DELETE" selection="selected"
        types="demo.tickets:Project,demo.tickets:Item"
        root="$project"
        children="$node.get(`demo.tickets:Item#children`)"
    >
        <inputs><input channel="project"/></inputs>
        <add-child
            create-type="if($parent.instanceOf(`demo.tickets:Item`) || $parent.instanceOf(`demo.tickets:Project`), `demo.tickets:Item`, null)"
            insert="$parent.add(`demo.tickets:Item#children`, $child)"
        />
        <columns>…</columns>
    </tree-table>
</form>
```

- **`<add-child>` adds a child of the selected row.** In edit mode, the *Add* command of the form (`RowSetTableControl#COMMAND_ADD_ROW`, contributed to the enclosing command scope as for a frameless row-editing `<table>`) adds a row below the selected row, its *parent*. While no row is selected, the parent is the object the tree's `root` function computes, and the new row is a top-level row. With more than one selected row, nothing is added.
- **`create-type`** computes the type of the new row from the parent - the variable `$parent`, renamed by `parent-as`, beside the inputs of the table: a concrete class is created directly; an abstract one stands for its concrete specializations the user may create, which the *Add* button offers in a menu (as the `create-type` of a row-editing `<table>`); nothing means the parent gets no child - the command is offered but not executable while it is selected. Anything else is an error naming `create-type`.
- **`insert`** puts the new row where the `children` function finds it: it sees the parent as `$parent` and the new row as `$child` (renamed by `child-as`), and typically adds the child to a composition of the parent. Both are buffers of the form - the parent the buffer its row is edited through, the child the buffer of the new transient object -, so the write is an edit like a [value listener's](doc:view-layer/forms#reacting-to-a-field-change-value-listeners) `$object.set(...)`: nothing is stored before *Save*, *Cancel* discards it. Both scripts read the objects as they are edited (they run in a `TracingScope` over the form's overlays).
- **After the insert** the children of the parent are computed again, the parent is opened, and the new row is selected - and edited, also under `row-edit="SELECTED"`. It is keyed by its new object until it is saved.
- **Saving** stores the new row once, as a part of the composition it was inserted into and in the context of the parent (`BufferSave`: a new object reached through a composition is created in it, whichever buffer reaches it first), together with the changed rows and the changed compositions of their parents. A parent that is no row - the object the tree is built from - is edited through `RowSetEditSession#editObject(TLObject)`: the object of the form through the form's own buffer, which the form stores (its references to the new rows are replaced by the stored objects first, `BufferSave.save(List, TLObject, TLReference, Collection)`), any other object through a buffer the session stores with its rows. After the save the saved row stays selected and open rows stay open, now keyed by the stored object (`RowSetEditSession#savedObject(TLObject)`).
- **`on-remove="DELETE"`** offers the removal of rows as in a row-editing `<table>`: a delete button at the end of every row in edit mode (`RowSetTableControl#COLUMN_DELETE`) and, with `selection-mode="multi"`, *Delete selected*. A removed row disappears at once with the rows below it; it is deleted on *Save*, and *Cancel* brings it back. Deleting an object deletes the parts of its compositions, so the rows that are parts of a removed row are removed with it (`RowSetEditSession#removeRows`). A row displayed below a removed row through a reference that is no composition - the tickets a milestone shows through a backwards reference, say - only disappears from the display with its parent: it is not deleted, and changes made to it are still saved. A new row removed again is taken out of the compositions it was inserted into and is not stored. `on-remove="UNLINK"` is a configuration error: a tree table has no row set to unlink from.
- **`<add-child>` and `on-remove` require `row-edit`**; `<add-child>` without it is reported.

### The tree in edit mode

- **The children are computed over the buffers.** While the edit session runs, `TreeRowArrangement` installs a `TreeFunctions.ChildList` on the `RowTree`: the `children` function is evaluated with the stored object of the node (the new object of a new row) in a `TracingScope` over the overlays of the form (`RowSetEditSession#overlays()`), which reads an object edited in the form from its buffer. The objects it returns are the children, buffers replaced by the objects they edit, the rows the session removed left out (`RowSetEditSession#isRemoved(TLObject)`).
- **Only the child lists that read a changed value are computed again.** Each computation of a child list records the attribute values it read. A value written into a buffer of the session - by a cell, by `insert`, by a value listener, by a dialog - is announced by `RowSetEditSession.Listener#onValueWritten`; the child lists that read it are computed again when the interaction has completed (`DeliveryScope#afterInteraction`), keeping the nodes of the children that stay, with their expansion and selection (`ObservableTreeModel#reconcileChildren(DefaultTreeUINode, TreeBuilder)`). A change of any other value computes nothing again. When the session begins, the child lists computed so far are computed once more, to record what they read.
- **What the buffers do not hold is not seen.** The values a child list reads are those of the buffers, and stored values for everything else. A backwards reference (`kind="backwards"`) is answered from the stored objects referring to the object (its `ReverseStorage` reads `tReferers`, which the database answers, also for a buffer, `TLObjectOverlay#tReferers`), never from buffered forward values: a new ticket whose buffered `milestone` is a milestone does not appear in that milestone's backwards `tickets` before it is saved. A `children` function listing rows through such a reference therefore does not show a child added there; add children through a forward reference - a composition - of the parent.
- **After the session** the child lists are computed from the stored objects again: saved new rows appear as their stored objects, discarded ones and deleted rows are gone.

## Drag and drop

A `<tree-table>` declares `<drag>` and `<drop>` with the vocabulary of a table — `row-executability` decides per row, `target` is `table`, `row` or `ordered` — but an insertion is made among the children of a row, as in a [tree](doc:view-layer/tables#drag-and-drop-of-tree-nodes):

| `target` | applies | arguments of `refuse-if` and the actions | renamed by |
|---|---|---|---|
| `table` | anywhere on the table | `objects`, `target` (always `null`) | `as`, `target-as` |
| `row` | onto the row under the pointer, in any part of it | `objects`, `target`: the object of the row dropped on | `as`, `target-as` |
| `ordered` | at a place among the rows | `objects`, `parent`: the object whose children the dropped objects become, `before`: the child they are inserted before, `null` for an insertion as the last children | `as`, `parent-as`, `before-as` |

The scripts use the arguments as variables without declaring them (see [named arguments](doc:view-layer/commands#scripts-use-the-arguments-as-variables)); `target-channel`, `parent-channel` and `before-channel` additionally publish the references as channel state where a view needs them there.

```xml
<tree-table root="$project" children="$node.get(`demo.tickets:Item#children`)"
    selection="selectedItem" types="demo.tickets:Item"
>
    <inputs><input channel="project"/></inputs>
    <drag kind="item">
        <row-executability>
            <disabled-if expr="$row.get(`demo.tickets:Item#locked`)"/>
        </row-executability>
    </drag>
    <!-- Move items: insert the dragged items under `parent` before its child `before`. -->
    <drop accept="item" target="ordered" as="items"
        refuse-if="$parent.recursion(p -> $p.container()).containsSome($items) || $items.containsElement($before)"
    >
        <with-transaction>
            <!-- Take the items out of their containers, then insert them at the index of `before`, at the end without one. -->
            <execute-script function="{
                $items.foreach(i -> $i.container().remove(`demo.tickets:Item#children`, $i));
                children = $parent.get(`demo.tickets:Item#children`);
                $parent.add(`demo.tickets:Item#children`, if($before == null, $children.size(), $children.elementIndex($before)), $items);
            }"/>
        </with-transaction>
    </drop>
    <!-- Drop a ticket dragged out of a table onto a row: assign it to the row's item. -->
    <drop accept="ticket" target="row" as="tickets" target-as="item">
        …
    </drop>
    <columns>…</columns>
</tree-table>
```

- **Where an insertion goes** — the rules of a tree: the **upper third** of row X inserts before X among its siblings (`parent` = the object of the row holding X, `before` = X; a line above X); the **middle third** inserts into X as its first children (`parent` = X, `before` = the first child of X or `null`; X highlighted), computing the children of a row that is not open; the **lower third** inserts as the first children of X where X is open and has children, and after X among its siblings otherwise (`before` = the next sibling, `null` after the last one; a line below X); **beside the rows** appends to the top-level rows (`parent` = the object `root` computes, `before` = `null`; the table highlighted). Siblings are taken in the order the rows are displayed in, so an insertion meant to follow the order of the `children` function is made while the table is not sorted.
- **Declared order and fall-through** as in a table: an `ordered` drop declared before a `row` drop takes the middle third as an insertion into the row and hands it to the `row` drop only where it refuses; a `row` drop declared first takes the whole row.
- `parent` belongs to the `ordered` drop of a tree table (`DropTargetMode.treeSignature()` is `DropSignature.ORDERED_TREE`, where a flat table's `signature()` is `ORDERED_LIST`); `parent-as` or `parent-channel` on a `table` or `row` drop is a configuration error, as is `target-as` or `target-channel` on an `ordered` drop. The drops are `TableDropConfig`s.
- **Activation**: `<on-activate>` runs its command with the activated row as the argument `row`, as in a table; `activate-as` names it otherwise.
- Dragging a selected row drags the whole selection, an unselected row drags itself. Rows and nodes move between tree tables, tables and trees in either direction, since all of them take part in the same protocol.

## Underneath

- **One set of tree rules.** `TreeDropPlace` (`com.top_logic.layout.react.control.dnd`) resolves a node and a zone into the location of each mode and the marker to draw, over a `DropTreeNavigation` - top-level nodes and their parent object, the parent of a node, its children in display order, whether it is expanded. `ReactTreeControl` implements the navigation over its tree model, `TableViewControl` over the `RowHierarchy` of its view; both resolve and probe drops through `DropSupport`.
- **Zones on the client.** A table whose rows form a tree (it has a hierarchy and is not grouped) sends `dropTreeZones` (`TableViewControl.DROP_TREE_ZONES`), and `TLTableView.tsx` splits its rows with `treeZoneSplit` instead of `flatZoneSplit`: an `ordered` drop needs the upper, middle and lower third of a row.
- **Recording**: a drop is recorded with the keys of the dragged and the target row, exactly as for a tree; a replay onto a row inside a collapsed row fails.
