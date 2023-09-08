/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.keystorages;

import com.top_logic.basic.KeyStorage;

/**
 * Checks the keys and values of the {@link KeyStorage}s for problems.
 * <p>
 * Every {@link KeyStorageChecker} is called for each key-value-pair, even if another
 * {@link KeyStorageChecker} already found a problem. (To prevent repeated reboots after fixes until
 * all problems have been reported and fixed.)
 * </p>
 * <p>
 * If a {@link KeyStorageChecker} cannot check a key or value, it has to ignore it and return null,
 * as all {@link KeyStorageChecker}s are called for each value.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface KeyStorageChecker {

	/**
	 * Check whether the given key is valid.
	 * <p>
	 * Does not log the problem or throw it as Exception. Only returns it.
	 * </p>
	 * 
	 * @param key
	 *        The key to check. Is not allowed to be null.
	 * @param value
	 *        The value to check. Is not allowed to be null.
	 * @return Null if no problem is found. Either because the key is valid, or this
	 *         {@link KeyStorageChecker} is not responsible. If a problem with the key is detected,
	 *         a {@link RuntimeException} describing the problem is returned.
	 */
	Exception check(String key, String value);

}
