/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.locking.handler;

import com.top_logic.base.locking.Lock;
import com.top_logic.util.error.TopLogicException;

/**
 * Dummy {@link LockHandler} that does not request tokens.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NoTokenHandling implements LockHandler {

	/**
	 * Singleton {@link NoTokenHandling} instance.
	 */
	public static final NoTokenHandling INSTANCE = new NoTokenHandling();

	private NoTokenHandling() {
		// Singleton constructor.
	}

	@Override
	public boolean hasLock() {
		return false;
	}

	@Override
	public Lock getLock() {
		return null;
	}

	@Override
	public void acquireLock(Object contextBase) throws IllegalStateException, TopLogicException {
		// Ignore.
	}

	@Override
	public void releaseLock() {
		// Ignore.
	}

	@Override
	public void updateLock() {
		// Ignore.
	}

}
