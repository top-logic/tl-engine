import { React, useTLState, useTLCommand } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const TLCounter: React.FC<TLCellProps> = () => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const count = (state.count as number) ?? 0;
  const label = (state.label as string) ?? 'React Counter';

  return (
    <div className="tlCounter">
      <h3 className="tlCounter__title">{label}</h3>
      <div className="tlCounter__controls">
        <button className="tlCounter__button" onClick={() => sendCommand('decrement')}>
          &minus;
        </button>
        <span className="tlCounter__value">
          {count}
        </span>
        <button className="tlCounter__button" onClick={() => sendCommand('increment')}>
          +
        </button>
      </div>
      <p className="tlCounter__description">
        State is managed on the server. Each click dispatches a command via POST,
        and the updated count is pushed back via SSE.
      </p>
    </div>
  );
};

export default TLCounter;
