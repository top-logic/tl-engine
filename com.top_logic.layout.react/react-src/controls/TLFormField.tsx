import {
  React, useTLState, TLChild, rootClassName, useI18N, tooltipProps, TOOLTIP_ATTR, FieldLabelContext, fieldLabel,
  focusFieldInput,
} from 'tl-react-bridge';
import type { TLCellProps, ChildDescriptor } from 'tl-react-bridge';
import FontIcon from './FontIcon';
import { FormLayoutContext } from './FormLayoutContext';

const { useContext, useState, useCallback, useMemo } = React;

const I18N_KEYS = {
  'js.formField.help': 'Help',
};

/**
 * Form field chrome wrapper that renders label, required indicator,
 * help icon, error message, warning messages, help text, and dirty
 * indicator around any field input control.
 *
 * The label and the input control refer to each other by id (see FieldLabelContext): the control
 * in the input slot reports the id of its focusable element and names that element by the label
 * text through `aria-labelledby`. A visible label text is a `label` element referring to that
 * element, so a click on it focuses the input (or toggles a checkbox); an input HTML does not
 * activate from a label - a group of options, an editable area - is focused by the label's click
 * handler instead. A hidden label ("hidden" label
 * position, either declared by the field or inherited from the form layout) is kept off the screen
 * in a visually hidden element that still names the input. The input area itself is a plain
 * element, so a click into the input reaches exactly the element under the pointer.
 *
 * State:
 * - label: string
 * - required: boolean
 * - error: string | null
 * - errorIcon: string (encoded theme icon displayed in front of the error message)
 * - warnings: string[] | null
 * - warningIcon: string (encoded theme icon displayed in front of each warning message)
 * - helpText: string | null
 * - tooltipText: string | null (plain text offered on the label; the rich `hasTooltip` wins)
 * - dirty: boolean
 * - labelPosition: "side" | "top" | "after" | "hidden" | null  (null = inherit from context)
 * - fullLine: boolean
 * - visible: boolean
 * - field: ChildDescriptor
 */
const TLFormField: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const ctx = useContext(FormLayoutContext);
  const i18n = useI18N(I18N_KEYS);

  const label = (state.label as string) ?? '';
  const required = state.required === true;
  const error = state.error as string | null;
  const errorIcon = state.errorIcon as string | undefined;
  const warnings = state.warnings as string[] | null;
  const warningIcon = state.warningIcon as string | undefined;
  const helpText = state.helpText as string | null;
  const dirty = state.dirty === true;
  const labelPos = (state.labelPosition as string | null) ?? ctx.resolvedLabelPosition;
  const fullLine = state.fullLine === true;
  const visible = state.visible !== false;
  const hasTooltip = state.hasTooltip === true;
  const tooltipText = state.tooltipText as string | null;
  const field = state.field;
  const fieldControlId = (field as ChildDescriptor | undefined)?.controlId;
  const readOnly = ctx.readOnly;

  const [helpVisible, setHelpVisible] = useState(false);
  const toggleHelp = useCallback(() => setHelpVisible(v => !v), []);

  const labelHidden = labelPos === 'hidden';

  // The id of the input control's focusable element, as the control reports it.
  const [inputId, setInputId] = useState<string | null>(null);

  // A field without label text has nothing to name its input with.
  const hasLabel = label !== '';
  const association = useMemo(
    () => (fieldControlId === undefined || !hasLabel ? null : fieldLabel(controlId, fieldControlId, setInputId)),
    [controlId, fieldControlId, hasLabel]
  );
  const handleLabelClick = useCallback(
    (event: React.MouseEvent) => {
      if (inputId !== null) {
        focusFieldInput(event, inputId);
      }
    },
    [inputId]
  );

  const hasError = error != null;
  const hasWarnings = warnings != null && warnings.length > 0;

  // What the label says about itself: the rich content the server holds under the tooltip key,
  // or - the common case of a one-sentence description - the text the state already carries, so
  // that hovering the label costs no round trip. Offered in view mode as well as in edit mode.
  const labelTooltip: Record<string, string> = {};
  if (hasTooltip) {
    labelTooltip[TOOLTIP_ATTR] = 'key:tooltip';
  } else if (tooltipText) {
    labelTooltip[TOOLTIP_ATTR] = `text:${tooltipText}`;
  }

  const className = [
    'tlFormField',
    `tlFormField--${labelPos}`,
    readOnly ? 'tlFormField--readonly' : '',
    fullLine ? 'tlFormField--fullLine' : '',
    hasError ? 'tlFormField--error' : '',
    !hasError && hasWarnings ? 'tlFormField--warning' : '',
    dirty ? 'tlFormField--dirty' : '',
  ].filter(Boolean).join(' ');

  // An invisible field is hidden via CSS instead of not being rendered: unmounting the child
  // control would drop its SSE subscription, so state patches arriving while hidden (e.g.
  // editable toggling with the form mode) would be lost until a full re-serialization.
  return (
    <div id={controlId} className={rootClassName(state, className)} style={visible ? undefined : { display: 'none' }}>
      {!labelHidden && (
        <div className="tlFormField__label">
          <label id={association?.labelId} htmlFor={inputId ?? undefined} className="tlFormField__labelText"
            onClick={handleLabelClick} {...labelTooltip}>{label}</label>
          {required && !readOnly && <span className="tlFormField__required">*</span>}
          {dirty && <span className="tlFormField__dirtyDot" />}
          {helpText && !readOnly && (
            <button type="button" className="tlFormField__helpIcon" onClick={toggleHelp}
              aria-label={i18n['js.formField.help']} {...tooltipProps(i18n['js.formField.help'])}>
              <svg viewBox="0 0 16 16" width="14" height="14" aria-hidden="true">
                <circle cx="8" cy="8" r="7" fill="none" stroke="currentColor" strokeWidth="1.5" />
                <text x="8" y="12" textAnchor="middle" fontSize="10"
                  fill="currentColor">?</text>
              </svg>
            </button>
          )}
        </div>
      )}
      {labelHidden && association !== null && (
        <span id={association?.labelId} className="tlVisuallyHidden">{label}</span>
      )}
      <div className="tlFormField__input">
        <FieldLabelContext.Provider value={association}>
          <TLChild control={field} />
        </FieldLabelContext.Provider>
      </div>
      {!readOnly && hasError && (
        <div className="tlFormField__error" role="alert">
          <FontIcon image={errorIcon} className="tlFormField__errorIcon" />
          <span>{error}</span>
        </div>
      )}
      {!readOnly && !hasError && hasWarnings && (
        <div className="tlFormField__warnings" aria-live="polite">
          {warnings.map((msg, i) => (
            <div key={i} className="tlFormField__warning">
              <FontIcon image={warningIcon} className="tlFormField__warningIcon" />
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
