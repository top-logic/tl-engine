import { React, useTLState, useTLCommand, TLChild, useI18N } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback } = React;

const I18N_KEYS = {
  'js.panel.minimize': 'Minimize',
  'js.panel.maximize': 'Maximize',
  'js.panel.restore': 'Restore',
  'js.panel.popOut': 'Pop out',
};

/* Inline SVG icon components (stroke-based, 24x24 viewBox). */

/** Horizontal line: minimize indicator. */
const IconMinimize = () => (
  <svg viewBox="0 0 24 24"><line x1="6" y1="12" x2="18" y2="12" /></svg>
);

/** Small box with upward chevron: restore from minimized. */
const IconRestoreFromMin = () => (
  <svg viewBox="0 0 24 24">
    <rect x="6" y="9" width="12" height="10" rx="1" />
    <polyline points="9,7 12,4 15,7" />
  </svg>
);

/** Full box outline: maximize indicator. */
const IconMaximize = () => (
  <svg viewBox="0 0 24 24"><rect x="4" y="4" width="16" height="16" rx="1" /></svg>
);

/** Two overlapping boxes: restore from maximized. */
const IconRestoreFromMax = () => (
  <svg viewBox="0 0 24 24">
    <rect x="4" y="8" width="12" height="12" rx="1" />
    <polyline points="8,8 8,4 20,4 20,16 16,16" />
  </svg>
);

/** Arrow pointing out of a box: pop-out indicator. */
const IconPopOut = () => (
  <svg viewBox="0 0 24 24">
    <polyline points="15,3 21,3 21,9" />
    <line x1="21" y1="3" x2="12" y2="12" />
    <path d="M18 13v6a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h6" />
  </svg>
);

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
 * - fill: boolean (fill the container's bounded height instead of growing with content)
 * - hoverActions: boolean (hide toolbar buttons until the panel is hovered or a button is focused)
 * - appearance: "default" | "card" (card renders a bordered, rounded panel with compact insets)
 * - toolbar: ChildDescriptor (a TLToolbar control, may be absent)
 * - buttonBar: ChildDescriptor (a TLToolbar control, may be absent)
 * - child: ChildDescriptor
 * - bare: boolean (this panel draws no chrome - no title, toolbar or border - and is a pure
 *     full-bleed container. Read NOT by this component but by an enclosing TLFormLayout: a form
 *     whose sole content is a bare panel renders flush, dropping its page inset so the panel fills
 *     the area instead of sitting inside an empty frame. A chromed panel omits it / sets it false
 *     and keeps the inset. Set e.g. by the frameless editable table, RowSetTableControl.)
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
  const fill = state.fill === true;
  const hoverActions = state.hoverActions === true;
  const card = state.appearance === 'card';

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

  if (isHidden) {
    return null;
  }

  const panelStyle: React.CSSProperties = isMaximized
    ? { position: 'absolute', inset: 0, zIndex: 10, display: 'flex', flexDirection: 'column' }
    : { display: 'flex', flexDirection: 'column', width: '100%', height: '100%' };

  // Render the header only when it carries something: a title, a toolbar, or an action button.
  // A chrome-less panel (e.g. a fill panel whose tab already labels it) then shows just its
  // content, without an empty header bar wasting space.
  const hasActionButtons =
    (showMinimize && !isMaximized) || (showMaximize && !isMinimized) || showPopOut;
  const hasHeader =
    (!!title && title.trim() !== '') || !!state.titleContent || !!state.toolbar || hasActionButtons;

  return (
    <div
      id={controlId}
      className={`tlPanel tlPanel--${expansionState.toLowerCase()}${fullLine ? ' tlPanel--fullLine' : ''}${fill ? ' tlPanel--fill' : ''}${hoverActions ? ' tlPanel--hoverActions' : ''}${card ? ' tlPanel--card' : ''}`}
      style={panelStyle}
    >
      {hasHeader && (
      <div className="tlPanel__header">
        {!!title && title.trim() !== '' && <span className="tlPanel__title">{title}</span>}
        {state.titleContent && (
          <div className="tlPanel__titleContent">
            <TLChild control={state.titleContent} />
          </div>
        )}
        <div className="tlPanel__toolbar">
          {state.toolbar && <TLChild control={state.toolbar} />}
          {showMinimize && !isMaximized && (
            <button
              type="button"
              className="tlPanel__actionButton"
              onClick={handleMinimize}
              title={isMinimized ? i18n['js.panel.restore'] : i18n['js.panel.minimize']}
            >
              {isMinimized ? <IconRestoreFromMin /> : <IconMinimize />}
            </button>
          )}
          {showMaximize && !isMinimized && (
            <button
              type="button"
              className="tlPanel__actionButton"
              onClick={handleMaximize}
              title={isMaximized ? i18n['js.panel.restore'] : i18n['js.panel.maximize']}
            >
              {isMaximized ? <IconRestoreFromMax /> : <IconMaximize />}
            </button>
          )}
          {showPopOut && (
            <button
              type="button"
              className="tlPanel__actionButton"
              onClick={handlePopOut}
              title={i18n['js.panel.popOut']}
            >
              <IconPopOut />
            </button>
          )}
        </div>
      </div>
      )}
      {!isMinimized && (
        <div className="tlPanel__content">
          <TLChild control={state.child} />
        </div>
      )}
      {!isMinimized && state.buttonBar && (
        <div className="tlPanel__buttonBar">
          <TLChild control={state.buttonBar} />
        </div>
      )}
    </div>
  );
};

export default TLPanel;
