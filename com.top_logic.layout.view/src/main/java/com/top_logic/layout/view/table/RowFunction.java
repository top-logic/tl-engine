/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.List;
import java.util.function.Function;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.layout.view.channel.ChannelInputs;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.annotate.ui.ValueColor;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.providers.CellClassProviderByExpression;

/**
 * A compiled function of a table element computing something of a row from the row's object, such
 * as the {@link RowClassConfig#getRowClass() row class} or the {@link RowRoleConfig#getRowRole()
 * row role}.
 *
 * <p>
 * Compiled once per element configuration, and bound to the input channels of each table the
 * element displays.
 * </p>
 *
 * @param <T>
 *        The type of what the function computes for a row.
 */
public final class RowFunction<T> {

	/** The compiled function, {@code null} when the table configures none. */
	private final QueryExecutor _executor;

	/** Converts the result of the {@link #_executor} to what the function computes. */
	private final Function<Object, T> _conversion;

	private RowFunction(QueryExecutor executor, Function<Object, T> conversion) {
		_executor = executor;
		_conversion = conversion;
	}

	/**
	 * Compiles the function computing the CSS class of a row.
	 *
	 * @param config
	 *        The configuration of the table, holding no row class function for none.
	 * @return The compiled function, never {@code null}.
	 *
	 * @see CellClassProviderByExpression#toCssClass(Object)
	 */
	public static RowFunction<String> rowClass(RowClassConfig config) {
		return compile(config, RowClassConfig.ROW_CLASS, CellClassProviderByExpression::toCssClass);
	}

	/**
	 * Compiles the function computing the color role of a row.
	 *
	 * @param config
	 *        The configuration of the table, holding no row role function for none.
	 * @return The compiled function, never {@code null}.
	 *
	 * @see AbstractColumnDeclaration#valueColor(Object)
	 */
	public static RowFunction<ValueColor> rowRole(RowRoleConfig config) {
		return compile(config, RowRoleConfig.ROW_ROLE, AbstractColumnDeclaration::valueColor);
	}

	/**
	 * Compiles the row function in the given property of the given configuration.
	 *
	 * @param config
	 *        The configuration of the table.
	 * @param property
	 *        The name of the property holding the expression, which sees the row object and the
	 *        values of the table's inputs.
	 * @param conversion
	 *        Converts a result of the expression to what the function computes.
	 * @return The compiled function, never {@code null}.
	 */
	public static <T> RowFunction<T> compile(ConfigurationItem config, String property,
			Function<Object, T> conversion) {
		return new RowFunction<>(InputScript.compile(config, property), conversion);
	}

	/**
	 * The function computing the value for a row object in a table reading the given inputs.
	 *
	 * @param inputs
	 *        The table's input channels, whose current values are the values of the input variables
	 *        of every evaluation.
	 * @return The function answering the value for a row object (or {@code null} for none), or
	 *         {@code null} when the table configures no expression.
	 */
	public Function<Object, T> bind(List<ViewChannel> inputs) {
		QueryExecutor executor = _executor;
		if (executor == null) {
			return null;
		}
		Function<Object, T> conversion = _conversion;
		return row -> conversion.apply(executor.execute(ChannelInputs.arguments(inputs, row)));
	}

}
