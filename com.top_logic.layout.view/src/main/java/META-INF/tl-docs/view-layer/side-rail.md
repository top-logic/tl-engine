---
description: Read before putting a collapsible column beside other content in a .view.xml with <side-rail> (SideRailElement, ReactSideRailControl, client TLSideRail) - sections with name / label / icon / badge / open / width / header / <visibility> / reveal / dismiss-on, the strip, open-channel / badge-channel / shown, the modal drawer below drawer-below, a rail in a <split-panel>, what is remembered per user, and a collapsible <data-list>.
order: 22
---

# A collapsible column: `<side-rail>`

`<side-rail>` (`SideRailElement`, control `ReactSideRailControl`, client `TLSideRail`) is a column
beside another element that collapses to a strip. Its `<section>`s stand one below the other in the
column, each with a header that expands and collapses it. The strip shows the `icon` of every
section, with its badge; a click on an icon opens the rail with that section expanded, a second
click on the shown section closes the rail.

A rail with one section is the collapsible column of any element, e.g. a list. A rail with several
sections is a side panel beside a main view, e.g. the sources, the plan and the memory beside a
chat. The demo application shows it under *Side rail* (`demo/side-rail-demo.view.xml` of
`com.top_logic.demo.react`).

```xml
<split-panel orientation="horizontal" resizable="true">
	<panes>
		<pane size="320" unit="px">
			<side-rail open-channel="railOpen" badge-channel="railBadge" drawer-below="44">
				<section name="contacts" header="false" icon="css:bi bi-person-lines-fill" dismiss-on="contact">
					<label><en>Contacts</en></label>
					<data-list selection="contact" ...>
						<title><en>Contacts</en></title>
					</data-list>
				</section>
				<section name="tickets" badge="ticketCount" icon="css:bi bi-ticket-perforated" open="false">
					<label><en>Tickets</en></label>
					<data-list count="ticketCount" .../>
				</section>
			</side-rail>
		</pane>
		<pane size="100" unit="%">
			<form input="contact">...</form>
		</pane>
	</panes>
</split-panel>
```

## The rail

- `side` - `left` (default) or `right`: the side of its neighbour the rail stands on, and the edge
  its drawer slides in from.
- `strip` (default `true`) - whether the strip of section icons is shown. Without it, the rail opens
  only through its `open-channel`.
- `open` (default `true`) - whether the rail starts open, for a user who has not opened or closed it
  yet.
- `width` (pixels, default 280) - the width of the column outside a split panel, which the user drags
  at its inner edge; the drawer has it as well.
- `drawer-below` (rem, default 0 = never) - see [narrow areas](#narrow-areas).

## Sections

- `name` (mandatory) - the key of the section within the rail; an overlay of the view adds, places,
  changes or removes a section by its name.
- `label`, `icon` - the title of the section in its header and the name and icon of its strip icon.
- `open` (default `true`) - whether the section starts expanded.
- `width` (pixels, default 0) - a width the section asks for while it is shown, e.g. for a document.
- `badge`, `badge-role`, `badge-label` - as on every [badged element](doc:view-layer/badges): on the
  strip icon and in the header of the section.
- `<visibility>` - the section is left out of the rail, strip icon included, while its condition
  does not hold.
- `header="false"` - the section has no header and is always expanded, for content that brings a
  title bar of its own; its label still names its strip icon and the section.
- `reveal="true"` - whenever an input of the `<visibility>` condition takes a new value for which the
  condition holds, the rail opens with the section expanded, e.g. for what an agent presents. This is
  no choice of the user and is not remembered.
- `dismiss-on` (channels) - while the rail is a drawer, a new value of one of these channels closes
  the drawer, e.g. the selection of a list in the section, so that a pick ends the visit. The column of
  a rail that is no drawer stays as it is, and nothing is remembered.

The content of a section is built at once and stays mounted while the section is collapsed or the
rail closed, so it keeps what the user did there and keeps reporting its badge.

## Channels

- `open-channel` - follows whether the rail is open (its column, or its drawer where it is one), and
  opens or closes it when written, so a button or a command elsewhere opens the rail. A command that
  toggles the channel shows whether the rail is open with an
  [`active-expr`](doc:view-layer/commands#a-toggle-active-expr):

  ```xml
  <generic-command inputs="railOpen" active-expr="$railOpen" badge="railBadge"
  		image="css:bi bi-layout-sidebar" placement="TOOLBAR">
  	<label><en>Side rail</en></label>
  	<execute-script function="!$railOpen">
  		<with-result><write-channel name="railOpen"/></with-result>
  	</execute-script>
  </generic-command>
  ```

- `badge-channel` - receives what the badges of the sections count beyond what the user has seen, as
  a number. A badge counts as seen while its section is shown: the rail open, the section expanded and
  not hidden. A collapsed section therefore counts as new also while the rail is open; a dot adds
  nothing to the number.
- `shown` - a channel holding a boolean that says whether the rail is on screen at all, for a rail in
  a frame that may be closed, such as a dock: while it holds `false`, nothing counts as seen. Empty,
  the rail is on screen whenever its view is.

## Remembered per user

Whether the rail is open, which sections are expanded and the width the user dragged are stored in
the personal configuration under the personalization key of the element. An opening caused by
`reveal`, by `dismiss-on` or by the drawer is not stored.

## In a split panel

The rail takes part in the collapse of its pane (see [split panels](doc:view-layer/layout#panes-the-user-resizes-split-panel)):
closed, its pane shrinks to the strip and the other panes take the room; open, the column fills the
pane, whose size the split panel's handle and remembered sizes decide. A shown section with a `width`
widens the pane for as long as it is shown, without changing the size the user gave the pane. The
rail may close even as the last open pane, since its strip stays usable.

## Narrow areas

Where the area the rail stands in - the element its drawer is anchored in, the nearest positioned
element around the rail - is narrower than `drawer-below` rem, the rail keeps only its strip, and
opening it opens a modal drawer over its neighbour, beside the strip, named by the labels of the
sections it shows. The drawer is the one of the design system (`ReactDrawerControl` with
`setModal`): a scrim over the area, the focus inside, Escape and a click on the scrim close it, and
the focus returns to the strip icon. Escape is the innermost layer's: in a frame that closes on
Escape itself, such as a chat dock, Escape closes the drawer first. The column choice of the user is
left alone meanwhile and comes back when the area is wide enough again.

## A collapsible list

A `<data-list>` has no collapse of its own. A list that collapses to a strip and opens as a drawer
where its place is narrow stands in a rail with one section - `header="false"` when the list has a
title of its own, and `dismiss-on` naming the list's `selection`, so that a pick closes the drawer.
See [lists](doc:view-layer/lists).

## Styling

The client classes are `.tl-side-rail`, `.tl-side-rail--start` / `.tl-side-rail--end`,
`.tl-side-rail__strip`, `.tl-side-rail__column`, `.tl-side-rail__handle` and the section parts
`.tl-side-rail__section`, `__header`, `__body` (design system, `side-rail.css`, contract
`side-rail.contract.md`); the states are `data-tl-state~="open"`, `~="drawer"` and `~="resizable"`,
and `data-tl-state~="headless"` on a section without header. The state contract of the controls is
`SideRailState` and `SideRailSectionsState` (`state.proto`).
