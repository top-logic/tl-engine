/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.tag;

import java.io.IOException;
import java.io.Writer;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.BodyTagSupport;
import jakarta.servlet.jsp.tagext.Tag;
import jakarta.servlet.jsp.tagext.TagSupport;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.mig.html.layout.MainLayout;

/**
 * Base class for {@link BoxTag}s
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractBoxTag extends BodyTagSupport implements BoxTag {

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
		tearDown();
		try {
			getOut().flushBuffer();
		} catch (IOException ex) {
			throw new JspException(ex);
		}
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
		resetTagWriter();

		return super.doAfterBody();
	}

	private Writer installTagWriterOut(Writer newOut) {
		return getOut().setOut(newOut);
	}

	/**
	 * The current {@link TagWriter} output.
	 */
	protected final TagWriter getOut() {
		return MainLayout.getTagWriter(pageContext);
	}

	/**
	 * The {@link BoxContainerTag} ancestor of this tag.
	 * 
	 * @throws IllegalStateException
	 *         If this tag has no {@link BoxContainerTag} ancestor.
	 */
	protected BoxContainerTag getBoxContainerTag() throws IllegalStateException {
		Tag parent = TagSupport.findAncestorWithClass(this, BoxContainerTag.class);
		if (parent == null) {
			throw new IllegalStateException(
				"A " + getTagName() + " tag must be nested within a box container tag (" + VerticalLayoutTag.VERTICAL_LAYOUT_TAG + ", "
					+ HorizontalLayoutTag.HORIZONTAL_LAYOUT_TAG + ", or " + CellTag.CELL_TAG + ").");
		}
		return (BoxContainerTag) parent;
	}

	private void resetTagWriter() {
		if (_outBefore != null) {
			installTagWriterOut(_outBefore);
			_outBefore = null;
		}
	}

}
