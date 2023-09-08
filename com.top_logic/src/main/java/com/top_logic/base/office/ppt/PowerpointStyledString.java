/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.ppt;

import java.io.Serializable;
import java.util.Properties;

import com.top_logic.basic.StringServices;

/**
 * Wrapper for a string to be used with a specific style.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class PowerpointStyledString implements Serializable {

    /** Comment for <code>serialVersionUID</code>. */
    private static final long serialVersionUID = 4938550246166505652L;

    /** the font family is e.g. arial, courier, times roman,... */
    public static final String FONT_FAMILY  = "font-family";
    
    /** the font size is in px e.g. 18px */
    public static final String FONT_SIZE    = "font-size";
    
    /** the font style is either normal or italic */
    public static final String FONT_STYLE   = "font-style";
    
    /** the font weight is either normal or bold */
    public static final String FONT_WEIGHT  = "font-weight";
    
    /** one of the font styles we support for now */
    public static final String ITALIC   = "italic";

    /** one of the font styles we support for now */
    public static final String NORMAL   = "normal";

    /** The wrapped string. */
    private String string;

    /** The original style description. */
    protected String styleText;

    protected Properties styleProperties;

    /**
     * @param    aString    The string to be wrapped, must not be <code>null</code>.
     * @param    aStyle     The style of the string, may be <code>null</code> or empty.
     * @throws   IllegalArgumentException    If the given string is <code>null</code>.
     */
    public PowerpointStyledString(String aString, String aStyle) {
        if (aString == null) {
            throw new IllegalArgumentException("String is null");
        }

        this.string    = aString;
        this.styleText = aStyle;
        initStyleProperties ();
    }

    /**
     * Return a debugging string representation of this instance.
     * 
     * @return    The string representation of this instance.
     */
    @Override
	public String toString() {
        return (this.string);
    }

    
    /**
     * convert the style text (if available) into Properties for the Font style
     */
    protected void initStyleProperties () {
        if (!StringServices.isEmpty(styleText)) {
            styleProperties = new Properties ();
            String[] propRows = styleText.split(";");
			{
                for (int i=0; i < propRows.length;i++) {
                    if (!StringServices.isEmpty(propRows[i])) {
                        String[] currentRow = propRows[i].split(":");
                        if (currentRow.length == 2) {
                            styleProperties.setProperty(currentRow[0],currentRow[1]);
                        }
                    }
                }
            }
        }
    }

    
}
