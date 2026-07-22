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
| Charts | **Migrated** | `demo/chart-demo.view.xml` (bar/doughnut/line/combined, click-to-drill dialog). Depends on `tl-layout-react-chartjs`; `demo.charts` model + seed data. |
| Flow diagram + Gantt | **Migrated** | `demo/flow-diagram-demo.view.xml` (build-plan tree) and `demo/gantt-demo.view.xml` (axis LOD, spans/edges/decorations). Depend on `tl-react-flow-server`; flow uses the `test.flowchart` model + seed data. |
| PDF viewer | **Migrated** | `demo/pdf-demo.view.xml` — inline PDF of server-rendered `pdfFile()` HTML via the `<pdf>` element (`tl-layout-view`). |
| Config editor | **Migrated** | `demo/config-editor-demo.view.xml` + `DemoConfigEditorElement`, over a self-contained `DemoEditorConfig` (not the legacy `TypeDemos`). Depends on `tl-layout-configedit`. |
| TL-Script editor | **Migrated** | `demo/tlscript-editor-demo.view.xml` via the `<tlscript-editor>` element (`tl-model-search-react`). |
| Constraint test + composition table | **Migrated** | `demo/constraint-test.view.xml` (+ create / edit-item dialogs) over the `test.constraints` model, exercising mandatory/size/range/warning/expression constraints and an in-form `<composition-table>`. |
| Tiles + multi-tab tiles | **Migrated** | `demo/tiles-demo.view.xml` and `demo/tiles-multi-demo.view.xml` (drill-down tile stacks with app-bar breadcrumb). |
| Responsive master-detail | **Migrated** | `demo/responsive-md-demo.view.xml` (`<adaptive-detail>`, cascading scopes → milestones) over the `tl.demo.projectManagement` model + seed data (+ create dialogs). |
| Error handling | **Migrated** | `demo/error-handling-demo.view.xml` — `TopLogicException` and wrapped KB errors. |
| Green-field table filtering | **Migrated** | Per-column `RegexpOptionsFilter` (facets) and model/script-defined `ScriptedFilter` (over `demo.filter:LongRange`) folded into the **Attributes** table. |

## Folded out (deliberately not reproduced as standalone demos)

| Area | Verdict | Notes |
|------|---------|-------|
| `DemoTypes` tree / structure (composite refs, `CyclicDemoTreeBuilder`) | **Drop here** | Structure is a separate concern from attribute kinds. A dedicated structure/tree demo belongs to a later phase, not on the attribute-kind type. |
| `DemoTypes` polymorphism (A/B/C/L/X inheritance, security P/PP/PX) | **Drop here** | Inheritance is a separate concern; not mixed into the flat `Demo` type. |
| Isolated common-UI-element demos (split-panel, tabs, channels, dialog, context-menu, open-window, dashboard + layout, scroll repro, commands display, command-security/theme settings) | **Drop** | These compositions appear naturally inside the real feature views; standalone pages add no value. |
| Standalone input-controls and green-field table demos | **Drop** | Redundant with the **Attributes** demo (field controls + green-field table); the table's distinct filtering capability was folded into Attributes instead. |
| Static graph layout and live module graph diagrams | **Drop** | The module graph was only the model-editor seed; the static graph layout is a subset of it. Flow + Gantt already cover the `tl-react-flow` capability. |

## Deferred (later phases — value + the dependency they need)

| Area | Verdict | Needs |
|------|---------|-------|
| Security permission matrix (`SecurityMatrixElement`) | **Defer** | Reusable from `com.top_logic.layout.view/views/admin/permissions.view.xml`. |
| BPE (business process) | **Defer** | `tl-bpe-app`. |
| Reporting / office (Word/Excel/PowerPoint export) | **Defer** | `tl-reporting`, `tl-reporting-office`. |
| Search | **Defer** | `tl-search-*`. |
| Mail folders | **Defer** | `tl-mail`. |
| Import / export (CSV / Excel importers) | **Defer** | `tl-importer`. |
