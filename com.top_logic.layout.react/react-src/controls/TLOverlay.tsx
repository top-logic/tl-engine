import { React, useTLState, TLChild, rootClassName } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

interface LayerDescriptor {
  control: unknown;
  anchor: string;
  cssClass: string | null;
}

/**
 * Stacks content over a base control.
 *
 * The base gives the overlay its height, the surrounding layout its width; every layer covers that
 * area and anchors its content to an edge, a corner or the middle of it. The free space of a layer
 * passes the pointer through to the base below.
 *
 * State:
 * - base: ChildDescriptor
 * - layers: { control: ChildDescriptor, anchor: string, cssClass: string | null }[]
 */
const TLOverlay: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();

  const base = state.base;
  const layers = (state.layers as LayerDescriptor[]) ?? [];

  return (
    <div id={controlId} className={rootClassName(state, 'tlOverlay')}>
      <div className="tlOverlay__base">
        <TLChild control={base} />
      </div>
      {layers.map((layer, i) => (
        <div
          key={i}
          className={['tlOverlay__layer', `tlOverlay__layer--${layer.anchor}`, layer.cssClass]
            .filter(Boolean)
            .join(' ')}
        >
          <TLChild control={layer.control} />
        </div>
      ))}
    </div>
  );
};

export default TLOverlay;
