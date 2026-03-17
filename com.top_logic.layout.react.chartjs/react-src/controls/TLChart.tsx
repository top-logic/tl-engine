// react-src/controls/TLChart.tsx
import { React } from 'tl-react-bridge';
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

/**
 * React component that renders a Chart.js chart driven by server-side state.
 *
 * State from server:
 *  - chartConfig: any        - Clean Chart.js config (type, data, options)
 *  - interactions: any       - Interaction descriptor per dataset
 *  - themeColors: any        - Theme palette and color defaults
 *  - zoomEnabled: boolean    - Whether zoom/pan is enabled
 *  - cssClass: string | null - Additional CSS class for the container
 *  - error: string | null    - Error message to display instead of chart
 *  - noDataMessage: string | null - Message when no datasets
 *  - tooltipContent: any     - Server-provided tooltip HTML + position info
 */
const TLChart: React.FC<TLCellProps> = ({ controlId, state }) => {
  const chartRef = React.useRef<ChartJS | null>(null);

  const chartConfig = state.chartConfig as any;
  const interactions = state.interactions as any;
  const themeColors = state.themeColors as any;
  const zoomEnabled = state.zoomEnabled as boolean;
  const cssClass = state.cssClass as string | null;
  const error = state.error as string | null;
  const noDataMessage = state.noDataMessage as string | null;
  const tooltipContent = state.tooltipContent as any;

  // Merge theme defaults.
  const themedConfig = useThemeDefaults(chartConfig, themeColors);

  // Build callbacks from interaction descriptor.
  const { callbacks, hasAnyTooltip, onTooltip } = useChartCallbacks(interactions);

  // Merge options: config options + callbacks + zoom.
  const options = React.useMemo(() => {
    if (!themedConfig) return {};

    const base = { ...themedConfig.options, ...callbacks };
    base.responsive = true;
    base.maintainAspectRatio = false;

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
            if (tooltip.opacity > 0 && tooltip.dataPoints?.length > 0) {
              const point = tooltip.dataPoints[0];
              onTooltip(point.datasetIndex, point.dataIndex);
            }
          },
        },
      };
    }

    // Merge legend callbacks if present.
    if (callbacks.plugins?.legend) {
      base.plugins = {
        ...base.plugins,
        legend: { ...base.plugins?.legend, ...callbacks.plugins.legend },
      };
    }

    return base;
  }, [themedConfig, callbacks, zoomEnabled, hasAnyTooltip, onTooltip]);

  // Error state.
  if (error) {
    return (
      <div id={controlId} className={'tlReactChart tlReactChart--error ' + (cssClass || '')}>
        <div className="tlReactChart__error">{error}</div>
      </div>
    );
  }

  // No data.
  if (!themedConfig?.data?.datasets?.length && noDataMessage) {
    return (
      <div id={controlId} className={'tlReactChart tlReactChart--noData ' + (cssClass || '')}>
        <div className="tlReactChart__noData">{noDataMessage}</div>
      </div>
    );
  }

  // No config yet.
  if (!themedConfig) {
    return <div id={controlId} className={'tlReactChart ' + (cssClass || '')} />;
  }

  return (
    <div id={controlId} className={'tlReactChart ' + (cssClass || '')}>
      <Chart
        ref={chartRef}
        type={themedConfig.type}
        data={themedConfig.data}
        options={options}
      />
      {zoomEnabled && (
        <button
          className="tlReactChart__zoomReset"
          onClick={() => chartRef.current?.resetZoom()}
        >
          Reset Zoom
        </button>
      )}
      <ChartTooltip content={tooltipContent} chartRef={chartRef} />
    </div>
  );
};

export default TLChart;
