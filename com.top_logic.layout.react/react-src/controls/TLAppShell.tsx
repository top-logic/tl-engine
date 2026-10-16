import { React, useTLState, useTLCommand, TLChild, useFill, FillBarrier, rootClassName, DisplayClassProvider } from 'tl-react-bridge';
import type { TLCellProps, DisplayClass } from 'tl-react-bridge';

/**
 * Viewport width (px) at or below which the UI is COMPACT.
 *
 * The one place the breakpoint stands: the shell reports it to the server, gives it to the components inside
 * (useDisplayClass) and marks its root (data-tl-display). No stylesheet repeats it.
 */
const COMPACT_MAX_WIDTH = 768;

const QUERY = `(max-width: ${COMPACT_MAX_WIDTH}px)`;

/**
 * Application shell with header / notices / content / footer layout and built-in snackbar.
 *
 * State:
 * - header:   ChildDescriptor | null  (optional, fixed height)
 * - notices:  ChildDescriptor | null  (optional, system-wide notices between header and content)
 * - content:  ChildDescriptor         (required, fills the rest; the main landmark)
 * - footer:   ChildDescriptor | null  (optional, fixed height)
 * - snackbar: ChildDescriptor         (built-in notification service)
 *
 * Always fills its container - the shell spans the viewport, so the app bar stays put and overflowing content
 * scrolls inside the content region rather than moving the page. That region is bounded by it and ends the fill
 * chain.
 */
const TLAppShell: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const fillClass = useFill(true);
  const [displayClass, setDisplayClass] = React.useState<DisplayClass>(
    () => (window.matchMedia(QUERY).matches ? 'compact' : 'regular'));

  // Report the display class to the server once on mount and whenever the breakpoint is crossed, so adaptive
  // controls can switch presentation; the components inside read it from the context.
  React.useEffect(() => {
    const query = window.matchMedia(QUERY);
    const report = (compact: boolean) => {
      setDisplayClass(compact ? 'compact' : 'regular');
      sendCommand('reportDisplayClass', { displayClass: compact ? 'COMPACT' : 'REGULAR' });
    };
    report(query.matches);
    const onChange = (e: MediaQueryListEvent) => report(e.matches);
    query.addEventListener('change', onChange);
    return () => query.removeEventListener('change', onChange);
  }, [sendCommand]);

  const header = state.header as unknown;
  const notices = state.notices as unknown;
  const content = state.content as unknown;
  const footer = state.footer as unknown;
  const snackbar = state.snackbar as unknown;

  return (
    <div id={controlId} className={rootClassName(state, 'tl-app-shell', fillClass)} data-tl-display={displayClass}>
      <DisplayClassProvider value={displayClass}>
        {!!header && (
          <div className="tl-app-shell__header">
            <TLChild control={header} />
          </div>
        )}
        {!!notices && (
          <div className="tl-app-shell__notices">
            <TLChild control={notices} />
          </div>
        )}
        <main className="tl-app-shell__content">
          <FillBarrier>
            <TLChild control={content} />
          </FillBarrier>
        </main>
        {!!footer && (
          <div className="tl-app-shell__footer">
            <TLChild control={footer} />
          </div>
        )}
        <TLChild control={snackbar} />
      </DisplayClassProvider>
    </div>
  );
};

export default TLAppShell;
