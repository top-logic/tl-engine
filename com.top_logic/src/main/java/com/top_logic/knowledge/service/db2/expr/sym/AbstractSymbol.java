/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.sym;

import com.top_logic.knowledge.search.QueryPart;

/**
 * Base class for {@link Symbol}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractSymbol implements Symbol {

	private final QueryPart definition;
	
	public AbstractSymbol(QueryPart definition) {
		this.definition = definition;
	}
	
	@Override
	public QueryPart getDefinition() {
		return definition;
	}

	public static Symbol getRootSymbol(Symbol symbol) {
		Symbol rootSym = symbol;
		while (rootSym.getParent() != null) {
			rootSym = rootSym.getParent();
		}
		return rootSym;
	}

}