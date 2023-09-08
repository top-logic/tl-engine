/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.search;

import com.top_logic.base.search.parser.QueryPrefixConstants;
import com.top_logic.base.search.visitors.PrefixVisitor;

/**
 * Subclasses of this class represent nodes in the Query Languge.
 * 
 * <p>
 * This class cooperates with the QueryVisitor to implement a visitor pattern. It uses the constants
 * provided by the PrefixParser.
 * </p>
 * 
 * @author <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public abstract class QueryNode implements QueryPrefixConstants {

    /** One of the Constants as found in 
     * {@link com.top_logic.base.search.parser.QueryPrefixConstants }
     */
    protected int kind;

    /**
     * Delivers the description of the instance of this class.
     *
     * @return    A Prefix Representation of this Node.
     */
    @Override
	public String toString () {
        return new PrefixVisitor().visit(this);
    }

    /**
     * Call back the Visitor for the actual subclass.
     */
    abstract public void visitNode(QueryVisitor aVisitor) throws QueryException;
    
    /** Accessor for kind 
     *
     * @return One of the Values found in 
     *  {@link com.top_logic.base.search.parser.QueryPrefixConstants }
     */  
    public final int getKind() {
        return kind;
    }
}
