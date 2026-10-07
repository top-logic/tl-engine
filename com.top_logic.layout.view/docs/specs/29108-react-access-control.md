# React UI Access Control (Ticket #29108)

**Status:** Phases 1 & 2 complete (browser-verified; see §3 and §4). Phase 3 in
progress — minimal admin area done (see §4); role assignment & per-object security outstanding.
**Scope:** Per-view access control for the new React view layer
(`com.top_logic.layout.view`, served by `ViewServlet` at `/view/*`), parallel to
— and independent of — the legacy `LayoutComponent`/`BoundChecker` security.

---

## 1. Big picture

The legacy UI wires access checks into *every* `LayoutComponent`
(`hideReason()` → `BoundChecker.allowShowModel` → `allow(user, securityObject,
commandGroup)`), with `CompoundSecurityLayout` woven into the tree to mark
sub-trees with a security id at a sensible granularity and to hold the
`role → command-group` mapping (`PersBoundComp`).

The React view layer is a different beast: a declarative `UIElement` tree parsed
from `.view.xml`, with no `LayoutComponent`/`BoundChecker`/model-as-security-object
plumbing. The goal of Phase 1 is the visibility half of the legacy model: make
**borders of access** explicit and remove the UI parts a user may not see.

### Guiding principles

1. **Only *removable* units carry access control.** Hiding one of two
   side-by-side panels makes no sense; the thing that disappears must be a
   navigable/removable container — a **nav-item, tab, or tile**. Plain content
   (forms, tables, split panes) never carries a scope; what it shows and offers
   follows the model access rights of the objects it works on (§7).
2. **Define scopes centrally; reference by id.** A *security scope* (`id` +
   `label`) is defined once in a service catalog and referenced from the view by
   id only. Several units sharing one scope reference the same id — no duplicated
   labels, single source of truth for the admin UI. (Earlier drafts that defined
   the scope *inline at the use site* were rejected: they cannot be reused and
   risk divergent labels for the same id.)
