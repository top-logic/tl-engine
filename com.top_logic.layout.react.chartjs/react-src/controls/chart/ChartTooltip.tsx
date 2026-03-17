// react-src/controls/chart/ChartTooltip.tsx
import { React } from 'tl-react-bridge';

interface TooltipContent {
  html: string;
  datasetIndex: number;
  index: number;
}

interface ChartTooltipProps {
  content: TooltipContent | null;
  chartRef: React.RefObject<any>;
}

/**
 * Renders server-provided tooltip HTML positioned near the data point.
 */
const ChartTooltip: React.FC<ChartTooltipProps> = ({ content, chartRef }) => {
  if (!content || !content.html) return null;

  // Position tooltip near the data point.
  let left = 0;
  let top = 0;
  const chart = chartRef.current;
  if (chart) {
    const meta = chart.getDatasetMeta(content.datasetIndex);
    if (meta && meta.data && meta.data[content.index]) {
      const point = meta.data[content.index];
      left = point.x;
      top = point.y - 10;
    }
  }

  return (
    <div
      className="tlReactChart__tooltip"
      style={{ left: left + 'px', top: top + 'px' }}
      dangerouslySetInnerHTML={{ __html: content.html }}
    />
  );
};

export default ChartTooltip;
