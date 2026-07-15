# TableElement Design

## Overview

Add a declarative `<table>` UIElement to the view system that wraps `ReactTableControl`.
The element replaces the legacy `LayoutComponent`+`ListModelBuilder` pattern by using
ViewChannels as input sources and TL-Script expressions for data derivation.

## Configuration

`TableElement` implements `UIElement` with `@TagName("table")`.

### Config Properties

| Property          | Type                                        | Required | Description                                                    |
|-------------------|---------------------------------------------|----------|----------------------------------------------------------------|
| `inputs`          | `List<ChannelRef>`                          | no       | ViewChannels whose values become positional args to expressions |
| `rows`            | `Expr`                                      | yes      | TL-Script function returning `Collection<?>` of row objects     |
| `supportsElement` | `Expr`                                      | no       | `(input0, ..., inputN, candidate) -> boolean/ElementUpdate`     |
| `modelForElement` | `Expr`                                      | no       | `(input0, ..., inputN, candidate) -> Object`                    |
| `columns`         | `PolymorphicConfiguration<TableConfigurationProvider>` | no | Reuses existing column infrastructure                          |
| `selection`       | `ChannelRef`                                | no       | Channel to write selected row object(s) to                     |

### Example XML

```xml
<table selection="selectedCustomer">
  <inputs>
    <channel-ref name="currentOrganization" />
    <channel-ref name="currentDivision" />
  </inputs>
  <rows>
    orga -> division ->
      $orga.get(`tl.customers:Organization#customers`)
           .filter(c -> $c.get(`tl.customers:Customer#division`) == $division)
  </rows>
  <columns class="...GenericTableConfigurationProvider"
           type="tl.customers:Customer" />
</table>
```

## Expression Argument Binding

The `inputs` list defines positional arguments to all expressions:

- **`rows`**: `(input0, input1, ..., inputN) -> Collection<?>`
- **`supportsElement`**: `(input0, input1, ..., inputN, candidate) -> boolean | ElementUpdate`
- **`modelForElement`**: `(input0, input1, ..., inputN, candidate) -> Object`

Input channel values always come first, followed by any element-specific parameters
(the candidate object for `supportsElement` and `modelForElement`).

At execution time:
```java
Object[] channelValues = inputChannels.stream().map(ViewChannel::get).toArray();

// rows: just channel values
Collection<?> rows = (Collection<?>) _rowsExecutor.execute(channelValues);

// supportsElement: channel values + candidate appended
Object[] args = ArrayUtils.add(channelValues, candidate);
ElementUpdate update = toElementUpdate(_supportsElementExecutor.execute(args));
```

## Runtime Lifecycle

### Constructor (shared, stateless)

`TableElement(InstantiationContext context, Config config)`:

1. Compile `rows` Expr into `QueryExecutor _rowsExecutor` (mandatory).
2. Compile `supportsElement` Expr into `QueryExecutor _supportsElementExecutor` (optional).
3. Compile `modelForElement` Expr into `QueryExecutor _modelForElementExecutor` (optional).
4. Store `ChannelRef` list and column config for later use.

QueryExecutors are compiled once and shared across all sessions (they are stateless).

### createControl(ViewContext) (per-session)

1. **Resolve input channels**: For each `ChannelRef` in `inputs`, call
   `context.resolveChannel(ref)` to get `List<ViewChannel>`.
2. **Execute initial query**: Read current values from all input channels, pass as
   positional args to `_rowsExecutor.execute(...)` to get `Collection<?>`.
3. **Build TableConfiguration**: Instantiate the configured `TableConfigurationProvider`,
   call `getTableConfiguration()`.
4. **Create ObjectTableModel**: From the row collection + `TableConfiguration`.
5. **Create ReactCellControlProvider**: Derive from column configuration (cell renderers).
6. **Create ReactTableControl**: Pass `ObjectTableModel` + cell provider.
7. **Wire selection channel** (if configured): Listen to `ReactTableControl` selection
   changes. On change, write selected object to the resolved selection channel.
8. **Wire input channel listeners**: Register `ChannelListener` on each input channel.
   On any input change:
   - Re-read all channel values
   - Re-execute `rows` expression
   - Replace rows in `ObjectTableModel`
   - `ReactTableControl` rebuilds viewport automatically via `TableModelListener`

## Selection Channel

When `selection` is configured:

- Resolve the `ChannelRef` to a `ViewChannel` via `ViewContext.resolveChannel()`.
- Listen to row selection changes in `ReactTableControl`.
- Single selection: write the selected row object (or `null` if deselected).
- The selection channel is output-only from the table's perspective.

## Column Configuration

Reuses the existing `TableConfigurationProvider` / `TableConfiguration` infrastructure.
This provides:

- Column names and labels
- Accessors (how to extract cell values from row objects)
- Comparators (sorting)
- Cell renderers / ResourceProviders
- Filter providers
- Column widths, frozen columns, etc.

The `columns` property accepts a `PolymorphicConfiguration<TableConfigurationProvider>`,
so users can use `GenericTableConfigurationProvider` (auto-derives from TL model type),
`ComponentTableConfigProvider`, or custom implementations.

## Module Placement

- **Package**: `com.top_logic.layout.view.element`
- **Module**: `com.top_logic.layout.view` (artifact `tl-layout-view`)
- **New dependency**: `com.top_logic.model.search` (for `QueryExecutor`, `Expr`)

## Relationship to Legacy System

| Legacy                              | New (TableElement)                      |
|-------------------------------------|-----------------------------------------|
| `ListModelBuilder.getModel(bm, lc)` | `_rowsExecutor.execute(channelValues)`  |
| `supportsListElement(lc, candidate)` | `_supportsElementExecutor.execute(channelValues..., candidate)` |
| `retrieveModelFromListElement(lc, c)`| `_modelForElementExecutor.execute(channelValues..., candidate)` |
| `LayoutComponent.getModel()`        | `ViewChannel.get()` (per input channel) |
| `TableComponent` orchestration      | `TableElement.createControl()` wiring   |
| `TableConfigurationProvider`         | Reused as-is via `columns` config       |
