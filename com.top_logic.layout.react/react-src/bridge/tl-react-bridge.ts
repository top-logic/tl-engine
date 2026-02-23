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

/**
 * Mounts a React component into the given DOM element.
 */
export function mount(
  controlId: string,
  moduleName: string,
  initialState: Record<string, unknown>,
  windowName?: string
): void {
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
        const resp = await fetch('/react-api/command', {
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

// --- Auto-connect SSE on load ---

function initSSE(): void {
  const basePath = document.querySelector('base')?.getAttribute('href') ?? '';
  connect(basePath + 'react-api/events');
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

// Initialize when DOM is ready.
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', () => {
    initSSE();
    setupAutoUnmount();
  });
} else {
  initSSE();
  setupAutoUnmount();
}
