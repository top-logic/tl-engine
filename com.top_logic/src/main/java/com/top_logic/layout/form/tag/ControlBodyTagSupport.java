/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.IterationTag;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.control.AbstractCompositeControl;

/**
 * Static mix-in implementation of {@link ControlBodyTag} that uses the tag body
 * for adding a list of child {@link Control}s to a {@link AbstractCompositeControl}
 * without any additional structure.
 * 
 * <p>
 * The body of a {@link ControlBodyTagSupport} tag must only contain other
 * control-creating tags or white space.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ControlBodyTagSupport {

	/**
	 * Called from {@link BodyTag#doInitBody()} implementations of
	 * {@link ControlBodyTag}s.
	 */
	public static void doInitBody() throws JspException {
		// There is no initialization necessary.
	}
	
	/**
	 * Called from {@link IterationTag#doAfterBody()} implementations of
	 * {@link ControlBodyTag}s.
	 * 
	 * @param bodyContent
	 *        The {@link BodyContent} passed into the {@link ControlBodyTag}
	 *        through its {@link BodyTag#setBodyContent(BodyContent)} method.
	 * @return The result to return from {@link IterationTag#doAfterBody()}.
	 */
	public static int doAfterBody(BodyContent bodyContent) throws JspException {
		// Fetch the bodycontent as string to check that it at most contains
		// whitespace characters. The only valid action for tags within a
		// table's body is to register an additional control with the table
		// control. Such tags must not generate content, since this content is
		// ignored by this tag.
		if (bodyContent != null) {
			String bodyAsString = bodyContent.getString();
			assert bodyAsString.trim().length() == 0 : 
				"Only control-creating tags are allowed as content of a " + 
				ControlBodyTagSupport.class.getName() + " tag.";
		}
		return IterationTag.SKIP_BODY;
	}

}
