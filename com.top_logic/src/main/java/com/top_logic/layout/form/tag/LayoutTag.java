/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import javax.servlet.jsp.tagext.Tag;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Common interface for all JSP tag implementations in the layout framework.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface LayoutTag extends Tag {
	
    public LayoutComponent getComponent();

}
