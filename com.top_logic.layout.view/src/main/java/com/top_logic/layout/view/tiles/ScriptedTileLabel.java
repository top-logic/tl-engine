/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.tiles;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelInputs;
import com.top_logic.layout.view.channel.InputRef;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.Inputs;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.ui.ScriptContextVariables;

/**
 * {@link TileLabelProvider} that computes the label from current channel values using a TL-Script
 * expression.
 *
 * <p>
 * Resolves all {@link Config#getInputs() inputs} to channels at compute time and evaluates the
 * expression over their current values, which it sees as variables named after the channels or by
 * their {@code as} names. The expression's result is converted to a {@link ResKey} according to its
 * runtime type - intended for labels derived from business object names.
 * </p>
 *
 * @implNote The result is turned into a {@link ResKey} by
 *           {@link TileLabelProvider#toLabel(Object)}.
 */
public class ScriptedTileLabel implements TileLabelProvider {

	/**
	 * Configuration for {@link ScriptedTileLabel}.
	 */
	@TagName("scripted")
	public interface Config extends TileLabelProvider.Config<ScriptedTileLabel>, Inputs {

		@Override
		@ClassDefault(ScriptedTileLabel.class)
		Class<? extends ScriptedTileLabel> getImplementationClass();

		/** Configuration name for {@link #getExpr()}. */
		String EXPR = "expr";

		/**
		 * TL-Script expression returning the label string.
		 *
		 * <p>
		 * The expression sees the values of the {@link #getInputs() inputs} as variables named after
		 * their channels, or by their {@code as} names.
		 * </p>
		 */
		@Name(EXPR)
		@Mandatory
		@NonNullable
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getExpr();
	}

	private final List<InputRef> _inputs;

	private final QueryExecutor _executor;

	/**
	 * Creates a new {@link ScriptedTileLabel}.
	 */
	@CalledByReflection
	public ScriptedTileLabel(InstantiationContext context, Config config) {
		_inputs = config.getInputs();
		_executor = InputScript.compile(config, Config.EXPR);
	}

	@Override
	public ResKey compute(ViewContext context) {
		Object[] args = ChannelInputs.arguments(ChannelInputs.resolve(context, _inputs));
		return TileLabelProvider.toLabel(_executor.execute(args));
	}
}
