import { React, useTLState, useTLCommand, useI18N, useListReorder, reorderState, filterOptions, rootClassName, tooltipProps, ThemeIcon, ICON_HANDLE, ICON_GROUP } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { buttonClassName } from './button/buttonClassName';
import { SearchField } from './SearchField';

const I18N_KEYS = {
  'js.table.columnSearch': 'Find column',
  'js.table.groupBy': 'Group by this column',
  'js.table.ungroup': 'Remove grouping',
};

/** Shows or hides one of the columns. */
const CMD_COLUMN_VISIBLE = 'columnVisible';

/** Moves one of the columns to the position the `targetIndex` argument names. */
const CMD_COLUMN_REORDER = 'columnReorder';

/** Groups the rows by one of the columns, or removes the grouping by it. */
const CMD_GROUP_BY = 'groupBy';

/** Argument of every command: which column it applies to. */
const ARG_COLUMN = 'column';

/** Argument of {@link CMD_COLUMN_VISIBLE}: whether the column is shown. */
const ARG_VISIBLE = 'visible';

/** Argument of {@link CMD_COLUMN_REORDER}: the position the moved column ends up at. */
const ARG_TARGET_INDEX = 'targetIndex';

interface ColumnEntry {
  name: string;
  label: string;
  visible: boolean;
  /** Whether the rows are grouped by this column - at most one entry is. */
  grouped: boolean;
}

/**
 * The column selection of a table: the columns as a `tl-reorder-list`: dragged by the row, moved
 * with Alt + arrow on the handle, shown or hidden by a checkbox (design system: tl-reorder-list).
 *
 * Both the order and the checked state live on the server (see ReactColumnSelectControl), so a
 * gesture sends a command and the re-pushed list is what renders. Each row also offers to group
 * the table's rows by that column; the choice applies with the columns. A table whose rows cannot
 * be grouped - a tree table - offers no grouping (state.groupable).
 */
const TLColumnSelect: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);

  const entries = (state.entries as ColumnEntry[] | undefined) ?? [];
  const groupable = state.groupable !== false;
  const visibleCount = entries.filter((entry) => entry.visible).length;

  // A table over a large type offers a column per attribute, which is a long list to scroll. The
  // search narrows what is rendered; the full list stays the reference for the drop position, so a
  // row can be dropped next to a row the search has hidden. Each rendered row therefore carries its
  // position within the full list, which is the position the reorder gesture works with.
  const [search, setSearch] = React.useState('');
  const matches = new Set(filterOptions(entries, search));
  const shown = entries.map((entry, index) => ({ entry, index })).filter(({ entry }) => matches.has(entry));

  const handleToggle = React.useCallback((name: string, visible: boolean) => {
    sendCommand(CMD_COLUMN_VISIBLE, { [ARG_COLUMN]: name, [ARG_VISIBLE]: visible });
  }, [sendCommand]);

  // Choosing a column moves the grouping there, choosing the grouped one removes it - resolved on
  // the server, which owns the edited working copy.
  const handleGroupBy = React.useCallback((name: string) => {
    sendCommand(CMD_GROUP_BY, { [ARG_COLUMN]: name });
  }, [sendCommand]);

  // The moved column is named rather than counted: the server resolves it against its own working
  // copy, which the client's list is only a picture of.
  const handleMove = React.useCallback((from: number, targetIndex: number) => {
    const moved = entries[from];
    if (moved) {
      sendCommand(CMD_COLUMN_REORDER, { [ARG_COLUMN]: moved.name, [ARG_TARGET_INDEX]: targetIndex });
    }
  }, [entries, sendCommand]);

  // The whole row is dragged by the pointer; its handle is the button the keyboard moves it with
  // (Alt + arrow, announced in the live region; the focus stays on the moved entry's handle).
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
        {shown.map(({ entry, index }) => {
          // Keep the table from losing its last column: there would be nothing left to click.
          const lastVisible = entry.visible && visibleCount <= 1;
          const groupByLabel = entry.grouped ? i18n['js.table.ungroup'] : i18n['js.table.groupBy'];
          const handle = reorder.handleProps(index);
          return (
            <li
              key={entry.name}
              className="tl-reorder-list__item"
              data-tl-state={reorderState(reorder.itemState(index))}
              {...reorder.itemProps(index)}
            >
              <button
                type="button"
                className={buttonClassName({ appearance: 'ghost', small: true, icon: true, extra: 'tl-reorder-list__handle' })}
                {...tooltipProps(handle['aria-label'])}
                {...handle}
              >
                <ThemeIcon encoded={ICON_HANDLE} className="tl-button__icon tl-icon-sm" />
              </button>
              {groupable && (
                <button
                  type="button"
                  className={buttonClassName({ appearance: 'ghost', small: true, icon: true })}
                  aria-label={groupByLabel}
                  {...tooltipProps(groupByLabel)}
                  aria-pressed={entry.grouped}
                  onClick={() => handleGroupBy(entry.name)}
                >
                  <ThemeIcon encoded={ICON_GROUP} className="tl-button__icon tl-icon-sm" />
                </button>
              )}
              <label className="tl-choice">
                <input
                  type="checkbox"
                  className="tl-checkbox"
                  checked={entry.visible}
                  disabled={lastVisible}
                  onChange={(e) => handleToggle(entry.name, e.target.checked)}
                />
                <span className="tl-reorder-list__label">{entry.label}</span>
              </label>
            </li>
          );
        })}
      </ul>
      {reorder.announcer}
    </div>
  );
};

export default TLColumnSelect;
