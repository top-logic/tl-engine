# Worked example: one role vocabulary, two grant tables

This document exists to make agent security explainable. It builds a small, complete application
from scratch - model, groups, roles, role rules, model rights, agent tool access - and then walks
every persona through every operation, naming which layer decides and why.

Read this before `agent-security-architecture.md`. That document explains the design; this one shows
it running.

## 1. The one idea you need

**A role is not a permission. A role is a relationship: `(group, object, role)`.**

Roles live in one table, the `SecurityStorage`, whose rows say "members of group G hold role R on
object X". Nothing else about security is stored anywhere. Asking "does Anna hold role R?" is not a
well-formed question. Asking "does Anna hold role R **on object X**?" is, and the answer walks X plus
X's security ancestors, never X's children.

On top of that one substrate sit **grant tables**, which say what holding a role permits. TopLogic 8
has three, and they never talk to each other:

| Grant table | Governs | Asks about the role at |
|---|---|---|
| roles profiles | the classic UI | each view |
| `<security-config>` | model access from TL-Script, search, REST, agents | **the object being touched** |
| `<tool-access>` | which agent tools may be called | **the security root** |

The last two are the ones this document is about. Both read the same role vocabulary. The crucial
difference is the last column: they ask about the same role at **different objects**, and that is
what lets a single role name answer two different questions.

- `projectPartner` held **on the security root** means "project partners, as a class of user, may
  use the agent's write tools." A deployment-wide statement. This is a *tier*.
- `projectPartner` held **on project P1** means "you are a partner *of P1*." An object-level
  statement. This is a *scope*.

The same role, assigned in two places by two rules, carries both. **You do not need a separate role
vocabulary for agent access.** You need one role and two assignments.

## 2. The hinge that makes or breaks this

Whether the tier and the scope stay independent depends on a single configuration flag:

```xml
<config service-class="com.top_logic.tool.boundsec.BoundHelper">
  <instance use-default-security-parent="false"/>
</config>
```

`false` is the engine default, and this example keeps it.

Role resolution is downward only. If the security root is made the security parent of every object,
then a role held on the root is held on **every object in the system**. The tier assignment swallows
the scope assignment, and "partner of P1" stops meaning anything, because you are a partner of
everything.

So:

- **Root not a security ancestor of business objects** (this example): a role on the root grants
  tools and nothing else. A role on P1 grants rights on P1 and nothing else. The two assignments are
  independent, and per-object scoping works.
- **Root is a security ancestor**: tier and scope collapse. You can still differentiate per *type*
  through `<security-config>`, but never per instance.

Whenever this design reads as incomprehensible, this flag is usually why. The existing `secdemo`
fixture sets it to `true` on purpose, so it demonstrates tier-based access only and cannot
demonstrate scoping at all.

## 3. The example application

Domain: projects with tickets. Two capabilities we want to express, which between them cover every
mechanism.

1. A project partner may read and edit **the projects they partner on**, and the tickets of those
   projects. Not other projects.
2. A ticket reader may read **all** tickets, and write none.

### 3.1 The model

`WEB-INF/model/pm.model.xml`, module `pm`:

- `pm:Project`
  - `name`: String
  - `partners`: reference to `tl.accounts:Person`, multiple. This attribute *is* the security rule's
    input.
  - `tickets`: composition of `pm:Ticket`, multiple
- `pm:Ticket`
  - `title`: String
  - `body`: String

Note what carries the security: an ordinary business attribute, `partners`. Security is derived from
the data, not maintained beside it.

### 3.2 Roles

Roles are declared centrally in TL 8. There is no per-module role declaration and no
`<role-assignments>` element any more.

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

Two roles. Both will appear in both grant tables.

### 3.3 Groups

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

`default-group="users"` means every account created is added to `users` automatically. Membership in
`projectPartners` and `ticketReaders` is what an administrator maintains.

### 3.4 Role rules: where the two assignments happen

This is the heart of the example. Four rules on the access manager: two tier rules, one scope rule,
one inheritance rule.

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

Three mechanisms, three jobs, worth naming individually because people conflate them:

- **`<singleton-rule>`** puts a role on one well-known object. Used here for the root, which is the
  only place a tier can live. Replaces the TL 7 `<role-assignment>` element.
- **`<rule type="reference">`** derives a role on every matching object by navigating a `<path>` of
  business attributes to the accounts or groups that should hold it. `<step attribute="...">`
  navigates forwards; add `inverse="true"` to navigate backwards. A path may end at a `Group` or at a
  `Person`; a Person is resolved to their representative group automatically.
- **`<security-parents>`** decides what "ancestor" means for role resolution, and therefore what a
  scoped role reaches downward.

Every rule needs a unique `id` in TL 8. That is what the `ticket29221` migration adds to older
configurations.

### 3.5 Model rights: what a role may do with data

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
    </security-config>
  </instance>
</config>
```

Two properties of this table matter more than its contents:

**Deny by default, and it is real.** A type with no grant for an operation resolves to an empty role
set, and a check against an empty role set is false. `pm:Ticket` has no Delete grant, so nobody
deletes tickets by role. You never write a denial.

**This service is active in every TL 8 application whether you configure it or not.** `tl-core`
registers it with an empty `<security-config>`, which denies every operation to every role-based
user. An application that does not write this block has not opted out of model security; it has
denied everything.

Administrators and engine super-users bypass all of it, and so does any code running in a system
context. Keep that in mind when testing: an admin account proves nothing.

### 3.6 Agent tool access: which tools a role may call

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
  </instance>
</config>
```

