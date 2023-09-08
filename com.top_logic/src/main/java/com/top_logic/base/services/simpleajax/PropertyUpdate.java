/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services.simpleajax;

import java.io.IOException;

import com.top_logic.basic.Logger;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DynamicText;
import com.top_logic.mig.html.HTMLUtil;

/**
 * {@link ClientAction} that sets a JavaScript property or a HTML5 data attribute on a client-side
 * DOM element identified by an ID.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PropertyUpdate extends DOMAction {

	/**
	 * The {@link #getXSIType()} of {@link PropertyUpdate}s.
	 */
	public static final String PROPERTY_UPDATE_XSI_TYPE = "PropertyUpdate";

	private final String property;

	private final DynamicText value;

	/**
	 * Creates a {@link PropertyUpdate}.
	 *
	 * @param elementID
	 *        See {@link #getElementID()}.
	 * @param property
	 *        The name of the property to update.
	 * @param value
	 *        The new value of the property.
	 */
	public PropertyUpdate(String elementID, String property, DynamicText value) {
		super(elementID);
		this.property = property;
		this.value = value;
	}

    /**
     * Return some reasonable String for debugging or logging.
     */
    @Override
	public String toString() {
        return "PropertyUpdate[" + property + "=" + value + "]";
    }

    @Override
	protected String getXSIType() {
		return PROPERTY_UPDATE_XSI_TYPE;
	}
	
	@Override
	protected void writeChildrenAsXML(DisplayContext context, TagWriter writer) throws IOException {
		super.writeChildrenAsXML(context, writer);
		
		writer.beginBeginTag(AJAXConstants.AJAX_PROPERTY);
		writer.writeAttribute(AJAXConstants.NAME_ATTRIBUTE, property);
		int currentDepth = writer.getDepth();
		try {
			HTMLUtil.writeAttribute(context, writer, AJAXConstants.VALUE_ATTRIBUTE, value);
			writer.endEmptyTag();
		} catch (Throwable throwable) {
			writer.endAll(currentDepth);
			writer.endTag(AJAXConstants.AJAX_PROPERTY);
			Logger.error("Could not create property update (Cause: " + throwable.getMessage(), throwable, this);
		}
	}

}
