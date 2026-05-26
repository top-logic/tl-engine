import { React, useTLState, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * Placeholder for {@code <slot-content>} contributions that the server-side
 * {@code SlotRegistry} routes to this slot.
 *
 * State:
 * - slotName: string  (the slot's name attribute, for debugging)
 * - children: ChildDescriptor[]  (contributions routed to this slot, in registration order)
 *
 * Renders as an inline container. The visual layout is the responsibility of whichever
 * parent element contains this slot (e.g. the app-bar wraps it in a flex row).
 */
const TLSlot: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const children = (state.children as unknown[]) ?? [];

  return (
    <div id={controlId} className="tlSlot">
      {children.map((child, i) => (
        <TLChild key={i} control={child} />
      ))}
    </div>
  );
};

export default TLSlot;
