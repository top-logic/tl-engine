import { React } from 'tl-react-bridge';
import type { DividerPartProps } from 'tl-react-bridge';
import Divider from '@mui/material/Divider';

/**
 * Draws the part `Divider` ({@link DividerPartProps}) with the MUI `Divider`.
 *
 * <p>Mapping from the props: orientation → `orientation`; a vertical divider is a `flexItem`, which
 * stretches to the height of the row it stands in; className and the passed-through attributes → the
 * divider.</p>
 */
const MuiDividerAdapter: React.FC<DividerPartProps> = ({ orientation, className, ...attributes }) => {
  const vertical = orientation === 'vertical';
  return (
    <Divider
      orientation={vertical ? 'vertical' : 'horizontal'}
      flexItem={vertical}
      className={className}
      {...attributes}
    />
  );
};

export default MuiDividerAdapter;
