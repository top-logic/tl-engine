import { React, TLChild, useTLCommand, activationProps, ActivationButton, ActivationOpenButton } from 'tl-react-bridge';
import type { ActivationAction, ActivationProps } from 'tl-react-bridge';
import { useRovingFocus } from './useRovingFocus';
import { useOptionSelection } from './option-selection';
import type { OptionProps } from './option-selection';

/**
 * Rendering of the children of a container that repeats them, and the selection among them.
 *
 * A container that displays a sequence of equally built children - a list of cards, a grid of
 * tiles - can wrap each of them in an element of its own, so that a stylesheet reaches the
 * individual item without the item's own control having to know it is repeated. The wrapper
 * carries the position of the item, which is what a staggered entrance animation needs; the
 * engine ships the position, not the animation.
 *
 * A container whose items each display an object is selectable (ReactLayoutControl.setItems): the
 * container is a list box, each item an option the user selects by a click (with Ctrl or Cmd
 * toggling, with Shift extending the selection), by a long press on a touch screen (toggling, where
 * several items may be selected), or by Enter or Space on the focused item (see
 * {@link useOptionSelection}). The arrow keys move the focus between the items (see
 * {@link useRovingFocus}).
 *
 * A container whose items each display an object can make them activatable
 * (ReactLayoutControl.setActivation): the user opens an item, and the server runs the activation
 * with its object. In a container that is not selectable, a click on the item opens it, and so do
 * Enter and Space on the item's hidden button; in a selectable one, a click selects the item, and a
 * double-click, Enter or the item's open button open it (see activationProps of the bridge). An item
 * without action cannot be opened.
 */

/**
 * CSS custom property on an item wrapper holding the 0-based position of the item within its
 * container.
 *
 * An application stylesheet composes a per-item value from it, e.g.
 * `animation-delay: calc(var(--item-index) * 60ms)`.
 *
 * Outside the design system's namespace (the `tl` prefix of its custom properties): it is data of the
 * engine for an application stylesheet, not a word of the package.
 */
export const ITEM_INDEX_PROPERTY = '--item-index';

/**
 * CSS class every item wrapper carries, next to the class the container is configured with.
 *
 * The design system draws it (layout.css): the wrapper passes the space it is given on to the item, and in a
 * selectable container it is the option the user presses.
 */
export const ITEM_CLASS = 'tl-item';

/** State key: the CSS class of the wrapper around each child (ReactLayoutControl.ITEM_CLASS). */
const STATE_ITEM_CLASS = 'itemClass';

/** State key: whether the items are selectable (ReactLayoutControl.SELECTABLE). */
const STATE_SELECTABLE = 'selectable';

/** State key: the client keys of the items' objects, aligned with the children (ReactLayoutControl.ITEM_KEYS). */
const STATE_ITEM_KEYS = 'itemKeys';

/** State key: the keys of the selected items (ReactLayoutControl.SELECTED). */
const STATE_SELECTED = 'selected';

/** State key: whether several items may be selected (ReactLayoutControl.MULTI_SELECT). */
const STATE_MULTI_SELECT = 'multiSelect';

/**
 * State key: the action of each item, aligned with the children, `null` for an item that cannot be
 * opened (ReactLayoutControl.ITEM_ACTIONS).
 */
const STATE_ITEM_ACTIONS = 'itemActions';

/** State key: whether each item of a selectable container shows an open button (ReactLayoutControl.ACTIVATION_BUTTON). */
const STATE_ACTIVATION_BUTTON = 'activationButton';

/** The command activating an item (ReactLayoutControl.CMD_ACTIVATE_ITEM). */
const CMD_ACTIVATE_ITEM = 'activateItem';

/** The command selecting an item (ReactLayoutControl.CMD_SELECT_ITEM). */
const CMD_SELECT_ITEM = 'selectItem';

/** The argument of {@link CMD_ACTIVATE_ITEM} naming the item (ActivateItemArguments.KEY). */
const ARG_ACTIVATE_KEY = 'key';

/** The argument of {@link CMD_SELECT_ITEM} naming the item (SelectItemArguments.KEY). */
const ARG_KEY = 'key';

/** The argument of {@link CMD_SELECT_ITEM} toggling the item (SelectionModifiers.TOGGLE). */
const ARG_TOGGLE = 'toggle';

/** The argument of {@link CMD_SELECT_ITEM} extending the selection (SelectionModifiers.RANGE). */
const ARG_RANGE = 'range';

/** Selects the item wrappers of a selectable container, evaluated on the container. */
const OPTION_SELECTOR = ':scope > [role="option"]';

/**
 * The wrapper a container puts around each child for a layout of its own, independent of the
 * configured item class - e.g. a column of a column layout.
 */
export interface ItemWrapper {
  /** The CSS class of the wrapper, in place of {@link ITEM_CLASS}. */
  className: string;

  /** The inline style of the wrapper of the item at the given position. */
  style?: (index: number) => React.CSSProperties;
}

/** Attributes and handlers a container spreads onto its root element. */
export interface ItemsContainerProps {
  ref: React.RefObject<HTMLDivElement | null>;
  role?: string;
  'aria-multiselectable'?: boolean;
  onKeyDown?: (event: React.KeyboardEvent) => void;
  onFocus?: (event: React.FocusEvent) => void;
}

/** The items of a container, as {@link useItems} prepares them. */
export interface Items {
  /** Attributes and handlers of the container's root element. */
  containerProps: ItemsContainerProps;

  /** The rendered children. */
  content: React.ReactNode;
}

