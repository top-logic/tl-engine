/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.office;



/**
 * This default implementation of the interfaces assumes the key to resolve is a key in the
 * ExpansionContext.
 * 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class DefaultStaticSymbolResolver implements StaticSymbolResolver {

    /** 
     * Look for the key in the expansion Context then, if there is a value for the key return it.
     * @see com.top_logic.reporting.office.StaticSymbolResolver#resolveSymbol(com.top_logic.reporting.office.ExpansionContext, com.top_logic.reporting.office.ExpansionObject)
     */
    @Override
	public Object resolveSymbol(ExpansionContext aContext, ExpansionObject aSymbol){
        String theSymbolKey = aSymbol.getSymbolContent();
        Object result = aContext.getBusinessObjects().get(theSymbolKey);

        return result;
    }

}
