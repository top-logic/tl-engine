import { React, useTLState, useTLCommand, useI18N, usePart, TLChild, FillBarrier, useFill, rootClassName, stateAttr, writeDragPayload, runningDrag, onDragEnd, afterDragImage, readDragPayload, dragKindAccepted, boundaryAt, isInteractiveWithin } from 'tl-react-bridge';
import type { TLCellProps, TLDropZone } from 'tl-react-bridge';
import { DropHint, NO_DRAG_IMAGE } from './DropHint';
import type { DropVerdict, HintAnchor } from './DropHint';
import { useOptionSelection, ATTR_OPTION_KEY } from './option-selection';
import { useRovingFocus } from './useRovingFocus';

const { useCallback, useEffect, useLayoutEffect, useMemo, useRef, useState } = React;

/** Texts of the board; the server translates them (I18NConstants, js.kanbanBoard.*). */
const I18N_KEYS = {
  'js.kanbanBoard.moved': 'Moved to {0}, position {1} of {2}.',
};

/** The command selecting a card (ReactKanbanBoardControl.CMD_SELECT_CARD). */
const CMD_SELECT_CARD = 'selectCard';

/** The argument of {@link CMD_SELECT_CARD} naming the card (SelectCardArguments.CARD). */
const ARG_CARD = 'card';

/** The argument of {@link CMD_SELECT_CARD} toggling the card (SelectionModifiers.TOGGLE). */
const ARG_TOGGLE = 'toggle';

/** The argument of {@link CMD_SELECT_CARD} extending the selection (SelectionModifiers.RANGE). */
const ARG_RANGE = 'range';

/** The command applying a drop (DropSupport.CMD_DROP). */
const CMD_DROP = 'drop';

/** The command asking whether a drop at the hovered target would be accepted (DropSupport.CMD_DROP_PROBE). */
const CMD_DROP_PROBE = 'dropProbe';

/** Selects the cards of a column, evaluated on its card list. */
const CARD_SELECTOR = ':scope > [role="option"]';

/** Selects the cards of a column, evaluated on the column. */
const COLUMN_CARD_SELECTOR = ':scope > .tl-board__cards > [role="option"]';

/** Selects the cards of the board, evaluated on the board - not those of a board inside a card. */
const BOARD_CARD_SELECTOR = ':scope > .tl-board__column > .tl-board__cards > [role="option"]';

/** A card as the server describes it. */
interface CardDescriptor {
  /** Stable key of the card, kept while its object is displayed. */
  key: string;

  /** The control rendering the card. */
  content: unknown;

  /** Whether the card may be dragged; present while cards are draggable at all. */
  draggable?: boolean;
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
 * Where a drop is made relative to the card it names: before or after it, or `none` for a drop on
 * the column beside its cards.
 */
type CardPosition = 'before' | 'after' | 'none';

/**
 * The zone a drop at a position is reported in: the upper part of the card it is made before, the
 * lower part of the card it is made after, none on the column beside its cards.
 */
const ZONE_OF_POSITION: Record<CardPosition, TLDropZone> = {
  before: 'upper',
  after: 'lower',
  none: 'none',
};

/** Where a drop is made: beside a card, or on a column. */
interface DropTarget {
  /** Key of the card the drop is made beside, or of the column for a drop appending to it. */
  target: string;

  /** Where the drop is made relative to the card, `none` for a drop on the column. */
  position: CardPosition;
}

/** Where a running drag hovers the board. */
interface DropState extends DropTarget {
  /** Key of the hovered column. */
  column: string;

