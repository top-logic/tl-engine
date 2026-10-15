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
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.ButtonTone;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ViewCommand} that executes a configured chain of {@link ViewAction}s.
 *
 * <p>
 * The actions run with the command's input as their single argument, named after the command's
 * {@link Config#getInput() input channel}, or {@value #DEFAULT_ARGUMENT} for a command without one
 * (typically {@code null} for toolbar buttons). The result of an action is seen by the follow-up
 * actions nested into it (see {@link WithResultActions}). This allows composing complex behaviors
 * from simple, reusable building blocks.
 * </p>
 *
 * <p>
 * Example: Create a transient object and open a dialog with it:
 * </p>
 *
 * <pre>
 * &lt;generic-command label="New"&gt;
 *   &lt;execute-script function="x -&gt; new(`my.module:MyType`, transient: true)"&gt;
 *     &lt;open-dialog dialog-view="demo/create.view.xml" bind-input-to="model"/&gt;
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
		 * Every action runs with the command's input as argument, named after the
		 * {@link #getInput() input channel}.
		 * </p>
		 */
		@DefaultContainer
		@Options(fun = AllInAppImplementations.class)
		List<PolymorphicConfiguration<? extends ViewAction>> getActions();
	}

	/**
	 * Name of the argument holding the input of a command without an
	 * {@link Config#getInput() input channel}.
	 */
	public static final String DEFAULT_ARGUMENT = "input";

	private final String _argumentName;

	private final List<ViewAction> _actions;

	private final ResKey _label;

	/**
	 * Creates a new {@link GenericViewCommand}.
	 */
	@CalledByReflection
	public GenericViewCommand(InstantiationContext context, Config config) {
		_argumentName = argumentName(config);
		_actions = ViewActions.instantiate(context, config.getActions(), ActionScope.single(_argumentName));
		_label = config.getLabel();
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
	 *        The name of the argument holding the command's input.
	 * @param actions
	 *        The chain to run, instantiated in the {@link ActionScope#single(String) scope} of the
	 *        argument.
	 */
	public GenericViewCommand(String argumentName, List<ViewAction> actions) {
		_argumentName = argumentName;
		_actions = List.copyOf(actions);
		_label = null;
	}

	/**
	 * The name of the argument holding the input of a command with the given configuration.
	 */
	public static String argumentName(ViewCommand.Config config) {
		return argumentName(config.getInput());
	}

	/**
	 * The name of the argument holding the input of a command reading the given input channel.
	 *
	 * @param input
	 *        The input channel, {@code null} for a command without one.
	 */
	public static String argumentName(ChannelRef input) {
		return input == null ? DEFAULT_ARGUMENT : input.getChannelName();
	}

	/**
	 * The {@link Config#getLabel() label} this command is configured with, {@code null} for a
	 * command without a label.
	 */
	@Override
	public ResKey getLabel() {
		return _label;
	}

	@Override
	public boolean appliesFormState() {
		return ViewActions.appliesFormState(_actions);
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
	 * Runs the configured chain, counting this command among the
	 * {@link SuspendedCommands suspended commands} of its region for as long as the chain is held
	 * by an action that has not settled it.
	 *
	 * @implNote The chain reports its end through
	 *           {@link ViewActionChain#run(ReactContext, java.util.List, ActionArguments, java.util.function.Consumer, Runnable)},
	 *           which for a synchronous chain happens before {@code run} returns; the handshake
	 *           then finds the chain settled and leaves the region untouched.
	 */
	@Override
	public HandlerResult execute(ReactContext context, Object input) {
		SuspendedCommands region = region(context);
		if (region == null) {
			ViewActionChain.run(context, _actions, arguments(input), null);
			return HandlerResult.DEFAULT_RESULT;
		}

		Suspension suspension = new Suspension(region);
		ViewActionChain.run(context, _actions, arguments(input), null, suspension::settled);
		suspension.returned();
		return HandlerResult.DEFAULT_RESULT;
	}

	private ActionArguments arguments(Object input) {
		return ActionArguments.of(_argumentName, input);
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
	 * Keeps a command counted among the suspended ones of its region exactly while its chain is
	 * held.
	 *
	 * <p>
	 * The two things it reconciles happen in either order: the chain settles, and the call that
	 * started it returns. A chain held over to another interaction may settle on the thread of that
	 * interaction, so both are recorded under the same lock and only the transition that is left
	 * reaches the region.
	 * </p>
	 */
	private static final class Suspension {

		private final SuspendedCommands _region;

		private boolean _settled;

		private boolean _suspended;

		Suspension(SuspendedCommands region) {
			_region = region;
		}

		/**
		 * Records that the call running the chain has returned, suspending the command when the
		 * chain is still held.
		 */
		void returned() {
			boolean suspend;
			synchronized (this) {
				suspend = !_settled;
				_suspended = suspend;
			}
			if (suspend) {
				_region.suspend();
			}
		}

		/**
		 * Records that the chain has settled, releasing the command when it was suspended.
		 */
		void settled() {
			boolean release;
			synchronized (this) {
				_settled = true;
				release = _suspended;
				_suspended = false;
			}
			if (release) {
				_region.settle();
			}
		}
	}
}
