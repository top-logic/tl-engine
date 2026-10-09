---
description: Read before extending app.view.xml - the application shell, sidebar items, badges and the rail chrome, login and anonymous sessions with the login view, and the state classes for transitions.
order: 80
---

# The application shell

## The application shell: extending `app.view.xml`

`tl-layout-view` ships the shell an application is displayed in as `WEB-INF/views/app.view.xml` — the `default-view` of `ViewConfig`: an `<app-shell>` with the system notices (`<maintenance-notice/>`, `<session-timeout-notice/>`), a `<sidebar>` ending in a separator (`id="system-separator"`) and the `administration` item, and an `<app-bar>` with the drawer-toggle slot `appbar-leading`, the projection slot `appbar-content` and the development menu plus the account area (`dev-menu.view.xml`, `user-menu.view.xml`) in its trailing area. An application does not copy it, it extends it with a **same-path overlay**: a `WEB-INF/views/app.view.xml` of its own module, which the `ViewLoader` merges onto the shipped one in dependency order.

- Each slot of `<app-shell>` (`header`, `notices`, `content`, `footer`) holds **at most one element of a kind** — the slot lists are keyed by the configuration interface of their entries. The overlay's `<content><sidebar>` and `<header><app-bar>` therefore extend the shell's sidebar and app bar instead of adding second ones; two elements of the same kind in one slot are rejected.
- Single-valued properties written in the overlay replace the shell's: the `<title>` of the app bar, the `active-item` of the sidebar.
- The rail chrome (`<header>`, `<footer>`, … of the sidebar) is a list of arbitrary elements; the overlay marks its list with `config:override="true"` to replace the shell's neutral texts instead of adding to them.
- Sidebar items are keyed by `id`. An item without a position is appended — after the `administration` item; an item for the application's own section names the separator: `config:position="before" config:reference="system-separator"`.
- Channels the items read go in the `<channels>` of the overlay's `<view>`; they are added to the (empty) channels of the shell.

```xml
<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
    <app-shell>
        <content>
            <sidebar active-item="home">
                <header config:override="true">
                    <text><label><en>My App</en></label></text>
                </header>
                <nav-item id="home" config:position="before" config:reference="system-separator">
                    <view-ref view="home.view.xml"/>
                    <label><en>Home</en></label>
                </nav-item>
            </sidebar>
        </content>
        <header>
            <app-bar>
                <title><en>My App</en></title>
            </app-bar>
        </header>
    </app-shell>
</view>
```

