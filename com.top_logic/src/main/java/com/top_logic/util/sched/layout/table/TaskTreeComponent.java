/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.layout.table;


import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.layout.StopTaskCommand;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.TaskListenerNotification;
import com.top_logic.util.sched.task.composite.CompositeTask;

/**
 * An {@link TaskListeningTreeComponent} for displaying {@link Task}s, especially
 * {@link CompositeTask}s.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class TaskTreeComponent extends TaskListeningTreeComponent {

	/**
	 * Configuration for the {@link TaskTreeComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends TaskListeningTreeComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			TaskListeningTreeComponent.Config.super.modifyIntrinsicCommands(registry);
			registry.registerButton(StopTaskCommand.COMMAND_ID);
		}

	}

	/**
	 * Creates a {@link TaskTreeComponent} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TaskTreeComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	protected boolean supportsInternalModel(Object object) {
		return object instanceof Scheduler;
	}

	@Override
	protected boolean processTaskStateChange(TaskListenerNotification notification) {
		return updateTableModel(notification.getTask());
	}

}
