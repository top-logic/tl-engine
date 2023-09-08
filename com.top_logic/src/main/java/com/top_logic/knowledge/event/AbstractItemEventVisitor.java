/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event;

/**
 * {@link AbstractItemEventVisitor} holds methods to handle not just the leaf cases of the
 * {@link ItemEvent} hierarchy but also for each element in the hierarchy.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractItemEventVisitor<R, A> implements ItemEventVisitor<R, A> {

	@Override
	public R visitCreateObject(ObjectCreation event, A arg) {
		return visitItemChange(event, arg);
	}

	@Override
	public R visitDelete(ItemDeletion event, A arg) {
		return visitItemChange(event, arg);
	}

	@Override
	public R visitUpdate(ItemUpdate event, A arg) {
		return visitItemChange(event, arg);
	}

	/**
	 * Handles {@link ItemChange}. Default dispatches to {@link #visitItemEvent(ItemEvent, Object)}
	 */
	protected R visitItemChange(ItemChange event, A arg) {
		return visitItemEvent(event, arg);
	}

	/**
	 * Handles {@link ItemEvent}. Default returns <code>null</code>
	 * 
	 * @param event
	 *        The event to process
	 * @param arg
	 *        the argument for the visit
	 * 
	 * @return the return value of the visit
	 */
	protected R visitItemEvent(ItemEvent event, A arg) {
		return null;
	}

}

