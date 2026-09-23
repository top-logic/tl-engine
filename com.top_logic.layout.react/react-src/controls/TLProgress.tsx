import { React, useTLState, rootClassName } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * Displays a fraction as a bar with an optional label beside it.
 *
 * A fraction of null is the indeterminate bar: the track carries a sweeping fill instead of a
 * share of it, and the bar reports itself as busy without a value.
 */
export function ProgressBar({
  id,
  fraction,
  label,
  className,
}: {
  id?: string;
  fraction: number | null;
  label?: string | null;
  className?: string;
}) {
  const indeterminate = typeof fraction !== 'number';
  const percent = indeterminate ? 0 : Math.round((fraction as number) * 100);
  const classNames = ['tlProgress'];
  if (indeterminate) {
    classNames.push('tlProgress--indeterminate');
  }
  if (className) {
    classNames.push(className);
  }

  return (
    <div
      id={id}
      className={classNames.join(' ')}
      role="progressbar"
      aria-valuemin={0}
      aria-valuemax={100}
      aria-valuenow={indeterminate ? undefined : percent}
      aria-valuetext={label || undefined}
      aria-busy={indeterminate || undefined}
    >
      <span className="tlProgress__track">
        <span
          className="tlProgress__fill"
          style={indeterminate ? undefined : { width: percent + '%' }}
        />
      </span>
      {label && <span className="tlProgress__label">{label}</span>}
    </div>
  );
}

/**
 * Control displaying the progress its state reports.
 *
 * State:
 * - fraction: number | null - the filled part of the track, between 0 and 1; null is the
 *   indeterminate bar
 * - label?: string - the text beside the bar; the bar stands alone without one
 */
const TLProgress: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const fraction = typeof state.fraction === 'number' ? state.fraction : null;
  const label = (state.label as string) || undefined;

  return <ProgressBar id={controlId} fraction={fraction} label={label} className={rootClassName(state)} />;
};

export default TLProgress;
