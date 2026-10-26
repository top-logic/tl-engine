import React, { createContext, useContext, useSyncExternalStore, useCallback, useLayoutEffect, useRef, useEffect } from 'react';
import { createRoot, type Root } from 'react-dom/client';
import { flushSync } from 'react-dom';
import type { TLCellProps } from './types';
import { getComponent } from './registry';
import { wrapRoot } from './root-wrapper';
import { installTouchDrag } from './touch-drag';
import { connect, subscribe, unsubscribe } from './sse-client';
import { setI18NApiBase, setI18NWindowName } from './i18n';
import { createScope, registerScope, addBinding, pageScope, type GestureHandler, type KeyboardScope } from './keyboard-dispatcher';
import { pushTrap, firstFocusable } from './focus-trap';
import { CMD_COMMIT, CMD_SUBMIT, CMD_UPLOAD_REJECTED, CMD_VALUE_CHANGED, enqueueCommand, registerPendingFlush, unregisterPendingFlush } from './command-channel';
import type { CommandOptions } from './command-channel';
import type { ChildControlJson } from '../state/control-state';

/**
 * Per-control state store compatible with React's useSyncExternalStore.
 */
class ControlStateStore {
  private _state: Record<string, unknown>;
  private _subscribers = new Set<() => void>();

  constructor(initialState: Record<string, unknown>) {
    this._state = { ...initialState };
  }

  getSnapshot = (): Record<string, unknown> => {
    return this._state;
  };

  subscribeStore = (callback: () => void): (() => void) => {
    this._subscribers.add(callback);
    return () => this._subscribers.delete(callback);
  };

  replaceState(newState: Record<string, unknown>): void {
    this._state = { ...newState };
    this._notify();
  }

  applyPatch(patch: Record<string, unknown>): void {
    this._state = { ...this._state, ...patch };
    this._notify();
  }

  private _notify(): void {
    for (const cb of this._subscribers) {
      cb();
    }
  }
}

interface TLControlContextValue {
  controlId: string;
  windowName: string;
  store: ControlStateStore;
}

const TLControlContext = createContext<TLControlContextValue | null>(null);

type StateListener = (state: Record<string, unknown>) => void;

/**
 * The client side of a control: its React root (for a top-level {@link mount}), its state store
 * and its SSE subscription.
 */
interface MountEntry {
  root: Root | null;
  store: ControlStateStore;
  sseListener: StateListener;

  /**
   * The live holders of the entry: one top-level {@link mount} or one or more
   * {@link createChildContext} callers.
   *
   * <p>It exists because a React re-render can create the next holder — reusing this entry and
   * its SSE subscription — before the previous holder's cleanup runs; tearing down on the first
   * {@link unmount} would then orphan the still-mounted component from further SSE patches. The
   * entry is torn down only when the last holder releases.</p>
   *
   * <p>An entry created for a child descriptor arriving with an SSE event (see
   * {@link registerChildDescriptors}) has no holder until a {@link TLChild} renders the child.</p>
   */
  refs: number;

  /**
   * The control describing this one, as long as the entry has no holder; {@code null} otherwise:
   * the control whose SSE update carried the descriptor this entry was created for, or, for a
   * descriptor nested in the state of another descriptor, the control of that other descriptor.
   */
  owner: string | null;

  /**
   * The entries created for descriptors in the SSE events of this control that have no holder
   * yet. They are torn down together with this entry.
   */
  owned: Set<string>;
}

/**
 * Map of mounted control IDs to their React roots, state stores, and SSE listeners.
 */
const _mounts = new Map<string, MountEntry>();

/**
 * The serialized state of the child descriptor last applied to a store, by store.
 *
 * <p>A descriptor is applied to a store once. Applying it again (when a parent remounts a child
 * with the descriptor it already held) would turn the child back to the state it had when the
 * parent serialized it, discarding the patches the child received since.</p>
 */
const _appliedDescriptors = new WeakMap<ControlStateStore, string>();

/**
 * Creates the entry of a control with a store holding the given state and subscribes it to the
 * SSE updates of the control.
 */
function createEntry(controlId: string, initialState: Record<string, unknown>, root: Root | null): MountEntry {
  const store = new ControlStateStore(initialState);
  // PatchEvents send partial state, StateEvents send full state; both arrive as a patch. Child
  // descriptors in the update are registered before the update reaches the store, so that their
  // own updates following in the same SSE batch find a listener.
  const sseListener = (data: Record<string, unknown>) => {
    registerChildDescriptors(controlId, data);
    store.applyPatch(data);
  };
  subscribe(controlId, sseListener);
  return { root, store, sseListener, refs: 0, owner: null, owned: new Set() };
}

/**
 * Whether the given value is a child descriptor (see {@link ChildControlJson}).
 */
function isChildDescriptor(value: Record<string, unknown>): boolean {
  return typeof value.controlId === 'string'
    && typeof value.module === 'string'
    && typeof value.state === 'object' && value.state !== null && !Array.isArray(value.state);
}

