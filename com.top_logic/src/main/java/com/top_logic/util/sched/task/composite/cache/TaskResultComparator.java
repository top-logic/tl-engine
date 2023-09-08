/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.composite.cache;

import java.util.Comparator;
import java.util.Date;

import com.top_logic.util.Utils;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.result.TaskResult;

/**
 * Compares {@link TaskResult}s by their cluster node and date.
 * <p>
 * <b>Note: This comparator imposes orderings that are inconsistent with equals.</b> Two
 * {@link TaskResult}s are considered equal if one is not {@link #before(TaskResult, TaskResult)} /
 * {@link #after(TaskResult, TaskResult)} the other and both are from the same cluster node.
 * </p>
 * <p>
 * This class is only to be used within the {@link CompositeTaskResultCache}, as the sorting defined
 * by this {@link Comparator} does not make sense anywhere else. Especially, the semantic of
 * "equals" was chosen to simplify the implementation of the {@link CompositeTaskResultCache} by
 * reducing the number of "should not happen" edge cases to worry about.
 * </p>
 * <p>
 * The compared {@link TaskResult}s are not allowed to be null.
 * </p>
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
class TaskResultComparator<T extends TaskResult> implements Comparator<T> {

	/** The singleton instance of the {@link TaskResultComparator}. */
	public static final TaskResultComparator<TaskResult> INSTANCE = new TaskResultComparator<>();

	/** Getter for the singleton instance of the {@link TaskResultComparator}. */
	@SuppressWarnings("unchecked")
	public static final <T extends TaskResult> Comparator<T> getInstance() {
		return (Comparator<T>) INSTANCE;
	}

	@Override
	public int compare(T left, T right) {
		if (!Utils.equals(left.getClusterNode(), right.getClusterNode())) {
			int clusterNodeCompare = left.getClusterNode().compareTo(right.getClusterNode());
			if (clusterNodeCompare != 0) {
				return clusterNodeCompare;
			}
		}
		if (before(left, right)) {
			return -1;
		}
		if (after(left, right)) {
			return +1;
		}
		return 0;
	}

	/**
	 * Is the left {@link TaskResult} after the right one?
	 * <p>
	 * The left {@link TaskResult} is after the right one, if the right is
	 * {@link #before(TaskResult, TaskResult)} the left.
	 * </p>
	 */
	protected final boolean after(TaskResult left, TaskResult right) {
		return before(right, left);
	}

	/**
	 * Is the left {@link TaskResult} before the right one?
	 * <p>
	 * A {@link TaskResult} is "before" another, if its {@link TaskResult#getStartDate()} is
	 * strictly before the other start date and its {@link TaskResult#getEndDate()} is strictly
	 * before the other end date.
	 * </p>
	 */
	protected boolean before(TaskResult left, TaskResult right) {
		return (compareDateNullSafe(left.getStartDate(), right.getStartDate()) < 0)
			&& (compareDateNullSafe(left.getEndDate(), right.getEndDate()) < 0);
	}

	/**
	 * Converts <code>null</code> to <code>new Date(Long.MAX_VALUE)</code>.
	 * <p>
	 * This should only happen for {@link TaskResult#getEndDate()} and means the {@link Task} is not
	 * finished. Therefore, the latest possible date is a valid replacement for null here.
	 * </p>
	 */
	protected int compareDateNullSafe(Date left, Date right) {
		Date leftNonNull = left == null ? new Date(Long.MAX_VALUE) : left;
		Date rightNonNull = right == null ? new Date(Long.MAX_VALUE) : right;
		return leftNonNull.compareTo(rightNonNull);
	}

}
