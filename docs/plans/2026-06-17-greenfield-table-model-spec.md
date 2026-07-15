# Green-field Table Model — Specification

**Status:** Draft for iteration · **Ticket:** #29108 · **Date:** 2026-06-17

Supersedes the legacy-integration approach in
[`2026-03-03-react-table-model-and-tree-design.md`](./2026-03-03-react-table-model-and-tree-design.md),
which wires `ReactTableControl` onto the legacy `TableModel`. That integration was the
bootstrap; this document is the clean replacement for the model layer behind it.

## 1. Motivation

The legacy table stack (`TableModel` / `TableViewModel` / `ObjectTableModel` /
`TreeTableModel` + `TableConfiguration` / `ColumnConfiguration` + the
`ConfiguredFilter` / `FilterDialogBuilder` machinery) is one conflated god-object that
mixes four unrelated concerns:

- **data storage** (always fully materialized in memory — no windowing/lazy/DB pushdown),
- **view state** (sort/filter/order/width/frozen/selection, persisted through ~10
  scattered `save*`/`load*` methods),
- **tree flattening** (a parallel `TreeTableModel` universe instead of a variant of the
  flat case),
- **UI rendering & input** (column filters are welded to `FormContext`/`FormField` and a
  `FilterDialogBuilder` that drags in `Control`/`DisplayContext`/`PopupHandler`).

Designing **from the UI backward**, the React control already proves how little the UI
truly needs: a *windowed sequence of rows*, each carrying a key + optional
depth/expansion, plus per-cell value/renderer, plus a small command set. This spec
defines the smallest clean model that satisfies that surface and covers every legacy
capability, with **zero dependency on legacy table/form/control code**.

## 2. Decisions locked for this draft

1. **Fully generic.** `Column<R,V>`, `RowSource<R>`, `TableView<R>`. No untyped
   `Accessor(Object,String)→Object`.
2. **Neutral renderer.** A new `CellRenderer<V>` SAM in the model module; the React tier
   supplies the `CellRenderer → ReactControl` adapter. (We accept that some casts may
   surface later; revisit only if they do.)
3. **Grouping & aggregation are first-class** from day one (`RowKind` + synthetic rows +
   `Aggregator`), not a UI-only afterthought like legacy.
4. **Query pushdown from the beginning.** `QueryRowSource` translates sort+filter to the
   data tier; we do not defer it to "never".

## 3. Architecture

```
        ┌───────────────────────────────────────────────┐
        │  ReactTableControl2   (thin adapter, no legacy)│   UI tier
        └───────────────────────┬───────────────────────┘
                                 │ talks ONLY to:
        ┌───────────────────────▼───────────────────────┐
        │  TableView<R> = Columns + RowSource + ViewState│   binding / conversion
        │  windowed read API · commands · change events  │
        └───┬───────────────┬───────────────────┬────────┘
            │               │                   │
   ┌────────▼──────┐ ┌──────▼────────┐ ┌────────▼─────────┐
   │ Column<R,V>[] │ │ RowSource<R>  │ │ TableViewState   │   model tier
   │ declarative   │ │ windowed      │ │ serializable     │
   └───────────────┘ └──────┬────────┘ └──────────────────┘
                            │ implementations
            ┌───────────────┼───────────────────┐
       ListRowSource   QueryRowSource       TreeRowSource
       (in-memory)     (DB/KB pushdown)     (flatten+expand+group)
```

**Provisional package root:** `com.top_logic.table` (parallel to legacy
`com.top_logic.layout.table`, so old/new never collide). Subpackages: `.column`,
`.rows`, `.view`, `.filter`, `.render`, `.react`. Name is bikeshed-open (see §12).

## 4. Core types

### 4.1 `Row<R>` — the windowed unit (unifies data / tree / group / aggregate)

The single insight that collapses flat tables, tree tables, grouping, and footers into
one abstraction: **everything the viewport displays is a `Row<R>`** with a *kind*, a
*depth*, and *expansion* state.

