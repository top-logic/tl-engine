/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

/**
 * Visitor for {@link TLStructuredTypePart}s
 * 
 * @param <R>
 *        The result type of the visit.
 * @param <A>
 *        The argument type to the visit.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TLStructuredTypePartVisitor<R, A> extends TLClassPartVisitor<R, A>, TLAssociationPartVisitor<R, A> {

	// Pure sum interface.

}
