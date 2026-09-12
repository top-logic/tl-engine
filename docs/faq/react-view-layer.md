# FAQ: React view layer (`com.top_logic.layout.view` and the React table)

## The `.view.xml` layer is a composition layer, not a place for new React components

`com.top_logic.layout.view` is declarative: a `.view.xml` assembles existing React controls (`TLPanel`, `TLWindow`, `TLForm` / `TLFormField`, `TLTextInput`, `TLButton`, `TLText`, `TLDialog`, …) through `UIElement` configs (each a `@TagName`) and view commands / actions. To build a feature (a login dialog, a user menu, …), compose these via XML plus small Java `ViewCommand` / `ViewAction` / `ViewExecutabilityRule` / `UIElement` classes that reuse existing controls. Do **not** hand-roll bespoke monolithic React components. A new React control is justified only for a genuinely new generic widget (e.g. a `type=password` input), not for assembling forms / buttons that already exist.

- **Forms** bind to a model object: `<form input="ch"><field attribute="x"/>`. For ad-hoc input, create a small transient model type (`new(\`mod:Type\`, transient: true)`); a field's control is chosen by a `<input-control><impl class="…Provider"/></input-control>` annotation on the attribute, or — where the choice belongs to one place in the user interface rather than to the model — by `<field attribute="x"><input-control class="…Provider" …/></field>` in the view itself (`FieldElement.Config.getInputControl()`), whose `class=` names the same `ReactFieldControlProvider` and carries its configuration. A single value that belongs to the view rather than to an object - a filter term, a search text - needs no form and no object at all: `<text-input value="ch"/>` (`TextInputElement`) binds the input to the channel in both directions.
- **Dialogs** open a `.view.xml` via `<open-dialog dialog-view="…">`; close via `CancelDialogCommand` / `DialogManager.closeTopDialog`. `currentUser()` is a TL-Script function usable in `<derived-channel expr="…">`.
- **Referencing a `UIElement` impl by `class=` in view content.** View content lists resolve entries by `@TagName`, so an app-specific element that should not claim a global tag is placed via the content property's *entry tag* plus `class=`. The `children` content property (`ContainerElement.Config`) is `@EntryTag("child")`, so write `<child class="fq.MyElement"/>` inside a `<panel>` / container. If a cell provider is reusable, make it public rather than justifying a separate element; justify a separate element by genuinely different data / behavior.
- **Standalone form-field controls bind to a `FieldModel`.** For a standalone field control (e.g. a checkbox cell), use the concrete `com.top_logic.layout.form.model.AbstractFieldModel` + `FieldModelListener` — not `FormContext` / `FormField` / `FormFieldAdapter`, which are legacy-compat shims. `AbstractFieldModel` is editable by default, needs no `FormContext` parent, and triggers no label resource lookup in `ReactFormFieldControl`.
- Modifying persistent state from a control's value listener needs a transaction; the listener has no ambient one, so open `beginTransaction()` there (or buffer changes and apply them under one transaction on save).

## A command is a chain of actions, and the chain can branch

`<generic-command>` (`GenericViewCommand`) runs the `<execute-script>`, `<store-form-state>`, `<confirm>`, `<with-transaction>`, `<open-dialog>`, `<write-channel>`, … actions written inside it as one chain (`ViewActionChain`): each action's result is the next action's input, the first action gets the command's input. An action that has to wait — `<confirm>`, which opens a dialog — suspends the chain and resumes it from the dialog's answer, or aborts it on cancel; an abort skips the remaining actions and runs the compensations the executed actions registered, newest first, as does a failure. A script over the chain's value takes further arguments from channels: `<inputs><input channel="context"/></inputs>` puts those channel values in front of the chain's value (`ActionScript`, shared by every action that takes a script).

