/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.importer.entry.parser;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.top_logic.reporting.report.importer.entry.Entry;
import com.top_logic.reporting.report.importer.entry.EntryParser;

/**
 * The AbstractEntryParser parses the key and value attribute from the entry. 
 * The date of the value attribute is parsed by the subclasses with the 
 * method {@link #getValue(String)}.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
@Deprecated
public abstract class AbstractEntryParser implements EntryParser {

    /**
     * Returns the parsed object (e.g. 'Boolean').
     * 
     * @param aValueAsString
     *        The value data as string (e.g. '01.01.2005').
     */
    protected abstract Object getValue(String aValueAsString);

    @Override
	public Entry parse(Node aEntryNode) {
        String theKey   = null;
        Object theValue = null; 
        
        NamedNodeMap theAttributes = aEntryNode.getAttributes();
        for (int i = 0; i < theAttributes.getLength(); i++) {
            Node theNode = theAttributes.item(i);
            
            // Set the key
            if (theNode.getNodeName().equalsIgnoreCase(KEY_ATTR)) {
                theKey = theNode.getNodeValue();
                continue;
            }
            
            // Set the value
            if (theNode.getNodeName().equalsIgnoreCase(VALUE_ATTR)) {
                theValue = getValue(theNode.getNodeValue());
                continue;
            }
        }
        
        return new Entry(theKey, theValue);
    }

}

