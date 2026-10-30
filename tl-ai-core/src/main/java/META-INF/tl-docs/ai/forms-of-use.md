---
description: Read to understand the three forms in which AI agents are used in a TopLogic application - the built-in assistant (the chat, tl-chat), the external agent (MCP clients, tl-ai-mcp-server; A2A, tl-ai-a2a) and internal agents run on a schedule or an event (AgentTask, aiAgentChatAsync) - why they exist, and how agent and generated TL-Script relate.
order: 10
---

# Forms of use

An agent of a TopLogic application is used in three forms:

- **The built-in assistant** - a copilot in the application, to which users put questions and hand tasks.
- **Internal agents** - agents inside the application that take over recurring tasks on their own. They are started by a user's prompt, on a schedule, by an event or by another (external) agent, and they may use external tools (other applications) as well. What an agent is specialized for shows in its *skills*:
  - *development agents* support building the application - the model, the configuration, the layouts. Typical skills: find, analyze and change model parts, configure views.
  - *business agents* work with the application's data, locally and across systems. Typical skills: evaluate and monitor data, create and change records, trigger actions in other systems, send mails.
- **External agents** - independent AI tools (Claude Code, Claude Desktop, Cursor, the agent of another system) that connect to the application from outside and work with it.

All three use the same tools. What the built-in assistant can do, an agent can do too - automatically and without anybody asking.

## Why

TopLogic applications are highly configurable: data models, user interfaces and business logic are adapted without programming. That configurability is a strength, but it takes knowledge of the platform.

At the same time every application has a gap between what its user interface offers and what its users need day to day. Frequent work steps have dedicated functions; the many one-off or rare tasks do not. "Set the status of all orders of the last quarter to 'archived'", "move the contact data from the old structure into the new one", "show me all projects where budget and effort differ by more than 20%" - such tasks come up regularly, and none of them justifies a function of its own. Without an agent there are two options: build a special function for each case (expensive, and most wishes are never built), or have the user do it by hand (error-prone, and tedious for large amounts of data).

An agent is the Swiss army knife for exactly these situations. It needs no prepared function: it understands the task in natural language and carries it out with the tools it has - not only bulk operations, but every task that has no button of its own: reconciling data, summarizing, checking plausibility, applying changes by rules. The question moves from "do we have a function for that?" to "can I tell the assistant?".

Different groups benefit:

- **End users** ask about the application, search and evaluate data and automate routine work without waiting for a special function.
- **Configurators** explore and extend data models, build and adapt user interfaces and change configuration.
- **Developers** understand the application's structure, plan and carry out changes, and write and test scripts.

## The built-in assistant

The built-in assistant is a chat inside the application (`tl-chat`, in React pages through `<chat-conversation>`, `<chat-rail>` or `<chat-dock>`, in classic layouts through `tl-chat-classic`). The user asks a question or gives an order in natural language; the assistant understands it, carries out what is needed and answers. Behind it, the assistant talks to a language model (Anthropic, OpenAI, ...). The model decides by itself which tools it needs and may use several of them in a row to solve a complex task.

### From chatbot to copilot

A chatbot answers questions. A copilot works along - it sees what the user sees and acts in the same context. The step that makes the difference is access to the user's context: the assistant knows which view is open, which object is selected, which filters are active and which fields of a form are filled or invalid (the `<turn-context>` block, see [the per-turn block](doc:ai/environment#the-per-turn-block)). It can react to "this one here" instead of asking for type names and object ids.

*Example: the user opens a customer list and asks "Why is Müller Ltd. missing here?" The assistant sees the active view and its filters and answers: "Müller Ltd. has the status 'inactive'; your current filter shows active customers only."*

The other way round, the assistant leads the user to information: "here is the order you are looking for" - and opens the order's detail view (`present`), or marks the relevant field of a form instead of only naming it. The assistant becomes a copilot that sits next to the user, reads along and steps in at the right moment.

What the assistant can do with its tools is described in [capabilities](doc:ai/capabilities).

## The external agent

An external agent is an independent AI tool that connects to the running application through a standard protocol:

- **MCP** (Model Context Protocol, `tl-ai-mcp-server`), an open standard supported by many AI tools. The connection runs over HTTP at `/mcp/*`. The client reaches the tools the application publishes and, in addition, passive data sources it queries on purpose - the types of a module, the configuration of a view, the schema of a configuration type, an object by its id.
- **A2A** (Agent-to-Agent, `tl-ai-a2a`). The application serves its configured agents at `/a2a/{agent}`; the caller talks to an agent rather than calling single tools.

