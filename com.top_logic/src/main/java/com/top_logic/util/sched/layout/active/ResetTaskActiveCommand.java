/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.layout.active;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.conditional.CommandStep;
import com.top_logic.tool.boundsec.conditional.Failure;
import com.top_logic.tool.boundsec.conditional.Success;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.Task;

/**
 * {@link Scheduler#resetActive(Task) Resets} a {@link Task} to its
 * {@link Scheduler#isConfiguredActive(Task) configured active state}.
 * 
 * <p>
 * The command is executable only for a {@link Task} whose state an administrator has
 * {@link Scheduler#hasActiveOverride(Task) overridden}.
 * </p>
 */
public class ResetTaskActiveCommand extends TaskActiveCommand {

	/**
	 * The {@link CommandHandler#getID() id} of the {@link ResetTaskActiveCommand}.
	 */
	public static final String COMMAND_ID = "resetTaskActive";

	/**
	 * Creates a {@link ResetTaskActiveCommand} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ResetTaskActiveCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected CommandStep prepare(LayoutComponent component, Scheduler scheduler, Task task) {
		if (!scheduler.hasActiveOverride(task)) {
			return new Failure(I18NConstants.ERROR_NO_ACTIVE_OVERRIDE__TASK.fill(task.getName()));
		}

		return new Success() {
			@Override
			protected void doExecute(DisplayContext context) {
				scheduler.resetActive(task);
				notifyChanged(component, task);
			}
		};
	}

}
