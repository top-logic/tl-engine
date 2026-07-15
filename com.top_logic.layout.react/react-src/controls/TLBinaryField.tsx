import { React, useTLState, useTLUpload, useTLDataUrl, useI18N } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const I18N_KEYS = {
  'js.fileUpload.choose': 'Choose file',
  'js.uploading': 'Uploading…',
  'js.download.noFile': 'No file',
  'js.download.file': 'Download {0}',
  'js.downloading': 'Downloading…',
};

type LocalStatus = 'idle' | 'uploading';

/**
 * Form-field control for a binary ({@code tl.core:Binary}) attribute. Renders a file upload
 * (drag-and-drop + button) in edit mode and a download link in view mode. The actual bytes are
 * fetched from / sent to the server control via the data and upload endpoints.
 */
const TLBinaryField: React.FC<TLCellProps> = ({ controlId, state: propState }) => {
  const liveState = useTLState();
  const state = liveState ?? propState ?? {};
  const upload = useTLUpload();
  const dataUrl = useTLDataUrl();
  const t = useI18N(I18N_KEYS);

  const editable = state.editable !== false;
  const hasData = !!state.hasData;
  const fileName = (state.fileName as string) ?? 'download';
  const dataRevision = (state.dataRevision as number) ?? 0;
  const accept = (state.accept as string) ?? '';
  const serverStatus = (state.status as string) ?? 'idle';
  const serverError = (state.error as string) ?? null;

  const [localStatus, setLocalStatus] = React.useState<LocalStatus>('idle');
  const [isDragOver, setIsDragOver] = React.useState(false);
  const [downloading, setDownloading] = React.useState(false);
  const fileInputRef = React.useRef<HTMLInputElement | null>(null);

  const doDownload = React.useCallback(async () => {
    if (!hasData || downloading) {
      return;
    }
    setDownloading(true);
    try {
      const url = dataUrl + (dataUrl.includes('?') ? '&' : '?') + 'rev=' + dataRevision;
      const resp = await fetch(url);
      if (!resp.ok) {
        console.error('[TLBinaryField] Failed to fetch data:', resp.status);
        return;
      }
      const blob = await resp.blob();
      const blobUrl = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = blobUrl;
      a.download = fileName;
      a.style.display = 'none';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(blobUrl);
    } catch (e) {
      console.error('[TLBinaryField] Fetch error:', e);
    } finally {
      setDownloading(false);
    }
  }, [hasData, downloading, dataUrl, dataRevision, fileName]);

  const doUpload = React.useCallback(async (file: File) => {
    setLocalStatus('uploading');
    const formData = new FormData();
    formData.append('file', file, file.name);
    await upload(formData);
    setLocalStatus('idle');
  }, [upload]);

  const isUploading = (serverStatus === 'received' ? 'idle' : (localStatus !== 'idle' ? localStatus : serverStatus)) === 'uploading';

  // All hooks must run unconditionally (Rules of Hooks), so the edit-mode handlers are declared
  // before the view-mode early return below.
  const handleFileChange = React.useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      doUpload(file);
    }
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

  const downloadLink = (
    <span className="tlDownload">
      <button
        type="button"
        className={'tlDownload__downloadBtn' + (downloading ? ' tlDownload__downloadBtn--downloading' : '')}
        onClick={doDownload}
        disabled={downloading}
        title={downloadLabel}
        aria-label={downloadLabel}
      >
        <svg className="tlDownload__downloadIcon" viewBox="0 0 16 16" width="16" height="16" aria-hidden="true">
          <path d="M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" fill="none" />
        </svg>
      </button>
      <span className="tlDownload__fileName" title={fileName}>{fileName}</span>
    </span>
  );

  // View (read-only) mode: just the download link, or a "no file" hint.
  if (!editable) {
    if (!hasData) {
      return (
        <div id={controlId} className="tlBinaryField tlDownload tlDownload--empty">
          <span className="tlDownload__fileName tlDownload__fileName--empty">{t['js.download.noFile']}</span>
        </div>
      );
    }
    return (
      <div id={controlId} className="tlBinaryField tlBinaryField--view">
        {downloadLink}
      </div>
    );
  }

  // Edit mode: drag-and-drop upload + button, plus a download link for the current file.
  const isDisabled = isUploading;
  const buttonLabel = isUploading ? t['js.uploading'] : t['js.fileUpload.choose'];

  return (
    <div
      id={controlId}
      className={`tlBinaryField tlFileUpload${isDragOver ? ' tlFileUpload--dragover' : ''}`}
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
        className={'tlFileUpload__button' + (isDisabled ? ' tlFileUpload__button--uploading' : '')}
        onClick={handleButtonClick}
        disabled={isDisabled}
        title={buttonLabel}
        aria-label={buttonLabel}
      >
        <svg className="tlFileUpload__icon" viewBox="0 0 16 16" width="16" height="16" aria-hidden="true">
          <path d="M8 10V1m0 0L4.5 4.5M8 1l3.5 3.5M2 13h12" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" fill="none" />
        </svg>
      </button>
      {hasData && downloadLink}
      {serverError && (
        <span className="tlFileUpload__status tlFileUpload__status--error">{serverError}</span>
      )}
    </div>
  );
};

export default TLBinaryField;
