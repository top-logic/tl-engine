/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Iterator;

/**
 * {@link CloseableIteratorAdapter} that closes nothing in {@link #internalClose()}.
 * 
 * <p>
 * Designed as adapter for a {@link CloseableIterator} API, but only an ordinary {@link Iterator} is
 * present.
 * </p>
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SimpleCloseableIteratorAdapter<T> extends CloseableIteratorAdapter<T> {

	/**
	 * Creates a new {@link SimpleCloseableIteratorAdapter}.
	 * 
	 * @param base
	 *        see {@link CloseableIteratorAdapter#CloseableIteratorAdapter(Iterator)}
	 */
	public SimpleCloseableIteratorAdapter(Iterator<? extends T> base) {
		super(base);
	}

	@Override
	protected void internalClose() {
		// nothing to do here
	}

}

