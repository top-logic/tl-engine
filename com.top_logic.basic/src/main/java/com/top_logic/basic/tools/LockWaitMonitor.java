/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.top_logic.basic.Logger;


/**
 * Monitors how long it takes to get a lock.
 * 
 * @author     <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class LockWaitMonitor {
	
	/**
	 * Singleton {@link LockWaitMonitor} instance.
	 */
	public static final LockWaitMonitor INSTANCE = new LockWaitMonitor();
	
	/**
	 * Mapping from Thread-IDs to time stamps.
	 * <br/>
	 * <b>All accesses have to be synchronized!</b>
	 */
	private final Map<Long, LockRequest> lockRequest = new HashMap<>();
	
	private LockWaitMonitor() {
		/* Private singleton constructor */
	}
	
	/**
	 * This method has to be called (directly) before the lock is requested for the first time.
	 * 
	 * @param timeoutMillis
	 *        If {@link #requestStillWaiting()} is called that many milliseconds after
	 *        {@link #requestingLock(String, int, int)} has been called,
	 *        it will not return but throw an {@link LockWaitFailedException}.
	 *        For no timeout, set {@link Integer#MAX_VALUE}.
	 *        (That will cause a timeout of >3 Weeks.)
	 * @param warnTimeout
	 *        If {@link #requestStillWaiting()} is called that many milliseconds after
	 *        {@link #requestingLock(String, int, int)} has been called,
	 *        the administrator will be warned that this is an unexpectedly long waiting time.
	 *        (Via logging a warning and/or via specialized monitors such as <code>LuceneMonitor</code> for Lucene locks.)
	 */
	public synchronized void requestingLock(String lockDescription, int timeoutMillis, int warnTimeout) {
		long threadId = Thread.currentThread().getId();
		if (lockRequest.containsKey(threadId)) {
			logWarn("My caller did not report that its last attempt to get lock ('" + lockRequest.get(threadId).getLockName() + "') already finished.");
			lockRequest.remove(threadId);
		}
		LockRequest newLockRequest = new LockRequest(lockDescription, timeoutMillis, warnTimeout);
		lockRequest.put(threadId, newLockRequest);
	}
	
	/**
	 * If the thread is periodically retrying to get the lock,
	 * this method should be called between every attempt to get the lock.
	 */
	public synchronized void requestStillWaiting() {
		long threadId = Thread.currentThread().getId();
		if ( ! lockRequest.containsKey(threadId)) {
			logWarn("My caller did not report that it was trying to get a lock.");
			lockRequest.put(threadId, new LockRequest("[???]", Integer.MAX_VALUE, Integer.MAX_VALUE));
		}
		LockRequest pendingLockRequest = lockRequest.get(threadId);
		pendingLockRequest.isWaiting();
		if (pendingLockRequest.hasWarnTimedOut()) {
			String checkLockWarning = " This is longer than usual and *might* indicate a locking problem. Please check that lock carefully. (If you *mistakenly* delete them, that part of the application will probably break!)";
			Logger.warn(pendingLockRequest.getThreadDescription() + " is after " + pendingLockRequest.getTimeWaitingText() + " still waiting for lock: " + pendingLockRequest.getLockName() + checkLockWarning, LockWaitMonitor.class);
		} else {
			Logger.info(pendingLockRequest.getThreadDescription() + " is after " + pendingLockRequest.getTimeWaitingText() + " still waiting for lock: " + pendingLockRequest.getLockName(), LockWaitMonitor.class);
		}
		if (pendingLockRequest.hasTimedOut()) {
			// Don't unregister this LockRequest, as the finally-Block of the caller does that.
			throw new LockWaitFailedException(pendingLockRequest);
		}
	}
	
	/**
	 * This method has to be called when the lock request finished either by getting the lock,
	 * giving up or when an Exception is propagating up the call stack.
	 * <br/>
	 * <b>This method has to be called in a finally-block to ensure the {@link LockWaitMonitor} is notified.</b>
	 * If it is not notified, it may mistakenly alarm the administrator that there is a lock waiting too long.
	 */
	public synchronized void requestFinished() {
		long threadId = Thread.currentThread().getId();
		if ( ! lockRequest.containsKey(threadId)) {
			logWarn("My caller did not report that it was trying to get a lock.");
		}
		LockRequest finishedLockRequest = lockRequest.get(threadId);
		if (finishedLockRequest.wasWaiting()) {
			Logger.info(finishedLockRequest.getThreadDescription() + " finished its lock request for " + finishedLockRequest.getLockName() + " after waiting " + finishedLockRequest.getTimeWaitingText(), LockWaitMonitor.class);
		} else {
			Logger.debug(finishedLockRequest.getThreadDescription() + " finished its lock request for " + finishedLockRequest.getLockName() + " within " + finishedLockRequest.getTimeWaitingText() + " without explicit waiting.", LockWaitMonitor.class);
		}
		lockRequest.remove(threadId);
	}
	
	public synchronized List<LockRequest> getPendingLockRequests() {
		return new ArrayList<>(lockRequest.values());
	}
	
	public List<LockRequest> filterPendingLockRequest(String lockNameRegExp) {
		List<LockRequest> pendingLockRequests = getPendingLockRequests();
		for (Iterator<LockRequest> lockRequestIter = pendingLockRequests.iterator(); lockRequestIter.hasNext();) {
			if ( ! contains(lockNameRegExp, lockRequestIter.next().getLockName())) {
				lockRequestIter.remove();
			}
		}
		return pendingLockRequests;
	}
	
	public List<LockRequest> findWarnTimedOutRequests(String lockNameRegExp) {
		List<LockRequest> pendingLockRequests = LockWaitMonitor.INSTANCE.filterPendingLockRequest(lockNameRegExp);
        for (Iterator<LockRequest> pendingLockRequestIter = pendingLockRequests.iterator(); pendingLockRequestIter.hasNext();) {
        	if ( ! pendingLockRequestIter.next().hasWarnTimedOut()) {
        		pendingLockRequestIter.remove();
        	}
        }
        return pendingLockRequests;
	}
	
	private boolean contains(String lockNameRegExp, String lockName) {
		return Pattern.compile(lockNameRegExp).matcher(lockName).find();
	}
    
	private static void logWarn(String message) {
		Logger.warn(message, new RuntimeException("Stacktrace holder"), LockWaitMonitor.class);
	}
	
}
