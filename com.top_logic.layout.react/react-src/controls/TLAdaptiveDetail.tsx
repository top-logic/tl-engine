import { React, useTLState, useTLCommand, useI18N, TLChild, rootClassName } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { Breadcrumb } from './TLBreadcrumb';

const I18N_KEYS = {
  'js.breadcrumb.label': 'Breadcrumb',
};

interface Crumb {
  depth: number;
  label: string;
}

/**
 * Renders the current presentation of a responsive master-detail (`<adaptive-detail>`).
 *
 * The server decides which presentation to build (a split or a full-width selector with a drawer for
 * wide viewports, or the selector/detail toggle for narrow ones) and pushes it as the `content`
 * child. A presentation that overlays the content rather than dividing it pushes the overlaying part
 * as the `overlay` child, positioned inside this element's own area. In compact mode the outermost
 * (coordinator) control also pushes a `breadcrumb` spanning all nested levels, drawn as the
 * breadcrumb of the design system; choosing a crumb sends a `navigate` command that clears the
 * selections from that level down.
 *
 * State:
 * - content:    ChildDescriptor        - the active presentation control (split, selector, or detail)
 * - overlay:    ChildDescriptor | null - the part overlaying the content (the detail drawer)
 * - breadcrumb: Crumb[] | null         - the unified breadcrumb (compact, coordinator only)
 */
const TLAdaptiveDetail: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);

  const content = state.content as unknown;
  const overlay = state.overlay as unknown;
  const crumbs = ((state.breadcrumb as Crumb[] | null) ?? []).map((crumb) => ({ id: String(crumb.depth), label: crumb.label }));

  return (
    <div id={controlId} className={rootClassName(state, 'tl-adaptive-detail')}>
      {crumbs.length > 0 && (
        <div className="tl-adaptive-detail__bar">
          <Breadcrumb
            className="tl-breadcrumb"
            label={i18n['js.breadcrumb.label']}
            items={crumbs}
            onNavigate={(id) => sendCommand('navigate', { depth: Number(id) })}
          />
        </div>
      )}
      <div className="tl-adaptive-detail__content">{!!content && <TLChild control={content} />}</div>
      {overlay ? <TLChild control={overlay} /> : null}
    </div>
  );
};

export default TLAdaptiveDetail;
