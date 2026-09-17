/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * The function of a {@link ViewAction} over the current value of its action chain.
 *
 * <p>
 * Every action that takes a script over the chain's value - the script it executes, the message it
 * asks for, the condition it branches on - compiles it into an {@link ActionScript}, so all of them
 * read their arguments from the same channels in the same way.
 * </p>
 */
public interface ActionScript {

	/**
	 * Calls the function with the current values of the input channels, followed by the given value
	 * of the action chain.
	 *
	 * @param context
	 *        The context the action executes in. It is a {@link ViewContext} whenever input channels
	 *        are configured, since those are resolved in it.
	 * @param input
	 *        The current value of the action chain, passed as the last argument.
	 * @return The function's result.
	 */
	Object execute(ReactContext context, Object input);

	/**
	 * Compiles the TL-Script function of an action, called with the values of the given channels as
	 * leading positional arguments, followed by the chain's current value as the last argument.
	 *
	 * @param function
	 *        The TL-Script function to call.
	 * @param inputs
	 *        The channels whose values are passed before the chain's current value.
	 */
	static ActionScript compile(Expr function, List<ChannelRef> inputs) {
		QueryExecutor executor = QueryExecutor.compile(function);
		if (inputs.isEmpty()) {
			return (context, input) -> executor.execute(input);
		}
		return (context, input) -> {
			ViewContext viewContext = (ViewContext) context;
			Object[] args = new Object[inputs.size() + 1];
			int i = 0;
			for (ChannelRef ref : inputs) {
				ViewChannel channel = viewContext.resolveChannel(ref);
				args[i++] = channel.get();
			}
			args[i] = input;
			return executor.execute(args);
		};
	}

	/**
	 * Configuration of the channels an {@link ActionScript} takes its leading arguments from.
	 */
	interface Inputs extends ConfigurationItem {

		/** Configuration name for {@link #getInputs()}. */
		String INPUTS = "inputs";

		/**
		 * References to the {@link ViewChannel}s whose current values become the leading positional
		 * arguments of the action's function, before the current value of the action chain.
		 *
		 * <p>
		 * Without such a reference, the function is called with the chain's current value as its
		 * single argument. A reference pulls further context into the function - a create container,
		 * a selection, a filter term - that the chain itself does not carry.
		 * </p>
		 */
		@Name(INPUTS)
		@ListBinding(format = ChannelRefFormat.class, tag = "input", attribute = "channel")
		List<ChannelRef> getInputs();
	}
}
