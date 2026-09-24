# FAQ: Model based access configuration

How TopLogic decides whether a user may read, write, create or delete an object of a model type,
where that is configured, and how to check that the configuration is complete.

## The access check in one sentence

A user may perform an operation on an object if the user **holds** a role on that object that is
**granted** the operation on the object's type. Two independent definitions feed this check:

| Question | Answered by | Configured in |
|---|---|---|
| Which roles is the operation *granted* on the **type**? | grants (access rights) | `SecurityConfigurationService` |
| Which roles does the user *hold* on the **object**? | role sources: role rules, role parent rules, direct role assignments | `AccessManager` (`ElementAccessManager`) |

Both must be satisfied. A role parent inherits **roles**, never grants: a type with a role parent
rule but no grant is unreadable for every user except a technical admin, however many roles the
user inherits from the container.

A type that needs neither has an **access parent** instead: its objects delegate the *whole*
decision, grants and roles, to another object. A composition part gets its container as access
parent by default, so the rows of a composition table, the comments of a ticket or the attachments
of a comment need no definition of their own. See [Access parent](#access-parent-delegating-the-whole-decision).

The check itself is `SecurityConfigurationService.isAllowed(Person, TLObject, BoundCommandGroup)`:

1. Bypasses: code in a system context (`ThreadContext.inSystemContext`), a system command group,
   and the technical admin (`ThreadContext.isAdmin()`) are allowed; a restricted user is denied
   operations outside the restricted set. An object that is not yet committed is not checked (its
   roles are computed at commit time).
2. The decision for the person, object and operation is answered from the memo of the current
   interaction, if it was made before (see [Caching](#caching-of-decisions)).
3. A type marked `without-security` is not checked at all.
4. A type with an access parent asks the object the relation leads to, with the same operation, or
   with `Write` in place of `Create` and `Delete`. The parent may delegate further. An object whose
   relation leads nowhere (no container, an empty reference) is denied, as is one whose chain of
   access parents runs in a cycle.
5. The roles granted the operation on the object's type are looked up (for a module singleton, the
   singleton's own grants take precedence).
6. `AccessManager.hasRole(person, object, roles)` decides whether the user holds one of them on the
   object.
7. For an attribute value there is a second step: if the attribute has its own `<part>` grants for
   the operation, the user must also hold one of those roles, on the object or, for a delegating
   object, on the end of its chain of access parents; without attribute-level grants the
   class-level decision stands.

## Grants: which roles may do what on a type

Grants live in the `SecurityConfigurationService` configuration (`<security-config>`). Each entry
names a model element and lists rules; the rules of all configuration layers (framework, modules,
application, in-app edits) are **appended**, so an application extends the framework's rights
instead of replacing them.

```xml
<config service-class="com.top_logic.model.security.SecurityConfigurationService">
	<instance>
		<security-config>
			<!-- Grants on one class. -->
			<class name="tl.accounts:Person">
				<grant inherit="true" operation="Read" roles="PersonalProfile"/>
				<grant inherit="true" operation="Write" roles="PersonalProfile"/>
			</class>
			<!-- Baseline for every class of a module. -->
			<module name="tl.demo.projectManagement">
				<grant inherit="true" operation="Read" roles="demo.react.ProjectReader"/>
			</module>
			<!-- Grants on a module singleton, taking precedence over the class grants. -->
			<singleton name="SecurityStructure#ROOT">
				<grant operation="Read" roles="Viewer"/>
			</singleton>
			<!-- Grants on the values of one attribute, checked on top of the class grants. -->
			<part name="tl.accounts:Person#password">
				<grant operation="Read" roles="Administrator"/>
			</part>
			<!-- Marks, see below. -->
			<class name="my.module:Cache" internal="true"/>
			<class name="my.module:PublicNotice" without-security="true"/>
			<!-- Access parents, see below: a composition navigated backwards, a to-one reference
			     navigated forwards. -->
			<class name="demo.tickets:Comment" access-parent="demo.tickets:Ticket#comments"/>
			<class name="demo.tickets:Reminder" access-parent="demo.tickets:Reminder#ticket"/>
		</security-config>
	</instance>
</config>
```

- `operation` is a command group id: `Read`, `Write`, `Create`, `Delete`, `Export`, … (see
  `SimpleBoundCommandGroup`).
- `roles` is a comma separated list of role names (`InitialRolesManager` declares roles).
- `<grant>` adds roles, `<revoke>` takes them away again, in the order the rules appear.
- `inherit="true"` passes the rule on to the specializations of the type. The rights of a type are
  computed as: what its generalizations pass on, plus the module rules of its module, plus its own
  rules.

### The two marks

`<class>` and `<module>` entries carry two boolean marks. They exclude each other: a configuration
setting both on one entry is rejected at startup (constraint `NotBothTrue`), the in-app editor drops
the one mark when the other is set, and the access rights dialog refuses to save both.

- **`without-security="true"`** — the type is not access controlled: every user may access its
  objects and attribute values and may create such objects; only a restricted user stays excluded.
  Specializations inherit the mark; on a module it covers every class of the module.
- **`internal="true"`** — the type is used by the application code alone and never accessed on
  behalf of a user. The access check is *not* changed (a user asking for such an object is denied,
  since no role is granted), but the coverage check does not report the missing definition.
  Specializations inherit the mark; on a module it covers every class of the module.

### Access parent: delegating the whole decision

A `<class>` entry with `access-parent="…"` names the reference leading from an object of the type
to its **access parent**, the object whose access definition decides. The type has no grants, no
marks and no roles of its own (an entry combining them is rejected at startup and by the editor):

- Read, Write and Export on the object are Read, Write and Export on the parent.
- Create and Delete of the object are **Write** on the parent, since a part is created or deleted
  by editing the whole. Creating an object of such a type requires Write on the context it is
  created in.
- The parent may delegate further; the chain is followed at runtime. An object whose relation
  leads nowhere is not accessible, and a chain running in a cycle is denied (and logged as an
  error).
- Attribute-level `<part>` grants on the type are checked against the roles the user holds at the
  end of the chain.
- Specializations inherit the setting.

The reference is either a **composition** holding objects of the type, navigated backwards to the
container (`demo.tickets:Ticket#comments`; the parent is the container only when it holds the
object through that composition), or a **to-one reference of the type**, navigated forwards
(`demo.tickets:Reminder#ticket`). Any other reference cannot lead to a single object and is a
configuration error.

**The default for composition parts.** A type held in a composition that has no role rule, no role
parent rule, no marks and no configured access parent delegates to its container by default,
whichever composition holds the object (`TLObject.tContainer()`). Most helper types of an
application therefore need no configuration at all. Any explicit definition — a role rule or a
role parent rule applying to the type (inherited ones included), one of the marks, or a configured
access parent — switches the default off for that type. The explicit `access-parent` is needed for
a type reached through a to-one reference, or for a composition part that inherits a role rule but
should delegate nevertheless; the coverage check then reports the shadowed rules.

In Java, `ModelAccessRights.getAccessParent(TLClass)` returns the relation in effect as an
`AccessParent` (container, backward or forward reference, explicit or default), and
`AccessManager.hasRoleSource(TLClass)` is the hook the default consults.

### Caching of decisions

Every decision of `isAllowed` is memoized per interaction (request) in an `AccessDecisionCache`
stored on the `InteractionContext`, keyed by person, object and operation. A composition table of
two hundred rows that all delegate to one container costs one real check of the container. The
memo is replaced when the knowledge base has a newer revision than the one it was created for, so
a command that commits and then renders does not reuse decisions from before its change; outside
an interaction a memo serves a single check and its delegations. The memo also detects cycles: a
decision is marked while it is being computed, and a chain of access parents that reaches the mark
again is denied.

## Role sources: which roles a user holds on an object

Roles on an object come from three sources, all collected by `ElementAccessManager.getRoles`:

1. **Direct assignments** — a `BoundedRole` assigned to a person or group on a specific object
   (`getLocalAndGlobalAndGroupRoles`). This is data, not a definition: it says nothing about the
   next object that is created.
2. **Role rules** — computed roles, configured in `<role-rules>` of the `AccessManager`.
3. **Role parent rules** — the object inherits every role the user holds on its role parents,
   configured in `<security-parents>` of the `AccessManager` (the tag keeps its historical name;
   the concept is called *role parent* since it passes on roles, not the access decision — that is
   the access parent above). Parents are collected transitively, so a sub-project reaches the
   project through its parent project. The Java API keeps the historical name as well
   (`BoundObject.getSecurityParents()`).

```xml
<config service-class="com.top_logic.tool.boundsec.manager.AccessManager">
	<instance>
		<role-rules>
			<rules>
				<!-- Every member of the group "users" holds AccountReader on every account. -->
				<rule id="AccountsReadableByUsers" inherit="true"
					meta-element="tl.accounts:Person" role="demo.react.AccountReader">
					<group-with-name name="users"/>
				</rule>
				<!-- Identity rule without a path: the account itself is the role holder, so a
				     user holds PersonalProfile on exactly their own account. -->
				<rule id="PersonalProfileForPerson" inherit="true"
					meta-element="tl.accounts:Person" role="PersonalProfile"/>
			</rules>
		</role-rules>
		<security-parents>
			<rules>
				<!-- A sub-project inherits the roles of the project containing it, on top of the
				     roles its own rules deliver: the rule navigates the composition
				     Project#subProjects backwards. -->
				<rule id="SubProject_roleParent" inherit="true" meta-element="demo.projects:SubProject">
					<step attribute="demo.projects:Project#subProjects" inverse="true"/>
				</rule>
			</rules>
		</security-parents>
	</instance>
</config>
```

- `meta-element` is the type the rule applies to, `inherit="true"` extends it to specializations.
- A rule's `<step attribute="…" inverse="true|false"/>` elements form the navigation path from the
  object to the role holder (role rule) or to the role parent.
- A role parent is the right choice where the type has grants of its own on top of the inherited
  roles (a sub-project with its own roles). Where the type has nothing of its own, an access parent
  is simpler and cheaper.
- A role rule of type `inheritance` (`source-role`, `source-meta-element`) derives the role from a
  role the user holds on the object the path leads to; a rule of type `reference` derives it from
  a relation to a group.

### The global default role parent

`BoundHelper` config `use-default-security-parent="true"` makes the security root the role parent
of every object without a rule of its own, so such objects inherit the roles held on the root. The
coverage check reports this case separately ("no role source, root fallback active") because it
usually means that access is defined by accident rather than by design.

## Where the files are

- Framework and module defaults: `WEB-INF/conf/*.config.xml` of the modules (for example
  `com.top_logic.element/…/personalProfileSecurity.config.xml`).
- Application: its own `WEB-INF/conf/<app>.config.xml` (the React demo keeps both services in
  `demoReactConf.config.xml`).
- In-app edits: `WEB-INF/autoconf/<service class>.config.xml`, written by
  `InAppServiceConfigStore`, one file per service (`…AccessManager.config.xml` for rules,
  `…SecurityConfigurationService.config.xml` for grants and marks). These files layer onto the
  base configuration and are read at startup and by "Apply configuration".

## Checking that the definition is complete: the coverage check

`SecurityCoverageCheck` (module `com.top_logic.element`) analyses every non-abstract class of every
module not listed in its `excluded-modules` and reports, per type:

| Finding | Meaning | Fix |
|---|---|---|
| no role source | neither a role rule nor a role parent rule applies and no composition holds the objects, so no user can hold a role and every access is denied (direct assignments do not count as a definition) | access parent through a to-one reference, role parent rule, role rule, or mark internal |
| no read grant | no role is granted `Read` on the type (after module and inherited grants), so its objects are inaccessible to every user | grant on the type or its module, or mark internal |
| dead grant | an operation is granted to roles that no rule can deliver on the type or its role parents; the grant never takes effect | add a rule delivering the role, or grant a delivered role |
| shadowed rules | the type has a configured access parent, but role rules or role parent rules apply to it; they have no effect | delete the rules, or drop the access parent |

A type is **covered** when it has a role source and at least one role may read it, **delegated**
when it has an access parent (whether the parent is accessible is reported with the parent's type),
**exempt** when it is internal or without security, **incomplete** otherwise.

- At startup the findings are logged at INFO (`log-findings="false"` switches the log off, not the
  analysis).
- In the React admin hub (Administration → Access control → Coverage) the same analysis is a table
  with a findings detail; the "Access parent" column shows what a delegating type delegates to.
  Its commands edit the definition: role parent and role rules, the grants, marks and access parent
  of a type ("Access rights…"), the grants and marks of its module, mark a type internal or exclude
  it from access control. Every edit is written to the
  `autoconf` files; "Apply configuration" reads the files again and restarts the access services,
  and only then does the table show the change.
- An application makes the check part of its test suite with `test-security-coverage="true"` in
  the `ElementTestCollector` global test configuration; `TestSecurityCoverage` then fails on every
  incomplete type.
