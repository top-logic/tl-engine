import { React, useTLState, useTLCommand, TLChild, rootClassName, stateAttr, useTokenPx, afterDragImage, useI18N, usePart, isEditorTarget, writeDragPayload, runningDrag, onDragEnd, readDragPayload, dragKindAccepted, dragModes, treeZoneSplit, dropZoneAt } from 'tl-react-bridge';
import type { TLCellProps, TLDropZone, TLDropMarker } from 'tl-react-bridge';
import { NO_DRAG_IMAGE } from './drop-hint';
import type { DropVerdict } from './drop-hint';
import { DropHint } from './DropHint';
import type { HintAnchor } from './DropHint';

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

/** State word (drag.css) of each marker drawn at a node. */
const NODE_MARKER_STATE: Record<Exclude<TLDropMarker, 'control'>, string> = {
  before: 'drop-before',
  after: 'drop-after',
  into: 'drop-onto',
};

/** How long a drag hovers the middle of a collapsed node before the node is expanded, in ms. */
const AUTO_EXPAND_DELAY = 700;

const I18N_KEYS = {
  'js.treeView.expand': 'Expand',
  'js.treeView.collapse': 'Collapse',
  'js.tree.label': 'Tree',
  'js.tree.loading': 'Loading entries\u2026',
};

/** The indent of a level until the token is read: the toggle's width, size-control-sm. */
const INDENT_FALLBACK_PX = 24;

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
  const DisclosureToggle = usePart('DisclosureToggle');
  const ProgressBar = usePart('ProgressBar');

  const nodes = (state.nodes as NodeState[]) ?? [];
  const selectionMode = (state.selectionMode as string) ?? SINGLE_SELECTION;
  const dragEnabled = (state.dragEnabled as boolean) ?? false;
  const dragKind = (state.dragKind as string | null) ?? undefined;
  const dropAcceptsAny = (state.dropAcceptsAny as boolean) ?? false;
  const dropAccepts = (state.dropAccepts as string[]) ?? [];
  const dropModes = (state.dropModes as string[]) ?? [];
  const dropModesByKind = (state.dropModesByKind as Record<string, string[]>) ?? {};
  const dropVerdicts = (state.dropVerdicts as Record<string, DropVerdict>) ?? {};

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

  // The nodes a drag started here carries, faded by drag.css (data-tl-state~="dragging") while it
  // runs: the grabbed node, or the whole selection when the grabbed node is part of it.
  const [draggedNode, setDraggedNode] = React.useState<{ id: string; selection: boolean } | null>(null);
  React.useEffect(() => (draggedNode ? onDragEnd(() => setDraggedNode(null)) : undefined), [draggedNode]);

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

  // Where the hint on a refused target stands, and the hook moving it: the hint follows the pointer
  // on every dragover without rendering the tree.
  const hintAnchorRef = React.useRef<HintAnchor>({ x: 0, y: 0, height: 0 });
  const hintMoveRef = React.useRef<((at: HintAnchor) => void) | null>(null);

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
    const dragged = { id: node.id, selection: node.selected };
    // A drag cancelled before the image was taken has already ended: no node stays marked dragging.
    afterDragImage(() => { if (runningDrag()) setDraggedNode(dragged); });
  }, [controlId, dragKind]);

  /**
   * Which node an event points at and the zone within it, split as the drop modes announced for the
   * dragged kind need, or no node where the pointer is beside the nodes or the nodes have no zones.
   */
  const dropTargetAt = React.useCallback(
    (e: React.DragEvent): { node: string | null; zone: TLDropZone } => {
      const zoneSplit = treeZoneSplit(dragModes(e.dataTransfer, dropModes, dropModesByKind));
      if (zoneSplit !== 'none' && e.target instanceof Element) {
        const nodeElement = e.target.closest('.tl-tree__node') as HTMLElement | null;
        const key = nodeElement?.dataset.dropNode;
        if (nodeElement && key) {
          return { node: key, zone: dropZoneAt(e.clientY, nodeElement, zoneSplit) };
        }
      }
      return { node: null, zone: 'none' };
    }, [dropModes, dropModesByKind]);

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
    const drag = runningDrag();
    // The browser draws the drag image around the pointer; the hint stands beside it.
    const image = drag?.image ?? NO_DRAG_IMAGE;
    hintAnchorRef.current = { x: e.clientX, y: e.clientY + image.top, height: image.bottom - image.top };
    hintMoveRef.current?.(hintAnchorRef.current);
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

  // The marker the server's verdict names, at the node it names; a refusal at the hovered node.
  const dropWord = (node: NodeState) => {
    if (!dropState) return undefined;
    if (dropRefused) return dropState.node === node.id ? 'drop-refused' : undefined;
    return dropMarker && dropMarker !== 'control' && dropMarkerKey === node.id ? NODE_MARKER_STATE[dropMarker] : undefined;
  };

  return (
    <ul
      ref={listRef}
      role="tree"
      aria-label={i18n['js.tree.label']}
      aria-multiselectable={isMulti || undefined}
      aria-activedescendant={cursorNode ? domId(cursorNode.id) : undefined}
      className={rootClassName(state, 'tl-tree', 'tl-type-body')}
      data-tl-state={dropState ? (dropRefused
        ? (dropState.node === null ? 'drop-refused' : undefined)
        : (dropMarker === 'control' ? 'drop-onto' : undefined)) : undefined}
      tabIndex={0}
      onKeyDown={handleKeyDown}
      onDragOver={handleRootDragOver}
      onDragLeave={handleRootDragLeave}
      onDrop={handleRootDrop}
    >
      {/* Why the target under the running drag refuses it, beside the pointer (DropHint). */}
      {dropRefused && dropVerdict?.reason && (
        <DropHint reason={dropVerdict.reason} start={hintAnchorRef.current} moveRef={hintMoveRef} />
      )}
      {nodes.map((node, index) => (
        <React.Fragment key={node.id}>
          <li
            id={domId(node.id)}
            role="treeitem"
            aria-expanded={node.expandable ? node.expanded : undefined}
            aria-selected={!!node.selected}
            aria-level={node.depth + 1}
            aria-busy={node.loading || undefined}
            className="tl-tree__node"
            data-tl-state={stateAttr(
              index === cursorIndex && 'cursor',
              draggedNode && (node.id === draggedNode.id || (draggedNode.selection && node.selected)) && 'dragging',
              dropWord(node),
            )}
            style={{ paddingInlineStart: node.depth * indent }}
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
              // The toggle expands its own node, so it points at no other element. Loading children
              // is shown by the status line below the node, not by the toggle.
              <DisclosureToggle
                expanded={node.expanded}
                label={toggleLabel(i18n, node.expanded)}
                tabIndex={-1}
                onClick={(e) => {
                  e.stopPropagation();
                  handleToggle(node.id, node.expanded);
                }}
                // A press on the toggle keeps the focus on the tree, so the keys and the ring stay with it.
                onMouseDown={(e) => e.preventDefault()}
              />
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
              // In a multi-selectable tree every item states whether it is selected; this one never is.
              aria-selected={isMulti ? false : undefined}
              aria-label={i18n['js.tree.loading']}
              className="tl-status"
              style={{ paddingInlineStart: (node.depth + 1) * indent }}
            >
              <ProgressBar aria-hidden />
            </li>
          )}
        </React.Fragment>
      ))}
    </ul>
  );
};

export default TLTreeView;
