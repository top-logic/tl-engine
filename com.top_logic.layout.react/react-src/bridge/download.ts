/**
 * Saves the files the server hands to the user of this window.
 *
 * Listens for the DownloadEvent of the SSE stream: a command on the server - an export, a
 * generated document - prepared a file, and the client fetches it as a download, so that the
 * browser saves it under its name instead of displaying it.
 */
import { getApiBase } from './tl-react-bridge';

/** Data shape of the msgbuf-generated DownloadEvent. */
export interface DownloadEventData {
  /** The address of the file, relative to the context path. */
  url: string;
  /** The name the file is saved under. */
  fileName: string;
}

/** Handles a DownloadEvent from SSE: saves the prepared file. */
export function handleDownload(event: DownloadEventData): void {
  // A link carrying the download attribute makes the browser save the response; the server sends
  // it as attachment as well, so the file is saved also where the attribute is ignored. Navigating
  // to the address directly keeps the file out of the page's memory, however large it is.
  const link = document.createElement('a');
  link.href = getApiBase() + event.url;
  link.download = event.fileName;
  link.rel = 'noopener';
  link.style.display = 'none';
  document.body.appendChild(link);
  link.click();
  link.remove();
}
