/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.taglibs;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Tag that executes its body if the given condition is true.
 * 
 * @author     <a href="mailto:sko@top-logic.com">sko</a>
 */
public class IfTag extends TagSupport {

    /** the condition to be checked */
    private boolean condition;

    /**
     * Convenience method to get the condition value. 
     */
    public boolean isCondition() {
        return (condition);
    }

    /**
     * This method sets the condition 
     */
    public void setCondition(boolean condition) {
        this.condition = condition;
    }

    /**
     * If the condition is <code>true</code>, the body of the tag will be executed. 
     * 
     * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
     */
    @Override
	public int doStartTag() throws JspException {
        if(isCondition()) {
            return EVAL_BODY_INCLUDE;
        }else {
            return SKIP_BODY;
        }
    }
    
    /**
     * Sets the condition to <code>false</code> to ensure reusability and keeps executing the page.
     * 
     * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
     */
    @Override
	public int doEndTag() throws JspException {
        setCondition(false);
        return EVAL_PAGE;
    }
}
