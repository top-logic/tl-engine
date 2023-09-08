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
 * The only currently known UnaryOperator is '!' (NOT).
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class UnaryOper extends UnaryNode {

    /** 
     * Construct a UnaryOper Node with the given subnode 
     *
     * @param aKind One of the Values found in 
     *  {@link com.top_logic.base.search.parser.QueryPrefixConstants }
     */
    public UnaryOper(int aKind, QueryNode aSubNode) 
    {
        kind    = aKind;
        subNode = aSubNode;
    }

    /**
     * a short version of the Operator (Token), as found in the Syntax.
     */
    @Override
	public String getOper () {
        switch (kind)  {
            case NOT:   return "!";
        }
        return "?UNKNOWN?";
    }

    /**
     * a long version of the Operator (Token), as found in the Syntax.
     */
    @Override
	public String getLongOper () {
        switch (kind)  {
            case NOT:   return "NOT";
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
