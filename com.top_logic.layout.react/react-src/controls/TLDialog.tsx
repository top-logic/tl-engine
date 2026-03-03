import { React, useTLState, useTLCommand, TLChild, useI18N } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback, useEffect, useRef } = React;

const I18N_KEYS = {
  'js.dialog.close': 'Close',
};

/**
 * A modal dialog overlay.
 *
 * State:
 * - open: boolean
 * - title: string
 * - size: "small" | "medium" | "large"  (default: "medium")
 * - closeOnBackdrop: boolean  (default: true)
 * - actions: ChildDescriptor[]  (footer buttons)
 * - child: ChildDescriptor
 */
const TLDialog: React.FC<TLCellProps> = () => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);

  const open = state.open === true;
  const title = (state.title as string) ?? '';
  const size = (state.size as string) ?? 'medium';
  const closeOnBackdrop = state.closeOnBackdrop !== false;
  const actions = (state.actions as unknown[]) ?? [];
  const child = state.child;

  const dialogRef = useRef<HTMLDivElement>(null);

  const handleClose = useCallback(() => {
    sendCommand('close');
  }, [sendCommand]);

  const handleBackdropClick = useCallback((e: React.MouseEvent) => {
    if (closeOnBackdrop && e.target === e.currentTarget) {
      handleClose();
    }
  }, [closeOnBackdrop, handleClose]);

  // Escape key.
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

  // Focus trap: focus the dialog when it opens.
  useEffect(() => {
    if (open && dialogRef.current) {
      dialogRef.current.focus();
    }
  }, [open]);

  if (!open) return null;

  const titleId = 'tlDialog-title';

  return (
    <div className="tlDialog__backdrop" onClick={handleBackdropClick}>
      <div
        className={`tlDialog tlDialog--${size}`}
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        ref={dialogRef}
        tabIndex={-1}
      >
        <div className="tlDialog__header">
          <span className="tlDialog__title" id={titleId}>{title}</span>
          <button
            type="button"
            className="tlDialog__closeBtn"
            onClick={handleClose}
            title={i18n['js.dialog.close']}
          >
            <svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true">
              <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" strokeWidth="2"
                strokeLinecap="round" />
              <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" strokeWidth="2"
                strokeLinecap="round" />
            </svg>
          </button>
        </div>
        <div className="tlDialog__body">
          <TLChild control={child} />
        </div>
        {actions.length > 0 && (
          <div className="tlDialog__footer">
            {actions.map((action, i) => (
              <TLChild key={i} control={action} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default TLDialog;
