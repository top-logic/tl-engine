/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

/**
 * A {@link Provider} that returns always the same constant value.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConstantProvider<T> implements Provider<T> {

	private final T _instance;
	
	/**
	 * Creates a {@link ConstantProvider}.
	 *
	 * @param instance the value to return in {@link #get()}.
	 */
	public ConstantProvider(T instance) {
		_instance = instance;
	}

	@Override
	public T get() {
		return _instance;
	}

}
