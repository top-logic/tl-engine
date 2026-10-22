---
description: Read before giving agents a new capability - a Java tool (@ToolMethod with mode and group, registered as a factory of ToolRegistryService), a TL-Script tool (createScriptTool, classified in <tool-groups>), an external MCP server or A2A agent, a skill, prompt contributors and agent definitions.
order: 50
---

# Extensibility

The agents' tools and behaviour are extended in several ways. A tool added for one form of use serves all of them - the chat, internal agents and, once published, the external clients.

## Java tools

A developer writes a method and annotates it; `AnnotatedTools` (in `tl-ai-tool-api`) turns it into a `Tool` with a JSON schema:

```java
@ToolMethod(description = "Files the current draft under the agent that produced it.",
    mode = ToolMode.DATA_WRITE, group = ToolGroups.DATA_WRITE)
public String fileDraft(ToolExecutionContext context, @P("The draft text to file") String text) {
    ...
}
```

The class is registered as a factory of `ToolRegistryService`, which names its tools `{factory}.{tool}`:

```xml
<factory name="drafts">
	<implementation class="com.top_logic.tl.ai.tool.api.registry.ReflectiveToolFactory"
		tool-class="com.acme.ai.DraftTool"
	/>
</factory>
```

and an agent definition lists the tool it carries (`<tool name="drafts.fileDraft"/>`). Three declarations on `@ToolMethod` are security relevant, see [tool authorization](doc:ai/security/tool-authorization):

- `mode` - what a call changes (`READ`, `USER_SCOPED`, `DATA_WRITE`, `STRUCTURAL`, `EXTERNAL`); it decides about approval and the turn's transaction.
- `group` - the capability group an administrator grants. A tool without a group is denied to everyone.
- `types` - optionally the model types the tool works on, see [declared types](doc:ai/security/declared-types).

A tool depends only on `tl-ai-tool-api`, so any module can contribute tools without depending on the agent engine. `tl-ai-tool-api/README.md` describes the rest of the contract: which run a call belongs to (`RunInfo`), how a tool says what it read for citations (`ReadSource`), how its answer is marked as untrusted data (`UntrustedEnvelope`, `AnswerMarking`), and the rule for tools that wait for other threads' commits. [Tools and skills](doc:ai/tools-and-skills) describes the dispatcher.

## TL-Script tools

New tools are defined with TL-Script, without programming. An agent with the `agent.authoring` capability writes them itself (`createScriptTool`, `updateScriptTool` in a factory of script tools), and a configurator gives the agents access to further information that way. A TL-Script tool carries no annotation, so it arrives without a group and is denied to everyone until an administrator classifies it in `<tool-groups>` - deliberately, because whoever chooses a tool's group chooses which capability it costs to call it, see [tool authorization](doc:ai/security/tool-authorization#why-a-tool-cannot-choose-its-own-group).

For the MCP endpoint, `<dynamic-resources>` on `MCPServerService` define resources by TL-Script in the same way.

## External tools: MCP servers and A2A agents

An agent definition names MCP servers registered in `McpServerRegistry` (stdio or HTTP transport); their tools join the agent's catalog as `mcp__{server}__{tool}`. Remote tools are untrusted by construction: every one is `ToolMode.EXTERNAL`, it is ungrouped until an administrator names it in `<tool-groups>` (typically in `egress`), and what it answers is marked as untrusted data. `tl-ai-a2a` gives agents the client tools `a2a.*` to talk to the agents of other systems. See [MCP and A2A](doc:ai/protocols).

## Skills, agents and prompts

What an agent knows and does is configuration:

- **Agent definitions** (`TopLogicAgentDefinitions`, or agent records in the knowledge base edited in the application) give an agent its system prompt, its tools, its skills, its MCP servers, its delegates and its model.
- **Skills** are instruction texts with the tools they need, loaded on demand with `loadSkill` - see [tools and skills](doc:ai/tools-and-skills#skills-and-tool-overlays).
- **Prompt contributors** on `AgentApplicationService` add the application's standing instructions to every agent's prompt, for instance the application context the skill `TL_init` writes.
- The **environment block** tells every run whom it works for and in which application - see [the agent loop](doc:ai/agent-loop#the-environment-block).

Which agents, models and MCP servers a role may use at all is decided by the administrator, not by the agent definition - see [run access](doc:ai/security/run-access).
