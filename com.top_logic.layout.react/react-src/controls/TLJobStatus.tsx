import { React, useTLState, useTLCommand, useI18N, rootClassName, tooltipProps, usePart } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { TICK_MS, formatDuration } from './duration';

const { useCallback, useEffect, useMemo, useState } = React;

const I18N_KEYS = {
  'js.jobStatus.running': 'Running',
  'js.jobStatus.completed': 'Completed',
  'js.jobStatus.failed': 'Failed',
  'js.jobStatus.cancelled': 'Cancelled',
  'js.jobStatus.elapsed': 'Elapsed time',
  'js.jobStatus.cancel': 'Cancel',
  'js.jobStatus.currentStep': 'Step: {0}',
  'js.wizard.steps': 'Steps',
};

/** One of the steps the job goes through, and where it stands. */
interface PhaseEntry {
  label: string;
  status: 'done' | 'active' | 'pending';
}

/** Where a step stands, as the attributes of its item in the step list (tl-steps). */
const STEP_ATTRIBUTES: Record<PhaseEntry['status'], Record<string, string>> = {
  done: { 'data-tl-state': 'done' },
  active: { 'aria-current': 'step' },
  pending: {},
};

/** The share of the job done, as the label beside its bar: a whole percentage in the browser's language. */
const PERCENT = new Intl.NumberFormat(undefined, { style: 'percent', maximumFractionDigits: 0 });

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
  const Button = usePart('Button');
  const ProgressBar = usePart('ProgressBar');

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

  // The bar shows its share as a percentage; the state above it says what progresses and names the
  // bar, so the percentage is announced once, as the bar's value.
  const stateId = `${controlId}-state`;
  // Rounded to whole percent as the bar rounds its aria-valuenow (Math.round of fraction × 100): the format
  // alone rounds the decimal digits of the fraction and can show one percent more (0.285: 29 %, the bar 28).
  const percent = fraction != null ? PERCENT.format(Math.round(fraction * 100) / 100) : null;

  const stateText = i18n[`js.jobStatus.${status}`] ?? status;

  // The live line says what is worth announcing: the state and the step the job is at, with the message. The
  // name of the step stands visible; the state and the word "Step" before it are only read, since the header
  // shows the state and the list the step. The elapsed time, the list and the bar stay outside: the time
  // changes every second and would be announced every second.
  const currentStep = phases.find(phase => phase.status === 'active') ?? null;
  const stepTemplate = i18n['js.jobStatus.currentStep'];
  const argument = stepTemplate.indexOf('{0}');
  const stepBefore = argument < 0 ? stepTemplate + ' ' : stepTemplate.substring(0, argument);
  const stepAfter = argument < 0 ? '' : stepTemplate.substring(argument + '{0}'.length);

  return (
    <div id={controlId} className={rootClassName(state, 'tl-job-status')}>
      <div className="tl-job-status__header">
        <span id={stateId} className="tl-job-status__state tl-type-body-strong">{stateText}</span>
        {elapsed !== null && (
          <span className="tl-job-status__elapsed tl-type-body tl-nums" {...tooltipProps(i18n['js.jobStatus.elapsed'])}>{elapsed}</span>
        )}
        {cancelable && (
          <span className="tl-job-status__cancel">
            <Button
              label={i18n['js.jobStatus.cancel']}
              display="label-only"
              appearance="secondary"
              onClick={handleCancel}
            />
          </span>
        )}
      </div>
      <div className="tl-job-status__live tl-type-body" role="status">
        {currentStep == null
          ? <span className="tl-visually-hidden">{stateText + '.'}</span>
          : (
            <>
              <span className="tl-visually-hidden">{stateText + '. ' + stepBefore}</span>
              {currentStep.label}
              {stepAfter !== '' && <span className="tl-visually-hidden">{stepAfter}</span>}
            </>
          )}
        {message !== null && message !== '' && <div className="tl-job-status__message tl-type-body">{message}</div>}
      </div>
      {phases.length > 0 && (
        <ol className="tl-steps tl-steps--vertical" aria-label={i18n['js.wizard.steps']}>
          {phases.map((phase, index) => (
            <li key={index} className="tl-steps__item" {...STEP_ATTRIBUTES[phase.status]}>
              <span className="tl-steps__disc tl-type-label tl-nums" aria-hidden="true">{index + 1}</span>
              <span className={'tl-steps__name ' + (phase.status === 'active' ? 'tl-type-body-strong' : 'tl-type-body')}>
                {phase.label}
              </span>
            </li>
          ))}
        </ol>
      )}
      {showBar && (
        <div className="tl-job-status__bar">
          <ProgressBar value={fraction ?? undefined} label={percent ?? undefined} labelledBy={stateId} />
        </div>
      )}
      {error !== null && error !== '' && <div className="tl-job-status__error tl-type-body" role="alert">{error}</div>}
      {error == null && result !== null && result !== '' && (
        <div className="tl-job-status__result tl-type-body">{result}</div>
      )}
    </div>
  );
};

export default TLJobStatus;
