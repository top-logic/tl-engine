/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.searching.internet;

import com.top_logic.base.search.Query;
import com.top_logic.base.search.QueryException;
import com.top_logic.base.search.nodes.BinaryNode;
import com.top_logic.base.search.nodes.LiteralNode;
import com.top_logic.base.search.nodes.UnaryNode;
import com.top_logic.base.search.parser.QueryPrefixConstants;
import com.top_logic.base.search.visitors.AbstractVisitor;

/** 
 * A query visitor providing full text searches for internet search machines.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class InternetQueryVisitor extends AbstractVisitor {

    /** The result of this visitor. */
    private StringBuffer result;

    /** The value to be used for AND in the result. */
    private String and;

    /** The value to be used for OR in the result. */
    private String or;

    /** Flag, if brackets should be used in the result. */
    private boolean brackets;

    /**
     * Create a simple visitor, which generates an infix query.
     * 
     * The AND and OR operator will be displayed (as "AND" and "OR"), the
     * different expressions will be grouped by brackets.
     */
    public InternetQueryVisitor() {
        this("AND", "OR", true);
    }

    public InternetQueryVisitor(String anAnd, String anOr, boolean withBrackets) {
        this.and      = anAnd;
        this.or       = anOr;
        this.brackets = withBrackets;
    }

    /**
     * Start visiting the given query and return the result of the visiting tour.
     * 
     * This is the entrance point for a visitor tour.
     * 
     * @param    aQuery    The query to be parsed.
     * @return   The resulting string representation of the query.
     */
    public String visit(Query aQuery) {
        try {
            this.result = new StringBuffer(256);

            this.visitQuery(aQuery);

            return (result.toString().trim());
        }
        catch (QueryException qe) {  // will not happen here 
            return qe.getMessage();
        }
    }

    /**
     * @see com.top_logic.base.search.visitors.AbstractVisitor#visitBinaryNode(BinaryNode)
     */
    @Override
	public void visitBinaryNode(BinaryNode aNode) throws QueryException {
        if (this.brackets) {
            this.result.append('(');
        }

        int     theOp       = aNode.getKind();
        boolean isOperation = (theOp == QueryPrefixConstants.OR || theOp == QueryPrefixConstants.AND);

        aNode.getNode1().visitNode(this);

        if (isOperation) {
            this.result.append(' ');

            if (((theOp == QueryPrefixConstants.OR)  && (this.or != null)) ||
                ((theOp == QueryPrefixConstants.AND) && (this.and != null))) {
                this.result.append(aNode.getLongOper()).append(' ');
            }
        }

        aNode.getNode2().visitNode(this);

        if (this.brackets) {
            this.result.append(')');
        }
    }

    /**
     * @see com.top_logic.base.search.visitors.AbstractVisitor#visitUnaryNode(UnaryNode)
     */
    @Override
	public void visitUnaryNode(UnaryNode aNode) throws QueryException {
        if (aNode.getKind() == QueryPrefixConstants.TEXT) {
            aNode.getSubNode().visitNode(this);
        }
    }

    /**
     * @see com.top_logic.base.search.visitors.AbstractVisitor#visitNode(LiteralNode)
     */
    @Override
	public void visitNode(LiteralNode aNode) throws QueryException {
        this.result.append(aNode.getValue());
    }

    /**
     * @see com.top_logic.base.search.visitors.AbstractVisitor#visitOrder(String[])
     */
    @Override
	protected void visitOrder(String[] orderBy) throws QueryException {
        // not needed / support
    }
}
