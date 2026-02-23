import React from 'react';
import type { TLCellProps } from '../bridge/types';
import { getComponent } from '../bridge/registry';

interface ColumnDef {
  name: string;
  label: string;
  cellModule?: string;
}

/**
 * A model-driven table rendered via React.
 *
 * Expects state.columns (ColumnDef[]) and state.rows (Record<string, unknown>[]).
 * Each column may specify a cellModule to use a registered React component for rendering cells.
 */
const TLTable: React.FC<TLCellProps> = ({ controlId, state }) => {
  const columns = (state.columns as ColumnDef[]) ?? [];
  const rows = (state.rows as Record<string, unknown>[]) ?? [];

  return (
    <table className="tlReactTable">
      <thead>
        <tr>
          {columns.map((col) => (
            <th key={col.name}>{col.label}</th>
          ))}
        </tr>
      </thead>
      <tbody>
        {rows.map((row, rowIdx) => (
          <tr key={rowIdx}>
            {columns.map((col) => {
              const CellComponent = col.cellModule ? getComponent(col.cellModule) : undefined;
              const cellValue = row[col.name];

              if (CellComponent) {
                const cellState = { value: cellValue, editable: state.editable };
                return (
                  <td key={col.name}>
                    <CellComponent
                      controlId={controlId + '-' + rowIdx + '-' + col.name}
                      state={cellState}
                    />
                  </td>
                );
              }

              return (
                <td key={col.name}>
                  {cellValue != null ? String(cellValue) : ''}
                </td>
              );
            })}
          </tr>
        ))}
      </tbody>
    </table>
  );
};

export default TLTable;
