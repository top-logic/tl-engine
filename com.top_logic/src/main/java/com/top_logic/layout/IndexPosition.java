/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.util.List;

import com.top_logic.basic.UnreachableAssertion;


/**
 * Description of a position in a list relative to a list index.
 * 
 * @see #START
 * @see #END
 * @see #position(PositionStrategy, int)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class IndexPosition {
	
	/**
	 * The {@link IndexPosition} at the {@link PositionStrategy#START} of the list.
	 */
	public static final IndexPosition START = new IndexPosition(PositionStrategy.START, -1);
	
	/**
	 * The {@link IndexPosition} at the {@link PositionStrategy#END} of the list.
	 */
	public static final IndexPosition END = new IndexPosition(PositionStrategy.END, -1);
	
	/**
	 * The {@link IndexPosition} at the {@link PositionStrategy#AUTO} of the list.
	 * 
	 * Use with caution. May not always work as expected.
	 */
	public static final IndexPosition AUTO = new IndexPosition(PositionStrategy.AUTO, -1);

	/**
	 * Creates a new {@link IndexPosition}.
	 * 
	 * @param position
	 *        See {@link #getStrategy()}.
	 * @param index
	 *        See {@link #getIndex()}.
	 * @return A new {@link IndexPosition}.
	 */
	public static IndexPosition position(PositionStrategy position, int index) {
		return new IndexPosition(position, index);
	}

	/**
	 * Creates a new {@link IndexPosition} with {@link #getStrategy()}
	 * {@link PositionStrategy#BEFORE}
	 */
	public static IndexPosition before(int index) {
		return new IndexPosition(PositionStrategy.BEFORE, index);
	}

	/**
	 * Creates a new {@link IndexPosition} with {@link #getStrategy()}
	 * {@link PositionStrategy#AFTER}
	 */
	public static IndexPosition after(int index) {
		return new IndexPosition(PositionStrategy.AFTER, index);
	}

	private final PositionStrategy position;

	private final int index;
	
	private IndexPosition(PositionStrategy position, int index) {
		this.position = position;
		this.index = index;
	}

	/**
	 * The positioning strategy.
	 */
	public PositionStrategy getStrategy() {
		return position;
	}
	
	/**
	 * The optional context index for the {@link #getStrategy()}.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Converts this {@link IndexPosition} into an index compatible with the
	 * {@link List#add(int, Object)} API.
	 */
	public int beforeIndex(List<?> children) {
		switch (getStrategy()) {
			case START:
				return 0;
			case AUTO:
			case END:
				return children.size();
			case BEFORE:
				return getIndex();
			case AFTER:
				return getIndex() + 1;
			default:
				throw new UnreachableAssertion("No such strategy: " + getStrategy());
		}
	}
	
}
