import { React, useTLState, useTLCommand, useI18N, useListReorder, reorderState, filterOptions, rootClassName, stateAttr, usePart, tooltipProps, ICON_HANDLE, ICON_VISIBLE, ICON_ADD, ICON_LOCK, ICON_UNLOCK, ICON_GROUP } from 'tl-react-bridge';
import type { TLCellProps, ReorderDirection, ReorderDropTarget, ReorderStep } from 'tl-react-bridge';
import { SearchField } from './SearchField';
import { applyColumnMove, lockedCount } from './columnMove';
import type { ColumnEntry, ColumnMove } from './columnMove';

const I18N_KEYS = {
  'js.table.columnSearch': 'Find column',
  'js.table.columnShow': 'Show {0}',
  'js.table.columnHide': 'Hide {0}',
  'js.table.columnLock': 'Freeze {0}',
  'js.table.columnUnlock': 'Unfreeze {0}',
  'js.table.columnShowLock': 'Show and freeze {0}',
  'js.table.columnHideUnlock': 'Hide and unfreeze {0}',
  'js.table.columnShowShort': 'Show',
  'js.table.columnHideShort': 'Hide',
  'js.table.columnLockShort': 'Freeze',
  'js.table.columnUnlockShort': 'Unfreeze',
  'js.table.columnsLocked': 'Frozen',
  'js.table.columnsMovable': 'Movable',
  'js.table.columnsHidden': 'Hidden',
  'js.table.columnDropLocked': 'Drag here to freeze',
  'js.table.columnDropMovable': 'Drag here to unfreeze',
  'js.table.columnDropHidden': 'Drag here to hide',
  'js.table.columnMovedLocked': '{0}, frozen, position {1} of {2}',
  'js.table.columnMovedMovable': '{0}, movable, position {1} of {2}',
  'js.table.columnMovedHidden': '{0}, hidden',
  'js.reorder.moved': '{0}, position {1} of {2}',
  'js.table.columnLastVisible': 'At least one column stays visible',
  'js.table.groupBy': 'Group by this column',
  'js.table.ungroup': 'Remove grouping',
};

/** Shows or hides one of the columns. */
const CMD_COLUMN_VISIBLE = 'columnVisible';

/**
 * Moves one of the columns to the position the `targetIndex` argument names and into the section the arguments
 * `locked` and `visible` name, in one step.
 */
const CMD_COLUMN_MOVE = 'columnMove';

/** Freezes or releases one of the columns. */
const CMD_COLUMN_LOCK = 'columnLock';

/** Groups the rows by the column the `column` argument names, or by none when it is empty. */
const CMD_GROUP = 'group';

/** Argument of every command: which column it applies to. */
const ARG_COLUMN = 'column';

/** Argument of {@link CMD_COLUMN_VISIBLE} and {@link CMD_COLUMN_MOVE}: whether the column is shown. */
const ARG_VISIBLE = 'visible';

/** Argument of {@link CMD_COLUMN_LOCK} and {@link CMD_COLUMN_MOVE}: whether the column is frozen. */
const ARG_LOCKED = 'locked';

/** Argument of {@link CMD_COLUMN_MOVE}: the position the moved column ends up at, in the whole list without it. */
const ARG_TARGET_INDEX = 'targetIndex';

/** The sections of the list, which are the zones a column is dropped into as well. */
type Section = 'locked' | 'movable' | 'hidden';

/** The sections in the order of the list, each with its heading and the placeholder it shows while empty. */
const SECTIONS: readonly { zone: Section; heading: string; empty: string }[] = [
  { zone: 'locked', heading: 'js.table.columnsLocked', empty: 'js.table.columnDropLocked' },
  { zone: 'movable', heading: 'js.table.columnsMovable', empty: 'js.table.columnDropMovable' },
  { zone: 'hidden', heading: 'js.table.columnsHidden', empty: 'js.table.columnDropHidden' },
];

/** The list of a control whose state holds none yet. */
const NO_ENTRIES: readonly ColumnEntry[] = [];

/** Fills the placeholders {0}, {1}, ... of a translated text. */
function format(template: string, ...args: string[]): string {
  return args.reduce((text, arg, i) => text.replace('{' + i + '}', arg), template);
}

