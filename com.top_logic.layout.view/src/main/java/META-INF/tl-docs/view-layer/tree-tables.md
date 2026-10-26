---
description: Read before configuring a <tree-table> in a .view.xml - a table whose rows form a tree (root and children functions, columns, selection, expansion) - or drag and drop on it (<drag>, <drop target="ordered"> with the arguments objects / parent / before and as / parent-as / before-as), and before touching TreeTableElement, AbstractTableElement, TreeFunctions, RowHierarchy or TreeDropPlace.
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

- **The tree**: `root`, `children`, `parents`, `node-as` and `canExpandAll` are the properties of a `<tree>` (`TreeStructureConfig`), compiled and evaluated by the same `TreeFunctions`. Its scripts see the inputs, and `children` and `parents` the object of the node as `$node` (renamed by `node-as`), while the scripts of the columns see the same object as the row, `$row` (renamed by `row-as`; see [which names a script sees](doc:view-layer/channels#which-names-a-script-sees)). The object `root` computes is not displayed; the objects `children` returns for it are the top-level rows, and the rows are the objects `children` returns - so a cell, the selection channel, a drag and a drop all see business objects, never nodes. A row's children are computed when it is opened (and to tell whether it has any).
- **Following the model** is that of a `<tree>`: the tree is held in a `DefaultTreeUINodeModel` kept in sync by the same `ObservableTreeModel` (through its `Display` seam), so a deleted object loses its row, an object that appeared in a child list gets one, the rows the user opened stay open, and an input naming another root builds the tree anew. An object of an `observed-types` type that is created or changed while the tree table has no row for it is placed through `parents`: the row of what holds it now has its children computed again when that row is open, and a closed row computes them when it is opened - so `observed-types` requires `parents` (see [a tree follows the model it displays](doc:view-layer/tables#selecting-rows-and-nodes)). The rows are keyed by their business object (`TreeStructure.key`), so expansion and selection survive a rebuild.
- **The table**: `types`, `<columns>` (whose scripts see the row as `$row`, or by the name `row-as` gives it), `fixed-columns`, `selection`, `selection-mode`, `<on-activate>`, `activation-button`, `filter-bar`, `<presets>`, `active-preset`, `search-term`, `<drag>` and `<export>` (the tree written as an outline, see [the export](doc:view-layer/tables#exporting-a-table-to-excel-export)) are those of a `<table>` (`AbstractTableElement.Config`), built by the same code (`AbstractTableElement.createTable`). Sorting orders siblings; a filter keeps the rows that match and their ancestors, all opened. A tree table has no `rows`, no `group-by` and no `row-edit`.
- **Selection** works as in a table (`TableSelectionBinding`): the channel holds the selected object, a set of them, or nothing, and an object written to it is selected - also one inside a row that is not open, which shows as selected once it is opened.
- The control is the `TableViewControl` in tree mode, fed by a `TreeRowSource`; the source is the table's `RowHierarchy` (`TableView.hierarchy()`): which row holds a row, and the children of a row in display order, whether it is open or not.

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

- **One set of tree rules.** `TreeDropPlace` (`com.top_logic.layout.react.control.dnd`) resolves a node and a zone into the location of each mode and the marker to draw, over a `DropTreeNavigation` - top-level nodes and their parent object, the parent of a node, its children in display order, whether it is expanded. `ReactTreeControl` implements the navigation over its tree model, `TableViewControl` over the `RowHierarchy` of its view; both resolve, probe, record and replay drops through `DropSupport`, and both check a replayed location with `TreeDropPlace.displays`, so a recorded insertion naming a `before` that is no child of its `parent` fails as a drift.
- **Zones on the client.** A table whose rows form a tree (it has a hierarchy and is not grouped) sends `dropTreeZones` (`TableViewControl.DROP_TREE_ZONES`), and `TLTableView.tsx` splits its rows with `treeZoneSplit` instead of `flatZoneSplit`: an `ordered` drop needs the upper, middle and lower third of a row.
- **Recording**: a drop is recorded as a `dropObjects` step naming the dragged objects, `parent` and `before` by their business identity, exactly as for a tree.
