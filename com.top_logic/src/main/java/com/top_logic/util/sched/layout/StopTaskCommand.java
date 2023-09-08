/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.layout;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.conditional.CommandStep;
import com.top_logic.tool.boundsec.conditional.Failure;
import com.top_logic.tool.boundsec.conditional.PreconditionCommandHandler;
import com.top_logic.tool.boundsec.conditional.Success;
import com.top_logic.util.Utils;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.TaskState;

/**
 * {@link Task#signalStop() Signals} the {@link Task} which is the model of the given component to
 * stop.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class StopTaskCommand extends PreconditionCommandHandler {

	/**
	 * Id of {@link StopTaskCommand}.
	 */
	public static final String COMMAND_ID = "stopTask";

	/** The continuation of the {@link StopTaskCommand} after the user confirmed it. */
	protected static final class StopTaskConfirmedCommand implements Command {

		private final Task _task;

		StopTaskConfirmedCommand(Task task) {
			_task = task;
		}

		@Override
		public HandlerResult executeCommand(DisplayContext context) {
			stopTask(_task);
			return showDone(_task, context);
		}

		/** Stop the {@link Task}. */
		protected void stopTask(Task task) {
			Logger.info("User requested stop of Task '" + task.getName() + "'.", StopTaskCommand.class);
			task.signalStop();
		}

		/**
		 * Show a message after stopping the {@link Task} to let the user know the command
		 * succeeded.
		 */
		protected HandlerResult showDone(Task task, DisplayContext context) {
			String message = context.getResources().getMessage(I18NConstants.TASK_STOP_SIGNALED, task.getName());
			CommandModel buttonOk = MessageBox.button(ButtonType.OK);
			return MessageBox.confirm(context, MessageType.INFO, message, buttonOk);
		}

	}

	/**
	 * Creates a {@link StopTaskCommand} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public StopTaskCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected CommandStep prepare(LayoutComponent component, Object model, Map<String, Object> arguments) {
		if (!(model instanceof Task)) {
			return new Failure(com.top_logic.tool.execution.I18NConstants.ERROR_NO_MODEL);
		}
		Task task = (Task) model;
		if (!Utils.equals(task.getLog().getState(), TaskState.RUNNING)) {
			return new Failure(I18NConstants.TASK_NOT_RUNNING__TASK.fill(task.getName()));
		}

		return new Success() {
			@Override
			protected void doExecute(DisplayContext context) {
				askConfirm(task, context, new StopTaskConfirmedCommand(task));
			}
		};
	}

	/** Ask for confirmation, before stopping the {@link Task}. */
	protected HandlerResult askConfirm(Task task, DisplayContext context, Command continuation) {
		String message = context.getResources().getMessage(I18NConstants.STOP_TASK_CONFIRM, task.getName());
		CommandModel buttonYes = MessageBox.button(ButtonType.YES, continuation);
		CommandModel buttonNo = MessageBox.button(ButtonType.NO);
		return MessageBox.confirm(context, MessageType.CONFIRM, message, buttonYes, buttonNo);
	}

}
