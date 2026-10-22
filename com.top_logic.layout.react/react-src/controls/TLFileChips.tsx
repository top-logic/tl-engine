import { React, useTLState, useTLCommand, useUpload, useTLDataUrl, useI18N, rootClassName, stateAttr, useFieldLabelProps, usePart, downloadBlob, ICON_FILE, ICON_ATTACH } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/** Command removing a file from the collection. */
const CMD_REMOVE_CHIP = 'removeChip';

/** Argument of {@link CMD_REMOVE_CHIP}: the key of the file to remove. */
const ARG_KEY = 'key';

/** The name of the multipart part carrying a file. */
const PART_FILE = 'file';

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
 * Renders a collection of files as chips (part `Chip`) in a chip group: file icon, name, size as
 * detail, pressed to download where the file has data. In edit mode, each chip offers to remove its
 * file, and files can be added via the add button (part `Button`) or by dropping them onto the group;
 * uploads go to the server control's upload endpoint, the error of the last one is announced below
 * the chips. In little room, a table cell for instance, the control keeps to one line: the chips
 * scroll sideways, each name ends with an ellipsis where it is too long, the sizes are left out, the
 * add button stays at the end of the line, showing its icon alone, its label being its tooltip, and
 * the error is cut short, its full text being its tooltip.
 *
 * State:
 * - chips: [{ key, name, size, hasData }]
 * - editable: boolean
 * - accept: string, the file types the file picker offers (HTML accept attribute); absent: all
 * - maxUploadSize: number, the size limit of each single file in bytes (0: none); the upload channel
 *   refuses a selection holding a larger file before transmitting it
 * - error: string, the message of the last failed upload; absent after a successful one. Hidden
 *   from the moment the next upload starts until the server reports its outcome.
 * - compact: boolean, whether the field is displayed in little room (see FieldState)
 */
const TLFileChips: React.FC<TLCellProps> = ({ controlId }) => {
  const labelProps = useFieldLabelProps(controlId, controlId);
  const state = useTLState();
  const sendCommand = useTLCommand();
  const { uploading, error, upload } = useUpload();
  const dataUrl = useTLDataUrl();
  const t = useI18N(I18N_KEYS);
  const Chip = usePart('Chip');
  const Button = usePart('Button');

  const chips = (state.chips as FileChip[] | null) ?? [];
  const editable = state.editable === true;
  const accept = (state.accept as string | null) ?? '';
  const compact = state.compact === true;

  const [isDragOver, setIsDragOver] = React.useState(false);
  const fileInputRef = React.useRef<HTMLInputElement | null>(null);
  // The state at the start of the latest upload: its error belongs to an earlier upload and is
  // hidden until the server sends the next state.
  const [stateAtUpload, setStateAtUpload] = React.useState<object | null>(null);

  const doUpload = React.useCallback((files: FileList | File[]) => {
    const list = Array.from(files);
    if (list.length === 0) {
      return;
    }
    setStateAtUpload(state);
    upload(list.map(file => ({ name: PART_FILE, file, fileName: file.name })));
  }, [upload, state]);

  const doDownload = React.useCallback((chip: FileChip) => {
    if (!chip.hasData) {
      return;
    }
    downloadBlob(dataUrl + '&key=' + encodeURIComponent(chip.key), chip.name);
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

  return (
    <div
      id={controlId}
      {...labelProps}
      role="group"
      className={rootClassName(state, 'tl-chip-group')}
      data-tl-state={stateAttr(isDragOver && 'dragover', compact && 'compact')}
      onDragOver={handleDragOver}
      onDragLeave={handleDragLeave}
      onDrop={handleDrop}
    >
      <div className="tl-chip-group__chips">
        {chips.map(chip => (
          <Chip
            key={chip.key}
            icon={ICON_FILE}
            label={chip.name}
            detail={chip.size != null && !compact ? formatSize(chip.size) : undefined}
            tooltip={chip.hasData ? t['js.download.file'].replace('{0}', chip.name) : chip.name}
            onClick={chip.hasData ? () => doDownload(chip) : undefined}
            onRemove={editable ? () => sendCommand(CMD_REMOVE_CHIP, { [ARG_KEY]: chip.key }) : undefined}
            removeLabel={t['js.fileChips.remove'].replace('{0}', chip.name)}
          />
        ))}
      </div>
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
            image={ICON_ATTACH}
            display={compact ? 'icon-only' : 'icon-label'}
            small={compact}
            appearance="secondary"
            disabled={uploading}
            aria-busy={uploading ? true : undefined}
            onClick={handleAddClick}
          />
        </>
      )}
      {error && state !== stateAtUpload && (
        <span className="tl-file-upload__status tl-type-label" role="alert"
          title={compact ? error : undefined}>{error}</span>
      )}
    </div>
  );
};

export default TLFileChips;
