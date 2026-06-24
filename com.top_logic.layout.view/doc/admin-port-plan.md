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
| System | Scheduler / tasks (history, block/unblock, run) | `admin/technical/scheduler/schedulerView.layout.xml` (`TaskTreeComponent`, `TaskResultTreeComponent`) |
| System | Maintenance mode | `admin/technical/maintenanceMode/maintenanceWindow.layout.xml` (`MaintenanceWindowManager`) |
| System | Lock management (view / force-release) | `admin/technical/locks/locksView.layout.xml` (`ReleaseLockCommand`) |
| System | Logs (view + logger-level config) | `admin/technical/logs/logView.layout.xml` (`LoggerAdminBean`) — **redesign freely, don't port the old structure** |
| Model | Model editor (modules / types / attributes) | demo `module-graph-demo.view.xml` already exists — **move into admin + polish into an editor**, not a from-scratch port |

### DEFER — out of scope now, do not lose

| Item | Why deferred |
|---|---|
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

### Batch 1 — Monitoring, read-only  ▢
Establishes the "Monitoring" admin section.
- [x] Add `monitoring` section tab to `admin.view.xml` with an inner tab-bar.
- [x] System environment (read-only property table) — `SystemEnvironmentElement`.
- [ ] Application monitor (JVM memory / threads / classpath / libs panels).
- [ ] Thread monitor (master table + detail with stack trace).
- [ ] Memory monitor (current heap/GC figures; chart deferred).

### Batch 2 — Monitoring, stateful / historical  ▢
- [ ] SQL/DB monitor with start / stop / clear commands.
- [ ] Revision / audit monitor (change history table + detail).
- [ ] User-session monitor (active sessions + failed-login tracking).

### Batch 3 — System / technical  ▢
Establishes the "System" admin section.
- [ ] Services management (list + start/stop/restart + config editor).
- [ ] Scheduler / tasks (task tree, run history, block/unblock, trigger).
- [ ] Maintenance mode (schedule window, disconnect users).
- [ ] Lock management (held locks table, force-release).

### Batch 4 — Logs (redesign)  ▢
- [ ] Log viewer + logger-level configuration, designed fresh for the React
      layer (not a structural copy of the legacy log folder / settings split).

### Batch 5 — Model editor (move + polish)  ▢
- [ ] Move `module-graph-demo.view.xml` out of the demo nav into the admin hub
      as a `model` section.
- [ ] Polish from a read-only module/class-diagram browser into an editor:
      edit type/attribute/module properties, create/delete via dialogs.

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
