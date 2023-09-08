/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

import com.top_logic.layout.form.control.AbstractFormFieldControl;
import com.top_logic.layout.form.tag.util.IntAttribute;
import com.top_logic.layout.form.tag.util.StringAttribute;

/**
 * Abstract base class for all tags rendering a view of a form input field.
 * 
 * <p>
 * Subclasses must implement {@link #writeFormField()} to actually render the
 * view. Implementations of this tag are empty (they do not have descendant
 * views).
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractFormFieldControlTag extends AbstractFormMemberControlTag {

	/** The <code>onChange</code> attribute. */
	public final StringAttribute onchange = new StringAttribute();

	/** The <code>tabindex</code> attribute. */
	public final IntAttribute tabindex = new IntAttribute();

	/**
	 * JSP tag setter for {@link #onchange}.
	 */
	public void setOnchange(String value) {
		this.onchange.set(value);
	}

	/**
	 * JSP tag setter for {@link #tabindex}.
	 */
	public void setTabindex(String value) {
		this.tabindex.set(value);
	}

	/**
	 * Forwards processing to {@link #writeFormField()}.
	 * 
	 * @return Always {@link Tag#SKIP_BODY}.
	 * 
	 * @see com.top_logic.layout.form.tag.AbstractFormTag#startFormMember()
	 */
	@Override
	protected int startFormMember() throws IOException, JspException {
		writeFormField();
		return SKIP_BODY;
	}

	/**
	 * Always {@link Tag#EVAL_PAGE}.
	 * 
	 * @see com.top_logic.layout.form.tag.AbstractFormTag#endFormMember()
	 */
	@Override
	protected int endFormMember() throws IOException, JspException {
		return EVAL_PAGE;
	}

	/**
	 * Render the complete view.
	 */
	protected void writeFormField() throws IOException, JspException {
		ControlTagUtil.writeControl(this, this.pageContext, getControl());
	}

	protected void initControl(AbstractFormFieldControl control) {
		if (tabindex.isSet())
			control.setTabIndex(tabindex.get());

		if (style.isSet())
			control.setInputStyle(style.get());
	}

	/**
	 * Render presentation attributes of this view's input element.
	 */
	protected void writePresentationAttributes() throws IOException {
		if (tabindex.isSet()) {
			writeAttribute(TABINDEX_ATTR, tabindex.getAsString());
		}
	}
	
	@Override
	protected void teardown() {
		super.teardown();
		
		this.onchange.reset();
		this.tabindex.reset();
	}
	
}
