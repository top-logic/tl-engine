import { React, useTLState, useTLCommand, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

interface Crumb {
  depth: number;
  label: string;
}

/**
 * Renders the current presentation of a responsive master-detail (`<adaptive-detail>`).
 *
 * The server decides which presentation to build (a split for wide viewports, or the
 * selector/detail toggle for narrow ones) and pushes it as the `content` child. In compact mode the
 * outermost (coordinator) control also pushes a `breadcrumb` spanning all nested levels; tapping a
 * crumb sends a `navigate` command that clears the selections from that level down.
 *
 * State:
 * - content:    ChildDescriptor       - the active presentation control (split, selector, or detail)
 * - breadcrumb: Crumb[] | null         - the unified breadcrumb (compact, coordinator only)
 */
const TLAdaptiveDetail: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const content = state.content as unknown;
  const breadcrumb = (state.breadcrumb as Crumb[] | null) ?? null;

  return (
    <div id={controlId} className="tlAdaptiveDetail">
      {breadcrumb && breadcrumb.length > 0 && (
        <nav className="tlAdaptiveDetail__breadcrumb" aria-label="Breadcrumb">
          {breadcrumb.map((crumb, index) => {
            const isLast = index === breadcrumb.length - 1;
            return (
              <React.Fragment key={crumb.depth}>
                {index > 0 && <span className="tlAdaptiveDetail__sep">›</span>}
                {isLast ? (
                  <span className="tlAdaptiveDetail__crumb tlAdaptiveDetail__crumb--current">{crumb.label}</span>
                ) : (
                  <button
                    type="button"
                    className="tlAdaptiveDetail__crumb"
                    onClick={() => sendCommand('navigate', { depth: crumb.depth })}
                  >
                    {crumb.label}
                  </button>
                )}
              </React.Fragment>
            );
          })}
        </nav>
      )}
      <div className="tlAdaptiveDetail__content">{content && <TLChild control={content} />}</div>
    </div>
  );
};

export default TLAdaptiveDetail;
