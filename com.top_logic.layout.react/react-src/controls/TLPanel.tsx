import { React, useTLState, useTLCommand, TLChild, useI18N, useFillHost, FillProvider, FillBarrier, rootClassName, ThemeIcon, ButtonDefaults, usePart } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback } = React;

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
 * - appearance: "default" | "card" (card renders a bordered, rounded panel with compact insets)
 * - toolbar: ChildDescriptor (a TLToolbar control, may be absent)
 * - buttonBar: ChildDescriptor (a TLToolbar control, may be absent)
 * - child: ChildDescriptor
 * - errorMessage: string (validation error displayed below the content, may be absent)
 * - errorIcon: string (encoded theme icon displayed in front of the error message)
 */
const TLPanel: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);

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

  const handleMinimize = useCallback(() => {
    sendCommand('toggleMinimize');
  }, [sendCommand]);

  const handleMaximize = useCallback(() => {
    sendCommand('toggleMaximize');
  }, [sendCommand]);

  const handlePopOut = useCallback(() => {
    sendCommand('popOut');
  }, [sendCommand]);

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

  // The height spans a container of definite height that is no flex column - a split pane - where
  // the fill class cannot size the panel. In a container as high as its content the percentage has
  // nothing to resolve against, and the panel is as high as its content, unless it fills.
  const panelStyle: React.CSSProperties = isMaximized
    ? { position: 'absolute', inset: 0, zIndex: 10, display: 'flex', flexDirection: 'column' }
    : {
        display: 'flex',
        flexDirection: 'column',
        width: width ?? '100%',
        ...(width ? { maxWidth: '100%' } : {}),
        height: '100%',
      };

  // Render the header only when it carries something: a title, a toolbar, or an action button.
  // A chrome-less panel (e.g. a fill panel whose tab already labels it) then shows just its
  // content, without an empty header bar wasting space.
  const hasActionButtons =
    (showMinimize && !isMaximized) || (showMaximize && !isMinimized) || showPopOut;
  const hasHeader =
    (!!title && title.trim() !== '') || !!state.titleContent || !!state.toolbar || hasActionButtons;

  const minimizeLabel = isMinimized ? i18n['js.panel.restore'] : i18n['js.panel.minimize'];
  const maximizeLabel = isMaximized ? i18n['js.panel.restore'] : i18n['js.panel.maximize'];

  return (
    <div
      id={controlId}
      className={rootClassName(state, `tlPanel tlPanel--${expansionState.toLowerCase()}${fullLine ? ' tlPanel--fullLine' : ''}${fillClass ? ' ' + fillClass : ''}${hoverActions ? ' tlPanel--hoverActions' : ''}${card ? ' tlPanel--card' : ''}`)}
      style={panelStyle}
    >
      {hasHeader && (
      <div className="tlPanel__header">
        {!!title && title.trim() !== '' && <span className="tlPanel__title">{title}</span>}
        {!!state.titleContent && (
          <div className="tlPanel__titleContent">
            <TLChild control={state.titleContent} />
          </div>
        )}
        <div className="tlPanel__toolbar">
          {!!state.toolbar && <TLChild control={state.toolbar} />}
          {showMinimize && !isMaximized && (
            <Button
              label={minimizeLabel}
              image={isMinimized ? RESTORE_ICON : MINIMIZE_ICON}
              display="icon-only"
              appearance="ghost"
              className="tlPanel__actionButton"
              onClick={handleMinimize}
            />
          )}
          {showMaximize && !isMinimized && (
            <Button
              label={maximizeLabel}
              image={isMaximized ? RESTORE_ICON : MAXIMIZE_ICON}
              display="icon-only"
              appearance="ghost"
              className="tlPanel__actionButton"
              onClick={handleMaximize}
            />
          )}
          {showPopOut && (
            <Button
              label={i18n['js.panel.popOut']}
              image={POP_OUT_ICON}
              display="icon-only"
              appearance="ghost"
              className="tlPanel__actionButton"
              onClick={handlePopOut}
            />
          )}
        </div>
      </div>
      )}
      {!isMinimized && (
        <div className="tlPanel__content">
          {neverFill ? (
            <FillBarrier>
              <TLChild control={state.child} />
            </FillBarrier>
          ) : (
            <FillProvider host={fillHost}>
              <TLChild control={state.child} />
            </FillProvider>
          )}
        </div>
      )}
      {!isMinimized && errorMessage && (
        <div className="tl-form-field__message tl-type-label tlPanel__error" role="alert">
          {typeof state.errorIcon === 'string' && <ThemeIcon encoded={state.errorIcon} className="tl-icon-sm" />}
          <span>{errorMessage}</span>
        </div>
      )}
      {!isMinimized && !!state.buttonBar && (
        <ButtonDefaults appearance="secondary">
          <div className="tlPanel__buttonBar">
            <TLChild control={state.buttonBar} />
          </div>
        </ButtonDefaults>
      )}
    </div>
  );
};

export default TLPanel;
