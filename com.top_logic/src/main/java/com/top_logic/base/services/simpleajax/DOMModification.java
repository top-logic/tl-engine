/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services.simpleajax;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.RenderErrorUtil;

/**
 * Abstract super class of all client actions that transport new contents to the
 * client.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class DOMModification extends DOMAction {

	/**
	 * The new contents that is transported to the client.
	 */
	HTMLFragment fragment;
	
	/**
	 * @see #elementID
	 * @see #fragment
	 */
	public DOMModification(String elementID, HTMLFragment fragment) {
		super(elementID);
		
		assert fragment != null : "Missing fragment.";
		this.fragment = fragment;
	}

	@Override
	protected void writeChildrenAsXML(DisplayContext context, TagWriter writer) throws IOException 
	{
		super.writeChildrenAsXML(context, writer);
		
		writer.beginTag(AJAXConstants.AJAX_FRAGMENT_ELEMENT);
		writer.beginQuotedXML();
		int currentDepth = writer.getDepth();
		try {
			fragment.write(context, writer);
		} catch (Throwable throwable) {
			writer.endAll(currentDepth);
			RenderErrorUtil.produceErrorOutput(context, writer, I18NConstants.RENDERING_ERROR,
				"Error occured during rendering of update fragment.", throwable, this);
		}
		writer.endQuotedXML();
		writer.endTag(AJAXConstants.AJAX_FRAGMENT_ELEMENT);
	}
}
