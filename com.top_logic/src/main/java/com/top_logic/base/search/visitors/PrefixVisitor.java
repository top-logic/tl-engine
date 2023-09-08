/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.search.visitors;

import com.top_logic.base.search.Query;
import com.top_logic.base.search.QueryException;
import com.top_logic.base.search.QueryNode;
import com.top_logic.base.search.nodes.AllNode;
import com.top_logic.base.search.nodes.BinaryNode;
import com.top_logic.base.search.nodes.ExampleNode;
import com.top_logic.base.search.nodes.LiteralNode;
import com.top_logic.base.search.nodes.UnaryNode;

/**
 * Visitor for QueryNodes in prefix order, using short expressions.
 *<p>
 *  This class must be used single threaded only.
 *</p>
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class PrefixVisitor extends AbstractVisitor {

    /** String Bufffer to accumulate the prefix image of the query. */
    private StringBuffer prefixBuffer;

    /** Main visitor function,
     *
     * @return the Query as Prefix Expression 
     */
    public String visit(Query aQuery) {
        try {
            prefixBuffer = new StringBuffer(64);
            visitQuery(aQuery);
            return prefixBuffer.toString();
        }
        catch (QueryException qe) {  // will not happen here 
            return qe.getMessage();
        }
    }

    /** Visitor function for QueryNodes (used by QueryNode.toString(),
     *
     * @return the Tree of QueryNodes as Prefix Expression.
     */
    public String visit(QueryNode aNode)  {
        try {
            prefixBuffer = new StringBuffer(64);
            aNode.visitNode(this);
            return prefixBuffer.toString();
        }
        catch (QueryException qe) {  // will not happen here 
            return qe.getMessage();
        }
    }
    
    /** Visit an AllNode */
    @Override
	public void visitNode(AllNode all) {
        prefixBuffer.append(all.getImage());
    }

    /** Visit an ExampleNode */
    @Override
	public void visitNode(ExampleNode example) {
        prefixBuffer.append(example.getImage());
    }

    /** Visit a LiteralNode */
    @Override
	public void visitNode(LiteralNode aLiteral) {
        prefixBuffer.append(aLiteral.getImage());
    }
    
    /** Visit a UnaryNode to create "oper(param)" */
    @Override
	public void visitUnaryNode(UnaryNode aNode) throws QueryException {
        prefixBuffer.append(aNode.getOper());
        prefixBuffer.append('(');
        aNode.getSubNode().visitNode(this);
        prefixBuffer.append(')');
    }

    /** Visit a BinaryNode to create "oper(param, param)" */
    @Override
	public void visitBinaryNode(BinaryNode aNode) throws QueryException  {
        prefixBuffer.append(aNode.getOper());
        prefixBuffer.append('(');
        aNode.getNode1().visitNode(this);
        prefixBuffer.append(',');
        aNode.getNode2().visitNode(this);
        prefixBuffer.append(')');
    }

    /** Append the ORDER BY part to the queryBuffer */
    @Override
	public void visitOrder(String orderBy[]) {
        if (orderBy != null)  {
            int size = orderBy.length;
            prefixBuffer.append(" ORDER BY ");
            prefixBuffer.append(orderBy[0]);
            for (int i=1; i < size; i++) {
                prefixBuffer.append(',');
                prefixBuffer.append(orderBy[i]);
            }        
        }
    }


}
