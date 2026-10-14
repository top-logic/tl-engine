import { useTLState, useTLDataUrl } from './tl-react-bridge';
import type { ImageSourceStateJson } from '../state/control-state';

/**
 * The address the picture of the surrounding control is loaded from, or null while it shows none.
 *
 * Reads the state described by ImageSourceStateJson: either a `url` naming the picture, or
 * `hasData` saying that the control serves the picture itself. In the latter case the address of
 * the data endpoint carries the `dataRevision`, so that a picture which was replaced is loaded
 * again instead of being taken from the browser cache.
 */
export function useImageSrc(): string | null {
  const state = useTLState<Partial<ImageSourceStateJson>>();
  const dataUrl = useTLDataUrl();

  if (state.hasData) {
    return dataUrl + '&rev=' + (state.dataRevision ?? 0);
  }

  const url = state.url;
  return url ? url : null;
}