/**
 * Makes sure every child control described in an update of the given control has its entry when
 * the update reaches the store.
 *
 * <p>The server creates a child control and sends the patches of the child in the same request as
 * the descriptor of the child in the state of its parent. A {@link TLChild} creates the entry of
 * the child only when React renders the parent's new state, which happens after the whole batch of
 * SSE events is dispatched. Without an entry, the patches of the child would find no listener and
 * the child would show the state of its descriptor.</p>
 *
 * <p>For each descriptor in the update (at any depth, also inside the state of another
 * descriptor), an entry missing so far is created from the descriptor's state, owned by the
 * control that describes it. A descriptor whose state has not been applied to an existing entry
 * is applied to its store. Either way, the descriptor counts as applied (see
 * {@link _appliedDescriptors}).</p>
 *
 * @param owner the control whose update contains the given value
 * @param value the update or a part of it
 */
function registerChildDescriptors(owner: string, value: unknown): void {
  if (Array.isArray(value)) {
    for (const element of value) {
      registerChildDescriptors(owner, element);
    }
    return;
  }
  if (value === null || typeof value !== 'object') {
    return;
  }
  const obj = value as Record<string, unknown>;
  if (isChildDescriptor(obj)) {
    registerChildDescriptor(owner, obj as unknown as ChildControlJson & { state: Record<string, unknown> });
    return;
  }
  for (const key in obj) {
    registerChildDescriptors(owner, obj[key]);
  }
}

/**
 * Registers a single child descriptor, see {@link registerChildDescriptors}.
 */
function registerChildDescriptor(
  owner: string,
  descriptor: ChildControlJson & { state: Record<string, unknown> }
): void {
  const childId = descriptor.controlId;
  const stateKey = JSON.stringify(descriptor.state);
  let entry = _mounts.get(childId);
  if (entry === undefined) {
    const ownerEntry = _mounts.get(owner);
    if (ownerEntry === undefined) {
      return;
    }
    entry = createEntry(childId, descriptor.state, null);
    entry.owner = owner;
    ownerEntry.owned.add(childId);
    _mounts.set(childId, entry);
    _appliedDescriptors.set(entry.store, stateKey);
    registerChildDescriptors(childId, descriptor.state);
  } else if (_appliedDescriptors.get(entry.store) !== stateKey) {
    _appliedDescriptors.set(entry.store, stateKey);
    registerChildDescriptors(childId, descriptor.state);
    entry.store.applyPatch(descriptor.state);
  }
}

/**
 * Removes the given entry with its SSE subscription and React root, together with the entries it
 * owns that still have no holder.
 */
function tearDown(controlId: string, entry: MountEntry): void {
  unsubscribe(controlId, entry.sseListener);
  if (entry.root) {
    entry.root.unmount();
  }
  if (_mounts.get(controlId) === entry) {
    _mounts.delete(controlId);
  }
  if (entry.owner !== null) {
    _mounts.get(entry.owner)?.owned.delete(controlId);
  }
  const owned = [...entry.owned];
  entry.owned.clear();
  for (const childId of owned) {
    const child = _mounts.get(childId);
    if (child !== undefined && child.refs === 0 && child.owner === controlId) {
      tearDown(childId, child);
    }
  }
}

/** The application context path (e.g. "/demo"), set on first mount(). */
let _contextPath = '';

/** Whether SSE has been connected. */
let _sseConnected = false;

/**
 * Returns the API base URL (context path with trailing slash).
 */
export function getApiBase(): string {
  return _contextPath + '/';
}

/**
 * Mounts a React component into the given DOM element.
 */
export function mount(
  controlId: string,
  moduleName: string,
  initialState: Record<string, unknown>,
  windowName?: string,
  contextPath?: string
): void {
  // Store context path from the first mount call.
  if (contextPath !== undefined) {
    _contextPath = contextPath;
  }

  const resolvedWindowName = windowName ?? '';

  // Connect SSE on first mount, or reconnect on page reload.
  // When contextPath is provided (from internalWrite()), a fresh page render is happening.
  // After an AJAX page reload, the JS module state persists (_sseConnected = true) but the
  // server-side AsyncContext for the old EventSource may have become invalid. Force a
  // reconnect to establish a fresh SSE connection.
  if (!_sseConnected) {
    _sseConnected = true;
    setI18NApiBase(getApiBase());
    connect(getApiBase() + 'react-api/events?windowName=' + encodeURIComponent(resolvedWindowName));
  } else if (contextPath !== undefined) {
    connect(getApiBase() + 'react-api/events?windowName=' + encodeURIComponent(resolvedWindowName));
  }

  // Always update windowName (it may change between mount calls).
  setI18NWindowName(resolvedWindowName);

  const element = document.getElementById(controlId);
  if (!element) {
    console.error('[TLReact] Mount point not found:', controlId);
    return;
  }

  const Component = getComponent(moduleName);
  if (!Component) {
    console.error('[TLReact] Component not registered:', moduleName);
    return;
  }

  // Clean up any existing mount for this controlId (e.g. after AJAX page reload).
  unmount(controlId);

  // Hide the mount point eagerly to prevent a visible flash when the control starts hidden.
  if (initialState.hidden === true) {
    element.style.display = 'none';
  }

  const root = createRoot(element);
  const entry = createEntry(controlId, initialState, root);
  entry.refs = 1;
  _mounts.set(controlId, entry);
  const store = entry.store;

  _lastWindowName = resolvedWindowName;

  const Wrapper = () => {
    const state = useSyncExternalStore(store.subscribeStore, store.getSnapshot);

    // Toggle visibility via display style when the server sends hidden state changes.
    // useLayoutEffect prevents any visible flash on state transitions.
    useLayoutEffect(() => {
      element.style.display = state.hidden === true ? 'none' : '';
    }, [state.hidden]);

    return React.createElement(
      TLControlContext.Provider,
      { value: { controlId, windowName: resolvedWindowName, store } },
      React.createElement(Component, { controlId, state })
    );
  };

  flushSync(() => {
    root.render(wrapRoot(React.createElement(Wrapper)));
  });
}

