/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewMessages;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.WithResultActions;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.Task;

/**
 * {@link ViewAction} returning the current tasks of the {@link Scheduler} for display in a table,
 * and optionally acting on the selected task first.
 *
 * <p>
 * The returned {@link SchedulerSnapshot} is meant to be written to the channel feeding the table
 * of the tasks. The task to act on is read from the configured {@link Config#getSelection()
 * selection channel} when the action runs.
 * </p>
 *
 * @implNote Every operation delegates to the {@link Scheduler}, which stores and logs it.
 */
public class SchedulerTaskAction implements ViewAction {

	/**
	 * What a {@link SchedulerTaskAction} does before returning the tasks.
	 */
	public enum Mode {
		/** Just return the current snapshot of the tasks. */
		REFRESH,

		/**
		 * Run the selected task as soon as possible, whether it is active or not.
		 *
		 * @see Scheduler#startTask(Task)
		 */
		RUN_NOW,

		/**
		 * Tell the selected task to stop its current run.
		 *
		 * @see Scheduler#stopTask(Task)
		 */
		STOP,

		/**
		 * Make the selected task active cluster-wide.
		 *
		 * @see Scheduler#activate(Task)
		 */
		ACTIVATE,

		/**
		 * Make the selected task inactive cluster-wide.
		 *
		 * @see Scheduler#deactivate(Task)
		 */
		DEACTIVATE,

		/**
		 * Remove the override of an administrator, so that the configured active state applies.
		 *
		 * @see Scheduler#resetActive(Task)
		 */
		RESET,

		/**
		 * Release the cluster lock of the selected task.
		 *
		 * @see Scheduler#releaseClusterLock(Task)
		 */
		RELEASE_CLUSTER_LOCK;
	}

	/**
	 * Configuration for {@link SchedulerTaskAction}.
	 *
	 * <p>
	 * App-specific action, referenced by {@code class=} in the scheduler view rather than claiming
	 * a global {@code @TagName}.
	 * </p>
	 */
	public interface Config extends PolymorphicConfiguration<SchedulerTaskAction>, WithResultActions {

		/** Configuration name for {@link #getMode()}. */
		String MODE = "mode";

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		@Override
		@ClassDefault(SchedulerTaskAction.class)
		Class<? extends SchedulerTaskAction> getImplementationClass();

		/**
		 * What the action does before returning the tasks.
		 */
		@Name(MODE)
		@Mandatory
		Mode getMode();

		/**
		 * Name of the channel holding the task to act on; required for all modes but
		 * {@link Mode#REFRESH}.
		 */
		@Name(SELECTION)
		@Nullable
		String getSelection();
	}

	private final Mode _mode;

	private final String _selectionChannel;

	/**
	 * Creates a new {@link SchedulerTaskAction} from configuration.
	 */
	@CalledByReflection
	public SchedulerTaskAction(InstantiationContext context, Config config) {
		_mode = config.getMode();
		_selectionChannel = config.getSelection();
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (_mode != Mode.REFRESH
			&& ViewContext.channelValue(context, _selectionChannel) instanceof Task task) {
			apply(context, task);
		}
		return SchedulerSnapshot.current();
	}

	/**
	 * Performs the configured operation on the given task, reporting any failure to the user.
	 */
	private void apply(ReactContext context, Task task) {
		Scheduler scheduler = Scheduler.getSchedulerInstance();
		try {
			switch (_mode) {
				case RUN_NOW:
					scheduler.startTask(task);
					break;
				case STOP:
					if (scheduler.stopTask(task)) {
						ViewMessages.info(context, I18NConstants.SCHEDULER_TASK_STOP_SIGNALED__NAME.fill(label(task)));
					} else {
						ViewMessages.warning(context,
							I18NConstants.ERROR_SCHEDULER_TASK_NOT_STOPPED__NAME.fill(label(task)));
					}
					break;
				case ACTIVATE:
					scheduler.activate(task);
					break;
				case DEACTIVATE:
					scheduler.deactivate(task);
					break;
				case RESET:
					scheduler.resetActive(task);
					break;
				case RELEASE_CLUSTER_LOCK:
					Scheduler.releaseClusterLock(task);
					break;
				case REFRESH:
					break;
			}
		} catch (TopLogicException ex) {
			ViewMessages.error(context, I18NConstants.ERROR_SCHEDULER_TASK__NAME.fill(label(task)),
				ex.getErrorKey());
		} catch (RuntimeException ex) {
			ViewMessages.error(context, I18NConstants.ERROR_SCHEDULER_TASK__NAME.fill(label(task)),
				ResKey.text(ex.getMessage()));
		}
	}

	private static String label(Task task) {
		return Scheduler.getTaskLabel(task);
	}

	/**
	 * Tells the user when the operation fails.
	 */
	@Override
	public boolean changesViewState() {
		return true;
	}
}
