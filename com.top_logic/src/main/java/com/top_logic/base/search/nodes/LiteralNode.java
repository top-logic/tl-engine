/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.search.nodes;


import com.top_logic.base.search.QueryException;
import com.top_logic.base.search.QueryNode;
import com.top_logic.base.search.QueryVisitor;
import com.top_logic.basic.StringServices;

/**
 * This node represents a Literal or Identifier as defined in the Query Language
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class LiteralNode extends QueryNode {

    /** Usefull constant for a TRUE expression */
    public static final LiteralNode LITERAL_TRUE = 
        new LiteralNode(BOOLEAN_LITERAL, Boolean.TRUE);

    /** Usefull constant for a FALSE expression */
    public static final LiteralNode LITERAL_FALSE = 
        new LiteralNode(BOOLEAN_LITERAL, Boolean.FALSE);

     /** The value actually represented, may be a String, Double, Date, or Boolean */
    private Object value;

    /** 
     * Construct a Literal Node by parsing the given value according to kind.
     *
     * @param aKind One of the Values found in 
     *  {@link com.top_logic.base.search.parser.QueryPrefixConstants }
     *
     *  @throws QueryException in case Parsing fails and similar errors.
     */
    public LiteralNode(int aKind, String aValue) 
        throws QueryException
    {
        kind  = aKind;
        value = LiteralHelper.objectForKind(aKind, aValue);
    }
     
    /** 
     * Construct a Literal Node with the given Value. 
     *
     * No test for the correctnes of the kind with the class of the value is performed.
     *
     * @param aKind One of the Values found in 
     *  {@link com.top_logic.base.search.parser.QueryPrefixConstants }
     */
    public LiteralNode(int aKind, Object aValue) {
        kind  = aKind;
        value = aValue;
    }
    
    /**
     * Delivers the description of the instance of this class.
     *
     * @return    The quoted value as String.
     */
    @Override
	public String toString () {
           return "Literal: '" + value + "'";
    }

    /**
     * A short version of the value, as found in the Syntax
     */
    public String getImage () {
        switch (kind)  {
            case BOOLEAN_LITERAL:
                return ((Boolean) value).booleanValue() ? "T" : "F";
            case STRING_LITERAL:
                return '"' + StringServices.escape(value.toString()) + '"';
            case DATE_LITERAL:
                return LiteralHelper.getIsoDate().format(value);
            case TIME_LITERAL:
                return LiteralHelper.getIsoTimeFormat().format(value);
            case DATE_TIME_LITERAL:
                return LiteralHelper.getIsoDateTimeFormat().format(value);
            case FLOATING_POINT_LITERAL:
                if (value instanceof Double)
                    return value.toString() + 'd';
                else    
                    return value.toString();    
            case INTEGER_LITERAL:
                if (value instanceof Long)
                    return value.toString() + 'l';
                // intentionally no break;
            default:    // may need some formatters here ...
                return value.toString();    
        }
    }

    /**
     * A long version of the value, as found in the Syntax
     */
    public String getLongImage () {
        switch (kind)  {
            case BOOLEAN_LITERAL:
                return ((Boolean) value).booleanValue() ? "TRUE" : "FALSE";
            case STRING_LITERAL:
                return '"' + StringServices.escape(value.toString()) + '"';
            case DATE_LITERAL:
                return LiteralHelper.getIsoDate().format(value);
            case TIME_LITERAL:
                return LiteralHelper.getIsoTimeFormat().format(value);
            case DATE_TIME_LITERAL:
                return LiteralHelper.getIsoDateTimeFormat().format(value);
            case FLOATING_POINT_LITERAL:
                if (value instanceof Double)
                    return value.toString() + 'd';
                else    
                    return value.toString();    
            case INTEGER_LITERAL:
                if (value instanceof Long)
                    return value.toString() + 'l';
                // intentionally no break;
            default:    // may need some formatters here ...
                return  value.toString();
        }
    }

    
    /** Accessor for the value carried. */
    public Object getValue()  {
        return value;
    }
    
    /**
     * Call back the Visitor for the actual subclass.
     */
    @Override
	public void visitNode(QueryVisitor aVisitor) throws QueryException {
        aVisitor.visitNode(this);
    }
}
