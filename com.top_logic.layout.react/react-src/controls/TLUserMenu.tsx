import { React, useTLState, useTLCommand } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * Shows the current user with a login (anonymous) or logout (authenticated) action.
 *
 * State:
 * - userName: string   (display name, or a "not logged in" label)
 * - loggedIn: boolean
 * - loginLabel / logoutLabel: string
 */
const TLUserMenu: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const loggedIn = state.loggedIn === true;
  const userName = (state.userName as string) ?? '';

  return (
    <div id={controlId} className="tlUserMenu">
      <span className="tlUserMenu__name">{userName}</span>
      {loggedIn ? (
        <button type="button" className="tlReactButton tlUserMenu__action"
          onClick={() => sendCommand('logout')}>
          {(state.logoutLabel as string) ?? 'Logout'}
        </button>
      ) : (
        <button type="button" className="tlReactButton tlUserMenu__action"
          onClick={() => sendCommand('openLogin')}>
          {(state.loginLabel as string) ?? 'Login'}
        </button>
      )}
    </div>
  );
};

export default TLUserMenu;
