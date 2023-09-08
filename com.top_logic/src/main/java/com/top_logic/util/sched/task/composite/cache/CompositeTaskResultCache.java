/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.composite.cache;

import static com.top_logic.basic.shared.collection.CollectionUtilShared.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.composite.CompositeTask;
import com.top_logic.util.sched.task.composite.CompositeTaskUtil;
import com.top_logic.util.sched.task.result.TaskResult;

/**
 * Caches all "parent {@link TaskResult}" => "child {@link TaskResult}" relations for a given
 * {@link Task} and all its descendants, recursively.
 * <p>
 * The relations are initialized when the constructor is called. The runtime complexity is optimized
 * from the naive O(n²) approach to O(n*log(n)), where "n" is the number of {@link TaskResult}s per
 * {@link Task}. Algorithm: Sort the {@link TaskResult}s for each {@link Task} separately by date.
 * Move along the lists of parent and child {@link TaskResult}s and take the first child
 * {@link TaskResult} from each list as the corresponding one. <br/>
 * Edge cases and error resilience makes the code a bit more complex. <br/>
 * This cache takes an {@link Filter} in its constructor to reduce the amount of cached data to the
 * necessary minimum.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class CompositeTaskResultCache {

	/**
	 * Maps a given {@link TaskResult} to the corresponding child results.
	 * <p>
	 * Stores the mapping not only for the {@link Task}s itself given in the constructor, but for
	 * every {@link CompositeTask} directly or indirectly below it. A separate map for every
	 * {@link Task} would be cleaner, but one big map is (probably) faster. <br/>
	 * The cache entries are declared as {@link Collection} and not {@link Set}, as the they are
	 * actually {@link List}s to optimize the performance for traversal. Traversal was the only
	 * operation performed on them when the cache was implemented. This might change later. To make
	 * it easier to adapt the cache to future use cases, the cache entries are declared as
	 * {@link Collection} and not {@link List} or {@link Set}.
	 * </p>
	 */
	private final Map<TaskResult, Collection<TaskResult>> _childrenMapping;

	/**
	 * Creates a {@link CompositeTaskResultCache}.
	 * 
	 * @param task
	 *        Is not allowed to be <code>null</code>.
	 */
	public CompositeTaskResultCache(Task task) {
		this(Collections.singleton(task), null);
	}

	/**
	 * Creates a {@link CompositeTaskResultCache}.
	 * 
	 * @param task
	 *        Is not allowed to be <code>null</code>.
	 * @param filter
	 *        Only {@link TaskResult}s matching this filter are cached. All other {@link TaskResult}
	 *        s are ignored and stored neither as keys nor values. For reducing amount of cached
	 *        data to the minimum. Is allowed to be <code>null</code>, in which case the
	 *        {@link FilterFactory#trueFilter()} will be used.
	 */
	public CompositeTaskResultCache(Task task, Filter<? super TaskResult> filter) {
		this(Collections.singleton(task), filter);
	}

	/**
	 * Creates a {@link CompositeTaskResultCache}.
	 * 
	 * @param tasks
	 *        Is not allowed to be <code>null</code>.
	 * @param filter
	 *        Only {@link TaskResult}s matching this filter are cached. All other {@link TaskResult}
	 *        s are ignored and stored neither as keys nor values. For reducing the amount of cached
	 *        data to the minimum. Is allowed to be <code>null</code>, in which case the
	 *        {@link FilterFactory#trueFilter()} will be used.
	 */
	public CompositeTaskResultCache(Set<? extends Task> tasks, Filter<? super TaskResult> filter) {
		_childrenMapping = buildChildMapping(tasks, filterNonNull(filter));
	}

	/**
	 * Get the children of the given parent {@link TaskResult}.
	 * <p>
	 * The result contains only {@link TaskResult}s that are matched by the {@link Filter} given in
	 * the constructor. If the given parent {@link TaskResult} is not matched by the {@link Filter},
	 * the result will be an empty collection, even if there is a child that is matched by the
	 * {@link Filter}.
	 * </p>
	 * 
	 * @return A {@link Collection} that is immutable, contains no duplicates and has no guaranteed
	 *         order.
	 */
	public Collection<TaskResult> getChildResults(TaskResult parentResult) {
		return Collections.unmodifiableCollection(nonNull(_childrenMapping.get(parentResult)));
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
	 * Calls {@link #fillWithParentToChildMappings(Map, CompositeTask, Filter)} for every given
	 * {@link Task} and their (recursive) descendants and returns the resulting {@link Map}.
	 * <p>
	 * The result contains only {@link TaskResult}s that are matched by the given {@link Filter}. If
	 * a parent {@link TaskResult} is not matched by the {@link Filter}, the result will be an empty
	 * collection, even if there is a child that is matched by the {@link Filter}.
	 * </p>
	 * 
	 * @param tasks
	 *        Is not allowed to be <code>null</code>. Is not allowed to contain <code>null</code>.
	 * @param filter
	 *        Is not allowed to be <code>null</code>.
	 * @return Never <code>null</code>. Does never contain <code>null</code>, neither as key, nor
	 *         value, nor value entry.
	 */
	protected Map<TaskResult, Collection<TaskResult>> buildChildMapping(Set<? extends Task> tasks,
			Filter<? super TaskResult> filter) {
		Map<TaskResult, Collection<TaskResult>> childrenMapping = new HashMap<>();
		for (Task task : tasks) {
			List<Task> descendants = CompositeTaskUtil.getDescendents(task);
			for (Task descendant : descendants) {
				if (descendant instanceof CompositeTask) {
					fillWithParentToChildMappings(childrenMapping, (CompositeTask) descendant, filter);
				}
			}
		}
		return childrenMapping;
	}

	/**
	 * Add the {@link TaskResult}s for the given {@link CompositeTask} to the given {@link Map}.
	 * 
	 * @param childrenMapping
	 *        <b>Output parameter.</b> The map of parent {@link TaskResult}s to their corresponding
	 *        children. Is not allowed to be <code>null</code>. Is not allowed to contain
	 *        <code>null</code>, neither as key nor value.
	 * @param task
	 *        The parent {@link Task}, whose child results should be added to the children
	 *        {@link Map}. Is not allowed to be <code>null</code>.
	 * @param filter
	 *        Only {@link TaskResult}s matching this filter are added. All other {@link TaskResult}
	 *        s are ignored and stored neither as keys nor values. For reducing the amount of cached
	 *        data to the minimum. Is not allowed to be <code>null</code>.
	 */
	protected void fillWithParentToChildMappings(Map<TaskResult, Collection<TaskResult>> childrenMapping,
			CompositeTask task, Filter<? super TaskResult> filter) {

		Map<Task, ListIterator<TaskResult>> childResultIterators =
			createChildResultsIterators(task, filter);
		/* Get the list of parent results after the child results to prevent race conditions where a
		 * new parent result is created but not in the list but the child result is in the list. */
		List<? extends TaskResult> parentResults = getResultsFilteredAndSorted(task, filter);
		for (TaskResult parentResult : parentResults) {
			List<TaskResult> matchingChildResults = findMatchingChildResults(parentResult, childResultIterators);
			childrenMapping.put(parentResult, matchingChildResults);
		}
	}

	/**
	 * Search the corresponding child {@link TaskResult}s for the given parent {@link TaskResult}.
	 * 
	 * @param parentResult
	 *        The {@link TaskResult} whose children should be searched. Has to be the result of a
	 *        {@link CompositeTask}. Is not allowed to be <code>null</code>.
	 * @param childResults
	 *        The {@link TaskResult}s for all the children of the given parent {@link Task}. Is not
	 *        allowed to be <code>null</code>. Is not allowed to contain <code>null</code>, neither
	 *        as key nor value.
	 */
	protected List<TaskResult> findMatchingChildResults(TaskResult parentResult,
			Map<Task, ListIterator<TaskResult>> childResults) {

		CompositeTask parentTask = (CompositeTask) parentResult.getTask();
		List<TaskResult> matchingChildResults = new ArrayList<>(parentTask.getChildren().size());
		for (Task childTask : parentTask.getChildren()) {
			ListIterator<TaskResult> allChildResults = childResults.get(childTask);
			addMatchingChildResults(parentResult, allChildResults, matchingChildResults);
		}
		return matchingChildResults;
	}

	/**
	 * Search the corresponding child {@link TaskResult} for the given parent {@link TaskResult} in
	 * the given list of all child {@link TaskResult}s.
	 * <p>
	 * Implementation note:
	 * <p>
	 * This loop should only perform one iteration, as there is at most one children in the given
	 * child result list. In some "should not happen" edge-cases, there might be more than one
	 * matching child {@link TaskResult}, though. To prevent this algorithm from failing because of
	 * them, this is a loop.
	 * </p>
	 * <p>
	 * Additionally, there might be child results without corresponding parent results: When a
	 * parent Task does not run all its children because one of them failed, there are fewer child
	 * results than parent results for some of the children. When the parent results are deleted,
	 * because there are too many of them, the results of those children live on, as there are fewer
	 * results for them. Those orphan child results can appear not only at the end/beginning of the
	 * results, but also somewhere in the middle, as there are separate limits for the number of
	 * successful results and failing results.
	 * </p>
	 * </p>
	 * 
	 * @param parentResult
	 *        The {@link TaskResult} whose children should be searched. Is not allowed to be
	 *        <code>null</code>.
	 * @param allChildResults
	 *        The {@link Task} of the given parent {@link TaskResult} has multiple child
	 *        {@link Task}s. These are the {@link TaskResult}s for one of them. Is not allowed to be
	 *        <code>null</code>. Is not allowed to contain <code>null</code>.
	 * @param matchingChildResults
	 *        <b>Output parameter.</b> The {@link List} of child {@link TaskResult}s belonging to
	 *        the given parent {@link TaskResult}. The matching child {@link TaskResult} is added to
	 *        it. In some "should not happen" edge-cases, there might be more than one matching
	 *        child {@link TaskResult}, though. In that case, all of them are added. Is not allowed
	 *        to be <code>null</code>. Is not allowed to contain <code>null</code>.
	 */
	protected void addMatchingChildResults(TaskResult parentResult,
			ListIterator<TaskResult> allChildResults, List<TaskResult> matchingChildResults) {
		while (allChildResults.hasNext()) {
			TaskResult currentChildResult = allChildResults.next();
			// Should match in the second iteration of this loop and optimize the
			// performance from O(parentResult * childTasks * childResults)
			// to O(parentResult * childTasks).
			if (after(currentChildResult, parentResult)) {
				allChildResults.previous();
				break;
			}
			if (before(currentChildResult, parentResult)) {
				/* Child result without corresponding parent result. Happens when the parent
				 * result has already been deleted but not yet the child result. */
				continue;
			}
			/* The childResult is neither before nor after the parentResult. It is therefore
			 * matching the parentResult. */
			matchingChildResults.add(currentChildResult);
		}
	}

	/**
	 * For every child of the given {@link Task}, creates an {@link Iterator} over the sorted and
	 * filtered list of {@link TaskResult}s.
	 * <p>
	 * Creates {@link ListIterator} as {@link ListIterator#previous()} is needed by the caller of
	 * this method.
	 * </p>
	 * 
	 * @param task
	 *        Is not allowed to be <code>null</code>.
	 * @param filter
	 *        Is not allowed to be <code>null</code>.
	 * @return Is never <code>null</code>. Contains never <code>null</code>, neither as key nor
	 *         value.
	 */
	protected Map<Task, ListIterator<TaskResult>> createChildResultsIterators(CompositeTask task,
			Filter<? super TaskResult> filter) {

		Map<Task, ListIterator<TaskResult>> result =
			new HashMap<>();

		for (Task childTask : task.getChildren()) {
			List<TaskResult> childResults = getResultsFilteredAndSorted(childTask, filter);
			result.put(childTask, childResults.listIterator());
		}
		return result;
	}

	/**
	 * Retrieves the {@link TaskResult}s for the given {@link Task}, filters, {@link #sort(List)
	 * sorts} and returns them.
	 * 
	 * @param task
	 *        Is not allowed to be <code>null</code>.
	 * @param filter
	 *        Is not allowed to be <code>null</code>.
	 * @return Is never <code>null</code>. Contains never <code>null</code>.
	 */
	protected List<TaskResult> getResultsFilteredAndSorted(Task task, Filter<? super TaskResult> filter) {
		return sort(FilterUtil.filterList(filter, task.getLog().getResults()));
	}

	/**
	 * Calls {@link Collections#sort(List, java.util.Comparator)} with the
	 * {@link TaskResultComparator}.
	 * 
	 * @param taskResults
	 *        Is not allowed to be <code>null</code>
	 */
	protected <T extends TaskResult> List<T> sort(List<T> taskResults) {
		Collections.sort(taskResults, TaskResultComparator.getInstance());
		return taskResults;
	}

	/**
	 * Convenience shortcut for {@link TaskResultComparator#compare(TaskResult, TaskResult)}.
	 * <p>
	 * <b>If this method is overridden, {@link #before(TaskResult, TaskResult)} needs to be
	 * overridden as well.</b>
	 * </p>
	 */
	protected boolean after(TaskResult left, TaskResult right) {
		return TaskResultComparator.INSTANCE.compare(left, right) > 0;
	}

	/**
	 * Convenience shortcut for {@link TaskResultComparator#compare(TaskResult, TaskResult)}.
	 * <p>
	 * <b>If this method is overridden, {@link #after(TaskResult, TaskResult)} needs to be
	 * overridden as well.</b>
	 * </p>
	 */
	protected boolean before(TaskResult left, TaskResult right) {
		return TaskResultComparator.INSTANCE.compare(left, right) < 0;
	}


}
