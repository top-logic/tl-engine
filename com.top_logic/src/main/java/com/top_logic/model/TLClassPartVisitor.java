/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

/**
 * Visitor for {@link TLClassPart}s
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
public interface TLClassPartVisitor<R, A> {

	/**
	 * Visit case for {@link TLProperty}s.
	 */
	R visitProperty(TLProperty model, A arg);

	/**
	 * Visit case for {@link TLReference}s.
	 */
	R visitReference(TLReference model, A arg);

	/**
	 * Visit case for {@link TLClassProperty}s.
	 */
	default R visitClassProperty(TLClassProperty model, A arg) {
		return visitProperty(model, arg);
	}

}

