import { React, useTLState, TLChild, rootClassName, ButtonDefaults } from 'tl-react-bridge';
import type { TLCellProps, AppBarStateJson, ChildControlJson } from 'tl-react-bridge';

/**
 * A top-level application bar with leading slot, title, inline children, and trailing actions.
 *
 * The state is described by AppBarStateJson. The bar renders the buttons of its actions ghost; the
 * toolbar collapses them to their icons when short of room.
 */
const TLAppBar: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<AppBarStateJson>>();

  const title = state.title ?? '';
  const leading = state.leading;
  const trailing = state.trailing;
  const children: ChildControlJson[] = state.children ?? [];
  const actions = state.actions;
  const variant = state.variant ?? 'flat';

  const className = [
    'tlAppBar',
    'tlAppBar--primary',
    variant === 'elevated' ? 'tlAppBar--elevated' : '',
  ].filter(Boolean).join(' ');

  return (
    <header id={controlId} className={rootClassName(state, className)}>
      {!!leading && (
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
      {!!trailing && (
        <div className="tlAppBar__trailing">
          <TLChild control={trailing} />
        </div>
      )}
    </header>
  );
};

export default TLAppBar;
