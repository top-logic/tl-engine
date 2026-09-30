// react-src/controls/chart/ChartTooltip.tsx
import { React } from 'tl-react-bridge';
import type { TooltipPosition } from './useChartCallbacks';

interface TooltipContent {
  html: string;
  datasetIndex: number;
  index: number;
}

interface ChartTooltipProps {
  content: TooltipContent | null;
  position: TooltipPosition;
}

/**
 * Renders server-provided tooltip HTML.
 * Position is tracked locally from Chart.js caret coordinates;
 * HTML content arrives asynchronously from the server via SSE.
 */
const ChartTooltip: React.FC<ChartTooltipProps> = ({ content, position }) => {
  if (!position.visible || !content || !content.html) return null;

  // Hide tooltip while waiting for server data for the new data point.
  if (content.datasetIndex !== position.datasetIndex || content.index !== position.index) return null;

  return (
    <div
      className="tlReactChart__tooltip"
      style={{ left: position.x + 'px', top: (position.y - 10) + 'px' }}
      dangerouslySetInnerHTML={{ __html: content.html }}
    />
  );
};

export default ChartTooltip;
