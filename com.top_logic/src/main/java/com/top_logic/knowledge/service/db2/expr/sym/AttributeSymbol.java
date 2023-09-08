/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.sym;

import com.top_logic.knowledge.search.QueryPart;

/**
 * Base class for {@link Symbol}s accessing object attributes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AttributeSymbol extends AbstractSymbol implements DataSymbol {

	public AttributeSymbol(QueryPart definition) {
		super(definition);
	}

	@Override
	public abstract ItemSymbol getParent();
	
	public abstract String getAttributeName();
	public abstract String getDataTableAlias();
	
	@Override
	public void initParent(TupleSymbol parent) {
		throw new UnsupportedOperationException("An attribute symbol gets its parent during construction.");
	}

}