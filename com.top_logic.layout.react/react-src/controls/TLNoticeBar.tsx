import { React, useTLState } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useEffect, useMemo, useState } = React;

/** Tick interval of the count-down, in ms. */
const TICK_MS = 1000;

/** Formats a duration in ms as `m:ss`, or `h:mm:ss` once an hour or more remains. */
function formatRemaining(millis: number): string {
  const total = Math.max(0, Math.floor(millis / 1000));
  const seconds = total % 60;
  const minutes = Math.floor(total / 60) % 60;
  const hours = Math.floor(total / 3600);
  const pad = (value: number) => (value < 10 ? `0${value}` : `${value}`);
  return hours > 0
    ? `${hours}:${pad(minutes)}:${pad(seconds)}`
    : `${minutes}:${pad(seconds)}`;
}

/**
 * A system-wide notice rendered as a full-width bar, e.g. the announcement of a maintenance window.
 *
 * State:
 * - visible:   boolean
 * - severity:  "info" | "warning" | "error"
 * - text:      string
 * - deadline:  number | null  (epoch ms of the announced event)
 * - serverNow: number | null  (the server's clock when it sent the deadline)
 *
 * While not visible the component renders nothing, so a notice area holding only hidden notices
 * collapses to zero height.
 *
 * With a deadline set, a count-down ticks once per second. It counts against server time: both
 * bounds come from the server, and the offset to the local clock is measured once per notice, so a
 * browser whose clock is off by minutes still shows the right remaining time.
 *
 * The count-down stops at 0:00 rather than running negative, and the text stays untouched until the
 * server pushes the new state: whether the announced event has actually happened is the server's
 * statement, not the browser's.
 */
const TLNoticeBar: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();

  const visible = state.visible === true;
  const severity = (state.severity as string) ?? 'info';
  const text = (state.text as string) ?? '';
  const deadline = (state.deadline as number | null) ?? null;
  const serverNow = (state.serverNow as number | null) ?? null;

  // Offset between this browser's clock and the server's, measured when the notice arrives. The
  // measurement is late by the transport latency, which shortens the count-down by those few
  // milliseconds - the safe direction.
  const clockOffset = useMemo(
    () => (serverNow != null ? serverNow - Date.now() : 0),
    [serverNow],
  );

  // Re-render once per second while a count-down is running, so the remaining time stays current.
  const [, setTick] = useState(0);
  const counting = visible && deadline != null;
  useEffect(() => {
    if (!counting) return;
    const timer = setInterval(() => setTick(t => t + 1), TICK_MS);
    return () => clearInterval(timer);
  }, [counting, deadline]);

  if (!visible) return null;

  const remaining = deadline != null ? formatRemaining(deadline - (Date.now() + clockOffset)) : null;

  return (
    <div id={controlId} className={`tlNoticeBar tlNoticeBar--${severity}`} role="status" aria-live="polite">
      <span className="tlNoticeBar__text">{text}</span>
      {remaining !== null && <span className="tlNoticeBar__countdown">{remaining}</span>}
    </div>
  );
};

export default TLNoticeBar;
