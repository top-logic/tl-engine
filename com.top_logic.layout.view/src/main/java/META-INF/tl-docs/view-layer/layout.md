---
description: Read before arranging content in a .view.xml with <stack>, <grid>, <columns>, <object-list>, <kanban-board>, <visible-if> or <accordion>.
order: 20
---

# Layout

## Layout: stack and grid

Two elements arrange content, and between them they cover the layouts an application would otherwise
write CSS for: a centered content column, a row spread across its width, a row of chips that falls
into further lines on a narrow screen.

**`<stack>`** (`StackElement`, `ReactStackControl`, `TLStack`) puts its children in one line:

- **`direction`** — `column` (the default) or `row`.
- **`gap`** — `default`, `compact` or `loose`; the space between the children, taken from the
  spacing tokens rather than given as a length.
- **`align`** — across the direction: `stretch` (the default), `start`, `center`, `end`.
- **`justify`** — along the direction, which is to say what happens with the space left over where
  the children together are smaller than the stack: `start` (the default), `center`, `end`,
  `space-between`, `space-around`, `space-evenly`.
- **`wrap`** — `true` lets the children flow into further lines once they no longer fit next to
  each other, instead of shrinking them into one line.
- **`max-width`** — a CSS length the stack is bounded to; see below.

**`<grid>`** (`GridElement`) places its children in as many columns as fit and reflows them with the
available width. Its options are `GridOptions`, which `<object-list layout="grid">` shares:

- **`min-column-width`** — the width a column must have at least (`16rem` by default); the number of
  columns follows from it and the available width.
- **`max-columns`** — the largest number of columns to place, so a handful of elements does not
  spread into a thin row on a wide screen. A bounded grid still drops columns as it narrows.
- **`gap`** — as on the stack.
- **`max-width`** — a CSS length the grid is bounded to; see below.

A content column that stays readable on a wide screen is a bounded stack:

```xml
<stack
	gap="loose"
	max-width="60rem"
>
	<text
		label="Quarterly report"
		variant="headline"
	/>
	<text label="Figures as of yesterday."/>
</stack>
```

A row that spreads a title and the actions belonging to it to the opposite ends, and falls into
further lines where the screen is too narrow for them:

```xml
<stack
	align="center"
	direction="row"
	justify="space-between"
	wrap="true"
>
	<text
		label="Open tickets"
		variant="title"
	/>
	<button .../>
</stack>
```

**A bounded container is centered.** `max-width` says the largest width the container takes; the
space left over is split between its two sides, so the content sits in the middle of the page rather
than against its left edge. Below the bound nothing changes, so the same element still fills a phone
screen. The bound travels as an inline `max-width` — it is a value, not a kind of layout — while the
centering is the shared class `tlBounded` (`width: 100%; margin-inline: auto`), which a stack and a
grid carry alike. The explicit width is what keeps the auto margins from shrinking the container to
its content.

Bounding the width leaves the **fill contract** (see below) alone: `max-width` and the auto margins
work across the direction of a column, while filling is about the height a container takes from its
own container. A bounded stack that hosts a filling child still carries `tlFill` and still reports
filling upwards, so a table inside a centered content column keeps bounding its own scroll viewport.

## A page of weighted columns: `<columns>`

`<columns breakpoint="48rem" gap="default">` (`ColumnsElement`) lays a page out in columns of unequal width and reflows it to a single column when the space gets narrow. Each child is a `<column weight="2">` (`ColumnElement`); a column takes a share of the width in proportion to its weight (weight 1 by default), and its own children stand below each other over the full width of the column. Below the breakpoint the columns stack in the order they are written — the main column first, the side column below it.

- **The breakpoint is the width of the element itself**, not the width of the browser window. The same page therefore stacks inside a narrow pane of a wide window exactly as it does on a phone. No measurement is involved: the client gives each column `flex: <weight> 1 calc((<breakpoint> - 100%) * 999)` in a wrapping flex row, so the browser layout decides.
- **The page scrolls, the columns do not.** A column is as tall as its content and is not stretched to the height of a taller neighbour; the layout is as tall as its tallest column. A long main column beside a short side column reads as one page.

Which of the arrangement elements fits:

| Element | Use it for |
| --- | --- |
| `<columns>` | A page of a few columns of *deliberately different* width that folds to one column when narrow. |
| `<grid>` | Many elements built alike, placed in as many equal columns as fit (`min-column-width`, `max-columns`). |
| `<group>` | A section of the fields of a `<form>` or `<fields>`, under a heading and optionally folded away; it keeps the columns of that grid. |
| `<stack direction="row">` | A row of elements that neither grow to a share of the width nor wrap. |
| `<split-panel>` | Panes with splitters the user drags; fills its box, scrolls per pane, and never folds. |
| `<dashboard>` | Tiles of definite row height whose order the user personalizes. |

