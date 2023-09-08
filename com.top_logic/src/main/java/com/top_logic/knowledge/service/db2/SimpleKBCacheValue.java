/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.knowledge.service.db2.AbstractKBCache.KBCacheValue;

/**
 * Simple {@link KBCacheValue} holding the global value direct.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SimpleKBCacheValue<E> extends KBCacheValue<E> {

	private final E _globalValue;

	/**
	 * Creates a new {@link SimpleKBCacheValue}.
	 */
	public SimpleKBCacheValue(long minValidity, E globalValue) {
		super(minValidity);
		_globalValue = globalValue;
	}

	@Override
	public E globalCacheValue() {
		return _globalValue;
	}

}
