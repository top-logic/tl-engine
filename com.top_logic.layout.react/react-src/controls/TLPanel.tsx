import {
  React, useTLState, useTLCommand, TLChild, useI18N, useFillHost, FillProvider, FillBarrier, rootClassName, stateAttr,
  ThemeIcon, ButtonDefaults, usePart, SurfaceProvider, useSurface, cardSurfaceClass, panelSurface, isInteractiveWithin,
} from 'tl-react-bridge';
import type { TLCellProps, Surface } from 'tl-react-bridge';

const { useCallback, useMemo } = React;

const I18N_KEYS = {
  'js.panel.minimize': 'Minimize',
  'js.panel.maximize': 'Maximize',
  'js.panel.restore': 'Restore',
  'js.panel.popOut': 'Pop out',
};

/** Icon of the button minimizing the panel. */
const MINIMIZE_ICON = 'css:fa-solid fa-window-minimize';

/** Icon of the button maximizing the panel. */
const MAXIMIZE_ICON = 'css:fa-solid fa-window-maximize';

/** Icon of the button restoring a minimized or maximized panel. */
const RESTORE_ICON = 'css:fa-solid fa-window-restore';

/** Icon of the button opening the panel in a window of its own. */
const POP_OUT_ICON = 'css:fa-solid fa-arrow-up-right-from-square';

/**
 * Whether a pointer gesture on the head of a panel is the head's: not on a button or another interactive element of
 * the head, and not anywhere in the toolbar's slot - its empty space included (spec §3: clicks on the toolbar stay
 * its own). React events bubble through portals: a gesture whose target lies outside the head's DOM - a menu of the
 * toolbar, opened on the page - is never the head's.
 */
function ownedByHead(event: React.MouseEvent<HTMLElement>): boolean {
  return event.target instanceof Node && event.currentTarget.contains(event.target)
    && !isInteractiveWithin(event.target, event.currentTarget)
    && !(event.target instanceof Element && event.target.closest('.tl-titlebar__actions'));
}

/**
 * A panel with a titled toolbar header wrapping a single child content area.
 *
 * State:
 * - title: string
 * - titleContent: ChildDescriptor (rendered in the title area, may be absent)
 * - expansionState: "NORMALIZED" | "MINIMIZED" | "MAXIMIZED" | "HIDDEN"
 * - showMinimize: boolean
 * - showMaximize: boolean
 * - showPopOut: boolean
 * - fill: boolean (absent: the panel fills while its content fills, e.g. a table; true: fills even
 *     around content of its own size; false: does not pass the fill request of its content on to
 *     its container)
 * - width: string (a CSS width of the panel's own, e.g. "380px", instead of the width its
 *     container offers; capped at the available width, so the panel stays visible on a narrow
 *     screen)
 * - hoverActions: boolean (hide toolbar buttons until the panel is hovered or a button is focused)
 * - appearance: "default" | "card" (card draws the panel as a card (tl-card), insets inside compact)
 * - toolbar: ChildDescriptor (a TLToolbar control, may be absent)
 * - buttonBar: ChildDescriptor (a TLToolbar control, may be absent)
 * - child: ChildDescriptor
 * - errorMessage: string (validation error displayed below the content, may be absent)
 * - errorIcon: string (encoded theme icon displayed in front of the error message)
 *
 * Handles like a window: a double click on the head maximizes and restores, Escape restores a maximized panel, a
 * click anywhere on the head of a minimized panel opens it. The buttons stay the way for the keyboard.
 */
