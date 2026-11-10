import { React, useTLState, useTLCommand, useGuardedCommand, rootClassName } from 'tl-react-bridge';
import type { TLCellProps, MenuStateJson } from 'tl-react-bridge';
import { Menu, MenuItem, MenuHeader, MenuSeparator } from 'tl-react-bridge';

/** Command choosing an entry of the menu. Must match {@code ReactMenuControl#SELECT_ITEM_COMMAND}. */
const CMD_SELECT_ITEM = 'selectItem';

/** Argument of {@link CMD_SELECT_ITEM}: the ID of the entry chosen. Must match {@code MenuSelectItemArguments#ITEM_ID}. */
const ARG_ITEM_ID = 'itemId';

type MenuItemState = Partial<MenuStateJson.Entry> & Required<Pick<MenuStateJson.Entry, 'type' | 'id' | 'label'>>;

/**
 * A popup menu the server opens at an anchor element (anchorId) or a viewport point (anchorX/Y).
 *
 * <p>An entry is chosen through {@link useGuardedCommand}: choosing an entry again while the choice
 * of an earlier one still awaits its answer is ignored.</p>
 */
const TLMenu: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<MenuStateJson>>();
  const sendCommand = useTLCommand();
  const selectItem = useGuardedCommand();
  const open = state.open === true;
  const items = (state.items ?? []) as MenuItemState[];
  const anchor = state.anchorId
    ? document.getElementById(state.anchorId)
    : state.anchorX != null && state.anchorY != null ? { x: state.anchorX, y: state.anchorY } : null;
  const close = React.useCallback(() => sendCommand('close'), [sendCommand]);
  return (
    <Menu id={controlId} className={rootClassName(state)} open={open} anchor={anchor} onClose={close}>
      {items.map((item, i) => item.type === 'separator' ? <MenuSeparator key={i} />
        : item.type === 'header' ? <MenuHeader key={i} label={item.label} />
        : <MenuItem key={item.id} icon={item.icon} label={item.label} disabled={item.disabled} current={item.active}
            danger={item.tone === 'danger'} className={item.cssClasses} onSelect={() => selectItem(CMD_SELECT_ITEM, { [ARG_ITEM_ID]: item.id })} />)}
    </Menu>
  );
};

export default TLMenu;
