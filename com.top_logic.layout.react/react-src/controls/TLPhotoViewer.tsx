import { React, useTLState, useTLDataUrl, useI18N } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const I18N_KEYS = {
  'js.photoViewer.alt': 'Captured photo',
};

const TLPhotoViewer: React.FC<TLCellProps> = () => {
  const state = useTLState();
  const dataUrl = useTLDataUrl();

  const hasPhoto = !!state.hasPhoto;
  const dataRevision: number = (state.dataRevision as number) ?? 0;

  const [imageUrl, setImageUrl] = React.useState<string | null>(null);
  const prevRevisionRef = React.useRef<number>(dataRevision);

  // Fetch image when hasPhoto becomes true or dataRevision changes.
  React.useEffect(() => {
    if (!hasPhoto) {
      // Release any previous object URL.
      if (imageUrl) {
        URL.revokeObjectURL(imageUrl);
        setImageUrl(null);
      }
      return;
    }

    if (dataRevision === prevRevisionRef.current && imageUrl) {
      // Same revision, already have image.
      return;
    }
    prevRevisionRef.current = dataRevision;

    // Revoke previous URL before fetching new one.
    if (imageUrl) {
      URL.revokeObjectURL(imageUrl);
      setImageUrl(null);
    }

    let cancelled = false;
    (async () => {
      try {
        const resp = await fetch(dataUrl);
        if (!resp.ok) {
          console.error('[TLPhotoViewer] Failed to fetch image:', resp.status);
          return;
        }
        const blob = await resp.blob();
        if (!cancelled) {
          setImageUrl(URL.createObjectURL(blob));
        }
      } catch (e) {
        console.error('[TLPhotoViewer] Fetch error:', e);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [hasPhoto, dataRevision, dataUrl]);

  // Cleanup object URL on unmount.
  React.useEffect(() => {
    return () => {
      if (imageUrl) {
        URL.revokeObjectURL(imageUrl);
      }
    };
  }, []);

  const t = useI18N(I18N_KEYS);

  if (!hasPhoto || !imageUrl) {
    return (
      <div className="tlPhotoViewer">
        <div className="tlPhotoViewer__placeholder" />
      </div>
    );
  }

  return (
    <div className="tlPhotoViewer">
      <img
        className="tlPhotoViewer__image"
        src={imageUrl}
        alt={t['js.photoViewer.alt']}
      />
    </div>
  );
};

export default TLPhotoViewer;
