/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.office;

import java.util.regex.Matcher;


/**
 * A helper class to parse a given String (or StringBuffer) for symbols.
 * The parser creates ExpansionObjects in the symbol state (i.e. not expanded).
 * 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public abstract class SymbolParser {
    
    /**
     * Parse the given text for the symbol pattern and create a expansion object. 
     * @param fieldID       the id as provided by the importer
     * @param fieldContent  the content of the field to be parsed.
     * @return an expansion object in symbol state or <code>null</code> is no symbol was found.
     */
    public ExpansionObject parse (String fieldID, String fieldContent) {
        
        ExpansionObject result = null;
        Matcher theMatcher = findMatch (fieldContent);
        
        if (theMatcher != null) {
            result = new ExpansionObject (fieldID);
            result.setFieldContent(fieldContent);
            result.setSymbol(theMatcher.group(0));
            transferGroups (theMatcher, result);
        }
        
        return result;    
    }
    
    /**
     * Depending on the strategy we transfer the found groups to the fields in the expansion object
     */
    protected abstract void transferGroups (Matcher aMatcher, ExpansionObject anObject);
    
    /** 
     * find a match in the given text
     * 
     * @param aText the text to find the pattern in
     * @return a {@link Matcher} with the result of the matching or <code>null</code> if no match was found.
     */
    protected abstract Matcher findMatch (String aText);    
}
