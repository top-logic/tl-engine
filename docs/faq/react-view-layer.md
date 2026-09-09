# FAQ: React view layer (`com.top_logic.layout.view` and the React table)

## The `.view.xml` layer is a composition layer, not a place for new React components

`com.top_logic.layout.view` is declarative: a `.view.xml` assembles existing React controls (`TLPanel`, `TLWindow`, `TLForm` / `TLFormField`, `TLTextInput`, `TLButton`, `TLText`, `TLDialog`, …) through `UIElement` configs (each a `@TagName`) and view commands / actions. To build a feature (a login dialog, a user menu, …), compose these via XML plus small Java `ViewCommand` / `ViewAction` / `ViewExecutabilityRule` / `UIElement` classes that reuse existing controls. Do **not** hand-roll bespoke monolithic React components. A new React control is justified only for a genuinely new generic widget (e.g. a `type=password` input), not for assembling forms / buttons that already exist.

- **Forms** bind to a model object: `<form input="ch"><field attribute="x"/>`. For ad-hoc input, create a small transient model type (`new(\`mod:Type\`, transient: true)`); a field's control is chosen by a `<input-control><impl class="…Provider"/></input-control>` annotation.
- **Dialogs** open a `.view.xml` via `<open-dialog dialog-view="…">`; close via `CancelDialogCommand` / `DialogManager.closeTopDialog`. `currentUser()` is a TL-Script function usable in `<derived-channel expr="…">`.
- **Referencing a `UIElement` impl by `class=` in view content.** View content lists resolve entries by `@TagName`, so an app-specific element that should not claim a global tag is placed via the content property's *entry tag* plus `class=`. The `children` content property (`ContainerElement.Config`) is `@EntryTag("child")`, so write `<child class="fq.MyElement"/>` inside a `<panel>` / container. If a cell provider is reusable, make it public rather than justifying a separate element; justify a separate element by genuinely different data / behavior.
- **Standalone form-field controls bind to a `FieldModel`.** For a standalone field control (e.g. a checkbox cell), use the concrete `com.top_logic.layout.form.model.AbstractFieldModel` + `FieldModelListener` — not `FormContext` / `FormField` / `FormFieldAdapter`, which are legacy-compat shims. `AbstractFieldModel` is editable by default, needs no `FormContext` parent, and triggers no label resource lookup in `ReactFormFieldControl`.
- Modifying persistent state from a control's value listener needs a transaction; the listener has no ambient one, so open `beginTransaction()` there (or buffer changes and apply them under one transaction on save).

## `TableViewControl` is the sole React table control

`TableViewControl` / `com.top_logic.table.TableView` (#29108) is the only React table control. Everything renders through this stack: the `<table>` element (`TableElement`; sort, per-column `<filter>`, type-derived default columns, width personalization, shared `ColumnsConfig` / `ColumnConfig`), the access-control permission matrix (`SecurityMatrixElement`), the in-form `<composition-table>` (`CompositionTableControl`), and the technical React-table demo (`DemoReactTableComponent`: flat `ListRowSource` + `TreeRowSource` tree).

- Each `TableViewControl` cell is a registered child control, so interactive cell controls work. Cell rendering (`CellContentReactAdapter`): `CellContent.Text` / `Labeled` → text; `CellContent.Editable` backed by a *boolean* `FieldModel` → interactive `ReactCheckboxControl` (other field types read-only); **`CellContent.Raw` whose payload is a `CellControlFactory` → whatever control the factory builds** — the escape hatch for bespoke cells. The composition table uses it to put typed field inputs (`FieldControlService.createFieldControl(part, fieldModel)`) and detail / delete action buttons in cells; a column's `value(R)=row` plus a renderer returning `Raw(ctx -> buildControl(ctx, row, …))` gives the factory the row.
- **`TLPanel` renders a single `toolbar` child control (a `ReactToolbarControl`), not a `toolbarButtons` list.** Push a panel toolbar via `putState("toolbar", new ReactToolbarControl(ctx))` + `addGroup(name, ToolbarGroupDisplay.INLINE, …, List.of(button))`.
- Mutable rows: `ListRowSource.setElements(list)` then `TableViewControl.refreshData()`. `DefaultTableView.create(columns, source[, ViewStateStore, TableId])` — the 2-arg form skips personalization; pass a stable `TableId` to persist column width / order.

## Filling the available height: the fill contract

A control that should span the height its container offers - instead of growing with its content - carries the CSS class `tlFill`, which the layout CSS turns into `flex: 1; min-height: 0`. That bounds the control only while its container has a definite height itself, so the decision is not local: a container hosting a filling child has to fill in turn, up to the first box that is bounded anyway (the viewport-high mount point, a tab-content region, a scrolling panel body). This chain is a property of the path through the control tree, which is why it lives in the React bridge (`react-src/bridge/fill.ts`) rather than in a CSS selector enumerating the child types allowed to grow.

A container component therefore declares how it takes part:

- `useFill(fills)` — a fixed decision by the control's own configuration: `<panel fill="true">` reports filling, a plain panel does not. Returns the class for the root element.
- `useFillHost(alwaysFills?)` — a container: it fills while it always does (a split panel, whose panes are sized proportionally; a tab bar, whose strip stays pinned) or while one of its children reports filling, and reports that on to its own container. Returns the class plus the host to provide to the children (`<FillProvider host={…}>`).
- `<FillBarrier>` — ends the chain: a region bounded on its own that scrolls what does not fit (a panel body, a tab content area), or a surface laid out apart from the page (a dialog, a window, a drawer). A filling control inside resolves its height against that region, and nothing outside reacts to it.

A container that declares nothing is opaque: a filling control inside it does not grow it, so a new container element that lays out vertical space adds its declaration.

## A tile-stack frame is kept, not rebuilt

`<tile-stack>` holds one frame per stack position (`ReactTileStackControl`): the `initial` view, followed by the frames of the path channel. The frame at the end of the path is the displayed one; the frames it covers keep their control tree and their layout box (`.tlTileStack__frame--covered` takes them out of the flow at the size of the stack and makes them invisible). Returning to a frame - a breadcrumb click, a `<navigate-pop>` - therefore shows it as the user left it: the selected tab, the selected table row, the values being edited and the scroll offsets included. A path change keeps the frames of the longest common prefix and disposes the ones it drops, so a path reconstructed from a URL keeps the frames it names (`TileFrame` compares by view, label and params).

The hiding sits on a wrapper element the stack renders itself: a frame's content renders its own root element, and a style set on that from the outside is overwritten the next time the content re-renders.
