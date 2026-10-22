import { React, anchoredOverlayProps, createPortal, useCloseOnOutsidePress, useMergeRefs, usePopover } from 'tl-react-bridge';
import type { ListboxPartProps } from 'tl-react-bridge';

const { useCallback, useEffect, useMemo, useRef } = React;

/** The classes of the surface: the popover of the design system with the inner layout of a select. */
const POPUP_CLASS = 'tl-popover tl-select__popup';

/** The classes of the list of the design system. */
const LIST_CLASS = 'tl-select__list tl-type-body';

/** The class of an entry of the list. */
const OPTION_CLASS = 'tl-select__option';

/** The classes, without the empty ones, joined. */
function classes(...names: (string | false | undefined)[]): string {
  return names.filter(Boolean).join(' ');
}

/** Keeps the focus where it is when the pointer presses into the list. */
function keepFocus(event: React.MouseEvent) {
  event.preventDefault();
}

/**
 * The TopLogic drawing of the part `Listbox` ({@link ListboxPartProps}): the list of options of
 * the select of the design system (tl-select__list, tl-select__option) on a popover surface
 * (tl-popover), placed at its anchor by usePopover and portaled to the document body.
 *
 * <p>The active entry is marked `data-tl-state="highlighted"`, which the design system draws as
 * the keyboard position of a select, and scrolled into view. A press into the list is kept from
 * taking the focus from the element controlling it; a press outside the list and its anchor
 * dismisses the list (useCloseOnOutsidePress).</p>
 */
const ListboxPart: React.FC<ListboxPartProps> = ({
  id, anchor, entries, activeIndex = -1, onPick, onActivate, onDismiss, className, ...attributes
}) => {
  const { setFloating, style } = usePopover({ open: true, anchor });
  const surfaceRef = useRef<HTMLDivElement | null>(null);
  const ref = useMergeRefs<HTMLDivElement>([surfaceRef, setFloating]);

  const anchorRef = useMemo(() => ({ current: anchor instanceof HTMLElement ? anchor : null }), [anchor]);
  const dismiss = useCallback(() => onDismiss?.(), [onDismiss]);
  useCloseOnOutsidePress(onDismiss !== undefined, [surfaceRef, anchorRef], dismiss);

  const activeId = activeIndex >= 0 ? entries[activeIndex]?.id : undefined;
  useEffect(() => {
    if (activeId === undefined) return;
    document.getElementById(activeId)?.scrollIntoView?.({ block: 'nearest' });
  }, [activeId]);

  const minWidth = anchor?.getBoundingClientRect().width;

  return createPortal(
    <div
      ref={ref}
      className={POPUP_CLASS}
      style={{ ...style, minWidth }}
      onMouseDown={keepFocus}
      {...anchoredOverlayProps}
    >
      <div id={id} role="listbox" className={classes(LIST_CLASS, className)} {...attributes}>
        {entries.map((entry, index) => (
          <div
            key={entry.id}
            id={entry.id}
            role="option"
            aria-selected={index === activeIndex}
            data-tl-state={index === activeIndex ? 'highlighted' : undefined}
            className={OPTION_CLASS}
            onClick={() => onPick(index)}
            onMouseEnter={onActivate ? () => onActivate(index) : undefined}
          >
            {entry.label}
          </div>
        ))}
      </div>
    </div>,
    document.body,
  );
};

export default ListboxPart;
