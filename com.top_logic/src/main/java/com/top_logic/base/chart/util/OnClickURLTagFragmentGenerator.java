/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.util;

import static com.top_logic.mig.html.HTMLConstants.*;

import org.jfree.chart.imagemap.URLTagFragmentGenerator;

import com.top_logic.basic.xml.TagUtil;

/**
 * The OnClickURLTagFragmentGenerator generates URLs using the onClick attribute 
 * for image map area tags.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class OnClickURLTagFragmentGenerator extends Object implements
        URLTagFragmentGenerator {

    /** The single instance of this class. */
    public static final OnClickURLTagFragmentGenerator INSTANCE = new OnClickURLTagFragmentGenerator();
    
    private OnClickURLTagFragmentGenerator() {
        // Use the single instance of this class
    }
    
    /** 
     * This method returns a URL link fragment (onclick-attribute).
     * 
     * @see org.jfree.chart.imagemap.URLTagFragmentGenerator#generateURLFragment(java.lang.String)
     */
    @Override
	public String generateURLFragment(String anOnClickText) {
		StringBuilder out = new StringBuilder();
        // The onclick-attribute only is not enough. Please, the href-attribute
        // is necessary (e.g. netscape browser), do not remove it.
		TagUtil.writeAttribute(out, HREF_ATTR, '#');
		
		TagUtil.beginAttribute(out, ONCLICK_ATTR);
		TagUtil.writeAttributeText(out, anOnClickText);
		TagUtil.writeAttributeText(out, "; return false;");
		TagUtil.endAttribute(out);

		return out.toString();
    }

}
