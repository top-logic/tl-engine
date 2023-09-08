/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

/**
 * The abstract data type stack.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Stack<T> {

	/**
	 * Pushes the given element on the stack.
	 */
	void push(T obj);

	/**
	 * Removes and returns the topmost element from the stack.
	 */
	T pop();

	/**
	 * Returns the topmost element on the stack and keeps it on the stack.
	 */
	T peek();

	/**
	 * The element at the given index.
	 * 
	 * <p>
	 * The element at index <code>0</code> is the topmost element, see {@link #peek()}. The element
	 * pushed first on this {@link Stack} is at index <code>{@link #size()} - 1</code>.
	 * </p>
	 */
	T peek(int index);

	/**
	 * Removes all elements from this {@link Stack}.
	 */
	void clear();

	/**
	 * Whether this {@link Stack} contains no elements.
	 */
	boolean isEmpty();
	
	/**
	 * The number of elements on this {@link Stack}.
	 */
	int size();
	
}