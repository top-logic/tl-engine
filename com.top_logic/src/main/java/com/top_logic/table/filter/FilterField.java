/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

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
 */
public final class FilterField {

	private final ResKey _label;

	private final FieldModel _model;

	private final LabelProvider _optionLabels;

	private final BoundCodec<?> _valueCodec;

	private final Option _option;

	private final Integer _count;

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
		this(label, model, null, valueCodec, null, null);
	}

	private FilterField(ResKey label, FieldModel model, LabelProvider optionLabels, BoundCodec<?> valueCodec) {
		this(label, model, optionLabels, valueCodec, null, null);
	}

	private FilterField(ResKey label, FieldModel model, LabelProvider optionLabels, BoundCodec<?> valueCodec,
			Option option, Integer count) {
		_label = label;
		_model = model;
		_optionLabels = optionLabels;
		_valueCodec = valueCodec;
		_option = option;
		_count = count;
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
	 */
	public static FilterField option(ResKey label, FieldModel model, Option option, Integer count) {
		return new FilterField(label, model, null, null, option, count);
	}

	/** The field label. */
	public ResKey label() {
		return _label;
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
	 * A UI tier knowing the type of the column shows the {@link Option#value() value} of the option
	 * as a cell of the column shows it, naming the checkbox by the {@link #label()}.
	 * </p>
	 */
	public Option option() {
		return _option;
	}

	/**
	 * The number of rows matching the {@link #option() option}, or {@code null} if not counted.
	 */
	public Integer count() {
		return _count;
	}

}
