/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services.simpleajax;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;

/**
 * An action that is targeted towards a DOM node within the client-side
 * document.
 * 
 * <p>
 * The concrete subclass of {@link DOMAction} defines the semantic of of this
 * action.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class DOMAction extends ClientAction {

	/**
	 * @see #getElementID()
	 */
	String elementID;

	/**
	 * Creates a new {@link DOMAction} that is directed to the element
	 * identified by the given ID.
	 * 
	 * @param elementID See {@link #getElementID()}.
	 */
	public DOMAction(String elementID) {
		this.elementID = elementID;
		assert elementID != null : "Missing element ID.";
	}

	/**
	 * A reference to a DOM element that is already displayed on the clients
	 * page. This action is directed to the referenced DOM element. 
	 */
	public String getElementID() {
		return elementID;
	}

	@Override
	protected void writeChildrenAsXML(DisplayContext context, TagWriter writer) 
		throws IOException 
	{
		writer.beginTag(AJAXConstants.AJAX_ID_ELEMENT);
		writer.writeContent(elementID);
		writer.endTag(AJAXConstants.AJAX_ID_ELEMENT);
	}

}
