import { React } from 'tl-react-bridge';
import type { ProgressBarPartProps } from 'tl-react-bridge';

/**
 * The TopLogic drawing of the part `ProgressBar` ({@link ProgressBarPartProps}): the progress bar of
 * the design system (tl-progress), a track with a fill and the label beside it.
 *
 * <p>An indeterminate bar is marked `aria-busy`, and the stylesheet answers that state with a
 * sweeping fill instead of a share of the track. The width of the fill is data, not design: it is
 * the share `aria-valuenow` states, and the only thing set inline.</p>
 */
const ProgressBarPart: React.FC<ProgressBarPartProps> = ({ id, value, label, className, ...attributes }) => {
  const indeterminate = value === undefined;
  const percent = indeterminate ? 0 : Math.round(value * 100);
  return (
    <div
      id={id}
      className={className ? 'tl-progress ' + className : 'tl-progress'}
      role="progressbar"
      aria-valuemin={0}
      aria-valuemax={100}
      aria-valuenow={indeterminate ? undefined : percent}
      aria-valuetext={label || undefined}
      aria-busy={indeterminate || undefined}
      {...attributes}
    >
      <span className="tl-progress__track">
        <span className="tl-progress__fill" style={indeterminate ? undefined : { width: percent + '%' }} />
      </span>
      {label && <span className="tl-progress__label tl-type-label tl-nums">{label}</span>}
    </div>
  );
};

export default ProgressBarPart;
