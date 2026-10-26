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
 * The deck pane generates no box of its own (tl-deck is `display: contents`): the child it shows is
 * laid out as if it stood in the deck pane's container itself - inline in an app bar or beside a
 * breadcrumb as wide and as high as its content, in a bounded region stretched to the region's
 * height. A CSS class configured on the deck pane therefore styles no box.
 *
 * Takes part in the fill contract as a container: the fill request of the child it shows passes
 * through to the deck pane's own container, so that a filling child makes that container fill.
 *
 * A deck pane in a form field's input slot - showing an input while the field is edited and a
 * display of its value while it is not, say - stands for the child it shows: the field's label
 * association (see FieldLabelContext) is handed on to that child, so the child's focusable element
 * is named by the field's label exactly as if the child were in the slot itself.
 */
const TLDeckPane: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const [, fillHost] = useFillHost();

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
      <div id={controlId} className={rootClassName(state, 'tl-deck')}>
        <FieldLabelContext.Provider value={childLabel}>
          {!!activeChild && <TLChild control={activeChild} />}
        </FieldLabelContext.Provider>
      </div>
    </FillProvider>
  );
};

export default TLDeckPane;
