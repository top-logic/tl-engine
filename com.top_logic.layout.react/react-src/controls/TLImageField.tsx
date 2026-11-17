import { React, useTLState, useTLCommand, useUpload, useTLDataUrl, useI18N, useListReorder, reorderState, rootClassName, stateAttr, useFieldLabelProps, usePart, useFocusTrap, createPortal, ThemeIcon, ICON_UPLOAD, ICON_CLOSE, ICON_HANDLE, ICON_PREVIOUS, ICON_NEXT } from 'tl-react-bridge';
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

/** The name of the multipart part naming the image the uploaded files follow, see ReactImageFieldControl.AFTER_PART. */
const PART_AFTER = 'after';

/** The data transfer type of a drag carrying files from outside the browser. */
const FILES_TRANSFER_TYPE = 'Files';

/** The drop target of the stage and of the area adding images, as opposed to the key of a tile. */
const DROP_AREA = '';

const KEY_ESCAPE = 'Escape';
const KEY_PREVIOUS = 'ArrowLeft';
const KEY_NEXT = 'ArrowRight';

const I18N_KEYS = {
  'js.imageField.upload': 'Upload image',
  'js.imageField.replace': 'Replace {0}',
  'js.imageField.remove': 'Remove {0}',
  'js.imageField.previous': 'Previous image',
  'js.imageField.next': 'Next image',
  'js.imageField.counter': '{0} of {1}',
  'js.imageField.open': 'Show {0} full page',
  'js.imageField.edit': 'Edit images, {0} shown',
  'js.imageField.done': 'Done',
  'js.imageField.close': 'Close',
  'js.image.alt': 'Image',
  'js.reorder.handle': 'Move {0}',
  'js.uploading': 'Uploading…',
};

interface ImageTile {
  key: string;
  name: string;
}

/** Fills the placeholders {0}, {1}, … of the given text with the given values. */
function format(text: string, ...values: (string | number)[]): string {
  return values.reduce<string>((result, value, n) => result.replace('{' + n + '}', String(value)), text);
}

