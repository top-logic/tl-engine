import { React, useTLState, useTLCommand, TLChild, FillBarrier, useFill, rootClassName } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { isInteractiveTarget } from './interactive';

/** The command selecting a card (ReactKanbanBoardControl.CMD_SELECT_CARD). */
const CMD_SELECT_CARD = 'selectCard';

/** The argument of {@link CMD_SELECT_CARD} naming the card (SelectCardArguments.CARD). */
const ARG_CARD = 'card';

/** A card as the server describes it. */
interface CardDescriptor {
  /** Stable key of the card, kept while its object is displayed. */
  key: string;

  /** The control rendering the card. */
  content: unknown;
}

/** A column as the server describes it. */
interface ColumnDescriptor {
  /** Stable key of the column, kept while its value is displayed. */
  key: string;

  /** The label displayed in the header. */
  label: string;

  /** The number of cards in the column. */
  count: number;

  /** The cards of the column, in display order. */
  cards: CardDescriptor[];
}

/**
 * A board of columns, each holding a vertical list of cards.
 *
 * State:
 * - columns: ColumnDescriptor[] - the columns, in display order
 * - selected: string | null - the key of the selected card
 *
 * The columns are placed side by side and scroll horizontally where they do not fit; each column
 * scrolls its cards vertically. A card is selected by a click or by Enter / Space while it has the
 * focus; a click on an interactive element inside the card is left to that element.
 *
 * The board always fills the height its container offers, and each card list is a fill barrier.
 */
const TLKanbanBoard: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const fillClass = useFill(true);

  const columns = (state.columns as ColumnDescriptor[]) ?? [];
  const selected = (state.selected as string | null) ?? null;

  const select = React.useCallback(
    (card: string) => sendCommand(CMD_SELECT_CARD, { [ARG_CARD]: card }),
    [sendCommand],
  );

  return (
    <div id={controlId} className={rootClassName(state, 'tlKanbanBoard', fillClass)}>
      {columns.map(column => {
        const headerId = `${controlId}-${column.key}`;
        return (
          <section key={column.key} className="tlKanbanBoard__column" aria-labelledby={headerId}>
            <header className="tlKanbanBoard__header">
              <span id={headerId} className="tlKanbanBoard__label">{column.label}</span>
              <span className="tlKanbanBoard__count">{column.count}</span>
            </header>
            <FillBarrier>
              <ul className="tlKanbanBoard__cards" role="list" aria-labelledby={headerId}>
                {column.cards.map(card => {
                  const isSelected = card.key === selected;
                  return (
                    <li
                      key={card.key}
                      role="listitem"
                      tabIndex={0}
                      aria-current={isSelected ? 'true' : undefined}
                      className={'tlKanbanBoard__card' + (isSelected ? ' tlKanbanBoard__card--selected' : '')}
                      onClick={event => {
                        if (!isInteractiveTarget(event)) {
                          select(card.key);
                        }
                      }}
                      onKeyDown={event => {
                        if (event.target === event.currentTarget && (event.key === 'Enter' || event.key === ' ')) {
                          event.preventDefault();
                          select(card.key);
                        }
                      }}
                    >
                      <TLChild control={card.content} />
                    </li>
                  );
                })}
              </ul>
            </FillBarrier>
          </section>
        );
      })}
    </div>
  );
};

export default TLKanbanBoard;
