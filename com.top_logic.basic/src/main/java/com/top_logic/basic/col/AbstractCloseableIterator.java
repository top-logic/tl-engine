/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

/**
 * Base class for {@link CloseableIterator} implementations.
 * 
 * <p>
 * This implementation ensures the idempotent behavior of the {@link #close()} method.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractCloseableIterator<T> implements CloseableIterator<T> {
	
	private boolean closed = false;

	/**
	 * Whether {@link #close()} has already been called on this instance.
	 */
	protected final boolean isClosed() {
		return closed;
	}

	@Override
	public final void close() {
		if (closed) {
			return;
		}
		
		closed = true;
		internalClose();
	}

	protected abstract void internalClose();

}
