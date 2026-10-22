/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.navigation;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.view.navigation.DisplayTargetService.BindConfig;
import com.top_logic.layout.view.command.ActionArguments;
import com.top_logic.layout.view.command.ActionScope;
import com.top_logic.layout.view.command.ActionScript;

/**
 * A value a {@link ShowStep} writes to one channel of the view it displays.
 *
 * <p>
 * The value is computed from the {@link ActionArguments arguments} of the display: with an
 * expression, the value is what the expression answers for them; without one, the value is the
 * default argument - the object being shown.
 * </p>
 *
 * @param channel
 *        Name of the channel in the displayed view that receives the value.
 * @param expr
 *        The script computing the value from the arguments, or {@code null} when the default
 *        argument is the value.
 */
public record Binding(String channel, ActionScript expr) {

	/**
	 * The value the {@link #channel()} receives for the given arguments.
	 *
	 * @param arguments
	 *        The arguments of the display; the default is the object being shown.
	 * @return The value to write to the channel.
	 */
	public Object evaluate(ActionArguments arguments) {
		return expr == null ? arguments.value() : expr.execute(null, arguments);
	}

	/**
	 * The {@link Binding} the given configuration describes.
	 *
	 * @param config
	 *        The configured value of one channel.
	 * @param scope
	 *        The names of the arguments the expression sees.
	 * @return The binding writing that value.
	 */
	public static Binding fromConfig(BindConfig config, ActionScope scope) {
		return new Binding(config.getChannel(), ShowStep.compile(config.getExpr(), scope));
	}

	/**
	 * The {@link Binding}s the given configurations describe, in configuration order.
	 *
	 * @param configs
	 *        The configured values of the channels of one view.
	 * @param scope
	 *        The names of the arguments the expressions see.
	 * @return The bindings writing those values.
	 */
	public static List<Binding> fromConfigs(List<? extends BindConfig> configs, ActionScope scope) {
		List<Binding> result = new ArrayList<>(configs.size());
		for (BindConfig config : configs) {
			result.add(fromConfig(config, scope));
		}
		return result;
	}

	@Override
	public String toString() {
		return channel + (expr == null ? "" : "=" + expr);
	}
}