/**
 * Mounts a React form field component.
 */
export function mountField(
  controlId: string,
  moduleName: string,
  initialState: Record<string, unknown>
): void {
  mount(controlId, moduleName, initialState);
}

/**
 * Unmounts a React component by control ID, cleaning up its SSE subscription.
 */
export function unmount(controlId: string): void {
  const entry = _mounts.get(controlId);
  if (!entry) {
    return;
  }
  // Reference-counted release: during a remount, the next holder can reuse this entry (see
  // createChildContext) before this cleanup runs. Only the last holder tears down, so the live
  // store keeps its SSE subscription across the remount instead of being orphaned.
  if (entry.refs > 1) {
    entry.refs--;
    return;
  }
  tearDown(controlId, entry);
}

/**
 * Returns true if the given id is currently mounted as a React control in this window.
 * Used by the TooltipHost to resolve a hovered DOM element to its owning control.
 */
export function isMountedControl(id: string): boolean {
  return _mounts.has(id);
}

// --- Hooks ---

/**
 * The React context that provides control identity and state to TopLogic React components.
 *
 * Exported so that composite controls can wrap child components in a
 * {@code TLControlContext.Provider} pointing to a different (child) control.
 */
export { TLControlContext };
export type { TLControlContextValue };

/**
 * Creates a child control context value suitable for wrapping a sub-component in a
 * {@code TLControlContext.Provider}.
 *
 * <p>The child gets its own {@link ControlStateStore} and SSE subscription so that it can
 * independently receive state/patch events from the server.</p>
 *
 * @param childControlId the server-side control ID of the child control
 * @param initialState   the initial state sent by the server for the child
 * @returns a context value to be passed to {@code TLControlContext.Provider}
 */
export function createChildContext(
  childControlId: string,
  initialState: Record<string, unknown>
): TLControlContextValue {
  // Re-use an existing mount entry if the child was independently mounted or registered when its
  // descriptor arrived (see registerChildDescriptors).
  let entry = _mounts.get(childControlId);
  if (!entry) {
    entry = createEntry(childControlId, initialState, null);
    _mounts.set(childControlId, entry);
  } else if (entry.owner !== null) {
    // The first holder claims an entry registered for an arriving descriptor: from now on, its
    // holders decide its lifetime, not the control that described it.
    _mounts.get(entry.owner)?.owned.delete(childControlId);
    entry.owner = null;
  }
  // Register this holder. A matching unmount() releases it; the entry (and its SSE subscription)
  // survives until the last holder is gone, which keeps patches flowing across a remount.
  entry.refs++;

  // Inherit windowName from the parent context if available.
  const parentWindowName = _lastWindowName;

  return { controlId: childControlId, windowName: parentWindowName, store: entry.store };
}

/**
 * Applies the state of a child's descriptor to the store of the child, unless this descriptor
 * state is the one applied last (see {@link _appliedDescriptors}).
 *
 * @param context  the context of the child, see {@link createChildContext}
 * @param state    the state of the descriptor
 * @param stateKey the serialized form of {@code state}
 */
export function applyChildDescriptor(
  context: TLControlContextValue,
  state: Record<string, unknown>,
  stateKey: string
): void {
  if (_appliedDescriptors.get(context.store) === stateKey) {
    return;
  }
  _appliedDescriptors.set(context.store, stateKey);
  context.store.applyPatch(state);
}

/**
 * Records the state of a child's descriptor as applied to the store of the child, unless a
 * descriptor has been applied to it before: the store started with this state or has received
 * newer state since.
 *
 * @param context the context of the child, see {@link createChildContext}
 * @param state   the state of the descriptor
 */
export function adoptChildDescriptor(context: TLControlContextValue, state: Record<string, unknown>): void {
  if (!_appliedDescriptors.has(context.store)) {
    _appliedDescriptors.set(context.store, JSON.stringify(state));
  }
}

/** Tracks the last windowName used in mount() for child context inheritance. */
let _lastWindowName = '';

/**
 * Returns the current state of the enclosing TopLogic control.
 *
 * @typeParam T The type of the state: the state message of the component (e.g. `ButtonStateJson`
 *        for `TLButton`), see the types exported from 'tl-react-bridge'. The state is not checked
 *        at runtime; the type states what the server side of the control sends.
 */
export function useTLState<T extends object = Record<string, unknown>>(): T {
  const ctx = useContext(TLControlContext);
  if (!ctx) {
    throw new Error('useTLState must be used inside a TLReact-mounted component.');
  }
  return useSyncExternalStore(ctx.store.subscribeStore, ctx.store.getSnapshot) as T;
}

