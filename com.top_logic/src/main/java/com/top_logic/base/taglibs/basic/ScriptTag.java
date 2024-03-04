/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.taglibs.basic;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;

import com.top_logic.layout.basic.fragments.Tag;

/**
 * {@link Tag} to render a single JavaScript <code>script</code> tag.
 * 
 * <p>
 * A JSP must not directly include the default CDATA decoration, since such content cannot be
 * transported in an AJAX update, since CDATA sections must be quoted. Quoting during JSP writing is
 * currently not done for plain JSP content.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ScriptTag extends AbstractTagBase {

	@Override
	protected int startElement() throws JspException, IOException {
		getOut().beginScript();
		return EVAL_BODY_INCLUDE;
	}

	@Override
	protected int endElement() throws IOException, JspException {
		getOut().endScript();
		return EVAL_PAGE;
	}

}
