/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.sym;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.TypeSystem;
import com.top_logic.knowledge.search.QueryPart;

/**
 * Base class for {@link Symbol}s representing links or objects.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractItemSymbol extends AbstractSymbol implements ItemSymbol {
	
	protected final Map<String, FlexAttributeSymbol> flexAttributeSymbols = new HashMap<>();
	
	public AbstractItemSymbol(QueryPart definition) {
		super(definition);
	}
	
	public final Collection<FlexAttributeSymbol> getFlexAttributeSymbols() {
		return flexAttributeSymbols.values();
	}
	
	@Override
	public FlexAttributeSymbol getFlexSymbol(QueryPart definition, TypeSystem typeSystem, MetaObject type, String attributeName) {
		FlexAttributeSymbol attributeSymbol = flexAttributeSymbols.get(attributeName);
		if (attributeSymbol == null) {
			attributeSymbol = new FlexAttributeSymbol(definition, typeSystem, this, type, attributeName);
			flexAttributeSymbols.put(attributeName, attributeSymbol);
		} else {
			assert attributeSymbol.getType().equals(type) : "Type mismatch for flex symbols.";
		}
		
		return attributeSymbol;
	}
}