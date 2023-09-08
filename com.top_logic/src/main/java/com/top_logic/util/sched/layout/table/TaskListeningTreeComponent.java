/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.layout.table;

import java.util.Iterator;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.tree.TreeTableComponent;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.TaskListenerNotification;
import com.top_logic.util.sched.task.TaskState;

/**
 * An {@link TreeTableComponent} that is informed about {@link Task} {@link TaskState} changes.
 * <p>
 * The component is only informed while it is visible. It is therefore invalidated when it is
 * {@link #becomingInvisible()}.
 * </p>
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class TaskListeningTreeComponent extends AbstractTaskTreeTableComponent {

	private Iterator<TaskListenerNotification> _updates;

	private volatile boolean _modelValid;

	/**
	 * Creates a {@link TaskListeningTreeComponent} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TaskListeningTreeComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public boolean validateModel(DisplayContext context) {
		super.validateModel(context);
		boolean rebuildNeeded = false;
		while (_updates.hasNext()) {
			rebuildNeeded |= processTaskStateChange(_updates.next());
		}
		if (rebuildNeeded || !_modelValid) {
			rebuildTableModel();
		}
		_modelValid = true;
		return rebuildNeeded;
	}

	/**
	 * Is called in {@link #validateModel(DisplayContext)} for every {@link TaskState} change that
	 * happened.
	 * 
	 * @return Has the table model to be {@link #rebuildTableModel() rebuild}?
	 */
	protected abstract boolean processTaskStateChange(TaskListenerNotification notification);

	@Override
	public boolean isModelValid() {
		return _modelValid && !_updates.hasNext();
	}

	/**
	 * Marks the model as invalid, which causes the {@link TableModel} to be
	 * {@link #rebuildTableModel() rebuilt}.
	 */
	protected void invalidateModel() {
		_modelValid = false;
	}

	@Override
	protected void becomingVisible() {
		super.becomingVisible();
		_updates = Scheduler.getSchedulerInstance().getTaskUpdateQueue().iterator();
	}

	@Override
	protected void becomingInvisible() {
		super.becomingInvisible();
		_updates = null;
		invalidate();
	}

}