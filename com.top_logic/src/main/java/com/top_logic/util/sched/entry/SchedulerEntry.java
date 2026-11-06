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
 * <h2>Attaching and detaching</h2>
 * <p>
 * The {@link Scheduler} attaches the {@link Task} of a top-level entry in two steps:
 * {@link #beginAttach()} moves the entry from {@link Attachment#DETACHED} to
 * {@link Attachment#ATTACHING}, {@link #completeAttach(Scheduler)} calls
 * {@link Task#attachTo(Scheduler)} and moves the entry to {@link Attachment#ATTACHED}. A
 * {@link #remove()} marks the entry as removed and detaches the {@link Task}, if it is attached.
 * {@link #completeAttach(Scheduler)} and {@link #remove()} exclude each other: A {@link #remove()}
 * during {@link Task#attachTo(Scheduler)} waits for it to complete and detaches the {@link Task}
 * afterwards, a {@link #completeAttach(Scheduler)} after a {@link #remove()} does not attach the
 * {@link Task}.
 * </p>
 * 
 * <h2>Lock order</h2>
 * <p>
 * {@link Task#attachTo(Scheduler)} and {@link Task#detachFromScheduler()} run under a lock of the
 * entry that is used for nothing else, not under the monitor of the entry: They can commit a
 * transaction and call the {@link Scheduler}, whereas the {@link Scheduler} reads the state of its
 * entries while holding its own monitor. The lock order is: attach lock of the entry, then the
 * monitor of the {@link Scheduler}, then the monitor of the entry. The attach lock must therefore
 * never be acquired while holding the monitor of the {@link Scheduler} or of an entry.
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

	/**
	 * Whether the {@link Task} is attached to the {@link Scheduler}.
	 * 
	 * @see SchedulerEntry#getAttachment()
	 */
	public enum Attachment {

		/**
		 * The {@link Task} is not attached to the {@link Scheduler}.
		 */
		DETACHED,

		/**
		 * The {@link Scheduler} attaches the {@link Task}, attaching has not completed yet.
		 * 
		 * @see Task#attachTo(Scheduler)
		 */
		ATTACHING,

		/**
		 * The {@link Task} is attached to the {@link Scheduler}.
		 */
		ATTACHED;

	}

	private final Task _task;

	private final boolean _isChild;

	private final boolean _configuredActive;

	private ScheduledThread _thread;

	private boolean _runEnded;

	private State _state = State.WAITING;

	private Throwable _reasonForBroken;

	private boolean _isOvertimeIgnored = false;

	private Attachment _attachment = Attachment.DETACHED;

	private boolean _removed;

	/**
	 * Lock under which {@link Task#attachTo(Scheduler)} and {@link Task#detachFromScheduler()} run.
	 * 
	 * @see SchedulerEntry Lock order
	 */
	private final Object _attachLock = new Object();

	/**
	 * Creates an {@link SchedulerEntry}.
	 * 
	 * @param task
	 *        See {@link #getTask()}.
	 * @param isChild
	 *        See {@link #isChild()}.
	 * @param configuredActive
	 *        See {@link #isConfiguredActive()}.
	 */
	public SchedulerEntry(Task task, boolean isChild, boolean configuredActive) {
		if (task == null) {
			throw new NullPointerException("Task is not allowed to be null.");
		}
		_task = task;
		_isChild = isChild;
		_configuredActive = configuredActive;
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
	 * Whether the {@link Task} is active by configuration, i.e. as long as no administrator
	 * overrides it.
	 * 
	 * @see Scheduler#isConfiguredActive(Task)
	 */
	public boolean isConfiguredActive() {
		return _configuredActive;
	}

	/**
	 * Create the {@link ScheduledThread} in which this will be executed.
	 * 
	 * @param runEndListener
	 *        Called in the created thread as its last action, after the run of the {@link Task}
	 *        has ended, normally or with an exception. At that time, {@link #isRunEnded()} is
	 *        <code>true</code>.
	 * 
	 * @throws IllegalStateException
	 *         If there is already a {@link ScheduledThread}.
	 */
	public synchronized ScheduledThread createThread(Runnable runEndListener) {
		if (_thread != null) {
			throw new IllegalStateException("Thread has already been built. " + toString());
		}
		_runEnded = false;
		_thread = new ScheduledThread(getTask()) {
			@Override
			public void run() {
				try {
					super.run();
				} finally {
					markRunEnded();
					runEndListener.run();
				}
			}
		};
		return _thread;
	}

	private synchronized void markRunEnded() {
		_runEnded = true;
	}

	/**
	 * Whether the run of the {@link Task} in the {@link #getThread() thread} has ended.
	 * 
	 * <p>
	 * The thread may still be alive, while it finishes its last actions.
	 * </p>
	 */
	public synchronized boolean isRunEnded() {
		return _runEnded;
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
	 * <p>
	 * Must only be called if there is a {@link #getThread() thread}. Otherwise, an error is
	 * logged, as this indicates an inconsistent state of the scheduler. No exception is thrown.
	 * </p>
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
			.add("configuredActive", isConfiguredActive())
			.add("state", getState())
			.add("isOvertimeIgnored", isOvertimeIgnored())
			.add("thread", getThread())
			.add("broken", getReasonForBroken())
			.add("attachment", getAttachment())
			.add("removed", isRemoved())
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
	 * Whether the {@link Task} is attached to the {@link Scheduler}.
	 * <p>
	 * Is {@link Attachment#DETACHED} for {@link #isChild() children}, as attaching is only relevant
	 * for top-level {@link Task}s.
	 * </p>
	 */
	public synchronized Attachment getAttachment() {
		return _attachment;
	}

	private synchronized void setAttachment(Attachment attachment) {
		_attachment = attachment;
	}

	/**
	 * Is the {@link Task} attached to the {@link Scheduler}?
	 * 
	 * @see #getAttachment()
	 */
	public boolean isAttached() {
		return getAttachment() == Attachment.ATTACHED;
	}

	/**
	 * Whether this entry has been {@link #remove() removed} from the {@link Scheduler}.
	 */
	public synchronized boolean isRemoved() {
		return _removed;
	}

	/**
	 * Starts attaching the {@link Task}: Moves this entry from {@link Attachment#DETACHED} to
	 * {@link Attachment#ATTACHING}.
	 * 
	 * <p>
	 * Must be followed by {@link #completeAttach(Scheduler)}, if it returns <code>true</code>.
	 * </p>
	 * 
	 * @return Whether the {@link Task} has to be attached. <code>false</code>, if this entry is
	 *         not {@link Attachment#DETACHED} or is {@link #isRemoved() removed}.
	 */
	public synchronized boolean beginAttach() {
		if (_removed || _attachment != Attachment.DETACHED) {
			return false;
		}
		_attachment = Attachment.ATTACHING;
		return true;
	}

	/**
	 * Attaches the {@link Task} to the given {@link Scheduler} after {@link #beginAttach()}.
	 * 
	 * <p>
	 * Does not attach the {@link Task}, if this entry has been {@link #remove() removed} in
	 * between. If {@link Task#attachTo(Scheduler)} fails, this entry is
	 * {@link Attachment#DETACHED} again.
	 * </p>
	 * 
	 * <p>
	 * Must not be called while holding the monitor of the {@link Scheduler} or of an entry, see
	 * {@link SchedulerEntry Lock order}.
	 * </p>
	 */
	public void completeAttach(Scheduler scheduler) {
		synchronized (_attachLock) {
			if (getAttachment() != Attachment.ATTACHING) {
				throw new IllegalStateException("Attaching has not been begun. " + toString());
			}
			if (isRemoved()) {
				setAttachment(Attachment.DETACHED);
				return;
			}
			boolean attached = false;
			try {
				_task.attachTo(scheduler);
				attached = true;
			} finally {
				setAttachment(attached ? Attachment.ATTACHED : Attachment.DETACHED);
			}
		}
	}

	/**
	 * Marks this entry as {@link #isRemoved() removed} and detaches the {@link Task}, if it is
	 * {@link Attachment#ATTACHED}.
	 * 
	 * <p>
	 * Waits for a {@link #completeAttach(Scheduler)} in progress. A
	 * {@link #completeAttach(Scheduler)} that has not started yet does not attach the {@link Task}.
	 * </p>
	 * 
	 * <p>
	 * Must not be called while holding the monitor of the {@link Scheduler} or of an entry, see
	 * {@link SchedulerEntry Lock order}.
	 * </p>
	 */
	public void remove() {
		synchronized (_attachLock) {
			synchronized (this) {
				_removed = true;
			}
			if (getAttachment() == Attachment.ATTACHED) {
				try {
					_task.detachFromScheduler();
				} finally {
					setAttachment(Attachment.DETACHED);
				}
			}
		}
	}

}
