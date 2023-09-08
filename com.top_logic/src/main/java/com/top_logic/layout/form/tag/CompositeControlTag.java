/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.Tag;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.layout.form.control.AbstractCompositeControl;
import com.top_logic.layout.form.control.PlainCompositeRenderer;
import com.top_logic.mig.html.HTMLConstants;

/**
 * {@link Tag} building a {@link AbstractCompositeControl} from its body contents.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class CompositeControlTag extends AbstractControlTag implements BodyTag, ControlBodyTag {

	private String localName = null;
	private String className = null;
	
	private AbstractCompositeControl<?> compositeControl;
	private BodyContent bodyContent;

	/**
	 * The tag name to use for the created control.
	 * 
	 * <p>
	 * By default, <code>span</code> is used.
	 * </p>
	 */
	public String getLocalName() {
		return localName;
	}

	/**
	 * @see #getLocalName()
	 */
	public void setLocalName(String localName) {
		this.localName = localName;
	}
	
	/**
	 * The contents of the CSS <code>class</code> attribute to write.
	 */
	public String getCssClass() {
		return className;
	}

	/**
	 * @see #getCssClass()
	 */
	public void setCssClass(String className) {
		this.className = className;
	}
	
	protected void initCompositeControl(AbstractCompositeControl<?> result) {
	}

	@Override
	protected int startElement() throws JspException, IOException {
		this.compositeControl = (AbstractCompositeControl<?>) getExistingControl();
		if (this.compositeControl == null) {
			// Fetch the layout from the JSP.
			return EVAL_BODY_BUFFERED;
		} else {
			// Render buffered contents in endElement.
			return SKIP_BODY;
		}
	}

	@Override
	public void setBodyContent(BodyContent content) {
		this.bodyContent = content;
	}

	@Override
	public void doInitBody() throws JspException {
		// Create control.
		this.compositeControl = (AbstractCompositeControl<?>) getControl();
		installCorrectTagWriter();
	}

	@Override
	public String addControl(HTMLFragment childControl) {
		compositeControl.addChild(childControl);
		
		// Enable creation of a composite renderer that replaces placeholders
		// with child controls.
		return PlainCompositeRenderer.PLACEHOLDER;
	}

	protected void renderContents() throws IOException {
		ControlTagUtil.writeControl(this, pageContext, compositeControl);
	}

	@Override
	protected int endElement() throws IOException, JspException {
		installCorrectTagWriter();
		try {
			if (bodyContent != null) {
				// Initialize the composite control with the layout string created
				// during buffered rendering.
				this.compositeControl.setRenderer(
					new PlainCompositeRenderer(
								localName, 
								className != null ? Collections.singletonMap(HTMLConstants.CLASS_ATTR, className) : null,
										bodyContent.getString()));
			}
			// Render the newly created control.
			renderContents();
			return EVAL_PAGE;
		} catch (IOException e) {
			throw new JspException(e);
		}
	}
	
	@Override
	protected void teardown() {
		super.teardown();
		
		this.localName = null;
		this.className = null;
		
		this.compositeControl = null;
		this.bodyContent = null;
	}

}
