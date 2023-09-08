/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import com.top_logic.basic.Logger;
import com.top_logic.layout.form.tag.AbstractProxyTag;
import com.top_logic.mig.html.HTMLConstants;

/**
 * The {@link CompositeProxyTag} is a wrapping tag for a {@link Collection} of
 * {@link Tag}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CompositeProxyTag extends AbstractProxyTag {

	private final Collection	tags;

	/** Space between the {@link Tag}s. */
	private String				SPACE		= HTMLConstants.NBSP;

	/** write space between {@link Tag}s. */
	private boolean				writeSpace	= true;

	public CompositeProxyTag(Collection aTagCollection) {
		this.tags = aTagCollection;
	}

	/**
	 * This method sets whether {@link #SPACE} should be written between the
	 * {@link Tag}s or not.
	 */
	public void setSpace(boolean space) {
		this.writeSpace = space;
	}

	/**
	 * Just calls doStartTag(), doEndTag() of each {@link Tag} in {@link #tags}.
	 * If {@link #writeSpace} is <code>true</code>, then also {@link #SPACE}
	 * is written between the {@link Tag}s.
	 * 
	 * @see com.top_logic.layout.form.tag.AbstractProxyTag#doStartTag()
	 */
	@Override
	public int doStartTag() throws JspException {
		for (Iterator theIter = tags.iterator(); theIter.hasNext();) {
			Tag currentTag = (Tag) theIter.next();
			currentTag.doStartTag();
			currentTag.doEndTag();
			if (writeSpace && theIter.hasNext()) {
				try {
					getPageContext().getOut().write(SPACE);
				} catch (IOException ex) {
					Logger.warn("Unable to write spacer!", ex, this);
				}
			}
		}
		return SKIP_BODY;
	}

	/**
	 * Actually does nothing, but returns {@link Tag#EVAL_PAGE}.
	 * 
	 * @see com.top_logic.element.meta.form.tag.MetaInputTag#doEndTag()
	 */
	@Override
	public int doEndTag() throws JspException {
		return EVAL_PAGE;
	}

	/**
	 * Sets the {@link PageContext} for each {@link Tag} in {@link #tags}.
	 * 
	 * @see com.top_logic.layout.form.tag.AbstractProxyTag#setPageContext(javax.servlet.jsp.PageContext)
	 */
	@Override
	public void setPageContext(PageContext aPageContext) {
		for (Iterator theIter = tags.iterator(); theIter.hasNext();) {
			Tag currentTag = (Tag) theIter.next();
			currentTag.setPageContext(aPageContext);
		}
		super.setPageContext(aPageContext);
	}

	/**
	 * Sets the given parent to all {@link Tag}s in {@link #tags}.
	 * 
	 * @see com.top_logic.layout.form.tag.AbstractProxyTag#setParent(javax.servlet.jsp.tagext.Tag)
	 */
	@Override
	public void setParent(Tag aParent) {
		for (Iterator theIter = tags.iterator(); theIter.hasNext();) {
			Tag currentTag = (Tag) theIter.next();
			currentTag.setParent(aParent);
		}
		super.setParent(aParent);
	}

	@Override
	protected Tag createImplementation() {
		return null;
	}

}
