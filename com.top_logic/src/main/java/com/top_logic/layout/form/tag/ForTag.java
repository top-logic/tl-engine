/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import java.util.ArrayList;
import java.util.StringTokenizer;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.IterationTag;

import com.top_logic.layout.form.tag.util.ShadowedAttribute;

/**
 * Tag for iterating over a given list of strings.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ForTag extends AbstractTag implements IterationTag {
	String varName;
	ArrayList values;

	final ShadowedAttribute shadow = new ShadowedAttribute(); 
	
	int index = 0;
	
	public void setVar(String varName) {
		this.varName = varName;
	}

	public void setIn(String valuesString) {
		values = new ArrayList();
		
		StringTokenizer tokens = new StringTokenizer(valuesString);
		while (tokens.hasMoreTokens()) {
			values.add(tokens.nextToken());
		}
	}
	
	@Override
	public int startElement() throws JspException {
		if (index < values.size()) {
			shadow.backupShadowedAttribute(pageContext, varName);
			pageContext.setAttribute(varName, values.get(index));
			
			return EVAL_BODY_INCLUDE;
		} else {
			return SKIP_BODY;
		}
	}

	@Override
	public int doAfterBody() throws JspException {
		index++;
		
		if (index < values.size()) {
			pageContext.setAttribute(varName, values.get(index));
			return EVAL_BODY_AGAIN;
		} else {
			pageContext.removeAttribute(varName);
			return EVAL_PAGE;
		}
	}
	
	@Override
	public int endElement() throws JspException {
		shadow.restoreShadowedAttribute(pageContext, varName);

		reset();
		return EVAL_PAGE;
	}

	private void reset() {
		index = 0;
		values = null;
	}
}
