import { React, useTLState, useTLCommand, TLChild, rootClassName, useI18N, tooltipProps, writeDragPayload, runningDrag, onDragEnd, readDragPayload, dragKindAccepted, treeZoneSplit, dropZoneAt, createPortal } from 'tl-react-bridge';
import type { TLCellProps, TLDropZone, TLDropMarker } from 'tl-react-bridge';
import { placeDropHint, NO_DRAG_IMAGE } from './drop-hint';
import type { DropVerdict } from './drop-hint';

interface NodeState {
  id: string;
  depth: number;
  expandable: boolean;
  expanded: boolean;
  leaf: boolean;
  loading: boolean;
  selected: boolean;
  content: unknown;
  /** Whether the node may be dragged; present while the tree's nodes are draggable at all. */
  draggable?: boolean;
}

/** Where a running drag hovers the tree: a node and the zone within it, or beside the nodes. */
interface DropState {
  /** Id of the hovered node, `null` beside the nodes. */
  node: string | null;
  zone: TLDropZone;
  /** Identifier of the probe asking about this target, `null` for a drag not started here. */
  probe: string | null;
}

/** Node class of each marker drawn at a node. */
const NODE_MARKER_CLASS: Record<Exclude<TLDropMarker, 'control'>, string> = {
  before: 'tlTreeView__node--dragOver-before',
  after: 'tlTreeView__node--dragOver-after',
  into: 'tlTreeView__node--dragOver-into',
};

/** How long a drag hovers the middle of a collapsed node before the node is expanded, in ms. */
const AUTO_EXPAND_DELAY = 700;

const I18N_KEYS = {
  'js.treeView.expand': 'Expand',
  'js.treeView.collapse': 'Collapse',
};

const INDENT_PX = 20;

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

/** Command applying a drop (DropSupport.CMD_DROP). */
const CMD_DROP = 'drop';