3. **No inheritance, no re-checks.** A unit with an `<access-control>` is an
   independent gate; a unit without one is always shown. If the parent decided
   "visible", re-checking the same scope on a child is pointless — so there is no
   scope threading through `ViewContext` and no cascading evaluation. (Phase 2
   relaxes this for *command executability* only: a command rule may default to
   the enclosing unit's scope, which does thread the scope down — see §4.)
4. **Reuse the legacy enforcement core, don't reimplement roles.** A scope is a
   `BoundChecker`; the decision goes through the same `BoundChecker.allow(...)`,
   `PersBoundComp`, and `AccessManager` as the legacy UI, so the React UI agrees
   with legacy semantics and any server-side double-check.
5. **Removal must be backed server-side.** Client-side hiding is not security;
   a denied unit must also be unreachable by deep-link. This falls out of
   omission (see §2) rather than needing a separate guard.

The "domain/grouping" idea from the design discussion did **not** become a
runtime concept; it survives only as the *tree structure of the catalog* (purely
organizational for the admin UI). Enforcement is flat: `scope id → roles`.

---

## 2. Architecture / key decisions

- **Scope catalog = a configured service.** `SecurityScopeService`
  (`KBBasedManagedClass`) holds a tree of `ScopeConfig` nodes (`id`, `label`,
  nested `scopes`). A node with an `id` is a referenceable `SecurityScope`;
  nesting provides grouping labels. Catalog is **static typed configuration**
  (developer-authored, like layouts); only the `role` assignments are persistent
  (admin-editable). Duplicate ids are flagged at startup; unknown references fail
  closed at the use site.
- **`SecurityScope` is a `BoundChecker`** (`extends AbstractBoundChecker`) keyed
  by its id (`ComponentName`). `isVisible()` = `BoundChecker.allowShowModel(this,
  null)` → READ against the **security root**
  (`SecurityRootObjectProvider.getSecurityRoot()`), roles resolved from the
  `PersBoundComp` for the id via `AccessManager`. Security-object is the root for
  now (structure-level "may this user see this part of the app"); per-object
  roles are a later extension (see §4) and need no call-site change.
- **`<access-control scope="id"/>` is a config property, not a child element.**
  A reusable `AccessControl` config item, exposed via the `WithAccessControl`
  mixin (`@Name("access-control")`). Removable element configs extend the mixin,
  so the same declaration and the same enforcement code apply uniformly. Authored
  as a property (named after the property) avoids colliding with the typed child
  lists of `<sidebar>`/`<dashboard>` (which forbid a foreign `<security-domain>`
  wrapper) — this was the key constraint that killed the wrapper-element design.
- **Enforcement is omission at build time.** `AccessChecks.isAccessible(...)`:
  `null` → granted; resolve scope, deny (and log) on unknown id, else
  `scope.isVisible()`. Each removable element, when building its control,
  **skips** the unit entirely when denied:
  - `nav-item`: `SidebarElement.NavItemElement.createSidebarItem` returns `null`;
    the build loop drops nulls.
  - `tab`: `TabBarElement.createControl` `continue`s past denied tabs.
  - `tile`: `DashboardElement.createControl` skips `!tile.isAccessible()`.
- **Deep-link safety falls out of omission.** Routes are collected from the
  *built* (already-filtered) item list (`ReactSidebarControl.declaredRoutes()` /
  `collectRoutes()`), and a denied unit's lazy content factory is never created.
  So a denied unit has no route and no content — omission *is* the server-side
  guard. (Caveat: evaluated when the control tree is built — per session / on
  view reload — matching legacy build-time evaluation.)
- **Boot materialization.** `SecurityScopeService.startUp()` creates a
  `PersBoundComp` per configured scope id (idempotent), so the **existing**
  security administration can assign roles to view scopes exactly as for legacy
  components. Startup runs inside an enclosing transaction, so a transaction is
  opened only when scopes are actually missing and is then committed, never
  rolled back (a nested rollback would abort the enclosing transaction).
- **Secure by default.** A gated scope with *no* roles assigned denies everyone
  except the technical super user (`ThreadContext.isAdmin()` — true for admin
  persons such as `root`). Protecting a unit is an explicit opt-in to denial.

### Module placement

- **`com.top_logic.layout.view`, new package `…view.security`:**
  `SecurityScopeService` (+ `Config`/`ScopeConfig`/`Module`), `SecurityScope`,
  `AccessControl`, `WithAccessControl`, `AccessChecks`, `I18NConstants`.
  Service enabled in `WEB-INF/conf/tl-layout-view.conf.config.xml`.
- **`com.top_logic.layout.view` element wiring:** `SidebarElement` (`nav-item`),
  `TabBarElement` (`tab`), `TileElement` + `DashboardElement` (`tile`) extend the
  mixin and enforce.
- **Reused from `tl-core`:** `BoundChecker`, `AbstractBoundChecker`,
  `PersBoundComp`, `SecurityComponentCache`, `AccessManager`,
  `SecurityRootObjectProvider`, `BoundHelper`, `ComponentName`.

---

## 3. Current state — Phase 1 (DONE, browser-verified)

Visibility-level access control for removable React-UI units, end to end.

**Commits (branch `CWS/CWS_29108_react_ui_security_concept`, on top of the
login work `9c954e8a3`):**
- `588f68c92` — access control for `nav-item`/`tab`/`tile` + `SecurityScopeService`
  catalog + `AccessControl`/`WithAccessControl`/`AccessChecks`/`SecurityScope`.
- `cf2a5b77c` — materialize `PersBoundComp`s for scopes at boot.
- `d8fda1f63` — demo wiring: `demo-restricted` scope + gated **Settings** nav-item.
- `50948cb18` — fix: open the boot transaction only when scopes are missing
  (avoid rolling back the enclosing transaction on idempotent re-boot).

**Demo:** a `demo-restricted` scope in
`com.top_logic.demo/.../conf/technicalDemoConf.config.xml`; the **Settings**
nav-item in `app.view.xml` carries `<access-control scope="demo-restricted"/>`.

**Verified (Playwright, real browser):**
- Anonymous user → app bar "anonymous" + Login; sidebar ends at the separator,
  **Settings absent** (deny path; no roles + non-admin).
- Login as `root`/`root1234` → app bar "root" + Logout; **Settings appears**
  (show path; admin person bypasses the empty-role scope).

---

## 4. Remaining phases

### Phase 2 — command-level access control (DONE, browser-verified)

**As built** (matches the plan below; one addition surfaced during verification):
- `SecurityRule` (`…view.security`, `@TagName("security")`, implements
  `ViewExecutabilityRule` + the new `ContextDependentRule` bind hook). Config:
  optional `scope`, mandatory `group`, optional `security-object` channel.
- `SecurityScope.allowCommand(group, object)` + `getCommandGroups()` returns the
  default group plus the catalog-declared `<group>`s; `ScopeConfig.getGroups()`.
- `ViewContext.getSecurityScope()` / `withSecurityScope(...)`, threaded by
  nav-item/tab/tile content builds — **and across `<view-ref>`**
  (`ReferenceElement`): the referenced view inherits the host unit's scope, just
  like it already inherits the error sink and command scope. This was a real bug
  found in the browser — without it a `<view-ref>`ed command's scope resolved to
  `null` and the command was (correctly, but surprisingly) hidden even for root.
- All four command hosts (`CommandCarrierElement`, `FormElement`,
  `ButtonElement`, `AppBarElement`) now build rules through one shared
  `ViewExecutabilityRules.build(configs, context)`, which binds context-dependent
  rules uniformly. `ViewCommand.Config.getGroup()` removed.

**Commits:** `a35044e6` (scope command groups) · `a581b204` (SecurityRule +
threading + shared builder + drop `group`) · `1a2817c1` (`<view-ref>` scope
propagation) · `650d40cc` (demo wiring).

**Verified (Playwright, real browser):**
- root → Settings → the `Write`-gated **"Save Settings"** button renders and is
  enabled (scope resolved from the enclosing `demo-restricted` nav-item; admin
  bypass); clicking it reaches the server (`DemoCommand` logged
  `Demo command executed`), confirming the **server-side dispatch re-check**
  passes via the same rule.
- Anonymous → no Settings nav-item at all (Phase 1 visibility deny intact — no
  regression).

**Deny path — not yet shown live.** Observing a *role-based* command deny needs a
logged-in **non-admin** who can reach a gated command: root is a technical admin
and bypasses every command group, the only access-controlled area
(`demo-restricted`) is admin-only at the visibility level, the demo seeds no
non-admin account, and there is no role-assignment surface yet (that is Phase 3).
The deny itself goes through the **same** `BoundChecker.allow()` (empty roles →
deny) that Phase 1 already verified live for visibility — only the command group
differs. A full live deny demo (create a non-admin account + a publicly visible
`Write`-gated button) is a follow-up.

---

### Phase 2 — original plan
Visibility (Phase 1) is the READ half — "may this user see this part of the
app". The other half is command **executability**: "may this user run *this
action* here", the analog of legacy command groups. Phase 2 expresses this as a
`ViewExecutabilityRule`, so it rides the existing command machinery end to end.

#### Key decisions

1. **Express the check as a rule, not as a `group` shortcut (option 2).**
   `ViewCommand.Config.getGroup()` is dropped (it is currently unread). Security
   is configured by adding an explicit security rule to a command's
   `<executability>` list. A bare `group=` shortcut that desugars to such a rule
   can be added later; nothing forces it now. Rationale: a `group` alone is not
   enforceable (it is only the *verb* — it still needs a role mapping and a
   security object), and an explicit rule keeps the gate self-contained and
   consistent with Phase 1's explicit-opt-in model.
2. **The server-side re-check is automatic — no new dispatch guard.**
   `ReactButtonControl` wires `_action = model::executeCommand`
   (`ReactButtonControl.java`), and `ViewCommandModel.executeCommand` already
   re-evaluates `_rule.isExecutable(input)` and bails before
   `ViewCommand.execute` (`ViewCommandModel.java`). So any check expressed as a
   `ViewExecutabilityRule` is enforced server-side at dispatch for free. (Verify
   the scripted/named-command invocation path also routes through
   `executeCommand`; if so it is covered too.)
3. **Two things are resolved, independently, on the rule:**
   - **Scope** — *which* `PersBoundComp` holds the role→command-group mapping (a
     Phase 1 `SecurityScope`, named by catalog id). The rule's `scope=` is
     **optional**: explicit id wins; else the **nearest enclosing removable
     unit's scope** (threaded through `ViewContext` — the principle-3 relaxation,
     for commands only); else (guarded command with neither) it is a config
     error → **fail closed + log**, matching Phase 1's unknown-scope handling.
     Reusing the enclosing unit's scope yields the legacy "one `PersBoundComp`,
     many command groups" model without legacy plumbing, and keeps the common
     case un-noisy.
   - **Security object** — the `BoundObject` the check runs against. The rule's
     `security-object=` is an optional `ChannelRef`; **no channel ⇒ security
     root** (structure-level, as Phase 1). A channel value enables per-object
     checks for free.
