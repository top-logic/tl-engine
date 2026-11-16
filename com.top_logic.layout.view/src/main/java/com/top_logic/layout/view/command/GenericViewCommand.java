/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.List;

import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.ButtonTone;
import com.top_logic.layout.view.ViewContext;

/**
 * A {@link ViewCommand} that executes a configured chain of {@link ViewAction}s.
 *
 * <p>
 * The actions run with the arguments of the command: the values of its
 * {@link Config#getInputs() inputs} and the value its trigger supplies, see
 * {@link CommandArguments}. The result of an action is seen by its follow-up actions (see
 * {@link WithResultActions}). This allows composing complex behaviors from simple, reusable
 * building blocks.
 * </p>
 *
 * <p>
 * Example: Create a transient object and open a dialog with it:
 * </p>
 *
 * <pre>
 * &lt;generic-command label="New"&gt;
 *   &lt;execute-script function="new(`my.module:MyType`, transient: true)"&gt;
 *     &lt;with-result&gt;
 *       &lt;open-dialog dialog-view="demo/create.view.xml" bind-input-to="model"/&gt;
 *     &lt;/with-result&gt;
 *   &lt;/execute-script&gt;
 * &lt;/generic-command&gt;
 * </pre>
 */
@InApp
public class GenericViewCommand implements ViewCommand {

	/**
	 * Configuration for {@link GenericViewCommand}.
	 */
	@TagName("generic-command")
	public interface Config extends ViewCommand.Config {

		@Override
		@ClassDefault(GenericViewCommand.class)
		Class<? extends ViewCommand> getImplementationClass();

		/**
		 * The chain of actions to execute sequentially.
		 *
		 * <p>
		 * Every action runs with the arguments of the command: the values of the
		 * {@link #getInputs() inputs}, and the value the trigger of the command supplies.
		 * </p>
		 */
		@DefaultContainer
		@Options(fun = AllInAppImplementations.class)
		@TreeProperty
		List<PolymorphicConfiguration<? extends ViewAction>> getActions();
	}

	/**
	 * Name of the main argument of a command with neither inputs nor a trigger supplying a value.
	 */
	public static final String DEFAULT_ARGUMENT = CommandArguments.DEFAULT_ARGUMENT;

	private final CommandArguments _arguments;

	private final List<ViewAction> _actions;

	private final ResKey _label;

	private final ButtonTone _after;

	/**
	 * Creates a new {@link GenericViewCommand}.
	 */
	@CalledByReflection
	public GenericViewCommand(InstantiationContext context, Config config) {
		_arguments = CommandArguments.of(config, ActionScope.of(context));
		_actions = ViewActions.instantiate(context, config.getActions(), _arguments.scope());
		_label = config.getLabel();
		_after = CommandCliques.tone(config.getClique());
	}

	/**
	 * Creates a {@link GenericViewCommand} running the given actions with its input as argument
	 * named {@value #DEFAULT_ARGUMENT}.
	 *
	 * @param actions
	 *        The chain to run.
	 */
	public GenericViewCommand(List<ViewAction> actions) {
		this(DEFAULT_ARGUMENT, actions);
	}

	/**
	 * Creates a {@link GenericViewCommand} running the given actions.
	 *
	 * @param argumentName
	 *        The name of the main argument, holding the value the command is executed with.
	 * @param actions
	 *        The chain to run, instantiated in the {@link ActionScope#single(String) scope} of the
	 *        argument.
	 */
	public GenericViewCommand(String argumentName, List<ViewAction> actions) {
		_arguments = new CommandArguments(List.of(), argumentName);
		_actions = List.copyOf(actions);
		_label = null;
		_after = ButtonTone.DEFAULT;
	}


	/**
	 * The {@link Config#getLabel() label} this command is configured with, {@code null} for a
	 * command without a label.
	 */
	@Override
	public ResKey getLabel() {
		return _label;
	}

	/**
	 * The rules the actions of the chain {@link ViewAction#getIntrinsicRule() bring of their own},
	 * combined.
	 */
	@Override
	public ViewExecutabilityRule getIntrinsicRule() {
		return ViewActions.intrinsicRule(_actions);
	}

	/**
	 * Destructive as soon as one of the actions of the chain is.
	 */
	@Override
	public ButtonTone getTone() {
		return ViewActions.tone(_actions);
	}

	/**
	 * Runs the configured chain with the given arguments, counting this command among the
	 * {@link SuspendedCommands suspended commands} of its region for as long as an action holds the
	 * chain (see {@link ViewActionChain.HoldObserver}).
	 *
	 * <p>
	 * A command destructive by its {@link Config#getClique() clique} runs the chain as followed by a
	 * destructive step (see {@link CommandCliques#tone(String)}), so that a confirmation in it draws
	 * its affirmative answer destructive also before a deleting script.
	 * </p>
	 */
	@Override
	public void execute(ReactContext context, ActionArguments arguments) {
		if (arguments == null) {
			execute(context, (Object) null);
			return;
		}
		SuspendedCommands region = region(context);
		ViewActionChain.run(context, _actions, arguments, _after, null,
			region == null ? null : new Suspension(region));
	}

	/**
	 * Runs the configured chain with the given value as main argument, the values of the inputs
	 * read from the given context, see {@link CommandArguments#arguments(ReactContext, Object)}.
	 */
	@Override
	public void execute(ReactContext context, Object input) {
		execute(context, _arguments.arguments(context, input));
	}

	/**
	 * The commands of the region this one runs in, {@code null} where nothing follows them.
	 */
	private static SuspendedCommands region(ReactContext context) {
		if (context instanceof ViewContext viewContext) {
			return viewContext.getScope(SuspendedCommands.class);
		}
		return null;
	}

	/**
	 * Counts a command among the suspended ones of its region while an action holds its chain.
	 */
	private static final class Suspension implements ViewActionChain.HoldObserver {

		private final SuspendedCommands _region;

		Suspension(SuspendedCommands region) {
			_region = region;
		}

		@Override
		public void held() {
			_region.suspend();
		}

		@Override
		public void handedBack() {
			_region.settle();
		}
	}
}
