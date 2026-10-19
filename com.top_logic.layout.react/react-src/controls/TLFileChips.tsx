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
 * the chips.
 *
 * State:
 * - chips: [{ key, name, size, hasData }]
 * - editable: boolean
 * - error: string, the error of the last upload; absent: none
 * - maxUploadSize: number, the upload limit (read by the upload channel)
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

  const [isDragOver, setIsDragOver] = React.useState(false);
  const fileInputRef = React.useRef<HTMLInputElement | null>(null);

  const doUpload = React.useCallback((files: FileList | File[]) => {
    upload(Array.from(files, file => ({ name: PART_FILE, file, fileName: file.name })));
  }, [upload]);

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
      data-tl-state={stateAttr(isDragOver && 'dragover')}
      onDragOver={handleDragOver}
      onDragLeave={handleDragLeave}
      onDrop={handleDrop}
    >
      {chips.map(chip => (
        <Chip
          key={chip.key}
          icon={ICON_FILE}
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
            onChange={handleFileChange}
            style={{ display: 'none' }}
          />
          <Button
            label={uploading ? t['js.uploading'] : t['js.fileChips.add']}
            image={ICON_ATTACH}
            display="icon-label"
            appearance="secondary"
            disabled={uploading}
            aria-busy={uploading ? true : undefined}
            onClick={handleAddClick}
          />
        </>
      )}
      {error && (
        <span className="tl-file-upload__status tl-type-label" role="alert">{error}</span>
      )}
    </div>
  );
};

export default TLFileChips;
