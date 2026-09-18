import { React, useTLState } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * Read-only control displaying an HTML fragment the server has produced and checked.
 *
 * State:
 * - display: string - how the fragment is shown; "inline" inserts it into the page around it
 * - html: string - the fragment to display
 * - error: string - message shown in place of a fragment that cannot be displayed
 * - cssClass: string - optional additional CSS class appended to the default "tlHtml" class
 *
 * The fragment is inserted as it stands. What may be inserted is decided on the server: the html
 * state carries only fragments that passed its check, a rejected one arrives as error instead.
 */
const TLHtml: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const display = (state.display as string) || 'inline';
  const html = (state.html as string) ?? '';
  const error = (state.error as string) || null;
  const extra = (state.cssClass as string) ?? '';

  if (error) {
    return (
      <div id={controlId} className="tlHtml tlHtml__error" role="alert">{error}</div>
    );
  }

  const className = `tlHtml tlHtml--${display}${extra ? ` ${extra}` : ''}`;

  if (display !== 'inline') {
    return <div id={controlId} className={className}/>;
  }

  return (
    <div
      id={controlId}
      className={className}
      dangerouslySetInnerHTML={{ __html: html }}
    />
  );
};

export default TLHtml;
