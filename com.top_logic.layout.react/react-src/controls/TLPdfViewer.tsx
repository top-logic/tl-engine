import { React, useTLState, useTLDataUrl, useI18N } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const I18N_KEYS = {
  'js.pdfViewer.title': 'PDF document',
  'js.pdfViewer.noDocument': 'No document available',
};

/**
 * Displays a PDF document inline using the browser's native PDF viewer.
 *
 * The PDF bytes are fetched from the {@code /react-api/data} endpoint (served by
 * {@code ReactPdfViewerControl}) as a {@code Blob} and shown in an {@code <iframe>}. The fetch is
 * repeated whenever the server increments {@code dataRevision}, mirroring {@code TLPhotoViewer}.
 */
const TLPdfViewer: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const dataUrl = useTLDataUrl();

  const hasPdf = !!state.hasPdf;
  const dataRevision: number = (state.dataRevision as number) ?? 0;

  const [objectUrl, setObjectUrl] = React.useState<string | null>(null);
  const prevRevisionRef = React.useRef<number>(dataRevision);

  // Fetch the PDF when it becomes available or its revision changes.
  React.useEffect(() => {
    if (!hasPdf) {
      if (objectUrl) {
        URL.revokeObjectURL(objectUrl);
        setObjectUrl(null);
      }
      return;
    }

    if (dataRevision === prevRevisionRef.current && objectUrl) {
      // Same revision, already have the document.
      return;
    }
    prevRevisionRef.current = dataRevision;

    if (objectUrl) {
      URL.revokeObjectURL(objectUrl);
      setObjectUrl(null);
    }

    let cancelled = false;
    (async () => {
      try {
        const resp = await fetch(dataUrl);
        if (!resp.ok) {
          console.error('[TLPdfViewer] Failed to fetch PDF:', resp.status);
          return;
        }
        const blob = await resp.blob();
        if (!cancelled) {
          setObjectUrl(URL.createObjectURL(blob));
        }
      } catch (e) {
        console.error('[TLPdfViewer] Fetch error:', e);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [hasPdf, dataRevision, dataUrl]);

  // Release the object URL on unmount.
  React.useEffect(() => {
    return () => {
      if (objectUrl) {
        URL.revokeObjectURL(objectUrl);
      }
    };
  }, []);

  const t = useI18N(I18N_KEYS);

  if (!hasPdf || !objectUrl) {
    return (
      <div id={controlId} className="tlPdfViewer">
        <div className="tlPdfViewer__placeholder">{t['js.pdfViewer.noDocument']}</div>
      </div>
    );
  }

  return (
    <div id={controlId} className="tlPdfViewer">
      <iframe
        className="tlPdfViewer__frame"
        src={objectUrl}
        title={t['js.pdfViewer.title']}
      />
    </div>
  );
};

export default TLPdfViewer;
