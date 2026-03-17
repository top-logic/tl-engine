# React Chart.js Integration

## Context

TopLogic has an existing Chart.js integration (`com.top_logic.chart.chartjs`) that renders charts via inline JavaScript in a traditional `AbstractVisibleControl`. The new React UI layer (`com.top_logic.layout.view` + `com.top_logic.layout.react`) provides a modern component model with SSE-based state updates, declarative view configuration, and reactive channels.

This design introduces a React-based chart control as a new `UIElement` in a dedicated module, enabling interactive Chart.js charts within the view-based React UI layer.

## Goals

- All Chart.js chart types (bar, line, pie, doughnut, radar, scatter, bubble, polar area, combined)
- TL-Script-based data sourcing (produces Chart.js configuration as JSON)
- Click handlers on data points and legend entries (goto or custom command)
- Server-side tooltip providers (TL-Script evaluates metadata to produce HTML)
- Zoom/pan support
- Live updates via channel-based reactivity and SSE
- Theme integration (default palette and colors from TopLogic theme)
- Demo view in `com.top_logic.demo` for testing

## Non-Goals

- Dedicated drill-down mechanism (handled externally via click handlers + navigation)
- Typed configuration model for chart options (pass-through approach chosen for flexibility)
- Replacement of the existing `com.top_logic.chart.chartjs` module (coexists)

## Module Structure

### New Maven Module: `com.top_logic.layout.react.chartjs`

- **Artifact:** `tl-layout-react-chartjs`
- **Parent:** `tl-parent-core-internal`
- **Dependencies:**
  - `com.top_logic.layout.react` (ReactControl, Bridge, SSE)
  - `com.top_logic.layout.view` (UIElement, ViewContext, Channels)
  - `com.top_logic` / tl-core (TL-Script, TLObject, Theme)
  - `tl-model-search` (QueryExecutor, Expr)
- **No dependency** on `ext.org.chartjs` or `com.top_logic.chart.chartjs` — Chart.js comes as npm package

### npm Dependencies

```json
{
  "dependencies": {
    "chart.js": "^4.4",
    "react-chartjs-2": "^5.2",
    "chartjs-plugin-zoom": "^2.0"
  },
  "devDependencies": {
    "typescript": "^5.7",
    "vite": "^6.0",
    "@vitejs/plugin-react": "^4.3"
  }
}
```

### Vite Build

- Entry: `react-src/chartjs-entry.ts`
- Output: `src/main/webapp/script/tl-react-chartjs.js`
- Externalizes `tl-react-bridge` (no duplicate React!)
- Chart.js is bundled (self-contained module)
- All chart types registered: `ChartJS.register(...registerables)`

### JS Bundle Registration

`WEB-INF/conf/tl-layout-react-chartjs.conf.config.xml`:

```xml
<config service-class="com.top_logic.gui.JSFileCompiler">
    <instance>
        <additional-files>
            <file resource="tl-react-chartjs.js" type="module" />
        </additional-files>
    </instance>
</config>
```

Load order in browser:
1. `tl-react-bridge.js` (importmap + React + Bridge APIs)
2. `tl-react-controls.js` (standard controls)
3. `tl-react-chartjs.js` (chart control)

## Java Architecture

### ChartElement (UIElement)

Stateless factory, configured in `.view.xml`, creates a `ReactChartJsControl` per session.

