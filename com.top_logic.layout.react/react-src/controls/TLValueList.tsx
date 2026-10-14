import { React, useTLState, useTLCommand, useTLUpload, useI18N, useListReorder, TLChild, rootClassName, tooltipProps, useFieldLabelProps, ThemeIcon } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { showsValueOnly } from './form/fieldState';

const I18N_KEYS = {
  'js.valueList.add': 'Add a value',
  'js.valueList.addFiles': 'Add files',
  'js.uploading': 'Uploading…',
  'js.valueList.remove': 'Remove this value',
  'js.valueList.move': 'Move this value',
};

/** Appends an empty value. */
const CMD_ADD_ELEMENT = 'addElement';

/** Drops the value at the `index` argument. */
const CMD_REMOVE_ELEMENT = 'removeElement';

/** Moves the value at the `index` argument to the position the `targetIndex` argument names. */
const CMD_MOVE_ELEMENT = 'moveElement';

/** Argument of {@link CMD_REMOVE_ELEMENT} and {@link CMD_MOVE_ELEMENT}: which value to work on. */
const ARG_INDEX = 'index';

/** Argument of {@link CMD_MOVE_ELEMENT}: the position the moved value ends up at. */
const ARG_TARGET_INDEX = 'targetIndex';

/** The multipart field name of an uploaded file, see `UploadSupport#FILE_PART`. */
const FILE_PART = 'file';

/** The data transfer type of a drag carrying files from outside the browser. */
const FILES_TRANSFER_TYPE = 'Files';

const ADD_ICON = 'css:fa-solid fa-plus';
const REMOVE_ICON = 'css:fa-solid fa-xmark';
const HANDLE_ICON = 'css:fa-solid fa-grip-vertical';

/**
 * The text between two values. It is written out rather than drawn by the style sheet, so that the
 * values can be copied and searched as the text they read as. Where the values stand on lines of
 * their own, the line break separates them and the style sheet hides this text.
 */
const SEPARATOR = ', ';

/**
 * The values of a field holding several of them, each drawn by the control of its own value type.
 *
 * State shape:
 * - elements: ChildDescriptor[] - one control per value, in value order
 * - layout: 'inline' | 'block' - values read as one text, or one value per line
 * - editable: boolean - whether the values can be changed
 * - disabled: boolean - whether the field that is not editable shows its rows as while edited, inactive
 * - ordered: boolean - whether the order of the values is part of the value, so the user arranges it
 * - upload: boolean - whether values are added by uploading files rather than as empty values
 * - accept: string - the file types the file picker offers (HTML accept attribute); absent: all
 *
 * While the field is only displayed, the values follow each other with a separator between them
 * ('inline') or each on a line of its own ('block', for a value that is a text of several lines).
 * The separator is written between two values in either layout; which of the two is read - the
 * separator or the line break - the style sheet decides, so that a display granting the list a
 * single line can fall back on the separator. While the field is edited, every value takes a row
 * with the button that removes it, and a further button below appends an empty value.
 *
 * Where values are added by uploading files, the button appending a value opens a file picker
 * selecting several files instead, and files dropped onto the edited list are uploaded as well; the
 * server appends one value for each file.
 *
 * Where the order is part of the value, each row starts with a handle that moves the value: by
 * dragging the row by that handle to where the value belongs, or with the arrow keys while the handle
 * has the focus.
 *
 * A disabled field shows the rows of an edited one, with the buttons that remove and append a value
 * natively `disabled` and without the handles (see showsValueOnly).
 */
