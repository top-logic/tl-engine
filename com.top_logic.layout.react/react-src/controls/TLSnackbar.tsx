import { React, useTLState, useTLCommand } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback, useEffect, useState } = React;

/**
 * Transient notification message at bottom of screen.
 *
 * State:
 * - message: string
 * - variant: "info" | "success" | "warning" | "error"
 * - action: { label, commandName } | null
 * - duration: number  (ms, 0 = sticky)
 * - visible: boolean
 */
const TLSnackbar: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const message = (state.message as string) ?? '';
  const content = (state.content as string) ?? '';
  const variant = (state.variant as string) ?? 'info';
  const action = state.action as { label: string; commandName: string } | null;
  const duration = (state.duration as number) ?? 5000;
  const visible = state.visible === true;
  const generation = (state.generation as number) ?? 0;

  const [exiting, setExiting] = useState(false);

  const handleDismiss = useCallback(() => {
    setExiting(true);
    setTimeout(() => {
      sendCommand('dismiss', { generation });
      setExiting(false);
    }, 200); // match fade-out animation
  }, [sendCommand, generation]);

  const handleAction = useCallback(() => {
    if (action) {
      sendCommand(action.commandName);
    }
    handleDismiss();
  }, [sendCommand, action, handleDismiss]);

  // Auto-dismiss timer.
  useEffect(() => {
    if (!visible || duration === 0) return;
    const timer = setTimeout(handleDismiss, duration);
    return () => clearTimeout(timer);
  }, [visible, duration, handleDismiss]);

  console.log('[TLSnackbar] render', { visible, exiting, generation, content, message });

  if (!visible && !exiting) return null;

  return (
    <div id={controlId} className={`tlSnackbar tlSnackbar--${variant}${exiting ? ' tlSnackbar--exiting' : ''}`}
      role="status" aria-live="polite">
      {content
        ? <span className="tlSnackbar__message" dangerouslySetInnerHTML={{ __html: content }} />
        : <span className="tlSnackbar__message">{message}</span>
      }
      {action && (
        <button type="button" className="tlSnackbar__action" onClick={handleAction}>
          {action.label}
        </button>
      )}
    </div>
  );
};

export default TLSnackbar;
