import React, { createContext, useContext, useSyncExternalStore, useCallback } from 'react';
import { createRoot, type Root } from 'react-dom/client';
import type { TLCellProps } from './types';
import { getComponent } from './registry';
import { connect, subscribe, unsubscribe } from './sse-client';

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

/** Map of mounted control IDs to their React roots and state stores. */
const _mounts = new Map<string, { root: Root; store: ControlStateStore }>();

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

  // Connect SSE on first mount (now that we know the context path).
  if (!_sseConnected) {
    _sseConnected = true;
    connect(getApiBase() + 'react-api/events');
  }
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

  const store = new ControlStateStore(initialState);

  // Listen for SSE updates.
  const sseListener = (data: Record<string, unknown>) => {
    // PatchEvents send partial state, StateEvents send full state.
    // Both arrive through notifyListeners. We check for a full replacement marker.
    store.applyPatch(data);
  };
  subscribe(controlId, sseListener);

  const root = createRoot(element);
  _mounts.set(controlId, { root, store });

  const resolvedWindowName = windowName ?? '';
  _lastWindowName = resolvedWindowName;

  const Wrapper = () => {
    const state = useSyncExternalStore(store.subscribeStore, store.getSnapshot);
    return React.createElement(
      TLControlContext.Provider,
      { value: { controlId, windowName: resolvedWindowName, store } },
      React.createElement(Component, { controlId, state })
    );
  };

  root.render(React.createElement(Wrapper));
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
 * Unmounts a React component by control ID.
 */
export function unmount(controlId: string): void {
  const entry = _mounts.get(controlId);
  if (entry) {
    entry.root.unmount();
    _mounts.delete(controlId);
  }
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
    _mounts.set(childControlId, { root: null as unknown as Root, store });
    entry = _mounts.get(childControlId)!;
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
 * Convenience hook for form field value + setter.
 */
export function useTLFieldValue(): [unknown, (newValue: unknown) => void] {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const setValue = useCallback(
    (newValue: unknown) => {
      sendCommand('valueChanged', { value: newValue });
    },
    [sendCommand]
  );

  return [state.value, setValue];
}

// --- MutationObserver for auto-unmount ---

function setupAutoUnmount(): void {
  const observer = new MutationObserver((mutations) => {
    for (const mutation of mutations) {
      for (const removed of mutation.removedNodes) {
        if (removed instanceof HTMLElement) {
          const id = removed.id;
          if (id && _mounts.has(id)) {
            unmount(id);
          }
          // Also check children.
          for (const [mountId] of _mounts) {
            if (removed.querySelector('#' + CSS.escape(mountId))) {
              unmount(mountId);
            }
          }
        }
      }
    }
  });

  observer.observe(document.body, { childList: true, subtree: true });
}

// Initialize auto-unmount when DOM is ready.
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', () => {
    setupAutoUnmount();
  });
} else {
  setupAutoUnmount();
}
