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
 * One input of a {@link FilterEditor}: a labelled {@link FieldModel} plus the
 * {@link FilterFieldKind kind} of control to render it with.
 *
 * <p>
 * Toolkit-neutral: a UI tier maps the {@link #kind()} to a concrete input control bound to
 * the {@link #model()}. For {@link FilterFieldKind#SELECT} fields, {@link #optionLabels()}
 * provides the option display labels.
 * </p>
 */
public final class FilterField {

	private final ResKey _label;

	private final FieldModel _model;

	private final FilterFieldKind _kind;

	private final LabelProvider _optionLabels;

	/**
	 * Creates a non-select {@link FilterField}.
	 */
	public FilterField(ResKey label, FieldModel model, FilterFieldKind kind) {
		this(label, model, kind, null);
	}

	/**
	 * Creates a {@link FilterField}.
	 *
	 * @param label
	 *        The field label.
	 * @param model
	 *        The field model holding the input value.
	 * @param kind
	 *        The control kind.
	 * @param optionLabels
	 *        Option labels for a {@link FilterFieldKind#SELECT} field, else {@code null}.
	 */
	public FilterField(ResKey label, FieldModel model, FilterFieldKind kind, LabelProvider optionLabels) {
		_label = label;
		_model = model;
		_kind = kind;
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

	/** The control kind. */
	public FilterFieldKind kind() {
		return _kind;
	}

	/** Option labels for a {@link FilterFieldKind#SELECT} field, or {@code null}. */
	public LabelProvider optionLabels() {
		return _optionLabels;
	}

}
