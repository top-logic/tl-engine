import { React, useTLState, useTLCommand, useI18N, useListReorder, reorderState, filterOptions, rootClassName, stateAttr, usePart, ICON_HANDLE, ICON_VISIBLE, ICON_HIDDEN, ICON_LOCK, ICON_UNLOCK } from 'tl-react-bridge';
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
  'js.table.columnLastVisible': 'At least one column stays visible',
};

/** Shows or hides one of the columns. */
const CMD_COLUMN_VISIBLE = 'columnVisible';

/** Moves one of the columns to the position the `targetIndex` argument names. */
const CMD_COLUMN_REORDER = 'columnReorder';

/** Freezes or releases one of the columns. */
const CMD_COLUMN_LOCK = 'columnLock';

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
}

/** Fills the placeholder {0} of a translated text. */
function format(template: string, arg: string): string {
  return template.replace('{0}', arg);
}

/**
 * The column selection of a table as a layer panel (design system: tl-reorder-list): each column with its handle at
 * the start, its name, and at the end a lock that freezes it and an eye that shows or hides it. Handle, lock and eye
 * appear on hover or focus; a hidden column keeps its place, dimmed, and shows its closed eye, a frozen one its lock.
 * The frozen columns stand as a block under the heading "Frozen". A frozen column is always shown: the eye of a frozen
 * column also releases it, the lock of a hidden one also shows it, and both say so in their names.
 *
 * Order, visibility and lock live on the server (see ReactColumnSelectControl), so a gesture sends a command and the
 * re-pushed list is what renders.
 */
const TLColumnSelect: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);
  const Button = usePart('Button');

  const entries = (state.entries as ColumnEntry[] | undefined) ?? [];
  const visibleCount = entries.filter((entry) => entry.visible).length;

  // A table over a large type offers a column per attribute, which is a long list to scroll. The search narrows what
  // is rendered; the full list stays the reference for the drop position, so a row can be dropped next to a row the
  // search has hidden. Each rendered row therefore carries its position within the full list.
  const [search, setSearch] = React.useState('');
  const matches = new Set(filterOptions(entries, search));
  const shown = entries.map((entry, index) => ({ entry, index })).filter(({ entry }) => matches.has(entry));
  const lastLockedShown = shown.filter(({ entry }) => entry.locked).pop()?.index;
  // The line under the block only where something follows it: a search can leave the block alone.
  const hasUnlockedShown = shown.some(({ entry }) => !entry.locked);

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

  // The moved column is named rather than counted: the server resolves it against its own working copy, which the
  // client's list is only a picture of. The server also decides the lock from where the column lands.
  const handleMove = React.useCallback((from: number, targetIndex: number) => {
    const moved = entries[from];
    if (moved) {
      sendCommand(CMD_COLUMN_REORDER, { [ARG_COLUMN]: moved.name, [ARG_TARGET_INDEX]: targetIndex });
    }
  }, [entries, sendCommand]);

  const reorder = useListReorder({
    axis: 'vertical',
    onMove: handleMove,
    count: entries.length,
    labelOf: (i) => entries[i]?.label ?? '',
    keyOf: (i) => entries[i]?.name ?? String(i),
  });

  const searchable = entries.length > 10;

  return (
    <div id={controlId} className={rootClassName(state)} {...reorder.containerProps}>
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
        {lastLockedShown !== undefined && (
          <li className="tl-reorder-list__section tl-type-label" role="presentation">{i18n['js.table.columnsLocked']}</li>
        )}
        {shown.map(({ entry, index }) => {
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
          const handle = reorder.handleProps(index);
          return (
            <li
              key={entry.name}
              className="tl-reorder-list__item"
              data-tl-state={stateAttr(
                reorderState(reorder.itemState(index)),
                !entry.visible && 'off',
                entry.locked && 'locked',
                index === lastLockedShown && hasUnlockedShown && 'section-end',
              )}
              {...reorder.itemProps(index)}
            >
              <Button
                label={handle['aria-label']}
                image={ICON_HANDLE}
                display="icon-only"
                appearance="ghost"
                small
                className="tl-reorder-list__handle tl-reveal-on-hover"
                {...handle}
              />
              <span className="tl-reorder-list__label">{entry.label}</span>
              <span className="tl-reorder-list__actions">
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
                  image={entry.visible ? ICON_VISIBLE : ICON_HIDDEN}
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
        })}
      </ul>
      {reorder.announcer}
    </div>
  );
};

export default TLColumnSelect;
