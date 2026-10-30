import { React, usePart, useCopyToClipboard, ICON_COPY, ICON_COPIED } from 'tl-react-bridge';
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
 * copy, a check mark. A visually hidden status (`role="status"`) beside it announces the copy.
 */
const CopyButtonPart: React.FC<CopyButtonPartProps> = ({ text, label, copiedLabel, className, ...attributes }) => {
  const Button = usePart('Button');
  const { copied, copy } = useCopyToClipboard(text);
  return (
    <span className={classes(COPY_BUTTON_CLASS, className)} {...attributes}>
      <Button
        label={label}
        image={copied ? ICON_COPIED : ICON_COPY}
        display="icon-only"
        appearance="ghost"
        small
        onClick={copy}
      />
      <span role="status" className={VISUALLY_HIDDEN_CLASS}>{copied ? copiedLabel : ''}</span>
    </span>
  );
};

export default CopyButtonPart;
