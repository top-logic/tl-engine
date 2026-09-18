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
| **Chat** (start item) | The agent chat (`tl-chat`), with its own history rail, the agent/model/MCP/trace menu, attachments, human-in-the-loop approvals and the context-usage ring. |
| **Chat mit Rechnungen** | The same `/chat/*` API composed as `DemoChat`: a Rechnungen rail, a read-only invoice pane, and a quote chip. Fixture invoices, not KB rows. |
| **Chat (Elemente)** | The same chat built from view elements instead of the one-element `<agent-chat>`: a `<data-list>` of this user's chats, a `<chat-conversation>`, and a `<chat-rail>` whose sections are configured in `WEB-INF/views/tl-ai-demo/chat-grouped.view.xml` - Fortschritt, Quellen, and a `<data-list>` of the KB's invoices whose selection opens an invoice form in the rail's pane. |
| **Belege** | The invoices: a table on the left, the selected invoice's form on the right. "Neuer Beleg" asks only for the scan and has the model read the description, the date and the amount out of it. The table's amount column shows the derived `amountLabel` (`84,90 €`); the form edits the number itself. |
| **Administration** | The framework's own administration view; visible to accounts holding the `administration` scope. |

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
| `WEB-INF/views/` | The React view layer: `app.view.xml` plus `tl-ai-demo/invoices.view.xml` and `tl-ai-demo/create-invoice.view.xml` |
| `WEB-INF/layouts/` | The classic LayoutServlet UI |
| `WEB-INF/model/` | The demo model (`tl.ai.demo.imageclassification:Invoice`) |
| `WEB-INF/conf/` | Application configuration, including the `ClientResources` declaration that loads the chat bundle |
| `doc/secdemo/` | A separate security testbed with its own personas; see `doc/secdemo/testbed.md` |