4. **Command groups are declared on the scope (for now).** `ScopeConfig` gains a
   `groups` list; `SecurityScope.getCommandGroups()` returns the default
   (visibility) group plus the declared ones, and boot materialization creates
   the `PersBoundComp` with all of them so the admin UI can assign roles per
   (scope, group). A rule may only reference a declared group. This is the light
   first step; it is replaceable later by a boot-scan of the view tree that
   collects `(scope, group)` pairs automatically (mirroring legacy
   `CompoundSecurityLayoutCommandGroupCollector`) if maintaining the catalog list
   by hand proves annoying.

#### Work items

- **`SecurityRule` (new, `…view.security`, implements `ViewExecutabilityRule`).**
  Config: `scope` (optional catalog id), `group` (`CommandGroupReference`),
  `security-object` (optional `ChannelRef`). Resolved at control-build time
  against the `ViewContext`: capture the resolved `SecurityScope` (explicit or
  enclosing) and the resolved security-object `ViewChannel`.
  `isExecutable(input)` calls `scope.allow(group, object)` with `object` =
  channel value (fresh at eval) or root; returns `NOT_EXEC_HIDDEN` (or disabled)
  on deny. Fail closed on an unresolved scope.
- **`SecurityScope`**: add `allow(BoundCommandGroup, BoundObject)` next to
  `isVisible()`; have `getCommandGroups()` return default + declared groups.
