import {
  React, useTLState, useI18N, TLChild, ThemeIcon, FieldLabelContext, fieldLabel, focusFieldInput, rootClassName,
  tooltipProps, useFormLayout, TOOLTIP_ATTR,
} from 'tl-react-bridge';
import type { TLCellProps, FormFieldStateJson } from 'tl-react-bridge';
import FormControl from '@mui/material/FormControl';
import FormLabel from '@mui/material/FormLabel';
import FormHelperText from '@mui/material/FormHelperText';
import IconButton from '@mui/material/IconButton';
import type { SxProps, Theme } from '@mui/material/styles';

const { useCallback, useMemo, useState } = React;

const I18N_KEYS = {
  'js.formField.help': 'Help',
};

/** The icon of the button showing the help text (the theme image TLFormField uses). */
const HELP_ICON = 'css:fa-regular fa-circle-question';

/** Size class of the design system for the icons of the field. */
const ICON_CLASS = 'tl-icon-sm';

/** The value of {@link TOOLTIP_ATTR} fetching the rich tooltip the server holds for the control. */
const RICH_TOOLTIP = 'key:tooltip';

/** The value of {@link TOOLTIP_ATTR} declaring a plain text as tooltip, followed by the text. */
const TEXT_TOOLTIP_PREFIX = 'text:';

/**
 * The root of the field keeps the grid of the design system's `tl-form-field`: FormControl brings
 * `inline-flex`, which would take precedence over the class, since the MUI styles come later.
 */
const ROOT_SX: SxProps<Theme> = { display: 'grid' };

/** A message (error, warning, help text) as a row: its icon in front of its text. */
const MESSAGE_SX = { display: 'flex', alignItems: 'flex-start', gap: 0.5, m: 0 } as const;

/** A warning: a message in the warning color. */
const WARNING_SX = { ...MESSAGE_SX, color: 'warning.main' } as const;

/**
 * Renders the state of a TopLogic form field (module name `TLFormField`) with the MUI
 * `FormControl`, `FormLabel` and `FormHelperText`, the input control rendered through
 * {@link TLChild} inside.
 *
 * <p>The field keeps the element structure and the classes of the design system's form field,
 * which place label, input and messages in its grid and the field in the grid of the form layout:
 * the root `tl-form-field` with the modifier of the label position (`--side`, `--top`, `--after`,
 * `--hidden`) and `--full`, and the parts `__label`, `__input`, `__message` and `__help-text`. The
 * MUI components are rendered inside these parts; the root is the `FormControl` itself, kept a
 * grid.</p>
 *
 * <p>Mapping from the control state:</p>
 * <ul>
 * <li>label → `FormLabel` referring to the focusable element of the input (`htmlFor`), whose
 *     click focuses an input HTML does not activate from a label ({@link focusFieldInput});
 *     tooltipText or hasTooltip → the tooltip of the label;</li>
 * <li>required → `required` of the `FormLabel` (its asterisk);</li>
 * <li>error → a `FormHelperText` with `error`, announced as an alert, with errorIcon in front;
 *     the `FormLabel` takes the error color as well;</li>
 * <li>warnings (while there is no error) → one `FormHelperText` per warning, announced politely,
 *     with warningIcon in front;</li>
 * <li>helpText → an `IconButton` beside the label toggling a `FormHelperText` below the input;</li>
 * <li>dirty → the dot of the design system beside the label;</li>
 * <li>labelPosition → the modifier class; `hidden` keeps the label off the screen, still naming
 *     the input; fullLine → `tl-form-field--full`;</li>
 * <li>visible `false` → the `hidden` attribute: the input control stays mounted and keeps
 *     receiving its updates;</li>
 * <li>field → the input control, given the label association through
 *     {@link FieldLabelContext} exactly as TLFormField gives it: the label names the input, the
 *     error, each warning and the shown help text describe it;</li>
 * <li>the configured CSS class → className of the root.</li>
 * </ul>
 *
 * <p>Whether the enclosing form is read-only and the label position of a field that states none
 * come from the form layout ({@link useFormLayout}), as in TLFormField: a read-only form shows no
 * required mark, messages or help.</p>
 */
const MuiFormFieldAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<FormFieldStateJson>>();
  const i18n = useI18N(I18N_KEYS);
  const formLayout = useFormLayout();

  const field = state.field;
  const fieldControlId = field?.controlId;
  const readOnly = formLayout.readOnly;
  const labelPos = state.labelPosition ?? formLayout.resolvedLabelPosition;
  const labelHidden = labelPos === 'hidden';

  const label = state.label ?? '';
  const error = state.error ?? null;
  const warnings = state.warnings ?? null;
  const helpText = state.helpText ?? null;
  const dirty = state.dirty === true;
  const visible = state.visible !== false;

  const [helpVisible, setHelpVisible] = useState(false);
  const toggleHelp = useCallback(() => setHelpVisible(shown => !shown), []);

  // Messages, help and the required mark belong to editing: a read-only form shows values only.
  // An error displaces the warnings.
  const showError = !readOnly && error != null;
  const showWarnings = !readOnly && error == null && warnings != null && warnings.length > 0;
  const showHelp = !readOnly && !!helpText;
  const required = !readOnly && state.required === true;
  const errorId = `${controlId}-error`;
  const warningId = (index: number) => `${controlId}-warning-${index}`;
  const helpTextId = `${controlId}-help`;
  const warningCount = showWarnings ? warnings.length : 0;
  const describedBy = [
    showError ? errorId : '',
    ...Array.from({ length: warningCount }, (_, index) => warningId(index)),
    showHelp && helpVisible ? helpTextId : '',
  ].filter(Boolean).join(' ') || undefined;

  // The id of the focusable element of the input control, as the control reports it.
  const [inputId, setInputId] = useState<string | null>(null);

  // A field without label text has nothing to name its input with.
  const hasLabel = label !== '';
  const association = useMemo(
    () => (fieldControlId === undefined || !hasLabel
      ? null : fieldLabel(controlId, fieldControlId, setInputId, describedBy)),
    [controlId, fieldControlId, hasLabel, describedBy],
  );

  const handleLabelClick = useCallback((event: React.MouseEvent) => {
    if (inputId !== null) {
      focusFieldInput(event, inputId);
    }
  }, [inputId]);

  const labelTooltip: Record<string, string> = {};
  if (state.hasTooltip === true) {
    labelTooltip[TOOLTIP_ATTR] = RICH_TOOLTIP;
  } else if (state.tooltipText) {
    labelTooltip[TOOLTIP_ATTR] = TEXT_TOOLTIP_PREFIX + state.tooltipText;
  }

  const className = rootClassName(
    state,
    'tl-form-field',
    `tl-form-field--${labelPos}`,
    state.fullLine === true && 'tl-form-field--full',
  );

  return (
    <FormControl id={controlId} component="div" className={className} sx={ROOT_SX}
      data-tl-state={dirty ? 'dirty' : undefined} hidden={!visible || undefined}>
      {!labelHidden && (
        <div className="tl-form-field__label">
          <FormLabel id={association?.labelId} htmlFor={inputId ?? undefined} onClick={handleLabelClick}
            required={required} error={showError} focused={false} {...labelTooltip}>
            {label}
          </FormLabel>
          {dirty && <span className="tl-form-field__dirty" aria-hidden="true" />}
          {showHelp && (
            <IconButton size="small" onClick={toggleHelp} aria-label={i18n['js.formField.help']}
              aria-expanded={helpVisible} aria-controls={helpTextId} {...tooltipProps(i18n['js.formField.help'])}>
              <ThemeIcon encoded={HELP_ICON} className={ICON_CLASS} />
            </IconButton>
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
        <div className="tl-form-field__message">
          <FormHelperText id={errorId} component="div" role="alert" error variant="standard" sx={MESSAGE_SX}>
            {typeof state.errorIcon === 'string' && <ThemeIcon encoded={state.errorIcon} className={ICON_CLASS} />}
            <span>{error}</span>
          </FormHelperText>
        </div>
      )}
      {showWarnings && warnings.map((message, index) => (
        <div key={index} className="tl-form-field__message">
          <FormHelperText id={warningId(index)} component="div" aria-live="polite" variant="standard"
            sx={WARNING_SX}>
            {typeof state.warningIcon === 'string' && <ThemeIcon encoded={state.warningIcon} className={ICON_CLASS} />}
            <span>{message}</span>
          </FormHelperText>
        </div>
      ))}
      {showHelp && (
        <div className="tl-form-field__help-text" hidden={!helpVisible}>
          <FormHelperText id={helpTextId} component="div" variant="standard" sx={MESSAGE_SX}>{helpText}</FormHelperText>
        </div>
      )}
    </FormControl>
  );
};

export default MuiFormFieldAdapter;
