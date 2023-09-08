/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.office;



/**
 * An expansion engine takes symbols and extracts them according to a certain
 * {@link com.top_logic.reporting.office.ExpansionContext}. Depending on the
 * type of the given symbols different mechanisms are used.
 * 
 * @author <a href=mailto:jco@top-logic.com>jco</a>
 */
public interface ExpansionEngine {

    /**
     * To expand an expansion object (or symbol) we need the context, delivering
     * the necessary information.
     * 
     * @param aContext the context with environmental information.
     * @param aSymbol the expansion object in its symbol state.
     * @return the expanded Object (not the ExpansionObject!) or
     *         <code>null</code>. The expanded object is the content
     *         evaluated by using the expansion object and the expansion
     *         context.
     */
    public Object expandSymbol (ExpansionContext aContext, ExpansionObject aSymbol);

    /**
     * Sets the resolver class to use for interpreting static symbols.
     * @param aResolver a resolver to use
     */
    public void setStaticSymbolResolver (StaticSymbolResolver aResolver);
}