/**
 * Form-field control for images: one image at a time on a stage as wide as the field, fetched from
 * the data endpoint of the server control by the key of its tile.
 *
 * Several images are stepped through with the buttons over the stage or the arrow keys, a counter
 * tells where the field is. A click on the image of a field that is only displayed (or disabled)
 * opens it in a view filling the page, with the same stepping; Escape closes it. A click on the image
 * of an edited field opens the arrange view: a tile per image with a button picking a file that
 * replaces it, a button removing it and, where the order of the images is part of the value, a handle
 * moving it by dragging or with Alt + arrow key; a dashed area after the tiles takes further images.
 * "Done", Escape, or a click on the image of a tile goes back to the stage, at that image.
 *
 * A file dropped on the stage of an edited field replaces a single image and is inserted after the
 * image shown among several; the stage then shows the first image added. Without an image, the stage
 * of an edited field is the dashed area, of a displayed field nothing.
 *
 * State:
 * - images: [{ key, name }]
 * - multiple: boolean, whether the field holds several images
 * - ordered: boolean, whether the user arranges the order of several images
 * - width: string, the CSS width the stage shows an image at most; absent: the width of the field
 * - height: string, the CSS height of the stage; absent: the height of the theme
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

  // The key of the image the stage shows; the first one where it shows none of the images.
  const [currentKey, setCurrentKey] = React.useState<string | null>(null);
  // The position of the image shown, for keeping the place when that image goes away.
  const lastIndexRef = React.useRef(0);
  // The keys of the images before an upload adding images, so that the stage moves to the first added.
  const keysBeforeAddRef = React.useRef<Set<string> | null>(null);
  const [arranging, setArranging] = React.useState(false);
  const [fullPage, setFullPage] = React.useState(false);

  let currentIndex = images.findIndex(tile => tile.key === currentKey);
  if (currentIndex < 0) {
    currentIndex = Math.min(lastIndexRef.current, images.length - 1);
  }
  const current = currentIndex >= 0 ? images[currentIndex] : null;

  React.useEffect(() => {
    lastIndexRef.current = Math.max(currentIndex, 0);
  }, [currentIndex]);

  React.useEffect(() => {
    const before = keysBeforeAddRef.current;
    if (before == null) {
      return;
    }
    const added = images.find(tile => !before.has(tile.key));
    if (added) {
      keysBeforeAddRef.current = null;
      setCurrentKey(added.key);
    }
  }, [images]);

  React.useEffect(() => {
    // A failed upload adds nothing to move to.
    if (error) keysBeforeAddRef.current = null;
  }, [error]);

  React.useEffect(() => {
    // The arrange view belongs to editing; the field that stops being edited goes back to its stage.
    if (!editing) setArranging(false);
  }, [editing]);

  React.useEffect(() => {
    if (images.length === 0) setFullPage(false);
  }, [images.length]);

  // The key of the tile a file is dropped on, DROP_AREA for the stage and for the area adding images,
  // null while no file is dragged over the field.
  const [dropTarget, setDropTarget] = React.useState<string | null>(null);
  const replaceInputRef = React.useRef<HTMLInputElement | null>(null);
  // The key of the tile whose image the file picked in the replace input replaces.
  const replacedKeyRef = React.useRef<string | null>(null);
  const stageButtonRef = React.useRef<HTMLButtonElement | null>(null);

  /**
   * Uploads the given files: replacing the image of the given key, or else adding them after the
   * image of the given key, at the end where none is given.
   */
  const doUpload = React.useCallback((files: FileList | File[], replacedKey: string | null, afterKey: string | null) => {
    const list = Array.from(files);
    if (list.length === 0) {
      return;
    }
    const parts: UploadPart[] = (replacedKey == null ? list : list.slice(0, 1))
      .map(file => ({ name: PART_FILE, file, fileName: file.name }));
    if (replacedKey != null) {
      parts.push({ name: PART_REPLACE, file: new Blob([replacedKey]), fileName: PART_REPLACE });
    } else if (afterKey != null) {
      parts.push({ name: PART_AFTER, file: new Blob([afterKey]), fileName: PART_AFTER });
    }
    keysBeforeAddRef.current = new Set(images.map(tile => tile.key));
    upload(parts);
  }, [upload, images]);

  const move = React.useCallback((index: number, targetIndex: number) => {
    sendCommand(CMD_MOVE_IMAGE, { [ARG_INDEX]: index, [ARG_TARGET_INDEX]: targetIndex });
  }, [sendCommand]);

  const reorder = useListReorder({
    axis: 'horizontal',
    onMove: move,
    enabled: arranging && arrangeable,
    handle: true,
    count: images.length,
    labelOf: index => images[index].name,
    keyOf: index => images[index].key,
  });

  const step = React.useCallback((delta: number) => {
    if (images.length < 2) return;
    const next = (currentIndex + delta + images.length) % images.length;
    setCurrentKey(images[next].key);
  }, [images, currentIndex]);

  /** Steps through the images with the arrow keys; answers whether the key was taken. */
  const stepKeys = (event: React.KeyboardEvent): boolean => {
    if (event.altKey || event.ctrlKey || event.metaKey || images.length < 2) return false;
    if (event.key === KEY_PREVIOUS) step(-1);
    else if (event.key === KEY_NEXT) step(1);
    else return false;
    event.preventDefault();
    return true;
  };

  const isFileDrag = (event: React.DragEvent) => event.dataTransfer.types.includes(FILES_TRANSFER_TYPE);

  /**
   * The drag handlers of an element a file is dropped on: a tile (replacing its image), the stage or
   * the area adding images.
   */
  const fileDropProps = (target: string, onFiles: (files: FileList) => void) => ({
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
      onFiles(event.dataTransfer.files);
    },
  });

  const imageUrl = (tile: ImageTile) => dataUrl + '&key=' + encodeURIComponent(tile.key);

  const altOf = (tile: ImageTile) => tile.name || t['js.image.alt'];

  const handleAddChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files) {
      doUpload(event.target.files, null, null);
    }
    // Reset, so that the same file can be picked again.
    event.target.value = '';
  };

  /** The dashed area taking images by a click opening the file picker or by dropping files. */
  const dropArea = (onFiles: (files: FileList) => void) => (
    <label className="tl-image-field__drop"
      data-tl-state={stateAttr(dropTarget === DROP_AREA && 'dragover')}
      aria-disabled={disabled ? true : undefined}
      aria-busy={uploading ? true : undefined}
      {...fileDropProps(DROP_AREA, onFiles)}>
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
  );

  const errorMessage = error && (
    <span className="tl-file-upload__status tl-type-label" role="alert">{error}</span>
  );

  const counter = (index: number, className: string) => (
    <span className={className + ' tl-type-label'} aria-live="polite">
      {format(t['js.imageField.counter'], index + 1, images.length)}
    </span>
  );

  const stepButtons = (className: string) => images.length > 1 && (
    <>
      <span className={className + ' ' + className + '--previous'}>
        <Button label={t['js.imageField.previous']} image={ICON_PREVIOUS} display="icon-only"
          appearance="secondary" onClick={() => step(-1)} />
      </span>
      <span className={className + ' ' + className + '--next'}>
        <Button label={t['js.imageField.next']} image={ICON_NEXT} display="icon-only"
          appearance="secondary" onClick={() => step(1)} />
      </span>
    </>
  );

  const rootProps = {
    id: controlId,
    ...labelProps,
    role: 'group' as const,
  };

  if (arranging && editing) {
    const handleReplaceChange = (event: React.ChangeEvent<HTMLInputElement>) => {
      if (event.target.files) {
        doUpload(event.target.files, replacedKeyRef.current, null);
      }
      replacedKeyRef.current = null;
      event.target.value = '';
    };

    const pickReplacement = (tile: ImageTile) => {
      if (uploading) return;
      replacedKeyRef.current = tile.key;
      replaceInputRef.current?.click();
    };

    const show = (tile: ImageTile) => {
      setCurrentKey(tile.key);
      setArranging(false);
    };

    // A single image is replaced by dropping a file on it; several images get an area adding further ones.
    const showsDropArea = multiple || images.length === 0;

    return (
      <div {...rootProps}
        className={rootClassName(state, 'tl-image-field', 'tl-image-field--arrange')}
        onKeyDown={event => {
          if (event.key === KEY_ESCAPE && !event.defaultPrevented) {
            event.preventDefault();
            setArranging(false);
          }
        }}>
        <div className="tl-image-field__tiles" data-tl-axis="horizontal" {...reorder.containerProps}>
          {images.map((tile, index) => {
            const dropProps = fileDropProps(tile.key, files => doUpload(files, tile.key, null));
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
                <button type="button" className="tl-image-field__show"
                  aria-label={format(t['js.imageField.open'], altOf(tile))}
                  aria-current={index === currentIndex ? true : undefined}
                  onClick={() => show(tile)}>
                  <img className="tl-image-field__image" src={imageUrl(tile)} alt={altOf(tile)} />
                </button>
                <div className="tl-image-field__actions">
                  {arrangeable && (
                    <Button
                      label={format(t['js.reorder.handle'], tile.name)}
                      image={ICON_HANDLE}
                      display="icon-only"
                      appearance="secondary"
                      small
                      {...reorder.handleProps(index)}
                    />
                  )}
                  <Button
                    label={format(t['js.imageField.replace'], tile.name)}
                    image={ICON_UPLOAD}
                    display="icon-only"
                    appearance="secondary"
                    small
                    disabled={uploading}
                    onClick={() => pickReplacement(tile)}
                  />
                  <Button
                    label={format(t['js.imageField.remove'], tile.name)}
                    image={ICON_CLOSE}
                    display="icon-only"
                    appearance="secondary"
                    small
                    onClick={() => sendCommand(CMD_REMOVE_IMAGE, { [ARG_KEY]: tile.key })}
                  />
                </div>
              </div>
            );
          })}
          <input
            ref={replaceInputRef}
            type="file"
            accept={accept || undefined}
            onChange={handleReplaceChange}
            hidden
          />
          {showsDropArea && dropArea(files => doUpload(files, null, null))}
        </div>
        <div className="tl-image-field__bar">
          <Button label={t['js.imageField.done']} display="label-only" appearance="secondary"
            onClick={() => setArranging(false)} />
        </div>
        {arrangeable && reorder.announcer}
        {errorMessage}
      </div>
    );
  }

  const style: React.CSSProperties = {};
  if (state.width) style.maxInlineSize = state.width as string;
  if (state.height) style.blockSize = state.height as string;

  if (current == null) {
    return (
      <div {...rootProps} className={rootClassName(state, 'tl-image-field')}>
        {!valueOnly && (
          <div className="tl-image-field__stage" data-tl-state="empty" style={style}>
            {dropArea(files => doUpload(files, null, null))}
          </div>
        )}
        {errorMessage}
      </div>
    );
  }

  const openCurrent = () => {
    if (editing) {
      setArranging(true);
    } else {
      setFullPage(true);
    }
  };

  const stageDrop = fileDropProps(DROP_AREA, files => {
    if (multiple) {
      doUpload(files, null, current.key);
    } else {
      doUpload(files, current.key, null);
    }
  });

  return (
    <div {...rootProps} className={rootClassName(state, 'tl-image-field')}>
      <div className="tl-image-field__stage" style={style}
        data-tl-state={stateAttr(dropTarget === DROP_AREA && 'dragover')}
        aria-busy={uploading ? true : undefined}
        onKeyDown={stepKeys}
        {...stageDrop}>
        <button type="button" ref={stageButtonRef} className="tl-image-field__show"
          aria-label={editing
            ? format(t['js.imageField.edit'], altOf(current))
            : format(t['js.imageField.open'], altOf(current))}
          onClick={openCurrent}>
          <img className="tl-image-field__image" src={imageUrl(current)} alt={altOf(current)} />
        </button>
        {stepButtons('tl-image-field__step')}
        {images.length > 1 && counter(currentIndex, 'tl-image-field__counter')}
      </div>
      {errorMessage}
      {fullPage && createPortal(
        <ImageViewer
          tile={current}
          url={imageUrl(current)}
          alt={altOf(current)}
          counter={images.length > 1 ? counter(currentIndex, 'tl-image-viewer__counter') : null}
          stepButtons={stepButtons('tl-image-viewer__step')}
          closeLabel={t['js.imageField.close']}
          onStep={stepKeys}
          onClose={() => setFullPage(false)}
        />,
        document.body,
      )}
    </div>
  );
};

