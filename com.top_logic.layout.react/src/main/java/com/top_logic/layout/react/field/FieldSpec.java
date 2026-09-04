/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.field;

import java.util.List;

import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.control.form.ReactDatePickerControl;
import com.top_logic.model.annotate.ui.BooleanPresentation;

/**
 * What is being edited, independent of where the edited value is stored.
 *
 * <p>
 * A value is edited in two places that describe it differently: an attribute of a model type, and a
 * property of a configuration. Both produce a {@link FieldSpec} and a {@link FieldModel} holding the
 * value, so one set of {@link ReactFieldControlProvider}s serves both instead of one per side.
 * </p>
 *
 * <p>
 * The {@link #getValueType() value type} selects the provider, see {@link FieldControlRegistry}. The
 * remaining properties are the display hints a provider needs; each has a neutral default, so a
 * caller states only what applies.
 * </p>
 */
public final class FieldSpec {

	private final Class<?> _valueType;

	private final String _label;

	private String _tooltip;

	private boolean _mandatory;

	private boolean _editable = true;

	private int _multilineRows;

	private BooleanPresentation _booleanPresentation = BooleanPresentation.CHECKBOX;

	private boolean _triState;

	private ReactDatePickerControl.Kind _dateKind = ReactDatePickerControl.Kind.DATE;

	private List<?> _options;

	private LabelProvider _optionLabels;

	private FieldSpec(Class<?> valueType, String label) {
		_valueType = valueType;
		_label = label;
	}

	/**
	 * Creates a {@link FieldSpec}.
	 *
	 * @param valueType
	 *        The type of the edited value, see {@link #getValueType()}.
	 * @param label
	 *        The label of the edited field.
	 * @return The new specification, to be further described through its setters.
	 */
	public static FieldSpec of(Class<?> valueType, String label) {
		return new FieldSpec(valueType, label);
	}

	/**
	 * The type of the edited value, deciding which control edits it.
	 */
	public Class<?> getValueType() {
		return _valueType;
	}

	/**
	 * The label of the edited field.
	 */
	public String getLabel() {
		return _label;
	}

	/**
	 * Explanatory text for the edited field, or {@code null} if there is none.
	 */
	public String getTooltip() {
		return _tooltip;
	}

	/**
	 * Sets the {@link #getTooltip() tooltip}.
	 *
	 * @return This specification for call chaining.
	 */
	public FieldSpec setTooltip(String tooltip) {
		_tooltip = tooltip;
		return this;
	}

	/**
	 * Whether a value must be entered.
	 */
	public boolean isMandatory() {
		return _mandatory;
	}

	/**
	 * Sets whether the field is {@link #isMandatory() mandatory}.
	 *
	 * @return This specification for call chaining.
	 */
	public FieldSpec setMandatory(boolean mandatory) {
		_mandatory = mandatory;
		return this;
	}

	/**
	 * Whether the value can be changed.
	 *
	 * <p>
	 * A computed value is displayed but not editable.
	 * </p>
	 */
	public boolean isEditable() {
		return _editable;
	}

	/**
	 * Sets whether the value is {@link #isEditable() editable}.
	 *
	 * @return This specification for call chaining.
	 */
	public FieldSpec setEditable(boolean editable) {
		_editable = editable;
		return this;
	}

	/**
	 * The number of text rows to display, or {@code 0} for a single-line input.
	 */
	public int getMultilineRows() {
		return _multilineRows;
	}

	/**
	 * Sets the number of {@link #getMultilineRows() text rows}.
	 *
	 * @return This specification for call chaining.
	 */
	public FieldSpec setMultilineRows(int multilineRows) {
		_multilineRows = multilineRows;
		return this;
	}

	/**
	 * How a boolean value asks to be displayed.
	 */
	public BooleanPresentation getBooleanPresentation() {
		return _booleanPresentation;
	}

	/**
	 * Sets the {@link #getBooleanPresentation() presentation} of a boolean value.
	 *
	 * @return This specification for call chaining.
	 */
	public FieldSpec setBooleanPresentation(BooleanPresentation booleanPresentation) {
		_booleanPresentation = booleanPresentation;
		return this;
	}

	/**
	 * Whether a boolean value keeps a state of its own for "no value".
	 */
	public boolean isTriState() {
		return _triState;
	}

	/**
	 * Sets whether a boolean value is {@link #isTriState() tri-state}.
	 *
	 * @return This specification for call chaining.
	 */
	public FieldSpec setTriState(boolean triState) {
		_triState = triState;
		return this;
	}

	/**
	 * Which part of a point in time the value holds: a date, a time of day, or both.
	 */
	public ReactDatePickerControl.Kind getDateKind() {
		return _dateKind;
	}

	/**
	 * Sets which {@link #getDateKind() part of a point in time} the value holds.
	 *
	 * @return This specification for call chaining.
	 */
	public FieldSpec setDateKind(ReactDatePickerControl.Kind dateKind) {
		_dateKind = dateKind;
		return this;
	}

	/**
	 * The values to choose from, or {@code null} if the value is entered freely.
	 */
	public List<?> getOptions() {
		return _options;
	}

	/**
	 * Labels for the {@link #getOptions() options}, or {@code null} to label them generically.
	 */
	public LabelProvider getOptionLabels() {
		return _optionLabels;
	}

	/**
	 * Sets the values to choose from.
	 *
	 * @param options
	 *        See {@link #getOptions()}.
	 * @param optionLabels
	 *        See {@link #getOptionLabels()}.
	 * @return This specification for call chaining.
	 */
	public FieldSpec setOptions(List<?> options, LabelProvider optionLabels) {
		_options = options;
		_optionLabels = optionLabels;
		return this;
	}

	@Override
	public String toString() {
		return FieldSpec.class.getSimpleName() + "(" + _label + ": " + _valueType.getName() + ")";
	}
}
