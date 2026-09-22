/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.List;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelInputs;
import com.top_logic.layout.view.channel.ChannelRef;
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
	 * Calls the function the way {@link #execute(ReactContext, Object)} does and reads its result as
	 * the message to show to the user.
	 *
	 * <p>
	 * A {@link ResKey} is the message itself; any other value is the message it reads as. Nothing -
	 * no result, or one whose text is empty - is nothing to say.
	 * </p>
	 *
	 * @param context
	 *        The context the action executes in.
	 * @param input
	 *        The current value of the action chain, passed as the last argument.
	 * @return The message to show, or {@code null} when there is nothing to say.
	 */
	default ResKey message(ReactContext context, Object input) {
		Object result = execute(context, input);
		if (result == null) {
			return null;
		}
		if (result instanceof ResKey) {
			return (ResKey) result;
		}
		String text = result.toString();
		return text.isEmpty() ? null : ResKey.text(text);
	}

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
			List<ViewChannel> channels = ChannelInputs.resolve((ViewContext) context, inputs);
			return executor.execute(ChannelInputs.arguments(channels, input));
		};
	}
}
