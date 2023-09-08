/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import javax.servlet.jsp.tagext.Tag;

import com.top_logic.layout.form.tag.ErrorTag;


/**
 * Error tag that operates with the same basic parameters as the {@link MetaInputTag}.
 * 
 * @author <a href="mailto:kbu@top-logic.com>Karsten Buch</a>
 */
public class MetaErrorTag extends AbstractMetaTag {

	private boolean useIcon = true;

	public void setIcon(boolean useIcon) {
		this.useIcon = useIcon;
	}
	
	@Override
	protected Tag createImplementation() {
    	ErrorTag tag = new ErrorTag();

    	tag.setName(getFieldName());
    	tag.setIcon(useIcon);
    	
		return tag;
	}
	
	@Override
	protected void teardown() {
		super.teardown();
		useIcon = true;
	}
}
