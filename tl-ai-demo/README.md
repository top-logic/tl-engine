# tl-ai-demo

The demonstration application for this repository: one TopLogic app that bundles `tl-ai-core`,
`tl-chat`, `tl-chat-classic`, `tl-ai-mcp-server` and `tl-ai-vector-index`, with one business domain
for the agent to work on: event management (module `demo.events` - events, venues and their rooms,
sessions and speakers, registrations, sponsorships, invoices with their lines). The model was designed
together with Albert in a running demo and exported from it; a fresh start loads demo data from
`WEB-INF/data/events.objects.xml`: three venues, four events in four states, their programme,
registrations, two sponsors and invoices on both sides of the budget.

## Running it

```bash
mvn -pl tl-chat,tl-chat-classic -am -DskipTests install   # the chat bundle must exist before the app starts
cd tl-ai-demo
tl_anthropic_api_key="$ANTHROPIC_API_KEY" mvn tl:resolve compile exec:java
```

Then open <http://localhost:8080/tl-ai-demo/>. Pass `-Dtl.port=…` to use another port.

The `LLMProviderService` configuration references each provider's key by the name of its environment
variable (`<api-key name="tl_anthropic_api_key"/>`),
read at each request: the configuration holds the variable name, not the key, so saving the providers in
the Service Editor never writes the key into the file. The variables `tl_mistral_api_key`,
`tl_openai_api_key` and `tl_xai_api_key` work alike; a provider whose variable is not set fails only
when it is called. The vector index reads `tl_mistral_api_key` when it starts; without it, the vector index
is disabled and the application starts anyway.

The first start creates the H2 database under `tmp/` and prints the initial password of the `root`
account to the console:

```
***  Initial password for 'root': ….…  ***
```

## What you see

The start page is `/view/` - the React view layer. `WEB-INF/views/app.view.xml` is an overlay of the engine's application shell, which brings the app bar with the development and user menus (login, logout, theme, language) and the Administration item; the demo adds its title, its pages and the chat dock:

