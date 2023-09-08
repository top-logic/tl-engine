/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.office.basic;

import com.top_logic.basic.Logger;
import com.top_logic.reporting.office.ExpansionContext;
import com.top_logic.reporting.office.ExpansionEngine;
import com.top_logic.reporting.office.ExpansionObject;
import com.top_logic.reporting.office.StaticSymbolResolver;


/**
 * This is the most basic implementation of an expansion engine, but should suffice 
 * for the most cases.
 * This engine is capable of interpreting static and script symbols.
 * 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class BasicExpansionEngine implements ExpansionEngine {

    
    private StaticSymbolResolver symbolResolver;
    public BasicExpansionEngine() {
        super();
    }
    
    /**
     * @see com.top_logic.reporting.office.ExpansionEngine#setStaticSymbolResolver(com.top_logic.reporting.office.StaticSymbolResolver)
     */
    @Override
	public void setStaticSymbolResolver (StaticSymbolResolver aResolver) {
        symbolResolver = aResolver;
    }
    /**
     * We delegate to the right method according to the type of symbol, static or script.
     * @see com.top_logic.reporting.office.ExpansionEngine#expandSymbol(com.top_logic.reporting.office.ExpansionContext, com.top_logic.reporting.office.ExpansionObject)
     */
    @Override
	public Object expandSymbol(ExpansionContext aContext, ExpansionObject aSymbol) {
        String symbolType = aSymbol.getSymbolType();
        if (ExpansionObject.STATIC.equals (symbolType)) {
            return expandStatic (aContext,aSymbol);
        }
        return null;
    }

    /**
     * Expand static symbols by using the configured StaticSymbolResolver
     * 
     * @param aContext  the expansion context to use
     * @param aSymbol   the symbol to expand
     * @return          and object which is the result of the resolving, may be <code>null</code>
     */
    protected Object expandStatic (ExpansionContext aContext, ExpansionObject aSymbol) {
        if (symbolResolver != null) {
            return symbolResolver.resolveSymbol(aContext, aSymbol);
        } else {
            Logger.warn ("no static resolver defined, so do nothing",this);
            return null;
        }
    }
    
}
