import { React, useTLState, useTLCommand, useI18N, useListReorder, reorderState, filterOptions, rootClassName, stateAttr, usePart, tooltipProps, ICON_HANDLE, ICON_VISIBLE, ICON_ADD, ICON_LOCK, ICON_UNLOCK, ICON_GROUP } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { SearchField } from './SearchField';

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
  'js.table.columnLastVisible': 'At least one column stays visible',
  'js.table.groupBy': 'Group by this column',
  'js.table.ungroup': 'Remove grouping',
};

/** Shows or hides one of the columns. */
const CMD_COLUMN_VISIBLE = 'columnVisible';

/** Moves one of the columns to the position the `targetIndex` argument names. */
const CMD_COLUMN_REORDER = 'columnReorder';

/** Freezes or releases one of the columns. */
const CMD_COLUMN_LOCK = 'columnLock';

/** Groups the rows by the column the `column` argument names, or by none when it is empty. */
const CMD_GROUP = 'group';

/** Argument of every command: which column it applies to. */
const ARG_COLUMN = 'column';

/** Argument of {@link CMD_COLUMN_VISIBLE}: whether the column is shown. */
const ARG_VISIBLE = 'visible';

/** Argument of {@link CMD_COLUMN_LOCK}: whether the column is frozen. */
const ARG_LOCKED = 'locked';

/** Argument of {@link CMD_COLUMN_REORDER}: the position the moved column ends up at. */
const ARG_TARGET_INDEX = 'targetIndex';

interface ColumnEntry {
  name: string;
  label: string;
  visible: boolean;
  /** Whether the table freezes this column; the locked entries stand as a block at the start. */
  locked: boolean;
  /** Whether the rows are grouped by this column; at most one entry is. */
  grouped: boolean;
}

/** Fills the placeholder {0} of a translated text. */
function format(template: string, arg: string): string {
  return template.replace('{0}', arg);
}

/**
 * The column selection of a table as a layer panel (design system: tl-reorder-list): each column with its handle at
 * the start, its name, and at the end a lock that freezes it and an eye that shows or hides it. Handle, lock and eye
 * appear on hover or focus; a frozen column shows its lock. The list has up to three sections in one scroll area: the
 * frozen columns under the heading "Frozen", the movable ones under "Movable" (the heading only where "Frozen" stands
 * before them) and the hidden ones under "Hidden" (left out where none is hidden). The sections are the display only:
 * the server keeps each column at its position, so a column shown again is back where it was. A hidden column has no
 * handle and is not dragged, and shows a plus where the others show the eye. A frozen column is always shown: the eye
 * of a frozen column also releases it, the lock of a hidden one also shows it, and both say so in their names.
 *
 * A column is dragged within its block, the frozen or the movable one; the lock is the way from one to the other.
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
 * re-pushed list is what renders; the table follows each one at once.
 */
