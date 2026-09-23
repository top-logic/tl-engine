import { React, useTLState, useTLUpload, rootClassName, tooltipProps, TOOLTIP_WHEN_CLIPPED } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { ThemeIcon } from './icon/ThemeIcon';
import { buttonClassName, useButtonDefaults } from './button/ButtonDefaults';
import type { ButtonAppearance } from './button/ButtonDefaults';

/**
 * A toolbar-style button that opens a native file picker on click and uploads the selected
 * file(s) directly (no intermediate dialog).
 *
 * <p>Renders the same chrome as {@link TLButton} (icon / label / appearance), but instead of
 * dispatching a server command it triggers a hidden {@code <input type="file">} and POSTs the
 * chosen files as repeated {@code file} parts to the upload endpoint. The server-side
 * {@code ReactUploadButtonControl} then processes them.</p>
 */
const TLUploadButton: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const upload = useTLUpload();
  const defaults = useButtonDefaults();
  const fileInputRef = React.useRef<HTMLInputElement | null>(null);
  const [uploading, setUploading] = React.useState(false);

  const label = (state.label as string) ?? '';
  const image = state.image as string | undefined;
  const disabled = state.disabled === true;
  const hidden = state.hidden === true;
  const displayMode = (state.displayMode as string | undefined) ?? 'label-only';
  const resolvedAppearance = (state.appearance as ButtonAppearance | undefined) ?? defaults.appearance ?? 'secondary';
  const accept = state.accept as string | undefined;
  const multiple = state.multiple === true;

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

  const iconOnly = displayMode === 'icon-only';
  const showIcon = displayMode === 'icon-only' || displayMode === 'icon-label';
  const showLabel = displayMode === 'label-only' || displayMode === 'icon-label' || (iconOnly && !image);

  return (
    <span id={controlId} className="tl-upload" hidden={hidden || undefined}>
      <input
        ref={fileInputRef}
        type="file"
        accept={accept && accept !== '*' ? accept : undefined}
        multiple={multiple || undefined}
        onChange={handleChange}
        hidden
      />
      <button
        type="button"
        onClick={handleClick}
        disabled={disabled || uploading}
        aria-busy={uploading ? true : undefined}
        className={rootClassName(state, buttonClassName({ appearance: resolvedAppearance, danger: state.tone === 'danger', small: state.size === 'small' && iconOnly }))}
        aria-label={iconOnly ? label : undefined}
        {...(iconOnly ? tooltipProps(label) : TOOLTIP_WHEN_CLIPPED)}
      >
        {showIcon && image && <ThemeIcon encoded={image} className={'tl-button__icon ' + (showLabel ? 'tl-icon-sm' : 'tl-icon-md')} />}
        {showLabel && <span className="tl-button__label">{label}</span>}
      </button>
    </span>
  );
};

export default TLUploadButton;
