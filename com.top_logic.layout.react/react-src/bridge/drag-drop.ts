/**
 * The payload a drag between two server-side controls carries, and the `dataTransfer` encoding of
 * it.
 *
 * A drag names client-side identities only: the control the drag started in, the keys of the rows
 * it started on, and a type tag classifying what is being dragged. The server resolves the objects
 * from those, so a control keeps sole authority over what its own keys mean.
 */

/** The `dataTransfer` entry holding the JSON {@link TLDragPayload}. */
export const DRAG_PAYLOAD_TYPE = 'application/x-tl-drag+json';

/**
 * Prefix of the `dataTransfer` entry whose type name carries the payload's
 * {@link TLDragPayload.type} tag, lower-cased.
 *
 * A `dragover` handler may read `dataTransfer.types` but not the entries' data, so the one
 * property a drop target needs while the pointer is still moving - what is being dragged - is
 * encoded in a type name rather than in a value.
 */
export const DRAG_TAG_TYPE_PREFIX = 'application/x-tl-drag-tag.';

/** Where a drop happened relative to the row it was made on. */
export type TLDropPosition = 'before' | 'after' | 'onto' | 'none';

/** What a drag carries from the control it started in to the control it is dropped on. */
export interface TLDragPayload {
  /** Id of the control the drag started in. */
  source: string;
  /** Client-side keys of the dragged rows within that control. */
  keys: string[];
  /** Whether the drag carries the source control's whole selection instead of {@link keys}. */
  selection: boolean;
  /** The type tag classifying the dragged objects. */
  type: string;
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
 * Writes a drag payload into a `dragstart` event's `dataTransfer`, as the JSON entry plus the type
 * tag entry {@link DRAG_TAG_TYPE_PREFIX a `dragover` handler} can read, and registers it as the
 * {@link runningDrag running drag}.
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
  dataTransfer.setData(DRAG_TAG_TYPE_PREFIX + payload.type.toLowerCase(), '');
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
 * Whether the running drag carries a payload whose type tag is one of the accepted ones.
 *
 * Readable from `dragover`, where the payload itself is not: the decision rests on the type names
 * of the `dataTransfer` entries alone.
 */
export function dragTypeAccepted(dataTransfer: DataTransfer, acceptedTypes: readonly string[]): boolean {
  if (acceptedTypes.length === 0) {
    return false;
  }
  const types = Array.from(dataTransfer.types);
  if (!types.includes(DRAG_PAYLOAD_TYPE)) {
    return false;
  }
  return acceptedTypes.some((accepted) => types.includes(DRAG_TAG_TYPE_PREFIX + accepted.toLowerCase()));
}

/**
 * Where a pointer at the given vertical position sits within a row: its outer thirds insert
 * `before` respectively `after` the row, its middle drops `onto` it.
 */
export function dropPositionAt(clientY: number, row: HTMLElement): TLDropPosition {
  const rect = row.getBoundingClientRect();
  const offset = clientY - rect.top;
  const third = rect.height / 3;
  if (offset < third) {
    return 'before';
  }
  if (offset > third * 2) {
    return 'after';
  }
  return 'onto';
}
