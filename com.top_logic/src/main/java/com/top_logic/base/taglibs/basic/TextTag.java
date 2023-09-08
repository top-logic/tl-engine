/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.taglibs.basic;

import static com.top_logic.basic.xml.TagUtil.*;
import static com.top_logic.mig.html.HTMLConstants.*;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.util.Resources;

/**
 * {@link Tag} escaping a given text.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TextTag extends TagSupport {

	private String value;
	
	private String cssClass;
    
	/**
	 * Sets the text to output to the given plain value.
	 */
    public void setValue(String text) {
        this.value = text;
    }
    
	/**
	 * Sets the text to output to the given {@link ResKey} given as key string.
	 * 
	 * @deprecated Use {@link #setKey(ResKey)}
	 */
	@Deprecated
	public void setI18n(String resKey) {
		setKey(ResKey.internalJsp(resKey));
    }

	/**
	 * Sets the text to output to the given {@link ResKey}.
	 */
	public void setKey(ResKey key) {
		setValue(Resources.getInstance().getString(key));
	}

	/**
	 * Workaround for not being able to call a tag from within a HTML attribute.
	 * 
	 * @param jspKey
	 *        The key to translate.
	 * @return Encoded translation of the given key.
	 */
	@CalledFromJSP
	public static String attributeText(ResKey jspKey) {
		return TagUtil.encodeXMLAttribute(Resources.getInstance().getString(jspKey));
	}
	
	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	@Override
	public int doStartTag() throws JspException {
		if (value != null) {
			JspWriter out = pageContext.getOut();
			if (cssClass != null) {
				beginBeginTag(out, SPAN);
				writeAttribute(out, CLASS_ATTR, cssClass);
				endBeginTag(out);
			}

			writeText(out, value);

			if (cssClass != null) {
				endTag(out, SPAN);
			}
		}
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() throws JspException {
		this.value = null;
		this.cssClass = null;

		return EVAL_PAGE;
	}

}
