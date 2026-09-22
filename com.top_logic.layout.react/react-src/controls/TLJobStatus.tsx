import { React, useTLState, useTLCommand, useI18N } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { TICK_MS, formatDuration } from './duration';
import { ProgressBar } from './TLProgress';

const { useCallback, useEffect, useMemo, useState } = React;

const I18N_KEYS = {
  'js.jobStatus.running': 'Running',
  'js.jobStatus.completed': 'Completed',
  'js.jobStatus.failed': 'Failed',
  'js.jobStatus.cancelled': 'Cancelled',
  'js.jobStatus.elapsed': 'Elapsed time',
  'js.jobStatus.cancel': 'Cancel',
};

/** One of the steps the job goes through, and where it stands. */
interface PhaseEntry {
  label: string;
  status: 'done' | 'active' | 'pending';
}

/**
 * What is known about a long-running job: the steps it goes through, how far it has come, how long
 * it has been at work, and what it says about what it is doing.
 *
 * State:
 * - status:     "running" | "completed" | "failed" | "cancelled" | null (null displays nothing)
 * - phases:     {label, status: "done" | "active" | "pending"}[]
 * - fraction:   number | null  (null is the indeterminate bar)
 * - message:    string | null
 * - startedAt:  number | null  (epoch ms the elapsed time is counted from)
 * - finishedAt: number | null  (epoch ms the job ended, null while it runs)
 * - serverNow:  number | null  (the server's clock when it sent this report)
 * - result:     string | null  (what the job produced)
 * - error:      string | null  (why the job failed)
 * - cancelable: boolean        (whether the job may be asked to stop)
 *
 * The elapsed time is counted here rather than pushed: a running job would otherwise report once a
 * second only to move a clock. It counts against server time - both bounds come from the server,
 * and the offset to the local clock is measured per report - and it stops at the moment the job
 * ended, which the server states.
 */
const TLJobStatus: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);

  const status = (state.status as string | null) ?? null;
  const phases = (state.phases as PhaseEntry[] | undefined) ?? [];
  const fraction = typeof state.fraction === 'number' ? state.fraction : null;
  const message = (state.message as string | null) ?? null;
  const startedAt = (state.startedAt as number | null) ?? null;
  const finishedAt = (state.finishedAt as number | null) ?? null;
  const serverNow = (state.serverNow as number | null) ?? null;
  const result = (state.result as string | null) ?? null;
  const error = (state.error as string | null) ?? null;
  const cancelable = state.cancelable === true;

  const running = status === 'running';

  // Offset between this browser's clock and the server's, measured with every report. The
  // measurement is late by the transport latency, which shortens the elapsed time by those few
  // milliseconds.
  const clockOffset = useMemo(
    () => (serverNow != null ? serverNow - Date.now() : 0),
    [serverNow],
  );

  // Re-render once per second while the job works, so the elapsed time stays current. A job that
  // has ended keeps the time it took, so nothing ticks any more.
  const [, setTick] = useState(0);
  useEffect(() => {
    if (!running || startedAt == null) return;
    const timer = setInterval(() => setTick(t => t + 1), TICK_MS);
    return () => clearInterval(timer);
  }, [running, startedAt]);

  const handleCancel = useCallback(() => {
    sendCommand('cancel', {});
  }, [sendCommand]);

  if (status == null) return null;

  const elapsedMs = startedAt == null
    ? null
    : (finishedAt != null ? finishedAt : Date.now() + clockOffset) - startedAt;
  const elapsed = elapsedMs != null ? formatDuration(elapsedMs) : null;

  // A finished job that never knew its share has nothing to draw: its bar would sweep on although
  // nothing is going on any more.
  const showBar = running || fraction != null;

  return (
    <div id={controlId} className={`tlJobStatus tlJobStatus--${status}`} role="status" aria-live="polite">
      <div className="tlJobStatus__header">
        <span className="tlJobStatus__state">{i18n[`js.jobStatus.${status}`] ?? status}</span>
        {elapsed !== null && (
          <span className="tlJobStatus__elapsed" title={i18n['js.jobStatus.elapsed']}>{elapsed}</span>
        )}
        {cancelable && (
          <button type="button" className="tlJobStatus__cancel" onClick={handleCancel}>
            {i18n['js.jobStatus.cancel']}
          </button>
        )}
      </div>
      {phases.length > 0 && (
        <ol className="tlJobStatus__phases">
          {phases.map((phase, index) => (
            <li key={index} className={`tlJobStatus__phase tlJobStatus__phase--${phase.status}`}>
              {phase.label}
            </li>
          ))}
        </ol>
      )}
      {showBar && <ProgressBar fraction={fraction} className="tlJobStatus__bar" />}
      {message !== null && message !== '' && <div className="tlJobStatus__message">{message}</div>}
      {error !== null && error !== '' && <div className="tlJobStatus__error">{error}</div>}
      {error == null && result !== null && result !== '' && (
        <div className="tlJobStatus__result">{result}</div>
      )}
    </div>
  );
};

export default TLJobStatus;
