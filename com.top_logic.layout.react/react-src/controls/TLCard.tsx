import { React, useTLState, TLChild, FillBarrier, rootClassName } from 'tl-react-bridge';
import type { TLCellProps, CardStateJson, ChildControlJson } from 'tl-react-bridge';

/**
 * An elevated content container, lighter than TLPanel.
 *
 * The state is described by CardStateJson.
 */
const TLCard: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<CardStateJson>>();

  const title = state.title ?? null;
  const variant = state.variant ?? 'outlined';
  const padding = state.padding ?? 'default';
  const headerActions: ChildControlJson[] = state.headerActions ?? [];
  const child = state.child;

  const hasHeader = title != null || headerActions.length > 0;

  return (
    <div id={controlId} className={rootClassName(state, `tlCard tlCard--${variant}`)}>
      {hasHeader && (
        <div className="tlCard__header">
          {title && <span className="tlCard__title">{title}</span>}
          {headerActions.length > 0 && (
            <div className="tlCard__headerActions">
              {headerActions.map((action, i) => (
                <TLChild key={i} control={action} />
              ))}
            </div>
          )}
        </div>
      )}
      <div className={`tlCard__body tlCard__body--pad-${padding}`}>
        <FillBarrier>
          <TLChild control={child} />
        </FillBarrier>
      </div>
    </div>
  );
};

export default TLCard;