const TLPanel: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);
  const surface = useSurface();

  const title = state.title as string;
  const expansionState = (state.expansionState as string) ?? 'NORMALIZED';
  const showMinimize = state.showMinimize === true;
  const showMaximize = state.showMaximize === true;
  const showPopOut = state.showPopOut === true;
  const fullLine = state.fullLine === true;
  const alwaysFill = state.fill === true;
  const neverFill = state.fill === false;
  const hoverActions = state.hoverActions === true;
  const card = state.appearance === 'card';
  const width = (state.width as string | undefined) ?? undefined;
  const errorMessage = state.errorMessage as string | undefined;

  const isMinimized = expansionState === 'MINIMIZED';
  const isMaximized = expansionState === 'MAXIMIZED';
  const isHidden = expansionState === 'HIDDEN';
  const bodyId = `${controlId}-body`;

  // What the panel is for the components inside: drawn as a card it is a surface and raises the level, plain it
  // paints none and passes on the level it lies on; its insets are those of a card or a panel.
  const inner = useMemo<Surface>(() => panelSurface(surface, card), [surface, card]);

  const handleMinimize = useCallback(() => {
    sendCommand('toggleMinimize');
  }, [sendCommand]);

  const handleMaximize = useCallback(() => {
    sendCommand('toggleMaximize');
  }, [sendCommand]);

  const handlePopOut = useCallback(() => {
    sendCommand('popOut');
  }, [sendCommand]);

  // The head as a window's: a double click maximizes or restores, a click opens a minimized panel (ownedByHead).
  const handleHeadDoubleClick = useCallback((event: React.MouseEvent<HTMLElement>) => {
    if (!showMaximize || isMinimized || !ownedByHead(event)) {
      return;
    }
    handleMaximize();
  }, [showMaximize, isMinimized, handleMaximize]);

  const handleHeadClick = useCallback((event: React.MouseEvent<HTMLElement>) => {
    if (!showMinimize || !isMinimized || !ownedByHead(event)) {
      return;
    }
    handleMinimize();
  }, [showMinimize, isMinimized, handleMinimize]);

  // Escape restores a maximized panel - unless something inside used the key already, a popup closing, say.
  const handleKeyDown = useCallback((event: React.KeyboardEvent) => {
    if (event.key !== 'Escape' || !isMaximized || event.defaultPrevented) {
      return;
    }
    event.preventDefault();
    event.stopPropagation();
    handleMaximize();
  }, [isMaximized, handleMaximize]);

  // The panel takes part in the fill contract as a container: it fills while it is configured to,
  // or while its content fills - a table, a split - which can only resolve its height against a
  // body of definite height. A hidden panel renders no content and occupies no space, so it asks
  // its container for none either. A panel configured not to fill ends the chain at its body
  // instead: the request of its content reaches no container.
  const [fillClass, fillHost] = useFillHost(alwaysFill && !isHidden);
  const Button = usePart('Button');

  if (isHidden) {
    return null;
  }

  // The size spans a container of definite height that is no flex column - a split pane - where the fill class
  // cannot size the panel. Maximized, the design system places the panel over its container.
  const panelStyle: React.CSSProperties | undefined = isMaximized
    ? undefined
    : { width: width ?? '100%', ...(width ? { maxWidth: '100%' } : {}), height: '100%' };

  // Render the head only when it carries something: a title, a toolbar, or a button. A chrome-less panel (a fill
  // panel whose tab already labels it) then shows just its content.
  const hasTitle = !!title && title.trim() !== '';
  const hasButtons = (showMinimize && !isMaximized) || (showMaximize && !isMinimized) || showPopOut;
  const hasHeader = hasTitle || !!state.titleContent || !!state.toolbar || hasButtons;

  const minimizeLabel = isMinimized ? i18n['js.panel.restore'] : i18n['js.panel.minimize'];
  const maximizeLabel = isMaximized ? i18n['js.panel.restore'] : i18n['js.panel.maximize'];

  return (
    <div
      id={controlId}
      className={rootClassName(state, 'tl-panel', card && 'tl-card', card && cardSurfaceClass(surface.level),
        fullLine && 'tl-panel--full-line', hoverActions && 'tl-panel--hover-actions', fillClass)}
      data-tl-state={stateAttr(isMinimized && 'minimized', isMaximized && 'maximized')}
      style={panelStyle}
      onKeyDown={handleKeyDown}
    >
      {hasHeader && (
        <div className="tl-titlebar" onDoubleClick={handleHeadDoubleClick} onClick={handleHeadClick}>
          {hasTitle && <h2 className="tl-titlebar__title tl-type-heading-sm">{title}</h2>}
          {!!state.titleContent && (
            <div className="tl-titlebar__content">
              <TLChild control={state.titleContent} />
            </div>
          )}
          {!!state.toolbar && (
            <div className="tl-titlebar__actions">
              <TLChild control={state.toolbar} />
            </div>
          )}
          {hasButtons && (
            <div className="tl-titlebar__buttons">
              {showMinimize && !isMaximized && (
                <Button
                  label={minimizeLabel}
                  image={isMinimized ? RESTORE_ICON : MINIMIZE_ICON}
                  display="icon-only"
                  appearance="ghost"
                  small
                  aria-expanded={!isMinimized}
                  aria-controls={isMinimized ? undefined : bodyId}
                  onClick={handleMinimize}
                />
              )}
              {showMaximize && !isMinimized && (
                <Button
                  label={maximizeLabel}
                  image={isMaximized ? RESTORE_ICON : MAXIMIZE_ICON}
                  display="icon-only"
                  appearance="ghost"
                  small
                  onClick={handleMaximize}
                />
              )}
              {showPopOut && (
                <Button
                  label={i18n['js.panel.popOut']}
                  image={POP_OUT_ICON}
                  display="icon-only"
                  appearance="ghost"
                  small
                  onClick={handlePopOut}
                />
              )}
            </div>
          )}
        </div>
      )}
      {!isMinimized && (
        <div id={bodyId} className="tl-panel__body">
          <SurfaceProvider value={inner}>
            {neverFill ? (
              <FillBarrier>
                <TLChild control={state.child} />
              </FillBarrier>
            ) : (
              <FillProvider host={fillHost}>
                <TLChild control={state.child} />
              </FillProvider>
            )}
          </SurfaceProvider>
        </div>
      )}
      {!isMinimized && errorMessage && (
        <div className="tl-form-field__message tl-type-label" role="alert">
          {typeof state.errorIcon === 'string' && <ThemeIcon encoded={state.errorIcon} className="tl-icon-sm" />}
          <span>{errorMessage}</span>
        </div>
      )}
      {!isMinimized && !!state.buttonBar && (
        <ButtonDefaults appearance="secondary">
          <div className="tl-button-bar">
            <TLChild control={state.buttonBar} />
          </div>
        </ButtonDefaults>
      )}
    </div>
  );
};

export default TLPanel;
