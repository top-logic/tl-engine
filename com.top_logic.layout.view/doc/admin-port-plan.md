# Administration UI port to the React view layer

Working plan for porting the legacy component-based administration UI
(`*.layout.xml`) to the React view layer (`com.top_logic.layout.view`,
`*.view.xml`). This is a living document: keep the checkboxes and the triage
table current as batches land.

Ticket: #29108. Branch: `CWS/CWS_29108_port_admin_uis_to_react`.

## Goal

Bring the operator- and administrator-facing tooling that today only exists in
the legacy layout UI into the React view layer, reusing the existing
declarative building blocks (`<table>`, `<form>`, `<split-panel>`, `<window>`,
`<tab-bar>`, command/action chains, `SecurityMatrixElement`). Access-control
administration (Accounts, Groups, Roles, Permissions) is already done and is
the template to follow.

## Integration points (verified)

- **Admin hub:** `com.top_logic.layout.view/.../views/admin/admin.view.xml` is
  an outer `<tab-bar>` whose tabs are admin *sections*. Today it holds one
  section, `access-control`, whose inner tab-bar holds Accounts / Groups /
  Roles / Permissions. **New sections are added as sibling tabs of
  `access-control`** (e.g. `monitoring`, `system`, `model`), each with its own
  inner tab-bar of views.
- **Reachability:** the demo's `app.view.xml` exposes the hub via a
  `<nav-item id="administration" route="none">` gated by
  `<access-control scope="administration"/>` (root sees it unconditionally). No
  new wiring is needed to surface additional sections — they appear inside the
  hub automatically.
- **Per-view access control:** gate sensitive sections/views with
  `<access-control scope="…"/>` and a `SecurityScope` registered via
  `SecurityScopeService` (see `technicalDemoConf.config.xml` for the pattern).
- **Reusable building blocks:** master/detail via `<split-panel>`+`<pane>`;
  list via `<table>` (rows from a TL-Script query, columns from model
  attributes, sort/filter/width-personalization); detail via `<form>`+`<field>`;
  creation via `<window>` dialogs; multi-step ops via `<generic-command>` +
  `<execute-script>`/`<with-transaction>`/`<write-channel>`/`<confirm>`. Bespoke
  cells via `CellContent.Raw` + a `CellControlFactory`. App-specific widgets
  (like `SecurityMatrixElement`) are `UIElement`s referenced by `class=`.

## Triage (final, per owner's decisions)

### PORT — in scope for this branch

| Area | View | Legacy source (key classes) |
|---|---|---|
| Monitoring | System environment | `admin/monitor/systemEnvironment.layout.xml` |
| Monitoring | Application monitor (JVM/memory/threads/classpath) | `admin/monitor/app/applicationMonitor.layout.xml` (`ApplicationMonitorComponent`) |
| Monitoring | Thread monitor | `admin/monitor/thread/threadMonitor.layout.xml` (`ThreadListModelBuilder`, `ThreadDetailComponent`) |
| Monitoring | Memory monitor | `admin/monitor/memory/memoryMonitor.layout.xml` |
| Monitoring | SQL/DB monitor (start/stop/clear) | `admin/monitor/db/sqlMonitor.layout.xml` |
| Monitoring | Revision / audit monitor | `monitoring/.../monitor/history/revisionMonitor.layout.xml` |
| Monitoring | User-session monitor (incl. failed logins) | `monitoring/.../monitor/session/userMonitorWithFailedLogin.layout.xml` |
| System | Services management (start/stop/restart/configure) | `admin/technical/services/servicesView.layout.xml` (`TLServiceConfigListModelBuilder`, `ServiceConfigEditor`) |
| System | Maintenance mode | `admin/technical/maintenanceMode/maintenanceWindow.layout.xml` (`MaintenanceWindowManager`) |
| System | Lock management (view / force-release) | `admin/technical/locks/locksView.layout.xml` (`ReleaseLockCommand`) |
| System | Logs (view + logger-level config) | `admin/technical/logs/logView.layout.xml` (`LoggerAdminBean`) — **redesign freely, don't port the old structure** |
| Model | Model editor (modules / types / attributes) | demo `module-graph-demo.view.xml` already exists — **move into admin + polish into an editor**, not a from-scratch port |