/**
 * Returns a function to send a command to the server for the enclosing control.
 *
 * <p>Commands are dispatched through the strict FIFO chain of {@link enqueueCommand}: they reach
 * the server in dispatch order, and an action command implicitly flushes all pending debounced
 * field values first (see {@link useTLFieldValue}), unless the given {@link CommandOptions} say
 * otherwise. The returned promise resolves when this command's server response has arrived.</p>
 */
export function useTLCommand(): (
  command: string,
  args?: Record<string, unknown>,
  options?: CommandOptions,
) => Promise<void> {
  const ctx = useContext(TLControlContext);
  if (!ctx) {
    throw new Error('useTLCommand must be used inside a TLReact-mounted component.');
  }
  const controlId = ctx.controlId;
  const windowName = ctx.windowName;

  return useCallback(
    async (command: string, args?: Record<string, unknown>, options?: CommandOptions) => {
      await enqueueCommand(getApiBase() + 'react-api/command', {
        controlId,
        command,
        windowName,
        arguments: args ?? {},
      }, options);
    },
    [controlId, windowName]
  );
}

/**
 * State key under which an upload control publishes the maximum upload size in bytes. Must match
 * {@code UploadSupport#MAX_UPLOAD_SIZE}; a value of 0 means that there is no limit.
 */
const STATE_MAX_UPLOAD_SIZE = 'maxUploadSize';

/**
 * The first file in the given form data that is larger than the given limit, `null` if all of them
 * stay within it.
 */
function oversizedFile(formData: FormData, limit: number): File | null {
  for (const value of formData.values()) {
    if (value instanceof File && value.size > limit) {
      return value;
    }
  }
  return null;
}

/**
 * Returns a function to upload a FormData to the server for the enclosing control.
 *
 * <p>The control ID and window name are appended automatically. The server dispatches
 * to controls implementing the {@code UploadHandler} interface.</p>
 *
 * <p>A selection holding a file larger than the control's {@link STATE_MAX_UPLOAD_SIZE} limit is
 * not transmitted at all. The limit applies to each file on its own, not to the selection as a
 * whole. Instead of the upload, the {@link CMD_UPLOAD_REJECTED} command tells the server about the
 * refusal, which reports the limit to the user.</p>
 */
export function useTLUpload(): (formData: FormData) => Promise<void> {
  const ctx = useContext(TLControlContext);
  if (!ctx) {
    throw new Error('useTLUpload must be used inside a TLReact-mounted component.');
  }
  const controlId = ctx.controlId;
  const windowName = ctx.windowName;
  const store = ctx.store;

  return useCallback(
    async (formData: FormData) => {
      const limit = Number(store.getSnapshot()[STATE_MAX_UPLOAD_SIZE] ?? 0);
      if (limit > 0) {
        const oversized = oversizedFile(formData, limit);
        if (oversized !== null) {
          await enqueueCommand(getApiBase() + 'react-api/command', {
            controlId,
            command: CMD_UPLOAD_REJECTED,
            windowName,
            arguments: {
              fileName: oversized.name,
              size: oversized.size,
            },
          });
          return;
        }
      }

      formData.append('controlId', controlId);
      formData.append('windowName', windowName);
      try {
        const resp = await fetch(getApiBase() + 'react-api/upload', {
          method: 'POST',
          body: formData,
        });
        if (!resp.ok) {
          console.error('[TLReact] Upload failed:', resp.status, await resp.text());
        }
      } catch (e) {
        console.error('[TLReact] Upload error:', e);
      }
    },
    [controlId, windowName, store]
  );
}

/**
 * Returns a URL for fetching binary data from the enclosing control's DataProvider.
 */
export function useTLDataUrl(): string {
  const ctx = useContext(TLControlContext);
  if (!ctx) {
    throw new Error('useTLDataUrl must be used inside a TLReact-mounted component.');
  }
  return getApiBase() + 'react-api/data?controlId=' + encodeURIComponent(ctx.controlId)
    + '&windowName=' + encodeURIComponent(ctx.windowName);
}

/**
 * The span a field the user types in holds a typed value back before sending it, unless the server
 * names another one (`state.debounceMs`): long enough to coalesce a burst of keystrokes into one
 * round-trip, short enough that server-side validation can surface while the user pauses. The final
 * value is always sent on blur regardless.
 *
 * <p>One span for every typed input, so that a text, a number and a password field report at the
 * same pace.</p>
 */
export const VALUE_DEBOUNCE_MS = 300;

/**
 * Options for {@link useTLFieldValue}.
 */
export interface TLFieldValueOptions {
  /**
   * When > 0, the server `valueChanged` is debounced by this many milliseconds instead of being
   * sent on every {@link setValue}. The local store is still updated immediately on every call, so
   * the controlled input stays responsive; only the round-trip is coalesced. Use the returned
   * `flush` (e.g. on blur) to send any pending value immediately; a pending value is also flushed
   * automatically before any action command is dispatched (see {@link enqueueCommand}), so e.g.
   * submitting a dialog with Enter never races the debounce window. Leave 0/undefined for discrete
   * controls (checkbox, select, date picker) that should commit on every change.
   */
  debounceMs?: number;