```java
enum RowKind { DATA, GROUP_HEADER, AGGREGATE }

interface Row<R> {
    RowKind kind();

    /** Stable identity — selection, expansion, incremental client patching. */
    Object key();

    /** Payload for DATA rows (and a group's representative row, if any). */
    R data();

    /** Group descriptor for GROUP_HEADER / AGGREGATE rows; null for DATA. */
    GroupKey group();

    /** Tree/group nesting. Flat data rows are depth 0. */
    int depth();
    boolean expandable();
    boolean expanded();
}
```

The React client already renders `depth`/`expandable`/`expanded` per row, so tree nodes,
group headers, and collapsible groups all map onto the *existing* client protocol with no
new client concepts. A `GROUP_HEADER`/`AGGREGATE` row is simply an expandable row whose
cells are rendered differently (see §4.4).

### 4.2 `Column<R,V>` — declarative, type-safe (replaces `ColumnConfiguration` + `Accessor`)

```java
interface Column<R, V> {
    String name();
    ResKey label();

    /** Typed accessor. Replaces Accessor.getValue(Object, String). */
    V value(R row);

    /** Neutral renderer SAM; the React tier adapts CellRenderer → ReactControl. */
    CellRenderer<V> renderer();

    /** Sorting: in-memory comparator and/or query pushdown. Absent ⇒ not sortable. */
    Optional<Sort<V>> sort();

    /** Filtering: UI-neutral input descriptor + predicate + optional pushdown. */
    Optional<ColumnFilter<V>> filter();

    /** Inline editing via FieldModel. Absent ⇒ read-only. */
    Optional<CellEditor<R, V>> editor();

    /** Footer / group-total computation. Absent ⇒ no aggregate cell. */
    Optional<Aggregator<R, V>> aggregate();

    // display
    int defaultWidth();
    boolean frozenEligible();
    CssClass css(R row);
    Optional<CellExistence<R>> existence();   // gates filter visibility on empty columns
}
```

A `TableView` holds `List<Column<R, ?>>`; per-cell access uses wildcard capture (a tiny
`cell(Column<R,V>, R)` helper). Heterogeneous `V` across columns is expected.

### 4.3 `CellRenderer<V>` — neutral rendering SAM (no React import)

```java
interface CellRenderer<V> {
    /** Produce a UI-neutral cell description; the UI tier turns it into a control. */
    CellContent render(V value);
}
```

