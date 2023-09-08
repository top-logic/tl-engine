/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

/**
 * {@link Mapping} that maps all values to <code>null</code>.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NullMapping<V> implements Mapping<Object, V> {

	/**
	 * Singleton {@link NullMapping} instance.
	 */
	public static final NullMapping<Object> INSTANCE = new NullMapping<>();

	private NullMapping() {
		// Singleton constructor.
	}
	
	@Override
	public V map(Object input) {
		return null;
	}
	
	/**
	 * The {@link NullMapping} in a type safe way.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" }) // Implementation is compatible with all value types.
	public static <K, V> Mapping<K, V> getInstance() {
		return (Mapping) INSTANCE;
	}

}
