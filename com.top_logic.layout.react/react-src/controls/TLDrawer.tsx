import {
  React, useTLState, useTLCommand, TLChild, useI18N, useStandaloneKeyboardScope, FillBarrier,
  tooltipProps, firstFocusable,
} from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback, useEffect, useRef } = React;

const I18N_KEYS = {
  'js.drawer.close': 'Close',
};

/**
 * A slide-in panel from the edge of the viewport or of the container it is rendered in.
 *
 * The drawer is not modal: the content it overlays stays scrollable and clickable, so focus is
 * moved into the drawer when it opens and returned to its origin when it closes, but never
 * confined to it.
 *
 * State:
 * - open: boolean
 * - position: "left" | "right" | "bottom"  (default: "right")
 * - size: "narrow" | "medium" | "wide"  (default: "medium")
 * - anchor: "viewport" | "container"  (default: "viewport")
 * - width: number | null  - explicit extent in pixels, overriding the named size
 * - title: string | null  (null = no header)
 * - child: ChildDescriptor
 */
const TLDrawer: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);
  const rootRef = useRef<HTMLElement | null>(null);
  const restoreRef = useRef<HTMLElement | null>(null);

  const open = state.open === true;
  const position = (state.position as string) ?? 'right';
  const size = (state.size as string) ?? 'medium';
  const anchor = (state.anchor as string) ?? 'viewport';
  const width = typeof state.width === 'number' ? state.width : null;
  const title = (state.title as string) ?? null;
  const child = state.child;

  const handleClose = useCallback(() => {
    sendCommand('close');
  }, [sendCommand]);

  // Escape key closes the drawer (via the shared keyboard dispatcher).
  useStandaloneKeyboardScope(open, { ESCAPE: handleClose });

  // Opening hands the keyboard to the drawer, closing gives it back to wherever it came from
  // (typically the list the drawer details). Without a trap: what lies underneath stays reachable.
  useEffect(() => {
    const root = rootRef.current;
    if (!open || !root) {
      return undefined;
    }
    const previous = document.activeElement as HTMLElement | null;
    restoreRef.current = previous && previous !== document.body ? previous : null;
    const target = firstFocusable(root) ?? root;
    if (target !== document.activeElement) {
      // The drawer is still parked outside the clipped box of the area it is anchored in while it
      // slides in, so a scrolling focus would drag the whole area sideways to reveal it.
      target.focus({ preventScroll: true });
    }
    return () => {
      const back = restoreRef.current;
      restoreRef.current = null;
      if (back && back.isConnected && typeof back.focus === 'function') {
        back.focus();
      }
    };
  }, [open]);

  const className = [
    'tlDrawer',
    `tlDrawer--${position}`,
    `tlDrawer--${size}`,
    `tlDrawer--${anchor}`,
    open ? 'tlDrawer--open' : '',
  ].filter(Boolean).join(' ');

  // A bottom drawer's extent is its height, a side drawer's its width; the stylesheet caps both at
  // the extent of the area the drawer is anchored in.
  const style = width === null ? undefined
    : position === 'bottom' ? { height: `${width}px` } : { width: `${width}px` };

  return (
    <aside
      id={controlId}
      className={className}
      aria-hidden={!open}
      tabIndex={-1}
      ref={rootRef}
      style={style}
    >
      {title !== null && (
        <div className="tlDrawer__header">
          <span className="tlDrawer__title">{title}</span>
          <button
            type="button"
            className="tlDrawer__closeBtn"
            onClick={handleClose}
            aria-label={i18n['js.drawer.close']}
            {...tooltipProps(i18n['js.drawer.close'])}
          >
            <svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true">
              <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" strokeWidth="2"
                strokeLinecap="round" />
              <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" strokeWidth="2"
                strokeLinecap="round" />
            </svg>
          </button>
        </div>
      )}
      <div className="tlDrawer__body">
        <FillBarrier>
          {child && <TLChild control={child} />}
        </FillBarrier>
      </div>
    </aside>
  );
};

export default TLDrawer;
