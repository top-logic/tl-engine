import { React, useTLState, TLChild, rootClassName, ButtonDefaults } from 'tl-react-bridge';
import type { TLCellProps, AppBarStateJson, ChildControlJson } from 'tl-react-bridge';

/**
 * A top-level application bar with leading slot, title, inline children, and trailing actions.
 *
 * The state is described by AppBarStateJson. The bar renders the buttons of its actions ghost; the
 * toolbar collapses them to their icons when short of room.
 *
 * When the bar is short of room, a collapsing toolbar gives way first, down to its overflow button;
 * the title truncates last (design system, app-bar.css).
 */
const TLAppBar: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<AppBarStateJson>>();

  const title = state.title ?? '';
  const leading = state.leading;
  const trailing = state.trailing;
  const children: ChildControlJson[] = state.children ?? [];
  const actions = state.actions;
  const variant = state.variant ?? 'flat';

  const className = ['tl-app-bar', variant === 'elevated' ? 'tl-app-bar--elevated' : ''].filter(Boolean).join(' ');

  return (
    <header id={controlId} className={rootClassName(state, className)}>
      {!!leading && (
        <div className="tl-app-bar__leading">
          <TLChild control={leading} />
        </div>
      )}
      <h1 className="tl-app-bar__title tl-type-heading-sm">{title}</h1>
      {children.length > 0 && (
        <div className="tl-app-bar__children">
          {children.map((child, i) => (
            <TLChild key={i} control={child} />
          ))}
        </div>
      )}
      {actions != null && (
        <ButtonDefaults appearance="ghost">
          <div className="tl-app-bar__actions">
            <TLChild control={actions} />
          </div>
        </ButtonDefaults>
      )}
      {!!trailing && (
        <div className="tl-app-bar__trailing">
          <TLChild control={trailing} />
        </div>
      )}
    </header>
  );
};

export default TLAppBar;
