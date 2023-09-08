/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.top_logic.basic.Logger;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class NodeHelper {

    public static String getAsString(NamedNodeMap someAttr, String anAttributeName) {
        Node theNode = someAttr.getNamedItem(anAttributeName);
        if (theNode != null) {
            return theNode.getNodeValue();
        }
        
        throw new IllegalArgumentException("attribute '" + anAttributeName + "' must not be null");
    }
    
    public static String getAsString(NamedNodeMap someAttr, String anAttributeName, String aDefault) {
        Node theNode = someAttr.getNamedItem(anAttributeName);
        if (theNode != null) {
            return theNode.getNodeValue();
        }
        
        return aDefault;
    }
    
    public static Object getAsInstanceOfClass(NamedNodeMap someAttr, String anAttributeName) {
        Node theNode = someAttr.getNamedItem(anAttributeName);
        if (theNode != null) {
            String theClassname = theNode.getNodeValue();
            try {
            
                Class theClazz = Class.forName(theClassname);
                return theClazz.newInstance();
            } catch (Exception ex) {
                Logger.error("Unable to get instance of class " + theClassname +" for attribute "+anAttributeName, ex, NodeHelper.class);
                throw new IllegalArgumentException("Unable to get instance of class " + theClassname +" for attribute "+anAttributeName);
            }
        }
        
        throw new IllegalArgumentException("attribute '" + anAttributeName + "' must not be null");
    }
    
}