`CellContent` is a small neutral value (text / icon / label+tooltip+css / "editable:
FieldModel" / "raw" escape hatch). The React adapter maps `CellContent → ReactControl`,
shaped like today's `ReactCellControlProvider`. Default renderers reuse foundational
`LabelProvider`/`ResourceProvider`-style SAMs (clean, no legacy table deps). If a column
needs bespoke React, it uses the `raw` escape hatch carrying a `CellControlFactory`
supplied by the React tier — keeping the column model itself React-agnostic.

### 4.4 Cell access dispatch on `RowKind`

```
DATA          → column.renderer().render(column.value(row.data()))
AGGREGATE     → column.aggregate().map(a -> render(a.over(group))).orElse(empty)
GROUP_HEADER  → first column renders the group label (spanning); others empty/aggregate
```

### 4.5 `RowSource<R>` — windowed (replaces `TableModel`/`ObjectTableModel`/`TreeTableModel`)

```java
interface RowSource<R> {
    /** Count of displayed rows after sort + filter + expansion + grouping. */
    int size();

    /** THE window. Enables lazy / DB-backed data. Returns synthetic + data rows. */
    List<Row<R>> window(int from, int to);

    /** Derive views — pure, no mutation of the receiver. */
    RowSource<R> withOrder(SortSpec sort);
    RowSource<R> withFilter(FilterSpec filter);
    RowSource<R> withGrouping(GroupSpec grouping);

    /** Per-option match counts for option-style filters (e.g. classification facets). */
    MatchCounts matchCounts(String column);

    /** Expansion (tree nodes AND collapsible group headers). */
    void setExpanded(Object rowKey, boolean expanded);

    void addListener(RowSourceListener l);   // SIZE_CHANGED | RANGE_INVALID(from,to)
}
```

Implementations:

- **`ListRowSource<R>`** — in-memory `List<R>`; sorts/filters/groups using the columns'
  `Comparator`/predicates/aggregators; `window()` slices. Replaces `ObjectTableModel` /
  `ArrayTableModel` / `EditableRowTableModel` / `CachedObjectTableModel`.
- **`QueryRowSource<R>`** — `size()` = count query, `window()` = windowed query,
  sort/filter pushed down via column pushdowns (§5). New capability: no full
  materialization. Mutations (add/remove/edit) invalidate ranges.
- **`TreeRowSource<N>`** — wraps a `TreeStructure<N>` and flattens the expanded, filtered
  subtree into the windowed sequence; `depth/expandable/expanded` come from the node.
  Encapsulates the legacy `_visibleSubtreeSize`, synthetic-node, and
  filter-parents/filter-children logic that today is smeared across
  `AbstractTreeTableModel`. **A tree is just a `RowSource`** — the UI tier never branches
  on "tree vs flat".

```java
interface TreeStructure<N> {
    List<N> roots();
    List<N> children(N node);          // may be lazy; loader invoked on expand
    boolean isLeaf(N node);
    boolean isFinite();                // false ⇒ infinite/lazy tree (no descendant counts)
    Object businessObject(N node);     // payload for cell value extraction
}
```

Grouping composes with all three sources via `withGrouping` (in-memory grouping for
`ListRowSource`; `GROUP BY` pushdown for `QueryRowSource`).

### 4.6 `TableViewState` — serializable (replaces `TableViewModel.save*/load*` + `PagingModel`)

```java
final class TableViewState {              // pure data, JSON-serializable
    List<String> columnOrder;             // visible columns, in order
    Map<String,Integer> widths;
    int frozenCount;
    List<SortColumn> sort;                // (column, ascending)+
    Map<String,FilterState> filters;      // serializable criteria — NO FormField
    GroupSpec grouping;                   // grouping columns + aggregate spec
    Set<Object> expanded;                 // tree + group expansion
    Selection selection;                  // {mode: SINGLE|MULTI, Set<key>}
    int pageSize;                         // 0 = virtual-scroll / show-all
    int page;
}

interface ViewStateStore {                // thin persistence port (no legacy ConfigKey)
    TableViewState load(TableId id);
    void save(TableId id, TableViewState state);
}
```

One value object is *both* the personalization payload *and* the client seed. The default
`ViewStateStore` persists through `PersonalConfiguration` behind this port; the model
never imports `PersonalConfiguration` directly.

### 4.7 Sorting, filtering, editing, aggregation contracts

```java
interface Sort<V> {
    Comparator<V> comparator();                 // in-memory
    Optional<OrderPushdown> pushdown();         // query ORDER BY translation
}

interface ColumnFilter<V> {
    FilterInput input();                        // UI-neutral descriptor (§6)
    Predicate<V> predicate(FilterState state);  // in-memory
    Optional<FilterPushdown> pushdown(FilterState state);  // query WHERE translation
    boolean countsMatches();                    // produce facet counts?
}

interface CellEditor<R, V> {
    FieldModel newField(R row, V current);      // AbstractFieldModel-based; React already binds it
    void commit(R row, V edited);               // write-back (caller owns the transaction)
}

interface Aggregator<R, V> {
    CellContent over(Group<R> group);           // sum/avg/min/max/count/median/custom
}
```

### 4.8 `TableView<R>` — the binding / conversion object (the only thing the UI tier sees)

```java
interface TableView<R> {
    // structure
    List<ColumnView> columns();                 // ordered visible: name,label,width,sortable,
                                                // filterable,sortDir,sortPriority,frozen,editable
    int frozenColumnCount();

    // data window
    int rowCount();
    List<Row<R>> rows(int from, int to);
    CellContent cell(Row<R> row, String column);

    // editing
    FieldModel editor(Object rowKey, String column);   // for inline edit cells

    // commands (UI → model: mutate view-state + re-derive RowSource)
    void sort(SortSpec spec);
    void filter(String column, FilterState state);
    void group(GroupSpec spec);
    void moveColumn(String column, int toIndex);
    void resizeColumn(String column, int width);
    void setColumnVisible(String column, boolean visible);
    void setFrozenColumnCount(int count);
    void setExpanded(Object rowKey, boolean expanded);
    void select(Selection selection);
    void commitEdit(Object rowKey, String column, Object value);
    void window(int page, int pageSize);

    // model → UI (incremental)
    void addListener(TableViewListener l);      // COLUMNS | COUNT | RANGE(from,to) | SELECTION

    // persistence + client seed
    TableViewState state();
}
```

`DefaultTableView<R>` composes `List<Column<R,?>> + RowSource<R> + TableViewState`,
applies commands by mutating the view-state and calling
`RowSource.withOrder/withFilter/withGrouping`, and emits incremental events. The
client JSON protocol is ≈1:1 with `TableViewState` + a `rows()` call.

## 5. Query pushdown

Pushdown keeps the core model data-tier-agnostic via two extension SAMs the
`QueryRowSource` backend interprets:

```java
interface OrderPushdown  { void contribute(QuerySink sink, boolean ascending); }
interface FilterPushdown { void contribute(QuerySink sink); }
```

`QuerySink` is defined by the query backend (TL search expressions / KB query). A
`QueryRowSource` binds columns' pushdowns to its backend; a column lacking a pushdown for
an active sort/filter triggers a **declared fallback** (either: fetch-then-in-memory for
that predicate, or reject with a clear error) — never a silent full materialization.
`size()` is a count query; `window(from,to)` is `OFFSET/LIMIT` (or seek-based). Facet
`matchCounts` is a `GROUP BY` count.

## 6. Filter UI without legacy form code

A column filter is **state + logic + a UI-neutral input descriptor** — no `FormField`,
no `FilterDialogBuilder`, no `Control`:

```java
sealed interface FilterInput {                  // what inputs the dialog needs
    record Text()                       implements FilterInput;   // + flags: caseSensitive, regexp, wholeField
    record Range<V>(V exampleBound)     implements FilterInput;   // operator + 1–2 values
    record Options(List<Option> values) implements FilterInput;   // multi-select + facet counts
    record Bool()                       implements FilterInput;   // true/false/null tri-state
}
```

`FilterState` is the serializable chosen criteria. The **React tier renders the descriptor
natively** by building `AbstractFieldModel`s (which the existing React form controls —
`ReactTextInputControl`, `ReactSelectFormFieldControl`, `ReactCheckboxControl`,
`ReactNumberInputControl`, `ReactDatePickerControl` — already bind to) and writes the
result back into `FilterState`. Apply/Reset mirror legacy semantics: edit transient field
models, commit into `FilterState` on Apply, then `TableView.filter(col, state)` re-derives
the `RowSource`. The legacy popup keeps working independently on the old stack; the two
renderers never share view code, only `FilterState`.

## 7. Capability coverage (vs. legacy survey)

| Legacy capability | Green-field home | Note |
|---|---|---|
| Row data / materialization | `RowSource.size/window` | **+lazy/DB windowing** |
| Accessor / cell value | `Column.value(R):V` | typed |
| Cell rendering | `CellRenderer<V>` → `CellContent` | React adapts |
| Sort: multi / custom / sort-key | `SortSpec` + `Column.sort` | comparator or pushdown |
| Filter: per-column / global / counts / existence | `Column.filter` + `FilterState`; `RowSource.matchCounts`; `Column.existence` | dialog rebuilt in React |
| Sidebar filter | same descriptors, different placement | pure UI concern |
| Selection single / **multi / range** | `Selection` in view-state | **first-class** |
| Reorder / resize / show-hide / frozen | view-state fields + commands | |
| Tree: expand/collapse, lazy, synthetic, filter parents/children | `TreeRowSource` + `TreeStructure` | unified with flat |
| Inline editing | `Column.editor` (`FieldModel` + commit) | replaces `FormTableModel` |
| **Grouping / aggregation / footer** | `RowKind` synthetic rows + `Aggregator` + `GroupSpec` | **modeled, not UI-only** |
| Personalization | `TableViewState` + `ViewStateStore` | one port |
| Export (Excel/PDF) | iterate `Column.value` over `RowSource.window` | no UI tier |
| Pagination | a `rows()` window | same API as virtual scroll |

Export and print consume `Column` + `RowSource` directly (no UI), so they get
lazy/windowed data and grouping for free.

## 8. Dependency boundary

**Allowed (fits well):** JDK (`Comparator`/`Predicate`/`Function`); `ResKey`/i18n;
`FieldModel`/`AbstractFieldModel`; new neutral `CellRenderer`/`LabelProvider`-style SAMs;
KB/search query API *inside `QueryRowSource` only*; `ReactControl`/`ReactContext` *inside
the `.react` adapter only*.

**Forbidden in the model tier:** `TableModel`, `TableViewModel`, `ObjectTableModel`,
`TreeTableModel`, `Accessor`, `TableConfiguration`/`ColumnConfiguration`, `PagingModel`,
`FormContext`/`FormField`/`FormGroup`, `Control`/`DisplayContext`,
`FilterDialogBuilder`/`ConfiguredFilter`, direct `PersonalConfiguration`.

Enforced by package discipline + an ArchUnit-style test asserting the `com.top_logic.table`
tree imports none of the forbidden packages.

## 9. Migration path (incremental, no big bang)

1. **Define the SPIs** in `com.top_logic.table.*` (compile-only; no legacy imports).
2. **`LegacyTableView implements TableView<Object>`** wrapping a legacy
   `TableModel`/`TreeTableModel`. Port `ReactTableControl` → `ReactTableControl2` onto the
   new `TableView` SPI *while still backed by legacy data*. Nothing visible changes; the UI
   tier becomes legacy-free first.
3. **`ListRowSource` + `TableViewState` + `DefaultTableView`** native; move demo tables
   over. Verify in `com.top_logic.demo` with Playwright.
4. **Filter redesign**: `ColumnFilter` + `FilterInput` + the React filter renderer
   (§6); column filtering lands in the React table.
5. **`QueryRowSource` (pushdown)** and **`TreeRowSource` (unification + grouping)**; retire
   `ObjectTableModel`/`TreeTableModel` per table as they migrate.
6. **Personalization** via `ViewStateStore`; **export** over the new SPI.

Each step is independently shippable and keeps legacy tables working untouched.

## 10. The React adapter (`com.top_logic.table.react`)

`ReactTableControl2(TableView<?> view, CellControlFactory raw)`:
- `columns` state ← `view.columns()`; `totalRowCount` ← `view.rowCount()`.
- `scroll(start,count)` → `view.rows(...)` → row JSON `{key, depth?, expandable?, expanded?,
  kind, selected, cells}`.
- `cells[col]` ← adapter `CellContent → ReactControl` (text/icon/label/editable-FieldModel/raw).
- commands `sort/select/selectAll/columnResize/columnReorder/expand/setFrozenColumnCount`
  → corresponding `TableView` commands (same names as today; near drop-in).
- new commands: `filter(col,state)`, `group(spec)`, `setColumnVisible`, `commitEdit`.
- `TableViewListener` → SSE patches (range invalidation, count, columns, selection).

`TLTableView.tsx` needs only additive changes: a column-header filter affordance + filter
popup (composed from existing `TL*` inputs), a group/aggregate row style keyed on
`row.kind`, and a column show/hide menu. Virtual scroll, multi-sort, selection, resize,
reorder, freeze, and tree indent/expand are already present.

## 11. Open questions

- **Package name.** `com.top_logic.table` vs `com.top_logic.layout.table.v2` vs a new
  module. A new module can't fully wall off legacy (FieldModel lives in tl-core), so the
  boundary is discipline + ArchUnit regardless.
- **`Row<R>` wrapper cost.** Wrapping every data row in a `Row<R>` adds an allocation per
  windowed row. Acceptable for windowed sizes; confirm under virtual-scroll churn.
- **Neutral renderer casts (decision 2).** Track whether `CellContent` forces awkward
  casts in real columns; if so, reconsider a thin React-typed renderer.
- **Transactions on `commit`.** `CellEditor.commit` assumes an ambient transaction owned by
  the caller (matches the view-layer rule that value listeners open their own
  `beginTransaction()`); confirm where the React edit command opens it.
- **`matchCounts` cost under pushdown.** Facet counts as `GROUP BY` per filtered column can
  be expensive; may need a debounce / opt-out per column.

## 12. Implementation status (updated 2026-06-17)

Branch `CWS/CWS_29108_react_table_config` (from `CWS/CWS_29108_integration`), ~20 commits.

**Where the code lives**
- `com.top_logic` (tl-core): SPI `com.top_logic.table.*`, impls `com.top_logic.table.impl.*`,
  filters + editors `com.top_logic.table.filter.*`; unit tests `test.com.top_logic.table.*`.
- `com.top_logic.layout.react`: `control.table.TableViewControl`, `CellContentReactAdapter`,
  client `react-src/controls/TLTableView.tsx` (funnel + filter popup), `js.table.*` i18n.
- `com.top_logic.layout.view`: `element.TableViewElement` (the `<table-view>` element).
- `com.top_logic.demo`: `react.DemoTableViewComponent` (legacy-compat `.layout.xml` demo) +
  `views/demo/table-view-demo.view.xml` (new view-layer demo).

**Done and unit-tested** (≈38 POJO tests, no service startup):
- Full SPI (interfaces + value types).
- `ListRowSource` — flat sort + filter, single-level grouping with subtotals, facet counts.
- `TreeRowSource` — expansion, sibling sort, ancestor-keeping filter (flat/tree unified).
- `DefaultColumn` builder, `DefaultTableView` binding, shared `ColumnLogic`.
- Filter library (text / comparable-range / options / boolean) + `FilterEditor` SPI,
  per-kind editors, `FilterEditors` dispatcher.

**Done and verified live** (Playwright, new view layer at `/tl-demo/view/table-view`):
- Render, multi-column sort, selection → channel → detail form.
- Localized column labels (resolved from model attributes).
- Column-filter popup: funnel → FieldModel mini-form (localized) → apply → rows filter →
  reopen seeded from current state → clear → restore; active-funnel indicator.
- Cell-tooltip 404 fixed.

**Decisions locked:** generics; neutral `CellRenderer`/`CellContent` (no casts forced so
far); grouping first-class (single-level); pushdown backend = TL search expressions
(not yet built); filter UI = Option B (server-composed `FieldModel` mini-form).

## 13. Open tasks / issues / bugs

### A. Not yet implemented (model tier)
- **`QueryRowSource` (pushdown).** Backend chosen (TL search expressions) but not built;
  `QuerySink`/`OrderPushdown`/`FilterPushdown` are inert stubs — every source is in-memory.
  **Split out to its own issue #29341** (feature + design options spectrum); deliberately deferred
  from the React transition and to be designed after a spike confirming what TL search actually
  pushes to SQL.
- **Multi-column grouping** — `ListRowSource.withGrouping` throws for >1 column.
- **Tree + grouping** — `TreeRowSource.withGrouping` throws for any grouping.
- **`LegacyTableView` adapter** (migration step 2) — skipped; existing legacy `TableModel`
  tables are *not* routed through the new control yet.
- ~~**`ViewStateStore` + JSON (de)serialization**~~ — **DONE (2026-06-18).** `TableViewStateCodec`
  serializes the value-only subset (column order, widths, frozen count, sort, grouping) to a JSON
  value model; `PersonalConfigViewStateStore` persists it under a per-table key through
  `PersonalConfiguration`. `DefaultTableView` restores on construction (reconciling stale/new
  columns) and saves after each layout command; `TableViewElement` supplies a stable `TableId` from
  the table's structural signature. Live-verified: sort survives page reload (asc→desc round-trips
  through `PersonalConfiguration`); 4 codec round-trip unit tests. **Not** persisted: filters,
  expansion, selection (their state carries arbitrary business-object values needing an identity
  serialization strategy — follow-up).