- **`ScopeConfig` + `SecurityScopeService`**: add the `groups` property; include
  the declared groups in boot `PersBoundComp` materialization.
- **`ViewContext` scope threading**: expose the current enclosing
  `SecurityScope`; removable elements (`SidebarElement` nav-item, `TabBarElement`
  tab, `TileElement`/`DashboardElement` tile) that carry an `<access-control>`
  set it (try/finally) around building their content subtree, so command rules
  built underneath can default to it. Honours the lazy-content build timing.
- **`CommandCarrierElement.buildExecutabilityRule`**: thread the `ViewContext` in
  so security rules can resolve their scope/channel (like input channels are
  resolved today).
- **Remove `ViewCommand.Config.getGroup()`/`GROUP`**; regenerate `messages_*`.
- **Demo + Playwright verify**: add a `write`-gated command on the existing
  `demo-restricted` scope (declare the `write` group on the scope). Confirm:
  `root` sees + executes; a role-less non-admin sees it hidden/disabled and a
  forced server dispatch is rejected.

### Phase 3 — per-object security & admin UX

**In progress — minimal admin area (DONE, browser-verified).** A reusable
access-control admin area, the first piece of the "admin surface" below:
- **Reusable views** in `com.top_logic.layout.view` (`WEB-INF/views/admin/`):
  `admin.view.xml` (Administration → "Access control" → tabs Accounts / Groups /
  Roles) plus per-entity master-detail views and create dialogs. Composed from
  existing elements (`<table>` + selection channel, `<form>`/`<field>`,
  `<split-panel>`, `<generic-command>` with `execute-script`/`with-transaction`/
  `write-channel`/`open-dialog`/`close-dialog`); Delete uses `$obj.delete()` and
  `<null-input-disabled/>` (no confirmation yet — `ViewCommandModel` confirmation
  is still a TODO).
