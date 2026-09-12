import { React, useTLState, useTLCommand } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback, useEffect, useRef, useState } = React;

/** Grace period in ms before the fade-out resumes once the mouse leaves the message. */
const FADEOUT_AFTER_HOVER_MS = 250;

/**
 * Transient notification message at bottom of screen.
 *
 * State:
 * - message: string
 * - content: string (HTML)
 * - variant: "info" | "success" | "warning" | "error"
 * - duration: number  (ms, 0 = sticky)
 * - visible: boolean
 * - generation: number
 *
 * Hovering the message pins it, so a long text stays readable for as long as the user needs;
 * leaving resumes the fade-out after a short grace period.
 */
const TLSnackbar: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const message = (state.message as string) ?? '';
  const content = (state.content as string) ?? '';
  const variant = (state.variant as string) ?? 'info';
  const duration = (state.duration as number) ?? 5000;
  const visible = state.visible === true;
  const generation = (state.generation as number) ?? 0;

  const [exiting, setExiting] = useState(false);
  const [hovered, setHovered] = useState(false);

  // Whether this message was hovered at least once: after the mouse leaves, it disappears after a
  // short grace period rather than starting its full display duration over again.
  const wasHovered = useRef(false);
  useEffect(() => {
    wasHovered.current = false;
  }, [generation]);

  const handleDismiss = useCallback(() => {
    setExiting(true);
    setTimeout(() => {
      sendCommand('dismiss', { generation });
      setExiting(false);
    }, 200); // match fade-out animation
  }, [sendCommand, generation]);

  // Auto-dismiss timer, suspended while the message is hovered.
  useEffect(() => {
    if (!visible || duration === 0 || hovered) return;
    const timer = setTimeout(handleDismiss, wasHovered.current ? FADEOUT_AFTER_HOVER_MS : duration);
    return () => clearTimeout(timer);
  }, [visible, duration, hovered, handleDismiss]);

  if (!visible && !exiting) return null;

  return (
    <div id={controlId} className={`tlSnackbar tlSnackbar--${variant}${exiting ? ' tlSnackbar--exiting' : ''}`}
      role="status" aria-live="polite"
      onMouseEnter={() => { wasHovered.current = true; setHovered(true); }}
      onMouseLeave={() => setHovered(false)}>
      {content
        ? <span className="tlSnackbar__message" dangerouslySetInnerHTML={{ __html: content }} />
        : <span className="tlSnackbar__message">{message}</span>
      }
    </div>
  );
};

export default TLSnackbar;
