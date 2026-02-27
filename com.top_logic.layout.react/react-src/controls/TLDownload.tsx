import { React, useTLState, useTLDataUrl, useTLCommand } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const TLDownload: React.FC<TLCellProps> = () => {
  const state = useTLState();
  const dataUrl = useTLDataUrl();
  const sendCommand = useTLCommand();

  const hasData = !!state.hasData;
  const dataRevision: number = state.dataRevision ?? 0;
  const fileName: string = state.fileName ?? 'download';
  const clearable: boolean = !!state.clearable;

  const [downloading, setDownloading] = React.useState(false);

  const handleDownload = React.useCallback(async () => {
    if (!hasData || downloading) {
      return;
    }

    setDownloading(true);
    try {
      const url = dataUrl + (dataUrl.includes('?') ? '&' : '?') + 'rev=' + dataRevision;
      const resp = await fetch(url);
      if (!resp.ok) {
        console.error('[TLDownload] Failed to fetch data:', resp.status);
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
      console.error('[TLDownload] Fetch error:', e);
    } finally {
      setDownloading(false);
    }
  }, [hasData, downloading, dataUrl, dataRevision, fileName]);

  const handleClear = React.useCallback(async () => {
    if (!hasData) {
      return;
    }
    await sendCommand('clear');
  }, [hasData, sendCommand]);

  if (!hasData) {
    return (
      <div className="tlDownload tlDownload--empty">
        <span className="tlDownload__fileName tlDownload__fileName--empty">No file</span>
      </div>
    );
  }

  return (
    <div className="tlDownload">
      <button
        type="button"
        className={'tlDownload__downloadBtn' + (downloading ? ' tlDownload__downloadBtn--downloading' : '')}
        onClick={handleDownload}
        disabled={downloading}
        title={downloading ? 'Downloading\u2026' : 'Download ' + fileName}
        aria-label={downloading ? 'Downloading\u2026' : 'Download ' + fileName}
      >
        <svg className="tlDownload__downloadIcon" viewBox="0 0 16 16" width="16" height="16" aria-hidden="true">
          <path d="M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" fill="none" />
        </svg>
      </button>
      <span className="tlDownload__fileName" title={fileName}>{fileName}</span>
      {clearable && (
        <button
          type="button"
          className="tlDownload__clearBtn"
          onClick={handleClear}
          title="Clear"
          aria-label="Clear file"
        >
          <svg className="tlDownload__clearIcon" viewBox="0 0 16 16" width="14" height="14" aria-hidden="true">
            <path d="M4 4l8 8M12 4l-8 8" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
          </svg>
        </button>
      )}
    </div>
  );
};

export default TLDownload;
