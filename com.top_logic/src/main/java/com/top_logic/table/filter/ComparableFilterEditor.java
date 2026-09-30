/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

import java.util.List;

import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.SimpleSelectFieldModel;
import com.top_logic.table.FilterState;

/**
 * {@link FilterEditor} for a {@link ComparableColumnFilter}: an operator dropdown plus one
 * or two bound-value fields (the second used only for {@link ComparisonOperator#BETWEEN}).
 *
 * @param <V>
 *        The compared value type.
 */
public class ComparableFilterEditor<V> implements FilterEditor {

	/** Displays operators as comparison symbols. */
	private static final LabelProvider OPERATOR_LABELS = value -> {
		ComparisonOperator operator = value instanceof ComparisonOperator op
			? op
			: ComparisonOperator.valueOf(String.valueOf(value));
		switch (operator) {
			case EQ:
				return "=";
			case NE:
				return "<>";
			case LT:
				return "<";
			case LE:
				return "<=";
			case GT:
				return ">";
			case GE:
				return ">=";
			case BETWEEN:
				return "between";
			default:
				return operator.name();
		}
	};

	private final BoundCodec<V> _codec;

	private final SimpleSelectFieldModel _operator;

	private final AbstractFieldModel _primary;

	private final AbstractFieldModel _secondary;

	/**
	 * Creates a {@link ComparableFilterEditor} seeded from the current state.
	 */
	public ComparableFilterEditor(ComparableColumnFilter<V> filter, RangeFilterState<V> current) {
		_codec = filter.codec();
		ComparisonOperator operator =
			current != null && current.operator() != null ? current.operator() : ComparisonOperator.EQ;
		_operator = new SimpleSelectFieldModel(operator, List.of(ComparisonOperator.values()), false);
		_primary = new AbstractFieldModel(text(current != null ? current.primary() : null));
		_secondary = new AbstractFieldModel(text(current != null ? current.secondary() : null));
	}

	/**
	 * A bound as the text the user reads and edits, in the user's language.
	 */
	private String text(V value) {
		if (value == null || _codec == null) {
			return "";
		}
		String formatted = _codec.format(value);
		return formatted == null ? "" : formatted;
	}

	@Override
	public List<FilterField> fields() {
		return List.of(
			new FilterField(I18NConstants.OPERATOR, _operator, OPERATOR_LABELS),
			new FilterField(I18NConstants.VALUE, _primary),
			new FilterField(I18NConstants.UPPER_BOUND, _secondary));
	}

	@Override
	public FilterState read() {
		Object operatorValue = _operator.getValue();
		ComparisonOperator operator = operatorValue instanceof ComparisonOperator op
			? op
			: ComparisonOperator.valueOf(String.valueOf(operatorValue));
		return new RangeFilterState<>(operator, parse(_primary), parse(_secondary));
	}

	private V parse(AbstractFieldModel model) {
		if (_codec == null) {
			return null;
		}
		return _codec.parse(TextFilterEditor.string(model));
	}

}
