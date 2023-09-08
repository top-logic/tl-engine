/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.office.basic;

import java.util.Iterator;

import com.top_logic.base.office.ppt.PowerpointStyledString;
import com.top_logic.reporting.office.ExpansionObject;


/**
 * This reader/writer differs from the basic reader/writer
 * in that respect, that this class is able to find multiple symbols
 * in one field of the template. This is only possible for String based symbols
 * 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class MultiSymbolPPTReaderWriter extends BasicPPTReaderWriter {

    
    public MultiSymbolPPTReaderWriter() {
        symbolParser = new MultiSymbolParser();
    }

    /**
	 * We evaluate the expanded object and return the expanded. In case the expanded object is a
	 * string there is special handling for replacement. We replace only the part of the field
	 * content representing the symbol, e.g.:
	 * <code>'&lt;exp type=static>NAME&lt;/exp> was here'</code> would convert to
	 * <code>'Kilroy was here'</code>
	 * 
	 * @param anObject
	 *        the ExpansionObject to get the information from.
	 * @return the String representation of the expanded object or the old field value.
	 */
    @Override
	protected Object replaceSymbolWithValue (ExpansionObject anObject) {
        if (anObject.isExpanded()) {
            Object expanded = anObject.getExpandedObject();
            if (expanded instanceof String) {
                // we use the PowerpointStyledString class to propagate the expanded content.
                return new PowerpointStyledString (expandStringTypedObject(anObject),anObject.getContentStyle());
            } else {
                return expanded;
            }
        } else {
            return anObject.getFieldContent();
        }
    }
    
    private String expandStringTypedObject (ExpansionObject anObject) {
        String theContentToWrite = anObject.getFieldContent();
        String theSymbol = anObject.getSymbol();
        Object theExpanded = anObject.getExpandedObject();
        if (theExpanded instanceof String) {
            theContentToWrite = replaceSymbolInText(theContentToWrite,theSymbol,(String)theExpanded);
        }
        // if we have multiple symbols in the same field we use the stacked version to replace all of them
        if (anObject instanceof StackedExpansionObject) {
            StackedExpansionObject stacked = (StackedExpansionObject) anObject;
            if (stacked.hasInnerExpansionObjects()) {
                Iterator iter = stacked.getInnerExpansionObjects().iterator();
                while (iter.hasNext()) {
                    ExpansionObject current = (ExpansionObject) iter.next();
                    theSymbol = current.getSymbol();
                    // we assume that alle parts are of string type!
                    theExpanded = current.getExpandedObject();
                    if (theExpanded instanceof String) {
                        theContentToWrite = replaceSymbolInText(theContentToWrite,theSymbol,(String) theExpanded);
                    }
                }
            }
        }
        return theContentToWrite;
    }
    

}
