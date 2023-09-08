/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

/**
 * Visitor for {@link TLAssociationPart}s
 * 
 * @param <R>
 *        The result type of the visit.
 * @param <A>
 *        The argument type to the visit.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TLAssociationPartVisitor<R, A> {

	/**
	 * Visit case for {@link TLProperty}s.
	 */
	R visitProperty(TLProperty model, A arg);

	/**
	 * Visit case for {@link TLAssociationEnd}s.
	 */
	R visitAssociationEnd(TLAssociationEnd model, A arg);

	/**
	 * Visit case for {@link TLAssociationProperty}s.
	 */
	default R visitAssociationProperty(TLAssociationProperty model, A arg) {
		return visitProperty(model, arg);
	}

}

