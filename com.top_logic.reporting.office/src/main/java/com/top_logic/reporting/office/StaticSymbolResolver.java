/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.office;


/**
 * This interface defines a resolver for static symbols. 
 * This resolving may be complicated so the implementing class may be the 
 * ReportHandler itself!
 * 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public interface StaticSymbolResolver {
    
    /**
     * Expands the symbol content of a static {@link ExpansionObject}.
     * 
     * @param aContext the expansion context may be needed for expansion
     * @param aSymbol the expansion object to expand
     * @return the expanded object or <code>null</code> expansion was not
     *         possible
     */
    public Object resolveSymbol (ExpansionContext aContext, ExpansionObject aSymbol);
}
