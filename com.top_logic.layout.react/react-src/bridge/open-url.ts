/**
 * Opens the addresses a command on the server computed for the user of this window.
 *
 * Listens for the OpenUrlEvent of the SSE stream: a command opens an address outside the
 * application - the homepage of the selected object, an e-mail to write - in a browser window of its
 * own, or in the window of the application, which then leaves its page.
 */
import { resolveAddress } from './address';
import { enqueueCommand, leavePageWhenAnswered } from './command-channel';
import { getApiBase } from './tl-react-bridge';

/** Data shape of the msgbuf-generated OpenUrlEvent. */
export interface OpenUrlEventData {
  /** The address to open, absolute or relative to the address of the page. */
  url: string;
  /** Whether the address opens in a window of its own; absent counts as `true`. */
  newWindow?: boolean;
}

/**
 * Command reporting an address the browser refused to open a window for, so that the server offers
 * it to the user as a link. Must match {@code ReactServlet#CMD_OPEN_URL_REFUSED}.
 */
export const CMD_OPEN_URL_REFUSED = 'openUrlRefused';

/**
 * Argument of {@link CMD_OPEN_URL_REFUSED} holding the refused address. Must match
 * {@code ReactServlet#ARG_REFUSED_URL}.
 */
export const ARG_URL = 'url';

/** Schemes whose address names a page the browser displays. */
const PAGE_SCHEMES = new Set(['http:', 'https:']);

/** Handles an OpenUrlEvent from SSE: opens the address. */
export function handleOpenUrl(event: OpenUrlEventData): void {
  const url = resolveAddress(event.url ?? '');
  if (url === null) {
    console.warn('[TLReact] Not an address to open:', event.url);
    return;
  }

  if (!PAGE_SCHEMES.has(new URL(url).protocol)) {
    // An e-mail to write, a number to call: the browser hands the address to another application,
    // and the page stays. A window opened for it would stay empty, and leaving the page would stop
    // a page that is still displayed.
    handOver(url);
    return;
  }

  if (event.newWindow === false) {
    // The address replaces the page, after the answer of the command that opened it, as a
    // navigation the server asks for does.
    leavePageWhenAnswered(url);
    return;
  }

  // The window is opened empty and cut off from this page before it loads the address, so that the
  // page it shows cannot reach the application through window.opener. Opening it with the
  // window feature noopener would do the same, but leaves no way to tell whether it was opened.
  const opened = window.open('', '_blank');
  if (opened === null) {
    // The browser opens a window only shortly after a click of the user; a command that took
    // longer, or a pop-up blocker, refuses it. The server offers the address as a link instead,
    // which the user's click opens.
    console.warn('[TLReact] The browser refused to open a window for', url);
    reportRefused(url);
    return;
  }
  opened.opener = null;
  opened.location.href = url;
}

/** Tells the server that the browser refused a window for the given address. */
function reportRefused(url: string): void {
  void enqueueCommand(getApiBase() + 'react-api/command', {
    controlId: '',
    command: CMD_OPEN_URL_REFUSED,
    windowName: document.body.dataset.windowName ?? '',
    arguments: { [ARG_URL]: url },
  });
}

/** Lets the browser open the given address the way it opens a link the user clicks. */
function handOver(url: string): void {
  const link = document.createElement('a');
  link.href = url;
  link.rel = 'noopener';
  link.style.display = 'none';
  document.body.appendChild(link);
  link.click();
  link.remove();
}
