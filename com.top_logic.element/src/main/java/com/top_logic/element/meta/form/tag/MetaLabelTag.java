/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import javax.servlet.jsp.tagext.Tag;

import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.layout.form.tag.LabelTag;
import com.top_logic.layout.form.tag.util.StringAttribute;


/**
 * Label tag that fetches part of its information from a {@link TLStructuredTypePart}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class MetaLabelTag extends AbstractMetaTag {
	
	private boolean colon;
	
	/** input tag style. */
	public final StringAttribute style = new StringAttribute();

	@Override
	protected Tag createImplementation() {
		LabelTag tag = new LabelTag();
		tag.setColon(this.colon);
		if (this.style.isSet()) {
			tag.setStyle(this.style.get());
		}
		tag.setName(getFieldName());

		return tag;
	}

	/** 
	 * Set if colon should be added
	 * 
	 * @param aColon if true add a colon to the label text
	 */
	public void setColon(boolean aColon) {
		this.colon = aColon;
	}
	
	/** 
	 * @see com.top_logic.layout.form.tag.AbstractProxyTag#teardown()
	 */
	@Override
	protected void teardown() {
		super.teardown();
		
		this.colon = false;
		this.style.reset();
	}

	/** 
	 * Set the tag style
	 * 
	 * @param aStyle the style
	 */
	public void setStyle(String aStyle) {
		this.style.set(aStyle);
	}
	
}
