/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.importer.node.parser;

import java.util.HashMap;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.top_logic.reporting.report.importer.AbstractXMLImporter;
import com.top_logic.reporting.report.importer.node.NodeParser;
import com.top_logic.reporting.report.model.Report;
import com.top_logic.reporting.report.model.ReportConfiguration;
import com.top_logic.reporting.report.xmlutilities.ReportReader;

/**
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 * @deprecated use {@link ReportReader} and {@link ReportConfiguration}
 */
@Deprecated
public class I18NParser extends AbstractXMLImporter implements NodeParser {

    /** The node name which this parser can handle. */
    public static final String TYPE = "i18n";

    /**
     * Creates a {@link I18NParser}. 
     */
    public I18NParser() {
        // Do nothing.
    }
    
    @Override
	public String toString() {
        return getClass() + "[node name=" + TYPE + "]";
    }
    
    // Node parser methods
    
    @Override
	public Object parse(Node aI18NNode, Report aReport) {
        HashMap  theMessages  = new HashMap();
        NodeList theLanguages = aI18NNode.getChildNodes();
        for (int i = 0; i < theLanguages.getLength(); i++ ) {
            Node theNode = theLanguages.item(i);
            
            if (!(theNode.getNodeType() == Node.ELEMENT_NODE)) { continue; }
            
            String theLanguage = theNode.getNodeName();
            String theMessage  = getDataAsString(theNode);
            theMessages.put(theLanguage, theMessage);
        }
        
        return theMessages;
    }

}

