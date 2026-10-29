import type {
  ContentReplacementData,
  CssClassUpdateData,
  ElementReplacementData,
  FragmentInsertionData,
  FunctionCallData,
  JSSnippletData,
  PageNavigationEventData,
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
import {
  handleRouteChangeEvent,
  handleRouteResumeEvent,
  type RouteChangeEventData,
  type RouteResumeEventData,
} from './route-sync';
import { handlePick, type PickEventData } from './element-picker';
import { handleDownload, type DownloadEventData } from './download';
import { afterNavigationGrace, isPageLeaving, leavePage, mayBeNavigating, onPageLeave, reloadForStaleUi } from './page-leave';

type StateListener = (state: Record<string, unknown>) => void;

const _listeners = new Map<string, Set<StateListener>>();
let _eventSource: EventSource | null = null;
let _url: string | null = null;
let _lastMessageTime: number = 0;
let _heartbeatCheckInterval: ReturnType<typeof setInterval> | null = null;
let _firstMessageTimer: ReturnType<typeof setTimeout> | null = null;
let _leaveListenerRegistered = false;

/**
 * Whether the events come by polling, because the stream does not arrive.
 *
 * A corporate proxy that inspects content (Zscaler, Netskope and others) may hold a
 * `text/event-stream` response back until it ends, forward it only in large blocks, or refuse it.
 * Every reaction of the page arrives as an event - a command's result, the login dialog, a
 * navigation - so behind such a proxy the page would not react at all. The server numbers the
 * events and keeps them until they are read; the client continues with `react-api/poll` after the
 * last number it saw. A page that polls keeps polling: the proxy is still there.
 */
let _polling = false;
/** The number of the last event received. */
let _lastSeq = 0;
/** What later polls send instead of the session cookie, so that polling does not keep the session alive. */
let _pollToken = '';

/** Must exceed the server-side heartbeat interval (15 s) to avoid false alarms. */
const HEARTBEAT_TIMEOUT_MS = 45_000;

/** How often to check for missing heartbeats. */
const HEARTBEAT_CHECK_INTERVAL_MS = 15_000;

/** A stream that brings no event within this after it was opened does not arrive. */
const FIRST_MESSAGE_TIMEOUT_MS = 3_000;

/** The longest wait between two failed polls. */
const MAX_POLL_BACKOFF_MS = 10_000;

/**
 * Type code of the event telling the page to leave. Must match
 * {@code PageNavigationEvent#PAGE_NAVIGATION_EVENT__TYPE}.
 */
const PAGE_NAVIGATION_EVENT = 'PageNavigationEvent';

/**
 * Session-storage key marking a tab that had to poll. The proxy between the browser and the server
 * does not change within a tab, so a reloaded page - the login reloads, for one - polls at once
 * instead of finding out again.
 */
const POLLING_KEY = 'tl-react-sse-polling';

/**
 * Connects to the SSE endpoint. Should be called once on page load.
 */
export function connect(url: string): void {
  if (isPageLeaving()) return;
  _url = url;

  if (!_leaveListenerRegistered) {
    _leaveListenerRegistered = true;
    onPageLeave(disconnect);
  }

  if (_heartbeatCheckInterval) {
    clearInterval(_heartbeatCheckInterval);
  }

  if (readStorage(POLLING_KEY)) {
    usePolling('this tab had to poll before');
    return;
  }

  createEventSource(url);

  _heartbeatCheckInterval = setInterval(() => {
    if (_lastMessageTime > 0 && Date.now() - _lastMessageTime > HEARTBEAT_TIMEOUT_MS) {
      // A stream that went silent is held back somewhere on the way; polling passes.
      usePolling('no heartbeat received');
    }
  }, HEARTBEAT_CHECK_INTERVAL_MS);
}

/**
 * Stops receiving events, because the page is leaving: the stream closing as a side effect of the
 * navigation must not be taken for a lost session, and a stream or poll opened now would reach the
 * server the navigation already changed.
 */
function disconnect(): void {
  if (_firstMessageTimer) {
    clearTimeout(_firstMessageTimer);
    _firstMessageTimer = null;
  }
  if (_heartbeatCheckInterval) {
    clearInterval(_heartbeatCheckInterval);
    _heartbeatCheckInterval = null;
  }
  if (_eventSource) {
    _eventSource.close();
    _eventSource = null;
  }
}

/**
 * Creates (or re-creates) the EventSource connection.
 */
function createEventSource(url: string): void {
  if (_eventSource) {
    _eventSource.close();
  }

  const source = new EventSource(url);
  _eventSource = source;
  _lastMessageTime = Date.now();

  // Clear i18n cache on reconnect to ensure fresh translations.
  clearI18NCache();

  if (_firstMessageTimer) clearTimeout(_firstMessageTimer);
  _firstMessageTimer = setTimeout(() => usePolling('the event stream brought nothing'), FIRST_MESSAGE_TIMEOUT_MS);

  source.onmessage = (event: MessageEvent) => {
    _lastMessageTime = Date.now();
    if (_firstMessageTimer) {
      clearTimeout(_firstMessageTimer);
      _firstMessageTimer = null;
    }
    // Only forward: the number the first heartbeat of a stream carries - the one its full state
    // stands for - may arrive after an event written to the stream at the same time, and a message
    // without a number of its own reports the last one again.
    const seq = Number(event.lastEventId);
    if (seq > _lastSeq) _lastSeq = seq;
    dispatchMessage(event.data);
  };

  source.onerror = () => {
    if (isPageLeaving()) {
      // The connection closing is expected; reloading here would abort the navigation.
      return;
    }
    if (mayBeNavigating()) {
      // The browser aborts the stream when a navigation it starts by itself begins (the reload
      // button), long before the page is left, and the server ends it when it renders the next
      // page: no reason to poll, and no reconnect that would take the stream from the next page. A
      // page that is still there afterwards - the user stayed on it - opens its stream again.
      source.close();
      afterNavigationGrace(() => {
        if (_eventSource === source && !_polling) createEventSource(_url!);
      });
      return;
    }
    if (source.readyState === EventSource.CLOSED) {
      // The stream was refused: the session is gone (401 after a server restart), or a proxy does
      // not let an event stream through. Polling tells the two apart - its first request answers
      // 401 or 410 for a lost session, which reloads the page then.
      usePolling('the event stream was refused');
      return;
    }
    console.warn('[TLReact] SSE connection error, will reconnect automatically.');
  };
}

function dispatchMessage(text: string): void {
  try {
    dispatch(JSON.parse(text));
  } catch (e) {
    console.error('[TLReact] Failed to parse SSE event:', e);
  }
}

/**
 * Gives up the event stream and receives the events by polling from now on.
 *
 * Not while a navigation may be starting ({@link mayBeNavigating}): a stream the navigation
 * aborted or held up says nothing about a proxy, and the tab would poll for good.
 */
function usePolling(reason: string): void {
  if (_polling || isPageLeaving() || mayBeNavigating()) return;
  _polling = true;
  console.warn('[TLReact] polling: ' + reason);
  writeStorage(POLLING_KEY, '1');
  if (_firstMessageTimer) {
    clearTimeout(_firstMessageTimer);
    _firstMessageTimer = null;
  }
  if (_heartbeatCheckInterval) {
    clearInterval(_heartbeatCheckInterval);
    _heartbeatCheckInterval = null;
  }
  if (_eventSource) {
    _eventSource.close();
    _eventSource = null;
  }
  void pollLoop();
}

interface PollAnswer {
  last: number;
  token: string;
  events: unknown[];
}

async function pollLoop(): Promise<void> {
  const base = _url!.replace('react-api/events', 'react-api/poll');
  // The first poll sends the cookie and asks for the full state: what the stream carried may not
  // have arrived either.
  let full = true;
  let backoff = 0;
  while (!isPageLeaving()) {
    if (mayBeNavigating()) {
      // The events from now on may be the next page's: the server keeps them until a poll reads
      // them, so a page that is still there after the navigation it announced reads them then.
      await new Promise<void>((resolve) => afterNavigationGrace(resolve));
      continue;
    }
    try {
      const withToken = !full && _pollToken !== '';
      const url = withToken
        ? base.replace(/\?.*$/, '') + '?token=' + encodeURIComponent(_pollToken) + '&after=' + _lastSeq
        : base + (base.includes('?') ? '&' : '?') + 'after=' + _lastSeq + (full ? '&full=true' : '');
      const response = await fetch(url, {
        cache: 'no-store',
        credentials: withToken ? 'omit' : 'same-origin',
      });
      if (response.status === 401 || response.status === 410) {
        // The session or the window is gone, as a closed stream used to say.
        reloadForLostSession();
        return;
      }
      if (response.status === 409) {
        // Events were dropped before they were read: ask for the full state with the cookie.
        full = true;
        continue;
      }
      if (!response.ok) throw new Error('poll failed (' + response.status + ')');
      const answer = (await response.json()) as PollAnswer;
      full = false;
      backoff = 0;
      if (answer.token) _pollToken = answer.token;
      _lastSeq = answer.last;
      for (const event of answer.events) {
        if (isPageLeaving()) return;
        dispatch(event);
      }
    } catch (e) {
      backoff = Math.min(backoff ? backoff * 2 : 1_000, MAX_POLL_BACKOFF_MS);
      console.warn('[TLReact] poll failed, trying again in ' + backoff + ' ms:', e);
      await new Promise((resolve) => setTimeout(resolve, backoff));
    }
  }
}

/** The scheduled or running check of the delivery, see {@link expectEvents}. */
let _deliveryCheck: ReturnType<typeof setTimeout> | null = null;

/** The highest event number a check has to see arrive, from the answers of commands. */
let _expectedSeq = 0;

/** Whether a check has to ask the server for its last event number. */
let _peekWanted = false;

/** How long the stream may take to bring the events the server has written. */
const DELIVERY_CHECK_MS = 3_000;

/**
 * Tells that the server has written events for this window - up to number `serverLast` if known -
 * and checks that the stream brings them.
 *
 * A proxy that forwards a response only in blocks of some kilobytes lets the start of the stream
 * through and then holds back every small event: the first-event check passes, and the page stops
 * reacting. So shortly after a command - whose answer names the last event the server wrote - the
 * client compares that number with the last event its stream brought. Without a number (a caller
 * outside the command channel, such as the chat sending a message), it asks the server for it
 * (`react-api/poll?peek=true`). When the stream is behind, also a second later, so that an event on
 * its way is not taken for a lost one, the client polls. What is expected while a check runs is
 * checked by the next one.
 */
export function expectEvents(serverLast?: number): void {
  if (_polling || !_url || isPageLeaving()) return;
  if (serverLast === undefined) {
    _peekWanted = true;
  } else if (serverLast > _lastSeq) {
    _expectedSeq = Math.max(_expectedSeq, serverLast);
  } else {
    return;
  }
  scheduleDeliveryCheck();
}

function scheduleDeliveryCheck(): void {
  if (_deliveryCheck) return;
  _deliveryCheck = setTimeout(() => void checkDelivery(), DELIVERY_CHECK_MS);
}

async function checkDelivery(): Promise<void> {
  let expected = _expectedSeq;
  const peek = _peekWanted;
  _expectedSeq = 0;
  _peekWanted = false;
  try {
    if (_polling || isPageLeaving()) return;
    if (peek) expected = Math.max(expected, await peekLastSeq());
    if (expected <= _lastSeq) return;
    await new Promise((resolve) => setTimeout(resolve, 1_000));
    if (!_polling && _lastSeq < expected) {
      usePolling('events the server wrote did not arrive');
    }
  } catch {
    // The server cannot be asked right now; the next command checks again.
  } finally {
    _deliveryCheck = null;
    if (!_polling && (_peekWanted || _expectedSeq > _lastSeq)) scheduleDeliveryCheck();
  }
}

async function peekLastSeq(): Promise<number> {
  const base = _url!.replace('react-api/events', 'react-api/poll');
  const response = await fetch(base + (base.includes('?') ? '&' : '?') + 'peek=true', { cache: 'no-store' });
  if (!response.ok) return 0;
  return ((await response.json()) as { last: number }).last;
}

/**
 * Reloads the page to log in again - but not in a loop: a page reloaded for this a moment ago
 * waits before it reloads again.
 */
function reloadForLostSession(): void {
  if (isPageLeaving()) return;
  // The server itself answered that the session or the window is gone, so no proxy held the
  // stream back: a tab reloaded for a lost session or window tries the stream first again.
  removeStorage(POLLING_KEY);
  reloadForStaleUi({ waitForGuard: true });
}

/** A value of the tab's session storage, `null` where there is none or no storage. */
function readStorage(key: string): string | null {
  try {
    return sessionStorage.getItem(key);
  } catch {
    return null;
  }
}

/** Writes to the tab's session storage, where there is one. */
function writeStorage(key: string, value: string): void {
  try {
    sessionStorage.setItem(key, value);
  } catch {
    // No storage: the next page finds out again.
  }
}

/** Removes a value from the tab's session storage, where there is one. */
function removeStorage(key: string): void {
  try {
    sessionStorage.removeItem(key);
  } catch {
    // No storage: there is nothing to remove.
  }
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
    case 'RouteChangeEvent':
      handleRouteChangeEvent(payload as unknown as RouteChangeEventData);
      break;
    case 'RouteResumeEvent':
      handleRouteResumeEvent(payload as unknown as RouteResumeEventData);
      break;
    case 'PickEvent':
      handlePick(payload as unknown as PickEventData);
      break;
    case 'DownloadEvent':
      handleDownload(payload as unknown as DownloadEventData);
      break;
    case PAGE_NAVIGATION_EVENT:
      leavePage((payload as unknown as PageNavigationEventData).url);
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
      // The global object addresses script objects by server-sent name.
      const globals: object = window;
      const globalsByName = globals as Record<string, unknown>;
      const ref = data.functionRef
        ? globalsByName[data.functionRef] as Record<string, unknown> | undefined
        : globalsByName;
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
