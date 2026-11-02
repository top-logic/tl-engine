/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
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
import com.top_logic.tool.boundsec.conditional.Hide;
import com.top_logic.tool.boundsec.conditional.Success;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.Task;

/**
 * {@link Scheduler#activate(Task) Activates} an inactive {@link Task}.
 * 
 * <p>
 * The command is hidden for an active {@link Task}.
 * </p>
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class ActivateTaskCommand extends TaskActiveCommand {

	/**
	 * The {@link CommandHandler#getID() id} of the {@link ActivateTaskCommand}.
	 */
	public static final String COMMAND_ID = "activateTask";

	/**
	 * Creates a {@link ActivateTaskCommand} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ActivateTaskCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected CommandStep prepare(LayoutComponent component, Scheduler scheduler, Task task) {
		if (scheduler.isActive(task)) {
			return new Hide();
		}

		return new Success() {
			@Override
			protected void doExecute(DisplayContext context) {
				scheduler.activate(task);
				notifyChanged(component, task);
			}
		};
	}

}