- **Export** over `Column` + `RowSource` — not built.
- **Inline editing** — `CellEditor` SPI exists, but `CellContentReactAdapter` renders
  `CellContent.Editable` read-only (value as text); `TableView.commitEdit` end-to-end
  (incl. transaction ownership) is unimplemented.

### B. View-layer `<table-view>` (`TableViewElement`) gaps
- **Type→column dispatch is now an extensible registry (2026-06-19).** The former
  `instanceof`/`switch(Kind)` cascade was replaced by `ColumnProviderService` (a
  `FieldControlService`-style `TLType`→`ColumnProvider` registry; built-in kind logic is the
  overridable default). `TableViewElement` holds no type logic. See
  [`2026-06-18-extensible-column-providers.md`](./2026-06-18-extensible-column-providers.md).
- ~~Assigns a **text** filter to every attribute~~ / ~~sorts by **label string**~~ — **DONE
  (2026-06-18).** `TableViewElement.buildColumn` now derives filter + comparator from the model
  attribute type: enumeration → options, `INT`/`FLOAT` → numeric range, `DATE` → date range,
  `BOOLEAN`/`TRISTATE` → boolean, `STRING` → text; references / multi-valued / unresolved parts
  fall back to a display-label text filter. Live-verified: boolean filter on `Person#admin`
  (real data, filters to the matching row); all six typed columns of `DemoTypes:A` render their
  funnels without error. Numeric/date/options *click-through* not yet exercised live (no
  populated demo type combines those attributes with rows — see §13.D).
