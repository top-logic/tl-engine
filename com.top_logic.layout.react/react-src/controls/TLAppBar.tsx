import { React, useTLState, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * A top-level application bar with leading slot, title, and trailing actions.
 *
 * State:
 * - title: string
 * - leading: ChildDescriptor | null
 * - actions: ChildDescriptor[]
 * - variant: "flat" | "elevated"  (default: "flat")
 * - color: "primary" | "surface"  (default: "primary")
 */
const TLAppBar: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();

  const title = (state.title as string) ?? '';
  const leading = state.leading;
  const actions = (state.actions as unknown[]) ?? [];
  const variant = (state.variant as string) ?? 'flat';
  const color = (state.color as string) ?? 'primary';

  const className = [
    'tlAppBar',
    `tlAppBar--${color}`,
    variant === 'elevated' ? 'tlAppBar--elevated' : '',
  ].filter(Boolean).join(' ');

  return (
    <header id={controlId} className={className}>
      {leading && (
        <div className="tlAppBar__leading">
          <TLChild control={leading} />
        </div>
      )}
      <h1 className="tlAppBar__title">{title}</h1>
      {actions.length > 0 && (
        <div className="tlAppBar__actions">
          {actions.map((action, i) => (
            <TLChild key={i} control={action} />
          ))}
        </div>
      )}
    </header>
  );
};

export default TLAppBar;
