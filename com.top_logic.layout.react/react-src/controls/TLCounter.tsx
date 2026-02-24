import { React, useTLState, useTLCommand } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const TLCounter: React.FC<TLCellProps> = () => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const count = (state.count as number) ?? 0;

  return (
    <div style={{ padding: '24px', fontFamily: 'sans-serif' }}>
      <h3 style={{ margin: '0 0 16px 0' }}>React Counter</h3>
      <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
        <button
          onClick={() => sendCommand('decrement')}
          style={{
            width: '40px', height: '40px', fontSize: '20px',
            cursor: 'pointer', borderRadius: '4px', border: '1px solid #ccc',
          }}
        >
          &minus;
        </button>
        <span style={{ fontSize: '32px', minWidth: '60px', textAlign: 'center' }}>
          {count}
        </span>
        <button
          onClick={() => sendCommand('increment')}
          style={{
            width: '40px', height: '40px', fontSize: '20px',
            cursor: 'pointer', borderRadius: '4px', border: '1px solid #ccc',
          }}
        >
          +
        </button>
      </div>
      <p style={{ marginTop: '16px', color: '#666', fontSize: '13px' }}>
        State is managed on the server. Each click dispatches a command via POST,
        and the updated count is pushed back via SSE.
      </p>
    </div>
  );
};

export default TLCounter;
