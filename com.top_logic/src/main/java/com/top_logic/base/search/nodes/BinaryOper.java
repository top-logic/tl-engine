/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.search.nodes;

import com.top_logic.base.search.QueryException;
import com.top_logic.base.search.QueryNode;
import com.top_logic.base.search.QueryVisitor;

/**
 * BinrayOperators are &amp; (AND) | (OR) M (MATCHES) &lt;- (ISSOURCE) -&gt; (ISDEST)
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class BinaryOper extends BinaryNode {

    /** 
     * Construct a BinaryOper with the given subexpressions.
     *
     * @param aKind One of the Values found in 
     *  {@link com.top_logic.base.search.parser.QueryPrefixConstants }
     */
    public BinaryOper(int aKind, QueryNode expr1, QueryNode expr2)
    {
        kind  = aKind;
        node1 = expr1;
        node2 = expr2;
    }

    /**
     * a short version of the Operator (Token), as found in the Syntax.
     */                             
    @Override
	public String getOper () {
        switch (kind)  {
            case AND      : return "&";
            case OR       : return "|";
            case MATCHES  : return "M";
            case ISSOURCE : return "->";
            case ISDEST   : return "<-";
        }
        return "?UNKNOWN?";
    }

    /**
     * a long version of the Operator (Token), as found in the Syntax.
     */
    @Override
	public String getLongOper () {
        switch (kind)  {
            case AND      : return "AND";
            case OR       : return "OR";
            case MATCHES  : return "MATCHES";
            case ISSOURCE : return "ISSOURCE";
            case ISDEST   : return "ISDEST";
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