An application that defines its whole shell itself writes `<app-shell config:override="true">` in its copy; the copy then takes the place of the shipped shell as a whole (channels of its `<view>` are still added to the shell's, which has none). `com.top_logic.demo.react`'s `app.view.xml` is a complete overlay example; `TestAppShellOverlay` pins the merge behavior.

## The sidebar: item kinds, badges and the rail chrome

`<sidebar>` (`SidebarElement`) is the navigation rail of an application shell. It holds a list of items and the chrome of the rail itself; the items are keyed by their `id`, so a configuration fragment of another module adds, repositions (`config:position`) or overrides a single item.

Five kinds of item:

- `<nav-item id icon route badge hidden>` leads to a page. Its content — written directly inside it, usually a `<view-ref>` — is built when the item is first selected, and its `id` is the route segment it contributes (see [URL routing](doc:view-layer/navigation#url-routing-what-ends-up-in-the-address-bar)).
- `<group id icon expanded>` gathers further items under a heading the user folds away. A group holds items, not content: the navigation items inside it lead to the same places and are addressed at the sidebar itself, so a group nests inside a group without changing where anything is displayed. Whether it is folded is remembered per user under the group's `id` (`PersonalizingExpandable`, the states stored as one JSON map beside the rail's own collapsed state), and applied to a group at any depth when the item list is pushed. On a folded rail a group opens as a flyout listing its items.
- `<header-item id icon>` is a caption naming the items that follow it. It leads nowhere and cannot be activated; it divides a long navigation into named sections that all stay visible, where a group would fold them away.
- `<command-item id>` runs a command instead of leading somewhere. The `<action>` it hosts is a `ViewCommand`, whose label and image the item displays, so the item reads like the button of the same command elsewhere. The item follows the command's executability for as long as the rail stands: invisible is not displayed, not executable is displayed out of reach (greyed), and the item takes its place back as soon as the rules allow it. The reason a rule gives for disabling the command (the text of a `<disabled-if>` expression, say) is what the item shows when the pointer rests on it, as the button of the same command does. The control refuses an activation of an item in either state, so a client addressing the command directly does not bypass the rules.
- `<separator/>` draws a line between items. It is the one entry that usually carries no `id`, and at most one such anonymous entry may occur — give separators explicit ids where more than one is needed.

`<nav-item>` and `<group>` take an `<access-control scope="…"/>`: an item the current user may not reach is not built at all, and a group whose access is denied withholds everything inside it.

```xml
<sidebar active-item="attributes">
    <header>
        <text>
            <label>
                <en>Application</en>
                <de>Anwendung</de>
            </label>
        </text>
    </header>
    <header-collapsed>
        <text>
            <label>
                <en>A</en>
                <de>A</de>
            </label>
        </text>
    </header-collapsed>
    <items>
        <header-item id="data">
            <label>
                <en>Data</en>
                <de>Daten</de>
            </label>
        </header-item>
        <nav-item id="attributes" icon="css:bi bi-card-list">
            <view-ref view="attributes.view.xml"/>
            <label>
                <en>Attributes</en>
                <de>Attribute</de>
            </label>
        </nav-item>
        <nav-item id="tickets" badge="ticketCount" icon="css:bi bi-chat-left-text">
            <view-ref view="tickets.view.xml"/>
            <label>
                <en>Tickets</en>
                <de>Tickets</de>
            </label>
        </nav-item>
        <group id="demos" expanded="false" icon="css:bi bi-collection">
            <label>
                <en>Demos</en>
                <de>Demos</de>
            </label>
            <nav-item id="charts" icon="css:bi bi-bar-chart-line">
                <view-ref view="demo/chart-demo.view.xml"/>
                <label>
                    <en>Charts</en>
                    <de>Diagramme</de>
                </label>
            </nav-item>
        </group>
        <nav-item id="print-view" hidden="true">
            <view-ref view="print.view.xml"/>
            <label>
                <en>Print view</en>
                <de>Druckansicht</de>
            </label>
        </nav-item>
        <separator/>
        <command-item id="about">
            <action class="com.top_logic.layout.view.command.GenericViewCommand"
                image="css:bi bi-info-circle" input="ticketCount"
            >
                <label>
                    <en>About</en>
                    <de>Info</de>
                </label>
                <executability>
                    <null-input-disabled/>
                </executability>
                <notify expr="count -> #('{0} tickets.'@en, '{0} Tickets.'@de).fill($count)"/>
            </action>
        </command-item>
    </items>
    <footer>
        <view-ref view="user-menu.view.xml"/>
    </footer>
    <footer-collapsed>
        <text>
            <label>
                <en>A</en>
                <de>A</de>
            </label>
        </text>
    </footer-collapsed>
</sidebar>
```

A label is written as a `<label><en>…</en><de>…</de></label>` child; the `label="…"` attribute of the same property names a resource *key*, which an application that keeps its texts in the view file does not have — such a key shows up in the rail as `[TL]`.

**`hidden`** withholds a `<nav-item>` from the rail while leaving it reachable by its route: the page a URL leads to directly, which has no place of its own in the navigation. Routing, content creation and `getChildGroups()` are untouched by it, so a deep link and `<show-object>` reach such a page exactly as they reach a listed one; what is refused is a *selection* sent by the client, which would otherwise switch to a view the user interface does not present.

**`badge`** names a channel whose value is displayed beside the item's label — the number of things waiting in the page it leads to. The value is named as the model names it (`MetaLabelProvider`); nothing and an empty text show no badge at all, which is how a count answers "nothing to report" with a `null` rather than a zero. The badge follows a new value on the channel *and* a change of the object that value points to, so a count computed from an edited object is up to date without the channel being written anew. A count over a whole type reads no channel at all, and therefore names the type it counts as an `observed-types` of its `<derived-channel>` — without that, a channel with no inputs is computed once when the view is built and never again:

```xml
<derived-channel name="ticketCount"
    expr="{ tickets = all(`demo.tickets:Ticket`).size(); if($tickets == 0, null, $tickets); }"
    observed-types="demo.tickets:Ticket"
/>
```

**The chrome of the rail** is four lists of view elements outside the item list: `<header>` and `<footer>` stand above and below the items for as long as the rail is on screen, and `<header-collapsed>` / `<footer-collapsed>` replace them while the rail is folded to a narrow strip — room for an abbreviation or an avatar, not for a name and a search field. Each list is created eagerly with the sidebar and may hold any element, a `<view-ref>` included; the views written there are addressed at the sidebar without a key, since whoever reaches them reaches them by opening the rail and nothing else. Left empty, the rail begins with its first item and ends with its last.

**`SidebarItemElement` is the extension point** for an item kind the configuration does not cover — items computed from the model, say, one per project of the current user. An implementation answers two things:

- `createSidebarItem(ViewContext, ItemSite)` builds the `SidebarItem` (`NavigationItem`, `GroupItem`, `HeaderItem`, `CommandItem`, `SeparatorItem`), or `null` for an item that must be omitted, e.g. because access is denied. The item is built *before* the control that displays it exists, so whatever the item has to say to that control — a badge to keep up to date, an executability to follow — is registered through `ItemSite.addBinding(Consumer<ReactSidebarControl>)` and run as soon as the control is there. Inside such a binding, `addAttachListener` / `addDetachListener` start and stop model observation, `addCleanupAction` removes listeners again, and `updateBadge(id, text)` / `refreshItems()` push a changed item list to the client (items are held as objects and serialized as a whole, so a change to one of them reaches the display only with the list it belongs to). The content of a navigation item is built later still, in `ItemSite.contentContext(context, key)` — the context that says where that content will sit, which is what makes an item's page revealable before it has ever been selected.
- `getChildGroups()` reports the content the item holds, keyed by the id of the item displaying it. An item displaying no content of its own holds nothing; an item holding further items (a group) answers what those hold, so that every navigation item of a sidebar — nested or not — is addressed at the sidebar itself. An element that holds content and does not report it hides every view below it from navigation (see [Where a view is mounted is known statically](doc:view-layer/navigation#where-a-view-is-mounted-is-known-statically)).

Build the items of a nested list with `SidebarElement.createItems(elements, context, site)`, passing the site on unchanged: an item then behaves the same wherever it is written.

## Login, anonymous sessions and the login view

Every visitor reaches the application under an anonymous session, so who is let in is a question the application answers rather than one the servlet refuses. An application that shows itself to visitors offers the login from its account area: `user-menu.view.xml` offers an anonymous session (`account -> $account.accountIsAnonymous()`) a Login button that opens `login.view.xml` as a dialog, and the parts that are not for visitors are gated in place — `<access-control scope="administration"/>` on a `<nav-item>`, `<authenticated-only/>` in a command's `<executability>`.

An application that has nothing to show a visitor names a login view in its `ViewConfig` instead: `<config config:interface="com.top_logic.layout.view.ViewConfig" login-view="login-page.view.xml">`. `ViewServlet` then renders that view for every request of a session that belongs to no account, whatever the URL names — a route, the default view, an entry point, or a view that is none of them — so the visitor sees the login and nothing else. `login-page.view.xml` is what the framework ships for this: the credentials form on a centered `<panel appearance="card" width="380px">` and nothing else, where `width` is the general option of `<panel>` for a panel that is not to take the width it is offered. An application with a login display of its own names that one. Both displays reference the same `login-form.view.xml` — the fields, the login action, the `<login-commands/>` a feature module contributes to and the `<login-methods/>` buttons of the configured SSO providers — so the dialog and the page offer the same login.

**The requested URL survives the login.** The login page is not the application the URL addresses, so it takes the URL up not at all: the route manager *holds* the requested route (`RouteManager.holdUrl`) instead of adopting it. Nothing is composed from the display and nothing is corrected, so the address bar keeps showing `/view/my-refunds` while the form is displayed. The login swaps the session and reloads the page (`PendingSessionAction`), and the reload carries that same URL into the application: a URL that names a route always gets that page, and the personal start page (`StartPage`, written by the user menu's "Start on this page") applies only to a URL that names no route. The redirects that establish the session in the first place — the cookie check, the anonymous login — return to the page that was asked for as well (`ViewServlet.requestedPage`), and an SSO button hands its provider the held route as the return path, so an external login lands on the requested page too.

**A page for a visitor without an account.** An entry point marked `<entry-point view="unknown-account.view.xml" anonymous="true"/>` is the one exception to the login view: a URL naming it shows that view to an anonymous session, and because the page is the one the URL addresses, its route is adopted like any other (`ViewServlet.resolveView` decides view and login-view flag together). `tl-layout-view` ships `unknown-account.view.xml` for exactly this and points the `unknownAccountPage` of `ApplicationPages` at it: the external authentication servlet sends a visitor whose identity an SSO provider confirmed but for whom the application has no account there, with the authenticated name in the `login` query parameter, which the view's `<query-bindings>` put on a channel and its message names.

Leaving works the other way round: `<logout-command>` swaps the session back and sends the browser to the root of the view application (`ViewServlet.ROOT_PATH`), because the page the departing user was on is theirs and not the landing page of whoever sits down at the browser next. `com.top_logic.demo.react` configures a login view, `com.top_logic.demo` does not — its React view is browsed anonymously and logged into through the account area.

## Transitions: the state classes an application animates

Three displays change what they show rather than only how it looks: a wizard moves to another step, a dialog opens and closes, a tile stack pushes and pops a frame. Each change is marked in the DOM with state classes, and which animation runs — or whether one runs at all — is the application stylesheet's decision. The classes are the API; the engine ships a default animation for the wizard only.

| Display | While it arrives | While it leaves | Direction on the root |
| --- | --- | --- | --- |
| `<wizard>` | `tlWizard__step--entering` | `tlWizard__step--exiting` | `tlWizard--forward` / `tlWizard--backward` |
| dialog | `tlDialog__backdrop--entering` | `tlDialog__backdrop--exiting` | — |
| `<tile-stack>` | `tlTileStack__frame--entering` | `tlTileStack__frame--exiting` | `tlTileStack--forward` / `tlTileStack--backward` |

The wizard takes its direction from the server (state key `direction`); the tile stack derives it from the position it displays, deeper into the stack being forward.

**What leaves is a copy.** The control of a step left behind, of a dialog closed or of a frame popped is disposed on the server the moment the display moves, so there is nothing left to render — what can still be animated out is a picture of it. The `--exiting` element is therefore an inert `cloneNode` copy of what left: `aria-hidden`, `inert`, without the ids of what it copied (so that nothing addresses it) and without pointer input. There is one copy per key at a time, and a copy is dropped when its `animationend` or `transitionend` arrives, when the same key is displayed again, or — where the stylesheet animates nothing — after a one-second fallback. The engine places the copy and ships only the structural rules that make it a picture: absolutely positioned over the stack for a tile frame, `pointer-events: none` for the dialog backdrop, whose copy covers the page and therefore has to let both the page and a dialog still open below it through.

**Reduced motion is decided before the stylesheet.** Where `(prefers-reduced-motion: reduce)` matches, no class is set and no copy is made at all, so the change is instantaneous whatever an application animates. Neither the engine stylesheet nor an application's needs a `prefers-reduced-motion` block for these classes.

**One hook does this for all three.** `useKeyedTransition` (`com.top_logic.layout.react/react-src/bridge/transition.ts`, exported from `tl-react-bridge`) takes the keys of what is displayed now, a way to resolve the element of a key, the container the copies are shown in, and the two class names; it notices which keys appeared and which disappeared, marks the DOM before the browser paints, and takes the marks away again. It writes the classes onto the DOM rather than handing them back for rendering, so the element marked may be rendered anywhere below the component that notices the change — a dialog's backdrop belongs to the dialog, while its opening and closing is the dialog manager's news. A display that newly wants a transition uses this hook rather than a mechanism of its own.

**An example stylesheet.** `com.top_logic.demo.react/src/main/webapp/style/tl-demo-react.css` animates the anchors the engine leaves free: a 150ms opacity fade for the dialog backdrop and a 200ms fade-and-slide for the tile frames, the slide following `tlTileStack--forward` / `tlTileStack--backward`.