  /**
   * When `true`, no typed value is sent while the user is still in the field: `setValue` only
   * updates the local store, and the pending value is sent by `flush` (on blur), before any action
   * command (see {@link enqueueCommand}), or on unmount. Overrides
   * {@link TLFieldValueOptions.debounceMs}.
   *
   * For a field whose server-side model rewrites the text it is given - a value parsed and
   * re-formatted through a format, say - rather than storing it verbatim. Debouncing is not enough
   * there: it only decides *when* the round-trip happens, and whenever it happens mid-edit the
   * field is re-rendered from the normalized value, throwing away what was being typed. A comma
   * separated list is the clearest case: pausing after `"red, "` sends that text, which parses to
   * `["red"]` and formats back to `"red"` - the comma and the space the user just typed are gone
   * from under the cursor.
   */
  sendOnBlur?: boolean;
}

/**
 * Convenience hook for a form field value, returning `[value, setValue, flush]`.
 *
 * <p>`setValue` always updates the local store immediately (optimistic, so a controlled input
 * reflects typing without a round-trip). With {@link TLFieldValueOptions.debounceMs} set, the
 * server `valueChanged` is debounced and `flush` sends any pending value at once (call it on
 * blur).</p>
 */
export function useTLFieldValue(
  options?: TLFieldValueOptions
): [unknown, (newValue: unknown) => void, () => Promise<void>] {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const ctx = useContext(TLControlContext);

  const sendOnBlur = options?.sendOnBlur === true;
  const debounceMs = sendOnBlur ? 0 : (options?.debounceMs ?? 0);
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const pendingRef = useRef<{ value: unknown } | null>(null);

  // Identity-stable flush wrapper, registered on the command channel while a debounced value is
  // pending so that any action command flushes it before being sent (see enqueueCommand).
  const stableFlushRef = useRef<(() => Promise<void>) | null>(null);

  const send = useCallback(
    (value: unknown) => sendCommand(CMD_VALUE_CHANGED, { value }),
    [sendCommand]
  );

  const flush = useCallback((): Promise<void> => {
    if (stableFlushRef.current !== null) {
      unregisterPendingFlush(stableFlushRef.current);
    }
    if (timerRef.current !== null) {
      clearTimeout(timerRef.current);
      timerRef.current = null;
    }
    const pending = pendingRef.current;
    if (pending !== null) {
      pendingRef.current = null;
      return send(pending.value);
    }
    return Promise.resolve();
  }, [send]);

  const flushRef = useRef(flush);
  flushRef.current = flush;
  if (stableFlushRef.current === null) {
    stableFlushRef.current = () => flushRef.current();
  }

  const setValue = useCallback(
    (newValue: unknown) => {
      // Optimistically update the local store so the controlled input reflects the new value
      // immediately, without waiting for a server round-trip.
      ctx?.store.applyPatch({ value: newValue });
      if (sendOnBlur) {
        // Hold the value back entirely - no timer. flush() still sends it on blur, before any
        // action command, and on unmount, so nothing typed is ever lost.
        pendingRef.current = { value: newValue };
        registerPendingFlush(stableFlushRef.current!);
      } else if (debounceMs > 0) {
        pendingRef.current = { value: newValue };
        registerPendingFlush(stableFlushRef.current!);
        if (timerRef.current !== null) {
          clearTimeout(timerRef.current);
        }
        timerRef.current = setTimeout(() => {
          timerRef.current = null;
          void flushRef.current();
        }, debounceMs);
      } else {
        send(newValue);
      }
    },
    [send, ctx, debounceMs, sendOnBlur]
  );

  // Flush a pending value if the field is unmounted mid-edit (e.g. a dialog closes before blur).
  useEffect(() => () => { void flushRef.current(); }, []);

  return [state.value, setValue, flush];
}

/**
 * Options for {@link useTLTypingField}.
 */
export interface TLTypingFieldOptions {
  /** The value an empty text is sent as: `''` for a text (the default), `null` for a number. */
  emptyValue?: '' | null;

  /**
   * Rewrites a text into the form it is stored in - a web address completed with its scheme, say.
   * Applied when the field is left and when an edit is finished with Enter, never while the user
   * is still typing, where it would fight the cursor.
   */
  normalize?: (text: string) => string;
}

/**
 * The value and handlers a typed field puts on its input, see {@link useTLTypingField}.
 */
export interface TLTypingField {
  /** The text the field shows, `''` for no value. */
  text: string;

  /** Sets the text typed: shown at once, sent as the state's typing behaviour says, and marks an edit. */
  setText: (text: string) => void;

  /** Sends a value held back at once, see {@link useTLFieldValue}. */
  flush: () => Promise<void>;

  /**
   * Finishes the edit: sends a value held back, then - with `state.commitEdits` and only after a
   * change - the {@link CMD_COMMIT} command. For a gesture that is a whole edit by itself, such as
   * the button emptying the input.
   */
  commit: () => Promise<void>;

  /** The blur handler of the input: normalizes the text and {@link TLTypingField.commit commits}. */
  onBlur: () => Promise<void>;

