/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;

import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;

/**
 * The class {@link ScopeTag} is a tag to define a cope for a {@link FormContainer}. It does not do
 * anything but allows child tags to access there {@link FormMember} by its name relative to its
 * parent container instead of its name relative to the parent of its parent container.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ScopeTag extends AbstractFormMemberTag implements FormContainerTag {

	@Override
	protected int startFormMember() throws IOException, JspException {
		return EVAL_BODY_INCLUDE;
	}

	@Override
	protected int endFormMember() throws IOException, JspException {
		return EVAL_PAGE;
	}

	@Override
	public FormContainer getFormContainer() {
		return (FormContainer) getMember();
	}

}
