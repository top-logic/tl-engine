/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;


import javax.servlet.jsp.JspException;

import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.tag.util.ExpressionSyntax;
import com.top_logic.layout.form.tag.util.StringAttribute;

/**
 * Common super class for all JSP tags rendering the view of a
 * {@link FormMember}.
 * 
 * <p>
 * This class provides convenient access (see {@link #getMember()}) to the
 * {@link FormMember}, which is the model of the currently rendered view.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractFormMemberTag extends AbstractFormTag implements FormMemberTag {

	/**
	 * The (local) name of the {@link #formMember}, for which this tag should
	 * render a view.
	 * 
	 * <p>
	 * The {@link #name} identifies the corresponding {@link #formMember} within
	 * its parent {@link FormContainer}. The parent {@link FormContainer}
	 * itself is identified by an ancestor {@link AbstractFormMemberTag}.
	 * </p>
	 * 
	 * @see FormMember#getName()
	 * @see FormMember#getQualifiedName()
	 */
	public final StringAttribute name = new StringAttribute();
	
	/** input tag style. */
	public final StringAttribute style = new StringAttribute();

	/**
	 * @see #getMember()
	 */
	private FormMember formMember;

    /** 
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString() {
        return (this.getClass().getName() + " ["
                + "name: '" + this.name
                + "']");
    }

    /**
	 * JSP tag setter for {@link #name}.
	 */
	public void setName(String value) {
		this.name.set(ExpressionSyntax.expandVariables(pageContext, value));
	}
	
	/** 
	 * Set the tag style
	 * 
	 * @param aStyle the style
	 */
	public void setStyle(String aStyle) {
		this.style.set(aStyle);
	}

	@Override
	public final FormMember getMember() { 
		return formMember;
	}
	
	@Override
	protected void setup() throws JspException {
		super.setup();
		
		// Lookup the form member that corresponds to this tag.
		this.formMember = FormContext.getMemberByRelativeName(getParentFormContainer(), this.name.get());
	}

	@Override
	protected void teardown() {
		super.teardown();
		
		this.name.reset();
		this.style.reset();
		this.formMember = null;
	}

}
