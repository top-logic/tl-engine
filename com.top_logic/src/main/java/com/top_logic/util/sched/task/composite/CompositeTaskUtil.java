/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.composite;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.result.TaskResult;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;

/**
 * Useful methods when working with {@link CompositeTask}.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class CompositeTaskUtil {

	/**
	 * Finds the {@link TaskResult}s of the child {@link Task}s of the parent {@link CompositeTask}
	 * that belong to the given parent {@link TaskResult}.
	 */
	public static List<TaskResult> findCorrespondingChildResults(TaskResult parentResult) {
		CompositeTask parentTask = (CompositeTask) parentResult.getTask();
		List<TaskResult> childResults = new ArrayList<>(parentTask.getChildren().size());
		for (Task childTask : parentTask.getChildren()) {
			childResults.addAll(findCorrespondingResults(childTask, parentResult));
		}
		return childResults;
	}

	/**
	 * Finds the {@link TaskResult} of the child {@link Task} that belongs to the given parent
	 * {@link TaskResult}.
	 * <p>
	 * If no corresponding child is found, an empty list is returned. <br/>
	 * <b>If there is more than one result, all of them are returned! And a warning is logged.</b>
	 * That can happen for example during debugging, when a "drop to frame" is done at the wrong
	 * position and a child task is run twice. If this method would throw an exception instead, the
	 * application would no longer be able to display results for the affected tasks.
	 * </p>
	 * 
	 * @return Never <code>null</code>.
	 */
	public static List<TaskResult> findCorrespondingResults(Task childTask, TaskResult parentResult) {
		List<TaskResult> matches = new ArrayList<>(1);
		for (TaskResult childResult : childTask.getLog().getResults()) {
			if (isCorrespondingChild(childResult, parentResult)) {
				matches.add(childResult);
			}
		}
		if (matches.size() > 1) {
			Logger.warn("Expected to find at most one child result for " + parentResult
				+ ", but found " + matches.size() + ": " + matches + ".", CompositeTaskUtil.class);
		}
		return matches;
	}

	/**
	 * Is the child {@link TaskResult} a child of the parent {@link TaskResult}?
	 * <p>
	 * None of the parameters is allowed to be <code>null</code>. <br/>
	 * The child {@link TaskResult} has to belong to a child {@link Task} of the parent
	 * {@link TaskResult}s {@link CompositeTask}. This is not checked, just assumed.
	 * </p>
	 */
	public static boolean isCorrespondingChild(TaskResult childResult, TaskResult parentResult) {
		if (!StringServices.equals(parentResult.getClusterNode(), childResult.getClusterNode())) {
			return false;
		}
		if ((parentResult.getResultType() == ResultType.NOT_FINISHED)
			&& (!childResult.getStartDate().before(parentResult.getStartDate()))) {
			return true;
		}
		if (childResult.getStartDate().before(parentResult.getStartDate())) {
			return false;
		}
		if (!hasEnded(childResult)) {
			return !hasEnded(parentResult);
		}
		if (!hasEnded(parentResult)) {
			return true;
		}
		return !childResult.getEndDate().after(parentResult.getEndDate());
	}

	private static boolean hasEnded(TaskResult taskResult) {
		return taskResult.getEndDate() != null;
	}

	/**
	 * Returns a {@link List} of children of this {@link Task}.
	 * <p>
	 * There is no guaranteed order.<br/>
	 * The returned {@link List} is always mutable. <br/>
	 * <b>The given {@link Task} itself is not included in the result.</b>
	 * </p>
	 * 
	 * @see #getDescendents(Task)
	 */
	public static List<Task> getChildrenRecursive(Task task) {
		if (!(task instanceof CompositeTask)) {
			return new ArrayList<>(0);
		}
		ArrayList<Task> children = new ArrayList<>();
		CompositeTask parentTask = (CompositeTask) task;
		for (Task child : parentTask.getChildren()) {
			children.add(child);
			if (child instanceof CompositeTask) {
				children.addAll(getChildrenRecursive(child));
			}
		}
		return children;
	}

	/**
	 * Returns a {@link List} of descendants of this {@link Task}.
	 * <p>
	 * Descendants are: The given {@link Task} itself, its children and all of their children,
	 * recursively. <br/>
	 * There is no guaranteed order. <br/>
	 * The returned {@link List} is always mutable. <br/>
	 * </p>
	 * 
	 * @see #getChildrenRecursive(Task)
	 */
	public static List<Task> getDescendents(Task task) {
		List<Task> result = getChildrenRecursive(task);
		result.add(task);
		return result;
	}

}
