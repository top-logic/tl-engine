/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.search;

import com.top_logic.base.search.nodes.AllNode;
import com.top_logic.base.search.nodes.BinaryFilter;
import com.top_logic.base.search.nodes.BinaryOper;
import com.top_logic.base.search.nodes.ExampleNode;
import com.top_logic.base.search.nodes.LiteralNode;
import com.top_logic.base.search.nodes.UnaryFilter;
import com.top_logic.base.search.nodes.UnaryOper;

/**
 * Visitor for Queries and Quernodes for several purposes.
 *<p>
 *  The Order of the visitor calls depends on the implementation.
 *</p>
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public interface QueryVisitor {

    /** Visit the Query itself */
    public void visitQuery(Query aQuery) throws QueryException;

    /** Visit an AllNode */
    public void visitNode(AllNode all) throws QueryException;

    /** Visit an ExampleNode */
    public void visitNode(ExampleNode example) throws QueryException;

    /** Visit a LiteralNode */
    public void visitNode(LiteralNode aLiteral) throws QueryException;

    /** Visit a UnaryOperator */
    public void visitNode(UnaryOper anOper) throws QueryException;

    /** Visit a UnaryFilter */
    public void visitNode(UnaryFilter aFilter) throws QueryException;

    /** Visit a BinaryOperator */
    public void visitNode(BinaryOper anOper) throws QueryException;

    /** Visit a BinaryFilter */
    public void visitNode(BinaryFilter aFilter) throws QueryException;
}
