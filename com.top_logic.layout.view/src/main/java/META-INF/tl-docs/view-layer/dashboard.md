---
description: Read before configuring a <dashboard> in a .view.xml - fixed <tile>s and <tiles> computed from a list of objects (items, observed-types, element-channel, element-as, width / row-span / label scripts, <action> run with the object; TileListElement), the ids of computed tiles (<tiles id>:<object key>), the mixed order of fixed and computed tiles, the arrangement the user edits (order, hidden tiles) stored in the PersonalConfiguration (personalize-by for one arrangement per displayed object) or by the application (arrangement / store-arrangement scripts over the inputs of the dashboard, <arrangement-access> deciding who may change it), and the property entries holding the tiles (DashboardElement, TileDeclaration).
order: 63
---

# Dashboards

A `<dashboard>` (`DashboardElement`) arranges tiles in a responsive grid of definite row height (see [the height contract](doc:view-layer/basics)). The user rearranges the tiles in the edit mode of the dashboard: moves them, hides them, and adds hidden ones again through the menu of the surrounding toolbar.

## Entries: `<tile>` and `<tiles>`

The tiles are written directly into the `<dashboard>`. They form its property `entries` (`DashboardElement.Config#getEntries()`, entries of the type `TileDeclaration.Config`); an entry is one of:

- `<tile>` (`TileElement`): one fixed tile with its content, `width`, `row-span`, `label`, `optional`, `<access-control>`, `<visibility>` and an `<action>` the tile runs when the user activates it (a click, `Enter`).
- `<tiles>` (`TileListElement`): one tile per object of a computed list.

```xml
<dashboard id="project-cockpit" inputs="project">
	<tile id="project" width="half" row-span="2">
		...
	</tile>
	<tiles id="milestone"
		inputs="project"
		items="if($project == null, [], $project.get(`my.module:Project#milestones`))"
		observed-types="my.module:Milestone"
		width="$element.get(`my.module:Milestone#tileSize`)"
		label="$element.get(`my.module:Milestone#name`)"
	>
		<view-ref view="projects/milestone-tile.view.xml">
			<bind channel="element" to="element"/>
		</view-ref>
		<action class="com.top_logic.layout.view.command.GenericViewCommand">
			<show-object/>
		</action>
	</tiles>
	<tile id="planning" width="half">
		...
	</tile>
</dashboard>
```

A `<tiles>` entry works like an [`<object-list>`](doc:view-layer/lists):

- `items` computes the objects from the values of its `inputs`. It is evaluated again when an input changes, when a displayed object changes, and when an object of the `observed-types` is created, changed or deleted.
- The content is instantiated once per object, with the object published on the `element-channel` (default `element`). The tile of an object that stays keeps its controls; a new object gets a new tile, the tile of a vanished object disappears.
- `width`, `row-span` and `label` are scripts over the inputs and the object, named `element` or as `element-as` says. `width` yields the name of a width (`quarter`, `third`, `half`, `two-thirds`, `full`) as text or as a classifier of that name; an unknown value is logged and the tile is a quarter wide. Without `row-span` a tile spans one row; without `label` it is named after its object.
- `<action>` runs with the object as its main argument, named like the object. The tile is activated like a fixed tile, through the shared activation of the item containers (`ItemActivation`, `CommandItemActivation`): the rules of the command decide per object whether the tile is an entry point, refused with a reason, or only a display.

The content of every computed tile is created when its object appears, also while the tile is hidden.

## Tile ids and order

The id of a fixed tile is its `id`, the id of a computed tile is `<id of the tiles entry>:<key of the object>` - the id of a persistent object, the text of any other value. The ids are what a stored arrangement refers to, so the `id` of a `<tiles>` entry is mandatory and must stay stable.

Without a stored arrangement, the tiles appear in the order of their entries, the tiles of a `<tiles>` entry at its place in the order of `items`. With one, the stored order comes first: stored ids without a tile are skipped, and the tiles without a stored position - a tile of an object created later, a tile added to the configuration - follow in declaration order.

## Where the arrangement is stored

### Per user: the personal configuration

By default each user has an arrangement of their own; the order and the tiles shown or hidden deviating from their default are stored in the `PersonalConfiguration` under keys formed from the dashboard `id`. An anonymous session offers no editing at all: all anonymous visitors share one account.

`personalize-by` adds a key computed from the `inputs` of the dashboard, so that the dashboard has an arrangement of its own per displayed object:

```xml
<dashboard id="project-cockpit" inputs="project" personalize-by="$project">
```

A persistent object adds its id, any other value its text; without a value, the keys of the dashboard alone are used. The name is not `personalization-key`, which every `UIElement` already has as a fixed string.

### By the application: `arrangement` and `store-arrangement`

With `arrangement`, the application stores the arrangement, e.g. at the displayed object, where it applies to all users:

```xml
<dashboard id="project-cockpit"
	inputs="project"
	arrangement="if($project.get(`my.module:Project#tileOrder`) == null, null, {
		'order': $project.get(`my.module:Project#tileOrder`).split(','),
		'hidden': $project.get(`my.module:Project#hiddenTiles`).split(',')
	})"
	store-arrangement="{
		$project.set(`my.module:Project#tileOrder`, $order.join(','));
		$project.set(`my.module:Project#hiddenTiles`, $hidden.join(','));
	}"
>
	<arrangement-access>
		<operation name="ArrangeCockpit" object="$project" type="my.module:Project"/>
	</arrangement-access>
	...
</dashboard>
```

- `arrangement` sees the inputs and yields a map with the tile ids in their order under `order` and the ids of the hidden tiles under `hidden` (`DashboardElement.Config.ORDER` / `HIDDEN`), or nothing while none is stored. A tile mentioned in neither list has its default visibility.
- `store-arrangement` runs in a transaction after the user moved or hid a tile. It sees the inputs, `$order` (the ids of all current tiles in their order) and `$hidden` (the ids of the hidden ones).
- `<arrangement-access>` holds the checks a command declares in its `<access>` (`<operation>`, `<create>`; see [access control](doc:view-layer/access-control)), over the inputs - the first input is the checked object where a check names none. A user the checks refuse, or any user where `store-arrangement` is missing, is offered no edit mode; a move or a hide the client sends anyway changes nothing, and the dashboard publishes its unchanged tiles again.

When an input changes - another object is displayed - the dashboard reads the arrangement again, and decides again whether the user may change it.

## Demo

`tl-demo-react`, *Projects*: the cockpit of a project (`WEB-INF/views/projects/milestones.view.xml`) shows one tile per milestone between the fixed tiles, as wide as the *Tile size* of the milestone; a click opens the milestone. The arrangement is stored at the project; only an administrator of the project (operation `ArrangeCockpit`) may change it.
