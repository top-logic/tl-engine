/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services.simpleajax;

/**
 * An action that replaces the contents of an element that is already displayed
 * on the client's page with new contents.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ContentReplacement extends DOMModification {
	public static final String CONTENT_REPLACEMENT_XSI_TYPE = "ContentReplacement";

	/**
	 * The given fragment replaces the contents of the element identified by the
	 * given ID.
	 */
	public ContentReplacement(String elementID, HTMLFragment fragment) {
		super(elementID, fragment);
	}

	@Override
	protected String getXSIType() {
		return CONTENT_REPLACEMENT_XSI_TYPE;
	}
}
