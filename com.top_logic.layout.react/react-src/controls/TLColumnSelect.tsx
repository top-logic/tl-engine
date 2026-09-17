import { React, useTLState, useTLCommand, useI18N } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const I18N_KEYS = {
  'js.table.columnSearch': 'Find column',
  'js.table.groupBy': 'Group by this column',
  'js.table.ungroup': 'Remove grouping',
};

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
  // row can be dropped next to a row the search has hidden.
  const [search, setSearch] = React.useState('');
  const needle = search.trim().toLowerCase();
  const shown = needle
    ? entries.filter((entry) => entry.label.toLowerCase().includes(needle))
    : entries;

  // The row being dragged, and the row it currently hovers over — the drop lands above or below
  // that row depending on which half the pointer is in, mirroring the column header drag.
  // The hovered row is held in a ref as well as in state: the state drives the drop indicator,
  // while the drop handler reads the ref, so it sees the last hover even if no render happened
  // between the two events.
  const dragNameRef = React.useRef<string | null>(null);
  const dragOverRef = React.useRef<{ name: string; side: 'top' | 'bottom' } | null>(null);
  const [dragOver, setDragOver] = React.useState<{ name: string; side: 'top' | 'bottom' } | null>(null);

  const setDragTarget = React.useCallback((target: { name: string; side: 'top' | 'bottom' } | null) => {
    dragOverRef.current = target;
    setDragOver(target);
  }, []);

  const handleToggle = React.useCallback((name: string, visible: boolean) => {
    sendCommand('columnVisible', { column: name, visible });
  }, [sendCommand]);

  // Choosing a column moves the grouping there, choosing the grouped one removes it - resolved on
  // the server, which owns the edited working copy.
  const handleGroupBy = React.useCallback((name: string) => {
    sendCommand('groupBy', { column: name });
  }, [sendCommand]);

  const handleDragStart = React.useCallback((name: string, event: React.DragEvent) => {
    dragNameRef.current = name;
    event.dataTransfer.effectAllowed = 'move';
    // Firefox starts no drag at all without payload.
    event.dataTransfer.setData('text/plain', name);
  }, []);

  const handleDragOver = React.useCallback((name: string, event: React.DragEvent) => {
    if (!dragNameRef.current || dragNameRef.current === name) {
      setDragTarget(null);
      return;
    }
    event.preventDefault();
    event.dataTransfer.dropEffect = 'move';
    const rect = event.currentTarget.getBoundingClientRect();
    const side = event.clientY < rect.top + rect.height / 2 ? 'top' : 'bottom';
    setDragTarget({ name, side });
  }, [setDragTarget]);

  const handleDragEnd = React.useCallback(() => {
    dragNameRef.current = null;
    setDragTarget(null);
  }, [setDragTarget]);

  const handleDrop = React.useCallback((event: React.DragEvent) => {
    event.preventDefault();
    const dragged = dragNameRef.current;
    const target = dragOverRef.current;
    dragNameRef.current = null;
    setDragTarget(null);
    if (!dragged || !target) {
      return;
    }
    const targetIdx = entries.findIndex((entry) => entry.name === target.name);
    const draggedIdx = entries.findIndex((entry) => entry.name === dragged);
    if (targetIdx < 0 || draggedIdx < 0) {
      return;
    }
    let insertAt = target.side === 'top' ? targetIdx : targetIdx + 1;
    // The dragged row is removed before it is re-inserted, so a target below it shifts up by one.
    if (draggedIdx < insertAt) {
      insertAt--;
    }
    if (insertAt !== draggedIdx) {
      sendCommand('columnReorder', { column: dragged, targetIndex: insertAt });
    }
  }, [entries, sendCommand, setDragTarget]);

  const searchable = entries.length > 10;

  return (
    <div id={controlId} className="tlColumnSelect" onDrop={handleDrop}>
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
      {shown.map((entry) => {
        // Keep the table from losing its last column: there would be nothing left to click.
        const lastVisible = entry.visible && visibleCount <= 1;
        let cls = 'tlColumnSelect__row';
        if (dragOver && dragOver.name === entry.name) {
          cls += ' tlColumnSelect__row--dragOver-' + dragOver.side;
        }
        return (
          <div
            key={entry.name}
            className={cls}
            draggable={true}
            onDragStart={(e) => handleDragStart(entry.name, e)}
            onDragOver={(e) => handleDragOver(entry.name, e)}
            onDrop={handleDrop}
            onDragEnd={handleDragEnd}
          >
            <i className="tlColumnSelect__handle bi bi-grip-vertical" aria-hidden="true" />
            <button
              type="button"
              className={'tlColumnSelect__groupBy'
                + (entry.grouped ? ' tlColumnSelect__groupBy--active' : '')}
              title={entry.grouped ? i18n['js.table.ungroup'] : i18n['js.table.groupBy']}
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