/**
 * Renders the children of a container, each wrapped in an element carrying the configured item
 * class and {@link ITEM_INDEX_PROPERTY}, and makes them selectable where the container is.
 *
 * Without an item class, a container {@link ItemWrapper wrapper} and selection, the children are
 * rendered directly, without a wrapper.
 *
 * @param state
 *        The state of the container.
 * @param wrapper
 *        The wrapper the container needs for its layout, if any.
 */
export function useItems(state: Record<string, unknown>, wrapper?: ItemWrapper): Items {
  const sendCommand = useTLCommand();
  const containerRef = React.useRef<HTMLDivElement | null>(null);

  const children = (state.children as unknown[]) ?? [];
  const itemClass = (state[STATE_ITEM_CLASS] as string | null) ?? null;
  const selectable = state[STATE_SELECTABLE] === true;
  const itemKeys = (state[STATE_ITEM_KEYS] as string[] | undefined) ?? [];
  const selectedKeys = (state[STATE_SELECTED] as string[] | undefined) ?? [];
  const multiSelect = state[STATE_MULTI_SELECT] === true;

  const selected = React.useMemo(() => new Set(selectedKeys), [selectedKeys]);
  const firstSelected = selectable ? itemKeys.findIndex(key => selected.has(key)) : -1;
  const roving = useRovingFocus(containerRef, OPTION_SELECTOR, selectable ? children.length : 0, firstSelected);

  const select = React.useCallback(
    (key: string, toggle: boolean, range: boolean) =>
      sendCommand(CMD_SELECT_ITEM, { [ARG_KEY]: key, [ARG_TOGGLE]: toggle, [ARG_RANGE]: range }),
    [sendCommand],
  );

  const selection = useOptionSelection(containerRef, select, selectable && multiSelect);

  const itemActions = state[STATE_ITEM_ACTIONS] as (ActivationAction | null)[] | undefined;
  const activationButton = state[STATE_ACTIVATION_BUTTON] !== false;
  const activate = React.useCallback(
    (key: string) => sendCommand(CMD_ACTIVATE_ITEM, { [ARG_ACTIVATE_KEY]: key }),
    [sendCommand],
  );

  if (!selectable && itemActions) {
    // Activatable: a click on an item opens it.
    const content = children.map((child, i) => {
      const key = itemKeys[i];
      const action = key !== undefined ? itemActions[i] : null;
      const activation = activationProps(action, () => activate(key), 'click');
      return (
        <div
          key={i}
          {...activation}
          className={wrapperClass(wrapper, itemClass, activation.className ?? null)}
          style={wrapperStyle(wrapper, i)}
        >
          {action && <ActivationButton action={action} activate={() => activate(key)} />}
          <TLChild control={child} />
        </div>
      );
    });
    return { containerProps: { ref: containerRef }, content };
  }

  if (!selectable) {
    const content = !itemClass && !wrapper
      ? children.map((child, i) => <TLChild key={i} control={child} />)
      : children.map((child, i) => (
        <div
          key={i}
          className={wrapperClass(wrapper, itemClass)}
          style={wrapperStyle(wrapper, i)}
        >
          <TLChild control={child} />
        </div>
      ));
    return { containerProps: { ref: containerRef }, content };
  }

  const content = children.map((child, i) => {
    const key = itemKeys[i];
    const isSelected = key !== undefined && selected.has(key);
    const action = key !== undefined && itemActions ? itemActions[i] : null;
    // Selectable and activatable: a click selects, a double-click and Enter open the item.
    const activation = activationProps(action, () => activate(key), 'double');
    const check = isSelected && <span className="tl-item__check" aria-hidden="true" />;
    return (
      <div
        key={i}
        {...combine(activation, selection.optionProps(key, isSelected))}
        tabIndex={roving.tabIndexOf(i)}
        className={wrapperClass(wrapper, itemClass)}
        style={wrapperStyle(wrapper, i)}
      >
        <TLChild control={child} />
        {action && activationButton
          // The check stands beside the open button in the corner they share.
          ? <ActivationOpenButton action={action} activate={() => activate(key)}>{check}</ActivationOpenButton>
          : check}
      </div>
    );
  });

  return {
    containerProps: {
      ref: containerRef,
      role: 'listbox',
      'aria-multiselectable': multiSelect || undefined,
      onKeyDown: roving.onKeyDown,
      onFocus: roving.onFocus,
    },
    content,
  };
}

/**
 * The handlers of an activatable option and of its selection on one element: the activation sees a
 * key first, and a key it handled (Enter opening the option) does not select.
 */
function combine(activation: ActivationProps, option: OptionProps): ActivationProps & OptionProps {
  return {
    ...activation,
    ...option,
    onMouseDown: event => {
      activation.onMouseDown?.(event as React.MouseEvent<HTMLElement>);
      option.onMouseDown(event);
    },
    onKeyDown: event => {
      activation.onKeyDown?.(event as React.KeyboardEvent<HTMLElement>);
      if (!event.defaultPrevented) {
        option.onKeyDown(event);
      }
    },
  };
}

function wrapperClass(wrapper: ItemWrapper | undefined, ...classes: (string | null | false)[]): string {
  return [wrapper ? wrapper.className : ITEM_CLASS, ...classes].filter(Boolean).join(' ');
}

function wrapperStyle(wrapper: ItemWrapper | undefined, index: number): React.CSSProperties {
  return { [ITEM_INDEX_PROPERTY as string]: index, ...wrapper?.style?.(index) } as React.CSSProperties;
}