/** The section a column is listed in. */
function sectionOf(entry: ColumnEntry): Section {
  return !entry.visible ? 'hidden' : entry.locked ? 'locked' : 'movable';
}

/**
 * The order the list shows: the frozen columns, the movable ones, the hidden ones, each in the order of the server's
 * list. This is the list the drag and the keyboard count positions in.
 */
function displayOrder(entries: readonly ColumnEntry[]): ColumnEntry[] {
  return [
    ...entries.filter((entry) => entry.visible && entry.locked),
    ...entries.filter((entry) => entry.visible && !entry.locked),
    ...entries.filter((entry) => !entry.visible),
  ];
}

/** Whether two lists show the same columns in the same order and state. */
function sameList(a: readonly ColumnEntry[], b: readonly ColumnEntry[]): boolean {
  return a.length === b.length && a.every((entry, i) =>
    entry.name === b[i].name && entry.visible === b[i].visible && entry.locked === b[i].locked);
}

/**
 * The move showing the given column at the given position among the shown columns, frozen or not.
 *
 * The server's list holds the hidden columns as well, at their places, and takes the target counted without the moved
 * column: the target is the place in front of the shown column the moved one comes before, or behind the last shown
 * one. The shown columns stand in the server's list in the order the list shows them, the frozen block first.
 */
function showAt(entries: readonly ColumnEntry[], name: string, position: number, locked: boolean): ColumnMove {
  const rest = entries.filter((entry) => entry.name !== name);
  const restVisible = rest.filter((entry) => entry.visible);
  const targetIndex = position < restVisible.length
    ? rest.indexOf(restVisible[position])
    : restVisible.length > 0
      ? rest.indexOf(restVisible[restVisible.length - 1]) + 1
      : entries.findIndex((entry) => entry.name === name);
  return { column: name, targetIndex, locked, visible: true };
}

/** The move hiding the given column; the server keeps it at its place and ignores the target. */
function hide(entries: readonly ColumnEntry[], name: string): ColumnMove {
  return { column: name, targetIndex: entries.findIndex((entry) => entry.name === name), locked: false, visible: false };
}

/** The commands sent and the list expected from them, see {@link TLColumnSelect}. */
interface Sync {

  /** The control the commands were sent to; a new control of the selection starts counting afresh. */
  controlId: string;

  /** The revision the server reaches once it has answered every command sent so far. */
  expected: number;

  /** The list expected after the last move, shown while answers are out; `null` for none. */
  predicted: readonly ColumnEntry[] | null;
}

/**
 * The column selection of a table as a layer panel (design system: tl-reorder-list): each column with its handle at
 * the start, its name, and at the end a lock that freezes it and an eye that shows or hides it. Handle, lock and eye
 * appear on hover or focus; a frozen column shows its lock. The list has three sections in one scroll area, each under
 * its heading: the frozen columns under "Frozen", the movable ones under "Movable" and the hidden ones under "Hidden".
 * Each section stands also while it is empty, with a quiet row naming what a drop there does ("Drag here to freeze");
 * a search leaves out the sections without a match. A hidden column shows a plus where the others show the eye. A
 * frozen column is always shown: the eye of a frozen column also releases it, the lock of a hidden one also shows it,
 * and both say so in their names.
 *
 * The section a column is dropped into decides its state: dragged among the frozen columns it is frozen there, among
 * the movable ones it is released there, and into "Hidden" it is hidden (a frozen one released as well) - a hidden
 * column has its handle as well and is shown where it is dropped. A drop is one command (`columnMove`) naming the
 * position and the state. "Hidden" is a zone rather than an order: the server keeps a hidden column at its place, so
 * shown again by its eye it is back where it was; the pointer over "Hidden" marks the whole section as the target
 * instead of a place between rows. Every heading and every placeholder takes the drop of its section as well. The last
 * shown column is not taken by "Hidden". Alt + arrow on the handle moves the same way, one step across a section line
 * at a time: up from the first movable column freezes it, down from the last frozen one releases it, down from the
 * last movable one hides it, up from a hidden one shows it at the end of "Movable"; within "Hidden" it moves nothing.
 *
 * Unless the table is a tree (state `groupable` false), each row also offers a toggle grouping the rows by its column,
 * hidden or shown; choosing another column moves the grouping there, choosing the grouped one removes it. Like lock and
 * eye it appears on hover or focus, but on the grouped row it always stands, so the current grouping is in sight.
 *
 * A click on the row itself shows or hides the column like the eye (the last shown column stays). The root is the host
 * of the list (tl-reorder-list__host): in the popover of the view it fills the page, the search stays above and the
 * list is the one area that scrolls.
 *
 * Order, visibility and lock live on the server (see ReactColumnSelectControl), so a gesture sends a command and the
 * re-pushed list is what renders; the table follows each one at once. A drop and an Alt + arrow show the list they
 * lead to at once, computed as the server computes it, so that the row does not jump back until the answer arrives.
 * The server counts every command of the selection in its state `revision`; the selection counts the commands it
 * sent, and shows the expected list as long as the revision has not caught up with them. Then the server's list is
 * the one shown - where it differs (the server refused), it holds.
 */