- **Groups & Roles** create purely in XML (`new(transient)` → edit → `copy(false)`).
  **Accounts** need Java — an account requires an authentication device, so a new
  `CreateAccountAction` (`<create-account>`, package `…view.admin`) calls
  `Person.create(kb, login, defaultDevice)` from a transient `tl.admin:NewAccount`
  form and, when a `password` is given, `device.setPassword(account, …)` so the
  account is **immediately usable for login** (password policy is enforced by the
  device and surfaces as a form error). Person `firstName`/`lastName` are *derived*
  (not settable) — the account form edits `name`/`language`/`restrictedUser`.
- **Wiring/demo:** an `administration` scope (admin-only) gates a new
  "Administration" nav-item in the demo's `app.view.xml`.
- **Verified (Playwright):** nav + gating; account create (login + password) /
  edit / delete; role create / delete; deep-link routing to tabs; Delete disabled
  with no selection. **Created a `tester` account, logged in as it, and confirmed
  the sidebar omits Settings + Administration** — the first live non-admin
  visibility-deny check (the admin-only scopes correctly deny a real non-admin).
- **Commits:** `c773ebbf` (admin area), `a152a60f` (demo wiring), `cf0e6b72`
  (initial password → usable accounts).

**Role assignment to scopes (DONE, browser-verified).** A permission matrix
(`SecurityMatrixElement`, package `…view.admin`) on a new **Permissions** tab:
rows = (scope, command group) from the `SecurityScopeService` catalog, columns =
all `tl.accounts:Role`s, each cell a checkbox that grants/revokes that role's
command group on that scope via `SecurityScope.setGranted(group, role, bool)`
(which wraps `PersBoundComp.addAccess`/`removeAccess` in its own transaction —
the value-listener has no ambient transaction; a buffered Save/Apply with one
transaction is a possible future alternative). Notable:
- The matrix is an app-specific widget, so it is referenced by `class=`, not a
  global `@TagName`. View content lists resolve by tag name, so a no-`@TagName`
  element is placed via the `children` property's entry tag — now made explicit:
  `ContainerElement.Config.getChildren()` is `@EntryTag("child")`, used as
  `<child class="…SecurityMatrixElement"/>`.
- Cell checkboxes use the **new-style** `AbstractFieldModel` (a concrete
  `FieldModel`) with a `FieldModelListener`, **not** the legacy
  `FormContext`/`FormField`/`FormFieldAdapter` (those are legacy-compat only). A
  plain `AbstractFieldModel` also skips the control's label resource lookup, so
  there is no stray label resource to define.
- **Verified (Playwright, root):** matrix renders (demo-restricted/Read,
  demo-restricted/Write, administration/Read × all roles); toggling a checkbox
  persists (still checked after a full reload that re-reads the `PersBoundComp`).
- **Commit:** `c1099a82`.

**Role → user is model configuration, not a UI feature** (correcting an earlier
note). Which roles a user holds on an object comes from:
- **Group membership** + a `<role-assignment role="…" group="…"/>` on a structure
  root singleton — e.g. `DemoConf.config.xml` assigns `SecurityStructure.OwnerRole`
  to group `securityOwner` on `SecurityStructure:Root`; and
- **`AccessManager` role-rules** (`InitialRoleRules.config.xml`) that derive roles
  along object paths (owner, responsible, …).

