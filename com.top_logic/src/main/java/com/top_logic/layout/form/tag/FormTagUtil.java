/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import jakarta.servlet.jsp.tagext.Tag;
import jakarta.servlet.jsp.tagext.TagSupport;

import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;

/**
 * Utilities for {@link Tag}s displaying {@link FormMember}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FormTagUtil {

	public static FormTag findFormTag(Tag self) {
		return (FormTag) TagSupport.findAncestorWithClass(self, FormTag.class);
	}

	/**
	 * The {@link FormContainer} of the first {@link FormContainerTag} ancestor of the given tag.
	 */
	public static FormContainer findParentFormContainer(Tag self) {
		FormContainerTag parentContainerTag = (FormContainerTag) TagSupport.findAncestorWithClass(self, FormContainerTag.class);
		return parentContainerTag.getFormContainer();
	}

}
