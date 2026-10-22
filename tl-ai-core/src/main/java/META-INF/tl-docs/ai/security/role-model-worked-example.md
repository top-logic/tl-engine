---
description: Read first when setting up agent security for an application - a complete worked example with one role vocabulary and two grant tables (<security-config> of SecurityConfigurationService and <tool-access> of AiToolAccessService), roles held on the security root (tier, <singleton-rule>) versus on objects (scope, <rule type="reference">), use-default-security-parent, <part> grants, and four personas walked through every operation.
order: 5
---

# Worked example: one role vocabulary, two grant tables

This article makes agent security explainable. It builds a small, complete application from scratch - model, groups, roles, role rules, model rights, agent tool access - and then walks every persona through every operation, naming which layer decides and why. It is the article to read before the [security overview](doc:ai/security), which explains the design; this one shows it running.

## The one idea you need

**A role is not a permission. A role is a relationship: `(group, object, role)`.**

Roles live in one table, the `SecurityStorage`, whose rows say "members of group G hold role R on object X". Nothing else about security is stored anywhere. Asking "does Anna hold role R?" is not a well-formed question. Asking "does Anna hold role R **on object X**?" is, and the answer walks X plus X's security ancestors, never X's children.

On top of that one substrate sit **grant tables**, which say what holding a role permits. TopLogic 8 has three, and they never talk to each other:

| Grant table | Governs | Asks about the role at |
|---|---|---|
| roles profiles | the classic UI | each view |
| `<security-config>` | model access from TL-Script, search, REST, agents | **the object being touched, or one of its security ancestors** |
| `<tool-access>` | which agent tools may be called | **the security root** |

The last two are the ones this article is about. Both read the same role vocabulary. The crucial difference is the last column: they ask about the same role at **different objects**, and that is what lets a single role name answer two different questions.

- `projectPartner` held **on the security root** means "project partners, as a class of user, may use the agent's write tools." A deployment-wide statement. This is a *tier*.
- `projectPartner` held **on project P1** means "you are a partner *of P1*." An object-level statement. This is a *scope*.

The same role, assigned in two places by two rules, carries both. **No separate role vocabulary is needed for agent access** - one role and two assignments.

## The hinge that makes or breaks this

Whether the tier and the scope stay independent depends on one configuration flag:

```xml
<config service-class="com.top_logic.tool.boundsec.BoundHelper">
  <instance use-default-security-parent="false"/>
</config>
```

`false` is the engine default, and this example keeps it.

Role resolution goes downward only. If the security root is made the security parent of every object, then a role held on the root is held on **every object in the system**. The tier assignment swallows the scope assignment, and "partner of P1" stops meaning anything, because you are a partner of everything.

- **Root not a security ancestor of business objects** (this example): a role on the root grants tools and nothing else. A role on P1 grants rights on P1 and nothing else. The two assignments are independent, and per-object scoping works.
- **Root is a security ancestor**: tier and scope collapse. You can still differentiate per *type* through `<security-config>`, but never per instance.

> ⚠️ **The consequence people trip over.** With the flag at `false`, a role held on the root grants **no model access at all** - not to one object, not to a type, not to anything. `<security-config>` resolves a role by walking the object and the object's security *ancestors*, and the root is not among them. So "everyone in this group may read all tickets" is not expressed by a root assignment plus a type grant; it needs a role rule that produces the role **on every ticket**. The [type-scope rule](#why-the-type-scope-rule-is-not-redundant) below is that rule.

Whenever this design reads as incomprehensible, this flag is usually why. `SecDemoRoleRuleTest.testTheTierDoesNotBecomeAScope` fails if the test bed ever flips it.

## The example application

Domain: projects with tickets. Two capabilities to express, which between them cover every mechanism:

1. A project partner may read and edit **the projects they partner on**, and the tickets of those projects. Not other projects.
2. A ticket reader may read **all** tickets, and write none.

### The model

`WEB-INF/model/pm.model.xml`, module `pm`:

- `pm:Project`
  - `name`: String
  - `partners`: reference to `tl.accounts:Person`, multiple. This attribute *is* the security rule's input.
  - `tickets`: composition of `pm:Ticket`, multiple
- `pm:Ticket`
  - `title`: String
  - `body`: String

