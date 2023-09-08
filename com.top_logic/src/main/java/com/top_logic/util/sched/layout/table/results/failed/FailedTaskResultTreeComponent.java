/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.layout.table.results.failed;


import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.tree.TreeTableComponent;
import com.top_logic.layout.table.tree.UpdateTreeTableCommandHandler;
import com.top_logic.layout.tree.model.AbstractTreeTableModel.AbstractTreeTableNode;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableNode;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.layout.table.AbstractTaskTreeTableComponent;
import com.top_logic.util.sched.layout.table.results.TaskResultTreeTableModel;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.composite.CompositeTask;
import com.top_logic.util.sched.task.log.TaskLog;
import com.top_logic.util.sched.task.result.TaskResult;

/**
 * An {@link TreeTableComponent} for displaying failed {@link TaskResult}s, especially those of
 * {@link CompositeTask}s.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class FailedTaskResultTreeComponent extends AbstractTaskTreeTableComponent {

	/**
	 * Config interface for the {@link FailedTaskResultTreeComponent}.
	 */
	public interface Config extends AbstractTaskTreeTableComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@NullDefault
		@Override
		PolymorphicConfiguration<TreeBuilder<DefaultTreeTableNode>> getTreeBuilder();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			AbstractTaskTreeTableComponent.Config.super.modifyIntrinsicCommands(registry);
			registry.registerButton(UpdateTreeTableCommandHandler.COMMAND_ID);
		}

	}

	/**
	 * Creates a {@link FailedTaskResultTreeComponent} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public FailedTaskResultTreeComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	protected boolean supportsInternalModel(Object object) {
		return object instanceof Scheduler;
	}

	@Override
	protected <N extends AbstractTreeTableNode<N>> TaskResultTreeTableModel createTreeModel(
			TableConfiguration tableConfig, List<String> columns) {
		return new TaskResultTreeTableModel(getTopLevelTasks(), columns, tableConfig, TaskLog.PROBLEM_FILTER);
	}

	/**
	 * Util for getting all top level {@link Task}s from the Scheduler as {@link Set}.
	 */
	protected Set<Task> getTopLevelTasks() {
		Scheduler scheduler = (Scheduler) getModel();
		/* "new HashSet<Task>(List<Task>)" is not possible: "Task" and "Task" seem to be
		 * incompatible here. Therefore, instantiate the set without type parameters: */
		return (Set<Task>) new HashSet(scheduler.getKnownTopLevelTasks());
	}

}