  /** Identifier of the probe asking about this target, `null` for a drag not started here. */
  probe: string | null;
}

/** The direction Alt + arrow moves a card in. */
type MoveDirection = 'up' | 'down' | 'left' | 'right';

/** The direction the given key moves a card in together with Alt, `null` for a key moving none. */
function moveOf(key: string): MoveDirection | null {
  switch (key) {
    case 'ArrowUp':
      return 'up';
    case 'ArrowDown':
      return 'down';
    case 'ArrowLeft':
      return 'left';
    case 'ArrowRight':
      return 'right';
    default:
      return null;
  }
}

/**
 * Where Alt + arrow moves the given cards of a column, named as a drag dropping them there names it
 * - the server resolves it like any drop (ReactKanbanBoardControl.placeOf): down after the card
 * following the last of them, up before the card preceding the first of them, left and right into
 * the neighbouring column, before the card standing there at the position of the moved card, or at
 * the column's end where there is none.
 *
 * @param columns The columns of the board.
 * @param columnIndex The position of the column the cards are in.
 * @param cardIndex The position of the card the key was pressed on, within its column.
 * @param moved The keys of the moved cards, in display order.
 * @param direction The direction of the move.
 * @returns The drop target, `null` where the cards cannot move in that direction.
 */
function keyboardDrop(
  columns: readonly ColumnDescriptor[],
  columnIndex: number,
  cardIndex: number,
  moved: readonly string[],
  direction: MoveDirection,
): DropTarget | null {
  const cards = columns[columnIndex].cards;
  switch (direction) {
    case 'down': {
      const next = cards[cards.findIndex((card) => card.key === moved[moved.length - 1]) + 1];
      return next ? { target: next.key, position: 'after' } : null;
    }
    case 'up': {
      const first = cards.findIndex((card) => card.key === moved[0]);
      return first > 0 ? { target: cards[first - 1].key, position: 'before' } : null;
    }
    default: {
      const neighbour = columns[columnIndex + (direction === 'left' ? -1 : 1)];
      if (!neighbour) {
        return null;
      }
      const card = neighbour.cards[cardIndex];
      return card ? { target: card.key, position: 'before' } : { target: neighbour.key, position: 'none' };
    }
  }
}

/** Fills the placeholders {0}, {1}, ... of a translated text. */
function format(template: string, ...args: string[]): string {
  return template.replace(/\{(\d+)\}/g, (placeholder, index: string) => args[Number(index)] ?? placeholder);
}

/** A column of the board, see {@link BoardColumn}. */
interface BoardColumnProps {
  column: ColumnDescriptor;

  /** The ID of the column's label, which names the column and its card list. */
  labelId: string;

  /** The keys of the selected cards. */
  selected: ReadonlySet<string>;

  /** Whether several cards may be selected. */
  multiSelect: boolean;

  /** Whether cards are dragged. */
  dragEnabled: boolean;

  /** Selects the card with the given key. */
  select: (key: string, toggle: boolean, range: boolean) => void;

  /** The `data-tl-state` of the column in the running drag. */
  columnState: string | undefined;

  /** The `data-tl-state` of the given card in the running drag. */
  cardState: (card: CardDescriptor) => string | undefined;

  onCardDragStart: (card: CardDescriptor, event: React.DragEvent) => void;

  onCardDragEnd: () => void;

  onColumnDragOver: (column: ColumnDescriptor, event: React.DragEvent) => void;

  onColumnDrop: (column: ColumnDescriptor, event: React.DragEvent) => void;

