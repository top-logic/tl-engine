/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.maintenance;

import com.top_logic.layout.ReadOnlyAccessor;

/**
 * Accessor for table component of {@link MaintenanceComponent}.
 * 
 * <p>
 * Returns the part of a {@link String} after the last '/' or '\'. Is used to find the actual name
 * of the file given by a "qualified" file name.
 * </p>
 * 
 * <p>
 * E.g.: /jsp/administration/maintentance/Page.jsp -&gt; Page.jsp
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class EndSectionAccessor extends ReadOnlyAccessor<String> {

	@Override
	public Object getValue(String object, String property) {
		int endSectionStart = getEndSectionStart(object);
		if (endSectionStart == -1) {
			return object;
		}
		return object.substring(endSectionStart + 1);
	}

	private int getEndSectionStart(String object) {
		int lastSlash = object.lastIndexOf('/');
		int lastBackSlash = object.lastIndexOf('\\');
		return Math.max(lastSlash, lastBackSlash);
	}

}

