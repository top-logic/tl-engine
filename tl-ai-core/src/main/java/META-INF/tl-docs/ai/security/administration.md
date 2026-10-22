---
description: Read when the precise semantics of the agent security configuration matter - the reference of AiToolAccessService / ToolAccessPolicy (<tool-groups>, <tool-access>, <baseline>, <background-allowlist>, boot errors and warnings, the three names of a tool), the defaults for a new tool, approval decided from the call (SecurityContextFactory, RunPrincipal, SecurityContextProxy, <security-context> on AgentRuntimeService), and the shipped read policy for Screenshot and CookiePool (tl-ai-core.binarySecurity.config.xml, AiBinaryOwner).
order: 70
---

# Administration reference

This article is the precise reference of the configuration; the [tool access configuration](doc:ai/security/tool-access-configuration) is the guide to start with, and [run access](doc:ai/security/run-access) covers `<agent-access>`, `<model-access>` and `<mcp-access>`.

## The tool access policy

`AiToolAccessService` is a `ConfiguredManagedClass` holding the role-to-tool policy, shaped like the engine's `SecurityConfigurationService` so administrators meet one idiom. Its configuration interface is `ToolAccessPolicy`. The service is active by default and cannot be switched off: a policy present while the module was off would be silently ignored, which is the one failure mode this layer must not have.

```xml
<config service-class="com.top_logic.tl.ai.core.security.AiToolAccessService">
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
<config service-class="com.top_logic.tl.ai.core.agent.AgentRuntimeService">
    <instance class="com.top_logic.tl.ai.core.agent.AgentRuntimeService">
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

The factory is told a `RunPrincipal`: the account the run acts as, how the run was started (`CHAT`, `SCRIPT`, `MCP`, `SCHEDULED`, `INTERNAL`), whether anybody is there to answer a prompt, and the conversation the run belongs to when the entry point has one. The run has no id and no agent yet - those are settled after the context is built. Three rules:

- **Every entry point asks.** `RunSecurity.contextFor` is the one call, and the chat, the TL-Script functions, the MCP endpoint and the prompt improver all go through it. An application substitutes one implementation, not several call sites.
- **Configuring nothing changes nothing.** The default is `DefaultSecurityContextFactory`.
- **A factory may narrow and may not widen.** Everything else about a run - which tools, which agents, which objects - is decided against the account's roles, and a context written from scratch would answer all of that permissively, since `SecurityContext`'s methods have permissive defaults. Build `DefaultSecurityContextFactory.defaultContext(run)`, wrap it in `SecurityContextProxy`, and override the one answer that is to differ.

## The shipped read policy for perceivable binaries

`tl-ai-core` fills one seam itself, for the two types it stores that are *perceivable*: `tl.ai.screenshots:Screenshot` and `tl.ai.cookiepool:CookiePool`. Both are reachable through `viewDocument`, which loads a `tl.core:Binary` attribute into a model's context, and neither is business data an application models: a screenshot is a picture of a page somebody drove a browser to while logged in somewhere, and a cookie pool is that login. Who may read one is therefore not left to whoever deploys the module.

**The policy: the owner, plus administrators.** A binary whose owner cannot be resolved - one an unattended run produced, with no `Person` to name - is readable by administrators and by code in a system context, and by nobody else. It is configuration, in `tl-ai-core/src/main/webapp/WEB-INF/conf/tl-ai-core.binarySecurity.config.xml`, in three pieces that are each the engine's own mechanism:

| Piece | What it does |
|---|---|
| `InitialRolesManager` role `AiBinaryOwner` | a technical role meaning "this object belongs to you"; nobody holds it globally |
| two `<role-rules>` of `type="reference"` | derive it, per object, from `Screenshot#owner` / `CookiePool#owner`. A path ending at a `Person` resolves to that person's representative group. An empty `owner` produces **no holder**, which is how the null-owner rule is written - as the absence of a holder, not as a denial somebody has to remember |
| `<security-config>` `Read` grants | make the role count, per type, and only for `Read` |

Nothing here is a second check: enforcement stays `ModelSecurity.canRead` → `ModelAccessRights.isAllowed`. The "plus administrators" half is not granted at all - it is `SecurityConfigurationService.isAllowedBypass`, which answers before any role is looked up. `Write`, `Delete` and `Create` are granted to nobody and stay with the administrators. The same file grants the owner of a research run read access to the run and the pages it references. Two boundaries:

- **A shared cookie pool is still owner-plus-administrator on the read path.** `CookiePoolService` lets every session *select* a shared pool to drive a browser with, and that path never asks model security. Sharing a login is not the same as letting a model read the stored session state.
- **`/screenshot/{id}` does not ride this policy.** It serves bytes to a browser, not to a run, and asks `ScreenshotService.mayRead`, which states the same rule in Java. An installation that *widens* the model grant does not widen the route, which is the conservative direction of the two.

An installation that wants a wider rule adds its own role to the grants, or a role rule of its own that puts `AiBinaryOwner` on the objects a reporting role is to reach. Binaries that existed before the rules were deployed are covered as well, because `ElementAccessManager` hashes the configured role rules into `DBProperties` and rebuilds the security storage when that hash changes. `TestPerceivableBinaryReadPolicy` pins all of it against the shipped configuration.