What carries the security is an ordinary business attribute, `partners`. Security is derived from the data, not maintained beside it.

### Roles

Roles are declared centrally in TopLogic 8:

```xml
<config service-class="com.top_logic.base.services.InitialRolesManager">
  <instance>
    <roles>
      <role name="projectPartner"/>
      <role name="ticketReader"/>
    </roles>
  </instance>
</config>
```

Two roles. Both appear in both grant tables.

### Groups

```xml
<config service-class="com.top_logic.base.services.InitialGroupManager">
  <instance default-group="users">
    <groups>
      <group name="users"/>
      <group name="projectPartners"/>
      <group name="ticketReaders"/>
    </groups>
  </instance>
</config>
```

`default-group="users"` adds every account created to `users` automatically. Membership in `projectPartners` and `ticketReaders` is what an administrator maintains.

### Role rules: where the two assignments happen

This is the heart of the example: two tier rules, one scope rule, one type-scope rule, and one security-parent rule on the access manager.

```xml
<config service-class="com.top_logic.tool.boundsec.manager.AccessManager">
  <instance class="com.top_logic.element.boundsec.manager.ElementAccessManager">
    <role-rules>
      <rules>

        <!-- TIER 1. Members of "projectPartners" hold projectPartner on the security root.
             This, and only this, is what <tool-access> can see. -->
        <singleton-rule id="tier-projectPartner"
          target="SecurityStructure#ROOT"
          role="projectPartner"
        >
          <path>
            <group-with-name name="projectPartners"/>
          </path>
        </singleton-rule>

        <!-- TIER 2. Same shape for ticket readers. -->
        <singleton-rule id="tier-ticketReader"
          target="SecurityStructure#ROOT"
          role="ticketReader"
        >
          <path>
            <group-with-name name="ticketReaders"/>
          </path>
        </singleton-rule>

        <!-- SCOPE. Whoever is listed in a project's "partners" attribute holds
             projectPartner ON THAT PROJECT. Derived from business data, per object. -->
        <rule id="scope-projectPartner"
          meta-element="pm:Project"
          role="projectPartner"
          type="reference"
        >
          <path>
            <step attribute="pm:Project#partners"/>
          </path>
        </rule>

        <!-- TYPE SCOPE. Members of "ticketReaders" hold ticketReader on EVERY ticket.
             Not redundant with the tier rule above. -->
        <rule id="type-ticketReader"
          meta-element="pm:Ticket"
          role="ticketReader"
          type="reference"
        >
          <path>
            <group-with-name name="ticketReaders"/>
          </path>
        </rule>

      </rules>
    </role-rules>

    <!-- A ticket's security parent is the project that contains it, so a partner's
         scope on a project reaches that project's tickets without a second rule. -->
    <security-parents>
      <rules>
        <rule id="ticket-in-project" meta-element="pm:Ticket">
          <path>
            <step attribute="pm:Project#tickets" inverse="true"/>
          </path>
        </rule>
      </rules>
    </security-parents>
  </instance>
</config>
```

Three mechanisms, three jobs, worth naming individually because they are often conflated:

- **`<singleton-rule>`** puts a role on one well-known object - here the root, which is the only place a tier can live.
- **`<rule type="reference">`** derives a role on every matching object by navigating a `<path>` of business attributes to the accounts or groups that should hold it. `<step attribute="...">` navigates forwards; `inverse="true"` navigates backwards. A path may end at a `Group` or at a `Person`; a `Person` is resolved to their representative group.
- **`<security-parents>`** decides what "ancestor" means for role resolution, and therefore what a scoped role reaches downward.

Every rule needs a unique `id` in TopLogic 8 (the `ticket29221` migration adds them to older configurations). `NavigationRuleConfig.getPathElements()` is `@Name("path")` *and* `@DefaultContainer`, so a step may be written inside `<path>...</path>` or directly as a child of the rule; both spellings parse to the same rule. This example uses the wrapper, because a two-element path is unambiguous that way.

#### Why the type-scope rule is not redundant

It looks like it duplicates the tier rule, and it does not. The two answer different questions, at different objects:

