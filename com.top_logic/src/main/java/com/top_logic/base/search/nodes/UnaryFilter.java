/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.search.nodes;

import com.top_logic.base.search.QueryException;
import com.top_logic.base.search.QueryVisitor;

/**
 * UnaryFilters are "$" (TEXT), "@" (ID) and "#" (TYPE)
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class UnaryFilter extends UnaryNode {

    /** 
     * Construct a UnaryFilter Node with the given Literal subnode 
     *
     * @param aKind One of the Values found in 
     *  {@link com.top_logic.base.search.parser.QueryPrefixConstants }
     */
    public UnaryFilter(int aKind, LiteralNode aSubNode) 
    {
        kind    = aKind;
        subNode = aSubNode;
    }

    /** 
     * Construct a UnaryFilter Node with the given (String)-Literal
     *
     * @param aKind One of the Values found in 
     *  {@link com.top_logic.base.search.parser.QueryPrefixConstants }
     */
    public UnaryFilter(int aKind, String literal) 
    {
        kind    = aKind;
        subNode = new LiteralNode(STRING_LITERAL, (Object) literal);
    }

    /**
     * a short version of the Operator (Token), as found in the Syntax.
     */
    @Override
	public String getOper () {
        switch (kind)  {
            case ID  :   return "@";
            case TYPE:   return "#";
            case TEXT:   return "$";
        }
        return "?UNKNOWN?";
    }

    /**
     * a long version of the Operator (Token), as found in the Syntax.
     */
    @Override
	public String getLongOper () {
        switch (kind)  {
            case ID  :   return "ID";
            case TYPE:   return "TYPE";
            case TEXT:   return "TEXT";
        }
        return "?UNKNOWN?";
    }
    
    /**
     * Call back the Visitor for the actual subclass.
     */
    @Override
	public void visitNode(QueryVisitor aVisitor) throws QueryException {
        aVisitor.visitNode(this);
    }
}