### DEFER — out of scope now, do not lose

| Item | Why deferred |
|---|---|
| Scheduler / tasks | Will be **completely redesigned** (execution engine + management UI), tracked under its own ticket — not a 1:1 port. |
| Memory monitor chart (trend over time) | Needs a redesign before porting; revisit after the table-based monitors land. |
| Maintenance pages (downtime page editor) | A new approach is required; not a 1:1 port. |
| DB schema admin (model→table mapping editor) | Large; its own effort. |
| Script console; "My latest changes" | Developer/user conveniences, not core admin. |
| Domain-module admins (Contact/company/org, BPE workflow, login messages, user invite) | Per-product, not core engine admin. |

### SKIP — will not port

| Item | Why |
|---|---|
| Person↔Role / Group↔Role assignment | Superseded by the upcoming access-control layer refactoring. |
| Attribute (field-level) security + release | Pure legacy; to be replaced by a new view. |
| DB-schema monitor | Requires redesign; not worth porting as-is. |
| Lists / Classifications | Legacy. |
| Units / Checklists / Currencies | Legacy / domain-specific. |
| Layout editor, template/legacy inspector | Superseded by the React View Designer (`designer.view.xml`). |

### Needs a decision

- **Role inspector** (effective-permissions viewer,
  `element/.../security/roleInspector/showSecurity.layout.xml`). Useful audit
  tool, but may be reshaped by the access-control refactoring that obsoletes the
  assignment views. Confirm before porting; held out of the batches below.

## Implementation batches

Each batch: (1) add/extend the admin-hub section, (2) author the `*.view.xml`(s)
reusing existing elements, (3) add the minimal Java (`ViewAction`/`UIElement`/
model-builder bridge) only where an existing control can't express it, (4) wire
into the demo so it's reachable, (5) Playwright-verify against a running app,
(6) normalize layouts + run the conformance checks, (7) commit.

Ordering is chosen to establish patterns on read-mostly views first, then
stateful command-driven ones, then the larger model editor.

### Batch 1 — Monitoring, read-only  ✅ DONE
Establishes the "Monitoring" admin section. A shared `SectionedTable` base renders the key/value
tables; views are hosted in the chrome-less `<full-page>` element (added this batch).
- [x] Add `monitoring` section tab to `admin.view.xml` with an inner tab-bar.
- [x] System environment (system properties / VM args / configuration variables) — `SystemEnvironmentTable`.
- [x] Application monitor (status, memory snapshot, Java VM, system) — `ApplicationMonitorTable`.
- [x] Thread monitor (thread table + live stack-trace detail via selection channel) — `ThreadTable` + `ThreadStackTable`.
- [n/a] Standalone memory snapshot folded into the Application monitor's Memory section; the
      historical memory **chart / GC** monitor stays **deferred** (needs redesign).

**Reusable infra added this batch:**
- `<full-page>` — chrome-less fill container for a view's single main content; the tab/nav labels
  it, so no title is rendered and **no local toolbar** (commands project to a slot up the tree).
- `<panel fill="true">` + the green-field table fill fix (tab-content flex column, split-panel
  fill, `.tlPanel--fill`) so virtualized tables bound their scroll viewport; documented on
  `TableViewControl`.
- `SectionedTable` base for (section, name, value) tables with stable per-row selection keys.

**Command-bearing views (decided):** Batches 2–4 (SQL start/stop/clear, scheduler, services,
maintenance, locks, logs) need **commands**. The clean app-bar **command projection** is
**postponed**. Until then, a command-bearing view uses a titled `<panel fill="true">` and places
its commands in the panel's `<commands>` toolbar (rendered in the panel header). This duplicates
the label (tab + panel title) — **accepted as a temporary tradeoff**. Read-only single-table views
keep using the chrome-less `<full-page>`. The reusable `<visible-if expr="…">` executability rule
(added with the SQL monitor) shows a command only while a TL-Script predicate over its `input`
channel holds — used to make Start vs. Refresh/Stop mutually exclusive.

