/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link ViewAction} that decides between two chains of actions by a TL-Script condition over the
 * current value of the enclosing chain.
 *
 * <p>
 * The chosen chain runs as a nested chain of the enclosing one: it starts with the current value
 * and its last action's result becomes the value the enclosing chain continues with. The branch
 * that is not chosen does not run, and a branch that is not configured passes the value through
 * unchanged. An abort inside a branch - a declined {@link ConfirmAction} - aborts the whole
 * command, and a compensation registered inside a branch runs on the command's abort like one
 * registered beside the branch.
 * </p>
 *
 * <p>
 * Example: select the ticket the chain found, or offer to create one when it found none:
 * </p>
 *
 * <pre>
 * &lt;if test="ticket -&gt; $ticket != null"&gt;
 *   &lt;then&gt;
 *     &lt;write-channel name="ticket"/&gt;
 *   &lt;/then&gt;
 *   &lt;else&gt;
 *     &lt;execute-script function="x -&gt; new(`demo.tickets:Ticket`, transient: true)"/&gt;
 *     &lt;open-dialog bind-input-to="model" dialog-view="tickets-create.view.xml"/&gt;
 *   &lt;/else&gt;
 * &lt;/if&gt;
 * </pre>
 */
@InApp
public class IfAction extends InterruptibleViewAction {

	/**
	 * Configuration for {@link IfAction}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<IfAction>, ActionScript.Inputs {

		/** Configuration tag of an {@link IfAction}. */
		String TAG_NAME = "if";

		/** Configuration name for {@link #getTest()}. */
		String TEST = "test";

		/** Configuration name for {@link #getThen()}. */
		String THEN = "then";

		/** Configuration name for {@link #getElse()}. */
		String ELSE = "else";

		@Override
		@ClassDefault(IfAction.class)
		Class<? extends IfAction> getImplementationClass();

		/**
		 * TL-Script condition deciding which branch runs.
		 *
		 * <p>
		 * Called with the {@link #getInputs() inputs} channel values as leading positional
		 * arguments, followed by the chain's current value as the last argument. Its result is taken
		 * as a condition in the fuzzy sense of TL-Script, so a plain object stands for a true
		 * condition and nothing - {@code null}, an empty list, an empty text - for a false one.
		 * </p>
		 */
		@Name(TEST)
		@Mandatory
		Expr getTest();

		/**
		 * The actions to run when the {@link #getTest() condition} holds; none to pass the chain's
		 * value through.
		 */
		@Name(THEN)
		@Options(fun = AllInAppImplementations.class)
		List<PolymorphicConfiguration<? extends ViewAction>> getThen();

		/**
		 * The actions to run when the {@link #getTest() condition} does not hold; none to pass the
		 * chain's value through.
		 */
		@Name(ELSE)
		@Options(fun = AllInAppImplementations.class)
		List<PolymorphicConfiguration<? extends ViewAction>> getElse();
	}

	private final ActionScript _test;

	private final List<ViewAction> _then;

	private final List<ViewAction> _else;

	/**
	 * Creates a new {@link IfAction} from configuration.
	 */
	@CalledByReflection
	public IfAction(InstantiationContext context, Config config) {
		this(ActionScript.compile(config.getTest(), config.getInputs()),
			ViewActions.instantiate(context, config.getThen()),
			ViewActions.instantiate(context, config.getElse()));
	}

	/**
	 * Creates a new {@link IfAction}.
	 *
	 * @param test
	 *        The condition deciding which branch runs.
	 * @param thenActions
	 *        The actions to run when the condition holds; empty to pass the chain's value through.
	 * @param elseActions
	 *        The actions to run when it does not hold; empty to pass the chain's value through.
	 */
	public IfAction(ActionScript test, List<ViewAction> thenActions, List<ViewAction> elseActions) {
		_test = test;
		_then = thenActions;
		_else = elseActions;
	}

	@Override
	public void execute(ReactContext context, Object input, Continuation continuation) {
		List<ViewAction> branch = SearchExpression.isTrue(_test.execute(context, input)) ? _then : _else;
		ViewActionChain.nest(context, branch, input, continuation);
	}

	@Override
	public boolean appliesFormState() {
		return ViewActions.appliesFormState(_then) || ViewActions.appliesFormState(_else);
	}
}
