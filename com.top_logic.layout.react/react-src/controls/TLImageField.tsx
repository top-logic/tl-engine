import { React, useTLState, useTLCommand, useUpload, useTLDataUrl, useI18N, useListReorder, reorderState, rootClassName, stateAttr, useFieldLabelProps, usePart, ThemeIcon, ICON_UPLOAD, ICON_CLOSE, ICON_HANDLE } from 'tl-react-bridge';
import type { TLCellProps, UploadPart } from 'tl-react-bridge';
import { showsValueOnly } from './form/fieldState';

/** Command removing an image, see ReactImageFieldControl.CMD_REMOVE_IMAGE. */
const CMD_REMOVE_IMAGE = 'removeImage';

/** Command moving an image to another position, see ReactImageFieldControl.CMD_MOVE_IMAGE. */
const CMD_MOVE_IMAGE = 'moveImage';

/** Argument of {@link CMD_REMOVE_IMAGE}: the key of the image to remove. */
const ARG_KEY = 'key';

/** Argument of {@link CMD_MOVE_IMAGE}: the position of the image to move. */
const ARG_INDEX = 'index';

/** Argument of {@link CMD_MOVE_IMAGE}: the position the moved image ends up at. */
const ARG_TARGET_INDEX = 'targetIndex';

/** The name of the multipart part carrying a file. */
const PART_FILE = 'file';

/** The name of the multipart part naming the image an uploaded file replaces, see ReactImageFieldControl.REPLACE_PART. */
const PART_REPLACE = 'replace';

/** The data transfer type of a drag carrying files from outside the browser. */
const FILES_TRANSFER_TYPE = 'Files';

/** The drop target of the area adding images, as opposed to the key of a tile. */
const DROP_AREA = '';

const I18N_KEYS = {
  'js.imageField.upload': 'Upload image',
  'js.imageField.replace': 'Replace {0}',
  'js.imageField.remove': 'Remove {0}',
  'js.image.alt': 'Image',
  'js.reorder.handle': 'Move {0}',
  'js.uploading': 'Uploading…',
};

interface ImageTile {
  key: string;
  name: string;
}

/**
 * Form-field control for images: a preview per image, fetched from the data endpoint of the server
 * control by the key of its tile.
 *
 * While the field is edited, each tile carries a button removing its image and a button picking a
 * file that replaces it; a file dropped on a tile replaces its image as well. A field holding a single
 * image without one, and a field holding several images after its last tile, shows a dashed area
 * that takes a dropped image or opens the file picker on click. Where the order of several images is
 * part of the value, each tile starts with a handle moving it: by dragging the tile by that handle,
 * or with Alt + arrow key while the handle has the focus. A field that is only displayed shows the
 * previews alone. A disabled field shows the tiles and the area of an edited one, without the
 * handles, its buttons and the input of the area natively `disabled`.
 *
 * State:
 * - images: [{ key, name }]
 * - multiple: boolean, whether the field holds several images
 * - ordered: boolean, whether the user arranges the order of several images
 * - accept: string, the file types the file picker offers (HTML accept attribute); absent: all
 * - maxUploadSize: number, the size limit of each single file in bytes (0: none)
 * - error: string, the message of the last failed upload; absent after a successful one
 */
