/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.sym;

import com.top_logic.knowledge.search.QueryPart;

/**
 * Representation of a declaration during compiling a query.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Symbol {

	/**
	 * Returns the {@link QueryPart} for which this {@link Symbol} was created.
	 */
	QueryPart getDefinition();

	Symbol getParent();
	void initParent(TupleSymbol tupleSymbol);
	
	<R,A> R visit(SymbolVisitor<R,A> v, A arg);

}