| | |
|--|--|
| **Chat** (start item) | The agent chat, built from view elements: a `<data-list>` of this user's chats, a `<chat-conversation>` (transcript, prompt field, agent/model/MCP/trace menu, attachments, human-in-the-loop approvals, context-usage ring), and a `<chat-rail>` whose sections are configured in `WEB-INF/views/tl-ai-demo/chat-grouped.view.xml` - Fortschritt, Quellen, Gedächtnis, and a `<data-list>` of the events whose selection opens an event form in the rail's pane. Fortschritt is a `<data-list>` too: the agent's plan is written to `tl.ai.execution:AgentTask` objects while it runs, so the list fills within about a second of each step and is still there after a reload. A chat row offers Umbenennen and Löschen behind the `⋯` that appears on hover - generic `<row-commands>` of the list, not chat-specific code; Umbenennen opens `tl-ai-demo/rename-session.view.xml`, Löschen confirms first and is hidden while that chat still has a run in it. The agent can open that pane itself: `<chat-conversation present="paneObject">` lets it write that one channel, so "zeig mir alle Events als Tabelle" is one `present` call with a TL-Script query and ends as a table of the events, "Umsatz pro Event als Balkendiagramm" as a bar chart, and "zeig mir KI im Mittelstand" as the event's form (#616). The pane is the rail's own and renders any type - venues and invoices as well as events, with nothing in the view naming a type. Clicking a row (or a bar) opens that object's form; "Zurück" returns to the table as it was left; `»` folds the pane away and the strip's first icon brings it back; the cross empties it. The rail's event rows offer "Dazu fragen" and "In die Nachricht übernehmen" the same way: the first writes a German sentence about that event to the `chatDraft` channel, the second writes the event itself to `chatReference`, and the composer shows the sentence, or the event as one atomic reference token, unsent (#440). Two more, "Beschreibung zitieren" and "Saldo zitieren", write a *passage* of the event to `chatQuote` - the token itself, with the marked text and where in the event it stands - so two passages of the same event insert as two tokens at the caret (#509). |
| **Veranstaltungen** | The events: a table on the left, the selected event on the right, on tabs - an overview with the figures the model computes (active registrations, free seats, revenue, expenses, balance) and the master data, then its programme, registrations, sponsors and invoices as `<composition-table>`s edited inside the event's form; a session and an invoice open a detail dialog for their speakers and lines. "Neu" creates an event, "Dazu fragen" writes a sentence about the selected event into the chat's composer - the chat being a *different* view on another nav item, which works because `app.view.xml` declares the `chatDraft` channel and hands the same one to both pages with `<bind>` (#440). `WEB-INF/views/tl-ai-demo/events/`. |
| **Veranstaltungsorte** | The venues with their rooms (a composition) and their address (a composition of one object, so it has a form of its own). A tile stack: a double-click on a venue opens it as a frame over the list, and the breadcrumb leads back. `WEB-INF/views/tl-ai-demo/venues/`. |
| **Rechnungen** | The invoices of all events: an invoice is part of its event, so the table shows the event as a column, and "Neu" asks which event the invoice belongs to, its direction and the scan - and has the model read the number, the dates, the tax rate, the counterparty and the lines off the image - try it with `doc/sample-invoice-scan.png`. `WEB-INF/views/tl-ai-demo/invoices/`. |
| **Meine Einstellungen** | What this account tells every agent about itself: one free-text field (`tl.accounts:Person#aiPreferences`, added by `tl-ai-core`). The harness renders it into the environment block of every run this user triggers, below what the application itself states; a text above the configured cap is refused here. `WEB-INF/views/tl-ai-demo/profile.view.xml`. |
| **Gedächtnis** (rail section on the Chat page) | What Albert has kept about this account across conversations (#439): a `<data-list>` over `tl.ai.execution:UserMemory` filtered by `owner == currentUser()`, newest first, with a count badge and a Forget command per row. Albert writes one only when the user asked it to in that turn, or after it asked with `askUser` and the user agreed - the `remember` tool refuses anything else, which is a Java check and not a line in its prompt. A new memory appears in this list at once and in Albert's system prompt in the *next* conversation, because that prompt has to stay byte-identical across the turns of one. |
| **Administration** | The framework's own administration view; visible to accounts holding the `administration` scope. Its "Agent activity" run detail shows the filled environment block of each run, beside the transcript. |
| **Albert**, the dock (bottom right, on every page but Chat) | The Chat page's conversation, reachable without leaving the page: a `<chat-dock>` in the shell's `<footer>` whose launcher opens a card holding the session list, the conversation and a rail with Fortschritt, Quellen and the events whose selection opens the pane. It is declared in `app.view.xml`, not in the chat page, because the footer is where a shell-wide element belongs. Its composer is on the shell's `chatDraft` channel with no binding at all - the shell does not isolate channels - so "Dazu fragen" on the page behind it puts its question here (#440). |

**The dock is a second place for one conversation, not a second conversation.** Its `chat-id` is
`chat` - the Chat page's own - so the dock opens what that page shows, and on the Chat page itself
it hides, launcher included: there is nothing there for it to open, and showing both would give one
`draft` channel two composers and one remembered layout two rails. A dock on a chat id of its own
would be a second conversation and would work: since #502 every `/chat/*` call names its
conversation beside its window, and the server holds an active session, a run and a stream for each.
Note that the id is written **twice** in `app.view.xml`, on the `<chat-dock>` and on the `<chat>`
inside it: the dock contains the chat, and a chat scope only reaches downward.

The panel is hidden when the dock is closed, never unmounted, so a run started in the dock keeps
running and keeps its transcript while the user works on the page behind it; the launcher carries
the unseen count of the rail's sections and a dot while the agent is working. Ask something in the
dock on **Veranstaltungen**, then open **Chat**: the same conversation is there, because it is the same
conversation.

The chat states facts about the situation it is asked in (`<chat-conversation><context>` in
`chat-grouped.view.xml`): the application, which holds for the whole conversation and is therefore
rendered into the run's system prompt, and the event open in the rail's pane, which follows the
user's clicks and is therefore not. Neither changes the message text - that is the point of the seam
(#437; see `tl-ai-core/doc/environment-block.md`).

The open object is stated by a `<selection channel="paneObject"/>` rather than by a `<fact>`, which is
the one-line way to say which object the user has selected: the agent gets its label, its type and
the identifier every tool reads back, so it can act on the object and not only name it (#494). The
**dock** states one too, and its channel is the *page's*: the shell declares a `pageSelection`
channel and publishes it with `<chat-channels>`, and the detail views of the Events, Venues and
Invoices pages write what they show into it with `<page-selection>` (#928). Select an invoice on
**Rechnungen**, open the dock and ask "Welches Objekt habe ich gerade ausgewählt?" - what the model
receives carries it. Double-click a venue on **Veranstaltungsorte**: the venue opens as a frame of a
tile stack, and the dock states that venue. On **Meine Einstellungen** it states `none`.

Try the memory: ask Albert "Merke dir, dass ich kurze Antworten mag", and the row appears in the
rail's Gedächtnis section. Ask it to remember something without saying so - "ich mag kurze
Antworten" alone - and it does not: the tool refuses, and Albert asks first.

The app bar carries one theme toggle: two `<set-theme>` commands of which the stylesheet shows only
the one that switches to the other theme (`style/tl-ai-demo-shell.css`). Everything below it, the
chat included, follows the choice without a reload.

The chat needs a signed-in user. A visitor who has not logged in gets an anonymous TopLogic session,
and `ChatServlet` answers every request from such a session with 401, so the chat shows a sign-in
note instead of a composer - the agent works with the user's data and the user's permissions.

**The classic UI** (`WEB-INF/layouts/mainTabbar.layout.xml`) serves the administration through the
LayoutServlet; the event domain has React views only. Reach it at
<http://localhost:8080/tl-ai-demo/servlet/LayoutServlet>. Its "Albert" tab comes from
`tl-chat-classic`, which this application depends on for exactly that reason: the React chat, written
into the classic tab (#618) - the module's generic view (`tl-chat-classic/chat.view.xml`: chats,
conversation, progress and sources), a conversation of its own beside the React pages' `chat`.

## An agent on a schedule

`WEB-INF/autoconf/com.top_logic.util.sched.Scheduler.config.xml` declares one
`com.top_logic.ai.chat.sched.AgentTask`, **DailyEventBriefing**, so that the path exists in a
running application and can be tried by hand. It asks the entry-point agent for the state of the
events - what takes place in the next 30 days, free seats, balance, overdue invoices - and writes the sitting into `root`'s chat history, where it can be opened and
continued like any other conversation.

It is configured `enabled="false"`, because every run of it costs a real model call and this
application is started for a minute at a time. To see it work, either set `enabled="true"` and wait
for 18:00, or - the quicker way - open **Administration → Scheduler**, select `DailyEventBriefing`
and press "Start task". Then open **Chat**: the sitting is in the session list, titled
*Tagesbericht Veranstaltungen*, with the transcript of what the agent did.

A scheduled run has no window, so it cannot ask anything: `askUser` in it answers "no user is present
in this run" instead of waiting, and what its tools may do is bounded by the tool-access policy's
`<background-allowlist>` rather than by an approval prompt. How to configure one, and what such a run
has and lacks, is in `tl-chat/doc/scheduled-agent.md`.

## Web research

Ask Albert to research a topic on the web ("Vergleiche die Wärmepumpen-Förderung in Deutschland,
Österreich und Dänemark"). It loads `TL_deepResearch`, shows a plan, starts research workers that
search and read the web, and answers with one report whose statements link to the pages the
workers read (`tl-ai-core/doc/deep-research.md`).

`WEB-INF/conf/research.config.xml`, on in `metaConf.txt`, supplies what that needs: Brave as search
engine, with the plain key in the environment variable `tl_brave_api_key`, and a
`<background-allowlist>` with `model.read`, `data.read` and `egress` - without it a spawned subagent
reaches no tool at all. The allowlist also bounds the scheduled task above. Turn the aspect off when
you turn on the security test bed, whose allowlist keeps `egress` out on purpose.

## Search by meaning

Ask Albert "Gibt es eine Veranstaltung zur Kühlkette für Impfstoffe?". It calls `index.search` and
finds the English *Cold Chain Logistics Forum 2027* (from `WEB-INF/data/events-en.objects.xml`),
whose text shares no word with the question: the index compares meanings, not only words
(`tl-ai-index/README.md`).

`WEB-INF/conf/ai-index.config.xml`, on in `metaConf.txt`, indexes the names and descriptions of
events and sessions and gives Albert the tools. The meaning side runs `snowflake-arctic-embed-m-v2.0`
in the JVM; put `model_int8.onnx` and `tokenizer.json` into `tmp/app-data/var/models/snowflake-arctic-embed-m-v2.0`
or into the directory named by the environment variable `tl_ai_index_model_dir`. Without them the demo
starts all the same and the index searches words only.

## Layout of the module

| Path | Contents |
|--|--|
| `WEB-INF/views/` | The React view layer: `app.view.xml` plus `tl-ai-demo/chat-grouped.view.xml`, `tl-ai-demo/rename-session.view.xml`, `tl-ai-demo/profile.view.xml` and the domain pages under `tl-ai-demo/events/`, `tl-ai-demo/venues/` and `tl-ai-demo/invoices/` |
| `WEB-INF/layouts/` | The classic LayoutServlet UI |
| `WEB-INF/model/` | The demo model `demo.events` (labels in `WEB-INF/conf/resources/model.demo.events.messages_*.properties`) and the security testbed's `pm` |
| `WEB-INF/data/` | The demo data loaded on the first start (`events.objects.xml`, and one English event in `events-en.objects.xml`) |
| `WEB-INF/conf/` | Application configuration, including the `ClientResources` declaration that loads the chat bundle |
| `WEB-INF/autoconf/` | Service overlays: the agent definitions, the LLM providers, the agent runtime (default model) and application knowledge (application name), the MCP server - and the `Scheduler` with the disabled `AgentTask` above |
| `doc/secdemo/` | A separate security testbed with its own personas; see `doc/secdemo/testbed.md` |
