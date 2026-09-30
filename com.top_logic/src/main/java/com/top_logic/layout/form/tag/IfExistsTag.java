/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import java.io.IOException;

import jakarta.servlet.jsp.tagext.Tag;

import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.tag.util.ExpressionSyntax;

/**
 * {@link Tag} for displaying parts of a form conditionally.
 * 
 * <p>
 * The visibility of the body of this tag depends on the existance of a
 * {@link FormMember} with a given name in the current context ({@link FormContainer}).
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IfExistsTag extends AbstractFormTag {
	
    private String memberName;
	
	/** Flag, if member has to exist or not. */
    private boolean not;
	
	public void setName(String memberName) {
		assert this.memberName == null;
		
		this.memberName = ExpressionSyntax.expandVariables(pageContext, memberName);
	}

	@Override
	protected int startFormMember() throws IOException {
		boolean theResult = this.getParentFormContainer().hasMember(memberName);

        if (theResult) {
            return this.not ? SKIP_BODY : EVAL_BODY_INCLUDE;
        }
        else {
            return this.not ? EVAL_BODY_INCLUDE : SKIP_BODY;
        }
		
	}

	@Override
	protected int endFormMember() throws IOException {
		return EVAL_PAGE;
	}

	@Override
	protected void teardown() {
		super.teardown();
		this.memberName = null;
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
