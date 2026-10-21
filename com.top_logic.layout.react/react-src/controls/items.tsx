import { React, TLChild, useTLCommand, isInteractiveWithin, ATTR_LONG_PRESS, LONG_PRESS_EVENT } from 'tl-react-bridge';
import { useRovingFocus } from './useRovingFocus';

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
 * several items may be selected), or by Enter or Space on the focused item. The arrow keys move the
 * focus between the items (see {@link useRovingFocus}).
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

/** Attribute of an item wrapper in a selectable container holding the client key of its object. */
const ATTR_ITEM_KEY = 'data-tl-item';

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

/** The command selecting an item (ReactLayoutControl.CMD_SELECT_ITEM). */
const CMD_SELECT_ITEM = 'selectItem';

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

  // A long press on an item toggles it. The event comes from the bridge's touch gesture.
  const longPress = selectable && multiSelect;
  React.useEffect(() => {
    const container = containerRef.current;
    if (!container || !longPress) {
      return undefined;
    }
    const handleLongPress = (event: Event) => {
      const item = event.target as HTMLElement | null;
      const key = item?.getAttribute(ATTR_ITEM_KEY);
      if (key && item!.parentElement === container) {
        void select(key, true, false);
      }
    };
    container.addEventListener(LONG_PRESS_EVENT, handleLongPress);
    return () => container.removeEventListener(LONG_PRESS_EVENT, handleLongPress);
  }, [longPress, select]);

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
    // A click on an interactive element inside the item - a button, a link, the option of a nested
    // list - is that element's. The item is an option itself, so it is not counted.
    const onInner = (event: React.SyntheticEvent) => isInteractiveWithin(event.target, event.currentTarget);
    return (
      <div
        key={i}
        role="option"
        aria-selected={isSelected}
        tabIndex={roving.tabIndexOf(i)}
        {...{ [ATTR_ITEM_KEY]: key }}
        {...(multiSelect ? { [ATTR_LONG_PRESS]: '' } : {})}
        className={wrapperClass(wrapper, itemClass)}
        style={wrapperStyle(wrapper, i)}
        onMouseDown={event => {
          if (event.shiftKey && !onInner(event)) {
            // A Shift-click extends the selection, not a text selection.
            event.preventDefault();
          }
        }}
        onClick={event => {
          if (key !== undefined && !onInner(event)) {
            void select(key, event.ctrlKey || event.metaKey, event.shiftKey);
          }
        }}
        onKeyDown={event => {
          if (key !== undefined && event.target === event.currentTarget
            && (event.key === 'Enter' || event.key === ' ')) {
            event.preventDefault();
            void select(key, event.ctrlKey || event.metaKey, event.shiftKey);
          }
        }}
      >
        <TLChild control={child} />
        {isSelected && <span className="tl-item__check" aria-hidden="true" />}
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

function wrapperClass(wrapper: ItemWrapper | undefined, ...classes: (string | null | false)[]): string {
  return [wrapper ? wrapper.className : ITEM_CLASS, ...classes].filter(Boolean).join(' ');
}

function wrapperStyle(wrapper: ItemWrapper | undefined, index: number): React.CSSProperties {
  return { [ITEM_INDEX_PROPERTY as string]: index, ...wrapper?.style?.(index) } as React.CSSProperties;
}
