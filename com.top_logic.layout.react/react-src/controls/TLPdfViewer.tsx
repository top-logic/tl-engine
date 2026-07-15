import { React, useTLState, useTLDataUrl, useI18N } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const I18N_KEYS = {
  'js.pdfViewer.title': 'PDF document',
  'js.pdfViewer.noDocument': 'No document available',
};

/**
 * Displays a PDF document inline using the bundled Mozilla PDF.js viewer.
 *
 * The PDF bytes are served by {@code ReactPdfViewerControl} from the {@code /react-api/data}
 * endpoint. Rather than relying on the browser's native PDF handling (which many browsers are
 * configured to download instead of render), the document is shown through {@code
 * html/pdfjs/web/viewer.html}, which renders to a canvas regardless of browser settings - the same
 * approach as the legacy {@code DisplayPDFControl}.
 *
 * The viewer's {@code file} parameter points back at the (same-origin, session-authenticated) data
 * endpoint; a {@code rev} parameter carries {@code dataRevision} so a replaced document is re-fetched
 * rather than served from cache.
 */
const TLPdfViewer: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const dataUrl = useTLDataUrl();

  const hasPdf = !!state.hasPdf;
  const dataRevision: number = (state.dataRevision as number) ?? 0;

  const t = useI18N(I18N_KEYS);

  // The data URL has the form "<contextPath>/react-api/data?controlId=...&windowName=...".
  // Derive the context path from it so the PDF.js viewer can be addressed without extra config.
  const apiMarker = 'react-api/';
  const markerIndex = dataUrl.indexOf(apiMarker);
  const contextBase = markerIndex >= 0 ? dataUrl.slice(0, markerIndex) : dataUrl;

  // Bust caches when the document is replaced so PDF.js loads the current revision.
  const fileUrl = dataUrl + '&rev=' + dataRevision;
  const viewerUrl = contextBase + 'html/pdfjs/web/viewer.html?file=' + encodeURIComponent(fileUrl);

  if (!hasPdf) {
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
        src={viewerUrl}
        title={t['js.pdfViewer.title']}
      />
    </div>
  );
};

export default TLPdfViewer;
