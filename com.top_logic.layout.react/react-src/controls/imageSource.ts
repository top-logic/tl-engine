import { useTLState, useTLDataUrl } from 'tl-react-bridge';

/**
 * The address the picture of the surrounding control is loaded from, or null while it shows none.
 *
 * Reads the state an `ImageSource` publishes: either a `url` naming the picture, or `hasData`
 * saying that the control serves the picture itself. In the latter case the address of the data
 * endpoint carries the `dataRevision`, so that a picture which was replaced is loaded again instead
 * of being taken from the browser cache.
 */
export function useImageSrc(): string | null {
  const state = useTLState();
  const dataUrl = useTLDataUrl();

  if (state.hasData) {
    return dataUrl + '&rev=' + ((state.dataRevision as number) ?? 0);
  }

  const url = state.url as string | null | undefined;
  return url ? url : null;
}
