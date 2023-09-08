/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.gui;

import org.xml.sax.Attributes;

import com.top_logic.basic.Logger;
import com.top_logic.basic.xml.XMLAttributeHelper;

/**
 * Wrapper around Attributes that will map values via Theme.
 * 
 * This is a glue-class used while importing Layouts.
 */
public class ThemeBasedAttributes implements Attributes {
    
    Attributes att;
    Theme      theme;
    
    public ThemeBasedAttributes(Attributes someAttr, Theme aTheme) {
        this.att   = someAttr;
        this.theme = aTheme;
    }

    /** delegate and replace */
    @Override
	public String getValue(int index) {
        return this.replace(att.getValue(index));
    }

    /** delegate and replace */
    @Override
	public String getValue(String qName) {
        return this.replace(XMLAttributeHelper.getAsStringOptional(att, qName));
    }

    /** delegate and replace */
    @Override
	public String getValue(String uri, String localName) {
        return this.replace(att.getValue(uri, localName));
    }
    
    /** replace via {@link #theme} if a value can be found */ 
    private String replace(String aValue) {
        if (aValue != null) {
			if (Theme.isThemeValueReference(aValue)) {
				String themeValue = this.theme.getRawValue(Theme.var(aValue));
				if (themeValue == null) {
					Logger.error(
						"Missing theme variable in theme '" + theme.getName() + "': '" + aValue + "'.",
						ThemeBasedAttributes.class);
					return null;
        		}

				return themeValue;
        	}
        }
        return aValue;
    }

	// delegate to inner attribute
    @Override
	public int getLength() {
        return att.getLength();
    }
    @Override
	public String getLocalName(int index) {
        return att.getLocalName(index);
    }
    @Override
	public String getQName(int index) {
        return att.getQName(index);
    }
    @Override
	public String getType(int index) {
        return att.getType(index);
    }
    @Override
	public String getURI(int index) {
        return att.getURI(index);
    }
    @Override
	public int getIndex(String qName) {
        return att.getIndex(qName);
    }
    @Override
	public String getType(String qName) {
        return att.getType(qName);
    }
    @Override
	public int getIndex(String uri, String localName) {
        return att.getIndex(uri, localName);
    }
    @Override
	public String getType(String uri, String localName) {
        return att.getType(uri, localName);
    }
}