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
| **Chat** (start item) | The agent chat, built from view elements rather than from the one-element `<agent-chat>`: a `<data-list>` of this user's chats, a `<chat-conversation>` (transcript, prompt field, agent/model/MCP/trace menu, attachments, human-in-the-loop approvals, context-usage ring), and a `<chat-rail>` whose sections are configured in `WEB-INF/views/tl-ai-demo/chat-grouped.view.xml` - Fortschritt, Quellen, Gedächtnis, and a `<data-list>` of the KB's invoices whose selection opens an invoice form in the rail's pane. Fortschritt is a `<data-list>` too: the agent's plan is written to `tl.ai.execution:AgentTask` objects while it runs, so the list fills within about a second of each step and is still there after a reload. A chat row offers Umbenennen and Löschen behind the `⋯` that appears on hover - generic `<row-commands>` of the list, not chat-specific code; Umbenennen opens `tl-ai-demo/rename-session.view.xml`, Löschen confirms first and is hidden while that chat still has a run in it. The agent can open that pane itself: `<chat-conversation present="paneObject">` lets it write that one channel, so "zeig mir den Beleg von Müller" ends as the invoice form and "zeig mir die drei teuersten" as a list - it names the objects, the page decides how they look (#436). The rail's invoice rows offer "Dazu fragen" and "In die Nachricht übernehmen" the same way: the first writes a German sentence about that invoice to the `chatDraft` channel, the second writes the invoice itself to `chatReference`, and the composer shows the sentence, or the invoice as one atomic reference token, unsent (#440). |
| **Belege** | The invoices: a table on the left, the selected invoice's form on the right. "Neuer Beleg" asks only for the scan and has the model read the description, the date and the amount out of it. The table's amount column shows the derived `amountLabel` (`84,90 €`); the form edits the number itself. "Dazu fragen" in the toolbar writes a sentence about the selected invoice into the chat's composer - the chat being a *different* view on another nav item, which works because `app.view.xml` declares the `chatDraft` channel and hands the same one to both pages with `<bind>` (#440). Switching to the chat shows the question standing in the composer; nothing is sent. |
| **Meine Einstellungen** | What this account tells every agent about itself: one free-text field (`tl.accounts:Person#aiPreferences`, added by `tl-ai-core`). The harness renders it into the environment block of every run this user triggers, below what the application itself states; a text above the configured cap is refused here. `WEB-INF/views/tl-ai-demo/profile.view.xml`. |
| **Gedächtnis** (rail section on the Chat page) | What Albert has kept about this account across conversations (#439): a `<data-list>` over `tl.ai.execution:UserMemory` filtered by `owner == currentUser()`, newest first, with a count badge and a Forget command per row. Albert writes one only when the user asked it to in that turn, or after it asked with `askUser` and the user agreed - the `remember` tool refuses anything else, which is a Java check and not a line in its prompt. A new memory appears in this list at once and in Albert's system prompt in the *next* conversation, because that prompt has to stay byte-identical across the turns of one. |
| **Administration** | The framework's own administration view; visible to accounts holding the `administration` scope. Its "Agent activity" run detail shows the filled environment block of each run, beside the transcript. |
| **Albert**, the dock (bottom right, on **Belege** and **Administration**) | The Chat page's conversation, reachable without leaving the page: a `<chat-dock>` in the shell's `<footer>` whose launcher opens a card holding the session list, the conversation and a rail with Fortschritt, Quellen and the invoices whose selection opens the pane. It is declared in `app.view.xml`, not in the chat page, because the footer is where a shell-wide element belongs. Its composer is on the shell's `chatDraft` channel with no binding at all - the shell does not isolate channels - so "Dazu fragen" on the page behind it puts its question here (#440). |

**The dock is a second place for one conversation, not a second conversation.** Its `chat-id` is
`chat` - the Chat page's own - so the dock opens what that page shows, and on the Chat page itself
it hides, launcher included: there is nothing there for it to open. That is not only tidiness. The
chat transport is **one conversation per window**: every `/chat/*` call is keyed by the window
alone, so two chat ids that each show a transcript in one window share the server's active session
and take each other's turns. Note that the id is written **twice** in `app.view.xml`, on the
`<chat-dock>` and on the `<chat>` inside it: the dock contains the chat, and a chat scope only
reaches downward.

The panel is hidden when the dock is closed, never unmounted, so a run started in the dock keeps
running and keeps its transcript while the user works on the page behind it; the launcher carries
the unseen count of the rail's sections and a dot while the agent is working. Ask something in the
dock on **Belege**, then open **Chat**: the same conversation is there, because it is the same
conversation.

The chat states facts about the situation it is asked in (`<chat-conversation><context>` in
`chat-grouped.view.xml`): the application, which holds for the whole conversation and is therefore
rendered into the run's system prompt, and the invoice open in the rail's pane, which follows the
user's clicks and is therefore not. Neither changes the message text - that is the point of the seam
(#437; see `tl-ai-core/doc/environment-block.md`).

The invoice is stated by a `<selection channel="paneObject"/>` rather than by a `<fact>`, which is
the one-line way to say which object the user has selected: the agent gets its label, its type and
the identifier every tool reads back, so it can act on the object and not only name it (#494). The
**dock** states one too, and its channel is the *page's*: the shell declares a `pageSelection`
channel, the Belege page hands its own selection over with one `<bind>` on its `<view-ref>`, and the
dock's conversation states that. Select a Beleg on **Belege**, open the dock and ask "Welches Objekt
habe ich gerade ausgewählt?" - what the model receives carries it.

Try the memory: ask Albert "Merke dir, dass ich kurze Antworten mag", and the row appears in the
rail's Gedächtnis section. Ask it to remember something without saying so - "ich mag kurze
Antworten" alone - and it does not: the tool refuses, and Albert asks first.

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

## An agent on a schedule

`WEB-INF/autoconf/com.top_logic.util.sched.Scheduler.config.xml` declares one
`com.top_logic.ai.chat.sched.AgentTask`, **DailyInvoiceSummary**, so that the path exists in a
running application and can be tried by hand. It asks the entry-point agent to summarise the invoices
recorded today and writes the sitting into `root`'s chat history, where it can be opened and
continued like any other conversation.

It is configured `enabled="false"`, because every run of it costs a real model call and this
application is started for a minute at a time. To see it work, either set `enabled="true"` and wait
for 18:00, or - the quicker way - open **Administration → Scheduler**, select `DailyInvoiceSummary`
and press "Start task". Then open **Chat**: the sitting is in the session list, titled
*Tageszusammenfassung der Belege*, with the transcript of what the agent did.

A scheduled run has no window, so it cannot ask anything: `askUser` in it answers "no user is present
in this run" instead of waiting, and what its tools may do is bounded by the tool-access policy's
`<background-allowlist>` rather than by an approval prompt. How to configure one, and what such a run
has and lacks, is in `tl-chat/doc/scheduled-agent.md`.

## Layout of the module

| Path | Contents |
|--|--|
| `WEB-INF/views/` | The React view layer: `app.view.xml` plus `tl-ai-demo/chat-grouped.view.xml`, `tl-ai-demo/rename-session.view.xml`, `tl-ai-demo/invoices.view.xml`, `tl-ai-demo/create-invoice.view.xml` and `tl-ai-demo/profile.view.xml` |
| `WEB-INF/layouts/` | The classic LayoutServlet UI |
| `WEB-INF/model/` | The demo model (`tl.ai.demo.imageclassification:Invoice`) |
| `WEB-INF/conf/` | Application configuration, including the `ClientResources` declaration that loads the chat bundle |
| `WEB-INF/autoconf/` | Service overlays: the agent definitions, the LLM service, the MCP server - and the `Scheduler` with the disabled `AgentTask` above |
| `doc/secdemo/` | A separate security testbed with its own personas; see `doc/secdemo/testbed.md` |
