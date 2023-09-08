/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.office.basic;

import com.top_logic.basic.AliasManager;
import com.top_logic.reporting.office.ExpansionContext;
import com.top_logic.reporting.office.ExpansionObject;
import com.top_logic.reporting.office.StaticSymbolResolver;


/**
 * This default implementation uses the AliasManager to resolve the 
 * given ExpansionObject.
 * 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class AliasSymbolResolver implements StaticSymbolResolver {

    /**
     * Try to expand the symbol by using the AliasManager :)
     * 
     * @see com.top_logic.reporting.office.StaticSymbolResolver#resolveSymbol(com.top_logic.reporting.office.ExpansionContext,
     *      com.top_logic.reporting.office.ExpansionObject)
     */
    @Override
	public Object resolveSymbol(ExpansionContext aContext, ExpansionObject aSymbol) {
        String theSymbolKey = aSymbol.getSymbolContent();
        
        String result =AliasManager.getInstance().getAlias(getEncodedKey(theSymbolKey));
        return result;
    }
    
    /**
     * @param aSymbolKey the symbol to encode for resolving.
     * @return the given symbol if leading and trailing '%'.
     */
    private String getEncodedKey (String aSymbolKey) {
        String result = (!aSymbolKey.startsWith("%")) ? ("%" + aSymbolKey) : aSymbolKey; 
        return (!aSymbolKey.endsWith("%")) ? (result + "%") : result;
    }
}
