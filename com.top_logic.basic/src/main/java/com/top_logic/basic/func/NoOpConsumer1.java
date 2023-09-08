/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.func;

/**
 * {@link Consumer1} that does nothing.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class NoOpConsumer1 extends Consumer1<Object> {

	private static final NoOpConsumer1 INSTANCE = new NoOpConsumer1();

	/**
	 * Getter for the {@link NoOpConsumer1} instance.
	 */
	// Cast is safe, as the unchecked argument is never used.
	@SuppressWarnings("unchecked")
	public static <T> Consumer1<T> getInstance() {
		return (Consumer1<T>) INSTANCE;
	}

	@Override
	protected void accept(Object arg) {
		// nothing
	}

}
