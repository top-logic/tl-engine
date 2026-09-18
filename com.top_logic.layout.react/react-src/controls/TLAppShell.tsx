import { React, useTLState, useTLCommand, TLChild, useFill, FillBarrier } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * Viewport width (px) at or below which the UI is treated as COMPACT.
 *
 * Must stay in sync with the `@media (max-width: 768px)` breakpoint in tlReactControls.css
 * (drawer/sidebar swap). TODO: promote both to a shared theme token.
 */
const COMPACT_MAX_WIDTH = 768;

/** The compact/regular viewport state, so a descendant (e.g. TLAppBar) can react to it. */
export const AppShellContext = React.createContext<{ compact: boolean }>({ compact: false });

/**
 * Application shell with header / content / footer layout and built-in snackbar.
 *
 * State:
 * - header:   ChildDescriptor | null  (optional, fixed height)
 * - notices:  ChildDescriptor | null  (optional, system-wide notices between header and content)
 * - content:  ChildDescriptor         (required, flex:1)
 * - footer:   ChildDescriptor | null  (optional, fixed height)
 * - snackbar: ChildDescriptor         (built-in notification service)
 *
 * Always fills its container - the shell spans the viewport, so the app bar stays put and
 * overflowing content scrolls inside the content region rather than moving the page. That region
 * is bounded by it and ends the fill chain.
 */
const TLAppShell: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const fillClass = useFill(true);
  const [compact, setCompact] = React.useState(false);

  // Report the viewport "display class" to the server once on mount and whenever the
  // responsive breakpoint is crossed, so adaptive controls can switch presentation. Also keep it
  // in AppShellContext, so a React descendant (e.g. the app bar) can react without a server
  // round-trip.
  React.useEffect(() => {
    const query = window.matchMedia(`(max-width: ${COMPACT_MAX_WIDTH}px)`);
    const report = (compact: boolean) => {
      sendCommand('reportDisplayClass', { displayClass: compact ? 'COMPACT' : 'REGULAR' });
      setCompact(compact);
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
    <AppShellContext.Provider value={{ compact }}>
      <div id={controlId} className={'tlAppShell ' + fillClass}>
        {header && (
          <div className="tlAppShell__header">
            <TLChild control={header} />
          </div>
        )}
        {notices && (
          <div className="tlAppShell__notices">
            <TLChild control={notices} />
          </div>
        )}
        <div className="tlAppShell__content">
          <FillBarrier>
            <TLChild control={content} />
          </FillBarrier>
        </div>
        {footer && (
          <div className="tlAppShell__footer">
            <TLChild control={footer} />
          </div>
        )}
        <TLChild control={snackbar} />
      </div>
    </AppShellContext.Provider>
  );
};

export default TLAppShell;
