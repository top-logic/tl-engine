import { React, ThemeIcon, tooltipProps, useCopyToClipboard, ICON_COPY, ICON_COPIED, ICON_COPY_FAILED } from 'tl-react-bridge';
import type { CopyButtonPartProps } from 'tl-react-bridge';
import IconButton from '@mui/material/IconButton';
import Box from '@mui/material/Box';

/** Size class of the design system for the icon of the button. */
const ICON_CLASS = 'tl-icon-sm';

/** The class of an element read by assistive technology only. */
const VISUALLY_HIDDEN_CLASS = 'tl-visually-hidden';

/**
 * Draws the part `CopyButton` ({@link CopyButtonPartProps}) with Material UI: a small MUI
 * `IconButton` showing the copy icon and, for a short while after a successful copy, a check mark,
 * after a failed one a warning; the copy itself is `useCopyToClipboard` of 'tl-react-bridge'.
 *
 * <p>Mapping from the props: label → `aria-label` and tooltip of the button; copiedLabel → the text of
 * a visually hidden status (`role="status"`) beside the button after a copy; failedLabel → that
 * status and the name and tooltip of the button after a failed copy; text → what is copied;
 * className and the passed-through attributes → the element around button and status.</p>
 */
const MuiCopyButtonAdapter: React.FC<CopyButtonPartProps> = ({
  text, label, copiedLabel, failedLabel, className, ...attributes
}) => {
  const { copied, failed, copy } = useCopyToClipboard(text);
  const name = failed ? failedLabel : label;
  return (
    <Box component="span" sx={{ display: 'inline-flex' }} className={className} {...attributes}>
      <IconButton size="small" aria-label={name} onClick={event => void copy(event.currentTarget)}
        {...tooltipProps(name)}>
        <ThemeIcon encoded={copied ? ICON_COPIED : failed ? ICON_COPY_FAILED : ICON_COPY} className={ICON_CLASS} />
      </IconButton>
      <span role="status" className={VISUALLY_HIDDEN_CLASS}>{copied ? copiedLabel : failed ? failedLabel : ''}</span>
    </Box>
  );
};

export default MuiCopyButtonAdapter;
