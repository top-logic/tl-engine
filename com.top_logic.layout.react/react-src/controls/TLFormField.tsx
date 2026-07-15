import { React, useTLState, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { FormLayoutContext } from './FormLayoutContext';

const { useContext, useState, useCallback } = React;

/**
 * Form field chrome wrapper that renders label, required indicator,
 * help icon, error message, warning messages, help text, and dirty
 * indicator around any field input control.
 *
 * State:
 * - label: string
 * - required: boolean
 * - error: string | null
 * - warnings: string[] | null
 * - helpText: string | null
 * - dirty: boolean
 * - labelPosition: "side" | "top" | null  (null = inherit from context)
 * - fullLine: boolean
 * - visible: boolean
 * - field: ChildDescriptor
 */
const TLFormField: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const ctx = useContext(FormLayoutContext);

  const label = (state.label as string) ?? '';
  const required = state.required === true;
  const error = state.error as string | null;
  const warnings = state.warnings as string[] | null;
  const helpText = state.helpText as string | null;
  const dirty = state.dirty === true;
  const labelPos = (state.labelPosition as string | null) ?? ctx.resolvedLabelPosition;
  const fullLine = state.fullLine === true;
  const visible = state.visible !== false;
  const hasTooltip = state.hasTooltip === true;
  const field = state.field;
  const readOnly = ctx.readOnly;

  const [helpVisible, setHelpVisible] = useState(false);
  const toggleHelp = useCallback(() => setHelpVisible(v => !v), []);

  if (!visible) return null;

  const hasError = error != null;
  const hasWarnings = warnings != null && warnings.length > 0;

  const className = [
    'tlFormField',
    `tlFormField--${labelPos}`,
    readOnly ? 'tlFormField--readonly' : '',
    fullLine ? 'tlFormField--fullLine' : '',
    hasError ? 'tlFormField--error' : '',
    !hasError && hasWarnings ? 'tlFormField--warning' : '',
    dirty ? 'tlFormField--dirty' : '',
  ].filter(Boolean).join(' ');

  return (
    <div id={controlId} className={className}>
      <div className="tlFormField__label">
        <span
          className="tlFormField__labelText"
          data-tooltip={hasTooltip ? 'key:tooltip' : undefined}
        >{label}</span>
        {required && !readOnly && <span className="tlFormField__required">*</span>}
        {dirty && <span className="tlFormField__dirtyDot" />}
        {helpText && !readOnly && (
          <button type="button" className="tlFormField__helpIcon" onClick={toggleHelp}
            aria-label="Help">
            <svg viewBox="0 0 16 16" width="14" height="14" aria-hidden="true">
              <circle cx="8" cy="8" r="7" fill="none" stroke="currentColor" strokeWidth="1.5" />
              <text x="8" y="12" textAnchor="middle" fontSize="10"
                fill="currentColor">?</text>
            </svg>
          </button>
        )}
      </div>
      <div className="tlFormField__input">
        <TLChild control={field} />
      </div>
      {!readOnly && hasError && (
        <div className="tlFormField__error" role="alert">
          <svg className="tlFormField__errorIcon" viewBox="0 0 16 16" width="14" height="14"
            aria-hidden="true">
            <path d="M8 1l7 14H1L8 1z" fill="none" stroke="currentColor" strokeWidth="1.2" />
            <line x1="8" y1="6" x2="8" y2="10" stroke="currentColor" strokeWidth="1.2" />
            <circle cx="8" cy="12" r="0.8" fill="currentColor" />
          </svg>
          <span>{error}</span>
        </div>
      )}
      {!readOnly && !hasError && hasWarnings && (
        <div className="tlFormField__warnings" aria-live="polite">
          {warnings.map((msg, i) => (
            <div key={i} className="tlFormField__warning">
              <svg className="tlFormField__warningIcon" viewBox="0 0 16 16" width="14" height="14"
                aria-hidden="true">
                <path d="M8 1l7 14H1L8 1z" fill="none" stroke="currentColor" strokeWidth="1.2" />
                <line x1="8" y1="6" x2="8" y2="10" stroke="currentColor" strokeWidth="1.2" />
                <circle cx="8" cy="12" r="0.8" fill="currentColor" />
              </svg>
              <span>{msg}</span>
            </div>
          ))}
        </div>
      )}
      {!readOnly && helpText && helpVisible && (
        <div className="tlFormField__helpText">{helpText}</div>
      )}
    </div>
  );
};

export default TLFormField;
