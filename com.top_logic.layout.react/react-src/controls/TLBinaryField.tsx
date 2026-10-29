import { React, useTLState, useUpload, useTLDataUrl, useI18N, rootClassName, stateAttr, useFieldLabelProps, fieldInputId, usePart, downloadBlob, ICON_UPLOAD, ICON_DOWNLOAD } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { showsValueOnly } from './form/fieldState';

const I18N_KEYS = {
  'js.fileUpload.choose': 'Choose file',
  'js.uploading': 'Uploading…',
  'js.download.noFile': 'No file',
  'js.download.file': 'Download {0}',
  'js.downloading': 'Downloading…',
};

/** The name of the multipart part carrying the file. */
const PART_FILE = 'file';

/**
 * Form-field control for a binary ({@code tl.core:Binary}) attribute. Renders a file upload
 * (drag-and-drop + button) in edit mode and a download link in view mode. The actual bytes are
 * fetched from / sent to the server control via the data and upload endpoints.
 *
 * A disabled field renders the upload of edit mode as an inactive one: the upload and download
 * buttons are natively `disabled`, and the field neither opens the file picker nor takes a dropped
 * file (see showsValueOnly).
 */
const TLBinaryField: React.FC<TLCellProps> = ({ controlId, state: propState }) => {
  const inputId = fieldInputId(controlId);
  const labelProps = useFieldLabelProps(controlId, inputId);
  const liveState = useTLState();
  const state = liveState ?? propState ?? {};
  const { uploading: isUploading, error, upload } = useUpload();
  const dataUrl = useTLDataUrl();
  const t = useI18N(I18N_KEYS);
  const Button = usePart('Button');

  const disabled = state.disabled === true;
  const hasData = !!state.hasData;
  const fileName = (state.fileName as string) ?? 'download';
  const dataRevision = (state.dataRevision as number) ?? 0;
  const accept = (state.accept as string) ?? '';

  const [isDragOver, setIsDragOver] = React.useState(false);
  const [downloading, setDownloading] = React.useState(false);
  const fileInputRef = React.useRef<HTMLInputElement | null>(null);

  const doDownload = React.useCallback(async () => {
    if (!hasData || downloading) {
      return;
    }
    setDownloading(true);
    try {
      await downloadBlob(dataUrl + (dataUrl.includes('?') ? '&' : '?') + 'rev=' + dataRevision, fileName);
    } finally {
      setDownloading(false);
    }
  }, [hasData, downloading, dataUrl, dataRevision, fileName]);

  const doUpload = React.useCallback((file: File) => {
    upload([{ name: PART_FILE, file, fileName: file.name }]);
  }, [upload]);

  // All hooks must run unconditionally (Rules of Hooks), so the edit-mode handlers are declared
  // before the view-mode early return below.
  const handleFileChange = React.useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      doUpload(file);
    }
    // Reset so picking the same file again still fires a change event.
    e.target.value = '';
  }, [doUpload]);

  const handleButtonClick = React.useCallback(() => {
    if (isUploading) return;
    fileInputRef.current?.click();
  }, [isUploading]);

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
    if (isUploading) return;
    const file = e.dataTransfer.files?.[0];
    if (file) {
      doUpload(file);
    }
  }, [isUploading, doUpload]);

  const downloadLabel = downloading
    ? t['js.downloading']
    : t['js.download.file'].replace('{0}', fileName);

  // The file row: the download button and the name of the file.
  const fileRow = (
    <>
      <Button
        label={downloadLabel}
        image={ICON_DOWNLOAD}
        display="icon-only"
        appearance="ghost"
        disabled={downloading || disabled}
        aria-busy={downloading ? true : undefined}
        onClick={doDownload}
      />
      <span className="tl-file__name">{fileName}</span>
    </>
  );

  // View (read-only) mode: just the download link, or a "no file" hint.
  if (showsValueOnly(state)) {
    if (!hasData) {
      return (
        <div id={controlId} className={rootClassName(state, 'tl-file')} data-tl-state={stateAttr('empty')}>
          <span className="tl-file__name">{t['js.download.noFile']}</span>
        </div>
      );
    }
    return <div id={controlId} className={rootClassName(state, 'tl-file')}>{fileRow}</div>;
  }

  // Edit mode: drag-and-drop upload + button, plus a download link for the current file.
  const isDisabled = isUploading || disabled;
  const buttonLabel = isUploading ? t['js.uploading'] : t['js.fileUpload.choose'];

  return (
    <div
      id={controlId}
      className={rootClassName(state, 'tl-file-upload')}
      data-tl-state={isDragOver ? 'dragover' : undefined}
      onDragOver={disabled ? undefined : handleDragOver}
      onDragLeave={disabled ? undefined : handleDragLeave}
      onDrop={disabled ? undefined : handleDrop}
    >
      {!disabled && (
        <input
          ref={fileInputRef}
          type="file"
          accept={accept || undefined}
          onChange={handleFileChange}
          style={{ display: 'none' }}
        />
      )}
      <Button
        id={inputId}
        label={buttonLabel}
        image={ICON_UPLOAD}
        display="icon-only"
        appearance="secondary"
        disabled={isDisabled}
        aria-busy={isUploading ? true : undefined}
        onClick={handleButtonClick}
        {...labelProps}
      />
      {hasData && <span className="tl-file">{fileRow}</span>}
      {error && (
        <span className="tl-file-upload__status tl-type-label" role="alert">{error}</span>
      )}
    </div>
  );
};

export default TLBinaryField;
