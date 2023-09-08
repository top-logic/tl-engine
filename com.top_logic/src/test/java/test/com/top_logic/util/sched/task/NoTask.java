/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util.sched.task;

import java.util.Calendar;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.Maybe;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.log.TaskLog;
import com.top_logic.util.sched.task.schedule.SchedulingAlgorithm;

/**
 * A {@link Task} implementation for tests.
 * <p>
 * Throws an {@link UnreachableAssertion} on every access. This class can be used if some API
 * requires a {@link Task} parameter, but in the concrete test situation it is never used, and if it
 * is accessed, that should fail as it is wrong. Therefore the {@link UnreachableAssertion}: Using
 * this class is an assertion that the {@link Task} is never accessed.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class NoTask implements Task {

	/** Singleton instance of the {@link NoTask}. */
	public static final NoTask INSTANCE = new NoTask();

	private NoTask() {
		// Reduce visibility
	}

	@Override
	public void started(long when) {
		throw fail();
	}

	@Override
	public String getName() {
		throw fail();
	}

	@Override
	public long getStarted() {
		throw fail();
	}

	@Override
	public void run() {
		throw fail();
	}

	@Override
	public boolean shouldStop() {
		throw fail();
	}

	@Override
	public SchedulingAlgorithm getSchedulingAlgorithm() {
		throw fail();
	}

	@Override
	public long calcNextShed(long notBefore) {
		throw fail();
	}

	@Override
	public long getNextShed() {
		throw fail();
	}

	@Override
	public Maybe<Calendar> getNextSchedule() {
		throw fail();
	}

	@Override
	public boolean markAsRun(long start) {
		throw fail();
	}

	@Override
	public long getLastSchedule() {
		return SchedulingAlgorithm.NO_SCHEDULE;
	}

	@Override
	public void forceRun(long start) {
		throw fail();
	}

	@Override
	public boolean isForcedRun() {
		throw fail();
	}

	@Override
	public boolean isNodeLocal() {
		throw fail();
	}

	@Override
	public boolean needsMaintenanceMode() {
		throw fail();
	}

	@Override
	public long getMaintenanceModeDelay() {
		throw fail();
	}

	@Override
	public boolean isMaintenanceModeSafe() {
		throw fail();
	}

	@Override
	public boolean isRunOnStartup() {
		throw fail();
	}

	@Override
	public boolean isPersistent() {
		throw fail();
	}

	@Override
	public TaskLog getLog() {
		throw fail();
	}

	@Override
	public void attachTo(Scheduler scheduler) {
		throw fail();
	}

	@Override
	public void detachFromScheduler() {
		throw fail();
	}

	@Override
	public void logGuiWarning(String message) {
		throw fail();
	}

	@Override
	public boolean signalStop() {
		throw fail();
	}

	@Override
	public void signalShutdown() {
		throw fail();
	}

	@Override
	public boolean isBlockingAllowed() {
		throw fail();
	}

	@Override
	public boolean isBlockedByDefault() {
		throw fail();
	}

	private UnreachableAssertion fail() {
		return new UnreachableAssertion("Unexpected Task usage.");
	}

}
