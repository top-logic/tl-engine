/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

import com.top_logic.basic.config.annotation.Container;

/**
 * Contents of {@link TLClass}es.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TLClassPart extends TLStructuredTypePart {

	@Override
	@Container
	TLClass getOwner();

	/**
	 * Visits this {@link TLClassPart} with the given {@link TLClassPartVisitor}.
	 * 
	 * @param <R>
	 *        The result type.
	 * @param <A>
	 *        The argument type.
	 * @param v
	 *        The visitor.
	 * @param arg
	 *        The argument to the visit.
	 * @return The result of the visit.
	 */
	<R, A> R visitClassPart(TLClassPartVisitor<R, A> v, A arg);

	@Override
	default <R, A> R visitStructuredTypePart(TLStructuredTypePartVisitor<R, A> v, A arg) {
		return visitClassPart(v, arg);
	}

}
