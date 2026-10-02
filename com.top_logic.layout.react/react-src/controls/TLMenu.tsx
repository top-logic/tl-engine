import { React, useTLState, useTLCommand, rootClassName } from 'tl-react-bridge';
import type { TLCellProps, MenuStateJson } from 'tl-react-bridge';
import { Menu, MenuItem, MenuHeader, MenuSeparator } from './menu/Menu';

type MenuItemState = Partial<MenuStateJson.Entry> & Required<Pick<MenuStateJson.Entry, 'type' | 'id' | 'label'>>;

/** A popup menu the server opens at an anchor element (anchorId) or a viewport point (anchorX/Y). */
const TLMenu: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<MenuStateJson>>();
  const sendCommand = useTLCommand();
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
            danger={item.tone === 'danger'} className={item.cssClasses} onSelect={() => sendCommand('selectItem', { itemId: item.id })} />)}
    </Menu>
  );
};

export default TLMenu;
