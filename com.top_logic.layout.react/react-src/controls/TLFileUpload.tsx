import { React, useTLState, useTLUpload } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

type LocalStatus = 'idle' | 'uploading';

const TLFileUpload: React.FC<TLCellProps> = () => {
  const state = useTLState();
  const upload = useTLUpload();

  const [localStatus, setLocalStatus] = React.useState<LocalStatus>('idle');
  const [selectedFileName, setSelectedFileName] = React.useState<string | null>(null);
  const [isDragOver, setIsDragOver] = React.useState(false);
  const fileInputRef = React.useRef<HTMLInputElement | null>(null);

  const serverStatus = (state.status as string) ?? 'idle';
  const serverError = state.error as string | null;
  const serverFileName = state.fileName as string | null;
  const accept = (state.accept as string) ?? '';

  const effectiveStatus = serverStatus === 'received' ? 'idle' : (localStatus !== 'idle' ? localStatus : serverStatus);
  const displayFileName = selectedFileName ?? serverFileName;

  const doUpload = React.useCallback(async (file: File) => {
    setSelectedFileName(file.name);
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
        className="tlFileUpload__button"
        onClick={handleButtonClick}
        disabled={isDisabled}
      >
        {effectiveStatus === 'uploading' ? 'Uploading\u2026' : 'Choose File'}
      </button>
      {displayFileName && (
        <span className="tlFileUpload__fileName">{displayFileName}</span>
      )}
      {serverError && (
        <span className="tlFileUpload__status tlFileUpload__status--error">{serverError}</span>
      )}
    </div>
  );
};

export default TLFileUpload;
