import { React, useTLState, useTLCommand, TLChild, rootClassName, stateAttr, useTokenPx, dropPositionAt, afterDragImage, useI18N, tooltipProps, ThemeIcon, isEditorTarget } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { ProgressBar } from './TLProgress';

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

const I18N_KEYS = {
  'js.treeView.expand': 'Expand',
  'js.treeView.collapse': 'Collapse',
  'js.tree.label': 'Tree',
  'js.tree.loading': 'Loading entries\u2026',
};

/** The glyph of the expand toggle (tl-toggle): right is closed, the toggle turns it down when open. */
const CHEVRON_ICON = 'css:fa-solid fa-chevron-right';

/** The indent of a level until the token is read: the toggle's width, size-control-sm. */
const INDENT_FALLBACK_PX = 24;

/** The server's names of the three drop positions (ReactTreeControl), by the shared calculator's. */
const SERVER_POSITION = { before: 'above', onto: 'within', after: 'below' } as const;

/** The data-tl-state word (drag.css) of a drop position the server reports. */
const DROP_STATE: Record<string, string> = { above: 'drop-before', within: 'drop-onto', below: 'drop-after' };

/** What the expansion toggle of a node in the given state does. */
function toggleLabel(i18n: Record<string, string>, expanded: boolean): string {
  return expanded ? i18n['js.treeView.collapse'] : i18n['js.treeView.expand'];
}

// The commands of the server-side tree control.
const EXPAND_COMMAND = 'expand';
const COLLAPSE_COMMAND = 'collapse';
const SELECT_COMMAND = 'select';
const ACTIVATE_COMMAND = 'activate';
const CONTEXT_MENU_COMMAND = 'contextMenu';
const DRAG_OVER_COMMAND = 'dragOver';
const DROP_COMMAND = 'drop';
const DRAG_END_COMMAND = 'dragEnd';

/** The selection mode in which one node at a time is selected. */
const SINGLE_SELECTION = 'single';

/** The selection mode in which several nodes can be selected at once. */
const MULTI_SELECTION = 'multi';

/**
 * React tree component with lazy-loaded children, selection, and keyboard navigation.
 */