| | `tier-ticketReader` | `type-ticketReader` |
|---|---|---|
| Puts the role on | the security root | every `pm:Ticket` |
| Satisfies | `<tool-access>` - "may this account call a read tool at all" | `<security-config>` - "may this account read *this* ticket" |
| Without it | Ben holds no capability, so no read tool is offered to the agent at all | Ben holds every read tool and gets **an empty list** from each of them |

Leave the second one out and the application looks like it works: the tools are there, they run, they return nothing, and nothing anywhere says why. That is the same mistake as granting only the scope and expecting L1 to pass, in the opposite direction, and it is why both assignments are written out here.

**`<path>` with no `<step>` is the shape for "everybody in this group, on every instance of this type".** It is what a `<singleton-rule>` does for one well-known object, applied to a type.

### Model rights: what a role may do with data

```xml
<config service-class="com.top_logic.model.security.SecurityConfigurationService">
  <instance>
    <security-config>
      <class name="pm:Project">
        <grant operation="Read"   roles="projectPartner"/>
        <grant operation="Write"  roles="projectPartner"/>
        <grant operation="Create" roles="projectPartner"/>
      </class>
      <class name="pm:Ticket">
        <grant operation="Read"   roles="projectPartner, ticketReader"/>
        <grant operation="Write"  roles="projectPartner"/>
        <grant operation="Create" roles="projectPartner"/>
      </class>
      <part name="pm:Ticket#body">
        <grant operation="Read"   roles="projectPartner"/>
      </part>
    </security-config>
  </instance>
</config>
```

Three properties of this table matter more than its contents:

**Deny by default, and it is real.** A type with no grant for an operation resolves to an empty role set, and a check against an empty role set is false. `pm:Ticket` has no Delete grant, so nobody deletes tickets by role. A denial is never written.

**A `<part>` grant narrows, it does not add.** An attribute-level grant is consulted only *after* the class-level check has passed, and its absence means the attribute inherits that decision. So the entry above says "of a ticket you may read, `body` is for partners only" - a ticket reader gets the ticket and its `title`, and `body` is simply absent from what a tool reports. Writing `<part>` grants for every attribute is neither necessary nor intended.

**This service is active in every TopLogic 8 application, configured or not.** `tl-core` registers it with an empty `<security-config>`, which denies every operation to every role-based user. An application that does not write this block has not opted out of model security; it has denied everything.

Administrators and engine super-users bypass all of it, and so does any code running in a system context: an admin account proves nothing in a test.

### Agent tool access: which tools a role may call

```xml
<config service-class="com.top_logic.tl.ai.core.security.AiToolAccessService">
  <instance>
    <tool-access>
      <role name="projectPartner">
        <grants>
          <grant group="model.read"/>
          <grant group="data.read"/>
          <grant group="data.write"/>
        </grants>
      </role>
      <role name="ticketReader">
        <grants>
          <grant group="model.read"/>
          <grant group="data.read"/>
        </grants>
      </role>
    </tool-access>
    <!-- A policy that names roles must also declare what an unattended run may reach, or it
         does not boot. <background-allowlist/> forbids such runs every tool. -->
    <background-allowlist>
      <grant group="model.read"/>
      <grant group="data.read"/>
    </background-allowlist>
  </instance>
</config>
```

**`data.write` is a tier and cannot be anything else.** At the moment this gate runs, the model has proposed a write tool and there is no object yet to check. So this table answers exactly one question: "may the agent wield this kind of power on your behalf at all." Every question of the form "on which object" and "of which type" belongs to `<security-config>` and is answered later, per object. That division is not a limitation to work around: it is the only decomposition available, and it is why `<tool-access>` reads roles on the security root - the root is where a deployment-wide statement about a person lives.

## The personas, and what each one can do

Four accounts. The fourth exists to demonstrate a failure mode, not a capability.

| Persona | Groups | `projectPartner` on | `ticketReader` on |
|---|---|---|---|
| Anna | projectPartners | root (tier), P1 (scope) | - |
| Ben | ticketReaders | - | root (tier), every ticket (type scope) |
| Cara | projectPartners, ticketReaders | root (tier), P2 (scope) | root (tier), every ticket (type scope) |
| Dora | projectPartners | root (tier) only, partner of nothing | - |

P1 and P2 are projects; Anna is in P1's `partners`, Cara in P2's.

Each row names the layer that decides. Note how often the answer is L2, and that L1 passes in almost every interesting case.

