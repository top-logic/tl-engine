import { React, useTLState, useTLCommand, TLChild, useI18N } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback } = React;

const I18N_KEYS = {
  'js.panel.minimize': 'Minimize',
  'js.panel.maximize': 'Maximize',
  'js.panel.restore': 'Restore',
  'js.panel.popOut': 'Pop out',
};

/**
 * A panel with a titled toolbar header wrapping a single child content area.
 *
 * State:
 * - title: string
 * - expansionState: "NORMALIZED" | "MINIMIZED" | "MAXIMIZED" | "HIDDEN"
 * - showMinimize: boolean
 * - showMaximize: boolean
 * - showPopOut: boolean
 * - toolbarButtons: ChildDescriptor[]
 * - child: ChildDescriptor
 */
const TLPanel: React.FC<TLCellProps> = () => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);

  const title = state.title as string;
  const expansionState = (state.expansionState as string) ?? 'NORMALIZED';
  const showMinimize = state.showMinimize === true;
  const showMaximize = state.showMaximize === true;
  const showPopOut = state.showPopOut === true;
  const toolbarButtons = (state.toolbarButtons as unknown[]) ?? [];

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

  return (
    <div
      className={`tlPanel tlPanel--${expansionState.toLowerCase()}`}
      style={panelStyle}
    >
      <div className="tlPanel__header">
        <span className="tlPanel__title">{title}</span>
        <div className="tlPanel__toolbar">
          {toolbarButtons.map((btn, i) => (
            <span key={i} className="tlPanel__toolbarButton">
              <TLChild control={btn} />
            </span>
          ))}
          {showMinimize && (
            <button
              type="button"
              className="tlPanel__actionButton"
              onClick={handleMinimize}
              title={isMinimized ? i18n['js.panel.restore'] : i18n['js.panel.minimize']}
            >
              {isMinimized ? '\u25A1' : '\u2500'}
            </button>
          )}
          {showMaximize && (
            <button
              type="button"
              className="tlPanel__actionButton"
              onClick={handleMaximize}
              title={isMaximized ? i18n['js.panel.restore'] : i18n['js.panel.maximize']}
            >
              {isMaximized ? '\u29C9' : '\u25A1'}
            </button>
          )}
          {showPopOut && (
            <button
              type="button"
              className="tlPanel__actionButton"
              onClick={handlePopOut}
              title={i18n['js.panel.popOut']}
            >
              {'\u2197'}
            </button>
          )}
        </div>
      </div>
      {!isMinimized && (
        <div className="tlPanel__content" style={{ flex: 1, overflow: 'auto', position: 'relative' }}>
          <TLChild control={state.child} />
        </div>
      )}
    </div>
  );
};

export default TLPanel;
