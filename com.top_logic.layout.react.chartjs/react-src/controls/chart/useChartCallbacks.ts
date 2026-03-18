// react-src/controls/chart/useChartCallbacks.ts
import { React, useTLCommand } from 'tl-react-bridge';

interface DatasetInteraction {
  clickable: boolean;
  legendClickable: boolean;
  hasTooltip: boolean;
}

interface Interactions {
  datasets: DatasetInteraction[];
}

export interface TooltipPosition {
  x: number;
  y: number;
  visible: boolean;
}

/**
 * Creates Chart.js callback options based on the interaction descriptor.
 */
export function useChartCallbacks(interactions: Interactions | null) {
  const sendCommand = useTLCommand();

  const hasAnyClick = interactions?.datasets.some(d => d.clickable) ?? false;
  const hasAnyLegendClick = interactions?.datasets.some(d => d.legendClickable) ?? false;
  const hasAnyTooltip = interactions?.datasets.some(d => d.hasTooltip) ?? false;

  // Local tooltip position state (updated by Chart.js external tooltip callback).
  const [tooltipPos, setTooltipPos] = React.useState<TooltipPosition>({ x: 0, y: 0, visible: false });

  const onClick = React.useCallback((_event: any, elements: any[]) => {
    if (elements.length > 0) {
      const { datasetIndex, index } = elements[0];
      sendCommand('elementClick', { datasetIndex, index, zone: 'datapoint' });
    }
  }, [sendCommand]);

  const onLegendClick = React.useCallback((_event: any, legendItem: any, legend: any) => {
    const chart = legend.chart;
    const index = legendItem.datasetIndex;
    if (chart.isDatasetVisible(index)) {
      chart.hide(index);
      legendItem.hidden = true;
    } else {
      chart.show(index);
      legendItem.hidden = false;
    }
    sendCommand('elementClick', { datasetIndex: index, index: -1, zone: 'legend' });
  }, [sendCommand]);

  // Debounce tooltip requests.
  const tooltipTimer = React.useRef<number | null>(null);
  const onTooltip = React.useCallback((datasetIndex: number, dataIndex: number, x: number, y: number) => {
    // Update position immediately (no debounce for position).
    setTooltipPos({ x, y, visible: true });

    if (tooltipTimer.current != null) {
      clearTimeout(tooltipTimer.current);
    }
    tooltipTimer.current = window.setTimeout(() => {
      sendCommand('tooltip', { datasetIndex, index: dataIndex });
      tooltipTimer.current = null;
    }, 200);
  }, [sendCommand]);

  const onTooltipHide = React.useCallback(() => {
    if (tooltipTimer.current != null) {
      clearTimeout(tooltipTimer.current);
      tooltipTimer.current = null;
    }
    setTooltipPos(prev => ({ ...prev, visible: false }));
  }, []);

  // Clean up pending tooltip timer when the component unmounts.
  React.useEffect(() => {
    return () => {
      if (tooltipTimer.current != null) {
        clearTimeout(tooltipTimer.current);
      }
    };
  }, []);

  return React.useMemo(() => {
    const callbacks: any = {};

    if (hasAnyClick) {
      callbacks.onClick = onClick;
    }

    if (hasAnyLegendClick) {
      callbacks.plugins = { legend: { onClick: onLegendClick } };
    }

    return { callbacks, hasAnyTooltip, onTooltip, onTooltipHide, tooltipPos };
  }, [hasAnyClick, hasAnyLegendClick, hasAnyTooltip, onClick, onLegendClick, onTooltip, onTooltipHide, tooltipPos]);
}
