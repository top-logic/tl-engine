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
 * This node represents All or Nothing.
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class AllNode extends QueryNode {

    /** Usefull constant for all Elements */
    public static final AllNode ALL  = new AllNode(true);

    /** Usefull constant for no Elements */
    public static final AllNode NONE = new AllNode(false);

    /** True for "ALL", false for "NONE" */
    boolean isAll;
    
    /** 
     * There are only the two constant AllNode, use one of them. 
     *
     * @param isAll indicates if this isn A (All) or N (None) Node.
     */
    private AllNode(boolean isAll) {
        kind        = ALLORNONE;
        this.isAll  = isAll;
    }
    
    /**
     * Delivers the description of the instance of this class.
     *
     * @return    The quoted value as String.
     */
    @Override
	public String toString () {
         return getLongImage();
    }

    /**
     * A short version of the value, as found in the Syntax
     */
    public String getImage () {
        return isAll ? "A" : "N";
    }

    /**
     * A long version of the value, as found in the Syntax
     */
    public String getLongImage () {
        return isAll ? "ALL" : "NONE";
    }

    /**
     * Call back the Visitor for the actual subclass.
     */
    @Override
	public void visitNode(QueryVisitor aVisitor) throws QueryException {
        aVisitor.visitNode(this);
    }
}
