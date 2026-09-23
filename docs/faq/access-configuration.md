# FAQ: Model based access configuration

How TopLogic decides whether a user may read, write, create or delete an object of a model type,
where that is configured, and how to check that the configuration is complete.

## The access check in one sentence

A user may perform an operation on an object if the user **holds** a role on that object that is
**granted** the operation on the object's type. Two independent definitions feed this check:

| Question | Answered by | Configured in |
|---|---|---|
| Which roles is the operation *granted* on the **type**? | grants (access rights) | `SecurityConfigurationService` |
| Which roles does the user *hold* on the **object**? | role sources: role rules, security parent rules, direct role assignments | `AccessManager` (`ElementAccessManager`) |

Both must be satisfied. A security parent inherits **roles**, never grants: a type with a security
parent rule but no grant is unreadable for every user except a technical admin, however many roles
the user inherits from the container.

The check itself is `SecurityConfigurationService.isAllowed(Person, TLObject, BoundCommandGroup)`:

1. Bypasses: code in a system context (`ThreadContext.inSystemContext`), a system command group,
   and the technical admin (`ThreadContext.isAdmin()`) are allowed; a restricted user is denied
   operations outside the restricted set. An object that is not yet committed is not checked (its
   roles are computed at commit time).
2. A type marked `without-security` is not checked at all.
3. The roles granted the operation on the object's type are looked up (for a module singleton, the
   singleton's own grants take precedence).
4. `AccessManager.hasRole(person, object, roles)` decides whether the user holds one of them on the
   object.
5. For an attribute value there is a second step: if the attribute has its own `<part>` grants for
   the operation, the user must also hold one of those roles; without attribute-level grants the
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

## Role sources: which roles a user holds on an object

Roles on an object come from three sources, all collected by `ElementAccessManager.getRoles`:

1. **Direct assignments** — a `BoundedRole` assigned to a person or group on a specific object
   (`getLocalAndGlobalAndGroupRoles`). This is data, not a definition: it says nothing about the
   next object that is created.
2. **Role rules** — computed roles, configured in `<role-rules>` of the `AccessManager`.
3. **Security parent rules** — the object inherits every role the user holds on its security
   parents, configured in `<security-parents>` of the `AccessManager`. Parents are collected
   transitively, so a ticket comment reaches the project through the ticket.

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
				<!-- A comment inherits the roles of the ticket containing it: the rule navigates
				     the composition Ticket#comments backwards. -->
				<rule id="Comment_securityParent" inherit="true" meta-element="demo.tickets:Comment">
					<step attribute="demo.tickets:Ticket#comments" inverse="true"/>
				</rule>
			</rules>
		</security-parents>
	</instance>
</config>
```

- `meta-element` is the type the rule applies to, `inherit="true"` extends it to specializations.
- A rule's `<step attribute="…" inverse="true|false"/>` elements form the navigation path from the
  object to the role holder (role rule) or to the security parent.
- A role rule of type `inheritance` (`source-role`, `source-meta-element`) derives the role from a
  role the user holds on the object the path leads to; a rule of type `reference` derives it from
  a relation to a group.

### The global default security parent

`BoundHelper` config `use-default-security-parent="true"` makes the security root the parent of
every object without a rule of its own, so such objects inherit the roles held on the root. The
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
| no role source | neither a role rule nor a security parent rule applies, so no user can hold a role and every access is denied (direct assignments do not count as a definition) | security parent rule, role rule, or mark internal |
| no read grant | no role is granted `Read` on the type (after module and inherited grants), so its objects are inaccessible to every user | grant on the type or its module, or mark internal |
| dead grant | an operation is granted to roles that no rule can deliver on the type or its security parents; the grant never takes effect | add a rule delivering the role, or grant a delivered role |
| suggested parent | the type has no role source but is contained in exactly one composition; a security parent rule navigating it backwards is proposed | accept the proposal |
| ambiguous parent | the type has no role source and is contained in several compositions | choose the container in a security parent rule |

A type is **covered** when it has a role source and at least one role may read it, **exempt** when
it is internal or without security, **incomplete** otherwise.

- At startup the findings are logged at INFO (`log-findings="false"` switches the log off, not the
  analysis).
- In the React admin hub (Administration → Access control → Coverage) the same analysis is a table
  with a findings detail. Its commands edit the definition: accept a proposal, or pick several in
  "Proposals…", edit security parent and role rules, edit the grants and marks of a type or its
  module, mark a type internal or exclude it from access control. Every edit is written to the
  `autoconf` files; "Apply configuration" reads the files again and restarts the access services,
  and only then does the table show the change.
- An application makes the check part of its test suite with `test-security-coverage="true"` in
  the `ElementTestCollector` global test configuration; `TestSecurityCoverage` then fails on every
  incomplete type.
