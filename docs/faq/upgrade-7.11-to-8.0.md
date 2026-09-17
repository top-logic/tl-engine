# FAQ: Upgrading an application from TopLogic 7.11 to 8.0

This is the runbook for a **purely technical upgrade** of an existing application from
TL 7.11.0 to TL 8.0.0: the application keeps its classic layout UI and must build and run on the
8.0 engine with its existing database. Adopting the React view layer (`tl-layout-view`), the
model-based search UI or any other new 8.0 feature is out of scope.

**Sources.** The guide was derived from two independent inputs and cross-checked:

- the engine diff `TL_7.11.0..master` (4277 commits, 419 tickets; state of 2026-09-17), and
- the Trac tickets on milestone `TL_8.0.0` that carry the `RequiresMigration` keyword and their
  `== Migration ==` sections.

Several breaking changes have **no** migration section in Trac (see the appendix), so do not rely
on the release changelog alone. The 7.11 maintenance branch `TL/TL_7.11_x` has no commits beyond
the `TL_7.11.0` tag, so 7.11.0 is the exact base of every 7.11 application.

**Legend.** *Mandatory* = the app does not build or start otherwise. *Conditional* = only if the
app uses the feature (each item names a `grep` to find out). *Optional* = recommended, not required.

**Work in this order.** The phases build on each other: a Latin-1 resource bundle is corrupted by
the very first 8.0 build, and the security configuration is what makes the app usable for
non-administrators again after the first start.

---

## Phase 0: Before you touch the code

1. **Back up the production database.** The first 8.0 start runs about 30 automatic schema and data
   migrations (list in phase 8). Rehearse the upgrade on a copy of production, not on a fresh DB.
2. **Know your database vendor.** PostgreSQL and Oracle need DB-level preparation (phase 8.2)
   *before* the first start; H2, MySQL and MS SQL Server do not.
3. **Inventory the app.** Run these from the app root; every hit is an item in the phases below.

   ```bash
   grep -rl --include=*.xml 'securityDomain\|domain-mapper\|role-assignments\|<roles>' src
   grep -rn --include=*.xml 'association=\|meta-object=\|structureRoot@\|securityObject="model"' src
   grep -rn --include=*.xml 'securityObject="\(slave\|rootForCreate\|securityStrRoot\)"' src
   grep -rn --include=*.xml 'shortcutIcon\|icon="' src/main/webapp/WEB-INF/layouts
   grep -rn --include=*.xml 'TLPersonManager\|ElementGroupManager\|excludeUIDs\|onlyOneSession\|LoginMessagesMainLayout\|contact.layout.CountryResourceProvider\|disable-optimizations' src
   grep -rn --include=*.xml 'tl.legacy.tabletypes\|tl.tables:' src
   grep -rln 'login.jsp\|/servlet/login\|LoginPageServlet\|changePwd.jsp' src
   grep -rn 'getSecurityParent()\|legacyKey\|ConfiguredRuntimeModule\|RuntimeModuleProxy\|MultiRuntimeModule\|getGlobalRoles\|HAS_ROLE_ASSOCIATION\|StoredFlexWrapper\|LegacyFlexWrapper\|ExternalRoleProvider\|SecurityStorageCommitObserver' --include=*.java src
   grep -rn 'QueryExecutor.compile\|new ApplicationModelBinding\|new TransientModelBinding\|supportsListElement\|ModeSelector\|getUpperBound()\|getLength()' --include=*.java src
   grep -rn 'tl_developerMode' . --include=*.launch --include=*.sh --include=*.config --include=Dockerfile* --include=*.yml --include=*.yaml
   ```

---

## Phase 1: Build and project setup

### 1.1 Parent POM version (mandatory)

Raise `<parent><version>` to `8.0.0` (or the current `8.0.0-SNAPSHOT` while master is not released).
Java 17, Jakarta Servlet 6.1 and Jetty 12.1 were already in 7.11; only patch/minor bumps of third
party libraries happen (Jetty 12.1.10 to 12.1.13, H2 2.4 to 2.5, httpclient5 5.2 to 5.6, BouncyCastle
1.86, log4j 2.26.1, Jackson 2.22.2). No new JVM flags are needed.

Related POM hygiene:

- `tl-maven-plugin` is now managed in `tl-parent-all` `<pluginManagement>`. Remove a pinned
  `<version>7.11.0</version>` on that plugin from the app POM (typical for the `generate-binding`
  execution), otherwise the stale 7.11 plugin runs against 8.0 sources.