```java
@TagName("chart")
public interface Config extends UIElement.Config {

    /** Input channels for the model objects (arguments to the TL-Script function). */
    @Name("inputs")
    @ListBinding(format = ChannelRefFormat.class, tag = "input", attribute = "channel")
    List<ChannelRef> getInputs();

    /** TL-Script expression producing Chart.js config map (type, data, options).
     *  Receives as many arguments as input channels are declared. */
    @Mandatory
    @Name("data")
    Expr getData();

    /** Named click handlers. */
    @Name("handlers")
    @EntryTag("handler")
    List<PolymorphicConfiguration<? extends OnClickHandler>> getHandlers();

    /** Named tooltip providers. */
    @Name("tooltips")
    @EntryTag("tooltip")
    List<TooltipProviderConfig> getTooltips();

    /** Whether zoom/pan is enabled. */
    @Name("zoomEnabled")
    @BooleanDefault(false)
    boolean getZoomEnabled();

    /** CSS class for the chart container. */
    @Name("cssClass")
    @Nullable
    String getCSSClass();

    /** Message shown when data is empty. */
    @Name("noDataMessage")
    @Nullable
    ResKey getNoDataMessage();
}
```

### OnClickHandler Configuration

Polymorphic, two variants:

```java
/** Goto: Navigate to the TL object from data point metadata. */
@TagName("goto")
public interface GotoAction extends PolymorphicConfiguration<OnClickHandler> {
    @Mandatory
    @Name("name")
    String getName();
}

/** Command: Execute an arbitrary ViewCommand. */
@TagName("command")
public interface CommandAction extends PolymorphicConfiguration<OnClickHandler> {
    @Mandatory
    @Name("name")
    String getName();

    @Mandatory
    PolymorphicConfiguration<? extends ViewCommand> getCommand();
}
```

### TooltipProviderConfig

```java
public interface TooltipProviderConfig extends ConfigurationItem {
    /** Name referenced from dataset tooltip key. */
    @Mandatory
    @Name("name")
    String getName();

    /** TL-Script expression: receives the metadata TL object, returns HTML string. */
    @Mandatory
    @Name("expr")
    Expr getExpr();
}
```

### ReactChartJsControl

Extends `ReactControl`. Session-scoped, stateful.

**Responsibilities:**
1. Evaluate TL-Script expression with input channel values as arguments
2. Walk the returned structure, extract `datasets[].metadata` into a server-side lookup table
3. Extract `datasets[].onClick`, `datasets[].onLegendClick`, `datasets[].tooltip` string references into a handler mapping, then remove these keys from the JSON
4. Validate: any TL objects found outside `datasets[].metadata` raise a `TopLogicException` with the path to the invalid object
5. Read theme colors and provide as state
6. Send clean JSON + interaction descriptor to React via SSE
7. Listen to input channels — re-evaluate TL-Script on change, send SSE patch

**State keys sent to React:**

| Key | Type | Description |
|-----|------|-------------|
| `chartConfig` | `object` | Clean Chart.js config (type, data, options) — no TL objects |
| `interactions` | `object` | Per dataset: `{ clickable, legendClickable, hasTooltip }` |
| `themeColors` | `object` | `{ palette, gridColor, textColor, backgroundColor }` |
| `zoomEnabled` | `boolean` | Zoom/pan toggle |
| `cssClass` | `string?` | Additional CSS class |
| `error` | `string?` | Error message (replaces chart with error display) |
| `noDataMessage` | `string?` | Message when no data |
| `tooltipContent` | `object?` | `{ html, x, y }` — server-rendered tooltip |

**React commands handled:**

| Command | Arguments | Behavior |
|---------|-----------|----------|
| `elementClick` | `{ datasetIndex, index, zone }` | Look up handler by (datasetIndex, zone). Resolve TL object from metadata. Execute goto or ViewCommand. |
| `tooltip` | `{ datasetIndex, index }` | Look up tooltip provider by datasetIndex. Evaluate TL-Script with metadata object. Send HTML via SSE patch as `tooltipContent`. |

## Data Flow

### TL-Script Output (with embedded objects)

The TL-Script `data` function returns a Chart.js config that may contain TL objects in `datasets[].metadata` and handler/tooltip references as string keys:

```
{
  type: 'bar',
  data: {
    labels: ['Q1', 'Q2', 'Q3'],
    datasets: [{
      label: 'Revenue',
      data: [10, 50, 30],
      metadata: [$order1, $order2, $order3],
      onClick: 'detail',
      onLegendClick: 'drill',
      tooltip: 'revenue-tip'
    }, {
      label: 'Costs',
      data: [8, 35, 22],
      metadata: [$cost1, $cost2, $cost3],
      onClick: 'detail',
      tooltip: 'default'
    }]
  },
  options: { maintainAspectRatio: false }
}
```

### Java Extraction

1. Walk `datasets[]`, extract `metadata` arrays → server-side 2D lookup: `metadata[datasetIdx][dataIdx] → TLObject`
2. Extract `onClick`, `onLegendClick`, `tooltip` strings → handler mapping: `handlerMap[datasetIdx] → { onClick: "detail", ... }`
3. Validate handler references exist in configured handlers/tooltips — fail early if not
4. Remove `metadata`, `onClick`, `onLegendClick`, `tooltip` keys from datasets
5. Validate remaining structure contains no TL objects — throw `TopLogicException` with path if found
6. Send cleaned config as `chartConfig` state

### Client JSON (after extraction)

```json
{
  "type": "bar",
  "data": {
    "labels": ["Q1", "Q2", "Q3"],
    "datasets": [{
      "label": "Revenue",
      "data": [10, 50, 30]
    }, {
      "label": "Costs",
      "data": [8, 35, 22]
    }]
  },
  "options": { "maintainAspectRatio": false }
}
```

Plus interaction descriptor:
```json
{
  "datasets": [
    { "clickable": true, "legendClickable": true, "hasTooltip": true },
    { "clickable": true, "legendClickable": false, "hasTooltip": true }
  ]
}
```

## React Component: TLChart

Registered as `TLChart` in `tl-react-chartjs.js`.

```typescript
import { React, useTLCommand } from 'tl-react-bridge';
import { Chart } from 'react-chartjs-2';
```

### Callback Injection

Based on `state.interactions`, the component injects Chart.js callbacks:

- **Data point click:** `options.onClick` — sends `elementClick` command with `{ datasetIndex, index, zone: 'datapoint' }`
- **Legend click:** `options.plugins.legend.onClick` — sends `elementClick` with `{ datasetIndex, index: -1, zone: 'legend' }`. Default toggle behavior (show/hide dataset) is preserved alongside the server command.
- **Tooltip:** If any dataset has `hasTooltip: true`, uses Chart.js external tooltip mode. Sends `tooltip` command, renders `state.tooltipContent` HTML when it arrives. Shows loading indicator while waiting. Discards if mouse moves away.

### Theme Integration

Merges `state.themeColors` as defaults into the chart config:
- Default dataset color palette (when no explicit colors set)
- Axis tick/label colors from `textColor`
- Grid line colors from `gridColor`
- Chart background from `backgroundColor`

Explicit colors in the TL-Script config override theme defaults.

### Zoom/Pan

Enabled via `state.zoomEnabled`. Uses `chartjs-plugin-zoom`. A reset button (absolute positioned top-right) resets zoom level.

### Error Display

When `state.error` is set, renders error message instead of the chart canvas.

### No-Data Display

When `state.chartConfig.data.datasets` is empty or has no data points, and `state.noDataMessage` is set, renders the message centered in the chart area.

## Theme

### New Theme Variables

Defined with defaults derived from existing base variables:

| Variable | Default derivation |
|----------|-------------------|
| `--chart-color-1` through `--chart-color-10` | Distinct palette |
| `--chart-grid-color` | `--border-color` |
| `--chart-text-color` | `--text-color` |
| `--chart-background-color` | `--surface-color` |
| `--chart-border-color` | `--border-color` |

### CSS

Minimal CSS in `tl-react-chartjs.css`:

```css
.tlReactChart {
    width: 100%;
    height: 100%;
    position: relative;
}

.tlReactChart__tooltip {
    position: absolute;
    pointer-events: none;
    z-index: 100;
}

.tlReactChart__zoomReset {
    position: absolute;
    top: 0.5rem;
    right: 0.5rem;
}
```