Both endpoints are closed as shipped. `/mcp/*` is switched off until an application switches its module on, and both serve no request they cannot attribute to an account; every request then acts as that account, bounded by its rights. [The MCP endpoint](doc:ai/security/mcp-endpoint) says what switching it on discloses and how; [MCP and A2A](doc:ai/protocols) describes the implementation.

### Agents as an interface

Integrations between applications are usually purpose-built interfaces: a REST API for customer data, a Kafka topic for order events, a file export for controlling. Each is built for one purpose and has to be adapted when that purpose changes. An application that makes its agent reachable from outside (A2A) offers an interface of a different kind: instead of a fixed data format, a counterpart that understands the application and answers questions - "which order types does your system have?", "are there open orders for customer 4711?", "how is the approval process for purchases set up?". This does not replace the classic interfaces - for high throughput and real-time processing a dedicated API stays the right way - but for exploration, alignment and occasional queries an agent offers what an API cannot: it understands context and explains.

## Internal agents

### From assistance to automation

The built-in assistant reacts to requests: a person asks a question or gives an order. Internal agents go one step further - they act on their own, triggered by time or by an event, without anybody starting them. An internal agent is set up for one task in one concrete application. It knows the business domain, has the same tools as the assistant and works in the background. An application runs one through the scheduler task `com.top_logic.ai.chat.sched.AgentTask` (a prompt to a named agent, run as a configured account, recorded as a chat session - see [agents on a schedule](doc:ai/script-and-scheduled-runs#agents-on-a-schedule)), or from TL-Script with `aiAgentChat` / `aiAgentChatAsync`.

On a schedule:

- *Every Monday morning*: "Check all open orders. If the delivery deadline ends in less than three days and no shipment is recorded, escalate to the responsible clerk."
- *On the first of each month*: "Compile all completed projects with their planned/actual budget comparison and send it to the management."
- *Every night*: "Check the data quality: customers without a contact person? Orders without items? Contacts with invalid e-mail addresses? Report the findings."

On an event:

- *An order is created*: "Check the customer's credit rating, compute the expected delivery time and notify the warehouse."
- *A status is set to 'completed'*: "Create the invoice, update the revenue statistics and archive the project documents."
- *An employee is added*: "Create the standard accounts, write an onboarding plan and assign the onboarding tasks to the team lead."

An unattended run has nobody to answer an approval prompt, so it is bounded by the `<background-allowlist>` in addition to the rights of the account it runs as - see [tool authorization](doc:ai/security/tool-authorization#background-and-delegated-runs).

### What distinguishes an agent from a script

Scheduled actions and event reactions exist without AI as well - as programmed scripts or workflow rules. The difference is flexibility. A script always performs exactly the same steps. A business agent *understands* the task and reacts to situations nobody foresaw: when the credit check returns something unexpected, the agent decides whether to hold the order back, propose a partial delivery or involve a person. A script fails or ignores the problem. Business agents are also easier to set up: instead of programming a technical rule, one describes in natural language what the agent shall do. That makes automation accessible to configurators and business departments, not only to developers.

### From agent to script - and back

Agent and script are not opposites but two ends of one spectrum, and on a no-code platform that is its own development environment the AI mediates between the two.

The typical path starts with a textual description: "every evening, check whether orders have a delivery deadline tomorrow, and send a reminder to the responsible clerk." That is enough to set up a business agent. On each run the AI interprets the task anew, picks the tools and reacts to the current data.

When it turns out that the task always runs the same way - the same steps, no special cases, no judgement - the AI can turn it into a TL-Script. That script is part of the application's configuration and runs natively, without a model call: faster, cheaper and predictable. The opposite direction works as well: a script that runs into new special cases is replaced by an agent that knows the basic logic and reacts flexibly to the exceptions, instead of being made ever more complicated.

```
  textual               business              generated
  description  ----->   agent       ----->    script
       ^                (flexible,            (fast,
       |                 AI-driven)            native)
       |                                        |
       +-------------- when needed <------------+
                        back to the agent
```

Both forms run in the same runtime. A generated script is no foreign body to deploy separately - it is a regular TL-Script configuration of the application - and the agent that wrote it can adapt or extend it later.

## Interplay of the three forms

| | Built-in assistant | External agent | Internal agents |
|---|---|---|---|
| Access | directly in the application | the user's own AI tool, or another system | in the background |
| Trigger | a question or an order | a question or an order | a schedule or an event |
| Audience | end users, business departments | configurators, developers, other systems | business departments, administrators |
| Strength | quick help in daily work | extensive configuration work | recurring tasks, acting proactively |
| Context | sees what the user sees | sees the whole structure | knows the business rules |

All three use the same set of tools, so a tool added for one form serves the others as well.