/** Command asking the server whether a drop at the hovered target would be accepted. */
const CMD_DROP_PROBE = 'dropProbe';

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
  const dragKind = (state.dragKind as string | null) ?? undefined;
  const dropAcceptsAny = (state.dropAcceptsAny as boolean) ?? false;
  const dropAccepts = (state.dropAccepts as string[]) ?? [];
  const dropModes = (state.dropModes as string[]) ?? [];
  const zoneSplit = treeZoneSplit(dropModes);
  const dropVerdicts = (state.dropVerdicts as Record<string, DropVerdict>) ?? {};

  const isMulti = selectionMode === MULTI_SELECTION;

  // The node carrying the keyboard cursor, tracked on the client: set by a click and by the
  // navigation keys, read by the keys that act on it (Enter, Space, the expand/collapse arrows).
  // Held as a node id rather than as a position, so it keeps naming its node when the flat list of
  // visible nodes changes underneath it (a node is expanded, the model pushes other nodes).
  const [cursorNodeId, setCursorNodeId] = React.useState<string | null>(null);
  const listRef = React.useRef<HTMLUListElement>(null);

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
    const selectedEl = listRef.current?.querySelector('.tlTreeView__node--selected');
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
    listRef.current?.querySelector('.tlTreeView__node--focused')?.scrollIntoView({ block: 'nearest' });
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

  // -- Drag-and-drop --

  // Where an accepted drag currently hovers the tree, or null while none does.
  const [dropState, setDropState] = React.useState<DropState | null>(null);

  // Drop probes sent for the running drag, by probe identifier: each target is asked once per drag.
  // Reset when a probe of another drag is sent.
  const probesRef = React.useRef<{ drag: string; sent: Set<string> } | null>(null);

  // The verdict on the hovered target: undefined while no verdict has arrived, which counts as
  // accepted until the server says otherwise.
  const dropVerdict: DropVerdict | undefined = dropState?.probe ? dropVerdicts[dropState.probe] : undefined;
  const dropRefused = dropVerdict !== undefined && !dropVerdict.accepted;

  // The marker of the drop accepted at the hovered place. Until the verdict on a newly hovered place
  // arrives, the marker of the place hovered before stays, so the marker does not flicker.
  const lastAcceptedRef = React.useRef<DropVerdict | null>(null);
  if (dropState === null || dropRefused) {
    lastAcceptedRef.current = null;
  } else if (dropVerdict !== undefined) {
    lastAcceptedRef.current = dropVerdict;
  }
  const dropMarker = lastAcceptedRef.current?.marker;
  const dropMarkerKey = lastAcceptedRef.current?.markerKey;

  // A collapsed node the running drag rests on in its middle, expanded once the drag stays there.
  const autoExpandRef = React.useRef<{ node: string; timer: number } | null>(null);
  const cancelAutoExpand = React.useCallback(() => {
    if (autoExpandRef.current) {
      window.clearTimeout(autoExpandRef.current.timer);
      autoExpandRef.current = null;
    }
  }, []);

  // A drag hovering this tree may end without any event reaching it: a refused drop is not
  // dispatched here, and the source's dragend reaches the source's control only.
  const dragHovers = dropState !== null;
  React.useEffect(() => {
    if (!dragHovers) {
      cancelAutoExpand();
      return undefined;
    }
    return onDragEnd(() => setDropState(null));
  }, [dragHovers, cancelAutoExpand]);

  // The pointer of the running drag over the tree, in viewport coordinates, and the hint that
  // follows it. Moved directly in the DOM: dragover fires continuously.
  const dragPointerRef = React.useRef<{ x: number; y: number }>({ x: 0, y: 0 });
  const dropHintRef = React.useRef<HTMLDivElement | null>(null);
  const attachDropHint = React.useCallback((hint: HTMLDivElement | null) => {
    dropHintRef.current = hint;
    if (hint) {
      placeDropHint(hint, dragPointerRef.current.x, dragPointerRef.current.y,
        runningDrag()?.image ?? NO_DRAG_IMAGE);
    }
  }, []);

  /**
   * Starts a node drag. The payload names the node by its id and says whether the node was
   * selected - the server then drags the whole selection, including selected nodes in collapsed
   * subtrees.
   */
  const handleDragStart = React.useCallback((node: NodeState, e: React.DragEvent) => {
    writeDragPayload(e, {
      source: controlId,
      keys: [node.id],
      selection: node.selected,
      kind: dragKind,
    });
  }, [controlId, dragKind]);

  /**
   * Which node an event points at and the zone within it, split as the announced drop modes need,
   * or no node where the pointer is beside the nodes or the nodes have no zones.
   */
  const dropTargetAt = React.useCallback(
    (e: React.DragEvent): { node: string | null; zone: TLDropZone } => {
      if (zoneSplit !== 'none' && e.target instanceof Element) {
        const nodeElement = e.target.closest('.tlTreeView__node') as HTMLElement | null;
        const key = nodeElement?.dataset.dropNode;
        if (nodeElement && key) {
          return { node: key, zone: dropZoneAt(e.clientY, nodeElement, zoneSplit) };
        }
      }
      return { node: null, zone: 'none' };
    }, [zoneSplit]);

  /** Expands a collapsed node once the drag rests on its middle for a while; cancels otherwise. */
  const scheduleAutoExpand = React.useCallback((target: { node: string | null; zone: TLDropZone }) => {
    const node = target.zone === 'middle' && target.node != null
      ? nodes.find((n) => n.id === target.node) : undefined;
    if (!node || !node.expandable || node.expanded) {
      cancelAutoExpand();
      return;
    }
    if (autoExpandRef.current?.node === node.id) {
      return;
    }
    cancelAutoExpand();
    autoExpandRef.current = {
      node: node.id,
      timer: window.setTimeout(() => {
        autoExpandRef.current = null;
        sendCommand(EXPAND_COMMAND, { nodeId: node.id });
      }, AUTO_EXPAND_DELAY),
    };
  }, [nodes, cancelAutoExpand, sendCommand]);

  const handleRootDragOver = React.useCallback((e: React.DragEvent) => {
    // Coarse acceptance from the payload's kind alone: during a drag the payload itself is
    // unreadable. Whether this particular drop is possible is the server's answer.
    if (!dragKindAccepted(e.dataTransfer, dropAcceptsAny, dropAccepts)) {
      return;
    }
    dragPointerRef.current = { x: e.clientX, y: e.clientY };
    const drag = runningDrag();
    const hint = dropHintRef.current;
    if (hint) {
      placeDropHint(hint, e.clientX, e.clientY, drag?.image ?? NO_DRAG_IMAGE);
    }
    const target = dropTargetAt(e);
    scheduleAutoExpand(target);
    let probe: string | null = null;
    if (drag) {
      // A drag started in this document is known: ask the server once per drag and target whether
      // a drop there would be accepted.
      probe = drag.id + '|' + (target.node ?? '') + '|' + target.zone;
      let probes = probesRef.current;
      if (!probes || probes.drag !== drag.id) {
        probes = { drag: drag.id, sent: new Set() };
        probesRef.current = probes;
      }
      if (!probes.sent.has(probe)) {
        probes.sent.add(probe);
        const args: Record<string, unknown> = {
          source: drag.payload.source,
          keys: drag.payload.keys.join(','),
          selection: drag.payload.selection,
          zone: target.zone,
          drag: drag.id,
          probe,
        };
        if (target.node) {
          args.targetKey = target.node;
        }
        void sendCommand(CMD_DROP_PROBE, args);
      }
    }
    setDropState((previous) =>
      previous && previous.node === target.node && previous.zone === target.zone
          && previous.probe === probe
        ? previous
        : { ...target, probe });
    const verdict = probe ? dropVerdicts[probe] : undefined;
    if (verdict && !verdict.accepted) {
      // Refused: leaving the default in place makes the target refuse the drop.
      e.dataTransfer.dropEffect = 'none';
      return;
    }
    e.preventDefault();
    e.dataTransfer.dropEffect = 'move';
  }, [dropAcceptsAny, dropAccepts, dropTargetAt, dropVerdicts, scheduleAutoExpand, sendCommand]);

  const handleRootDragLeave = React.useCallback((e: React.DragEvent) => {
    // Moving among the tree's own descendants fires a leave on each one left behind; only leaving
    // the tree itself ends the feedback.
    if (!e.currentTarget.contains(e.relatedTarget as Node | null)) {
      setDropState(null);
    }
  }, []);

  const handleRootDrop = React.useCallback((e: React.DragEvent) => {
    if (!dragKindAccepted(e.dataTransfer, dropAcceptsAny, dropAccepts)) {
      return;
    }
    e.preventDefault();
    e.stopPropagation();
    cancelAutoExpand();
    const payload = readDragPayload(e.dataTransfer);
    const target = dropTargetAt(e);
    setDropState(null);
    if (payload) {
      const args: Record<string, unknown> = {
        source: payload.source,
        // Comma-separated: the command argument is a formatted string list, and a node id holds no
        // comma.
        keys: payload.keys.join(','),
        selection: payload.selection,
        zone: target.zone,
      };
      if (target.node) {
        // Named only for a drop on a node; a drop beside the nodes names none.
        args.targetKey = target.node;
      }
      sendCommand(CMD_DROP, args);
    }
  }, [dropAcceptsAny, dropAccepts, dropTargetAt, cancelAutoExpand, sendCommand]);

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

  const handleKeyDown = React.useCallback((e: React.KeyboardEvent) => {
    if (nodes.length === 0) {
      return;
    }
    const cursorNode = cursorIndex >= 0 ? nodes[cursorIndex] : null;
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
  }, [cursorIndex, nodes, sendCommand, isMulti, handleActivate, moveCursor]);

  return (
    <ul
      ref={listRef}
      role="tree"
      className={rootClassName(state, 'tlTreeView',
        dropState && (dropRefused
          ? (dropState.node === null && 'tlTreeView--dropRefused')
          : (dropMarker === 'control' && 'tlTreeView--dragover')))}
      tabIndex={0}
      onKeyDown={handleKeyDown}
      onDragOver={handleRootDragOver}
      onDragLeave={handleRootDragLeave}
      onDrop={handleRootDrop}
    >
      {/* Why the target under the running drag refuses it. A native tooltip is not shown while a
          drag runs, so the reason follows the pointer, placed in the document body so that the
          tree's scrolling cannot hide it. */}
      {dropRefused && dropVerdict?.reason && createPortal(
        <div ref={attachDropHint} className="tlTreeView__dropHint" role="status">
          {dropVerdict.reason}
        </div>,
        document.body)}
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
            index === cursorIndex ? 'tlTreeView__node--focused' : '',
            dropState && dropRefused && dropState.node === node.id ? 'tlTreeView__node--dropRefused' : '',
            dropState && !dropRefused && dropMarker && dropMarker !== 'control' && dropMarkerKey === node.id
              ? NODE_MARKER_CLASS[dropMarker] : '',
          ].filter(Boolean).join(' ')}
          style={{ paddingLeft: node.depth * INDENT_PX }}
          data-drop-node={node.id}
          draggable={dragEnabled && node.draggable !== false}
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
          onDragStart={dragEnabled && node.draggable !== false ? (e) => handleDragStart(node, e) : undefined}
          onDragEnd={dragEnabled && node.draggable !== false ? () => setDropState(null) : undefined}
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
              aria-label={toggleLabel(i18n, node.expanded)}
              {...tooltipProps(toggleLabel(i18n, node.expanded))}
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
