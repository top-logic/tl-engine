import {
    BACKGROUND_COMMAND, React, createPortal, rootClassName, stateAttr, subscribe, unsubscribe, useFill, useTLCommand,
    DROP_MODE_ONTO, DropHint, NO_DRAG_IMAGE, dragKindAccepted, dragModes, onDragEnd, putDropPoint, readDragPayload,
    runningDrag,
} from 'tl-react-bridge';
import type { DropVerdict, HintAnchor, TLDropZone } from 'tl-react-bridge';

/**
 * CSS class of the element the diagram is drawn into (tl-react-flow.css): the SVG is positioned within it and never
 * gives it a size of its own.
 */
const DIAGRAM_CLASS = 'tl-flow-diagram';

/** CSS class of the frame drawn over the node a drag hovers (tl-react-flow.css). */
const DROP_MARKER_CLASS = 'tl-flow-diagram__drop-marker';

/** How long to keep waiting for the GWT module to register itself, and how often to look. */
const MOUNT_TIMEOUT_MS = 10000;

const MOUNT_RETRY_MS = 50;

/** The command applying a drop (`DropSupport.CMD_DROP`). */
const CMD_DROP = 'drop';

/** The command asking whether a drop where the drag hovers would be accepted (`DropSupport.CMD_DROP_PROBE`). */
const CMD_DROP_PROBE = 'dropProbe';

/** Arguments of a drop and a drop probe (`DropArguments`, `DropProbeArguments`). */
const ARG_SOURCE = 'source';
const ARG_KEYS = 'keys';
const ARG_SELECTION = 'selection';
const ARG_TARGET_KEY = 'targetKey';
const ARG_ZONE = 'zone';
const ARG_DRAG = 'drag';
const ARG_PROBE = 'probe';

/** State keys announcing the drop target (`DropSupport`). */
const STATE_DROP_ACCEPTS_ANY = 'dropAcceptsAny';
const STATE_DROP_ACCEPTS = 'dropAccepts';
const STATE_DROP_MODES = 'dropModes';
const STATE_DROP_MODES_BY_KIND = 'dropModesByKind';
const STATE_DROP_VERDICTS = 'dropVerdicts';

/** The zone a drop onto a node is reported in: a node is taken as a whole. */
const ZONE_NODE: TLDropZone = 'middle';

/** The zone a drop on the diagram as a whole is reported in. */
const ZONE_NONE: TLDropZone = 'none';

/**
 * Edge length, in diagram units, of the squares a probe stands for: the server is asked once per drag, node and
 * square, not on every move of the pointer, also where its refusal depends on the point.
 */
const PROBE_GRID = 10;

/**
 * What the GWT control answers for a point of the screen (`FlowDiagramClientControl.nodeAt`): the ID of the node a
 * drop there is made onto, `null` for the diagram as a whole, the point in the coordinate system of that target in
 * diagram units, and for a node its place on the screen.
 */
interface NodeHit {
    node: string | null;
    x: number;
    y: number;
    left?: number;
    top?: number;
    width?: number;
    height?: number;
}

/** A rectangle on the screen, in client coordinates. */
interface ScreenRect {
    left: number;
    top: number;
    width: number;
    height: number;
}

/** Where an accepted drag currently hovers. */
interface DropState {
    /** The node under the pointer, `null` for the diagram as a whole. */
    node: string | null;
    /** The probe the verdict for this place is answered under, `null` for a drag of another document. */
    probe: string | null;
    /** Where the node is drawn on the screen, clipped to the diagram; `null` for the diagram as a whole. */
    rect: ScreenRect | null;
}

interface TLFlowDiagramProps {
    controlId: string;
    state: Record<string, unknown>;
}

/**
 * The intersection of the two rectangles, `null` where they do not overlap.
 */
function clip(rect: ScreenRect, bounds: DOMRect): ScreenRect | null {
    const left = Math.max(rect.left, bounds.left);
    const top = Math.max(rect.top, bounds.top);
    const right = Math.min(rect.left + rect.width, bounds.right);
    const bottom = Math.min(rect.top + rect.height, bounds.bottom);
    return right > left && bottom > top ? { left, top, width: right - left, height: bottom - top } : null;
}