The React `SecurityScope` checks against the same security root
(`SecurityRootObjectProvider` → `BoundHelper.getDefaultObject()`), so a role
granted to a group there is exactly what a gated scope sees. The deferred live
**command-deny** demo therefore needs no new code: put `tester` in a group that
holds a role on the root, grant that role the `Write` group on `demo-restricted`
in the permission matrix → `tester` passes; remove it → denied.

Remaining Phase 3 work:
- **Wire + record the live command-deny demo** via the group/role config above.
- **Per-object roles.** Today the security object is always the root
  (structure-level). Allow a scope reference to bind a model channel
  (`<access-control scope="…" security-object="{chan}"/>`) so roles are checked
  on a concrete object — the new analog of `SecurityObjectProvider`, since "the
  model" now flows via channels.
- **Admin surface.** Confirm the materialized `PersBoundComp`s show up usefully
  in the existing security administration (they are keyed by scope id /
  `ComponentName`), or build a view-scope-aware admin surface driven by the
  catalog tree (this is what the `label`/grouping in `ScopeConfig` is for).
- **Command-group aggregation.** Mirror `CompoundSecurityLayoutCommandGroupCollector`:
  collect the `getGroup()`s present under a scope so the admin UI offers exactly
  the relevant groups (only meaningful once Phase 2 lands).

---

## 5. Known follow-ups / cleanups

- **Re-evaluation granularity.** Access is decided when the control tree is
  built. A mid-session role change is reflected only on view reload (matches
  legacy build-time `hideReason`). Revisit if live re-evaluation is wanted.
- **Demo scope location.** The `demo-restricted` scope lives in the demo config;
  it exists purely to exercise the feature. Real catalogs live per app.
- **Catalog persistence.** Catalog is static config by design; if runtime-editable
  scopes are ever needed, that is a separate (migration-bearing) decision.
- **Logout / login reload occasionally lost (FIXED — `aa5b516f`).** The React
  `LogoutCommand`/`LoginAction` defer the session swap to a
  `window.location.reload()` enqueued on the SSE queue. `SSEUpdateQueue.flush()`
  polled each event off the queue *before* writing it, so a write to a
  dead/half-open connection dropped the event for good (reconnect only replays
  control state via `sendFullState`, not one-off events) — the reload never fired
  and logout/login was left half-applied (needed a fresh browser context / app
  restart). Fixed by removing an event only after a successful write (retry on the
  next reconnect) and synchronizing `flush()`. Verified a full
  login → logout → login cycle in one browser context.

---

## 6. Key references

- Legacy model (mirrored): `tool.boundsec.BoundChecker` (`allow`,
  `allowShowModel`, `hideReasonForSecurity`), `compound.CompoundSecurityLayout`,
  `wrap.PersBoundComp`, `wrap.SecurityComponentCache`, `manager.AccessManager`,
  `securityObjectProvider.SecurityRootObjectProvider`,
  `simple.SimpleBoundCommandGroup`, `util.LayoutBasedSecurity` (boot
  materialization pattern).
- This feature: `com.top_logic.layout.view.security.*`; wiring in
  `element/{SidebarElement,TabBarElement,TileElement,DashboardElement}`.
- Model access rights: `specs/model-based-access-rights.md` (repository root),
  `com.top_logic.model.security.ModelAccessRights`,
  `SecurityConfigurationService`.
- View layer composition: `ViewServlet`, `UIElement`/`ViewContext`,
  `element/*Element`, `command/*`; see also `docs/specs/29108-react-login.md`.

---

## 7. Model access rights in views (Tickets #29675, #29692)

Scopes (§1–§4) gate *navigable units* by view-level command groups. The objects
a view shows, edits, creates and deletes are governed by the **model access
rights**: grants per type, attribute, module and singleton in the
`SecurityConfigurationService`, queried through `ModelAccessRights`
(`specs/model-based-access-rights.md`). This section maps those rights onto the
view layer.

### Principles

1. **The model decides, the view follows.** The view layer grants nothing of its
   own and defines no command groups for model data. A view operation *is* a
   model operation on an object, a type or an attribute (Read, Write, Create,
   Delete, or a custom command group such as `Approve`). The model operations
   enforce the rights; the UI only avoids offering what they would refuse.
