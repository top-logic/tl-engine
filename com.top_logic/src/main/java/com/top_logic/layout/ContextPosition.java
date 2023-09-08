/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;


/**
 * Description of a position in a list relative to a list element.
 * 
 * @see #START
 * @see #END
 * @see #position(PositionStrategy, Object)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ContextPosition {
	
	/**
	 * The {@link ContextPosition} at the {@link PositionStrategy#START} of the list.
	 */
	public static final ContextPosition START = new ContextPosition(PositionStrategy.START, null);
	
	/**
	 * The {@link ContextPosition} at the {@link PositionStrategy#END} of the list.
	 */
	public static final ContextPosition END = new ContextPosition(PositionStrategy.END, null);
	
	/**
	 * Creates a new {@link ContextPosition}.
	 * 
	 * @param position
	 *        See {@link #getStrategy()}.
	 * @param context
	 *        See {@link #getContext()}.
	 * @return A new {@link ContextPosition}.
	 */
	public static ContextPosition position(PositionStrategy position, Object context) {
		return new ContextPosition(position, context);
	}

	private final PositionStrategy position;
	private final Object context;
	
	private ContextPosition(PositionStrategy position, Object context) {
		this.position = position;
		this.context = context;
	}

	/**
	 * The positioning strategy.
	 */
	public PositionStrategy getStrategy() {
		return position;
	}
	
	/**
	 * The optional context for the {@link #getStrategy()}.
	 */
	public Object getContext() {
		return context;
	}
	
}
