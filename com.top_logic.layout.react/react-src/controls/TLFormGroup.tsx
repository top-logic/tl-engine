import { React, useTLState, useTLCommand, TLChild, useI18N, rootClassName, tooltipProps, ThemeIcon } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback } = React;

const I18N_KEYS = {
  'js.formGroup.collapse': 'Collapse',
  'js.formGroup.expand': 'Expand',
};

const CHEVRON_OPEN = 'css:fa-solid fa-chevron-down';
const CHEVRON_COLLAPSED = 'css:fa-solid fa-chevron-right';

/** The class of the group's border: a subtle one is a separator line, an outlined one a frame. */
const BORDER_CLASS: Record<string, string> = {
  subtle: 'tl-form-group--separator',
  outlined: 'tl-form-group--outlined',
};

/**
 * A nestable form section with optional header, collapsible body, and border.
 * Participates as a grid item in the parent layout; uses CSS subgrid internally.
 *
 * State:
 * - headerControl: ChildDescriptor | null
 * - headerActions: ChildDescriptor[]
 * - collapsible: boolean
 * - collapsed: boolean
 * - border: "none" | "subtle" | "outlined"
 * - fullLine: boolean
 * - hidden: boolean
 * - children: ChildDescriptor[]
 */
const TLFormGroup: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);

  const headerControl = (state.headerControl as unknown) ?? null;
  const headerActions = (state.headerActions as unknown[]) ?? [];
  const collapsible = state.collapsible === true;
  const collapsed = state.collapsed === true;
  const border = (state.border as string) ?? 'none';
  const fullLine = state.fullLine === true;
  const children = (state.children as unknown[]) ?? [];
  const hidden = state.hidden === true;

  const hasHeader = headerControl != null || headerActions.length > 0 || collapsible;

  const handleToggle = useCallback(() => {
    sendCommand('toggleCollapse');
  }, [sendCommand]);

  const toggleLabel = collapsed ? i18n['js.formGroup.expand'] : i18n['js.formGroup.collapse'];
  const bodyId = `${controlId}-body`;

  const className = [
    'tl-form-group',
    BORDER_CLASS[border] ?? '',
    fullLine ? 'tl-form-group--full' : '',
  ].filter(Boolean).join(' ');

  // A hidden group is hidden via CSS instead of not being rendered, like an invisible form field:
  // unmounting its children would drop their SSE subscriptions.
  return (
    <div id={controlId} className={rootClassName(state, className)} style={hidden ? { display: 'none' } : undefined}>
      {hasHeader && (
        <div className="tl-form-group__header">
          {collapsible && (
            <button type="button" className="tl-form-group__toggle"
              onClick={handleToggle}
              aria-expanded={!collapsed}
              aria-controls={bodyId}
              aria-label={toggleLabel}
              {...tooltipProps(toggleLabel)}>
              <ThemeIcon encoded={collapsed ? CHEVRON_COLLAPSED : CHEVRON_OPEN} className="tl-icon-sm" />
            </button>
          )}
          {headerControl && (
            <span className="tl-form-group__title tl-type-heading-sm">
              <TLChild control={headerControl} />
            </span>
          )}
          {headerActions.length > 0 && (
            <div className="tl-form-group__actions">
              {headerActions.map((action, i) => (
                <TLChild key={i} control={action} />
              ))}
            </div>
          )}
        </div>
      )}
      <div id={bodyId} className="tl-form-group__body" hidden={collapsed}>
        {children.map((child, i) => (
          <TLChild key={i} control={child} />
        ))}
      </div>
    </div>
  );
};

export default TLFormGroup;
