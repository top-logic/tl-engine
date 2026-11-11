/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.util.stream.Collectors;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.ReactImages;
import com.top_logic.layout.react.TooltipContent;
import com.top_logic.layout.react.TooltipProvider;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.state.FormFieldState;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;

/**
 * A {@link ReactControl} that renders field anatomy chrome (label, required indicator, error,
 * help text, dirty indicator) around a child field control via the {@code TLFormField} React
 * component.
 *
 * <p>
 * The state is described by {@link FormFieldState}.
 * </p>
 */
public class ReactFormFieldChromeControl extends ReactControl implements TooltipProvider {

	private static final String REACT_MODULE = "TLFormField";

	/** Key expected by {@link #getTooltipContent(String)}. */
	private static final String TOOLTIP_KEY = "tooltip";

	/**
	 * Command removing the value entered in the field, so that its inherited value applies again.
	 *
	 * @see #setResetAction(Runnable)
	 */
	public static final String RESET_COMMAND = "resetValue";

	/** What {@link #RESET_COMMAND} does, {@code null} while the field offers no reset. */
	private Runnable _resetAction;

	private ReactControl _field;

	/** Detaches {@link #followField()}'s listener, or {@code null} while no field carries one. */
	private Runnable _fieldBinding;

	/**
	 * The model whose validation is drawn, as {@link #setFieldModel(FieldModel) given}, or
	 * {@code null} for the one of the {@link #setField(ReactControl) field control}.
	 */
	private FieldModel _fieldModel;

	private String _agentName;

	private String _tooltipHtml;

	private String _tooltipCaption;

	private boolean _tooltipInteractive;

	/** The class {@link #setCssClass(String) configured} for this field, {@code null} for none. */
	private String _configuredCssClass;

	/** The class {@link #setStateCssClass(String) marking the state} of the value, {@code null} for none. */
	private String _stateCssClass;

	/**
	 * Creates a form field chrome wrapper.
	 *
	 * @param label
	 *        The field label text.
	 * @param field
	 *        The child field control to wrap.
	 */
	public ReactFormFieldChromeControl(ReactContext context, String label, ReactControl field) {
		this(context, label, false, false, null, null, null, false, true, field);
	}

	/**
	 * Sets a stable, locale-independent name for this field (its technical attribute name) used as
	 * the headless interface address discriminator.
	 *
	 * <p>
	 * The display {@code label} varies by locale and by whether an object is loaded (the placeholder
	 * form uses the attribute name, a loaded form uses the localized label), which would make the
	 * agent address unstable. The agent name pins the address to the technical field name regardless.
	 * </p>
	 *
	 * @param scriptingName
	 *        The technical field name, or {@code null} to fall back to label-based naming.
	 */
	public void setAgentName(String scriptingName) {
		_agentName = scriptingName;
	}

	@Override
	public String scriptingName() {
		return _agentName;
	}

	/**
	 * Creates a form field chrome wrapper with full configuration.
	 *
	 * @param label
	 *        The field label text.
	 * @param required
	 *        Whether the field is required.
	 * @param dirty
	 *        Whether the field has been modified.
	 * @param error
	 *        Error message, or {@code null}.
	 * @param helpText
	 *        Help text, or {@code null}.
	 * @param labelPosition
	 *        The {@link LabelPosition}, or {@code null} to inherit from the layout.
	 * @param fullLine
	 *        Whether the field spans the full grid row.
	 * @param visible
	 *        Whether the field is visible.
	 * @param field
	 *        The child field control.
	 */
	public ReactFormFieldChromeControl(ReactContext context, String label, boolean required, boolean dirty,
			String error, String helpText, LabelPosition labelPosition,
			boolean fullLine, boolean visible, ReactControl field) {
		super(context, null, REACT_MODULE);
		_field = field;
		putState(FormFieldState.ERROR_ICON__PROP, ReactImages.encode(context, Icons.VALIDATION_ERROR));
		putState(FormFieldState.WARNING_ICON__PROP, ReactImages.encode(context, Icons.VALIDATION_WARNING));
		setLabel(label);
		setRequired(required);
		setDirty(dirty);
		setError(error);
		setHelpText(helpText);
		setLabelPosition(labelPosition);
		putState(FormFieldState.FULL_LINE__PROP, fullLine);
		putState(FormFieldState.COMPACT__PROP, false);
		setVisible(visible);
		putState(FormFieldState.FIELD__PROP, field);
		followField();
	}

