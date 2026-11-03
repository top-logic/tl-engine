import { React, useTLState, useTLCommand, TLChild, useFill, FillBarrier, rootClassName, useDisplayClass } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

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
 * scrolls inside the content region rather than moving the page. That region is bounded by it (tl-region) and ends
 * the fill chain: it stretches the content to its height.
 *
 * The shell reports the display class of the window (useDisplayClass) to the server, so adaptive controls can switch
 * presentation, and marks its root with it (data-tl-display). The components of the window read it themselves,
 * inside the shell and beside it (the dialogs).
 */
const TLAppShell: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const fillClass = useFill(true);
  const displayClass = useDisplayClass();

  // Report the display class to the server once on mount and whenever the breakpoint is crossed.
  React.useEffect(() => {
    sendCommand('reportDisplayClass', { displayClass: displayClass === 'compact' ? 'COMPACT' : 'REGULAR' });
  }, [sendCommand, displayClass]);

  const header = state.header as unknown;
  const notices = state.notices as unknown;
  const content = state.content as unknown;
  const footer = state.footer as unknown;
  const snackbar = state.snackbar as unknown;

  return (
    <div id={controlId} className={rootClassName(state, 'tl-app-shell', fillClass)} data-tl-display={displayClass}>
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
      <main className="tl-app-shell__content tl-region">
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
    </div>
  );
};

export default TLAppShell;
