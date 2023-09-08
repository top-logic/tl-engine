/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.search.nodes;

import com.top_logic.base.search.QueryException;
import com.top_logic.base.search.QueryNode;

/**
 * This is a QueryNode with just one Subnode.
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public abstract class UnaryNode extends QueryNode {

    /** The Subnode this node modifies */
    protected QueryNode   subNode;

    /**
     * A short version of the Operator (Token), as found in the Syntax.
     */
    public abstract String getOper ();

    /**
     * A long version of the Operator (Token), as found in the Syntax.
     */
    public abstract String getLongOper ();
    
    /** Accessor to the Subnode this node modifies */
    public QueryNode getSubNode() throws QueryException {
        return subNode;
    }
    
}
