import { React, useTLState, useUpload, useI18N, rootClassName, tooltipProps, useFieldLabelProps, fieldInputId, ThemeIcon, ICON_UPLOAD } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { buttonClassName } from './button/buttonClassName';

const I18N_KEYS = {
  'js.fileUpload.choose': 'Choose file',
  'js.uploading': 'Uploading\u2026',
};

/** The name of the multipart part carrying the file. */
const PART_FILE = 'file';

const TLFileUpload: React.FC<TLCellProps> = ({ controlId }) => {
  const inputId = fieldInputId(controlId);
  const labelProps = useFieldLabelProps(controlId, inputId);
  const state = useTLState();
  const { uploading, error, upload } = useUpload();

  const [isDragOver, setIsDragOver] = React.useState(false);
  const fileInputRef = React.useRef<HTMLInputElement | null>(null);

  const accept = (state.accept as string) ?? '';

  const doUpload = React.useCallback((file: File) => {
    upload([{ name: PART_FILE, file, fileName: file.name }]);
  }, [upload]);

  const handleFileChange = React.useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      doUpload(file);
    }
    // Reset so picking the same file again still fires a change event.
    e.target.value = '';
  }, [doUpload]);

  const handleButtonClick = React.useCallback(() => {
    if (uploading) return;
    fileInputRef.current?.click();
  }, [uploading]);

  const handleDragOver = React.useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(true);
  }, []);

  const handleDragLeave = React.useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(false);
  }, []);

  const handleDrop = React.useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(false);
    if (uploading) return;
    const file = e.dataTransfer.files?.[0];
    if (file) {
      doUpload(file);
    }
  }, [uploading, doUpload]);

  const t = useI18N(I18N_KEYS);
  const buttonLabel = uploading ? t['js.uploading'] : t['js.fileUpload.choose'];

  return (
    <div
      id={controlId}
      className={rootClassName(state, 'tl-file-upload')}
      data-tl-state={isDragOver ? 'dragover' : undefined}
      onDragOver={handleDragOver}
      onDragLeave={handleDragLeave}
      onDrop={handleDrop}
    >
      <input
        ref={fileInputRef}
        type="file"
        accept={accept || undefined}
        onChange={handleFileChange}
        style={{ display: 'none' }}
      />
      <button
        type="button"
        className={buttonClassName({ appearance: 'secondary' })}
        onClick={handleButtonClick}
        disabled={uploading}
        aria-busy={uploading ? true : undefined}
        aria-label={buttonLabel}
        {...tooltipProps(buttonLabel)}
        id={inputId}
        {...labelProps}
      >
        <ThemeIcon encoded={ICON_UPLOAD} className="tl-button__icon tl-icon-md" />
      </button>
      {error && (
        <span className="tl-file-upload__status tl-type-label" role="alert">{error}</span>
      )}
    </div>
  );
};

export default TLFileUpload;