interface ImageViewerProps {
  tile: ImageTile;
  url: string;
  alt: string;
  counter: React.ReactNode;
  stepButtons: React.ReactNode;
  closeLabel: string;
  onStep: (event: React.KeyboardEvent) => boolean;
  onClose: () => void;
}

/**
 * The view showing an image filling the page, over a scrim: the buttons stepping through the images,
 * the counter, and a button closing the view. Escape and a click on the scrim close it as well; the
 * focus goes back to where it was.
 */
const ImageViewer: React.FC<ImageViewerProps> = ({ tile, url, alt, counter, stepButtons, closeLabel, onStep, onClose }) => {
  const ref = React.useRef<HTMLDivElement | null>(null);
  const Button = usePart('Button');
  useFocusTrap(true, ref, 'container');

  return (
    <div ref={ref} className="tl-image-viewer" role="dialog" aria-modal="true" aria-label={alt} tabIndex={-1}
      onKeyDown={event => {
        if (event.key === KEY_ESCAPE) {
          event.preventDefault();
          event.stopPropagation();
          onClose();
          return;
        }
        onStep(event);
      }}
      onClick={event => {
        if (event.target === event.currentTarget) onClose();
      }}>
      <img key={tile.key} className="tl-image-viewer__image" src={url} alt={alt} />
      {stepButtons}
      {counter}
      <span className="tl-image-viewer__close">
        <Button label={closeLabel} image={ICON_CLOSE} display="icon-only" appearance="secondary" onClick={onClose} />
      </span>
    </div>
  );
};

export default TLImageField;
