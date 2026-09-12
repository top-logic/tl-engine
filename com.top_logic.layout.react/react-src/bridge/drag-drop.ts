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
 * Writes a drag payload into a `dragstart` event's `dataTransfer`, as the JSON entry plus the type
 * tag entry {@link DRAG_TAG_TYPE_PREFIX a `dragover` handler} can read.
 */
export function writeDragPayload(dataTransfer: DataTransfer, payload: TLDragPayload): void {
  dataTransfer.effectAllowed = 'move';
  dataTransfer.setData(DRAG_PAYLOAD_TYPE, JSON.stringify(payload));
  dataTransfer.setData(DRAG_TAG_TYPE_PREFIX + payload.type.toLowerCase(), '');
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
