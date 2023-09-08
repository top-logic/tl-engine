/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.layout.table.results;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.FilterFactory;
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
import com.top_logic.util.sched.layout.table.AbstractTaskTreeTableComponent;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.composite.CompositeTask;
import com.top_logic.util.sched.task.log.TaskLog;
import com.top_logic.util.sched.task.result.TaskResult;

/**
 * An {@link TreeTableComponent} for displaying {@link TaskResult}s, especially those of
 * {@link CompositeTask}s.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class TaskResultTreeComponent extends AbstractTaskTreeTableComponent {

	/**
	 * Config interface for the {@link TaskResultTreeComponent}.
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
	 * Creates a {@link TaskResultTreeComponent} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TaskResultTreeComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	protected boolean supportsInternalModel(Object object) {
		return (object == null) || (object instanceof Task);
	}

	@Override
	protected <N extends AbstractTreeTableNode<N>> TaskResultTreeTableModel createTreeModel(
			TableConfiguration tableConfig, List<String> columns) {
		Filter<? super TaskResult> taskResultFilter = FilterFactory.not(TaskLog.ACTIVE_FILTER);
		return new TaskResultTreeTableModel(getTaskAsList(), columns, tableConfig, taskResultFilter);
	}

	/**
	 * Util for getting {@link #getModel()} as {@link Set}.
	 * <p>
	 * Returns either the empty {@link Set} (if {@link #getModel()} is null) or a singleton set.
	 * Either way, the result is immutable.
	 * </p>
	 */
	protected Set<Task> getTaskAsList() {
		Task task = (Task) getModel();
		if (task == null) {
			return Collections.<Task> emptySet();
		} else {
			return Collections.<Task> singleton(task);
		}
	}

}
