import { React, isInteractiveWithin, ATTR_LONG_PRESS, LONG_PRESS_EVENT } from 'tl-react-bridge';

/**
 * The selection among the options of a list box - the items of a selectable container, the cards of
 * a Kanban column.
 *
 * The user selects an option by a click (with Ctrl or Cmd toggling, with Shift extending the
 * selection), by a long press on a touch screen (toggling, where several options may be selected),
 * or by Enter or Space on the focused option. A Shift-press does not start a text selection. A click
 * on an interactive element inside an option - a button, a link, the option of a nested list - is
 * that element's; the option itself, interactive by its role, is not counted (see
 * {@link isInteractiveWithin}).
 *
 * The options are the direct children of the container; the focus among them is not part of this
 * hook (see useRovingFocus).
 */

/** Attribute of an option holding the key of the object it displays. */
const ATTR_OPTION_KEY = 'data-tl-item';

/** Attributes and handlers an option spreads onto its element. */
export interface OptionProps {
  role: 'option';
  'aria-selected': boolean;
  /** The key of the option, see {@link ATTR_OPTION_KEY}. */
  'data-tl-item': string | undefined;
  /** Present where several options may be selected: a long press then toggles the option. */
  [ATTR_LONG_PRESS]?: '';
  onMouseDown: (event: React.MouseEvent) => void;
  onClick: (event: React.MouseEvent) => void;
  onKeyDown: (event: React.KeyboardEvent) => void;
}

/** The selection among the options of a list box, as {@link useOptionSelection} prepares it. */
export interface OptionSelection {
  /**
   * The attributes and handlers of the option with the given key.
   *
   * @param key The key of the object the option displays; `undefined` for an option without one,
   *        which is drawn as an option but cannot be selected.
   * @param selected Whether the option is selected.
   */
  optionProps(key: string | undefined, selected: boolean): OptionProps;
}

/**
 * Makes the options of a list box selectable.
 *
 * @param containerRef
 *        The list box, the parent of the options. It receives the long press of an option.
 * @param select
 *        Selects the option with the given key: `toggle` for Ctrl, Cmd or a long press, `range` for
 *        Shift - the command of the control.
 * @param multiSelect
 *        Whether several options may be selected; only then a long press toggles an option.
 */
export function useOptionSelection(
  containerRef: React.RefObject<HTMLElement | null>,
  select: (key: string, toggle: boolean, range: boolean) => void,
  multiSelect: boolean,
): OptionSelection {
  // A long press on an option toggles it. The event comes from the bridge's touch gesture.
  React.useEffect(() => {
    const container = containerRef.current;
    if (!container || !multiSelect) {
      return undefined;
    }
    const handleLongPress = (event: Event) => {
      const option = event.target as HTMLElement | null;
      const key = option?.getAttribute(ATTR_OPTION_KEY);
      if (key && option!.parentElement === container) {
        select(key, true, false);
      }
    };
    container.addEventListener(LONG_PRESS_EVENT, handleLongPress);
    return () => container.removeEventListener(LONG_PRESS_EVENT, handleLongPress);
  }, [containerRef, multiSelect, select]);

  const optionProps = React.useCallback((key: string | undefined, selected: boolean): OptionProps => {
    const onInner = (event: React.SyntheticEvent) => isInteractiveWithin(event.target, event.currentTarget);
    return {
      role: 'option',
      'aria-selected': selected,
      [ATTR_OPTION_KEY]: key,
      ...(multiSelect ? { [ATTR_LONG_PRESS]: '' as const } : {}),
      onMouseDown: event => {
        if (event.shiftKey && !onInner(event)) {
          // A Shift-click extends the selection, not a text selection.
          event.preventDefault();
        }
      },
      onClick: event => {
        if (key !== undefined && !onInner(event)) {
          select(key, event.ctrlKey || event.metaKey, event.shiftKey);
        }
      },
      onKeyDown: event => {
        if (key !== undefined && event.target === event.currentTarget
          && (event.key === 'Enter' || event.key === ' ')) {
          event.preventDefault();
          select(key, event.ctrlKey || event.metaKey, event.shiftKey);
        }
      },
    };
  }, [multiSelect, select]);

  return { optionProps };
}
