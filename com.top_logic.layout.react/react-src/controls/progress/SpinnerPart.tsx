import { React } from 'tl-react-bridge';
import type { SpinnerPartProps } from 'tl-react-bridge';

/** The class of the spinner. */
const SPINNER_CLASS = 'tl-spinner';

/** The class of a small spinner, which stands in for an icon. */
const SMALL_CLASS = 'tl-spinner--sm';

/**
 * The TopLogic drawing of the part `Spinner` ({@link SpinnerPartProps}): a ring on the track of the
 * progress bar of the design system, with an arc in the color of the brand turning on it. A small
 * one (tl-spinner--sm) has the size of a small icon and turns its arc in the color of the text.
 */
const SpinnerPart: React.FC<SpinnerPartProps> = ({ size, label, className, ...attributes }) => {
  const classes = [SPINNER_CLASS, size === 'small' ? SMALL_CLASS : '', className ?? ''].filter(Boolean).join(' ');
  return label
    ? <span className={classes} role="progressbar" aria-label={label} {...attributes} />
    : <span className={classes} aria-hidden="true" {...attributes} />;
};

export default SpinnerPart;