  /**
   * The key handler of a single-line input, `undefined` while the state asks for nothing on Enter.
   * Never attach it to a text area, where Enter is part of the text.
   */
  onKeyDown?: (e: React.KeyboardEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
}

/**
 * The typing behaviour of a field with a `TypingFieldState`, shared by every typed input.
 *
 * <p>The text typed is shown at once. It is sent as {@link CMD_VALUE_CHANGED} after
 * `state.debounceMs` (default {@link VALUE_DEBOUNCE_MS}), or - with `state.sendValueOnBlur` - only
 * when the field is left; leaving the field always sends a value still held back.</p>
 *
 * <p>With `state.commitEdits` each finished edit sends {@link CMD_COMMIT} once: leaving the field
 * after a change, or plain Enter in a single-line input after a change. A field left right after
 * such an Enter, without typing in between, sends no second commit; Enter or leaving the field
 * without a change sends none.</p>
 *
 * <p>With `state.submitOnEnter` plain Enter in a single-line input sends {@link CMD_SUBMIT} with
 * the text as typed, after the commit; only then is the key's default action prevented, so that
 * Enter otherwise still reaches what binds it around the field (a dialog's default button).</p>
 *
 * <p>All commands go through the FIFO chain of {@link enqueueCommand}, so the value reaches the
 * server before the commit, and the commit before the submit.</p>
 */
export function useTLTypingField(options?: TLTypingFieldOptions): TLTypingField {
  const state = useTLState<{
    debounceMs?: number;
    sendValueOnBlur?: boolean;
    commitEdits?: boolean;
    submitOnEnter?: boolean;
  }>();
  const sendCommand = useTLCommand();
  const [value, setValue, flush] = useTLFieldValue({
    debounceMs: state.debounceMs ?? VALUE_DEBOUNCE_MS,
    sendOnBlur: state.sendValueOnBlur === true,
  });
  // Not `??`: a `null` given is the value asked for.
  const emptyValue = options?.emptyValue === null ? null : '';
  const normalize = options?.normalize;
  const commitEdits = state.commitEdits === true;
  const submitOnEnter = state.submitOnEnter === true;
  const dirtyRef = useRef(false);

  const text = value == null ? '' : String(value);

  const toValue = useCallback((t: string) => (t === '' ? emptyValue : t), [emptyValue]);

  const setText = useCallback((t: string) => {
    dirtyRef.current = true;
    setValue(toValue(t));
  }, [setValue, toValue]);

  // Sends the commit of a finished edit after a value still held back, once per edit.
  const commitEdit = useCallback((): Promise<void> => {
    const flushed = flush();
    if (!commitEdits || !dirtyRef.current) {
      return flushed;
    }
    dirtyRef.current = false;
    return sendCommand(CMD_COMMIT);
  }, [flush, commitEdits, sendCommand]);

  const onBlur = useCallback(async () => {
    if (normalize !== undefined) {
      const normalized = normalize(text);
      if (normalized !== text) {
        setValue(toValue(normalized));
      }
    }
    await commitEdit();
  }, [normalize, text, setValue, toValue, commitEdit]);

  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent<HTMLInputElement | HTMLTextAreaElement>) => {
      if (e.key !== 'Enter' || e.shiftKey || e.altKey || e.ctrlKey || e.metaKey) {
        return;
      }
      const input = e.currentTarget;
      if (normalize !== undefined) {
        const normalized = normalize(input.value);
        if (normalized !== input.value) {
          input.value = normalized;
          setValue(toValue(normalized));
        }
      }
      // Read the value before dispatching: the event is no longer available afterwards.
      const typed = input.value;
      void commitEdit();
      if (submitOnEnter) {
        e.preventDefault();
        void sendCommand(CMD_SUBMIT, { value: typed });
      }
    },
    [normalize, setValue, toValue, commitEdit, submitOnEnter, sendCommand]
  );

  return {
    text,
    setText,
    flush,
    commit: commitEdit,
    onBlur,
    onKeyDown: commitEdits || submitOnEnter ? handleKeyDown : undefined,
  };
}

// --- Keyboard scopes & gesture bindings ---

/**
 * The nearest enclosing keyboard scope. Provided by {@link KeyboardScopeProvider}
 * (a dialog, window or table) and consumed by {@link useKeyboardBinding}. Outside any
 * such surface the context is empty and bindings go to the dispatcher's page scope.
 */
const KeyboardScopeContext = createContext<KeyboardScope | null>(null);

/**
 * Provides a keyboard scope to its descendants and registers it on the global
 * keyboard dispatcher stack while mounted.
 *
 * <p>Gesture bindings contributed by descendant controls (via
 * {@link useKeyboardBinding}) land in this scope. The dispatcher walks scopes from
 * the innermost (deepest in the tree / most recently opened) outward, so a dialog's
 * scope sits above the page, and a table's scope nested in a dialog sits above the
 * dialog.</p>
 *
 * @param active
 *        Optional predicate; when it returns {@code false} the dispatcher skips this
 *        scope. Use it for focus-gated scopes (e.g. a table that only handles arrow
 *        keys while focused). Defaults to always active (modal dialogs/windows).
 * @param modal
 *        When true, the scope traps all gestures: keys it does not bind do not fall through to
 *        scopes below (the page behind a dialog). Defaults to false.
 */
