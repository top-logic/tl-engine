/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.attr.storage;

import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.attr.MOAttributeImpl;

/**
 * {@link DBAttributeStorageImpl} for {@link MOAttributeImpl}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MOAttributeStorageImpl extends DBAttributeStorageImpl {

	/** Singleton {@link MOAttributeStorageImpl} instance. */
	public static final MOAttributeStorageImpl INSTANCE = new MOAttributeStorageImpl();

	private MOAttributeStorageImpl() {
		// singleton instance
	}

	@Override
	protected Object fromCacheToDBValue(MOAttribute attribute, Object cacheValue) {
		return cacheValue;
	}

	@Override
	protected Object fromDBToCacheValue(MOAttribute attribute, Object dbValue) {
		return dbValue;
	}


}
