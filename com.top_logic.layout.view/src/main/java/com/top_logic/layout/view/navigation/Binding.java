/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.navigation;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.view.navigation.DisplayTargetService.BindConfig;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * A value a {@link ShowStep} writes to one channel of the view it displays.
 *
 * <p>
 * The value is computed from the object being shown: with an expression, the value is what the
 * expression answers for that object; without one, the value is the object itself.
 * </p>
 *
 * @param channel
 *        Name of the channel in the displayed view that receives the value.
 * @param expr
 *        The function of the shown object computing the value, or {@code null} when the shown
 *        object is the value.
 */
public record Binding(String channel, QueryExecutor expr) {

	/**
	 * The value the {@link #channel()} receives when the given object is displayed.
	 *
	 * @param shownObject
	 *        The object being displayed.
	 * @return The value to write to the channel.
	 */
	public Object evaluate(Object shownObject) {
		return expr == null ? shownObject : expr.execute(shownObject);
	}

	/**
	 * The {@link Binding} the given configuration describes.
	 *
	 * @param config
	 *        The configured value of one channel.
	 * @return The binding writing that value.
	 */
	public static Binding fromConfig(BindConfig config) {
		return new Binding(config.getChannel(), QueryExecutor.compileOptional(config.getExpr()));
	}

	/**
	 * The {@link Binding}s the given configurations describe, in configuration order.
	 *
	 * @param configs
	 *        The configured values of the channels of one view.
	 * @return The bindings writing those values.
	 */
	public static List<Binding> fromConfigs(List<? extends BindConfig> configs) {
		List<Binding> result = new ArrayList<>(configs.size());
		for (BindConfig config : configs) {
			result.add(fromConfig(config));
		}
		return result;
	}

	@Override
	public String toString() {
		return channel + (expr == null ? "" : "=" + expr);
	}
}
