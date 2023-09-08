/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Unsynchronized stack implementation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ArrayStack<T> extends ArrayList<T> implements Stack<T> {

	/**
	 * Creates an empty {@link ArrayStack}.
	 */
	public ArrayStack() {
		super();
	}

	/**
	 * Creates a {@link ArrayStack} with the initial elements from the given {@link Collection}.
	 */
	public ArrayStack(Collection<? extends T> c) {
		super(c);
	}

	/**
	 * Creates a {@link ArrayStack} with the given initial capacity.
	 */
	public ArrayStack(int initialCapacity) {
		super(initialCapacity);
	}
	
	@Override
	public void push(T obj) {
		add(obj);
	}
	
	@Override
	public T pop() {
		return remove(last());
	}
	
	@Override
	public T peek() {
		return get(last());
	}
	
	@Override
	public T peek(int index) {
		return get(last() - index);
	}

	private int last() {
		return size() - 1;
	}

}
