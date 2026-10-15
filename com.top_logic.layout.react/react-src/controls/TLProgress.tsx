import { React, useTLState, rootClassName, usePart } from 'tl-react-bridge';
import type { TLCellProps, ProgressStateJson } from 'tl-react-bridge';

/**
 * Control displaying the progress its state reports, drawn by the part `ProgressBar`: the fraction
 * as the share of the bar - none: an indeterminate bar -, the label beside it.
 *
 * The state is described by ProgressStateJson.
 */
const TLProgress: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<ProgressStateJson>>();
  const ProgressBar = usePart('ProgressBar');

  if (state.hidden === true) {
    return null;
  }

  return (
    <ProgressBar
      id={controlId}
      value={typeof state.fraction === 'number' ? state.fraction : undefined}
      label={state.label || undefined}
      className={rootClassName(state) || undefined}
    />
  );
};

export default TLProgress;
