import { React, useTLState, TLChild, useFillHost, FillProvider, rootClassName, FieldLabelContext } from 'tl-react-bridge';
import type { TLCellProps, ChildDescriptor } from 'tl-react-bridge';

/**
 * A deck pane that shows one child at a time, driven by a server-side active index.
 *
 * State:
 * - activeIndex: number
 * - activeChild: ChildDescriptor | null
 * - childCount: number
 *
 * Takes part in the fill contract as a container: a deck pane showing a filling child fills its
 * own container and offers that height to the child, while a deck pane showing content of its own
 * size takes exactly that size - so a deck standing inline, in an app bar or beside a breadcrumb,
 * is as wide and as high as the child it shows.
 *
 * A deck pane in a form field's input slot - showing an input while the field is edited and a
 * display of its value while it is not, say - stands for the child it shows: the field's label
 * association (see FieldLabelContext) is handed on to that child, so the child's focusable element
 * is named by the field's label exactly as if the child were in the slot itself.
 */
const TLDeckPane: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const [fillClass, fillHost] = useFillHost();

  const activeChild = (state.activeChild as ChildDescriptor | null | undefined) ?? null;
  const activeId = activeChild?.controlId;
  const label = React.useContext(FieldLabelContext);
  const childLabel = React.useMemo(
    () => (label !== null && label.controlId === controlId && activeId !== undefined
      ? { ...label, controlId: activeId } : label),
    [label, controlId, activeId]
  );

  return (
    <FillProvider host={fillHost}>
      <div id={controlId} className={rootClassName(state, 'tl-deck', fillClass)}>
        <FieldLabelContext.Provider value={childLabel}>
          {!!activeChild && <TLChild control={activeChild} />}
        </FieldLabelContext.Provider>
      </div>
    </FillProvider>
  );
};

export default TLDeckPane;
