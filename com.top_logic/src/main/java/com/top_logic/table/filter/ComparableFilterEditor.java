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
import com.top_logic.util.Resources;

/**
 * {@link FilterEditor} for a {@link ComparableColumnFilter}: an operator dropdown plus one
 * or two bound-value fields (the second used only for {@link ComparisonOperator#BETWEEN}).
 *
 * <p>
 * The bound fields hold the typed bounds and are marked with the filter's
 * {@link ComparableColumnFilter#codec() codec}, see {@link FilterField#valueCodec()}.
 * </p>
 *
 * @param <V>
 *        The compared value type.
 */
public class ComparableFilterEditor<V> implements FilterEditor {

	/**
	 * Displays operators as comparison symbols, the range operator by its name in the language of
	 * the current user.
	 */
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
				return Resources.getInstance().getString(I18NConstants.OPERATOR_BETWEEN);
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
		_primary = new AbstractFieldModel(current != null ? current.primary() : null);
		_secondary = new AbstractFieldModel(current != null ? current.secondary() : null);
	}

	@Override
	public List<FilterField> fields() {
		return List.of(
			new FilterField(I18NConstants.OPERATOR, _operator, OPERATOR_LABELS),
			new FilterField(I18NConstants.VALUE, _primary, _codec),
			new FilterField(I18NConstants.UPPER_BOUND, _secondary, _codec));
	}

	@Override
	public FilterState read() {
		Object operatorValue = _operator.getValue();
		ComparisonOperator operator = operatorValue instanceof ComparisonOperator op
			? op
			: ComparisonOperator.valueOf(String.valueOf(operatorValue));
		return new RangeFilterState<>(operator, bound(_primary), bound(_secondary));
	}

	/**
	 * The typed bound a field holds.
	 *
	 * @implNote The fields are created holding a bound of the filtered column, which a UI tier
	 *           replaces only by another one.
	 */
	@SuppressWarnings("unchecked")
	private V bound(AbstractFieldModel model) {
		return (V) model.getValue();
	}

}
