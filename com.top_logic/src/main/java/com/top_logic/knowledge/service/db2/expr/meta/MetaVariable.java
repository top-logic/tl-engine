/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.meta;

import com.top_logic.knowledge.search.QueryPart;

/**
 * Variable representing a {@link QueryPart}
 * 
 * @author <a href=mailto:bhu@top-logic.com>bhu</a>
 */
public interface MetaVariable<T extends QueryPart> {

	/**
	 * the name of the variable.
	 */
	String getName();

	/**
	 * Binds this variable to the given {@link QueryPart}.
	 * 
	 * @param expr
	 *        the part to bind this variable
	 */
	void bind(T expr);
	
	/**
	 * the bounded {@link QueryPart}
	 */
	T getBinding();
	
}
