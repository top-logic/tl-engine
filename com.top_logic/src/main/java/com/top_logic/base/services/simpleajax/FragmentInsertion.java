/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services.simpleajax;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;

/**
 * Inserts new contents on the client's page relative to a given elemnt. 
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FragmentInsertion extends DOMModification {

	public static final String FRAGMENT_INSERTION_XSI_TYPE = "FragmentInsertion";
	/**
	 * @see #getPosition()
	 */
	private String position;

	/**
	 * Insert the given HTML fragment relative to the element identified by the
	 * given ID.
	 * 
	 * @param position See {@link #getPosition()}.
	 */
	public FragmentInsertion(String elementID, String position, HTMLFragment fragment) {
		super(elementID, fragment);
		
		this.position = position;
	}
	
	/**
	 * Determines, whether the fresh contents is inserted immediately before the
	 * given element, immediately after the given element, as first child of the
	 * given element or as last child of the given element.
	 * 
	 * @return Possible values are
	 *         {@link AJAXConstants#AJAX_POSITION_BEFORE_VALUE},
	 *         {@link AJAXConstants#AJAX_POSITION_AFTER_VALUE},
	 *         {@link AJAXConstants#AJAX_POSITION_START_VALUE} or
	 *         {@link AJAXConstants#AJAX_POSITION_END_VALUE},
	 */
	public String getPosition() {
		return position;
	}

	@Override
	protected String getXSIType() {
		return FRAGMENT_INSERTION_XSI_TYPE;
	}
	
	@Override
	protected void writeFieldsAsXML(TagWriter out) throws IOException {
		super.writeFieldsAsXML(out);
		
		out.writeAttribute(AJAXConstants.AJAX_POSITION_ATTRIBUTE, position);
	}
}
