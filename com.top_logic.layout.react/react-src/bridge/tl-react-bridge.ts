import React, { createContext, useContext, useSyncExternalStore, useCallback, useLayoutEffect, useRef, useEffect } from 'react';
import { createRoot, type Root } from 'react-dom/client';
import { flushSync } from 'react-dom';
import type { TLCellProps } from './types';
import { getComponent } from './registry';
import { connect, subscribe, unsubscribe } from './sse-client';
import { setI18NApiBase, setI18NWindowName } from './i18n';
import { createScope, registerScope, addBinding, type GestureHandler, type KeyboardScope } from './keyboard-dispatcher';
import { pushTrap, firstFocusable } from './focus-trap';

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

/** Map of mounted control IDs to their React roots, state stores, and SSE listeners. */
const _mounts = new Map<string, { root: Root | null; store: ControlStateStore; sseListener: StateListener }>();

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

  const store = new ControlStateStore(initialState);

  // Hide the mount point eagerly to prevent a visible flash when the control starts hidden.
  if (initialState.hidden === true) {
    element.style.display = 'none';
  }

  // Listen for SSE updates.
  const sseListener = (data: Record<string, unknown>) => {
    // PatchEvents send partial state, StateEvents send full state.
    // Both arrive through notifyListeners. We check for a full replacement marker.
    store.applyPatch(data);
  };
  subscribe(controlId, sseListener);

  const root = createRoot(element);
  _mounts.set(controlId, { root, store, sseListener });

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
    root.render(React.createElement(Wrapper));
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
  if (entry) {
    unsubscribe(controlId, entry.sseListener);
    if (entry.root) {
      entry.root.unmount();
    }
    _mounts.delete(controlId);
  }
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
  // Re-use an existing mount entry if the child was independently mounted.
  let entry = _mounts.get(childControlId);
  if (!entry) {
    const store = new ControlStateStore(initialState);
    const sseListener = (data: Record<string, unknown>) => {
      store.applyPatch(data);
    };
    subscribe(childControlId, sseListener);
    // Track it so we can clean up on unmount.
    entry = { root: null, store, sseListener };
    _mounts.set(childControlId, entry);
  }

  // Inherit windowName from the parent context if available.
  const parentWindowName = _lastWindowName;

  return { controlId: childControlId, windowName: parentWindowName, store: entry.store };
}

/** Tracks the last windowName used in mount() for child context inheritance. */
let _lastWindowName = '';

/**
 * Returns the current state of the enclosing TopLogic control.
 */
export function useTLState(): Record<string, unknown> {
  const ctx = useContext(TLControlContext);
  if (!ctx) {
    throw new Error('useTLState must be used inside a TLReact-mounted component.');
  }
  return useSyncExternalStore(ctx.store.subscribeStore, ctx.store.getSnapshot);
}

/**
 * Returns a function to send a command to the server for the enclosing control.
 */
export function useTLCommand(): (command: string, args?: Record<string, unknown>) => Promise<void> {
  const ctx = useContext(TLControlContext);
  if (!ctx) {
    throw new Error('useTLCommand must be used inside a TLReact-mounted component.');
  }
  const controlId = ctx.controlId;
  const windowName = ctx.windowName;

  return useCallback(
    async (command: string, args?: Record<string, unknown>) => {
      const body = JSON.stringify({
        controlId,
        command,
        windowName,
        arguments: args ?? {},
      });
      try {
        const resp = await fetch(getApiBase() + 'react-api/command', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body,
        });
        if (!resp.ok) {
          console.error('[TLReact] Command failed:', resp.status, await resp.text());
        }
      } catch (e) {
        console.error('[TLReact] Command error:', e);
      }
    },
    [controlId, windowName]
  );
}

/**
 * Returns a function to upload a FormData to the server for the enclosing control.
 *
 * <p>The control ID and window name are appended automatically. The server dispatches
 * to controls implementing the {@code UploadHandler} interface.</p>
 */
export function useTLUpload(): (formData: FormData) => Promise<void> {
  const ctx = useContext(TLControlContext);
  if (!ctx) {
    throw new Error('useTLUpload must be used inside a TLReact-mounted component.');
  }
  const controlId = ctx.controlId;
  const windowName = ctx.windowName;

  return useCallback(
    async (formData: FormData) => {
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
    [controlId, windowName]
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
 * Options for {@link useTLFieldValue}.
 */
export interface TLFieldValueOptions {
  /**
   * When > 0, the server `valueChanged` is debounced by this many milliseconds instead of being
   * sent on every {@link setValue}. The local store is still updated immediately on every call, so
   * the controlled input stays responsive; only the round-trip is coalesced. Use the returned
   * `flush` (e.g. on blur) to send any pending value immediately. Leave 0/undefined for discrete
   * controls (checkbox, select, date picker) that should commit on every change.
   */
  debounceMs?: number;
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

  const debounceMs = options?.debounceMs ?? 0;
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const pendingRef = useRef<{ value: unknown } | null>(null);

  const send = useCallback(
    (value: unknown) => sendCommand('valueChanged', { value }),
    [sendCommand]
  );

  const flush = useCallback((): Promise<void> => {
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

  const setValue = useCallback(
    (newValue: unknown) => {
      // Optimistically update the local store so the controlled input reflects the new value
      // immediately, without waiting for a server round-trip.
      ctx?.store.applyPatch({ value: newValue });
      if (debounceMs > 0) {
        pendingRef.current = { value: newValue };
        if (timerRef.current !== null) {
          clearTimeout(timerRef.current);
        }
        timerRef.current = setTimeout(() => {
          timerRef.current = null;
          pendingRef.current = null;
          send(newValue);
        }, debounceMs);
      } else {
        send(newValue);
      }
    },
    [send, ctx, debounceMs]
  );

  // Flush a pending value if the field is unmounted mid-edit (e.g. a dialog closes before blur).
  const flushRef = useRef(flush);
  flushRef.current = flush;
  useEffect(() => () => { void flushRef.current(); }, []);

  return [state.value, setValue, flush];
}

// --- Keyboard scopes & gesture bindings ---

/**
 * The nearest enclosing keyboard scope. Provided by {@link KeyboardScopeProvider}
 * (a dialog/window/table/app-root) and consumed by {@link useKeyboardBinding}.
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
 * Registers a gesture-&gt;handler binding into the nearest enclosing keyboard scope.
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
  const scope = useContext(KeyboardScopeContext);
  const handlerRef = React.useRef(handler);
  handlerRef.current = handler;
  const gestures = gesture == null ? [] : (Array.isArray(gesture) ? gesture : [gesture]);
  const key = gestures.join('|');
  React.useEffect(() => {
    if (!scope || gestures.length === 0) {
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
 *        container itself (default; e.g. a menu that manages its own roving focus). In every mode
 *        the fallback is the container - never an incidental title-bar button.
 */
function useFocusTrap(
  active: boolean,
  ref: { current: HTMLElement | null },
  mode: 'field' | 'first' | 'container' = 'container'
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
      const pick = mode === 'field' ? firstFocusable(cur, true)
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
    const unpush = pushTrap(() => ref.current, preferredTarget);

    const target = preferredTarget();
    if (target && target !== document.activeElement) {
      target.focus();
    }

    return () => {
      unpush();
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

// Start the DOM observer as soon as the DOM is ready.
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', setupDOMObserver);
} else {
  setupDOMObserver();
}

// Run initial discovery after all scripts have loaded (controls bundle must
// be registered before mount() can look up components).
window.addEventListener('load', () => discoverAndMount());
