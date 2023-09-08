/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Comparator;

/**
 * {@link Comparator} that identifies all given objects (returns always
 * <code>0</code>.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class Equality implements Comparator<Object> {
	
	/**
	 * Singleton instance of {@link Equality}.
	 */
	public static final Comparator<Object> INSTANCE = new Equality();
	
	/**
	 * Returns the singleton instance of {@link Equality}.
	 * 
	 * @param <T> the type to compare
	 */
	@SuppressWarnings("unchecked")
	public static <T> Comparator<T> getInstance(){
		return (Comparator<T>) INSTANCE;
	}

	private Equality() {
		// Singleton constructor.
	}
	
	@Override
	public int compare(Object o1, Object o2) {
		return 0;
	}
}