const TLTreeView: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);

  const nodes = (state.nodes as NodeState[]) ?? [];
  const selectionMode = (state.selectionMode as string) ?? SINGLE_SELECTION;
  const dragEnabled = (state.dragEnabled as boolean) ?? false;
  const dropEnabled = (state.dropEnabled as boolean) ?? false;
  const dropIndicatorNodeId = (state.dropIndicatorNodeId as string) ?? null;
  const dropIndicatorPosition = (state.dropIndicatorPosition as string) ?? null;

  const isMulti = selectionMode === MULTI_SELECTION;

  // The node carrying the keyboard cursor, tracked on the client: set by a click and by the
  // navigation keys, read by the keys that act on it (Enter, Space, the expand/collapse arrows).
  // Held as a node id rather than as a position, so it keeps naming its node when the flat list of
  // visible nodes changes underneath it (a node is expanded, the model pushes other nodes).
  const [cursorNodeId, setCursorNodeId] = React.useState<string | null>(null);
  const listRef = React.useRef<HTMLUListElement>(null);

  // One level indents by the width of the toggle, so each child stands under its parent's label; read
  // from the token, so the indent follows the density. The list is rendered unconditionally, so the
  // ref is attached when the hook first measures: the token is read at the tree, not at the root.
  const indent = useTokenPx(listRef, '--tl-size-control-sm', INDENT_FALLBACK_PX);
  const domId = (id: string) => `${controlId}-node-${id}`;

  // The node being dragged out of this tree, faded by drag.css while the drag runs.
  const [draggingNodeId, setDraggingNodeId] = React.useState<string | null>(null);

  // Where the keyboard navigation continues. Without a cursor of its own it continues at the
  // selected node, so that the keys take up a selection made elsewhere (a click on another view
  // writing the tree's selection channel).
  const cursorIndex = React.useMemo(() => {
    const index = cursorNodeId == null ? -1 : nodes.findIndex((n) => n.id === cursorNodeId);
    return index >= 0 ? index : nodes.findIndex((n) => n.selected);
  }, [nodes, cursorNodeId]);

  // Scroll the selected node into view when the selection changes (e.g. set externally by the
  // "select view" picker). block:'nearest' scrolls minimally, so it is a no-op when the node is
  // already visible (normal clicks).
  const selectedNodeId = nodes.find(n => n.selected)?.id ?? null;
  React.useEffect(() => {
    if (selectedNodeId == null) {
      return;
    }
    const selectedEl = listRef.current?.querySelector('[role="treeitem"][aria-selected="true"]');
    if (selectedEl) {
      selectedEl.scrollIntoView({ block: 'nearest' });
    }
  }, [selectedNodeId]);

  // Keep the node carrying the keyboard cursor visible as the navigation keys move it.
  // block:'nearest' scrolls minimally, so it is a no-op for a node that is on screen anyway.
  React.useEffect(() => {
    if (cursorNodeId == null) {
      return;
    }
    listRef.current?.querySelector('[data-tl-state~="cursor"]')?.scrollIntoView({ block: 'nearest' });
  }, [cursorNodeId]);

  const handleToggle = React.useCallback((nodeId: string, expanded: boolean) => {
    sendCommand(expanded ? COLLAPSE_COMMAND : EXPAND_COMMAND, { nodeId });
  }, [sendCommand]);

  const handleSelect = React.useCallback((nodeId: string, e: React.MouseEvent) => {
    // A click that concluded a text-selection drag inside the node copies text,
    // it does not change the node selection.
    const selection = window.getSelection();
    if (selection && !selection.isCollapsed && e.currentTarget.contains(selection.anchorNode)) {
      return;
    }
    // Give the tree keyboard focus even when the mousedown default (which would
    // focus it natively) was suppressed for a modifier click.
    listRef.current?.focus({ preventScroll: true });
    // The clicked node carries the keyboard cursor from now on, so the arrow keys step from it and
    // Enter opens it - the keyboard continues where the mouse left off.
    setCursorNodeId(nodeId);
    sendCommand(SELECT_COMMAND, {
      nodeId,
      ctrlKey: e.ctrlKey || e.metaKey,
      shiftKey: e.shiftKey,
    });
  }, [sendCommand]);

  // A double-click opens the node: the server selects it and runs what the view configured for an
  // activation.
  const handleActivate = React.useCallback((nodeId: string) => {
    setCursorNodeId(nodeId);
    sendCommand(ACTIVATE_COMMAND, { nodeId });
  }, [sendCommand]);

  const handleContextMenu = React.useCallback((nodeId: string, e: React.MouseEvent) => {
    e.preventDefault();
    sendCommand(CONTEXT_MENU_COMMAND, { nodeId, x: e.clientX, y: e.clientY });
  }, [sendCommand]);

  // -- Drag-and-drop handlers --

  const dragOverTimerRef = React.useRef<number | null>(null);

  // The shared calculator (thirds: before, onto, after), in the server's names.
  const computeDropPosition = React.useCallback((e: React.DragEvent, element: HTMLElement): string => {
    const position = dropPositionAt(e.clientY, element);
    return position === 'none' ? 'within' : SERVER_POSITION[position];
  }, []);

  const handleDragStart = React.useCallback((nodeId: string, e: React.DragEvent) => {
    e.dataTransfer.effectAllowed = 'move';
    e.dataTransfer.setData('text/plain', nodeId);
    afterDragImage(() => setDraggingNodeId(nodeId));
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
      sendCommand(DRAG_OVER_COMMAND, { nodeId, position });
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
    sendCommand(DROP_COMMAND, { nodeId, position });
  }, [sendCommand, computeDropPosition]);

  const handleDragEnd = React.useCallback(() => {
    setDraggingNodeId(null);
    if (dragOverTimerRef.current != null) {
      window.clearTimeout(dragOverTimerRef.current);
      dragOverTimerRef.current = null;
    }
    sendCommand(DRAG_END_COMMAND);
  }, [sendCommand]);

  // Moves the keyboard cursor onto the node at the given index, with the selection following it:
  // in single selection the node the cursor lands on becomes the selection, in multi selection a
  // plain move leaves the selection untouched and Shift grows the range from its anchor.
  const moveCursor = React.useCallback((index: number, extend: boolean) => {
    const node = nodes[index];
    if (node == null) {
      return;
    }
    setCursorNodeId(node.id);
    if (!isMulti) {
      sendCommand(SELECT_COMMAND, { nodeId: node.id, ctrlKey: false, shiftKey: false });
    } else if (extend) {
      sendCommand(SELECT_COMMAND, { nodeId: node.id, ctrlKey: false, shiftKey: true });
    }
  }, [nodes, isMulti, sendCommand]);

  // The node the cursor names. Every node in the list is rendered with its id, so the tree points
  // aria-activedescendant at a rendered node or leaves the attribute out (no cursor, empty tree) -
  // a reference to a missing id would hide the focus (WCAG 2.4.7); without it the tree shows the
  // ring itself.
  const cursorNode = cursorIndex >= 0 ? nodes[cursorIndex] : undefined;

  const handleKeyDown = React.useCallback((e: React.KeyboardEvent) => {
    if (nodes.length === 0) {
      return;
    }
    // An editor inside a node keeps its keys: Space, Enter and the arrows are its own.
    if (e.target !== e.currentTarget && isEditorTarget(e.target)) {
      return;
    }
    let newIndex = cursorIndex;

    switch (e.key) {
      case 'ArrowDown':
        e.preventDefault();
        newIndex = Math.min(cursorIndex + 1, nodes.length - 1);
        break;
      case 'ArrowUp':
        e.preventDefault();
        newIndex = Math.max(cursorIndex - 1, 0);
        break;
      case 'ArrowRight':
        e.preventDefault();
        if (cursorNode == null) {
          break;
        }
        if (cursorNode.expandable && !cursorNode.expanded) {
          sendCommand(EXPAND_COMMAND, { nodeId: cursorNode.id });
          return;
        }
        if (cursorNode.expanded) {
          // The first child of an expanded node is the node following it.
          newIndex = cursorIndex + 1;
        }
        break;
      case 'ArrowLeft':
        e.preventDefault();
        if (cursorNode == null) {
          break;
        }
        if (cursorNode.expanded) {
          sendCommand(COLLAPSE_COMMAND, { nodeId: cursorNode.id });
          return;
        }
        // Up to the parent: the closest node above the cursor at a smaller depth.
        for (let i = cursorIndex - 1; i >= 0; i--) {
          if (nodes[i].depth < cursorNode.depth) {
            newIndex = i;
            break;
          }
        }
        break;
      case 'Home':
        e.preventDefault();
        newIndex = 0;
        break;
      case 'End':
        e.preventDefault();
        newIndex = nodes.length - 1;
        break;
      case 'Enter':
        // Enter opens the cursor node.
        e.preventDefault();
        if (cursorNode != null) {
          handleActivate(cursorNode.id);
        }
        return;
      case ' ':
        // Space selects the cursor node, adding it to a multiple selection or taking it out again.
        e.preventDefault();
        if (cursorNode != null) {
          sendCommand(SELECT_COMMAND, { nodeId: cursorNode.id, ctrlKey: isMulti, shiftKey: false });
        }
        return;
      default:
        return;
    }

    if (newIndex !== cursorIndex) {
      moveCursor(newIndex, e.shiftKey);
    }
  }, [cursorIndex, cursorNode, nodes, sendCommand, isMulti, handleActivate, moveCursor]);

  return (
    <ul
      ref={listRef}
      role="tree"
      aria-label={i18n['js.tree.label']}
      aria-multiselectable={isMulti || undefined}
      aria-activedescendant={cursorNode ? domId(cursorNode.id) : undefined}
      className={rootClassName(state, 'tl-tree', 'tl-type-body')}
      tabIndex={0}
      onKeyDown={handleKeyDown}
    >
      {nodes.map((node, index) => (
        <React.Fragment key={node.id}>
          <li
            id={domId(node.id)}
            role="treeitem"
            aria-expanded={node.expandable ? node.expanded : undefined}
            aria-selected={node.selected}
            aria-level={node.depth + 1}
            aria-busy={node.loading || undefined}
            className="tl-tree__node"
            data-tl-state={stateAttr(
              index === cursorIndex && 'cursor',
              draggingNodeId === node.id && 'dragging',
              dropIndicatorNodeId === node.id && dropIndicatorPosition ? DROP_STATE[dropIndicatorPosition] : undefined,
            )}
            style={{ paddingInlineStart: node.depth * indent }}
            draggable={dragEnabled}
            onMouseDown={(e) => {
              // Suppress the text selection the browser would start as a side
              // effect of node-selection gestures (shift/ctrl range or toggle,
              // double-click); plain click-and-drag still selects label text.
              if (e.shiftKey || e.ctrlKey || e.metaKey || e.detail > 1) {
                e.preventDefault();
              }
            }}
            onClick={(e) => handleSelect(node.id, e)}
            onDoubleClick={() => handleActivate(node.id)}
            onContextMenu={(e) => handleContextMenu(node.id, e)}
            onDragStart={(e) => handleDragStart(node.id, e)}
            onDragOver={dropEnabled ? (e) => handleDragOver(node.id, e) : undefined}
            onDrop={dropEnabled ? (e) => handleDrop(node.id, e) : undefined}
            onDragEnd={handleDragEnd}
          >
            {node.expandable ? (
              // The toggle expands its own node, so it points at no other element;
              // pointing and pressing are the design system's veil.
              <button
                type="button"
                className="tl-toggle"
                onClick={(e) => {
                  e.stopPropagation();
                  handleToggle(node.id, node.expanded);
                }}
                // A press on the toggle keeps the focus on the tree, so the keys and the ring stay with it.
                onMouseDown={(e) => e.preventDefault()}
                tabIndex={-1}
                aria-expanded={node.expanded}
                aria-label={toggleLabel(i18n, node.expanded)}
                {...tooltipProps(toggleLabel(i18n, node.expanded))}
              >
                <ThemeIcon encoded={CHEVRON_ICON} className="tl-icon-sm" />
              </button>
            ) : (
              <span className="tl-tree__spacer" />
            )}
            <span className="tl-tree__content">
              <TLChild control={node.content} />
            </span>
          </li>
          {/* The children are on their way: a status line one level deeper, with an indeterminate
              progress - the one sign of loading the design system has. A tree owns tree items only,
              so the line is one, named for what it says; the progress inside is hidden from
              assistive technology, so the name is announced once. It is no cursor stop (no id, never
              the active descendant). Its indent is that of its level: (aria-level - 1) toggle widths. */}
          {node.loading && (
            <li
              role="treeitem"
              aria-level={node.depth + 2}
              aria-label={i18n['js.tree.loading']}
              className="tl-status"
              style={{ paddingInlineStart: (node.depth + 1) * indent }}
            >
              <ProgressBar fraction={null} ariaHidden />
            </li>
          )}
        </React.Fragment>
      ))}
    </ul>
  );
};

export default TLTreeView;
