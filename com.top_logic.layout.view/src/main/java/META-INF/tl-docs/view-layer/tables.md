---
description: Read before configuring a <table> (TableViewControl) in a .view.xml - column declarations, row selection, row activation, grouping, the filter bar and its presets, pinned columns, and drag and drop of rows.
order: 60
---

# Tables

## `TableViewControl` is the sole React table control

`TableViewControl` / `com.top_logic.table.TableView` (#29108) is the only React table control. Everything renders through this stack: the `<table>` element (`TableElement`; sort, per-column `<filter>`, type-derived default columns, width personalization, shared `ColumnsConfig` / `ColumnConfig`), the access-control permission matrix (`SecurityMatrixElement`), the in-form `<composition-table>` (`CompositionTableControl`), and the technical React-table demo (`DemoReactTableComponent`: flat `ListRowSource` + `TreeRowSource` tree).

- Each `TableViewControl` cell is a registered child control, so interactive cell controls work. Cell rendering (`CellContentReactAdapter`): `CellContent.Text` / `Labeled` → text; `CellContent.Editable` backed by a *boolean* `FieldModel` → interactive `ReactCheckboxControl` (other field types read-only); **`CellContent.Raw` whose payload is a `CellControlFactory` → whatever control the factory builds** — the escape hatch for bespoke cells. The composition table uses it to put typed field inputs (`FieldControlService.createFieldControl(part, fieldModel)`) and detail / delete action buttons in cells; a column's `value(R)=row` plus a renderer returning `Raw(ctx -> buildControl(ctx, row, …))` gives the factory the row.
- **`TLPanel` renders a single `toolbar` child control (a `ReactToolbarControl`), not a `toolbarButtons` list.** Push a panel toolbar via `putState("toolbar", new ReactToolbarControl(ctx))` + `addGroup(name, ToolbarGroupDisplay.INLINE, …, List.of(button))`.
- **A column's display width**: `<column width="220"/>` on a `<table>` or a `<composition-table>` is the width in pixels a column is shown with until the user resizes it, from then on their own width is remembered; `0` (the default) keeps the width the column's type derives (`ColumnProviderService` config: boolean 80, number 100, date 110, time 90, date-time 160, enumeration 120, string and everything else 150, each retunable per application). The configured width is applied uniformly after the `ColumnBinding` built the column — `ColumnSetup.buildColumn()` wraps it through `DelegatingColumn.withDefaultWidth(…)`, the general `Column` decorator for overriding single aspects of a column — so no binding knows about widths.
- Mutable rows: `ListRowSource.setElements(list)` then `TableViewControl.refreshData()`. `DefaultTableView.create(columns, source[, ViewStateStore, TableId])` — the 2-arg form skips personalization; pass a stable `TableId` to persist column width / order.

### Column declarations

The `<columns>` of a `<table>` (and of a `<composition-table>`) is a list of column declarations (`ColumnsConfig` / `ColumnDeclaration` in `com.top_logic.layout.view.table`), tag-dispatched and resolved in display order. A row is any object a TL-Script function yields — a model object, a transient one, a dictionary — so a declaration says what a cell holds rather than which attribute it reads. Four kinds:

- **`<column attribute="title"/>`** (`AttributeColumn`) — the attribute decides everything: display, sort, filter, search, width, header, and what an edited cell writes.
- **`<computed-column name="total" type="tl.core:Double">`** (`ComputedColumn`) with `<value>row -> …</value>` — a value computed per row. The declared `type` (plus `multiple`) gives the column the same type-derived display, sort, filter and width an attribute column gets; a column declaring no type shows its values by their display label. `<update>row -> value -> …</update>` makes the column editable and `<can-update>row -> …</can-update>` decides per row whether the edit is offered; an edited column must declare its type, because the type picks the input control. `<aggregate>rows -> …</aggregate>` is what the column shows in a group header row.
- **`<embedded-columns reference="assignee">`** (`EmbeddedColumns`) — the columns of an object the row points to, shown as columns of the row. `reference` is a reference name or a dotted path (`milestone.tickets`), and a step holding several objects makes every embedded column collect the values of all of them; alternatively `<object>row -> …</object>` plus `name`, `type` and `<label>` computes the embedded object. Nested declarations are of any kind and resolve against the embedded type; an embedding declaring nothing shows that type's main properties. Names are prefixed with the path (`assignee.name`) and labels with the reference labels (`Assignee / Name`, `I18NConstants.EMBEDDED_COLUMN_LABEL__PREFIX_COLUMN`), so a column of the row and one of the embedded object stay apart in the header and in the column selection. Embedded columns are read-only — an embedded object is changed where that object itself is edited.
- **`<dynamic-columns name="milestones" type="tl.core:Integer">`** (`DynamicColumns`) — one column per element of the set `<columns>…</columns>` computes, for a table of tasks with a column per milestone. `<value>row -> col -> …</value>` fills a cell from the row and the object its column stands for; `<column-name>` and `<column-label>` name and label a column (default: the object's display label), `<column-type>` computes the type per column where `type` does not fit all of them, and `<update>` / `<can-update>` / `<aggregate>` take the column object as an extra argument. A column's name is the declaration name, a dot and the column object's name, so a user's arrangement of it survives; the filter is the one the type derives, as there is no single column to declare one for. The columns are those the function yields when the table is built.

Further rules that hold across the kinds:

- **Input channels lead.** Every declaration may carry `<inputs><input channel="…"/></inputs>`; the channel values become the leading positional arguments of *all* of its functions, ahead of the row — `<value>factor -> row -> …</value>`. A `<computed-column>`'s `<aggregate>` is the exception: it takes the group's rows alone.
- **Default columns.** A table without `<columns>` shows the main properties of its row type (`<main-properties properties="name, dueDate"/>` on the type), and all non-hidden attributes of that type when it names none.
- **Offered columns.** Whatever the declared columns do not cover, the table offers in its column selection, hidden until the user picks it — the remaining attributes of the configured `types`, and the remaining attributes of an embedded type under their prefixed labels. Only an explicitly configured `types` produces this offering; a type guessed from the first row would vary with the data.
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
- **A tree reads its channel too** (`TreeSelectionBinding`): the node of an object another writer puts on it is looked for, the subtrees above it are opened so that it is visible, and it becomes the selection. A collection is answered by mode as in a table — a `multi` tree selects the nodes it has for those objects, a `single` tree cannot display such a value and shows no selection — and an object the tree has no node for is "nothing selected here": the channel **and** the tree's own selection are left alone, since that object may well get a node in a moment. Finding the node means searching the tree, which computes the child list of every node it passes; a large or unbounded tree therefore declares `parents` — `<tree parents="input -> node -> $node.get(\`my:Type#parent\`)">` — and the tree walks from the object up to its root and descends along that chain instead, computing only the child lists on the way.
- **A tree follows the model it displays** (`ObservableTreeModel`, the tree's counterpart of the row observation a `<table>` does over its `observed-types`): a change reconciles the child lists in place, so a node whose object is still there is the node the display was working with and the subtrees the user opened stay open, a deleted object loses its node, and an object that appeared gets one. Creates need the type in `observed-types`, since no channel value changes when an object is added. The selection is re-applied after every such change, which is what a create command needs: it writes the new object to the selection channel before the tree has a node for it, and the object is revealed and selected as soon as the node exists. An input naming another root builds the tree anew and opens the subtrees that were open again.
- **A command working on one object binds to a derived channel rather than to the selection**, since the selection may be several: `<derived-channel name="selectedSingle" inputs="selected" expr="sel -> if($sel.size() == 1, $sel.singleElement(), null)"/>` is the selection while it consists of exactly one object and nothing otherwise — `size()` counts nothing as zero, a single object as one and a set as the number of its elements. With `<null-input-disabled/>` that is the whole of "enabled for one selected object". A command working on the whole selection needs nothing: `delete()` and the other collection-valued script functions accept a single object as well as a set of them.
- Demos in `com.top_logic.demo.react`: the *Attributes* table (`views/attributes.view.xml`) selects several rows — Delete works on all of them, Edit on the single selection through such a derived channel — and *Tree Demo* (`views/demo/tree-demo.view.xml`) shows the selected nodes and the activated one side by side, and creates, detaches and deletes nodes in place: a milestone created through a dialog and a ticket created without one are revealed and selected as soon as their node exists, detaching a ticket from its milestone takes the node out of the child list without deleting anything, and a delete works on the whole selection - in each case the opened subtrees stay open.

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

- `<on-activate>` holds an ordinary `ViewCommand` configuration — the same thing `<button><action>` holds, hence the `class=` form. The server selects the activated row first (so the table's `selection` channel holds it), then runs the command **with that row as its input**; the command's own `<executability>` rules decide over the row, so a row the rules reject activates nothing.
- The element instantiates the configured command in its constructor (with the element's `InstantiationContext`, so a bad configuration is reported at load time) and keeps command plus config, exactly as `ButtonElement` does. `ViewCommandModel.forCommand(context, command, config)` then builds the model for it — resolve the input channel, build the executability rule, pick the matching model — and is the single construction path shared by `ButtonElement`, `TableElement`, `TreeElement`, `CommandCarrierElement` (panels, menus) and `AppBarElement`. `ViewCommandModel.execute(context, input)` runs the command with an input the caller supplies instead of the channel value; that is the one path for "run this configured command with this value".
- The control-level seam is `TableViewControl.setActivationHandler(…)` / `ReactTreeControl.setActivationHandler(…)` — one handler, called with the row business object (the tree node) after the row became the selection, returning the `HandlerResult` the gesture reports. Without a handler, activating a row only selects it.
- An activation is recorded for scripted tests the way a selection is: the index-based `activate` becomes an `activateByKey` naming the row's business object, so the step survives sorting, filtering and a fresh session.
- A double-click inside an interactive cell element (a text input of an editable cell) belongs to that element and opens nothing; Enter is likewise declined while the focus sits in a cell input or action button.
- **In a `<tree>` the selection follows the keyboard cursor.** A single-selection tree selects the node that the up/down arrows, `Home` and `End` move the cursor onto; a multi-selection tree moves the cursor alone, grows the selected range from its anchor with `Shift` and an arrow, and adds the cursor node to the selection or takes it out again with `Space`. The right and left arrows expand and collapse the cursor node, stepping to its first child and back to its parent where there is nothing to open or close, and `Enter` activates it in either mode.
- **A table with an `<on-activate>` offers the same command as a button on every row**, in a pinned column at the right edge — the gestures are invisible, and a touch device has neither of them. Each button is icon-only (the command's `<image>`, else a chevron), takes the command's label as its tooltip, and follows the command's executability *for its own row*: a row the rules reject shows a disabled button, a row they hide shows an empty cell. `<table activation-button="false">` opts out, for a table whose rows are opened another way.
- The column itself is general, not an activation column: `RowCommandColumn.columns(context, List.of(new RowCommandColumn.RowCommand(model, image, label)))` builds one pinned column per `ViewCommandModel` a caller wants to offer per row, and `ViewCommandModel.executability(row)` is what a button decides its state by. `TableElement` feeds the activation command into it — one model serving both the gesture and the buttons — and `RowSetTableControl.setTrailingColumns(…)` is where the editable variant appends them, behind the columns it builds itself.
- Demos in `com.top_logic.demo.react`: the *Attributes* table opens the object's detail dialog, *Tree Demo* (`demo/tree-demo.view.xml`) writes the activated node to a channel displayed next to the selected one, and the *Tiles Demo* account tables (`demo/tiles-demo/overview.view.xml`, `demo/tiles-demo/person-detail.view.xml`) carry an `<on-activate class="…NavigatePushCommand">` that drills into the activated person.

## Grouping

A `<table>` buckets its rows by the value of one column: one collapsible header row per value, showing that value, how many rows it holds, and what every other column aggregates over them. `<table group-by="status">` is the grouping a table *starts* with; the user regroups it from a column header's context menu ("Group by this column" / "Remove grouping") or from the column selection (the group icon on each row of the dialog, applied together with the columns), and that choice is kept under the table's personalization key exactly like the sort order.

- **The model does the grouping** (`com.top_logic.table`): `TableView.group(GroupSpec)` sets `TableViewState.getGrouping()`, persists it and hands it to `RowSource.withGrouping`. `ListRowSource` filters, then sorts, then buckets — so a filter decides what a group holds and the sort decides the order *within* it — and emits a `GroupRow` (`RowKind.GROUP_HEADER`, `expandable()`, `depth() == 0`, keyed by its `GroupKey`) followed by its members at `depth() == 1`. The groups themselves are ordered by the grouping column's own comparator (`Column#sort()`, a `null` group value last); a grouping column that cannot sort keeps its groups in the order their first rows appear in. The direction of the group order is that of the grouping column's own sort, else that of the sort of the first displayed column (the one showing the group values in the header rows), else ascending: `DefaultTableView` derives this by putting the grouping column in front of the order it hands to the row source, while `TableViewState.getSort()` keeps the user's sort. `DefaultTableView.cell(row, column)` renders the group value in the first column and the column's `Aggregator` result in the others.
- **The UI follows that representation**: `TableViewControl` pushes the group rows through the same `treeMode` / `treeDepth` / `expandable` / `expanded` state the tree table already uses, plus a `groupCount` present exactly on a group header, so collapsing a group *is* collapsing a node (`expand` command) and needs no second mechanism. A table becomes `treeMode` while it is grouped, whatever it was built as.
- **A group header stands for no object**: the gesture that would select it (a click, or `Enter`) collapses or expands it instead, the selection channel never receives a group, and range selection, select-all and keyboard navigation only ever collect `RowKind.DATA` rows. Changing the grouping clears the selection, since the rows it named are gone.
- **One column at a time** — `ListRowSource` rejects a multi-column `GroupSpec`, and the UI offers a single column accordingly.
- Grouping is offered for the columns the user may choose at all (the `columnOptions()`, i.e. the selectable ones): an action column carries the row itself and would yield one group per row.
- Demo: *Object list* (`tickets.view.xml`) groups its first ticket list by status and leaves the second flat; *Attributes → Table* starts ungrouped and is grouped from the header menu.

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
            <execute-script function="tickets -> $tickets.foreach(t -> $t.set(`demo.tickets:Ticket#status`, `demo.tickets:TicketStatus#closed`))"/>
        </with-transaction>
    </drop>
</table>
<table rows="all(`tl.accounts:Person`)" types="tl.accounts:Person">
    <drop accept="ticket" target="row" target-channel="dropPerson">
        <with-transaction>
            <execute-script function="person -> tickets -> …">
                <inputs><input channel="dropPerson"/></inputs>
            </execute-script>
        </with-transaction>
    </drop>
</table>
```

- **`<drag/>`** makes the rows draggable. Dragging a selected row drags the whole selection, an unselected row drags itself — and the selection is read on the server, so a selection reaching beyond the rendered row window is dragged completely. `kind` classifies the drag with a free name the application chooses (`ticket`, `assignment`, …); without it, the drag has no kind.
- **`<drop>`** is a list, so a table can accept several kinds of drag, and accept one kind on its rows and another as a whole. `accept` lists the kinds it takes, comma-separated (`accept="ticket, milestone"`); a drag without a kind or of an unlisted kind is refused. Without `accept`, the drop takes every drag, of any kind or none. Kinds are compared literally, so a drag and the drops meant to take it agree on the spelling. What a drop takes beyond the kind — only objects of a certain model type, say — is decided by its `refuse-if`, e.g. ``refuse-if="target -> objects -> $objects.filter(o -> !$o.instanceOf(`demo.tickets:Ticket`)).size() > 0"``. `target` is `table` (default), `row` or `ordered`, and decides which objects the drop refers to at its place — its *references*, the leading arguments of `refuse-if`, each written to its channel *before* the actions run, which is how the chain reads where the drop was made:

  | `target` | applies | `refuse-if` | channel |
  |---|---|---|---|
  | `table` | anywhere on the table | `target -> objects -> reason`, `target` is `null` | `target-channel` (written with `null`) |
  | `row` | onto the row under the pointer | `target -> objects -> reason` | `target-channel`: the row dropped on |
  | `ordered` | between two rows (insertion line); beside the rows appends | `before -> objects -> reason` | `before-channel`: the row to insert before, `null` at the end |

  A channel the drop's target has no reference for — `before-channel` on a `table` or `row` drop, `target-channel` on an `ordered` drop — and `target-executability` on anything but a `row` drop are reported as configuration errors. The element content is the action chain, declared exactly as a `<generic-command>` declares its actions, and its first action receives the **list of dropped objects** as its input. Nothing about a drop is implicit: a drop that changes persistent state wraps its script in `<with-transaction>`, as any other command does.
- **Which drop applies**: the declared drops are tried in declaration order, and the first one that accepts the drop where it was made applies it. A drop is skipped where it does not accept the drag's kind, where it has no location at the place of the drop — a `row` drop beside the rows, an `ordered` drop in the middle third of a row —, and where its `executability`, `target-executability` or `refuse-if` refuses; a refusal hands the drop on to the next declared one. So a `row` drop declared before a `table` drop takes a drop on a row and leaves a drop beside the rows, or one the row drop refuses, to the table drop; a `table` drop declared first takes every drop of its kinds, the rows included. Where no drop accepts, the first refusal is shown.

A **reorderable table** declares an `ordered` drop; next to a `row` drop, a row is split into thirds. Declared first, the `ordered` drop takes the upper and lower thirds and the place beside the rows, and the `row` drop the middle third — and every insertion the `ordered` drop refuses; a `row` drop declared first would take the whole row:

```xml
<table rows="$sprint.get(`demo.tickets:Sprint#tickets`)" types="demo.tickets:Ticket">
    <drag kind="ticket"/>
    <!-- Reorder within the list: insert the dragged tickets before the row `before`. -->
    <drop accept="ticket" target="ordered" before-channel="insertBefore"
        refuse-if="before -> tickets -> $tickets.contains($before)">
        <with-transaction>
            <execute-script function="sprint -> before -> tickets -> …">
                <inputs><input channel="sprint"/><input channel="insertBefore"/></inputs>
            </execute-script>
        </with-transaction>
    </drop>
    <!-- Drop onto a row: make the dragged tickets sub-tasks of the ticket dropped on. -->
    <drop accept="ticket" target="row" target-channel="parentTicket">
        …
    </drop>
</table>
```

**Acceptance is decided twice, on purpose.** The server sends the client the kinds the table's enabled drops accept, or that it accepts any drag (`dropAcceptsAny`, `dropAccepts`). A drag carries its kind in its payload and as an empty `dataTransfer` entry `application/x-tl-drag-kind.<kind, lower-cased>`, the one part of a drag a `dragover` handler can read. While a drag moves, the client compares that entry against the accepted kinds alone — no round trip; a drag without a kind passes only a table accepting any drag — and asks the server for the verdict on the place under the pointer (`dropProbe`). The client reports that place only as the row and its zone (`zone`: `upper`, `middle`, `lower`, or `none` beside the rows); how it splits a row follows the modes the table's enabled drops announce (`dropModes`: `control` for a `table` drop, `onto` for a `row` drop, `ordered` for an `ordered` drop) — a row is a whole for `onto` and has no zones for `control` alone. The server resolves the place per mode into a location (`DropLocation`: `Control`, `Onto` the row, `Insert` before a row), picks the drop as above, and answers with the marker to draw (`marker`: `into` highlights a row, `control` the table, `before`/`after` draw an insertion line; `markerKey` names the row). When the drop arrives, the server matches the kind again, per declared drop, and the receiving table refuses a drop of a kind it never offered: the client-side check narrows the gesture for the user, it does not decide it. The dragged objects are resolved by the control the drag started in, from its own row keys, so no wire value can designate an object neither table displays.

**Recording**: a drop is recorded as a `dropObjects` step naming the dragged objects, the mode of the drop that applied it, and the objects of its location (the row dropped onto) by their business identity, so it replays after sorting, filtering and in a fresh session — a replayed drop is offered exactly that location, and the same declared drop applies it. A replayed drop names no source control — it names the objects instead — and carries the kind the drag had when it was recorded, by which it is matched.

**The protocol underneath** (`com.top_logic.layout.react.control.dnd`) has a mode per `target`: `DropMode.CONTROL` (`table`), `ONTO` (`row`) and `ORDERED` (`ordered`, an insertion among the rows); a `DropTarget` written in Java offers them to `TableViewControl.setDropTarget(…)`. In the view layer, a declared drop's `DropSignature` fixes its mode and its references (`DropReference.TARGET`, `DropReference.BEFORE`), which `DropBinding` reads from the location for `refuse-if` and the channels. A target announces its modes (`DropTarget.dropModes()`), and the client splits a row accordingly: `ordered` alone into an upper and a lower half, `onto` alone not at all (the whole row is its middle), both together into thirds. The table resolves an insertion as in a flat list (`DropLocation.Insert(parent, before)` with `parent` null): the upper part of row X inserts before X, its lower part before the data row following X (`before` null below the last one), and a drop beside the rows at the end; the marker is a line before X, after X, or after the last row. `DropTarget.check(DropRequest)` picks the operation and returns `DropVerdict.accepted(location)`; `onDrop(DropEvent)` then receives that location, and a recorded drop names it (`mode`, `targetObject`, `parent`, `before`).

`<drag>` and `<drop>` apply to the read-only table. A table in edit mode (`row-edit`) renders through `RowSetTableControl`, a control of its own that carries no drag-and-drop seam, so declaring either there is reported as a configuration error.