Two actions branch the chain by a TL-Script function over its current value. `<if>` decides between two chains; `<switch>` computes a switch value with its `value` function (the chain's own value when no `value` is configured) and gives it to the `<case>`s, each of which either names the value it stands for with `match` or decides with a `test` predicate:

```xml
<if test="ticket -> $ticket != null">
  <then>
    <write-channel name="ticket"/>
  </then>
  <else>
    <execute-script function="x -> new(`demo.tickets:Ticket`, transient: true)"/>
    <open-dialog bind-input-to="model" dialog-view="tickets-create.view.xml"/>
  </else>
</if>

<switch value="t -> $t.get(`demo.tickets:Ticket#status`)">
  <case match="`demo.tickets:TicketStatus#closed`">
    <with-transaction>
      <execute-script function="t -> $t.set(`demo.tickets:Ticket#status`, `demo.tickets:TicketStatus#open`)"/>
    </with-transaction>
  </case>
  <case test="s -> $s == null">
    <confirm expr="x -> #('The ticket has no status.'@en)"/>
  </case>
  <default>
    <with-transaction>
      <execute-script function="t -> $t.set(`demo.tickets:Ticket#status`, `demo.tickets:TicketStatus#closed`)"/>
    </with-transaction>
  </default>
</switch>
```

- The chosen branch runs as a *nested* chain (`ViewActionChain.nest`): it starts with the chain's current value, and the result of its last action becomes the value the enclosing chain continues with. A branch that is not configured — a missing `<else>`, a `<switch>` without a matching case and without a `<default>` — passes the value through unchanged, so a branch never breaks the chain.
- An abort inside a branch aborts the whole command, and a compensation registered inside a branch takes its place in the enclosing chain's unwind: it runs whenever the command is later aborted or fails, before the compensations of the actions preceding the branch. A `<confirm>` therefore works inside a branch exactly as beside it, including the suspension: the branch may resume long after the command returned.
- `<if>` reads its condition in the fuzzy sense of TL-Script, so an object stands for a true condition and nothing (`null`, an empty list, an empty text) for a false one. A `<case match="…">` holds a TL-Script expression without parameters and matches when the switch value equals its value under the TL-Script comparison (`==`), so a classifier is written as `` `module:Enumeration#literal` ``, a text as `'text'` and a number as the number it is. A `<case test="…">` holds a predicate that is called with the switch value and read in the same fuzzy sense as an `<if>` condition; a case configures exactly one of the two. Without a `value` function the switch value is the chain's own value, so `<switch><case test="t -> $t == null">` decides on what the chain carries.
- A command whose chain applies the entered form values is disabled while the form has errors — a branch reports that for the actions of *all* its branches, taken or not, because the button's state cannot depend on the decision.

## `TableViewControl` is the sole React table control

`TableViewControl` / `com.top_logic.table.TableView` (#29108) is the only React table control. Everything renders through this stack: the `<table>` element (`TableElement`; sort, per-column `<filter>`, type-derived default columns, width personalization, shared `ColumnsConfig` / `ColumnConfig`), the access-control permission matrix (`SecurityMatrixElement`), the in-form `<composition-table>` (`CompositionTableControl`), and the technical React-table demo (`DemoReactTableComponent`: flat `ListRowSource` + `TreeRowSource` tree).

- Each `TableViewControl` cell is a registered child control, so interactive cell controls work. Cell rendering (`CellContentReactAdapter`): `CellContent.Text` / `Labeled` → text; `CellContent.Editable` backed by a *boolean* `FieldModel` → interactive `ReactCheckboxControl` (other field types read-only); **`CellContent.Raw` whose payload is a `CellControlFactory` → whatever control the factory builds** — the escape hatch for bespoke cells. The composition table uses it to put typed field inputs (`FieldControlService.createFieldControl(part, fieldModel)`) and detail / delete action buttons in cells; a column's `value(R)=row` plus a renderer returning `Raw(ctx -> buildControl(ctx, row, …))` gives the factory the row.
- **`TLPanel` renders a single `toolbar` child control (a `ReactToolbarControl`), not a `toolbarButtons` list.** Push a panel toolbar via `putState("toolbar", new ReactToolbarControl(ctx))` + `addGroup(name, ToolbarGroupDisplay.INLINE, …, List.of(button))`.
- Mutable rows: `ListRowSource.setElements(list)` then `TableViewControl.refreshData()`. `DefaultTableView.create(columns, source[, ViewStateStore, TableId])` — the 2-arg form skips personalization; pass a stable `TableId` to persist column width / order.

## Colored values

A value's color is part of the model: two annotations say where it comes from, and one seam answers it.

- **`<color>`** on an enumeration literal (`TLColor`) gives that literal its color, stated either as a fixed color (`<color value="#04a38d"/>`) or as the name of a UI-theme design token (`<color token="support-success"/>`), which follows the theme the user has active. A literal without the annotation has no color.
- **`<dynamic-color>`** on a type (`TLDynamicColor`) holds the `ValueColorProvider` computing the color of its instances, in the shape `<dynamic-icon>` and `<label>` use for icons and labels. `ColorByExpression` states that algorithm as a TL-Script expression over the object: the demo ticket takes its color from its status, `<dynamic-color><color-by-expression color="t -> $t.get(`demo.tickets:Ticket#status`)"/></dynamic-color>`, so a ticket appears in the color of the status literal it holds. A color value as the result is the color itself, an enumeration literal is colored by its own `<color>`, any other result and `null` leave the object uncolored. Specializations inherit the annotation.
- **`AnnotationValueColorProvider.INSTANCE`** (a `ValueColorProvider`) answers both: `colorOf(value)` returns the `ValueColor` of a classifier or an object, the color a color value itself is, and `null` for everything the model gives no color to. `ValueColor.cssValue()` is the CSS to apply it with — the fixed color, or `var(--<token>)` against the custom properties `UIThemeService` emits per theme.

A colored value is displayed as a **pill** in its color, an uncolored one as plain text. The color travels as the single state / descriptor field `ReactValueColor.COLOR`, filled by `ReactValueColor.putColor(descriptor, value)` / `cssColorOf(value)`, and the client hands it to the stylesheet as the inline custom property `--tlPill-color` — there is no class per color. One shared presentational component `TLPill` (`react-src/controls/pill/TLPill.tsx`, `.tlPill` in `tlReactControls.css`) draws it everywhere; the tint is composed with `color-mix()` from the color and the `color-surface` / `text-primary` tokens, so one declaration stays legible on a light and a dark theme.

The sites that fill the field:

- **`ReactDropdownSelectControl`** — every option and every selected value passes through its one descriptor factory, so the pill appears in the read-only display of a reference or enumeration attribute (a `<table>` cell, a view-mode `<form>` field), on the chips of the selection while editing, and on the rows of the open dropdown.
- **`ReactResourceCellControl`** — resource cells and tree nodes, beside the type icon.
- **`ReactTextControl`** / `<text>` (`TextElement`) — a channel value rendered through `MetaLabelProvider`; `setText(text, cssColor)` updates both in one patch.

## Progress

A fraction between 0 and 1 is displayed as a bar with an optional label beside it. `ReactProgressControl` (`TLProgress`) holds the two state entries `FRACTION` and `LABEL` and nothing else - what the fraction counts is the caller's business. A number outside the range is drawn at the end it exceeds, so two counts that disagree give a full or an empty bar rather than one running past its track; `setProgress(fraction, label)` updates both in one patch.

`<progress>` (`ProgressElement`) states the bar one of two ways, never both:

- `<progress input="ch" fraction="x -> …"/>` — the filled part directly. Such a bar carries no label unless `label="x -> …"` gives it one.
- `<progress input="ch" done="x -> …" total="x -> …"/>` — the two counts the fraction is the ratio of, which are also the label (`3 / 7`) unless `label=` replaces it. A total of zero leaves the bar empty.

Every expression is called with the current value of the `input` channel, which is optional: a bar counting the model as a whole needs none. The bar recomputes on a new channel value, on a change of the object the channel holds, and on a create / change / delete of an `observed-types` type — the last is what a bar counting all objects of a type needs, since no channel value changes when one is added. The observation is the shared `ChannelObjectObserver`, attached and detached with the control.

A table cell needs nothing new: a `CellRenderer` yields `new CellContent.Raw((CellControlFactory) ctx -> new ReactProgressControl(ctx, fraction, label))`, the escape hatch `CellContentReactAdapter` already resolves.

## Drag and drop of table rows

A `<table>` declares that its rows may be dragged, and what it accepts a drop of. Both are declarations of the table, so a drag between two tables needs no code on either side:

```xml
<table rows="…" selection="ticket" types="demo.tickets:Ticket">
    <drag/>
    <drop accept="demo.tickets:Ticket">
        <with-transaction>
            <execute-script function="tickets -> $tickets.foreach(t -> $t.set(`demo.tickets:Ticket#status`, `demo.tickets:TicketStatus#closed`))"/>
        </with-transaction>
    </drop>
</table>
<table rows="all(`tl.accounts:Person`)" types="tl.accounts:Person">
    <drop accept="demo.tickets:Ticket" target="row" target-channel="dropPerson">
        <with-transaction>
            <execute-script function="person -> tickets -> …">
                <inputs><input channel="dropPerson"/></inputs>
            </execute-script>
        </with-transaction>
    </drop>
</table>
```

- **`<drag/>`** makes the rows draggable. Dragging a selected row drags the whole selection, an unselected row drags itself — and the selection is read on the server, so a selection reaching beyond the rendered row window is dragged completely. The rows are announced under a type tag: `type` when the drag declares one, otherwise the table's first `types` entry. A table declaring neither is a configuration error — nothing would say what its rows are.
- **`<drop>`** is a list, so a table can accept several kinds of object, and accept one kind on its rows and another as a whole. `accept` names the types (a subtype of an accepted type is accepted as well); `target` is `table` (default) or `row`; `target-channel` is written with the row dropped on — `null` for a table drop — *before* the actions run, which is how the chain reads what was dropped on. The element content is the action chain, declared exactly as a `<generic-command>` declares its actions, and its first action receives the **list of dropped objects** as its input. Nothing about a drop is implicit: a drop that changes persistent state wraps its script in `<with-transaction>`, as any other command does.
- **Which drop applies**: the first declared one that accepts the drag. A drop made on a row is offered to the `row` drops first and falls back to a `table` drop when none of them accepts it, so a table whose rows are targets for one kind of object still accepts another kind wherever the pointer was.

**Acceptance is decided twice, on purpose.** The server expands each accepted type to its own qualified name plus those of all its subtypes and sends that set of tags to the client. While a drag moves, the client compares the drag's tag against those tags alone — no round trip — and highlights the table, or the row under the pointer for a table whose rows are targets. When the drop arrives, the server matches it again, per declared drop, and the receiving table refuses a drop of a tag it never offered: the client-side check narrows the gesture for the user, it does not decide it. The dragged objects are resolved by the control the drag started in, from its own row keys, so no wire value can designate an object neither table displays.

**Recording**: a drop is recorded as a `dropObjects` step naming the dragged objects and the target row by their business identity, so it replays after sorting, filtering and in a fresh session. A replayed drop names no source control — it names the objects instead — and is matched by their type.

`<drag>` and `<drop>` apply to the read-only table. A table in edit mode (`row-edit`) renders through `RowSetTableControl`, a control of its own that carries no drag-and-drop seam, so declaring either there is reported as a configuration error.
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

- **The model does the grouping** (`com.top_logic.table`): `TableView.group(GroupSpec)` sets `TableViewState.getGrouping()`, persists it and hands it to `RowSource.withGrouping`. `ListRowSource` filters, then sorts, then buckets — so a filter decides what a group holds and the sort decides the order *within* it — and emits a `GroupRow` (`RowKind.GROUP_HEADER`, `expandable()`, `depth() == 0`, keyed by its `GroupKey`) followed by its members at `depth() == 1`. `DefaultTableView.cell(row, column)` renders the group value in the first column and the column's `Aggregator` result in the others.
- **The UI follows that representation**: `TableViewControl` pushes the group rows through the same `treeMode` / `treeDepth` / `expandable` / `expanded` state the tree table already uses, plus a `groupCount` present exactly on a group header, so collapsing a group *is* collapsing a node (`expand` command) and needs no second mechanism. A table becomes `treeMode` while it is grouped, whatever it was built as.
- **A group header stands for no object**: the gesture that would select it (a click, or `Enter`) collapses or expands it instead, the selection channel never receives a group, and range selection, select-all and keyboard navigation only ever collect `RowKind.DATA` rows. Changing the grouping clears the selection, since the rows it named are gone.
- **One column at a time** — `ListRowSource` rejects a multi-column `GroupSpec`, and the UI offers a single column accordingly.
- Grouping is offered for the columns the user may choose at all (the `columnOptions()`, i.e. the selectable ones): an action column carries the row itself and would yield one group per row.
- Demo: *Object list* (`tickets.view.xml`) groups its first ticket list by status and leaves the second flat; *Attributes → Table* starts ungrouped and is grouped from the header menu.

## Pinned columns

`Column.pinnedEnd()` keeps a column at the end of the table: it is rendered behind every other column and stays fixed to the right edge while the table scrolls horizontally — the place for what acts on a row (the per-row buttons) rather than for what the row *is*. `DefaultColumn.builder(name, value).pinnedEnd(true)` is how a column author asks for it; there is no `.view.xml` attribute.

- A pinned column is the table's own, not part of the arrangement the user makes: it is neither `frozenEligible()` nor `selectable()` (`DefaultColumn` enforces both, whatever they are set to), so the column selection does not offer it, `setColumnOrder` / `moveColumn` leave it where it is, `resizeColumn` and `setColumnVisible` decline it, the frozen prefix counts only the columns in front of it, and the rows cannot be grouped by it.
- `DefaultTableView` normalizes the column order whenever it is set — by the caller, by a restored personalization, by a column selection — so the pinned columns trail it. Every consumer sees that one order: `columns()` lists them last with `ColumnView.pinnedEnd()` set, and `frozenColumnCount()` never reaches into them.
- `TableViewControl` pushes the flag as the per-column `pinnedEnd` state. The client (`TLTableView.tsx`) renders such a cell `position: sticky` with a `right` offset of the widths of the pinned columns behind it plus the reserve the row ends with — in the header the measured scrollbar width, which the header has no vertical scrollbar of its own for, and where the table does not end in a pinned column the column button's width in header and body alike. A table ending in a pinned column has a heading without a label there, so the column selector's cog sits in that heading and the pinned column reaches the right edge. Rows and header row fill the table when the columns are narrower than it; the last *unpinned* column grows into the space left over, in the heading exactly as in the rows. A pinned cell keeps its width, offers no resize handle and no drag, and the header menu offers it neither the freeze boundary, nor a grouping, nor the fit to content.
- `Column.cssClass()` (`DefaultColumn.Builder.cssClass(String)`) names a class the client puts on every cell of the column, its heading included — how the column presents its cells, as opposed to the per-row `cssClass(R row)`. `RowCommandColumn` uses it to drop the text padding and center its frameless button.
- Every unpinned column can be fitted to its content: "Fit width to content" in the header menu, or a double-click on the column's resize handle, sets the width the heading and the rendered cells need and persists it through the same `columnResize` command a drag ends with. Only the rows currently in the DOM are measured, so the fit follows what is displayed, as the virtual scroller renders it.

## Drill-down navigation with `<tile-stack>`

`com.top_logic.layout.view.tiles` provides drill-down navigation. A `<tile-stack path="navPath" initial="products/overview.view.xml"/>` displays the last frame of a path of `TileFrame`s, the `initial` view when the path is empty, and keeps the frames the displayed one covers (see below). The path itself lives on a normal channel of the enclosing view (`List<TileFrame>`), which is the single source of truth: every navigation is a write to that channel.

- **Push**: `<navigate-push view="products/detail.view.xml" input="selectedProduct" bind-input-to="product">` (`NavigatePushCommand`) mounts a view as a new frame. `bind-input-to` maps the command's input onto a channel of the pushed view; further `<bind>` entries capture the *current value* of caller-scope channels at push time. `frame-label` supplies the breadcrumb label — `<static>` for fixed text, `<scripted expr="p -> $p.get(\`tl.accounts:Person#name\`)" inputs="selectedPerson"/>` for one derived from channels at the push moment. Where the stack declares a `<label>` for the pushed view (see below), the push needs no `frame-label` of its own.
- **Pop**: `<navigate-pop>` drops the top frame, `<navigate-pop-to depth="1">` truncates to a given depth. Each of the two is a `ViewCommand` *and*, under the same tag, a `ViewAction` (`NavigatePopAction` / `NavigatePopToAction`) — the command delegates to the action, and the tag is resolved per list type, so the same `<navigate-pop/>` names the command in a `<commands>` list and the action in a `<generic-command>` chain. Leaving a frame is therefore the last step of a longer command: `<generic-command><execute-script .../><navigate-pop/></generic-command>` deletes the displayed object and then leaves the frame that displayed it. Both actions pass the chain's object through as their result, so an action after them still sees it.
- **The path inside a frame**: `<tile-stack bind-path-to="navPath">` registers the stack's own path channel under that name in every mounted frame's context, so a frame view declaring `<channel name="navPath"/>` reads the live path — the declaration is pre-bound and not instantiated locally. `<derived-channel name="depth" inputs="navPath" expr="p -> $p.size()"/>` turns it into the frame's depth, which a command tests via `<executability><visible-if expr="d -> $d > 0"/></executability>`: that is how a Back button hides itself on the initial frame and shows on every pushed one. Without `bind-path-to` a frame sees only the parameters it was pushed with.
- `<tile-breadcrumb path="navPath">` reads the same channel and renders a crumb per frame; clicking a crumb truncates the path. Wrapping the breadcrumb in `<slot-content to="appbar-content">` raises it to the app bar instead of showing it inside the frame.
- A frame's parameters are a snapshot, not live channels: `TileFrame` is the view reference, the label, and the parameter values captured at push time, which become the mounted view's channels. The path channel a `bind-path-to` adds is the exception — that one is the stack's own.
- **The path in the URL**: a `<frame route="person/:person" view="accounts/person-detail.view.xml">` entry inside `<tile-stack>` declares the segments a frame of that view occupies, so the drill-down path becomes part of the address: each frame on the path contributes its route, in the order it was drilled down (`…/person/<id>`). A `<param name="person" expr="p -> objectId($p)" reverse="id -> objectResolve(`tl.accounts:Person`, $id)"/>` converts between the parameter value and the text the URL carries — a parameter without a `<param>` entry is carried as text, which is what a string parameter needs. A `<label class="…ScriptedTileLabel" expr="p -> $p.get(`tl.accounts:Person#name`)" inputs="person"/>` inside the `<frame>` names every frame of that view, evaluated over its parameters as channels: the frame a URL restores and the one a `<navigate-push>` pushes without a `frame-label` of its own — so a frame is named once, at the stack, for every place that pushes it.
- **Restoring, and coming back out**: opening such an address restores the path frame by frame — the participant takes up the route of the next frame, puts it on the path, and is offered the rest of the URL again for the frame above it (`TileFrameRouteParticipant`, one per stack, anchored at the stack's control, so that the path enters the URL where the stack sits in the display). A route naming something that cannot be displayed — a deleted object, a mistyped identifier — restores no frame, and the address bar is corrected to the path that could be restored; a frame view without a `<frame>` declaration has no address, and neither has anything drilled into from it. A URL that names fewer frames than the display shows — the back button out of a drill-down, or a bookmark of the bare view — returns the stack to the view it started in (`RoutingParticipant.resetRoute`). Drilling down and coming back out are one history entry each, whatever the exchanged frames bring and take with them.
- Both demos live in `com.top_logic.demo.react`: `demo/tiles-demo.view.xml` (accounts overview, drill-down to a person detail) and `demo/tiles-multi-demo.view.xml` (one stack per tab, on the channels `navPathA` / `navPathB`). Both stacks bind their path as `navPath`, declare the person frame's route and label, and the two shared frame views carry the depth-guarded Back command. A push is the frame view's row activation: the account table holds the `NavigatePushCommand` as its `<on-activate>`, so a double-click, `Enter`, or the row's chevron button drills into that person.

## URL routing: what ends up in the address bar

`RouteManager` composes the URL below the view servlet path from the segments of the `RoutingParticipant`s the display contains, in the order in which it contains them — sidebar item, then tab, then route parameters — and pushes each change to the browser over SSE (`RouteChangeEvent`, applied by `react-src/bridge/route-sync.ts` via `pushState` / `replaceState`). The displayed participants come from a walk of the control tree (`ReactControl.displayedChildren()`), so a participant that has left the display contributes nothing, whenever it happened to register. `popstate` (browser back / forward) is sent back as a `navigateToRoute` command.

Adopting a URL — a deep link, a reload, a back navigation — works in two steps: `RouteManager.adoptUrl` records it, and its segments are consumed either by participants as they register (a display being built for the first time) or by `resolvePending()` over those already registered (a page rendered into the control tree its window still holds). Activating a route materializes the display below it, which is how nested segments reach their participants. Once the page is written, `finishAdoption()` drops whatever remained unresolved, corrects the address bar to what the display composes, and asks every displayed participant that shows a route the URL did not name — neither activated by the URL nor brought into the display by one of its activations — to `resetRoute()`, which is how a URL naming fewer segments than the display shows returns it. `RouteManager.adoptionId()` identifies the adoption in progress, so that a participant restoring several levels from one URL tells the levels of this URL from those of the last. Every adoption ends: `finishAdoption()` is the normal end and the only place the composed URL is reported — a URL the display settles on that the browser already shows is not reported at all — while `cancelAdoption()` ends one whose URL the display refused (a dirty-form veto), leaving the display untouched and the address bar to the caller's `RouteVetoEvent`. Nothing is reported in between: a participant that leaves or enters the display while a URL is adopted (a page loaded into the control tree its window still holds re-attaches that tree; an activated route replaces the content beside it) reports no address of its own, because the URL a half-built display composes is nobody's address — the walk of the tree it is composed from does not even reach the participants yet. An adoption left open would report the user's next navigation as a replacement instead of a history entry. None of this pushes a history entry: adopting a URL the client already shows is not a navigation, so only a route change reported by a displayed participant does.

- **Sidebar**: every `<nav-item>` contributes its `id` as a segment; `route="custom"` overrides it, `route="none"` opts out (`SidebarElement.resolveRoute`).
- **Tabs**: `ReactTabBarControl` contributes the active tab. A tab bar shown for one case of a `<switch>` removes its segment again when the case changes (via `replaceState`, so the disappearance is not a history step of its own).
- **Tile stack**: the drill-down path of a `<tile-stack>`, one route per frame, for every frame view the stack declares a `<frame>` for (see above). It is the one participant that takes up route after route of a single URL (`RoutingParticipant.acceptsRouteSequence`) and the one that has a state without a route to return to (`resetRoute` empties the path). Give a frame route a static prefix (`person/:person`, not `:person`): a bare parameter pattern matches any single segment and would take up whatever follows the stack in the URL.
- **Route parameters**: `<param-bindings><bind channel="ticketKey" route-param="ticket"/></param-bindings>` on `<view>` binds a channel to one path segment: a channel change writes the segment, a deep link writes the segment's value into the channel. The segment is always the value the channel holds, so a link naming an object that does not exist leaves no segment behind. A URL without the segment says nothing about the value rather than saying there is none: the view keeps what it establishes itself — a default selection, or the selection the session still holds — and the address bar is completed with it, as a replacement rather than a history entry. The value is a string, so a model object is bound through a bidirectional derived channel: `expr="t -> objectId($t)"` maps the object onto its identifier, `reverse="id -> objectResolve(\`mod:Type\`, $id)"` maps the identifier back to the object (`views/tickets.view.xml` in `com.top_logic.demo.react` shows this). `objectId` yields nothing for a transient object and `objectResolve` nothing for an identifier no object of that type carries, so an unusable link simply leaves no segment. A business key works the same way where the URL should be readable — `expr` to the attribute, `reverse` a lookup over `all(...)` — at the price of the uniqueness and URL-safety the key then has to have. An optional `prefix` puts static segments in front of the value — `<bind channel="ticketId" prefix="ticket" route-param="ticket"/>` compiles to `ticket/:ticket` and yields `/view/tickets/ticket/<id>` — which names what the value identifies; without a prefix the compiled pattern is a bare `:param` matching any single segment, and the position of the binding in the URL is what identifies it. The prefix appears exactly when the value does, and a URL carrying the prefix alone matches nothing, so it says nothing about the value. Values are percent-encoded as path segments (`RouteEncoding`, applied by `RoutePattern.produce` and undone by `RoutePattern.match`), so a value containing a slash, a space or a `%` stays the one segment it was written into; static segments of a pattern are used verbatim. Both entry points therefore deliver the route encoded: the client sends `window.location.pathname` on `popstate`, and `ViewServlet` reads the route from the raw request URI rather than from the container-decoded path info.
- **Query parameters**: `<query-bindings><bind channel="filter" query-param="q"/></query-bindings>` on `<view>` binds a channel to one query parameter (`QueryBindingParticipant`). It works like a route parameter, with two differences that follow from the query naming its parameters instead of placing them: the binding occupies no path segment, so it neither depends on nor disturbs the position of anything else in the URL and simply disappears when the channel holds nothing; and the query of an adopted URL is offered to *every* participant (`RoutingParticipant.activateQuery`), each taking the parameters it declares. A change that leaves the path as it is and alters only the query is always reported with `replaceState`, whatever the participant asked for: refining what a page shows - narrowing a filter, picking a sorting - stays on that page, so the back button leaves it rather than walking the terms typed on it. A URL without the parameter says nothing about the value, exactly as for a route parameter: the view keeps what it establishes and the address bar is completed with it. A displayed element list bound to such a channel (a `<table>`'s `<rows>` over its `<inputs>`, an `<object-list>`, a `<calendar>`) is re-read when it starts observing (`RowSourceObserver.attach`), because the URL writes the channel while the display is still being attached - and equally when a display comes back after being detached, which ignored every change meanwhile. Keys and values are `application/x-www-form-urlencoded` (UTF-8), and the parameters follow the display order of the participants contributing them. The value is text, so a `<text-input value="filter"/>` writes it and a `<table>` reads it through an `<inputs><input channel="filter"/></inputs>` of its `<rows>` expression - `views/tickets.view.xml` in `com.top_logic.demo.react` shows this as `/view/tickets/ticket/<id>?q=<term>`.

An encoded slash (`%2F`) in a route value needs a servlet container that accepts such a path — containers reject it by default, as "ambiguous". The embedded Jetty of `tl-ide-jetty` does accept it: `Bootstrap` allows `UriCompliance.Violation.AMBIGUOUS_PATH_SEPARATOR` on the connector's `HttpConfiguration` and sets `ServletHandler.setDecodeAmbiguousURIs(true)`. A standalone Jetty needs the same two settings (`jetty.httpConfig.uriCompliance`, `jetty.servlet.decodeAmbiguousURIs`), a Tomcat 10.1 `encodedSolidusHandling="decode"` on its `<Connector>` (the default `reject` answers 400). Object identifiers never contain a slash, so this concerns bindings on a business key only.

## Filling the available height: the fill contract

A control that should span the height its container offers - instead of growing with its content - carries the CSS class `tlFill`, which the layout CSS turns into `flex: 1; min-height: 0`. That bounds the control only while its container has a definite height itself, so the decision is not local: a container hosting a filling child has to fill in turn, up to the first box that is bounded anyway (the viewport-high mount point, a tab-content region, a scrolling panel body). This chain is a property of the path through the control tree, which is why it lives in the React bridge (`react-src/bridge/fill.ts`) rather than in a CSS selector enumerating the child types allowed to grow.

A container component therefore declares how it takes part:

- `useFill(fills)` — a fixed decision by the control's own configuration: `<panel fill="true">` reports filling, a plain panel does not. Returns the class for the root element.
- `useFillHost(alwaysFills?)` — a container: it fills while it always does (a split panel, whose panes are sized proportionally; a tab bar, whose strip stays pinned) or while one of its children reports filling, and reports that on to its own container. Returns the class plus the host to provide to the children (`<FillProvider host={…}>`).
- `<FillBarrier>` — ends the chain: a region bounded on its own that scrolls what does not fit (a panel body, a tab content area), or a surface laid out apart from the page (a dialog, a window, a drawer). A filling control inside resolves its height against that region, and nothing outside reacts to it.

A container that declares nothing is opaque: a filling control inside it does not grow it, so a new container element that lays out vertical space adds its declaration.

## A tile-stack frame is kept, not rebuilt

`<tile-stack>` holds one frame per stack position (`ReactTileStackControl`): the `initial` view, followed by the frames of the path channel. The frame at the end of the path is the displayed one; the frames it covers keep their control tree and their layout box (`.tlTileStack__frame--covered` takes them out of the flow at the size of the stack and makes them invisible). Returning to a frame - a breadcrumb click, a `<navigate-pop>` - therefore shows it as the user left it: the selected tab, the selected table row, the values being edited and the scroll offsets included. A path change keeps the frames of the longest common prefix and disposes the ones it drops, so a path reconstructed from a URL keeps the frames it names (`TileFrame` compares by view, label and params). A covered frame is rendered but not seen, and the stack reports only the active frame as `visibleChildren()`: the URL is composed from the participants below the visible children, and only those take up a route of an adopted URL, so a tab bar inside a covered frame neither names its tab in the address nor is offered the segment meant for the tab bar of the frame on top.

The hiding sits on a wrapper element the stack renders itself: a frame's content renders its own root element, and a style set on that from the outside is overwritten the next time the content re-renders.

## Object navigation: display targets, the reveal protocol, `<show-object>`

"Show this business object where the application displays objects of its type" is the view-layer counterpart of the classic `GotoHandler` / `LayoutComponent.makeVisible()`. It consists of three generic parts in `com.top_logic.layout.view.navigation`; none of them knows sidebars, tab bars or tile stacks in particular.

### Display targets are declared globally, per type

`DisplayTargetService` (a configured service, `<config service-class="com.top_logic.layout.view.navigation.DisplayTargetService">`) holds the application's targets: per model type an **ordered list of views to show**, each with the channel values to set. The application decides which of the places that display a type is *the* place an object of it is shown at; a view file stays reusable and does not know it is a target, and a library module contributes targets for its own types in its configuration fragment.

```xml
<target type="tl.demo.projectManagement:Ticket">
  <show view="projects/overview.view.xml"><bind channel="project" expr="t -> $t.get(`…:Ticket#milestone`).container()"/></show>
  <show view="projects/milestones.view.xml"><bind channel="milestone" expr="t -> $t.get(`…:Ticket#milestone`)"/></show>
  <show view="projects/ticket-detail.view.xml"><bind channel="ticket"/></show>
</target>
<target type="tl.demo.projectManagement:Contributor">
  <show view="projects/contributor-dialog.view.xml" dialog="true"><bind channel="contributor"/></show>
</target>
```

A `<show>` entry is carried out in one of three ways, decided by how its view is reached:

- **Mounted view** (reachable from the root view through sidebar items, tabs, `<view-ref>`, `<adaptive-detail>` panes or the initial view of a `<tile-stack>`): the mount is revealed and the bindings are written to the view's channels in declared order.
- **Unmounted view**: it is a drill-down frame, pushed onto the tile stack that hosts the previously shown view (the bindings become the frame's channel values, exactly like `<navigate-push bind-input-to=…>`). A chain of such entries rebuilds a drill-down path; frames already on the stack with the same view and values are kept (pop to the longest matching prefix, push the rest), so the frames a target pushes must use the same view refs and channel names as the user's own drill-down.
- **`dialog="true"`**: the view is opened as a dialog with the bindings as initial channel values (the same seam as `<open-dialog>`). It must be the last entry.

A `<bind expr>` is a TL-Script function of the object being shown and defaults to the object itself. A frame entry carries its breadcrumb label as `<label>` or, computed from the shown object, as `label-expr` — the same expression the drill-down's `<frame-label>` uses, because `TileFrame` equality includes the label. Resolution (`DisplayTargets.resolve`): the most specific type wins (an exact class beats a generalization, following `TLClass.getGeneralizations()`); among targets for the same type, the one whose first view is mounted **nearest** to the view that triggered the navigation (longest common mount prefix), then the one flagged `default="true"`, then the first declared. `hasTarget(type)` is the question "can objects of this type be shown at all?" — it decides whether a value is rendered as a link. At startup the service checks every `<bind channel>` against the channels the view declares and logs a configuration error for a mismatch (a typo in a channel name is found without clicking through the app).

### Where a view is mounted is known statically

Sidebar items and tabs create their content lazily, so the mount of a view cannot be read from the control tree. `UIElement.getChildGroups()` reports an element's content **statically**, as `ChildGroup`s: a keyed group for each child a container addresses by a key of its own (sidebar item id, tab id, `AdaptiveDetailElement.Config.SELECTOR` / `DETAIL`, `TileStackElement.Config.INITIAL`), an unkeyed group for content a container shows unconditionally, and an `EmbeddedView` for a `<view-ref>`. `ViewMounts.forRootView(rootViewRef)` walks these groups over all reachable view files and yields, per view file, its `MountPath`s: the sequence of `MountStep(container element, key)` from the root. The scan is cached per root view and invalidated together with the view files. **A container element that holds child elements must implement `getChildGroups()`** — a container that does not report its children hides every view below it from navigation.

### The reveal protocol

Every control that shows one of several children implements `com.top_logic.layout.react.reveal.ChildRevealer` — `revealChild(key)` makes the child addressed by `key` the displayed one, creating it if needed, and throws `ChannelVetoException` when unsaved changes stand in the way: `ReactSidebarControl` (item id), `ReactTabBarControl` (tab id), `ReactAdaptiveDetailControl` (selector/detail), `ReactTileStackControl` (`initial`, or `frame<n>` via `frameKey(n)` = pop to that frame), and a `DialogRevealer` around a `DialogHandle` (closes the dialogs above it). Every keyed container appends a `RevealStep` to the **`RevealPath`** scope when it derives a child's `ViewContext` (`context.withScope(RevealPath.class, path.append(this, key))`), and every view instance and every revealing control announces itself in the window's **`RevealRegistry`** (`ViewContext.getRevealRegistry()`, one per root context and inherited like the slot registry) under that path, unregistering when its control is cleaned up (cached hidden content stays registered while alive). Revealing a mounted view walks its `MountPath` from the root: at each step the registered container at the current prefix reveals the next key — which creates lazily built content, whose own containers and views register on the way — and the view instance found at the full path finally receives the bindings. Vetoes are handled as `<write-channel>` handles them: the dirty-confirm dialog, then the step is retried; cancelling aborts the chain.

### Entry points

- **`<show-object/>`** (`ShowObjectAction`) in a `<generic-command>` chain shows the chain's input object and passes it on; no input passes through unchanged; a selection of exactly one object shows that object. `<generic-command input="selection"><show-object/></generic-command>` is the whole configuration of a "go to" button. Java code calls `ObjectNavigation.show(context, object, continuation)`.
- **`ReactContext.getObjectNavigator()`** (`com.top_logic.layout.react.navigation.ObjectNavigator`: `canShow(value)`, `show(context, value)`) is the seam for controls in `com.top_logic.layout.react`, which cannot depend on the view layer; the view layer answers it with `DisplayTargetNavigator`. Through it, **object values displayed read-only are links automatically** wherever a target exists for their type: `ReactResourceCellControl` (tree nodes via `MetaResourceControlProvider`, and any cell built with `useLink`), the read-only values of `ReactDropdownSelectControl` (which is what reference attributes in `<table>` cells and view-mode `<form>` fields render as), and `tlObject` anchors in read-only structured text (`ReactWysiwygControl`, command `showObjectLink`, resolved with `TLObjectLinkUtil` like the classic `OpenTLObjectLink`). The TL-Script functions `htmlObjectLink(object, label)`, `htmlSource(content)` and `htmlText(source)` (`HtmlFunctions` in `com.top_logic.layout.wysiwyg`) write such an anchor and read or write the HTML source of a structured-text attribute, e.g. to append an object reference to a comment.

- **The WYSIWYG editor carries configured commands and inserts what they write.** The editor is chosen for a field by `<input-control class="com.top_logic.layout.react.wysiwyg.WysiwygControlProvider">`, which takes `<commands>` — ordinary view commands (`ViewCommand.Config`, e.g. `<generic-command>`) — and an optional `insert-channel`. The commands run in a child `ViewContext` of the field's view context: they see the channels of the surrounding view, so they take their input from it and hand it on to the dialogs they open, and beside those channels they see the insertion channel the editor declares. Markup written to that channel is inserted at the cursor of the editor (`ReactWysiwygControl.insertAtCursor`, state `insert` = `{seq, html}`; the client inserts it once per `seq`, reports the resulting text, and the request is taken back). Commands placed in a toolbar — the default placement — are rendered as a `ReactToolbarControl` in state `toolbar`, which the client renders beside the formatting buttons.

  ```xml
  <field attribute="content">
    <input-control class="com.top_logic.layout.react.wysiwyg.WysiwygControlProvider"
      insert-channel="insert"
    >
      <commands>
        <generic-command image="css:ri-links-line" input="ticket">
          <label><en>Reference ticket...</en></label>
          <executability><null-input-disabled/></executability>
          <open-dialog dialog-view="tickets/reference-ticket.view.xml">
            <bind channel="context" to="ticket"/>
            <bind channel="result" to="insert"/>
          </open-dialog>
        </generic-command>
      </commands>
    </input-control>
  </field>
  ```

  The dialog picks whatever it likes in whatever way it likes (a table, a search, a tree) and publishes the markup on its result channel: `<generic-command input="ticket"><execute-script function="t -> htmlObjectLink($t)"/><write-channel name="result"/><close-dialog/></generic-command>` is the whole contract, and `context` gives it the object the text is written for. Nothing about the editor knows what an object link is: it inserts the markup it is handed.

The demo (`com.top_logic.demo.react`): the *Projects* drill-down (project → milestone → ticket → detail) with targets for its types in `demoReactConf.config.xml`, the contributor dialog target, `tl.accounts:Person` shown in the tiles demo, and the object-list comments, whose editor offers a "Reference ticket…" command opening `tickets/reference-ticket.view.xml`.
