import { React, useTLState, useTLCommand, useTLUpload, useTLDataUrl, useI18N } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const I18N_KEYS = {
  'js.fileChips.add': 'Add file',
  'js.fileChips.remove': 'Remove {0}',
  'js.uploading': 'Uploading…',
  'js.download.file': 'Download {0}',
};

interface Chip {
  key: string;
  name: string;
  size: number | null;
  hasData: boolean;
}

/** Formats a byte count as a compact human-readable size. */
function formatSize(size: number): string {
  if (size < 1024) {
    return size + ' B';
  }
  if (size < 1024 * 1024) {
    return (size / 1024).toFixed(size < 10 * 1024 ? 1 : 0) + ' KB';
  }
  return (size / (1024 * 1024)).toFixed(1) + ' MB';
}

/**
 * Renders a collection of files as chips: file icon, name, size, click-to-download. In edit mode,
 * each chip carries a remove button, and files can be added via the add button or by dropping them
 * onto the control; uploads go to the server control's upload endpoint.
 *
 * State:
 * - chips: [{ key, name, size, hasData }]
 * - editable: boolean
 */
const TLFileChips: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const upload = useTLUpload();
  const dataUrl = useTLDataUrl();
  const t = useI18N(I18N_KEYS);

  const chips = (state.chips as Chip[] | null) ?? [];
  const editable = state.editable === true;

  const [uploading, setUploading] = React.useState(false);
  const [isDragOver, setIsDragOver] = React.useState(false);
  const fileInputRef = React.useRef<HTMLInputElement | null>(null);

  const doUpload = React.useCallback(async (files: FileList | File[]) => {
    const list = Array.from(files);
    if (list.length === 0) {
      return;
    }
    setUploading(true);
    try {
      const formData = new FormData();
      for (const file of list) {
        formData.append('file', file, file.name);
      }
      await upload(formData);
    } finally {
      setUploading(false);
    }
  }, [upload]);

  const doDownload = React.useCallback(async (chip: Chip) => {
    if (!chip.hasData) {
      return;
    }
    try {
      const url = dataUrl + '&key=' + encodeURIComponent(chip.key);
      const resp = await fetch(url);
      if (!resp.ok) {
        console.error('[TLFileChips] Failed to fetch data:', resp.status);
        return;
      }
      const blob = await resp.blob();
      const blobUrl = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = blobUrl;
      a.download = chip.name;
      a.style.display = 'none';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(blobUrl);
    } catch (e) {
      console.error('[TLFileChips] Fetch error:', e);
    }
  }, [dataUrl]);

  const handleFileChange = React.useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      doUpload(e.target.files);
    }
    // Reset so the same file can be uploaded again.
    e.target.value = '';
  }, [doUpload]);

  const handleAddClick = React.useCallback(() => {
    if (uploading) return;
    fileInputRef.current?.click();
  }, [uploading]);

  const handleDragOver = React.useCallback((e: React.DragEvent) => {
    if (!editable) return;
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(true);
  }, [editable]);

  const handleDragLeave = React.useCallback((e: React.DragEvent) => {
    if (!editable) return;
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(false);
  }, [editable]);

  const handleDrop = React.useCallback((e: React.DragEvent) => {
    if (!editable) return;
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(false);
    if (uploading) return;
    if (e.dataTransfer.files) {
      doUpload(e.dataTransfer.files);
    }
  }, [editable, uploading, doUpload]);

  const className = [
    'tlFileChips',
    editable ? 'tlFileChips--editable' : '',
    isDragOver ? 'tlFileChips--dragover' : '',
  ].filter(Boolean).join(' ');

  return (
    <div
      id={controlId}
      className={className}
      onDragOver={handleDragOver}
      onDragLeave={handleDragLeave}
      onDrop={handleDrop}
    >
      {chips.map(chip => {
        const downloadLabel = t['js.download.file'].replace('{0}', chip.name);
        const removeLabel = t['js.fileChips.remove'].replace('{0}', chip.name);
        return (
          <span key={chip.key} className="tlFileChip">
            <button
              type="button"
              className="tlFileChip__main"
              onClick={() => doDownload(chip)}
              disabled={!chip.hasData}
              title={chip.hasData ? downloadLabel : chip.name}
            >
              <svg className="tlFileChip__icon" viewBox="0 0 16 16" width="14" height="14" aria-hidden="true">
                <path d="M9.5 1H4a1 1 0 0 0-1 1v12a1 1 0 0 0 1 1h8a1 1 0 0 0 1-1V4.5L9.5 1z"
                  fill="none" stroke="currentColor" strokeWidth="1.2" strokeLinejoin="round" />
                <path d="M9.5 1v3.5H13" fill="none" stroke="currentColor" strokeWidth="1.2"
                  strokeLinejoin="round" />
              </svg>
              <span className="tlFileChip__name">{chip.name}</span>
              {chip.size != null && (
                <span className="tlFileChip__size">{formatSize(chip.size)}</span>
              )}
            </button>
            {editable && (
              <button
                type="button"
                className="tlFileChip__remove"
                onClick={() => sendCommand('removeChip', { key: chip.key })}
                title={removeLabel}
                aria-label={removeLabel}
              >
                <svg viewBox="0 0 16 16" width="12" height="12" aria-hidden="true">
                  <path d="M4 4l8 8M12 4l-8 8" stroke="currentColor" strokeWidth="1.5"
                    strokeLinecap="round" />
                </svg>
              </button>
            )}
          </span>
        );
      })}
      {editable && (
        <>
          <input
            ref={fileInputRef}
            type="file"
            multiple
            onChange={handleFileChange}
            style={{ display: 'none' }}
          />
          <button
            type="button"
            className={'tlFileChips__add' + (uploading ? ' tlFileChips__add--uploading' : '')}
            onClick={handleAddClick}
            disabled={uploading}
            title={uploading ? t['js.uploading'] : t['js.fileChips.add']}
          >
            <svg viewBox="0 0 16 16" width="14" height="14" aria-hidden="true">
              <path d="M13.5 7.5l-5.6 5.6a3.3 3.3 0 0 1-4.7-4.7l6-6a2.2 2.2 0 0 1 3.1 3.1l-5.8 5.8a1.1 1.1 0 0 1-1.6-1.6l5.2-5.2"
                fill="none" stroke="currentColor" strokeWidth="1.2" strokeLinecap="round" strokeLinejoin="round" />
            </svg>
            <span>{uploading ? t['js.uploading'] : t['js.fileChips.add']}</span>
          </button>
        </>
      )}
    </div>
  );
};

export default TLFileChips;