  /**
   * Handles a key pressed in the card list of the column that moves a card (Alt + arrow); whether
   * it was such a key, which then does not move the focus.
   */
  onMoveKey: (column: ColumnDescriptor, event: React.KeyboardEvent) => boolean;
}

/**
 * A column of the board: a section named by its header, with its cards as the options of a list
 * box - one tab stop, the arrow keys moving the focus among the cards (useRovingFocus), the
 * selection as among any options (useOptionSelection).
 */
const BoardColumn: React.FC<BoardColumnProps> = ({
  column, labelId, selected, multiSelect, dragEnabled, select, columnState, cardState,
  onCardDragStart, onCardDragEnd, onColumnDragOver, onColumnDrop, onMoveKey,
}) => {
  const Badge = usePart('Badge');
  const listRef = useRef<HTMLDivElement | null>(null);
  const cards = column.cards;

  const firstSelected = cards.findIndex((card) => selected.has(card.key));
  const roving = useRovingFocus(listRef, CARD_SELECTOR, cards.length, firstSelected, { linear: true });
  const selection = useOptionSelection(listRef, select, multiSelect);

  // A focused card moved within the column keeps its focus without a focus event: React gives the
  // focus back to a node it moves while committing, with its own events switched off. The tab stop
  // would stay at the card's old position; it follows the card here.
  const trackFocus = roving.onFocus;
  useLayoutEffect(() => {
    const active = document.activeElement;
    if (active !== null && active.parentElement === listRef.current) {
      trackFocus({ target: active } as unknown as React.FocusEvent);
    }
  }, [cards, trackFocus]);

  const handleKeyDown = (event: React.KeyboardEvent) => {
    // Alt + arrow moves the card instead of the focus.
    if (!onMoveKey(column, event)) {
      roving.onKeyDown(event);
    }
  };

  return (
    <section className="tl-board__column" aria-labelledby={labelId} data-tl-state={columnState}
      onDragOver={(event) => onColumnDragOver(column, event)}
      onDrop={(event) => onColumnDrop(column, event)}
    >
      <header className="tl-board__header tl-type-body-strong">
        <span id={labelId} className="tl-board__label">{column.label}</span>
        <Badge content={String(column.count)} />
      </header>
      <FillBarrier>
        <div ref={listRef} className="tl-board__cards" role="listbox" aria-labelledby={labelId}
          aria-multiselectable={multiSelect || undefined}
          onKeyDown={handleKeyDown} onFocus={roving.onFocus}>
          {cards.map((card, index) => {
            const isSelected = selected.has(card.key);
            const draggable = dragEnabled && card.draggable !== false;
            return (
              <div
                key={card.key}
                {...selection.optionProps(card.key, isSelected)}
                tabIndex={roving.tabIndexOf(index)}
                className="tl-item tl-board__card"
                draggable={draggable}
                data-tl-state={cardState(card)}
                onDragStart={draggable ? (event) => onCardDragStart(card, event) : undefined}
                onDragEnd={draggable ? onCardDragEnd : undefined}
              >
                <TLChild control={card.content} />
                {isSelected && <span className="tl-item__check" aria-hidden="true" />}
              </div>
            );
          })}
        </div>
      </FillBarrier>
    </section>
  );
};

/**
 * A board of columns, each holding a vertical list of cards.
 *
 * State:
 * - columns: ColumnDescriptor[] - the columns, in display order
 * - selected: string[] - the keys of the selected cards
 * - multiSelect: boolean - whether several cards may be selected
 * - dragEnabled: boolean, dragKind: string | null - whether cards are dragged, and the kind of such a
 *   drag (none without a kind)
 * - dropAcceptsAny: boolean, dropAccepts: string[] - whether a drop on a column is accepted of any
 *   drag, and otherwise the kinds a drop is accepted of
 * - reorder: boolean - whether a drop within a column reorders it
 * - dropVerdicts: Record<string, DropVerdict> - the answers to the probes of the running drag
 *
 * The columns are placed side by side and scroll horizontally where they do not fit; each column
 * scrolls its cards vertically. The cards of a column are the options of a list box with one tab
 * stop; the arrow keys move the focus among them. A card is selected by a click or by Enter / Space
 * while it has the focus; a click on an interactive element inside the card is left to that
 * element. With `Ctrl` (or `Cmd`) the card is toggled instead, with `Shift` the selection is
 * extended up to it; a long press on a touch screen toggles as well, where several cards may be
 * selected.
 *
 * Cards are dragged with the drag payload every drag source of the bridge writes, so they can be
 * dropped on any control accepting their type, and a column accepts what such a control drags.
 * Dragging a selected card drags all selected cards, in display order.
 * While the board reorders its columns, a drop is made at the card boundary nearest to the pointer
 * - on a card or in the gap between two - and an insertion line shows where; otherwise a drop is
 * made on the column as a whole, and the column a dragged card already is in accepts no drop. A
 * column the server refuses a drop on shows the reason next to the pointer.
 *
 * Alt + arrow moves the focused card - with the other selected cards of its column, where it is
 * selected - by the drop a drag would make: up and down by one place within its column, while the
 * board reorders its columns; left and right into the neighbouring column, at the same position or
 * at its end. Once the new state shows the card at its new place, it has the focus, and its place
 * is announced; a move the server refuses leaves the focus where it is.
 *
 * The board always fills the height its container offers, and each card list is a fill barrier.
 */
const TLKanbanBoard: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const fillClass = useFill(true);
  const i18n = useI18N(I18N_KEYS);

