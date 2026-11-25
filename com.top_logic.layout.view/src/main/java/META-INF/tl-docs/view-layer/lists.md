---
description: Read before showing a selectable list of objects in a .view.xml in the style of a contact or message list - <data-list> (DataListElement, client TLDataList, design system tl-list) with rows, inputs, observed-types, lines (attribute / value / emphasis), <leading> avatar or icon, <trailing> metadata text or badge, group-by, selection and count channels, search, a list in a collapsible <side-rail>, header commands, row-commands / row-channel / row-actions, row-css-class, and a <context-menu> around the list.
order: 62
---

# Lists of objects: `<data-list>`

A `<data-list>` shows a set of objects as a list of entries in the style of a contact or message
list: in front an avatar or an icon, then one or more lines of text, after them metadata text or a
badge, and a trigger for the commands of the entry. The entries can be grouped under headings and
searched; a click selects an entry. It is the element of choice for
a master-detail picker of moderate size. It renders all of its entries - for large sets of objects,
use a [table](doc:view-layer/tables), which loads its rows as they scroll into view.

The demo application shows it under *List* (`demo/data-list-demo.view.xml` of
`com.top_logic.demo.react`).

```xml
<channels>
	<channel name="contact"/>
</channels>

<context-menu input="contact">
	<data-list
		observed-types="Contacts:PersonContact, my.module:Ticket"
		selection="contact"
	>
		<title>
			<en>Contacts</en>
			<de>Kontakte</de>
		</title>
		<rows>all(`Contacts:PersonContact`)</rows>
		<group-by><![CDATA[c -> $c.get(`Contacts:PersonContact#name`).subString(0, 1).toUpperCase()]]></group-by>
		<leading name="c -> $c.get(`Contacts:PersonContact#name`)"/>
		<lines>
			<line attribute="name"/>
			<line attribute="email"/>
		</lines>
		<trailing
			display="badge"
			value="c -> $c.referers(`my.module:Ticket#assignee`).size()"
		/>
	</data-list>
	<commands>
		<generic-command inputs="contact" placement="CONTEXT_MENU">…</generic-command>
	</commands>
</context-menu>

<form input="contact">…</form>
```

## Rows

- `rows` is a TL-Script expression computing the objects, the same as the rows of a `<table>`. The
  values of the `<inputs>` channels are its arguments, in order (`<inputs><input channel="…"/></inputs>`).
- `observed-types` (comma-separated type names) makes the list follow the model: a created,
  changed or deleted object of one of these types runs `rows` again, as does a new value of an
  input channel. An expression reading objects of other types - the tickets counted for a contact -
  names those types too, so that a change there updates the entry.
- `row-css-class` is a function of one row computing additional CSS classes for the entry
  (`tl-list__item`) - the hook for a state the application styles itself (a done step, a running
  job). `css-class` is the class of the list itself, as on every element.

## The entry

- `<lines>` holds the lines of an entry, top to bottom - the columns of a table, stacked. A
  `<line attribute="…"/>` shows an attribute of the row object, a `<line value="row -> …"/>` a
  computed text. `emphasis` is `primary` or `secondary`; without it the first line is primary and
  every further one secondary. A secondary line that is empty for a row is not drawn. Without lines
  an entry shows the label of its object.
- `<leading name="row -> …" image="row -> …"/>` stands in front of the lines: an avatar (part
  `Avatar`) showing the picture of the row or the initials of its `name`, on the color category the
  name maps to. `image` follows the rules of an icon expression: an icon, the text of an icon
  resource (`css:bi bi-building`) or image data of at most 32 KB. An icon of an icon font is drawn
  as icon instead of the avatar; a picture is drawn in the avatar.
- `<trailing attribute="…" | value="row -> …" display="text|badge"/>` stands after the lines:
  metadata text (`text`, the default, e.g. a date) or a badge (part `Badge`) for a count or a short
  status. An empty value - and `0` as badge - shows nothing. A number is written in the number
  format of the user.

## Groups, search, a collapsible list

- `group-by` is a function of one row computing the heading of its group. The groups stand in the
  order of their first row, so `rows` sorted by the same key gives sorted groups. Without it the
  list is flat.
- `search` (default on) shows a search field filtering the entries by the text of their lines, in
  the browser.
- `title` and `empty-text` name the list and its empty state.
- A list that collapses to a narrow strip, and opens as a drawer over its neighbours where its place
  is narrow, stands in a [`<side-rail>`](doc:view-layer/side-rail) with
  one section; the list itself has no collapse of its own. A list with a title of its own takes a
  section without header, and `dismiss-on` naming the list's `selection` closes the drawer on a pick:

  ```xml
  <side-rail drawer-below="44" width="264">
  	<section name="contacts" header="false" icon="css:bi bi-people" dismiss-on="contact">
  		<label><en>Contacts</en></label>
  		<data-list selection="contact" ...>
  			<title><en>Contacts</en></title>
  		</data-list>
  	</section>
  </side-rail>
  ```

## Selection and commands

- `selection` is a two-way channel holding the selected object: a click on an entry writes it, a
  value written from elsewhere is shown as selected. `count` is a channel receiving the number of
  entries whenever they change.
- `<commands>` are buttons above the entries, ordinary view commands rendered as command buttons:
  they follow their rules - hidden, or disabled with the reason as tooltip.
- `<row-commands>` are offered on a single entry, in a menu opened by the trigger of the entry. The
  entry reaches the commands through the local channel `row-channel` (default `row`), which a command
  names in its `inputs`; its `<executability>` rules decide on the entry the menu was opened on.
  `row-actions` says when the trigger shows: `hover` (default, on pointing at the entry and on
  keyboard focus in it, always on a touch device), `always` or `none`. The menu is the frame's, so
  a list outside an `<app-shell>` shows no trigger.

## Context menu

A `<context-menu input="…">` whose input is the `selection` channel of the list offers its commands
(`placement="CONTEXT_MENU"`) on a right click on an entry. The rule is the one of a table and a flow
diagram: a right click on an entry outside the selection selects it first, a right click on the
selected entry keeps the selection - so the menu acts on the entry clicked. The selection reaches
the server before the menu opens, since all commands of a page go through one ordered channel.

## Styling

The list draws the classes of the design system component `tl-list` (`list.contract.md` of
`tl-design-system`), which also describes its state contract `DataListState` (`state.proto` of
`com.top_logic.layout.react`). It sets no surface and no width of its own: its place gives them. A
library replacing the whole list registers a component for `TLDataList`; the avatar, the badge and
the triggers are parts and follow a replacement of these parts.
