# tl-chat-classic

The React chat in a classic LayoutServlet application - the **"Albert"** tab. A host opts into it by
depending on this module; depending on `tl-chat` alone gives the chat API and the React chat in
React pages only.

**A temporary bridge.** It gives agents to applications that keep the classic UI until they move to
the React view layer; everything named `LayoutComponent` is to leave tl-core. The engine lacks five
pieces for hosting React in a classic page, and this module works around each of them itself (see
[Temporary workarounds](#temporary-workarounds)). The hand-written `chat-client.js` control that
lived here from #398 to #618 is gone; there is one chat, the React one.

**Parent:** `tl-parent-core` | **Dependencies:** `tl-core`, `tl-layout-react`, `tl-layout-view`,
`tl-chat`

## How it works

A React control is an `HTMLFragment` like any classic control: it writes its root tag, and the
React bridge renders the rest on the client from the control's state. So a classic component can
write one.

1. `ReactViewComponent` (`com.top_logic.ai.chat.classic`) is a `LayoutComponent implements
   ControlRepresentable`. Its `view` attribute names a `.view.xml` below `/WEB-INF/views/`. On its
   first rendering in a window it builds that view's control tree, as `ViewServlet` would.
2. **The React window is the classic window.** The tree is registered in `ReactWindowRegistry`
   under the name of the classic window's sub-session (`ChatSessionRegistry.windowId`). That is what
   makes the two update paths meet in one page:
   - A React command (`/react-api/command`) carries that window name. `ReactServlet` resolves it to
     the classic sub-session *and* to the classic `MainLayout` (`ReactWindowReplay.installSubSession`
     returns its `SubsessionHandler`), so the classic repaints and `InfoService` messages a React
     command causes are forwarded to the page over the React event stream (`/react-api/events`).
   - Classic commands keep going through the AJAX servlet, which re-renders this component - and
     with it the React root tag, with the tree's current state - when its tab is shown.
   - The chat's own API (`/chat/*`) is addressed with the same window id.
3. The tree gets the window chrome a React page has: the snackbar (the window's error sink, so an
   error of a React command is shown over the classic page), the context menu and the dialog
   manager.
4. The component writes a `div.tlReactHost` carrying the React theme's attributes (`data-theme`,
   `data-tl-mode`, `data-tl-density`), the root tag inside it, and a script that loads
   `script/tl-react-host.js` once per page. That script installs the React client resources and
   mounts the controls (E1, E3).
5. Lifecycle: the tree is built once per window and kept, so a tab switch or F5 of the classic page
   shows the same conversation. It is detached while the component is invisible and closed with the
   window (`notifyRemoveFromMainLayout`). It is written inside the registry's `Interaction`, because
   the React servlet and the agent refresher change the same tree from other threads.

## The tab and the view

| File | Role |
|--|--|
| `WEB-INF/layouts/mainTabbar.layout.overlay.xml` | Prepends the Albert tab to a host's `mainTabbar`. |
| `WEB-INF/layouts/tl-chat/chat/tabLayout.layout.xml` | The tab. The reusable piece: a host that builds its own tab bar references it. |
| `WEB-INF/layouts/tl-chat/chat/agent.layout.xml` → `agent.xml` | The `ReactViewComponent`, `view="tl-chat-classic/chat.view.xml"`. |
| `WEB-INF/views/tl-chat-classic/chat.view.xml` | The default chat: the user's chats, the conversation, a rail with Progress and Sources. Generic on purpose - no application facts. |

The layout paths stay `tl-chat/chat/…`: they are how a host's persisted layouts and its
`component-access` entries name these components (see `tl-ai-demo/src/main/webapp/WEB-INF/conf/secdemo.config.xml`).

**An application configures its chat by its own view.** The agent (`<chat-conversation agent=…>`),
the `chat-id`, facts about the situation (`<context>`, `<selection>`), the rail sections - everything
`<chat>` offers - is written in a `.view.xml` of the application, and its tab points a
`ReactViewComponent` at it. `chat.view.xml` here is the example to start from. The default view uses
`chat-id="classic"`, so the classic tab is a conversation of its own, not the React pages' `chat`.

## Temporary workarounds

Each is marked in the code with "Temporary workaround for the classic-UI bridge (En)". Remove a
workaround when the engine provides the piece, and the whole module when the classic UI is gone.

| | What the engine lacks | What this module does instead | Where |
|--|--|--|--|
| **E1** | A style boundary between the classic theme and the React stylesheets. The React CSS assumes it owns the document (`.is-control { height: 100% }` - `is-control` is also the class of every classic control -, `html, body {…}`, `*`), and the classic theme's element rules (`button:not(.disabled, :disabled):focus { color: … }`) reach React controls. | `tl-react-host.js` loads every React stylesheet as a constructable sheet wrapped in `@layer tl-react { @scope (.tlReactHost, .tlReactPortal) {…} }`, with `:root` → the scope root and `html`/`body` → the host; `@font-face`/`@keyframes` stand beside the scope. One unlayered reset inside the scope (`all: revert-layer`, specificity above any classic rule) gives every React element the React layer's value. Elements React portals into `<body>` (tooltip host, popups) are marked `.tlReactPortal` and get the theme attributes. | `script/tl-react-host.js` |
| **E2** | A public way to build a React window's root tree: `ViewServlet.displayWindow` builds snackbar, context menu and dialog manager in private methods. | Rebuilt the same way. | `ReactViewComponent.windowRoot` |
| **E3** | A way to load the React client resources (import map, module scripts, stylesheets, theme tokens) into a classic page: `ClientResources` writes them only into a React page's `<head>`, and `tl-react-bridge` discovers controls only on `window.load`. | The component renders that head markup into a string; `tl-react-host.js` installs the import map, imports the modules as one module graph and calls `TLReact.discoverAndMount()`. | `ReactViewComponent.writeBootScript`, `script/tl-react-host.js` |
| **E4** | A shared request lock: `ReactServlet` revalidates the classic `MainLayout` for a React command without entering the classic window's `RequestLock`, which `AJAXServlet` holds for every classic request. | `ReactCommandLockFilter` (`META-INF/web-fragment.xml`, `/react-api/command`) enters that lock as a reader. **Gap left:** `/react-api/upload` - its window name is in a multipart body a filter cannot read; per window only, a display glitch at worst. | `ReactCommandLockFilter` |
| **E5** | Start pages that stay where the application put them: `tl-layout-view`, which `tl-chat` depends on, sets `ApplicationPages` `startPage`, `loginPage`, `loginRetrySSO` and `logoutPage` to `/view/` and `unknownAccountPage` to `/view/unknown-account.view.xml` for every application that has it on the classpath. A classic host has no `app.view.xml`, so its login fails. | `tl-chat-classic.conf.config.xml` sets the five pages back to the values of tl-core (`/servlet/LayoutServlet`, `/jsp/main/LoginErrorPage_unknownAccount.jsp`). The configuration of this module loads after `tl-layout-view`, and before the application's own files and its deploy aspects, so a host's `ApplicationPages` entry (an SSO `loginPage`, its own `logoutPage`, or `/view/` for a React-first host like `tl-ai-demo`) still wins. `TestClassicHostConfig` pins the values. | `WEB-INF/conf/tl-chat-classic.conf.config.xml` |

Known limits of the workarounds:

- An `!important` rule of the classic theme still wins over the E1 reset.
- React's own rules on `body` state (e.g. a class the drag shield sets on `<body>`) do not apply in
  the scope.
- Every classic re-rendering of the tab mounts the React root again, and the bridge then reopens its
  event stream: the server logs "SSE connection REPLACED" per tab switch. Harmless - the queue keeps
  its events - but it is log noise a React page does not make.
- The React chat follows the theme the user selected for the React UI, or the default one; it does
  not follow the operating system's dark mode, because the classic page around it does not either.

## Resources

- `WEB-INF/conf/tl-chat-classic.conf.config.xml` registers the `tl-chat-classic-messages` bundle,
  whose key `chat.layout.agent.title` is the `resPrefix` title of `agent.xml`, and sets the start
  and login pages back to the classic ones (E5).
- The chat's client bundle (`/script/tl-chat-ui.js`, `tl-chat-ui.css`) is registered on
  `ClientResources` by `tl-chat`. A host needs no entry of its own; see `tl-chat/CLAUDE.md`.
- `WEB-INF/themes/core/theme-settings.xml` declares the `TL_CHAT_AGENT` icon of the tab.
- `META-INF/web-fragment.xml` registers `ReactCommandLockFilter` (E4).

## Tests

- `mvn -pl tl-chat-classic test -DskipTests=false` runs the engine's generic suite over this
  module's files (layout normalization among them), `TestReactCommandLockFilter` and
  `TestClassicHostConfig` (what a classic host gets with no configuration of its own: the chat bundle and the classic start pages, E5).
- The bridge itself is verified end to end in a browser against `tl-ai-demo`'s classic UI; there is
  no automated browser test of it in this repository.
