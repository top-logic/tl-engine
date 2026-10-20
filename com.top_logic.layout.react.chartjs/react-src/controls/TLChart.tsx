// react-src/controls/TLChart.tsx
import { React, rootClassName, useI18N, usePart } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import {
  Chart as ChartJS,
  registerables,
} from 'chart.js';
import { Chart } from 'react-chartjs-2';
import zoomPlugin from 'chartjs-plugin-zoom';
import { useThemeDefaults } from './chart/useThemeDefaults';
import { useChartCallbacks } from './chart/useChartCallbacks';
import ChartTooltip from './chart/ChartTooltip';

// Register all chart types + zoom plugin.
ChartJS.register(...registerables, zoomPlugin);

const I18N_KEYS = {
  'js.chart.label': 'Chart',
  'js.chart.resetZoom': 'Reset zoom',
};

/**
 * React component that renders a Chart.js chart driven by server-side state.
 *
 * State from server:
 *  - chartConfig: any        - Clean Chart.js config (type, data, options)
 *  - interactions: any       - Interaction descriptor per dataset
 *  - zoomEnabled: boolean    - Whether zoom/pan is enabled
 *  - error: string | null    - Error message to display instead of chart
 *  - noDataMessage: string | null - Message when no datasets
 *  - tooltipContent: any     - Server-provided tooltip HTML + position info
 */
const TLChart: React.FC<TLCellProps> = ({ controlId, state }) => {
  const chartRef = React.useRef<ChartJS | null>(null);

  const chartConfig = state.chartConfig as any;
  const interactions = state.interactions as any;
  const zoomEnabled = state.zoomEnabled as boolean;
  const error = state.error as string | null;
  const noDataMessage = state.noDataMessage as string | null;
  const tooltipContent = state.tooltipContent as any;
  const i18n = useI18N(I18N_KEYS);
  const Button = usePart('Button');

  // Merge theme defaults: the categories, the separator and the secondary text of the theme, read in the browser; the
  // scales are painted by the plugin the themed config carries.
  const themedConfig = useThemeDefaults(chartConfig);

  // Build callbacks from interaction descriptor.
  const { callbacks, hasAnyTooltip, onTooltip, onTooltipHide, tooltipPos } = useChartCallbacks(interactions);

  // Merge options: config options + callbacks + zoom.
  // Start from config options only; merge callbacks individually to avoid clobbering plugins.
  const options = React.useMemo(() => {
    if (!themedConfig) return {};

    const base: any = { ...themedConfig.options };
    base.responsive = true;
    base.maintainAspectRatio = false;

    // Apply onClick callback directly (not via spread to avoid plugin clobbering).
    if (callbacks.onClick) {
      base.onClick = callbacks.onClick;
    }

    // Deep-merge legend callback into plugins.legend.
    if (callbacks.plugins?.legend) {
      base.plugins = {
        ...base.plugins,
        legend: { ...base.plugins?.legend, ...callbacks.plugins.legend },
      };
    }

    // Zoom/pan.
    if (zoomEnabled) {
      base.plugins = {
        ...base.plugins,
        zoom: {
          zoom: { wheel: { enabled: true }, pinch: { enabled: true }, mode: 'xy' as const },
          pan: { enabled: true, mode: 'xy' as const },
        },
      };
    }

    // External tooltip for server-side content.
    if (hasAnyTooltip) {
      base.plugins = {
        ...base.plugins,
        tooltip: {
          ...base.plugins?.tooltip,
          enabled: false,
          external: (context: any) => {
            const tooltip = context.tooltip;
            if (tooltip.opacity === 0) {
              // Mouse left the data point — hide tooltip.
              onTooltipHide();
            } else if (tooltip.dataPoints?.length > 0) {
              const point = tooltip.dataPoints[0];
              onTooltip(point.datasetIndex, point.dataIndex, tooltip.caretX, tooltip.caretY);
            }
          },
        },
      };
    }

    return base;
  }, [themedConfig, callbacks, zoomEnabled, hasAnyTooltip, onTooltip]);

  // Error state.
  if (error) {
    return (
      <div id={controlId} className={rootClassName(state, 'tl-chart')}>
        <div className="tl-status tl-type-body" role="alert">{error}</div>
      </div>
    );
  }

  // No data.
  if (!themedConfig?.data?.datasets?.length && noDataMessage) {
    return (
      <div id={controlId} className={rootClassName(state, 'tl-chart')}>
        <div className="tl-status tl-type-body" role="status">{noDataMessage}</div>
      </div>
    );
  }

  // No config yet.
  if (!themedConfig) {
    return <div id={controlId} className={rootClassName(state, 'tl-chart')} />;
  }

  return (
    <div id={controlId} className={rootClassName(state, 'tl-chart')}>
      <Chart
        ref={chartRef}
        type={themedConfig.type}
        data={themedConfig.data}
        options={options}
        plugins={themedConfig.plugins}
        role="img"
        aria-label={i18n['js.chart.label']}
      />
      {zoomEnabled && (
        <div className="tl-chart__zoom-reset">
          <Button
            label={i18n['js.chart.resetZoom']}
            display="label-only"
            appearance="secondary"
            onClick={() => chartRef.current?.resetZoom()}
          />
        </div>
      )}
      <ChartTooltip content={tooltipContent} position={tooltipPos} />
    </div>
  );
};

export default TLChart;
