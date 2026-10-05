import {
  React, useTLState, useTLCommand, useCloseOnOutsidePress, useFocusTrap, usePopover, createPortal,
  rootClassName, ThemeIcon, anchoredOverlayProps,
} from 'tl-react-bridge';
import type { TLCellProps, MenuStateJson, PopoverAnchor } from 'tl-react-bridge';
import Paper from '@mui/material/Paper';
import MenuList from '@mui/material/MenuList';
import MenuItem from '@mui/material/MenuItem';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import ListSubheader from '@mui/material/ListSubheader';
import Divider from '@mui/material/Divider';
import type { SxProps, Theme } from '@mui/material/styles';

const { useCallback, useMemo, useRef } = React;

/** Command a menu sends when an item is chosen. */
const CMD_SELECT_ITEM = 'selectItem';

/** Argument of {@link CMD_SELECT_ITEM}: the ID of the chosen item. */
const ARG_ITEM_ID = 'itemId';

/** Command a menu sends when it is closed without choosing an item. */
const CMD_CLOSE = 'close';

/** The key closing the menu. */
const KEY_ESCAPE = 'Escape';

/** Size class of the design system for the icon of an item. */
const ICON_CLASS = 'tl-icon-sm';

/** The elevation of a menu in Material UI. */
const MENU_ELEVATION = 8;

/** An entry as the server describes it, always with its type, ID and label. */
type MenuEntry = Partial<MenuStateJson.Entry> & Required<Pick<MenuStateJson.Entry, 'type' | 'id' | 'label'>>;

/** The label of an item whose command is in force: strong, as in TLMenu. */
const ACTIVE_LABEL_PROPS = { sx: { fontWeight: 'fontWeightMedium' } } as const;

/** An item whose command destroys or discards what the user has. */
const DANGER_SX: SxProps<Theme> = { color: 'error.main', '& .MuiListItemIcon-root': { color: 'error.main' } };

/**
 * The space a menu keeps free towards the edge of the browser window: the gap to its anchor and the
 * padding by which the placement shifts it inside the browser window.
 */
const VIEWPORT_MARGIN = 16;

/**
 * The surface of the menu: on the popover layer of TopLogic, above a dialog the menu may be opened
 * in, scrolling a list longer than the space it has.
 */
const PAPER_SX: SxProps<Theme> = { zIndex: 'var(--tl-layer-popover)', overflow: 'auto', outline: 'none' };

/**
 * The height a menu at the given anchor can take: the space on the side of the anchor that has
 * more of it. The placement of the bridge opens the menu below its anchor and flips it above where
 * only that side has room for it.
 */
function availableHeight(anchor: PopoverAnchor): number {
  let top: number;
  let bottom: number;
  if (anchor instanceof Element) {
    const rect = anchor.getBoundingClientRect();
    top = rect.top;
    bottom = rect.bottom;
  } else {
    top = anchor.y;
    bottom = anchor.y;
  }
  return Math.max(window.innerHeight - bottom, top) - VIEWPORT_MARGIN;
}

/**
 * Renders the state of a TopLogic popup menu (module name `TLMenu`) with an MUI `Paper` holding
 * a `MenuList` with a `MenuItem` per item.
 *
 * <p>The menu keeps the behaviour of TLMenu with the bridge's means; MUI provides the look only.
 * MUI's `Menu` is not used, as its modal would catch the press beside the menu on an invisible
 * backdrop:</p>
 * <ul>
 * <li>the menu is placed as TLMenu is ({@link usePopover}): below its anchor, flipped above it
 *     where only that side has room, shifted to stay inside the viewport. It is at most as high as
 *     the space on the side with more room, and its list scrolls;</li>
 * <li>a press outside the menu closes it ({@link useCloseOnOutsidePress}) and reaches the element
 *     below, as with TLMenu. The trigger of the menu learns through `pressClosedSurface` of the
 *     bridge that the press has closed the menu, and does not open it again;</li>
 * <li>the focus moves to the first item when the menu opens and back to where it was when it
 *     closes ({@link useFocusTrap}); Tab keeps it inside the menu;</li>
 * <li>Escape closes the menu, as in TLMenu by the key handler of the menu, which keeps the key
 *     from the keyboard scopes around it (a window);</li>
 * <li>the `MenuList` moves the focus with the arrow keys, Home, End and the first letters of a
 *     label; Enter and Space choose the focused item.</li>
 * </ul>
 *
 * <p>Mapping from the control state to the MUI props:</p>
 * <ul>
 * <li>open → the menu is shown;</li>
 * <li>anchorId → the anchor is the element with that ID. Without anchorId, anchorX/anchorY → the
 *     anchor is that point of the viewport (a context menu at the pointer). A menu whose anchor is
 *     not in the page is not shown;</li>
 * <li>items → an entry per item: an item → `MenuItem` with the label, the icon in a
 *     `ListItemIcon` rendered by the bridge's {@link ThemeIcon}, disabled → `disabled`, active →
 *     `selected`, `aria-current` and a strong label, tone `danger` → the error color, cssClasses →
 *     className; a separator → `Divider`; a header → `ListSubheader`, which can neither be chosen
 *     nor focused;</li>
 * <li>choosing an item → the command `selectItem` with the argument `itemId`; the server closes
 *     the menu;</li>
 * <li>closing without a choice (Escape, a press outside) → the command `close`;</li>
 * <li>hidden → the menu is not shown; the configured CSS class → className of the `MenuList`,
 *     which carries the control ID.</li>
 * </ul>
 *
 * <p>The menu is portaled to the document body; its root carries {@link anchoredOverlayProps}, so
 * that the focus trap of a window around its trigger lets the focus into the menu.</p>
 *
 * <p>The contract has no submenus and no checkable items besides the active mark; the adapter
 * offers none.</p>
 */
const MuiMenuAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<MenuStateJson>>();
  const sendCommand = useTLCommand();

  const surfaceRef = useRef<HTMLDivElement | null>(null);

  const close = useCallback(() => {
    sendCommand(CMD_CLOSE);
  }, [sendCommand]);

  const anchorX = state.anchorX;
  const anchorY = state.anchorY;
  const point: PopoverAnchor | null = useMemo(
    () => anchorX != null && anchorY != null ? { x: anchorX, y: anchorY } : null,
    [anchorX, anchorY]);
  const anchor = state.anchorId ? document.getElementById(state.anchorId) : point;
  const open = state.open === true && state.hidden !== true && anchor !== null;

  const { setFloating, style } = usePopover({ open, anchor });
  const setSurfaceRef = useCallback((element: HTMLDivElement | null) => {
    surfaceRef.current = element;
    setFloating(element);
  }, [setFloating]);
  useCloseOnOutsidePress(open, [surfaceRef], close);
  useFocusTrap(open, surfaceRef, 'first');

  const handleKeyDown = useCallback((event: React.KeyboardEvent) => {
    if (event.key === KEY_ESCAPE) {
      event.preventDefault();
      close();
    }
  }, [close]);

  const items = (state.items ?? []) as MenuEntry[];

  // The item the focus moves to when the menu opens: the first that can be chosen, the only one
  // in the tab order (the list moves the focus between the items with the arrow keys).
  const firstItemId = items.find(item => item.type === 'item' && item.disabled !== true)?.id;

  if (!open) {
    return null;
  }

  return createPortal(
    <Paper
      ref={setSurfaceRef}
      elevation={MENU_ELEVATION}
      style={{ ...style, maxHeight: availableHeight(anchor!) }}
      sx={PAPER_SX}
      onKeyDown={handleKeyDown}
      {...anchoredOverlayProps}
    >
      <MenuList id={controlId} className={rootClassName(state)} variant="menu">
        {items.map((item, index) => {
          if (item.type === 'separator') {
            return <Divider key={index} />;
          }
          if (item.type === 'header') {
            return <ListSubheader key={index}>{item.label}</ListSubheader>;
          }
          const active = item.active === true;
          return (
            <MenuItem
              key={item.id}
              disabled={item.disabled === true}
              selected={active}
              tabIndex={item.id === firstItemId ? 0 : -1}
              aria-current={active ? 'true' : undefined}
              className={item.cssClasses}
              sx={item.tone === 'danger' ? DANGER_SX : undefined}
              onClick={() => sendCommand(CMD_SELECT_ITEM, { [ARG_ITEM_ID]: item.id })}
            >
              {item.icon && (
                <ListItemIcon><ThemeIcon encoded={item.icon} className={ICON_CLASS} /></ListItemIcon>
              )}
              <ListItemText slotProps={{ primary: active ? ACTIVE_LABEL_PROPS : undefined }}>
                {item.label}
              </ListItemText>
            </MenuItem>
          );
        })}
      </MenuList>
    </Paper>,
    document.body,
  );
};

export default MuiMenuAdapter;
