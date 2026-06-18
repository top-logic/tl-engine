import { React, useTLState, useTLCommand, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * Viewport width (px) at or below which the UI is treated as COMPACT.
 *
 * Must stay in sync with the `@media (max-width: 768px)` breakpoint in tlReactControls.css
 * (drawer/sidebar swap). TODO: promote both to a shared theme token.
 */
const COMPACT_MAX_WIDTH = 768;

/**
 * Application shell with header / content / footer layout and built-in snackbar.
 *
 * State:
 * - header:   ChildDescriptor | null  (optional, fixed height)
 * - content:  ChildDescriptor         (required, flex:1)
 * - footer:   ChildDescriptor | null  (optional, fixed height)
 * - snackbar: ChildDescriptor         (built-in notification service)
 * - dialogManager: ChildDescriptor   (built-in dialog manager)
 * - menuOverlay:  ChildDescriptor   (built-in menu overlay)
 */
const TLAppShell: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  // Report the viewport "display class" to the server once on mount and whenever the
  // responsive breakpoint is crossed, so adaptive controls can switch presentation.
  React.useEffect(() => {
    const query = window.matchMedia(`(max-width: ${COMPACT_MAX_WIDTH}px)`);
    const report = (compact: boolean) => {
      sendCommand('reportDisplayClass', { displayClass: compact ? 'COMPACT' : 'REGULAR' });
    };
    report(query.matches);
    const onChange = (e: MediaQueryListEvent) => report(e.matches);
    query.addEventListener('change', onChange);
    return () => query.removeEventListener('change', onChange);
  }, [sendCommand]);

  const header = state.header as unknown;
  const content = state.content as unknown;
  const footer = state.footer as unknown;
  const snackbar = state.snackbar as unknown;
  const dialogManager = state.dialogManager as unknown;
  const menuOverlay = state.menuOverlay as unknown;

  return (
    <div id={controlId} className="tlAppShell">
      {header && (
        <div className="tlAppShell__header">
          <TLChild control={header} />
        </div>
      )}
      <div className="tlAppShell__content">
        <TLChild control={content} />
      </div>
      {footer && (
        <div className="tlAppShell__footer">
          <TLChild control={footer} />
        </div>
      )}
      <TLChild control={snackbar} />
      {dialogManager && <TLChild control={dialogManager} />}
      {menuOverlay && <TLChild control={menuOverlay} />}
    </div>
  );
};

export default TLAppShell;
