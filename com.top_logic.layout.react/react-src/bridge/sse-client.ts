import type {
  ContentReplacementData,
  CssClassUpdateData,
  ElementReplacementData,
  FragmentInsertionData,
  FunctionCallData,
  JSSnippletData,
  PatchEventData,
  PropertyUpdateData,
  RangeReplacementData,
  StateEventData,
} from './types';
import { clearI18NCache } from './i18n';
import {
  handleWindowOpen,
  handleWindowClose,
  handleWindowFocus,
  type WindowOpenEventData,
  type WindowCloseEventData,
  type WindowFocusEventData,
} from './window-manager';

type StateListener = (state: Record<string, unknown>) => void;

const _listeners = new Map<string, Set<StateListener>>();
let _eventSource: EventSource | null = null;
let _url: string | null = null;
let _lastMessageTime: number = 0;
let _heartbeatCheckInterval: ReturnType<typeof setInterval> | null = null;

/** Must exceed the server-side heartbeat interval (30 s) to avoid false reconnects. */
const HEARTBEAT_TIMEOUT_MS = 45_000;

/** How often to check for missing heartbeats. */
const HEARTBEAT_CHECK_INTERVAL_MS = 15_000;

/**
 * Connects to the SSE endpoint. Should be called once on page load.
 */
export function connect(url: string): void {
  _url = url;

  if (_heartbeatCheckInterval) {
    clearInterval(_heartbeatCheckInterval);
  }

  createEventSource(url);

  _heartbeatCheckInterval = setInterval(() => {
    if (_lastMessageTime > 0 && Date.now() - _lastMessageTime > HEARTBEAT_TIMEOUT_MS) {
      console.warn('[TLReact] No heartbeat received, reconnecting SSE.');
      createEventSource(_url!);
    }
  }, HEARTBEAT_CHECK_INTERVAL_MS);
}

/**
 * Creates (or re-creates) the EventSource connection.
 */
function createEventSource(url: string): void {
  if (_eventSource) {
    _eventSource.close();
  }

  _eventSource = new EventSource(url);
  _lastMessageTime = Date.now();

  // Clear i18n cache on reconnect to ensure fresh translations.
  clearI18NCache();

  _eventSource.onmessage = (event: MessageEvent) => {
    _lastMessageTime = Date.now();
    try {
      const data = JSON.parse(event.data);
      dispatch(data);
    } catch (e) {
      console.error('[TLReact] Failed to parse SSE event:', e);
    }
  };

  _eventSource.onerror = () => {
    console.warn('[TLReact] SSE connection error, will reconnect automatically.');
  };
}

/**
 * Subscribes to state updates for a given control ID.
 */
export function subscribe(controlId: string, listener: StateListener): void {
  let set = _listeners.get(controlId);
  if (!set) {
    set = new Set();
    _listeners.set(controlId, set);
  }
  set.add(listener);
}

/**
 * Unsubscribes from state updates for a given control ID.
 */
export function unsubscribe(controlId: string, listener: StateListener): void {
  const set = _listeners.get(controlId);
  if (set) {
    set.delete(listener);
    if (set.size === 0) {
      _listeners.delete(controlId);
    }
  }
}

function notifyListeners(controlId: string, state: Record<string, unknown>): void {
  const set = _listeners.get(controlId);
  if (set) {
    for (const listener of set) {
      listener(state);
    }
  }
}

/**
 * Dispatches an SSE event based on its msgbuf type tag.
 *
 * msgbuf serializes polymorphic messages as [typeCode, { ...fields }].
 */
