/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.search.visitors;

import com.top_logic.base.search.Query;
import com.top_logic.base.search.QueryException;
import com.top_logic.base.search.QueryNode;
import com.top_logic.base.search.QueryVisitor;
import com.top_logic.base.search.nodes.AllNode;
import com.top_logic.base.search.nodes.BinaryFilter;
import com.top_logic.base.search.nodes.BinaryNode;
import com.top_logic.base.search.nodes.BinaryOper;
import com.top_logic.base.search.nodes.ExampleNode;
import com.top_logic.base.search.nodes.LiteralNode;
import com.top_logic.base.search.nodes.UnaryFilter;
import com.top_logic.base.search.nodes.UnaryNode;
import com.top_logic.base.search.nodes.UnaryOper;

/**
 * Common subclass for visitors, relates all methods up the Node hierarchy.
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public abstract class AbstractVisitor implements QueryVisitor {

    /** Start visiting a Query here. */
    @Override
	public void visitQuery(Query aQuery)  throws QueryException {
                   aQuery.getRoot().visitNode(this);
        visitOrder(aQuery.getOrderBy());
    }

    /** Do something with the ORDER BY part of the query */
    protected abstract void visitOrder(String orderBy[]) throws QueryException;
    
    /** Override to catch any QueryNode */
    protected void visitQueryNode(QueryNode aNode) throws QueryException {
        // catch all, but do nothing
    }

    /** Override to catch unary Nodes. */
    protected void visitUnaryNode(UnaryNode aNode) throws QueryException {
        visitQueryNode(aNode);
    }

    /** Override to catch binary Nodes. */
    protected void visitBinaryNode(BinaryNode aNode) throws QueryException {
        visitQueryNode(aNode);
    }
    
    /** Visit an AllNode */
    @Override
	public void visitNode(AllNode all) throws QueryException {
        visitQueryNode(all);
    }

    /** Visit an ExampleNode */
    @Override
	public void visitNode(ExampleNode example) throws QueryException {
        visitQueryNode(example);
    }

    /** Visit a LiteralNode */
    @Override
	public void visitNode(LiteralNode aLiteral) throws QueryException {
        visitQueryNode(aLiteral);
    }

    /** Visit a UnaryOperator */
    @Override
	public void visitNode(UnaryOper anOper) throws QueryException {
        visitUnaryNode(anOper);
    }

    /** Visit a UnaryFilter */
    @Override
	public void visitNode(UnaryFilter aFilter) throws QueryException {
        visitUnaryNode(aFilter);
    }
    
    /** Visit a BinaryOperator */
    @Override
	public void visitNode(BinaryOper anOper) throws QueryException {
        visitBinaryNode(anOper);
    }

    /** Visit a BinaryFilter */
    @Override
	public void visitNode(BinaryFilter aFilter) throws QueryException {
        visitBinaryNode(aFilter);
    }
    
    


}