const TLImageField: React.FC<TLCellProps> = ({ controlId }) => {
  const labelProps = useFieldLabelProps(controlId, controlId);
  const state = useTLState();
  const sendCommand = useTLCommand();
  const { uploading, error, upload } = useUpload();
  const dataUrl = useTLDataUrl();
  const t = useI18N(I18N_KEYS);
  const Button = usePart('Button');

  const images = (state.images as ImageTile[] | null) ?? [];
  const multiple = state.multiple === true;
  const accept = (state.accept as string | null) ?? '';
  const disabled = state.disabled === true;
  const valueOnly = showsValueOnly(state);
  const editing = !valueOnly && !disabled;
  // A single image has nowhere to move to.
  const arrangeable = editing && multiple && state.ordered === true && images.length > 1;

  // The key of the tile a file is dropped on, DROP_AREA for the area adding images, null while no
  // file is dragged over the field.
  const [dropTarget, setDropTarget] = React.useState<string | null>(null);
  const replaceInputRef = React.useRef<HTMLInputElement | null>(null);
  // The key of the tile whose image the file picked in the replace input replaces.
  const replacedKeyRef = React.useRef<string | null>(null);

  const doUpload = React.useCallback((files: FileList | File[], replacedKey: string | null) => {
    const list = Array.from(files);
    if (list.length === 0) {
      return;
    }
    const parts: UploadPart[] = (replacedKey == null ? list : list.slice(0, 1))
      .map(file => ({ name: PART_FILE, file, fileName: file.name }));
    if (replacedKey != null) {
      parts.push({ name: PART_REPLACE, file: new Blob([replacedKey]), fileName: PART_REPLACE });
    }
    upload(parts);
  }, [upload]);

  const move = React.useCallback((index: number, targetIndex: number) => {
    sendCommand(CMD_MOVE_IMAGE, { [ARG_INDEX]: index, [ARG_TARGET_INDEX]: targetIndex });
  }, [sendCommand]);

  const reorder = useListReorder({
    axis: 'horizontal',
    onMove: move,
    enabled: arrangeable,
    handle: true,
    count: images.length,
    labelOf: index => images[index].name,
    keyOf: index => images[index].key,
  });

  const isFileDrag = (event: React.DragEvent) => event.dataTransfer.types.includes(FILES_TRANSFER_TYPE);

  /** The drag handlers of an element a file is dropped on: a tile, or the area adding images. */
  const fileDropProps = (target: string) => ({
    onDragOver: (event: React.DragEvent) => {
      if (!editing || uploading || !isFileDrag(event)) {
        return;
      }
      event.preventDefault();
      event.stopPropagation();
      event.dataTransfer.dropEffect = 'copy';
      setDropTarget(target);
    },
    onDragLeave: (event: React.DragEvent) => {
      // Moving over a child of the element leaves the element as well; only leaving it ends the hovering.
      if (event.currentTarget.contains(event.relatedTarget as Node | null)) {
        return;
      }
      setDropTarget(current => (current === target ? null : current));
    },
    onDrop: (event: React.DragEvent) => {
      if (!editing || uploading || !isFileDrag(event)) {
        return;
      }
      event.preventDefault();
      event.stopPropagation();
      setDropTarget(null);
      doUpload(event.dataTransfer.files, target === DROP_AREA ? null : target);
    },
  });

  const imageUrl = (tile: ImageTile) => dataUrl + '&key=' + encodeURIComponent(tile.key);

  const preview = (tile: ImageTile) => (
    <img className="tl-image-field__image" src={imageUrl(tile)} alt={tile.name || t['js.image.alt']} />
  );

  if (valueOnly) {
    return (
      <div id={controlId} {...labelProps} role="group"
        className={rootClassName(state, 'tl-image-field')}>
        {images.map(tile => (
          <div key={tile.key} className="tl-image-field__tile">{preview(tile)}</div>
        ))}
      </div>
    );
  }

  const handleReplaceChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files) {
      doUpload(event.target.files, replacedKeyRef.current);
    }
    replacedKeyRef.current = null;
    // Reset, so that the same file can be picked again.
    event.target.value = '';
  };

  const handleAddChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files) {
      doUpload(event.target.files, null);
    }
    event.target.value = '';
  };

  const pickReplacement = (tile: ImageTile) => {
    if (uploading) return;
    replacedKeyRef.current = tile.key;
    replaceInputRef.current?.click();
  };

  // A single image is replaced by dropping a file on it; several images get an area adding further ones.
  const showsDropArea = multiple || images.length === 0;

  return (
    <div id={controlId} {...labelProps} role="group"
      className={rootClassName(state, 'tl-image-field')}
      data-tl-axis="horizontal"
      {...reorder.containerProps}>
      {images.map((tile, index) => {
        const dropProps = fileDropProps(tile.key);
        const reorderProps = reorder.itemProps(index);
        return (
          <div key={tile.key} className="tl-image-field__tile"
            data-tl-state={stateAttr(reorderState(reorder.itemState(index)), dropTarget === tile.key && 'dragover')}
            {...reorderProps}
            onDragOver={event => {
              dropProps.onDragOver(event);
              if (!event.defaultPrevented) reorderProps.onDragOver?.(event);
            }}
            onDragLeave={dropProps.onDragLeave}
            onDrop={event => {
              dropProps.onDrop(event);
              if (!event.defaultPrevented) reorderProps.onDrop?.(event);
            }}>
            {preview(tile)}
            <div className="tl-image-field__actions">
              {arrangeable && (
                <Button
                  label={t['js.reorder.handle'].replace('{0}', tile.name)}
                  image={ICON_HANDLE}
                  display="icon-only"
                  appearance="secondary"
                  small
                  {...reorder.handleProps(index)}
                />
              )}
              <Button
                label={t['js.imageField.replace'].replace('{0}', tile.name)}
                image={ICON_UPLOAD}
                display="icon-only"
                appearance="secondary"
                small
                disabled={disabled || uploading}
                onClick={() => pickReplacement(tile)}
              />
              <Button
                label={t['js.imageField.remove'].replace('{0}', tile.name)}
                image={ICON_CLOSE}
                display="icon-only"
                appearance="secondary"
                small
                disabled={disabled}
                onClick={() => sendCommand(CMD_REMOVE_IMAGE, { [ARG_KEY]: tile.key })}
              />
            </div>
          </div>
        );
      })}
      {!disabled && (
        <input
          ref={replaceInputRef}
          type="file"
          accept={accept || undefined}
          onChange={handleReplaceChange}
          hidden
        />
      )}
      {showsDropArea && (
        <label className="tl-image-field__drop"
          data-tl-state={stateAttr(dropTarget === DROP_AREA && 'dragover')}
          aria-disabled={disabled ? true : undefined}
          aria-busy={uploading ? true : undefined}
          {...fileDropProps(DROP_AREA)}>
          <input
            type="file"
            className="tl-visually-hidden"
            multiple={multiple}
            accept={accept || undefined}
            disabled={disabled || uploading}
            onChange={handleAddChange}
          />
          <ThemeIcon encoded={ICON_UPLOAD} className="tl-icon-md" />
          <span className="tl-image-field__label">{uploading ? t['js.uploading'] : t['js.imageField.upload']}</span>
        </label>
      )}
      {arrangeable && reorder.announcer}
      {error && (
        <span className="tl-file-upload__status tl-type-label" role="alert">{error}</span>
      )}
    </div>
  );
};

export default TLImageField;
