/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.util.stream.Collectors;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.TooltipContent;
import com.top_logic.layout.react.TooltipProvider;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.util.Resources;

/**
 * A {@link ReactControl} that renders field anatomy chrome (label, required indicator, error,
 * help text, dirty indicator) around a child field control via the {@code TLFormField} React
 * component.
 *
 * <p>
 * State:
 * </p>
 * <ul>
 * <li>{@code label} - the field label text</li>
 * <li>{@code required} - whether the field is required</li>
 * <li>{@code error} - error message, or {@code null}</li>
 * <li>{@code errorIcon} - encoded theme icon displayed in front of the error message</li>
 * <li>{@code warningIcon} - encoded theme icon displayed in front of each warning message</li>
 * <li>{@code helpText} - help/description text, or {@code null}</li>
 * <li>{@code dirty} - whether the field has been modified</li>
 * <li>{@code labelPosition} - a {@link LabelPosition}, or {@code null} (inherit from layout)</li>
 * <li>{@code fullLine} - whether the field spans the full grid row</li>
 * <li>{@code visible} - whether the field is visible</li>
 * <li>{@code field} - the child field control descriptor</li>
 * </ul>
 */
public class ReactFormFieldChromeControl extends ReactControl implements TooltipProvider {

	private static final String REACT_MODULE = "TLFormField";

	private static final String LABEL = "label";

	private static final String REQUIRED = "required";

	private static final String ERROR = "error";

	private static final String ERROR_ICON = "errorIcon";

	private static final String WARNINGS = "warnings";

	private static final String WARNING_ICON = "warningIcon";

	private static final String HELP_TEXT = "helpText";

	private static final String DIRTY = "dirty";

	private static final String LABEL_POSITION = "labelPosition";

	private static final String FULL_LINE = "fullLine";

	private static final String VISIBLE = "visible";

	private static final String FIELD = "field";

	private static final String HAS_TOOLTIP = "hasTooltip";

	/** Key expected by {@link #getTooltipContent(String)}. */
	private static final String TOOLTIP_KEY = "tooltip";

	private ReactControl _field;

	/** Detaches {@link #followField()}'s listener, or {@code null} while no field carries one. */
	private Runnable _fieldBinding;

	private String _agentName;

	private String _tooltipHtml;

	private String _tooltipCaption;

	private boolean _tooltipInteractive;

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
		putState(ERROR_ICON, Icons.VALIDATION_ERROR.resolve().toEncodedForm());
		putState(WARNING_ICON, Icons.VALIDATION_WARNING.resolve().toEncodedForm());
		setLabel(label);
		setRequired(required);
		setDirty(dirty);
		setError(error);
		setHelpText(helpText);
		setLabelPosition(labelPosition);
		putState(FULL_LINE, fullLine);
		setVisible(visible);
		putState(FIELD, field);
		followField();
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
	 */
	private void followField() {
		if (_fieldBinding != null) {
			_fieldBinding.run();
			_fieldBinding = null;
		}
		if (!(_field instanceof ReactFormFieldControl fieldControl)) {
			return;
		}
		FieldModel model = fieldControl.getFieldModel();
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
		putState(LABEL, label);
	}

	/**
	 * Updates the label position.
	 *
	 * @param labelPosition
	 *        The new {@link LabelPosition}, or {@code null} to inherit from the enclosing layout.
	 */
	public void setLabelPosition(LabelPosition labelPosition) {
		putState(LABEL_POSITION, labelPosition == null ? null : labelPosition.protocolName());
	}

	/**
	 * Updates whether this field spans the full grid row.
	 */
	public void setFullLine(boolean fullLine) {
		putState(FULL_LINE, fullLine);
	}

	/**
	 * Updates the help text.
	 *
	 * @param helpText
	 *        The new help text, or {@code null} to clear.
	 */
	public void setHelpText(String helpText) {
		putState(HELP_TEXT, helpText);
	}

	/**
	 * Updates the error message.
	 *
	 * @param error
	 *        The error message, or {@code null} to clear.
	 */
	public void setError(String error) {
		putState(ERROR, error);
	}

	/**
	 * Updates the warning messages.
	 *
	 * @param warnings
	 *        The warning messages, or {@code null} to clear.
	 */
	public void setWarnings(java.util.List<String> warnings) {
		putState(WARNINGS, warnings);
	}

	/**
	 * Updates the dirty state.
	 *
	 * @param dirty
	 *        Whether the field has been modified.
	 */
	public void setDirty(boolean dirty) {
		putState(DIRTY, dirty);
	}

	/**
	 * Updates visibility.
	 *
	 * @param visible
	 *        Whether the field is visible.
	 */
	public void setVisible(boolean visible) {
		putState(VISIBLE, visible);
	}

	/**
	 * Updates the required state.
	 *
	 * @param required
	 *        Whether the field is required.
	 */
	public void setRequired(boolean required) {
		putState(REQUIRED, required);
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
		putState(FIELD, field);
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
		putState(HAS_TOOLTIP, _tooltipHtml != null);
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
