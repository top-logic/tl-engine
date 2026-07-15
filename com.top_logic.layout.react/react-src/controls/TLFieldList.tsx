import { React, useTLState, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * Simple container component that renders a title and a vertical list of child controls.
 *
 * State shape:
 * - title: string - heading text
 * - fields: ChildDescriptor[] - list of child controls to render
 */
const TLFieldList: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const title = state.title as string;
  const fields = (state.fields as unknown[]) ?? [];

  return (
    <div id={controlId} className="tlFieldList">
      {title && <h3 className="tlFieldList__title">{title}</h3>}
      <div className="tlFieldList__fields">
        {fields.map((field, index) => (
          <div key={index} className="tlFieldList__item">
            <TLChild control={field} />
          </div>
        ))}
      </div>
    </div>
  );
};

export default TLFieldList;
