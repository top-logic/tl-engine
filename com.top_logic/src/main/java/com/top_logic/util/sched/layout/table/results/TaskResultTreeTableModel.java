/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.layout.table.results;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.tree.model.DefaultTreeTableModel;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableBuilder;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableNode;
import com.top_logic.layout.tree.model.IndexedTreeTableModel;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.composite.CompositeTask;
import com.top_logic.util.sched.task.composite.cache.CompositeTaskResultCache;
import com.top_logic.util.sched.task.result.TaskResult;

/**
 * An optimized {@link DefaultTreeTableModel} for {@link TaskResult} trees.
 * <p>
 * <ul>
 * <li>The relation "parent {@link TaskResult}" => "child {@link TaskResult}s" is cached.</li>
 * <li>The aforementioned relation is calculated in O(n*log(n)) and not in the naive O(n²).</li>
 * <li>As price for that, the whole tree is build in advance and not on demand.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * For a {@link TaskResult} to become included in the tree, it and all its parents (recursively)
 * have to be matched by the {@link Filter} given in the constructor.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TaskResultTreeTableModel extends IndexedTreeTableModel<DefaultTreeTableNode> {

	/**
	 * Creates a {@link TaskResultTreeTableModel}.
	 * 
	 * @see DefaultTreeTableModel#DefaultTreeTableModel(com.top_logic.layout.tree.model.TreeBuilder,
	 *      Object, List, TableConfiguration)
	 * 
	 * @param tasks
	 *        Is not allowed to be <code>null</code>.
	 * @param filter
	 *        Only {@link TaskResult}s matching this filter are added to the tree. All other
	 *        {@link TaskResult} s are ignored. Is allowed to be <code>null</code>, in which case
	 *        the {@link FilterFactory#trueFilter()} will be used.
	 */
	public TaskResultTreeTableModel(Set<? extends Task> tasks, List<String> columnNames,
			TableConfiguration config, Filter<? super TaskResult> filter) {
		super(new DefaultTreeTableBuilder(), tasks, columnNames, config);
		buildTree(filterNonNull(filter));
	}

	/**
	 * Util for converting <code>null</code> to {@link FilterFactory#trueFilter()}.
	 */
	protected static Filter<? super TaskResult> filterNonNull(Filter<? super TaskResult> filter) {
		if (filter == null) {
			return FilterFactory.trueFilter();
		} else {
			return filter;
		}
	}

	/**
	 * Build the complete tree of {@link TaskResult}s matching the given filter.
	 * <p>
	 * If root has no business object, i.e. if {@link #getTasks()} returns <code>null</code>,
	 * nothing is done.
	 * </p>
	 */
	protected void buildTree(Filter<? super TaskResult> filter) {
		Set<Task> tasks = getTasks();
		if (tasks == null) {
			return;
		}
		CompositeTaskResultCache cache = new CompositeTaskResultCache(tasks, filterNonNull(filter));
		for (Task task : tasks) {
			for (TaskResult taskResult : FilterUtil.filterList(filter, task.getLog().getResults())) {
				buildNode(cache, getRoot(), taskResult);
			}
		}
	}

	/**
	 * Build the node for the given {@link TaskResult} under the given parent.
	 * <p>
	 * Builds not only the node itself, but also its children, recursively.
	 * </p>
	 * 
	 * @param cache
	 *        For fast resolution of the children of the given {@link TaskResult}.
	 */
	protected void buildNode(CompositeTaskResultCache cache, DefaultTreeTableNode parent, TaskResult childResult) {
		DefaultTreeTableNode childNode = parent.createChild(childResult);
		boolean isLeaf = isLeaf(childResult.getTask());
		if (!isLeaf) {
			addChildren(cache, childNode, childResult);
		}
	}

	/**
	 * Build and add all the children of the given {@link TaskResult} to the given node.
	 * <p>
	 * Builds not only the children nodes themselves, but also their children, recursively.
	 * </p>
	 * 
	 * @param cache
	 *        For fast resolution of the children of the given {@link TaskResult}.
	 */
	protected void addChildren(CompositeTaskResultCache cache, DefaultTreeTableNode node, TaskResult taskResult) {
		for (TaskResult childResult : getChildResults(cache, taskResult)) {
			buildNode(cache, node, childResult);
		}
	}

	/**
	 * Get the children of the given parent {@link TaskResult}, from the given cache.
	 * <p>
	 * The returned {@link Collection} is immutable, contains no duplicates and has no guaranteed
	 * order.
	 * </p>
	 */
	protected Collection<TaskResult> getChildResults(CompositeTaskResultCache cache, TaskResult parentResult) {
		// No need to filter the cache entries here, as the cache already filtered them.
		return cache.getChildResults(parentResult);
	}

	/**
	 * Is the given {@link Task} a leaf?
	 * <p>
	 * This is the definition of the semantic of "leaf" for this {@link TaskResultTreeTableModel}.
	 * </p>
	 */
	protected boolean isLeaf(Task task) {
		return !(task instanceof CompositeTask);
	}

	/**
	 * Get the {@link Set} of top level {@link Task}s which is the business object of the root
	 * {@link TLTreeNode}.
	 */
	protected Set<Task> getTasks() {
		return (Set<Task>) getRoot().getBusinessObject();
	}

}