const TLColumnSelect: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);
  const Button = usePart('Button');

  const serverEntries = (state.entries as ColumnEntry[] | undefined) ?? NO_ENTRIES;
  const revision = typeof state.revision === 'number' ? state.revision : 0;
  const revisionRef = React.useRef(revision);
  revisionRef.current = revision;

  const [sync, setSync] = React.useState<Sync | null>(null);
  const current = sync !== null && sync.controlId === controlId ? sync : null;
  const predicted = current !== null && revision < current.expected ? current.predicted : null;
  // The list shown and read by every gesture: the predicted one while the server has not answered, else the server's.
  const entries = predicted ?? serverEntries;

  // The list in the order it is shown. The hook counts positions in it; with a search the rendered rows are a part of
  // it, and each rendered row carries its position in the whole, so a row can be dropped next to one the search hides.
  const display = React.useMemo(() => displayOrder(entries), [entries]);
  const visibleCount = display.filter((entry) => entry.visible).length;
  const lockedVisible = display.filter((entry) => entry.visible && entry.locked).length;
  const groupable = state.groupable !== false;

  const [search, setSearch] = React.useState('');
  const searching = search.trim() !== '';
  const matches = new Set(filterOptions(display, search));

  // Every command counts towards the revision the server answers with, the move carries the list it leads to.
  const send = React.useCallback((command: string, args: Record<string, unknown>, expected?: readonly ColumnEntry[]) => {
    setSync((prev) => {
      const sent = prev !== null && prev.controlId === controlId ? prev : null;
      const open = sent !== null && revisionRef.current < sent.expected;
      return {
        controlId,
        expected: (open ? sent.expected : revisionRef.current) + 1,
        predicted: expected ?? (open ? sent.predicted : null),
      };
    });
    sendCommand(command, args);
  }, [controlId, sendCommand]);

  // A click on lock or eye can reorder the list (the column joins or leaves the frozen block; hiding a frozen column
  // releases it), and React then moves the row's node, which takes the focus from the button. The clicked button is
  // remembered by column name and kind and focused again once the re-pushed list has rendered, as the keyboard move of
  // the handle does - but only a button that had the focus: a click that does not focus the button (Safari, for one)
  // must not scroll the list to the block and show a focus ring. A drag or an Alt + arrow remembers the handle the same
  // way, and keeps it until the server has answered: an answer differing from the expected list moves the row again.
  const buttonRefs = React.useRef(new Map<string, HTMLElement>());
  const pendingFocus = React.useRef<string | null>(null);
  const registerButton = (key: string, element: HTMLElement | null) => {
    if (element) buttonRefs.current.set(key, element); else buttonRefs.current.delete(key);
  };
  const buttonRef = (key: string) => (element: HTMLElement | null) => registerButton(key, element);

  React.useLayoutEffect(() => {
    const key = pendingFocus.current;
    if (key === null) {
      return;
    }
    const button = buttonRefs.current.get(key);
    // The user went on before the new list arrived: the focus is not the list's to take back.
    const active = document.activeElement;
    if (active !== null && active !== document.body && active !== button) {
      pendingFocus.current = null;
      return;
    }
    if (button && active !== button) {
      button.focus();
    }
    if (predicted === null) {
      pendingFocus.current = null;
    }
  }, [entries]);

  const keepFocus = (key: string, event: React.MouseEvent<HTMLElement>) => {
    pendingFocus.current = document.activeElement === event.currentTarget ? key : null;
  };

  const handleToggle = React.useCallback((name: string, visible: boolean, event: React.MouseEvent<HTMLElement>) => {
    keepFocus(`${name}:eye`, event);
    send(CMD_COLUMN_VISIBLE, { [ARG_COLUMN]: name, [ARG_VISIBLE]: visible });
  }, [send]);

  const handleLock = React.useCallback((name: string, locked: boolean, event: React.MouseEvent<HTMLElement>) => {
    keepFocus(`${name}:lock`, event);
    send(CMD_COLUMN_LOCK, { [ARG_COLUMN]: name, [ARG_LOCKED]: locked });
  }, [send]);

  // A click on the row itself shows or hides the column, as the eye does: the row is the click target of the whole entry,
  // and the eye shows the state. The handle, the toggle, the lock and the eye are buttons with a gesture of their own,
  // which a click on them must not repeat. The last shown column stays, as its eye does.
  const handleRowClick = React.useCallback((entry: ColumnEntry, lastVisible: boolean, event: React.MouseEvent<HTMLElement>) => {
    const button = (event.target as HTMLElement).closest('button');
    if (lastVisible || (button !== null && event.currentTarget.contains(button))) {
      return;
    }
    send(CMD_COLUMN_VISIBLE, { [ARG_COLUMN]: entry.name, [ARG_VISIBLE]: !entry.visible });
  }, [send]);

  const handleGroup = React.useCallback((name: string, grouped: boolean) => {
    send(CMD_GROUP, { [ARG_COLUMN]: grouped ? '' : name });
  }, [send]);

  // Sends a move of a drag or an Alt + arrow and shows the list it leads to; `null` where the move changes nothing,
  // which sends nothing. The moved column is named rather than counted: the server resolves it against its own list.
  const commit = (name: string, move: ColumnMove): readonly ColumnEntry[] | null => {
    const next = applyColumnMove(entries, move);
    if (sameList(next, entries)) {
      return null;
    }
    const handleKey = `${name}:handle`;
    pendingFocus.current = document.activeElement === buttonRefs.current.get(handleKey) ? handleKey : null;
    send(CMD_COLUMN_MOVE, {
      [ARG_COLUMN]: move.column,
      [ARG_TARGET_INDEX]: move.targetIndex,
      [ARG_LOCKED]: move.locked,
      [ARG_VISIBLE]: move.visible,
    }, next);
    return next;
  };

  // A drop on a shown row: the section of that row decides the state. The place after the last frozen column and the
  // place before the first movable one are the same position, so the side of the drop tells the section.
  const handleMove = (from: number, to: number, target?: ReorderDropTarget) => {
    const moved = display[from];
    const onto = target !== undefined ? display[target.index] : moved;
    if (moved === undefined || onto === undefined) {
      return;
    }
    commit(moved.name, showAt(entries, moved.name, to, onto.visible ? onto.locked : moved.locked));
  };

  // "Hidden" takes every shown column but the last one; it holds no order, so a hidden column dropped there stays.
  const acceptsZone = (from: number, zone: string) => {
    const moved = display[from];
    if (moved === undefined) {
      return false;
    }
    return zone !== 'hidden' || (moved.visible && visibleCount > 1);
  };

  // A drop on a heading, on a placeholder or among the hidden rows: frozen at the start of the block, released at the
  // start of the rest, or hidden at its place.
  const handleZoneDrop = (from: number, zone: string) => {
    const moved = display[from];
    if (moved === undefined) {
      return;
    }
    switch (zone as Section) {
      case 'locked':
        commit(moved.name, { column: moved.name, targetIndex: 0, locked: true, visible: true });
        break;
      case 'movable':
        commit(moved.name, {
          column: moved.name,
          targetIndex: lockedCount(entries.filter((entry) => entry.name !== moved.name)),
          locked: false,
          visible: true,
        });
        break;
      case 'hidden':
        commit(moved.name, hide(entries, moved.name));
        break;
    }
  };

  // Alt + arrow: one position within a section, or across the line into the neighbouring section, where the column
  // takes the state of that section and keeps its position among the shown columns. "Hidden" holds no order: a hidden
  // column only goes up, to the end of "Movable".
  const handleStep = (index: number, direction: ReorderDirection): ReorderStep | null => {
    const moved = display[index];
    if (moved === undefined) {
      return null;
    }
    const name = moved.name;
    let move: ColumnMove | null;
    if (!moved.visible) {
      move = direction < 0 ? showAt(entries, name, visibleCount, false) : null;
    } else if (moved.locked) {
      if (direction < 0) {
        move = index === 0 ? null : showAt(entries, name, index - 1, true);
      } else {
        move = index === lockedVisible - 1 ? showAt(entries, name, index, false) : showAt(entries, name, index + 1, true);
      }
    } else if (direction < 0) {
      move = index === lockedVisible ? showAt(entries, name, index, true) : showAt(entries, name, index - 1, false);
    } else if (index === visibleCount - 1) {
      move = visibleCount > 1 ? hide(entries, name) : null;
    } else {
      move = showAt(entries, name, index + 1, false);
    }
    const next = move === null ? null : commit(name, move);
    if (next === null) {
      return null;
    }
    const after = displayOrder(next);
    const to = after.findIndex((entry) => entry.name === name);
    const entry = after[to];
    const shown = String(after.filter((other) => other.visible).length);
    const section = sectionOf(entry);
    const announcement = section === 'hidden'
      ? format(i18n['js.table.columnMovedHidden'], entry.label)
      : format(i18n[section !== sectionOf(moved)
        ? (section === 'locked' ? 'js.table.columnMovedLocked' : 'js.table.columnMovedMovable')
        : 'js.reorder.moved'], entry.label, String(to + 1), shown);
    return { to, announcement };
  };

  const reorder = useListReorder({
    axis: 'vertical',
    onMove: handleMove,
    count: display.length,
    labelOf: (i) => display[i]?.label ?? '',
    keyOf: (i) => display[i]?.name ?? String(i),
    // Where the frozen block meets the movable columns, a drop at the column's own position may still change its section.
    reportUnmoved: true,
    // The hidden rows are the zone "Hidden": no insertion mark between them, the whole section is the target.
    zoneOf: (i) => display[i] !== undefined && !display[i].visible ? 'hidden' : null,
    acceptsZone,
    onZoneDrop: handleZoneDrop,
    onStep: handleStep,
  });

  // The section a drop would currently go into: the zone the pointer is over, or the section of the row it marks.
  const hovered = reorder.dropTarget !== null ? display[reorder.dropTarget.index] : undefined;
  const targetSection: Section | null = reorder.dropZone !== null
    ? reorder.dropZone as Section
    : hovered !== undefined ? sectionOf(hovered) : null;

  const renderRow = (entry: ColumnEntry, index: number, target: boolean) => {
    // Keep the table from losing its last column: there would be nothing left to click.
    const lastVisible = entry.visible && visibleCount <= 1;
    const eyeLabel = lastVisible
      ? i18n['js.table.columnLastVisible']
      : format(i18n[!entry.visible ? 'js.table.columnShow'
        : entry.locked ? 'js.table.columnHideUnlock' : 'js.table.columnHide'], entry.label);
    // The tooltips of eye and lock stand next to the row and need no column name; the accessible names keep it,
    // since a screen reader announces a button on its own.
    const eyeTip = lastVisible
      ? eyeLabel
      : i18n[entry.visible ? 'js.table.columnHideShort' : 'js.table.columnShowShort'];
    const lockLabel = format(i18n[entry.locked ? 'js.table.columnUnlock'
      : entry.visible ? 'js.table.columnLock' : 'js.table.columnShowLock'], entry.label);
    const lockTip = i18n[entry.locked ? 'js.table.columnUnlockShort' : 'js.table.columnLockShort'];
    const grouped = groupable && entry.grouped === true;
    // The hook registers the handle for the focus after a keyboard move, the list for the focus after the server's answer.
    const { ref: handleRef, ...handle } = reorder.handleProps(index);
    const handleKey = `${entry.name}:handle`;
    return (
      <li
        key={'column-' + entry.name}
        className="tl-reorder-list__item"
        data-tl-state={stateAttr(
          reorderState(reorder.itemState(index)),
          !entry.visible && 'off',
          entry.locked && 'locked',
          grouped && 'grouped',
          lastVisible && 'last',
          target && 'drop-target',
        )}
        // The row says why a click does nothing, as the disabled eye does.
        {...(lastVisible ? tooltipProps(i18n['js.table.columnLastVisible']) : {})}
        {...reorder.itemProps(index)}
        onClick={(event) => handleRowClick(entry, lastVisible, event)}
      >
        <Button
          label={handle['aria-label']}
          image={ICON_HANDLE}
          display="icon-only"
          appearance="ghost"
          small
          className="tl-reorder-list__handle tl-reveal-on-hover"
          {...handle}
          ref={(element: HTMLElement | null) => {
            handleRef?.(element);
            registerButton(handleKey, element);
          }}
        />
        <span className="tl-reorder-list__label">{entry.label}</span>
        <span className="tl-reorder-list__actions">
          {groupable && (
            <Button
              label={i18n[grouped ? 'js.table.ungroup' : 'js.table.groupBy']}
              image={ICON_GROUP}
              display="icon-only"
              appearance="ghost"
              small
              checked={grouped}
              // The grouped row keeps its toggle in sight; the others show it on hover or focus.
              className={grouped ? undefined : 'tl-reveal-on-hover'}
              onClick={() => handleGroup(entry.name, grouped)}
            />
          )}
          <Button
            ref={buttonRef(`${entry.name}:lock`)}
            label={lockLabel}
            tooltip={lockTip}
            image={entry.locked ? ICON_LOCK : ICON_UNLOCK}
            display="icon-only"
            appearance="ghost"
            small
            className="tl-reorder-list__lock tl-reveal-on-hover"
            onClick={(event) => handleLock(entry.name, !entry.locked, event)}
          />
          <Button
            ref={buttonRef(`${entry.name}:eye`)}
            label={eyeLabel}
            tooltip={eyeTip}
            image={entry.visible ? ICON_VISIBLE : ICON_ADD}
            display="icon-only"
            appearance="ghost"
            small
            disabled={lastVisible}
            className="tl-reorder-list__eye tl-reveal-on-hover"
            onClick={(event) => handleToggle(entry.name, !entry.visible, event)}
          />
        </span>
      </li>
    );
  };

  // One flat list of headings, placeholders and rows, keyed apart: a row moving into another section keeps its node.
  const rows: Record<Section, { entry: ColumnEntry; index: number }[]> = { locked: [], movable: [], hidden: [] };
  display.forEach((entry, index) => {
    if (matches.has(entry)) {
      rows[sectionOf(entry)].push({ entry, index });
    }
  });
  const items: React.ReactElement[] = [];
  for (const section of SECTIONS) {
    const sectionRows = rows[section.zone];
    if (sectionRows.length === 0 && searching) {
      continue;
    }
    const target = targetSection === section.zone;
    const targetState = stateAttr(target && 'drop-target');
    items.push(
      <li
        key={'section-' + section.zone}
        className="tl-reorder-list__section tl-type-label"
        role="presentation"
        data-tl-state={targetState}
        {...reorder.zoneProps(section.zone)}
      >
        {i18n[section.heading]}
      </li>,
    );
    if (sectionRows.length === 0) {
      items.push(
        <li
          key={'empty-' + section.zone}
          className="tl-reorder-list__empty"
          data-tl-state={targetState}
          {...reorder.zoneProps(section.zone)}
        >
          {i18n[section.empty]}
        </li>,
      );
    }
    for (const { entry, index } of sectionRows) {
      items.push(renderRow(entry, index, target));
    }
  }

  const searchable = entries.length > 10;

  return (
    <div id={controlId} className={rootClassName(state, 'tl-reorder-list__host')} {...reorder.containerProps}>
      {searchable && (
        <SearchField
          className="tl-reorder-list__search"
          label={i18n['js.table.columnSearch']}
          placeholder={i18n['js.table.columnSearch']}
          value={search}
          onChange={setSearch}
        />
      )}
      <ul className="tl-reorder-list tl-type-body">
        {items}
      </ul>
      {reorder.announcer}
    </div>
  );
};

export default TLColumnSelect;
