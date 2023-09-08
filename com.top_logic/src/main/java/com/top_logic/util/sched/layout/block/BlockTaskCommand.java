/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.layout.block;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.conditional.CommandStep;
import com.top_logic.tool.boundsec.conditional.Failure;
import com.top_logic.tool.boundsec.conditional.Hide;
import com.top_logic.tool.boundsec.conditional.PreconditionCommandHandler;
import com.top_logic.tool.boundsec.conditional.Success;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.Task;

/**
 * {@link Scheduler#blockTask(Task) blocks} the {@link Task} selected in the given component.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class BlockTaskCommand extends PreconditionCommandHandler {

	/**
	 * The {@link CommandHandler#getID() id} of the {@link BlockTaskCommand}.
	 */
	public static final String COMMAND_ID = "blockTask";

	/**
	 * Creates a {@link BlockTaskCommand} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public BlockTaskCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected CommandStep prepare(LayoutComponent component, Object model, Map<String, Object> arguments) {
		if (!(model instanceof Task)) {
			return new Failure(com.top_logic.tool.execution.I18NConstants.ERROR_NO_MODEL);
		}

		Task task = (Task) model;

		if (Scheduler.getSchedulerInstance().isTaskBlocked(task)) {
			return new Hide();
		}
		if (!Scheduler.getSchedulerInstance().isBlockingAllowed(task)) {
			return new Failure(
				I18NConstants.BLOCKING_NOT_ALLOWED__TASK.fill(task.getName()));
		}

		return new Success() {
			@Override
			protected void doExecute(DisplayContext context) {
				Scheduler.getSchedulerInstance().blockTask(task);
			}
		};
	}

}
