import { React, useTLState, rootClassName } from 'tl-react-bridge';
import type { TLCellProps, ProgressStateJson } from 'tl-react-bridge';
import LinearProgress from '@mui/material/LinearProgress';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
import type { SxProps, Theme } from '@mui/material/styles';

/** The bar takes the width the label leaves. */
const BAR_SX: SxProps<Theme> = { flex: '1 1 auto' };

/** The label keeps its width and shows its digits in columns of equal width. */
const LABEL_SX: SxProps<Theme> = { flexShrink: 0, fontVariantNumeric: 'tabular-nums' };

/**
 * Renders the state of a TopLogic progress bar (module name `TLProgress`) with the MUI
 * `LinearProgress` and a label beside it.
 *
 * <p>Mapping from the control state:</p>
 * <ul>
 * <li>fraction → `variant="determinate"` with `value` = fraction × 100; absent or `null` →
 *     `variant="indeterminate"`, marked `aria-busy` as TLProgress marks it;</li>
 * <li>label → a `Typography` beside the bar, and the `aria-valuetext` of the bar;</li>
 * <li>hidden → nothing is rendered; the configured CSS class → className of the element around
 *     bar and label, which carries the control ID.</li>
 * </ul>
 */
const MuiProgressAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<ProgressStateJson>>();

  if (state.hidden === true) {
    return null;
  }

  const fraction = typeof state.fraction === 'number' ? state.fraction : null;
  const label = state.label || undefined;

  return (
    <Stack id={controlId} direction="row" spacing={1} className={rootClassName(state)} sx={{ alignItems: 'center' }}>
      {fraction === null
        ? <LinearProgress variant="indeterminate" aria-valuetext={label} aria-busy sx={BAR_SX} />
        : <LinearProgress variant="determinate" value={fraction * 100} aria-valuetext={label} sx={BAR_SX} />}
      {label !== undefined && <Typography variant="body2" sx={LABEL_SX}>{label}</Typography>}
    </Stack>
  );
};

export default MuiProgressAdapter;