- ~~**Static rows only** — no refresh on model change~~ — **DONE (2026-06-18).** `TableViewElement`
  now takes `observed-types`; a `RowSourceObserver` (green-field analog of `ObservableTableModel`)
  listens to model + input-channel changes and re-runs the rows query into the
  `ListRowSource` (`setElements`), then calls `TableViewControl.refreshData()`. Live-verified on the
  `DemoTypes:A` demo: a "Generate samples" command creates rows and the table auto-refreshes; a
  Delete command removes them.
- ~~Requires explicit `<column>`s~~ — **DONE (2026-06-18).** When `<columns>` is omitted and the
  row type resolves, the default column set is derived from the type's non-hidden
  `getAllParts()` (each via the type-aware `buildColumn`). Live-verified: a no-`<columns>`
  `Person` table renders 16 derived, filterable, localized columns (hidden `mfaSecret` excluded).

### C. React control gaps / polish
- ~~**Facet counts** count over all backing rows~~ — **DONE (2026-06-18).** `ListRowSource.matchCounts`
  now counts over rows passing every *other* active filter, excluding the column's own
  (`ColumnLogic.predicate(filter, byName, excludeColumn)`); unit-tested. (Tree sources still use the
  `MatchCounts.NONE` default — no facets for trees yet.)
