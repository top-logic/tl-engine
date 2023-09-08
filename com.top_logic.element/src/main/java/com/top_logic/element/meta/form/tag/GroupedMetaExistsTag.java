/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Show inner content only in case an attribute exists.
 * 
 * @author <a href=mailto:klaus.halfmann@top-logic.com>Klaus Halfmann</a>
 */
public class GroupedMetaExistsTag extends TagSupport {

    /** Name of attribute to be checked. */
    private String name;
    
    /** Flag, if attribute has to exist or not. */
    private boolean not;

    /**
     * @see com.top_logic.layout.form.tag.AbstractProxyTag#doStartTag()
     */
    @Override
	public int doStartTag() throws JspException {
        boolean theResult = GroupedMetaInputTag.hasAttributeUpdate(this, this.name);

        if (theResult) {
            return this.not ? SKIP_BODY : EVAL_BODY_INCLUDE;
        }
        else {
            return this.not ? EVAL_BODY_INCLUDE : SKIP_BODY;
        }
    }

    /**
     * Override as this has no impl.
     */
    @Override
	public int doEndTag() throws JspException {
        return EVAL_PAGE;
    }

    public String getName() {
        return this.name;
    }

    /**
	 * Set the Name of the attribute to use.
	 */
    public void setName(String aName) {
        this.name = aName;
    }

    /**
     * This method sets the not.
     *
     * @param    not    The not to set.
     */
    public void setNot(boolean not) {
        this.not = not;
        
    }

    /**
     * This method returns the not.
     * 
     * @return    Returns the not.
     */
    public boolean getNot() {
        return this.not;
    }
}

