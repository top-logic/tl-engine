/**
 * The payload a drag between two server-side controls carries, and the `dataTransfer` encoding of
 * it.
 *
 * A drag names client-side identities only: the control the drag started in, the keys of the rows
 * it started on, and optionally a kind classifying what is being dragged. The server resolves the objects
 * from those, so a control keeps sole authority over what its own keys mean.
 */

/** The `dataTransfer` entry holding the JSON {@link TLDragPayload}. */
export const DRAG_PAYLOAD_TYPE = 'application/x-tl-drag+json';

/**
 * Prefix of the `dataTransfer` entry whose type name carries the payload's
 * {@link TLDragPayload.kind}, lower-cased; a drag without a kind has no such entry.
 *
 * A `dragover` handler may read `dataTransfer.types` but not the entries' data, so the one
 * property a drop target needs while the pointer is still moving - what is being dragged - is
 * encoded in a type name rather than in a value.
 */
export const DRAG_KIND_TYPE_PREFIX = 'application/x-tl-drag-kind.';

/** Wire name of the drop mode inserting among the items (`DropMode.ORDERED`). */
export const DROP_MODE_ORDERED = 'ordered';

/** Wire name of the drop mode dropping onto a single item (`DropMode.ONTO`). */
export const DROP_MODE_ONTO = 'onto';

/** Wire name of the drop mode dropping on the control as a whole (`DropMode.CONTROL`). */
export const DROP_MODE_CONTROL = 'control';

/**
 * Where within an item the pointer is (`DropZone`): its upper, middle or lower part, or `none`
 * beside the items. The client only reports the zone; the server decides what a drop there does.
 */
export type TLDropZone = 'upper' | 'middle' | 'lower' | 'none';

/**
 * What a drop target draws for an accepted drop (`DropMarker`): an insertion line before or after
 * an item, a highlight of the item, or a highlight of the control as a whole.
 */
export type TLDropMarker = 'before' | 'after' | 'into' | 'control';

/**
 * How an item is split into zones:
 * - `thirds`: upper, middle and lower third,
 * - `halves`: upper and lower half,
 * - `whole`: the whole item is its middle,
 * - `none`: an item has no zone of its own, the pointer is always beside the items.
 */
export type TLZoneSplit = 'thirds' | 'halves' | 'whole' | 'none';

/** What a drag carries from the control it started in to the control it is dropped on. */
export interface TLDragPayload {
  /** Id of the control the drag started in. */
  source: string;
  /** Client-side keys of the dragged rows within that control. */
  keys: string[];
  /** Whether the drag carries the source control's whole selection instead of {@link keys}. */
  selection: boolean;
  /** The kind classifying the dragged objects, absent for a drag without a kind. */
  kind?: string;
}

/**
 * A drag started in this document, readable while it runs - unlike the `dataTransfer` payload,
 * which a `dragover` handler cannot read.
 */
export interface TLRunningDrag {
  /** Identifier of this drag, unique within the document. */
  id: string;
  /** What the drag carries. */
  payload: TLDragPayload;
  /**
   * Vertical extent of the drag image around the pointer, in pixels relative to the pointer: the
   * browser draws the source element so that the point it was grabbed at stays under the pointer.
   * `top` is at most 0, `bottom` at least 0.
   */
  image: { top: number; bottom: number };
}

/** What {@link writeDragPayload} reads from a `dragstart` event. */
export interface TLDragStart {
  dataTransfer: DataTransfer;
  /** The element the drag starts on. */
  currentTarget: EventTarget;
  clientY: number;
}

/** The drag running in this document, `null` while none does. */
let _runningDrag: TLRunningDrag | null = null;

/** Number of drags started in this document, the source of {@link TLRunningDrag.id}. */
let _dragCount = 0;

/** Whether the document-wide listeners ending {@link _runningDrag} are installed. */
let _endListenersInstalled = false;

/** Listeners registered by {@link onDragEnd}. */
const _endListeners = new Set<() => void>();

/** Ends the running drag, if one runs, and tells the {@link onDragEnd} listeners. */
function endDrag(): void {
  if (_runningDrag === null) {
    return;
  }
  _runningDrag = null;
  for (const listener of Array.from(_endListeners)) {
    listener();
  }
}