	/**
	 * Offers an action beside the field that removes the value entered in it, so that the value the
	 * field inherits applies again.
	 *
	 * <p>
	 * The action is one for all kinds of input: it also serves an input that has no empty state of
	 * its own to return to, such as a checkbox or a slider.
	 * </p>
	 *
	 * @param action
	 *        What resetting the field does, {@code null} to offer no reset.
	 */
	public void setResetAction(Runnable action) {
		_resetAction = action;
		putState(FormFieldState.RESET_LABEL__PROP,
			action == null ? null : Resources.getInstance().getString(I18NConstants.RESET_TO_INHERITED));
	}

	/**
	 * Handles {@link #RESET_COMMAND}: runs the {@link #setResetAction(Runnable) reset action}, if
	 * one is offered.
	 */
	@ReactCommandHandler(RESET_COMMAND)
	void handleReset() {
		Runnable action = _resetAction;
		if (action != null) {
			action.run();
		}
	}

	/**
	 * Sets the CSS class configured for this field.
	 *
	 * <p>
	 * The configured class stands beside the class {@link #setStateCssClass(String) marking the
	 * state} of the displayed value, so that neither replaces the other.
	 * </p>
	 */
	@Override
	public void setCssClass(String cssClass) {
		_configuredCssClass = cssClass;
		updateCssClass();
	}

	/**
	 * Sets a CSS class marking the state of the displayed value, e.g. whether it is entered
	 * explicitly or computed as fallback.
	 *
	 * <p>
	 * The class is put on the field container beside the {@link #setCssClass(String) configured}
	 * one.
	 * </p>
	 *
	 * @param cssClass
	 *        The class, or {@code null} for none.
	 */
	public void setStateCssClass(String cssClass) {
		_stateCssClass = cssClass;
		updateCssClass();
	}

	private void updateCssClass() {
		String combined;
		if (_configuredCssClass == null) {
			combined = _stateCssClass;
		} else if (_stateCssClass == null) {
			combined = _configuredCssClass;
		} else {
			combined = _configuredCssClass + ' ' + _stateCssClass;
		}
		super.setCssClass(combined);
	}

