/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.layout.active;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.conditional.CommandStep;
import com.top_logic.tool.boundsec.conditional.Hide;
import com.top_logic.tool.boundsec.conditional.PreconditionCommandHandler;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.Task;

/**
 * Base class for commands changing whether a {@link Task} is {@link Scheduler#isActive(Task)
 * active}.
 * 
 * <p>
 * The command is hidden, if its target is not a {@link Task}.
 * </p>
 */
public abstract class TaskActiveCommand extends PreconditionCommandHandler {

	/**
	 * Creates a {@link TaskActiveCommand} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public TaskActiveCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected final CommandStep prepare(LayoutComponent component, Object model, Map<String, Object> arguments) {
		if (!(model instanceof Task)) {
			return new Hide();
		}
		return prepare(component, Scheduler.getSchedulerInstance(), (Task) model);
	}

	/**
	 * Checks the preconditions and creates the step that changes the given {@link Task}.
	 * 
	 * @param component
	 *        The component the command is executed on.
	 * @param scheduler
	 *        The {@link Scheduler} the {@link Task} is registered at.
	 * @param task
	 *        The {@link Task} to change.
	 * @return The {@link CommandStep} to execute.
	 */
	protected abstract CommandStep prepare(LayoutComponent component, Scheduler scheduler, Task task);

	/**
	 * Announces the change of the given {@link Task}, so that all components displaying it are
	 * updated.
	 * 
	 * @param component
	 *        The component the command was executed on.
	 * @param task
	 *        The changed {@link Task}.
	 */
	protected static void notifyChanged(LayoutComponent component, Task task) {
		component.fireModelModifiedEvent(task, component);
	}

}
