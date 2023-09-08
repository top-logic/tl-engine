/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.diff.filediff;

import java.util.List;

/**
 * {@link InputQueue} that reads a {@link List}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ListQueue<T> implements InputQueue<T> {
	private final List<T> list;

	private int index = 0;

	public ListQueue(List<T> list) {
		this.list = list;
	}

	@Override
	public boolean isEmpty() {
		return index == list.size();
	}

	@Override
	public T peek() {
		return list.get(index);
	}

	@Override
	public T pop() {
		return list.get(index++);
	}
	
	@Override
	public T peekOrNull() {
		if (isEmpty()) {
			return null;
		}
		return peek();
	}
}