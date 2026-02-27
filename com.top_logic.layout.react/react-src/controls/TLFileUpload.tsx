import { React, useTLState, useTLUpload } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

type LocalStatus = 'idle' | 'uploading';

const TLFileUpload: React.FC<TLCellProps> = () => {
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

  return (
    <div
      className={`tlFileUpload${isDragOver ? ' tlFileUpload--dragover' : ''}`}
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
        className={'tlFileUpload__button' + (effectiveStatus === 'uploading' ? ' tlFileUpload__button--uploading' : '')}
        onClick={handleButtonClick}
        disabled={isDisabled}
        title={effectiveStatus === 'uploading' ? 'Uploading\u2026' : 'Upload file'}
        aria-label={effectiveStatus === 'uploading' ? 'Uploading\u2026' : 'Upload file'}
      >
        <svg className="tlFileUpload__icon" viewBox="0 0 16 16" width="16" height="16" aria-hidden="true">
          <path d="M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" fill="none" />
        </svg>
      </button>
      {serverError && (
        <span className="tlFileUpload__status tlFileUpload__status--error">{serverError}</span>
      )}
    </div>
  );
};

export default TLFileUpload;
