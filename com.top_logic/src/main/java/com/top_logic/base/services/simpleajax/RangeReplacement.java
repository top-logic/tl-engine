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
 * Replaces a range within the client's XML document with fresh contents.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RangeReplacement extends DOMModification {
	public static final String RANGE_REPLACEMENT_XSI_TYPE = "RangeReplacement";
	
	String stopID;
	
	/**
	 * Replaces the range that starts immediately before the element identified by
	 * <code>startID</code> and that stops immediately after the element identified by
	 * <code>stopID</code> with the given new contents, i.e. the first removed element is the
	 * element identified by <code>startID</code> and the last removed element is the element
	 * identified by <code>stopID</code>.
	 */
	public RangeReplacement(String startID, String stopID, HTMLFragment fragment) {
		super(startID, fragment);
		
		assert stopID != null : "Missing stop ID.";
		this.stopID = stopID;
	}

	@Override
	protected String getXSIType() {
		return RANGE_REPLACEMENT_XSI_TYPE;
	}

	@Override
	protected void writeChildrenAsXML(DisplayContext context, TagWriter writer) throws IOException {
		
		super.writeChildrenAsXML(context, writer);
		
		writer.beginTag(AJAXConstants.AJAX_STOP_ID_ELEMENT);
		writer.writeContent(stopID);
		writer.endTag(AJAXConstants.AJAX_STOP_ID_ELEMENT);
	}
}
