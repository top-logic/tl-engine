/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.field;

import java.text.DateFormat;
import java.text.Format;
import java.text.NumberFormat;
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

	private String _placeholder;

	private String _icon;

	private boolean _clearable;

	private Long _debounce;

	private boolean _mandatory;

	private boolean _editable = true;

	private boolean _multiple;

	private boolean _ordered;

	private int _multilineRows;

	private BooleanPresentation _booleanPresentation = BooleanPresentation.CHECKBOX;

	private boolean _triState;

	private ReactDatePickerControl.Kind _dateKind = ReactDatePickerControl.Kind.DATE;

	private Format _numberFormat;

	private DateFormat _dateFormat;

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
	 * The text shown in the empty input, or {@code null} if it stays empty.
	 *
	 * <p>
	 * What the user is expected to enter, said inside the input itself: "Search" in a search box,
	 * "name@example.com" in a mail address. It is the place to state the purpose of an input whose
	 * {@link #getLabel() label} is hidden or stands elsewhere - in a toolbar, above a list - and it
	 * disappears as soon as a value is entered, so it never replaces a label the field can show.
	 * </p>
	 */
	public String getPlaceholder() {
		return _placeholder;
	}

	/**
	 * Sets the {@link #getPlaceholder() placeholder}.
	 *
	 * @return This specification for call chaining.
	 */
	public FieldSpec setPlaceholder(String placeholder) {
		_placeholder = placeholder;
		return this;
	}

	/**
	 * The icon shown inside the input, ahead of what is typed, or {@code null} for an input that
	 * stands on its own.
	 *
	 * <p>
	 * What kind of input this is, said as a picture: the magnifier of a search box, the envelope of
	 * a mail address. It is decoration rather than a control - nothing happens when it is clicked -
	 * so the input still needs a {@link #getLabel() label} or a {@link #getPlaceholder()
	 * placeholder} to be named.
	 * </p>
	 *
	 * <p>
	 * The encoded form of a {@link com.top_logic.layout.basic.ThemeImage}, an icon font class such
	 * as {@code css:fa-solid fa-magnifying-glass} or a path to an image. Only a single-line text
	 * input shows it; a text area, a number and a password input ignore it.
	 * </p>
	 */
	public String getIcon() {
		return _icon;
	}

	/**
	 * Sets the {@link #getIcon() icon}.
	 *
	 * @return This specification for call chaining.
	 */
	public FieldSpec setIcon(String icon) {
		_icon = icon;
		return this;
	}

	/**
	 * Whether the input offers a button that empties it.
	 *
	 * <p>
	 * For an input whose value is taken back as often as it is given - the term a list is searched
	 * by, the text a filter is narrowed to - where emptying it is a step of its own rather than the
	 * accident of deleting every character. The button appears only while the input holds something
	 * and while it is {@link #isEditable() editable}, and pressing it writes the empty value at once
	 * instead of after the {@link #getDebounce() delay}.
	 * </p>
	 *
	 * <p>
	 * Only a single-line text input offers it; a text area, a number and a password input ignore
	 * it.
	 * </p>
	 */
	public boolean isClearable() {
		return _clearable;
	}

	/**
	 * Sets whether the input is {@link #isClearable() clearable}.
	 *
	 * @return This specification for call chaining.
	 */
	public FieldSpec setClearable(boolean clearable) {
		_clearable = clearable;
		return this;
	}

	/**
	 * How long a typed value is held back before it is sent, in milliseconds, or {@code null} for
	 * the delay a typed field uses by default.
	 *
	 * <p>
	 * The time after the last keystroke before what is typed reaches the server: long enough that a
	 * burst of keystrokes costs one round-trip, short enough that an answer computed from the value
	 * - the rows a search narrows to - follows the typing. A shorter delay makes the answer more
	 * immediate at the price of more round-trips; a longer one waits for the user to stop.
	 * </p>
	 *
	 * <p>
	 * Ignored by a field that
	 * {@link com.top_logic.layout.react.control.form.ReactFormFieldControl#setSendValueOnBlur(boolean)
	 * sends its value on blur}, which holds a typed value back entirely until the field is left.
	 * The text, number and password inputs honour it.
	 * </p>
	 */
	public Long getDebounce() {
		return _debounce;
	}

	/**
	 * Sets the {@link #getDebounce() delay} before a typed value is sent.
	 *
	 * @return This specification for call chaining.
	 */
	public FieldSpec setDebounce(Long debounce) {
		_debounce = debounce;
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
	 * Whether the field holds several values rather than one.
	 *
	 * <p>
	 * A control that edits a collection of values - a list of configurations, say - needs to know
	 * this even where the field is currently empty, since an empty collection and no value look
	 * alike in the {@link FieldModel}.
	 * </p>
	 */
	public boolean isMultiple() {
		return _multiple;
	}

	/**
	 * Sets whether the field holds {@link #isMultiple() several values}.
	 *
	 * @return This specification for call chaining.
	 */
	public FieldSpec setMultiple(boolean multiple) {
		_multiple = multiple;
		return this;
	}

	/**
	 * Whether the order of the values of a {@link #isMultiple() multi-valued} field is part of the
	 * value, so that the user may arrange them.
	 *
	 * <p>
	 * A field whose values form a set has no such order: its values are displayed in some order,
	 * but moving one of them would change nothing. Only where the order is stored with the values
	 * is arranging them offered.
	 * </p>
	 */
	public boolean isOrdered() {
		return _ordered;
	}

	/**
	 * Sets whether the values of the field are {@link #isOrdered() ordered}.
	 *
	 * @return This specification for call chaining.
	 */
	public FieldSpec setOrdered(boolean ordered) {
		_ordered = ordered;
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
	 * The format a numeric value is displayed in and entered in, or {@code null} to use the default
	 * format for the value type.
	 *
	 * <p>
	 * One format serves every place the value appears - a form field, a table cell, and the bounds
	 * of that column's filter - so that the same number is always written the same way. A
	 * {@link NumberFormat} carries the user's locale and the number of digits the value asks for;
	 * a format of another kind writes its own text, a duration in milliseconds as {@code 1h 30min}
	 * for instance.
	 * </p>
	 */
	public Format getNumberFormat() {
		return _numberFormat;
	}

	/**
	 * Sets the {@link #getNumberFormat() number format}.
	 *
	 * @return This specification for call chaining.
	 */
	public FieldSpec setNumberFormat(Format numberFormat) {
		_numberFormat = numberFormat;
		return this;
	}

	/**
	 * The format a point in time is displayed in when the field is read-only, or {@code null} to
	 * use the default format of its {@link #getDateKind() kind}.
	 *
	 * <p>
	 * The attribute's annotated format where it has one: an application chooses whether a point in
	 * time reads as {@code 17.09.26, 10:23} or {@code 17.09.2026, 10:23:45}, and the same format
	 * writes the value in a form field, in a table cell, and in the bounds of that column's filter.
	 * </p>
	 */
	public DateFormat getDateFormat() {
		return _dateFormat;
	}

	/**
	 * Sets the {@link #getDateFormat() date format}.
	 *
	 * @return This specification for call chaining.
	 */
	public FieldSpec setDateFormat(DateFormat dateFormat) {
		_dateFormat = dateFormat;
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

	/**
	 * This specification describing a single one of the values of a
	 * {@link #isMultiple() multi-valued} field.
	 *
	 * <p>
	 * The element is described like the field itself - same value type, same label, same display
	 * hints - except that it holds one value. That is what a control editing a single value is
	 * created with, while the collection around it is built from one such control per element.
	 * </p>
	 *
	 * @return A specification of one element, independent of this one.
	 */
	public FieldSpec elementSpec() {
		FieldSpec result = new FieldSpec(_valueType, _label);
		result._tooltip = _tooltip;
		result._placeholder = _placeholder;
		result._icon = _icon;
		result._clearable = _clearable;
		result._debounce = _debounce;
		result._mandatory = _mandatory;
		result._editable = _editable;
		// The element is one value: it is not several, and a single value has no order to arrange.
		result._multiple = false;
		result._ordered = false;
		result._multilineRows = _multilineRows;
		result._booleanPresentation = _booleanPresentation;
		result._triState = _triState;
		result._dateKind = _dateKind;
		result._numberFormat = _numberFormat;
		result._dateFormat = _dateFormat;
		result._options = _options;
		result._optionLabels = _optionLabels;
		return result;
	}

	@Override
	public String toString() {
		return FieldSpec.class.getSimpleName() + "(" + _label + ": " + _valueType.getName() + ")";
	}
}
