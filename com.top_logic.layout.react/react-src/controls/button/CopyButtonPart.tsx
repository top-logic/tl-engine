import { React, usePart, useCopyToClipboard, ICON_COPY, ICON_COPIED, ICON_COPY_FAILED } from 'tl-react-bridge';
import type { CopyButtonPartProps } from 'tl-react-bridge';

/** The class of the element around the button and its announcement. */
const COPY_BUTTON_CLASS = 'tl-copy-button';

/** The class of an element read by assistive technology only. */
const VISUALLY_HIDDEN_CLASS = 'tl-visually-hidden';

/** The classes, without the empty ones, joined. */
function classes(...names: (string | false | undefined)[]): string {
  return names.filter(Boolean).join(' ');
}

/**
 * The TopLogic drawing of the part `CopyButton` ({@link CopyButtonPartProps}): the part `Button`,
 * `icon-only`, `ghost` and `small`, showing the copy icon and, for a short while after a successful
 * copy, a check mark, after a failed one a warning, which then is named by the failure. A visually
 * hidden status (`role="status"`) beside it announces the outcome.
 */
const CopyButtonPart: React.FC<CopyButtonPartProps> = ({
  text, label, copiedLabel, failedLabel, className, ...attributes
}) => {
  const Button = usePart('Button');
  const { copied, failed, copy } = useCopyToClipboard(text);
  return (
    <span className={classes(COPY_BUTTON_CLASS, className)} {...attributes}>
      <Button
        label={failed ? failedLabel : label}
        image={copied ? ICON_COPIED : failed ? ICON_COPY_FAILED : ICON_COPY}
        display="icon-only"
        appearance="ghost"
        small
        onClick={event => void copy(event.currentTarget)}
      />
      <span role="status" className={VISUALLY_HIDDEN_CLASS}>{copied ? copiedLabel : failed ? failedLabel : ''}</span>
    </span>
  );
};

export default CopyButtonPart;
