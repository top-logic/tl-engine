import { React, useTLState, useTLUpload, useI18N, rootClassName, tooltipProps, useFieldLabelProps, fieldInputId } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { ThemeIcon } from './icon/ThemeIcon';
import { buttonClassName } from './button/ButtonDefaults';

const I18N_KEYS = {
  'js.fileUpload.choose': 'Choose file',
  'js.uploading': 'Uploading\u2026',
};

type LocalStatus = 'idle' | 'uploading';

const UPLOAD_ICON = 'css:fa-solid fa-upload';

const TLFileUpload: React.FC<TLCellProps> = ({ controlId }) => {
  const inputId = fieldInputId(controlId);
  const labelProps = useFieldLabelProps(controlId, inputId);
  const state = useTLState();
  const upload = useTLUpload();

  const [localStatus, setLocalStatus] = React.useState<LocalStatus>('idle');
  const [isDragOver, setIsDragOver] = React.useState(false);
  const fileInputRef = React.useRef<HTMLInputElement | null>(null);

  const serverStatus = (state.status as string) ?? 'idle';
  const serverError = state.error as string | null;
  const accept = (state.accept as string) ?? '';

  const effectiveStatus = serverStatus === 'received' ? 'idle' : (localStatus !== 'idle' ? localStatus : serverStatus);

  const doUpload = React.useCallback(async (file: File) => {
    setLocalStatus('uploading');
    const formData = new FormData();
    formData.append('file', file, file.name);
    await upload(formData);
    setLocalStatus('idle');
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
    if (localStatus === 'uploading') return;
    fileInputRef.current?.click();
  }, [localStatus]);

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
    if (localStatus === 'uploading') return;
    const file = e.dataTransfer.files?.[0];
    if (file) {
      doUpload(file);
    }
  }, [localStatus, doUpload]);

  const isDisabled = effectiveStatus === 'uploading';
  const t = useI18N(I18N_KEYS);
  const buttonLabel = effectiveStatus === 'uploading' ? t['js.uploading'] : t['js.fileUpload.choose'];

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
        disabled={isDisabled}
        aria-busy={isDisabled ? true : undefined}
        aria-label={buttonLabel}
        {...tooltipProps(buttonLabel)}
        id={inputId}
        {...labelProps}
      >
        <ThemeIcon encoded={UPLOAD_ICON} className="tl-button__icon tl-icon-md" />
      </button>
      {serverError && (
        <span className="tl-file-upload__status tl-type-label" role="alert">{serverError}</span>
      )}
    </div>
  );
};

export default TLFileUpload;
