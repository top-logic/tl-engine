/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.search.nodes;

import com.top_logic.base.search.QueryNode;

/**
 * This is a QueryNode with two Subnodes.
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public abstract class BinaryNode extends QueryNode {

    /** The First Subnode used as parameter */
    protected QueryNode   node1;

    /** The Second Subnode used as parameter */
    protected QueryNode   node2;

    /**
     * A short version of the Operator (Token), as found in the Syntax.
     */
    public abstract String getOper ();

    /**
     * A long version of the Operator (Token), as found in the Syntax.
     */
    public abstract String getLongOper ();
    
    /** Accessor to the first Subnode */
    public QueryNode getNode1() {
        return node1;
    }
    
    /** Accessor to the second Subnode */
    public QueryNode getNode2() {
        return node2;
    }
}
