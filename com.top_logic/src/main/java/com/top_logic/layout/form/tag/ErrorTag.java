/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.Tag;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.ErrorControl;

/**
 * View of an error message associated with a {@link FormMember} either rendered
 * as plain text or error icon with a tool-tip text.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ErrorTag extends AbstractFormFieldControlTag {

    private boolean useIcon = true;

	public void setIcon(boolean useIcon) {
		this.useIcon = useIcon;
	}

	@Override
	protected int startFormMember() throws IOException, JspException {
		if (!(getMember() instanceof FormField)) {
			/* Do not write anything when member is not a form field. This may happen, e.g. when
			 * iterating over a group and for each member then actual tag and an error tag is
			 * written. */
			return Tag.SKIP_BODY;
		}
		return super.startFormMember();
	}

	@Override
	public Control createControl(FormMember member, String displayStyle) {
		return new ErrorControl(member, useIcon);
	}
	
	@Override
	protected void teardown() {
		super.teardown();
		useIcon = true;
	}
}
