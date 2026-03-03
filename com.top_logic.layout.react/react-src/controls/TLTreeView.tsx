import { React, useTLState, useTLCommand, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

interface NodeState {
  id: string;
  depth: number;
  expandable: boolean;
  expanded: boolean;
  leaf: boolean;
  loading: boolean;
  selected: boolean;
  content: unknown;
}

const INDENT_PX = 20;

/**
 * React tree component with lazy-loaded children, selection, and keyboard navigation.
 */
const TLTreeView: React.FC<TLCellProps> = () => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const nodes = (state.nodes as NodeState[]) ?? [];
  const selectionMode = (state.selectionMode as string) ?? 'single';
  const dragEnabled = (state.dragEnabled as boolean) ?? false;
  const dropEnabled = (state.dropEnabled as boolean) ?? false;
  const dropIndicatorNodeId = (state.dropIndicatorNodeId as string) ?? null;
  const dropIndicatorPosition = (state.dropIndicatorPosition as string) ?? null;

  // Focus tracking (client-side only).
  const [focusIndex, setFocusIndex] = React.useState(-1);
  const listRef = React.useRef<HTMLUListElement>(null);

  const handleToggle = React.useCallback((nodeId: string, expanded: boolean) => {
    sendCommand(expanded ? 'collapse' : 'expand', { nodeId });
  }, [sendCommand]);

  const handleSelect = React.useCallback((nodeId: string, e: React.MouseEvent) => {
    sendCommand('select', {
      nodeId,
      ctrlKey: e.ctrlKey || e.metaKey,
      shiftKey: e.shiftKey,
    });
  }, [sendCommand]);

  const handleContextMenu = React.useCallback((nodeId: string, e: React.MouseEvent) => {
    e.preventDefault();
    sendCommand('contextMenu', { nodeId, x: e.clientX, y: e.clientY });
  }, [sendCommand]);

  // -- Drag-and-drop handlers --

  const dragOverTimerRef = React.useRef<number | null>(null);

  const computeDropPosition = React.useCallback((e: React.DragEvent, element: HTMLElement): string => {
    const rect = element.getBoundingClientRect();
    const y = e.clientY - rect.top;
    const third = rect.height / 3;
    if (y < third) return 'above';
    if (y > third * 2) return 'below';
    return 'within';
  }, []);

  const handleDragStart = React.useCallback((nodeId: string, e: React.DragEvent) => {
    e.dataTransfer.effectAllowed = 'move';
    e.dataTransfer.setData('text/plain', nodeId);
  }, []);

  const handleDragOver = React.useCallback((nodeId: string, e: React.DragEvent) => {
    e.preventDefault();
    e.dataTransfer.dropEffect = 'move';
    const position = computeDropPosition(e, e.currentTarget as HTMLElement);
    // Debounce: only send command if position or node changed.
    if (dragOverTimerRef.current != null) {
      window.clearTimeout(dragOverTimerRef.current);
    }
    dragOverTimerRef.current = window.setTimeout(() => {
      sendCommand('dragOver', { nodeId, position });
      dragOverTimerRef.current = null;
    }, 50);
  }, [sendCommand, computeDropPosition]);

  const handleDrop = React.useCallback((nodeId: string, e: React.DragEvent) => {
    e.preventDefault();
    if (dragOverTimerRef.current != null) {
      window.clearTimeout(dragOverTimerRef.current);
      dragOverTimerRef.current = null;
    }
    const position = computeDropPosition(e, e.currentTarget as HTMLElement);
    sendCommand('drop', { nodeId, position });
  }, [sendCommand, computeDropPosition]);

  const handleDragEnd = React.useCallback(() => {
    if (dragOverTimerRef.current != null) {
      window.clearTimeout(dragOverTimerRef.current);
      dragOverTimerRef.current = null;
    }
    sendCommand('dragEnd');
  }, [sendCommand]);

  const handleKeyDown = React.useCallback((e: React.KeyboardEvent) => {
    if (nodes.length === 0) return;

    let newIndex = focusIndex;

    switch (e.key) {
      case 'ArrowDown':
        e.preventDefault();
        newIndex = Math.min(focusIndex + 1, nodes.length - 1);
        break;
      case 'ArrowUp':
        e.preventDefault();
        newIndex = Math.max(focusIndex - 1, 0);
        break;
      case 'ArrowRight':
        e.preventDefault();
        if (focusIndex >= 0 && focusIndex < nodes.length) {
          const node = nodes[focusIndex];
          if (node.expandable && !node.expanded) {
            sendCommand('expand', { nodeId: node.id });
            return;
          } else if (node.expanded) {
            // Move to first child.
            newIndex = focusIndex + 1;
          }
        }
        break;
      case 'ArrowLeft':
        e.preventDefault();
        if (focusIndex >= 0 && focusIndex < nodes.length) {
          const node = nodes[focusIndex];
          if (node.expanded) {
            sendCommand('collapse', { nodeId: node.id });
            return;
          } else {
            // Move to parent: find previous node with lower depth.
            const currentDepth = node.depth;
            for (let i = focusIndex - 1; i >= 0; i--) {
              if (nodes[i].depth < currentDepth) {
                newIndex = i;
                break;
              }
            }
          }
        }
        break;
      case 'Enter':
        e.preventDefault();
        if (focusIndex >= 0 && focusIndex < nodes.length) {
          sendCommand('select', {
            nodeId: nodes[focusIndex].id,
            ctrlKey: e.ctrlKey || e.metaKey,
            shiftKey: e.shiftKey,
          });
        }
        return;
      case ' ':
        e.preventDefault();
        if (selectionMode === 'multi' && focusIndex >= 0 && focusIndex < nodes.length) {
          sendCommand('select', {
            nodeId: nodes[focusIndex].id,
            ctrlKey: true,
            shiftKey: false,
          });
        }
        return;
      case 'Home':
        e.preventDefault();
        newIndex = 0;
        break;
      case 'End':
        e.preventDefault();
        newIndex = nodes.length - 1;
        break;
      default:
        return;
    }

    if (newIndex !== focusIndex) {
      setFocusIndex(newIndex);
    }
  }, [focusIndex, nodes, sendCommand, selectionMode]);

  return (
    <ul
      ref={listRef}
      role="tree"
      className="tlTreeView"
      tabIndex={0}
      onKeyDown={handleKeyDown}
    >
      {nodes.map((node, index) => (
        <li
          key={node.id}
          role="treeitem"
          aria-expanded={node.expandable ? node.expanded : undefined}
          aria-selected={node.selected}
          aria-level={node.depth + 1}
          className={[
            'tlTreeView__node',
            node.selected ? 'tlTreeView__node--selected' : '',
            index === focusIndex ? 'tlTreeView__node--focused' : '',
            dropIndicatorNodeId === node.id && dropIndicatorPosition === 'above' ? 'tlTreeView__node--drop-above' : '',
            dropIndicatorNodeId === node.id && dropIndicatorPosition === 'within' ? 'tlTreeView__node--drop-within' : '',
            dropIndicatorNodeId === node.id && dropIndicatorPosition === 'below' ? 'tlTreeView__node--drop-below' : '',
          ].filter(Boolean).join(' ')}
          style={{ paddingLeft: node.depth * INDENT_PX }}
          draggable={dragEnabled}
          onClick={(e) => handleSelect(node.id, e)}
          onContextMenu={(e) => handleContextMenu(node.id, e)}
          onDragStart={(e) => handleDragStart(node.id, e)}
          onDragOver={dropEnabled ? (e) => handleDragOver(node.id, e) : undefined}
          onDrop={dropEnabled ? (e) => handleDrop(node.id, e) : undefined}
          onDragEnd={handleDragEnd}
        >
          {node.expandable ? (
            <button
              type="button"
              className="tlTreeView__toggle"
              onClick={(e) => {
                e.stopPropagation();
                handleToggle(node.id, node.expanded);
              }}
              tabIndex={-1}
              aria-label={node.expanded ? 'Collapse' : 'Expand'}
            >
              {node.loading ? (
                <span className="tlTreeView__spinner" />
              ) : (
                <span className={
                  node.expanded ? 'tlTreeView__chevron--down' : 'tlTreeView__chevron--right'
                } />
              )}
            </button>
          ) : (
            <span className="tlTreeView__toggleSpacer" />
          )}
          <span className="tlTreeView__content">
            <TLChild control={node.content} />
          </span>
        </li>
      ))}
    </ul>
  );
};

export default TLTreeView;