const TLColumnSelect: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);
  const Button = usePart('Button');

  const entries = (state.entries as ColumnEntry[] | undefined) ?? [];
  // The shown columns in the order of the server's list: the hook counts positions among them, since the hidden ones
  // are listed apart and neither dragged nor dropped on.
  const visibleEntries = React.useMemo(() => entries.filter((entry) => entry.visible), [entries]);
  const visibleCount = visibleEntries.length;
  const groupable = state.groupable !== false;

  // A table over a large type offers a column per attribute, which is a long list to scroll. The search narrows what
  // is rendered; the list of the shown columns stays the reference for the drop position, so a row can be dropped next
  // to a row the search has hidden. Each rendered row therefore carries its position within that list.
  const [search, setSearch] = React.useState('');
  const matches = new Set(filterOptions(entries, search));
  const shown = entries.filter((entry) => matches.has(entry));
  const lockedShown = shown.filter((entry) => entry.visible && entry.locked);
  const movableShown = shown.filter((entry) => entry.visible && !entry.locked);
  const hiddenShown = shown.filter((entry) => !entry.visible);

  // A click on lock or eye can reorder the list (the column joins or leaves the frozen block; hiding a frozen column
  // releases it), and React then moves the row's node, which takes the focus from the button. The clicked button is
  // remembered by column name and kind and focused again once the re-pushed list has rendered, as the keyboard move of
  // the handle does - but only a button that had the focus: a click that does not focus the button (Safari, for one)
  // must not scroll the list to the block and show a focus ring.
  const buttonRefs = React.useRef(new Map<string, HTMLElement>());
  const pendingFocus = React.useRef<string | null>(null);
  const buttonRef = (key: string) => (element: HTMLElement | null) => {
    if (element) buttonRefs.current.set(key, element); else buttonRefs.current.delete(key);
  };

  React.useLayoutEffect(() => {
    const key = pendingFocus.current;
    if (key === null) {
      return;
    }
    pendingFocus.current = null;
    const button = buttonRefs.current.get(key);
    // The user went on before the new list arrived: the focus is not the list's to take back.
    const active = document.activeElement;
    if (active !== null && active !== document.body && active !== button) {
      return;
    }
    if (button && active !== button) {
      button.focus();
    }
  }, [entries]);

  const keepFocus = (key: string, event: React.MouseEvent<HTMLElement>) => {
    pendingFocus.current = document.activeElement === event.currentTarget ? key : null;
  };

  const handleToggle = React.useCallback((name: string, visible: boolean, event: React.MouseEvent<HTMLElement>) => {
    keepFocus(`${name}:eye`, event);
    sendCommand(CMD_COLUMN_VISIBLE, { [ARG_COLUMN]: name, [ARG_VISIBLE]: visible });
  }, [sendCommand]);

  const handleLock = React.useCallback((name: string, locked: boolean, event: React.MouseEvent<HTMLElement>) => {
    keepFocus(`${name}:lock`, event);
    sendCommand(CMD_COLUMN_LOCK, { [ARG_COLUMN]: name, [ARG_LOCKED]: locked });
  }, [sendCommand]);

  // A click on the row itself shows or hides the column, as the eye does: the row is the click target of the whole entry,
  // and the eye shows the state. The handle, the toggle, the lock and the eye are buttons with a gesture of their own,
  // which a click on them must not repeat. The last shown column stays, as its eye does.
  const handleRowClick = React.useCallback((entry: ColumnEntry, lastVisible: boolean, event: React.MouseEvent<HTMLElement>) => {
    const button = (event.target as HTMLElement).closest('button');
    if (lastVisible || (button !== null && event.currentTarget.contains(button))) {
      return;
    }
    sendCommand(CMD_COLUMN_VISIBLE, { [ARG_COLUMN]: entry.name, [ARG_VISIBLE]: !entry.visible });
  }, [sendCommand]);

  const handleGroup = React.useCallback((name: string, grouped: boolean) => {
    sendCommand(CMD_GROUP, { [ARG_COLUMN]: grouped ? '' : name });
  }, [sendCommand]);

  // The moved column is named rather than counted: the server resolves it against its own list, which the client's
  // is only a picture of. The hook counts among the shown columns; the server's list holds the hidden ones too, so the
  // target is the position in front of the column the drop lands before (or behind the last shown one) in that list.
  // The server keeps the column in its block.
  const handleMove = React.useCallback((from: number, to: number) => {
    const moved = visibleEntries[from];
    if (!moved) {
      return;
    }
    const restVisible = visibleEntries.filter((_, i) => i !== from);
    const rest = entries.filter((entry) => entry !== moved);
    const before = restVisible[to];
    const targetIndex = before !== undefined
      ? rest.indexOf(before)
      : restVisible.length > 0 ? rest.indexOf(restVisible[restVisible.length - 1]) + 1 : rest.length;
    sendCommand(CMD_COLUMN_REORDER, { [ARG_COLUMN]: moved.name, [ARG_TARGET_INDEX]: targetIndex });
  }, [entries, visibleEntries, sendCommand]);

  const reorder = useListReorder({
    axis: 'vertical',
    onMove: handleMove,
    count: visibleEntries.length,
    labelOf: (i) => visibleEntries[i]?.label ?? '',
    keyOf: (i) => visibleEntries[i]?.name ?? String(i),
    // Dragging stays within the block: the frozen columns among themselves, the movable ones among themselves.
    accepts: (from, to) => !visibleEntries[from]?.locked === !visibleEntries[to]?.locked,
  });

  const renderRow = (entry: ColumnEntry) => {
    // The position among the shown columns, for the drag; a hidden column has none.
    const index = visibleEntries.indexOf(entry);
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
    const handle = index >= 0 ? reorder.handleProps(index) : null;
    return (
      <li
        key={entry.name}
        className="tl-reorder-list__item"
        data-tl-state={stateAttr(
          index >= 0 && reorderState(reorder.itemState(index)),
          !entry.visible && 'off',
          entry.locked && 'locked',
          grouped && 'grouped',
          lastVisible && 'last',
        )}
        // The row says why a click does nothing, as the disabled eye does.
        {...(lastVisible ? tooltipProps(i18n['js.table.columnLastVisible']) : {})}
        // A hidden column is not dragged and not dropped on: it has no place in the order of the shown ones.
        {...(index >= 0 ? reorder.itemProps(index) : {})}
        onClick={(event) => handleRowClick(entry, lastVisible, event)}
      >
        {handle !== null && (
          <Button
            label={handle['aria-label']}
            image={ICON_HANDLE}
            display="icon-only"
            appearance="ghost"
            small
            className="tl-reorder-list__handle tl-reveal-on-hover"
            {...handle}
          />
        )}
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
        {lockedShown.length > 0 && (
          <li className="tl-reorder-list__section tl-type-label" role="presentation">{i18n['js.table.columnsLocked']}</li>
        )}
        {lockedShown.map(renderRow)}
        {lockedShown.length > 0 && movableShown.length > 0 && (
          <li className="tl-reorder-list__section tl-type-label" role="presentation">{i18n['js.table.columnsMovable']}</li>
        )}
        {movableShown.map(renderRow)}
        {hiddenShown.length > 0 && (
          <li className="tl-reorder-list__section tl-type-label" role="presentation">{i18n['js.table.columnsHidden']}</li>
        )}
        {hiddenShown.map(renderRow)}
      </ul>
      {reorder.announcer}
    </div>
  );
};

export default TLColumnSelect;
