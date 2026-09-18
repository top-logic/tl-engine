# tl-ai-demo

The demonstration application for this repository: one TopLogic app that bundles `tl-ai-core`,
`tl-chat`, `tl-chat-classic`, `tl-ai-mcp-server` and `tl-ai-vector-index`, with a small domain
model (an invoice read out of its scan by a model) to have something for the agent to work on.

## Running it

```bash
mvn -pl tl-chat,tl-chat-classic -am -DskipTests install   # the chat bundle must exist before the app starts
cd tl-ai-demo
tl_anthropic_api_key="unencrypted:$ANTHROPIC_API_KEY" mvn tl:resolve compile exec:java
```

Then open <http://localhost:8080/tl-ai-demo/>. Pass `-Dtl.port=…` to use another port.

The `%ANTHROPIC_API_KEY%` placeholder in the LLM service configuration resolves from the environment
variable `tl_anthropic_api_key`; the `unencrypted:` prefix is what marks the value as a literal
rather than an encrypted one.

The first start creates the H2 database under `tmp/` and prints the initial password of the `root`
account to the console:

```
***  Initial password for 'root': ….…  ***
```

## What you see

The start page is `/view/` - the React view layer, declared in `WEB-INF/views/app.view.xml`:

| | |
|--|--|
| **Chat** (start item) | The agent chat, built from view elements rather than from the one-element `<agent-chat>`: a `<data-list>` of this user's chats, a `<chat-conversation>` (transcript, prompt field, agent/model/MCP/trace menu, attachments, human-in-the-loop approvals, context-usage ring), and a `<chat-rail>` whose sections are configured in `WEB-INF/views/tl-ai-demo/chat-grouped.view.xml` - Fortschritt, Quellen, and a `<data-list>` of the KB's invoices whose selection opens an invoice form in the rail's pane. Fortschritt is a `<data-list>` too: the agent's plan is written to `tl.ai.execution:AgentTask` objects while it runs, so the list fills within about a second of each step and is still there after a reload. A chat row offers Umbenennen and Löschen behind the `⋯` that appears on hover - generic `<row-commands>` of the list, not chat-specific code; Umbenennen opens `tl-ai-demo/rename-session.view.xml`, Löschen confirms first and is hidden while that chat still has a run in it. |
| **Belege** | The invoices: a table on the left, the selected invoice's form on the right. "Neuer Beleg" asks only for the scan and has the model read the description, the date and the amount out of it. The table's amount column shows the derived `amountLabel` (`84,90 €`); the form edits the number itself. |
| **Administration** | The framework's own administration view; visible to accounts holding the `administration` scope. |
| **Albert**, the dock (bottom right, every page) | The same chat again, but as a `<chat-dock>` in the shell's `<footer>` - a launcher over the corner of the window that opens a card over the page, holding a session list, the conversation and a rail with Fortschritt, Quellen and the invoices whose selection opens the pane. It is declared in `app.view.xml`, not in the chat page, because the footer is where a shell-wide element belongs. |

**The dock is a second conversation, not a second view of the first.** Its `chat-id` is `dock`, the
page's is `chat`, so the two have their own session lists, their own transcripts and their own
stored rail layout. That is what the demo is showing - one product, two placements - and an
application that wants one conversation in both places writes the same id in both. Note the id is
written **twice** in `app.view.xml`, on the `<chat-dock>` and on the `<chat>` inside it: the dock
contains the chat, and a chat scope only reaches downward.

The panel is hidden when the dock is closed, never unmounted, so a run started in the dock keeps
running and keeps its transcript while the user works on a page behind it; the launcher carries the
unseen count of the rail's sections and a dot while the agent is working.

The app bar carries one theme toggle: two `<set-theme>` commands of which the stylesheet shows only
the one that switches to the other theme (`style/tl-ai-demo-shell.css`). Everything below it, the
chat included, follows the choice without a reload.

The chat needs a signed-in user. A visitor who has not logged in gets an anonymous TopLogic session,
and `ChatServlet` answers every request from such a session with 401, so the chat shows a sign-in
note instead of a composer - the agent works with the user's data and the user's permissions.

**The classic UI is unchanged.** `WEB-INF/layouts/mainTabbar.layout.xml` still serves the same
invoices and administration through the LayoutServlet, and the classic chat control still works.
Reach it at <http://localhost:8080/tl-ai-demo/servlet/LayoutServlet>. Its "Albert" tab comes from
`tl-chat-classic`, which this application depends on for exactly that reason (#398).

## Layout of the module

| Path | Contents |
|--|--|
| `WEB-INF/views/` | The React view layer: `app.view.xml` plus `tl-ai-demo/chat-grouped.view.xml`, `tl-ai-demo/rename-session.view.xml`, `tl-ai-demo/invoices.view.xml` and `tl-ai-demo/create-invoice.view.xml` |
| `WEB-INF/layouts/` | The classic LayoutServlet UI |
| `WEB-INF/model/` | The demo model (`tl.ai.demo.imageclassification:Invoice`) |
| `WEB-INF/conf/` | Application configuration, including the `ClientResources` declaration that loads the chat bundle |
| `doc/secdemo/` | A separate security testbed with its own personas; see `doc/secdemo/testbed.md` |
