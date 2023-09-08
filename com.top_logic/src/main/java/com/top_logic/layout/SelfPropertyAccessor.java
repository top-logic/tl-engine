/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

/**
 * {@link PropertyAccessor} that returns the given target object.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SelfPropertyAccessor<T> extends ReadOnlyPropertyAccessor<T> {

	/**
	 * Singleton {@link SelfPropertyAccessor} instance.
	 */
	public static final SelfPropertyAccessor<Object> INSTANCE = new SelfPropertyAccessor<>();

	private SelfPropertyAccessor() {
		// Singleton constructor.
	}
	
	@Override
	public Object getValue(T target) {
		return target;
	}

}
