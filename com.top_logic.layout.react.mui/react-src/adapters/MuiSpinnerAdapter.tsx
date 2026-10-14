import { React } from 'tl-react-bridge';
import type { SpinnerPartProps, SpinnerSize } from 'tl-react-bridge';
import CircularProgress from '@mui/material/CircularProgress';

/** The diameter of the spinner by size, in pixels: the size of a small and of a large icon. */
const DIAMETER: Record<SpinnerSize, number> = {
  small: 16,
  medium: 24,
};

/**
 * Draws the part `Spinner` ({@link SpinnerPartProps}) with the MUI `CircularProgress` in its
 * indeterminate variant.
 *
 * <p>Mapping from the props: size → the diameter (16 px small, 24 px medium); a small spinner, which
 * stands in for an icon, takes the color of the text (`color="inherit"`), a medium one the color
 * `primary`; label → `aria-label` of the progress bar, absent → `aria-hidden`; className and the
 * passed-through attributes → the root of the spinner.</p>
 */
const MuiSpinnerAdapter: React.FC<SpinnerPartProps> = ({ size, label, className, ...attributes }) => {
  const small = size === 'small';
  return (
    <CircularProgress
      size={DIAMETER[size ?? 'medium']}
      color={small ? 'inherit' : 'primary'}
      className={className}
      aria-label={label || undefined}
      aria-hidden={label ? undefined : true}
      {...attributes}
    />
  );
};

export default MuiSpinnerAdapter;
