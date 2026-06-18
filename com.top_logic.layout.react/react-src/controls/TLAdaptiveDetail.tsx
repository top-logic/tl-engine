import { React, useTLState, useTLCommand, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * Renders the current presentation of a responsive master-detail (`<adaptive-detail>`).
 *
 * The server decides which presentation to build (a split for wide viewports, or the
 * selector/detail toggle for narrow ones) and pushes it as the `content` child. This component only
 * renders that child and, in the compact detail view, a back bar that clears the selection.
 *
 * State:
 * - content:  ChildDescriptor - the active presentation control (split, selector, or detail)
 * - showBack: boolean         - whether to render the back bar (compact detail view)
 * - barLabel: string | null   - label of the selected object shown in the back bar
 */
const TLAdaptiveDetail: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const content = state.content as unknown;
  const showBack = state.showBack as boolean;
  const barLabel = state.barLabel as string | undefined;

  return (
    <div id={controlId} className="tlAdaptiveDetail">
      {showBack && (
        <div className="tlAdaptiveDetail__bar">
          <button
            type="button"
            className="tlAdaptiveDetail__back"
            onClick={() => sendCommand('back', {})}
            aria-label="Back"
          >
            &lsaquo;
          </button>
          {barLabel && <span className="tlAdaptiveDetail__title">{barLabel}</span>}
        </div>
      )}
      <div className="tlAdaptiveDetail__content">{content && <TLChild control={content} />}</div>
    </div>
  );
};

export default TLAdaptiveDetail;
