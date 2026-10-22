/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.List;
import java.util.function.Function;

import com.top_logic.layout.view.channel.ChannelInputs;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.providers.CellClassProviderByExpression;

/**
 * The compiled {@link RowClassConfig#getRowClass() row class function} of a table element.
 *
 * <p>
 * Compiled once per element configuration, and bound to the input channels of each table the
 * element displays.
 * </p>
 */
public final class RowClassFunction {

	/** The compiled function, {@code null} when the table configures none. */
	private final QueryExecutor _executor;

	private RowClassFunction(QueryExecutor executor) {
		_executor = executor;
	}

	/**
	 * Compiles the row class function of the given configuration.
	 *
	 * @param config
	 *        The configuration of the table, holding no row class function for none.
	 * @return The compiled function, never {@code null}.
	 */
	public static RowClassFunction compile(RowClassConfig config) {
		return new RowClassFunction(InputScript.compile(config, RowClassConfig.ROW_CLASS));
	}

	/**
	 * The function computing the CSS class of a row object in a table reading the given inputs.
	 *
	 * @param inputs
	 *        The table's input channels, whose current values are the values of the input variables
	 *        of every evaluation.
	 * @return The function answering the CSS class of a row object (or {@code null} for none), or
	 *         {@code null} when no row class function is configured.
	 *
	 * @see CellClassProviderByExpression#toCssClass(Object)
	 */
	public Function<Object, String> bind(List<ViewChannel> inputs) {
		QueryExecutor executor = _executor;
		if (executor == null) {
			return null;
		}
		return row -> CellClassProviderByExpression.toCssClass(
			executor.execute(ChannelInputs.arguments(inputs, row)));
	}

}