- **No `TableViewListener` registration** in `TableViewControl` → external model changes
  don't refresh the control (only command-driven rebuilds). OK for static demos, not live data.
- **Typed value inputs**: the filter value field is a generic text input + the filter's
  parser (so a new datatype needs only a parser, no UI plugin). For *typed* widgets (number
  spinner, date picker) in the view layer, route the value field through
  `com.top_logic.layout.view.form.FieldControlService.createFieldControl(part, model)` — the
  same datatype→control registry forms use. Not yet wired (would need the column's
  `TLStructuredTypePart` threaded to the filter field).
- **Selection** not persisted; no `ChannelVetoException`/veto handling like the legacy
  `ReactTableControl`.

> **Filter UI (resolved 2026-06-17).** The filter dialog is now built from reused controls
> — `ReactWindowControl` (window chrome), `ReactFormBuilder`/`ReactFormFieldChromeControl`
> (labeled fields), `MessageButtons`/`ReactButtonControl` (footer) — opened via
> `DialogManager`. The hand-rolled popup/buttons/labels in `TLTableView.tsx`, the
> `FilterFieldKind` enum + `createFieldControl` switch, and the apply/clear/close commands
> were deleted. The input control per field is chosen from the `FieldModel` itself
> (`SelectFieldModel`→dropdown, boolean→checkbox, else text) — one generic rule, no
> per-datatype branch.

