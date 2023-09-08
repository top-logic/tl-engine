/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.search.nodes;

import com.top_logic.base.search.QueryException;
import com.top_logic.base.search.QueryVisitor;

/**
 * BinaryFilters are =, &lt;, &gt;,&lt;=, &gt;=, c=, L.
 * <pre>
 *  "="   | "EQUALS"          |
 *  "&lt;"   | "LESS"            | 
 *  "&gt;"   | "GREATER"         |
 *  "&lt;="  | "LESSOREQUAL"     | 
 *  "&gt;="  | "GERATEROREQUALS" |
 *  "c="  | "ISIN"            |
 *  "L"   | "LIKE"; 
 *</pre>
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class BinaryFilter extends BinaryNode {

    /** 
     * Construct a BinaryFilter with the given subnodes
     *
     * @param aKind One of the Values found in 
     *  {@link com.top_logic.base.search.parser.QueryPrefixConstants }
     */
    public BinaryFilter(int aKind, LiteralNode aFirstNode, LiteralNode aSecondNode) 
        throws QueryException
    {
        kind    = aKind;
        //if (aFirdstNode.getKind() != IDENTIFIFER)
        //    throw new QueryException(
        //        "UnaryFilter must have INDETIFIER as First Paramert but got " +
        //            QueryInfixConstants.tokenImage[aFirdstNode.getKind()]);
        node1   = aFirstNode;
        node2   = aSecondNode;
    }

    /** 
     * Construct a BinaryFilter for given Attribute / value Parameters.
     *
     * @param aKind One of the Values found in 
     *  {@link com.top_logic.base.search.parser.QueryPrefixConstants }
     * @param value always a String value (as of now).
     */
    public BinaryFilter(int aKind, String attrib, String value) {
        kind  = aKind;
        node1 = new LiteralNode(IDENTIFIER    , (Object) attrib);
        node2 = new LiteralNode(STRING_LITERAL, (Object) value);
    }

    /** Static version of getOper for use in GUIs etc.
     *
     * @return a short version of the Operator (Token), as found in the Syntax.
     */
    public static String getOper (int aKind) {
        switch (aKind)  {
            case EQUALS : return "=";
            case LT     : return "<";
            case GT     : return ">";
            case LE     : return "<=";
            case GE     : return ">=";
            case ISIN   : return "c=";
            case LIKE   : return "L";
        }
        return "?UNKNOWN?";
    }

    /** 
     * a short version of the Operator (Token), as found in the Syntax.
     */
    @Override
	public String getOper () {
        return getOper(kind);
    }

    /** Static version of getLongOper for use in GUIs etc.
     *
     * @return a long version of the Operator (Token), as found in the Syntax.
     */
    public static String getLongOper (int aKind) {
        switch (aKind)  {
            case EQUALS : return "EQUALS";
            case LT     : return "LESS";
            case GT     : return "GREATER";
            case LE     : return "LESSOREQUAL";
            case GE     : return "GREATEROREQUAL";
            case ISIN   : return "ISIN";
            case LIKE   : return "LIKE";
        }
        return "?UNKNOWN?";
    }
    
    /**
     * a long version of the Operator (Token), as found in the Syntax.
     */
    @Override
	public String getLongOper () {
        return getLongOper(kind);
    }

    /**
     * Call back the Visitor for the actual subclass.
     */
    @Override
	public void visitNode(QueryVisitor aVisitor) throws QueryException {
        aVisitor.visitNode(this);
    }
}
