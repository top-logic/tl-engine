/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.diff.filediff;

/**
 * Read-only queue interface.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface InputQueue<T> {
	
	/**
	 * Whether this queue has no more elements.
	 */
	boolean isEmpty();
	
	/**
	 * The next element without removing it.
	 *
	 * <p>
	 * Must not be called on an {@link #isEmpty() empty} queue.
	 * </p>
	 */
	T peek();
	
	/**
	 * Removes and returns the next element.
	 *
	 * <p>
	 * Must not be called on an {@link #isEmpty() empty} queue.
	 * </p>
	 */
	T pop();

	/**
	 * The next element without removing it, {@code null} if this queue is empty.
	 */
	T peekOrNull();

}