const TLValueList: React.FC<TLCellProps> = ({ controlId }) => {
  const labelProps = useFieldLabelProps(controlId, controlId);
  const state = useTLState();
  const sendCommand = useTLCommand();
  const upload = useTLUpload();
  const t = useI18N(I18N_KEYS);

  const elements = (state.elements as unknown[]) ?? [];
  const block = state.layout === 'block';
  const editable = state.editable !== false;
  const disabled = state.disabled === true;
  // A single value has nowhere to move to.
  const arrangeable = editable && state.ordered === true && elements.length > 1;
  const uploads = state.upload === true;
  const accept = (state.accept as string | null) ?? '';

  const [uploading, setUploading] = React.useState(false);
  const [fileDragOver, setFileDragOver] = React.useState(false);
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
        formData.append(FILE_PART, file, file.name);
      }
      await upload(formData);
    } finally {
      setUploading(false);
    }
  }, [upload]);

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files) {
      doUpload(event.target.files);
    }
    // Reset, so that the same file can be selected again.
    event.target.value = '';
  };

  // Files are dropped while the list takes uploads; a value of the list dragged to another position
  // is no file drag and goes to the arranging gesture.
  const takesFileDrop = uploads && editable && !disabled && !uploading;
  const isFileDrag = (event: React.DragEvent) => event.dataTransfer.types.includes(FILES_TRANSFER_TYPE);

  const cls = 'tlValueList ' + (block ? 'tlValueList--block' : 'tlValueList--inline')
    + (fileDragOver ? ' tlValueList--dragover' : '');

  // The handles by row position: a move shifts the values while the rows stay where they are, so the
  // focus follows the moved value to the handle of the row it now sits in.
  const handles = React.useRef<(HTMLButtonElement | null)[]>([]);

  const move = React.useCallback((index: number, targetIndex: number) => {
    sendCommand(CMD_MOVE_ELEMENT, { [ARG_INDEX]: index, [ARG_TARGET_INDEX]: targetIndex });
  }, [sendCommand]);

  // A value is edited in a text field, so the row is picked up by its handle rather than anywhere:
  // a row that is draggable throughout would drag itself when text within it is selected.
  const reorder = useListReorder({ axis: 'vertical', onMove: move, enabled: arrangeable, handle: true });

  const handleKeyDown = (index: number, event: React.KeyboardEvent<HTMLButtonElement>) => {
    let targetIndex: number;
    if (event.key === 'ArrowUp') {
      targetIndex = index - 1;
    } else if (event.key === 'ArrowDown') {
      targetIndex = index + 1;
    } else {
      return;
    }
    // The list takes the arrow keys over from the scrolling of the page.
    event.preventDefault();
    if (targetIndex < 0 || targetIndex >= elements.length) {
      return;
    }
    move(index, targetIndex);
    handles.current[targetIndex]?.focus();
  };

  if (showsValueOnly(state)) {
    return (
      <span id={controlId} className={rootClassName(state, cls)}>
        {elements.map((element, index) => (
          <React.Fragment key={index}>
            {index > 0 && <span className="tlValueList__separator">{SEPARATOR}</span>}
            <span className="tlValueList__item">
              <TLChild control={element} />
            </span>
          </React.Fragment>
        ))}
      </span>
    );
  }

  const handleDragOver = (event: React.DragEvent) => {
    if (!takesFileDrop || !isFileDrag(event)) {
      return;
    }
    event.preventDefault();
    event.stopPropagation();
    event.dataTransfer.dropEffect = 'copy';
    setFileDragOver(true);
  };

  const handleDragLeave = (event: React.DragEvent) => {
    // Moving over a row inside the list leaves the list element as well; only leaving the list
    // itself ends the hovering.
    if (event.currentTarget.contains(event.relatedTarget as Node | null)) {
      return;
    }
    setFileDragOver(false);
  };

  const handleDrop = (event: React.DragEvent) => {
    if (takesFileDrop && isFileDrag(event)) {
      event.preventDefault();
      event.stopPropagation();
      setFileDragOver(false);
      doUpload(event.dataTransfer.files);
      return;
    }
    reorder.containerProps.onDrop?.(event);
  };

  const addLabel = uploads ? (uploading ? t['js.uploading'] : t['js.valueList.addFiles']) : t['js.valueList.add'];

  return (
    <div id={controlId} {...labelProps} role="group" className={rootClassName(state, cls)}
      onDragOver={handleDragOver} onDragLeave={handleDragLeave} onDrop={handleDrop}>
      {elements.map((element, index) => {
        const dragState = reorder.itemState(index);
        let rowCls = 'tlValueList__row';
        if (dragState.dragging) {
          rowCls += ' tlValueList__row--dragging';
        }
        if (dragState.dropBefore) {
          rowCls += ' tlValueList__row--dragOver-before';
        }
        if (dragState.dropAfter) {
          rowCls += ' tlValueList__row--dragOver-after';
        }
        return (
          <div key={index} className={rowCls} {...reorder.itemProps(index)}>
            {arrangeable && (
              <button
                type="button"
                className="tlValueList__handle"
                aria-label={t['js.valueList.move']}
                {...tooltipProps(t['js.valueList.move'])}
                ref={(handle) => {
                  handles.current[index] = handle;
                }}
                onKeyDown={(event) => handleKeyDown(index, event)}
                {...reorder.handleProps(index)}
              >
                <ThemeIcon encoded={HANDLE_ICON} />
              </button>
            )}
            <span className="tlValueList__item">
              <TLChild control={element} />
            </span>
            <button
              type="button"
              className="tlValueList__remove"
              aria-label={t['js.valueList.remove']}
              {...tooltipProps(t['js.valueList.remove'])}
              disabled={disabled}
              onClick={() => sendCommand(CMD_REMOVE_ELEMENT, { [ARG_INDEX]: index })}
            >
              <ThemeIcon encoded={REMOVE_ICON} />
            </button>
          </div>
        );
      })}
      {uploads && (
        <input
          ref={fileInputRef}
          type="file"
          multiple
          accept={accept || undefined}
          onChange={handleFileChange}
          style={{ display: 'none' }}
        />
      )}
      <button
        type="button"
        className="tlValueList__add"
        aria-label={addLabel}
        {...tooltipProps(addLabel)}
        disabled={disabled || uploading}
        onClick={() => {
          if (uploads) {
            fileInputRef.current?.click();
          } else {
            sendCommand(CMD_ADD_ELEMENT);
          }
        }}
      >
        <ThemeIcon encoded={ADD_ICON} />
      </button>
    </div>
  );
};

export default TLValueList;
