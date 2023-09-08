/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event;

/**
 * {@link ItemEventVisitorAdapter} implementation that holds the inner
 * {@link ItemEventVisitorAdapter} as instance variable.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultItemEventVisitorAdapter<R, A> extends ItemEventVisitorAdapter<R, A> {

	/** @see #getImpl() */
	private ItemEventVisitor<? extends R, ? super A> _impl;

	/**
	 * Creates a new {@link DefaultItemEventVisitorAdapter}.
	 * 
	 * @param implementation
	 *        see {@link #getImpl()}
	 */
	public DefaultItemEventVisitorAdapter(ItemEventVisitor<? extends R, ? super A> implementation) {
		_impl = implementation;
	}

	@Override
	protected ItemEventVisitor<? extends R, ? super A> getImpl() {
		return _impl;
	}

}

