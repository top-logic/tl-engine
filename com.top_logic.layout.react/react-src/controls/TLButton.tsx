import { React, useTLState, useTLCommand } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback } = React;

/**
 * A button rendered via React that sends a "click" command to the server.
 */
const TLButton: React.FC<TLCellProps> = () => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const handleClick = useCallback(() => {
    sendCommand('click');
  }, [sendCommand]);

  return (
    <button
      type="button"
      onClick={handleClick}
      disabled={state.disabled === true}
      className="tlReactButton"
      style={{ padding: '4px 8px', marginRight: '4px', cursor: 'pointer' }}
    >
      {state.label as string}
    </button>
  );
};

export default TLButton;
