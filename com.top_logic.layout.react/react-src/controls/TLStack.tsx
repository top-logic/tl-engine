import { React, useTLState, useFillHost, FillProvider, rootClassName, useFieldLabelProps } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { useItems } from './items';

/**
 * A flexbox container that arranges children with consistent spacing.
 *
 * State:
 * - direction: "column" | "row"  (default: "column")
 * - gap: "compact" | "default" | "loose"  (default: "default")
 * - align: "start" | "center" | "end" | "stretch"  (default: "stretch")
 * - justify: "start" | "center" | "end" | "space-between" | "space-around" | "space-evenly"
 *   (default: "start") - how the free space along the direction is distributed
 * - wrap: boolean  (default: false)
 * - growFirst: boolean  (default: false) — first child fills the main axis
 * - maxWidth: string - CSS length the stack is bounded to, centered in its container
 *   (default: null - the width the container offers)
 * - itemClass: string - CSS class of a wrapper around each child (default: null - no wrapper)
 * - children: ChildDescriptor[]
 * - selectable, itemKeys, selected, multiSelect: the selection among the items (see items.tsx); a
 *   selectable stack is a list box
 *
 * Takes part in the fill contract as a container: a stack hosting a filling child fills its own
 * container, so that the child's height resolves against a definite one.
 *
 * A stack in a form field's input slot - an input together with an adornment button, say - is
 * the field's input as a whole: a group named by the field's label (see FieldLabelContext), whose
 * label click focuses the first control inside taking a value.
 */
const TLStack: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const labelProps = useFieldLabelProps(controlId, controlId);
  const role = labelProps['aria-labelledby'] === undefined ? undefined : 'group';

  const direction = (state.direction as string) ?? 'column';
  const gap = (state.gap as string) ?? 'default';
  const align = (state.align as string) ?? 'stretch';
  const justify = (state.justify as string) ?? 'start';
  const wrap = state.wrap === true;
  const maxWidth = (state.maxWidth as string) ?? null;
  const growFirst = state.growFirst === true;
  const items = useItems(state);

  const [fillClass, fillHost] = useFillHost();

  const className = rootClassName(
    state,
    'tl-stack',
    `tl-stack--${direction}`,
    `tl-stack--gap-${gap}`,
    `tl-stack--align-${align}`,
    `tl-stack--justify-${justify}`,
    wrap && 'tl-stack--wrap',
    growFirst && 'tl-stack--grow-first',
    maxWidth && 'tl-bounded',
    fillClass,
  );

  // Only the bound itself is a length; that it is centered is the rule the class carries.
  const style: React.CSSProperties | undefined = maxWidth ? { maxWidth } : undefined;

  return (
    <FillProvider host={fillHost}>
      <div id={controlId} role={role} {...labelProps} {...items.containerProps} className={className} style={style}>
        {items.content}
      </div>
    </FillProvider>
  );
};

export default TLStack;
