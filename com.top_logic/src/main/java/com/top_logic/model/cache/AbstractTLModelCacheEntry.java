/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.cache;

/**
 * An entry of the {@link AbstractTLModelCache}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface AbstractTLModelCacheEntry<I extends AbstractTLModelCacheEntry<I>> {

	/**
	 * Creates a cache value with the same internal state as this one.
	 * <p>
	 * Only immutable objects must be shared between this object and the copy.
	 * </p>
	 * 
	 * @return Never null.
	 */
	I copy();

	/**
	 * Removes all data.
	 * <p>
	 * The cache entry has to be in the same state as it would be directly after construction from
	 * the default constructor.
	 * </p>
	 */
	void clear();

}
