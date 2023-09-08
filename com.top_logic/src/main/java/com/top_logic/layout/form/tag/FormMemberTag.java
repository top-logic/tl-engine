/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import javax.servlet.jsp.tagext.Tag;

import com.top_logic.layout.form.FormMember;

/**
 * Common interface for all JSP tags rendering the view of a {@link FormMember}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface FormMemberTag extends Tag {

	/**
	 * Returns the {@link FormTag}, which renders the top-level view of the
	 * current form.
	 */
	FormTag getFormTag();
	
	/**
	 * The {@link FormMember}, for which this tag should render a view.
	 */
	FormMember getMember();

}