	/**
	 * Follows the field's own validation state, so that what it rejects is drawn here - under the
	 * field, with the error icon - rather than only marked on the input itself.
	 *
	 * <p>
	 * The chrome is the only place a message can appear: the input control shows a border and sets
	 * {@code aria-invalid}, but has nowhere to put words. Left to each caller to wire, this is
	 * simply forgotten - it was, for every field of a configuration, and for the polymorphic type
	 * selector - and the field then carries an error that only its border betrays. Doing it here
	 * means a chrome cannot be built without it.
	 * </p>
	 *
	 * <p>
	 * Only the error and the warnings are followed, not {@link #setRequired(boolean)}: several
	 * callers pass a required state of their own that need not be the field's, and taking that over
	 * would change what they display. A field without a {@link FieldModel} - a bare control wrapped
	 * for its label alone - is left as its caller set it.
	 * </p>
	 *
	 * @see #setFieldModel(FieldModel)
	 */
	private void followField() {
		if (_fieldBinding != null) {
			_fieldBinding.run();
			_fieldBinding = null;
		}
		FieldModel model = followedModel();
		if (model == null) {
			return;
		}
		FieldModelListener listener = new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				// The value belongs to the input control; the chrome only frames it.
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				// Likewise handled by the input control itself.
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				showValidation(source);
			}
		};
		model.addListener(listener);
		_fieldBinding = () -> model.removeListener(listener);

		// A field can already carry an error when its chrome is built - a form rebuilt after a
		// refused apply re-creates every control while the errors that refusal placed still stand.
		showValidation(model);
	}

	/**
	 * The model whose error and warnings are drawn: the one {@link #setFieldModel(FieldModel)
	 * given}, else the one of a {@link ReactFormFieldControl} in the field, else none.
	 */
	private FieldModel followedModel() {
		if (_fieldModel != null) {
			return _fieldModel;
		}
		if (_field instanceof ReactFormFieldControl fieldControl) {
			return fieldControl.getFieldModel();
		}
		return null;
	}

	/**
	 * Sets the model whose error and warnings this field draws.
	 *
	 * <p>
	 * Needed for a field control that edits a {@link FieldModel} without being a
	 * {@link ReactFormFieldControl}, e.g. a control composed of a preview and a button opening an
	 * editor: the chrome cannot find the model of such a control on its own. Without a model given
	 * here, the chrome follows the model of a {@link ReactFormFieldControl} in its field.
	 * </p>
	 *
	 * @param model
	 *        The model of the field, {@code null} for the one of the field control.
	 */
	public void setFieldModel(FieldModel model) {
		_fieldModel = model;
		followField();
	}

	/** Draws the given field's error and warnings, or clears them. */
	private void showValidation(FieldModel model) {
		Resources resources = Resources.getInstance();
		setError(model.hasError() ? resources.getString(model.getError()) : null);
		setWarnings(model.hasWarnings()
			? model.getWarnings().stream().map(resources::getString).collect(Collectors.toList())
			: null);
	}

	@Override
	protected void onCleanup() {
		if (_fieldBinding != null) {
			_fieldBinding.run();
			_fieldBinding = null;
		}
		super.onCleanup();
	}

	/**
	 * Updates the label text.
	 *
	 * @param label
	 *        The new label text.
	 */
	public void setLabel(String label) {
		putState(FormFieldState.LABEL__PROP, label);
	}

	/**
	 * Updates the label position.
	 *
	 * @param labelPosition
	 *        The new {@link LabelPosition}, or {@code null} to inherit from the enclosing layout.
	 */
	public void setLabelPosition(LabelPosition labelPosition) {
		putState(FormFieldState.LABEL_POSITION__PROP, labelPosition == null ? null : labelPosition.getExternalName());
	}

	/**
	 * Updates whether this field spans the full grid row.
	 */
	public void setFullLine(boolean fullLine) {
		putState(FormFieldState.FULL_LINE__PROP, fullLine);
	}

	/**
	 * Updates the help text.
	 *
	 * @param helpText
	 *        The new help text, or {@code null} to clear.
	 */
	public void setHelpText(String helpText) {
		putState(FormFieldState.HELP_TEXT__PROP, helpText);
	}

	/**
	 * Updates the error message.
	 *
	 * @param error
	 *        The error message, or {@code null} to clear.
	 */
	public void setError(String error) {
		putState(FormFieldState.ERROR__PROP, error);
	}

	/**
	 * Updates the warning messages.
	 *
	 * @param warnings
	 *        The warning messages, or {@code null} to clear.
	 */
	public void setWarnings(java.util.List<String> warnings) {
		putState(FormFieldState.WARNINGS__PROP, warnings);
	}

	/**
	 * Sets whether the field takes no room beside or below its input.
	 *
	 * <p>
	 * A compact field keeps the size of its input, so that it fits where the input alone would fit,
	 * e.g. into a cell of a table: the label names the input for assistive technology only, the
	 * marks for a required and a modified field and the help are left out, and the error or the
	 * warnings show as a marker at the corner of the field that opens them on a press and shows
	 * them as tooltip. See {@link FormFieldState#isCompact()}.
	 * </p>
	 *
	 * @param compact
	 *        Whether the field is displayed compact.
	 */
	public void setCompact(boolean compact) {
		putState(FormFieldState.COMPACT__PROP, compact);
	}

	/**
	 * Updates the dirty state.
	 *
	 * @param dirty
	 *        Whether the field has been modified.
	 */
	public void setDirty(boolean dirty) {
		putState(FormFieldState.DIRTY__PROP, dirty);
	}

	/**
	 * Updates visibility.
	 *
	 * @param visible
	 *        Whether the field is visible.
	 */
	public void setVisible(boolean visible) {
		putState(FormFieldState.VISIBLE__PROP, visible);
	}

	/**
	 * Whether the field is visible.
	 *
	 * @see #setVisible(boolean)
	 */
	public boolean isVisible() {
		return !Boolean.FALSE.equals(getState(FormFieldState.VISIBLE__PROP));
	}

	/**
	 * An {@link #isVisible() invisible} field is not seen, see {@link #setVisible(boolean)}.
	 */
	@Override
	public boolean scriptingVisible() {
		return isVisible() && super.scriptingVisible();
	}

	/**
	 * The {@link #RESET_COMMAND} is offered while a {@link #setResetAction(Runnable) reset action}
	 * is set and the field takes input: a read-only field shows no reset button.
	 */
	@Override
	public ExecutableState scriptingCommandState(String command) {
		if (RESET_COMMAND.equals(command)) {
			if (_resetAction == null) {
				return ExecutableState.NOT_EXEC_HIDDEN;
			}
			if (_field != null) {
				ExecutableState fieldState = _field.scriptingCommandState(ReactFormFieldControl.CMD_VALUE_CHANGED);
				if (!fieldState.isExecutable()) {
					return fieldState;
				}
			}
		}
		return super.scriptingCommandState(command);
	}

	/**
	 * Updates the required state.
	 *
	 * @param required
	 *        Whether the field is required.
	 */
	public void setRequired(boolean required) {
		putState(FormFieldState.REQUIRED__PROP, required);
	}

	/**
	 * The control of the input this chrome frames.
	 *
	 * @see #setField(ReactControl)
	 */
	public ReactControl getField() {
		return _field;
	}

	/**
	 * Replaces the child field control.
	 *
	 * @param field
	 *        The new child field control.
	 */
	public void setField(ReactControl field) {
		if (_field != null) {
			_field.cleanupTree();
		}
		_field = field;
		putState(FormFieldState.FIELD__PROP, field);
		followField();
	}

	/**
	 * Sets the rich tooltip shown on the field label. The HTML must already be sanitized (see
	 * {@code SafeHTML}); the caption is optional.
	 *
	 * @param html
	 *        Sanitized tooltip HTML, or {@code null} to clear.
	 * @param caption
	 *        Optional caption, or {@code null}.
	 */
	public void setTooltip(String html, String caption) {
		setTooltip(html, caption, false);
	}

	/**
	 * Sets the rich tooltip shown on the field label.
	 *
	 * @param html
	 *        Sanitized tooltip HTML, or {@code null} to clear.
	 * @param caption
	 *        Optional caption, or {@code null}.
	 * @param interactive
	 *        When {@code true}, the popover remains open while the pointer hovers over it, so the
	 *        user can select and copy content (e.g. {@code JavaDoc} with code snippets).
	 */
	public void setTooltip(String html, String caption, boolean interactive) {
		_tooltipHtml = (html == null || html.isEmpty()) ? null : html;
		_tooltipCaption = caption;
		_tooltipInteractive = interactive;
		putState(FormFieldState.HAS_TOOLTIP__PROP, _tooltipHtml != null);
	}

	/**
	 * Sets the plain text offered as the tooltip of the field label, in view mode as well as in
	 * edit mode.
	 *
	 * <p>
	 * A one-sentence description of what the field holds is shown from this text alone, without
	 * the round trip {@link #setTooltip(String, String, boolean)} takes to fetch its markup. A
	 * rich tooltip set there wins wherever both are given.
	 * </p>
	 *
	 * @param text
	 *        The description of the field, or {@code null} to clear.
	 */
	public void setTooltipText(String text) {
		putState(FormFieldState.TOOLTIP_TEXT__PROP, (text == null || text.isEmpty()) ? null : text);
	}

	@Override
	public TooltipContent getTooltipContent(String key) {
		if (!TOOLTIP_KEY.equals(key)) {
			return null;
		}
		if (_tooltipHtml == null) {
			return null;
		}
		return new TooltipContent(_tooltipHtml, _tooltipCaption, _tooltipInteractive);
	}

}
