---
description: Read before putting a count or a mark at a button, a tab, an avatar or an entry of the navigation in a .view.xml - the attributes badge, badge-role and badge-label (WithBadge) on a command, a <tab> of a <tab-bar>, an <avatar>, a <nav-item> or <command-item> of the <sidebar> and an <item> of a <bottom-bar>, the rules turning a channel value into a badge (0 and null hide it, "99+", a dot for true), how a badge follows the model, and how it is named for a screen reader.
order: 32
---

# Badges

A badge is a small count or a dot at the corner of an element, saying that something waits there:
the number of open tickets at the navigation entry leading to them, a dot at a bell. Every element
that can carry one declares it the same way (the configuration mixin `WithBadge`, bound by
`BadgeBinding`):

| Element | Where the badge stands |
|---|---|
| a command - its button, its entry in a menu, its `<command-item>` in the sidebar | after the label; over the corner of the icon of a button showing its icon alone |
| a `<tab>` of a `<tab-bar>` | after the label of the tab |
| an `<avatar>` | over the corner of the circle |
| a `<nav-item>` of the `<sidebar>` | after the label; over the corner of the icon in the folded rail |
| an `<item>` of a `<bottom-bar>` | over the corner of the icon |

## Declaring a badge

- **`badge`** names the channel whose value the badge shows.
- **`badge-role`** is the color role: `brand` (the default), `error`, `warning`, `success` or `info`.
- **`badge-label`** is the text naming the badge for a screen reader where the badge does not say it
  itself: the name of a dot ("New entries"), or what a count at an icon or an avatar counts. Written
  as `<badge-label><en>…</en><de>…</de></badge-label>`; the attribute `badge-label="…"` names a
  resource key.

```xml
<button>
    <action class="com.top_logic.layout.view.command.GenericViewCommand"
        badge="openTickets"
        badge-role="error"
        image="css:bi bi-inbox"
    >
        <label><en>Inbox</en></label>
    </action>
</button>
<tab-bar>
    <tabs>
        <tab id="open" badge="openTickets" badge-role="warning">
            <label><en>Open tickets</en></label>
            …
        </tab>
    </tabs>
</tab-bar>
<avatar input="person" badge="marked" badge-role="success">
    <badge-label><en>Online</en></badge-label>
</avatar>
```

## The value rules

The channel value becomes a badge as follows:

| Value | Badge |
|---|---|
| `null`, `false`, an empty text | none |
| the number `0` | none |
| a number above 99 | `99+` |
| any other number | its digits |
| a short text | the text as it is |
| `true` | a dot |
| any other value | its label, as the model names it |

A badge carries digits or an abbreviation; a longer text belongs in a pill
(`<text appearance="pill">`, see [colored values](doc:view-layer/display#colored-values)).

## A badge follows the model

The badge follows a new value on its channel and a change of the object that value points to, for
as long as the element is displayed; an element displayed again shows the badge as it is then.
There is no script of its own at the element: a computed badge comes from a
[derived channel](doc:view-layer/channels). A count over a whole type reads no channel, so it names
the type it counts as `observed-types` - without that, a channel without inputs is computed once
when the view is built:

```xml
<derived-channel name="openTickets"
    expr="all(`demo.tickets:Ticket`).filter(t -> $t.get(`demo.tickets:Ticket#status`) == `demo.tickets:TicketStatus#open`).size()"
    observed-types="demo.tickets:Ticket"
/>
```

A ticket created, closed or deleted - in this session or in another one - recomputes the count, and
every badge reading the channel follows it; a count dropping to zero removes the badges.

## Accessibility

The badge belongs to the accessible name of the element it stands at. The name of the badge itself
is the `badge-label` of a dot, and the count of a count - after its `badge-label` where it has one
("Open tickets: 3"): a `badge-label` says what is counted, it never replaces the count.

- Beside a visible label - a button showing its label, a tab, an entry of the open rail - a plain
  count is visible text of the element, read with its label ("Inbox 3"); a dot and a count with a
  `badge-label` are an image (`role="img"`) of that name ("Inbox New entries").
- At an element without visible text - an icon-only button, an entry of the folded rail, an item
  of the bottom bar, an avatar - the element is named by its label followed by the name of the
  badge ("Inbox, 3", "Anna Meier, Open tickets: 3"), and the badge over its corner is hidden from
  assistive technology.
- A dot without a `badge-label` has no name, so a dot always gets one.

The color alone says nothing; the name says what is counted or marked.

## Drawing

Every badge is drawn through the part `Badge` (see [parts](doc:view-layer/parts)) with the badge
of the design system: `tl-badge`, the color role as `data-tl-role`, a dot as `tl-badge--dot`. A
component library replacing the part draws every badge of the application; the state of a badge is
the message `Badge` of `state.proto` (`content`, `dot`, `role`, `label`).