### D. Verification gaps
- **Options / range / boolean** filter kinds are unit-tested and wired into the legacy-compat
  employees demo tab, but **not clicked through live** in Playwright (only text was).
- No scripted/integration test for `TableViewControl` or `TableViewElement` (model tier has
  POJO unit tests only).

### E. Known environment issue (not this work)
- Workspace hit a stale-jar `IncompatibleClassChangeError`
  (`SearchExpression.asString` non-static → static) in `tl-service-openapi-server`, blocking
  app start. Fix: `mvn clean install -pl com.top_logic.service.openapi.server`.
  `rebuild-stale.sh` does **not** catch cross-module API drift (only a module's own sources).

### F. Naming / structure (still-open §11 forks)
- Package `com.top_logic.table` is provisional (module-vs-package undecided); **no ArchUnit
  boundary test** yet enforcing the dependency rule in §8.
- `Row<R>` per-window allocation cost not benchmarked.
- `i18n` for the client `js.table.*` keys is in `react` `I18NConstants`; German "apply" was
  hand-corrected to "Übernehmen" (DeepL gave "Bewerben").

## 14. Suggested next steps (priority order)
1. ~~Attribute-type-aware filter + comparator selection in `TableViewElement`~~ — **DONE
   2026-06-18** (§13.B). Remaining sub-task: live click-through of options/range/date once a
   demo type with those attributes *and* populated rows exists (or a seeded dataset is added).
2. `LegacyTableView` adapter — route existing `TableModel` tables through the new control.
3. `QueryRowSource` over TL search expressions (the lazy/pushdown win).
4. ~~`ViewStateStore` + serialization (personalization).~~ — **DONE 2026-06-18** (§13.A). Layout
   subset persisted; filter/expansion/selection persistence is the follow-up.
5. Inline editing end-to-end; live-data refresh (`TableViewListener` + observable rows).

**Done since (correctness, no new decisions, 2026-06-18):** facet counts that exclude a column's
own filter (§13.C) ✓; type-derived default columns (§13.B) ✓.

Remaining big items: `QueryRowSource` pushdown — **moved to its own issue #29341** (TL apps deal
with large datasets, but pushdown is a separate concern from the React transition); `LegacyTableView`
adapter (§14.2, re-introduces legacy coupling, parked); inline editing + live-data refresh (§14.5).
Smaller remaining: live refresh on model change (§13.B), typed value widgets via
`FieldControlService` (§13.C), selection persistence + veto (§13.C), filter/expansion persistence
(§13.A follow-up), multi-column & tree grouping (§13.A).
