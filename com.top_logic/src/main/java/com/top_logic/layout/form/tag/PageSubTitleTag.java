/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;

/**
 * Sub tag of {@link PageAreaTag} specifying the subtitle content as JSP content.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PageSubTitleTag extends PageTagBase {

	@Override
	protected int startElement() throws JspException, IOException {
		return pageTag().beginSubTitle();
	}

	@Override
	protected int endElement() throws IOException, JspException {
		pageTag().endSubTitle();
		return EVAL_PAGE;
	}

}
