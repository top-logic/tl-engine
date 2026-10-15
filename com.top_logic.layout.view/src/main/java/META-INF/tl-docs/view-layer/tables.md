---
description: Read before configuring a <table> (TableViewControl) or a <tree> (ReactTreeControl) in a .view.xml - column declarations, row and node selection, activation, grouping, CSS classes marking rows by their data (row-class on <table> and <composition-table>, tl-status-error / -warning / -success / -info), the filter bar and its presets, the Excel export (<export>), pinned columns, and drag and drop of rows and tree nodes (<drag>, <drop target="ordered"> with its arguments objects / target / parent / before, as / target-as / parent-as / before-as, refuse-if), and the name of the activated row or node (activate-as).
order: 60
---

# Tables

## `TableViewControl` is the sole React table control

`TableViewControl` / `com.top_logic.table.TableView` (#29108) is the only React table control. Everything renders through this stack: the `<table>` element (`TableElement`; sort, per-column `<filter>`, type-derived default columns, width personalization, shared `ColumnsConfig` / `ColumnConfig`), the access-control permission matrix (`SecurityMatrixElement`), the in-form `<composition-table>` (`CompositionTableControl`), and the technical React-table demo (`DemoReactTableComponent`: flat `ListRowSource` + `TreeRowSource` tree).

- Each `TableViewControl` cell is a registered child control, so interactive cell controls work. Cell rendering (`CellContentReactAdapter`): `CellContent.Text` / `Labeled` → text; `CellContent.Editable` backed by a *boolean* `FieldModel` → interactive `ReactCheckboxControl` (other field types read-only); **`CellContent.Raw` whose payload is a `CellControlFactory` → whatever control the factory builds** — the escape hatch for bespoke cells. The composition table uses it to put typed field inputs (`FieldControlService.createFieldControl(part, fieldModel)`) and detail / delete action buttons in cells; a column's `value(R)=row` plus a renderer returning `Raw(ctx -> buildControl(ctx, row, …))` gives the factory the row.
- **`TLPanel` renders a single `toolbar` child control (a `ReactToolbarControl`), not a `toolbarButtons` list.** Push a panel toolbar via `putState("toolbar", new ReactToolbarControl(ctx))` + `addGroup(name, ToolbarGroupDisplay.INLINE, …, List.of(button))`.
- **A column's display width**: `<column width="220"/>` on a `<table>` or a `<composition-table>` is the width in pixels a column is shown with until the user resizes it, from then on their own width is remembered; `0` (the default) keeps the width the column's type derives (`ColumnProviderService` config: boolean 80, number 100, date 110, time 90, date-time 160, enumeration 120, string and everything else 150, each retunable per application). The configured width is applied uniformly after the `ColumnBinding` built the column — `ColumnSetup.buildColumn()` wraps it through `DelegatingColumn.withDefaultWidth(…)`, the general `Column` decorator for overriding single aspects of a column — so no binding knows about widths.
- A table whose rows form a tree is a `<tree-table>`, see [Tree tables](doc:view-layer/tree-tables); it shares the configuration and the building code of a `<table>` through `AbstractTableElement`.
- Mutable rows: `ListRowSource.setElements(list)` then `TableViewControl.refreshData()`. `DefaultTableView.create(columns, source[, ViewStateStore, TableId])` — the 2-arg form skips personalization; pass a stable `TableId` to persist column width / order.

### Column declarations

The `<columns>` of a `<table>` (and of a `<composition-table>`) is a list of column declarations (`ColumnsConfig` / `ColumnDeclaration` in `com.top_logic.layout.view.table`), tag-dispatched and resolved in display order. A row is any object a TL-Script function yields — a model object, a transient one, a dictionary — so a declaration says what a cell holds rather than which attribute it reads. Four kinds:

- **`<column attribute="title"/>`** (`AttributeColumn`) — the attribute decides everything: display, sort, filter, search, width, header, and what an edited cell writes. An enumeration column sorts, and groups, in the enumeration's order — the declaration order of its classifiers; by label only for an enumeration annotated `<unordered-enum/>`. Its options filter offers the classifiers in declaration order.
- **`<computed-column name="total" type="tl.core:Double">`** (`ComputedColumn`) with `<value>row -> …</value>` — a value computed per row. The declared `type` (plus `multiple`) gives the column the same type-derived display, sort, filter and width an attribute column gets; a column declaring no type shows its values by their display label. `<update>row -> value -> …</update>` makes the column editable and `<can-update>row -> …</can-update>` decides per row whether the edit is offered; an edited column must declare its type, because the type picks the input control. `<aggregate>rows -> …</aggregate>` is what the column shows in a group header row.
- **`<embedded-columns reference="assignee">`** (`EmbeddedColumns`) — the columns of an object the row points to, shown as columns of the row. `reference` is a reference name or a dotted path (`milestone.tickets`), and a step holding several objects makes every embedded column collect the values of all of them; alternatively `<object>row -> …</object>` plus `name`, `type` and `<label>` computes the embedded object. Nested declarations are of any kind and resolve against the embedded type; an embedding declaring nothing shows that type's main properties. Names are prefixed with the path (`assignee.name`) and labels with the reference labels (`Assignee / Name`, `I18NConstants.EMBEDDED_COLUMN_LABEL__PREFIX_COLUMN`), so a column of the row and one of the embedded object stay apart in the header and in the column selection. Embedded columns are read-only — an embedded object is changed where that object itself is edited.
- **`<dynamic-columns name="milestones" type="tl.core:Integer">`** (`DynamicColumns`) — one column per element of the set `<columns>…</columns>` computes, for a table of tasks with a column per milestone. `<value>row -> col -> …</value>` fills a cell from the row and the object its column stands for; `<column-name>` and `<column-label>` name and label a column (default: the object's display label), `<column-type>` computes the type per column where `type` does not fit all of them, and `<update>` / `<can-update>` / `<aggregate>` take the column object as an extra argument. A column's name is the declaration name, a dot and the column object's name, so a user's arrangement of it survives; the filter is the one the type derives, as there is no single column to declare one for. The columns are those the function yields when the table is built.

Further rules that hold across the kinds:

- **Input channels lead.** Every declaration may carry `<inputs><input channel="…"/></inputs>`; the channel values become the leading positional arguments of *all* of its functions, ahead of the row — `<value>factor -> row -> …</value>`. A `<computed-column>`'s `<aggregate>` and `<comparator>` are the exception: they take the group's rows, respectively the two compared values, alone.
- **A column may order its values itself.** `<column>`, `<computed-column>` and `<dynamic-columns>` take a `<comparator>`: a function of two cell values yielding a negative number, zero or a positive number — the contract of the comparator of TL-Script's `sort` function, so an order by a key is written with the `comparator(key)` sugar: `` <comparator>comparator(p -> $p.get(`m:Page#position`))</comparator> ``. The column sorts the rows by it and orders the groups it forms by it, instead of the order the type of its values derives (`ColumnSetup#comparator()`, applied as the column's `Column#sort()`). The function is never passed an empty value; rows without a value are ordered apart from it, a group without a value comes last. In `<dynamic-columns>` the input values precede the two compared values, and every column of the set shares the function. An embedded column keeps the comparator of the column it embeds; one collecting the values of several objects orders those collections element by element by the same comparator (`ColumnSetup.elementwise`).
- **Default columns.** A table without `<columns>` shows the main properties of its row type (`<main-properties properties="name, dueDate"/>` on the type), and all non-hidden attributes of that type when it names none.
- **Offered columns.** Whatever the declared columns do not cover, the table offers in its column selection, hidden until the user picks it — the remaining attributes of the configured `types`, and the remaining attributes of an embedded type under their prefixed labels. Only an explicitly configured `types` produces this offering; a type guessed from the first row would vary with the data.
- **Columns hidden at the start.** `visible="false"` on a `<column>` or `<computed-column>` (also inside `<embedded-columns>`) offers the column in the column selection only: the table starts out without it, and the user shows it from there. A `<dynamic-columns>` decides this per column object with `<column-visible>inputs… -> col -> …</column-visible>`, a boolean function (unset, or a `null` result, shows the column). A user's own arrangement of the table, once stored, takes precedence.
- **A row need not be a model object.** A table whose rows are dictionaries declares no `types` at all and computes every column (`$row["name"]`); transient objects are displayed by the same attribute columns persistent ones are, and are skipped when the table observes its row types for changes.
- `com.top_logic.demo.react` shows all of this under the *Table columns* nav item (`WEB-INF/views/demo/table-columns.view.xml`), one tab per kind.

## Selecting rows and nodes

A `<table>` and a `<tree>` write what the user selects to the channel named by `selection`, and `selection-mode` says how much may be selected at a time — `single` (the default) or `multi`:

```xml
<table selection="selected" selection-mode="multi" types="demo.react:Demo">…</table>
<tree selection="selected" selection-mode="multi">…</tree>
```

- **A `single` table** replaces the selection with every click, and a click with `Ctrl` on the selected row gives it up again. **A `multi` table** puts a checkbox in front of every row and one in the header selecting and deselecting all of them; a click with `Ctrl` adds a row to the selection or takes it out again, a click with `Shift` selects the range from the row selected last, and `Ctrl+A` selects every row.
- **In a `multi` tree** a plain click still replaces the selection, a click with `Ctrl` adds a node or takes it out again, and a click with `Shift` selects the range from the node the selection started at; the keyboard gestures are described under [Row activation](#row-activation), where the cursor is.
- **The channel holds the selection, never a wrapper around it**: the selected object while exactly one row or node is selected, the `Set` of the selected objects while there are several, and `null` while there is none. A display or a command bound to the channel therefore works with either mode, and only one that is to show or process several objects at once has to expect a set. A tree writes the *business objects* of the selected nodes, not the nodes (`TreeSelectionBinding`).
- **A table also reads its channel** (`TableSelectionBinding`), and how it answers a collection depends on its mode: a `multi` table selects the rows it has for those objects, a `single` table cannot display such a value at all and shows no selection. Either way a value the table has no row for is "nothing selected here" and is **left alone** — clearing it would destroy what another writer put there, the row a second table over a different row set selected or the object a create command wrote before this table's rows caught up.
- **A tree reads its channel too** (`TreeSelectionBinding`): the node of an object another writer puts on it is looked for, the subtrees above it are opened so that it is visible, and it becomes the selection. A collection is answered by mode as in a table — a `multi` tree selects the nodes it has for those objects, a `single` tree cannot display such a value and shows no selection — and an object the tree has no node for is "nothing selected here": the channel **and** the tree's own selection are left alone, since that object may well get a node in a moment. A selected node that vanishes - its object deleted, or taken out of the child list it hung in - hands its place to the node it hung in. The channel announcing the deletion of a selected object (it drops the object, see [deleted objects on a channel](doc:view-layer/channels#deleted-objects-on-a-channel)) does not clear the tree's selection; the tree answers when its node is removed: where nothing of the selection survives, that node is revealed and selected and its object written to the channel (the nearest node above it the tree still holds, where its parent is gone too; the root only where the tree displays it, otherwise the channel is cleared). Surviving nodes of a `multi` selection simply stay the selection. Finding the node means searching the tree, which computes the child list of every node it passes; a large or unbounded tree therefore declares `parents` — `<tree parents="input -> node -> $node.get(\`my:Type#parent\`)">` — and the tree walks from the object up to its root and descends along that chain instead, computing only the child lists on the way.
- **A tree follows the model it displays** (`ObservableTreeModel`, the tree's counterpart of the row observation a `<table>` does over its `observed-types`): a change reconciles the child lists in place, so a node whose object is still there is the node the display was working with and the subtrees the user opened stay open, a deleted object loses its node, and an object that appeared gets one. A selected node that loses its place this way makes the node it hung in the selection, as described above, so the user stays where the change happened instead of on an empty detail. An object of an `observed-types` type that is created or changed while the tree has no node for it is placed through the tree's `parents` expression: the node of what holds it now has its child list reconciled when that node is open, and a closed node computes it when it is opened — so `observed-types` requires `parents`, and the tree is never reconciled as a whole. The selection is re-applied after every such change, which is what a create command needs: it writes the new object to the selection channel before the tree has a node for it, and the object is revealed and selected as soon as the node exists. An input naming another root builds the tree anew and opens the subtrees that were open again.
- **A command working on one object binds to a derived channel rather than to the selection**, since the selection may be several: `<derived-channel name="selectedSingle" inputs="selected" expr="sel -> if($sel.size() == 1, $sel.singleElement(), null)"/>` is the selection while it consists of exactly one object and nothing otherwise — `size()` counts nothing as zero, a single object as one and a set as the number of its elements. With `<null-input-disabled/>` that is the whole of "enabled for one selected object". A command working on the whole selection needs nothing: `delete()` and the other collection-valued script functions accept a single object as well as a set of them.
- Demos in `com.top_logic.demo.react`: the *Attributes* table (`views/attributes.view.xml`) selects several rows — Delete works on all of them, Edit on the single selection through such a derived channel — and *Tree Demo* (`views/demo/tree-demo.view.xml`) shows the selected nodes and the activated one side by side, and creates, detaches and deletes nodes in place: a milestone created through a dialog and a ticket created without one are revealed and selected as soon as their node exists, detaching a ticket from its milestone takes the node out of the child list without deleting anything, attaching a free ticket to the selected milestone places the ticket that had no node under that open milestone and selects it, and a delete works on the whole selection - in each case the opened subtrees stay open.

## Row activation

A `<table>` and a `<tree>` open a row / node on a **double-click**, and on **Enter** while the row or node carries the keyboard cursor. The gesture is one command, `activate`, carrying the row index (the node id):

```xml
<table selection="selected" types="demo.react:Demo">
	<columns>…</columns>
	<rows>all(`demo.react:Demo`)</rows>
	<on-activate class="com.top_logic.layout.view.command.GenericViewCommand">
		<open-dialog bind-input-to="model" dialog-view="attributes-detail.view.xml"/>
	</on-activate>
</table>
```

- `<on-activate>` holds an ordinary `ViewCommand` configuration — the same thing `<button><action>` holds, hence the `class=` form. The server selects the activated row first (so the table's `selection` channel holds it), then runs the command **with that row as its main argument**, named `row` in a table and a tree table and `node` in a tree; `activate-as="ticket"` on the element names it otherwise (see [named arguments](doc:view-layer/commands#a-command-is-a-chain-of-actions-over-named-arguments)). The command's own `inputs` are available beside it, and its `<executability>` rules decide over the row, so a row the rules reject activates nothing. An action processing one value — the `<open-dialog>` above — takes the row by default; a script names it: ``<execute-script function="$row.get(`demo.react:Demo#name`)">``.
- The element instantiates the configured command in its constructor (with the element's `InstantiationContext`, so a bad configuration is reported at load time) and keeps command plus config, exactly as `ButtonElement` does. `ViewCommandModel.forCommand(context, command, config, trigger)` then builds the model for it — resolve the input channels, take the name of the trigger's value (`trigger`, the `activate-as` name), build the executability rule, pick the matching model — and is the single construction path shared by `ButtonElement`, `TableElement`, `TreeElement`, `CommandCarrierElement` (panels, menus) and `AppBarElement`. `ViewCommandModel.execute(context, input)` runs the command with an input the caller supplies instead of the channel value; that is the one path for "run this configured command with this value".
- The control-level seam is `TableViewControl.setActivationHandler(…)` / `ReactTreeControl.setActivationHandler(…)` — one handler, called with the row business object (the tree node) after the row became the selection, returning the `HandlerResult` the gesture reports. Without a handler, activating a row only selects it.
- An activation is recorded for scripted tests the way a selection is: the index-based `activate` becomes an `activateByKey` naming the row's business object, so the step survives sorting, filtering and a fresh session.
- A double-click inside an interactive cell element (a text input of an editable cell) belongs to that element and opens nothing; Enter is likewise declined while the focus sits in a cell input or action button.
- **In a `<tree>` the selection follows the keyboard cursor.** A single-selection tree selects the node that the up/down arrows, `Home` and `End` move the cursor onto; a multi-selection tree moves the cursor alone, grows the selected range from its anchor with `Shift` and an arrow, and adds the cursor node to the selection or takes it out again with `Space`. The right and left arrows expand and collapse the cursor node, stepping to its first child and back to its parent where there is nothing to open or close, and `Enter` activates it in either mode.
- **A table with an `<on-activate>` offers the same command as a button on every row**, in a pinned column at the right edge — the gestures are invisible, and a touch device has neither of them. Each button is icon-only (the command's `<image>`, else a chevron), takes the command's label as its tooltip, and follows the command's executability *for its own row*: a row the rules reject shows a disabled button, a row they hide shows an empty cell. Pressing the button activates its row exactly like the gesture: the row becomes the selection first, then the command runs — so a command reading the selection channel (a `<frame-label inputs="…">` breadcrumb, a `<bind>`, an `<execute-script><inputs>`) sees that row. `<table activation-button="false">` opts out, for a table whose rows are opened another way.
- The column itself is general, not an activation column: `RowCommandColumn.columns(List.of(command))` builds one pinned column per `RowCommand` a caller wants to offer per row. A `RowCommand` pairs a `ViewCommandModel` — whose `executability(row)`, icon and label decide the button — with the action a press runs for the row; `RowCommand.of(context, model, image, label)` makes the action run the model with the row as input. `TableElement` feeds the activation command into it with `TableViewControl.activate(row)` (`RowSetTableControl.activate(row)` for the editable variant) as the action — one model serving both the gesture and the buttons — and `RowSetTableControl.setTrailingColumns(…)` is where the editable variant appends them, behind the columns it builds itself.
- Demos in `com.top_logic.demo.react`: the *Attributes* table opens the object's detail dialog, *Tree Demo* (`demo/tree-demo.view.xml`) writes the activated node to a channel displayed next to the selected one, and the *Tiles Demo* account tables (`demo/tiles-demo/overview.view.xml`, `demo/tiles-demo/person-detail.view.xml`) carry an `<on-activate class="…NavigatePushCommand">` that drills into the activated person.

## Grouping

A `<table>` buckets its rows by the value of one column: one collapsible header row per value, showing that value, how many rows it holds, and what every other column aggregates over them. `<table group-by="status">` is the grouping a table *starts* with; the user regroups it from a column header's context menu ("Group by this column" / "Remove grouping") or from the column selection (the group icon on each row of the dialog, applied together with the columns), and that choice is kept under the table's personalization key exactly like the sort order.

- **The model does the grouping** (`com.top_logic.table`): `TableView.group(GroupSpec)` sets `TableViewState.getGrouping()`, persists it and hands it to `RowSource.withGrouping`. `ListRowSource` filters, then sorts, then buckets — so a filter decides what a group holds and the sort decides the order *within* it — and emits a `GroupRow` (`RowKind.GROUP_HEADER`, `expandable()`, `depth() == 0`, keyed by its `GroupKey`) followed by its members at `depth() == 1`. The groups themselves are ordered by the grouping column's own comparator (`Column#sort()`, a `null` group value last) — the `<comparator>` the column declares, else the one the type of its values derives; a grouping column that cannot sort keeps its groups in the order their first rows appear in. The direction of the group order is that of the grouping column's own sort, else that of the sort of the first displayed column (the one showing the group values in the header rows), else ascending: `DefaultTableView` derives this by putting the grouping column in front of the order it hands to the row source, while `TableViewState.getSort()` keeps the user's sort. `DefaultTableView.cell(row, column)` renders the group value in the first column and the column's `Aggregator` result in the others.
- **The UI follows that representation**: `TableViewControl` pushes the group rows through the same `treeMode` / `treeDepth` / `expandable` / `expanded` state the tree table already uses, plus a `groupCount` present exactly on a group header, so collapsing a group *is* collapsing a node (`expand` command) and needs no second mechanism. A table becomes `treeMode` while it is grouped, whatever it was built as.
- **A group header stands for no object**: the gesture that would select it (a click, or `Enter`) collapses or expands it instead, the selection channel never receives a group, and range selection, select-all and keyboard navigation only ever collect `RowKind.DATA` rows. Changing the grouping clears the selection, since the rows it named are gone.
- **One column at a time** — `ListRowSource` rejects a multi-column `GroupSpec`, and the UI offers a single column accordingly.
- Grouping is offered for the columns the user may choose at all (the `columnOptions()`, i.e. the selectable ones): an action column carries the row itself and would yield one group per row. A tree table (`TableViewControl` built in `treeMode` over a `TreeRowSource`) offers no grouping at all: its rows are structured by their nodes already.
- Demo: *Object list* (`tickets.view.xml`) groups its first ticket list by status and leaves the second flat; *Attributes → Table* starts ungrouped and is grouped from the header menu.

## Row classes: marking rows by their data

`row-class` gives each row a CSS class computed from the row's object — the way to tint the failed jobs of a list red or mark the overdue tickets. It is the same property on `<table>` and on `<composition-table>`:

```xml
<table group-by="status" observed-types="demo.tickets:Ticket">
	<inputs>
		<input channel="state"/>
	</inputs>
	<rows>state -> …</rows>
	<row-class>state -> row -> $row.get(`demo.tickets:Ticket#status`) == `demo.tickets:TicketStatus#inProgress` ? 'tl-status-info' : null</row-class>
</table>

<composition-table attribute="parts"
	row-class="row -> $row.get(`demo.react:DemoPart#amount`) > 100 ? 'tl-status-warning' : null"
/>
```

- **Arguments follow the convention of every table function**: the values of the `<table>`'s declared `<inputs>` first, in declaration order, the row object last. A `<composition-table>` has no inputs, so there the row object is the only argument.
- **The result becomes the class**: `null` or an empty string gives the row none, a list gives it all of its members (joined with spaces), any other value its text — the conversion the classic table's `RowClassProviderByExpression` applies, `CellClassProviderByExpression.toCssClass(Object)`.
- **Four standard classes come with the theme**: `tl-status-error`, `tl-status-warning`, `tl-status-success` and `tl-status-info` tint a row (or a single cell) with the subtle background of the matching status token, frozen and pinned cells included. A selected row shows its selection instead. Any other class is the application's to style in its own stylesheet.
- **Only data rows get a class**: a group header and an aggregate row stand for no object and carry none.
- **The class follows the row's data**: it is evaluated whenever the control pushes a row to the client, so it is computed again with every push of the rows — after a change of an object of the `observed-types`, after an input channel changed, and when a `<composition-table>` or a row-editing `<table>` switches between view and edit mode (on save, say) or a row is added or removed.
- **The Java seam** (`com.top_logic.table`): `TableView#rowCssClass(Row)` answers the class of a row and `TableView#cellCssClass(Row, String)` that of one cell; `TableViewControl` sends both, and the client puts them on the row and cell elements. `DefaultTableView#setRowCssClass(Function)` installs a function over the row object (applied to data rows only), and a column's per-cell class is `Column#cssClass(row)`, set with `DefaultColumn.Builder#css(Function)`. `RowClassConfig` is the configuration mixin both elements extend; `RowClassFunction` compiles the function once per element and binds it to the input channels of each table built — `TableElement` installs it on its `DefaultTableView`, the row-editing `<table row-edit="…">` and `<composition-table>` through `RowSetTableControl#setRowCssClass(Function)`, which applies it to every table it (re-)builds on an edit-mode switch.
- Demos in `com.top_logic.demo.react`: *Object list* (`tickets.view.xml`) tints the tickets in progress of its Open list; *Attributes* (`attributes.view.xml`) marks the parts of an object with an amount above 100 in its *Parts* composition table.

## Filtering: the filter bar and its presets

`<table filter-bar="true">` puts a bar above the table: the named filters it offers as chips, a search field examining the displayed columns, and saving the criteria the table currently shows under a name of the user's own. A table declaring `<presets>` shows the bar in any case — that is where the presets are offered.

```xml
<view>
    <channels>
        <channel name="activeFilter"/>
        <channel name="searchTerm"/>
    </channels>
    <query-bindings>
        <bind channel="activeFilter" query-param="filter"/>
        <bind channel="searchTerm" query-param="q"/>
    </query-bindings>
    <panel>
        <fields>
            <value-input type="tl.core:String" value="searchTerm">
                <label><en>Search</en></label>
            </value-input>
        </fields>
        <table active-preset="activeFilter" search-term="searchTerm"
            personalization-key="demo-tickets-open" types="demo.tickets:Ticket"
        >
            <presets initial="mine">
                <preset name="mine">
                    <label><en>Commented by me</en></label>
                    <criterion column="commentedByMe" expr="true"/>
                </preset>
                <preset name="discussed">
                    <label><en>Discussed</en></label>
                    <criterion column="comments"><range operator="GT" primary="0"/></criterion>
                </preset>
            </presets>
            <columns>…</columns>
            <rows>all(`demo.tickets:Ticket`)</rows>
        </table>
    </panel>
</view>
```

Such a page is linkable: `/view/tickets?filter=discussed` opens the list filtered by that preset, `?q=login` opens it searched for that text, and `?filter=discussed&q=login` opens it filtered by the preset *and* searched within it — the two parameters describe the displayed rows together, so every address the page leaves behind opens the rows it was taken from.

- **What the bar filters by.** The search is a plain case-insensitive substring over the **displayed** columns (`TableView.search`, `SearchSpec` over `TableViewState.getColumnOrder()`), so it finds what the user sees — a column they hide is no longer searched, one they show is — and it narrows the rows in addition to the per-column `<filter>`s. The matching flags of a column's own text filter stay that column's business.
- **`<presets>`** is a list of `<preset name="mine">` entries, each with a `<label>` and one `<criterion>` per column. `name` is the technical identity the user's choice is remembered — and linked — under, so it must not change once the table is in use. A criterion says what it selects in one of two ways, never both: `expr="currentUser()"` names a **value** the column's own filter translates (a text filter matches its text, a selection filter takes one value or a list of them, a boolean filter `true` / `false`, a range filter a single value as an exact match or a list of two as inclusive bounds), or it is written in the **form of that filter** — `<text pattern="'^Value [135]$'" regexp="true" case-sensitive="true" whole-field="true"/>`, `<range operator="GT" primary="0"/>`, `<options selected="sel -> $sel.get(\`demo.react:Demo#priority\`)"/>`, `<boolean accept="true"/>` — which is how a criterion says what a single value cannot. `inverted="true"` makes the column accept exactly the rows the criterion does not select.
- **Criteria follow the inputs.** Every value expression is evaluated with the table's `<inputs>` as its arguments, the same ones the rows are computed from, and re-evaluated whenever one of them changes (`TableElement` then calls `DefaultTableView.setDeclaredFilters`) — so `currentUser()` yields a preset that means something different to every user, and an expression over an input yields one that follows what is displayed elsewhere. A chip the user has applied goes on being the active one and filters by what it now means.
- **A criterion that does not materialize** — an unknown column, a column that cannot be filtered, a form of another kind than the column's filter, a value the filter cannot express, a selected option the column does not offer — is a declaration error (`DeclaredFilters`): it is reported and *the whole preset* is not offered, rather than offered filtering by less than its name says. A criterion selecting **nothing** is no error but no criterion either (an input nothing is selected in leaves its column unfiltered); a preset whose every criterion selects nothing is withheld and offered again as soon as its inputs select something.
- **`initial="mine"`** on the `<presets>` container names the preset the table starts out filtered by. It is part of the table's *initial state*, like the sort order and the grouping, so it takes effect only as long as no personalization is stored for this table (`DefaultTableView.restore()`): a user who applied other criteria keeps them, one who cleared the filter keeps the table unfiltered — and so does one who merely resized a column, since the persisted state replaces the initial one as a whole. Applying it persists nothing itself, so it is what every fresh session of an unpersonalized table shows. A name none of the declared presets carries is a configuration error at startup; a preset that is withheld at runtime leaves the table unfiltered instead of failing. A table offering presets should carry a `personalization-key`, so that the choices made about it survive an edit of its columns.
- **`active-preset="ch"`** publishes the `NamedFilter.id()` of the named filter the table matches, and nothing while it matches none. That is a declared preset's `name` as well as the generated identifier (a UUID) of a filter the user saved on the bar, so a saved filter is linkable exactly like a preset. Which one is active is not a flag the table keeps but a comparison of its live criteria with the offered filters (`TableView.activeNamedFilter()`), so the channel follows every filter the user applies, edits or clears — from the bar, from a column's own filter dialog, or from the channel — and it follows a change of the filters the table *offers* as well.
- **`search-term="ch"`** publishes the text the table searches for, and nothing while it searches for none. The search narrows the rows *within* whatever the table is filtered by and says nothing about that filtering, so a preset the table matches stays the active one while the user searches — the chip keeps its mark, and `filter` and `q` stand in the address side by side.
- **Both are two-way** (`TableFilterBinding`). An identifier written to `active-preset` filters the table by that filter; a text written to `search-term` is searched for. What no offered filter carries leaves the table unfiltered, and the state the table ends up in is written back — so a stale link, a mistyped identifier, or a preset that has since been removed corrects itself instead of describing something nobody sees. At creation a channel holding nothing takes the table's state rather than unfiltering it (the `initial` preset survives a page that binds the channel), while a channel that does hold a value describes the table someone asked for and is applied.
- **A write to either channel applies both.** Every writer sets one channel at a time — `QueryBindingParticipant` writes one bound parameter after the other, in the order the `<bind>` elements are declared in — and applying a named filter replaces the whole filtering, the search term included. The binding therefore brings the table to what *both* channels say on every write: the preset first, then the term. That is order-independent and repeatable, so `?filter=mine&q=Export` opens the same rows whichever parameter is bound first; applying only the channel that changed would let the term be wiped by a preset arriving after it. Once the table and the channels are together, nothing on `active-preset` means "by no named filter" and leaves the term in effect, and nothing on `search-term` means "searched for nothing" and leaves the preset applied. A filter the user saved *with* a term is named by the two channels together, which is exactly what the binding publishes when it is applied.
- **Which term is compared** (`NamedFilter.matches`): a named filter's search term takes part in the match only when the filter *defines* one. A `<preset>` never does — `DeclaredFilters` builds every declaration with no term — so it matches on its column criteria alone and survives a search. A filter the *user* saved on the bar captures the text that was searched when they saved it, and therefore matches only while exactly that text is searched for again: the term is one of the things it was saved as. Both can match at once — saving "the preset, searched for X" yields a filter whose columns are the preset's — and then the one naming the term wins, because it describes the displayed rows completely while the preset describes only their columns; among equally specific matches the offered order decides, so a preset still wins over a saved filter with the same criteria and no term. The other way round, picking a preset replaces the whole filtering, the search term included — which for a preset means the search field is cleared. A preset declaring no criterion at all (the "All" chip) matches exactly while no column is filtered, whether or not a search is running.
- **The binding follows the `TableView`, not the bar**, so both channels work for a table that displays no bar at all: an input elsewhere — a `<value-input value="searchTerm" type="tl.core:String"/>` — searches it, and several tables bound to one search channel are searched together.
- Demo: *Object list* (`views/tickets.view.xml` in `com.top_logic.demo.react`) — the Open list declares three presets over its computed `comments` / `commentedByMe` columns, opens filtered by `initial="mine"`, and binds both channels to the query parameters `filter` and `q`; the Closed list beside it shows no bar and is searched through the same channel. *Attributes → Table* (`views/attributes.view.xml`) shows the criterion forms: a value, a case-sensitive regular expression, an inverted range, and a selection following the table's own selection channel.

## Exporting a table to Excel: `<export>`

`<table><export/></table>` offers "Export to Excel" in the toolbar of the element the table is displayed in - the panel around it - as a command of the `export` clique, so it sits in the *Export* menu - first, ahead of whatever else the panel exports (`CommandScope.addCommandFirst`). A table without `<export>` offers no export; a table outside an element with a toolbar has nowhere to offer it (logged at info level).

```xml
<panel>
  <table types="demo.react:Demo" filter-bar="true">
    <columns>
      <column attribute="name"/>
      <column attribute="amount">
        <export-renderer class="com.top_logic.tool.export.FormattedValueExcelRenderer" format="#,##0.00 &quot;EUR&quot;"/>
      </column>
      <column attribute="email" export="false"/>
    </columns>
    <rows>all(`demo.react:Demo`)</rows>
    <export download-name="#('Demo objects'@en, 'Demo-Objekte'@de)">
      <sheet-name><en>Demo objects</en><de>Demo-Objekte</de></sheet-name>
    </export>
  </table>
</panel>
```

- **What is exported is what the user sees** (`TableViewExcelExport`, `com.top_logic.tool.export.tableview`): the displayed columns in their display order and the displayed rows - filtered, searched (the filter bar's term and its presets included) and sorted as the table is. Columns, rows and every cell value are read from the `TableView` when the command runs, in the request (`snapshot`), so a background export touches no row object - no row being edited, no overlay - and changing the table while it runs does not change the file.
- **Columns that are no data are never exported, by identity**, whatever their position: a column that is not `Column#exportable()` - by default every column that is not `selectable()`, i.e. the per-row actions (activation button, row commands, the detail and delete buttons of a row-edit table) - and every column declared `export="false"`. The selection checkbox and the drag handle are no columns at all.
- **A cell is written from the column's typed value** by an `ExcelCellRenderer` - the classic table export's renderers, run without a classic `TableModel` (`RenderContext.model()` / `modelColumn()` are `null`, `resourceProvider()` labels collection elements). The renderer is the declaration's `<export-renderer>`, otherwise the one of the value type in the order the classic generic columns use (`ColumnExport.rendererFor`): a `<column-info>` annotation's `excelRenderer`, a format pattern annotated to a primitive (`FormattedValueExcelRenderer`), the renderer `LabelProviderService` registers for the type, and the default, which keeps numbers, dates and booleans typed and writes everything else as its label. `export="false"` and `<export-renderer>` are available on every column kind (`ColumnExportConfig`); embedded columns take them from their nested declarations, `<dynamic-columns>` for all of its columns.
- **Grouping and trees become an outline of the sheet**: a group header is written as a bold row "value (count)" with the column aggregates beside it, its members grouped below it at outline level 1, and a tree node at the level of its depth. Nothing is left out for being collapsed: the rows a collapsed group or tree node hides (`TableView.allRows()`) are written as rows the outline hides, the collapsed row marked as such. (A tree whose node set cannot be enumerated - `TreeStructure.isFinite()` false - exports its displayed nodes.) The header row is frozen together with the table's fixed columns (`freeze-header`) and offers Excel's filter (`auto-filter`).
- **Options**: `download-name` (a TL-Script function over the table's `inputs`, like `<rows>`; default "date Export data (application)", the classic default), `sheet-name`, `template` (a workbook in `WEB-INF/reportTemplates/excel`, filled into the named or first sheet; never streamed; it decides the format),  `autofit-columns` (otherwise the displayed column widths), `streaming` (SXSSF, constant memory, no rich text in cells), `progress-threshold` (default 1000) plus everything a view command has - `label`, `image`, `clique`, `executability`, ... The format comes from one source - the template where there is one, otherwise the download name (`.xls` for the legacy format) - and the file name is made to end in its extension, ignoring case (`POIUtil.withExcelExtension`). The config is typed, so the in-app view editor offers all of it. The export exists only as the `<export>` of a `<table>`: configured as a command of its own, it is a configuration error.
- **Long exports run in the background**: from `progress-threshold` rows on, the file is written on a worker thread by a `JobRunner`, while a dialog shows a `ReactJobStatusControl` with the row count and a cancel button; the dialog closes and the browser saves the file once it is written, a failure stays visible in the dialog. Smaller tables are exported in the request.
- **The row-edit table** (`row-edit` inside a `<form>`, the successor of the classic `GridComponent`) offers the same export, in view and in edit mode. In edit mode the rows are the form's `TLObjectOverlay`s, so the file holds the values as currently edited, saved or not - what the classic grid exported from its form fields.
- **Delivering the file** goes through `SSEUpdateQueue.deliverDownload(BinaryData)`: the window keeps the file under a random key and sends a `DownloadEvent`, on which the client (`bridge/download.ts`) fetches `react-api/download` - once - as an attachment (`ContentDisposition`). A file the client does not fetch is given up after `DOWNLOAD_TIMEOUT` (5 minutes), and the oldest beyond `MAX_PENDING_DOWNLOADS` (10) per window; `deliverDownload(data, discard)` releases it then, or once it is sent - the export deletes its temporary file that way. Every command that produces a file for the user uses this path.

`<download/>` (`DownloadAction`) is the same delivery as an action of a command chain: it hands the value it processes - by default the result of the enclosing action, see [`value`](doc:view-layer/commands#actions-processing-one-value-value); a `BinaryData`, as `excelFile(...)`, a document template or a file attribute yields it - to the browser. `file-name` computes another name, a script over the arguments of the chain and the action's `inputs`; no file at all, or a value that is no file, ends the command with a message instead of doing nothing.

```xml
<generic-command clique="export" placement="TOOLBAR">
  <label><en>Priority summary (Excel)</en></label>
  <execute-script function="excelFile(name: 'summary', content: [excelSheet(content: [['Priority', 'Objects']])])">
    <download/>
  </execute-script>
</generic-command>
```

Demo: *Attributes* → *Table* (export with an export renderer and two suppressed columns, followed by a script-built "Priority summary (Excel)" report) and *Attributes* → *Editable table* (`com.top_logic.demo.react`, `views/attributes.view.xml`).

## Pinned columns

`Column.pinnedEnd()` keeps a column at the end of the table: it is rendered behind every other column and stays fixed to the right edge while the table scrolls horizontally — the place for what acts on a row (the per-row buttons) rather than for what the row *is*. `DefaultColumn.builder(name, value).pinnedEnd(true)` is how a column author asks for it; there is no `.view.xml` attribute.

- A pinned column is the table's own, not part of the arrangement the user makes: it is neither `frozenEligible()` nor `selectable()` (`DefaultColumn` enforces both, whatever they are set to), so the column selection does not offer it, `setColumnOrder` / `moveColumn` leave it where it is, `resizeColumn` and `setColumnVisible` decline it, the frozen prefix counts only the columns in front of it, and the rows cannot be grouped by it.
- `DefaultTableView` normalizes the column order whenever it is set — by the caller, by a restored personalization, by a column selection — so the pinned columns trail it. Every consumer sees that one order: `columns()` lists them last with `ColumnView.pinnedEnd()` set, and `frozenColumnCount()` never reaches into them.
- `TableViewControl` pushes the flag as the per-column `pinnedEnd` state. The client (`TLTableView.tsx`) renders such a cell `position: sticky` with a `right` offset of the widths of the pinned columns behind it plus the reserve the row ends with — in the header the measured scrollbar width, which the header has no vertical scrollbar of its own for, and where the table does not end in a pinned column the column button's width in header and body alike. A table ending in a pinned column has a heading without a label there, so the column selector's cog sits in that heading and the pinned column reaches the right edge. Rows and header row fill the table when the columns are narrower than it; the last *unpinned* column grows into the space left over, in the heading exactly as in the rows. A pinned cell keeps its width, offers no resize handle and no drag, and the header menu offers it neither the freeze boundary, nor a grouping, nor the fit to content.
- `Column.cssClass()` (`DefaultColumn.Builder.cssClass(String)`) names a class the client puts on every cell of the column, its heading included — how the column presents its cells, as opposed to the per-row `cssClass(R row)`. `RowCommandColumn` uses it to drop the text padding and center its frameless button.
- Every unpinned column can be fitted to its content: "Fit width to content" in the header menu, or a double-click on the column's resize handle, sets the width the heading and the rendered cells need and persists it through the same `columnResize` command a drag ends with. Only the rows currently in the DOM are measured, so the fit follows what is displayed, as the virtual scroller renders it.

## Drag and drop of table rows

A `<table>` declares that its rows may be dragged, and what it accepts a drop of. Both are declarations of the table, so a drag between two tables needs no code on either side:

```xml
<table rows="…" selection="ticket" types="demo.tickets:Ticket">
    <drag kind="ticket"/>
    <drop accept="ticket">
        <with-transaction>
            <execute-script function="$objects.foreach(t -> $t.set(`demo.tickets:Ticket#status`, `demo.tickets:TicketStatus#closed`))"/>
        </with-transaction>
    </drop>
</table>
<table rows="all(`tl.accounts:Person`)" types="tl.accounts:Person">
    <drop accept="ticket" target="row" as="tickets" target-as="person">
        <with-transaction>
            <execute-script function="$tickets.foreach(t -> … $person …)"/>
        </with-transaction>
    </drop>
</table>
```

- **`<drag/>`** makes the rows draggable. Dragging a selected row drags the whole selection, an unselected row drags itself — and the selection is read on the server, so a selection reaching beyond the rendered row window is dragged completely. `kind` classifies the drag with a free name the application chooses (`ticket`, `assignment`, …); without it, the drag has no kind.
- **`<drop>`** is a list, so a table can accept several kinds of drag, and accept one kind on its rows and another as a whole. `accept` lists the kinds it takes, comma-separated (`accept="ticket, milestone"`); a drag without a kind or of an unlisted kind is refused. Without `accept`, the drop takes every drag, of any kind or none. Kinds are compared literally, so a drag and the drops meant to take it agree on the spelling. What a drop takes beyond the kind — only objects of a certain model type, say — is decided by its `refuse-if`, e.g. ``refuse-if="$objects.filter(o -> !$o.instanceOf(`demo.tickets:Ticket`)).size() > 0"``. `target` is `table` (default), `row` or `ordered`, and decides which objects the drop refers to at its place — its *references*.

  **The arguments of a drop** are the list of dragged objects, named `objects` (renamed with `as`) and its main argument, followed by its references, each named after the reference (renamed with `target-as`, `before-as`; see [named arguments](doc:view-layer/commands#a-command-is-a-chain-of-actions-over-named-arguments)). `refuse-if` and the action chain see the same arguments, as variables without declaring them (`DropArguments`):

  | `target` | applies | arguments | renamed by |
  |---|---|---|---|
  | `table` | anywhere on the table | `objects`, `target` (always `null`) | `as`, `target-as` |
  | `row` | onto the row under the pointer | `objects`, `target`: the row dropped on | `as`, `target-as` |
  | `ordered` | between two rows (insertion line); beside the rows appends | `objects`, `before`: the row to insert before, `null` at the end | `as`, `before-as` |

  `refuse-if` computes why the drop must not be made: no value or `false` accepts the drop, `true` refuses it with a generic reason, a resource key or a text refuses it with that reason — the interpretation of `<disabled-if>`. A name for a reference the drop's target does not have — `before-as` on a `table` or `row` drop, `target-as` on an `ordered` drop, `parent-as` on any table drop (a flat list has no parent; see [tree nodes](#drag-and-drop-of-tree-nodes)) — is a configuration error, as is `target-executability` on anything but a `row` drop.

  The element content is the action chain, declared exactly as a `<generic-command>` declares its actions. Where the references are needed as channel state as well — a dialog the chain opens bound to the row dropped on, say — `target-channel` and `before-channel` write them to a channel *before* the actions run; the same restrictions by `target` apply to them. Nothing about a drop is implicit: a drop that changes persistent state wraps its script in `<with-transaction>`, as any other command does.
- **Which drop applies**: the declared drops are tried in declaration order, and the first one that accepts the drop where it was made applies it. A drop is skipped where it does not accept the drag's kind, where it has no location at the place of the drop — a `row` drop beside the rows, an `ordered` drop in the middle third of a row —, and where its `executability`, `target-executability` or `refuse-if` refuses; a refusal hands the drop on to the next declared one. So a `row` drop declared before a `table` drop takes a drop on a row and leaves a drop beside the rows, or one the row drop refuses, to the table drop; a `table` drop declared first takes every drop of its kinds, the rows included. Where no drop accepts, the first refusal is shown.

A **reorderable table** declares an `ordered` drop; next to a `row` drop, a row is split into thirds. Declared first, the `ordered` drop takes the upper and lower thirds and the place beside the rows, and the `row` drop the middle third — and every insertion the `ordered` drop refuses; a `row` drop declared first would take the whole row:

```xml
<table rows="$sprint.get(`demo.tickets:Sprint#tickets`)" types="demo.tickets:Ticket">
    <drag kind="ticket"/>
    <!-- Reorder within the list: insert the dragged tickets before the row `before`. -->
    <drop accept="ticket" target="ordered" as="tickets"
        refuse-if="$tickets.containsElement($before)">
        <with-transaction>
            <!-- Take the tickets out, then insert them at the index of `before`, at the end without one. -->
            <execute-script inputs="sprint" function="{
                $sprint.remove(`demo.tickets:Sprint#tickets`, $tickets);
                list = $sprint.get(`demo.tickets:Sprint#tickets`);
                $sprint.add(`demo.tickets:Sprint#tickets`, if($before == null, $list.size(), $list.elementIndex($before)), $tickets);
            }"/>
        </with-transaction>
    </drop>
    <!-- Drop onto a row: make the dragged tickets sub-tasks of the ticket dropped on. -->
    <drop accept="ticket" target="row" as="tickets" target-as="parentTicket">
        …
    </drop>
</table>
```

**Acceptance is decided twice, on purpose.** The server sends the client the kinds the table's enabled drops accept, or that it accepts any drag (`dropAcceptsAny`, `dropAccepts`). A drag carries its kind in its payload and as an empty `dataTransfer` entry `application/x-tl-drag-kind.<kind, lower-cased>`, the one part of a drag a `dragover` handler can read. While a drag moves, the client compares that entry against the accepted kinds alone — no round trip; a drag without a kind passes only a table accepting any drag — and asks the server for the verdict on the place under the pointer (`dropProbe`). The client reports that place only as the row and its zone (`zone`: `upper`, `middle`, `lower`, or `none` beside the rows); how it splits a row follows the modes the table's enabled drops announce (`dropModes`: `control` for a `table` drop, `onto` for a `row` drop, `ordered` for an `ordered` drop) — a row is a whole for `onto` and has no zones for `control` alone. The server resolves the place per mode into a location (`DropLocation`: `Control`, `Onto` the row, `Insert` before a row), picks the drop as above, and answers with the marker to draw (`marker`: `into` highlights a row, `control` the table, `before`/`after` draw an insertion line; `markerKey` names the row). When the drop arrives, the server matches the kind again, per declared drop, and the receiving table refuses a drop of a kind it never offered: the client-side check narrows the gesture for the user, it does not decide it. The dragged objects are resolved by the control the drag started in, from its own row keys, so no wire value can designate an object neither table displays.

**Recording**: a drop is recorded as a `dropObjects` step naming the dragged objects, the mode of the drop that applied it, and the objects of its location (the row dropped onto) by their business identity, so it replays after sorting, filtering and in a fresh session — a replayed drop is offered exactly that location, and the same declared drop applies it. A replayed drop names no source control — it names the objects instead — and carries the kind the drag had when it was recorded, by which it is matched.

**The protocol underneath** (`com.top_logic.layout.react.control.dnd`) has a mode per `target`: `DropMode.CONTROL` (`table`), `ONTO` (`row`) and `ORDERED` (`ordered`, an insertion among the rows); a `DropTarget` written in Java offers them to `TableViewControl.setDropTarget(…)`. In the view layer, a declared drop's `DropSignature` fixes its mode and its references (`DropReference.TARGET`, `DropReference.PARENT`, `DropReference.BEFORE`; a table's `ordered` drop is `DropSignature.ORDERED_LIST`, a tree's `ORDERED_TREE`), which `DropBinding` reads from the location for the arguments of `refuse-if` and the actions, and for the channels. A target announces its modes (`DropTarget.dropModes()`), and the client splits a row accordingly: `ordered` alone into an upper and a lower half, `onto` alone not at all (the whole row is its middle), both together into thirds. A flat table resolves an insertion as in a flat list (`DropLocation.Insert(parent, before)` with `parent` null): the upper part of row X inserts before X, its lower part before the data row following X (`before` null below the last one), and a drop beside the rows at the end; the marker is a line before X, after X, or after the last row. `DropTarget.check(DropRequest)` picks the operation and returns `DropVerdict.accepted(location)`; `onDrop(DropEvent)` then receives that location, and a recorded drop names it (`mode`, `targetObject`, `parent`, `before`).

A table whose rows form a tree resolves its drops by the rules of a tree instead, see [drag and drop on a tree table](doc:view-layer/tree-tables#drag-and-drop).

`<drag>` and `<drop>` apply to the read-only table. A table in edit mode (`row-edit`) renders through `RowSetTableControl`, a control of its own that carries no drag-and-drop seam, so declaring either there is reported as a configuration error.

## Drag and drop of tree nodes

A `<tree>` declares `<drag>` and `<drop>` exactly as a table does, and takes part in the same protocol: a node dragged out of a tree is dropped on a table, a row dragged out of a table on a tree. What is dragged are the *business objects* of the nodes — the objects the tree's `children` function works on —, never the nodes.

```xml
<tree root="all(`demo.tickets:Project`).firstElement()"
    children="node -> $node.get(`demo.tickets:Item#children`)"
    selection="selectedItem"
>
    <drag kind="item">
        <node-executability>
            <disabled-if expr="item -> $item.get(`demo.tickets:Item#locked`)"/>
        </node-executability>
    </drag>
    <!-- Move nodes: insert the dragged items under `parent` before its child `before`. -->
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
    <!-- Drop a ticket dragged out of a table onto a node: assign it to the node's item. -->
    <drop accept="ticket" target="node" as="tickets" target-as="item">
        …
    </drop>
</tree>
```

- **`<drag>`** makes the nodes draggable; dragging a selected node drags the whole selection, including selected nodes in collapsed subtrees. `node-executability` decides per node, with the node's object as the rules' input — a node the rules refuse offers no drag, and a selection including it is refused as a whole.
- **`<drop target="…">`** is `tree` (default), `node` or `ordered`:

  | `target` | applies | arguments | renamed by |
  |---|---|---|---|
  | `tree` | anywhere on the tree | `objects`, `target` (always `null`) | `as`, `target-as` |
  | `node` | onto the node under the pointer, in any part of it | `objects`, `target`: the object of the node dropped on | `as`, `target-as` |
  | `ordered` | at a place among the nodes | `objects`, `parent`: the object whose children the dropped objects become, `before`: the child they are inserted before, `null` for an insertion as the last children | `as`, `parent-as`, `before-as` |

  The optional channels `target-channel`, `parent-channel` and `before-channel` publish the same references as channel state.

  For an `ordered` drop the client splits a node X into thirds: the **upper third** inserts before X among its siblings (`parent` = parent of X, `before` = X, an insertion line above X); the **middle third** inserts into X as its first children (`parent` = X, `before` = the first child of X or `null`, X highlighted); the **lower third** inserts as the first children of X where X is expanded and has children, and after X among its siblings otherwise (`before` = the sibling following X, `null` after the last one; an insertion line below X). **Beside the nodes** the drop appends to the top-level nodes (`parent` = the object the tree is built from, `before` = `null`; the tree as a whole highlighted). The parent of a top-level node is the object `root` computes; where that object is itself displayed as a node, its own parent is `null`. Children are taken in the order the `children` function returns them, so the action chain inserting before `before` changes exactly the list `children` reads. A collapsed node the drag rests on in its middle third is expanded after a moment, so a drop can reach into it.
- A `node` drop alone does not split a node; next to an `ordered` drop the declared order decides, as for a table: an `ordered` drop declared first takes the middle third as an insertion into the node and hands it to the `node` drop only where it refuses, a `node` drop declared first takes the whole node.
- `parent` belongs to a tree's `ordered` drop only: `parent-as` or `parent-channel` declared on a `tree` or `node` drop, or on any drop of a `<table>`, is a configuration error, as is `target-as` or `target-channel` on an `ordered` drop and `target-executability` on anything but a `node` drop.
- **The protocol underneath** is the table's: `ReactTreeControl.setDragSource(…)` / `setDropTarget(…)`, the client-side node id as the item key, `DropLocation.Insert(parent, before)` with business objects, the markers `before`/`after`/`into`/`control`, and `dropObjects` recordings naming `parent` and `before` by business identity. The place a drop is made at is resolved by the rules of a `TreeDropPlace`, which a [tree table](doc:view-layer/tree-tables#underneath) shares; applying, probing, recording and replaying a drop is shared (`DropSupport`).
