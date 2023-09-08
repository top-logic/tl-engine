/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport.converterfunction;

import com.top_logic.element.genericimport.interfaces.GenericCache;
import com.top_logic.element.genericimport.interfaces.GenericConverterFunction;
import com.top_logic.knowledge.wrap.Document;

/**
 * The DocumentMapping imports a document from the external path saved in the
 * exported document.
 * 
 * @author    <a href=mailto:TEH@top-logic.com>TEH</a>
 */
public class DocumentMapping implements GenericConverterFunction {
    
    /**
     * TODO make sure this works
     * @see com.top_logic.element.genericimport.interfaces.GenericConverterFunction#map(java.lang.Object, com.top_logic.element.genericimport.interfaces.GenericCache)
     */
    @Override
	public Object map(Object aValue, GenericCache aCache) {
        return Document.createDocument("", (String)aValue, Document.getDefaultKnowledgeBase());
    }
}