**`data.write` is a tier and cannot be anything else.** At the moment this gate runs, the model has
proposed `executeTLScript` and there is no object yet to check. So this table answers exactly one
question: "may the agent wield this kind of power on your behalf at all." Every question of the form
"on which object" and "of which type" belongs to `<security-config>` and is answered later, per
object.

That division is not a limitation to work around. It is the only decomposition available, and it is
the reason `<tool-access>` reads roles on the security root: the root is where a deployment-wide
statement about a person lives.

## 4. The personas, and what each one can do

Four accounts. The fourth exists to demonstrate a failure mode, not a capability.

| Persona | Groups | `projectPartner` on | `ticketReader` on |
|---|---|---|---|
| Anna | projectPartners | root (tier), P1 (scope) | - |
| Ben | ticketReaders | - | root (tier) |
| Cara | projectPartners, ticketReaders | root (tier), P2 (scope) | root (tier) |
| Dora | projectPartners | root (tier) only, partner of nothing | - |

P1 and P2 are projects; Anna is in P1's `partners`, Cara is in P2's.

### The walkthrough

Each row names the layer that decides. Note how often the answer is L2, and that L1 passes in almost
every interesting case.

| Persona asks the agent to | L1 (tools) | L2 (data) | Result |
|---|---|---|---|
| Anna: edit project P1 | pass, holds `data.write` on root | pass, holds `projectPartner` on P1 | **allowed** |
| Anna: edit project P2 | pass, same tier | deny, no `projectPartner` on P2 | **refused** |
| Anna: edit a ticket of P1 | pass | pass, P1 is the ticket's security parent | **allowed** |
| Anna: edit a ticket of P2 | pass | deny, no role on P2 or its children | **refused** |
| Anna: delete a ticket of P1 | pass | deny, no Delete grant on `pm:Ticket` at all | **refused** |
| Ben: read any ticket | pass, holds `data.read` | pass, `ticketReader` on root and `pm:Ticket` grants Read to it | **allowed** |
| Ben: create a ticket | **deny**, no `data.write` grant | never reached | **refused at L1** |
| Ben: read project P1 | pass | deny, `pm:Project` grants Read only to `projectPartner` | **refused** |
| Cara: edit a ticket of P2 | pass | pass, via P2 scope | **allowed** |
| Cara: read a ticket of P1 | pass | pass, as a ticket reader | **allowed** |
| Cara: edit a ticket of P1 | pass | deny, reader is not a writer, no scope on P1 | **refused** |
| Dora: edit any project | pass, holds `data.write` on root | deny everywhere, she scopes nothing | **refused, silently** |

Three things this table is meant to teach:

**The user's instinct that a global `data.write` is dangerous is right about the mechanism and wrong
about the outcome.** Anna holds write tools globally. She cannot write P2. L1 was never going to be
what stopped her, and it did not have to be.

**One role name did both jobs.** `projectPartner` appears in both grant tables and is assigned by two
rules. Nothing was duplicated, and no "agent role" was invented alongside a "model role".

**Dora is the failure mode to design against.** She has write tools and nothing to write. Every
attempt is refused, and under the engine's read semantics a refused read returns empty rather than an
error, so an agent acting for Dora may report "there are no projects" rather than "you may not see
them". This is the drift the tier/scope split makes possible: two assignments that can disagree. It
argues for boot-time warnings, not for a different architecture.

## 5. How to explain this to someone in two minutes

1. A role is a relationship between a group and an object, not a permission.
2. Agent tool access asks whether you hold the role **on the security root**. That is a
   deployment-wide question, because a tool call has no object yet when the gate runs.
3. Model access asks whether you hold the role **on the object being touched**. That is where every
   per-type and per-object decision is made.
4. So one role, assigned twice - once on the root for the tier, once on business objects by a rule
   for the scope - expresses "may use write tools, on these things".
5. Do not make the security root a security parent of everything, or step 4 collapses.

## 6. Status of the claims in this document

Verified against the `8.0.0-alpha7` engine sources: the configuration interfaces and tag names used
above (`InitialRolesManager.Config`, `InitialGroupManager.Config`, `RoleRulesConfig`,
`SingletonRuleConfig`, `RoleRuleConfig`, `PathElementConfig`, `SecurityParentsConfig`,
`GroupWithName`); `RoleProvider.Type` having exactly the constants `reference` and `inheritance`;
`GroupMapper` resolving a `Person` to their representative group; rule `id` being mandatory;
deny-by-default in `SecurityConfigurationService` and the empty-role-set check in
`AccessManager.hasRole`; `SecurityConfigurationService` being registered active by default in
`tl-core`'s web fragment; and `use-default-security-parent` defaulting to false.

Not yet exercised on a running application: the XML above is assembled from those interfaces rather
than copied from a booted configuration. The one element worth confirming on first boot is the
nesting of `<group-with-name>` inside `<path>` within `<singleton-rule>`, since the ticket #29221
description shows it without the wrapper.
