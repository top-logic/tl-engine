/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.model.FieldModel;

/**
 * One input of a {@link FilterEditor}: a labelled {@link FieldModel}.
 *
 * <p>
 * Toolkit-neutral and control-agnostic: the UI tier picks the input control from the
 * {@link #model() field model} itself (a {@code SelectFieldModel} renders a dropdown, a
 * boolean-valued model a checkbox, anything else a text input) using its standard form
 * controls &mdash; there is no per-datatype mapping here. For a dropdown,
 * {@link #optionLabels()} supplies the option display labels.
 * </p>
 */
public final class FilterField {

	private final ResKey _label;

	private final FieldModel _model;

	private final LabelProvider _optionLabels;

	/**
	 * Creates a {@link FilterField} rendered by the field model's natural control.
	 */
	public FilterField(ResKey label, FieldModel model) {
		this(label, model, null);
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
		_label = label;
		_model = model;
		_optionLabels = optionLabels;
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

}
