import { React, useTLState, useTLCommand, useI18N, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import FontIcon from './FontIcon';

const I18N_KEYS = {
  'js.valueList.add': 'Add a value',
  'js.valueList.remove': 'Remove this value',
};

/** Appends an empty value. */
const CMD_ADD_ELEMENT = 'addElement';

/** Drops the value at the `index` argument. */
const CMD_REMOVE_ELEMENT = 'removeElement';

/** Argument of {@link CMD_REMOVE_ELEMENT}: which value to drop. */
const ARG_INDEX = 'index';

const ADD_ICON = 'css:fa-solid fa-plus';
const REMOVE_ICON = 'css:fa-solid fa-xmark';

/**
 * The text between two values read in one line. It is written out rather than drawn by the style
 * sheet, so that the values can be copied and searched as the text they read as.
 */
const SEPARATOR = ', ';

/**
 * The values of a field holding several of them, each drawn by the control of its own value type.
 *
 * State shape:
 * - elements: ChildDescriptor[] - one control per value, in value order
 * - layout: 'inline' | 'block' - values read as one text, or one value per line
 * - editable: boolean - whether the values can be changed
 *
 * While the field is only displayed, the values follow each other with a separator between them
 * ('inline') or each on a line of its own ('block', for a value that is a text of several lines).
 * While it is edited, every value takes a row with the button that removes it, and a further button
 * below appends an empty value.
 */
const TLValueList: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const t = useI18N(I18N_KEYS);

  const elements = (state.elements as unknown[]) ?? [];
  const block = state.layout === 'block';
  const cls = 'tlValueList ' + (block ? 'tlValueList--block' : 'tlValueList--inline');

  if (state.editable === false) {
    return (
      <span id={controlId} className={cls}>
        {elements.map((element, index) => (
          <React.Fragment key={index}>
            {index > 0 && !block && <span className="tlValueList__separator">{SEPARATOR}</span>}
            <span className="tlValueList__item">
              <TLChild control={element} />
            </span>
          </React.Fragment>
        ))}
      </span>
    );
  }

  return (
    <div id={controlId} className={cls}>
      {elements.map((element, index) => (
        <div key={index} className="tlValueList__row">
          <span className="tlValueList__item">
            <TLChild control={element} />
          </span>
          <button
            type="button"
            className="tlValueList__remove"
            title={t['js.valueList.remove']}
            aria-label={t['js.valueList.remove']}
            onClick={() => sendCommand(CMD_REMOVE_ELEMENT, { [ARG_INDEX]: index })}
          >
            <FontIcon image={REMOVE_ICON} />
          </button>
        </div>
      ))}
      <button
        type="button"
        className="tlValueList__add"
        title={t['js.valueList.add']}
        aria-label={t['js.valueList.add']}
        onClick={() => sendCommand(CMD_ADD_ELEMENT)}
      >
        <FontIcon image={ADD_ICON} />
      </button>
    </div>
  );
};

export default TLValueList;
