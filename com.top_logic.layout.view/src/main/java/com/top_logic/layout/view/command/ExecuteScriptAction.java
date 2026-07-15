/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link ViewAction} that evaluates a configured TL-Script function.
 *
 * <p>
 * Without {@link Config#getInputs() inputs}, the configured expression must be a function that takes
 * one argument: the chain's current input value is passed as the argument and the function's return
 * value becomes the output of this action.
 * </p>
 *
 * <p>
 * Example: Create a transient object:
 * </p>
 *
 * <pre>
 * &lt;execute-script function="x -&gt; new(`test.constraints:ConstraintTestType`, transient: true)"/&gt;
 * </pre>
 *
 * <p>
 * With {@link Config#getInputs() inputs}, the current values of the referenced {@link ViewChannel}s
 * are passed as leading positional arguments, followed by the chain's current input value as the
 * last argument. This allows an action to combine a separate channel value (e.g. a create context /
 * container) with the chained value (e.g. the form object being created). Example: persist a
 * transient form object into a container channel's composite reference:
 * </p>
 *
 * <pre>
 * &lt;execute-script function="container -&gt; obj -&gt; $container.add(`my.module:Container#children`, $obj.copy(transient: false))"&gt;
 *     &lt;inputs&gt;
 *         &lt;input channel="context"/&gt;
 *     &lt;/inputs&gt;
 * &lt;/execute-script&gt;
 * </pre>
 */
public class ExecuteScriptAction implements ViewAction {

	/**
	 * Configuration for {@link ExecuteScriptAction}.
	 */
	@TagName("execute-script")
	public interface Config extends com.top_logic.basic.config.PolymorphicConfiguration<ExecuteScriptAction> {

		/** Configuration name for {@link #getInputs()}. */
		String INPUTS = "inputs";

		@Override
		@ClassDefault(ExecuteScriptAction.class)
		Class<? extends ExecuteScriptAction> getImplementationClass();

		/**
		 * TL-Script function expression.
		 *
		 * <p>
		 * Called with the {@link #getInputs() input} channel values as leading positional arguments
		 * (in declaration order), followed by the chain's current input value as the last argument.
		 * With no inputs, called with the chain's current input value as the single argument.
		 * </p>
		 */
		@Name("function")
		@Mandatory
		Expr getFunction();

		/**
		 * References to {@link ViewChannel}s whose current values become leading positional
		 * arguments to {@link #getFunction()} (before the chain's current input value).
		 */
		@Name(INPUTS)
		@ListBinding(format = ChannelRefFormat.class, tag = "input", attribute = "channel")
		List<ChannelRef> getInputs();
	}

	private final QueryExecutor _executor;

	private final List<ChannelRef> _inputRefs;

	/**
	 * Creates a new {@link ExecuteScriptAction}.
	 */
	@CalledByReflection
	public ExecuteScriptAction(InstantiationContext context, Config config) {
		_executor = QueryExecutor.compile(config.getFunction());
		_inputRefs = config.getInputs();
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (_inputRefs.isEmpty()) {
			return _executor.execute(input);
		}

		ViewContext viewContext = (ViewContext) context;
		Object[] args = new Object[_inputRefs.size() + 1];
		int i = 0;
		for (ChannelRef ref : _inputRefs) {
			ViewChannel channel = viewContext.resolveChannel(ref);
			args[i++] = channel.get();
		}
		args[i] = input;
		return _executor.execute(args);
	}
}
