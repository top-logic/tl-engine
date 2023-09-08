/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;

/**
 * The MapBasedXMLAttributes provide an {@link Attributes} view to a map
 *
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class MapBasedXMLAttributes implements Attributes {

    /** The original content this was cerated from */
    protected Map  content;
    
    /** A list to provide some Order to the keys (as required by {@link Attributes}. */
    protected List keyList;

    /**
     * Creates a {@link MapBasedXMLAttributes}.
     * 
     * Tip use a {@link LinkedHashMap} to keep the Attributes in Order.
     *
     * @param someContent Map < String , String >, never <code>null</code>
     */
    public MapBasedXMLAttributes(Map someContent) {
        this.content = someContent;
        this.keyList = new ArrayList(someContent.keySet());
    }

    /**
     * @see org.xml.sax.Attributes#getIndex(java.lang.String)
     */
    @Override
	public int getIndex(String aKey) {
        return keyList.indexOf(aKey);
    }

    /**
     * @see org.xml.sax.Attributes#getIndex(java.lang.String, java.lang.String)
     */
    @Override
	public int getIndex(String aUri, String aLocalName) {
        return getIndex(aLocalName);
    }

    /**
     * @see org.xml.sax.Attributes#getLength()
     */
    @Override
	public int getLength() {
        return this.content.size();
    }

    /**
     * @see org.xml.sax.Attributes#getLocalName(int)
     */
    @Override
	public String getLocalName(int anIndex) {
        if (anIndex >= 0 && anIndex < keyList.size()) {
            return (String) this.keyList.get(anIndex);
        }
        return null; 
    }

    /**
     * @see org.xml.sax.Attributes#getQName(int)
     */
    @Override
	public String getQName(int anIndex) {
        if (anIndex >= 0 && anIndex < keyList.size()) {
            return ""; // now real QName here
        }
        return null; 
    }

    /**
     * @see org.xml.sax.Attributes#getType(int)
     */
    @Override
	public String getType(int anIndex) {
        return "CDATA";
    }

    /**
     * @see org.xml.sax.Attributes#getType(java.lang.String)
     */
    @Override
	public String getType(String aName) {
        return "CDATA";
    }

    /**
     * @see org.xml.sax.Attributes#getType(java.lang.String, java.lang.String)
     */
    @Override
	public String getType(String aUri, String aLocalName) {
        return "CDATA";
    }

    /**
     * @see org.xml.sax.Attributes#getURI(int)
     */
    @Override
	public String getURI(int anIndex) {
        return (anIndex >= 0 && anIndex < this.keyList.size()) ? "" : null;
    }

    /**
     * @see org.xml.sax.Attributes#getValue(int)
     */
    @Override
	public String getValue(int anIndex) {
        return (String) ((anIndex >= 0 && anIndex < this.keyList.size()) 
            ? this.content.get(this.keyList.get(anIndex)) : null);
    }

    /**
     * @see org.xml.sax.Attributes#getValue(java.lang.String)
     */
    @Override
	public String getValue(String aName) {
        return (String) this.content.get(aName);
    }

    /**
     * @see org.xml.sax.Attributes#getValue(java.lang.String, java.lang.String)
     */
    @Override
	public String getValue(String aUri, String aLocalName) {
        return (String) this.content.get(aLocalName);
    }

}

