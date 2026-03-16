/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.util.Map;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.form.model.FormFieldAdapter;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.util.Resources;

/**
 * A form field control that renders via a React component.
 *
 * <p>
 * Extends {@link ReactControl} so it can be composed with other React controls (e.g. as a child of
 * {@link com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl}). Listens to
 * {@link FieldModel} property changes and delivers incremental patches via SSE.
 * </p>
 *
 * <p>
 * On initial render, the full field state (value, editable, mandatory, errors, label, tooltip) is
 * sent as JSON. Subsequent field changes are delivered as incremental patches via SSE.
 * </p>
 */
public class ReactFormFieldControl extends ReactControl {

	/** State key for the field value. */
	protected static final String VALUE = "value";

	/** State key for whether the field is editable. */
	protected static final String EDITABLE = "editable";

	/** State key for whether the field is mandatory. */
	protected static final String MANDATORY = "mandatory";

	/** State key for whether the field has a validation error. */
	protected static final String HAS_ERROR = "hasError";

	/** State key for the error message text. */
	protected static final String ERROR_MESSAGE = "errorMessage";

	/** State key for whether the field has validation warnings. */
	protected static final String HAS_WARNINGS = "hasWarnings";

	/** State key for the field label. */
	protected static final String LABEL = "label";

	/** State key for the tooltip text. */
	protected static final String TOOLTIP = "tooltip";

	/** State key for whether the control is hidden on the client. */
	protected static final String HIDDEN = "hidden";

	private final FieldModel _fieldModel;

	private FieldModelListener _modelListener;

	/**
	 * Creates a new {@link ReactFormFieldControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The field model.
	 * @param reactModule
	 *        The React module identifier (e.g. "TLTextInput").
	 */
	protected ReactFormFieldControl(ReactContext context, FieldModel model, String reactModule) {
		super(context, model, reactModule);
		_fieldModel = model;
		initFieldState();
		registerModelListeners();
	}

	@Override
	protected void onCleanup() {
		if (_modelListener != null) {
			_fieldModel.removeListener(_modelListener);
			_modelListener = null;
		}
	}

	/**
	 * Populates the initial React state from the {@link FieldModel}.
	 *
	 * <p>
	 * Called from the constructor before the control is attached to any SSE queue, so all
	 * {@link #putState} calls simply store values in the pre-render state map.
	 * </p>
	 */
	private void initFieldState() {
		putState(VALUE, _fieldModel.getValue());
		putState(EDITABLE, _fieldModel.isEditable());
		putState(MANDATORY, _fieldModel.isMandatory());
		putState(HAS_ERROR, _fieldModel.hasError());
		putState(HAS_WARNINGS, _fieldModel.hasWarnings());
		if (_fieldModel.hasError()) {
			putState(ERROR_MESSAGE, Resources.getInstance().getString(_fieldModel.getError()));
		}
		// Display properties from FormFieldAdapter.
		if (_fieldModel instanceof FormFieldAdapter) {
			FormFieldAdapter adapter = (FormFieldAdapter) _fieldModel;
			putState(LABEL, adapter.getLabel());
			putState(TOOLTIP, adapter.getTooltip());
			putState(HIDDEN, Boolean.valueOf(!adapter.isVisible()));
		}
	}

	/**
	 * Registers a {@link FieldModelListener} that pushes incremental state patches to the React
	 * client whenever the model changes.
	 */
	private void registerModelListeners() {
		_modelListener = new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				handleModelValueChanged(source, oldValue, newValue);
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				putState(EDITABLE, editable);
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				putState(HAS_ERROR, source.hasError());
				putState(HAS_WARNINGS, source.hasWarnings());
				if (source.hasError()) {
					putState(ERROR_MESSAGE, Resources.getInstance().getString(source.getError()));
				} else {
					putState(ERROR_MESSAGE, null);
				}
				putState(MANDATORY, source.isMandatory());
			}
		};
		_fieldModel.addListener(_modelListener);
	}

	/**
	 * Called when the model value changes. Subclasses can override to customize value change
	 * handling (e.g., to send option descriptors instead of raw values).
	 *
	 * @param source
	 *        The field model whose value changed.
	 * @param oldValue
	 *        The previous value.
	 * @param newValue
	 *        The new value.
	 */
	protected void handleModelValueChanged(FieldModel source, Object oldValue, Object newValue) {
		putState(VALUE, newValue);
	}

	/**
	 * Returns the field model.
	 */
	public FieldModel getFieldModel() {
		return _fieldModel;
	}

	/**
	 * Handles value changes from the React client.
	 */
	@ReactCommand("valueChanged")
	void handleValueChanged(Map<String, Object> arguments) {
		Object newValue = arguments.get(VALUE);
		_fieldModel.setValue(parseClientValue(newValue));
	}

	/**
	 * Parses the raw client value into the appropriate typed value.
	 *
	 * <p>
	 * Subclasses override for type-specific parsing. Default returns raw value.
	 * </p>
	 *
	 * @param rawValue
	 *        The value sent by the React client (typically a JSON-typed value).
	 * @return The parsed value suitable for the field model.
	 */
	protected Object parseClientValue(Object rawValue) {
		return rawValue;
	}

}
