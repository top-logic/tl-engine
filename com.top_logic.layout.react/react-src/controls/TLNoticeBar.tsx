import { React, useTLState, useTLCommand } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback, useEffect, useMemo, useRef, useState } = React;

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
 * - visible:     boolean
 * - severity:    "info" | "warning" | "error"
 * - text:        string
 * - deadline:    number | null  (epoch ms of the announced event)
 * - serverNow:   number | null  (the server's clock when it sent the deadline)
 * - leadMs:      number | null  (show only once less than this remains until the deadline)
 * - actionLabel: string | null  (non-null makes the bar a button; the label is its tooltip)
 * - pingGraceMs: number | null  (ask the server once, this long after the deadline passed)
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
 *
 * A lead time keeps the bar out of the way until the announced event comes close - the session
 * timeout is known from the start but is worth a warning only in its last minutes. The count-down
 * runs while the bar is still held back, so it appears on time without the server being asked.
 *
 * With a ping grace period set, the client asks the server once per deadline what happened, a grace
 * period after the count-down passed zero. Without it the bar would stand at 0:00 until something
 * else makes the page talk to the server - a session, for instance, is discarded by the container
 * only when the next request arrives, so nobody would tell the browser that it has ended.
 */
const TLNoticeBar: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const visible = state.visible === true;
  const severity = (state.severity as string) ?? 'info';
  const text = (state.text as string) ?? '';
  const deadline = (state.deadline as number | null) ?? null;
  const serverNow = (state.serverNow as number | null) ?? null;
  const leadMs = (state.leadMs as number | null) ?? null;
  const actionLabel = (state.actionLabel as string | null) ?? null;
  const pingGraceMs = (state.pingGraceMs as number | null) ?? null;

  // Offset between this browser's clock and the server's, measured when the notice arrives. The
  // measurement is late by the transport latency, which shortens the count-down by those few
  // milliseconds - the safe direction.
  const clockOffset = useMemo(
    () => (serverNow != null ? serverNow - Date.now() : 0),
    [serverNow],
  );

  // Re-render once per second while a count-down is running, so the remaining time stays current.
  // This also runs while the lead time still holds the bar back, which is what lets it appear.
  const [tick, setTick] = useState(0);
  const counting = visible && deadline != null;
  useEffect(() => {
    if (!counting) return;
    const timer = setInterval(() => setTick(t => t + 1), TICK_MS);
    return () => clearInterval(timer);
  }, [counting, deadline]);

  // Ask the server what happened, once per deadline and only once the grace period after it has
  // elapsed. The grace period is the server's, sized to cover how imprecisely this client knows
  // the deadline: asking while the announced event is still ahead would, for a session timeout,
  // renew the very session the notice is counting down.
  const askedFor = useRef<number | null>(null);
  useEffect(() => {
    if (!counting || pingGraceMs == null || deadline == null) return;
    if (askedFor.current === deadline) return;
    if (Date.now() + clockOffset < deadline + pingGraceMs) return;
    askedFor.current = deadline;
    sendCommand('deadlinePassed', {});
  }, [tick, counting, deadline, pingGraceMs, clockOffset, sendCommand]);

  const handleAction = useCallback(() => {
    if (actionLabel != null) {
      sendCommand('action', {});
    }
  }, [sendCommand, actionLabel]);

  if (!visible) return null;

  const remainingMs = deadline != null ? deadline - (Date.now() + clockOffset) : null;
  if (leadMs != null && remainingMs != null && remainingMs > leadMs) return null;

  const remaining = remainingMs != null ? formatRemaining(remainingMs) : null;
  const clickable = actionLabel != null;

  return (
    <div id={controlId}
      className={`tlNoticeBar tlNoticeBar--${severity}${clickable ? ' tlNoticeBar--clickable' : ''}`}
      role={clickable ? 'button' : 'status'} aria-live="polite"
      tabIndex={clickable ? 0 : undefined}
      title={actionLabel ?? undefined}
      aria-label={clickable ? `${text} ${actionLabel}` : undefined}
      onClick={clickable ? handleAction : undefined}
      onKeyDown={clickable
        ? (event: React.KeyboardEvent) => {
            if (event.key === 'Enter' || event.key === ' ') {
              event.preventDefault();
              handleAction();
            }
          }
        : undefined}>
      <span className="tlNoticeBar__text">{text}</span>
      {remaining !== null && <span className="tlNoticeBar__countdown">{remaining}</span>}
    </div>
  );
};

export default TLNoticeBar;
