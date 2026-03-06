/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import com.top_logic.layout.react.ReactControl;

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
 * <li>{@code helpText} - help/description text, or {@code null}</li>
 * <li>{@code dirty} - whether the field has been modified</li>
 * <li>{@code labelPosition} - "side", "top", or {@code null} (inherit from layout)</li>
 * <li>{@code fullLine} - whether the field spans the full grid row</li>
 * <li>{@code visible} - whether the field is visible</li>
 * <li>{@code field} - the child field control descriptor</li>
 * </ul>
 */
public class ReactFormFieldChromeControl extends ReactControl {

	private static final String REACT_MODULE = "TLFormField";

	private static final String LABEL = "label";

	private static final String REQUIRED = "required";

	private static final String ERROR = "error";

	private static final String HELP_TEXT = "helpText";

	private static final String DIRTY = "dirty";

	private static final String LABEL_POSITION = "labelPosition";

	private static final String FULL_LINE = "fullLine";

	private static final String VISIBLE = "visible";

	private static final String FIELD = "field";

	private ReactControl _field;

	/**
	 * Creates a form field chrome wrapper.
	 *
	 * @param label
	 *        The field label text.
	 * @param field
	 *        The child field control to wrap.
	 */
	public ReactFormFieldChromeControl(String label, ReactControl field) {
		this(label, false, false, null, null, null, false, true, field);
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
	 *        "side", "top", or {@code null} to inherit from layout.
	 * @param fullLine
	 *        Whether the field spans the full grid row.
	 * @param visible
	 *        Whether the field is visible.
	 * @param field
	 *        The child field control.
	 */
	public ReactFormFieldChromeControl(String label, boolean required, boolean dirty,
			String error, String helpText, String labelPosition,
			boolean fullLine, boolean visible, ReactControl field) {
		super(null, REACT_MODULE);
		_field = field;
		putState(LABEL, label);
		putState(REQUIRED, required);
		putState(DIRTY, dirty);
		if (error != null) {
			putState(ERROR, error);
		}
		if (helpText != null) {
			putState(HELP_TEXT, helpText);
		}
		if (labelPosition != null) {
			putState(LABEL_POSITION, labelPosition);
		}
		putState(FULL_LINE, fullLine);
		putState(VISIBLE, visible);
		putState(FIELD, field);
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
	}

	@Override
	protected void cleanupChildren() {
		if (_field != null) {
			_field.cleanupTree();
		}
	}

}