const KeyboardScopeProvider: React.FC<{ active?: () => boolean; modal?: boolean; children?: React.ReactNode }> =
  ({ active, modal, children }) => {
    // Keep the latest predicate without recreating the scope on every render.
    const activeRef = React.useRef(active);
    activeRef.current = active;
    const scope = React.useMemo(
      () => createScope(() => (activeRef.current ? activeRef.current() : true), modal === true),
      []
    );
    React.useEffect(() => registerScope(scope), [scope]);
    return React.createElement(KeyboardScopeContext.Provider, { value: scope }, children);
  };

/**
 * Registers a gesture-&gt;handler binding into the nearest enclosing keyboard scope, or
 * into the dispatcher's page scope for a control that no {@link KeyboardScopeProvider}
 * encloses (a page toolbar's button, a form on a page).
 *
 * <p>The handler runs when the gesture is pressed and this scope is the innermost
 * active scope binding it. Returning {@code false} from the handler declines, letting
 * the dispatcher continue to outer scopes (e.g. a disabled button declining Enter).
 * When several bindings in one scope share a gesture, the most recently mounted wins
 * (so a dialog's explicit Cancel action overrides the window's built-in Escape).</p>
 *
 * @param gesture
 *        A gesture spec ("ENTER", "ESCAPE", "Ctrl+S", "Shift+ArrowDown") or array
 *        thereof; {@code null}/empty disables the binding.
 * @param handler
 *        Invoked on the gesture. Need not be stable; the latest closure is always used.
 */
function useKeyboardBinding(gesture: string | string[] | null | undefined, handler: GestureHandler): void {
  const scope = useContext(KeyboardScopeContext) ?? pageScope();
  const handlerRef = React.useRef(handler);
  handlerRef.current = handler;
  const gestures = gesture == null ? [] : (Array.isArray(gesture) ? gesture : [gesture]);
  const key = gestures.join('|');
  React.useEffect(() => {
    if (gestures.length === 0) {
      return;
    }
    const stable: GestureHandler = () => handlerRef.current();
    const removers = gestures.map(g => addBinding(scope, g, stable));
    return () => removers.forEach(r => r());
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [scope, key]);
}

/**
 * Registers a self-contained keyboard scope (no enclosing {@link KeyboardScopeProvider} needed)
 * while {@code active} is true, binding the given gesture-&gt;handler map.
 *
 * <p>Convenience for overlays (drawers, popups, context menus) that previously installed their own
 * {@code document.addEventListener('keydown', ...)} Escape handler. Registering on open gives the
 * scope a high stack position, so the innermost open overlay wins. Handlers are read live, so the
 * map's closures may change between renders.</p>
 */
function useStandaloneKeyboardScope(active: boolean, bindings: Record<string, GestureHandler>): void {
  const bindingsRef = React.useRef(bindings);
  bindingsRef.current = bindings;
  React.useEffect(() => {
    if (!active) {
      return;
    }
    const scope = createScope(() => true);
    for (const gesture of Object.keys(bindingsRef.current)) {
      addBinding(scope, gesture, () => bindingsRef.current[gesture]?.());
    }
    return registerScope(scope);
  }, [active]);
}

/**
 * Binds Escape to {@code onClose} in the enclosing keyboard scope.
 *
 * <p>A component, not a hook: a dialog or window opens its own scope with a
 * {@link KeyboardScopeProvider} and renders this as the provider's first child, so the binding lands
 * in that scope - a hook called by the same component would bind into the scope around it. Being the
 * first child, it is overridden by an explicit action bound to Escape that mounts later.</p>
 */
const EscapeToClose: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  useKeyboardBinding('ESCAPE', () => {
    onClose();
    return true;
  });
  return null;
};

/**
 * Confines keyboard focus to {@code ref}'s element while {@code active}, moving focus into it on
 * activation and restoring focus to the previously focused element on deactivation.
 *
 * <p>Use on modal surfaces (dialogs/windows, popup menus) so the default focus is inside the
 * surface (its primary action can be triggered with Enter) rather than left in the background, and
 * so focus returns to its origin (e.g. a table) once the surface closes.</p>
 *
 * @param active
 *        Whether the trap is engaged (typically the surface's open state).
 * @param ref
 *        The container element to confine focus within.
 * @param mode
 *        Where to place initial focus: {@code 'field'} = the first form field (form dialogs),
 *        {@code 'first'} = the first focusable element (item menus), {@code 'container'} = the
 *        container itself (default; e.g. a menu that manages its own roving focus), or a function
 *        that picks the target in the container for a surface with an order of its own (a dialog).
 *        In every mode the fallback is the container - never an incidental title-bar button.
 * @param options
 *        {@code trap: false} moves focus in and gives it back, but does not confine it - a surface that
 *        is not modal (a drawer without scrim). {@code preventScroll: true} moves focus in without
 *        scrolling, for a surface that slides in from outside the clipped box of its area.
 *        {@code restore: 'from-inside'} gives the focus back only when it is still inside the
 *        surface or was dropped with it, not when the user has put it elsewhere meanwhile - a surface
 *        that is not modal and closes on a press outside, which may be a press into another field.
 */
