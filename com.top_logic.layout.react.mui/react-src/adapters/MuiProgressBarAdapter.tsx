import { React } from 'tl-react-bridge';
import type { ProgressBarPartProps } from 'tl-react-bridge';
import LinearProgress from '@mui/material/LinearProgress';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
import type { SxProps, Theme } from '@mui/material/styles';

/** The bar takes the width the label leaves. */
const BAR_SX: SxProps<Theme> = { flex: '1 1 auto' };

/** The label keeps its width and shows its digits in columns of equal width. */
const LABEL_SX: SxProps<Theme> = { flexShrink: 0, fontVariantNumeric: 'tabular-nums' };

/** The element around bar and label lines them up in a row. */
const ROW_SX: SxProps<Theme> = { alignItems: 'center', minWidth: 0 };

/**
 * Draws the part `ProgressBar` ({@link ProgressBarPartProps}) with the MUI `LinearProgress` and a
 * label beside it.
 *
 * <p>Mapping from the props: value → `variant="determinate"` with `value` = value × 100; absent →
 * `variant="indeterminate"`, marked `aria-busy`; label → a `Typography` beside the bar, and the
 * `aria-valuetext` of the bar; id, className and the passed-through attributes → the element around
 * bar and label.</p>
 */
const MuiProgressBarAdapter: React.FC<ProgressBarPartProps> = ({ id, value, label, className, ...attributes }) => (
  <Stack id={id} direction="row" spacing={1} className={className} sx={ROW_SX} {...attributes}>
    {value === undefined
      ? <LinearProgress variant="indeterminate" aria-valuetext={label} aria-busy sx={BAR_SX} />
      : <LinearProgress variant="determinate" value={value * 100} aria-valuetext={label} sx={BAR_SX} />}
    {label && <Typography variant="body2" sx={LABEL_SX}>{label}</Typography>}
  </Stack>
);

export default MuiProgressBarAdapter;
