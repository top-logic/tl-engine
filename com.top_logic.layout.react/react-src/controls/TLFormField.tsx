import {
  React, useTLState, TLChild, rootClassName, useI18N, tooltipProps, TOOLTIP_ATTR, FieldLabelContext, fieldLabel,
  focusFieldInput, ThemeIcon,
} from 'tl-react-bridge';
import type { TLCellProps, FormFieldStateJson } from 'tl-react-bridge';
import { buttonClassName } from './button/ButtonDefaults';
import { FormLayoutContext } from './FormLayoutContext';

const { useContext, useState, useCallback, useMemo } = React;

const I18N_KEYS = {
  'js.formField.help': 'Help',
};

const HELP_ICON = 'css:fa-regular fa-circle-question';

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
 * in a visually hidden `label` that still names the input. The input area itself is a plain
 * element, so a click into the input reaches exactly the element under the pointer. The error
 * message, each warning message and the shown help text describe the input (`aria-describedby`,
 * through the same association), in this order.
 *
 * The state is described by FormFieldStateJson.
 */
const TLFormField: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<FormFieldStateJson>>();
  const ctx = useContext(FormLayoutContext);
  const i18n = useI18N(I18N_KEYS);

  const label = state.label ?? '';
  const required = state.required === true;
  const error = state.error ?? null;
  const errorIcon = state.errorIcon;
  const warnings = state.warnings ?? null;
  const warningIcon = state.warningIcon;
  const helpText = state.helpText ?? null;
  const dirty = state.dirty === true;
  const labelPos = state.labelPosition ?? ctx.resolvedLabelPosition;
  const fullLine = state.fullLine === true;
  const visible = state.visible !== false;
  const hasTooltip = state.hasTooltip === true;
  const tooltipText = state.tooltipText ?? null;
  const field = state.field;
  const fieldControlId = field?.controlId;
  const readOnly = ctx.readOnly;

  const [helpVisible, setHelpVisible] = useState(false);
  const toggleHelp = useCallback(() => setHelpVisible(v => !v), []);

  const labelHidden = labelPos === 'hidden';

  const hasError = error != null;
  const hasWarnings = warnings != null && warnings.length > 0;

  // Messages, help and the required star belong to editing: a read-only form shows values only.
  // An error displaces the warnings.
  const showError = !readOnly && hasError;
  const showWarnings = !readOnly && !hasError && hasWarnings;
  const showHelp = !readOnly && !!helpText;
  const errorId = `${controlId}-error`;
  const warningId = (i: number) => `${controlId}-warning-${i}`;
  const helpTextId = `${controlId}-help`;
  const warningCount = showWarnings ? warnings.length : 0;
  const describedBy = [
    showError ? errorId : '',
    ...Array.from({ length: warningCount }, (_, i) => warningId(i)),
    showHelp && helpVisible ? helpTextId : '',
  ].filter(Boolean).join(' ') || undefined;

  // The id of the input control's focusable element, as the control reports it.
  const [inputId, setInputId] = useState<string | null>(null);

  // A field without label text has nothing to name its input with.
  const hasLabel = label !== '';
  const association = useMemo(
    () => (fieldControlId === undefined || !hasLabel
      ? null : fieldLabel(controlId, fieldControlId, setInputId, describedBy)),
    [controlId, fieldControlId, hasLabel, describedBy]
  );
  const handleLabelClick = useCallback(
    (event: React.MouseEvent) => {
      if (inputId !== null) {
        focusFieldInput(event, inputId);
      }
    },
    [inputId]
  );

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
    'tl-form-field',
    `tl-form-field--${labelPos}`,
    fullLine ? 'tl-form-field--full' : '',
  ].filter(Boolean).join(' ');

  // An invisible field is hidden via the `hidden` attribute instead of not being rendered:
  // unmounting the child control would drop its SSE subscription, so state patches arriving while
  // hidden (e.g. editable toggling with the form mode) would be lost until a full re-serialization.
  // The design system's `.tl-form-field[hidden]` keeps it hidden against the block's own display.
  return (
    <div id={controlId} className={rootClassName(state, className)} data-tl-state={dirty ? 'dirty' : undefined}
      hidden={!visible || undefined}>
      {!labelHidden && (
        <div className="tl-form-field__label tl-type-label">
          <label id={association?.labelId} htmlFor={inputId ?? undefined} className="tl-form-field__label-text"
            onClick={handleLabelClick} {...labelTooltip}>{label}</label>
          {required && !readOnly && <span className="tl-form-field__required" aria-hidden="true">*</span>}
          {dirty && <span className="tl-form-field__dirty" aria-hidden="true" />}
          {showHelp && (
            <button type="button" className={`${buttonClassName({ appearance: 'ghost', small: true, icon: true })} tl-form-field__help`} onClick={toggleHelp}
              aria-label={i18n['js.formField.help']} aria-expanded={helpVisible} aria-controls={helpTextId}
              {...tooltipProps(i18n['js.formField.help'])}>
              <ThemeIcon encoded={HELP_ICON} className="tl-button__icon tl-icon-sm" />
            </button>
          )}
        </div>
      )}
      {labelHidden && association !== null && (
        <label id={association.labelId} htmlFor={inputId ?? undefined} className="tl-visually-hidden">{label}</label>
      )}
      <div className="tl-form-field__input">
        <FieldLabelContext.Provider value={association}>
          <TLChild control={field} />
        </FieldLabelContext.Provider>
      </div>
      {showError && (
        <div id={errorId} className="tl-form-field__message tl-type-label" role="alert">
          {typeof errorIcon === 'string' && <ThemeIcon encoded={errorIcon} className="tl-icon-sm" />}
          <span>{error}</span>
        </div>
      )}
      {showWarnings && warnings.map((msg, i) => (
        <div key={i} id={warningId(i)} className="tl-form-field__message tl-type-label" aria-live="polite">
          {typeof warningIcon === 'string' && <ThemeIcon encoded={warningIcon} className="tl-icon-sm" />}
          <span>{msg}</span>
        </div>
      ))}
      {showHelp && (
        <div id={helpTextId} className="tl-form-field__help-text tl-type-label" hidden={!helpVisible}>{helpText}</div>
      )}
    </div>
  );
};

export default TLFormField;
