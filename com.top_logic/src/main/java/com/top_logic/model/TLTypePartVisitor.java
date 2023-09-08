/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

/**
 * Visitor for {@link TLTypePart}s
 * 
 * @param <R>
 *        The result type of the visit.
 * @param <A>
 *        The argument type to the visit.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TLTypePartVisitor<R, A> extends TLStructuredTypePartVisitor<R, A> {

	/**
	 * Visit case for {@link TLClassifier}s.
	 */
	R visitClassifier(TLClassifier model, A arg);

	/**
	 * Common visit case for {@link TLClassProperty}s and {@link TLAssociationProperty}s.
	 * 
	 * @see TLClassPartVisitor#visitProperty(TLProperty, Object)
	 * @see TLAssociationPartVisitor#visitProperty(TLProperty, Object)
	 */
	@Override
	R visitProperty(TLProperty model, A arg);

}
