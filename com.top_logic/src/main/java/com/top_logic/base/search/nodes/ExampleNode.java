/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.search.nodes;

import java.util.Date;
import java.util.Map;

import com.top_logic.base.search.QueryException;
import com.top_logic.base.search.QueryNode;
import com.top_logic.base.search.QueryVisitor;
import com.top_logic.basic.StringServices;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.simple.ExampleDataObject;

/**
 * This node represents a DataObject given as an Example.
 *<p>
 *  There is a Problem with the (Token) kinds. This class will
 *  not store the kinds per Attribute/Value pair, so when recreating
 *  the Object as String it may not be the exepcted.
 *</p>
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class ExampleNode extends QueryNode {

    /** The object actually used as example */
    DataObject example;

    /** 
     * Construct an ExampleNode with an arbitrary DataObject.
     *
     */
    public ExampleNode(DataObject example) {
        kind            = EXAMPLE;
        this.example    = example;
    }
    
    /** 
     * Construct an ExampleNode with a Map of Key/Value pairs.
     *  {@link com.top_logic.base.search.parser.QueryPrefixConstants}
     */
    public ExampleNode(Map map) {
        kind            = EXAMPLE;
        this.example    = new ExampleDataObject(map);
    }

    /**
     * Delivers the description of the instance of this class.
     *
     * @return    The quoted value as String.
     */
    @Override
	public String toString () {
         return example.toString();
    }

    /** Format an abritraty object according to the EXAMPLE Syntax.
     *
     * @return  A short version of the value, as found in the Syntax
     */
    public String getImage (Object value) {
        if (value instanceof String)
            return '"' + StringServices.escape(value.toString()) + '"';
        else if (value instanceof Number) {
            if (value instanceof Long)
                return value.toString() + 'l';
            if (value instanceof Double)
                return value.toString() + 'd';
            else // Integer, Float, whatever
                return value.toString();
        }
        else if (value instanceof Boolean)
            return ((Boolean) value).booleanValue() ? "T" : "F";
        else if (value instanceof Date)
            // cannot distinguish DATE_LITERAL / TIME_LITERAL here
            return LiteralHelper.getIsoDateTimeFormat().format(value);
        else // ???
            return value.toString();    
    }

    /** Format an abritraty object according to the EXAMPLE Syntax.
     *
     * @return  A short version of the value, as found in the Syntax
     */
    public String getLongImage (Object value) {
        if (value instanceof String)
            return '"' + StringServices.escape(value.toString()) + '"';
        else if (value instanceof Number) {
            if (value instanceof Long)
                return value.toString() + 'l';
            if (value instanceof Double)
                return value.toString() + 'd';
            else // Integer, Float, whatever
                return value.toString();
        }
        else if (value instanceof Boolean)
            return ((Boolean) value).booleanValue() ? "TRUE" : "FALSE";
        else if (value instanceof Date)
            // cannot distinguish DATE_LITERAL / TIME_LITERAL here
            return LiteralHelper.getIsoDateTimeFormat().format(value);
        else // ???
            return value.toString();    
    }

    /**
     * A short version of the value, as found in the Syntax
     */
    public String getImage () {
        String [] names = example.getAttributeNames();
        int len = names.length;
        StringBuffer result = new StringBuffer(len << 6);
        result.append("E(");
        for (int i=0; i < len;  i++) {
            String name = names[i];
            if (i > 0)
                result.append(',');
            Object val; 
            try {
                val = example.getAttributeValue(name);
            } 
            catch (NoSuchAttributeException nse) {
                val = nse.getMessage();  // shutld not happen anyway :-)
            }
            result.append(name);
            result.append(',');
            result.append(getImage(val));
        }
        result.append(')');
        return result.toString();
    }

    /**
     * A long version of the value, as found in the Syntax
     */
    public String getLongImage () {
        String [] names = example.getAttributeNames();
        int len = names.length;
        StringBuffer result = new StringBuffer(len << 7);
        result.append("EXAMPLE ( ");
        for (int i=0; i < len;  i++) {
            String name = names[i];
            if (i > 0)
                result.append(" , ");
            Object val;
            try {
                val = example.getAttributeValue(name);
            } 
            catch (NoSuchAttributeException nse) {
                val = nse.getMessage();  // shutld not happen anyway :-)
            }
            result.append(name);
            result.append(" , ");
            result.append(getLongImage(val));
        }
        result.append(" )");
        return result.toString();
    }

    /**
     * Call back the Visitor for the actual subclass.
     */
    @Override
	public void visitNode(QueryVisitor aVisitor)  throws QueryException {
        aVisitor.visitNode(this);
    }
    
    /** 
     * Accessor for the Example Object.
     */
    public DataObject getExample() {
        return example;
    }
}
