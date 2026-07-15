# Dashboard Layout Widget Design

## Summary

A new `<dashboard>` view element that arranges content widgets in a flexible
column/row grid. Widgets can span multiple columns (via relative width like
`half`, `third`) and multiple rows. Users can reorder widgets at runtime via
drag-and-drop in an edit mode. User-specific ordering is persisted in
PersonalConfiguration.

## Motivation

The current `<grid>` element only supports equal-sized columns with
`auto-fit`. There is no way to have widgets span multiple columns or rows,
which forces complex nesting of `<stack>` and `<grid>` for dashboard-style
layouts (see `chart-demo.view.xml`). Adding or rearranging tiles requires
editing the view XML.

## XML Configuration Model

### `<dashboard>` element

Container for `<dashboard-item>` children. Defines the grid parameters.

| Attribute | Type | Default | Description |
|-----------|------|---------|-------------|
| `min-column-width` | String (CSS length) | `"16rem"` | Minimum column width for responsive auto-fit |
| `row-height` | String (CSS length) | `"12rem"` | Height of one grid row |
| `gap` | `compact` \| `default` \| `loose` | `default` | Gap between items |

### `<dashboard-item>` element

Layout slot that defines size/position. Contains arbitrary child elements
(cards, charts, tables, raw content).

| Attribute | Type | Default | Description |
|-----------|------|---------|-------------|
| `id` | String | (mandatory) | Unique identifier for persistence |
| `width` | `full` \| `two-thirds` \| `half` \| `third` \| `quarter` | `full` | Relative width of the item |
| `row-span` | int | `1` | Number of grid rows to span |

### Example

```xml
<dashboard min-column-width="16rem" row-height="12rem" gap="default">
    <dashboard-item id="sales-chart" width="half" row-span="2">
        <card>
            <title><en>Sales Overview</en></title>
            <chart data="..."/>
        </card>
    </dashboard-item>
    <dashboard-item id="kpi-tasks" width="quarter">
        <card>
            <title><en>Active Tasks</en></title>
            <demo-counter/>
        </card>
    </dashboard-item>
    <dashboard-item id="kpi-issues" width="quarter">
        <card>
            <title><en>Open Issues</en></title>
            <demo-counter/>
        </card>
    </dashboard-item>
    <dashboard-item id="timeline" width="full">
        <card>
            <title><en>Timeline</en></title>
            <chart data="..."/>
        </card>
    </dashboard-item>
</dashboard>
```

## Architecture

### Java (Server)

**Module `com.top_logic.layout.view`:**

- `DashboardElement` -- extends `AbstractUIElement` (NOT `ContainerElement`,
  since children are restricted to `DashboardItemElement`).
  - Config extends `UIElement.Config` with `@TagName("dashboard")`.
  - Typed list property `List<DashboardItemElement.Config> getItems()` keyed
    by `id` -- only `DashboardItemElement` children are allowed.
  - Config properties: `min-column-width`, `row-height`, `gap`.
  - `createControl()` instantiates each item, collects child controls,
    reads personal order from `PersonalConfiguration`, reorders items,
    creates `ReactDashboardControl`.

- `DashboardItemElement` -- extends `ContainerElement`.
  - Config defines `@TagName("dashboard-item")`.
  - Config properties: `id` (mandatory String), `width` (enum), `row-span` (int).
  - Contains arbitrary child UIElements.

- `DashboardWidth` -- enum with values `FULL`, `TWO_THIRDS`, `HALF`, `THIRD`,
  `QUARTER`. Each value has an `externalName` (for XML/JSON) and a `fraction`
  (double).

**Module `com.top_logic.layout.react`:**

- `ReactDashboardControl` -- new ReactControl.
  - React module: `"TLDashboard"`.
  - State: `minColumnWidth`, `rowHeight`, `gap`, `items` (array of
    `{id, width, rowSpan, children}`).
  - Handles `reorder` command: receives new ID ordering, persists to
    PersonalConfiguration, reorders internal item list.

### React (Client)

**`TLDashboard.tsx`** in `com.top_logic.layout.react/react-src/controls/`:

- Renders a CSS Grid container with `grid-auto-rows: <rowHeight>`.
- Does NOT use CSS Grid auto-placement (`auto-fit`, `auto-flow`).
  Instead, a **JS layout engine** calculates explicit `grid-column` and
  `grid-row` positions for every item. This is necessary because CSS
  Grid auto-placement cannot stretch items to fill remaining row space.

**Layout algorithm (runs on every resize via `ResizeObserver`):**

1. Determine column count: `Math.max(1, Math.floor((width + gap) / (minColWidth + gap)))`.
2. Calculate each item's span: `Math.round(fraction * columnCount)`,
   clamped to `[1, columnCount]`.
3. Pack items into rows left-to-right:
   - Track occupied cells in a 2D grid (for row-spanning items).
   - If the next item doesn't fit in the current row, **stretch the last
     item in that row to fill remaining columns**, then start a new row.
   - Row-spanning items reserve their columns in subsequent rows.
4. After the last item, stretch it to fill its row.
5. Assign explicit `grid-column: <start> / <end>` and
   `grid-row: <start> / <end>` (1-based CSS Grid line numbers).

This guarantees:
- **No gaps**: every row is completely filled.
- **Row-span works**: items can span multiple rows, and the packing
  algorithm accounts for reserved cells.
- **Responsive**: on resize, the column count changes and the layout
  is fully recalculated.

At 1 column (mobile), `row-span` is reset to 1 to prevent excessively
tall pages.

**Edit mode:**

- Toggle button (pencil icon) at top-right of the dashboard.
- Edit mode is client-side state only (no server roundtrip to toggle).
- In edit mode:
  - Each item shows a drag handle (grip icon at top).
  - HTML5 Drag API: `dragstart`, `dragover`, `drop`, `dragend`.
  - Dragged item becomes semi-transparent; drop target shows a dashed
    placeholder.
  - On drop: item order is updated in the DOM, layout is recalculated,
    server command `reorder` is called with new ID list.
- In normal mode: no drag handles visible, no drag interaction.

### Persistence

- Item order is stored as a list of IDs in `PersonalConfiguration`, keyed by
  the dashboard's component name.
- On load: server reads personal config, reorders items accordingly.
  Missing IDs (new items added to XML) are appended at the end.
  Extra IDs (items removed from XML) are ignored.
- No personal config entry = default order from view XML.

## Scope

### First iteration (this spec)

- `<dashboard>` and `<dashboard-item>` elements with CSS Grid rendering
- Responsive column layout with relative widths and row-span
- Edit mode with drag-and-drop reordering
- Persistence of user-specific order in PersonalConfiguration
- Demo page in `com.top_logic.demo`

### Future iterations (out of scope)

- Adding widgets from a palette
- Removing widgets
- Resizing widgets at runtime (changing width/row-span)
- Model-based dashboard configuration (Knowledge Base)

## File Locations

| File | Module |
|------|--------|
| `DashboardElement.java` | `com.top_logic.layout.view` |
| `DashboardItemElement.java` | `com.top_logic.layout.view` |
| `DashboardWidth.java` | `com.top_logic.layout.view` |
| `ReactDashboardControl.java` | `com.top_logic.layout.react` |
| `TLDashboard.tsx` | `com.top_logic.layout.react/react-src/controls/` |
| `TLDashboard.css` | `com.top_logic.layout.react/react-src/controls/` |
| `dashboard-demo.view.xml` | `com.top_logic.demo` |
