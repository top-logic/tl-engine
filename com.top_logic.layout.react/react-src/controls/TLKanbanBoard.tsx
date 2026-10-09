import { React, useTLState, useTLCommand, TLChild, FillBarrier, useFill, rootClassName, writeDragPayload, runningDrag, onDragEnd, readDragPayload, dragTypeAccepted, createPortal, ATTR_LONG_PRESS, LONG_PRESS_EVENT } from 'tl-react-bridge';
import type { TLCellProps, TLDropPosition } from 'tl-react-bridge';
import { isInteractiveTarget } from './interactive';
import { placeDropHint, NO_DRAG_IMAGE } from './drop-hint';
import type { DropVerdict } from './drop-hint';

/** The command selecting a card (ReactKanbanBoardControl.CMD_SELECT_CARD). */
const CMD_SELECT_CARD = 'selectCard';

/** The argument of {@link CMD_SELECT_CARD} naming the card (SelectCardArguments.CARD). */
const ARG_CARD = 'card';

/** The argument of {@link CMD_SELECT_CARD} toggling the card (SelectCardModifiers.TOGGLE). */
const ARG_TOGGLE = 'toggle';

/** The argument of {@link CMD_SELECT_CARD} extending the selection (SelectCardModifiers.RANGE). */
const ARG_RANGE = 'range';

/** The command applying a drop (DropSupport.CMD_DROP). */
const CMD_DROP = 'drop';

/** The command asking whether a drop at the hovered target would be accepted (DropSupport.CMD_DROP_PROBE). */
const CMD_DROP_PROBE = 'dropProbe';

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

/** Where a running drag hovers the board. */
interface DropState {
  /** Key of the hovered column. */
  column: string;

  /** Key of the card the drop is made beside, or of the column for a drop appending to it. */
  target: string;

  /** Where the drop is made relative to the card, `none` for a drop on the column. */
  position: TLDropPosition;

  /** Identifier of the probe asking about this target, `null` for a drag not started here. */
  probe: string | null;
}

/**
 * A board of columns, each holding a vertical list of cards.
 *
 * State:
 * - columns: ColumnDescriptor[] - the columns, in display order
 * - selected: string[] - the keys of the selected cards
 * - multiSelect: boolean - whether several cards may be selected
 * - dragEnabled: boolean, dragType: string - whether and under which type tag cards are dragged
 * - dropAccepts: string[] - the type tags a drop on a column is accepted of
 * - reorder: boolean - whether a drop within a column reorders it
 * - dropVerdicts: Record<string, DropVerdict> - the answers to the probes of the running drag
 *
 * The columns are placed side by side and scroll horizontally where they do not fit; each column
 * scrolls its cards vertically. A card is selected by a click or by Enter / Space while it has the
 * focus; a click on an interactive element inside the card is left to that element. With `Ctrl`
 * (or `Cmd`) the card is toggled instead, with `Shift` the selection is extended up to it; a long
 * press on a touch screen toggles as well, where several cards may be selected.
 *
 * Cards are dragged with the drag payload every drag source of the bridge writes, so they can be
 * dropped on any control accepting their type, and a column accepts what such a control drags.
 * Dragging a selected card drags all selected cards, in display order.
 * While the board reorders its columns, a drop is made at the card boundary nearest to the pointer
 * - on a card or in the gap between two - and an insertion line shows where; otherwise a drop is made on the column as a whole, and the
 * column a dragged card already is in accepts no drop. A column the server refuses a drop on shows
 * the reason next to the pointer.
 *
 * The board always fills the height its container offers, and each card list is a fill barrier.
 */
