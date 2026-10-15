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
const ProgressBarPart: React.FC<ProgressBarPartProps> = ({ id, value, label, labelledBy, className, ...attributes }) => {
  const labelId = React.useId();
  const indeterminate = value === undefined;
  const percent = indeterminate ? 0 : Math.round(value * 100);
  const hidden = !!attributes['aria-hidden'];
  const named = !!label && labelledBy === undefined && !hidden;
  return (
    <div
      id={id}
      className={className ? 'tl-progress ' + className : 'tl-progress'}
      role="progressbar"
      aria-valuemin={0}
      aria-valuemax={100}
      aria-valuenow={indeterminate ? undefined : percent}
      aria-labelledby={hidden ? undefined : labelledBy ?? (named ? labelId : undefined)}
      aria-busy={indeterminate || undefined}
      {...attributes}
    >
      <span className="tl-progress__track">
        <span className="tl-progress__fill" style={indeterminate ? undefined : { width: percent + '%' }} />
      </span>
      {label && (
        // A label that only shows the value is hidden from assistive technology: aria-valuenow says
        // it, and inside a live region (a job's status) its every change would be announced.
        <span id={named ? labelId : undefined} className="tl-progress__label tl-type-label tl-nums"
          aria-hidden={labelledBy !== undefined ? true : undefined}>{label}</span>
      )}
    </div>
  );
};

export default ProgressBarPart;
