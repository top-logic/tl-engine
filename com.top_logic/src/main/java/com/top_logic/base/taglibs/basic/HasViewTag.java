/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.taglibs.basic;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Evaluates the tag body only if a referenced component view is defined and visible.
 * 
 * @see LayoutComponent#getConfiguredViews()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HasViewTag extends AbstractViewTag {

	@Override
	protected int startElement() throws JspException, IOException {
		HTMLFragment view = getView();
		if (view != null && isVisible(view)) {
			return EVAL_BODY_INCLUDE;
		} else {
			return SKIP_BODY;
		}
	}

}
