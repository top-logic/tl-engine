import { React, useTLState, TLChild, rootClassName, useKeyedTransition } from 'tl-react-bridge';
import type { TLCellProps, ChildDescriptor } from 'tl-react-bridge';

const { useRef } = React;

/**
 * Renders the stack of open dialogs managed by the server-side dialog manager.
 *
 * State:
 * - dialogs: ChildDescriptor[]
 *
 * Opening and closing a dialog is a transition an application stylesheet owns: the scrim of a dialog
 * that opened carries `entering` in `data-tl-state` while it arrives, and a dialog that closed is held
 * on screen as an inert copy of its scrim marked `exiting` while it leaves; the design system
 * package animates both, an application may override it. The copy belongs here rather than to the
 * dialog, whose control is disposed on the server the moment it closes; this element outlives every
 * dialog it holds and is therefore where the copy is shown.
 */
const TLDialogManager: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const dialogs = (state.dialogs as ChildDescriptor[]) ?? [];
  const rootRef = useRef<HTMLDivElement | null>(null);

  useKeyedTransition({
    keys: dialogs.map((dialog) => dialog.controlId),
    // The scrim is the dialog's own root element, so it carries the control id.
    node: (key) => document.getElementById(key),
    container: () => rootRef.current,
    state: true,
  });

  return (
    <div id={controlId} ref={rootRef} className={rootClassName(state)}>
      {dialogs.map((dialog) => (
        <TLChild key={dialog.controlId} control={dialog} />
      ))}
    </div>
  );
};

export default TLDialogManager;