```xml
<columns
	breakpoint="48rem"
	gap="default"
>
	<column weight="2">
		<card variant="outlined">
			<title>
				<en>Main</en>
			</title>
			<text>
				<label>
					<en>The wide column.</en>
				</label>
			</text>
		</card>
	</column>
	<column>
		<card variant="outlined">
			<title>
				<en>Side</en>
			</title>
			<text>
				<label>
					<en>Half as wide as the main column.</en>
				</label>
			</text>
		</card>
	</column>
</columns>
```

The client classes an application styles against are `.tlColumns`, `.tlColumns--gap-<gap>` and `.tlColumns__column`. The demo is `com.top_logic.demo.react`'s `WEB-INF/views/demo/columns-demo.view.xml`.

## Repeating a template over a computed list: `<object-list>`

`<object-list>` (`ObjectListElement`) instantiates its `<item>` content once per object of a list it computes itself, with the object published on a channel (`element-channel`, `element` by default). What the list holds is decided by TL-Script over channel values, not by a containment the element knows about, so the same element serves the comments of a ticket, a catalogue of products and the result of a search.

- **Inputs.** `inputs` names the channels the functions read, either as the comma-separated attribute `inputs="catalogue, term"` or as nested `<inputs><input channel="catalogue"/></inputs>` (`Inputs`, the same notation every `inputs` property of the view layer takes). Their values are the leading positional arguments, in declaration order: `items` is `...inputs -> elements`, `link` and `remove` are `...inputs -> element -> ...`, the element coming last. A list with no inputs is a repeater over a plain query — `items="all(\`test.flowchart:FlowNode\`)"` — and follows the model through its `observed-types`.
- **With and without a container.** A read-only list configures `items` alone. A list that composes objects adds `element-type` plus the `<new-element>` content bound to `new-element-channel`, whose command chain persists the draft with `<link-element>`, and `<remove-element>` inside the `<item>` content detaches one. The composer appears only while *every* input holds a value, because an element is composed to be attached somewhere; `link` is what attaches it, so any containment style works — a composite reference, a back-reference, an association.
- **Arrangement.** `layout="list"` (the default) stacks the elements in a column; `layout="grid"` places as many next to each other as fit, taking the shared grid options `min-column-width` (16rem by default), `max-columns`, `gap` and `max-width` — the same options `<grid>` takes (see [Layout: stack and grid](#layout-stack-and-grid)). `gap` and `max-width` apply to either arrangement, the column options to a grid.
- **Item wrapper and staggered entrance.** The client wraps every element in `<div class="tlItem tlObjectList__item" style="--tl-item-index: 0">` carrying the 0-based position (`ObjectListElement.ITEM_CSS_CLASS`). The engine ships the position, not the animation: an application composes a per-item delay from it. The arrangement is what an application's rule addresses next to that class — a grid puts its items into a `.tlGrid`, a list into a `.tlStack` — so an entrance can be given to the cards of a grid while the rows of a list keep appearing at once. Items are reused by key, so an element that stays through a change of the inputs keeps its DOM node and does not animate again — only the ones that appear do.

```xml
<object-list
	gap="default"
	inputs="search"
	items="search -> all(`test.flowchart:FlowNode`).filter(n -> $search.isEmpty() || $n.get(`test.flowchart:FlowNode#name`).stringContains($search))"
	layout="grid"
	max-columns="3"
	min-column-width="16rem"
	observed-types="test.flowchart:FlowNode"
>
	<item>
		<card>
			<text input="element"/>
		</card>
	</item>
	<empty-text>
		<en>No flow node matches the search.</en>
	</empty-text>
</object-list>
```

```css
@keyframes tlDemoRepeaterFadeUp {
	from { opacity: 0; transform: translateY(8px); }
	to { opacity: 1; transform: none; }
}

/* The items of a grid arrangement; a list arranges its items in a `.tlStack` instead. */
.tlGrid > .tlObjectList__item {
	animation: tlDemoRepeaterFadeUp 300ms ease-out both;
	animation-delay: calc(var(--tl-item-index) * 60ms);
}

@media (prefers-reduced-motion: reduce) {
	.tlGrid > .tlObjectList__item {
		animation: none;
	}
}
```

The demo is `com.top_logic.demo.react`'s `WEB-INF/views/demo/repeater-demo.view.xml` with `style/tl-demo-react.css`.

## Cards in columns: `<kanban-board>`

`<kanban-board>` (`KanbanBoardElement`, control `ReactKanbanBoardControl`, client `TLKanbanBoard`) distributes computed objects into columns of cards. Over its `inputs`, `columns` computes the column values (the classifiers of an enumeration - `all(\`my:Status\`)` - or arbitrary objects), `items` the objects, whose order is their order within a column, and `column` maps an object to its column value; an object whose column value is none of the columns is not shown. The header shows `column-label` (`column -> label`, the label of the column value by default) and the number of cards.

The `<card>` content is instantiated per object with the object on `element-channel` (`element` by default, as for `<object-list>`), by the same keyed `TemplateInstances` the items of `<object-list>` use: a card of an object that stays keeps its controls, also when the object moves to another column. `selection` names the channel a clicked card writes its object to; the card of the object the channel holds is highlighted (`KanbanSelectionBinding`, a `SelectionChannelBinding`). The board follows its displayed objects and the `observed-types`, so changing the attribute the column is computed from moves the card. A card selection is recorded as `selectCardByKey` naming the object, since the client card keys are allocated per session.

**Drag and drop.** `<drag kind=…/>` (`KanbanDragConfig`: the optional `kind` of the drag; board-wide `executability`; per-card `card-executability`) makes the cards drag sources exactly like table rows - same payload, so a card can be dropped on a `<table>` accepting its kind and table rows on the board. Each `<drop accept=… target-channel=… refuse-if=…>` (`accept` lists the kinds taken, none for every drag; see [tables](doc:view-layer/tables#drag-and-drop-of-table-rows); the shared `DropConfig`, compiled with `DropSignature.ONTO`) makes the columns drop targets: the **column value** is the target - written to `target-channel` before the action chain runs, passed to `target-executability` and to `refuse-if` (`column -> objects -> reason`). While a drag hovers a column, the client probes the server (`dropProbe`), and a refusal is shown at the column with its reason next to the pointer. A recorded drop becomes `dropObjects`, naming the objects and the place on the board by business identity: an insertion (`ordered`) into the column value before the card it was dropped before, none at the end. On the wire, a drop beside a card is reported as its `upper` or `lower` zone, a drop on the column beside the cards as `none`.

**Order within a column: `on-reorder`.** `column -> objects -> …`, run in a transaction, receives the column value and the objects of that column in their new order: those displayed when the drop was made, with the dropped objects placed where they were dropped. It typically numbers the objects in an attribute `items` sorts by. Order of execution:

- a drop of cards within the column they are in runs `on-reorder` alone (no `<drop>` chain); without `on-reorder` such a drop is not accepted, and the client shows no insertion line there. With `on-reorder`, the board accepts the kind of its own drags; a board dragging without a kind accepts any drag then, and a drag from elsewhere is decided by the `<drop>`s;
- a drop on another column (or from another control) runs the matching `<drop>` chain first and, once that chain has run to its end (not after an abort), `on-reorder` with the new order of the target column - so the card lands where it was dropped.

With `on-reorder`, the client draws an insertion line before or after the card under the pointer, or at the end of the column; without it, a drop is made on the column as a whole.

The shared server half of the drop protocol (wire names, resolving the dragged objects, probe verdicts, recording) is `com.top_logic.layout.react.control.dnd.DropSupport`, used by `TableViewControl` and `ReactKanbanBoardControl` alike; the refusal hint of both clients is `react-src/controls/drop-hint.ts`.

The demo is `com.top_logic.demo.react`'s `WEB-INF/views/board.view.xml` (nav item "Board"): dragging a ticket to another column sets its status, a closed ticket cannot be dropped on "Open", and `on-reorder` numbers the tickets in the `order` attribute.

## Content shown under one condition: `<visible-if>`

`<visible-if input="ch" expr="x -> …">` (`VisibleIfElement`) shows its children while a TL-Script predicate over the value of the `input` channel holds, and nothing while it does not. It is the short form of a `<switch>` with a single case and no default, and it is that literally: the element builds the `ReactSwitchControl` of such a switch, so the re-evaluation, the observation and the disposal are the ones of a `<switch>`. Several alternatives of which one is shown at a time stay a `<switch>`.

- The condition is re-evaluated on a new channel value and on a change of the object the channel holds - a condition usually decides by an attribute of that object, which is edited without the channel value changing. A condition reaching beyond the input object (deciding by an attribute of its container, say) names the types it navigates to in `observed-types`, since a change of another object is invisible to the input's own observation.
- Content that is hidden is disposed rather than kept alive, so it leaves no contributions - a form's edit / save / cancel commands, a slot contribution - behind in the enclosing scope. While the condition keeps holding, the content stays as it is and follows its own channels.
- Not to be confused with the `<visible-if>` inside a command's `<executability>` (`com.top_logic.layout.view.command.VisibleIf`), which carries the same tag name and decides whether a *command* is offered for its input.

**Conditional content takes the size of what it shows.** A `<switch>` and a `<visible-if>` render through the deck pane (`TLDeckPane`, `.tlDeckPane`), a column flex box that states no size of its own: a deck showing content of its own size is exactly as wide and as high as that content, so conditional content stands inline - in an app bar, beside a breadcrumb, in a row of buttons - instead of claiming the box around it. A deck whose content fills takes part in the fill contract as a container (see below) and is then the bounded box the child's height resolves against.

The app bar of `com.top_logic.demo.react` shows both this and the two ways an unlabelled input says what it is for. `WEB-INF/views/tickets.view.xml` raises its jump-to box into the shell's app bar with `<slot-content to="appbar-content">` - the contribution belongs to the view that owns the `ticket` channel, and the shell knows nothing of it - and puts the name of the selected ticket beside it, shown only while a ticket is selected:

```xml
<slot-content to="appbar-content">
	<stack
		align="center"
		direction="row"
		gap="compact"
	>
		<value-input
			label-position="hide-label"
			value="jump"
		>
			<label>
				<en>Jump to ticket</en>
			</label>
			<placeholder>
				<en>Jump to ticket</en>
			</placeholder>
			<on-submit>
				<execute-script function="name -> all(`demo.tickets:Ticket`).filter(t -> $t.get(`demo.tickets:Ticket#name`) == $name).firstElement()"/>
				<write-channel name="ticket"/>
			</on-submit>
		</value-input>
		<visible-if
			expr="t -> $t != null"
			input="ticket"
		>
			<text
				input="ticket"
				overflow="ellipsis"
			/>
		</visible-if>
	</stack>
</slot-content>
```

## Collapsible sections: `<accordion>`

`<accordion>` (`AccordionElement`, control `ReactAccordionControl`) stacks sections, each with a header — label, optional icon, optional commands — and a body the user expands or collapses. Like the tabs of a `<tab-bar>`, the sections are keyed by their `id`, so a configuration fragment of another module adds, repositions (`config:position`) or overrides a single section; a section shares `id`, `label`, `icon`, `<access-control>` and the content children with a tab (`ContentSectionConfig`).

```xml
<accordion exclusive="false">
	<section id="general" icon="css:bi bi-gear" expanded="true">
		<label><en>General</en><de>Allgemein</de></label>
		<commands>
			<generic-command placement="TOOLBAR" …>…</generic-command>
		</commands>
		<form input="item">…</form>
	</section>
	<section id="advanced">…</section>
</accordion>
```

- **Attributes.** `exclusive` (default `false`): at most one section is expanded at a time, expanding one collapses the other; all may be collapsed. `personalize` (default `true`): the expansion is remembered per user. `command-display` (default `icon-only`): how the header buttons show icon and label. `css-class` as for every element.
- **Sections.** `expanded` (default `false`) is the expansion the section starts with; in an exclusive accordion only the first section written as expanded is. A section the current user may not see (`<access-control>`) is left out entirely, and its `<access-control>` scope is the security scope of the content's command rules, as for a tab. A section without `<label>` shows its `id`.
- **Header commands.** Like a `<panel>`, every section is a `CommandScope` for its content, with or without `<commands>` of its own. Its header shows a live toolbar (`ToolbarBuilder.buildLive` over `TOOLBAR` then `BUTTON_BAR` — a section has no footer, so button-bar commands appear in the header after the toolbar ones) built with the accordion, so the header commands are there whether the section is expanded or not. Commands the content contributes (a form's edit and save commands) join the header once the section is first expanded and its content created. A header without commands renders an empty toolbar, which takes no space (`.tl-accordion__actions:empty`).
- **Lazy content.** The content of a section is created when the section is first expanded and kept while it is collapsed — what the user entered there survives collapsing, and collapsing asks nothing about unsaved changes. A section's content gets the personalization segment `section` and the slot path segment of the section `id`; it shares the channels of the enclosing view.
- **Personalization key.** The expansion is stored as a JSON map from section `id` to `true`/`false`, under the element's `personalization-key` if one is written, else under the view context's personalization key plus `.accordion`. A remembered entry overrides the written `expanded`. A change writes only the entry of the changed section back, so two accordions sharing one key keep each other's entries as long as their section ids differ — two personalized accordions in the same view context therefore need **distinct section ids or a `personalization-key` each**, otherwise they share their expansion.
- **Reveal.** The accordion is a container of the reveal protocol, keyed by section `id`: revealing something inside a collapsed section expands it (and, in an exclusive accordion, collapses the others).
