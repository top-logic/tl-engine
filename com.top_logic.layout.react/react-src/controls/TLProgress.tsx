import { React, useTLState, rootClassName } from 'tl-react-bridge';
import type { TLCellProps, ProgressStateJson } from 'tl-react-bridge';

/**
 * Displays a fraction as a bar with an optional label beside it: `tl-progress` of the design
 * system.
 *
 * A fraction of null is the indeterminate bar: the bar reports itself as busy without a value, and
 * the stylesheet answers that state with a sweeping fill instead of a share of the track. The width
 * of the fill is data, not design: it is the same share `aria-valuenow` states, and the only thing
 * set inline.
 *
 * A progress bar needs a name (ARIA 1.2): a bar with a label is named by it (`aria-labelledby` on the
 * label's id). Where the label is the value itself - a percentage - the place names the bar instead:
 * `labelledBy` is the id of the element saying what progresses (the state of a job), and the label
 * only shows the value. The bar states its value through `aria-valuenow` alone: the label as value
 * text as well would be announced twice. A bar inside an element that carries the
 * name of what loads itself (the loading row of a tree, a tree item with its own name) is hidden from
 * assistive technology and has no name of its own, so the name is announced once.
 */
export function ProgressBar({
  id,
  fraction,
  label,
  labelledBy,
  className,
  ariaHidden,
}: {
  id?: string;
  fraction: number | null;
  label?: string | null;
  labelledBy?: string;
  className?: string;
  ariaHidden?: boolean;
}) {
  const labelId = React.useId();
  const indeterminate = typeof fraction !== 'number';
  const named = !!label && !labelledBy && !ariaHidden;
  const percent = indeterminate ? 0 : Math.round((fraction as number) * 100);

  return (
    <div
      id={id}
      className={['tl-progress', className].filter(Boolean).join(' ')}
      role="progressbar"
      aria-valuemin={0}
      aria-valuemax={100}
      aria-valuenow={indeterminate ? undefined : percent}
      aria-labelledby={ariaHidden ? undefined : labelledBy ?? (named ? labelId : undefined)}
      aria-busy={indeterminate || undefined}
      aria-hidden={ariaHidden || undefined}
    >
      <span className="tl-progress__track">
        <span className="tl-progress__fill" style={indeterminate ? undefined : { width: percent + '%' }} />
      </span>
      {label && (
        // A label that only shows the value is hidden from assistive technology: aria-valuenow says
        // it, and inside a live region (a job's status) its every change would be announced.
        <span id={named ? labelId : undefined} className="tl-progress__label tl-type-label tl-nums"
          aria-hidden={labelledBy ? true : undefined}>{label}</span>
      )}
    </div>
  );
}

/**
 * Control displaying the progress its state reports.
 *
 * The state is described by ProgressStateJson.
 */
const TLProgress: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<ProgressStateJson>>();
  const fraction = typeof state.fraction === 'number' ? state.fraction : null;
  const label = state.label || undefined;

  return <ProgressBar id={controlId} fraction={fraction} label={label} className={rootClassName(state)} />;
};

export default TLProgress;
