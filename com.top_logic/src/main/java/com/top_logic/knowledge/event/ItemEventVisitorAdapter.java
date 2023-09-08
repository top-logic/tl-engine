/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event;

/**
 * {@link ItemEventVisitor} that delegates visit methods to a different {@link ItemEventVisitor}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ItemEventVisitorAdapter<R, A> implements ItemEventVisitor<R, A> {

	@Override
	public R visitCreateObject(ObjectCreation event, A arg) {
		return getImpl().visitCreateObject(event, arg);
	}

	@Override
	public R visitDelete(ItemDeletion event, A arg) {
		return getImpl().visitDelete(event, arg);
	}

	@Override
	public R visitUpdate(ItemUpdate event, A arg) {
		return getImpl().visitUpdate(event, arg);
	}
	
	/**
	 * Implementation to delegate visit methods to.
	 */
	protected abstract ItemEventVisitor<? extends R, ? super A> getImpl();

}

