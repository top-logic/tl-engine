import { React, useTLState } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * Displays a fraction as a bar with an optional label beside it.
 *
 * State:
 * - fraction: number - the filled part of the track, between 0 and 1
 * - label?: string - the text beside the bar; the bar stands alone without one
 */
const TLProgress: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const fraction = typeof state.fraction === 'number' ? state.fraction : 0;
  const label = (state.label as string) || undefined;
  const percent = Math.round(fraction * 100);

  return (
    <div
      id={controlId}
      className="tlProgress"
      role="progressbar"
      aria-valuemin={0}
      aria-valuemax={100}
      aria-valuenow={percent}
      aria-valuetext={label}
    >
      <span className="tlProgress__track">
        <span className="tlProgress__fill" style={{ width: percent + '%' }} />
      </span>
      {label && <span className="tlProgress__label">{label}</span>}
    </div>
  );
};

export default TLProgress;