| Persona asks the agent to | L1 (tools) | L2 (data) | Result |
|---|---|---|---|
| Anna: edit project P1 | pass, holds `data.write` on root | pass, holds `projectPartner` on P1 | **allowed** |
| Anna: edit project P2 | pass, same tier | deny, no `projectPartner` on P2 | **refused** |
| Anna: edit a ticket of P1 | pass | pass, P1 is the ticket's security parent | **allowed** |
| Anna: edit a ticket of P2 | pass | deny, no role on P2 or its children | **refused** |
| Anna: delete a ticket of P1 | pass | deny, no Delete grant on `pm:Ticket` at all | **refused** |
| Ben: read any ticket | pass, holds `data.read` | pass, `ticketReader` **on that ticket** (the type-scope rule) and `pm:Ticket` grants Read to it | **allowed** |
| Ben: create a ticket | **deny**, no `data.write` grant | never reached | **refused at L1** |
| Ben: read project P1 | pass | deny, `pm:Project` grants Read only to `projectPartner` | **refused** |
| Cara: edit a ticket of P2 | pass | pass, via the P2 scope | **allowed** |
| Cara: read a ticket of P1 | pass | pass, as a ticket reader | **allowed** |
| Cara: edit a ticket of P1 | pass | deny, a reader is not a writer, no scope on P1 | **refused** |
| Ben: read a ticket's `body` | pass | deny, the `<part>` grant names `projectPartner` only | **the attribute is absent, the ticket still renders** |
| Dora: edit any project | pass, holds `data.write` on root | deny everywhere, she scopes nothing | **refused, silently** |

Three things this table teaches:

**The instinct that a global `data.write` is dangerous is right about the mechanism and wrong about the outcome.** Anna holds write tools globally. She cannot write P2. L1 was never going to be what stopped her, and it did not have to be.

**One role name did both jobs.** `projectPartner` appears in both grant tables and is assigned by two rules. Nothing was duplicated, and no "agent role" was invented alongside a "model role".

**Dora is the failure mode to design against.** She has write tools and nothing to write. Every attempt is refused, and under the engine's read semantics a refused read returns empty rather than an error, so an agent acting for Dora may report "there are no projects" rather than "you may not see them". This is the drift the tier/scope split makes possible: two assignments that can disagree. `AiToolAccessService` warns at boot about a role granted `data.read` or `data.write` that appears in no Read, Write or Create grant of `<security-config>`.

## The idea in two minutes

1. A role is a relationship between a group and an object, not a permission.
2. Agent tool access asks whether you hold the role **on the security root**. That is a deployment-wide question, because a tool call has no object yet when the gate runs.
3. Model access asks whether you hold the role **on the object being touched**. That is where every per-type and per-object decision is made.
4. So one role, assigned twice - once on the root for the tier, once on business objects by a rule for the scope - expresses "may use write tools, on these things".
5. Do not make the security root a security parent of everything, or step 4 collapses.
6. A role on the root grants **no data access whatsoever**. Even "all tickets" needs a rule that puts the role on every ticket. Forgetting it produces an agent whose tools all work and all return nothing.

## The test bed

The example is the agent security test bed of `tl-ai-demo`, behind one line of its `metaConf.txt`: the model is `WEB-INF/model/pm.model.xml`, everything else is `WEB-INF/conf/secdemo.config.xml`, and `tl-ai-demo/doc/secdemo/testbed.md` describes how to drive it. Every row of the walkthrough is a JUnit test that runs against a real application boot, so this article and the test bed cannot drift apart:

| Claim | Asserted by |
|---|---|
| the rules produce the matrix, and the tier does not leak into the scope | `SecDemoRoleRuleTest` |
| every row of the persona walkthrough | `SecDemoModelSecurityTest` |
| the L1 half of each row, and that L1 is blind to the scope | `SecDemoToolAccessTest` |
| the data tools disclose only what a persona may read | `SecDemoReadFilterTest` |

The test bed differs from this article in two deliberate points: it declares no `default-group`, because it is an aspect over an application that already has its own account defaults; and it contributes its role rules *additively* to the access manager the application already runs - `StorageAccessManager` from `tl-element` - rather than naming `ElementAccessManager` as the class, which would replace the security storage the application maintains.
