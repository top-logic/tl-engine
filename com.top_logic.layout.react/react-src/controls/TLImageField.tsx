import { React, useTLState, useTLCommand, useUpload, useTLDataUrl, useI18N, useListReorder, reorderState, rootClassName, stateAttr, useFieldLabelProps, usePart, useFocusTrap, createPortal, ThemeIcon, ICON_ADD, ICON_UPLOAD, ICON_REMOVE, ICON_CLOSE, ICON_HANDLE, ICON_PREVIOUS, ICON_NEXT, ICON_MANAGE_IMAGES } from 'tl-react-bridge';
import type { TLCellProps, UploadPart } from 'tl-react-bridge';
import { showsValueOnly } from './form/fieldState';
import { isFull } from './form/valueCount';

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
  'js.imageField.add': 'Add image',
  'js.imageField.replaceShown': 'Replace',
  'js.imageField.removeShown': 'Remove',
  'js.imageField.manage': 'Manage images',
  'js.imageField.editorTitle': 'Images ({0})',
  'js.imageField.replace': 'Replace {0}',
  'js.imageField.remove': 'Remove {0}',
  'js.imageField.previous': 'Previous image',
  'js.imageField.next': 'Next image',
  'js.imageField.counter': '{0} of {1}',
  'js.imageField.open': 'Show {0} full page',
  'js.imageField.show': 'Show {0}',
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
 * the data endpoint of the server control by the key of its tile. The form never shows more than
 * the stage; several images are arranged in an editor covering the page.
 *
 * The stage of several images has a fixed height, so stepping never moves the layout; without an
 * image, an edited field shows the dashed area filling it. The stage of a single image is exactly as
 * large as the image, at most the height of the theme or of the dimensions; without an image, an
 * edited field shows a small dashed area. An image is never enlarged; a smaller one stands centered.
 *
 * Several images are stepped through with the buttons over the stage or the arrow keys, a counter
 * tells where the field is. A click on the image opens it in a view covering the page opaquely, with
 * the same stepping; Escape closes it. This holds for a displayed, a disabled and an edited field alike.
 *
 * An edited field holding an image has a bar of buttons below the stage: "Add image" (several images
 * only) picks files inserted after the image shown, "Replace" picks a file replacing the image
 * shown, "Remove" removes it, and "Manage images" (several images only) opens the editor: a tile per
 * image with a button picking a file that replaces it (a file dropped on the tile replaces it as well),
 * a button removing it and, where the order of the images is part of the value, a handle moving it
 * by dragging or with Alt + arrow key; a dashed tile after the others takes further images. "Done"
 * or Escape closes the editor, a click on the image of a tile closes it and shows that image on the
 * stage. An edited field of several images keeps the bar with "Add image" alone while it is empty,
 * so that the first image does not move the layout.
 *
 * A file dropped on the stage of an edited field replaces a single image and is inserted after the
 * image shown among several; the stage then shows the first image added. Without an image, the stage
 * of an edited field is the dashed area, of a displayed field nothing.
 *
 * Several images at their maximum number take no further one: "Add image", the dashed tile of the
 * editor and the stage taking dropped files are left out, the images are still replaced and removed.
 *
 * From the files picked or dropped until the image they become is visible, the stage shows a spinner
 * over the image, the dashed areas a spinner and "Uploading…", and the buttons of the bar are
 * disabled; in the editor, the tile whose image is replaced shows the spinner. An image whose data
 * has not arrived yet, e.g. after stepping, shows the spinner as well.
 *
 * State:
 * - images: [{ key, name }]
 * - multiple: boolean, whether the field holds several images
 * - ordered: boolean, whether the user arranges the order of several images
 * - width: string, the CSS width the stage shows an image at most; absent: the width of the field
 * - height: string, the CSS height of the stage of several images, the largest height of a single
 *   image; absent: the height of the theme
 * - accept: string, the file types the file picker offers (HTML accept attribute); absent: all
 * - maxUploadSize: number, the size limit of each single file in bytes (0: none)
 * - maxCount: number, the maximum number of several images (0: none); at the maximum, the add
 *   button, the area adding images in the editor and the stage taking dropped files are left out,
 *   the images can still be replaced and removed
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
  const Spinner = usePart('Spinner');
  const editorTitleId = React.useId();

  const images = (state.images as ImageTile[] | null) ?? [];
  const multiple = state.multiple === true;
  const accept = (state.accept as string | null) ?? '';
  const disabled = state.disabled === true;
  const valueOnly = showsValueOnly(state);
  const editing = !valueOnly && !disabled;
  // A single image has nowhere to move to.
  const arrangeable = editing && multiple && state.ordered === true && images.length > 1;
  // Several images up to their maximum take further ones.
  const addable = multiple && !isFull(images.length, state.maxCount);

  // The key of the image the stage shows; the first one where it shows none of the images.
  const [currentKey, setCurrentKey] = React.useState<string | null>(null);
  // The position of the image shown, for keeping the place when that image goes away.
  const lastIndexRef = React.useRef(0);
  // The keys of the images before an upload adding images, so that the stage moves to the first added.
  const keysBeforeAddRef = React.useRef<Set<string> | null>(null);
  const [managing, setManaging] = React.useState(false);
  const [fullPage, setFullPage] = React.useState(false);

  let currentIndex = images.findIndex(tile => tile.key === currentKey);
  if (currentIndex < 0) {
    currentIndex = Math.min(lastIndexRef.current, images.length - 1);
  }
  const current = currentIndex >= 0 ? images[currentIndex] : null;

  React.useEffect(() => {
    lastIndexRef.current = Math.max(currentIndex, 0);
  }, [currentIndex]);

  // The keys of the images whose data has arrived (or failed to), at the stage or at a tile.
  const [loadedKeys, setLoadedKeys] = React.useState<ReadonlySet<string>>(() => new Set());
  // The key of the image an upload added (or replaced an image by), until its data has arrived.
  const [arrivedKey, setArrivedKey] = React.useState<string | null>(null);
  // The key of the image an upload replaces, while the upload runs.
  const [replacingKey, setReplacingKey] = React.useState<string | null>(null);

  /** Notes that the data of the image of the given key has arrived (or failed to). */
  const loaded = React.useCallback((key: string) => {
    setLoadedKeys(keys => (keys.has(key) ? keys : new Set(keys).add(key)));
  }, []);

  // From the files picked or dropped until the image they become is visible.
  const working = uploading || (arrivedKey != null && !loadedKeys.has(arrivedKey));

  React.useEffect(() => {
    const before = keysBeforeAddRef.current;
    if (before == null) {
      return;
    }
    const added = images.find(tile => !before.has(tile.key));
    if (added) {
      keysBeforeAddRef.current = null;
      setCurrentKey(added.key);
      setArrivedKey(added.key);
    }
  }, [images]);

  React.useEffect(() => {
    if (!uploading) setReplacingKey(null);
  }, [uploading]);

  React.useEffect(() => {
    // A failed upload adds nothing to move to.
    if (error) keysBeforeAddRef.current = null;
  }, [error]);

  React.useEffect(() => {
    // The editor belongs to editing; it closes when the field stops being edited.
    if (!editing) setManaging(false);
  }, [editing]);

  React.useEffect(() => {
    if (images.length === 0) setFullPage(false);
  }, [images.length]);

  // The key of the tile a file is dropped on, DROP_AREA for the stage and for the area adding images,
  // null while no file is dragged over the field.
  const [dropTarget, setDropTarget] = React.useState<string | null>(null);
  const addInputRef = React.useRef<HTMLInputElement | null>(null);
  const replaceInputRef = React.useRef<HTMLInputElement | null>(null);
  // The key of the image the file picked in the replace input replaces.
  const replacedKeyRef = React.useRef<string | null>(null);

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
    setReplacingKey(replacedKey);
    upload(parts);
  }, [upload, images]);

  const move = React.useCallback((index: number, targetIndex: number) => {
    sendCommand(CMD_MOVE_IMAGE, { [ARG_INDEX]: index, [ARG_TARGET_INDEX]: targetIndex });
  }, [sendCommand]);

  const remove = (tile: ImageTile) => sendCommand(CMD_REMOVE_IMAGE, { [ARG_KEY]: tile.key });

  const reorder = useListReorder({
    axis: 'horizontal',
    onMove: move,
    enabled: managing && arrangeable,
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
      if (!editing || working || !isFileDrag(event)) {
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
      if (!editing || working || !isFileDrag(event)) {
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

  /** Runs the given upload with the files picked in an input, and resets the input for the next pick. */
  const picked = (onFiles: (files: FileList) => void) => (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files) {
      onFiles(event.target.files);
    }
    // Reset, so that the same file can be picked again.
    event.target.value = '';
  };

  /** Opens the file picker for a file replacing the image of the given tile. */
  const pickReplacement = (tile: ImageTile) => {
    if (working) return;
    replacedKeyRef.current = tile.key;
    replaceInputRef.current?.click();
  };

  /** The dashed area taking images by a click opening the file picker or by dropping files. */
  const dropArea = (onFiles: (files: FileList) => void) => (
    <label className="tl-image-field__drop"
      data-tl-state={stateAttr(dropTarget === DROP_AREA && 'dragover')}
      aria-disabled={disabled ? true : undefined}
      aria-busy={working ? true : undefined}
      {...fileDropProps(DROP_AREA, onFiles)}>
      <input
        type="file"
        className="tl-visually-hidden"
        multiple={multiple}
        accept={accept || undefined}
        disabled={disabled || working}
        onChange={picked(onFiles)}
      />
      {working ? <Spinner /> : <ThemeIcon encoded={ICON_UPLOAD} className="tl-icon-md" />}
      <span className="tl-image-field__label">{working ? t['js.uploading'] : t['js.imageField.upload']}</span>
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

  // The stage of several images has the height of the dimensions; a single image is at most that high.
  const style: React.CSSProperties = {};
  if (state.width) style.maxInlineSize = state.width as string;
  const imageStyle: React.CSSProperties = {};
  if (state.height) {
    if (multiple || images.length === 0) {
      style.blockSize = state.height as string;
    } else {
      imageStyle.maxBlockSize = state.height as string;
    }
  }
  const rootClass = rootClassName(state, 'tl-image-field', !multiple && 'tl-image-field--single');

  /** The hidden input picking the file that replaces the image {@link pickReplacement} was called for. */
  const replaceInput = () => editing && (
    <input ref={replaceInputRef} type="file" accept={accept || undefined} hidden
      onChange={picked(files => {
        const replacedKey = replacedKeyRef.current;
        replacedKeyRef.current = null;
        if (replacedKey != null) doUpload(files, replacedKey, null);
      })} />
  );

  /** The editor covering the page: a tile per image and a tile adding further images. */
  const editor = () => {
    const show = (tile: ImageTile) => {
      setCurrentKey(tile.key);
      setManaging(false);
    };

    return createPortal(
      <ImageOverlay labelledBy={editorTitleId} onClose={() => setManaging(false)}>
        <div className="tl-image-editor">
          <div className="tl-titlebar">
            <h2 className="tl-titlebar__title tl-type-heading-sm" id={editorTitleId}>
              {format(t['js.imageField.editorTitle'], images.length)}
            </h2>
            <Button label={t['js.imageField.done']} display="label-only" appearance="primary"
              onClick={() => setManaging(false)} />
          </div>
          <div className="tl-image-editor__tiles" data-tl-axis="horizontal" {...reorder.containerProps}>
            {images.map((tile, index) => {
              const dropProps = fileDropProps(tile.key, files => doUpload(files, tile.key, null));
              const reorderProps = reorder.itemProps(index);
              return (
                <div key={tile.key} className="tl-image-editor__tile"
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
                  <button type="button" className="tl-image-editor__show"
                    aria-label={format(t['js.imageField.show'], altOf(tile))}
                    aria-current={index === currentIndex ? true : undefined}
                    onClick={() => show(tile)}>
                    <img className="tl-image-editor__image" src={imageUrl(tile)} alt={altOf(tile)}
                      onLoad={() => loaded(tile.key)} onError={() => loaded(tile.key)} />
                    {(tile.key === replacingKey || !loadedKeys.has(tile.key)) && busyLayer('tl-image-editor__busy')}
                  </button>
                  <div className="tl-image-editor__actions">
                    {arrangeable && (
                      <Button
                        label={format(t['js.reorder.handle'], tile.name)}
                        image={ICON_HANDLE}
                        display="icon-only"
                        appearance="ghost"
                        small
                        {...reorder.handleProps(index)}
                      />
                    )}
                    <Button
                      label={format(t['js.imageField.replace'], tile.name)}
                      image={ICON_UPLOAD}
                      display="icon-only"
                      appearance="ghost"
                      small
                      disabled={working}
                      onClick={() => pickReplacement(tile)}
                    />
                    <Button
                      label={format(t['js.imageField.remove'], tile.name)}
                      image={ICON_REMOVE}
                      display="icon-only"
                      appearance="ghost"
                      small
                      onClick={() => remove(tile)}
                    />
                  </div>
                </div>
              );
            })}
            {addable && dropArea(files => doUpload(files, null, null))}
          </div>
          {arrangeable && reorder.announcer}
          {errorMessage}
        </div>
      </ImageOverlay>,
      document.body,
    );
  };

  /** The layer over an image whose data is on its way: a translucent cover with the spinner. */
  const busyLayer = (className: string) => (
    <span className={className}>
      <span className="tl-image-field__indicator"><Spinner /></span>
    </span>
  );

  /**
   * The button of the bar picking images that are added after the image of the given key, at the end
   * where none is given, with its hidden file input.
   */
  const addButton = (afterKey: string | null) => (
    <>
      <Button label={t['js.imageField.add']} image={ICON_ADD} display="icon-label"
        appearance="secondary" disabled={working}
        onClick={() => addInputRef.current?.click()} />
      <input ref={addInputRef} type="file" multiple accept={accept || undefined} hidden
        onChange={picked(files => doUpload(files, null, afterKey))} />
    </>
  );

  if (current == null) {
    return (
      <div {...rootProps} className={rootClass}>
        {!valueOnly && (
          <div className="tl-image-field__stage" data-tl-state="empty" style={style}>
            {dropArea(files => doUpload(files, null, null))}
          </div>
        )}
        {editing && multiple && (
          // The bar of several images stays while the field is empty, so the first image does not move the layout.
          <div className="tl-image-field__bar">{addable && addButton(null)}</div>
        )}
        {!managing && errorMessage}
        {replaceInput()}
        {managing && editor()}
      </div>
    );
  }

  // A file dropped on the stage replaces a single image, and is added to several images up to their maximum.
  const stageDrop = !multiple
    ? fileDropProps(DROP_AREA, files => doUpload(files, current.key, null))
    : addable ? fileDropProps(DROP_AREA, files => doUpload(files, null, current.key)) : {};

  // The stage works while an upload runs and until the data of the image it shows has arrived.
  const stageBusy = working || !loadedKeys.has(current.key);

  return (
    <div {...rootProps} className={rootClass}>
      <div className="tl-image-field__stage" style={style}
        data-tl-state={stateAttr(dropTarget === DROP_AREA && 'dragover')}
        aria-busy={stageBusy ? true : undefined}
        onKeyDown={stepKeys}
        {...stageDrop}>
        <button type="button" className="tl-image-field__show"
          aria-label={format(t['js.imageField.open'], altOf(current))}
          onClick={() => setFullPage(true)}>
          <img className="tl-image-field__image" src={imageUrl(current)} alt={altOf(current)} style={imageStyle}
            onLoad={() => loaded(current.key)} onError={() => loaded(current.key)} />
        </button>
        {stageBusy && busyLayer('tl-image-field__busy')}
        {stepButtons('tl-image-field__step')}
        {images.length > 1 && counter(currentIndex, 'tl-image-field__counter')}
      </div>
      {editing && (
        <div className="tl-image-field__bar">
          {addable && addButton(current.key)}
          <Button label={t['js.imageField.replaceShown']} image={ICON_UPLOAD} display="icon-label"
            appearance="secondary" disabled={working}
            onClick={() => pickReplacement(current)} />
          <Button label={t['js.imageField.removeShown']} image={ICON_REMOVE} display="icon-label"
            appearance="secondary" disabled={working}
            onClick={() => remove(current)} />
          {multiple && (
            <Button label={t['js.imageField.manage']} image={ICON_MANAGE_IMAGES} display="icon-label"
              appearance="secondary" disabled={working}
              onClick={() => setManaging(true)} />
          )}
        </div>
      )}
      {!managing && errorMessage}
      {replaceInput()}
      {managing && editor()}
      {fullPage && createPortal(
        <ImageOverlay
          view
          label={altOf(current)}
          closeLabel={t['js.imageField.close']}
          stepButtons={stepButtons('tl-image-overlay__step')}
          counter={images.length > 1 ? counter(currentIndex, 'tl-image-overlay__counter') : null}
          onKeyDown={stepKeys}
          onClose={() => setFullPage(false)}>
          <img key={current.key} className="tl-image-overlay__image" src={imageUrl(current)} alt={altOf(current)} />
        </ImageOverlay>,
        document.body,
      )}
    </div>
  );

};

interface ImageOverlayProps {
  /** Whether the overlay shows an image covering the page opaquely, rather than content on the scrim. */
  view?: boolean;
  /** The accessible name of the overlay; absent where {@link labelledBy} names it. */
  label?: string;
  /** The ID of the element naming the overlay. */
  labelledBy?: string;
  /** The label of the button closing the overlay; absent: no such button. */
  closeLabel?: string;
  /** The buttons stepping through the images, placed at the start and end margins. */
  stepButtons?: React.ReactNode;
  /** The counter, placed in the bottom margin. */
  counter?: React.ReactNode;
  /** Takes the keys the overlay does not handle itself; answers whether it took the key. */
  onKeyDown?: (event: React.KeyboardEvent) => boolean;
  onClose: () => void;
  children: React.ReactNode;
}

/**
 * A modal layer covering the page, opaquely for a {@link ImageOverlayProps.view view} of an image, on
 * the scrim otherwise: its content in the middle, the buttons in the margins around it, never over
 * the content. Escape, the close button and a click on the margin or
 * beside the content close it; the focus stays inside while it is open and goes back to where it was.
 */
const ImageOverlay: React.FC<ImageOverlayProps> = ({ view, label, labelledBy, closeLabel, stepButtons, counter, onKeyDown, onClose, children }) => {
  const ref = React.useRef<HTMLDivElement | null>(null);
  const contentRef = React.useRef<HTMLDivElement | null>(null);
  const Button = usePart('Button');
  useFocusTrap(true, ref, 'container');

  return (
    <div ref={ref} className={view ? 'tl-image-overlay tl-image-overlay--view' : 'tl-image-overlay'} role="dialog" aria-modal="true"
      aria-label={labelledBy ? undefined : label} aria-labelledby={labelledBy} tabIndex={-1}
      onKeyDown={event => {
        if (event.defaultPrevented) return;
        if (event.key === KEY_ESCAPE) {
          event.preventDefault();
          event.stopPropagation();
          onClose();
          return;
        }
        onKeyDown?.(event);
      }}
      onClick={event => {
        if (event.target === event.currentTarget || event.target === contentRef.current) onClose();
      }}>
      <div ref={contentRef} className="tl-image-overlay__content">{children}</div>
      {stepButtons}
      {counter}
      {closeLabel && (
        <span className="tl-image-overlay__close">
          <Button label={closeLabel} image={ICON_CLOSE} display="icon-only" appearance="secondary" onClick={onClose} />
        </span>
      )}
    </div>
  );
};

export default TLImageField;
