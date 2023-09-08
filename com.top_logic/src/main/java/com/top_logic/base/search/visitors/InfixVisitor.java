/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.search.visitors;

import com.top_logic.base.search.Query;
import com.top_logic.base.search.QueryException;
import com.top_logic.base.search.nodes.AllNode;
import com.top_logic.base.search.nodes.BinaryNode;
import com.top_logic.base.search.nodes.ExampleNode;
import com.top_logic.base.search.nodes.LiteralNode;
import com.top_logic.base.search.nodes.UnaryNode;

/**
 * Visitor for QueryNodes in infix order, using longer expressions.
 *<p>
 *  This class must be used single threaded only.
 *</p>
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class InfixVisitor extends AbstractVisitor {

    /** String Bufffer to accumulate the prefix image of the query. */
    private StringBuffer infixBuffer;

    /** Main visitor function,
     *
     * @return the Query as Prefix Expression 
     */
    public String visit(Query aQuery) {
        try {
            infixBuffer = new StringBuffer(256);
            visitQuery(aQuery);
            return infixBuffer.toString();
        }
        catch (QueryException qe) {  // will not happen here 
            return qe.getMessage();
        }
    }

    /** Visit an AllNode */
    @Override
	public void visitNode(AllNode all) {
        infixBuffer.append(all.getLongImage());
    }

    /** Visit an ExampleNode */
    @Override
	public void visitNode(ExampleNode example) {
        infixBuffer.append(example.getLongImage());
    }

    /** Visit a LiteralNode */
    @Override
	public void visitNode(LiteralNode aLiteral) throws QueryException {
        infixBuffer.append(aLiteral.getLongImage());
    }
    
    /** Visit a LiteralNode */
    @Override
	public void visitUnaryNode(UnaryNode aNode) throws QueryException  {
        infixBuffer.append(aNode.getLongOper());
        infixBuffer.append(" (");
        aNode.getSubNode().visitNode(this);
        infixBuffer.append(')');
    }

    /** Visit a BinaryNode to create "(param oper param)" */
    @Override
	public void visitBinaryNode(BinaryNode aNode)  throws QueryException  {
        infixBuffer.append("( ");
        aNode.getNode1().visitNode(this);
        infixBuffer.append(' ');
        infixBuffer.append(aNode.getLongOper());
        infixBuffer.append(' ');
        aNode.getNode2().visitNode(this);
        infixBuffer.append(" )");
    }

    /** Append the ORDER BY part to the queryBuffer */
    @Override
	public void visitOrder(String orderBy[]) {
        if (orderBy != null)  {
            int size = orderBy.length;
            infixBuffer.append(" ORDER BY ");
            infixBuffer.append(orderBy[0]);
            for (int i=1; i < size; i++) {
                infixBuffer.append(", ");
                infixBuffer.append(orderBy[i]);
            }        
        }
    }
}