Chart fills its container — size determined by surrounding layout element.

## Error Handling

| Scenario | Behavior |
|----------|----------|
| TL-Script returns invalid config (no `type` or `data` key) | `TopLogicException` with I18N message, chart shows error state |
| TL objects found outside `datasets[].metadata` | `TopLogicException` with path to invalid object |
| Dataset references handler name not configured in view.xml | Error at control creation time (fail early) |
| Empty datasets / no data points | Show `noDataMessage` if configured, otherwise empty chart |
| Tooltip response slow | Loading indicator in tooltip; discarded if mouse moves |
| Channel update while tooltip visible | Tooltip closed, metadata table refreshed |

## Demo View

### New file: `com.top_logic.demo/.../views/demo/chart-demo.view.xml`

A demo view showcasing the chart control with interactive features.

**Layout:** Vertical stack with two rows:
- **Top row:** Two charts in a grid (bar chart + pie chart), both fed from demo data
- **Bottom row:** A line chart showing a time series

**Features demonstrated:**
- Multiple chart types (bar, pie, line)
- Click handler (goto on data point click)
- Legend interaction
- Zoom/pan on the line chart
- Server-side tooltips
- Theme color integration
- Channel-based reactivity (selection channel updates detail area)

**Data source:** TL-Script expressions operating on `DemoTypes` model objects (already available in the demo app).

### Navigation

New sidebar entry in `app.view.xml` after "Input Controls":
- id: `chart-demo`
- icon: `bi bi-bar-chart-line`
- label: `Chart Demo`

### Demo dependency

`com.top_logic.demo/pom.xml` needs a new dependency on `com.top_logic.layout.react.chartjs`.

### Example view.xml sketch

```xml
<view>
  <channels>
    <channel name="selectedObject"/>
  </channels>
  <stack direction="column" gap="default">
    <grid gap="default" min-column-width="24rem">
      <card title="Revenue by Quarter">
        <chart data="${model -> ...bar chart TL-Script...}">
          <inputs>
            <input channel="model"/>
          </inputs>
          <handlers>
            <goto name="detail"/>
          </handlers>
          <tooltips>
            <tooltip name="default" expr="${obj -> $obj.get(`name`)}"/>
          </tooltips>
        </chart>
      </card>
      <card title="Distribution">
        <chart data="${model -> ...pie chart TL-Script...}">
          <inputs>
            <input channel="model"/>
          </inputs>
        </chart>
      </card>
    </grid>
    <card title="Trend">
      <chart data="${model -> ...line chart TL-Script...}" zoomEnabled="true">
        <inputs>
          <input channel="model"/>
        </inputs>
      </chart>
    </card>
  </stack>
</view>
```

## Architecture Overview

```
view.xml: <chart input="model" data="${...}">
              handlers + tooltips config
                     |
                     | parse
                     v
ChartElement (UIElement, stateless, shared)
                     |
                     | createControl(ViewContext)
                     v
ReactChartJsControl (ReactControl, session-scoped)
  - evaluates TL-Script -> raw config with TL objects
  - extracts metadata -> server-side lookup table
  - extracts handler/tooltip refs -> handler mapping
  - validates no TL objects outside metadata
  - sends clean JSON + interactions via SSE
  - listens to channels -> re-evaluate on change
  - handles elementClick -> resolve handler + object -> goto/command
  - handles tooltip -> evaluate TL-Script -> send HTML via SSE
                     |
                     | SSE (state + patches)
                     v
TLChart (React Component)
  - receives chartConfig, interactions, themeColors
  - merges theme defaults
  - injects click/legend/tooltip callbacks
  - renders via react-chartjs-2 <Chart/>
  - zoom/pan via chartjs-plugin-zoom
  - server tooltips: command -> wait -> render HTML
```