### Batch 2 — Monitoring, stateful / historical  ▢
- [x] User-session monitor — `UserSessionTable` (Sessions tab) + `FailedLoginTable` (Failed logins tab), read-only, 30-day window.
- [x] SQL/DB monitor — `SqlMonitorAction` (start / refresh / stop) + `SqlStatisticsTable`, in a titled
      fill panel; **validates the titled-panel command pattern** for Batches 3–4.
- [x] Revision / audit monitor (v1) — `revision-monitor.view.xml` (Monitoring › Änderungen): a flat,
      read-only `<full-page>` `<table>` of recent commits. Rows come from the existing `changeLog()`
      TL-Script function (`changeLog(null, 50)` = global, newest 50); the row type is
      `tl.changelog:ChangeSet`, so columns (revision, date, author, message, changes) and their labels
      derive from the model attributes — no hand-written table or column I18N. **Based on the "Meine
      letzten Änderungen" personal-history dialog, not the old technical revision admin** (same
      new-style `tl.changelog:ChangeSet` model type). The per-object change drill-down
      (`Change`/`Modification`) and revert remain a follow-up.

### Batch 3 — System / technical  ▢
Establishes the "System" admin section (added with the locks view as a sibling tab of
`access-control` / `monitoring`).
- [x] Lock management — `LockTable` (one row per `LockInfo`: owner, operation, locked objects,
      aspects, timeout, cluster node) + `LockListAction` (refresh / force-release the selected lock),
      in a titled fill panel. Reuses the titled-panel command pattern and the `<visible-if>` rule
      (Release shown only while a lock is selected). The table self-loads a live
      `TokenService.getAllLocks()` snapshot on open and rebuilds from the `locks` channel after a
      command. The legacy tree-table (LockInfo → Token) is flattened to one row per lock for v1.
- [x] Services management (v1: list + start/stop/restart) — `ServiceTable` (one row per
      `BasicRuntimeModule`: status, label, implementation class) + `ServiceLifecycleAction`
      (start / stop / restart / refresh). The table writes the selected module to a `selection`
      channel and its running status (`ACTIVE`/`INACTIVE`) to a `state` channel; the commands gate on
      `state` via `<visible-if>` so Start (stopped) and Stop / Restart (running) are mutually exclusive
      and stay fresh after each op (the command rewrites `state`). The action reads the module from the
      `selection` channel itself (a `ViewAction` can resolve channels via `ViewContext`), decoupling the
      gating input from the action's data. The **per-service config editor is deferred** (a follow-up).
- [~] Scheduler / tasks — **dropped from this plan.** The scheduler will be completely redesigned
      (execution engine + management UI), tracked under its own ticket; not a 1:1 port.
- [x] Maintenance mode — `maintenance.view.xml` (System › Maintenance mode): a titled fill panel whose
      `MaintenanceStatusView` shows the live state (normal / about-to-enter / in maintenance, plus the
      user message) and seeds a `state` token channel (`NORMAL` / `PENDING` / `ACTIVE`). The commands gate
      on that token via `<visible-if>` so Enter (normal), Abort (scheduled) and Leave (active) are mutually
      exclusive; Refresh re-reads. Enter opens `enter-maintenance.view.xml`, a dialog bound to a transient
      `tl.admin:MaintenanceMode` model (delay in minutes — empty/zero enters immediately — and an optional
      message); the primary command runs `MaintenanceModeAction` (ENTER), which drives the
      `MaintenanceWindowManager`. Disconnecting non-allowed users is handled by the manager on entering.
      The legacy JS countdown timer is dropped for v1 (the pending status is shown as text).

