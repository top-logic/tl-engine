import React, { createContext, useContext, useSyncExternalStore, useCallback, useLayoutEffect } from 'react';
import { createRoot, type Root } from 'react-dom/client';
import { flushSync } from 'react-dom';
import type { TLCellProps } from './types';
import { getComponent } from './registry';
import { connect, subscribe, unsubscribe } from './sse-client';
import { setI18NApiBase, setI18NWindowName } from './i18n';

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
function getApiBase(): string {
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
 * Convenience hook for form field value + setter.
 */
export function useTLFieldValue(): [unknown, (newValue: unknown) => void] {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const ctx = useContext(TLControlContext);

  const setValue = useCallback(
    (newValue: unknown) => {
      // Optimistically update the local store so the controlled input reflects the
      // new value immediately, without waiting for a server round-trip.
      ctx?.store.applyPatch({ value: newValue });
      sendCommand('valueChanged', { value: newValue });
    },
    [sendCommand, ctx]
  );

  return [state.value, setValue];
}

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
