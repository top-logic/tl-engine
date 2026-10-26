import { React, useTLState, useTLDataUrl, useTLCommand, useI18N, rootClassName, stateAttr, usePart, downloadBlob, ICON_CLEAR, ICON_DOWNLOAD } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const I18N_KEYS = {
  'js.download.noFile': 'No file',
  'js.download.file': 'Download {0}',
  'js.downloading': 'Downloading\u2026',
  'js.download.clear': 'Clear',
  'js.download.clearFile': 'Clear file',
};

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
      await downloadBlob(dataUrl + (dataUrl.includes('?') ? '&' : '?') + 'rev=' + dataRevision, fileName);
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
      <div id={controlId} className={rootClassName(state, 'tl-file')} data-tl-state={stateAttr('empty')}>
        <span className="tl-file__name">{t['js.download.noFile']}</span>
      </div>
    );
  }

  const downloadLabel = downloading
    ? t['js.downloading']
    : t['js.download.file'].replace('{0}', fileName);

  return (
    <div id={controlId} className={rootClassName(state, 'tl-file')}>
      <Button
        label={downloadLabel}
        image={ICON_DOWNLOAD}
        display="icon-only"
        appearance="ghost"
        disabled={downloading}
        aria-busy={downloading ? true : undefined}
        onClick={handleDownload}
      />
      <span className="tl-file__name">{fileName}</span>
      {clearable && (
        <Button
          label={t['js.download.clearFile']}
          image={ICON_CLEAR}
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
