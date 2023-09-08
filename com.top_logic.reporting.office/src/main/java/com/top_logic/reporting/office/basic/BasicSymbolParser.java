/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.office.basic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.StringServices;
import com.top_logic.reporting.office.ExpansionObject;
import com.top_logic.reporting.office.SymbolParser;


/**
 * Basic implementation of a symbol parser.
 * This parser assumes that the symbols follow the following syntax:
 * 
 * <code>
 *  &lt;exp type=[static|script]&gt;[symbol content]&lt;/exp&gt;
 * </code>
 * 
 * TODO JCO extend with new type picture where width and breath can be entered in the style!
 * 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class BasicSymbolParser extends SymbolParser {

   
    public BasicSymbolParser () {
        super();
    }

    protected static Pattern expPattern      = Pattern.compile ("<exp type=([^>^=]*)>([^<]*)</exp>");
    protected static Pattern expStylePattern = Pattern.compile ("<exp type=([^>]*) style=([^>]*)>([^<]*)</exp>");

    protected static  Pattern hybridPattern = Pattern.compile ("<exp type=([^\\s]*|[^>]*)(|\\sstyle=([^>]*))>([^<]*)</exp>");
    

    /**
     * @see com.top_logic.reporting.office.SymbolParser#findMatch(java.lang.String)
     */
    @Override
	protected Matcher findMatch(String aText) {
        Matcher matcher = hybridPattern.matcher(aText);
        //Matcher matcher = expStylePattern.matcher(aText);
        if (matcher != null && matcher.find()) {
            return matcher;
        }
        return null;
//        else {
//            matcher = expPattern.matcher(aText);
//            return (matcher != null && matcher.find()) ? matcher : null;
//        }
    }

    /**
     *   according to the used pattern (hybridPattern) the groupcount must deliver 4 groups:
     *   1: type ; 2: [ignore] ; 3: null or style ; 4: content
     * 
     * @see com.top_logic.reporting.office.SymbolParser#transferGroups(java.util.regex.Matcher, com.top_logic.reporting.office.ExpansionObject)
     */
    @Override
	protected void transferGroups(Matcher aMatcher, ExpansionObject anObject) {
        int groups = aMatcher.groupCount();
        
        if (groups != 4) throw new IllegalArgumentException ("unknown pattern matched, we expect excatly 4 groups!");
                
        // the the type of the symbol (either script or static or image
        anObject.setSymbolType(aMatcher.group(1));
        
        // if there is an entry style= we have a group 3 not null.
        if (!StringServices.isEmpty(aMatcher.group(3))) {
            anObject.setContentStyle(aMatcher.group(3));
        }
        // the content of the symbol is always in the last group
        anObject.setSymbolContent(aMatcher.group(4));
    }
    
    
    
}
