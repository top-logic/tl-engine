import { React, useTLState, useTLCommand, useI18N, useListReorder, tooltipProps } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

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
 * The column selection of a table: the columns as a list of checkboxes, reordered by dragging a
 * row onto another one.
 *
 * Both the order and the checked state live on the server (see ReactColumnSelectControl), so a
 * gesture sends a command and the re-pushed list is what renders. Each row also offers to group
 * the table's rows by that column; the choice applies with the columns.
 */
const TLColumnSelect: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);

  const entries = (state.entries as ColumnEntry[] | undefined) ?? [];
  const visibleCount = entries.filter((entry) => entry.visible).length;

  // A table over a large type offers a column per attribute, which is a long list to scroll. The
  // search narrows what is rendered; the full list stays the reference for the drop position, so a
  // row can be dropped next to a row the search has hidden. Each rendered row therefore carries its
  // position within the full list, which is the position the reorder gesture works with.
  const [search, setSearch] = React.useState('');
  const needle = search.trim().toLowerCase();
  const positioned = entries.map((entry, index) => ({ entry, index }));
  const shown = needle
    ? positioned.filter(({ entry }) => entry.label.toLowerCase().includes(needle))
    : positioned;

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

  const reorder = useListReorder({ axis: 'vertical', onMove: handleMove });

  const searchable = entries.length > 10;

  return (
    <div id={controlId} className="tlColumnSelect" {...reorder.containerProps}>
      {searchable && (
        <div className="tlColumnSelect__search">
          <i className="bi bi-search" aria-hidden="true" />
          <input
            type="search"
            className="tlColumnSelect__searchInput"
            placeholder={i18n['js.table.columnSearch']}
            aria-label={i18n['js.table.columnSearch']}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
      )}
      {/* Alongside the search the list scrolls within a fixed height: a list that grows and shrinks
          with the number of matches would resize the dialog under the pointer on every keystroke. */}
      <div className={'tlColumnSelect__list' + (searchable ? ' tlColumnSelect__list--fixed' : '')}>
      {shown.map(({ entry, index }) => {
        // Keep the table from losing its last column: there would be nothing left to click.
        const lastVisible = entry.visible && visibleCount <= 1;
        const dragState = reorder.itemState(index);
        const groupByLabel = entry.grouped ? i18n['js.table.ungroup'] : i18n['js.table.groupBy'];
        let cls = 'tlColumnSelect__row';
        if (dragState.dropBefore) {
          cls += ' tlColumnSelect__row--dragOver-before';
        }
        if (dragState.dropAfter) {
          cls += ' tlColumnSelect__row--dragOver-after';
        }
        return (
          <div
            key={entry.name}
            className={cls}
            {...reorder.itemProps(index)}
          >
            <i className="tlColumnSelect__handle bi bi-grip-vertical" aria-hidden="true" />
            <button
              type="button"
              className={'tlColumnSelect__groupBy'
                + (entry.grouped ? ' tlColumnSelect__groupBy--active' : '')}
              aria-label={groupByLabel}
              {...tooltipProps(groupByLabel)}
              aria-pressed={entry.grouped}
              onClick={() => handleGroupBy(entry.name)}
            >
              <i className={entry.grouped ? 'bi bi-collection-fill' : 'bi bi-collection'} aria-hidden="true" />
            </button>
            <label className="tlColumnSelect__label">
              <input
                type="checkbox"
                className="tlReactCheckbox"
                checked={entry.visible}
                disabled={lastVisible}
                onChange={(e) => handleToggle(entry.name, e.target.checked)}
              />
              <span>{entry.label}</span>
            </label>
          </div>
        );
      })}
      </div>
    </div>
  );
};

export default TLColumnSelect;
