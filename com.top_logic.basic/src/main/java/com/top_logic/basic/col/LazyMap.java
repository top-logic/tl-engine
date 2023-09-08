/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Map;

/**
 * Proxy for a {@link Map} that is created lazily. This {@link Map} is
 * modifiable if and only if the created implementation is modifiable. This
 * instance is not thread safe, also if the created implementation instance is
 * (calling the factory method {@link #initInstance()} is not synchronized).
 * 
 * @see LazyListModifyable
 * @see LazySetModifyable
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class LazyMap<K, V> extends MapProxy<K, V> {

	private Map<K, V> cachedImplementation;

	@Override
	protected Map<K, V> impl() {
		Map<K, V> implementation = this.cachedImplementation;
		if (implementation == null) {
			implementation = this.cachedImplementation = this.initInstance();
			assert implementation != null : "Map initialization must not return null.";

		}
		return implementation;
	}

	/**
	 * Create the implementation list that this {@link LazyMap} is a lazy proxy
	 * for.
	 * 
	 * @return The base map to which all further accesses are redirected.
	 */
	protected abstract Map<K, V> initInstance();

}
