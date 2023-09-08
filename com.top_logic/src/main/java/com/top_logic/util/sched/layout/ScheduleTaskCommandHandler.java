/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.layout;

import java.util.Map;

import com.top_logic.base.administration.MaintenanceWindowManager;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.conditional.CommandStep;
import com.top_logic.tool.boundsec.conditional.Failure;
import com.top_logic.tool.boundsec.conditional.PreconditionCommandHandler;
import com.top_logic.tool.boundsec.conditional.Success;
import com.top_logic.util.Resources;
import com.top_logic.util.Utils;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.layout.table.TaskAccessor;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.TaskState;

/**
 * Schedule a task immediately.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ScheduleTaskCommandHandler extends PreconditionCommandHandler {

	/** Unique ID of this command. */
	public static final String COMMAND_ID = "scheduleTask";

	/**
	 * Creates a {@link ScheduleTaskCommandHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ScheduleTaskCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected CommandStep prepare(LayoutComponent component, Object model, Map<String, Object> arguments) {
		if (!(model instanceof Task)) {
			return new Failure(com.top_logic.tool.execution.I18NConstants.ERROR_NO_MODEL);
		}
		Task task = (Task) model;
		if (Scheduler.getSchedulerInstance().isSuspended()) {
			return new Failure(I18NConstants.SCHEDULER_SUSPENDED);
		}
		String taskName = task.getName();
		if (Scheduler.getSchedulerInstance().isTaskBlocked(task)) {
			return new Failure(I18NConstants.TASK_BLOCKED__TASK.fill(taskName));
		}
		if (!Utils.equals(task.getLog().getState(), TaskState.INACTIVE)) {
			return new Failure(I18NConstants.TASK_RUNNING__TASK.fill(taskName));
		}
		if (!Scheduler.getSchedulerInstance().getKnownTopLevelTasks().contains(task)) {
			return new Failure(I18NConstants.TASK_IS_NOT_TOP_LEVEL__TASK.fill(taskName));
		}

		return new Success() {
			@Override
			protected void doExecute(DisplayContext context) {
				scheduleTaskWithOrWithoutMaintenanceMode(context, task);
			}
		};
	}

	/**
	 * Check, if the given task needs maintenance mode and schedule it then.
	 * 
	 * When the task needs a maintenance mode this method will activate it and set the delay to the
	 * {@link MaintenanceWindowManager#minIntervallInCluster}.
	 * 
	 * @param aContext
	 *        The display context for the {@link MessageBox}.
	 * @param aTask
	 *        The task to get the error message for, may be <code>null</code>.
	 * @return The command result description.
	 * @see #runTask(DisplayContext, Task)
	 */
	protected HandlerResult scheduleTaskWithOrWithoutMaintenanceMode(DisplayContext aContext, final Task aTask) {
		final long theSeconds;

		if (aTask.needsMaintenanceMode()) {
			final MaintenanceWindowManager theManager = MaintenanceWindowManager.getInstance();
			final int theState = theManager.getMaintenanceModeState();

			if (theState == MaintenanceWindowManager.DEFAULT_MODE) {
				theSeconds = theManager.minIntervallInCluster / 1000;
			} else if (theState == MaintenanceWindowManager.ABOUT_TO_ENTER_MAINTENANCE_MODE) {
				theSeconds = theManager.getTimeLeft() / 1000;
			} else {
				theSeconds = 0;
			}

			if (theSeconds > 0) {
				String theMessage =
					aContext.getResources().getString(I18NConstants.SHOULD_START_MAINTENANCE_MODE_FOR_TASK);

				return MessageBox.confirm(aContext, MessageType.CONFIRM, theMessage,
					MessageBox.button(ButtonType.YES, new Command() {

						@Override
						public HandlerResult executeCommand(DisplayContext aDisplayContext) {
							if (theState == MaintenanceWindowManager.DEFAULT_MODE) {
								theManager.enterMaintenanceWindow();
							}

							return runTask(aDisplayContext, aTask);
						}

					}), MessageBox.button(ButtonType.NO));
			}
		}

		return this.runTask(aContext, aTask);
	}

	/**
	 * Schedule the given {@link Task} and display the resulting {@link MessageBox}.
	 * 
	 * @param context
	 *        The display context for the {@link MessageBox}.
	 * @param task
	 *        The task to be started. Must not be <code>null</code>.
	 */
	protected HandlerResult runTask(DisplayContext context, Task task) {
		Scheduler.getSchedulerInstance().startTask(task);
		Logger.info("Requested start of task '" + task.getName() + ".", ScheduleTaskCommandHandler.class);
		return getSuccessResult(context, task);
	}

	/**
	 * Display a {@link MessageBox} informing the user, that the task has been scheduled.
	 * 
	 * @param context
	 *        The display context for the {@link MessageBox}.
	 * @param task
	 *        The task to get the error message for, may be <code>null</code>, if the given message
	 *        key does not expect a task name parameter.
	 * @return The requested success result object.
	 */
	protected HandlerResult getSuccessResult(DisplayContext context, Task task) {
		return this.showDialog(context, task, I18NConstants.SCHEDULED_SUCCESSFULLY__TASK, MessageType.INFO);
	}

	/**
	 * Create an error result for the given parameters.
	 * 
	 * @param context
	 *        The display context for the {@link MessageBox}.
	 * @param task
	 *        The task to get the error message for, may be <code>null</code>, if the given message
	 *        key does not expect a task name parameter.
	 * @param messageKey_task
	 *        The error message key with task name parameter to be used for the message.
	 * @return The requested error result object.
	 */
	protected HandlerResult getErrorResult(DisplayContext context, Task task, ResKey1 messageKey_task) {
		return this.showDialog(context, task, messageKey_task, MessageType.WARNING);
	}

	/**
	 * Display a {@link MessageBox} containing a message for the given parameters.
	 * 
	 * @param context
	 *        The display context for the {@link MessageBox}.
	 * @param task
	 *        The task to get the error message for, may be <code>null</code>, if the given message
	 *        key does not expect a task name parameter.
	 * @param messageKey_task
	 *        The error message key with task name parameter to be used for the message.
	 * @param messageType
	 *        The type of the dialog displayed.
	 * @return The requested result object.
	 */
	protected HandlerResult showDialog(DisplayContext context, Task task, ResKey1 messageKey_task,
			MessageType messageType) {
		String theName = (task != null) ? (String) TaskAccessor.INSTANCE.getValue(task, TaskAccessor.NAME) : "";
		String theMessage = Resources.getInstance().getString(messageKey_task.fill(theName));

		return MessageBox.confirm(context, messageType, theMessage, MessageBox.button(ButtonType.OK, new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext aDisplayContext) {
				return HandlerResult.DEFAULT_RESULT;
			}
		}));
	}
}
