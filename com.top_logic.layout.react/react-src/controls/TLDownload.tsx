import { React, useTLState, useTLDataUrl, useTLCommand, useI18N, rootClassName, usePart } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const I18N_KEYS = {
  'js.download.noFile': 'No file',
  'js.download.file': 'Download {0}',
  'js.downloading': 'Downloading\u2026',
  'js.download.clear': 'Clear',
  'js.download.clearFile': 'Clear file',
};

/** Icon of the button downloading the file. */
const DOWNLOAD_ICON = 'css:fa-solid fa-download';

/** Icon of the button clearing the file. */
const CLEAR_ICON = 'css:fa-solid fa-xmark';

const TLDownload: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const dataUrl = useTLDataUrl();
  const sendCommand = useTLCommand();

  const hasData = !!state.hasData;
  const dataRevision: number = (state.dataRevision as number | undefined) ?? 0;
  const fileName: string = (state.fileName as string | undefined) ?? 'download';
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

  const t = useI18N(I18N_KEYS);
  const Button = usePart('Button');

  if (!hasData) {
    return (
      <div id={controlId} className={rootClassName(state, 'tlDownload tlDownload--empty')}>
        <span className="tlDownload__fileName tlDownload__fileName--empty">{t['js.download.noFile']}</span>
      </div>
    );
  }

  const downloadLabel = downloading
    ? t['js.downloading']
    : t['js.download.file'].replace('{0}', fileName);

  return (
    <div id={controlId} className={rootClassName(state, 'tlDownload')}>
      <Button
        label={downloadLabel}
        image={DOWNLOAD_ICON}
        display="icon-only"
        appearance="ghost"
        disabled={downloading}
        aria-busy={downloading ? true : undefined}
        onClick={handleDownload}
      />
      <span className="tlDownload__fileName">{fileName}</span>
      {clearable && (
        <Button
          label={t['js.download.clearFile']}
          image={CLEAR_ICON}
          display="icon-only"
          appearance="ghost"
          small
          tooltip={t['js.download.clear']}
          onClick={handleClear}
        />
      )}
    </div>
  );
};

export default TLDownload;
