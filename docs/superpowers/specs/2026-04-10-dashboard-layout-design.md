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

- Renders a CSS Grid container.
- Does NOT use `auto-fit` -- `auto-fit` and explicit column spans conflict
  (spanning items create implicit extra columns). Instead, the column count
  is calculated in JavaScript: `Math.max(1, Math.floor((width + gap) / (minColWidth + gap)))`,
  then snapped down to a "nice" column count from `[1, 2, 3, 4, 6]` where
  all width fractions (1/2, 1/3, 1/4, 2/3) produce clean integer spans.
  This prevents gaps where items don't fill a row.
- Sets `grid-template-columns: repeat(N, 1fr)` explicitly based on
  calculated column count.
- Sets `grid-auto-rows: <rowHeight>`.
- Sets `grid-auto-flow: dense` so that smaller items fill gaps left by
  spanning items -- the grid always fills completely without holes.
- Uses `ResizeObserver` to recalculate on container resize.
- Sets `style.gridColumn` and `style.gridRow` directly on each item element.
- Maps each item's `width` to a `grid-column: span N` value based on current
  column count (see mapping table below).
- Sets `grid-row: span <rowSpan>` for each item.

**Edit mode:**

- Toggle button (pencil icon) at top-right of the dashboard.
- Edit mode is client-side state only (no server roundtrip to toggle).
- In edit mode:
  - Each item shows a drag handle (grip icon at top).
  - HTML5 Drag API: `dragstart`, `dragover`, `drop`, `dragend`.
  - Dragged item becomes semi-transparent; drop target shows a dashed
    placeholder.
  - On drop: item order array is updated, React re-renders, server command
    `reorder` is called with new ID list.
- In normal mode: no drag handles visible, no drag interaction.

### Width-to-Span Mapping

The `width` fraction is mapped to a column span based on the current number
of columns (determined by `ResizeObserver`):

```
span = Math.max(1, Math.round(fraction * totalColumns))
span = Math.min(span, totalColumns)  // clamp to available columns
```

Column count is restricted to `[1, 2, 3, 4, 6]` (divisors of 12 up to 6)
so that all fractions always produce clean integer spans with no gaps:

| `width` | fraction | 6 cols | 4 cols | 3 cols | 2 cols | 1 col |
|---------|----------|--------|--------|--------|--------|-------|
| `full` | 1.0 | 6 | 4 | 3 | 2 | 1 |
| `two-thirds` | 0.667 | 4 | 3 | 2 | 1 | 1 |
| `half` | 0.5 | 3 | 2 | 2 | 1 | 1 |
| `third` | 0.333 | 2 | 1 | 1 | 1 | 1 |
| `quarter` | 0.25 | 2 | 1 | 1 | 1 | 1 |

At 1 column (mobile), `row-span` is reset to 1 to prevent excessively tall
pages.

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
