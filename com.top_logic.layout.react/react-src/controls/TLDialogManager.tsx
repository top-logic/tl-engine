import { React, useTLState, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useEffect, useRef } = React;

/**
 * Renders the stack of open dialogs managed by the server-side dialog manager.
 *
 * State:
 * - dialogs: ChildDescriptor[]
 */
const TLDialogManager: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const dialogs = (state.dialogs as unknown[]) ?? [];
  const prevCount = useRef(dialogs.length);

  // When the dialog count decreases, focus the topmost remaining dialog's backdrop.
  useEffect(() => {
    if (dialogs.length < prevCount.current && dialogs.length > 0) {
      // The topmost dialog just rendered; its backdrop should auto-focus via TLDialog.
      // Nothing extra needed here.
    }
    prevCount.current = dialogs.length;
  }, [dialogs.length]);

  if (dialogs.length === 0) return null;

  return (
    <div id={controlId} className="tlDialogManager">
      {dialogs.map((dialog, i) => (
        <TLChild key={i} control={dialog} />
      ))}
    </div>
  );
};

export default TLDialogManager;
