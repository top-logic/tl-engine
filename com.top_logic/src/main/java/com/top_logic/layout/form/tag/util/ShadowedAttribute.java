/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag.util;

import jakarta.servlet.jsp.PageContext;

/**
 * TODO BHU: Work in progress. Stop reading here.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ShadowedAttribute {

	Object shadowedAttributeValue; 
	
	/** Saves the current value of the given page attribute. */
	public void backupShadowedAttribute(PageContext pageContext, String varName) {
		shadowedAttributeValue = pageContext.getAttribute(varName);
	}

	/**
	 * Restores the page attribute saved in {@link #backupShadowedAttribute(PageContext, String)}.
	 */
	public void restoreShadowedAttribute(PageContext pageContext, String varName) {
		if (shadowedAttributeValue != null) {
			pageContext.setAttribute(varName, shadowedAttributeValue);
			shadowedAttributeValue = null;
		}
	}
	
	/** Forgets the saved value. */
	public void reset() {
		this.shadowedAttributeValue = null;
	}

}