  const columns = (state.columns as ColumnDescriptor[]) ?? [];
  const selected = useMemo(() => new Set((state.selected as string[] | null) ?? []), [state.selected]);
  const multiSelect = state.multiSelect === true;
  const dragEnabled = state.dragEnabled === true;
  const dragKind = (state.dragKind as string | null) ?? undefined;
  const dropAcceptsAny = (state.dropAcceptsAny as boolean) ?? false;
  const dropAccepts = (state.dropAccepts as string[]) ?? [];
  const reorder = state.reorder === true;
  const dropVerdicts = (state.dropVerdicts as Record<string, DropVerdict>) ?? {};

  const select = useCallback(
    (card: string, toggle: boolean, range: boolean) => {
      void sendCommand(CMD_SELECT_CARD, { [ARG_CARD]: card, [ARG_TOGGLE]: toggle, [ARG_RANGE]: range });
    },
    [sendCommand],
  );

  // -- Where an accepted drag currently hovers, or null while none does. --
  const [dropState, setDropState] = useState<DropState | null>(null);

  // -- Drop probes sent for the running drag: each target is asked once per drag. --
  const probesRef = useRef<{ drag: string; sent: Set<string> } | null>(null);

  const dropVerdict: DropVerdict | undefined = dropState?.probe ? dropVerdicts[dropState.probe] : undefined;
  const dropRefused = dropVerdict !== undefined && !dropVerdict.accepted;

  // A drag hovering the board may end without any event reaching it: a refused drop is not
  // dispatched here, and the source's dragend reaches the source's control only.
  const dragHovers = dropState !== null;
  useEffect(() => {
    if (!dragHovers) {
      return undefined;
    }
    return onDragEnd(() => setDropState(null));
  }, [dragHovers]);

  // -- The cards dragged from this board, marked once the browser has taken the drag image. --
  const [dragged, setDragged] = useState<ReadonlySet<string> | null>(null);
  useEffect(() => (dragged ? onDragEnd(() => setDragged(null)) : undefined), [dragged]);

  // -- Where the hint on a refused column stands - at the pointer, beside the drag image - and the
  //    hint's way to follow it: dragover fires continuously, and only the hint re-renders for each
  //    move, not the board. --
  const hintAnchorRef = useRef<HintAnchor>({ x: 0, y: 0, height: 0 });
  const hintMoveRef = useRef<((at: HintAnchor) => void) | null>(null);

  const handleCardDragStart = useCallback((card: CardDescriptor, event: React.DragEvent) => {
    if (isInteractiveWithin(event.target, event.currentTarget)) {
      // A drag begun on an input or a link inside the card belongs to that element. The card itself,
      // an option, counts as interactive by its role, but it is what is dragged.
      event.preventDefault();
      return;
    }
    // A selected card takes the other selected cards along, in display order.
    const keys = selected.has(card.key)
      ? columns.flatMap((column) => column.cards.map((c) => c.key).filter((key) => selected.has(key)))
      : [card.key];
    writeDragPayload(event, { source: controlId, keys, selection: false, kind: dragKind });
    const marked = new Set(keys);
    // A drag cancelled before the image was taken has already ended: no card stays marked dragging.
    afterDragImage(() => {
      if (runningDrag()) {
        setDragged(marked);
      }
    });
  }, [controlId, dragKind, columns, selected]);

  const handleCardDragEnd = useCallback(() => {
    setDropState(null);
    setDragged(null);
  }, []);

  /**
   * The target a drag event over the given column points at, or `null` where the column accepts
   * no drop of the running drag.
   */
  const dropTargetAt = useCallback(
    (column: ColumnDescriptor, event: React.DragEvent): DropTarget | null => {
      const drag = runningDrag();
      const withinColumn = drag !== null && drag.payload.source === controlId
        && drag.payload.keys.every((key) => column.cards.some((card) => card.key === key));
      if (withinColumn && !reorder) {
        return null;
      }
      if (reorder) {
        // The nearest boundary between two cards, wherever the pointer is in the column - on a card
        // or in the gap between two, or below the last one.
        const boundary = boundaryAt(event.clientY,
          Array.from(event.currentTarget.querySelectorAll(COLUMN_CARD_SELECTOR)));
        const key = boundary?.item.getAttribute(ATTR_OPTION_KEY);
        if (boundary && key) {
          return { target: key, position: boundary.side };
        }
      }
      return { target: column.key, position: 'none' };
    }, [controlId, reorder]);

