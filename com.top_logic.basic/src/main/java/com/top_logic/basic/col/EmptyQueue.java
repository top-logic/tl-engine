/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.AbstractQueue;
import java.util.Collections;
import java.util.Iterator;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.annotation.FrameworkInternal;

/**
 * Always empty queue.
 * 
 * @see CollectionUtil#emptyQueue() Use through type-safe utility method.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@FrameworkInternal
public class EmptyQueue<E> extends AbstractQueue<E> {

	/**
	 * Raw type singleton instance.
	 * 
	 * @see CollectionUtil#emptyQueue()
	 */
	@SuppressWarnings("rawtypes")
	@FrameworkInternal
	public static EmptyQueue INSTANCE = new EmptyQueue<>();

	/**
	 * @see java.util.Queue#offer(java.lang.Object)
	 */
	@Override
	public boolean offer(E e) {
		return false;
	}

	/**
	 * @see java.util.Queue#poll()
	 */
	@Override
	public E poll() {
		// Queue is empty
		return null;
	}

	/**
	 * @see java.util.Queue#peek()
	 */
	@Override
	public E peek() {
		// Queue is empty
		return null;
	}

	/**
	 * @see java.util.AbstractCollection#iterator()
	 */
	@Override
	public Iterator<E> iterator() {
		return Collections.emptyIterator();
	}

	/**
	 * @see java.util.AbstractCollection#size()
	 */
	@Override
	public int size() {
		return 0;
	}

}

