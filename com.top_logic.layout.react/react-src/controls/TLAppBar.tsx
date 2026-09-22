import { React, useTLState, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { ButtonDefaults } from './button/ButtonDefaults';

/**
 * A top-level application bar with leading slot, title, inline children, and trailing actions.
 *
 * State:
 * - title: string
 * - leading: ChildDescriptor | null
 * - children: ChildDescriptor[]  (inline content between title and actions, e.g. a <slot>)
 * - actions: ChildDescriptor  (the toolbar of the commands placed in the bar; it renders nothing
 *   while there is no command, and folds the ones that do not fit into its overflow menu; the
 *   bar renders its buttons ghost, the toolbar collapses them to their icons when short of room)
 * - trailing: ChildDescriptor | null  (closes the bar, right of the actions)
 * - variant: "flat" | "elevated"  (default: "flat")
 * - color: "primary" | "surface"  (default: "primary")
 */
const TLAppBar: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();

  const title = (state.title as string) ?? '';
  const leading = state.leading;
  const trailing = state.trailing;
  const children = (state.children as unknown[]) ?? [];
  const actions = state.actions;
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
      {children.length > 0 && (
        <div className="tlAppBar__children">
          {children.map((child, i) => (
            <TLChild key={i} control={child} />
          ))}
        </div>
      )}
      {actions != null && (
        <ButtonDefaults appearance="ghost">
          <div className="tlAppBar__actions">
            <TLChild control={actions} />
          </div>
        </ButtonDefaults>
      )}
      {trailing && (
        <div className="tlAppBar__trailing">
          <TLChild control={trailing} />
        </div>
      )}
    </header>
  );
};

export default TLAppBar;
