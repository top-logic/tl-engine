# Legacy `tl-demo` triage for the React-only demo

This application (`tl-demo-react`) is a React-only demo built on the
`com.top_logic.layout.view` declarative `.view.xml` layer. It is the successor
test bed for UI-facing TopLogic features and a kitchen sink of React UI
elements. The legacy `com.top_logic.demo` (`tl-demo`) is left untouched and
serves as a reference.

This file triages legacy `tl-demo` view/model areas into **Migrate** (rebuild
here, clean), **Drop** (legacy-only or superseded), or **Defer** (valuable, but
later phase / needs an additional module). Only "Migrate now" rows are
implemented in the current deliverable.

## Implemented now

| Area | Verdict | Notes |
|------|---------|-------|
| `DemoTypes` attribute kinds | **Migrate now** | Folded into one flat, non-polymorphic, non-structured `demo.react:Demo` class. Shown in two presentation forms (wide table + detail dialog; selector table + form) under the **Attributes** entry, with real persistent CRUD. |
| Account / group / role administration | **Migrate now** | Reuses the shipped `admin/admin.view.xml` from `com.top_logic.layout.view`, gated by the `administration` security scope. |
| React login / logout | **Migrate now** | Reuses the shipped `login.view.xml` + `tl.login:Credentials`. |
| WYSIWYG HTML attribute (`tl.model.wysiwyg:Html`) | **Migrate now** | `html` attribute on `Demo`, edited by the React WYSIWYG control. Depends on `tl-model-wysiwyg` (the type) + `tl-layout-react-wysiwyg` (the control, which registers a `FieldControlService` provider for the type). |

## Folded out (deliberately not reproduced as standalone demos)

| Area | Verdict | Notes |
|------|---------|-------|
| `DemoTypes` tree / structure (composite refs, `CyclicDemoTreeBuilder`) | **Drop here** | Structure is a separate concern from attribute kinds. A dedicated structure/tree demo belongs to a later phase, not on the attribute-kind type. |
| `DemoTypes` polymorphism (A/B/C/L/X inheritance, security P/PP/PX) | **Drop here** | Inheritance is a separate concern; not mixed into the flat `Demo` type. |
| Per-element micro-demos (split-panel, tabs, channels, dialog, context-menu as standalone pages) | **Drop** | These compositions appear naturally inside real feature views; standalone pages add no value. |

## Deferred (later phases — value + the dependency they need)

| Area | Verdict | Needs |
|------|---------|-------|
| Green-field `TableViewControl` filtering/scripted filters | **Defer** | Already partly exercised by the Attributes table; expand with per-column filters. |
| `composition-table` (in-form child editing) | **Defer** | Needs a child type — reintroduce once a second type is justified. |
| Security permission matrix (`SecurityMatrixElement`) | **Defer** | Reusable from `com.top_logic.layout.view/views/admin/permissions.view.xml`. |
| Config editor | **Defer** | `tl-layout-configedit` (already transitive). |
| Charts | **Defer** | `tl-layout-react-chartjs`. |
| Flow / graph / module diagrams | **Defer** | `tl-react-flow-*`, graph modules. |
| BPE (business process) | **Defer** | `tl-bpe-app`. |
| Reporting / office (Word/Excel/PowerPoint export) | **Defer** | `tl-reporting`, `tl-reporting-office`. |
| Search | **Defer** | `tl-search-*`. |
| Mail folders | **Defer** | `tl-mail`. |
| Import / export (CSV / Excel importers) | **Defer** | `tl-importer`. |
| Gantt | **Defer** | reporting/gantt modules. |
