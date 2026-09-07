import { React, useTLState } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/** How often the displayed relative text is recomputed. */
const REFRESH_MS = 30_000;

/** Formats the distance from `ts` to now as relative time, or an absolute date if older. */
function formatRelative(ts: number, locale: string): string {
  const diffSec = Math.round((ts - Date.now()) / 1000);
  const abs = Math.abs(diffSec);
  const rtf = new Intl.RelativeTimeFormat(locale, { numeric: 'auto' });
  if (abs < 60) {
    return rtf.format(Math.trunc(diffSec / 1), 'second');
  }
  if (abs < 3600) {
    return rtf.format(Math.trunc(diffSec / 60), 'minute');
  }
  if (abs < 86_400) {
    return rtf.format(Math.trunc(diffSec / 3600), 'hour');
  }
  if (abs < 7 * 86_400) {
    return rtf.format(Math.trunc(diffSec / 86_400), 'day');
  }
  // Older than a week: the exact date carries more information than "34 days ago".
  return new Date(ts).toLocaleDateString(locale);
}

/**
 * Displays a timestamp as periodically refreshed relative time ("5 minutes ago"); the exact
 * server-formatted timestamp is shown as tooltip.
 *
 * State:
 * - timestamp: number | null (epoch milliseconds)
 * - label: string | null (exact timestamp text for the tooltip)
 * - locale: string (session locale language tag)
 */
const TLRelativeTime: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const timestamp = state.timestamp as number | null;
  const label = (state.label as string | null) ?? undefined;
  const locale = (state.locale as string | null) || navigator.language;

  const [, setTick] = React.useState(0);
  React.useEffect(() => {
    const interval = setInterval(() => setTick(t => t + 1), REFRESH_MS);
    return () => clearInterval(interval);
  }, []);

  if (timestamp == null) {
    return <span id={controlId} className="tlRelativeTime tlRelativeTime--empty" />;
  }

  return (
    <span id={controlId} className="tlRelativeTime" title={label}>
      {formatRelative(timestamp, locale)}
    </span>
  );
};

export default TLRelativeTime;
