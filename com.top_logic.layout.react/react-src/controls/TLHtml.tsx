import { React, useTLState, useTLDataUrl, useI18N, useFill } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const I18N_KEYS = {
  'js.html.document': 'HTML document',
  'js.html.print': 'Print',
};

/**
 * Read-only control displaying HTML content the server has produced.
 *
 * State:
 * - display: string - how the content is shown; "inline" inserts it into the page around it,
 *   "document" shows it in a frame of its own, "thumbnail" shows that frame scaled down to a
 *   preview
 * - html: string - the fragment inserted into the page in "inline" display; empty in the other
 *   modes, where the content is fetched from the data endpoint instead of sent with the state
 * - error: string - message shown in place of content that cannot be displayed
 * - dataRevision: number - changes with every content, part of the frame URL so that a replaced
 *   document is fetched rather than taken from the browser cache
 * - print: boolean - whether a document is shown with a button that prints it
 * - thumbnailWidth, thumbnailHeight: number - the size in CSS pixels a thumbnail lays its page out
 *   at before scaling it down; their ratio is the ratio of the preview box
 * - cssClass: string - optional additional CSS class appended to the default "tlHtml" class
 *
 * An inline fragment is inserted as it stands. What may be inserted is decided on the server: the
 * html state carries only fragments that passed its check, a rejected one arrives as error instead.
 *
 * A document is not inserted into the page at all and does not travel with the state: the frame
 * fetches it from the control's data endpoint and keeps it isolated - it brings its own styles, takes none of the page's, and its
 * sandbox runs no script in it, which is why a document reaches the browser unchecked. The sandbox
 * keeps the frame same-origin so the page can hand it to the print dialog, which is also where the
 * browser offers saving the document as a PDF file.
 *
 * A thumbnail is the same isolated document as a picture of itself: the frame is laid out at the
 * page size the content is written for and scaled down to the width the box measures, so the
 * content is shown in its own proportions rather than reflowed into a small one. It is looked at
 * rather than used - it takes no clicks, no focus and no print button - and it is fetched only once
 * it comes near the viewport, so a grid of previews loads what the reader actually reaches.
 */
const TLHtml: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const dataUrl = useTLDataUrl();
  const t = useI18N(I18N_KEYS);

  const display = (state.display as string) || 'inline';
  const html = (state.html as string) ?? '';
  const error = (state.error as string) || null;
  const extra = (state.cssClass as string) ?? '';
  const dataRevision: number = (state.dataRevision as number) ?? 0;
  const print = state.print === true;
  const thumbnailWidth: number = (state.thumbnailWidth as number) || 800;
  const thumbnailHeight: number = (state.thumbnailHeight as number) || 1130;

  const frameRef = React.useRef<HTMLIFrameElement>(null);
  const boxRef = React.useRef<HTMLDivElement>(null);

  // The scale a thumbnail shows its page at: the width the box measures over the width the page is
  // laid out at. Measured before the first paint, so the page is never seen at its full size.
  const [boxWidth, setBoxWidth] = React.useState(0);

  React.useLayoutEffect(() => {
    const box = boxRef.current;
    if (!box) {
      return;
    }
    const measure = () => {
      const width = box.clientWidth;
      setBoxWidth((previous) => (previous === width ? previous : width));
    };
    measure();
    const observer = new ResizeObserver(measure);
    observer.observe(box);
    return () => observer.disconnect();
  }, [display]);

  // A document spans the height its container offers; content that flows with the page does not.
  const fillClass = useFill(display === 'document' && !error);

  const handlePrint = React.useCallback(() => {
    const frame = frameRef.current;
    const frameWindow = frame ? frame.contentWindow : null;
    if (!frameWindow) {
      return;
    }
    // Printing prints the focused frame, so the document has to be the one in hand.
    frameWindow.focus();
    frameWindow.print();
  }, []);

  if (error) {
    return (
      <div id={controlId} className="tlHtml tlHtml__error" role="alert">{error}</div>
    );
  }

  const className = ['tlHtml', `tlHtml--${display}`, fillClass, extra].filter(Boolean).join(' ');

  if (display === 'document') {
    return (
      <div id={controlId} className={className}>
        <iframe
          ref={frameRef}
          className="tlHtml__frame"
          // No "allow-scripts": a document brought along from elsewhere is displayed, not run.
          // "allow-same-origin" keeps it reachable for the print button, "allow-modals" lets the
          // print dialog open on it.
          sandbox="allow-same-origin allow-modals"
          src={dataUrl + '&rev=' + dataRevision}
          title={t['js.html.document']}
        />
        {print && (
          <button
            type="button"
            className="tl-button tl-button--ghost tl-type-label tlHtml__print"
            title={t['js.html.print']}
            aria-label={t['js.html.print']}
            onClick={handlePrint}
          >
            <i className="bi bi-printer" aria-hidden="true" />
          </button>
        )}
      </div>
    );
  }

  if (display === 'thumbnail') {
    return (
      <div
        id={controlId}
        ref={boxRef}
        className={className}
        style={{ aspectRatio: `${thumbnailWidth} / ${thumbnailHeight}` }}
      >
        <iframe
          className="tlHtml__page"
          // As for a document: no script runs in the frame, and same-origin keeps it readable.
          sandbox="allow-same-origin"
          // A preview is fetched once it comes near the viewport, not with the page holding it.
          loading="lazy"
          src={dataUrl + '&rev=' + dataRevision}
          title={t['js.html.document']}
          tabIndex={-1}
          aria-hidden="true"
          style={{
            width: thumbnailWidth + 'px',
            height: thumbnailHeight + 'px',
            transform: `scale(${boxWidth > 0 ? boxWidth / thumbnailWidth : 0})`,
            transformOrigin: 'top left',
          }}
        />
      </div>
    );
  }

  if (display !== 'inline') {
    return <div id={controlId} className={className}/>;
  }

  return (
    <div
      id={controlId}
      className={className}
      dangerouslySetInnerHTML={{ __html: html }}
    />
  );
};

export default TLHtml;
