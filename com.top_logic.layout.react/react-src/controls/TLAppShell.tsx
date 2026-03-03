import { React, useTLState, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * Application shell with header / content / footer layout and built-in snackbar.
 *
 * State:
 * - header:   ChildDescriptor | null  (optional, fixed height)
 * - content:  ChildDescriptor         (required, flex:1)
 * - footer:   ChildDescriptor | null  (optional, fixed height)
 * - snackbar: ChildDescriptor         (built-in notification service)
 */
const TLAppShell: React.FC<TLCellProps> = () => {
  const state = useTLState();

  const header = state.header as unknown;
  const content = state.content as unknown;
  const footer = state.footer as unknown;
  const snackbar = state.snackbar as unknown;

  return (
    <div className="tlAppShell">
      {header && (
        <div className="tlAppShell__header">
          <TLChild control={header} />
        </div>
      )}
      <div className="tlAppShell__content">
        <TLChild control={content} />
      </div>
      {footer && (
        <div className="tlAppShell__footer">
          <TLChild control={footer} />
        </div>
      )}
      <TLChild control={snackbar} />
    </div>
  );
};

export default TLAppShell;