const TLKanbanBoard: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const fillClass = useFill(true);

  const columns = (state.columns as ColumnDescriptor[]) ?? [];
  const selected = React.useMemo(() => new Set((state.selected as string[] | null) ?? []), [state.selected]);
  const multiSelect = state.multiSelect === true;
  const dragEnabled = state.dragEnabled === true;
  const dragType = (state.dragType as string | null) ?? '';
  const dropAccepts = (state.dropAccepts as string[]) ?? [];
  const reorder = state.reorder === true;
  const dropVerdicts = (state.dropVerdicts as Record<string, DropVerdict>) ?? {};

  const select = React.useCallback(
    (card: string, toggle: boolean, range: boolean) =>
      sendCommand(CMD_SELECT_CARD, { [ARG_CARD]: card, [ARG_TOGGLE]: toggle, [ARG_RANGE]: range }),
    [sendCommand],
  );

  // -- A long press on a card toggles it. The event comes from the bridge's touch gesture. --
  const rootRef = React.useRef<HTMLDivElement | null>(null);
  React.useEffect(() => {
    const root = rootRef.current;
    if (!root || !multiSelect) {
      return undefined;
    }
    const handleLongPress = (event: Event) => {
      const card = (event.target as Element | null)?.closest<HTMLElement>('[data-card]');
      const key = card?.dataset.card;
      if (key && root.contains(card)) {
        void select(key, true, false);
      }
    };
    root.addEventListener(LONG_PRESS_EVENT, handleLongPress);
    return () => root.removeEventListener(LONG_PRESS_EVENT, handleLongPress);
  }, [multiSelect, select]);

  // -- Where an accepted drag currently hovers, or null while none does. --
  const [dropState, setDropState] = React.useState<DropState | null>(null);

  // -- Drop probes sent for the running drag: each target is asked once per drag. --
  const probesRef = React.useRef<{ drag: string; sent: Set<string> } | null>(null);

  const dropVerdict: DropVerdict | undefined = dropState?.probe ? dropVerdicts[dropState.probe] : undefined;
  const dropRefused = dropVerdict !== undefined && !dropVerdict.accepted;

  // A drag hovering the board may end without any event reaching it: a refused drop is not
  // dispatched here, and the source's dragend reaches the source's control only.
  const dragHovers = dropState !== null;
  React.useEffect(() => {
    if (!dragHovers) {
      return undefined;
    }
    return onDragEnd(() => setDropState(null));
  }, [dragHovers]);

  // -- The hint carrying the reason of a refusal, following the pointer. --
  const dragPointerRef = React.useRef<{ x: number; y: number }>({ x: 0, y: 0 });
  const dropHintRef = React.useRef<HTMLDivElement | null>(null);
  const attachDropHint = React.useCallback((hint: HTMLDivElement | null) => {
    dropHintRef.current = hint;
    if (hint) {
      placeDropHint(hint, dragPointerRef.current.x, dragPointerRef.current.y,
        runningDrag()?.image ?? NO_DRAG_IMAGE);
    }
  }, []);

  const handleCardDragStart = React.useCallback((card: CardDescriptor, event: React.DragEvent) => {
    if (isInteractiveTarget(event)) {
      // A drag begun on an input inside the card belongs to that input.
      event.preventDefault();
      return;
    }
    // A selected card takes the other selected cards along, in display order.
    const keys = selected.has(card.key)
      ? columns.flatMap((column) => column.cards.map((c) => c.key).filter((key) => selected.has(key)))
      : [card.key];
    writeDragPayload(event, { source: controlId, keys, selection: false, type: dragType });
  }, [controlId, dragType, columns, selected]);

  /**
   * The target a drag event over the given column points at, or `null` where the column accepts
   * no drop of the running drag.
   */
  const dropTargetAt = React.useCallback(
    (column: ColumnDescriptor, event: React.DragEvent): { target: string; position: TLDropPosition } | null => {
      const drag = runningDrag();
      const withinColumn = drag !== null && drag.payload.source === controlId
        && drag.payload.keys.every((key) => column.cards.some((card) => card.key === key));
      if (withinColumn && !reorder) {
        return null;
      }
      if (reorder) {
        // The nearest boundary between two cards, wherever the pointer is in the column - on a card
        // or in the gap between two: before the first card whose middle is below the pointer, after
        // the last card when the pointer is below the middle of every card.
        const cardElements = Array.from(
          event.currentTarget.querySelectorAll<HTMLElement>('.tlKanbanBoard__card[data-card]'));
        let last: string | null = null;
        for (const cardElement of cardElements) {
          const key = cardElement.dataset.card;
          if (!key) {
            continue;
          }
          const rect = cardElement.getBoundingClientRect();
          if (event.clientY < rect.top + rect.height / 2) {
            return { target: key, position: 'before' };
          }
          last = key;
        }
        if (last !== null) {
          return { target: last, position: 'after' };
        }
      }
      return { target: column.key, position: 'none' };
    }, [controlId, reorder]);

  const handleColumnDragOver = React.useCallback((column: ColumnDescriptor, event: React.DragEvent) => {
    // Coarse acceptance from the payload's type tag alone; whether this particular drop is possible
    // is the server's answer, given once it arrives.
    if (!dragTypeAccepted(event.dataTransfer, dropAccepts)) {
      return;
    }
    const target = dropTargetAt(column, event);
    if (target === null) {
      setDropState(null);
      return;
    }
    dragPointerRef.current = { x: event.clientX, y: event.clientY };
    const drag = runningDrag();
    const hint = dropHintRef.current;
    if (hint) {
      placeDropHint(hint, event.clientX, event.clientY, drag?.image ?? NO_DRAG_IMAGE);
    }
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
          position: target.position,
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
  }, [dropAccepts, dropTargetAt, dropVerdicts, sendCommand]);

  const handleColumnDrop = React.useCallback((column: ColumnDescriptor, event: React.DragEvent) => {
    if (!dragTypeAccepted(event.dataTransfer, dropAccepts)) {
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
      sendCommand(CMD_DROP, {
        source: payload.source,
        // Comma-separated: the command argument is a formatted string list, and a key holds no comma.
        keys: payload.keys.join(','),
        selection: payload.selection,
        targetKey: target.target,
        position: target.position,
      });
    }
  }, [dropAccepts, dropTargetAt, sendCommand]);

  const handleRootDragLeave = React.useCallback((event: React.DragEvent) => {
    // Moving among the board's own descendants fires a leave on each one left behind; only leaving
    // the board itself ends the highlight.
    if (!event.currentTarget.contains(event.relatedTarget as Node | null)) {
      setDropState(null);
    }
  }, []);

  return (
    <div id={controlId} ref={rootRef} className={rootClassName(state, 'tlKanbanBoard', fillClass)}
      onDragLeave={handleRootDragLeave}>
      {dropRefused && dropVerdict?.reason && createPortal(
        <div ref={attachDropHint} className="tlKanbanBoard__dropHint" role="status">
          {dropVerdict.reason}
        </div>,
        document.body)}
      {columns.map(column => {
        const headerId = `${controlId}-${column.key}`;
        const hovered = dropState !== null && dropState.column === column.key ? dropState : null;
        const appendLine = hovered !== null && !dropRefused && reorder && hovered.position === 'none';
        return (
          <section key={column.key}
            className={'tlKanbanBoard__column'
              + (hovered ? (dropRefused ? ' tlKanbanBoard__column--dropRefused' : ' tlKanbanBoard__column--dragover') : '')}
            aria-labelledby={headerId}
            onDragOver={(event) => handleColumnDragOver(column, event)}
            onDrop={(event) => handleColumnDrop(column, event)}
          >
            <header className="tlKanbanBoard__header">
              <span id={headerId} className="tlKanbanBoard__label">{column.label}</span>
              <span className="tlKanbanBoard__count">{column.count}</span>
            </header>
            <FillBarrier>
              <ul className={'tlKanbanBoard__cards' + (appendLine ? ' tlKanbanBoard__cards--dropEnd' : '')}
                role="list" aria-labelledby={headerId}>
                {column.cards.map(card => {
                  const isSelected = selected.has(card.key);
                  const draggable = dragEnabled && card.draggable !== false;
                  const line = hovered !== null && !dropRefused && hovered.target === card.key
                    ? (hovered.position === 'before' ? ' tlKanbanBoard__card--dropBefore' : ' tlKanbanBoard__card--dropAfter')
                    : '';
                  return (
                    <li
                      key={card.key}
                      role="listitem"
                      tabIndex={0}
                      data-card={card.key}
                      draggable={draggable}
                      aria-current={isSelected ? 'true' : undefined}
                      {...(multiSelect ? { [ATTR_LONG_PRESS]: '' } : {})}
                      className={'tlKanbanBoard__card' + (isSelected ? ' tlKanbanBoard__card--selected' : '') + line}
                      onDragStart={draggable ? (event) => handleCardDragStart(card, event) : undefined}
                      onDragEnd={draggable ? () => setDropState(null) : undefined}
                      onMouseDown={event => {
                        if (event.shiftKey && !isInteractiveTarget(event)) {
                          // A Shift-click extends the selection, not a text selection.
                          event.preventDefault();
                        }
                      }}
                      onClick={event => {
                        if (!isInteractiveTarget(event)) {
                          select(card.key, event.ctrlKey || event.metaKey, event.shiftKey);
                        }
                      }}
                      onKeyDown={event => {
                        if (event.target === event.currentTarget && (event.key === 'Enter' || event.key === ' ')) {
                          event.preventDefault();
                          select(card.key, event.ctrlKey || event.metaKey, event.shiftKey);
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
