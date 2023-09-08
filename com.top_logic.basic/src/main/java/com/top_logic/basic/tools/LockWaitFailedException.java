/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.tools;




/**
 * This {@link RuntimeException} is thrown when a {@link LockRequest}
 * {@link LockRequest#hasTimedOut() times out} and {@link LockWaitMonitor#requestStillWaiting()} is called.
 * 
 * @author     <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class LockWaitFailedException extends RuntimeException {
	
	private final LockRequest lockRequest;
	
	public LockWaitFailedException(LockRequest lockRequest) {
		super("A lock request timed out: " + lockRequest);
		this.lockRequest = lockRequest;
	}
	
	public LockRequest getLockRequest() {
		return lockRequest;
	}
	
}