### Batch 4 — Logs (redesign)  ▢
- [x] Log viewer — `logs.view.xml` (System › Protokolle): a `<split-panel>` master/detail. The
      `LogFileTable` lists the files in `LoggerAdminBean.getLogDir()` (name, human-readable size, last
      modified) and writes the selected file to a `selectedFile` channel; the `LogLineTable` reads that
      channel and shows the file's **parsed** entries — one row per entry — in a sortable, per-column
      filterable/searchable table (time, severity, category, thread, message, details), newest first.
      Parsing **reuses the legacy `com.top_logic.monitoring` `LogParser`/`LogLine`/`LogFile`** (added a
      `tl-monitoring` dependency); only the last 4 MB are parsed. The time column filters by **date-time
      range** (`ComparableColumnFilter`), severity by **value checkboxes** (`OptionsColumnFilter` over the
      standard severities), the rest by text. Each file row carries a **download button** (a read-only
      `ReactBinaryFieldControl` serving the file). Refresh ticks a `reload` channel so the current file is
      re-parsed (newly appended entries appear) without changing the selection. Per-file selection only —
      the legacy single combined cross-file table is deliberately **not** carried over.
- [x] Logger-level configuration — `logger-levels.view.xml` (System › Protokollebenen): a titled fill
      panel whose `LoggerLevelTable` lists one row per configured logger (name + an **inline level
      dropdown**) read from the extended `LogConfigurator` facade. Choosing a level applies it immediately
      at runtime (`LogConfigurator.setLoggerLevel`); Refresh re-reads the live config. The facade gained
      `getLoggerLevels` / `setLoggerLevel` / `getLevelNames` (no-op defaults; implemented in
      `Log4JConfigurator` via the Log4j2 `Configurator`), so the view stays backend-agnostic. The inline
      cell reuses `SimpleSelectFieldModel` + `ReactDropdownSelectControl` (a `CellContent.Raw` factory
      cell); a `FieldModelListener` applies the chosen level.

### Batch 5 — Model editor  ✅ DONE
The model editor cannot live in `com.top_logic.layout.view`: it reuses the
`<flow-diagram>` element (and `reactFlow*` script functions), which are defined in
`com.top_logic.react.flow.server` — a module that itself depends on the view layer, so
the view layer cannot depend back on it. The editor therefore lives in a new
**`com.top_logic.dev.tools`** (`tl-dev-tools`) module for in-app developer tools, which
sits above the flow module. The admin hub holds a **forward reference**
(`<view-ref view="admin/model/model-editor.view.xml"/>`) into that module; an app that
surfaces the hub and wants the Model section adds a `tl-dev-tools` dependency (the demo
does). A general modular view plug-in mechanism is left for later.
- [x] `model-editor.view.xml` (admin hub, Model section): master/detail editor over the
      application model. A `<table>` of `tl.model:TLModule` (left) selects a module; the reused
      class `<flow-diagram>` (center) shows and selects the module's types and their attributes;
      read-only detail forms show the selected module / part.
- [x] Create / delete / rename via dialogs and a confirm, driven by one reusable
      `ModelEditAction` (modes CREATE_MODULE / CREATE_TYPE / CREATE_ATTRIBUTE / RENAME / DELETE)
      over the `TLModelUtil` primitives (`addModule` / `addClass` / `addEnumeration` /
      `addProperty` / `deleteRecursive`) inside `<with-transaction>`. New type picks class vs.
      enumeration via a `tl.devtools:TypeKind` enum; new attribute picks the value type and
      cardinality (mandatory / multiple). Rename pre-fills the current name and uses `setName`.
      A `reload` trigger input is bumped (`now()`) after a diagram-affecting mutation so the
      diagram re-reads its (unchanged) module input. Model-part names are read-only through the
      generic form, so renaming goes through the action rather than live form binding.

## Conventions & verification checklist (every batch)

- Dialogs: primary command `placement="BUTTON_BAR"`; `<text>` truncation via
  `overflow="ellipsis"`.
- New `.java`: SPDX header + class-level Javadoc (else `TestComment` fails);
  configured classes follow the TLDoclet "no method links in main description"
  rule.
- New/edited `.view.xml`: normalize with `XMLPrettyPrinter` (no trailing
  newline) or `TestLayoutsNormalized` fails.
- German `messages_de.properties` maintained by hand; English is generated.
- Manual Playwright verification in the running demo before marking a view done.
- Commit per batch (per-turn), `Ticket #29108: …`.
