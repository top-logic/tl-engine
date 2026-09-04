/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

import com.top_logic.basic.util.ResKey;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.FilterInput;
import com.top_logic.table.FilterState;

/**
 * A {@link ColumnFilter} over {@link Boolean}-valued cells, accepting any subset of
 * {@code true} / {@code false} / no-value as configured by a {@link BooleanFilterState}.
 *
 * <p>
 * The {@code true} / {@code false} options carry their own display labels so the filter can
 * present each value exactly as the column renders it (e.g. "Yes" / "No"). Use the
 * {@link #INSTANCE default} for the generic "True" / "False" wording, or
 * {@link #BooleanColumnFilter(ResKey, ResKey)} to match a column's value rendering.
 * </p>
 *
 * <p>
 * A tri-state column also offers the no-value option; for a two-valued column (whose cells are
 * never empty) it would be an option that can never match, so
 * {@link #BooleanColumnFilter(ResKey, ResKey, boolean) a non-nullable filter} drops it and offers
 * just the two value options.
 * </p>
 */
public class BooleanColumnFilter implements ColumnFilter<Boolean> {

	/** Default instance labelling the options with the generic "True" / "False" wording. */
	public static final BooleanColumnFilter INSTANCE =
		new BooleanColumnFilter(I18NConstants.VALUE_TRUE, I18NConstants.VALUE_FALSE);

	private final ResKey _trueLabel;

	private final ResKey _falseLabel;

	private final boolean _nullable;

	/**
	 * Creates a tri-state {@link BooleanColumnFilter} with explicit option labels for {@code true}
	 * and {@code false}, so the filter matches the column's value rendering.
	 */
	public BooleanColumnFilter(ResKey trueLabel, ResKey falseLabel) {
		this(trueLabel, falseLabel, true);
	}

	/**
	 * Creates a {@link BooleanColumnFilter}.
	 *
	 * @param trueLabel
	 *        The label presenting the {@code true} option.
	 * @param falseLabel
	 *        The label presenting the {@code false} option.
	 * @param nullable
	 *        Whether the column has cells without a value, see
	 *        {@link com.top_logic.table.FilterInput.Bool#nullable()}.
	 */
	public BooleanColumnFilter(ResKey trueLabel, ResKey falseLabel, boolean nullable) {
		_trueLabel = trueLabel;
		_falseLabel = falseLabel;
		_nullable = nullable;
	}

	/** The label presenting the {@code true} option. */
	public ResKey trueLabel() {
		return _trueLabel;
	}

	/** The label presenting the {@code false} option. */
	public ResKey falseLabel() {
		return _falseLabel;
	}

	@Override
	public FilterInput input() {
		return new FilterInput.Bool(_nullable);
	}

	@Override
	public Predicate<Boolean> predicate(FilterState state) {
		BooleanFilterState bool = (BooleanFilterState) state;
		return value -> {
			if (value == null) {
				return bool.acceptNull();
			}
			return value ? bool.acceptTrue() : bool.acceptFalse();
		};
	}

	@Override
	public Object toJson(FilterState state) {
		BooleanFilterState bool = (BooleanFilterState) state;
		Map<String, Object> json = new LinkedHashMap<>();
		json.put("acceptTrue", Boolean.valueOf(bool.acceptTrue()));
		json.put("acceptFalse", Boolean.valueOf(bool.acceptFalse()));
		json.put("acceptNull", Boolean.valueOf(bool.acceptNull()));
		return json;
	}

	@Override
	public FilterState fromJson(Object json) {
		if (!(json instanceof Map<?, ?> map)) {
			return null;
		}
		return new BooleanFilterState(bool(map.get("acceptTrue")), bool(map.get("acceptFalse")),
			bool(map.get("acceptNull")));
	}

	private static boolean bool(Object value) {
		return Boolean.TRUE.equals(value) || "true".equals(value);
	}

}
