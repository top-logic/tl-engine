import { React } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * Renders a list of "Wert 1", "Wert 2", ... "Wert N" entries,
 * where N comes from the server state.
 */
const TLValueList: React.FC<TLCellProps> = ({ controlId, state }) => {
  const count = (state.count as number) ?? 0;
  const title = (state.title as string) ?? '';

  const rows = [];
  for (let i = 1; i <= count; i++) {
    rows.push(
      <tr key={i}>
        <td style={{ padding: '4px 12px', borderBottom: '1px solid #e2e8f0' }}>
          {i}
        </td>
        <td style={{ padding: '4px 12px', borderBottom: '1px solid #e2e8f0' }}>
          Wert {i}
        </td>
      </tr>
    );
  }

  return (
    <div id={controlId} style={{ padding: '0.5rem' }}>
      {title && <h4 style={{ margin: '0 0 0.5rem 0' }}>{title}</h4>}
      {count > 0 ? (
        <table style={{ borderCollapse: 'collapse', width: '100%' }}>
          <thead>
            <tr>
              <th style={{ padding: '4px 12px', borderBottom: '2px solid #cbd5e1', textAlign: 'left' }}>#</th>
              <th style={{ padding: '4px 12px', borderBottom: '2px solid #cbd5e1', textAlign: 'left' }}>Eintrag</th>
            </tr>
          </thead>
          <tbody>{rows}</tbody>
        </table>
      ) : (
        <p style={{ color: '#64748b', fontStyle: 'italic' }}>Keine Eintr&auml;ge</p>
      )}
    </div>
  );
};

export default TLValueList;
