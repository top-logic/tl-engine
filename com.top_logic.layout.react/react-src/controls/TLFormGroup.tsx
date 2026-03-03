import { React, useTLState, useTLCommand, TLChild, useI18N } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback } = React;

const I18N_KEYS = {
  'js.formGroup.collapse': 'Collapse',
  'js.formGroup.expand': 'Expand',
};

/**
 * A nestable form section with optional header, collapsible body, and border.
 * Participates as a grid item in the parent layout; uses CSS subgrid internally.
 *
 * State:
 * - header: string | null
 * - headerActions: ChildDescriptor[]
 * - collapsible: boolean
 * - collapsed: boolean
 * - border: "none" | "subtle" | "outlined"
 * - fullLine: boolean
 * - children: ChildDescriptor[]
 */
const TLFormGroup: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);

  const header = state.header as string | null;
  const headerActions = (state.headerActions as unknown[]) ?? [];
  const collapsible = state.collapsible === true;
  const collapsed = state.collapsed === true;
  const border = (state.border as string) ?? 'none';
  const fullLine = state.fullLine === true;
  const children = (state.children as unknown[]) ?? [];

  const hasHeader = header != null || headerActions.length > 0 || collapsible;

  const handleToggle = useCallback(() => {
    sendCommand('toggleCollapse');
  }, [sendCommand]);

  const className = [
    'tlFormGroup',
    `tlFormGroup--border-${border}`,
    fullLine ? 'tlFormGroup--fullLine' : '',
    collapsed ? 'tlFormGroup--collapsed' : '',
  ].filter(Boolean).join(' ');

  return (
    <div id={controlId} className={className}>
      {hasHeader && (
        <div className="tlFormGroup__header">
          {collapsible && (
            <button type="button" className="tlFormGroup__collapseToggle"
              onClick={handleToggle}
              aria-expanded={!collapsed}
              title={collapsed ? i18n['js.formGroup.expand'] : i18n['js.formGroup.collapse']}>
              <svg viewBox="0 0 16 16" width="14" height="14" aria-hidden="true"
                className={collapsed ? 'tlFormGroup__chevron--collapsed' : 'tlFormGroup__chevron'}>
                <polyline points="4,6 8,10 12,6" fill="none" stroke="currentColor"
                  strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
              </svg>
            </button>
          )}
          {header && <span className="tlFormGroup__title">{header}</span>}
          {headerActions.length > 0 && (
            <div className="tlFormGroup__actions">
              {headerActions.map((action, i) => (
                <TLChild key={i} control={action} />
              ))}
            </div>
          )}
        </div>
      )}
      {!collapsed && (
        <div className="tlFormGroup__body">
          {children.map((child, i) => (
            <TLChild key={i} control={child} />
          ))}
        </div>
      )}
    </div>
  );
};

export default TLFormGroup;
