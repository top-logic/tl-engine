/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.office.basic;

import java.util.regex.Matcher;

import com.top_logic.reporting.office.ExpansionObject;


/**
 * This special class is able to extract multiple symbols from the 
 * field content.
 * It generates a chain of {@link ExpansionObject}s iterating over the symbols in the 
 * field content. This class corresponds to a special {@link StackedExpansionObject}.
 * 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class MultiSymbolParser extends BasicSymbolParser {

    
    public MultiSymbolParser() {
        super();
    }

    /**
     * @see com.top_logic.reporting.office.SymbolParser#parse(java.lang.String, java.lang.String)
     */
    @Override
	public ExpansionObject parse(String fieldID, String fieldContent) {       
        StackedExpansionObject result = null;
        
        // first find the symbols for the simple expression:
        Matcher theMatcher = findMatches (fieldContent,false);
        if (theMatcher != null) {
            while (theMatcher.find()) {
                result = addMatchResults(fieldID,fieldContent,result,theMatcher);
            }
        }
        
//        //now for the second pass:
//        theMatcher = findMatches(fieldContent,true);
//        if (theMatcher != null) {
//            while (theMatcher.find()) {
//                result = addMatchResults(fieldID,fieldContent,result,theMatcher);
//            }
//        }
        
        return result;    
    }
    
    /**
     * Creates a {@link StackedExpansionObject} for the symbol found by the given match.
     *
     * @param aFieldID
     *         The key of the field the symbol was found in.
     * @param aFieldContent
     *         The content of the field the symbol was found in.
     * @param theParent
     *         The object to add the new object to as inner object, or <code>null</code> for
     *         creating the top-level object.
     * @param aMatch
     *         The match of the symbol.
     * @return The given parent, or the newly created object, if no parent was given.
     */
    protected StackedExpansionObject addMatchResults (String aFieldID, String aFieldContent, StackedExpansionObject theParent, Matcher aMatch) {
        StackedExpansionObject newExpansionObject = new StackedExpansionObject(aFieldID);

        newExpansionObject.setSymbol(aMatch.group(0));
        transferGroups (aMatch, newExpansionObject);
        
        if (theParent != null) {
            theParent.addExpansionObject(newExpansionObject);
            return theParent;
        } else {
            newExpansionObject.setFieldContent(aFieldContent);
            return newExpansionObject;
        }
    }
    /**
     * @see com.top_logic.reporting.office.basic.BasicSymbolParser#findMatch(java.lang.String)
     */
    protected Matcher findMatches(String aText, boolean styledPattern) {
        //Pattern thePattern = styledPattern ? expStylePattern : expPattern;
        Matcher matcher = hybridPattern.matcher(aText);
        if (matcher != null) {
            return matcher;
        }
        return null;
    }
}
