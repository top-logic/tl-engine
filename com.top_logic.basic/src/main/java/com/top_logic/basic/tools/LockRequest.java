/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.tools;

import com.top_logic.basic.Logger;
import com.top_logic.basic.util.StopWatch;

/**
 * Represents a pending request for some kind of lock.
 * Is used by the {@link LockWaitMonitor}.
 * 
 * @author     <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class LockRequest {
	
	private final Thread thread = Thread.currentThread();
	private final String lockName;
	private final long timestamp;
	private final int timeout;
	private final int warnTimeout;
	private boolean wasWaiting = false;
	
	LockRequest(String lockName, int timeout, int warnTimeout) {
		this.lockName = lockName;
		this.timeout = timeout;
		this.warnTimeout = warnTimeout;
		this.timestamp = System.currentTimeMillis();
		Logger.debug(getThreadDescription() + " is requesting a lock: " + lockName, LockWaitMonitor.class);
	}
	
	/**
	 * The thread requesting the lock.
	 */
	public Thread getThread() {
		return thread;
	}
	
	/**
	 * The name of the requested lock.
	 */
	public String getLockName() {
		return lockName;
	}
	
	/**
	 * The time when the lock was requested.
	 */
	public long getTimestamp() {
		return timestamp;
	}
	
	/**
	 * Marks that the requesting thread had to wait for the lock.
	 */
	public void isWaiting() {
		wasWaiting = true;
	}
	
	/**
	 * Whether the requesting thread had to wait for the lock.
	 */
	public boolean wasWaiting() {
		return wasWaiting;
	}
	
	/**
	 * Whether the request has been waiting longer than its timeout.
	 */
	public boolean hasTimedOut() {
		return System.currentTimeMillis() > (timestamp + timeout);
	}
	
	/**
	 * In milliseconds.
	 */
	public long getTimeWaiting() {
		return System.currentTimeMillis() - getTimestamp();
	}
	
	/**
	 * The {@link #getTimeWaiting()} as text.
	 */
	public String getTimeWaitingText() {
		return StopWatch.toStringMillis(getTimeWaiting());
	}
	
	/**
	 * Has the {@link Thread} been waiting so long, that a warning should be logged?
	 */
	public boolean hasWarnTimedOut() {
		return getTimeWaiting() > warnTimeout;
	}
	
	/**
	 * Description of the requesting thread for log messages.
	 */
	public String getThreadDescription() {
		return "Thread '" + thread.getName() + "' (id: " + thread.getId() + ")";
	}
	
	@Override
	public String toString() {
		return getThreadDescription() + " is waiting " + getTimeWaitingText() + " for lock: " + getLockName();
	}

}