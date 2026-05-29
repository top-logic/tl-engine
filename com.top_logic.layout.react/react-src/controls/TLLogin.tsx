import { React, useTLState, useTLCommand } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useState } = React;

interface LoginMethodDescriptor {
  id: string;
  label: string;
  url: string;
}

/**
 * The login dialog body: built-in username/password form plus any external login methods
 * (e.g. "Login with Google") contributed by the server.
 *
 * State:
 * - title, usernameLabel, passwordLabel, loginLabel: string
 * - errorMessage: string | undefined
 * - loginMethods: LoginMethodDescriptor[]
 */
const TLLogin: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');

  const methods = (state.loginMethods as LoginMethodDescriptor[]) ?? [];
  const errorMessage = state.errorMessage as string | undefined;

  const submit = (e: React.FormEvent) => {
    e.preventDefault();
    sendCommand('login', { username, password });
  };

  return (
    <div id={controlId} className="tlLogin">
      <h2 className="tlLogin__title">{(state.title as string) ?? 'Login'}</h2>

      <form className="tlLogin__form" onSubmit={submit}>
        <label className="tlLogin__field">
          <span className="tlLogin__label">{(state.usernameLabel as string) ?? 'User name'}</span>
          <input
            type="text"
            autoFocus
            autoComplete="username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
          />
        </label>

        <label className="tlLogin__field">
          <span className="tlLogin__label">{(state.passwordLabel as string) ?? 'Password'}</span>
          <input
            type="password"
            autoComplete="current-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </label>

        {errorMessage ? <div className="tlLogin__error" role="alert">{errorMessage}</div> : null}

        <button type="submit" className="tlReactButton tlLogin__submit">
          {(state.loginLabel as string) ?? 'Login'}
        </button>
      </form>

      {methods.length > 0 && (
        <div className="tlLogin__methods">
          {methods.map((method) => (
            <button
              key={method.id}
              type="button"
              className="tlReactButton tlLogin__method"
              onClick={() => window.location.assign(method.url)}
            >
              {method.label}
            </button>
          ))}
        </div>
      )}
    </div>
  );
};

export default TLLogin;