- `bcutil-jdk18on` / `bcpkix-jdk18on` are now managed; drop your own version if you pinned one.
- If the app declares an `.factorypath` with a hard-coded `tl-build-processor-7.x.y` jar, bump it.
- New projects declare the `tl-releases` / `tl-snapshots` repositories in the POM (see
  `tl-archetype-app`); copy them if your build relied on a `settings.xml` for that (#27889).
- No Maven module was removed or renamed between 7.11 and 8.0, so no `artifactId` changes.
- No Node/npm is needed: the React modules are optional and not transitive from `tl-core`/`tl-element`.

### 1.2 Source and resource encoding ISO-8859-1 to UTF-8 (mandatory, do it before the first build)

Ticket #29286. The inherited `project.build.sourceEncoding` is now `UTF-8`, `javac` decodes the app's
`.java` files as UTF-8, and **resource bundles are read and written as UTF-8** at runtime
(`ResourcesModule`) and at build time (`ResourceFile` in the inherited resource-check/normalize goals,
`MessagesGenerator`). `\uXXXX` escapes are still read but no longer written.

Consequence: a still-Latin-1 `*.properties` file shows `U+FFFD` instead of umlauts at runtime, and the
first 8.0 build that rewrites a bundle **persists the replacement bytes `EF BF BD`**, losing the
original characters. Convert first:

```bash
git commit -a            # the goal writes in place
# (parent version bumped, see 1.1)
mvn tl:migrate-to-utf8 -DdryRun=true   # preview; inspect the "already UTF-8" list for old mojibake
mvn tl:migrate-to-utf8                 # apply
mvn install
```

The goal (`MigrateToUtf8Mojo`, prefix `tl` resolves through the inherited plugin declaration; fallback
`mvn com.top-logic:tl-maven-plugin:8.0.0:migrate-to-utf8`) re-encodes genuine ISO-8859-1 `.java` and
`.properties` files (pure ASCII and valid UTF-8 are skipped), flips an explicit `sourceEncoding` in the
app POMs, and rewrites `.settings/org.eclipse.core.resources.prefs` (project/source-folder defaults plus
one `encoding//<path>=UTF-8` line per `.properties` file, because Eclipse's Properties content type is
Latin-1 by default). Parameters: `-DbaseDir`, `-Dextensions=java,properties[,xml,...]`,
`-DskipSources`, `-DskipPoms`, `-DskipEclipse`.

Check afterwards: `grep -rc $'\xef\xbf\xbd' src --include=*.properties` must report 0 everywhere, and
`file` must report UTF-8. Close and reopen `.properties` editors in Eclipse.

### 1.3 `I18NConstants`: `legacyKey()` becomes `@CustomKey` (conditional)

Ticket #29111. The JavaDoc-based generation of `messages_en.properties` ignores `legacyKey(...)`
initialisers, so the `@en` text lands under the normative key and the custom key is never found.

```java
// 7.11
public static ResKey MY_KEY = legacyKey("my.custom.key");
// 8.0 (field must stay uninitialised; annotation in com.top_logic.basic.i18n)
@CustomKey("my.custom.key")
public static ResKey MY_KEY;
```

`legacyKey*()` still compiles, so this is silent. Optional: `MigrateResourcesToJavaDoc`
(`com.top_logic.layout.tools.cleanup`) moves `*_en.properties` texts into `@en` JavaDoc.

---

## Phase 2: Developer and runtime environment

### 2.1 `tl_developerMode` is dead, use `tl_operation_mode` (mandatory for every dev setup)

Ticket #29400 (no migration section in Trac). `Environment.isDeployed()` is now `true` unless the
system property or environment variable `tl_operation_mode=development` is set. In 7.11 an IDE run
from a classes directory was automatically "not deployed"; on 8.0 it runs as a production deployment
(no modular resource path, deployed theme/JS handling, IDE-only commands hidden).

Replace `-Dtl_developerMode=true` by `-Dtl_operation_mode=development` in every Eclipse `.launch`,
`.mvn/jvm.config`, start script, Dockerfile and CI job. Values: `development`, `test`, `production`
(default). Production deployments need nothing, `ENV tl_operation_mode=production` is documentation.

### 2.2 Log receiver (optional)

Ticket #29383. The Chainsaw socket appender in the default log config is now gated by
`-Dtl_logReceiver=true` instead of developer mode. A copied log4j2 config keyed on `tl_developerMode`
should switch its `propertyName`.

### 2.3 Local developer overlay (optional)

The archetype now generates `.mvn/jvm.config` with `-Dtl_config=local.conf.xml`, a git-ignored
`local.conf.xml` and a `local.conf.xml.template` for local secrets (#29385). `tl_config` already
existed in 7.11, so this is a convention you may adopt.

### 2.4 H2 developer databases

H2 moved from 2.4 to 2.5. Recreating a local `tmp/` H2 database is the safe path.

---

## Phase 3: Security rewrite (mandatory for every application)

This is the largest block (#29221, #28710, #29088, #29447, #29564, #29563). Two things changed at
once: the **classic role machinery** lost security structures, domains and module-scoped roles, and a
**new model-based access control** is active and deny-by-default. Do the whole phase before the first
start; most of it fails at startup with a typed-configuration error otherwise.

### 3.1 Roles are global: move them to `InitialRolesManager`

`TLRoleDefinitions` (the `<roles>` annotation on a `<module>` in `*.model.xml`) and `<roles>` under
`ModelService` settings are deleted; an app model with such an annotation no longer loads. Declare the
roles in a service config instead. Keep the old `Module.role` spelling: the name is the identifier that
matches the existing rows in the `BoundedRole` table.

```xml
<config service-class="com.top_logic.base.services.InitialRolesManager">
	<instance>
		<roles>
			<role name="myModule.editor"/>
		</roles>
	</instance>
</config>
```

- The core pre-defines the roles `navigation` and `selection`; per-module copies
  (`myModule.navigation`) can be dropped in favour of them.
- The core also defines the role `PersonalProfile` (granted to every user on their own account).
  Rename an application role of that name.
- Data: the migration `Ticket_29221_Disconnect_roles_from_modules` drops the `definesRole` table and
  strips `<roles>` from the *stored* model baseline. The app's own XML must be edited by hand.

### 3.2 Role assignments on singletons become role rules

`<role-assignments>` inside `<singleton>` (model XML or `ModelService` config) and
`ElementGroupManager` are gone. Replace each `<role-assignment role="R" group="G"/>` on
`Module#ROOT` by a rule in the `AccessManager` config and drop
`class="com.top_logic.element.util.model.ElementGroupManager"` from the `InitialGroupManager` config:

```xml
<config service-class="com.top_logic.tool.boundsec.manager.AccessManager">
	<instance>
		<role-rules>
			<rules>
				<singleton-rule id="Administrator" role="myModule.admin" target="myModule#ROOT">
					<group-with-name name="myAdmins"/>
				</singleton-rule>
			</rules>
		</role-rules>
	</instance>
</config>
```

### 3.3 Role rule syntax (`InitialRoleRules.config.xml` and friends)

| 7.11 | 8.0 |
|---|---|
| `<rule meta-object="StoredQuery" ...>` | `<rule meta-element="tl.search:StoredQuery" ...>` (model type, never a table) |
| `<step attribute="myAttr" meta-element="Mod:Type"/>` | `<step attribute="Mod:Type#myAttr"/>` |
| `<step association="hasStructureChild"/>` | `<step attribute="Mod:Type#children"/>` (navigate the model attribute stored in that table; raw association navigation is gone) |
| `<inheritance-rule base="structureRoot@Mod" ...>` | drop `base`, add a first path step `<script-step expr="`Mod#ROOT`"/>` |
| `source-meta-object="..."` | `source-meta-element="Mod:Type"` |
| rule without `id` | `id` is mandatory (added by the tool in 3.6) |

Programmatic extension points `ExternalRoleProvider` and `SecurityStorageCommitObserver`
(`<role-provider>`, `<commit-observer>` on `ElementAccessManager`) and `FallbackAccessManager` are
removed without replacement: express the assignment as a configured rule.

### 3.4 Remove security structures, domains and the legacy admin views

- Delete the block every engine app carried:
  `<config config:interface="com.top_logic.base.security.SecurityConfiguration"><layout><domain-mapper .../></layout></config>`
  (`StructureDomainMapper` is deleted; unknown property = startup error).
- `AccessManager` `<structures>` is gone; `securityDomain="..."` is an unknown attribute on every
  component and template (`tab`, `tile*`, `securityLayout`, ...). The tool in 3.6 strips it.
- `securityObject` provider aliases `slave`, `rootForCreate`, `securityStrRoot` are deleted. Use
  `securityRoot`, `modelOrSecRoot`, `path:...` (`PathSecurityObjectProvider`),
  `<security-object-by-expression function="..."/>` or `ModuleSingletonSecurityProvider`.
- Removed layouts you may reference: `com.top_logic.element/admin/security/securityStructure.xml`,
  `.../groupRoles/adminGroupRolesView.layout.xml`, `.../roleAssignment/adminPersonsRolesView.layout.xml`,
  `.../indexLegacy.layout.xml`, `.../profilesLegacy/*`, template `templates/securityAdmin.template.xml`.
  `admin/security/index.layout.xml` now uses `com.top_logic/legacyTabbar.template.xml`; the profile
  editor is `com.top_logic/admin/security/profiles/adminSecurity.layout.xml`.
- Removed command handlers you may reference or grant: `applyCheckerRoles`, `expandRoleProfileTree`,
  `collapseRoleProfileTree`, `setAllRoleProfileTree`, `unSetAllRoleProfileTree`,
  `savePersonOrGroupRoles`, `applyPersonOrGroupRoles`, `securityExport`.
- Narrow `securityObject="model"` to `securityObject="model(Mod:Type1,Mod:Type2)"` where the possible
  types are known; otherwise the profile editor offers every role/command-group cell (optional).
- The defaults for "which component is the security master of a dialog" and "is a dialog button bar
  shown" changed (#29221): verify dialogs and mark the security master explicitly.

### 3.5 Model-based access rights: ship a `<security-config>` (mandatory)

Ticket #29088. `SecurityConfigurationService` is started by the core and is **deny-by-default**: a
type without a grant for a command group is inaccessible to every role-based user. Administrators and
code in a system context bypass the checks, so the app *looks* fine for `root` and is broken for
everyone else: TL-Script `get()` returns the empty value, `set/add/remove/new/delete/copy` throw, and
TL-Script model builders (`ListModelByExpression`, `TreeModelByExpression`, `ModelProviderByExpression`,
`SelectionUpdaterByExpression`, `TableContentProvider`, BPE instantiation) filter their result.

Add a config file (registered in the app's `metaConf.txt`):

```xml
<config service-class="com.top_logic.model.security.SecurityConfigurationService">
	<instance>
		<security-config>
			<module name="myapp">
				<grant operation="Read"   roles="Viewer, Editor, Manager" inherit="true"/>
				<grant operation="Write"  roles="Editor, Manager"         inherit="true"/>
				<grant operation="Create" roles="Editor, Manager"         inherit="true"/>
				<grant operation="Delete" roles="Manager"                 inherit="true"/>
			</module>
			<class name="myapp:Order">
				<grant operation="Approve" roles="Manager, Approver"/>
			</class>
			<part name="myapp:Customer#salary">      <!-- attribute grants restrict -->
				<grant operation="Read" roles="Manager"/>
			</part>
			<class name="myapp:TechnicalLog" without-security="true"/>
		</security-config>
	</instance>
</config>
```

Rules: `operation` is a `BoundCommandGroup`; module/type grants are additive (union), `<part>` grants
restrict, `inherit="true"` propagates to subtypes; roles must exist (3.1), an unknown role is a
startup error. Since #29564 grants are an ordered list: use `<revoke operation=... roles=.../>` to
remove a framework role and `config:override="true"` on `<class>` to replace the whole list. Starting
point for the values: the existing role profile (`security.xml`) of the views showing each type.
Full example: `com.top_logic.demo/.../conf/com.top_logic.model.security.SecurityConfigurationService.config.xml`;
concept: `specs/model-based-access-rights.md`.

Not checked: derived attributes, constraints, system context, administrators. `all()` and
`kbQuery()` return unfiltered results (`filterSecurity()` if needed). **A script executed without a
logged-in user has no rights**: background jobs must run in `ThreadContext.inSystemContext(...)` or use
an executor with security disabled (phase 6.2).

### 3.6 Security parents are configured, not coded

`AbstractBoundWrapper.getSecurityParent()` is `final` and throws. Either override
`Collection<? extends BoundObject> getSecurityParents()` or, preferred, configure the parent:

```xml
<config service-class="com.top_logic.tool.boundsec.manager.AccessManager">
	<instance>
		<security-parents>
			<rule id="ProjectForMilestone" inherit="true" meta-element="myapp:Milestone">
				<step attribute="myapp:Milestone#project"/>
			</rule>
			<rule id="SecRootForProject" inherit="true" meta-element="myapp:Project">
				<singleton module="SecurityStructure"/>
			</rule>
		</security-parents>
	</instance>
</config>
```

Behaviour change: once parents are configured for a type, the global security root is **no longer
added automatically**; name it explicitly (`<singleton module="SecurityStructure"/>`) or roles granted
on the root stop being inherited. `<security-parents>` rules accept only `id`, `meta-element`,
`inherit` and the path (#29447); `role`, `source-role`, `type`, `resource-key` are rejected.

### 3.7 Run the migration tool, then normalize

```bash
mvn exec:java@migrate-ticket29221     # inherited from tl-parent-all; needs src/main/webapp/WEB-INF/layouts
mvn exec:java@normalize-layouts
```

Step 1 strips `securityDomain` from every layout under `${tl.layoutDir}` (and pretty-prints them,
hence the normalize afterwards); step 2 adds a UUID `id` to every `<rule>`, `<inheritance-rule>`,
`<singleton-rule>` and security-parent rule under `${tl.confDir}` (default `src/main/webapp/WEB-INF`)
that lacks one. It does **not** rewrite `association=`, `meta-object=` or `base=` (3.3). Layouts
stored in the database are migrated automatically (`Ticket_29221_Remove_security_domain`).

### 3.8 Role profiles and global roles

- Re-export or update the role profiles (`WEB-INF/conf/security.xml`) after the first start: removed
  views and commands (3.4) drop out, new command groups appear. A `security.xml` import that names an
  unknown role/view/command group is recorded as applied and not retried (#29562); change the file
  (new hash) to get it re-imported.
- The `hasGlobalRole` table is **dropped without data transfer** (`Ticket_28710_Update_application_types`)
  and `Person.getGlobalRoles/addGlobalRole/removeGlobalRole` are gone. 7.11 had no UI for global roles,
  so only apps that called `addGlobalRole` in code are affected: re-express those as group membership
  plus a role rule.
- `Group#setDefaultGroup(boolean)` / `Group.GROUP_DEFAULT` are removed (#29563); the default group is
  only the one configured as `default-group` on `InitialGroupManager` (automatic migration). The
  #29447 migration that removes the anonymous account from the default group needs that config.

---

## Phase 4: Accounts, login and session

### 4.1 Login page replaced by an in-app dialog (conditional)

Ticket #29092 (no migration section in Trac). `LoginPageServlet` (`/servlet/login`), `login.jsp`,
`login.banner.inc`, `doOnload.inc`, `jsp/main/loginError.jsp`, `jsp/util/administration/changePwd.jsp`
and `script/tl/loginError.js` are deleted. An unauthenticated request now gets an **anonymous
session** and the layout offers `LoginViewDialog` through `<login-hooks>` on `MainLayout$GlobalConfig`.

- Delete app overlays of those JSPs/includes and `web.xml` mappings of `LoginPageServlet`; redirect
  bookmarks and monitoring probes from `/login.jsp` or `/servlet/login` to `/servlet/LayoutServlet`.
- Remove `login`, `loginRetryPage`, `loginErrorPage`, `changePassword` from an `ApplicationPages$Config`
  override (unknown property = startup error).
- Custom login Java code: `Login.login(user, req, resp)` becomes `checkUserPassword(user, char[], req, resp)`
  followed by `SessionService.getInstance().loginUser(req, resp, person)`; `login(req, resp, credentials)`
  becomes `checkLoginCredentials(credentials, req, resp)` plus `loginUser`; `MaxUsersExceededException`
  becomes `LoginHookFailedException`; `LoginCredentials` is no longer `AutoCloseable` (`clearPassword()`
  in a `finally`).
- `ExternalAuthenticationServlet` (SSO) now extends `NoContextServlet`: rename `forwardPage` to
  `forwardToPage`, `forwardToStartPage` to `redirectToStartPage`, drop `checkRequest`/`forwardToTarget`
  overrides. Subclasses that only implement `retrieveLoginCredentials` need no change.
- Login messages addon: `LoginMessagesMainLayout` became `LoginMessagesHook`; drop a `MAIN_LAYOUT_CLASS`
  theme setting pointing at it and configure
  `<login-hooks><login-hook class="com.top_logic.addons.loginmessages.layout.LoginMessagesHook" showLoginMessages="true"/></login-hooks>`
  (the addon's own config already does).
- Expect an `anonymous` account in `Person.all()`; guard commands with the `AnonymousAccountDisabled`
  executability rule where needed.
- Resource keys: `tl.logout` is now `class.com.top_logic.layout.component.configuration.I18NConstants.LOGOUT`,
  `layouts.admin.persons.changePassword.*` moved to
  `class.com.top_logic.knowledge.gui.layout.person.I18NConstants.CHANGE_PASSWORD_FORM.*`.

### 4.2 Multi-factor authentication config (conditional)

`mfa-requirement` is `@Mandatory` on `DBAuthenticationAccessDevice` and `LDAPAuthenticationAccessDevice`
configs. The core sets `optional` on `dbSecurity` and `%LDAP_MFA_REQUIREMENT%` in `ldapConf`; an app
that declares its own `<security-device>` of these classes (or copied `ldapConf.config.xml`) must add
`mfa-requirement="optional|required|disabled"`. A custom `AuthenticationDevice` implementation must
implement `getMFARequirement()`. `Person.create(kb, name, deviceId)` became `Person.create(kb, name, device)`.

### 4.3 `PersonManager` and account names (conditional, log review mandatory)

- `TLPersonManager` is deleted: configure `com.top_logic.knowledge.wrap.person.PersonManager`
  (`ContactPersonManager` extends it directly now; its `Config` lost the generic parameter).
- Ticket #29423: `Person.byName()` trims and resolves **case-insensitively**; `Person.create()` throws
  `TopLogicException` for empty, pattern-violating, too long (`person-name-max-length`, default 128) or
  case-insensitively existing names. Check every programmatic account creation (imports, provisioning,
  own auth devices); the LDAP sync skips such accounts with an error log.
- Default `person-name-pattern` changed from `[-_a-zA-Z0-9@\.]+` to `[-_\p{L}\p{N}.@+~]+`. Apps with
  logins like `DOMAIN\user`, names with spaces or colons **must** widen the pattern in their
  `PersonManager` config; apps with an own narrower override should drop it.
- The migration `Ticket_29423_resolve_person_name_collisions` renames accounts that differ only by case
  (`User` and `user` become `User` and `user1`; alive and directory-managed accounts win). **Read the
  migration log** after the first start: renamed users must log in with the new name; a renamed LDAP
  account is re-created by the next sync, so transfer its roles/memberships or delete the surplus
  account. A remaining collision makes the app refuse to start. App migrations that create accounts
  must depend on this version.
- `tl.accounts:Person#admin` is not writable through model access any more; `Person#timezone` is
  mandatory (automatic migration, code setting it to `null` fails validation).

### 4.4 `SessionService` / `UserMonitor` (conditional)

Ticket #29106. `SessionService` properties `onlyOneSession` and `excludeUIDs` are removed;
`UserMonitor#excludeIDs` moved into a listener:

```xml
<config service-class="com.top_logic.base.accesscontrol.SessionService">
	<instance>
		<listeners>
			<listener name="storeEvent">
				<impl excludeUIDs="..."/>
			</listener>
		</listeners>
	</instance>
</config>
```

---

## Phase 5: Configuration, layouts and themes

### 5.1 Other config renames (conditional)

| 7.11 | 8.0 |
|---|---|
| `com.top_logic.contact.layout.CountryResourceProvider` | `com.top_logic.util.CountryResourceProvider` (registered in core; #29525) |
| `ModelBasedSearch` config `disable-optimizations="..."` | removed (unknown property) |
| `@Format(TLModelPartRefsFormat.class)` on `List<TLModelPartRef>` properties | `@Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class)` |
| config properties typed `Orientation` with values `HORIZONTAL`/`VERTICAL` | external names are now `horizontal`/`vertical` |
| own `<modules>` list (replacing, not extending, the framework's) | add `OperationModeService`, `InitialRolesManager`, `LoginFailuresModule`, `SecurityConfigurationService` |
| own `metaConf.txt` replacing the framework's | add `languageFlags.config.xml` (core), `personalProfileSecurity.config.xml` (element), `personalProfileContactSecurity.config.xml` (contact) |

### 5.2 Layouts (conditional)

- `masterFrame.template.xml` / `MainLayout.Config` lost `icon` and `shortcutIcon` (#29457): an own master
  frame setting them **fails at startup**. The favicon is the theme variable `DEFAULT_ICON` (default
  `/images/favicon.png`). JSP tag `<basic:favicon>` has no `shortcutIcon` attribute; Java
  `FaviconTag.write(out, shortcutIcon, icon)` became `writeIcon(out, icon)`.
- `supportsElement` was removed from the model-search table/grid templates (#29080); the instance-of
  check on the configured `types` is implicit now. A `ListModelByExpression` without `supportsElement`
  returns `ElementUpdate.UNKNOWN`, which rebuilds the table on changes from other sessions; review
  in-app tables that filtered by script.
- New optional template parameters: `defaultSelectionProvider`, `revealSelection`, `custom-config-key`,
  `pageTitle` (`MainLayout` `<page-title>`, #29262).
- Layout normalization rules are unchanged; re-normalize after the tool in 3.7.

### 5.3 Themes and logos (conditional)

Ticket #29457: without action the app **silently** shows the TopLogic logo.

| 7.11 | 8.0 |
|---|---|
| `src/main/webapp/images/TL_App_icon_blue.svg` (collapsed sidebar) | `src/main/webapp/images/appLogoMinimized.svg` (or `themes/<id>/images/...`) |
| `theme-settings.xml` `IMAGES_APP_LOGO_MINIMIZED=/images/TL_App_icon_blue.svg` | new name, or drop the entry |
| `/images/appLogoSmall.svg` | `/images/appLogoMinimized.svg` |
| `/themes/core/images/TL_Logo_blue.svg` | `/images/appLogo.svg` |
| `/themes/core/images/TL-Logo-without-text.svg` | `/images/TL-Logo-without-text.svg` |
| `/images/shortcut.ico` | `/images/favicon.png` |
| Font Awesome stylesheet `/style/fontawesome/css/all.css` | `/style/fontawesome.css` (#29108; only for themes not extending `core` or JSPs linking it) |

No theme was removed or renamed (`core`, `coreCondensed`, `coreSidebar`, `coreSidebarCondensed`).
Optional (#29528): give dark themes `color-scheme="dark"` and mark the theme that should answer a dark
OS preference `system-default="true"`; `UIThemeService.getActiveThemeId()/setActiveThemeId()` became
`getSelectedThemeId()/setSelectedThemeId()`.

### 5.4 Resource keys an app may override

Only 65 keys were really removed, all belonging to the deleted security views and login pages
(`admin.group.roles.*`, `admin.person.roles.*`, `admin.security.import.roleRules.problem.*`,
`tl.command.setAllRoleProfileTree*`, `role.name.orgStructure.{navigation,selection}`, `tl.logout`,
`layouts.admin.persons.changePassword.*`, `MAX_USERS_EXCEEDED`). Drop overrides of these.
The ~2000 other removed lines in the diff are the UTF-8 re-encoding of `_de` bundles.

---

## Phase 6: Java code

### 6.1 Compile errors (removed or changed API)

| 7.11 | 8.0 replacement | Ticket |
|---|---|---|
| `AbstractBoundWrapper#getSecurityParent()` override | `getSecurityParents()` or configured `<security-parents>` (3.6) | #29088 |
| `RuntimeModule`, `ConfiguredRuntimeModule`, `MultiRuntimeModule`, `RuntimeModuleProxy`, `ServiceManager`; `ManagedClass(Properties)` / `(IterableConfiguration)` ctors | `ConfiguredManagedClass<Config>` + `TypedRuntimeModule` + `@ServiceDependencies`; the properties `<section>` becomes a typed `<config service-class=...>` | #29088 |
| `DataManager()`, `FileSystemCache()`, `FlexDataManagerFactory()`, `InitialDataSetupService()`, `CommandApprovalService()` no-arg/`Properties` ctors | `(InstantiationContext, Config)` | #29088 |
| `LegacyFlexWrapper`, `StoredFlexWrapper` | `com.top_logic.element.meta.kbbased.AttributedWrapper`; `MapBasedPersistancySupport.getObjects(KnowledgeItem)` / `setObjects(Collection, KnowledgeItem)` | #28710 |
| `BoundedRole.HAS_ROLE_ASSOCIATION`, `DEFINES_ROLE_ASSOCIATION`, `getScope()`, `bind()`, `getAllGlobalRoles()`, `getDefinedRoles(TLModule)` | `hasRole` is a `KnowledgeObject` of type `tl.accounts:RoleAssignment`: `BoundedRole.getAll()`, `getRoleByName()`, `roleAssignmentsForContext(...)` (close the iterator) | #28710, #29221 |
| `Person.getGlobalRoles/addGlobalRole/removeGlobalRole` | `BoundedRole.assignRole(context, person, role)` / `getLocalAndGlobalRoles(context, person)` | #28710 |
| `AccessManager#handleSecurityUpdate(kb, changed, created, removed, handler)`, `LogHandler#logSecurityUpdate(...)` | `handleSecurityUpdate(TLObjectChangeSet, CommandHandler)` (silent if the override lacked `@Override`) | #28710 |
| `RoleRule` ctors, `PathElement` class, `PathElementConfig#getAssociation()`, `ElementAccessHelper#getAvailableRoles(TLClass, ...)`, `BoundHelper#getPossibleRoles(TLModule)`, `ElementSingletonManager.getSingleton("structureRoot@M")` | `DefaultRoleRule`/`SingletonRule`, `PathNavigation`; `BoundedRole.getAll()` + `AccessManager#canHaveRole`; `TLModelUtil.findSingleton(module)` / `ElementBoundHelper.getSecurityRoot()` | #29221 |
| `ModelSecurityObjectProvider.INSTANCE`, `FirstSlaveSecurityObjectProvider`, `FlexibleSecurityObjectProvider`, `RootForCreate...`, `SecurityStructureRoot...` | `PathSecurityObjectProvider.MODEL_INSTANCE`, `ModelOrSecurityRootSecurityObjectProvider`, configured providers (3.4) | #29221 |
| `BoundChecker#getRolesForCommandGroup` returning `null` | must return a non-null set (NPE otherwise) | #29088 |
| `ListModelBuilder#supportsListElement` returning `boolean` | returns `ElementUpdate` (`ADD`, `REMOVE`, `NO_CHANGE`, `UNKNOWN`); return `UNKNOWN` when undecidable; `SelectableBuilderComponent#receiveModelCreatedEvent` became `handleTLObjectCreations` + `getTypesToObserve` | #29080 |
| `ModeSelector#getMode(TLObject, TLStructuredTypePart)` | `+ boolean editMode`; `traceDependencies(..., FormContext)` takes `OverlayLookup` | #29396, #29108 |
| `AttributeOperations.getUpperBound()` == `-1`, `getLength()` returning `-1` | `Integer.MAX_VALUE` resp. `null` (`Integer`); `StringLengthConstraint.NO_LIMIT` | #29465 |
| `new ApplicationModelBinding(kb, model)`, `TransientModelBinding(model)` | trailing `boolean usesSecurity` (`false` for external imports) | #29447 |
| `QueryExecutor` subclass overriding `executeWith(EvalContext, Args)` / `disableSecurity()` | both `final`; override `internalExecuteWith` / `internalDisableSecurity` | #29449 |
| `GenerateSequenceId.SEQUENCE_SUFFIX`, `ResetSequence.SEQUENCE_SUFFIX` | `SequenceIdGenerator.SEQUENCE_SUFFIX` / `sequenceName(base, context)` | #29384 |
| `FormMember#setLabel(null)` / `setTooltip(null)` | ambiguous with new `ResKey` overloads: cast `(String) null` or pass the `ResKey` | #29092 |
| `WrapperResourceProvider.getLabel`, `AbstractTLItemResourceProvider.getDefaultLabel/getTooltip` (final) | pulled up into `DefaultResourceProvider.getLabel(Object)`; `getDefaultLabel` is `super.getLabel(object)` | #28388 |
| `InternationalizedDescription#getDescription()` returning `ResKey` | `HtmlResKey` (descriptions are rich text) | #28694 |
| `Base32` | `EncodeTypable` (same static method names) | #29092 |
| `com.top_logic.basic.graph.ExplicitGraph` | `com.top_logic.basic.shared.graph.ExplicitGraph` | #29108 |
| `com.top_logic.bpe.modeler.upload.Updater` | `com.top_logic.bpe.util.Updater` | #29037 |
| `TLTreeNode` / `WindowScope` implementations | new abstract `getDepth()` resp. `getPageTitle()/setPageTitle()` | #29102, #29262 |
| `SQLH.createLIKE/quote/createInsert/...` static helpers | `DBHelper` / `SQLFactory` | #29425 |
| `Util.updateTLReference(...)` etc. in migration processors | additional `DeletionPolicy` parameter (`null` keeps) | #29361 |
| `MetaElementUtil.getAllInstancesOf(TLClass)` | `getAllInstancesOf(TLClass, Class<T>)` / `AttributeOperations.allDirectInstances` | #29092 |
| `ListStorage.listConfig(...)`, `SetStorage.setConfig(...)`, `SingletonLinkStorage.singletonLinkConfig(...)` | trailing `boolean unversioned` (`false`) | #29092 |
| `DiffVisitor` implementations | `CreateRole`/`DeleteRole` cases gone; `UpdateDeletionPolicy`/`UpdateHistoryType` added | #29221, #29361 |

Deprecations are almost absent in the range (only `Environment.DEVELOPER_MODE` and `isJarFile()`):
breaking changes were made by removal, so compile the app (including tests, see
[build-conformance.md](build-conformance.md): `-DskipTests=true` skips test compilation).

### 6.2 Silent runtime changes (no compile error)

- **`QueryExecutor` filters results by the current user's read rights** (#29449). Every executor used
  for internal computation must call `disableSecurity()` after `compile(...)`, otherwise objects the
  user may not read are missing. Engine extension points with definer's rights (derived attributes,
  locators, constraints, lock strategies, migration and maintenance scripts, JMS consumers, BPE
  engine) already do; everything user-facing (`ListModelByExpression`, `CommandHandlerByExpression`,
  script console, ...) does not.
- Custom `GenericMethod` implementations that touch the model **bypass** model security unless they
  extend `GenericMethodWithSecurity` / `SearchExpressionWithSecurity` and honour `usesSecurity()`;
  ones that read session state must override `canEvaluateAtCompileTime()` to return `false` (#29555).
- **Abstract attributes** on transient objects throw instead of returning `null` (#29073): a
  `StructuredElement`-based model must redeclare inherited abstract attributes such as `parent` in its
  root type (`MyNode extends StructuredElement { parent: MyContainer }`, containers extend it).
- **Dangling multi-valued references** throw `KnowledgeBaseRuntimeException` instead of being dropped
  (#28816); clean such data before the upgrade.
- A monomorphic table reference targeting an abstract `MOClass` or an `MOAlternative` is rejected at
  schema resolution (#29361): make it polymorphic or point to a concrete type.
- `BasicRuntimeModule#shutDown()` resets the instance before `shutDownImplementation()`: a service
  calling `Module.INSTANCE.getImplementationInstance()` on itself during shutdown sees it inactive.
- `Utils.isEmpty(StructuredText)` counts empty HTML as empty (#29195); `DBType.fromLiteralValue`
  maps `Float`/`Integer` to `FLOAT`/`INT`; `ErrorHandlingHelper` logs only `SYSTEM_FAILURE` as ERROR.
- The session locale is no longer fixed at login (#29525): do not cache resolved `ResKey`s in shared objects.

### 6.3 TL-Script semantics

- `dateFormat()` without an explicit `timeZone` now formats in the **user's** time zone instead of the
  JVM default (#29306); pass `"system"`, a zone id or `$cal.timeZone()` where the old output is needed.
- `label()`, `localize()`, `fill()` are no longer constant-folded, so they localize per reading user (#29555).
- No builtin function was removed or renamed. New: `canRead`, `canWrite`, `canDelete`, `canCreate`,
  `canReadAttribute`, `canWriteAttribute`, `canExecute`, `filterSecurity`, `timeZone`, `changeLog`,
  `revertChanges`, `parseXml`, `gzip`/`gunzip`, `enumName`, `resolveEnum`; check app-registered
  `<method name=...>` for clashes.

---

## Phase 7: Model and schema

- Remove `<roles>` and `<role-assignments>` from every `*.model.xml` (3.1, 3.2).
- **Legacy table types** (#28710): the modules `tl.legacy.tabletypes` and `tl.tables` no longer exist. The
  migration `Ticket_28710_Removed_legacy_types` deletes the *framework's* types only. If your database
  still holds `tl.legacy.tabletypes:<MyTable>Table` or `tl.tables:<MyTable>TableInterface` types (created
  by the TL 6 to 7 upgrade or a `TableInterface`-based model), delete them in an own migration exactly as
  `com.top_logic.demo/.../migration/tl-demo/Ticket_28710_Removed_legacy_types.migration.xml` does, and
  re-type attributes pointing at them (`change-part-type ... target="tl.accounts:Person"`). Also remove
  `IndexedObjectNaming` `<type name="tl.legacy.tabletypes:...">` entries and `defaultFor` references.
- `hasRole` is a `MOKnowledgeObject` now; an own `*Meta.xml` referencing `hasGlobalRole` or `definesRole`
  must drop it.
- Columns that must stay case-sensitive on PostgreSQL/Oracle (technical keys, hashes, Base64) get
  `binary="true"` in the app's `*Meta.xml` (see 8.2).
- New mandatory flags on `tl.model` parts (#29512) and `Person#timezone` are automatic; code creating
  model parts programmatically must supply `name`, `module`, `scope`, `owner`, `index`.
- New system roles `navigation`/`selection` and datatypes `tl.util:EMail`/`tl.util:Phone` are inserted;
  an app already defining roles or `tl.util` types of these names collides.
- A model upgrade at startup (`DynamicModelService.upgradeModel`, default `auto-upgrade`) applies the
  XML edits above to the stored baseline automatically ("Started incremental model upgrade" in the log).
  With `auto-upgrade="PREVENT"` the app refuses to start until you ship a migration.

---

## Phase 8: Database

### 8.1 Automatic framework migrations

They run once, at the first start, in dependency order, and need no declaration by the app. The chain
is complete from the 7.11.0 heads (`tl: Ticket_29019_Removed_log_entries`,
`tl-element: Ticket_29001_Added_revertedBy_attribute`). Human follow-up is needed for two of them
(marked). Module `tl`: `Ticket_29221_Remove_security_domain` (stored layouts),
`Ticket_29423_resolve_person_name_collisions` (**read the log**, 4.3),
`Ticket_29447_anonymous_not_in_default_group`, `Ticket_29492_enlarge_layout_key`. Module `tl-element`:
`Ticket_28710_Removed_legacy_types` (**framework types only**, phase 7), `Ticket_28710_Update_application_types`
(drops `hasGlobalRole`, `hasRole` becomes an object), `Ticket_28710_RoleAssignment_TLSearch`,
`Ticket_29073_added_derived_to_tlassociationend`, `Ticket_29092_added_unversioned_link_tables`,
`Ticket_29092_multi_factor_authentication`, `Ticket_29236_Added_isRevert_isRedo`,
`Ticket_29361_*` (Tag), `Ticket_29384_unify_sequence_names`, `Ticket_29221_Disconnect_roles_from_modules`,
`Ticket_29088_Add_PersistentView`, `Ticket_29512_mandatory_model_attributes`,
`Ticket_29540_mandatory_timezone`, `Ticket_29563_default_group_derived`, `Ticket_29548_create_email_phone`.
Other modules: `tl-model-search` (`Ticket_28710_Added_TLSearch`), `tl-layout-formeditor`
(`Ticket_29092_multi_factor_authentication` **replaces** the `tl.accounts:Person` form definition; an
in-app customization of that form is lost), `tl-model-wysiwyg` (`Ticket_29410_filename_binary`), `tl-bpe`
(`Ticket_28710_Missing_migrations`, `Ticket_29037_Update_diagram_attribute`).

### 8.2 PostgreSQL and Oracle: case-insensitive collation (mandatory on those databases)

Ticket #29425. Non-binary string columns are case-insensitive on every dialect now. H2, MySQL and MS
SQL Server already were; PostgreSQL and Oracle need preparation.

**PostgreSQL**

1. PostgreSQL 12 or newer, built with ICU (checked at startup).
2. The application's DB user needs `CREATE` on the schema: the collation `tl_ci` is created on **every**
   connection-pool initialization (`CREATE COLLATION IF NOT EXISTS "tl_ci" (provider = icu, locale = 'und-u-ks-level2', deterministic = false)`).
   Without the right the application does not start; without the collation every `ORDER BY` fails.
3. **Existing columns are not converted automatically**, and the DDL for new columns already uses
   `tl_ci`. Comparing an old and a new column then fails with SQLSTATE 42P22 ("could not determine which
   collation to use"), so the conversion is part of the upgrade, not optional. Ship an own migration:

   ```xml
   <?xml version="1.0" encoding="utf-8" ?>
   <migration config:interface="com.top_logic.knowledge.service.migration.MigrationConfig"
   	xmlns:config="http://www.top-logic.com/ns/config/6.0">
   	<version name="Ticket_XXXXX_recollate_string_columns" module="my-app"/>
   	<dependencies>
   		<dependency name="<last-own-version>" module="my-app"/>
   	</dependencies>
   	<processors>
   		<processor class="com.top_logic.knowledge.service.migration.processors.RecollateStringColumnsProcessor"/>
   	</processors>
   	<post-processors/>
   </migration>
   ```

   Without `table`/`column` all non-binary string columns of all tables are converted (logical
   meta-schema names for a targeted run). Binary columns are skipped; on other dialects it is a no-op.
4. Before running it, remove case-duplicates under `UNIQUE` indexes, or the index rebuild fails:
   `SELECT lower(NAME), count(*) FROM PERSON GROUP BY lower(NAME) HAVING count(*) > 1;`
5. It is a locking `ALTER TABLE ... ALTER COLUMN ... TYPE` sweep that rewrites tables and indexes
   (temp disk of table size): plan a maintenance window; split large databases table-wise.

**Oracle**: no per-column collation; set `NLS_COMP=LINGUISTIC` and `NLS_SORT=<...>_CI` (e.g.
`GENERIC_M_CI`) at database/instance level (`ALTER SYSTEM`). A startup check logs an error otherwise
and columns stay case-sensitive. **DB2**: the startup check warns if the database collation is not
case-insensitive.

### 8.3 Own migrations

- A new app migration must depend on the current head version of every framework module it builds on;
  generate the template with `com.top_logic.knowledge.service.migration.CreateMigrationScriptTemplate`
  (see the `Create migration template (<module>).launch` files under `bin/launch` of the engine modules)
  rather than copying names. Current 8.0 heads (they may still move before the release):
  `tl: Ticket_29492_enlarge_layout_key`, `tl-element: Ticket_29548_create_email_phone`,
  `tl-contact: Ticket_28557_enlarge_contact_attributes`, `tl-model-search: Ticket_28710_Added_TLSearch`,
  `tl-bpe: Ticket_29037_Update_diagram_attribute`.
- Remove `<post-processor class="...MoveRolesFromSingletonsToModules"/>` from old app migrations (class deleted).
- New processors available: `RecollateStringColumnsProcessor`, `UpdateTLPropertiesProcessor`
  (`<update-tl-properties>`), `ResolvePersonNameCollisionsProcessor`, `AddDatabaseColumnProcessor`,
  `MoveHistoricReferenceToColumn`, `RemoveApplicationTypesProcessor` / `AddApplicationTypesProcessor`.
- BPE (#29037): workflows under `WEB-INF/workflow` are re-imported when their file hash changes; on the
  upgrade the hashes of already loaded workflows are recorded, **not** re-imported. To force a re-import
  add `<update-tl-properties property="initial-workflow.<file>" value="<wf-name>:XXX"/>` to an own migration.

---

## Phase 9: Tests

- Scripted tests: re-record steps that reference removed security views (`Berechtigungsstruktur`,
  per-structure role assignment, legacy roles profile), `GlobalDomain`, or the create-login form field
  label `Login` (now `Benutzername`, #29484); scripts that edit model-part descriptions now carry
  `StructuredTextNamingScheme$Name` values (#28694); date assertions may shift with the `dateFormat()`
  time zone (6.3). Person fixtures with invalid or case-colliding names now throw.
- `TestComment` now accepts `record` (relaxation); layout normalization and the other CI gates are
  unchanged, see [build-conformance.md](build-conformance.md).
- The test container overlays test fragments in memory over `autoconf` (#29414): app test setups that
  swapped `metaConf.txt` should re-check their configuration.

---

## Phase 10: First start and verification

1. Start on the copy of production with `-Dtl_operation_mode=development` in the IDE (2.1).
2. Read the log: account renames (4.3), "Started incremental model upgrade" (phase 7), unresolvable roles
   in `<security-config>` (3.5), the PostgreSQL collation check (8.2), `ERROR` lines from the resource
   check (missing keys in one language only).
3. Log in as an ordinary user, not as `root`: administrators bypass the model access rights, so only a
   role-based user reveals a missing grant (empty tables, failing saves).
4. Re-export or update the role profiles (3.8), then compare against the 7.11 export.
5. Verify a non-admin user can log in through the dialog, change the password, and reach every
   navigation node the old role profile granted.
6. Run the scripted tests and the app's own test suite with tests actually compiled and executed.

---

## Appendix A: Trac coverage of the changes above

Every change in this guide is covered by a ticket flagged `RequiresMigration` with a `== Migration ==`
section in its description: #28388, #28694, #28710, #28816, #29037, #29073, #29080, #29088, #29092,
#29106, #29108, #29111, #29221, #29286, #29306, #29361, #29383, #29384, #29396, #29400, #29420,
#29423, #29425, #29429, #29447, #29449, #29457, #29465, #29484, #29492, #29512, #29525, #29528,
#29540, #29542, #29548, #29555, #29563, #29564.

The review of the diff found eleven of them incomplete; their sections were written on 2026-09-17
and are the source of the corresponding items above: #29092 (login rewrite, MFA config, account
API), #29400 (operation mode), #28710 (data side of the security rework, removed wrapper base
classes), #29088 (removed legacy module system), #29108 (Font Awesome path, moved classes),
#28816 (dangling references), #29361 (schema check, migration API), #29484 (login field label),
#28388 (resource provider API), #29548 (new `tl.util` types), #29555 (`canEvaluateAtCompileTime`).
#29400 still carries milestone `TL_8.1.0` although the change is on master before 8.0.

Flagged but not implemented (status `new`, no instructions): #26916, #27285, #28031, #28487,
#28488. #28026 (`AttributedObject` to `TLObject` in `*Meta.xml`) is flagged but has **not** landed
on master, so it is not part of this upgrade.

## Appendix B: Evidence for the minimal path

The engine's own upgrade fixtures `test-migrate-apps/test-app-7-4-0`, `test-app-7-5-0-M1`,
`test-app-7-9-3` and `test-app-rewrite` build and start on master with only the parent-version bump,
the UTF-8 encoding step and the launch-config edit, because they carry no security configuration of
their own. A real application with roles, role rules and a customized login needs phases 3 to 5.
