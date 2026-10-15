import { React, useTLState, useTLCommand, useTLUpload, useTLDataUrl, useI18N, rootClassName, useFieldLabelProps, usePart } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/** Command removing a file from the collection. */
const CMD_REMOVE_CHIP = 'removeChip';

/** Argument of {@link CMD_REMOVE_CHIP}: the key of the file to remove. */
const ARG_KEY = 'key';

/** The icon of a file. */
const FILE_ICON = 'css:fa-regular fa-file';

/** The icon of the button adding files. */
const ADD_ICON = 'css:fa-solid fa-paperclip';

const I18N_KEYS = {
  'js.fileChips.add': 'Add file',
  'js.fileChips.remove': 'Remove {0}',
  'js.uploading': 'Uploading…',
  'js.download.file': 'Download {0}',
};

interface FileChip {
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
 * Renders a collection of files as chips (part `Chip`): file icon, name, size as detail, pressed to
 * download where the file has data. In edit mode, each chip offers to remove its file, and files can
 * be added via the add button (part `Button`) or by dropping them onto the control; uploads go to the
 * server control's upload endpoint.
 *
 * State:
 * - chips: [{ key, name, size, hasData }]
 * - editable: boolean
 * - accept: string, the file types the file picker offers (HTML accept attribute); absent: all
 */
const TLFileChips: React.FC<TLCellProps> = ({ controlId }) => {
  const labelProps = useFieldLabelProps(controlId, controlId);
  const state = useTLState();
  const sendCommand = useTLCommand();
  const upload = useTLUpload();
  const dataUrl = useTLDataUrl();
  const t = useI18N(I18N_KEYS);
  const Chip = usePart('Chip');
  const Button = usePart('Button');

  const chips = (state.chips as FileChip[] | null) ?? [];
  const editable = state.editable === true;
  const accept = (state.accept as string | null) ?? '';

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

  const doDownload = React.useCallback(async (chip: FileChip) => {
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
      {...labelProps}
      role="group"
      className={rootClassName(state, className)}
      onDragOver={handleDragOver}
      onDragLeave={handleDragLeave}
      onDrop={handleDrop}
    >
      {chips.map(chip => (
        <Chip
          key={chip.key}
          icon={FILE_ICON}
          label={chip.name}
          detail={chip.size != null ? formatSize(chip.size) : undefined}
          tooltip={chip.hasData ? t['js.download.file'].replace('{0}', chip.name) : chip.name}
          onClick={chip.hasData ? () => doDownload(chip) : undefined}
          onRemove={editable ? () => sendCommand(CMD_REMOVE_CHIP, { [ARG_KEY]: chip.key }) : undefined}
          removeLabel={t['js.fileChips.remove'].replace('{0}', chip.name)}
        />
      ))}
      {editable && (
        <>
          <input
            ref={fileInputRef}
            type="file"
            multiple
            accept={accept || undefined}
            onChange={handleFileChange}
            style={{ display: 'none' }}
          />
          <Button
            label={uploading ? t['js.uploading'] : t['js.fileChips.add']}
            image={ADD_ICON}
            display="icon-label"
            appearance="secondary"
            disabled={uploading}
            className={uploading ? 'tlFileChips__add tlFileChips__add--uploading' : 'tlFileChips__add'}
            onClick={handleAddClick}
          />
        </>
      )}
    </div>
  );
};

export default TLFileChips;
