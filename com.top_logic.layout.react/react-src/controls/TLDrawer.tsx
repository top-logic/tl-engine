import { React, useTLState, useTLCommand, TLChild, useI18N } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback, useEffect } = React;

const I18N_KEYS = {
  'js.drawer.close': 'Close',
};

/**
 * A slide-in panel from a screen edge.
 *
 * State:
 * - open: boolean
 * - position: "left" | "right" | "bottom"  (default: "right")
 * - size: "narrow" | "medium" | "wide"  (default: "medium")
 * - title: string | null  (null = no header)
 * - child: ChildDescriptor
 */
const TLDrawer: React.FC<TLCellProps> = () => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);

  const open = state.open === true;
  const position = (state.position as string) ?? 'right';
  const size = (state.size as string) ?? 'medium';
  const title = (state.title as string) ?? null;
  const child = state.child;

  const handleClose = useCallback(() => {
    sendCommand('close');
  }, [sendCommand]);

  // Escape key closes the drawer.
  useEffect(() => {
    if (!open) return;
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        handleClose();
      }
    };
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [open, handleClose]);

  const className = [
    'tlDrawer',
    `tlDrawer--${position}`,
    `tlDrawer--${size}`,
    open ? 'tlDrawer--open' : '',
  ].filter(Boolean).join(' ');

  return (
    <aside className={className} aria-hidden={!open}>
      {title !== null && (
        <div className="tlDrawer__header">
          <span className="tlDrawer__title">{title}</span>
          <button
            type="button"
            className="tlDrawer__closeBtn"
            onClick={handleClose}
            title={i18n['js.drawer.close']}
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
        {child && <TLChild control={child} />}
      </div>
    </aside>
  );
};

export default TLDrawer;
