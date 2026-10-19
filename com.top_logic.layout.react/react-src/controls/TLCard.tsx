import {
  React, useTLState, TLChild, FillBarrier, rootClassName, SurfaceProvider, useSurface, cardSurfaceClass, paintedLevel,
} from 'tl-react-bridge';
import type { TLCellProps, CardStateJson, ChildControlJson, Surface } from 'tl-react-bridge';

const { useMemo } = React;

/**
 * A content container lighter than TLPanel: a card (tl-card) with an optional head.
 *
 * The card takes its surface from where it lies - the layer on the page, the nested step inside a surface, the
 * surface of a dialog in a dialog. `variant` `elevated` draws like `outlined`: a card lies in the page, only what
 * floats above it casts a shadow.
 *
 * The state is described by CardStateJson.
 */
const TLCard: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<CardStateJson>>();
  const surface = useSurface();
  // What the card is for the components inside: a surface with compact insets, on a floating surface that surface's level.
  const inner = useMemo<Surface>(() => ({ level: paintedLevel(surface.level), inset: 'card' }), [surface.level]);

  const title = state.title ?? null;
  const padding = state.padding ?? 'default';
  const headerActions: ChildControlJson[] = state.headerActions ?? [];

  const hasHeader = title != null || headerActions.length > 0;

  return (
    <div id={controlId} className={rootClassName(state, 'tl-card', cardSurfaceClass(surface.level))}>
      {hasHeader && (
        <div className="tl-titlebar">
          {title && <h2 className="tl-titlebar__title tl-type-heading-sm">{title}</h2>}
          {headerActions.length > 0 && (
            <div className="tl-titlebar__actions">
              {headerActions.map((action, i) => (
                <TLChild key={i} control={action} />
              ))}
            </div>
          )}
        </div>
      )}
      <div className={`tl-card__body tl-card__body--pad-${padding}`}>
        <SurfaceProvider value={inner}>
          <FillBarrier>
            <TLChild control={state.child} />
          </FillBarrier>
        </SurfaceProvider>
      </div>
    </div>
  );
};

export default TLCard;
