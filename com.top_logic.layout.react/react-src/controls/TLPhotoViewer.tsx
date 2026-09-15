import { React, useTLState, useTLDataUrl, useI18N } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const I18N_KEYS = {
  'js.photoViewer.alt': 'Captured photo',
};

/**
 * Displays an image inline.
 *
 * The image bytes are served by {@code ReactPhotoViewerControl} from the {@code /react-api/data}
 * endpoint. They are fetched into a blob and shown through an object URL, so that the browser
 * neither re-requests nor caches the session-authenticated endpoint on its own.
 *
 * A fetch belongs to the {@code dataRevision} that started it: the effect's cleanup aborts a
 * request still in flight, which happens both when the image is replaced and when the viewer is
 * removed from the page - a control that is gone must not ask the server for its data any more. An
 * aborted request is the expected outcome of that cleanup and is reported nowhere.
 *
 * The object URL of a displayed image is released as soon as another one takes its place, and when
 * the viewer goes away.
 */
const TLPhotoViewer: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const dataUrl = useTLDataUrl();

  const hasPhoto = !!state.hasPhoto;
  const dataRevision: number = (state.dataRevision as number) ?? 0;

  const [imageUrl, setImageUrl] = React.useState<string | null>(null);

  // Fetch the image the current revision denotes.
  React.useEffect(() => {
    if (!hasPhoto) {
      setImageUrl(null);
      return undefined;
    }

    const controller = new AbortController();
    const signal = controller.signal;

    const isAborted = (e: unknown) =>
      signal.aborted || (e as { name?: string } | null)?.name === 'AbortError';

    (async () => {
      try {
        const resp = await fetch(dataUrl, { signal });
        if (!resp.ok) {
          if (!signal.aborted) {
            console.error('[TLPhotoViewer] Failed to fetch image:', resp.status);
          }
          return;
        }
        const blob = await resp.blob();
        if (signal.aborted) {
          return;
        }
        setImageUrl(URL.createObjectURL(blob));
      } catch (e) {
        if (!isAborted(e)) {
          console.error('[TLPhotoViewer] Fetch error:', e);
        }
      }
    })();

    return () => controller.abort();
  }, [hasPhoto, dataRevision, dataUrl]);

  // Release an object URL that is replaced, and the last one when the viewer goes away.
  React.useEffect(() => {
    return () => {
      if (imageUrl) {
        URL.revokeObjectURL(imageUrl);
      }
    };
  }, [imageUrl]);

  const t = useI18N(I18N_KEYS);

  if (!hasPhoto || !imageUrl) {
    return (
      <div id={controlId} className="tlPhotoViewer">
        <div className="tlPhotoViewer__placeholder" />
      </div>
    );
  }

  return (
    <div id={controlId} className="tlPhotoViewer">
      <img
        className="tlPhotoViewer__image"
        src={imageUrl}
        alt={(state.alt as string | undefined) || t['js.photoViewer.alt']}
      />
    </div>
  );
};

export default TLPhotoViewer;