function useFocusTrap(
  active: boolean,
  ref: { current: HTMLElement | null },
  mode: 'field' | 'first' | 'container' | ((container: HTMLElement) => HTMLElement | null) = 'container',
  options: { trap?: boolean; preventScroll?: boolean; restore?: 'always' | 'from-inside' } = {}
): void {
  React.useEffect(() => {
    if (!active) {
      return;
    }
    const el = ref.current;
    if (!el) {
      return;
    }
    const previous = document.activeElement as HTMLElement | null;

    // Preferred focus target, shared by the initial move-in and the escape pull-back so focus
    // never lands on an incidental control (e.g. the title-bar close button).
    const preferredTarget = (): HTMLElement | null => {
      const cur = ref.current;
      if (!cur) {
        return null;
      }
      const pick = typeof mode === 'function' ? mode(cur)
        : mode === 'field' ? firstFocusable(cur, true)
        : mode === 'first' ? firstFocusable(cur, false)
        : null;
      if (pick) {
        return pick;
      }
      if (!cur.hasAttribute('tabindex')) {
        cur.setAttribute('tabindex', '-1');
      }
      return cur;
    };
    const unpush = options.trap === false ? () => {} : pushTrap(() => ref.current, preferredTarget);

    const target = preferredTarget();
    if (target && target !== document.activeElement) {
      target.focus({ preventScroll: options.preventScroll === true });
    }

    return () => {
      unpush();
      if (options.restore === 'from-inside') {
        // The surface may already be gone, its focus then fell to the document body.
        const current = document.activeElement;
        const inside = current == null || current === document.body || el.contains(current);
        if (!inside) {
          return;
        }
      }
      if (previous && previous.isConnected && typeof previous.focus === 'function') {
        previous.focus();
      }
    };
  }, [active]);
}

export {
  KeyboardScopeContext,
  KeyboardScopeProvider,
  useKeyboardBinding,
  useStandaloneKeyboardScope,
  useFocusTrap,
  EscapeToClose,
};

// --- MutationObserver for auto-mount and auto-unmount ---

/**
 * Discovers unmounted {@code [data-react-module]} elements in the given root
 * and mounts them. Context ({@code windowName}, {@code contextPath}) is read
 * from per-element data attributes written by {@code ReactControl.write()}.
 *
 * <p>Safe to call at any time and multiple times — already-mounted elements
 * (present in {@code _mounts}) are skipped.</p>
 */
export function discoverAndMount(root: ParentNode = document.body): void {
  // Use the body's window name as the authoritative source. Pre-built control trees
  // from programmatically opened windows may carry the opener's window name on their
  // elements, but the body always reflects the current document's window identity.
  const bodyWindowName = document.body.dataset.windowName;
  const bodyContextPath = document.body.dataset.contextPath;

  const elements = root.querySelectorAll<HTMLElement>('[data-react-module]');
  for (const el of elements) {
    if (!el.id || _mounts.has(el.id)) {
      continue;
    }
    const moduleName = el.dataset.reactModule;
    const windowName = bodyWindowName ?? el.dataset.windowName;
    const contextPath = bodyContextPath ?? el.dataset.contextPath;
    if (!moduleName || windowName === undefined || contextPath === undefined) {
      continue;
    }
    const stateJson = el.dataset.reactState;
    const state = stateJson ? JSON.parse(stateJson) : {};
    mount(el.id, moduleName, state, windowName, contextPath);
  }
}

/**
 * Combined MutationObserver that auto-mounts newly added React controls
 * and auto-unmounts removed ones.
 */
function setupDOMObserver(): void {
  const observer = new MutationObserver((mutations) => {
    // Check for removed mounts first.
    // Only unmount entries that were directly mounted via mount() (root !== null).
    // Child contexts (root === null) are managed by their parent React tree and must
    // not be unmounted when their DOM element is temporarily removed (e.g. a snackbar
    // returning null from render).
    for (const mutation of mutations) {
      for (const removed of mutation.removedNodes) {
        if (removed instanceof HTMLElement) {
          const id = removed.id;
          if (id && _mounts.has(id) && _mounts.get(id)!.root !== null) {
            unmount(id);
          }
          for (const [mountId, entry] of _mounts.entries()) {
            if (entry.root !== null && removed.querySelector('#' + CSS.escape(mountId))) {
              unmount(mountId);
            }
          }
        }
      }
    }

    // Check for newly added mount points.
    for (const mutation of mutations) {
      for (const added of mutation.addedNodes) {
        if (added instanceof HTMLElement) {
          // The added node itself may be a mount point.
          if (added.dataset?.reactModule) {
            discoverAndMount(added.parentElement ?? document.body);
          } else {
            // Or it may contain mount points as descendants.
            if (added.querySelector('[data-react-module]')) {
              discoverAndMount(added);
            }
          }
        }
      }
    }
  });

  observer.observe(document.body, { childList: true, subtree: true });
}

// Drag sources and drop targets work by touch as well.
installTouchDrag();

// Start the DOM observer as soon as the DOM is ready.
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', setupDOMObserver);
} else {
  setupDOMObserver();
}

// Run initial discovery after all scripts have loaded (controls bundle must
// be registered before mount() can look up components).
window.addEventListener('load', () => discoverAndMount());
