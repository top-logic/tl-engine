/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider;

import java.util.Collection;

import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.model.TLObject;

/**
 * Utilities for displaying {@link WebFolder} values in a table.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class WebFolderTableUtil {

	/**
	 * Calculate the number of children in a web folder.
	 * 
	 * @param aValue
	 *        The object to get the value from, may be <code>null</code>.
	 * @return The requested number (children for a {@link WebFolder} instance), never
	 *         <code>null</code>.
	 */
	protected static Integer getWebFolderCount(Object aValue) {
		if (aValue instanceof WebFolder) {
			WebFolder theFolder = (WebFolder) aValue;
			Collection<? extends TLObject> theChildren = theFolder.getContent();
	
			if (theChildren != null) {
				return theChildren.size();
			}
		} else if (aValue instanceof Number) {
			return Integer.valueOf(((Number) aValue).intValue());
		}
	
		return 0;
	}

}