2. **Enforcement lives in the model operations.** TL-Script `set`/`add`/`new`/
   `copy`/`delete` check their right and fail with a message naming the
   operation. Every write path of the view layer that bypasses TL-Script — form
   save (`TLObjectOverlay.apply`), table cell save — checks Write per changed
   attribute. A non-transient `copy()` of a transient object is a *creation* and
   reports a refusal as one. Option lists of form fields are read-filtered like
   table rows and every other TL-Script result.
3. **Derive where the element knows object and attribute** (#29692). A form field
   or table column hides a value the user may not read and is read-only for an
   attribute the user may not write; a form offers editing only with Write on its
   object (Edit command, `initial-edit-mode` and edit channel alike). No
   configuration in the view. The attributes of a draft (an object to be created)
   are decided by `ModelAccessRights#isAllowedInitial`: for a type with an access
   parent, the part grants are checked on the roles at the end of the draft's
   access-parent chain (through drafts up to the first committed object); attribute
   grants of a self-deciding type do not restrict a draft, since the roles the
   object will hold are computed only once it exists. For a draft, the
   locked field is the enforcement — it rejects client values server-side —, and
   persisting the draft writes its values without a per-attribute check, so values
   the application prefills are kept. Reading an attribute without a Read grant of
   its own needs no check per row or form object (both come from read-filtered
   access); the `CommandApprovalService` is consulted for writes only.
4. **Commands say what they do, not which right they need.** An action that knows
   the model operation it performs brings the matching executability rule itself
   (`ViewAction#getIntrinsicRule()`, combined by `generic-command` with its
   configured `<executability>`; `with-transaction` forwards the rules of the
   actions it wraps, while `if`/`switch` do not, since their branch is chosen only
   when the chain runs). The rule decides on the command input, not on the value
   handed along the chain:
   - an object-deleting action → Delete on the command input;
   - a draft-creating action for a create dialog (type, optionally container and
     composition reference) → Create on the type (plus Write on the composition),
     checked **before** the dialog opens;
   - the dialog's persisting action → the same Create check.

   Commands whose effect is a free script, and custom business operations, use
   one general rule: `<model-access operation="…"/>` on the command input, an
   attribute of it, a type, or a container's composition reference.
5. **One presentation policy.** How a refused element is displayed is derived
   from what the refusal depends on (`ModelAccessPolicy`):
   - **hidden** when the refusal depends on no concrete object — the check runs
     against the security root (a creation without container, a check on a
     `type`), the type grants the operation to no role at all, the checked
     attribute has a grant for the operation listing no role, or the user is
     restricted: the operation is never possible for this user;
   - **disabled**, giving the reason that names the operation ("You may not
     delete this object."), when the check on a concrete object — the object
     operated on (the command input), or the container to create in — fails.

   A rule overrides the derived display with `denied="hide"` or
   `denied="disable"`. An empty object or container is nothing to check: the
   rule answers executable and leaves a missing input to `<null-input-disabled/>`
   and its kin.

   ```xml
   <model-access operation="Delete"/>                         <!-- the command input -->
   <model-access operation="Write" attribute="secret"/>       <!-- one attribute of it -->
   <model-access operation="Approve" object="selection"/>     <!-- custom command group -->
   <model-access operation="Create" type="my.module:Project"/><!-- top level -->
   <model-access operation="Create" container="project" reference="tasks"/>
   <model-access operation="Create"/>                         <!-- the type of the transient command input -->
   ```

   The actions knowing their model operation are `<delete-object/>`,
   `<create-transient type="…" [container reference]/>` (the draft; checked before the
   dialog opens) and `<persist-transient [type] [container reference]/>` (the dialog's
   Create button, created type from its input unless given).
6. **Typeless units keep their scope.** Navigation units, dashboards and admin
   areas have no model type; they stay gated by `<access-control scope>`
   (§1–§4).