/**
 * Calls `callback` once the browser has taken the drag image, if the drag still runs then: the one
 * moment at which a drag source is marked as dragged (`data-tl-state~="dragging"`, faded by drag.css).
 * Called from a `dragstart` handler. A change made within `dragstart` itself would show in the drag
 * image, which the browser takes from the source after the handler returns; React renders a state set
 * there before that.
 *
 * Whether the drag still runs is watched here rather than read from {@link runningDrag}: a drag
 * started without {@link writeDragPayload} (tree, list reordering) is not registered there.
 */
export function afterDragImage(callback: () => void): void {
  let ended = false;
  const end = () => { ended = true; };
  window.addEventListener('dragend', end, true);
  window.addEventListener('drop', end, true);
  window.setTimeout(() => {
    window.removeEventListener('dragend', end, true);
    window.removeEventListener('drop', end, true);
    if (!ended) {
      callback();
    }
  }, 0);
}

/**
 * Ends the running drag once it ends, wherever it ends.
 *
 * `dragend` is dispatched at the element the drag started on, which may have been removed from the
 * document meanwhile (a virtualized list re-rendering its rows), so its event never reaches a
 * window listener; {@link writeDragPayload} therefore also listens on that element itself. `drop`
 * is dispatched at the target, in whatever control. Listening to both in the capture phase on the
 * window ends the drag in either case.
 */
function installEndListeners(): void {
  if (_endListenersInstalled) {
    return;
  }
  _endListenersInstalled = true;
  window.addEventListener('dragend', endDrag, true);
  window.addEventListener('drop', endDrag, true);
}

/**
 * Registers a listener called when the {@link runningDrag running drag} ends, by a drop anywhere,
 * by a drop refused, or by a cancel.
 *
 * A drop target showing feedback for the drag above it clears it here: a refused drop is not
 * dispatched to the target, and the source's `dragend` reaches only the source's control.
 *
 * @returns Removes the listener again.
 */
export function onDragEnd(listener: () => void): () => void {
  _endListeners.add(listener);
  return () => {
    _endListeners.delete(listener);
  };
}

/**
 * Writes a drag payload into a `dragstart` event's `dataTransfer`, as the JSON entry plus - for a
 * drag of a kind - the kind entry {@link DRAG_KIND_TYPE_PREFIX a `dragover` handler} can read, and
 * registers it as the {@link runningDrag running drag}.
 *
 * The element the drag starts on is also listened to for the drag's `dragend`, which it receives
 * even when it is removed from the document before the drag ends.
 *
 * @returns The running drag the payload now describes.
 */
export function writeDragPayload(event: TLDragStart, payload: TLDragPayload): TLRunningDrag {
  const dataTransfer = event.dataTransfer;
  dataTransfer.effectAllowed = 'move';
  dataTransfer.setData(DRAG_PAYLOAD_TYPE, JSON.stringify(payload));
  if (payload.kind) {
    dataTransfer.setData(DRAG_KIND_TYPE_PREFIX + payload.kind.toLowerCase(), '');
  }
  installEndListeners();
  const source = event.currentTarget;
  source.addEventListener('dragend', endDrag, { once: true });
  let image = { top: 0, bottom: 0 };
  if (source instanceof Element) {
    const rect = source.getBoundingClientRect();
    image = {
      top: Math.min(0, rect.top - event.clientY),
      bottom: Math.max(0, rect.bottom - event.clientY),
    };
  }
  _dragCount++;
  _runningDrag = { id: 'drag' + _dragCount, payload, image };
  return _runningDrag;
}

/**
 * The drag started by {@link writeDragPayload} that is still running, `null` if none is.
 *
 * A `dragover` handler reads the dragged rows here to ask the server whether a drop at the pointer
 * would be accepted. A drag that started in another document (another window, another
 * application) is not known here.
 */
export function runningDrag(): TLRunningDrag | null {
  return _runningDrag;
}

/**
 * The drag payload of a `drop` event, or `null` if the drop carries none (a file, a text selection,
 * a drag from another application).
 */
export function readDragPayload(dataTransfer: DataTransfer): TLDragPayload | null {
  const json = dataTransfer.getData(DRAG_PAYLOAD_TYPE);
  if (!json) {
    return null;
  }
  try {
    return JSON.parse(json) as TLDragPayload;
  } catch {
    return null;
  }
}

