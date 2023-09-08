/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.layout.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableBuilder;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableNode;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.composite.CompositeTask;

/**
 * A {@link DefaultTreeTableBuilder} for {@link Task}s and especially {@link CompositeTask}s.
 * <p>
 * <b>Assumes that the {@link Scheduler} is the root of the {@link Task} tree.</b>
 * </p>
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class TaskTreeTableBuilder extends DefaultTreeTableBuilder {

	/** The singleton instance of the {@link TaskTreeTableBuilder}. */
	public static final TaskTreeTableBuilder INSTANCE = new TaskTreeTableBuilder();

	private TaskTreeTableBuilder() {
		// Reduce visibility
	}

	@Override
	public List<DefaultTreeTableNode> createChildList(DefaultTreeTableNode parent) {
		return convertToNodeList(getChildTasks(parent.getBusinessObject()), parent);
	}

	private List<Task> getChildTasks(Object parent) {
		if (parent instanceof Scheduler) {
			Scheduler scheduler = (Scheduler) parent;
			return scheduler.getKnownTopLevelTasks();
		}
		if (parent instanceof CompositeTask) {
			CompositeTask task = (CompositeTask) parent;
			return task.getChildren();
		}
		if (parent instanceof Task) {
			return Collections.emptyList();
		}
		throw new IllegalArgumentException("Don't know how to get the child tasks of: "
			+ StringServices.getObjectDescription(parent));
	}

	private List<DefaultTreeTableNode> convertToNodeList(List<Task> childTasks, DefaultTreeTableNode parent) {
		List<DefaultTreeTableNode> childNodes = new ArrayList<>(childTasks.size());
		for (Task task : childTasks) {
			childNodes.add(createNode(parent.getModel(), parent, task));
		}
		return childNodes;
	}

}
