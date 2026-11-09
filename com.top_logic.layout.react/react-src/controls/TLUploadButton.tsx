import { React, useTLState, useTLUpload, rootClassName, usePart } from 'tl-react-bridge';
import type { TLCellProps, ButtonStateJson } from 'tl-react-bridge';
import { useButtonPresentation } from './button/useButtonPresentation';

/**
 * The state of an upload button: the state of a button and the filter of its file picker.
 */
interface UploadButtonState extends Partial<ButtonStateJson> {
  /** The file types the picker offers (`accept` of the file input); `*` or absent: all. */
  accept?: string;
  /** Whether several files can be picked at once. */
  multiple?: boolean;
}

/**
 * A button that opens a native file picker on click and uploads the selected file(s) directly
 * (no intermediate dialog).
 *
 * <p>The button is drawn by the part {@code Button} and resolved like {@link TLButton}: label,
 * icon, display mode, appearance, tone and size come from the state and the container (a compact
 * toolbar makes it a square icon button if it shows its icon, a menu makes it an entry that shows
 * its label). An explicit tooltip from {@code state.tooltip} - e.g. the reason why the command
 * cannot be executed while the button is disabled - is shown whatever the button displays.</p>
 *
 * <p>Instead of dispatching a server command, a click triggers a hidden
 * {@code <input type="file">} beside the button and POSTs the chosen files as repeated
 * {@code file} parts to the upload endpoint, where the server-side
 * {@code ReactUploadButtonControl} processes them. While the upload runs, the button is disabled
 * and marked busy.</p>
 *
 * <p>A hidden button renders nothing.</p>
 */
const TLUploadButton: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<UploadButtonState>();
  const upload = useTLUpload();
  const fileInputRef = React.useRef<HTMLInputElement | null>(null);
  const [uploading, setUploading] = React.useState(false);

  const image = state.image;
  const disabled = state.disabled === true;
  const accept = state.accept;
  const multiple = state.multiple === true;
  const { appearance, display } = useButtonPresentation({
    image,
    displayMode: state.displayMode,
    appearance: state.appearance,
  });

  const handleClick = React.useCallback(() => {
    if (disabled || uploading) return;
    fileInputRef.current?.click();
  }, [disabled, uploading]);

  const handleChange = React.useCallback(async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (!files || files.length === 0) return;
    const formData = new FormData();
    for (let i = 0; i < files.length; i++) {
      formData.append('file', files[i], files[i].name);
    }
    // Reset so picking the same file(s) again still fires a change event.
    e.target.value = '';
    setUploading(true);
    try {
      await upload(formData);
    } finally {
      setUploading(false);
    }
  }, [upload]);

  const Button = usePart('Button');

  // A hidden button renders nothing: its empty wrapper (e.g. a toolbar item) can then collapse.
  if (state.hidden === true) {
    return null;
  }

  return (
    <span id={controlId} className="tl-upload">
      <input
        ref={fileInputRef}
        type="file"
        accept={accept && accept !== '*' ? accept : undefined}
        multiple={multiple || undefined}
        onChange={handleChange}
        hidden
      />
      <Button
        label={state.label}
        image={image}
        display={display}
        appearance={appearance}
        danger={state.tone === 'danger'}
        small={state.size === 'small'}
        disabled={disabled || uploading}
        tooltip={state.tooltip}
        aria-busy={uploading ? true : undefined}
        className={rootClassName(state, state.cssClasses)}
        onClick={handleClick}
      />
    </span>
  );
};

export default TLUploadButton;
