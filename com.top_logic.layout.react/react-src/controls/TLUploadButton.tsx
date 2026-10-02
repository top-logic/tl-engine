import { React, useTLState, useTLUpload, rootClassName, tooltipProps, TOOLTIP_WHEN_CLIPPED, ThemeIcon } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { buttonClassName, menuItemProps, useButtonDefaults } from './button/ButtonDefaults';
import type { ButtonAppearance } from './button/ButtonDefaults';

/**
 * A toolbar-style button that opens a native file picker on click and uploads the selected
 * file(s) directly (no intermediate dialog).
 *
 * <p>Renders the same chrome as {@link TLButton} (icon / label / appearance), but instead of
 * dispatching a server command it triggers a hidden {@code <input type="file">} and POSTs the
 * chosen files as repeated {@code file} parts to the upload endpoint. The server-side
 * {@code ReactUploadButtonControl} then processes them.</p>
 *
 * <p>Like {@link TLButton} it follows its container: a compact toolbar makes it a square icon
 * button if it shows its icon, a menu makes it an entry that shows its label.</p>
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
  // Inside a menu the container wins over the server: every button there is an entry.
  const asMenuItem = defaults.appearance === 'menu-item';
  const resolvedAppearance: ButtonAppearance = asMenuItem
    ? 'menu-item'
    : (state.appearance as ButtonAppearance | undefined) ?? defaults.appearance ?? 'secondary';
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

  // Only a button that shows its icon goes compact; inside a menu every button shows its label.
  const shows = displayMode !== 'label-only' && !!image;
  const mode = defaults.iconOnly && shows
    ? 'icon-only'
    : asMenuItem && image ? 'icon-label' : displayMode;
  const iconOnly = mode === 'icon-only';
  const showIcon = mode === 'icon-only' || mode === 'icon-label';
  const showLabel = mode === 'label-only' || mode === 'icon-label' || (iconOnly && !image);
  const part = asMenuItem ? 'tl-menu' : 'tl-button';

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
        className={rootClassName(state, buttonClassName({ appearance: resolvedAppearance, danger: state.tone === 'danger', small: state.size === 'small' && iconOnly, icon: iconOnly && !!image }))}
        aria-label={iconOnly ? label : undefined}
        {...(iconOnly ? tooltipProps(label) : TOOLTIP_WHEN_CLIPPED)}
        {...menuItemProps(defaults)}
      >
        {showIcon && image && <ThemeIcon encoded={image} className={part + '__icon ' + (showLabel ? 'tl-icon-sm' : 'tl-icon-md')} />}
        {showLabel && <span className={part + '__label'}>{label}</span>}
      </button>
    </span>
  );
};

export default TLUploadButton;
