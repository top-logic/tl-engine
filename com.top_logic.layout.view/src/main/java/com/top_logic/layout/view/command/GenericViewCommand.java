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
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ViewCommand} that executes a configured chain of {@link ViewAction}s.
 *
 * <p>
 * Each action's return value becomes the input of the next action. The first action receives the
 * command's input (typically {@code null} for toolbar buttons). This allows composing complex
 * behaviors from simple, reusable building blocks.
 * </p>
 *
 * <p>
 * Example: Create a transient object and open a dialog with it:
 * </p>
 *
 * <pre>
 * &lt;generic-command label="New"&gt;
 *   &lt;execute-script function="x -&gt; new(`my.module:MyType`, transient: true)"/&gt;
 *   &lt;open-dialog dialog-view="demo/create.view.xml" bind-input-to="model"/&gt;
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
		 */
		@DefaultContainer
		@Options(fun = AllInAppImplementations.class)
		List<PolymorphicConfiguration<? extends ViewAction>> getActions();
	}

	private final List<ViewAction> _actions;

	/**
	 * Creates a new {@link GenericViewCommand}.
	 */
	@CalledByReflection
	public GenericViewCommand(InstantiationContext context, Config config) {
		_actions = ViewActions.instantiate(context, config.getActions());
	}

	/**
	 * Creates a {@link GenericViewCommand} running the given actions.
	 *
	 * @param actions
	 *        The chain to run, each action's result becoming the input of the next one.
	 */
	public GenericViewCommand(List<ViewAction> actions) {
		_actions = List.copyOf(actions);
	}

	@Override
	public boolean appliesFormState() {
		return ViewActions.appliesFormState(_actions);
	}

	/**
	 * Runs the configured chain, counting this command among the
	 * {@link SuspendedCommands suspended commands} of its region for as long as the chain is held
	 * by an action that has not settled it.
	 *
	 * @implNote The chain reports its end through
	 *           {@link ViewActionChain#run(ReactContext, java.util.List, Object, java.util.function.Consumer, Runnable)},
	 *           which for a synchronous chain happens before {@code run} returns; the handshake
	 *           then finds the chain settled and leaves the region untouched.
	 */
	@Override
	public HandlerResult execute(ReactContext context, Object input) {
		SuspendedCommands region = region(context);
		if (region == null) {
			ViewActionChain.run(context, _actions, input, null);
			return HandlerResult.DEFAULT_RESULT;
		}

		Suspension suspension = new Suspension(region);
		ViewActionChain.run(context, _actions, input, null, suspension::settled);
		suspension.returned();
		return HandlerResult.DEFAULT_RESULT;
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