/**
 * Whether the running drag carries a payload a drop target accepts: any payload where the target
 * accepts any drag, otherwise one whose kind is among the accepted kinds - a drag without a kind
 * then never is.
 *
 * Readable from `dragover`, where the payload itself is not: the decision rests on the type names
 * of the `dataTransfer` entries alone.
 *
 * @param acceptsAny Whether the target accepts every drag, with any kind or none.
 * @param acceptedKinds The kinds the target accepts where it does not accept any drag.
 */
export function dragKindAccepted(
  dataTransfer: DataTransfer,
  acceptsAny: boolean,
  acceptedKinds: readonly string[],
): boolean {
  const types = Array.from(dataTransfer.types);
  if (!types.includes(DRAG_PAYLOAD_TYPE)) {
    return false;
  }
  if (acceptsAny) {
    return true;
  }
  return acceptedKinds.some((accepted) => types.includes(DRAG_KIND_TYPE_PREFIX + accepted.toLowerCase()));
}

/**
 * The drop modes that apply to the drag a transfer carries: the modes the target announces for the
 * dragged kind, or all its modes where the kind is not listed - a target accepting any kind lists
 * none. The split of an item follows these modes, so a target inserting one kind among its items and
 * dropping another kind onto an item splits an item in halves for the first and takes it as a whole
 * for the second.
 *
 * @param dataTransfer The transfer of the running drag.
 * @param modes The wire names of the drop modes the target announces.
 * @param modesByKind The wire names of the drop modes the target announces per accepted kind.
 */
export function dragModes(
  dataTransfer: DataTransfer,
  modes: readonly string[],
  modesByKind: Readonly<Record<string, readonly string[]>>,
): readonly string[] {
  const types = Array.from(dataTransfer.types);
  const result = new Set<string>();
  let listed = false;
  for (const [kind, kindModes] of Object.entries(modesByKind)) {
    if (types.includes(DRAG_KIND_TYPE_PREFIX + kind.toLowerCase())) {
      listed = true;
      kindModes.forEach((mode) => result.add(mode));
    }
  }
  return listed ? [...result] : modes;
}

/**
 * The split of a row of a flat list for the given announced drop modes: an insertion between two
 * rows needs their upper and lower halves told apart, a drop onto a row needs the row itself, both
 * together need the row in thirds, and a drop on the control as a whole needs no row at all.
 *
 * @param modes The wire names of the drop modes the target announces.
 */
export function flatZoneSplit(modes: readonly string[]): TLZoneSplit {
  const ordered = modes.includes(DROP_MODE_ORDERED);
  const onto = modes.includes(DROP_MODE_ONTO);
  if (ordered && onto) {
    return 'thirds';
  }
  if (ordered) {
    return 'halves';
  }
  if (onto) {
    return 'whole';
  }
  return 'none';
}

/**
 * The split of a node of a tree for the given announced drop modes: an insertion needs the upper,
 * middle and lower third of a node told apart - before the node, into it, after it -, a drop onto
 * a node only needs the node itself, and a drop on the control as a whole needs no node at all.
 *
 * @param modes The wire names of the drop modes the target announces.
 */
export function treeZoneSplit(modes: readonly string[]): TLZoneSplit {
  if (modes.includes(DROP_MODE_ORDERED)) {
    return 'thirds';
  }
  if (modes.includes(DROP_MODE_ONTO)) {
    return 'whole';
  }
  return 'none';
}

/**
 * The zone of the given item a pointer at the given vertical position is in, under the given split.
 */
export function dropZoneAt(clientY: number, item: HTMLElement, split: TLZoneSplit): TLDropZone {
  if (split === 'none') {
    return 'none';
  }
  if (split === 'whole') {
    return 'middle';
  }
  const rect = item.getBoundingClientRect();
  const offset = clientY - rect.top;
  if (split === 'halves') {
    return offset < rect.height / 2 ? 'upper' : 'lower';
  }
  const third = rect.height / 3;
  if (offset < third) {
    return 'upper';
  }
  if (offset > third * 2) {
    return 'lower';
  }
  return 'middle';
}