function dispatch(data: unknown): void {
  if (!Array.isArray(data) || data.length < 2) {
    console.warn('[TLReact] Unexpected SSE event format:', data);
    return;
  }

  const typeCode = data[0] as string;
  const payload = data[1] as Record<string, unknown>;

  if (typeCode !== 'Heartbeat') {
    console.log('[SSE] dispatch', typeCode, payload);
  }

  switch (typeCode) {
    case 'Heartbeat':
      // Server keep-alive, no action needed.
      break;
    case 'StateEvent':
      handleStateEvent(payload as unknown as StateEventData);
      break;
    case 'PatchEvent':
      handlePatchEvent(payload as unknown as PatchEventData);
      break;
    case 'ContentReplacement':
      DOMActionProcessor.contentReplacement(payload as unknown as ContentReplacementData);
      break;
    case 'ElementReplacement':
      DOMActionProcessor.elementReplacement(payload as unknown as ElementReplacementData);
      break;
    case 'PropertyUpdate':
      DOMActionProcessor.propertyUpdate(payload as unknown as PropertyUpdateData);
      break;
    case 'CssClassUpdate':
      DOMActionProcessor.cssClassUpdate(payload as unknown as CssClassUpdateData);
      break;
    case 'FragmentInsertion':
      DOMActionProcessor.fragmentInsertion(payload as unknown as FragmentInsertionData);
      break;
    case 'RangeReplacement':
      DOMActionProcessor.rangeReplacement(payload as unknown as RangeReplacementData);
      break;
    case 'JSSnipplet':
      DOMActionProcessor.jsSnipplet(payload as unknown as JSSnippletData);
      break;
    case 'FunctionCall':
      DOMActionProcessor.functionCall(payload as unknown as FunctionCallData);
      break;
    case 'I18NCacheInvalidation':
      clearI18NCache();
      break;
    case 'WindowOpenEvent':
      handleWindowOpen(payload as unknown as WindowOpenEventData);
      break;
    case 'WindowCloseEvent':
      handleWindowClose(payload as unknown as WindowCloseEventData);
      break;
    case 'WindowFocusEvent':
      handleWindowFocus(payload as unknown as WindowFocusEventData);
      break;
    default:
      console.warn('[TLReact] Unknown SSE event type:', typeCode);
  }
}

function handleStateEvent(data: StateEventData): void {
  const state = JSON.parse(data.state) as Record<string, unknown>;
  notifyListeners(data.controlId, state);
}

function handlePatchEvent(data: PatchEventData): void {
  const patch = JSON.parse(data.patch) as Record<string, unknown>;
  // The store in tl-react-bridge.ts handles merging, so we notify with the patch.
  // Listeners that use ControlStateStore will merge correctly.
  notifyListeners(data.controlId, patch);
}

/**
 * Processes DOM-level actions from the server (for non-React controls coexisting with React).
 */
export const DOMActionProcessor = {
  contentReplacement(data: ContentReplacementData): void {
    const el = document.getElementById(data.elementId);
    if (el) {
      el.innerHTML = data.html;
    }
  },

  elementReplacement(data: ElementReplacementData): void {
    const el = document.getElementById(data.elementId);
    if (el) {
      el.outerHTML = data.html;
    }
  },

  propertyUpdate(data: PropertyUpdateData): void {
    const el = document.getElementById(data.elementId);
    if (el) {
      for (const prop of data.properties) {
        el.setAttribute(prop.name, prop.value);
      }
    }
  },

  cssClassUpdate(data: CssClassUpdateData): void {
    const el = document.getElementById(data.elementId);
    if (el) {
      el.className = data.cssClass;
    }
  },

  fragmentInsertion(data: FragmentInsertionData): void {
    const el = document.getElementById(data.elementId);
    if (el) {
      el.insertAdjacentHTML(data.position as InsertPosition, data.html);
    }
  },

  rangeReplacement(data: RangeReplacementData): void {
    const startEl = document.getElementById(data.startId);
    const stopEl = document.getElementById(data.stopId);
    if (startEl && stopEl && startEl.parentNode) {
      const parent = startEl.parentNode;
      const range = document.createRange();
      range.setStartBefore(startEl);
      range.setEndAfter(stopEl);
      range.deleteContents();

      const template = document.createElement('template');
      template.innerHTML = data.html;
      parent.insertBefore(template.content, range.startContainer.childNodes[range.startOffset] || null);
    }
  },

  jsSnipplet(data: JSSnippletData): void {
    try {
      // eslint-disable-next-line no-eval
      (0, eval)(data.code);
    } catch (e) {
      console.error('[TLReact] Error executing JS snippet:', e);
    }
  },

  functionCall(data: FunctionCallData): void {
    try {
      const args = JSON.parse(data.arguments) as unknown[];
      const ref = data.functionRef
        ? (window as Record<string, unknown>)[data.functionRef] as Record<string, unknown>
        : window as unknown as Record<string, unknown>;
      const fn = ref?.[data.functionName];
      if (typeof fn === 'function') {
        fn.apply(ref, args);
      } else {
        console.warn('[TLReact] Function not found:', data.functionRef + '.' + data.functionName);
      }
    } catch (e) {
      console.error('[TLReact] Error executing function call:', e);
    }
  },
};