function sameRect(a: ScreenRect | null, b: ScreenRect | null): boolean {
    if (a === null || b === null) {
        return a === b;
    }
    return a.left === b.left && a.top === b.top && a.width === b.width && a.height === b.height;
}

/**
 * Minimal React lifecycle wrapper for the GWT-rendered flow diagram.
 * All diagram logic runs in GWT-compiled Java. This component only
 * manages mount/unmount lifecycle, and drops on the diagram.
 *
 * The GWT control is mounted once per control. It receives the state updates of the control
 * itself, also the serialization replacing the displayed diagram: the patches the server sends
 * after that serialization belong to the new diagram, and only a control applying the updates in
 * the order they were sent gets each patch to the diagram it was made for. The updates arriving
 * while the GWT module is still loading are kept and handed to the control once it is mounted.
 *
 * The control sends its commands through the command channel of the page (useTLCommand), so that a
 * command of the diagram - e.g. the selection a right-click makes before the context menu around
 * the diagram opens - reaches the server before a command the user triggers afterwards.
 *
 * A drag of the view layer's drag and drop is dropped onto the diagram as onto a table or a board: the GWT control
 * tells the node under the pointer and the point in diagram units (`nodeAt`), this component asks the server whether
 * a drop there is accepted (`dropProbe`), shows a refusal with its reason, and sends the drop.
 */
