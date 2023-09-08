/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

/**
 * Visitor for {@link TLType}s.
 * 
 * @param <R> The result type of the visit.
 * @param <A> The argument type to the visit.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TLTypeVisitor<R, A> {

	/**
	 * Visit case for {@link TLPrimitive}s.
	 */
	R visitPrimitive(TLPrimitive model, A arg);
	
	/**
	 * Visit case for {@link TLEnumeration}s.
	 */
	R visitEnumeration(TLEnumeration model, A arg);

	/**
	 * Visit case for {@link TLClass}es.
	 */
	R visitClass(TLClass model, A arg);

	/**
	 * Visit case for {@link TLAssociation}s.
	 */
	R visitAssociation(TLAssociation model, A arg);
	
}
