/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

import java.util.function.Function;
import java.util.function.Predicate;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.table.Option;

/**
 * One input of a {@link FilterEditor}: a labelled {@link FieldModel}.
 *
 * <p>
 * Toolkit-neutral and control-agnostic: the UI tier picks the input control from the
 * {@link #model() field model} itself (a {@code SelectFieldModel} renders a dropdown, a
 * boolean-valued model a checkbox, anything else a text input) using its standard form
 * controls. For a dropdown, {@link #optionLabels()} supplies the option display labels.
 * </p>
 *
 * <p>
 * A field holding a value of the filtered column - a bound of a range - is marked by its
 * {@link #valueCodec()}: its model holds the typed value (a number, a date), so that a UI tier
 * knowing the type of the column enters it as a form does (a number field, a date picker). A UI
 * tier without such an input enters it as text, written and read by the codec.
 * </p>
 *
 * <p>
 * A field whose meaning depends on the value of another field of the same editor - a second bound
 * that exists only for a range, a bound whose label names its role - {@link #following(FieldModel,
 * Function, Predicate) follows} that field: a UI tier observes the {@link #source() source} and
 * updates the {@link #label()} and the {@link #isVisible() visibility} of the field when its value
 * changes.
 * </p>
 */
public final class FilterField {

	private final ResKey _label;

	private final FieldModel _model;

	private final LabelProvider _optionLabels;

	private final BoundCodec<?> _valueCodec;

	private final Option _option;

	private final Integer _count;

	private final boolean _columnValue;

	private final FieldModel _source;

	private final Function<Object, ResKey> _labelFor;

	private final Predicate<Object> _visibleFor;

	/**
	 * Creates a {@link FilterField} rendered by the field model's natural control.
	 */
	public FilterField(ResKey label, FieldModel model) {
		this(label, model, (LabelProvider) null);
	}

	/**
	 * Creates a {@link FilterField}, optionally with option labels for a dropdown.
	 *
	 * @param label
	 *        The field label.
	 * @param model
	 *        The field model holding the input value.
	 * @param optionLabels
	 *        Option labels for a select field model, else {@code null}.
	 */
	public FilterField(ResKey label, FieldModel model, LabelProvider optionLabels) {
		this(label, model, optionLabels, null);
	}

	/**
	 * Creates a {@link FilterField} holding a value of the filtered column.
	 *
	 * @param label
	 *        The field label.
	 * @param model
	 *        The field model holding the typed value, {@code null} for none.
	 * @param valueCodec
	 *        Writes and reads the value as text, for a UI tier entering it as text.
	 */
	public FilterField(ResKey label, FieldModel model, BoundCodec<?> valueCodec) {
		this(label, model, null, valueCodec, null, null, false);
	}

	private FilterField(ResKey label, FieldModel model, LabelProvider optionLabels, BoundCodec<?> valueCodec) {
		this(label, model, optionLabels, valueCodec, null, null, false);
	}

	private FilterField(ResKey label, FieldModel model, LabelProvider optionLabels, BoundCodec<?> valueCodec,
			Option option, Integer count, boolean columnValue) {
		this(label, model, optionLabels, valueCodec, option, count, columnValue, null, null, null);
	}

	private FilterField(ResKey label, FieldModel model, LabelProvider optionLabels, BoundCodec<?> valueCodec,
			Option option, Integer count, boolean columnValue, FieldModel source, Function<Object, ResKey> labelFor,
			Predicate<Object> visibleFor) {
		_label = label;
		_model = model;
		_optionLabels = optionLabels;
		_valueCodec = valueCodec;
		_option = option;
		_count = count;
		_columnValue = columnValue;
		_source = source;
		_labelFor = labelFor;
		_visibleFor = visibleFor;
	}

	/**
	 * This field with a label and a visibility following the value of another field.
	 *
	 * @param source
	 *        The model of the field whose value the label and the visibility depend on.
	 * @param labelFor
	 *        The label of this field for a value of the source.
	 * @param visibleFor
	 *        Whether this field is shown for a value of the source; a field not shown takes no
	 *        part in the filter.
	 * @return A copy of this field following the source.
	 */
	public FilterField following(FieldModel source, Function<Object, ResKey> labelFor,
			Predicate<Object> visibleFor) {
		return new FilterField(_label, _model, _optionLabels, _valueCodec, _option, _count, _columnValue, source,
			labelFor, visibleFor);
	}

	/**
	 * A checkbox field selecting the given option of the filtered column.
	 *
	 * @param label
	 *        The field label: the label of the option, with its count where there is one.
	 * @param model
	 *        The boolean field model holding whether the option is selected.
	 * @param option
	 *        The option the field selects.
	 * @param count
	 *        The number of rows matching the option, {@code null} if not counted.
	 * @param columnValue
	 *        Whether the {@link Option#value() value} of the option is a value of the column, see
	 *        {@link #isColumnValue()}.
	 */
	public static FilterField option(ResKey label, FieldModel model, Option option, Integer count,
			boolean columnValue) {
		return new FilterField(label, model, null, null, option, count, columnValue);
	}

	/**
	 * The field label, for the current value of the {@link #source() source} where the field
	 * follows one.
	 */
	public ResKey label() {
		return _source == null ? _label : _labelFor.apply(_source.getValue());
	}

	/**
	 * Whether the field is shown, for the current value of the {@link #source() source} where the
	 * field follows one.
	 */
	public boolean isVisible() {
		return _source == null || _visibleFor.test(_source.getValue());
	}

	/**
	 * The model of the field whose value the {@link #label()} and the {@link #isVisible()
	 * visibility} of this field follow, or {@code null} if they are fixed.
	 *
	 * @see #following(FieldModel, Function, Predicate)
	 */
	public FieldModel source() {
		return _source;
	}

	/** The field model holding the input value. */
	public FieldModel model() {
		return _model;
	}

	/** Option labels for a select field model, or {@code null}. */
	public LabelProvider optionLabels() {
		return _optionLabels;
	}

	/**
	 * The codec writing and reading the value of a field holding a value of the filtered column as
	 * text, or {@code null} if the field holds no such value.
	 */
	public BoundCodec<?> valueCodec() {
		return _valueCodec;
	}

	/**
	 * The option of the filtered column a checkbox field selects, or {@code null} if the field
	 * selects none.
	 *
	 * <p>
	 * A UI tier knowing the type of the column shows the {@link Option#value() value} of an option
	 * that {@link #isColumnValue() is a value of the column} as a cell of the column shows it,
	 * naming the checkbox by the {@link #label()}.
	 * </p>
	 */
	public Option option() {
		return _option;
	}

	/**
	 * Whether the value of the {@link #option() option} is a value of the column, which a UI tier
	 * shows as a cell of the column shows it.
	 *
	 * <p>
	 * An option standing for something else - a condition such as a pattern the text of a cell
	 * matches, or {@link Option#NO_VALUE no value} - is shown by its label.
	 * </p>
	 */
	public boolean isColumnValue() {
		return _columnValue;
	}

	/**
	 * The number of rows matching the {@link #option() option}, or {@code null} if not counted.
	 */
	public Integer count() {
		return _count;
	}

}