function TLFlowDiagram({ controlId, state }: TLFlowDiagramProps) {
    // The diagram always spans the space its container offers: the client lays it out within that space, so its
    // height must come from the container, never from its drawing.
    const fillClass = useFill(true);
    const ref = React.useRef<HTMLDivElement>(null);
    const controlRef = React.useRef<any>(null);

    const sendCommand = useTLCommand();
    const sendRef = React.useRef(sendCommand);
    sendRef.current = sendCommand;

    const diagramJson = typeof state.diagram === 'string'
        ? state.diagram
        : JSON.stringify(state.diagram);

    const dropAcceptsAny = state[STATE_DROP_ACCEPTS_ANY] === true;
    const dropAccepts = (state[STATE_DROP_ACCEPTS] as string[] | undefined) ?? [];
    const dropModes = (state[STATE_DROP_MODES] as string[] | undefined) ?? [];
    const dropModesByKind = (state[STATE_DROP_MODES_BY_KIND] as Record<string, string[]> | undefined) ?? {};
    const dropVerdicts = (state[STATE_DROP_VERDICTS] as Record<string, DropVerdict> | undefined) ?? {};

    // -- Where an accepted drag currently hovers, or null while none does. --
    const [dropState, setDropState] = React.useState<DropState | null>(null);

    // -- Drop probes sent for the running drag: each place is asked once per drag. --
    const probesRef = React.useRef<{ drag: string; sent: Set<string> } | null>(null);

    const dropVerdict: DropVerdict | undefined = dropState?.probe ? dropVerdicts[dropState.probe] : undefined;
    const dropRefused = dropVerdict !== undefined && !dropVerdict.accepted;

    // A drag hovering the diagram may end without any event reaching it: a refused drop is not dispatched here, and
    // the source's dragend reaches the source's control only.
    const dragHovers = dropState !== null;
    React.useEffect(() => {
        if (!dragHovers) {
            return undefined;
        }
        return onDragEnd(() => setDropState(null));
    }, [dragHovers]);

    // -- Where the hint on a refused drop stands - at the pointer, beside the drag image - and the hint's way to
    //    follow it: dragover fires continuously, and only the hint re-renders for each move. --
    const hintAnchorRef = React.useRef<HintAnchor>({ x: 0, y: 0, height: 0 });
    const hintMoveRef = React.useRef<((at: HintAnchor) => void) | null>(null);

    // Subscribed in a layout effect, so that no update arriving after the render that mounts the
    // control is missed.
    React.useLayoutEffect(() => {
        const div = ref.current;
        if (!div) return;

        // The serialization the control is mounted with: the one of the render mounting it. Later
        // ones reach the control as state updates.
        const mountJson = diagramJson;

        let cancelled = false;
        let retry: number | undefined;
        let waited = 0;

        const pending: Record<string, unknown>[] = [];
        const buffer = (update: Record<string, unknown>) => {
            pending.push(update);
        };
        subscribe(controlId, buffer);

        const mount = () => {
            if (cancelled) return;

            const gwtApi = (window as any).GWT_FlowDiagram;
            if (!gwtApi) {
                // The GWT module registers itself asynchronously, and a freshly loaded page renders
                // this component before that has happened. Waiting for it is the whole point: giving
                // up here left the diagram blank for good, because this effect only re-runs when the
                // control changes.
                if (waited >= MOUNT_TIMEOUT_MS) {
                    unsubscribe(controlId, buffer);
                    console.error('[TLFlowDiagram] GWT_FlowDiagram not loaded');
                    return;
                }
                waited += MOUNT_RETRY_MS;
                retry = window.setTimeout(mount, MOUNT_RETRY_MS);
                return;
            }

            // Sends a command of the control, until it is unmounted.
            const send = (command: string, args: Record<string, unknown>, background: boolean) => {
                if (cancelled) return;
                void sendRef.current(command, args, background ? BACKGROUND_COMMAND : undefined);
            };

            // The control subscribes to the updates of its own while mounting.
            unsubscribe(controlId, buffer);
            const control = gwtApi.mount(div, controlId, send, mountJson);
            controlRef.current = control;
            for (const update of pending) {
                control.onServerState(update);
            }
        };
        mount();

        return () => {
            cancelled = true;
            unsubscribe(controlId, buffer);
            if (retry !== undefined) {
                window.clearTimeout(retry);
            }
            if (controlRef.current) {
                controlRef.current.destroy();
                controlRef.current = null;
            }
        };
    }, [controlId]);

    // A serialization can also reach the component without an update of the control's own, as
    // part of a parent's update rendering the control again. The control ignores the serialization
    // it already displays.
    React.useLayoutEffect(() => {
        if (controlRef.current) {
            controlRef.current.onServerState({ diagram: diagramJson });
        }
    }, [diagramJson]);

    /**
     * The target of a drop of the running drag at the pointer, `null` while the diagram is not mounted. A node is
     * looked for only where a drop of the dragged kind is made onto a node at all.
     */
    const hitAt = React.useCallback((event: React.DragEvent): NodeHit | null => {
        const control = controlRef.current;
        if (!control) {
            return null;
        }
        const modes = dragModes(event.dataTransfer, dropModes, dropModesByKind);
        return control.nodeAt(event.clientX, event.clientY, modes.includes(DROP_MODE_ONTO)) as NodeHit;
    }, [dropModes, dropModesByKind]);

    /** The arguments of a drop of the given drag at the given place. */
    const dropArgs = (source: string, keys: string[], selection: boolean, hit: NodeHit): Record<string, unknown> => {
        const args: Record<string, unknown> = {
            [ARG_SOURCE]: source,
            [ARG_KEYS]: keys,
            [ARG_SELECTION]: selection,
            [ARG_ZONE]: hit.node !== null ? ZONE_NODE : ZONE_NONE,
        };
        if (hit.node !== null) {
            args[ARG_TARGET_KEY] = hit.node;
        }
        return putDropPoint(args, { x: hit.x, y: hit.y });
    };

    const handleDragOver = React.useCallback((event: React.DragEvent<HTMLDivElement>) => {
        // Coarse acceptance from the payload's kind alone; whether this particular drop is possible is the server's
        // answer, given once it arrives.
        if (!dragKindAccepted(event.dataTransfer, dropAcceptsAny, dropAccepts)) {
            return;
        }
        const hit = hitAt(event);
        if (hit === null) {
            return;
        }
        const drag = runningDrag();
        // The browser draws the drag image around the pointer; the hint stands beside it.
        const image = drag?.image ?? NO_DRAG_IMAGE;
        hintAnchorRef.current = { x: event.clientX, y: event.clientY + image.top, height: image.bottom - image.top };
        hintMoveRef.current?.(hintAnchorRef.current);

        let probe: string | null = null;
        if (drag) {
            // The payload is unreadable here, but a drag started in this document is known: ask the server once per
            // drag, node and square of the grid whether a drop there would be accepted.
            probe = drag.id + '|' + (hit.node ?? '') + '|' + Math.floor(hit.x / PROBE_GRID) + '|'
                + Math.floor(hit.y / PROBE_GRID);
            let probes = probesRef.current;
            if (!probes || probes.drag !== drag.id) {
                probes = { drag: drag.id, sent: new Set() };
                probesRef.current = probes;
            }
            if (!probes.sent.has(probe)) {
                probes.sent.add(probe);
                const args = dropArgs(drag.payload.source, drag.payload.keys, drag.payload.selection, hit);
                args[ARG_DRAG] = drag.id;
                args[ARG_PROBE] = probe;
                void sendRef.current(CMD_DROP_PROBE, args, BACKGROUND_COMMAND);
            }
        }

        let rect: ScreenRect | null = null;
        if (hit.node !== null && Number.isFinite(hit.left) && Number.isFinite(hit.top) && Number.isFinite(hit.width)
            && Number.isFinite(hit.height)) {
            rect = clip({ left: hit.left!, top: hit.top!, width: hit.width!, height: hit.height! },
                event.currentTarget.getBoundingClientRect());
        }
        setDropState((previous) =>
            previous && previous.node === hit.node && previous.probe === probe && sameRect(previous.rect, rect)
                ? previous
                : { node: hit.node, probe, rect });

        const verdict = probe ? dropVerdicts[probe] : undefined;
        if (verdict && !verdict.accepted) {
            // Refused: leaving the default in place makes the target refuse the drop.
            event.dataTransfer.dropEffect = 'none';
            return;
        }
        event.preventDefault();
        event.dataTransfer.dropEffect = 'move';
    }, [dropAcceptsAny, dropAccepts, dropVerdicts, hitAt]);

    const handleDragLeave = React.useCallback((event: React.DragEvent<HTMLDivElement>) => {
        // Moving among the diagram's own descendants fires a leave on each one left behind; only leaving the diagram
        // itself ends the highlight.
        if (!event.currentTarget.contains(event.relatedTarget as Node | null)) {
            setDropState(null);
        }
    }, []);

    const handleDrop = React.useCallback((event: React.DragEvent<HTMLDivElement>) => {
        if (!dragKindAccepted(event.dataTransfer, dropAcceptsAny, dropAccepts)) {
            return;
        }
        const hit = hitAt(event);
        setDropState(null);
        if (hit === null) {
            return;
        }
        event.preventDefault();
        event.stopPropagation();
        const payload = readDragPayload(event.dataTransfer);
        if (payload) {
            void sendRef.current(CMD_DROP, dropArgs(payload.source, payload.keys, payload.selection, hit));
        }
    }, [dropAcceptsAny, dropAccepts, hitAt]);

    // The diagram as a whole is marked where the drag points beside the nodes, a node by a frame drawn over it.
    const onDiagram = dropState !== null && dropState.node === null;
    const dropWord = dropRefused ? 'drop-refused' : 'drop-onto';
    const markerRect = dropState !== null ? dropState.rect : null;

    return (
        <div id={controlId} ref={ref} className={rootClassName(state, DIAGRAM_CLASS, fillClass)}
            data-tl-state={stateAttr(onDiagram && dropWord)}
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
        >
            {markerRect !== null && createPortal(
                <div className={DROP_MARKER_CLASS} data-tl-state={dropWord}
                    style={{ left: markerRect.left, top: markerRect.top, width: markerRect.width,
                        height: markerRect.height }} />,
                document.body)}
            {dropRefused && dropVerdict?.reason && (
                <DropHint reason={dropVerdict.reason} start={hintAnchorRef.current} moveRef={hintMoveRef} />
            )}
        </div>
    );
}

export default TLFlowDiagram;
