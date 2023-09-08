/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task;

import java.util.Calendar;

import com.top_logic.base.cluster.ClusterManager;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.util.ExponentialBackoff;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.schedule.SchedulingAlgorithm;

/**
 * Methods used in different places of the {@link Task} / {@link Scheduler} framework.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TaskCommon {

	/** How often a commit should be retried. (If it is retried at all.) */
	public static final int COMMIT_RETRIES = 20;

	private static final int COMMIT_RETRY_WAIT_MIN = 100;

	private static final double COMMIT_RETRY_BACKOFF_FACTOR = 1.5;

	private static final int COMMIT_RETRY_WAIT_MAX = 1000;

	/**
	 * Creates a {@link ExponentialBackoff} with the parameters: <br/>
	 * <code>
	 * start = {@value #COMMIT_RETRY_WAIT_MIN}, <br/>
	 * backoffFactor = {@value #COMMIT_RETRY_BACKOFF_FACTOR}, <br/>
	 * max = {@value #COMMIT_RETRY_WAIT_MAX} <br/>
	 * </code>
	 */
	public static ExponentialBackoff createExponentialBackoff() {
		return new ExponentialBackoff(COMMIT_RETRY_WAIT_MIN, COMMIT_RETRY_BACKOFF_FACTOR, COMMIT_RETRY_WAIT_MAX);
	}

	/**
	 * Returns an unique identifier that is stable for a node as long as it's running, but will be
	 * reused for other nodes once the original node is shut down.
	 */
	public static Long getCurrentClusterNodeId() {
		return ClusterManager.getInstance().getNodeId();
	}

	/**
	 * Returns a name for the current cluster node.
	 */
	public static String getCurrentClusterNodeName() {
		return SystemPropertyTaskClusterNodeName.VALUE;
	}

	/**
	 * Convert the given {@link Maybe} {@link Calendar} to a 'long', with {@link Maybe#none()} being
	 * represented by {@link SchedulingAlgorithm#NO_SCHEDULE}.
	 */
	public static long lastScheduleAsLong(Maybe<Calendar> lastSchedule) {
		if (!lastSchedule.hasValue()) {
			return SchedulingAlgorithm.NO_SCHEDULE;
		}
		return lastSchedule.get().getTimeInMillis();
	}

	/**
	 * Convert the given {@link Maybe} {@link Calendar} to a 'long', with {@link Maybe#none()} being
	 * represented by {@link SchedulingAlgorithm#NO_SCHEDULE}.
	 */
	public static long nextScheduleToLong(Maybe<Calendar> nextSchedule) {
		if (!nextSchedule.hasValue()) {
			return SchedulingAlgorithm.NO_SCHEDULE;
		}
		return nextSchedule.get().getTimeInMillis();
	}

	/**
	 * Convert the given 'long' to a {@link Maybe} {@link Calendar}, with
	 * {@link SchedulingAlgorithm#NO_SCHEDULE} being represented by {@link Maybe#none()}.
	 */
	public static Maybe<Calendar> nextScheduleFromLong(long nextSchedule) {
		if (nextSchedule == SchedulingAlgorithm.NO_SCHEDULE) {
			return Maybe.none();
		}
		return Maybe.some(CalendarUtil.createCalendar(nextSchedule));
	}

}
