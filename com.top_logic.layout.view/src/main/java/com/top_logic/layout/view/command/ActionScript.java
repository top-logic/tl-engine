/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.InputRef;
import com.top_logic.layout.view.channel.NamedInputs;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * A TL-Script of a {@link ViewAction} over the {@link ActionArguments arguments} of its chain.
 *
 * <p>
 * Every action that takes a script - the script it executes, the message it asks for, the
 * condition it branches on - compiles it into an {@link ActionScript}, so all of them see the same
 * variables in the same way: the script uses the arguments defined at the position of the action
 * as variables, followed by the {@link NamedInputs inputs} of the action itself, without declaring
 * them. A script whose action adds values of its own - the value a switch decides on, say - sees
 * them as further variables behind those.
 * </p>
 *
 * @see #variables(ActionScope, List, List)
 */
public interface ActionScript {

	/**
	 * Evaluates the script.
	 *
	 * @param context
	 *        The context the action executes in. It is a {@link ViewContext} whenever the action
	 *        declares inputs, since those are resolved in it.
	 * @param arguments
	 *        The arguments of the chain; they hold at least the names of the scope the script was
	 *        compiled in.
	 * @param additional
	 *        The values of the additional variables the script was compiled with, in their order.
	 * @return The script's result.
	 */
	Object execute(ReactContext context, ActionArguments arguments, Object... additional);

	/**
	 * Evaluates the script the way {@link #execute(ReactContext, ActionArguments, Object...)} does
	 * and reads its result as the message to show to the user.
	 *
	 * <p>
	 * A {@link ResKey} is the message itself; any other value is the message it reads as. Nothing -
	 * no result, or one whose text is empty - is nothing to say.
	 * </p>
	 *
	 * @return The message to show, or {@code null} when there is nothing to say.
	 */
	default ResKey message(ReactContext context, ActionArguments arguments, Object... additional) {
		Object result = execute(context, arguments, additional);
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
	 * The variables a script sees: the names of the given scope, followed by the names of the given
	 * inputs and the given additional names.
	 *
	 * <p>
	 * A later name shadows an equal earlier one.
	 * </p>
	 *
	 * @param scope
	 *        The scope the action is instantiated in.
	 * @param inputs
	 *        The inputs of the action itself.
	 * @param additional
	 *        The names of values the action adds for the script.
	 */
	static List<String> variables(ActionScope scope, List<InputRef> inputs, List<String> additional) {
		List<String> result = new ArrayList<>(scope.names());
		result.addAll(InputRef.names(inputs));
		result.addAll(additional);
		return result;
	}

	/**
	 * Compiles the TL-Script of an action instantiated in the given scope.
	 *
	 * @param script
	 *        The script, using the {@link #variables(ActionScope, List, List) variables} without
	 *        declaring them.
	 * @param scope
	 *        The scope the action is instantiated in.
	 * @param inputs
	 *        The inputs of the action itself.
	 * @param additional
	 *        The names of the values passed as additional values on execution.
	 */
	static ActionScript compile(Expr script, ActionScope scope, List<InputRef> inputs, String... additional) {
		List<String> scopeNames = scope.names();
		QueryExecutor executor = QueryExecutor.compile(script, variables(scope, inputs, List.of(additional)));
		return (context, arguments, additionalValues) -> {
			List<Object> args = arguments.values(scopeNames);
			if (!inputs.isEmpty()) {
				ViewContext viewContext = (ViewContext) context;
				for (InputRef input : inputs) {
					ViewChannel channel = viewContext.resolveChannel(input.getChannel());
					args.add(channel.get());
				}
			}
			for (Object value : additionalValues) {
				args.add(value);
			}
			Object[] values = new Object[args.size()];
			for (int n = 0; n < values.length; n++) {
				values[n] = SearchExpression.normalizeValue(args.get(n));
			}
			return executor.execute(values);
		};
	}
}