  const handleColumnDragOver = useCallback((column: ColumnDescriptor, event: React.DragEvent) => {
    // Coarse acceptance from the payload's kind alone; whether this particular drop is possible
    // is the server's answer, given once it arrives.
    if (!dragKindAccepted(event.dataTransfer, dropAcceptsAny, dropAccepts)) {
      return;
    }
    const target = dropTargetAt(column, event);
    if (target === null) {
      setDropState(null);
      return;
    }
    const drag = runningDrag();
    // The browser draws the drag image around the pointer; the hint stands beside it.
    const image = drag?.image ?? NO_DRAG_IMAGE;
    hintAnchorRef.current = { x: event.clientX, y: event.clientY + image.top, height: image.bottom - image.top };
    hintMoveRef.current?.(hintAnchorRef.current);
    let probe: string | null = null;
    if (drag) {
      probe = drag.id + '|' + target.target + '|' + target.position;
      let probes = probesRef.current;
      if (!probes || probes.drag !== drag.id) {
        probes = { drag: drag.id, sent: new Set() };
        probesRef.current = probes;
      }
      if (!probes.sent.has(probe)) {
        probes.sent.add(probe);
        void sendCommand(CMD_DROP_PROBE, {
          source: drag.payload.source,
          keys: drag.payload.keys.join(','),
          selection: drag.payload.selection,
          targetKey: target.target,
          zone: ZONE_OF_POSITION[target.position],
          drag: drag.id,
          probe,
        });
      }
    }
    setDropState((previous) =>
      previous && previous.column === column.key && previous.target === target.target
          && previous.position === target.position && previous.probe === probe
        ? previous
        : { column: column.key, ...target, probe });
    const verdict = probe ? dropVerdicts[probe] : undefined;
    if (verdict && !verdict.accepted) {
      // Refused: leaving the default in place makes the target refuse the drop.
      event.dataTransfer.dropEffect = 'none';
      return;
    }
    event.preventDefault();
    event.dataTransfer.dropEffect = 'move';
  }, [dropAcceptsAny, dropAccepts, dropTargetAt, dropVerdicts, sendCommand]);

  const handleColumnDrop = useCallback((column: ColumnDescriptor, event: React.DragEvent) => {
    if (!dragKindAccepted(event.dataTransfer, dropAcceptsAny, dropAccepts)) {
      return;
    }
    const target = dropTargetAt(column, event);
    setDropState(null);
    if (target === null) {
      return;
    }
    event.preventDefault();
    event.stopPropagation();
    const payload = readDragPayload(event.dataTransfer);
    if (payload) {
      void sendCommand(CMD_DROP, {
        source: payload.source,
        // Comma-separated: the command argument is a formatted string list, and a key holds no comma.
        keys: payload.keys.join(','),
        selection: payload.selection,
        targetKey: target.target,
        zone: ZONE_OF_POSITION[target.position],
      });
    }
  }, [dropAcceptsAny, dropAccepts, dropTargetAt, sendCommand]);

  const handleRootDragLeave = useCallback((event: React.DragEvent) => {
    // Moving among the board's own descendants fires a leave on each one left behind; only leaving
    // the board itself ends the highlight.
    if (!event.currentTarget.contains(event.relatedTarget as Node | null)) {
      setDropState(null);
    }
  }, []);

  // -- After a keyboard move the focus follows the moved card. Its element is keyed by the card, so
  //    React moves it - within the column - or creates it anew - in another column - once the new
  //    state arrives, and the browser drops the focus of a node it moves. The card it was pressed
  //    on, and where it stood then; null while no move is pending. --
  const rootRef = useRef<HTMLDivElement | null>(null);
  const pendingMoveRef = useRef<{ key: string; column: string; index: number } | null>(null);
  const [announcement, setAnnouncement] = useState('');
  const movedText = i18n['js.kanbanBoard.moved'];

