---
description: Read when the precise semantics of the agent security configuration matter - the reference of AiToolAccessService / ToolAccessPolicy (<tool-groups>, <tool-access>, <baseline>, <background-allowlist>, boot errors and warnings, the three names of a tool), the defaults for a new tool, approval decided from the call (SecurityContextFactory, RunPrincipal, SecurityContextProxy, <security-context> on AgentRuntimeService), the shipped read policy for Screenshot and CookiePool (tl-ai-core.binarySecurity.config.xml, AiBinaryOwner), the shipped access policy for chat sessions and memories under model-based security (tl-ai-core.chatSecurity.config.xml, AiChatOwner), the Read and Write rights the chat asks for, how an application lets further accounts read a conversation about its object (a grant on AgentSession plus a role rule over AgentSession#subject), and the shipped access definition for stored agents and skills (tl-ai-core.agentSecurity.config.xml, recipes for team and personal agents).
order: 70
---

# Administration reference

This article is the precise reference of the configuration; the [tool access configuration](doc:ai/security/tool-access-configuration) is the guide to start with, and [run access](doc:ai/security/run-access) covers `<agent-access>`, `<model-access>` and `<mcp-access>`.

## The tool access policy

`AiToolAccessService` is a `ConfiguredManagedClass` holding the role-to-tool policy, shaped like the engine's `SecurityConfigurationService` so administrators meet one idiom. Its configuration interface is `ToolAccessPolicy`. The service is active by default and cannot be switched off: a policy present while the module was off would be silently ignored, which is the one failure mode this layer must not have.

```xml
<config service-class="com.top_logic.ai.core.security.AiToolAccessService">
    <instance>

        <!-- Group membership is declared on the tool (@ToolMethod(group=...)).
             This section only *overrides* it, or assigns a group to tools that
             cannot be annotated (MCP tools, TL-Script tools authored at runtime). -->
        <tool-groups>
            <group name="egress"
                tools="mcp__jira__createIssue, mcp__jira__addComment"
            />
            <group name="data.read"
                tools="script-tools.findOverdueInvoices"
            />
        </tool-groups>

        <!-- The role x tool matrix. Deny-by-default: a role with no entry gets nothing. -->
        <tool-access>
            <role name="SalesAssistantUser">
                <grants>
                    <grant group="model.read"/>
                    <grant group="data.read"/>
                    <grant group="agent.meta"/>
                    <grant tools="task-tools.createTask"/>
                </grants>
                <denies>
                    <deny group="model.structure"/>
                    <deny group="script"/>
                </denies>
            </role>

            <role name="SalesManager">
                <grants>
                    <grant group="model.read"/>
                    <grant group="data.read"/>
                    <grant group="data.write"/>
                    <grant group="workspace"/>
                    <grant group="egress"/>
                </grants>
                <denies>
                    <deny group="model.structure"/>
                </denies>
            </role>

            <role name="ApplicationDeveloper">
                <grants>
                    <grant group="model.read"/>
                    <grant group="model.structure"/>
                    <grant group="script"/>
                    <grant group="agent.authoring"/>
                </grants>
            </role>
        </tool-access>

        <!-- Granted to every authenticated user, on top of their role grants. "interaction" and
             "introspection" are granted implicitly and need no entry. -->
        <baseline>
            <grant group="workspace"/>
        </baseline>

        <!-- Ceiling for runs with no human to approve (aiAgentChatAsync, scheduler, MCP, A2A).
             Mandatory as soon as a role is named; all="true" opts out explicitly,
             an empty element forbids unattended runs every tool. -->
        <background-allowlist>
            <grant group="model.read"/>
            <grant group="data.read"/>
            <grant group="agent.meta"/>
        </background-allowlist>
    </instance>
</config>
```

The `<grants>` / `<denies>` and `<tools>` wrappers come from the typed-configuration list and map properties.

| Rule | Behaviour |
|---|---|
| No `<role>` entry for any role the user holds | All tools denied (baseline only) |
| `interaction`, `introspection` | Granted to every role and every run implicitly, whatever `<baseline>` says; a `<deny>` still wins (`RoleToolAccess.IMPLICIT_BASELINE`) |
| No `<baseline>` element at all | Every account gets `model.read` and `data.read` (`RoleToolAccess.DEFAULT_BASELINE`) |
| `<baseline/>` | Nothing beyond the implicit grants |
| `<baseline>` with entries | Exactly those - the default is replaced, not extended |
| Several held roles | Grants **union**; then denies subtract |
| `<deny>` anywhere among the held roles | Wins over any `<grant>`, from any role, and over the baseline |
| A tool with no group and no `<grant tools>` naming it | Denied |
| `<grant tools="a, b">` | Grants exactly the named tools, matched against any of `ToolIdentity.names()`; no pattern syntax |
| An entry naming a group and tools at once, or neither | Boot error - it would otherwise look like a grant and be none |
| A role that does not exist | Boot error, as `SecurityConfigurationService.processRights` does for unknown roles - a typo must not silently widen or narrow access |
| A group no tool is in | Boot **warning** listing the unknown and the known names - group names are an open space (applications, MCP servers and TL-Script tools contribute their own, and one can appear after boot), but a grant of a misspelled group reaches nothing |
| A policy that names a role and declares no `<background-allowlist>` | Boot error naming the three declarations that can be written instead; a policy with no `<tool-access>` entries boots without one |
| `<background-allowlist>` empty | Unattended runs reach nothing beyond the implicit grants |
| `<background-allowlist all="true"/>` | Unattended runs are bounded only by the acting user's grants |
| `<background-allowlist>` and administrators | Applies to them too - its reason is the missing approval prompt, not the principal's authority |
| `<background-allowlist>` and subagents | Applies to a detached one, not to a synchronous delegation, which completes inside the attended parent turn |

Design decisions worth knowing:

- **The default baseline lives in Java** (`RoleToolAccess.DEFAULT_BASELINE`), not as a `<baseline>` element in `tl-ai-core.conf.config.xml`. A keyless configuration list composes *additively* across configuration files, so a shipped element would be unioned with an application's own rather than replaced by it. Resolving it in Java keeps "writing `<baseline>` replaces the default" exactly true; `<baseline>` is therefore a nullable *item* with three states rather than a list, the same shape `<background-allowlist>` has.
- **The group constants live in `tl-ai-tool-api`** (`ToolGroups`), not next to the policy: a tool declares its group on `@ToolMethod`, so the legal values must be nameable from every module that can define a tool.
- **A tool has three names** - the bare `Tool.name()` the model calls, the configured `{factory}.{tool}` name an administrator writes, and the provider-safe `factory__tool` form the registry keys it by. A `<grant tools="...">` entry matches any of them.
- **The policy is static application configuration**, loaded at startup, like the engine's `<security-config>`. The *Policy* tab of the agent security view writes it into the application's `autoconf` and the service applies it after a restart ([the policy editor](doc:ai/security/tool-access-editor)).

At boot the service logs what it read, e.g. `Agent tool access: roles=2 baseline=default[model.read, data.read] backgroundAllowlist=3`, and warns about roles that reach nobody ([how the two layers combine](doc:ai/security/data-authorization#how-the-two-layers-combine)).

## Defaults for a new tool

1. The tool's author sets `group` on `@ToolMethod`. If they forget, the tool is ungrouped, denied to everyone, and listed as *NO GROUP* in the tool catalog of the agent security view.
2. No existing grant reaches a group it does not name, so a tool in a new group needs an administrator to add that group deliberately - a privileged one above all.
3. If the group is an ordinary one, roles already granted that group get the tool. This is the trade-off that makes groups worth having: adding `getInvoiceById` to `data.read` grants it to everyone with `data.read`, and the compensating control is L2 - the tool still cannot return an object the user may not read.
4. MCP tools arrive at runtime and cannot be annotated. They are ungrouped and denied until granted through `<tool-groups>` - explicitly, tool by tool, ideally into `egress`.

Approval stays a property of the tool's `ToolMode` for every role: a tool that changes shared data asks the user to confirm regardless of the roles they hold, and no role is pre-approved.

## Approval decided from the call

`SecurityContext.checkApproval(Tool, ToolCall)` receives the call with its arguments and can therefore answer two calls of the same tool differently. The default answers by `ToolMode` alone, which is right for most applications and wrong for one whose authority follows the *state of the object*: overwriting an empty section a machine wrote, overwriting what a colleague wrote, and releasing a document to a customer are all `DATA_WRITE`, and only the second and third deserve a prompt. `ApprovalMode` cannot express that - it decides *who* answers a prompt, not *whether* one is raised.

The seam is the factory that builds the context of a run, configured on `AgentRuntimeService`:

```xml
<config service-class="com.top_logic.ai.core.agent.AgentRuntimeService">
    <instance class="com.top_logic.ai.core.agent.AgentRuntimeService">
        <security-context class="com.acme.agent.SectionApproval"/>
    </instance>
</config>
```

```java
public class SectionApproval implements SecurityContextFactory {
    @Override
    public SecurityContext createSecurityContext(RunPrincipal run) {
        SecurityContext base = DefaultSecurityContextFactory.defaultContext(run);
        return new SecurityContextProxy(base) {   // delegates everything
            @Override
            public PermissionDecision checkApproval(Tool tool, ToolCall call) {
                // ... read the object the call names, answer Allowed or NeedsApproval
            }
        };
    }
}
```

The factory is told a `RunPrincipal`: the account the run acts as, how the run was started (`CHAT`, `SCRIPT`, `MCP`, `A2A`, `SCHEDULED`, `INTERNAL`), whether anybody is there to approve a tool call, and the conversation the run belongs to when the entry point has one. The run has no id and no agent yet - those are settled after the context is built. Three rules:

- **Every entry point asks.** `RunSecurity.contextFor` is the one call, and the chat, the TL-Script functions, the MCP and A2A endpoints and the prompt improver all go through it. An application substitutes one implementation, not several call sites.
- **Configuring nothing changes nothing.** The default is `DefaultSecurityContextFactory`.
- **A factory may narrow and may not widen.** Everything else about a run - which tools, which agents, which objects - is decided against the account's roles, and a context written from scratch would answer all of that permissively, since `SecurityContext`'s methods have permissive defaults. Build `DefaultSecurityContextFactory.defaultContext(run)`, wrap it in `SecurityContextProxy`, and override the one answer that is to differ.

## The shipped read policy for perceivable binaries

`tl-ai-core` fills one seam itself, for the types it stores that are *perceivable*: `tl.ai.screenshots:Screenshot`, and - with the browser tools of `tl-ai-browser` - `tl.ai.cookiepool:CookiePool`. Both are reachable through `viewDocument`, which loads a `tl.core:Binary` attribute into a model's context, and neither is business data an application models: a screenshot is a picture of a page somebody drove a browser to while logged in somewhere, and a cookie pool is that login. Who may read one is therefore not left to whoever deploys the module.

**The policy: the owner, plus administrators.** A binary whose owner cannot be resolved - one an unattended run produced, with no `Person` to name - is readable by administrators and by code in a system context, and by nobody else. It is configuration, in `tl-ai-core/src/main/webapp/WEB-INF/conf/tl-ai-core.binarySecurity.config.xml` and, for the cookie pools, `tl-ai-browser/src/main/webapp/WEB-INF/conf/tl-ai-browser.binarySecurity.config.xml`, in three pieces that are each the engine's own mechanism:

| Piece | What it does |
|---|---|
| `InitialRolesManager` role `AiBinaryOwner` | a technical role meaning "this object belongs to you"; nobody holds it globally |
| `<role-rules>` of `type="reference"` | derive it, per object, from `Screenshot#owner` / `CookiePool#owner`. A screenshot's owner is the owner of the agent session it is a part of. A path ending at a `Person` resolves to that person's representative group. An empty `owner` produces **no holder**, which is how the null-owner rule is written - as the absence of a holder, not as a denial somebody has to remember |
| `<security-config>` `Read` grants | make the role count, per type, and only for `Read` |

Nothing here is a second check: enforcement stays `ModelSecurity.canRead` → `ModelAccessRights.isAllowed`. The "plus administrators" half is not granted at all - it is `SecurityConfigurationService.isAllowedBypass`, which answers before any role is looked up. `Write`, `Delete` and `Create` are granted to nobody and stay with the administrators. The same file grants the owner of a research run read access to the run and the pages it references. Two boundaries:

- **A shared cookie pool is still owner-plus-administrator on the read path.** `CookiePoolService` lets every session *select* a shared pool to drive a browser with, and that path never asks model security. Sharing a login is not the same as letting a model read the stored session state.
- **`/screenshot/{id}` does not ride this policy.** It serves bytes to a browser, not to a run, and asks `ScreenshotService.mayRead`, which states the same rule in Java. An installation that *widens* the model grant does not widen the route, which is the conservative direction of the two.

An installation that wants a wider rule adds its own role to the grants, or a role rule of its own that puts `AiBinaryOwner` on the objects a reporting role is to reach. Binaries that existed before the rules were deployed are covered as well, because `ElementAccessManager` hashes the configured role rules into `DBProperties` and rebuilds the security storage when that hash changes. `TestPerceivableBinaryReadPolicy` (`tl-ai-core`) and `TestCookiePoolReadPolicy` (`tl-ai-browser`) pin all of it against the shipped configuration.

## The shipped access policy for conversations

The chat sessions (`tl.ai.execution:AgentSession`) and the memories (`tl.ai.execution:UserMemory`) are their owner's. In an application with model-based security, TL-Script hides every object the acting user may not discover ([R4](doc:access-discover-spec)), so without a policy a user who is no administrator would find none of their own chats - not through `referers` on the subject, not through `all(...)`, and not as the result of a script function such as `chatStart(...)`, and a `<chat-conversation>` bound by such an expression would never show the user's session. `tl-ai-core/src/main/webapp/WEB-INF/conf/tl-ai-core.chatSecurity.config.xml` ships the policy, built like the one for perceivable binaries:

| Piece | What it does |
|---|---|
| `InitialRolesManager` role `AiChatOwner` | a technical role meaning "this conversation, or this memory, is yours"; nobody holds it globally |
| two `<role-rules>` of `type="reference"` | derive it, per object, from `AgentSession#owner` / `UserMemory#owner`. A session without an owner - of a background job, a schedule - has no holder |
| `<security-config>` grants | `Read` and `Write` on a session and on a memory |
| `<access-parent>` `<target>` | a run (`AgentExecution#session`) and a plan item (`AgentTask#session`) decide through their session, a question (`InteractionRequest#execution`) through its run |

`Create` and `Delete` of a session or a memory are granted to nobody: sessions and memories are created and deleted by the `AgentExecutionStore`, which writes in a system context, after the caller checked `Write`. A memory decides through its own owner and not through its session, since the session reference is optional and a memory outlives its conversation. Administrators and system contexts bypass the check as everywhere. `TestChatSessionAccessPolicy` pins the policy against the shipped configuration.

**The chat decides through these rights alone.** There is no check of the owner in Java: every access to a session asks `ModelAccessRights.isAllowed` for the acting account.

| Right on `tl.ai.execution:AgentSession` | What it allows |
|---|---|
| `Read` | open the session in a `<chat-conversation>` and see its history, the history of its subagents and its run state; take it over from the element's `input` channel; follow a running run live |
| `Write` | send, edit and resend, regenerate, rate, answer approvals and questions, stop a run, change the approval mode, attach files, improve a prompt, rename (`aiSessionRename`), delete (`aiSessionDelete`), mark as seen, and every other change of the session's data |

`Write` on a `tl.ai.execution:UserMemory` is what `aiMemoryDelete` asks for. The shipped policy gives both rights to the owner only, so without further configuration a chat is its owner's. A session somebody may read but not write shows no input line.

**Letting further accounts read a conversation.** Which roles read or write a session is the application's decision, made with its usual means: a grant of its own on `tl.ai.execution:AgentSession` for a role of its own, and a role rule that brings that role to the session. A session points at what it is about through `tl.ai.execution:AgentSession#subject`, so the rule typically starts there. Example: the members of a workspace (role `my.module.Member` on a `my.module:Workspace`) read the conversations about it, without writing into them:

```xml
<config service-class="com.top_logic.base.services.InitialRolesManager">
	<instance>
		<roles>
			<role name="my.module.ChatReader"/>
		</roles>
	</instance>
</config>

<config service-class="com.top_logic.tool.boundsec.manager.AccessManager">
	<instance>
		<role-rules>
			<rules>
				<!-- Who is a member of the workspace a conversation is about reads that conversation. -->
				<inheritance-rule id="my.module.ChatReader.subject"
					meta-element="tl.ai.execution:AgentSession"
					role="my.module.ChatReader"
					source-meta-element="my.module:Workspace"
					source-role="my.module.Member"
				>
					<path>
						<step attribute="tl.ai.execution:AgentSession#subject"/>
					</path>
				</inheritance-rule>
			</rules>
		</role-rules>
	</instance>
</config>

<config service-class="com.top_logic.model.security.SecurityConfigurationService">
	<instance>
		<security-config>
			<class name="tl.ai.execution:AgentSession">
				<grant operation="Read" roles="my.module.ChatReader"/>
			</class>
		</security-config>
	</instance>
</config>
```

The grant adds to the owner's grants of the shipped policy, so the owner keeps reading and writing every session, and a session about another object, or about nothing, stays its owner's. Granting `Write` to such a role as well lets its holders write into the conversation; a run they start belongs to them. Whether a conversation that may be written is offered for writing at all - e.g. not for a closed workspace - is no question of rights: the `writable` function of `<chat-conversation>` decides it, together with `Write`.

## The shipped access definition for stored agents and skills

Stored agents and skills (`tl.ai.core:Agent`, `tl.ai.core:Skill`) are under the model rights of the application, in addition to `<agent-access>`; an account that may not read one does not know it ([run access](doc:ai/security/run-access#stored-agents-and-skills-under-model-rights)). `tl-ai-core/src/main/webapp/WEB-INF/conf/tl-ai-core.agentSecurity.config.xml` gives the two base types their definition:

| Piece | What it does |
|---|---|
| `<security-parents>` rules to `SecurityStructure#ROOT`, not inherited | a role held on the security root is held on each stored agent and skill, so a grant of the application to such a role takes effect |
| `<security-config>` `Read` for `SecurityStructure.user`, not inherited | every member of the group `user` - every new account - sees the stored agents and skills, as before |
| `<part>` grants on `Agent#mcpServers` and `Agent#infrastructure` with no role | nobody but an administrator changes them: every run of an agent reaches the tools of its MCP servers, and the engine runs an infrastructure agent in its own code |

`Create`, `Write` and `Delete` of the two base types are granted to nobody. Who may create an agent where is a model right that each application writes into its own security configuration. Administrators and code in a system context bypass the check as everywhere.

Nothing is inherited, on purpose. **A specialization starts closed**: without rules of the application, nobody but an administrator reads it. **A specialization in a composition follows its container**: a composition part with no rule of its own takes its container as access parent ([access configuration](doc:access-configuration#access-parent-delegating-the-decision)), so whoever reads the container reads its agents, and whoever writes the container creates, changes and deletes them. An inherited role source would switch that default off. The `<part>` grants apply to the specializations as well; an application gives `Write` on the two attributes to another role with a `<grant>` of its own on the same `<part>`.

The `Read` grant makes the instructions of a stored agent (`systemPrompt`) and the text of a skill (`content`) readable for the group `user`, also with TL-Script and with a data tool of an agent. An application that hides them adds a `<part>` grant; a run of the agent still gets its instructions, since the engine reads them without the rights of the account, and the authoring tools of an agent hand out only the attributes its run may read:

```xml
<part name="tl.ai.core:Agent#systemPrompt">
	<grant operation="Read" roles="AgentEditor"/>
</part>
<part name="tl.ai.core:Skill#content">
	<grant operation="Read" roles="AgentEditor"/>
</part>
```

**Names are global.** `UniqueNameConstraint` refuses, at commit, a name that another stored record of the base type has (in any case of the letters, also a record the person may not read), and the name of a configured agent for a stored agent and of a configured skill for a stored skill, unless an administrator or a system context saves it. A record that already has the name of a configured one is refused at its next change by a person who is no administrator; rename it. A name that starts or ends with a blank is refused for everybody, administrators and system contexts too, so a record that already has such a name cannot be changed until it is renamed. The lookup by name (`AgentDefinitionQuery.findStoredAgent`, `findStoredSkill`) filters what the database finds with the same comparison, so that a collation that ignores accents or trailing blanks cannot make one record answer for another name.

### Recipe: the agents of a team

A specialization held in a composition of the team. No security rule is needed: the agents follow the team.

```xml
<class name="Team">
	<attributes>
		<reference name="agents" composite="true" multiple="true" kind="forwards" type="TeamAgent"/>
	</attributes>
</class>
<class name="TeamAgent">
	<generalizations>
		<generalization type="tl.ai.core:Agent"/>
	</generalizations>
</class>
```

A member of a team who may read the team sees, starts and delegates to its agents; a member who may write the team creates, changes and deletes them. Everybody else does not know them, and a person of another team who picks the same name is told that it is in use. The security test bed of `tl-ai-demo` does this with `pm:ProjectAgent` and `pm:ProjectSkill` ([test bed](doc:ai/security/test-bed)).

### Recipe: personal agents

A specialization with an owner, and a role that a rule derives from the owner, built like the access policy for conversations above:

```xml
<!-- my.module.model.xml -->
<class name="PersonalAgent">
	<generalizations>
		<generalization type="tl.ai.core:Agent"/>
	</generalizations>
	<attributes>
		<reference name="owner" kind="forwards" type="tl.accounts:Person"/>
	</attributes>
</class>

<!-- InitialRolesManager -->
<role name="AgentOwner"/>

<!-- AccessManager -->
<rule id="my.module.agent.owner" meta-element="my.module:PersonalAgent" role="AgentOwner" type="reference">
	<path>
		<step attribute="my.module:PersonalAgent#owner"/>
	</path>
</rule>

<!-- SecurityConfigurationService -->
<class name="my.module:PersonalAgent">
	<grant operation="Read" roles="AgentOwner"/>
	<grant operation="Write" roles="AgentOwner"/>
	<grant operation="Delete" roles="AgentOwner"/>
	<grant operation="Create" roles="SecurityStructure.user"/>
</class>
```

`Create` is checked on the security root for a type that no composition holds, where every member of the group `user` holds `SecurityStructure.user`. The view that creates the agent sets `owner` to the creating account. To share a personal agent with somebody, grant `Read` to a second role that a rule derives from a reference such as `sharedWith`.
