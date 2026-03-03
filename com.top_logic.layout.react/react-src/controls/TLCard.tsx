import { React, useTLState, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * An elevated content container, lighter than TLPanel.
 *
 * State:
 * - title: string | null
 * - variant: "outlined" | "elevated"  (default: "outlined")
 * - padding: "none" | "compact" | "default"  (default: "default")
 * - headerActions: ChildDescriptor[]
 * - child: ChildDescriptor
 */
const TLCard: React.FC<TLCellProps> = () => {
  const state = useTLState();

  const title = state.title as string | null;
  const variant = (state.variant as string) ?? 'outlined';
  const padding = (state.padding as string) ?? 'default';
  const headerActions = (state.headerActions as unknown[]) ?? [];
  const child = state.child;

  const hasHeader = title != null || headerActions.length > 0;

  return (
    <div className={`tlCard tlCard--${variant}`}>
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
        <TLChild control={child} />
      </div>
    </div>
  );
};

export default TLCard;
