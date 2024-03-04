/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.reactive_tag;

import java.io.IOException;
import java.io.Writer;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.BodyTag;
import jakarta.servlet.jsp.tagext.BodyTagSupport;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.mig.html.layout.MainLayout;

/**
 * Base class for {@link BodyTag}s
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractBodyTag extends BodyTagSupport {

	private Writer _outBefore;
	
	/**
	 * The JSP local name of this tag handler for error reporting.
	 */
	protected abstract String getTagName();

	@Override
	public int doStartTag() throws JspException {
		setup();
		return super.doStartTag();
	}
	
	/**
	 * Work to be done before processing in {@link #doStartTag()}.
	 */
	protected void setup() {
		// Noting to set up, hook for subclasses.
	}

	@Override
	public int doEndTag() throws JspException {
		try {
			getOut().flushBuffer();
		} catch (IOException ex) {
			throw new JspException(ex);
		}
		tearDown();
		return super.doEndTag();
	}

	/**
	 * Cleanup work to be done after {@link #doEndTag()};
	 */
	protected void tearDown() {
		// Nothing to tear down, hook for subclasses.
	}

	@Override
	public void doInitBody() throws JspException {
		super.doInitBody();

		_outBefore = installTagWriterOut(pageContext.getOut());
	}

	@Override
	public int doAfterBody() throws JspException {
		int doAfterBody = super.doAfterBody();
		if (BodyTag.EVAL_BODY_AGAIN != doAfterBody) {
			resetTagWriter();
		}
		return doAfterBody;
	}

	private Writer installTagWriterOut(Writer newOut) {
		TagWriter out = getOut();
		return out.setOut(newOut);
	}

	/**
	 * The current {@link TagWriter} output.
	 */
	protected final TagWriter getOut() {
		return MainLayout.getTagWriter(pageContext);
	}

	private void resetTagWriter() {
		if (_outBefore != null) {
			installTagWriterOut(_outBefore);
			_outBefore = null;
		}
	}
}