  useLayoutEffect(() => {
    const pending = pendingMoveRef.current;
    if (pending === null) {
      return;
    }
    const column = columns.find((c) => c.cards.some((card) => card.key === pending.key));
    const index = column ? column.cards.findIndex((card) => card.key === pending.key) : -1;
    const element = Array.from(rootRef.current?.querySelectorAll<HTMLElement>(BOARD_CARD_SELECTOR) ?? [])
      .find((card) => card.getAttribute(ATTR_OPTION_KEY) === pending.key);
    if (column && element && (column.key !== pending.column || index !== pending.index)) {
      pendingMoveRef.current = null;
      element.focus();
      setAnnouncement(format(movedText, column.label, String(index + 1), String(column.cards.length)));
      return;
    }
    // The card is gone, or the user went on before the new state arrived: the focus is not the
    // board's to take back.
    const active = document.activeElement;
    if (!column || (active !== null && active !== document.body && active !== element)) {
      pendingMoveRef.current = null;
    }
  }, [columns, movedText]);

  const handleMoveKey = useCallback((column: ColumnDescriptor, event: React.KeyboardEvent): boolean => {
    const direction = moveOf(event.key);
    if (!event.altKey || direction === null) {
      return false;
    }
    // Only a key pressed on a card itself moves it; one pressed in an element inside a card is that
    // element's.
    const target = event.target as Element;
    const index = target.parentElement === event.currentTarget
      ? column.cards.findIndex((card) => card.key === target.getAttribute(ATTR_OPTION_KEY))
      : -1;
    const vertical = direction === 'up' || direction === 'down';
    if (index < 0 || !dragEnabled || (vertical && !reorder) || event.ctrlKey || event.metaKey || event.shiftKey) {
      return true;
    }
    // The board takes Alt + arrow over from the page: no scrolling, no step in the browser history.
    event.preventDefault();
    event.stopPropagation();
    const card = column.cards[index];
    // A selected card takes the other selected cards of its column along, in display order.
    const moved = selected.has(card.key)
      ? column.cards.map((c) => c.key).filter((key) => selected.has(key))
      : [card.key];
    const drop = keyboardDrop(columns, columns.indexOf(column), index, moved, direction);
    if (drop === null) {
      return true;
    }
    pendingMoveRef.current = { key: card.key, column: column.key, index };
    // The drop a drag of these cards would make there: the keys, not the selection, as in a drag.
    void sendCommand(CMD_DROP, {
      source: controlId,
      keys: moved.join(','),
      selection: false,
      targetKey: drop.target,
      zone: ZONE_OF_POSITION[drop.position],
    });
    return true;
  }, [columns, controlId, dragEnabled, reorder, selected, sendCommand]);

  return (
    <div id={controlId} ref={rootRef} className={rootClassName(state, 'tl-board', fillClass)}
      onDragLeave={handleRootDragLeave}>
      {/* Why the column under the running drag refuses it: a tl-popover in the document body, so
          that neither the board's scrolling nor its clipping can hide it. */}
      {dropRefused && dropVerdict?.reason && (
        <DropHint reason={dropVerdict.reason} start={hintAnchorRef.current} moveRef={hintMoveRef} />
      )}
      {columns.map((column) => {
        const hovered = dropState !== null && dropState.column === column.key ? dropState : null;
        // The column refuses the drop, or receives it at its end; a drop beside a card is shown by
        // the insertion line at that card.
        const columnState = hovered === null
          ? undefined
          : (dropRefused ? 'drop-refused' : (hovered.position === 'none' ? 'drop-onto' : undefined));
        const line = hovered !== null && !dropRefused && hovered.position !== 'none' ? hovered : null;
        return (
          <BoardColumn
            key={column.key}
            column={column}
            labelId={`${controlId}-${column.key}`}
            selected={selected}
            multiSelect={multiSelect}
            dragEnabled={dragEnabled}
            select={select}
            columnState={columnState}
            cardState={(card) => stateAttr(
              dragged !== null && dragged.has(card.key) && 'dragging',
              line !== null && line.target === card.key && (line.position === 'before' ? 'drop-before' : 'drop-after'),
            )}
            onCardDragStart={handleCardDragStart}
            onCardDragEnd={handleCardDragEnd}
            onColumnDragOver={handleColumnDragOver}
            onColumnDrop={handleColumnDrop}
            onMoveKey={handleMoveKey}
          />
        );
      })}
      <div className="tl-board__live tl-visually-hidden" role="status">{announcement}</div>
    </div>
  );
};

export default TLKanbanBoard;
