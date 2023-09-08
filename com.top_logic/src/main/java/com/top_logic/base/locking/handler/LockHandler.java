/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.locking.handler;

import com.top_logic.base.locking.Lock;
import com.top_logic.util.error.TopLogicException;

/**
 * Algorithm for requesting, updating, renewing and releasing {@link Lock}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface LockHandler {

	/**
	 * Whether this handler has acquired a {@link Lock}.
	 * 
	 * @see #acquireLock(Object)
	 */
	boolean hasLock();

	/**
	 * The currently hold {@link Lock}, or <code>null</code>, if no {@link Lock} has been acquired.
	 */
	Lock getLock();

	/**
	 * Request a {@link Lock} for the given model.
	 * 
	 * @param model
	 *        The model to acquire a {@link Lock} for.
	 * 
	 * @throws IllegalStateException
	 *         If another {@link Lock} is already acquired.
	 * @throws TopLogicException
	 *         If the {@link Lock} cannot be acquired.
	 */
	void acquireLock(Object model) throws IllegalStateException, TopLogicException;

	/**
	 * Release the TokenContext if there is one
	 */
	void releaseLock();

	/**
	 * Makes sure that the hold {@link Lock} has not expired.
	 * 
	 * @throws TopLogicException
	 *         If the {@link Lock} has expired.
	 */
	void updateLock() throws TopLogicException;

}
