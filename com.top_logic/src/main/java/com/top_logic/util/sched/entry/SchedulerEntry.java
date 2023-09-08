/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.entry;

import com.top_logic.basic.Logger;
import com.top_logic.basic.sched.ScheduledThread;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.composite.CompositeTask;

/**
 * An representative for the {@link Task} in the Scheduler.
 * <p>
 * It stores information about a {@link Task} that are only relevant for the {@link Scheduler}, but
 * not for the {@link Task} itself.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class SchedulerEntry {

	/**
	 * Whether the {@link Task} is waiting or running.
	 */
	public enum State {

		/**
		 * The {@link Task} is waiting to be run.
		 */
		WAITING,

		/**
		 * The {@link Task} is running.
		 */
		RUNNING,

		/**
		 * The {@link Task} is being prepared for running.
		 */
		STARTING,

		/**
		 * The {@link Task} does not want to be run again.
		 */
		DONE,

		/**
		 * The {@link Task} is waiting for the maintenance mode.
		 */
		WAITING_FOR_MAINTENANCE_MODE;

	}

	private final Task _task;

	private final boolean _isChild;

	private boolean _enabled = false;

	private ScheduledThread _thread;

	private State _state = State.WAITING;

	private Throwable _reasonForBroken;

	private boolean _isOvertimeIgnored = false;

	private boolean _isAttached = false;

	/**
	 * Creates an {@link SchedulerEntry}.
	 */
	public SchedulerEntry(Task task, boolean isChild) {
		if (task == null) {
			throw new NullPointerException("Task is not allowed to be null.");
		}
		_task = task;
		_isChild = isChild;
	}

	/**
	 * The {@link Task} represented by this object.
	 */
	public Task getTask() {
		return _task;
	}

	/**
	 * Is this {@link Task} a child of a {@link CompositeTask}?
	 */
	public boolean isChild() {
		return _isChild;
	}

	/**
	 * @see Scheduler#isEnabled(Task)
	 */
	public synchronized void setEnabled(boolean enabled) {
		_enabled = enabled;
	}

	/**
	 * @see Scheduler#isEnabled(Task)
	 */
	public synchronized boolean isEnabled() {
		return _enabled;
	}

	/**
	 * Create the {@link ScheduledThread} in which this will be executed.
	 * 
	 * @throws IllegalStateException
	 *         If there is already a {@link ScheduledThread}.
	 */
	public synchronized ScheduledThread createThread() {
		if (_thread != null) {
			throw new IllegalStateException("Thread has already been built. " + toString());
		}
		_thread = new ScheduledThread(getTask());
		return _thread;
	}

	/**
	 * Get the {@link Thread} in which this is running.
	 * 
	 * @return null, if this is not running.
	 */
	public synchronized ScheduledThread getThread() {
		return _thread;
	}

	/**
	 * Removes the {@link ScheduledThread}.
	 * 
	 * @throws IllegalStateException
	 *         If there is no {@link ScheduledThread}.
	 */
	public synchronized void removeThread() {
		if (_thread == null) {
			String message = "There is no thread to remove. " + toString();
			Logger.error(message, new IllegalStateException(message), getClass());
		}
		_thread = null;
	}

	@Override
	public String toString() {
		return new NameBuilder(this)
			.add("task", getTask())
			.add("isChild", isChild())
			.add("enabled", isEnabled())
			.add("state", getState())
			.add("isOvertimeIgnored", isOvertimeIgnored())
			.add("thread", getThread())
			.add("broken", getReasonForBroken())
			.buildName();
	}

	/**
	 * @param state
	 *        Is not allowed to be null.
	 * 
	 * @see #getState()
	 */
	public synchronized void setState(State state) {
		_state = state;
	}

	/**
	 * Is the {@link Task} waiting or running?
	 */
	public synchronized State getState() {
		return _state;
	}

	/**
	 * The {@link Task} cannot be scheduled as it is somehow broken.
	 * <p>
	 * Logs an error with the given reason.
	 * </p>
	 * <p>
	 * If it is called multiple times, at least the first reason is stored. What happens to the
	 * other reasons is unspecified.
	 * </p>
	 * 
	 * @param reason
	 *        Has to explain what is wrong and why the {@link Task} cannot be scheduled. Is not
	 *        allowed to be null.
	 * 
	 * @see #isBroken()
	 */
	public synchronized void markAsBroken(Throwable reason) {
		if (reason == null) {
			throw new NullPointerException("If a Task is broken, there has to be a reason. " + toString());
		}
		Logger.error("Task is broken. " + toString(), reason, getClass());
		if (_reasonForBroken == null) {
			_reasonForBroken = reason;
		}
	}

	/**
	 * Reverts {@link #markAsBroken(Throwable)}.
	 * <p>
	 * After this call succeeded, {@link #isBroken()} will return false, even if it has been called
	 * multiple times. If {@link #markAsBroken(Throwable)} is called again, {@link #isBroken()} will
	 * return true again.
	 * </p>
	 */
	public synchronized void markAsFixed() {
		Logger.info("Task is marked as fixed. " + toString(), getClass());
		_reasonForBroken = null;
	}

	/**
	 * The {@link Task} cannot be scheduled as it is somehow broken.
	 */
	public synchronized boolean isBroken() {
		return _reasonForBroken != null;
	}

	/**
	 * The reason, why the {@link Task} is broken.
	 * 
	 * @return null, if the {@link Task} is not broken.
	 */
	public synchronized Throwable getReasonForBroken() {
		return _reasonForBroken;
	}

	/**
	 * @see #isOvertimeIgnored()
	 */
	public synchronized void setOvertimeIgnored(boolean isOvertimeIgnored) {
		_isOvertimeIgnored = isOvertimeIgnored;
	}

	/**
	 * Should the {@link Scheduler} ignore if the {@link Task} is over the time limit?
	 * <p>
	 * If not, it logs a message. This flag is used to prevent flooding the log with those messages.
	 * </p>
	 */
	public synchronized boolean isOvertimeIgnored() {
		return _isOvertimeIgnored;
	}

	/**
	 * @see #isAttached()
	 */
	public void setAttached(boolean isAttached) {
		_isAttached = isAttached;
	}

	/**
	 * Is the {@link Task} attached to the {@link Scheduler}?
	 * <p>
	 * Is never set for {@link #isChild() children}, as it is only relevant for top-level
	 * {@link Task}s.
	 * </p>
	 */
	public synchronized boolean isAttached() {
		return _isAttached;
	}

}
