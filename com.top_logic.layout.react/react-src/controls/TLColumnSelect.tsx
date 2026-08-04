import { React, useTLState, useTLCommand } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

interface ColumnEntry {
  name: string;
  label: string;
  visible: boolean;
}

/**
 * The column selection of a table: the columns as a list of checkboxes, reordered by dragging a
 * row onto another one.
 *
 * Both the order and the checked state live on the server (see ReactColumnSelectControl), so a
 * gesture sends a command and the re-pushed list is what renders.
 */
const TLColumnSelect: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const entries = (state.entries as ColumnEntry[] | undefined) ?? [];
  const visibleCount = entries.filter((entry) => entry.visible).length;

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

  return (
    <div id={controlId} className="tlColumnSelect" onDrop={handleDrop}>
      {entries.map((entry) => {
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
  );
};

export default TLColumnSelect;
