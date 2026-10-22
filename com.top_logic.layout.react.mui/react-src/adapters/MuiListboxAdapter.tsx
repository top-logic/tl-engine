import {
  React, useCloseOnOutsidePress, usePopover, useMergeRefs, createPortal, anchoredOverlayProps,
} from 'tl-react-bridge';
import type { ListboxPartProps } from 'tl-react-bridge';
import Paper from '@mui/material/Paper';
import List from '@mui/material/List';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemText from '@mui/material/ListItemText';
import type { SxProps, Theme } from '@mui/material/styles';

const { useCallback, useEffect, useMemo, useRef } = React;

/** The elevation of a list floating at an element in Material UI, the one of a menu. */
const LISTBOX_ELEVATION = 8;

/** The number of entries the list shows before it scrolls; a count, not a measure. */
const VISIBLE_ENTRIES = 8;

/**
 * The surface of the list: on the popover layer of TopLogic, above a dialog the list may stand in,
 * scrolling a list longer than the entries it shows.
 */
const PAPER_SX: SxProps<Theme> = {
  zIndex: 'var(--tl-layer-popover)',
  overflow: 'auto',
  maxHeight: theme => `calc(${VISIBLE_ENTRIES} * ${theme.spacing(4.5)} + ${theme.spacing(2)})`,
};

/** Keeps the focus where it is when the pointer presses into the list. */
function keepFocus(event: React.MouseEvent) {
  event.preventDefault();
}

/**
 * Draws the part `Listbox` ({@link ListboxPartProps}) with Material UI: a `Paper` placed at its
 * anchor by usePopover of 'tl-react-bridge' and portaled to the document body, holding a dense
 * `List` of the role `listbox` whose entries are `ListItemButton`s of the role `option`.
 *
 * <p>Mapping from the props to the MUI props:</p>
 * <ul>
 * <li>id, className and the passed-through attributes → the `List`;</li>
 * <li>entries → one `ListItemButton` each, with the ID of the entry and its label as
 *     `ListItemText`;</li>
 * <li>activeIndex → `selected` and `aria-selected="true"` of the active entry, scrolled into view;
 *     `aria-selected="false"` on every other entry;</li>
 * <li>onPick → the click of an entry, onActivate → the pointer entering it;</li>
 * <li>onDismiss → a press outside the list and its anchor (useCloseOnOutsidePress);</li>
 * <li>anchor → the position, below the anchor and at least as wide as it.</li>
 * </ul>
 *
 * <p>The entries are not in the tab order and a press into the list does not take the focus: the
 * element controlling the list keeps it. The surface carries {@link anchoredOverlayProps}, so that
 * the focus trap of a dialog around the anchor takes a press into it for one inside the dialog.</p>
 */
const MuiListboxAdapter: React.FC<ListboxPartProps> = ({
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
    <Paper
      ref={ref}
      elevation={LISTBOX_ELEVATION}
      style={{ ...style, minWidth }}
      sx={PAPER_SX}
      onMouseDown={keepFocus}
      {...anchoredOverlayProps}
    >
      <List id={id} role="listbox" dense className={className} {...attributes}>
        {entries.map((entry, index) => (
          <ListItemButton
            key={entry.id}
            id={entry.id}
            component="li"
            role="option"
            tabIndex={-1}
            selected={index === activeIndex}
            aria-selected={index === activeIndex}
            onClick={() => onPick(index)}
            onMouseEnter={onActivate ? () => onActivate(index) : undefined}
          >
            <ListItemText primary={entry.label} />
          </ListItemButton>
        ))}
      </List>
    </Paper>,
    document.body,
  );
};

export default MuiListboxAdapter;
