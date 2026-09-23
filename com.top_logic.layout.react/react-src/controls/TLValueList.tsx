import { React, useTLState, useTLCommand, useI18N, useListReorder, TLChild, rootClassName, tooltipProps } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import FontIcon from './FontIcon';

const I18N_KEYS = {
  'js.valueList.add': 'Add a value',
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
 * - ordered: boolean - whether the order of the values is part of the value, so the user arranges it
 *
 * While the field is only displayed, the values follow each other with a separator between them
 * ('inline') or each on a line of its own ('block', for a value that is a text of several lines).
 * The separator is written between two values in either layout; which of the two is read - the
 * separator or the line break - the style sheet decides, so that a display granting the list a
 * single line can fall back on the separator. While the field is edited, every value takes a row
 * with the button that removes it, and a further button below appends an empty value.
 *
 * Where the order is part of the value, each row starts with a handle that moves the value: by
 * dragging the row by that handle to where the value belongs, or with the arrow keys while the handle
 * has the focus.
 */
const TLValueList: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const t = useI18N(I18N_KEYS);

  const elements = (state.elements as unknown[]) ?? [];
  const block = state.layout === 'block';
  const editable = state.editable !== false;
  // A single value has nowhere to move to.
  const arrangeable = editable && state.ordered === true && elements.length > 1;
  const cls = 'tlValueList ' + (block ? 'tlValueList--block' : 'tlValueList--inline');

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

  if (!editable) {
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

  return (
    <div id={controlId} className={rootClassName(state, cls)} {...reorder.containerProps}>
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
                <FontIcon image={HANDLE_ICON} />
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
              onClick={() => sendCommand(CMD_REMOVE_ELEMENT, { [ARG_INDEX]: index })}
            >
              <FontIcon image={REMOVE_ICON} />
            </button>
          </div>
        );
      })}
      <button
        type="button"
        className="tlValueList__add"
        aria-label={t['js.valueList.add']}
        {...tooltipProps(t['js.valueList.add'])}
        onClick={() => sendCommand(CMD_ADD_ELEMENT)}
      >
        <FontIcon image={ADD_ICON} />
      </button>
    </div>
  );
};

export default TLValueList;
