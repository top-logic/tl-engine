/**
 * Saves data a control serves itself (its data endpoint) as a file of the user.
 *
 * Unlike the download of the SSE stream (download.ts), which lets the browser navigate to a prepared
 * file, the data is fetched into the page first: the address of a data endpoint carries the control
 * and window, and the response is no attachment.
 *
 * @param url The address of the data.
 * @param fileName The name the file is saved under.
 * @returns Whether the data was fetched and handed to the browser; `false` on an HTTP or network
 *          error, which is logged.
 */
export async function downloadBlob(url: string, fileName: string): Promise<boolean> {
  let blob: Blob;
  try {
    const resp = await fetch(url);
    if (!resp.ok) {
      console.error('[TLReact] Download failed:', resp.status);
      return false;
    }
    blob = await resp.blob();
  } catch (e) {
    console.error('[TLReact] Download error:', e);
    return false;
  }

  // A link carrying the download attribute makes the browser save the data under the file name; the
  // blob address is freed once the link was followed.
  const blobUrl = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = blobUrl;
  link.download = fileName;
  link.style.display = 'none';
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(blobUrl);
  return true;
}
