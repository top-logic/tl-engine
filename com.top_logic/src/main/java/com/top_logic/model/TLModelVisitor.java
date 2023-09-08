/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

/**
 * Visitor for {@link TLModelPart}s.
 * 
 * @param <R> The result type of the visit.
 * @param <A> The argument type to the visit.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TLModelVisitor<R, A> extends TLTypeVisitor<R, A>, TLTypePartVisitor<R, A> {

	/**
	 * Visit case for {@link TLModel}s.
	 */
	R visitModel(TLModel model, A arg);
	
	/**
	 * Visit case for {@link TLModule}s.
	 */
	R visitModule(TLModule model, A arg);
	
}
