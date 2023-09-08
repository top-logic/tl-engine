/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event;

/**
 * Visitor interface for {@link ItemEvent}s.
 * 
 * <p>
 * The type parameter <code>R</code> gives the result type of the visit methods, the type parameter
 * <code>A</code> gives the (single) argument type of this visitor.
 * </p>
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ItemEventVisitor<R, A> {

	/**
	 * Visit the given {@link ObjectCreation} using this visitor.
	 */
	R visitCreateObject(ObjectCreation event, A arg);

	/**
	 * Visit the given {@link ItemDeletion} using this visitor.
	 */
	R visitDelete(ItemDeletion event, A arg);

	/**
	 * Visit the given {@link ItemUpdate} using this visitor.
	 */
	R visitUpdate(ItemUpdate event, A arg);

}
