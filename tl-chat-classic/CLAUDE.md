# tl-chat-classic

The classic chat control for LayoutServlet applications - the **"Albert"** tab. **Frozen: still
shipped and still working, not developed further.** Split out of `tl-chat` by #398 so that a host
opts into it instead of inheriting it, and so that retiring it later is the removal of a module
rather than surgery inside the live one.

**Parent:** `tl-parent-core` | **Dependencies:** `tl-core`, `tl-ai-core`, `tl-chat`

## What is here, and what is not

| Here | In `tl-chat` |
|--|--|
| `layout/ChatControl` - renders the transcript, the input section and the history panel, and injects `chat-client.js` | `ChatServlet` and the whole `/chat/*` REST + SSE API |
| `layout/ChatComponentBuilder` - the `ModelBuilder` named by `agent.xml` | `Chat`, `ChatMessage`, `ChatProcessor`, `ChatSessionManager`, `ChatSessionRegistry`, `TaskProgressTracker`, `PromptImprovementProcessor` |
| `webapp/script/chat-client.js` (4152 lines), `webapp/style/chatStyles.css`, `webapp/themes/core/icons/albert.svg` | `ChatHistoryService`, the msgbuf `messages.proto` DTOs |
| The `WEB-INF/layouts/tl-chat/chat/*` layouts and the `mainTabbar.layout.overlay.xml` that pushes the tab into a host's tab bar | The React application `ui/` and its composed view elements |

Both Java classes keep the package `com.top_logic.ai.chat.layout` they had in `tl-chat`, so the
split cost no import churn. The package is therefore split across two jars - harmless here, and the
alternative would have broken `git blame` for no gain.

## How it hangs together

1. A host application that depends on this module gets `mainTabbar.layout.overlay.xml`, which
   prepends `tl-chat/chat/tabLayout.layout.xml` to its `mainTabbar`.
2. That tab holds `tl-chat/chat/agent.layout.xml` → `agent.xml`, a `form` whose `modelBuilder` is
   `ChatComponentBuilder`.
3. `ChatComponentBuilder` resolves the per-window `TaskProgressTracker` from `ChatSessionRegistry`
   (in `tl-chat`) and embeds a `ChatControl` in a hidden field's `ControlProvider`.
4. `ChatControl.internalWrite` renders the markup, writes the `data-*` attributes
   `chat-client.js` reads (window id, the `/chat/cancel`, `/chat/respond` and
   `/chat/approval-mode` URLs, the element ids), and appends an inline script that loads
   `/script/chat-client.js`. Everything after that is the JS talking to `tl-chat`'s `/chat/*`.

The layout resource paths stay `tl-chat/chat/…`. They are how a host's persisted layouts and its
`component-access` entries name these components (see
`tl-ai-demo/src/main/webapp/WEB-INF/conf/secdemo.config.xml`), so renaming them would be a
user-visible change, which #398 is not.

## Resources

- `WEB-INF/conf/tl-chat-classic.conf.config.xml` registers the `tl-chat-classic-messages` bundle,
  whose single key `chat.layout.agent.title` is the `resPrefix` title of `agent.xml`.
- `WEB-INF/themes/core/theme.xml` declares `/style/chatStyles.css`; `theme-settings.xml` declares
  the `TL_CHAT_AGENT` icon used as the tab icon.
- There is no `web-fragment.xml`: this module registers no servlet and no filter.

## Tests

`src/test/javascript/test-markdown-links.html` is a hand-opened browser page, not a runner: it
holds a copy of the markdown link handling of `chat-client.js` and must be kept in sync with it by
hand. There are no Java tests; the behaviour this module renders is covered by the suites that stay
with `tl-chat`.

## Changing something here

Don't, unless it is broken. If it is: the React chat in `tl-chat` is where new chat work goes, and
a fix here should be the smallest one that makes the classic tab correct again. A fix cherry-picked
to `maintenance/7.11` needs a path rewrite, because 7.11 has these files inside `tl-chat`.
