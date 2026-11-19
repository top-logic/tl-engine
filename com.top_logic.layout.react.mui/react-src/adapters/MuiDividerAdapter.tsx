import { React } from 'tl-react-bridge';
import type { DividerPartProps, DividerLabelPosition } from 'tl-react-bridge';
import Divider from '@mui/material/Divider';

/** The alignment of the MUI `Divider` text for each {@link DividerLabelPosition}. */
const TEXT_ALIGN: Record<DividerLabelPosition, 'left' | 'center' | 'right'> = {
  start: 'left',
  center: 'center',
  end: 'right',
};

/**
 * Draws the part `Divider` ({@link DividerPartProps}) with the MUI `Divider`.
 *
 * <p>Mapping from the props: orientation → `orientation`; a vertical divider is a `flexItem`, which
 * stretches to the height of the row it stands in; label → the children of a horizontal divider;
 * labelPosition `start` / `center` / `end` → `textAlign` `left` / `center` / `right`; inset `start` →
 * `variant="inset"`; id, className and the passed-through attributes → the divider.</p>
 */
const MuiDividerAdapter: React.FC<DividerPartProps> = ({
  orientation, label, labelPosition, inset, className, ...attributes
}) => {
  const vertical = orientation === 'vertical';
  const text = vertical ? undefined : label || undefined;
  return (
    <Divider
      orientation={vertical ? 'vertical' : 'horizontal'}
      flexItem={vertical}
      variant={inset === 'start' ? 'inset' : 'fullWidth'}
      textAlign={text ? TEXT_ALIGN[labelPosition ?? 'center'] : undefined}
      className={className}
      {...attributes}
    >
      {text}
    </Divider>
  );
};

export default MuiDividerAdapter;
